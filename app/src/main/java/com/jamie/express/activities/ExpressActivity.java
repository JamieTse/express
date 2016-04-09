package com.jamie.express.activities;

import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.jamie.express.R;
import com.jamie.express.fragments.ApplyRecordFragment;
import com.jamie.express.fragments.CreditRecordFragment;
import com.jamie.express.fragments.ExpressagesFragment;
import com.jamie.express.fragments.MessagesFragment;
import com.jamie.express.fragments.PersonInfoFragment;
import com.jamie.express.fragments.PostFragment;
import com.jamie.express.fragments.PostRecordFragment;
import com.jamie.express.fragments.ToCommentsFragment;
import com.jamie.express.utils.ExpressSharedPreference;
import com.jamie.express.utils.HttpUtil;
import com.jamie.express.utils.ImageLoader;
import com.jamie.express.utils.RequestUrl;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class ExpressActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "ExpressActivity";
    public static int TAKE_PHOTO = 1000;
    public static int PICK_PHOTO = 1001;

    private int currItem;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View nav_header;
    ExpressSharedPreference preference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_express);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        nav_header = navigationView.inflateHeaderView(R.layout.nav_header_express);
        preference = new ExpressSharedPreference(this);

        final ImageView ivHead = (ImageView) nav_header.findViewById(R.id.iv_main_head);
        TextView tvName = (TextView) nav_header.findViewById(R.id.tv_main_name);
        TextView tvID = (TextView) nav_header.findViewById(R.id.tv_main_id);
        ivHead.setScaleType(ImageView.ScaleType.FIT_XY);
        ImageLoader.loadBitmap(RequestUrl.IMG_URL + preference.getIcon(), new ImageLoader.BitmapCallback() {
            @Override
            public void onGotBitmap(Bitmap bitmap) {
                ivHead.setImageBitmap(bitmap);
                ivHead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(ExpressActivity.this, "修改头像", Toast.LENGTH_SHORT).show();
                        //drawer.closeDrawer(GravityCompat.START);
                        View popView = ExpressActivity.this.getLayoutInflater().inflate(R.layout.modify_head_popup, null);
                        Button takeHead = (Button) popView.findViewById(R.id.btn_take_head);
                        Button pickHead = (Button) popView.findViewById(R.id.btn_pick_head);
                        Button cancelHead = (Button) popView.findViewById(R.id.btn_cancel_head);
                        final PopupWindow popupWindow = new PopupWindow(popView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                        popupWindow.setBackgroundDrawable(new BitmapDrawable());
                        popupWindow.setAnimationStyle(R.style.animationPopup);
                        popupWindow.showAtLocation(ivHead, Gravity.BOTTOM, 0, 0);
                        takeHead.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(intent, TAKE_PHOTO);
                            }
                        });
                        pickHead.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                intent.putExtra("crop", true);
                                intent.putExtra("return-data", true);
                                startActivityForResult(intent, PICK_PHOTO);
                            }
                        });
                        cancelHead.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });
                    }
                });
            }
        });
        tvName.setText(preference.getUserName());
        tvID.setText(preference.getUserId());

        goToFragment(R.id.nav_expressages);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView ivHead = (ImageView) nav_header.findViewById(R.id.iv_main_head);
        ivHead.setScaleType(ImageView.ScaleType.FIT_XY);
        byte[] imgBytes;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
                ivHead.setImageBitmap(bitmap);
                imgBytes = outputStream.toByteArray();
                HttpUtil.postImageFile(preference.getUserId(), RequestUrl.UPLOAD_ICON_URL, imgBytes, new HttpUtil.SaveImageCallback() {
                    @Override
                    public void onSaved(String savedImageName) {
                        preference.setIcon(savedImageName);
                    }
                });
            }
        } else if (requestCode == PICK_PHOTO && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            ContentResolver resolver = ExpressActivity.this.getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri));
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
                    ivHead.setImageBitmap(bitmap);
                    imgBytes = outputStream.toByteArray();
                    HttpUtil.postImageFile(preference.getUserId(), RequestUrl.UPLOAD_ICON_URL, imgBytes, new HttpUtil.SaveImageCallback() {
                        @Override
                        public void onSaved(String savedImageName) {
                            preference.setIcon(savedImageName);
                        }
                    });
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    //自定义函数
    public void goToFragment(int id) {
        //currItem = id;
        navigationView.setCheckedItem(id);
        //TODO 添加fragment
        FragmentManager fragmentManager = getFragmentManager();

        if (id == currItem) {
            // Handle the camera action
        } else if (id == R.id.nav_expressages) {
            // Handle the camera action
            ExpressagesFragment expressagesFragment = ExpressagesFragment.getInstance();
            fragmentManager.beginTransaction().replace(R.id.main_container, expressagesFragment).commit();
        } else if (id == R.id.nav_post) {
            PostFragment postFragment = new PostFragment();
            fragmentManager.beginTransaction().replace(R.id.main_container, postFragment).commit();
        } else if (id == R.id.nav_post_records) {
            PostRecordFragment postRecordFragment = new PostRecordFragment();
            fragmentManager.beginTransaction().replace(R.id.main_container, postRecordFragment).commit();
        } else if (id == R.id.nav_apply_records) {
            ApplyRecordFragment applyRecordFragment = new ApplyRecordFragment();
            fragmentManager.beginTransaction().replace(R.id.main_container, applyRecordFragment).commit();
        } else if (id == R.id.nav_to_comments) {
            ToCommentsFragment toCommentsFragment = new ToCommentsFragment();
            fragmentManager.beginTransaction().replace(R.id.main_container, toCommentsFragment).commit();
        } else if (id == R.id.nav_personal) {
            PersonInfoFragment personInfoFragment = new PersonInfoFragment();
            fragmentManager.beginTransaction().replace(R.id.main_container, personInfoFragment).commit();
        } else if (id == R.id.nav_credit) {
            CreditRecordFragment creditRecordFragment = new CreditRecordFragment();
            fragmentManager.beginTransaction().replace(R.id.main_container, creditRecordFragment).commit();
        } else if (id == R.id.nav_messages) {
            MessagesFragment messagesFragment = new MessagesFragment();
            fragmentManager.beginTransaction().replace(R.id.main_container, messagesFragment).commit();
        } else if (id == R.id.nav_exit) {
            preference.clearUserInfo();
            Intent intent = new Intent(ExpressActivity.this, LoginActivity.class);
            startActivity(intent);
            ExpressActivity.this.finish();
        }
        currItem = id;

        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.express, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            preference.setSearchCondition();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        goToFragment(id);

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
