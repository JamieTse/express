package com.jamie.express.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.jamie.express.activities.ExpressActivity;
import com.jamie.express.activities.ViewExpressageActivity;
import com.jamie.express.utils.HttpUtil;
import com.jamie.express.utils.RequestUrl;
import com.jamie.express.utils.UsedFields;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by jamie on 2016/3/22.
 */
public class PushReceiver extends BroadcastReceiver {
    private static final String TAG = "Jpush";

    public PushReceiver() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            //send the Registration Id to your server...

        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
            processCustomMessage(context, bundle);

        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
            try {
                if (jsonObject.getInt("type") == 0) {
                    final int ex_id = jsonObject.getInt(UsedFields.DBExpressage.EXPRESSAGE_ID);
                    new AsyncTask<Void, Void, JSONObject>() {

                        @Override
                        protected JSONObject doInBackground(Void... params) {
                            HashMap<String, Object> paramList = new HashMap<String, Object>();
                            paramList.put(UsedFields.DBExpressage.EXPRESSAGE_ID, ex_id);
                            return HttpUtil.getData(RequestUrl.GET_SINGLE_EXPRESSAGE_URL, paramList);
                        }

                        @Override
                        protected void onPostExecute(JSONObject result) {
                            super.onPostExecute(result);
                            try {
                                int responseCode = result.getInt("code");
                                if (responseCode == HttpUtil.FAILED) {
                                    return;
                                } else if (responseCode == HttpUtil.SUCCESS) {
                                    Bundle args = new Bundle();
                                    JSONObject resultData = result.getJSONObject("data");
                                    //Map<String, Object> map = new HashMap<String, Object>();
                                    Iterator<String> iterator = resultData.keys();
                                    while (iterator.hasNext()) {
                                        String key = iterator.next();
                                        if (key.equals(UsedFields.ID)) {
                                            args.putInt(key, resultData.getInt(key));
                                        } else {
                                            args.putString(key, resultData.getString(key));
                                        }
                                    }
                                    Intent exIntent = new Intent(context, ViewExpressageActivity.class);
                                    exIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    exIntent.putExtra("expressage", args);
                                    exIntent.putExtra("listID", -1);
                                    context.startActivity(exIntent);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.execute();
                } else {
                    //打开自定义的Activity
                    Intent i = new Intent(context, ExpressActivity.class);
                    i.putExtras(bundle);
                    //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

        } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
            boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
            Log.w(TAG, "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
        } else {
            Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
                if (bundle.getString(JPushInterface.EXTRA_EXTRA).isEmpty()) {
                    Log.i(TAG, "This message has no Extra data");
                    continue;
                }

                try {
                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
                    Iterator<String> it = json.keys();

                    while (it.hasNext()) {
                        String myKey = it.next().toString();
                        sb.append("\nkey:" + key + ", value: [" +
                                myKey + " - " + json.optString(myKey) + "]");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Get message extra JSON error!");
                }

            } else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }

    private void processCustomMessage(Context context, Bundle bundle) {

    }

    //send msg to MainActivity
   /* private void processCustomMessage(Context context, Bundle bundle) {
        if (MainActivity.isForeground) {
            String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
            Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
            msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
            if (!ExampleUtil.isEmpty(extras)) {
                try {
                    JSONObject extraJson = new JSONObject(extras);
                    if (null != extraJson && extraJson.length() > 0) {
                        msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
                    }
                } catch (JSONException e) {

                }

            }
            context.sendBroadcast(msgIntent);
        }
    }*/
}
