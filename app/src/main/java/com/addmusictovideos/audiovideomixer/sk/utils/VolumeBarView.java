package com.addmusictovideos.audiovideomixer.sk.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class VolumeBarView extends View {

    private Paint gradientPaint, pointerPaint, dropPaint;
    private float pointerX;
    private int minVolume = 0;
    private int maxVolume = 500;
    private int currentVolume = 100;
    private OnVolumeChangeListener listener;
    private int barSpacing = 20;
    private int barCount; // Number of bars
    private float offsetX = 0; // Wave movement offset
    private int defaultPointerX;

    public VolumeBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gradientPaint = new Paint();
        gradientPaint.setStrokeWidth(6); // Adjusted thickness for the wave bars

        pointerPaint = new Paint();
        pointerPaint.setColor(Color.WHITE);
        pointerPaint.setStyle(Paint.Style.FILL);
        pointerPaint.setStrokeWidth(12); // Slightly thicker pointer

        dropPaint = new Paint();
        dropPaint.setColor(Color.WHITE);
        dropPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        defaultPointerX = w / 2; // Center pointer for default volume (100%)
        pointerX = defaultPointerX; // Initialize pointer position at center
        barCount = w / barSpacing;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int barHeight = getHeight() - 60;

        // Apply gradient for wave bars
        LinearGradient gradient = new LinearGradient(0, 0, getWidth(), 0,
                Color.parseColor("#14FF00"), Color.parseColor("#FF000F"), Shader.TileMode.CLAMP);
        gradientPaint.setShader(gradient);

        // Draw moving wave bars
        for (int i = 0; i < barCount; i++) {
            float xPos = (i * barSpacing) + offsetX;
            if (xPos >= 0 && xPos <= getWidth()) {
                canvas.drawLine(xPos, 0, xPos, barHeight, gradientPaint);
            }
        }

        // Draw the white pointer (thicker)
        canvas.drawLine(pointerX, 0, pointerX, barHeight, pointerPaint);

        // Draw the drop symbol (💧) below the pointer
        Path dropPath = new Path();
        float dropSize = 20;
        float dropGap = 10;
        float dropTop = barHeight + dropGap;
        float dropBottom = barHeight + dropGap + dropSize;

        dropPath.moveTo(pointerX, dropTop);
        dropPath.lineTo(pointerX - dropSize, dropBottom);
        dropPath.lineTo(pointerX + dropSize, dropBottom);
        dropPath.close();
        canvas.drawPath(dropPath, dropPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                pointerX = event.getX();

                // Clamp the pointerX to stay within the bounds of the bar
                if (pointerX < 0) pointerX = 0;
                if (pointerX > getWidth()) pointerX = getWidth();

                // Update the volume based on pointer position (0-500 range)
                currentVolume = (int) ((pointerX / getWidth()) * (maxVolume - minVolume));

                // Ensure the volume is within the 0-500 range
                if (currentVolume < 0) currentVolume = 0;
                if (currentVolume > 500) currentVolume = 500;

                if (listener != null) listener.onVolumeChanged(currentVolume);

                invalidate();
                break;
        }
        return true;
    }

    // Method to update the volume and move the pointer left or right
    public void updateVolume(int delta) {
        currentVolume += delta;

        // Ensure the volume stays within 0-500 range
        if (currentVolume < 0) currentVolume = 0;
        if (currentVolume > 500) currentVolume = 500;

        // Calculate the new pointer position based on the updated volume
        pointerX = (float) currentVolume / maxVolume * getWidth();

        // Notify listener of volume change
        if (listener != null) listener.onVolumeChanged(currentVolume);

        // Update wave offset to create smooth dragging
        offsetX += (float) delta / 100;
        invalidate();
    }


    public void setOnVolumeChangeListener(OnVolumeChangeListener listener) {
        this.listener = listener;
    }

    public interface OnVolumeChangeListener {
        void onVolumeChanged(int volume);
    }
}
