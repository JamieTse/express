package com.jamie.express.activities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jamie.express.R;
import com.jamie.express.utils.CommonFunction;
import com.jamie.express.utils.ExpressSharedPreference;
import com.jamie.express.utils.HttpUtil;
import com.jamie.express.utils.ImageLoader;
import com.jamie.express.utils.PushMessages;
import com.jamie.express.utils.RequestUrl;
import com.jamie.express.utils.UsedFields;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ViewDetailActivity extends AppCompatActivity {

    public static final String TAG = "ViewDetailActivity";
    public static final String STATE = "state";
    public static final String IS_APPLYER = "isApplyer";
    public static final String BUNDLE_NAME = "expressage";
    public static final int STATE_NOT_YET = 0;
    public static final int STATE_NOT_DONE = 1;
    public static final int STATE_DISABLED = 2;
    public static final int STATE_EXPIRED = 3;
    public static final int STATE_DONE = 4;
    public static final int STATE_ERROR = 11;

    private TextView tvName;
    private TextView tvSubs;
    private TextView tvReward;
    private TextView tvAddr;
    private TextView tvDeadline;
    private TextView tvDescr;
    private TextView tvApplyTime;
    private TextView tvIsApplyer;

    private ImageView ivHead;
    private Button btnLeft;
    private Button btnRight;
    private LinearLayout linearPersonBlock;
    private LinearLayout linearApplyTimeBlock;
    private ExpressSharedPreference preference;
    private Bundle args;
    private String toID;
    private String toName;
    int exID;
    int applyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_detail);
        preference = new ExpressSharedPreference(ViewDetailActivity.this);
        tvAddr = (TextView) this.findViewById(R.id.avd_tv_addr);
        tvDeadline = (TextView) this.findViewById(R.id.avd_tv_deadline);
        tvDescr = (TextView) this.findViewById(R.id.avd_tv_description);
        tvName = (TextView) this.findViewById(R.id.avd_tv_name);
        tvReward = (TextView) this.findViewById(R.id.avd_tv_reward);
        tvSubs = (TextView) this.findViewById(R.id.avd_tv_substance);
        tvApplyTime = (TextView) this.findViewById(R.id.avd_tv_apply_time);
        tvIsApplyer = (TextView) this.findViewById(R.id.avd_tv_is_applyer);
        linearApplyTimeBlock = (LinearLayout) this.findViewById(R.id.avd_linear_apply_time);
        linearPersonBlock = (LinearLayout) this.findViewById(R.id.avd_linear_applyer);
        ivHead = (ImageView) this.findViewById(R.id.avd_img_head);
        btnLeft = (Button) this.findViewById(R.id.avd_btn_left);
        btnRight = (Button) this.findViewById(R.id.avd_btn_right);
        btnLeft.setEnabled(false);
        btnRight.setEnabled(false);
        args = getIntent().getBundleExtra(BUNDLE_NAME);
        if (args.getBoolean(IS_APPLYER)) {
            applyerInit();
            Log.i(TAG, args.getString(UsedFields.DBApplyRecord._OWNER_NAME));
        } else {
            ownerInit();
            Log.i(TAG, args.getString(UsedFields.DBExpressage.APPLY_ID));
        }
    }

    private void ownerInit() {
        tvIsApplyer.setText("领取人：");
        btnLeft.setText("确认收到");
        btnRight.setText("过期未送");
        exID = args.getInt(UsedFields.ID);
        applyID = Integer.valueOf(args.getString(UsedFields.DBExpressage.APPLY_ID));
        if (applyID == 0) {
            hidePersonBlock();
        } else {
            showPersonBlock();
            new GetApplyerTask().execute();
            tvApplyTime.setText(CommonFunction.formatDateTime(args.getString(UsedFields.DBExpressage._APPLY_TIME)));
        }
        if (args.getInt(STATE) == STATE_NOT_DONE) {
            btnLeft.setEnabled(true);
            btnRight.setEnabled(true);
            btnLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DoneMissionTask().execute();
                }
            });
            btnRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new ExpireMissionTask().execute();
                }
            });
        }
        tvAddr.setText(args.getString(UsedFields.DBExpressage.ADDR));
        tvDeadline.setText(CommonFunction.formatDateTime(args.getString(UsedFields.DBExpressage.DEADLINE)));
        tvDescr.setText(args.getString(UsedFields.DBExpressage.DESCRIPTION));
        tvReward.setText(args.getString(UsedFields.DBExpressage.REWARD));
        tvSubs.setText(args.getString(UsedFields.DBExpressage.SUBSTANCE));
    }

    private void applyerInit() {
        tvIsApplyer.setText("发布人：");
        showPersonBlock();
        btnLeft.setText("提醒确认");
        btnRight.setText("取消代领");
        exID = Integer.valueOf(args.getString(UsedFields.DBApplyRecord.EX_ID));
        applyID = args.getInt(UsedFields.ID);
        toID = args.getString(UsedFields.DBApplyRecord.EX_OWNER);
        toName = args.getString(UsedFields.DBApplyRecord._OWNER_NAME);
        if (args.getInt(STATE) == STATE_NOT_DONE) {
            btnLeft.setEnabled(true);
            btnRight.setEnabled(true);
            btnLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new SendMessageTask().execute(args.getString(UsedFields.DBApplyRecord._APPLYER_NAME) + "提醒您确认收到快递。");
                }
            });
            btnRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DisableMissionTask().execute();
                }
            });
        }
        tvAddr.setText(args.getString(UsedFields.DBApplyRecord._EX_ADDR));
        tvDeadline.setText(CommonFunction.formatDateTime(args.getString(UsedFields.DBApplyRecord._EX_DEADLINE)));
        tvDescr.setText(args.getString(UsedFields.DBApplyRecord._EX_DESCRIPTION));
        tvReward.setText(args.getString(UsedFields.DBApplyRecord._EX_REWARD));
        tvSubs.setText(args.getString(UsedFields.DBApplyRecord._EX_SUBSTANCE));
        tvApplyTime.setText(CommonFunction.formatDateTime(args.getString(UsedFields.DBApplyRecord.CREATE_TIME)));
        tvName.setText(args.getString(UsedFields.DBApplyRecord._APPLYER_NAME));
        ImageLoader.loadBitmap(RequestUrl.IMG_URL + args.getString(UsedFields.DBApplyRecord._APPLYER_ICON), new ImageLoader.BitmapCallback() {
            @Override
            public void onGotBitmap(Bitmap bitmap) {
                ivHead.setImageBitmap(bitmap);
            }
        });
    }

    private void hidePersonBlock() {
        linearPersonBlock.setVisibility(View.GONE);
        linearApplyTimeBlock.setVisibility(View.GONE);
    }

    private void showPersonBlock() {
        linearPersonBlock.setVisibility(View.VISIBLE);
        linearApplyTimeBlock.setVisibility(View.VISIBLE);
    }

    private class SendMessageTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            return PushMessages.pushMessage(preference, toID, toName, params[0], exID, applyID);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            executeResult(integer);
        }
    }

    private class DoneMissionTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return PushMessages.pushMissionDone(preference, toID, toName, exID, applyID);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            executeResult(integer);
        }
    }

    private class DisableMissionTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return PushMessages.pushDisabled(preference, toID, toName, exID, applyID);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            executeResult(integer);
        }
    }

    private class ExpireMissionTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            return PushMessages.pushExpired(preference, toID, toName, exID, applyID);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            executeResult(integer);
        }
    }

    private void executeResult(int integer) {
        if (integer == PushMessages.SUCCESS) {
            Toast.makeText(getApplicationContext(), "操作成功", Toast.LENGTH_SHORT).show();
            ViewDetailActivity.this.finish();
            return;
        } else if (integer == PushMessages.FAILED_PUSHING) {
            Toast.makeText(getApplicationContext(), "消息发送失败", Toast.LENGTH_SHORT).show();
            return;
        } else if (integer == PushMessages.FAILED_SAVING) {
            Toast.makeText(getApplicationContext(), "操作失败", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(getApplicationContext(), "网络出错", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private class GetApplyerTask extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... params) {
            HashMap<String, Object> paramList = new HashMap<String, Object>();
            paramList.put(UsedFields.DBApplyRecord.APPLY_ID, args.getString(UsedFields.DBExpressage.APPLY_ID));
            return HttpUtil.getData(RequestUrl.GET_APPLY_URL, paramList);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            if (jsonObject != null) {
                try {
                    int resultCode = jsonObject.getInt("code");
                    if (resultCode == HttpUtil.SUCCESS) {
                        JSONObject result = jsonObject.getJSONObject("data");
                        toName = result.getString(UsedFields.DBApplyRecord._APPLYER_NAME);
                        toID = result.getString(UsedFields.DBApplyRecord.EX_APPLYER);
                        tvName.setText(toName);
                        ImageLoader.loadBitmap(RequestUrl.IMG_URL + result.getString(UsedFields.DBApplyRecord._APPLYER_ICON), new ImageLoader.BitmapCallback() {
                            @Override
                            public void onGotBitmap(Bitmap bitmap) {
                                ivHead.setImageBitmap(bitmap);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return;
            }
            Toast.makeText(getApplicationContext(), "网络繁忙", Toast.LENGTH_SHORT).show();
        }
    }
}
