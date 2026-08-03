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
import android.provider.Settings;
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
    private Handler handler;
    private Runnable updateRunnable;

    public static boolean isOverlayRunning() {
        return isRunning;
    }

    public static void startOverlay(Context context) {
        if (context == null || isRunning) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) return;
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

        if (!setupFloatingView()) {
            isRunning = false;
            stopSelf();
            return;
        }
        setupTicker();
    }

    private View layoutCollapsedPill;
    private View layoutExpandedDock;
    private TextView tvPillMetrics;
    private TextView tvHudFps;
    private TextView tvHudRam;
    private TextView tvHudTemp;
    private TextView tvHudMa;
    private boolean isCollapsed = true;
    private boolean isDndActive = false;

    private boolean setupFloatingView() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager == null) return false;

        android.view.LayoutInflater inflater = (android.view.LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return false;
        overlayView = inflater.inflate(R.layout.floating_hud_layout, null);

        layoutCollapsedPill = overlayView.findViewById(R.id.layout_collapsed_pill);
        layoutExpandedDock = overlayView.findViewById(R.id.layout_expanded_dock);
        tvPillMetrics = overlayView.findViewById(R.id.tv_pill_metrics);
        tvHudFps = overlayView.findViewById(R.id.tv_hud_fps);
        tvHudRam = overlayView.findViewById(R.id.tv_hud_ram);
        tvHudTemp = overlayView.findViewById(R.id.tv_hud_temp);
        tvHudMa = overlayView.findViewById(R.id.tv_hud_ma);

        View tvCollapseBtn = overlayView.findViewById(R.id.tv_hud_collapse_btn);
        View btnBoost = overlayView.findViewById(R.id.btn_hud_boost);
        View btnExtreme = overlayView.findViewById(R.id.btn_hud_extreme);
        View btnDnd = overlayView.findViewById(R.id.btn_hud_dnd);
        View btnCrosshair = overlayView.findViewById(R.id.btn_hud_crosshair);
        isDndActive = com.gamebooster.app.functions.GameSpaceDndManager.isDndActive(getApplicationContext());

        if (tvCollapseBtn != null) {
            tvCollapseBtn.setOnClickListener(v -> toggleDockState(true));
        }

        if (btnBoost != null) {
            btnBoost.setOnClickListener(v -> {
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.functions.RamZramChannel.trimMemoryAndCleanCache(getApplicationContext());
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() ->
                            android.widget.Toast.makeText(getApplicationContext(), "⚡ Executed: pm trim-caches 1000M & sync", android.widget.Toast.LENGTH_LONG).show());
                });
            });
        }

        if (btnExtreme != null) {
            btnExtreme.setOnClickListener(v -> {
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.functions.PerformanceChannel.ProfileResult result =
                            com.gamebooster.app.functions.PerformanceChannel.applyProfileWithResult(
                                    getApplicationContext(),
                                    com.gamebooster.app.functions.PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() ->
                            android.widget.Toast.makeText(getApplicationContext(), "🔥 " + result.message,
                                    android.widget.Toast.LENGTH_LONG).show());
                });
            });
        }

        if (btnDnd != null) {
            btnDnd.setOnClickListener(v -> {
                isDndActive = !isDndActive;
                final boolean targetDnd = isDndActive;
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    boolean applied = com.gamebooster.app.functions.GameSpaceDndManager
                            .setGamingDndMode(getApplicationContext(), targetDnd);
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() ->
                            android.widget.Toast.makeText(getApplicationContext(), applied
                                    ? (targetDnd ? "🚫 Gaming DND enabled" : "🔔 Gaming DND disabled")
                                    : "DND permission is required", android.widget.Toast.LENGTH_LONG).show());
                });
            });
        }

        if (btnCrosshair != null) {
            btnCrosshair.setOnClickListener(v -> {
                boolean active = CrosshairOverlayManager.toggleCrosshair(getApplicationContext());
                android.widget.Toast.makeText(getApplicationContext(), active ? "🎯 FPS Crosshair Overlay ON" : "🎯 Crosshair OFF", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

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
        params.x = 20;
        params.y = 200;

        View headerDock = overlayView.findViewById(R.id.tv_hud_collapse_btn) != null ? 
                (View) overlayView.findViewById(R.id.tv_hud_collapse_btn).getParent() : overlayView;

        View.OnTouchListener dragListener = new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isClick = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isClick = true;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(event.getRawX() - initialTouchX);
                        float dy = Math.abs(event.getRawY() - initialTouchY);
                        if (dx > 12 || dy > 12) {
                            isClick = false;
                        }
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        if (windowManager != null && overlayView != null) {
                            windowManager.updateViewLayout(overlayView, params);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isClick) {
                            if (v == layoutCollapsedPill) {
                                toggleDockState(false);
                            }
                        } else {
                            // Magnetic Edge Snapping
                            if (windowManager != null && overlayView != null) {
                                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                                int viewWidth = overlayView.getWidth();
                                int midPoint = screenWidth / 2;
                                if (params.x + (viewWidth / 2) < midPoint) {
                                    params.x = 10; // Magnetic Snap Left
                                } else {
                                    params.x = Math.max(10, screenWidth - viewWidth - 10); // Magnetic Snap Right
                                }
                                windowManager.updateViewLayout(overlayView, params);
                            }
                        }
                        return true;
                }
                return false;
            }
        };

        if (layoutCollapsedPill != null) {
            layoutCollapsedPill.setOnTouchListener(dragListener);
        }
        if (headerDock != null) {
            headerDock.setOnTouchListener(dragListener);
        }

        try {
            windowManager.addView(overlayView, params);
            return true;
        } catch (Exception e) {
            isRunning = false;
            return false;
        }
    }

    private void toggleDockState(boolean collapse) {
        this.isCollapsed = collapse;
        if (layoutCollapsedPill != null && layoutExpandedDock != null) {
            if (collapse) {
                layoutCollapsedPill.setVisibility(View.VISIBLE);
                layoutExpandedDock.setVisibility(View.GONE);
            } else {
                layoutCollapsedPill.setVisibility(View.GONE);
                layoutExpandedDock.setVisibility(View.VISIBLE);
            }
        }
        updateMetricsText();
    }

    private int realTimeFps = 60;
    private int frameCounter = 0;
    private long lastFpsCalcTimeNanos = 0;

    private final android.view.Choreographer.FrameCallback choreographerCallback = new android.view.Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (!isRunning) return;
            frameCounter++;
            if (lastFpsCalcTimeNanos == 0) {
                lastFpsCalcTimeNanos = frameTimeNanos;
            } else {
                long elapsedNanos = frameTimeNanos - lastFpsCalcTimeNanos;
                if (elapsedNanos >= 1_000_000_000L) { // 1 second interval
                    realTimeFps = (int) Math.round((frameCounter * 1_000_000_000.0) / elapsedNanos);
                    frameCounter = 0;
                    lastFpsCalcTimeNanos = frameTimeNanos;
                }
            }
            try {
                android.view.Choreographer.getInstance().postFrameCallback(this);
            } catch (Exception ignored) {}
        }
    };

    private void updateMetricsText() {
        DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(getApplicationContext());
        com.gamebooster.app.core.DisplayCapabilitiesDetector.DisplayCaps caps = 
                com.gamebooster.app.core.DisplayCapabilitiesDetector.detect(getApplicationContext());
        int currentHz = caps != null ? caps.currentRefreshRate : 60;
        int activeFps = realTimeFps > 0 ? realTimeFps : currentHz;

        if (tvPillMetrics != null) {
            tvPillMetrics.setText(String.format("⚡ %d FPS | %.1f°C", activeFps, m.batteryTempC));
        }

        if (tvHudFps != null) {
            tvHudFps.setText(String.format("⚡ HUD FPS: %d • Display: %d Hz", activeFps, currentHz));
        }
        if (tvHudRam != null) {
            tvHudRam.setText(String.format("🧠 Memory RAM: %d%% Used", m.ramUsagePct));
        }
        if (tvHudTemp != null) {
            tvHudTemp.setText(String.format("🌡️ Battery Temp: %.1f°C", m.batteryTempC));
        }
        if (tvHudMa != null) {
            if (m.batteryCurrentMa != 0) {
                tvHudMa.setText(String.format("🔋 Power Current: %d mA", m.batteryCurrentMa));
            } else {
                tvHudMa.setText("🔋 Power Current: Normal");
            }
        }
    }

    private void setupTicker() {
        handler = new Handler(Looper.getMainLooper());
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                updateMetricsText();
                if (handler != null && isRunning) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateRunnable);
        try {
            android.view.Choreographer.getInstance().postFrameCallback(choreographerCallback);
        } catch (Exception ignored) {}
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
        try {
            android.view.Choreographer.getInstance().removeFrameCallback(choreographerCallback);
        } catch (Exception ignored) {}
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
