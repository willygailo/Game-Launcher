package com.gamebooster.app.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class CrosshairOverlayManager {

    private static WindowManager windowManager;
    private static View crosshairView;
    private static boolean isShowing = false;

    public static boolean isShowing() {
        return isShowing;
    }

    public static boolean toggleCrosshair(Context context) {
        if (isShowing) {
            hideCrosshair(context);
            return false;
        } else {
            showCrosshair(context);
            return true;
        }
    }

    public static void showCrosshair(Context context) {
        if (context == null || isShowing) return;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) return;

        crosshairView = new View(context) {
            private final Paint paintDot = new Paint();
            private final Paint paintLine = new Paint();

            {
                paintDot.setColor(Color.parseColor("#00FF66")); // Neon green dot
                paintDot.setAntiAlias(true);
                paintDot.setStyle(Paint.Style.FILL);

                paintLine.setColor(Color.parseColor("#00F0FF")); // Cyan target lines
                paintLine.setAntiAlias(true);
                paintLine.setStrokeWidth(3f);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;

                // Center neon dot
                canvas.drawCircle(cx, cy, 5f, paintDot);

                // Target cross lines
                canvas.drawLine(cx - 15, cy, cx - 6, cy, paintLine);
                canvas.drawLine(cx + 6, cy, cx + 15, cy, paintLine);
                canvas.drawLine(cx, cy - 15, cx, cy - 6, paintLine);
                canvas.drawLine(cx, cy + 6, cx, cy + 15, paintLine);
            }
        };

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                60,
                60,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER;

        try {
            windowManager.addView(crosshairView, params);
            isShowing = true;
        } catch (Exception ignored) {}
    }

    public static void hideCrosshair(Context context) {
        if (!isShowing || crosshairView == null) return;
        try {
            if (windowManager != null) {
                windowManager.removeView(crosshairView);
            }
        } catch (Exception ignored) {}
        crosshairView = null;
        isShowing = false;
    }
}
