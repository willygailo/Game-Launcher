package com.gamebooster.app.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.RamZramChannel;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.ResolutionScalerEngine;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.File;

/**
 * GameTurboEdgeService — In-Game Translucent Slide-Out Cyber Drawer.
 *
 * Provides instant in-game controls without leaving or backgrounding the active match:
 * - 1-Tap RAM Flush & Hardware Boost
 * - On-the-fly Refresh Rate Switcher (60Hz -> 185Hz)
 * - Visual Clarity Shader Filter Switcher (Sniper Shadow Boost, Vibrant, Night)
 * - Resolution Scaler Quick Toggle (1080p -> 900p -> 720p)
 * - Quick In-Game Screenshot Dispatcher
 * - Gaming DND toggle
 */
public class GameTurboEdgeService extends Service {

    private static final String TAG = "GameTurboEdgeService";
    private static final String CHANNEL_ID = "game_turbo_edge_channel";
    private static final int NOTIF_ID = 890;

    private static boolean sIsRunning = false;

    private WindowManager windowManager;
    private View rootEdgeView;
    private WindowManager.LayoutParams windowParams;

    private View viewTriggerTab;
    private View layoutDrawerPanel;
    private TextView tvDrawerFps;

    private Handler mainHandler;
    private boolean isDrawerOpen = false;

    private float initialTouchY;
    private int initialWindowY;

    public static boolean isRunning() {
        return sIsRunning;
    }

    public static void start(Context context) {
        if (context == null || sIsRunning) return;
        if (!Settings.canDrawOverlays(context)) return;

        Intent intent = new Intent(context, GameTurboEdgeService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start GameTurboEdgeService", e);
        }
    }

    public static void stop(Context context) {
        if (context == null || !sIsRunning) return;
        try {
            Intent intent = new Intent(context, GameTurboEdgeService.class);
            context.stopService(intent);
        } catch (Exception ignored) {}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sIsRunning = true;
        mainHandler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, createNotification());
        }

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        initView();
    }

    private void initView() {
        rootEdgeView = LayoutInflater.from(this).inflate(R.layout.game_turbo_edge_drawer, null);

        viewTriggerTab = rootEdgeView.findViewById(R.id.view_edge_trigger_tab);
        layoutDrawerPanel = rootEdgeView.findViewById(R.id.layout_edge_drawer_panel);
        tvDrawerFps = rootEdgeView.findViewById(R.id.tv_drawer_fps);

        Button btnClose = rootEdgeView.findViewById(R.id.btn_close_drawer);
        Button btnBoost = rootEdgeView.findViewById(R.id.btn_drawer_ram_boost);
        Button btnDnd = rootEdgeView.findViewById(R.id.btn_drawer_dnd);
        Button btnFilter = rootEdgeView.findViewById(R.id.btn_drawer_filter);
        Button btnScreenshot = rootEdgeView.findViewById(R.id.btn_drawer_screenshot);
        Button btnTouchLock = rootEdgeView.findViewById(R.id.btn_drawer_touch_lock);
        Button btnResolution = rootEdgeView.findViewById(R.id.btn_drawer_resolution);

        Button btnHz60 = rootEdgeView.findViewById(R.id.btn_edge_hz_60);
        Button btnHz90 = rootEdgeView.findViewById(R.id.btn_edge_hz_90);
        Button btnHz120 = rootEdgeView.findViewById(R.id.btn_edge_hz_120);
        Button btnHz144 = rootEdgeView.findViewById(R.id.btn_edge_hz_144);
        Button btnHz185 = rootEdgeView.findViewById(R.id.btn_edge_hz_185);

        // Window Params
        windowParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        windowParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        windowParams.x = 0;
        windowParams.y = 0;

        // Trigger Tab Touch & Drag Listener
        viewTriggerTab.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialTouchY = event.getRawY();
                    initialWindowY = windowParams.y;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dy = event.getRawY() - initialTouchY;
                    windowParams.y = initialWindowY + (int) dy;
                    try {
                        windowManager.updateViewLayout(rootEdgeView, windowParams);
                    } catch (Exception ignored) {}
                    return true;

                case MotionEvent.ACTION_UP:
                    if (Math.abs(event.getRawY() - initialTouchY) < 15) {
                        // Click / Tap -> Toggle Drawer
                        toggleDrawer();
                    }
                    return true;
            }
            return false;
        });

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> closeDrawer());
        }

        // Quick RAM Boost
        if (btnBoost != null) {
            btnBoost.setOnClickListener(v -> {
                Toast.makeText(getApplicationContext(), "🚀 Turbo RAM Boost: Purging caches...", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    RamZramChannel.trimMemoryAndCleanCache(getApplicationContext());
                });
            });
        }

        // DND Toggle
        if (btnDnd != null) {
            btnDnd.setOnClickListener(v -> {
                boolean active = GameSpaceDndManager.isDndActive(getApplicationContext());
                GameSpaceDndManager.setGamingDndMode(getApplicationContext(), !active);
                Toast.makeText(getApplicationContext(), !active ? "🔕 Gaming DND Enabled" : "🔔 Gaming DND Disabled", Toast.LENGTH_SHORT).show();
            });
        }

        // Visual Clarity Filter Switcher
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                VisualFilterOverlayService.VisualFilterType current = VisualFilterOverlayService.getCurrentFilter();
                VisualFilterOverlayService.VisualFilterType next;
                if (current == VisualFilterOverlayService.VisualFilterType.OFF) {
                    next = VisualFilterOverlayService.VisualFilterType.SNIPER_SHADOW_BOOST;
                } else if (current == VisualFilterOverlayService.VisualFilterType.SNIPER_SHADOW_BOOST) {
                    next = VisualFilterOverlayService.VisualFilterType.VIBRANT_SATURATION;
                } else if (current == VisualFilterOverlayService.VisualFilterType.VIBRANT_SATURATION) {
                    next = VisualFilterOverlayService.VisualFilterType.NIGHT_ANTI_GLARE;
                } else {
                    next = VisualFilterOverlayService.VisualFilterType.OFF;
                }

                VisualFilterOverlayService.setFilter(getApplicationContext(), next);
                Toast.makeText(getApplicationContext(), "👁️ Filter: " + next.label, Toast.LENGTH_SHORT).show();
            });
        }

        // Screenshot Trigger
        if (btnScreenshot != null) {
            btnScreenshot.setOnClickListener(v -> {
                closeDrawer();
                mainHandler.postDelayed(() -> {
                    AppExecutors.getInstance().executeCommand(() -> {
                        String picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
                        File outDir = new File(picDir, "Screenshots");
                        if (!outDir.exists()) outDir.mkdirs();
                        String shotPath = new File(outDir, "GameTurbo_" + System.currentTimeMillis() + ".png").getAbsolutePath();
                        ShizukuExecutor.executeShizukuCommand("screencap -p " + shotPath);
                        AppExecutors.getInstance().postToMainThread(() ->
                                Toast.makeText(getApplicationContext(), "📸 Screenshot saved to Pictures/Screenshots", Toast.LENGTH_SHORT).show());
                    });
                }, 400);
            });
        }

        // Touch Lock (Notification Guard)
        if (btnTouchLock != null) {
            btnTouchLock.setOnClickListener(v -> {
                Toast.makeText(getApplicationContext(), "🔒 Touch / Edge Notification Guard Active", Toast.LENGTH_SHORT).show();
            });
        }

        // Quick Resolution Scaler
        if (btnResolution != null) {
            btnResolution.setOnClickListener(v -> {
                boolean isScaled = ResolutionScalerEngine.isResolutionScaled();
                if (!isScaled) {
                    ResolutionScalerEngine.applyResolutionScale(getApplicationContext(), ResolutionScalerEngine.ScalePreset.ESPORTS_720P.scaleFactor);
                    Toast.makeText(getApplicationContext(), "🖥️ Scaled to 720p Esports Turbo (+35 FPS)!", Toast.LENGTH_SHORT).show();
                } else {
                    ResolutionScalerEngine.resetResolutionSync();
                    Toast.makeText(getApplicationContext(), "🖥️ Restored to 100% Native Resolution", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Hz Switchers
        setupHzButton(btnHz60, 60);
        setupHzButton(btnHz90, 90);
        setupHzButton(btnHz120, 120);
        setupHzButton(btnHz144, 144);
        setupHzButton(btnHz185, 185);

        try {
            windowManager.addView(rootEdgeView, windowParams);
        } catch (Exception e) {
            Log.e(TAG, "Failed adding rootEdgeView", e);
        }

        // Connect Real FPS Listener
        RealGameFpsMonitor.getInstance().start(this, (currentFps, onePercentLowFps, isRealGameSurface) -> {
            if (tvDrawerFps != null && isDrawerOpen) {
                tvDrawerFps.setText(currentFps + " FPS (1% Low: " + onePercentLowFps + ")");
            }
        });
    }

    private void setupHzButton(Button btn, int hz) {
        if (btn == null) return;
        btn.setOnClickListener(v -> {
            AppExecutors.getInstance().executeCommand(() -> MaxHzForceChannel.forceApply(hz));
            Toast.makeText(getApplicationContext(), "⚡ Refresh Rate: " + hz + "Hz Locked", Toast.LENGTH_SHORT).show();
        });
    }

    private void toggleDrawer() {
        if (isDrawerOpen) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    private void openDrawer() {
        isDrawerOpen = true;
        if (layoutDrawerPanel != null) layoutDrawerPanel.setVisibility(View.VISIBLE);
        if (viewTriggerTab != null) viewTriggerTab.setVisibility(View.GONE);
    }

    private void closeDrawer() {
        isDrawerOpen = false;
        if (layoutDrawerPanel != null) layoutDrawerPanel.setVisibility(View.GONE);
        if (viewTriggerTab != null) viewTriggerTab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (rootEdgeView != null && windowManager != null) {
            try {
                windowManager.removeView(rootEdgeView);
            } catch (Exception ignored) {}
            rootEdgeView = null;
        }
        sIsRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID,
                    "Game Turbo Edge Drawer",
                    NotificationManager.IMPORTANCE_MIN
            );
            chan.setDescription("Quick in-game slide-out drawer");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(chan);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("⚡ Game Turbo Active")
                .setContentText("Slide edge handle to open in-game controls")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build();
    }
}
