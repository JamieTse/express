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
public class ApplyRecordFragment extends Fragment {

    public static final String TAG = "ApplyRecordFragment";

    private ExpressSharedPreference preference;
    private List<Map<String, Object>> listData;
    private RefreshableListView listView;
    private ApplysAdapter adapter;

    private static final int AVAILABLE = 1;
    private static final int LOCKED = -1;
    private int GET_DATA_TASK_STATE;
    private int page;
    private boolean endData;

    public ApplyRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listData = new ArrayList<Map<String, Object>>();
        adapter = new ApplysAdapter(getActivity(), listData);
        page = 1;
        endData = false;
        GET_DATA_TASK_STATE = AVAILABLE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_apply_record, container, false);
        preference = new ExpressSharedPreference(getActivity());
        listView = (RefreshableListView) view.findViewById(R.id.far_rlv_main);
        listView.setOnRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadData() {
                if (!endData && GET_DATA_TASK_STATE == AVAILABLE) {
                    new GetApplysTask().execute();
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
        new GetApplysTask().execute();
        return view;
    }

    private void refresh() {
        listData.clear();
        page = 1;
        endData = false;
        new GetApplysTask().execute();
    }

    private class GetApplysTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            GET_DATA_TASK_STATE = LOCKED;
            HashMap<String, Object> paramList = new HashMap<String, Object>();
            paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
            paramList.put("page", page);
            return HttpUtil.getData(RequestUrl.GET_USER_APPLYS_URL, paramList);
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
                        JSONArray applysArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < applysArray.length(); i++) {
                            JSONObject item = applysArray.getJSONObject(i);
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
                        if (applysArray.length() < 10) {
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

    private class ApplysAdapter extends CommonAdapter {

        public ApplysAdapter(Context context, List<Map<String, Object>> list) {
            super(context, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.far_rlv_item, parent, false);
            TextView tvAddr = (TextView) view.findViewById(R.id.far_tv_addr);
            TextView tvSubstance = (TextView) view.findViewById(R.id.far_tv_substance);
            TextView tvDeadline = (TextView) view.findViewById(R.id.far_tv_deadline);
            TextView tvCreateTime = (TextView) view.findViewById(R.id.far_tv_create_time);
            TextView tvReward = (TextView) view.findViewById(R.id.far_tv_reward);
            TextView tvState = (TextView) view.findViewById(R.id.far_tv_state);
            int disabled = Integer.valueOf(list.get(position).get(UsedFields.DBApplyRecord.DISABLED).toString());
            int expired = Integer.valueOf(list.get(position).get(UsedFields.DBApplyRecord.EXPIRED).toString());
            int missionDone = Integer.valueOf(list.get(position).get(UsedFields.DBApplyRecord.MISSION_DONE).toString());
            list.get(position).put(ViewDetailActivity.IS_APPLYER, true);
            if (disabled == 0 && expired == 0 && missionDone == 0) {
                tvState.setText("状态：待完成");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_NOT_DONE);
            } else if (disabled == 1) {
                tvState.setText("状态：已取消");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_DISABLED);
            } else if (expired == 1) {
                tvState.setText("状态：已过期");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_EXPIRED);
            } else if (missionDone == 1) {
                tvState.setText("状态：已收到");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_DONE);
            } else {
                tvState.setText("该快递状态出错");
                list.get(position).put(ViewDetailActivity.STATE, ViewDetailActivity.STATE_ERROR);
                return view;
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
