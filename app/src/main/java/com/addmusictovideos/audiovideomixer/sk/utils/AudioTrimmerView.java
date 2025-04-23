package com.addmusictovideos.audiovideomixer.sk.utils;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AudioTrimmerView extends View {
    private Paint waveformPaint, dimmedPaint, highlightPaint, thumbPaint, circleFillPaint, arrowPaint;
    private float leftThumbX = 100, rightThumbX = 600, minTrimDistance = 50;
    private boolean isLeftThumbSelected = false, isRightThumbSelected = false;
    private float[] waveformPoints;
    private float playbackIndicatorX = 100; // Start at left thumb initially
    private boolean isSeeking = false;
    private boolean isPlaying = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    private TrimChangeListener trimChangeListener;

    private LinearGradient gradient;

    public interface TrimChangeListener {
        void onTrimChanged(float startRatio, float endRatio);
        void onPlaybackSeeked(float positionRatio);
    }

    public void setTrimChangeListener(TrimChangeListener listener) {
        this.trimChangeListener = listener;
    }

    public AudioTrimmerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setWaveformData(float[] waveform) {
        this.waveformPoints = waveform;
        invalidate();
    }

    public void moveLeftThumbBy(float offset) {
        float newLeftThumbX = Math.max(0, Math.min(leftThumbX + offset, rightThumbX - minTrimDistance));

        // Only reset the playback indicator if it was at the left trim position
        if (playbackIndicatorX == leftThumbX) {
            playbackIndicatorX = newLeftThumbX;
        }

        leftThumbX = newLeftThumbX;

        if (trimChangeListener != null) {
            trimChangeListener.onTrimChanged(getTrimStart(), getTrimEnd());
            trimChangeListener.onPlaybackSeeked(getPlaybackPositionRatio()); // Use the fixed method
        }
        invalidate();
    }

    private float getPlaybackPositionRatio() {
        return (playbackIndicatorX - leftThumbX) / (rightThumbX - leftThumbX);
    }

    public void moveRightThumbBy(float offset) {
        rightThumbX = Math.max(leftThumbX + minTrimDistance, Math.min(rightThumbX + offset, getWidth()));
        if (trimChangeListener != null) {
            trimChangeListener.onTrimChanged(getTrimStart(), getTrimEnd());
        }
        invalidate();
    }

    private void init() {
        waveformPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waveformPaint.setStyle(Paint.Style.FILL);
        waveformPaint.setStrokeWidth(8);

        dimmedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dimmedPaint.setColor(0xFF707070);
        dimmedPaint.setStyle(Paint.Style.FILL);
        dimmedPaint.setStrokeWidth(6);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setStrokeWidth(8);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setColor(Color.GREEN);
        thumbPaint.setStrokeWidth(8);
        thumbPaint.setStyle(Paint.Style.STROKE);

        circleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleFillPaint.setColor(Color.GREEN);
        circleFillPaint.setStyle(Paint.Style.FILL);

        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.BLACK);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(5);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        gradient = new LinearGradient(leftThumbX, 0, rightThumbX, 0,
                Color.parseColor("#E3E9F6"), Color.parseColor("#36698D"),
                Shader.TileMode.CLAMP);
        highlightPaint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerY = getHeight() / 2f;

        if (waveformPoints != null) {
            int barWidth = Math.max(4, getMeasuredWidth() / waveformPoints.length);
            for (int i = 0; i < waveformPoints.length; i++) {
                float x = i * barWidth;
                float barHeight = Math.max(10, waveformPoints[i] * getHeight() * 0.5f);
                Paint paint = (x < leftThumbX || x > rightThumbX) ? dimmedPaint : highlightPaint;
                canvas.drawLine(x, centerY - barHeight, x, centerY + barHeight, paint);
            }
        }

        drawThumb(canvas, leftThumbX, centerY, true);
        drawThumb(canvas, rightThumbX, centerY, false);

        // 🎯 Playback Indicator
        Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setColor(Color.WHITE);
        indicatorPaint.setStrokeWidth(6);

        float indicatorTop = centerY - (getHeight() * 0.45f);
        float indicatorBottom = centerY + (getHeight() * 0.45f);

        playbackIndicatorX = Math.max(leftThumbX, Math.min(playbackIndicatorX, rightThumbX)); // Restrict movement
        canvas.drawLine(playbackIndicatorX, indicatorTop, playbackIndicatorX, indicatorBottom, indicatorPaint);
    }

    private void drawThumb(Canvas canvas, float x, float centerY, boolean isLeft) {
        canvas.drawLine(x, centerY - getHeight() * 0.45f, x, centerY + getHeight() * 0.45f, thumbPaint);
        canvas.drawCircle(x, centerY, 40, circleFillPaint);
        canvas.drawCircle(x, centerY, 40, thumbPaint);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        String arrowSymbol = isLeft ? "▶" : "◀";
        canvas.drawText(arrowSymbol, x, centerY + 15, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (Math.abs(x - leftThumbX) < 40) {
                    isLeftThumbSelected = true;
                } else if (Math.abs(x - rightThumbX) < 40) {
                    isRightThumbSelected = true;
                } else {
                    playbackIndicatorX = Math.max(leftThumbX, Math.min(x, rightThumbX));
                    if (trimChangeListener != null) {
                        float newRatio = (playbackIndicatorX - leftThumbX) / (rightThumbX - leftThumbX);
                        trimChangeListener.onPlaybackSeeked(newRatio);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isLeftThumbSelected) {
                    float newLeftThumbX = Math.max(0, Math.min(x, rightThumbX - minTrimDistance));

                    // Only move the indicator if it was at the left trim
                    if (playbackIndicatorX == leftThumbX) {
                        playbackIndicatorX = newLeftThumbX;
                    }

                    leftThumbX = newLeftThumbX;
                }
                if (isRightThumbSelected) {
                    rightThumbX = Math.max(leftThumbX + minTrimDistance, Math.min(x, getWidth()));
                }
                if (trimChangeListener != null) {
                    trimChangeListener.onTrimChanged(getTrimStart(), getTrimEnd());
                }
                gradient = new LinearGradient(leftThumbX, 0, rightThumbX, 0,
                        Color.parseColor("#E3E9F6"), Color.parseColor("#36698D"),
                        Shader.TileMode.CLAMP);
                highlightPaint.setShader(gradient);
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                isLeftThumbSelected = false;
                isRightThumbSelected = false;
                break;
        }
        return true;
    }

    public float getTrimStart() {
        return leftThumbX / getWidth();
    }

    public float getTrimEnd() {
        return rightThumbX / getWidth();
    }

    public void startPlayback() {
        isPlaying = true;
        playbackIndicatorX = leftThumbX;
        handler.post(playbackRunnable);
    }

    private Runnable playbackRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying && playbackIndicatorX < rightThumbX) {
                playbackIndicatorX += 5;
                invalidate();
                handler.postDelayed(this, 16);
            } else {
                isPlaying = false;
            }
        }
    };

    public void updatePlaybackIndicator(float positionRatio) {
        playbackIndicatorX = leftThumbX + (rightThumbX - leftThumbX) * positionRatio;
        invalidate();
    }


    public void stopPlayback() {
        isPlaying = false;
        handler.removeCallbacks(playbackRunnable);
    }
}
