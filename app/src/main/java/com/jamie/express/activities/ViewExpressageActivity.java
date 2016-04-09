package com.jamie.express.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jamie.express.R;
import com.jamie.express.utils.CommonFunction;
import com.jamie.express.utils.ExpressSharedPreference;
import com.jamie.express.utils.ImageLoader;
import com.jamie.express.utils.PushMessages;
import com.jamie.express.utils.RequestUrl;
import com.jamie.express.utils.UsedFields;

public class ViewExpressageActivity extends AppCompatActivity {

    public static final String TAG = "ViewExpressageActivity";
    private int expressage_id;

    private TextView tvName;
    private TextView tvSubs;
    private TextView tvReward;
    private TextView tvAddr;
    private TextView tvDeadline;
    private TextView tvDescr;
    private ImageView ivHead;
    private Button btnHelp;
    private ExpressSharedPreference preference;

    public static final int DID_HELP = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_expressage);
        preference = new ExpressSharedPreference(ViewExpressageActivity.this);
        final Intent fromIntent = ViewExpressageActivity.this.getIntent();
        final Bundle args = fromIntent.getBundleExtra("expressage");
        final long listID = fromIntent.getLongExtra("listID", -1);
        expressage_id = args.getInt(UsedFields.ID);
        tvAddr = (TextView) this.findViewById(R.id.ave_tv_addr);
        tvDeadline = (TextView) this.findViewById(R.id.ave_tv_deadline);
        tvDescr = (TextView) this.findViewById(R.id.ave_tv_description);
        tvName = (TextView) this.findViewById(R.id.ave_tv_name);
        tvReward = (TextView) this.findViewById(R.id.ave_tv_reward);
        tvSubs = (TextView) this.findViewById(R.id.ave_tv_substance);
        ivHead = (ImageView) this.findViewById(R.id.ave_iv_head);
        btnHelp = (Button) this.findViewById(R.id.ave_btn_help);
        preference = new ExpressSharedPreference(ViewExpressageActivity.this);
        tvSubs.setText(args.getString(UsedFields.DBExpressage.SUBSTANCE));
        tvReward.setText(args.getString(UsedFields.DBExpressage.REWARD) + "元");
        tvName.setText(args.getString(UsedFields.DBExpressage._USER_NAME));
        tvDescr.setText(args.getString(UsedFields.DBExpressage.DESCRIPTION));
        tvAddr.setText("从" + args.getString(UsedFields.DBExpressage.ADDR) + "送到" + args.getString(UsedFields.DBExpressage._USER_ADDR));
        tvDeadline.setText(CommonFunction.formatDateTime(args.getString(UsedFields.DBExpressage.DEADLINE)));
        ivHead.setScaleType(ImageView.ScaleType.FIT_XY);
        ImageLoader.loadBitmap(RequestUrl.IMG_URL + args.getString(UsedFields.DBExpressage._USER_ICON), new ImageLoader.BitmapCallback() {
            @Override
            public void onGotBitmap(Bitmap bitmap) {
                ivHead.setImageBitmap(bitmap);
            }
        });
//        if (args.getString(UsedFields.DBExpressage.FROM_USER).equals(preference.getUserId())) {
//            btnHelp.setEnabled(false);
//        }
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO new Task
                if (args.getString(UsedFields.DBExpressage.FROM_USER).equals(preference.getUserId())) {
                    Toast.makeText(getApplicationContext(), "您不能领取自己发布的快递", Toast.LENGTH_SHORT).show();
                    ViewExpressageActivity.this.finish();
                    return;
                }
                new DoHelpTask().execute(args.getString(UsedFields.DBExpressage.FROM_USER));
                Intent intent = new Intent();
                intent.putExtra("listID", listID);
                ViewExpressageActivity.this.setResult(DID_HELP, intent);
            }
        });
    }

    private class DoHelpTask extends AsyncTask<Object, Void, Integer> {

        @Override
        protected Integer doInBackground(Object... params) {
            return PushMessages.pushApply(preference, (String) params[0], expressage_id);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer == PushMessages.SUCCESS) {
                Toast.makeText(getApplicationContext(), "操作成功", Toast.LENGTH_SHORT).show();
                ViewExpressageActivity.this.finish();
                return;
            } else if (integer == PushMessages.FAILED_PUSHING) {
                Toast.makeText(getApplicationContext(), "发送消息失败", Toast.LENGTH_SHORT).show();
                return;
            } else if (integer == PushMessages.FAILED_SAVING) {
                Toast.makeText(getApplicationContext(), "操作失败", Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(getApplicationContext(), "网络出错", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
}
