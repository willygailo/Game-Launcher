package com.gamebooster.app.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.core.DeviceInfoChannel;

public class FloatingOverlayService extends Service {

    private static final String CHANNEL_ID = "game_booster_overlay_channel";
    private static final int NOTIF_ID = 888;
    private static boolean isRunning = false;

    private WindowManager windowManager;
    private View overlayView;
    private TextView tvMetrics;
    private Handler handler;
    private Runnable updateRunnable;

    public static boolean isOverlayRunning() {
        return isRunning;
    }

    public static void startOverlay(Context context) {
        if (context == null || isRunning) return;
        Intent intent = new Intent(context, FloatingOverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopOverlay(Context context) {
        if (context == null || !isRunning) return;
        Intent intent = new Intent(context, FloatingOverlayService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        createNotificationChannel();
        startForeground(NOTIF_ID, createNotification());

        setupFloatingView();
        setupTicker();
    }

    private void setupFloatingView() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager == null) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#CC0F172A")); // Glassmorphic dark slate
        layout.setPadding(24, 16, 24, 16);

        tvMetrics = new TextView(this);
        tvMetrics.setTextColor(Color.parseColor("#00F0FF"));
        tvMetrics.setTextSize(12f);
        tvMetrics.setText("⚡ GAME HUD: Loading...");
        layout.addView(tvMetrics);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.y = 150;

        layout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        if (windowManager != null && overlayView != null) {
                            windowManager.updateViewLayout(overlayView, params);
                        }
                        return true;
                }
                return false;
            }
        });

        overlayView = layout;
        try {
            windowManager.addView(overlayView, params);
        } catch (Exception e) {
            isRunning = false;
        }
    }

    private void setupTicker() {
        handler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (tvMetrics != null) {
                    DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(getApplicationContext());
                    com.gamebooster.app.core.DisplayCapabilitiesDetector.DisplayCaps caps = 
                            com.gamebooster.app.core.DisplayCapabilitiesDetector.detect(getApplicationContext());
                    int currentHz = caps != null ? caps.currentRefreshRate : 60;

                    String text = String.format("⚡ FPS/Hz: %dHz | RAM: %d%% | Temp: %.1f°C",
                            currentHz, m.ramUsagePct, m.batteryTempC);
                    tvMetrics.setText(text);
                }
                if (handler != null && isRunning) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (handler != null && updateRunnable != null) {
            handler.removeCallbacks(updateRunnable);
        }
        if (windowManager != null && overlayView != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {}
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Game Booster Performance Overlay",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GAME SPACE — Performance HUD")
                .setContentText("FPS, RAM & Thermal overlay active")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
}
