package com.jamie.express.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jamie on 2016/3/20.
 */
public class CommonFunction {

    public static final String TAG = "CommonFunction";

    //TODO 如format日期的函数等
    public static String formatDateTime(String date_str) {
        String result = null;
        String from_pattern_str = "yyyy-MM-dd HH:mm:ss";
        String to_pattern_str = "M月dd日HH时mm分";
        SimpleDateFormat from_pattern = new SimpleDateFormat(from_pattern_str);
        SimpleDateFormat to_pattern = new SimpleDateFormat(to_pattern_str);
        Date date = null;
        try {
            date = (Date) from_pattern.parse(date_str);
            result = to_pattern.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String formatDate(int year, int month, int date) {
        StringBuilder result = new StringBuilder(String.valueOf(year));
        result.append("年");
        result.append(formatNumbers(month));
        result.append("月");
        result.append(formatNumbers(date));
        result.append("日");
        return result.toString();
    }

    public static String formatTime(int hour, int minute) {
        StringBuilder result = new StringBuilder(formatNumbers(hour));
        result.append("时");
        result.append(formatNumbers(minute));
        result.append("分");
        return result.toString();
    }

    public static String formatNumbers(int minute) {
        return String.format("%02d", minute);
    }

    //TODO 未完整测试过功能，目前可用
    public static HashMap<String, String> implodeDataMultiField(Set<String> fieldSet, List<Map<String, Object>> list) {
        HashMap<String, String> map = new HashMap<String, String>();
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            for (String key : fieldSet) {
                String temp = map.get(key);
                if (temp == null) {
                    temp = "";
                }
                if (i >= listSize - 1) {
                    temp = temp + String.valueOf(list.get(i).get(key));
                } else {
                    temp = temp + String.valueOf(list.get(i).get(key)) + ",";
                }
                map.put(key, temp);
            }
        }
        return map;
    }

    //TODO 未完整测试过功能，目前可用
    public static HashMap<String, String> implodeDataSingleField(String field, List<Map<String, Object>> list) {
        HashMap<String, String> map = new HashMap<String, String>();
        int listSize = list.size();
        for (int i = 0; i < listSize; i++) {
            String temp = map.get(field);
            if (temp == null) {
                temp = "";
            }
            if (i >= listSize - 1) {
                temp = temp + String.valueOf(list.get(i).get(field));
            } else {
                temp = temp + String.valueOf(list.get(i).get(field)) + ",";
            }
            map.put(field, temp);
        }
        return map;
    }

    public static Set<String> listToSet(List<Map<String, Object>> listMap, String field) {
        Set<String> set = new HashSet<String>();
        for (Map map : listMap) {
            set.add(String.valueOf(map.get(field)));
        }
        return set;
    }
}
