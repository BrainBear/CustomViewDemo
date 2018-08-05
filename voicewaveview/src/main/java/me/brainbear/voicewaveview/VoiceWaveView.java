package me.brainbear.voicewaveview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by brainBear on 2018/7/25.
 */

public class VoiceWaveView extends View {

    private int lines = 5;
    private Paint mPaint;

    private int heightOffset;
    /**
     * 线最长长度
     */
    private int maxLength;
    /**
     * 线最短长度
     */
    private int minLenght;
    /**
     * 线之间的间隔
     */
    private int lineInterval;

    /**
     * 线宽
     */
    private int lineWidth;
    @ColorInt
    private int lineColor;
    private ValueAnimator valueAnimator;


    public VoiceWaveView(Context context) {
        this(context, null);
    }

    public VoiceWaveView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VoiceWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceWaveView);
        lines = typedArray.getInt(R.styleable.VoiceWaveView_voice_lines, 7);
        maxLength = typedArray.getDimensionPixelSize(R.styleable.VoiceWaveView_voice_line_max_length, dp2px(50));
        minLenght = typedArray.getDimensionPixelSize(R.styleable.VoiceWaveView_voice_line_min_length, dp2px(10));
        lineInterval = typedArray.getDimensionPixelSize(R.styleable.VoiceWaveView_voice_line_interval, dp2px(10));
        lineWidth = typedArray.getDimensionPixelSize(R.styleable.VoiceWaveView_voice_line_width, dp2px(5));
        lineColor = typedArray.getColor(R.styleable.VoiceWaveView_voice_line_color, Color.WHITE);
        typedArray.recycle();

        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(lineColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }


    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (null == valueAnimator) {
            valueAnimator = ValueAnimator.ofInt(0, maxLength - minLenght, 0);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setDuration(1500);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    heightOffset = (Integer) animation.getAnimatedValue();
                    postInvalidate();
                }
            });

            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.start();
        }

        int width = getWidth();
        int height = getHeight();


        int _lines = lines;

        int centerX = width / 2;
        int xOffset = 0;

        int _lineHeight = maxLength - heightOffset;
        if (_lines % 2 != 0) {
            canvas.drawLine(centerX, height / 2 - _lineHeight / 2, centerX, height / 2 + _lineHeight / 2, mPaint);
            _lines--;
            xOffset = lineWidth + lineInterval;
        } else {
            xOffset = lineInterval / 2;
        }
        for (int i = _lines / 2; i > 0; i--) {
            if (i % 2 == 0) {
                _lineHeight = maxLength - heightOffset;
            } else {
                _lineHeight = minLenght + heightOffset;
            }
            canvas.drawLine(centerX - xOffset, height  / 2 - _lineHeight / 2, centerX - xOffset, height / 2 + _lineHeight / 2, mPaint);
            canvas.drawLine(centerX + xOffset, height / 2 - _lineHeight / 2, centerX + xOffset, height / 2 + _lineHeight / 2, mPaint);
            xOffset += lineWidth + lineInterval;
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != valueAnimator) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }
}
