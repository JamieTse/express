package com.jamie.express.fragments;


import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jamie.express.R;
import com.jamie.express.activities.ExpressActivity;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class PersonInfoFragment extends Fragment {

    public static final String TAG = "PersonInfoFragment";

    private ExpressSharedPreference preference;
    private TextView tvUserID;
    private TextView tvName;
    private TextView tvNick;
    private TextView tvSex;
    private TextView tvSchool;
    private TextView tvCollege;
    private TextView tvDepartment;
    private TextView tvAddr;
    private TextView tvTel;
    private TextView tvQq;
    private TextView tvProvince;
    private TextView tvCity;
    private TextView tvYear;
    private GridView gvTags;
    private FloatingActionButton fabSetTags;
    private SimpleAdapter tagsAdapter;
    private List<Map<String, Object>> listItem;
    private int gvItemHeight;

    private SetTagsDialog dialog;

    public PersonInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_person_info, container, false);
        preference = new ExpressSharedPreference(getActivity());
        tvUserID = (TextView) view.findViewById(R.id.fpi_tv_userid);
        tvName = (TextView) view.findViewById(R.id.fpi_tv_name);
        tvNick = (TextView) view.findViewById(R.id.fpi_tv_nick);
        tvSex = (TextView) view.findViewById(R.id.fpi_tv_sex);
        tvSchool = (TextView) view.findViewById(R.id.fpi_tv_school);
        tvCollege = (TextView) view.findViewById(R.id.fpi_tv_college);
        tvDepartment = (TextView) view.findViewById(R.id.fpi_tv_department);
        tvAddr = (TextView) view.findViewById(R.id.fpi_tv_addr);
        tvTel = (TextView) view.findViewById(R.id.fpi_tv_tel);
        tvQq = (TextView) view.findViewById(R.id.fpi_tv_qq);
        tvProvince = (TextView) view.findViewById(R.id.fpi_tv_province);
        tvCity = (TextView) view.findViewById(R.id.fpi_tv_city);
        tvYear = (TextView) view.findViewById(R.id.fpi_tv_year);
        gvTags = (GridView) view.findViewById(R.id.fpi_gv_tags);
        fabSetTags = (FloatingActionButton) view.findViewById(R.id.fpi_fab_set_tags);
        listItem = new ArrayList<Map<String, Object>>();
        tagsAdapter = new SimpleAdapter(getActivity(), listItem, R.layout.gv_tags_cell_pink, new String[]{"label_title"}, new int[]{R.id.gv_tags_item_pink});
        dialog = new SetTagsDialog(getActivity());

        tvUserID.setText(preference.getUserId());
        tvAddr.setText(preference.getAddr());
        tvName.setText(preference.getUserName());
        tvNick.setText(preference.getNick());
        tvSex.setText(preference.getSex());
        tvSchool.setText(preference.getSchool());
        tvCollege.setText(preference.getCollege());
        tvDepartment.setText(preference.getDepartment());
        tvTel.setText(preference.getTel());
        tvQq.setText(preference.getQq());
        tvProvince.setText(preference.getProvince());
        tvCity.setText(preference.getCity());
        tvYear.setText(preference.getYear());
        gvTags.setAdapter(tagsAdapter);
        new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... params) {
                HashMap<String, Object> paramList = new HashMap<String, Object>();
                paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
                return HttpUtil.getData(RequestUrl.GET_USER_TAGS_URL, paramList);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                if (jsonObject != null) {
                    try {
                        int resultCode = jsonObject.getInt("code");
                        if (resultCode == HttpUtil.SUCCESS) {
                            JSONArray resultArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject item = resultArray.getJSONObject(i);
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                int id = item.getInt(UsedFields.DBUserLabel.LABEL_ID);
                                map.put(UsedFields.ID, id);
                                map.put("label_title", item.getString("label_title"));
                                listItem.add(map);
                            }
                            gvItemHeight = tagsAdapter.getView(0, null, gvTags).getLayoutParams().height + 15;
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) gvTags.getLayoutParams();
                            layoutParams.height = gvItemHeight * (listItem.size() % 2 == 0 ? listItem.size() / 2 : (listItem.size() / 2 + 1)) + 15;
                            gvTags.setLayoutParams(layoutParams);
                            tagsAdapter.notifyDataSetChanged();
                        } else if (resultCode == HttpUtil.NO_DATA) {
                            Snackbar.make(gvTags, "未设置标签", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(gvTags, "获取标签失败", Snackbar.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Snackbar.make(gvTags, "获取标签失败", Snackbar.LENGTH_SHORT).show();
                }
            }
        }.execute();
        fabSetTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
//        gvTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                dialog.show();
//            }
//        });

        return view;
    }

    private class SetTagsDialog extends Dialog {

        private Button btnSet;
        private Button btnCancel;
        private GridView gvMyTags;
        private GridView gvAllTags;
        private List<Map<String, Object>> listMyTags;
        private List<Map<String, Object>> listAllTags;
        private SimpleAdapter myTagsAdapter;
        private SimpleAdapter allTagsAdapter;

        public SetTagsDialog(Context context) {
            super(context);
            initDialog();
        }

        private void initDialog() {
            View main = LayoutInflater.from(getContext()).inflate(R.layout.fpi_set_tags_dialog, null);
            super.setContentView(main);
            btnSet = (Button) main.findViewById(R.id.fpi_btn_set);
            btnCancel = (Button) main.findViewById(R.id.fpi_btn_cancel);
            gvMyTags = (GridView) main.findViewById(R.id.fpi_gv_myTag);
            gvAllTags = (GridView) main.findViewById(R.id.fpi_gv_allTag);
            setTitle("设置个人关注标签");
            listMyTags = new ArrayList<Map<String, Object>>();
            listAllTags = new ArrayList<Map<String, Object>>();
            myTagsAdapter = new SimpleAdapter(getActivity(), listMyTags, R.layout.gv_tags_cell_pink, new String[]{"label_title"}, new int[]{R.id.gv_tags_item_pink});
            allTagsAdapter = new SimpleAdapter(getActivity(), listAllTags, R.layout.gv_tags_cell_green, new String[]{"label_title"}, new int[]{R.id.gv_tags_item_green});
            gvMyTags.setAdapter(myTagsAdapter);
            gvAllTags.setAdapter(allTagsAdapter);
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
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            btnSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    new AsyncTask<Void, Void, Integer>() {

                        @Override
                        protected Integer doInBackground(Void... params) {
                            final Map<String, String> paramMap = CommonFunction.implodeDataSingleField(UsedFields.ID, listMyTags);
                            final HashMap<String, Object> paramList = new HashMap<String, Object>();
                            Set tagsSet = CommonFunction.listToSet(listMyTags, UsedFields.DBUserLabel.LABEL_TITLE);
                            JPushInterface.setTags(getActivity().getApplicationContext(), tagsSet, new TagAliasCallback() {
                                @Override
                                public void gotResult(int responseCode, String alias, Set<String> tags) {
                                    if (responseCode == 0) {
                                        //TODO 出问题，程序不执行Callback中的代码
                                    }
                                }
                            });
                            paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
                            paramList.put(UsedFields.DBExpressage._LABEL_IDS, paramMap.get(UsedFields.ID));
                            return HttpUtil.postData(RequestUrl.SET_USER_TAGS_URL, paramList);
                        }

                        @Override
                        protected void onPostExecute(Integer integer) {
                            super.onPostExecute(integer);
                            if (integer == HttpUtil.SUCCESS) {
                                Toast.makeText(getActivity(), R.string.success_executing, Toast.LENGTH_SHORT).show();
                                ((ExpressActivity) getActivity()).goToFragment(R.id.nav_expressages);
                            } else {
                                Toast.makeText(getActivity(), R.string.failed_executing, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute();
                }
            });
        }

        @Override
        public void show() {
            super.show();
            new AsyncTask<Void, Void, JSONObject>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    //TODO
                    listAllTags.clear();
                    listMyTags.clear();
                }

                @Override
                protected JSONObject doInBackground(Void... params) {
                    HashMap<String, Object> paramList = new HashMap<String, Object>();
                    //paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
                    return HttpUtil.getData(RequestUrl.GET_ALL_TAGS_URL, paramList);
                }

                @Override
                protected void onPostExecute(JSONObject jsonObject) {
                    if (jsonObject == null) {
                        Toast.makeText(getActivity(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                        //dialog.dismiss();
                        return;
                    }
                    try {
                        if (jsonObject.getInt("code") == HttpUtil.SUCCESS) {
                            JSONArray tags = jsonObject.getJSONArray("data");
                            for (int i = 0; i < tags.length(); i++) {
                                JSONObject item = tags.getJSONObject(i);
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                int id = item.getInt(UsedFields.ID);
                                map.put(UsedFields.ID, id);
                                map.put("label_title", item.getString("label_title"));
                                if (listItem.contains(map)) {
                                    listMyTags.add(map);
                                } else {
                                    listAllTags.add(map);
                                }
                            }
                            myTagsAdapter.notifyDataSetChanged();
                            allTagsAdapter.notifyDataSetChanged();
                            return;
                        } else {
                            Toast.makeText(getActivity(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                            //dialog.dismiss();
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

}
