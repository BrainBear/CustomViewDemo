package me.brainbear.tapeview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by brainBear on 2018/6/24.
 */

public class TapeView extends View {

    private static final String TAG = "TapeView";

    private Paint mDivisionPaint;

    private Paint mIndicatorPaint;

    /**
     * 两个刻度的距离，单位px
     */
    private int mDivisionGapWidth = dp2px(30);
    /**
     * 滑动过程时中点到当前值刻度的偏移量，负值代表中点在当前值右边，正值代表中点在当前值左边
     */
    private int mDivisionOffset;
    private int mShortDivisionHeight = dp2px(10);
    private int mLongDivisionHeight = dp2px(40);
    private int mMinValue = 0;
    private int mMaxValue = 100;
    private int mCurrentValue = 50;
    private int mTextSize = sp2px(20);
    private int mTextHeight;
    private int mTextMarginTop = dp2px(8);
    private TextPaint mTextPaint;
    private Rect mTextRect;
    private float mLastX;
    private OverScroller mScroller;
    private int mMaxVelocity;
    private int mMinVelocity;
    private VelocityTracker mVelocityTracker;
    private boolean mCanScroll;
    private List<OnTapeIndexChangedListener> mListeners = new CopyOnWriteArrayList();


    public TapeView(Context context) {
        this(context, null);
    }

    public TapeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TapeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mDivisionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDivisionPaint.setColor(Color.BLACK);
        mDivisionPaint.setStrokeWidth(dp2px(1));
        mDivisionPaint.setStyle(Paint.Style.STROKE);


        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setColor(Color.BLUE);
        mIndicatorPaint.setStrokeWidth(dp2px(3));
        mIndicatorPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);

        mTextRect = new Rect();
        mTextPaint.getTextBounds("0", 0, 1, mTextRect);
        mTextHeight = mTextRect.height();


        mScroller = new OverScroller(getContext());

        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
        mMinVelocity = vc.getScaledMinimumFlingVelocity();

        Log.d(TAG, "init: max velocity:" + mMaxVelocity + " min velocity:" + mMinVelocity);

        mVelocityTracker = VelocityTracker.obtain();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mScroller.forceFinished(true);
                mCanScroll = false;

                mLastX = event.getX();
                mVelocityTracker.addMovement(event);

                break;
            case MotionEvent.ACTION_MOVE:
                mVelocityTracker.addMovement(event);

                int dx = (int) (event.getX() - mLastX);

                computeCurrentIndex(dx);

                mLastX = event.getX();
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                float xVelocity = mVelocityTracker.getXVelocity();

                Log.d(TAG, "onTouchEvent ACTION_MOVE: x velocity:" + xVelocity);
//
                if (Math.abs(xVelocity) > 2 * mMinVelocity) {
                    mCanScroll = true;
                    mScroller.fling((int) event.getX(), 0, (int) xVelocity, 0, Integer.MIN_VALUE,
                            Integer.MAX_VALUE, 0, 0);

                    invalidate();
                } else if (mDivisionOffset != 0) {
                    scrollToDivision((int) event.getX());
                }

                break;
        }

        return true;
    }


    private void computeCurrentIndex(int dx) {
        int currentValue = mCurrentValue - (dx + mDivisionOffset) / mDivisionGapWidth;
        mDivisionOffset = (dx + mDivisionOffset) % mDivisionGapWidth;

        if (currentValue >= mMaxValue && mDivisionOffset < 0) {
            currentValue = mMaxValue;
            mDivisionOffset = 0;
        }
        if (currentValue <= mMinValue && mDivisionOffset > 0) {
            currentValue = mMinValue;
            mDivisionOffset = 0;
        }

        if (currentValue != mCurrentValue) {
            mCurrentValue = currentValue;
        }
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int dx = (int) (mScroller.getCurrX() - mLastX);
            computeCurrentIndex(dx);

            mLastX = mScroller.getCurrX();
            invalidate();
        } else if (mCanScroll && mDivisionOffset != 0) {
            scrollToDivision(mScroller.getCurrX());
        }
    }

    private void scrollToDivision(int startX, int distance) {
        Log.d(TAG, "scrollToDivision: " + startX + " " + distance);
        mScroller.startScroll(startX, 0, distance, 0);
        invalidate();
    }

    private void scrollToDivision(int startX) {
        if (mDivisionOffset < 0) {
            boolean b = Math.abs(mDivisionOffset) > mDivisionGapWidth / 2;
            if (b) {
                scrollToDivision(startX, -(mDivisionGapWidth + mDivisionOffset));
            } else {
                scrollToDivision(startX, -mDivisionOffset);
            }

        } else if (mDivisionOffset > 0) {
            boolean b = Math.abs(mDivisionOffset) > mDivisionGapWidth / 2;
            if (b) {
                scrollToDivision(startX, mDivisionGapWidth - mDivisionOffset);
            } else {
                scrollToDivision(startX, -mDivisionOffset);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLeft(canvas);
        drawRight(canvas);
        drawIndicator(canvas);
    }


    private void drawIndicator(Canvas canvas) {
        int centerX = getWidth() / 2;

        canvas.drawLine(centerX, 0, centerX, mLongDivisionHeight, mIndicatorPaint);
    }


    private void drawRight(Canvas canvas) {
        int value = mCurrentValue;
        int width = getWidth();

        int centerX = width / 2;


        for (int x = centerX + mDivisionOffset; x < width + 100; x += mDivisionGapWidth) {
            if (value % 10 == 0) {
                canvas.drawLine(x, 0, x, mLongDivisionHeight, mDivisionPaint);
                drawText(canvas, value, x, mLongDivisionHeight);
            } else {
                canvas.drawLine(x, 0, x, mShortDivisionHeight, mDivisionPaint);
            }
            value++;

            if (value > mMaxValue) {
                return;
            }
        }

    }

    private void drawLeft(Canvas canvas) {
        int value = mCurrentValue;
        int width = getWidth();

        int centerX = width / 2;


        for (int x = centerX + mDivisionOffset; x > 0 - 100; x -= mDivisionGapWidth) {
            if (value % 10 == 0) {
                canvas.drawLine(x, 0, x, mLongDivisionHeight, mDivisionPaint);
                drawText(canvas, value, x, mLongDivisionHeight);
            } else {
                canvas.drawLine(x, 0, x, mShortDivisionHeight, mDivisionPaint);
            }
            value--;

            if (value < mMinValue) {
                return;
            }
        }
    }


    private void drawText(Canvas canvas, int value, int x, int y) {
        String s = String.valueOf(value);

        mTextPaint.getTextBounds(s, 0, s.length(), mTextRect);

        canvas.drawText(s, x - mTextRect.width() / 2, y + mTextHeight + mTextMarginTop, mTextPaint);
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getContext().getResources().getDisplayMetrics());
    }


    public interface OnTapeIndexChangedListener {
        void onTapeIndexChanged(int index);
    }


    public void addOnTapeIndexChangedListener(OnTapeIndexChangedListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeOnTapeIndexChangedListener(OnTapeIndexChangedListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }

}
