package com.qun.lib.pullrefreshlistview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by Administrator on 2018/3/30 0030.
 */

public class PullRefreshListView extends ListView {

    private Context mContext;
    private int mHeaderViewHeight;
    private STATE mSTATE = STATE.IDLE;
    private int mFirstVisibleItem;
    private View mHeaderView;

    private enum STATE {
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
        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    private void initHeaderView() {
        mHeaderView = LayoutInflater.from(mContext).inflate(R.layout.listview_header, null, false);
        mHeaderView.measure(0, 0);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        openHeaderView(0);
        addHeaderView(mHeaderView, null, false);
    }

    /**
     * @param dy 垂直方向拖动距离
     */
    private void openHeaderView(int dy) {
        mHeaderView.setPadding(0, -mHeaderViewHeight + dy, 0, 0);
    }


    private void closeHeaderView(STATE state) {
        int endValue;
        if (state == STATE.READY) {
            endValue = 0;
        } else {
            endValue = -mHeaderViewHeight;
        }
        ValueAnimator va = ValueAnimator.ofInt(mHeaderView.getPaddingTop(), endValue);
        va.setDuration(300);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int dy = (int) animation.getAnimatedValue();
                mHeaderView.setPadding(0, dy, 0, 0);
            }
        });
        va.start();
    }

    private void changeState(STATE state) {
        switch (state) {
            case LOADING:
                break;
            case READY:
                break;
            case IDLE:
                closeHeaderView(STATE.IDLE);
                break;
        }
    }

    private int mLastY;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mSTATE != STATE.IDLE) {
                    return false;
                }
                mLastY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mFirstVisibleItem != 0) {
                    break;
                }
                int curY = (int) ev.getY();
                int dy = curY - mLastY;
                openHeaderView(dy / 2);
                if (mHeaderView.getPaddingTop() >= 0) {
                    mSTATE = STATE.READY;
                } else {
                    mSTATE = STATE.IDLE;
                }
                break;
            case MotionEvent.ACTION_UP:
                closeHeaderView(mSTATE);
                break;
        }
        return super.onTouchEvent(ev);
    }
}
