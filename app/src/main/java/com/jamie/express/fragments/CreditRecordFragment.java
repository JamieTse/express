package com.jamie.express.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
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
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreditRecordFragment extends Fragment {

    public static final String TAG = "CreditRecordFragment";

    private ExpressSharedPreference preference;
    private List<Map<String, Object>> listData;
    private RefreshableListView listView;
    private CreditsAdapter adapter;
    private int page;
    private boolean endData;

    private TextView tvCount;
    private TextView tvSum;

    private static final int AVAILABLE = 1;
    private static final int LOCKED = -1;
    private int GET_DATA_TASK_STATE;

    public CreditRecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listData = new ArrayList<Map<String, Object>>();
        adapter = new CreditsAdapter(getActivity(), listData);
        page = 1;
        endData = false;
        GET_DATA_TASK_STATE = AVAILABLE;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_credit_record, container, false);
        preference = new ExpressSharedPreference(getActivity());
        tvCount = (TextView) view.findViewById(R.id.fcr_tv_count);
        tvSum = (TextView) view.findViewById(R.id.fcr_tv_sum);
        listView = (RefreshableListView) view.findViewById(R.id.fcr_rlv_main);
        listView.setOnRefreshListener(new RefreshableListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }

            @Override
            public void onLoadData() {
                if (!endData && GET_DATA_TASK_STATE == AVAILABLE) {
                    new GetCreditsTask().execute();
                }
            }
        });
        listView.setAdapter(adapter);
        new GetCreditsTask().execute();
        return view;
    }

    public void refresh() {
        listData.clear();
        page = 1;
        endData = false;
        new GetCreditsTask().execute();
    }

    private class GetCreditsTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            GET_DATA_TASK_STATE = LOCKED;
            HashMap<String, Object> paramList = new HashMap<String, Object>();
            paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
            paramList.put("page", page);
            return HttpUtil.getData(RequestUrl.GET_USER_CREDITS_URL, paramList);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            listView.onRefreshComplete();
            if (jsonObject != null) {
                Log.i(TAG, jsonObject.toString());
                try {
                    int resultCode = jsonObject.getInt("code");
                    if (resultCode == HttpUtil.NO_DATA) {
                        endData = true;
                        Toast.makeText(getActivity().getApplicationContext(), R.string.no_data, Toast.LENGTH_SHORT).show();
                        return;
                    } else if (resultCode == HttpUtil.SUCCESS) {
                        JSONObject allData = jsonObject.getJSONObject("data");
                        String count = allData.getString("count");
                        String sum = allData.getString("sum");
                        tvCount.setText("共" + count + "个评价");
                        if (!sum.equals("null")) {
                            tvSum.setText("共" + sum + "个信用");
                        } else {
                            tvSum.setText("共0个信用");
                        }
                        JSONArray credits = allData.getJSONArray("records");
                        for (int i = 0; i < credits.length(); i++) {
                            JSONObject item = credits.getJSONObject(i);
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put(UsedFields.ID, item.getString(UsedFields.ID));
                            map.put(UsedFields.DBCreditRecord._FROM_ICON, item.getString(UsedFields.DBCreditRecord._FROM_ICON));
                            map.put(UsedFields.DBCreditRecord._FROM_NAME, item.getString(UsedFields.DBCreditRecord._FROM_NAME));
                            map.put(UsedFields.DBCreditRecord.REMARK, item.getString(UsedFields.DBCreditRecord.REMARK));
                            map.put(UsedFields.DBCreditRecord.TYPE, item.getInt(UsedFields.DBCreditRecord.TYPE));
                            map.put(UsedFields.DBCreditRecord.CREATE_TIME, item.getString(UsedFields.DBCreditRecord.CREATE_TIME));
                            map.put(UsedFields.DBCreditRecord.CONTENT, item.getString(UsedFields.DBCreditRecord.CONTENT));
                            map.put(UsedFields.DBCreditRecord.APPLY_ID, item.getString(UsedFields.DBCreditRecord.APPLY_ID));
                            map.put(UsedFields.DBCreditRecord.EX_ID, item.getString(UsedFields.DBCreditRecord.EX_ID));
                            listData.add(map);
                        }
                        page += 1;
                        if (credits.length() < 10) {
                            endData = true;
                            Toast.makeText(getActivity().getApplicationContext(), R.string.no_data, Toast.LENGTH_SHORT).show();
                        }
                        adapter.notifyDataSetChanged();
                        GET_DATA_TASK_STATE = AVAILABLE;
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private class CreditsAdapter extends CommonAdapter {

        public CreditsAdapter(Context context, List<Map<String, Object>> list) {
            super(context, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = inflater.inflate(R.layout.fcr_rlv_item, parent, false);
            TextView tvContent = (TextView) view.findViewById(R.id.fcr_tv_content);
            TextView tvState = (TextView) view.findViewById(R.id.fcr_tv_state);
            TextView tvName = (TextView) view.findViewById(R.id.fcr_tv_name);
            TextView tvCreateTime = (TextView) view.findViewById(R.id.fcr_tv_create_time);
            TextView tvRemark = (TextView) view.findViewById(R.id.fcr_tv_remark);
            final ImageView ivHead = (ImageView) view.findViewById(R.id.fcr_iv_head);
            tvContent.setText("评价内容：" + list.get(position).get(UsedFields.DBCreditRecord.CONTENT).toString());
            tvCreateTime.setText("评价时间：" + CommonFunction.formatDateTime(list.get(position).get(UsedFields.DBCreditRecord.CREATE_TIME).toString()));
            tvName.setText(list.get(position).get(UsedFields.DBCreditRecord._FROM_NAME).toString());
            tvRemark.setText("获得积分：" + list.get(position).get(UsedFields.DBCreditRecord.REMARK).toString());
            int state = Integer.valueOf(list.get(position).get(UsedFields.DBCreditRecord.TYPE).toString());
            switch (state) {
                case 1:
                    tvState.setText(R.string.state_done);
                    break;
                case 2:
                    tvState.setText(R.string.state_disabled);
                    break;
                case 3:
                    tvState.setText(R.string.state_expired);
                    break;
                default:
                    break;
            }
            ivHead.setScaleType(ImageView.ScaleType.FIT_XY);
            ImageLoader.loadBitmap(RequestUrl.IMG_URL + list.get(position).get(UsedFields.DBCreditRecord._FROM_ICON), new ImageLoader.BitmapCallback() {
                @Override
                public void onGotBitmap(Bitmap bitmap) {
                    ivHead.setImageBitmap(bitmap);
                }
            });
            return view;
        }
    }

}
