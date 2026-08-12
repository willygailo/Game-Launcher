package com.gamebooster.app.feature.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.view.View;

/**
 * Custom View rendering 4 distinct crosshair presets with customizable size, stroke, color, and opacity.
 */
public class CrosshairOverlayView extends View {

    private final Paint paintCenter = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintLines = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintRing = new Paint(Paint.ANTI_ALIAS_FLAG);

    private CrosshairPreset preset = CrosshairPreset.TACTICAL_CROSS;
    private int color = Color.parseColor("#00FF66"); // Neon Green default
    private int sizePx = 80;
    private float strokeWidth = 4f;
    private float opacity = 1.0f;

    public CrosshairOverlayView(Context context) {
        super(context);
        initPaints();
    }

    private void initPaints() {
        paintCenter.setStyle(Paint.Style.FILL);
        paintLines.setStyle(Paint.Style.STROKE);
        paintRing.setStyle(Paint.Style.STROKE);
        updatePaintStyles();
    }

    private void updatePaintStyles() {
        int colorWithAlpha = (int) (Color.alpha(color) * opacity) << 24 | (color & 0x00FFFFFF);

        paintCenter.setColor(colorWithAlpha);

        paintLines.setColor(colorWithAlpha);
        paintLines.setStrokeWidth(strokeWidth);

        paintRing.setColor(colorWithAlpha);
        paintRing.setStrokeWidth(strokeWidth * 0.75f);

        invalidate();
    }

    public void setPreset(CrosshairPreset preset) {
        this.preset = preset;
        invalidate();
    }

    public void setColor(int color) {
        this.color = color;
        updatePaintStyles();
    }

    public void setSizePx(int sizePx) {
        this.sizePx = Math.max(20, sizePx);
        requestLayout();
        invalidate();
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = Math.max(1f, strokeWidth);
        updatePaintStyles();
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.1f, Math.min(1.0f, opacity));
        updatePaintStyles();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        float radius = sizePx / 2f;

        switch (preset) {
            case DOT:
                canvas.drawCircle(cx, cy, radius * 0.25f, paintCenter);
                break;

            case TACTICAL_CROSS:
                // Center Dot
                canvas.drawCircle(cx, cy, radius * 0.15f, paintCenter);
                // 4 Cross lines with gap
                float gap = radius * 0.25f;
                canvas.drawLine(cx - radius, cy, cx - gap, cy, paintLines);
                canvas.drawLine(cx + gap, cy, cx + radius, cy, paintLines);
                canvas.drawLine(cx, cy - radius, cx, cy - gap, paintLines);
                canvas.drawLine(cx, cy + gap, cx, cy + radius, paintLines);
                break;

            case SCOPE_RING:
                // Outer ring
                canvas.drawCircle(cx, cy, radius * 0.75f, paintRing);
                // Center dot
                canvas.drawCircle(cx, cy, radius * 0.12f, paintCenter);
                // Small inner tick marks
                float tickGap = radius * 0.4f;
                canvas.drawLine(cx - radius * 0.75f, cy, cx - tickGap, cy, paintLines);
                canvas.drawLine(cx + tickGap, cy, cx + radius * 0.75f, cy, paintLines);
                canvas.drawLine(cx, cy - radius * 0.75f, cx, cy - tickGap, paintLines);
                canvas.drawLine(cx, cy + tickGap, cx, cy + radius * 0.75f, paintLines);
                break;

            case SNIPER_CROSS:
                // Full diameter cross lines
                canvas.drawLine(cx - radius, cy, cx + radius, cy, paintLines);
                canvas.drawLine(cx, cy - radius, cx, cy + radius, paintLines);
                // Tiny center dot
                canvas.drawCircle(cx, cy, radius * 0.1f, paintCenter);
                // Range ticks
                float t1 = radius * 0.35f;
                float t2 = radius * 0.7f;
                canvas.drawLine(cx - 6, cy - t1, cx + 6, cy - t1, paintLines);
                canvas.drawLine(cx - 6, cy + t1, cx + 6, cy + t1, paintLines);
                canvas.drawLine(cx - 6, cy + t2, cx + 6, cy + t2, paintLines);
                break;
        }
    }
}
