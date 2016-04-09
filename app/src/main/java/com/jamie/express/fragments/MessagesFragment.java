package com.jamie.express.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jamie.express.R;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {

    public static final String TAG = "MessagesFragment";

    private ExpressSharedPreference preference;
    private List<Map<String, Object>> listData;
    private RefreshableListView listView;
    private MessagesAdapter adapter;

    private static final int AVAILABLE = 1;
    private static final int LOCKED = -1;
    private int GET_DATA_TASK_STATE;
    private int page;
    private boolean endData;

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listData = new ArrayList<Map<String, Object>>();
        adapter = new MessagesAdapter(getActivity(), listData);
        page = 1;
        endData = false;
        GET_DATA_TASK_STATE = AVAILABLE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        preference = new ExpressSharedPreference(getActivity());
        listView = (RefreshableListView) view.findViewById(R.id.fm_rlv_main);
        listView.setOnRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadData() {
                if (!endData && GET_DATA_TASK_STATE == AVAILABLE) {
                    new GetMessagesTask().execute();
                }
            }
        });
        listView.setAdapter(adapter);
        new GetMessagesTask().execute();
        return view;
    }

    private void refresh() {
        listData.clear();
        page = 1;
        endData = false;
        new GetMessagesTask().execute();
    }

    private class GetMessagesTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            GET_DATA_TASK_STATE = LOCKED;
            HashMap<String, Object> paramList = new HashMap<String, Object>();
            paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
            paramList.put("page", page);
            return HttpUtil.getData(RequestUrl.GET_USER_MESSAGES_URL, paramList);
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
                        JSONArray messagesArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < messagesArray.length(); i++) {
                            JSONObject item = messagesArray.getJSONObject(i);
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
                        if (messagesArray.length() < 10) {
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

    private class MessagesAdapter extends CommonAdapter {

        public MessagesAdapter(Context context, List<Map<String, Object>> list) {
            super(context, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.fm_rlv_item, parent, false);
            final ImageView ivHead = (ImageView) view.findViewById(R.id.fm_iv_head);
            TextView tvName = (TextView) view.findViewById(R.id.fm_tv_name);
            TextView tvMessage = (TextView) view.findViewById(R.id.fm_tv_message);
            TextView tvCreateTime = (TextView) view.findViewById(R.id.fm_tv_create_time);
            tvName.setText(list.get(position).get(UsedFields.DBPush.CREATE_NAME).toString());
            tvMessage.setText("内容：" + list.get(position).get(UsedFields.DBPush.MSG).toString());
            tvCreateTime.setText("时间：" + CommonFunction.formatDateTime(list.get(position).get(UsedFields.DBPush.CREATE_TIME).toString()));
            ImageLoader.loadBitmap(RequestUrl.IMG_URL + list.get(position).get(UsedFields.DBPush.CREATE_ICON).toString(), new ImageLoader.BitmapCallback() {
                @Override
                public void onGotBitmap(Bitmap bitmap) {
                    ivHead.setScaleType(ImageView.ScaleType.FIT_XY);
                    ivHead.setImageBitmap(bitmap);
                }
            });
            return view;
        }
    }

}
