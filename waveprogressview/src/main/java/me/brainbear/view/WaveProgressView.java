package me.brainbear.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by brainBear on 2018/8/21.
 */
public class WaveProgressView extends View {

    Paint mPaint = new Paint();
    Path mPath = new Path();

    private static final int DEFAULT_MAX = 100;
    private static final int DEFAULT_PROGRESS = 50;
    private static final int DEFAULT_AMPLITUDE = 80;
    private static final int DEFAULT_FRONT_WAVE_DURATION = 1000;
    private static final int DEFAULT_BACK_WAVE_DURATION = 2000;
    private static final boolean DEFAULT_WAVE_ANIMATION_ENABLE = true;
    private static final int DEFAULT_FRONT_WAVE_COLOR = Color.RED;
    private static final int DEFAULT_BACK_WAVE_COLOR = Color.parseColor("#33FF0000");
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_WAVE_OFFSET = 150;
    private static final int DEFAULT_STROKE_COLOR = Color.RED;
    private static final int DEFAULT_STROKE_WIDTH = 20;

    private long max;
    private long progress;
    private int amplitude;
    private int period = -1;

    private int frontWaveDuration;
    private int backWaveDuration;

    private int frontWaveOffset = 0;
    private int backWaveOffset = 0;
    private boolean waveAnimationEnable;

    @ColorInt
    private int frontWaveColor;
    @ColorInt
    private int backWaveColor;
    @ColorInt
    private int backgroundColor;
    private int waveOffset;

    @ColorInt
    private int strokeColor;
    private int strokeWidth;
    private ValueAnimator mFrontWaveAnimator;
    private ValueAnimator mBackWaveAnimator;
    private PorterDuffXfermode porterDuffXfermode;

    public WaveProgressView(Context context) {
        this(context, null);
    }

    public WaveProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveProgressView);

        max = typedArray.getInt(R.styleable.WaveProgressView_wave_max, DEFAULT_MAX);
        progress = typedArray.getInt(R.styleable.WaveProgressView_wave_progress, DEFAULT_PROGRESS);
        amplitude = typedArray.getDimensionPixelSize(R.styleable.WaveProgressView_wave_amplitude, DEFAULT_AMPLITUDE);
        period = typedArray.getDimensionPixelSize(R.styleable.WaveProgressView_wave_period, -1);
        frontWaveDuration = typedArray.getInt(R.styleable.WaveProgressView_front_wave_duration, DEFAULT_FRONT_WAVE_DURATION);
        backWaveDuration = typedArray.getInt(R.styleable.WaveProgressView_back_wave_duration, DEFAULT_BACK_WAVE_DURATION);
        waveOffset = typedArray.getDimensionPixelSize(R.styleable.WaveProgressView_wave_offset, DEFAULT_WAVE_OFFSET);
        waveAnimationEnable = typedArray.getBoolean(R.styleable.WaveProgressView_wave_animation, DEFAULT_WAVE_ANIMATION_ENABLE);
        frontWaveColor = typedArray.getColor(R.styleable.WaveProgressView_front_wave_color, DEFAULT_FRONT_WAVE_COLOR);
        backWaveColor = typedArray.getColor(R.styleable.WaveProgressView_back_wave_color, DEFAULT_BACK_WAVE_COLOR);
        backgroundColor = typedArray.getColor(R.styleable.WaveProgressView_background_color, DEFAULT_BACKGROUND_COLOR);
        strokeColor = typedArray.getColor(R.styleable.WaveProgressView_stroke_color, DEFAULT_STROKE_COLOR);
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.WaveProgressView_stroke_width, DEFAULT_STROKE_WIDTH);
        typedArray.recycle();
        init();
    }


    private void init() {
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        initAnimation();


        int width = getWidth();
        int height = getHeight();


        if (-1 == period) {
            period = width / 2;
        }

        int waveHeight = getWaveHeight();


        int layer = canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG);

        canvas.drawColor(backgroundColor);

        if (waveOffset != 0) {
            mPaint.reset();
            mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(backWaveColor);

            int drawCount = (waveOffset + getWidth()) * 2 / period;
            if (getWidth() * 2 % period > 0) {
                drawCount++;
            }

            mPath.reset();
            mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            mPath.moveTo(-width - waveOffset + backWaveOffset, waveHeight);
            for (int i = 0; i < drawCount; i++) {
                mPath.rQuadTo(period / 4, amplitude, period / 2, 0);
                mPath.rQuadTo(period / 4, -amplitude, period / 2, 0);
            }

            mPath.lineTo(width + backWaveOffset, height);
            mPath.lineTo(-width - waveOffset + backWaveOffset, height);
            mPath.close();

            canvas.drawPath(mPath, mPaint);
        }

        mPaint.reset();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(frontWaveColor);

        int drawCount = getWidth() * 2 / period;
        if (getWidth() * 2 % period > 0) {
            drawCount++;
        }

        mPath.reset();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mPath.moveTo(-width + frontWaveOffset, waveHeight);
        for (int i = 0; i < drawCount; i++) {
            mPath.rQuadTo(period / 4, amplitude, period / 2, 0);
            mPath.rQuadTo(period / 4, -amplitude, period / 2, 0);
        }

        mPath.lineTo(width + frontWaveOffset, height);
        mPath.lineTo(-width + frontWaveOffset, height);
        mPath.close();

        canvas.drawPath(mPath, mPaint);

        //画边界
        Path borderShapePath = getBorderShapePath();
        if (null != borderShapePath) {
            mPaint.setColor(strokeColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(strokeWidth);
            canvas.drawPath(borderShapePath, mPaint);
        }

        //按边界裁剪
        Bitmap shapeBitmap = getBorderShapeBitmap();
        if (null != shapeBitmap) {
            mPaint.setXfermode(porterDuffXfermode);
            canvas.drawBitmap(shapeBitmap, 0, 0, mPaint);
            mPaint.setXfermode(null);
        }
        canvas.restoreToCount(layer);
    }

    private int getWaveHeight() {
        return (int) ((float) (max - progress) / max * getHeight());
    }

    private void initAnimation() {
        //幅度为0的时候也没必要执行动画
        if (!waveAnimationEnable || amplitude == 0) {
            return;
        }
        if (null == mFrontWaveAnimator) {
            mFrontWaveAnimator = ValueAnimator.ofInt(0, getWidth());
            mFrontWaveAnimator.setDuration(frontWaveDuration);
            mFrontWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mFrontWaveAnimator.setInterpolator(new LinearInterpolator());
            mFrontWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    frontWaveOffset = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            mFrontWaveAnimator.start();
        }

        if (null == mBackWaveAnimator) {
            mBackWaveAnimator = ValueAnimator.ofInt(0, getWidth());
            mBackWaveAnimator.setDuration(backWaveDuration);
            mBackWaveAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mBackWaveAnimator.setInterpolator(new LinearInterpolator());
            mBackWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    backWaveOffset = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            mBackWaveAnimator.start();
        }
    }


    private Path getBorderShapePath() {
//        Path path = new Path();
//
//        path.addCircle(getWidth() / 2, getHeight() / 2, getWidth() / 2, Path.Direction.CW);

//        return path;
        return null;
    }

    private Bitmap getBorderShapeBitmap() {
        Path shapePath = getBorderShapePath();
        if (null == shapePath) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawPath(shapePath, paint);

        return bitmap;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
        postInvalidate();
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
        postInvalidate();
    }


    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        postInvalidate();
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        postInvalidate();
    }


}