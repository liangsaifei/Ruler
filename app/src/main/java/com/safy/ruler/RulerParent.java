package com.safy.ruler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.text.DecimalFormat;

/**
 * Created by saifei on 2017/10/16.
 * 尺子View Parent
 *
 */

public class RulerParent extends LinearLayout {

    Scroller scroller;
    private int mXDown;
    private int mXMove;
    private int mXLastMove;
    private int mScaleTouchSlop;
    private int leftBorder;
    private int rightBorder;
    private RulerListener rulerListener;
    private DecimalFormat decimalFormat;
    private int screenWidth;
    //一屏幕宽度 四十个刻度
    int screenCount = 40;
    private double scaleUnit;
    private VelocityTracker mVelocityTracker;
    private int mPointerId;
    private int mMinFlingSpeed;
    private int mMaxFlingSpeed;

    //初始化刻度100cm
    private int initialScale = 100;

    public RulerParent(Context context) {
        this(context, null);
    }

    public RulerParent(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerParent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        screenWidth = DisplayUtils.getScreenWidth(getContext());

        //一刻度多少像素
        scaleUnit = screenWidth / screenCount;

        scroller = new Scroller(getContext());
        decimalFormat = new DecimalFormat("#");
        RulerView rulerView = new RulerView(getContext());
        addView(rulerView);
        setBackgroundColor(getContext().getResources().getColor(R.color.rulerColor));
        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mScaleTouchSlop = viewConfiguration.getScaledTouchSlop();


        mMinFlingSpeed = viewConfiguration.getScaledMinimumFlingVelocity();
        mMaxFlingSpeed = viewConfiguration.getScaledMaximumFlingVelocity();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!scroller.isFinished()) { //fling
                    scroller.abortAnimation();
                }

                mPointerId = ev.getPointerId(0);
                mXDown = (int) ev.getRawX();
                mXLastMove = mXDown;
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = (int) ev.getRawX();
                int diff = Math.abs(mXMove - mXLastMove);
                if (diff > mScaleTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        leftBorder = getChildAt(0).getLeft();
        rightBorder = getChildAt(getChildCount() - 1).getRight() + screenWidth / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        acquireVelocityTracker(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                mXMove = (int) event.getRawX();
                int scrolledX = mXLastMove - mXMove;
                if (getScrollX() + scrolledX < leftBorder) {
                    scrollTo(leftBorder, 0);
                    return true;
                } else if (getScrollX() + getWidth() + scrolledX > rightBorder) {
                    scrollTo(rightBorder - getWidth(), 0);
                    return true;
                }
                scrollBy(scrolledX, 0);
                mXLastMove = mXMove;
                calculateScale(getScrollX());
                break;

            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingSpeed);
                float instantVelocityX = mVelocityTracker.getXVelocity(mPointerId);
                gotoCorrectScale(getScrollX(), instantVelocityX);
                releaseVelocityTracker();
                break;
            default:
                break;
        }


        return true;
    }

    private void gotoCorrectScale(int scrollX, float instantVelocityX) {

        scrollX = (int) calculateScrollX(scrollX);

        smoothScroll(scrollX, 0, -instantVelocityX);


        double percent = (double) scrollX / screenWidth;
        if (rulerListener != null) {
            rulerListener.onChanged(decimalFormat.format(initialScale + screenCount * percent));
        }
    }

    private double calculateScrollX(double scrollX) {
        double halfScaleUnit = scaleUnit / 2;
        if (scrollX % scaleUnit >= halfScaleUnit) { //没有整数倍
            scrollX = (int) (scrollX - scrollX % scaleUnit + scaleUnit);
        } else if (scrollX % scaleUnit < halfScaleUnit) {
            scrollX = (int) (scrollX - scrollX % scaleUnit);
        }
        return scrollX;
    }

    private void smoothScroll(int destX, int destY, float instantVelocityX) {
        int scrollX = getScrollX();
        int deltaX = destX - scrollX;
//  scroller.startScroll(scrollX,
// getScrollY(), deltaX, destY, 500);
        if (Math.abs(instantVelocityX) > mMinFlingSpeed) {
            scroller.fling(scrollX, getScrollY(), (int) instantVelocityX, 0, 0, rightBorder - screenWidth, 0, 0);
        } else {
            scroller.startScroll(scrollX, getScrollY(), deltaX, destY, 500);
        }
        invalidate();
    }


    /**
     * 计算厘米刻度
     *
     * @param scrollX
     */
    private void calculateScale(int scrollX) {

        //一刻度一半，用来判断在刻度中间某个位置时 手指抬起 然后 刻度指向前一个还是后一个
        double percent = (double) scrollX / screenWidth;
        if (rulerListener != null) {
            rulerListener.onChanged(decimalFormat.format(initialScale + screenCount * percent));
        }

    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            double percent = (double) scroller.getCurrX() / screenWidth;
            if (rulerListener != null) {
                rulerListener.onChanged(decimalFormat.format(initialScale + screenCount * percent));
            }
//            double scrollX = calculateScrollX(scroller.getCurrX());

            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }else {
            if(scroller.isFinished()&&scroller.getCurrX()%scaleUnit!=0 ){
                gotoCorrectScale(scroller.getCurrX(),0);
            }
        }
    }

    public void setRulerListener(RulerListener rulerListener) {
        this.rulerListener = rulerListener;
    }



    interface RulerListener {
        /**
         * @param scale 刻度
         */
        public void onChanged(String scale);
    }

    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

}
