package com.jamie.express.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jamie.express.R;
import com.jamie.express.utils.DeviceUuidFactory;
import com.jamie.express.utils.EncryptionTools;
import com.jamie.express.utils.ExpressSharedPreference;
import com.jamie.express.utils.HttpUtil;
import com.jamie.express.utils.ImageLoader;
import com.jamie.express.utils.RequestUrl;
import com.jamie.express.utils.UsedFields;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";

    private Button btn_confrim;
    private Button btn_reset;
    private EditText et_userid;
    private EditText et_password;
    private ImageView imgHead;
    private ExpressSharedPreference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_confrim = (Button) this.findViewById(R.id.login_btn_confirm);
        btn_reset = (Button) this.findViewById(R.id.login_btn_reset);
        et_userid = (EditText) this.findViewById(R.id.login_et_userid);
        et_password = (EditText) this.findViewById(R.id.login_et_password);
        imgHead = (ImageView) this.findViewById(R.id.login_img_head);
        preference = new ExpressSharedPreference(LoginActivity.this);
        et_userid.setText(preference.getUserId());
        et_password.setText(preference.getPassword());
        imgHead.setScaleType(ImageView.ScaleType.FIT_XY);
        if (preference.getUserId() != null && preference.getIcon() != null) {
            ImageLoader.loadBitmap(RequestUrl.IMG_URL + preference.getIcon(), new ImageLoader.BitmapCallback() {
                @Override
                public void onGotBitmap(Bitmap bitmap) {
                    if (bitmap != null) {
                        imgHead.setImageBitmap(bitmap);
                    }
                }
            });
        }
        btn_confrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_userid.getText().toString().trim().equals("") && !et_password.getText().toString().trim().equals("")) {
                    new LoginTask().execute(et_userid.getText().toString().trim(), et_password.getText().toString().trim());
                } else {
                    Toast.makeText(getApplicationContext(), "账号或密码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_userid.setText("");
                et_password.setText("");
                preference.clearUserInfo();
            }
        });
        JPushInterface.setDebugMode(true);
        JPushInterface.init(getApplicationContext());
        //JPushInterface.resumePush(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        JPushInterface.onResume(LoginActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        JPushInterface.onPause(LoginActivity.this);
    }

    private class LoginTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btn_reset.setEnabled(false);
            btn_confrim.setEnabled(false);
            et_password.setEnabled(false);
            et_userid.setEnabled(false);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            HashMap<String, Object> paramList = new HashMap<String, Object>();
            paramList.put(UsedFields.DBUserInfo.USER_ID, params[0]);
            paramList.put(UsedFields.DBUserInfo.PASSWORD, EncryptionTools.SHA1LowerCase(params[1]));
            JSONObject result = HttpUtil.getData(RequestUrl.LOGIN_URL, paramList);
            return result;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject == null) {
                Toast.makeText(LoginActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                btn_reset.setEnabled(true);
                btn_confrim.setEnabled(true);
                et_password.setEnabled(true);
                et_userid.setEnabled(true);
                return;
            }
            try {
                int resultCode = jsonObject.getInt("code");
                if (resultCode == HttpUtil.FAILED) {
                    Toast.makeText(getApplicationContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    btn_reset.setEnabled(true);
                    btn_confrim.setEnabled(true);
                    et_password.setEnabled(true);
                    et_userid.setEnabled(true);
                } else if (resultCode == HttpUtil.SUCCESS) {
                    Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
                    preference.setPsw(et_password.getText().toString().trim());
                    preference.setUserInfo(jsonObject.getJSONObject("data"));
                    if (preference.getAlias()) {
                        String alias = new DeviceUuidFactory(LoginActivity.this).getDeviceUuid().toString().trim() + preference.getUserId();
                        alias = EncryptionTools.SHA1LowerCase(alias);
                        JPushInterface.setAlias(getApplicationContext(), alias, new TagAliasCallback() {
                            @Override
                            public void gotResult(int responseCode, final String responseAlias, Set<String> tags) {
                                if (responseCode == 0) {
                                    //TODO 出错了。。。。
                                    //Toast.makeText(getApplicationContext(), "试试看执行了没有。。。", Toast.LENGTH_SHORT).show();
                                    new AsyncTask<String, Void, Integer>() {

                                        @Override
                                        protected Integer doInBackground(String... params) {
                                            Log.i(TAG, params[0]);
                                            HashMap<String, Object> paramList = new HashMap<String, Object>();
                                            paramList.put(UsedFields.DBUserInfo.USER_ID, preference.getUserId());
                                            paramList.put(UsedFields.DBUserInfo.ALIAS, params[0]);
                                            int resultCode = HttpUtil.postData(RequestUrl.SET_ALIAS_URL, paramList);
                                            return resultCode;
                                        }

                                        @Override
                                        protected void onPostExecute(Integer integer) {
                                            super.onPostExecute(integer);
                                            if (integer == HttpUtil.SUCCESS) {
                                                preference.setAlias();
                                                Toast.makeText(getApplicationContext(), "Alias保存成功", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getApplicationContext(), "Alias保存失败", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }.execute(responseAlias);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Alias设置失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    Intent intent = new Intent(LoginActivity.this, ExpressActivity.class);
                    startActivity(intent);
                    LoginActivity.this.finish();
                } else {
                    Toast.makeText(getApplicationContext(), "网络出错", Toast.LENGTH_SHORT).show();
                    btn_reset.setEnabled(true);
                    btn_confrim.setEnabled(true);
                    et_password.setEnabled(true);
                    et_userid.setEnabled(true);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
