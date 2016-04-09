package com.jamie.express.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by jamie on 2016/3/20.
 */
public class PushMessages {

    public static final String TAG = "PushMessages";
    public static final int FAILED_SAVING = 0;
    public static final int FAILED_PUSHING = 1;
    public static final int SUCCESS = 2;

    public static int pushApply(ExpressSharedPreference preference, String ownerID, int expressageID) {
        HashMap<String, Object> paramList = new HashMap<String, Object>();
        paramList.put(UsedFields.DBApplyRecord.EX_APPLYER, preference.getUserId());
        //paramList.put(UsedFields.DBApplyRecord._APPLYER_NAME, preference.getUserName());
        //paramList.put(UsedFields.DBApplyRecord._APPLYER_ICON, preference.getIcon());
        paramList.put(UsedFields.DBApplyRecord.EX_OWNER, ownerID);
        paramList.put(UsedFields.DBApplyRecord.EX_ID, expressageID);
        JSONObject result = HttpUtil.getData(RequestUrl.DO_HELP_URL, paramList);
        Log.i(TAG, String.valueOf(result));
        try {
            int resultCode = result.getInt("code");
            if (resultCode == HttpUtil.SUCCESS) {
                int apply_id = result.getInt("data");
                HashMap<String, Object> push = new HashMap<String, Object>();
                push.put(UsedFields.DBPush.TO_ID, ownerID);
                push.put(UsedFields.DBPush.EX_ID, expressageID);
                push.put(UsedFields.DBPush.APPLY_ID, apply_id);
                push.put(UsedFields.DBPush.CREATE_ID, preference.getUserId());
                int pushResultCode = HttpUtil.postData(RequestUrl.PUSH_APPLY_URL, push);
                if (pushResultCode == HttpUtil.SUCCESS) {
                    HashMap<String, Object> pushPhone = new HashMap<String, Object>();
                    pushPhone.put(UsedFields.DBPush.TO_ID, preference.getUserId());
                    pushPhone.put(UsedFields.DBPush.EX_ID, expressageID);
                    pushPhone.put(UsedFields.DBPush.APPLY_ID, apply_id);
                    pushPhone.put(UsedFields.DBPush.CREATE_ID, ownerID);
                    int pushPhoneResultCode = HttpUtil.postData(RequestUrl.PUSH_PHONE_URL, pushPhone);
                    if (pushPhoneResultCode == HttpUtil.SUCCESS) {
                        return SUCCESS;
                    } else if (pushResultCode == HttpUtil.FAILED) {
                        return FAILED_PUSHING;
                    }
                } else if (pushResultCode == HttpUtil.FAILED) {
                    return FAILED_PUSHING;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return FAILED_SAVING;
    }

    public static int pushPost(ExpressSharedPreference preference, String addr, String deadline, String description, String reward, String substance, String labelIDs, String tags) {
        HashMap<String, Object> paramList = new HashMap<String, Object>();
        paramList.put(UsedFields.DBExpressage.ADDR, addr);
        paramList.put(UsedFields.DBExpressage.DEADLINE, deadline);
        paramList.put(UsedFields.DBExpressage.DESCRIPTION, description);
        paramList.put(UsedFields.DBExpressage.REWARD, reward);
        paramList.put(UsedFields.DBExpressage.SUBSTANCE, substance);
        paramList.put(UsedFields.DBExpressage.FROM_USER, preference.getUserId());
        paramList.put(UsedFields.DBExpressage._LABEL_IDS, labelIDs);
        Log.i(TAG, paramList.toString());
        //paramList.put("tags", tags);
        JSONObject result = HttpUtil.getData(RequestUrl.POST_URL, paramList);
        Log.i(TAG, String.valueOf(result));
        try {
            int resultCode = result.getInt("code");
            if (resultCode == HttpUtil.SUCCESS) {
                int expressage_id = result.getInt("data");
                HashMap<String, Object> push = new HashMap<String, Object>();
                push.put(UsedFields.DBExpressage._LABEL_IDS, labelIDs);
                push.put("tags", tags);
                push.put(UsedFields.DBPush.MSG, "有与[" + tags + "]相关的新内容发布");
                push.put(UsedFields.DBExpressage.EXPRESSAGE_ID, expressage_id);
                push.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
                push.put(UsedFields.DBUserInfo.USER_NAME, preference.getUserName());
                push.put(UsedFields.DBUserInfo.ICON, preference.getIcon());
                int pushResultCode = HttpUtil.postData(RequestUrl.PUSH_BY_TAGS_URL, push);
                if (pushResultCode == HttpUtil.SUCCESS) {
                    return SUCCESS;
                } else if (pushResultCode == HttpUtil.FAILED) {
                    return FAILED_PUSHING;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return FAILED_SAVING;
    }

    public static int pushCredit(ExpressSharedPreference preference, String toID, String toName, String content, int exID, int applyID, int type, int rating, boolean toWhom) {
        HashMap<String, Object> paramList = new HashMap<String, Object>();
        paramList.put(UsedFields.DBCreditRecord.FROM_USER, preference.getUserId());
        paramList.put(UsedFields.DBCreditRecord.TO_USER, toID);
        paramList.put(UsedFields.DBCreditRecord.APPLY_ID, applyID);
        paramList.put(UsedFields.DBCreditRecord.EX_ID, exID);
        paramList.put(UsedFields.DBCreditRecord.TYPE, type);
        paramList.put(UsedFields.DBCreditRecord.REMARK, rating);
        paramList.put(UsedFields.DBCreditRecord.CONTENT, content);
        if (toWhom) {
            paramList.put("to_whom", 0);
        } else {
            paramList.put("to_whom", 1);
        }
        int resultCode = HttpUtil.postData(RequestUrl.DO_CREDIT_URL, paramList);
        if (resultCode == HttpUtil.SUCCESS) {
            HashMap<String, Object> push = new HashMap<String, Object>();
            push.put(UsedFields.DBPush.TO_ID, toID);
            push.put(UsedFields.DBPush.TO_NAME, toName);
            push.put(UsedFields.DBPush.MSG, preference.getUserName() + "对你作出评价。");
            push.put(UsedFields.DBPush.EX_ID, exID);
            push.put(UsedFields.DBPush.APPLY_ID, applyID);
            push.put(UsedFields.DBPush.CREATE_ID, preference.getUserId());
            push.put(UsedFields.DBPush.CREATE_NAME, preference.getUserName());
            push.put(UsedFields.DBPush.CREATE_ICON, preference.getIcon());
            int pushResultCode = HttpUtil.postData(RequestUrl.PUSH_CREDIT_DONE_URL, push);
            if (pushResultCode == HttpUtil.SUCCESS) {
                return SUCCESS;
            } else {
                return FAILED_PUSHING;
            }
        }
        return FAILED_SAVING;
    }

    public static int pushMissionDone(ExpressSharedPreference preference, String toID, String toName, int exID, int applyID) {
        HashMap<String, Object> paramList = new HashMap<String, Object>();
        paramList.put(UsedFields.DBApplyRecord.APPLY_ID, applyID);
        int resultCode = HttpUtil.postData(RequestUrl.DONE_MISSION_URL, paramList);
        if (resultCode == HttpUtil.SUCCESS) {
            HashMap<String, Object> push = new HashMap<String, Object>();
            push.put(UsedFields.DBPush.TO_ID, toID);
            push.put(UsedFields.DBPush.TO_NAME, toName);
            push.put(UsedFields.DBPush.MSG, preference.getUserName() + "已确认收到快递。");
            push.put(UsedFields.DBPush.EX_ID, exID);
            push.put(UsedFields.DBPush.APPLY_ID, applyID);
            push.put(UsedFields.DBPush.CREATE_ID, preference.getUserId());
            push.put(UsedFields.DBPush.CREATE_NAME, preference.getUserName());
            push.put(UsedFields.DBPush.CREATE_ICON, preference.getIcon());
            Log.i(TAG, push.toString());
            int pushResultCode = HttpUtil.postData(RequestUrl.PUSH_MISSION_DONE_URL, push);
            if (pushResultCode == HttpUtil.SUCCESS) {
                return SUCCESS;
            } else {
                return FAILED_PUSHING;
            }
        }
        return FAILED_SAVING;
    }

    public static int pushDisabled(ExpressSharedPreference preference, String toID, String toName, int exID, int applyID) {
        HashMap<String, Object> paramList = new HashMap<String, Object>();
        paramList.put(UsedFields.DBApplyRecord.APPLY_ID, applyID);
        int resultCode = HttpUtil.postData(RequestUrl.DISABLE_MISSION_URL, paramList);
        if (resultCode == HttpUtil.SUCCESS) {
            HashMap<String, Object> push = new HashMap<String, Object>();
            push.put(UsedFields.DBPush.TO_ID, toID);
            push.put(UsedFields.DBPush.TO_NAME, toName);
            push.put(UsedFields.DBPush.MSG, preference.getUserName() + "已取消代领你的快递。");
            push.put(UsedFields.DBPush.EX_ID, exID);
            push.put(UsedFields.DBPush.APPLY_ID, applyID);
            push.put(UsedFields.DBPush.CREATE_ID, preference.getUserId());
            push.put(UsedFields.DBPush.CREATE_NAME, preference.getUserName());
            push.put(UsedFields.DBPush.CREATE_ICON, preference.getIcon());
            int pushResultCode = HttpUtil.postData(RequestUrl.PUSH_DISABLED_URL, push);
            if (pushResultCode == HttpUtil.SUCCESS) {
                return SUCCESS;
            } else {
                return FAILED_PUSHING;
            }
        }
        return FAILED_SAVING;
    }

    public static int pushExpired(ExpressSharedPreference preference, String toID, String toName, int exID, int applyID) {
        HashMap<String, Object> paramList = new HashMap<String, Object>();
        paramList.put(UsedFields.DBApplyRecord.APPLY_ID, applyID);
        int resultCode = HttpUtil.postData(RequestUrl.EXPIRE_MISSION_URL, paramList);
        if (resultCode == HttpUtil.SUCCESS) {
            HashMap<String, Object> push = new HashMap<String, Object>();
            push.put(UsedFields.DBPush.TO_ID, toID);
            push.put(UsedFields.DBPush.TO_NAME, toName);
            push.put(UsedFields.DBPush.MSG, preference.getUserName() + "已确认你代领的快递过期。");
            push.put(UsedFields.DBPush.EX_ID, exID);
            push.put(UsedFields.DBPush.APPLY_ID, applyID);
            push.put(UsedFields.DBPush.CREATE_ID, preference.getUserId());
            push.put(UsedFields.DBPush.CREATE_NAME, preference.getUserName());
            push.put(UsedFields.DBPush.CREATE_ICON, preference.getIcon());
            int pushResultCode = HttpUtil.postData(RequestUrl.PUSH_EXPIRED_URL, push);
            if (pushResultCode == HttpUtil.SUCCESS) {
                return SUCCESS;
            } else {
                return FAILED_PUSHING;
            }
        }
        return FAILED_SAVING;
    }

    public static int pushMessage(ExpressSharedPreference preference, String toID, String toName, String msg, int exID, int applyID) {
        HashMap<String, Object> push = new HashMap<String, Object>();
        push.put(UsedFields.DBPush.TO_ID, toID);
        push.put(UsedFields.DBPush.TO_NAME, toName);
        push.put(UsedFields.DBPush.MSG, preference.getUserName() + "：" + msg);
        push.put(UsedFields.DBPush.EX_ID, exID);
        push.put(UsedFields.DBPush.APPLY_ID, applyID);
        push.put(UsedFields.DBPush.CREATE_ID, preference.getUserId());
        push.put(UsedFields.DBPush.CREATE_NAME, preference.getUserName());
        push.put(UsedFields.DBPush.CREATE_ICON, preference.getIcon());
        int pushResultCode = HttpUtil.postData(RequestUrl.PUSH_DISABLED_URL, push);
        if (pushResultCode == HttpUtil.SUCCESS) {
            return SUCCESS;
        }
        return FAILED_PUSHING;
    }
}
