package com.jamie.express.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jamie on 2016/3/16.
 */
public class ExpressSharedPreference {

    public static final String TAG = "ExpressSharedPreference";
    private static final String USER = "userInfo";
    private static final int MODE = 0;

    private Context context;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public ExpressSharedPreference(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(USER, MODE);
        editor = preferences.edit();
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public String getUserId() {
        return preferences.getString(UsedFields.DBUserInfo.USER_ID, null);
    }

    public String getPassword() {
        return preferences.getString(UsedFields.DBUserInfo.PASSWORD, null);
    }

    public String getIcon() {
        return preferences.getString(UsedFields.DBUserInfo.ICON, null);
    }

    public String getNick() {
        return preferences.getString(UsedFields.DBUserInfo.NICK, null);
    }

    public String getUserName() {
        return preferences.getString(UsedFields.DBUserInfo.USER_NAME, null);
    }

    public String getSex() {
        return preferences.getString(UsedFields.DBUserInfo.SEX, null);
    }

    public String getSchool() {
        return preferences.getString(UsedFields.DBUserInfo._SCHOOL, null);
    }

    public String getCollege() {
        return preferences.getString(UsedFields.DBUserInfo._COLLEGE, null);
    }

    public String getDepartment() {
        return preferences.getString(UsedFields.DBUserInfo._DEPARTMENT, null);
    }

    public String getTel() {
        return preferences.getString(UsedFields.DBUserInfo.TEL, null);
    }

    public String getQq() {
        return preferences.getString(UsedFields.DBUserInfo.QQ, null);
    }

    public String getYear() {
        return preferences.getString(UsedFields.DBUserInfo.YEAR, null);
    }

    public String getProvince() {
        return preferences.getString(UsedFields.DBUserInfo._PROVINCE, null);
    }

    public String getCity() {
        return preferences.getString(UsedFields.DBUserInfo._CITY, null);
    }

    public String getAddr() {
        return preferences.getString(UsedFields.DBUserInfo.ADDR, null);
    }

    public boolean getAlias() {
        return preferences.getBoolean("firstLogin", true);
    }

    public void setIcon(String icon) {
        editor.putString(UsedFields.DBUserInfo.ICON, icon);
        editor.commit();
    }

    public String getSearchCondition() {
        return preferences.getString(UsedFields.DBExpressage._LABEL_IDS, "*");
    }

    public Set<Integer> getSearchConditionSet() {
        Set<Integer> set = new HashSet<Integer>();
        if (getSearchCondition().equals("*")) {
            return set;
        }
        String[] arr = getSearchCondition().split(",");
        for (int i = 0; i < arr.length; i++) {
            set.add(Integer.valueOf(arr[i]));
        }
        return set;
    }

    public void setSearchCondition() {
        editor.putString(UsedFields.DBExpressage._LABEL_IDS, "*");
        editor.commit();
    }

    public void setSearchCondition(List<Map<String, Object>> list) {
        int listSize = list.size();
        if (listSize == 0) {
            editor.putString(UsedFields.DBExpressage._LABEL_IDS, "*");
            editor.commit();
            return;
        }
        if (listSize == 1) {
            editor.putString(UsedFields.DBExpressage._LABEL_IDS, String.valueOf(list.get(0).get(UsedFields.ID)));
            editor.commit();
            return;
        }
        int start = 1;
        StringBuilder stringBuilder = new StringBuilder("");
        for (Map<String, Object> map : list) {
            stringBuilder.append(map.get(UsedFields.ID));
            if (start >= listSize) {
                editor.putString(UsedFields.DBExpressage._LABEL_IDS, stringBuilder.toString());
                editor.commit();
                Log.i(TAG, getSearchCondition());
                return;
            }
            stringBuilder.append(",");
            start++;
        }
    }

    public void setPsw(String psw) {
        editor.putString(UsedFields.DBUserInfo.PASSWORD, psw);
        editor.commit();
    }

    public void setAlias() {
        editor.putBoolean("firstLogin", false);
        editor.commit();
    }

    public void setUserInfo(JSONObject jsonObject) {
        try {
            String userID = jsonObject.getString(UsedFields.ID).trim();
            if (getUserId() != null && !getUserId().trim().equals(userID)) {
                editor.putBoolean("firstLogin", true);
                editor.putString(UsedFields.DBExpressage._LABEL_IDS, "*");
            }
            editor.putString(UsedFields.DBUserInfo.USER_ID, userID);
            //editor.putString(UsedFields.DBUserInfo.PASSWORD, jsonObject.getString(UsedFields.DBUserInfo.PASSWORD));
            editor.putString(UsedFields.DBUserInfo.NICK, jsonObject.getString(UsedFields.DBUserInfo.NICK));
            editor.putString(UsedFields.DBUserInfo.USER_NAME, jsonObject.getString(UsedFields.DBUserInfo.USER_NAME));
            editor.putString(UsedFields.DBUserInfo.SEX, jsonObject.getString(UsedFields.DBUserInfo.SEX));
            editor.putString(UsedFields.DBUserInfo.ICON, jsonObject.getString(UsedFields.DBUserInfo.ICON));
            editor.putString(UsedFields.DBUserInfo._SCHOOL, jsonObject.getString(UsedFields.DBUserInfo._SCHOOL));
            editor.putString(UsedFields.DBUserInfo._COLLEGE, jsonObject.getString(UsedFields.DBUserInfo._COLLEGE));
            editor.putString(UsedFields.DBUserInfo._DEPARTMENT, jsonObject.getString(UsedFields.DBUserInfo._DEPARTMENT));
            editor.putString(UsedFields.DBUserInfo.TEL, jsonObject.getString(UsedFields.DBUserInfo.TEL));
            editor.putString(UsedFields.DBUserInfo.QQ, jsonObject.getString(UsedFields.DBUserInfo.QQ));
            editor.putString(UsedFields.DBUserInfo.YEAR, jsonObject.getString(UsedFields.DBUserInfo.YEAR));
            editor.putString(UsedFields.DBUserInfo._PROVINCE, jsonObject.getString(UsedFields.DBUserInfo._PROVINCE));
            editor.putString(UsedFields.DBUserInfo._CITY, jsonObject.getString(UsedFields.DBUserInfo._CITY));
            editor.putString(UsedFields.DBUserInfo.ADDR, jsonObject.getString(UsedFields.DBUserInfo.ADDR));
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void clearUserInfo() {
        editor.putBoolean("firstLogin", true);
        editor.putString(UsedFields.DBUserInfo.USER_ID, null);
        editor.putString(UsedFields.DBUserInfo.PASSWORD, null);
        editor.putString(UsedFields.DBExpressage._LABEL_IDS, "*");
        editor.putString(UsedFields.DBUserInfo.NICK, null);
        editor.putString(UsedFields.DBUserInfo.USER_NAME, null);
        editor.putString(UsedFields.DBUserInfo.SEX, null);
        editor.putString(UsedFields.DBUserInfo.ICON, null);
        editor.putString(UsedFields.DBUserInfo._SCHOOL, null);
        editor.putString(UsedFields.DBUserInfo._COLLEGE, null);
        editor.putString(UsedFields.DBUserInfo._DEPARTMENT, null);
        editor.putString(UsedFields.DBUserInfo.TEL, null);
        editor.putString(UsedFields.DBUserInfo.QQ, null);
        editor.putString(UsedFields.DBUserInfo.YEAR, null);
        editor.putString(UsedFields.DBUserInfo._PROVINCE, null);
        editor.putString(UsedFields.DBUserInfo._CITY, null);
        editor.putString(UsedFields.DBUserInfo.ADDR, null);
        editor.commit();
    }

    public void setUserInfo(String userID, String nick, String name, String sex, String icon, String school, String college,
                            String department, String tel, String qq, String year, String province, String city, String addr) {
        editor.putString(UsedFields.DBUserInfo.USER_ID, userID);
        editor.putString(UsedFields.DBUserInfo.NICK, nick);
        editor.putString(UsedFields.DBUserInfo.USER_NAME, name);
        editor.putString(UsedFields.DBUserInfo.SEX, sex);
        editor.putString(UsedFields.DBUserInfo.ICON, icon);
        editor.putString(UsedFields.DBUserInfo._SCHOOL, school);
        editor.putString(UsedFields.DBUserInfo._COLLEGE, college);
        editor.putString(UsedFields.DBUserInfo._DEPARTMENT, department);
        editor.putString(UsedFields.DBUserInfo.TEL, tel);
        editor.putString(UsedFields.DBUserInfo.QQ, qq);
        editor.putString(UsedFields.DBUserInfo.YEAR, year);
        editor.putString(UsedFields.DBUserInfo._PROVINCE, province);
        editor.putString(UsedFields.DBUserInfo._CITY, city);
        editor.putString(UsedFields.DBUserInfo.ADDR, addr);
        editor.commit();
    }

}
