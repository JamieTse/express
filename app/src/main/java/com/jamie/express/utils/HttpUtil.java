package com.jamie.express.utils;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jamie on 2016/3/15.
 */
public class HttpUtil {

    public static final String TAG = "HttpUtil";
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int UNKOWN_ERROR = 0;
    public static final int INTERNET_ERROR = 1;
    public static final int SUCCESS = 200;
    public static final int NO_DATA = 300;
    public static final int FAILED = 500;
    //private static StackTraceElement traceElement;

    /**
     *
     * 可做成http任务池
     */
    //private static HttpClient httpClient;
    //private static HttpPost httpPost;

    /**
     * 向服务器提交数据，返回int类型结果码
     *
     * @param url
     * @param params
     * @return
     */
    public static int postData(String url, HashMap<String, Object> params) {
        int resultCode = UNKOWN_ERROR;
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            //nameValuePairs.add(new BasicNameValuePair(key, (String)params.get(key)));
            nameValuePairs.add(new BasicNameValuePair(key, String.valueOf(params.get(key))));
        }
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                JSONObject result = new JSONObject(EntityUtils.toString(response.getEntity()));
                Log.i(TAG, result.toString());
                resultCode = result.getInt("code");
            }
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "UnsupportedEncodingException at line ");
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Log.i(TAG, "ClientProtocolException at line ");
            e.printStackTrace();
            resultCode = INTERNET_ERROR;
        } catch (IOException e) {
            Log.i(TAG, "IOException at line ");
            e.printStackTrace();
            resultCode = INTERNET_ERROR;
        } catch (JSONException e) {
            Log.i(TAG, "JSONException at line ");
            e.printStackTrace();
        } finally {
            httpPost.abort();
        }
        //resultCode等于200时成功，等于500时失败，等于300时无数据
        return resultCode;
    }

    /**
     * 向服务器请求单条数据，返回JSONObject
     *
     * @param url
     * @param params
     * @return
     */
    public static JSONObject getData(String url, HashMap<String, Object> params) {
        JSONObject resultObject = null;
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIMEOUT);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            //nameValuePairs.add(new BasicNameValuePair(key, (String)params.get(key)));
            nameValuePairs.add(new BasicNameValuePair(key, String.valueOf(params.get(key))));
        }
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                resultObject = new JSONObject(EntityUtils.toString(response.getEntity()));
                Log.i(TAG, resultObject.toString());
            } else {
                //TODO
            }
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "UnsupportedEncodingException at line ");
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Log.i(TAG, "ClientProtocolException at line ");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, "IOException at line ");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.i(TAG, "JSONException at line ");
            e.printStackTrace();
        } finally {
            httpPost.abort();
        }
        //resultObject不等于null时取数据成功，否则失败
        return resultObject;
    }

    public static void postImageFile(final String userID, final String url, final byte[] imgBytes, final SaveImageCallback callback) {
        final MultipartEntity entity = new MultipartEntity();
        entity.addPart("image", new ByteArrayBody(imgBytes, "pic_head.png"));
        try {
            entity.addPart(UsedFields.DBUserInfo.USER_ID, new StringBody(userID));
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "UnsupportedEncodingException at line ");
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, CONNECTION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(httpParams, CONNECTION_TIMEOUT);
                HttpClient httpClient = new DefaultHttpClient(httpParams);
                HttpPost httpPost = new HttpPost(url);
                try {
                    httpPost.setEntity(entity);
                    HttpResponse response = httpClient.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        JSONObject result = new JSONObject(EntityUtils.toString(response.getEntity()));
                        if (result.getInt("code") == SUCCESS) {
                            //callback.onSaved(UsedFields.IMG_PREFIX + result.getString("data"));
                            callback.onSaved(result.getString("data"));
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    Log.i(TAG, "UnsupportedEncodingException at line ");
                    e.printStackTrace();
                } catch (ClientProtocolException e) {
                    Log.i(TAG, "ClientProtocolException at line ");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.i(TAG, "IOException at line ");
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.i(TAG, "JSONException at line ");
                    e.printStackTrace();
                } finally {
                    httpPost.abort();
                }
            }
        }).start();
    }

    public interface SaveImageCallback {
        public void onSaved(String savedImageName);
    }

}
