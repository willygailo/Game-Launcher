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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.device.DeviceInfoChannel;

public class FloatingOverlayService extends Service {

    private static final String CHANNEL_ID = "game_booster_overlay_channel";
    private static final int NOTIF_ID = 888;
    private static boolean isRunning = false;

    private WindowManager windowManager;
    private View overlayView;
    private Handler handler;
    private Runnable updateRunnable;

    /**
     * Class-level LayoutParams so toggleDockState() can dynamically swap window flags
     * without recreating the view (required for FLAG_NOT_TOUCHABLE to take effect).
     */
    private WindowManager.LayoutParams params;

    // -----------------------------------------------------------------------
    // Window flag sets: collapsed = touch pass-through, expanded = interactive
    // -----------------------------------------------------------------------

    /** Collapsed pill state: touch events pass-through to game — zero obstruction. */
    private static final int FLAGS_COLLAPSED =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

    /** Expanded dock state: buttons are interactive, focus still not taken. */
    private static final int FLAGS_EXPANDED =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, createNotification());
        }

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

    /** Kept as field so updateDndButtonTint() can change its tint without re-finding the view. */
    private Button btnDndRef;

    /** Dedicated Runnable ref so scheduleAutoCollapse() can cancel any pending collapse cleanly. */
    private final Runnable autoCollapseRunnable = () -> toggleDockState(true);

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
        View btnHz120 = overlayView.findViewById(R.id.btn_hud_hz120);
        View btnHz144 = overlayView.findViewById(R.id.btn_hud_hz144);
        View btnHz165 = overlayView.findViewById(R.id.btn_hud_hz165);
        View btnBypass = overlayView.findViewById(R.id.btn_hud_bypass);
        View btnTouch = overlayView.findViewById(R.id.btn_hud_touch);

        isDndActive = com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getApplicationContext());

        // Store DND button reference and sync initial tint
        if (btnDnd instanceof Button) {
            btnDndRef = (Button) btnDnd;
            updateDndButtonTint();
        }

        if (tvCollapseBtn != null) {
            tvCollapseBtn.setOnClickListener(v -> toggleDockState(true));
        }

        if (btnBoost != null) {
            btnBoost.setOnClickListener(v -> {
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.RamZramChannel.trimMemoryAndCleanCache(getApplicationContext());
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                        android.widget.Toast.makeText(getApplicationContext(),
                                "⚡ Executed: pm trim-caches 1000M & sync",
                                android.widget.Toast.LENGTH_LONG).show();
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        if (btnExtreme != null) {
            btnExtreme.setOnClickListener(v -> {
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    int targetHz = com.gamebooster.app.config.GameProfileAutoConfigurator.getTargetFpsHz(getApplicationContext());
                    com.gamebooster.app.booster.MaxHzForceChannel.ForceResult forceRes =
                            com.gamebooster.app.booster.MaxHzForceChannel.forceApply(getApplicationContext(), targetHz, null);
                    com.gamebooster.app.booster.PerformanceChannel.ProfileResult result =
                            com.gamebooster.app.booster.PerformanceChannel.applyProfileWithResult(
                                    getApplicationContext(),
                                    com.gamebooster.app.booster.PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                        android.widget.Toast.makeText(getApplicationContext(),
                                "🚀 " + forceRes.message,
                                android.widget.Toast.LENGTH_LONG).show();
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        if (btnDnd != null) {
            btnDnd.setOnClickListener(v -> {
                isDndActive = !isDndActive;
                final boolean targetDnd = isDndActive;
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    boolean applied = com.gamebooster.app.gamespace.GameSpaceDndManager
                            .setGamingDndMode(getApplicationContext(), targetDnd);
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                        android.widget.Toast.makeText(getApplicationContext(), applied
                                ? (targetDnd ? "🚫 Gaming DND enabled" : "🔔 Gaming DND disabled")
                                : "DND permission is required",
                                android.widget.Toast.LENGTH_LONG).show();
                        updateDndButtonTint();
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        if (btnCrosshair != null) {
            btnCrosshair.setOnClickListener(v -> {
                boolean active = CrosshairOverlayManager.toggleCrosshair(getApplicationContext());
                android.widget.Toast.makeText(getApplicationContext(),
                        active ? "🎯 FPS Crosshair Overlay ON" : "🎯 Crosshair OFF",
                        android.widget.Toast.LENGTH_SHORT).show();
                scheduleAutoCollapse();
            });
        }

        if (btnHz120 != null) {
            btnHz120.setOnClickListener(v -> applyHzFromHud(120));
        }
        if (btnHz144 != null) {
            btnHz144.setOnClickListener(v -> applyHzFromHud(144));
        }
        if (btnHz165 != null) {
            btnHz165.setOnClickListener(v -> applyHzFromHud(165));
        }

        if (btnBypass != null) {
            btnBypass.setOnClickListener(v -> {
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.bypasscharging.BypassChargingManager manager =
                            com.gamebooster.app.bypasscharging.BypassChargingManager.getInstance();
                    boolean currentlyEnabled = manager.isBypassEnabled(getApplicationContext());
                    String msg = currentlyEnabled
                            ? manager.disableBypassCharging(getApplicationContext())
                            : manager.enableBypassCharging(getApplicationContext());
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                        android.widget.Toast.makeText(getApplicationContext(),
                                (currentlyEnabled ? "🔌 Charge Bypass OFF: " : "⚡ Charge Bypass ON: ") + msg,
                                android.widget.Toast.LENGTH_LONG).show();
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        if (btnTouch != null) {
            btnTouch.setOnClickListener(v -> {
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.config.TouchUltraFastNoDelayPatcher.applyTouchNoDelay(null);
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                        android.widget.Toast.makeText(getApplicationContext(),
                                "👆 Touch Zero-Delay Response Applied",
                                android.widget.Toast.LENGTH_LONG).show();
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // Start collapsed → FLAG_NOT_TOUCHABLE ensures the pill NEVER intercepts game touches.
        // Flags are swapped dynamically in toggleDockState() via windowManager.updateViewLayout().
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                FLAGS_COLLAPSED,
                PixelFormat.TRANSLUCENT
        );

        // Force overlay window to request maximum hardware refresh rate (120Hz, 144Hz, 165Hz)
        try {
            com.gamebooster.app.device.DisplayCapabilitiesDetector.DisplayCaps caps =
                    com.gamebooster.app.device.DisplayCapabilitiesDetector.detect(getApplicationContext());
            if (caps != null && caps.maxRefreshRate > 0) {
                params.preferredRefreshRate = (float) caps.maxRefreshRate;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && windowManager != null) {
                android.view.Display display = windowManager.getDefaultDisplay();
                if (display != null) {
                    android.view.Display.Mode[] modes = display.getSupportedModes();
                    int bestModeId = 0;
                    float maxFps = 0.0f;
                    for (android.view.Display.Mode m : modes) {
                        if (m != null && m.getRefreshRate() > maxFps) {
                            maxFps = m.getRefreshRate();
                            bestModeId = m.getModeId();
                        }
                    }
                    if (bestModeId != 0) {
                        params.preferredDisplayModeId = bestModeId;
                    }
                }
            }
        } catch (Throwable ignored) {}

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
                            // Magnetic Edge Snapping with safety clamp (prevents negative x)
                            if (windowManager != null && overlayView != null) {
                                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                                int viewWidth = overlayView.getWidth();
                                int midPoint = screenWidth / 2;
                                if (params.x + (viewWidth / 2) < midPoint) {
                                    params.x = 10; // Magnetic Snap Left
                                } else {
                                    // Safety clamp: never go negative or off-screen
                                    params.x = Math.max(10, Math.max(0, screenWidth - viewWidth - 10));
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

        // Semi-transparent pill initially so it's less distracting while gaming
        overlayView.setAlpha(0.75f);

        try {
            windowManager.addView(overlayView, params);
            return true;
        } catch (Exception e) {
            isRunning = false;
            return false;
        }
    }

    private void applyHzFromHud(int hz) {
        com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
            com.gamebooster.app.booster.MaxHzForceChannel.ForceResult forceRes =
                    com.gamebooster.app.booster.MaxHzForceChannel.forceApply(getApplicationContext(), hz, null);
            if (forceRes.success) {
                com.gamebooster.app.device.DisplayRefreshRatePreferences.saveSelectedHz(getApplicationContext(), forceRes.appliedHz);
                com.gamebooster.app.config.GameProfileAutoConfigurator.setTargetFpsHz(getApplicationContext(), forceRes.appliedHz);
            }

            com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                android.widget.Toast.makeText(getApplicationContext(),
                        "Requested supported refresh mode: " + forceRes.message,
                        android.widget.Toast.LENGTH_LONG).show();
                scheduleAutoCollapse();
            });
        });
    }

    /**
     * Updates the DND button background tint to visually reflect active/inactive state.
     * Green = DND active (notifications silenced), default grey = DND inactive.
     */
    private void updateDndButtonTint() {
        if (btnDndRef == null) return;
        if (isDndActive) {
            btnDndRef.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#FF4CAF50")));
            btnDndRef.setTextColor(Color.parseColor("#FF000000"));
        } else {
            // Reset to original card_bg colour
            btnDndRef.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#CC1C1C2E")));
            btnDndRef.setTextColor(Color.parseColor("#FFE0E0E0"));
        }
    }

    /**
     * Schedules the HUD dock to auto-collapse 1.5 seconds after an action button is pressed,
     * so the overlay automatically gets out of the way during gameplay.
     * Any previous pending collapse is cancelled before rescheduling.
     */
    private void scheduleAutoCollapse() {
        if (handler == null) return;
        handler.removeCallbacks(autoCollapseRunnable);
        handler.postDelayed(autoCollapseRunnable, 1500);
    }

    /**
     * Toggles between collapsed pill mode and expanded dock mode.
     * Critically, this also swaps the WindowManager flags so:
     *   - Collapsed → FLAG_NOT_TOUCHABLE (game receives all touches)
     *   - Expanded  → no FLAG_NOT_TOUCHABLE (HUD buttons are interactive)
     */
    private void toggleDockState(boolean collapse) {
        this.isCollapsed = collapse;
        if (layoutCollapsedPill != null && layoutExpandedDock != null) {
            if (collapse) {
                layoutCollapsedPill.setVisibility(View.VISIBLE);
                layoutExpandedDock.setVisibility(View.GONE);
                // Collapsed: pass touch events through — game is never blocked
                if (params != null) params.flags = FLAGS_COLLAPSED;
                overlayView.setAlpha(0.75f);
            } else {
                layoutCollapsedPill.setVisibility(View.GONE);
                layoutExpandedDock.setVisibility(View.VISIBLE);
                // Expanded: allow user to interact with HUD buttons
                if (params != null) params.flags = FLAGS_EXPANDED;
                overlayView.setAlpha(1.0f);
            }
            // Apply updated flags to the live window immediately
            if (windowManager != null && overlayView != null && params != null) {
                try {
                    windowManager.updateViewLayout(overlayView, params);
                } catch (Exception ignored) {}
            }
        }
        updateMetricsText();
    }

    private int realTimeFps = 0;
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
        com.gamebooster.app.device.DisplayCapabilitiesDetector.DisplayCaps caps =
                com.gamebooster.app.device.DisplayCapabilitiesDetector.detect(getApplicationContext());
        int currentHz = (caps != null && caps.currentRefreshRate > 0) ? caps.currentRefreshRate : 0;

        int activeFps = realTimeFps > 0 ? realTimeFps : currentHz;

        // Try querying live SurfaceFlinger game FPS via Shizuku ADB
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            try {
                String sfFpsRes = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("cmd SurfaceFlinger get_fps");
                if (sfFpsRes != null && sfFpsRes.contains("FPS:")) {
                    String[] parts = sfFpsRes.split("FPS:");
                    if (parts.length > 1) {
                        float sfFps = Float.parseFloat(parts[1].trim().split(" ")[0]);
                        if (sfFps > 0) {
                            activeFps = Math.round(sfFps);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        float socTemp = com.gamebooster.app.booster.thermal.ThermalMonitorService.readSocTemperatureSysfs();
        float displayTemp = socTemp > 0 ? socTemp : m.batteryTempC;
        int pingMs = com.gamebooster.app.booster.NetworkOptimizer.measureNetworkPingMs();
        String pingStr = pingMs > 0 ? pingMs + "ms" : "--";

        if (tvPillMetrics != null) {
            tvPillMetrics.setText(String.format("⚡ %d FPS | %dHz | %.1f°C | %s", activeFps, currentHz, displayTemp, pingStr));
        }

        if (tvHudFps != null) {
            tvHudFps.setText(String.format("⚡ FPS: %d • Display: %d Hz • Ping: %s", activeFps, currentHz, pingStr));
        }
        if (tvHudRam != null) {
            tvHudRam.setText(String.format("🧠 Memory RAM: %d%% Used", m.ramUsagePct));
        }
        if (tvHudTemp != null) {
            tvHudTemp.setText(String.format("🌡️ SOC Temp: %.1f°C (Bat: %.1f°C)", displayTemp, m.batteryTempC));
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
        if (handler != null) {
            if (updateRunnable != null) handler.removeCallbacks(updateRunnable);
            handler.removeCallbacks(autoCollapseRunnable);
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
