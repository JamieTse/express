package com.jamie.express.fragments;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jamie.express.R;
import com.jamie.express.activities.ExpressActivity;
import com.jamie.express.utils.CommonFunction;
import com.jamie.express.utils.ExpressSharedPreference;
import com.jamie.express.utils.HttpUtil;
import com.jamie.express.utils.PushMessages;
import com.jamie.express.utils.RequestUrl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "PostFragment";

    private ExpressSharedPreference preference;
    private Button btnSetDate;
    private Button btnSetTime;
    private Button btnDoPost;
    private Button btnCancel;
    private TextView etAddr;
    private TextView etSubst;
    private TextView etDescr;
    private TextView etReward;
    private GridView gvAllTags;
    private GridView gvCurrTags;

    private int initHour;
    private int initMinute;
    private int initYear;
    private int initMonth;
    private int initDay;
    private int gvItemHeight;
    private List<Map<String, Object>> listAllTags;
    private List<Map<String, Object>> listCurrTags;
    private SimpleAdapter allTagsAdapter;
    private SimpleAdapter currTagsAdapter;

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        preference = new ExpressSharedPreference(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listAllTags = new ArrayList<Map<String, Object>>();
        listCurrTags = new ArrayList<Map<String, Object>>();
        allTagsAdapter = new SimpleAdapter(getActivity(), listAllTags, R.layout.gv_tags_cell_green, new String[]{"label_title"}, new int[]{R.id.gv_tags_item_green});
        currTagsAdapter = new SimpleAdapter(getActivity(), listCurrTags, R.layout.gv_tags_cell_pink, new String[]{"label_title"}, new int[]{R.id.gv_tags_item_pink});
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        btnSetDate = (Button) view.findViewById(R.id.fp_btn_pick_date);
        btnSetTime = (Button) view.findViewById(R.id.fp_btn_pick_time);
        btnDoPost = (Button) view.findViewById(R.id.fp_btn_post);
        btnCancel = (Button) view.findViewById(R.id.fp_btn_cancel);
        etAddr = (EditText) view.findViewById(R.id.fp_et_addr);
        etDescr = (EditText) view.findViewById(R.id.fp_et_description);
        etReward = (EditText) view.findViewById(R.id.fp_et_reward);
        etSubst = (EditText) view.findViewById(R.id.fp_et_substance);
        gvAllTags = (GridView) view.findViewById(R.id.fp_gv_all_tags);
        gvCurrTags = (GridView) view.findViewById(R.id.fp_gv_curr_tags);
        preference = new ExpressSharedPreference(getActivity());

        Calendar calendar = Calendar.getInstance();
        initHour = calendar.get(Calendar.HOUR_OF_DAY);
        initMinute = calendar.get(Calendar.MINUTE);
        initYear = calendar.get(Calendar.YEAR);
        initMonth = calendar.get(Calendar.MONTH);
        initDay = calendar.get(Calendar.DAY_OF_MONTH);
        btnSetDate.setText(CommonFunction.formatDate(initYear, initMonth + 1, initDay));
        btnSetTime.setText(CommonFunction.formatTime(initHour, initMinute));
        btnDoPost.setOnClickListener(this);
        btnSetTime.setOnClickListener(this);
        btnSetDate.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        gvAllTags.setAdapter(allTagsAdapter);
        gvCurrTags.setAdapter(currTagsAdapter);
        new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                disableComponents();
            }

            @Override
            protected JSONObject doInBackground(Void... params) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                return HttpUtil.getData(RequestUrl.GET_ALL_TAGS_URL, map);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                enableComponents();
                try {
                    int resultCode = jsonObject.getInt("code");
                    if (resultCode == HttpUtil.SUCCESS) {
                        JSONArray resultArray = jsonObject.getJSONArray("data");
                        for (int i = 0; i < resultArray.length(); i++) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("id", resultArray.getJSONObject(i).getInt("id"));
                            map.put("label_title", resultArray.getJSONObject(i).getString("label_title"));
                            listAllTags.add(map);
                        }
                        gvItemHeight = allTagsAdapter.getView(0, null, gvAllTags).getLayoutParams().height + 10;
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) gvAllTags.getLayoutParams();
                        layoutParams.height = gvItemHeight * (listAllTags.size() % 2 == 0 ? (listAllTags.size() / 2) : (listAllTags.size() / 2 + 1)) + 10;
                        gvAllTags.setLayoutParams(layoutParams);
                        allTagsAdapter.notifyDataSetChanged();
                        return;
                    } else if (resultCode == HttpUtil.FAILED) {
                        Snackbar.make(gvAllTags, R.string.error_fetching_tags, Snackbar.LENGTH_SHORT).show();
                        return;
                    } else if (resultCode == HttpUtil.NO_DATA) {
                        Snackbar.make(gvAllTags, R.string.no_tags, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
        gvAllTags.setOnItemClickListener(new OnAllTagsClick());
        gvCurrTags.setOnItemClickListener(new OnCurrTagsClick());

        return view;
    }

    private void disableComponents() {
        etSubst.setEnabled(false);
        etReward.setEnabled(false);
        etDescr.setEnabled(false);
        etAddr.setEnabled(false);
        btnCancel.setEnabled(false);
        btnSetDate.setEnabled(false);
        btnSetTime.setEnabled(false);
        btnDoPost.setEnabled(false);
    }

    private void enableComponents() {
        etSubst.setEnabled(true);
        etReward.setEnabled(true);
        etDescr.setEnabled(true);
        etAddr.setEnabled(true);
        btnCancel.setEnabled(true);
        btnSetDate.setEnabled(true);
        btnSetTime.setEnabled(true);
        btnDoPost.setEnabled(true);
    }

    private boolean checkInputs() {
        if (etAddr.getText().toString().equals(null) || etAddr.getText().toString().trim().equals("")) {
            return false;
        }
        if (etSubst.getText().toString().equals(null) || etSubst.getText().toString().trim().equals("")) {
            return false;
        }
        if (etReward.getText().toString().equals(null) || etReward.getText().toString().trim().equals("")) {
            return false;
        }
        if (etDescr.getText().toString().equals(null) || etDescr.getText().toString().trim().equals("")) {
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fp_btn_pick_date:
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DeadlineTimePicker(), initYear, initMonth, initDay);
                datePickerDialog.show();
                break;
            case R.id.fp_btn_pick_time:
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new DeadlineTimePicker(), initHour, initMinute, true);
                timePickerDialog.show();
                break;
            case R.id.fp_btn_cancel:
                ((ExpressActivity) getActivity()).goToFragment(R.id.nav_expressages);
                break;
            case R.id.fp_btn_post:
                if (checkInputs()) {
                    new AsyncTask<String, Void, Integer>() {

                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            disableComponents();
                        }

                        @Override
                        protected Integer doInBackground(String... params) {
                            Set<String> set = new HashSet<String>();
                            set.add("id");
                            set.add("label_title");
                            HashMap<String, String> tags = CommonFunction.implodeDataMultiField(set, listCurrTags);
                            int postResult = PushMessages.pushPost(preference, params[0], params[1], params[2], params[3], params[4], tags.get("id"), tags.get("label_title"));
                            return postResult;
                        }

                        @Override
                        protected void onPostExecute(Integer integer) {
                            super.onPostExecute(integer);
                            if (integer == PushMessages.SUCCESS) {
                                Toast.makeText(getActivity(), R.string.success_executing, Toast.LENGTH_SHORT).show();
                                ((ExpressActivity) getActivity()).goToFragment(R.id.nav_expressages);
                            } else if (integer == PushMessages.FAILED_PUSHING) {
                                Toast.makeText(getActivity(), R.string.failed_pushing, Toast.LENGTH_SHORT).show();
                                enableComponents();
                            } else if (integer == PushMessages.FAILED_SAVING) {
                                Toast.makeText(getActivity(), R.string.failed_executing, Toast.LENGTH_SHORT).show();
                                enableComponents();
                            }
                        }
                    }.execute(etAddr.getText().toString().trim(), CommonFunction.formatDateTime(initYear, initMonth + 1, initDay, initHour, initMinute),
                            etDescr.getText().toString().trim(), etReward.getText().toString().trim(), etSubst.getText().toString().trim());
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.input_empty, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private class OnCurrTagsClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, Object> cell = listCurrTags.get(position);
            listCurrTags.remove(position);
            listAllTags.add(cell);
            LinearLayout.LayoutParams currParams = (LinearLayout.LayoutParams) gvCurrTags.getLayoutParams();
            currParams.height = gvItemHeight * (listCurrTags.size() % 2 == 0 ? (listCurrTags.size() / 2) : (listCurrTags.size() / 2 + 1)) + 10;
            gvCurrTags.setLayoutParams(currParams);
            LinearLayout.LayoutParams allParams = (LinearLayout.LayoutParams) gvAllTags.getLayoutParams();
            allParams.height = gvItemHeight * (listAllTags.size() % 2 == 0 ? (listAllTags.size() / 2) : (listAllTags.size() / 2 + 1)) + 10;
            gvAllTags.setLayoutParams(allParams);
            currTagsAdapter.notifyDataSetChanged();
            allTagsAdapter.notifyDataSetChanged();
        }
    }

    private class OnAllTagsClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, Object> cell = listAllTags.get(position);
            listAllTags.remove(position);
            listCurrTags.add(cell);
            LinearLayout.LayoutParams currParams = (LinearLayout.LayoutParams) gvCurrTags.getLayoutParams();
            currParams.height = gvItemHeight * (listCurrTags.size() % 2 == 0 ? (listCurrTags.size() / 2) : (listCurrTags.size() / 2 + 1)) + 10;
            gvCurrTags.setLayoutParams(currParams);
            LinearLayout.LayoutParams allParams = (LinearLayout.LayoutParams) gvAllTags.getLayoutParams();
            allParams.height = gvItemHeight * (listAllTags.size() % 2 == 0 ? (listAllTags.size() / 2) : (listAllTags.size() / 2 + 1)) + 10;
            gvAllTags.setLayoutParams(allParams);
            currTagsAdapter.notifyDataSetChanged();
            allTagsAdapter.notifyDataSetChanged();
        }
    }

    private class DeadlineTimePicker implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            btnSetDate.setText(CommonFunction.formatDate(year, monthOfYear + 1, dayOfMonth));
            initYear = year;
            initMonth = monthOfYear;
            initDay = dayOfMonth;
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            btnSetTime.setText(CommonFunction.formatTime(hourOfDay, minute));
            initHour = hourOfDay;
            initMinute = minute;
        }
    }

}
