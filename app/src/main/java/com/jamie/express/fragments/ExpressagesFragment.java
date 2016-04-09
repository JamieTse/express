package com.jamie.express.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jamie.express.R;
import com.jamie.express.activities.ViewExpressageActivity;
import com.jamie.express.adapters.CommonAdapter;
import com.jamie.express.listviews.RefreshableListView;
import com.jamie.express.utils.CommonFunction;
import com.jamie.express.utils.ExpressSharedPreference;
import com.jamie.express.utils.HttpUtil;
import com.jamie.express.utils.ImageLoader;
import com.jamie.express.utils.RequestUrl;
import com.jamie.express.utils.UsedFields;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExpressagesFragment extends Fragment {

    public static final String TAG = "ExpressagesFragment";
    public static ExpressagesFragment expressagesFragment;

    private RefreshableListView listView;
    private ListViewAdapter adapter;
    private List<Map<String, Object>> list;
    private FloatingActionButton fabSearch;
    private SearchDialog dialog;

    private ExpressSharedPreference preference;
    private int page;
    private boolean endData;

    private static final int AVAILABLE = 1;
    private static final int LOCKED = -1;
    private int GET_DATA_TASK_STATE;
    public static final int VIEW_REQUEST = 1000;

    public static ExpressagesFragment getInstance() {
        if (expressagesFragment == null) {
            expressagesFragment = new ExpressagesFragment();
        }
        return expressagesFragment;
    }

    public ExpressagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = 1;
        endData = false;
        GET_DATA_TASK_STATE = AVAILABLE;
        list = new ArrayList<Map<String, Object>>();
        adapter = new ListViewAdapter(getActivity(), list);
        dialog = new SearchDialog(getActivity());
        dialog.setTitle("设置筛选条件");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_expressages, container, false);
        listView = (RefreshableListView) view.findViewById(R.id.fe_rlv_main);
        fabSearch = (FloatingActionButton) view.findViewById(R.id.fe_fab_search);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
        listView.setOnRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadData() {
                if (!endData && GET_DATA_TASK_STATE == AVAILABLE) {
                    new GetDataTask().execute();
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = new Bundle();
                //JSONObject jsonObject = new JSONObject();
                //position-1是因为加了下拉刷新的头部,用id则不需要-1
                Map<String, Object> map = list.get((int) id);
                for (String key : map.keySet()) {
                    if (key.equals(UsedFields.ID)) {
                        args.putInt(key, Integer.valueOf(map.get(key).toString()));
                    } else {
                        args.putString(key, String.valueOf(map.get(key)));
                    }
                }
                Intent intent = new Intent(getActivity(), ViewExpressageActivity.class);
                intent.putExtra("expressage", args);
                intent.putExtra("listID", id);
                //startActivity(intent);
                startActivityForResult(intent, VIEW_REQUEST);
            }
        });
        listView.setAdapter(adapter);
        preference = new ExpressSharedPreference(getActivity());
        new GetDataTask().execute();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIEW_REQUEST && resultCode == ViewExpressageActivity.DID_HELP) {
            long listID = data.getLongExtra("listID", -1);
            if (listID != -1) {
                list.remove((int) listID);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void refresh() {
        page = 1;
        endData = false;
        new GetDataTask().execute();
        adapter.refreshData();
        //adapter.notifyDataSetChanged();
    }

    private class SearchDialog extends Dialog {

        private Button btnPositive;
        private Button btnNegative;
        private GridView gvMyTags;
        private GridView gvAllTags;
        private List<Map<String, Object>> listMyTags;
        private List<Map<String, Object>> listAllTags;
        private SimpleAdapter myTagsAdapter;
        private SimpleAdapter allTagsAdapter;

        public SearchDialog(Context context) {
            super(context);
            initSearchDialog();
        }

        private void initSearchDialog() {
            View main = LayoutInflater.from(getContext()).inflate(R.layout.fe_search_dialog, null);
            btnPositive = (Button) main.findViewById(R.id.fe_search_btnPositive);
            btnNegative = (Button) main.findViewById(R.id.fe_search_btnNegative);
            gvMyTags = (GridView) main.findViewById(R.id.fe_gv_myTag);
            gvAllTags = (GridView) main.findViewById(R.id.fe_gv_allTag);
            listMyTags = new ArrayList<Map<String, Object>>();
            listAllTags = new ArrayList<Map<String, Object>>();
            myTagsAdapter = new SimpleAdapter(getActivity(), listMyTags, R.layout.gv_tags_cell_pink, new String[]{"label_title"}, new int[]{R.id.gv_tags_item_pink});
            allTagsAdapter = new SimpleAdapter(getActivity(), listAllTags, R.layout.gv_tags_cell_green, new String[]{"label_title"}, new int[]{R.id.gv_tags_item_green});
            gvMyTags.setAdapter(myTagsAdapter);
            gvAllTags.setAdapter(allTagsAdapter);
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    preference.setSearchCondition(listMyTags);
                    refresh();
                    dialog.dismiss();
                }
            });
            btnNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            gvMyTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, Object> map = listMyTags.get(position);
                    listAllTags.add(map);
                    listMyTags.remove(position);
                    allTagsAdapter.notifyDataSetChanged();
                    myTagsAdapter.notifyDataSetChanged();
                }
            });
            gvAllTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, Object> map = listAllTags.get(position);
                    listMyTags.add(map);
                    listAllTags.remove(position);
                    allTagsAdapter.notifyDataSetChanged();
                    myTagsAdapter.notifyDataSetChanged();
                }
            });
            super.setContentView(main);
        }

        @Override
        public void show() {
            super.show();
            new AsyncTask<Void, Void, JSONObject>() {

                Set<Integer> currCondition;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    //TODO
                    listAllTags.clear();
                    listMyTags.clear();
                    currCondition = preference.getSearchConditionSet();
                }

                @Override
                protected JSONObject doInBackground(Void... params) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    return HttpUtil.getData(RequestUrl.GET_ALL_TAGS_URL, map);
                }

                @Override
                protected void onPostExecute(JSONObject jsonObject) {
                    if (jsonObject == null) {
                        Toast.makeText(getActivity(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        return;
                    }
                    try {
                        if (jsonObject.getInt("code") == HttpUtil.SUCCESS) {
                            JSONArray tags = jsonObject.getJSONArray("data");
                            for (int i = 0; i < tags.length(); i++) {
                                JSONObject item = tags.getJSONObject(i);
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                int id = Integer.valueOf(item.get(UsedFields.ID).toString());
                                map.put(UsedFields.ID, item.get(UsedFields.ID));
                                map.put("label_title", item.get("label_title"));
                                if (currCondition.contains(id)) {
                                    listMyTags.add(map);
                                } else {
                                    listAllTags.add(map);
                                }
                            }
                            allTagsAdapter.notifyDataSetChanged();
                            return;
                        } else {
                            Toast.makeText(getActivity(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    super.onPostExecute(jsonObject);
                }
            }.execute();
        }
    }

    private class GetDataTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            GET_DATA_TASK_STATE = LOCKED;
            HashMap<String, Object> paramList = new HashMap<String, Object>();
            paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
            paramList.put("page", page);
            paramList.put(UsedFields.DBExpressage._LABEL_IDS, preference.getSearchCondition());
            return HttpUtil.getData(RequestUrl.GET_EXPRESSAGES_URL, paramList);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            listView.onRefreshComplete();
            if (jsonObject == null) {
                Toast.makeText(getActivity(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                GET_DATA_TASK_STATE = AVAILABLE;
                return;
            }
            try {
                int responseCode = jsonObject.getInt("code");
                if (responseCode == HttpUtil.FAILED) {
                    Toast.makeText(getActivity(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                    GET_DATA_TASK_STATE = AVAILABLE;
                    return;
                } else if (responseCode == HttpUtil.SUCCESS) {
                    JSONArray results = jsonObject.getJSONArray("data");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject item = results.getJSONObject(i);
                        Map<String, Object> map = new HashMap<String, Object>();
                        Iterator<String> iterator = item.keys();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            Object value = item.get(key);
                            map.put(key, value);
                        }
                        list.add(map);
                    }
                    page = page + 1;
                    if (results.length() < 10) {
                        endData = true;
                        //CONVERT_CACHE == false
                        Snackbar.make(listView, R.string.no_data, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    }
                    adapter.notifyDataSetChanged();
                    GET_DATA_TASK_STATE = AVAILABLE;
                    return;
                } else if (responseCode == HttpUtil.NO_DATA) {
                    endData = true;
                    Snackbar.make(listView, R.string.no_data, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                    GET_DATA_TASK_STATE = AVAILABLE;
                    return;
                } else {
                    Toast.makeText(getActivity(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                    GET_DATA_TASK_STATE = AVAILABLE;
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class ListViewAdapter extends CommonAdapter {

        public ListViewAdapter(Context context, List<Map<String, Object>> list) {
            super(context, list);
        }

        public void refreshData() {
            list.clear();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
//            if (convertView != null || CONVERT_CACHE == true) {
//                view = convertView;
//            } else {
            view = inflater.inflate(R.layout.fe_rlv_item, null);
            TextView tvAddr = (TextView) view.findViewById(R.id.fe_tv_addr);
            TextView tvSub = (TextView) view.findViewById(R.id.fe_tv_sub);
            TextView tvDeadline = (TextView) view.findViewById(R.id.fe_tv_deadline);
            TextView tvReward = (TextView) view.findViewById(R.id.fe_tv_reward);
            tvAddr.setText("从" + list.get(position).get(UsedFields.DBExpressage.ADDR).toString() + "送到" + list.get(position).get(UsedFields.DBExpressage._USER_ADDR).toString());
            tvSub.setText("快递包含:" + list.get(position).get(UsedFields.DBExpressage.SUBSTANCE).toString());
            tvDeadline.setText("截至" + CommonFunction.formatDateTime(list.get(position).get(UsedFields.DBExpressage.DEADLINE).toString()));
            tvReward.setText("报酬:￥" + list.get(position).get(UsedFields.DBExpressage.REWARD).toString() + "元");
            final ImageView ivHead = (ImageView) view.findViewById(R.id.fe_iv_head);
            ivHead.setScaleType(ImageView.ScaleType.FIT_XY);
            ImageLoader.loadBitmap(RequestUrl.IMG_URL + list.get(position).get(UsedFields.DBExpressage._USER_ICON).toString(), new ImageLoader.BitmapCallback() {
                @Override
                public void onGotBitmap(Bitmap bitmap) {
                    ivHead.setImageBitmap(bitmap);
                }
            });
//            }
            return view;
        }
    }

}
