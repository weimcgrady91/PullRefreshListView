package com.qun.lib.pullrefreshlistview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Administrator on 2018/3/30 0030.
 */

public class PullRefreshListView extends ListView {

    private long mLastRefreshTime;
    private Context mContext;
    private int mHeaderViewHeight;
    private boolean mIsSliderTop;
    private View mHeaderView;
    private RefreshState mState = RefreshState.IDLE;
    private TextView mTvState;
    private OnFetchDataListener mOnFetchDataListener;
    private ImageView mIvState;
    private ProgressBar mPb;
    private TextView mTvLastRefreshTime;
    private View mFooterView;
    private boolean mIsSliderBottom;
    private TextView mTvLoadMoreState;
    private boolean mNoMoreData;
    private boolean mLoadingMoreData;
    private boolean mOpenLoadMore = true;
    private boolean mOpenPullRefresh = true;

    private enum RefreshState {
        LOADING, READY, IDLE
    }

    public PullRefreshListView(Context context) {
        this(context, null);
    }

    public PullRefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PullRefreshListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initHeaderView();
        initFooterView();
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    mIsSliderTop = true;
                } else {
                    mIsSliderTop = false;
                }
                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                    mIsSliderBottom = true;
                } else {
                    mIsSliderBottom = false;
                }
            }
        });
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(parent, view, position - 1, id);
                }
            }
        });
        setSelector(android.R.color.transparent);
    }

    public interface OnItemClickListener {
        void onItemClick(AdapterView<?> parent, View view, int position, long id);
    }

    public void setOpenLoadMore(boolean openLoadMore) {
        mOpenLoadMore = openLoadMore;
    }

    public void setOpenPullRefresh(boolean openPullRefresh) {
        mOpenPullRefresh = openPullRefresh;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    private OnItemClickListener mOnItemClickListener;

    private void initHeaderView() {
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.listview_header, null, false);
        mHeaderView.measure(0, 0);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
        addHeaderView(mHeaderView, null, true);
        mTvState = mHeaderView.findViewById(R.id.tv_state);
        mTvState.setText(R.string.pull_to_refresh);
        mIvState = findViewById(R.id.iv_state);
        mPb = mHeaderView.findViewById(R.id.pb);
        mTvLastRefreshTime = findViewById(R.id.tv_last_refresh_time);
    }

    private void initFooterView() {
        mFooterView = LayoutInflater.from(mContext).inflate(R.layout.listview_footer, null, false);
        mFooterView.measure(0, 0);
        mTvLoadMoreState = mFooterView.findViewById(R.id.tv_load_more_state);
        mTvLoadMoreState.setText(R.string.load_more_data);
        addFooterView(mFooterView, null, false);
        mFooterView.setVisibility(View.GONE);
        mFooterView.setPadding(0, -mFooterView.getMeasuredHeight(), 0, 0);
    }

    private boolean intercept;
    private int mLastY;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = (int) ev.getY();
                if (mLastRefreshTime == 0) {
                    mTvLastRefreshTime.setText(String.format(mContext.getString(R.string.last_refresh_time), "unKnow"));
                } else {
                    mTvLastRefreshTime.setText(String.format(mContext.getString(R.string.last_refresh_time),
                            formatTime(mLastRefreshTime)));
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int curY = (int) ev.getY();
                int dy = curY - mLastY;
                mLastY = curY;
                if (mState == RefreshState.LOADING) {
                    break;
                }
                if (mLoadingMoreData) {
                    break;
                }
                if (mIsSliderTop) {
                    if (mHeaderView.getPaddingTop() <= -mHeaderViewHeight && dy < 0) {
                        intercept = false;
                        if (!mOpenLoadMore) {
                            break;
                        }
                        if (mIsSliderBottom && !mNoMoreData) {
                            mFooterView.setVisibility(View.VISIBLE);
                            mFooterView.setPadding(0, 0, 0, 0);
                            mLoadingMoreData = true;
                            if (mOnFetchDataListener != null) {
                                mOnFetchDataListener.onLoadMore();
                            }
                            break;
                        }
                        break;
                    }
                    if (!mOpenPullRefresh) {
                        break;
                    }
                    mHeaderView.setPadding(0, mHeaderView.getPaddingTop() + dy / 2, 0, 0);
                    if (mHeaderView.getPaddingTop() >= 0) {
                        if (mState != RefreshState.READY) {
                            mState = RefreshState.READY;
                            startReadyAnimation();
                            mTvState.setText(R.string.release_to_refresh);
                        }
                    } else {
                        if (mState != RefreshState.IDLE) {
                            mState = RefreshState.IDLE;
                            startIdleAnimation();
                            mTvState.setText(R.string.pull_to_refresh);
                        }
                    }
                    intercept = true;
                    return true;
                }
                if (mIsSliderBottom && dy < 0) {
                    if (!mOpenLoadMore) {
                        break;
                    }
                    if (!mNoMoreData) {
                        if (mState == RefreshState.LOADING) {
                            break;
                        }
                        if (mLoadingMoreData) {
                            break;
                        }
                        mTvLoadMoreState.setText(R.string.load_more_data);
                        mFooterView.setVisibility(View.VISIBLE);
                        mFooterView.setPadding(0, 0, 0, 0);
                        mLoadingMoreData = true;
                        if (mOnFetchDataListener != null) {
                            mOnFetchDataListener.onLoadMore();
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (intercept) {
                    confirmFinalState();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void startReadyAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        mIvState.startAnimation(rotateAnimation);
    }

    private void startIdleAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(200);
        rotateAnimation.setFillAfter(true);
        mIvState.startAnimation(rotateAnimation);
    }

    private void confirmFinalState() {
        intercept = false;
        switch (mState) {
            case IDLE:
                foldHeaderView(mHeaderView.getPaddingTop(), -mHeaderViewHeight);
                break;
            case READY:
                foldHeaderView(mHeaderView.getPaddingTop(), 0);
                mState = RefreshState.LOADING;
                mTvState.setText(R.string.refreshing);
                mPb.setVisibility(View.VISIBLE);
                mIvState.clearAnimation();
                mIvState.setVisibility(View.INVISIBLE);
                mLastRefreshTime = System.currentTimeMillis();
                if (mOnFetchDataListener != null) {
                    mOnFetchDataListener.onRefresh();
                }
                break;
            case LOADING:
                break;
        }
    }

    private void foldHeaderView(int fromY, int toY) {
        ValueAnimator animator = ValueAnimator.ofInt(fromY, toY);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHeaderView.setPadding(0, (int) animation.getAnimatedValue(), 0, 0);
            }
        });
        animator.start();
    }

    public void onRefreshCompleted() {
        mTvState.setText(R.string.refresh_finish);
        foldHeaderView(0, -mHeaderViewHeight);
        mState = RefreshState.IDLE;
        mPb.setVisibility(View.GONE);
        mIvState.setVisibility(View.VISIBLE);
    }

    public void onLoadMoreCompleted(boolean hasMore) {
        mNoMoreData = hasMore;
        mLoadingMoreData = false;
        if (mNoMoreData) {
            mTvLoadMoreState.setText(R.string.no_more_data);
            mFooterView.setVisibility(View.VISIBLE);
            mFooterView.setPadding(0, 0, 0, 0);
        } else {
            mFooterView.setVisibility(View.GONE);
            mFooterView.setPadding(0, -mFooterView.getMeasuredHeight(), 0, 0);
        }

    }

    public void setOnFetchDataListener(OnFetchDataListener onFetchDataListener) {
        mOnFetchDataListener = onFetchDataListener;
    }


    public interface OnFetchDataListener {
        void onRefresh();

        void onLoadMore();
    }

    private String formatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return sdf.format(calendar.getTime());
    }
}
