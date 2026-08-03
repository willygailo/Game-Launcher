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

    private View layoutCollapsedPill;
    private View layoutExpandedDock;
    private TextView tvPillMetrics;
    private TextView tvHudFps;
    private TextView tvHudRam;
    private TextView tvHudTemp;
    private TextView tvHudMa;
    private boolean isCollapsed = true;
    private boolean isDndActive = false;

    private void setupFloatingView() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager == null) return;

        android.view.LayoutInflater inflater = (android.view.LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return;
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
                    com.gamebooster.app.functions.PerformanceChannel.applyProfile(getApplicationContext(), com.gamebooster.app.functions.PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() ->
                            android.widget.Toast.makeText(getApplicationContext(), "🔥 Executed: 165Hz Lock & Vulkan 3D Profile", android.widget.Toast.LENGTH_LONG).show());
                });
            });
        }

        if (btnDnd != null) {
            btnDnd.setOnClickListener(v -> {
                isDndActive = !isDndActive;
                final boolean targetDnd = isDndActive;
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.root.CommandExecutor.executeSystemCommand(targetDnd ? "settings put global zen_mode 2" : "settings put global zen_mode 0");
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() ->
                            android.widget.Toast.makeText(getApplicationContext(), targetDnd ? "🚫 Executed: settings put global zen_mode 2" : "🔔 Executed: settings put global zen_mode 0", android.widget.Toast.LENGTH_LONG).show());
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

        overlayView.setOnTouchListener(new View.OnTouchListener() {
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
                            toggleDockState(!isCollapsed);
                        } else {
                            // Magnetic Edge Snapping
                            if (windowManager != null && overlayView != null) {
                                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                                int viewWidth = overlayView.getWidth();
                                int midPoint = screenWidth / 2;
                                if (params.x + (viewWidth / 2) < midPoint) {
                                    params.x = 10; // Magnetic Snap Left
                                } else {
                                    params.x = screenWidth - viewWidth - 10; // Magnetic Snap Right
                                }
                                windowManager.updateViewLayout(overlayView, params);
                            }
                        }
                        return true;
                }
                return false;
            }
        });

        try {
            windowManager.addView(overlayView, params);
        } catch (Exception e) {
            isRunning = false;
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

    private void updateMetricsText() {
        DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(getApplicationContext());
        com.gamebooster.app.core.DisplayCapabilitiesDetector.DisplayCaps caps = 
                com.gamebooster.app.core.DisplayCapabilitiesDetector.detect(getApplicationContext());
        int currentHz = caps != null ? caps.currentRefreshRate : 60;

        if (tvPillMetrics != null) {
            tvPillMetrics.setText(String.format("⚡ %d FPS | %.1f°C", currentHz, m.batteryTempC));
        }

        if (tvHudFps != null) {
            tvHudFps.setText(String.format("⚡ FPS / Refresh Rate: %d Hz", currentHz));
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
