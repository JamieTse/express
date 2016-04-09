package com.jamie.express.listviews;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jamie.express.R;

import java.util.Date;

/**
 * Created by jamie on 2016/3/22.
 */
public class RefreshableListView extends ListView implements AbsListView.OnScrollListener {

    public static final String TAG = "RefreshableListView";

    private final static int RELEASE_TO_REFRESH = 0;
    private final static int PULL_TO_REFRESH = 1;
    private final static int REFRESHING = 2;
    private final static int DONE = 3;
    private final static int LOADING = 4;

    private final static int RATIO = 3;
    private LayoutInflater inflater;

    private LinearLayout headerView;
    private TextView lvHeaderTipsTv;
    private TextView lvHeaderLastUpdateTv;
    private ImageView lvHeaderArrowIv;
    private ProgressBar lvHeaderProgressBar;

    private int headerContentHeight;

    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;

    private int startY;
    private int state;
    private boolean isBack;
    private boolean isRecorded;

    private OnRefreshListener refreshListener;

    private boolean isRefreshable;
    private boolean isLoadable;

    public RefreshableListView(Context context) {
        super(context);
        init(context);
    }

    public RefreshableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setCacheColorHint(context.getResources().getColor(R.color.transparent));
        inflater = LayoutInflater.from(context);

        headerView = (LinearLayout) inflater.inflate(R.layout.refreshable_lv_header, null);
        lvHeaderTipsTv = (TextView) headerView.findViewById(R.id.lvHeaderTipsTv);
        lvHeaderLastUpdateTv = (TextView) headerView.findViewById(R.id.lvHeaderLastUpdateTv);
        lvHeaderArrowIv = (ImageView) headerView.findViewById(R.id.lvHeaderArrowIv);
        lvHeaderArrowIv.setMinimumWidth(70);
        lvHeaderArrowIv.setMinimumHeight(50);
        lvHeaderProgressBar = (ProgressBar) headerView.findViewById(R.id.lvHeaderProgressBar);

        measureView(headerView);
        headerContentHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -1 * headerContentHeight, 0, 0);
        headerView.invalidate();
        addHeaderView(headerView, null, false);

        setOnScrollListener(RefreshableListView.this);

        animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(250);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(200);
        reverseAnimation.setFillAfter(true);

        state = DONE;
        isRefreshable = false;
        isLoadable = false;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            isRefreshable = true;
        } else {
            isRefreshable = false;
        }
        if (view.getLastVisiblePosition() == view.getCount() - 1 && isLoadable) {
            if (refreshListener != null) {
                refreshListener.onLoadData();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isRefreshable) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!isRecorded) {
                        isRecorded = true;
                        startY = (int) ev.getY();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (state != REFRESHING && state != LOADING) {
                        if (state == PULL_TO_REFRESH) {
                            state = DONE;
                            changeHeaderViewByState();
                        }
                        if (state == RELEASE_TO_REFRESH) {
                            state = REFRESHING;
                            changeHeaderViewByState();
                            onLvRefresh();
                        }
                    }
                    isRecorded = false;
                    isBack = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    int tempY = (int) ev.getY();
                    if (!isRecorded) {
                        isRecorded = true;
                        startY = tempY;
                    }
                    if (state != REFRESHING && isRecorded && state != LOADING) {
                        if (state == RELEASE_TO_REFRESH) {
                            setSelection(0);
                            if (((tempY - startY) / RATIO < headerContentHeight) && (tempY - startY) > 0) {
                                state = PULL_TO_REFRESH;
                                //changeHeaderViewByState();
                            } else if (tempY - startY <= 0) {
                                state = DONE;
                                //changeHeaderViewByState();
                            }
                            changeHeaderViewByState();
                        }
                        if (state == PULL_TO_REFRESH) {
                            setSelection(0);
                            if ((tempY - startY) / RATIO >= headerContentHeight) {
                                state = RELEASE_TO_REFRESH;
                                isBack = true;
                                //changeHeaderViewByState();
                            } else if (tempY - startY <= 0) {
                                state = DONE;
                                //changeHeaderViewByState();
                            }
                            changeHeaderViewByState();
                        }
                        if (state == DONE) {
                            if (tempY - startY > 0) {
                                state = PULL_TO_REFRESH;
                                changeHeaderViewByState();
                            }
                        }
                        if (state == PULL_TO_REFRESH) {
                            headerView.setPadding(0, -1 * headerContentHeight + (tempY - startY) / RATIO, 0, 0);
                        }
                        if (state == RELEASE_TO_REFRESH) {
                            headerView.setPadding(0, (tempY - startY) / RATIO - headerContentHeight, 0, 0);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    private void changeHeaderViewByState() {
        switch (state) {
            case RELEASE_TO_REFRESH:
                lvHeaderArrowIv.setVisibility(View.VISIBLE);
                lvHeaderProgressBar.setVisibility(View.GONE);
                lvHeaderTipsTv.setVisibility(View.VISIBLE);
                lvHeaderLastUpdateTv.setVisibility(View.VISIBLE);

                lvHeaderArrowIv.clearAnimation();
                lvHeaderArrowIv.startAnimation(animation);
                lvHeaderTipsTv.setText("松开刷新");
                break;
            case PULL_TO_REFRESH:
                lvHeaderProgressBar.setVisibility(View.GONE);
                lvHeaderTipsTv.setVisibility(View.VISIBLE);
                lvHeaderLastUpdateTv.setVisibility(View.VISIBLE);
                lvHeaderArrowIv.clearAnimation();
                lvHeaderArrowIv.setVisibility(View.VISIBLE);
                if (isBack) {
                    isBack = false;
                    //lvHeaderArrowIv.clearAnimation();
                    lvHeaderArrowIv.startAnimation(reverseAnimation);
                    //lvHeaderTipsTv.setText("下拉刷新");
                } else {
                    //lvHeaderTipsTv.setText("下拉刷新");
                }
                lvHeaderTipsTv.setText("下拉刷新");
                break;
            case REFRESHING:
                headerView.setPadding(0, 0, 0, 0);
                lvHeaderProgressBar.setVisibility(View.VISIBLE);
                lvHeaderArrowIv.clearAnimation();
                lvHeaderArrowIv.setVisibility(View.GONE);
                lvHeaderTipsTv.setText("正在刷新...");
                lvHeaderLastUpdateTv.setVisibility(View.VISIBLE);
                break;
            case DONE:
                headerView.setPadding(0, -1 * headerContentHeight, 0, 0);
                lvHeaderProgressBar.setVisibility(View.GONE);
                lvHeaderArrowIv.clearAnimation();
                lvHeaderArrowIv.setImageResource(R.drawable.ic_arrow_down);
                lvHeaderTipsTv.setText("下拉刷新");
                lvHeaderLastUpdateTv.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, params.width);
        int lpHeight = params.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
        isRefreshable = true;
        isLoadable = true;
    }

    public interface OnRefreshListener {
        public void onRefresh();

        public void onLoadData();
    }

    public void onRefreshComplete() {
        state = DONE;
        lvHeaderLastUpdateTv.setText("最近更新" + new Date().toLocaleString());
        changeHeaderViewByState();
    }

    private void onLvRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }

    public void setAdapter(BaseAdapter adapter) {
        lvHeaderLastUpdateTv.setText("最近更新" + new Date().toLocaleString());
        super.setAdapter(adapter);
    }

}
