package com.jamie.express.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.jamie.express.R;
import com.jamie.express.activities.CommentActivity;
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
public class ToCommentsFragment extends Fragment {

    public static final String TAG = "ToCommentsFragment";

    private ExpressSharedPreference preference;
    private List<Map<String, Object>> listData;
    private RefreshableListView listView;
    private ToCommentsAdapter adapter;

    private static final int AVAILABLE = 1;
    private static final int LOCKED = -1;
    private int GET_DATA_TASK_STATE;
    private int page;
    private boolean endData;

    public ToCommentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listData = new ArrayList<Map<String, Object>>();
        adapter = new ToCommentsAdapter(getActivity(), listData);
        page = 1;
        endData = false;
        GET_DATA_TASK_STATE = AVAILABLE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_to_comments, container, false);
        preference = new ExpressSharedPreference(getActivity());
        listView = (RefreshableListView) view.findViewById(R.id.ftc_rlv_main);
        listView.setOnRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadData() {
                if (!endData && GET_DATA_TASK_STATE == AVAILABLE) {
                    new ToCommentsTask().execute();
                }
            }
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), CommentActivity.class);
                Bundle args = new Bundle();
                Log.i(TAG, listData.get((int) id).toString());
                int currPosition = (int) id;
                args.putInt(UsedFields.DBCreditRecord.APPLY_ID, Integer.valueOf(listData.get(currPosition).get(UsedFields.ID).toString()));
                args.putInt(UsedFields.DBCreditRecord.EX_ID, Integer.valueOf(listData.get(currPosition).get(UsedFields.DBApplyRecord.EX_ID).toString()));
                args.putInt(UsedFields.DBCreditRecord.TYPE, Integer.valueOf(listData.get(currPosition).get(UsedFields.DBCreditRecord.TYPE).toString()));
                if (listData.get(currPosition).get(UsedFields.DBApplyRecord.EX_APPLYER).toString().trim().equals(preference.getUserId())) {
                    args.putString(UsedFields.DBCreditRecord.TO_USER, listData.get(currPosition).get(UsedFields.DBApplyRecord.EX_OWNER).toString());
                    args.putString("toUserName", listData.get(currPosition).get(UsedFields.DBApplyRecord._OWNER_NAME).toString());
                    args.putBoolean("toWhom", false);
                } else {
                    args.putString(UsedFields.DBCreditRecord.TO_USER, listData.get(currPosition).get(UsedFields.DBApplyRecord.EX_APPLYER).toString());
                    args.putString("toUserName", listData.get(currPosition).get(UsedFields.DBApplyRecord._APPLYER_NAME).toString());
                    args.putBoolean("toWhom", true);
                }
                intent.putExtra("credit", args);
                getActivity().startActivity(intent);
            }
        });
        new ToCommentsTask().execute();
        return view;
    }

    private void refresh() {
        listData.clear();
        page = 1;
        endData = false;
        new ToCommentsTask().execute();
    }

    private class ToCommentsTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            GET_DATA_TASK_STATE = LOCKED;
            HashMap<String, Object> paramList = new HashMap<String, Object>();
            paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
            paramList.put("page", page);
            return HttpUtil.getData(RequestUrl.TO_COMMENTS_URL, paramList);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            GET_DATA_TASK_STATE = AVAILABLE;
            listView.onRefreshComplete();
            if (jsonObject == null) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                return;
            } else {
                try {
                    int resultCode = jsonObject.getInt("code");
                    if (resultCode == HttpUtil.NO_DATA) {
                        endData = true;
                        Toast.makeText(getActivity().getApplicationContext(), R.string.no_data, Toast.LENGTH_SHORT).show();
                        return;
                    } else if (resultCode == HttpUtil.SUCCESS) {
                        JSONArray commentsArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < commentsArray.length(); i++) {
                            JSONObject item = commentsArray.getJSONObject(i);
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
                        if (commentsArray.length() < 10) {
                            endData = true;
                            Toast.makeText(getActivity().getApplicationContext(), R.string.no_data, Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ToCommentsAdapter extends CommonAdapter {

        public ToCommentsAdapter(Context context, List<Map<String, Object>> list) {
            super(context, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.ftc_rlv_item, parent, false);
            int disabled = Integer.valueOf(list.get(position).get(UsedFields.DBApplyRecord.DISABLED).toString());
            int expired = Integer.valueOf(list.get(position).get(UsedFields.DBApplyRecord.EXPIRED).toString());
            int missionDone = Integer.valueOf(list.get(position).get(UsedFields.DBApplyRecord.MISSION_DONE).toString());
            TextView tvAddr = (TextView) view.findViewById(R.id.ftc_tv_addr);
            TextView tvSubstance = (TextView) view.findViewById(R.id.ftc_tv_substance);
            TextView tvDeadline = (TextView) view.findViewById(R.id.ftc_tv_deadline);
            TextView tvCreateTime = (TextView) view.findViewById(R.id.ftc_tv_create_time);
            TextView tvReward = (TextView) view.findViewById(R.id.ftc_tv_reward);
            TextView tvState = (TextView) view.findViewById(R.id.ftc_tv_state);
            if (list.get(position).get(UsedFields.DBApplyRecord.EX_APPLYER).toString().trim().equals(preference.getUserId())) {
                view.setBackgroundResource(R.color.light_green);
                if (disabled == 1) {
                    tvState.setText(R.string.state_disabled);
                    list.get(position).put(UsedFields.DBCreditRecord.TYPE, 5);
                } else if (expired == 1) {
                    tvState.setText(R.string.state_expired);
                    list.get(position).put(UsedFields.DBCreditRecord.TYPE, 6);
                } else if (missionDone == 1) {
                    tvState.setText(R.string.state_done);
                    list.get(position).put(UsedFields.DBCreditRecord.TYPE, 4);
                } else {
                    tvState.setText(R.string.state_error);
                    list.get(position).put(UsedFields.DBCreditRecord.TYPE, 0);
                    return view;
                }
            } else {
                if (disabled == 1) {
                    tvState.setText(R.string.state_disabled);
                    list.get(position).put(UsedFields.DBCreditRecord.TYPE, 2);
                } else if (expired == 1) {
                    tvState.setText(R.string.state_expired);
                    list.get(position).put(UsedFields.DBCreditRecord.TYPE, 3);
                } else if (missionDone == 1) {
                    tvState.setText(R.string.state_done);
                    list.get(position).put(UsedFields.DBCreditRecord.TYPE, 1);
                } else {
                    tvState.setText(R.string.state_error);
                    list.get(position).put(UsedFields.DBCreditRecord.TYPE, 0);
                    return view;
                }
            }
            tvAddr.setText("快递地址：" + list.get(position).get(UsedFields.DBApplyRecord._EX_ADDR).toString());
            tvSubstance.setText("快递包含：" + list.get(position).get(UsedFields.DBApplyRecord._EX_SUBSTANCE).toString());
            tvDeadline.setText("截止时间：" + CommonFunction.formatDateTime(list.get(position).get(UsedFields.DBApplyRecord._EX_DEADLINE).toString()));
            tvCreateTime.setText("领取时间：" + CommonFunction.formatDateTime(list.get(position).get(UsedFields.DBApplyRecord.CREATE_TIME).toString()));
            tvReward.setText("报酬：" + list.get(position).get(UsedFields.DBApplyRecord._EX_REWARD).toString());
            return view;
        }
    }

}
