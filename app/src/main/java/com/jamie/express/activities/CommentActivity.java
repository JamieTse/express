package com.jamie.express.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.jamie.express.R;
import com.jamie.express.utils.ExpressSharedPreference;
import com.jamie.express.utils.PushMessages;
import com.jamie.express.utils.UsedFields;

public class CommentActivity extends AppCompatActivity {

    public static final String TAG = "CommentActivity";

    private ExpressSharedPreference preference;
    private RatingBar ratingBarRemark;
    private EditText etContent;
    private Button btnCancel;
    private Button btnConfirm;

    private int applyID;
    private int expressageID;
    private String toUser;
    private String toUserName;
    private int type;
    private boolean toWhom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        preference = new ExpressSharedPreference(CommentActivity.this);
        Intent fromIntent = CommentActivity.this.getIntent();
        Bundle args = fromIntent.getBundleExtra("credit");
        applyID = args.getInt(UsedFields.DBCreditRecord.APPLY_ID);
        expressageID = args.getInt(UsedFields.DBCreditRecord.EX_ID);
        type = args.getInt(UsedFields.DBCreditRecord.TYPE);
        toUser = args.getString(UsedFields.DBCreditRecord.TO_USER);
        toUserName = args.getString("toUserName");
        toWhom = args.getBoolean("toWhom");
        ratingBarRemark = (RatingBar) this.findViewById(R.id.ac_ratingBar_remark);
        etContent = (EditText) this.findViewById(R.id.ac_et_content);
        btnCancel = (Button) this.findViewById(R.id.ac_btn_cancel);
        btnConfirm = (Button) this.findViewById(R.id.ac_btn_confirm);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (checkInputs()) {
                    int rating = (int) ratingBarRemark.getRating();
                    String content = etContent.getText().toString();
                    new CommentTask().execute(rating, content);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.input_empty, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkInputs() {
        if (etContent.getText().toString().equals(null) || etContent.getText().toString().trim().equals("")) {
            return false;
        }
        return true;
    }

    private class CommentTask extends AsyncTask<Object, Void, Integer> {

        @Override
        protected Integer doInBackground(Object... params) {
            return PushMessages.pushCredit(preference, toUser, toUserName, (String) params[1], expressageID, applyID, type, (int) params[0], toWhom);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (integer == PushMessages.SUCCESS) {
                Toast.makeText(getApplicationContext(), R.string.success_executing, Toast.LENGTH_SHORT).show();
                CommentActivity.this.finish();
                return;
            } else if (integer == PushMessages.FAILED_PUSHING) {
                Toast.makeText(getApplicationContext(), R.string.failed_pushing, Toast.LENGTH_SHORT).show();
                return;
            } else if (integer == PushMessages.FAILED_SAVING) {
                Toast.makeText(getApplicationContext(), R.string.failed_executing, Toast.LENGTH_SHORT).show();
                return;
            } else {
                Toast.makeText(getApplicationContext(), R.string.network_busy, Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }
}
