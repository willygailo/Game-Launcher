package com.gamebooster.app.overlay;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * VirtualTriggerOverlayView — Draggable, Glowing Virtual Shoulder Trigger.
 *
 * Simulates high-speed touch input at target screen coordinates.
 */
public class VirtualTriggerOverlayView extends View {

    public interface OnPositionChangedListener {
        void onPositionChanged(int x, int y);
    }

    private String label = "L1";
    private int accentColor = Color.parseColor("#00F0FF"); // Neon Cyan
    private Paint circlePaint;
    private Paint borderPaint;
    private Paint textPaint;

    private float pulseRadius = 0;
    private ValueAnimator pulseAnimator;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private OnPositionChangedListener positionListener;

    private float initialTouchX, initialTouchY;
    private int initialWindowX, initialWindowY;
    private boolean isDragging = false;

    public VirtualTriggerOverlayView(Context context) {
        super(context);
        init();
    }

    public VirtualTriggerOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#990B1017")); // Cyber Translucent Dark
        circlePaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(accentColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    public void setup(String label, int color, WindowManager wm, WindowManager.LayoutParams params, OnPositionChangedListener listener) {
        this.label = label;
        this.accentColor = color;
        this.windowManager = wm;
        this.layoutParams = params;
        this.positionListener = listener;

        borderPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float baseRadius = Math.min(cx, cy) - 8f;

        // Background Circle
        canvas.drawCircle(cx, cy, baseRadius, circlePaint);

        // Neon Glow Border
        canvas.drawCircle(cx, cy, baseRadius, borderPaint);

        // Pulse Effect when clicked
        if (pulseRadius > 0) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(accentColor);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(6f);
            p.setAlpha(120);
            canvas.drawCircle(cx, cy, pulseRadius, p);
        }

        // Text Label
        float textY = cy - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(label, cx, textY, textPaint);
    }

    /**
     * Executes the simulated tap at this trigger's screen coordinates.
     */
    public void triggerAction() {
        // Visual Pulse
        if (pulseAnimator != null) pulseAnimator.cancel();
        pulseAnimator = ValueAnimator.ofFloat(0, Math.min(getWidth(), getHeight()) / 2f);
        pulseAnimator.setDuration(160);
        pulseAnimator.addUpdateListener(anim -> {
            pulseRadius = (float) anim.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();

        // Dispatch simulated touch input
        if (layoutParams != null) {
            int tapX = layoutParams.x + (getWidth() / 2);
            int tapY = layoutParams.y + (getHeight() / 2);

            AppExecutors.getInstance().executeCommand(() -> {
                String cmd = "input tap " + tapX + " " + tapY;
                CommandExecutor.executeSystemCommand(cmd);
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                if (layoutParams != null) {
                    initialWindowX = layoutParams.x;
                    initialWindowY = layoutParams.y;
                }
                isDragging = false;
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = event.getRawX() - initialTouchX;
                float dy = event.getRawY() - initialTouchY;
                if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                    isDragging = true;
                    if (layoutParams != null && windowManager != null) {
                        layoutParams.x = initialWindowX + (int) dx;
                        layoutParams.y = initialWindowY + (int) dy;
                        try {
                            windowManager.updateViewLayout(this, layoutParams);
                        } catch (Exception ignored) {}
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (!isDragging) {
                    // Quick Tap -> Trigger the action!
                    triggerAction();
                } else {
                    // Finished Drag -> Save new coordinates
                    if (layoutParams != null && positionListener != null) {
                        positionListener.onPositionChanged(layoutParams.x, layoutParams.y);
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }
}
