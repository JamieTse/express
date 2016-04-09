package com.jamie.express.fragments;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.jamie.express.R;
import com.jamie.express.activities.ViewDetailActivity;
import com.jamie.express.adapters.CommonAdapter;
import com.jamie.express.listviews.RefreshableListView;
import com.jamie.express.utils.CommonFunction;
import com.jamie.express.utils.ExpressSharedPreference;
import com.jamie.express.utils.HttpUtil;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class PostRecordFragment extends Fragment {

    public static final String TAG = "PostRecordFragment";

    private ExpressSharedPreference preference;
    private List<Map<String, Object>> listData;
    private RefreshableListView listView;
    private PostRecordAdapter adapter;

    private int page;
    private boolean endData;

    private static final int AVAILABLE = 1;
    private static final int LOCKED = -1;
    private int GET_DATA_TASK_STATE;

    public PostRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listData = new ArrayList<Map<String, Object>>();
        adapter = new PostRecordAdapter(getActivity(), listData);
        page = 1;
        endData = false;
        GET_DATA_TASK_STATE = AVAILABLE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_record, container, false);
        preference = new ExpressSharedPreference(getActivity());
        listView = (RefreshableListView) view.findViewById(R.id.fpr_rlv_main);
        listView.setOnRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadData() {
                if (!endData && GET_DATA_TASK_STATE == AVAILABLE) {
                    new GetPostsTask().execute();
                }
            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = listData.get((int) id);
                Log.i(TAG, map.toString());
                if (Integer.valueOf(map.get(ViewDetailActivity.STATE).toString()) != ViewDetailActivity.STATE_ERROR) {
                    Bundle args = new Bundle();
                    for (String key : map.keySet()) {
                        if (key.equals(UsedFields.ID) || key.equals(ViewDetailActivity.STATE)) {
                            args.putInt(key, Integer.valueOf(map.get(key).toString()));
                        } else if (key.equals(ViewDetailActivity.IS_APPLYER)) {
                            args.putBoolean(key, Boolean.valueOf(map.get(key).toString()));
                        } else {
                            args.putString(key, String.valueOf(map.get(key)));
                        }
                    }
                    Intent intent = new Intent(getActivity(), ViewDetailActivity.class);
                    intent.putExtra(ViewDetailActivity.BUNDLE_NAME, args);
                    startActivity(intent);
                }
            }
        });
        new GetPostsTask().execute();
        return view;
    }

    private void refresh() {
        listData.clear();
        page = 1;
        endData = false;
        new GetPostsTask().execute();
    }

    private class GetPostsTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            GET_DATA_TASK_STATE = LOCKED;
            HashMap<String, Object> paramList = new HashMap<String, Object>();
            paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
            paramList.put("page", page);
            return HttpUtil.getData(RequestUrl.GET_POST_RECORD_URL, paramList);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            GET_DATA_TASK_STATE = AVAILABLE;
            listView.onRefreshComplete();
            if (jsonObject == null) {
                Toast.makeText(getActivity().getApplicationContext(), "网络出错", Toast.LENGTH_SHORT).show();
                return;
            } else {
                try {
                    int resultCode = jsonObject.getInt("code");
                    if (resultCode == HttpUtil.NO_DATA) {
                        endData = true;
                        Toast.makeText(getActivity().getApplicationContext(), "没有更多记录", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (resultCode == HttpUtil.SUCCESS) {
                        JSONArray postsArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < postsArray.length(); i++) {
                            JSONObject item = postsArray.getJSONObject(i);
                            Map<String, Object> map = new HashMap<String, Object>();
                            Iterator<String> iterator = item.keys();
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                if (key.equals(UsedFields.ID)) {
                                    map.put(key, item.getInt(key));
                                } else {
                                    map.put(key, item.getString(key));
                                }
                            }
                            listData.add(map);
                        }
                        page += 1;
                        if (postsArray.length() < 10) {
                            endData = true;
                            Toast.makeText(getActivity().getApplicationContext(), "没有更多记录", Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PostRecordAdapter extends CommonAdapter {

        public PostRecordAdapter(Context context, List<Map<String, Object>> list) {
            super(context, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.fpr_rlv_item, parent, false);
            TextView tvAddr = (TextView) view.findViewById(R.id.fpr_tv_addr);
            TextView tvSubstance = (TextView) view.findViewById(R.id.fpr_tv_substance);
            TextView tvDeadline = (TextView) view.findViewById(R.id.fpr_tv_deadline);
            TextView tvCreateTime = (TextView) view.findViewById(R.id.fpr_tv_create_time);
            TextView tvReward = (TextView) view.findViewById(R.id.fpr_tv_reward);
            TextView tvApplied = (TextView) view.findViewById(R.id.fpr_tv_applied);
            tvAddr.setText("快递地址：" + list.get(position).get(UsedFields.DBExpressage.ADDR).toString());
            tvSubstance.setText("快递包含：" + list.get(position).get(UsedFields.DBExpressage.SUBSTANCE).toString());
            tvDeadline.setText("截止时间：" + CommonFunction.formatDateTime(list.get(position).get(UsedFields.DBExpressage.DEADLINE).toString()));
            tvCreateTime.setText("发布时间：" + CommonFunction.formatDateTime(list.get(position).get(UsedFields.DBExpressage.CREATE_TIME).toString()));
            tvReward.setText("报酬：" + list.get(position).get(UsedFields.DBExpressage.REWARD).toString() + "元");
            int applyID = Integer.valueOf(list.get(position).get(UsedFields.DBExpressage.APPLY_ID).toString());
            list.get(position).put(ViewDetailActivity.IS_APPLYER, false);
            if (applyID == 0) {
                tvApplied.setText("状态：未领取");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_NOT_YET);
            } else if (Integer.valueOf(list.get(position).get(UsedFields.DBExpressage._EX_MISSION_DONE).toString()) == 1) {
                tvApplied.setText("状态：已收到");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_DONE);
            } else if (Integer.valueOf(list.get(position).get(UsedFields.DBExpressage._EX_EXPIRED).toString()) == 1) {
                tvApplied.setText("状态：已过期");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_EXPIRED);
            } else if (Integer.valueOf(list.get(position).get(UsedFields.DBExpressage._APPLY_DISABLED).toString()) == 1) {
                tvApplied.setText("状态：已取消");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_DISABLED);
            } else {
                tvApplied.setText("状态：待完成");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_NOT_DONE);
            }
            return view;
        }
    }

}
