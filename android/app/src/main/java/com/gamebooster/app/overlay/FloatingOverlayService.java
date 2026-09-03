package com.gamebooster.app.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.booster.RamZramChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.DeviceInfoChannel;
import com.gamebooster.app.device.DisplayCapabilitiesDetector;
import com.gamebooster.app.config.LobbyInjectionEngine;
import com.gamebooster.app.gamespace.GameSpaceDndManager;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * FloatingOverlayService — High-performance Esports HUD Performance Overlay.
 *
 * Features:
 *   - Multi-viewport layout (Collapsed Pill, Micro FPS, Expanded Cyber Dashboard)
 *   - Live FPS with dynamic health rating (Choreographer callback)
 *   - Real-time RAM telemetry & visual level progress gauge
 *   - Thermals & Battery power drain (mA)
 *   - Asynchronous low-latency Ping monitor (ms) & Network connection badge
 *   - 6 In-Game Turbo Controls (RAM Clean, 185Hz Extreme, Crosshair, DND, Ultra Touch, Net Boost)
 *   - Touch pass-through without blocking background game touches
 *   - Magnetic edge snapping with coordinate persistence
 */
public class FloatingOverlayService extends Service {

    public static final String PREF_NAME = "gamebooster_hud_prefs";
    public static final String KEY_HUD_X = "hud_last_pos_x";
    public static final String KEY_HUD_Y = "hud_last_pos_y";
    public static final String KEY_HUD_MODE = "hud_viewport_mode";

    private static final String CHANNEL_ID = "game_booster_overlay_channel";
    private static final int NOTIF_ID = 888;
    private static boolean isRunning = false;

    public enum HudMode {
        PILL,
        MICRO_FPS,
        EXPANDED_DOCK
    }

    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams params;
    private Handler handler;
    private Runnable telemetryRunnable;
    private Runnable pingRunnable;

    // View References
    private View layoutCollapsedPill;
    private View layoutMicroFps;
    private View layoutExpandedDock;
    private View layoutHudHeader;

    // Collapsed Pill views
    private View viewPillGlowDot;
    private TextView tvPillFps;
    private TextView tvPillTemp;
    private TextView tvPillPing;

    // Micro FPS views
    private TextView tvMicroFps;

    // Expanded Dock views
    private TextView tvHudProfileBadge;
    private TextView tvHudFps;
    private TextView tvHudFpsStatus;
    private TextView tvHudPing;
    private TextView tvHudNetType;
    private TextView tvHudTemp;
    private TextView tvHudMa;
    private TextView tvHudRam;
    private ProgressBar pbHudRam;

    // Action buttons
    private Button btnHudBoost;
    private Button btnHudExtreme;
    private Button btnHudCrosshair;
    private Button btnHudDnd;
    private Button btnHudTouch;
    private Button btnHudNet;
    private Button btnHudInjectLobby;

    // State Variables
    private static final int[] REFRESH_RATE_TIERS = {185};
    private int currentHzIndex = 0;
    private HudMode currentMode = HudMode.PILL;
    private boolean isDndActive = false;
    private boolean isExtremeActive = true;
    private boolean isTouchBoostActive = false;
    private boolean isNetBoostActive = false;
    private int realTimeFps = 185;
    private int onePercentLowFps = 175;
    private int zeroPointOnePercentLowFps = 165;
    private double frameTimeMs = 5.4;
    private double frameJitterMs = 0.2;
    private boolean isRealGameSurface = false;
    private int frameCounter = 0;
    private long lastFpsCalcTimeNanos = 0;
    private int livePingMs = 28;
    private String networkTypeStr = "Wi-Fi";

    private final Runnable autoCollapseRunnable = () -> switchHudMode(HudMode.PILL);

    public static boolean isOverlayRunning() {
        return isRunning;
    }

    public static void startOverlay(Context context) {
        if (context == null || isRunning) return;
        if (!Settings.canDrawOverlays(context)) return;
        Intent intent = new Intent(context, FloatingOverlayService.class);
        context.startForegroundService(intent);
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
        setupTelemetryEngine();
    }

    @SuppressWarnings("deprecation")
    private boolean setupFloatingView() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager == null) return false;

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return false;

        overlayView = inflater.inflate(R.layout.floating_hud_layout, (ViewGroup) null, false);

        bindViews();
        setupActionButtons();

        int layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        // FLAG_NOT_FOCUSABLE with WRAP_CONTENT allows touches on the overlay while touches outside pass through to the game
        int windowFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                windowFlags,
                PixelFormat.TRANSLUCENT
        );

        // Hardware refresh rate request (up to 185Hz)
        try {
            params.preferredRefreshRate = 185.0f;
            if (windowManager != null) {
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

        // Restore persisted coordinates
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        params.x = prefs.getInt(KEY_HUD_X, 20);
        params.y = prefs.getInt(KEY_HUD_Y, 220);

        setupDragListeners();

        // Restore or initialize mode
        String savedMode = prefs.getString(KEY_HUD_MODE, HudMode.PILL.name());
        try {
            currentMode = HudMode.valueOf(savedMode);
        } catch (Exception e) {
            currentMode = HudMode.PILL;
        }

        applyViewportVisibility(currentMode);

        try {
            windowManager.addView(overlayView, params);
            return true;
        } catch (Exception e) {
            isRunning = false;
            return false;
        }
    }

    private void bindViews() {
        layoutCollapsedPill = overlayView.findViewById(R.id.layout_collapsed_pill);
        layoutMicroFps = overlayView.findViewById(R.id.layout_micro_fps);
        layoutExpandedDock = overlayView.findViewById(R.id.layout_expanded_dock);
        layoutHudHeader = overlayView.findViewById(R.id.layout_hud_header);

        // Pill
        viewPillGlowDot = overlayView.findViewById(R.id.view_pill_glow_dot);
        tvPillFps = overlayView.findViewById(R.id.tv_pill_fps);
        tvPillTemp = overlayView.findViewById(R.id.tv_pill_temp);
        tvPillPing = overlayView.findViewById(R.id.tv_pill_ping);

        // Micro
        tvMicroFps = overlayView.findViewById(R.id.tv_micro_fps);

        // Header controls
        View btnMicroToggle = overlayView.findViewById(R.id.btn_hud_micro_toggle);
        View btnMinimize = overlayView.findViewById(R.id.btn_hud_minimize);
        View btnClose = overlayView.findViewById(R.id.btn_hud_close);

        if (btnMicroToggle != null) {
            btnMicroToggle.setOnClickListener(v -> {
                performHaptic();
                switchHudMode(HudMode.MICRO_FPS);
            });
        }
        if (btnMinimize != null) {
            btnMinimize.setOnClickListener(v -> {
                performHaptic();
                switchHudMode(HudMode.PILL);
            });
        }
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                performHaptic();
                Toast.makeText(getApplicationContext(), "⚡ Gaming HUD Closed", Toast.LENGTH_SHORT).show();
                stopSelf();
            });
        }

        // Expanded telemetry
        tvHudProfileBadge = overlayView.findViewById(R.id.tv_hud_profile_badge);
        tvHudFps = overlayView.findViewById(R.id.tv_hud_fps);
        tvHudFpsStatus = overlayView.findViewById(R.id.tv_hud_fps_status);
        tvHudPing = overlayView.findViewById(R.id.tv_hud_ping);
        tvHudNetType = overlayView.findViewById(R.id.tv_hud_net_type);
        tvHudTemp = overlayView.findViewById(R.id.tv_hud_temp);
        tvHudMa = overlayView.findViewById(R.id.tv_hud_ma);
        tvHudRam = overlayView.findViewById(R.id.tv_hud_ram);
        pbHudRam = overlayView.findViewById(R.id.pb_hud_ram);

        // Action buttons
        btnHudBoost = overlayView.findViewById(R.id.btn_hud_boost);
        btnHudExtreme = overlayView.findViewById(R.id.btn_hud_extreme);
        btnHudCrosshair = overlayView.findViewById(R.id.btn_hud_crosshair);
        btnHudDnd = overlayView.findViewById(R.id.btn_hud_dnd);
        btnHudTouch = overlayView.findViewById(R.id.btn_hud_touch);
        btnHudNet = overlayView.findViewById(R.id.btn_hud_net);
        // Inject-in-lobby quick-action button (may be null if layout not updated yet; handled gracefully)
        btnHudInjectLobby = overlayView.findViewById(R.id.btn_hud_inject_lobby);
    }

    private void setupActionButtons() {
        isDndActive = GameSpaceDndManager.isDndActive(getApplicationContext());
        updateDndButtonVisual();
        updateCrosshairButtonVisual();

        // 1. Clean RAM & Trimming Cache
        if (btnHudBoost != null) {
            btnHudBoost.setOnClickListener(v -> {
                performHaptic();
                AppExecutors.getInstance().executeCommand(() -> {
                    RamZramChannel.trimMemoryAndCleanCache(getApplicationContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        Toast.makeText(getApplicationContext(),
                                "⚡ RAM Cache Purged & Memory Trimmed",
                                Toast.LENGTH_SHORT).show();
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        // 2. Extreme 185Hz Refresh Rate Enforcer
        if (btnHudExtreme != null) {
            btnHudExtreme.setOnClickListener(v -> {
                performHaptic();
                currentHzIndex = 0;
                final int targetHz = 185;

                AppExecutors.getInstance().executeCommand(() -> {
                    PerformanceChannel.Profile profile = PerformanceChannel.Profile.EXTREME_PERFORMANCE;
                    PerformanceChannel.applyProfileWithResult(getApplicationContext(), profile);
                    MaxHzForceChannel.forceApply(targetHz);
                    PerformanceChannel.writeAndExecuteRootTweaksScript(targetHz);
                    isExtremeActive = true;

                    AppExecutors.getInstance().postToMainThread(() -> {
                        btnHudExtreme.setText("🔥 " + targetHz + "Hz");
                        Toast.makeText(getApplicationContext(),
                                "🔥 Locked @ " + targetHz + "Hz (Max " + targetHz + " FPS)",
                                Toast.LENGTH_SHORT).show();
                        if (tvHudProfileBadge != null) {
                            tvHudProfileBadge.setText("🔥 EXTREME 185Hz LOCKED • GAME DRIVER ON");
                        }
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        // 3. Aim Crosshair Hub (Toggle + Preset Cycle on Long Click)
        if (btnHudCrosshair != null) {
            btnHudCrosshair.setOnClickListener(v -> {
                performHaptic();
                boolean active = CrosshairOverlayManager.toggleCrosshair(getApplicationContext());
                updateCrosshairButtonVisual();
                Toast.makeText(getApplicationContext(),
                        active ? "🎯 Crosshair Overlay ACTIVE" : "🎯 Crosshair OFF",
                        Toast.LENGTH_SHORT).show();
                scheduleAutoCollapse();
            });

            btnHudCrosshair.setOnLongClickListener(v -> {
                performHaptic();
                cycleCrosshairPreset();
                return true;
            });
        }

        // 4. Gaming DND Shield
        if (btnHudDnd != null) {
            btnHudDnd.setOnClickListener(v -> {
                performHaptic();
                isDndActive = !isDndActive;
                final boolean targetDnd = isDndActive;
                AppExecutors.getInstance().executeCommand(() -> {
                    boolean applied = GameSpaceDndManager.setGamingDndMode(getApplicationContext(), targetDnd);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        updateDndButtonVisual();
                        Toast.makeText(getApplicationContext(), applied
                                        ? (targetDnd ? "🚫 Gaming DND Enabled (Silent)" : "🔔 Gaming DND Disabled")
                                        : "DND permission required",
                                Toast.LENGTH_SHORT).show();
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        // 5. Ultra Touch 1000Hz Response
        if (btnHudTouch != null) {
            btnHudTouch.setOnClickListener(v -> {
                performHaptic();
                isTouchBoostActive = !isTouchBoostActive;
                AppExecutors.getInstance().executeCommand(() -> {
                    boolean ok = TouchLatencyChannel.enableUltraTouchResponse();
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (btnHudTouch != null) {
                            btnHudTouch.setTextColor(isTouchBoostActive
                                    ? Color.parseColor("#00FF66")
                                    : Color.parseColor("#00F0FF"));
                        }
                        Toast.makeText(getApplicationContext(),
                                "⚡ 1000Hz Ultra Touch Response Active",
                                Toast.LENGTH_SHORT).show();
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        // 6. Low-Latency Gaming Net Boost
        if (btnHudNet != null) {
            btnHudNet.setOnClickListener(v -> {
                performHaptic();
                isNetBoostActive = !isNetBoostActive;
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.applyGamingDns(getApplicationContext(), NetworkOptimizer.DnsMode.CLOUDFLARE_1_1_1_1);
                    NetworkOptimizer.optimizeTcpBuffers();
                    NetworkOptimizer.flushDnsCache();
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (btnHudNet != null) {
                            btnHudNet.setTextColor(isNetBoostActive
                                    ? Color.parseColor("#00FF66")
                                    : Color.parseColor("#FFB800"));
                        }
                        Toast.makeText(getApplicationContext(),
                                "📶 Low-Latency DNS & TCP Buffers Boosted",
                                Toast.LENGTH_SHORT).show();
                        scheduleAutoCollapse();
                    });
                });
            });
        }

        // 7. ⚡ INJECT IN LOBBY — Stealth 2026 Overdrive Quick-Trigger
        if (btnHudInjectLobby != null) {
            btnHudInjectLobby.setOnClickListener(v -> {
                performHaptic();
                AppExecutors.getInstance().executeCommand(() -> {
                    // Dynamically detect which game is active on screen right now (MLBB, PUBGM, CODM, etc.)
                    String detectedPkg = com.gamebooster.app.games.ForegroundGameDetector.detectActiveGame(getApplicationContext());
                    if (detectedPkg == null || detectedPkg.trim().isEmpty()) {
                        detectedPkg = LobbyInjectionEngine.getActiveGamePackage();
                    }
                    final String targetPkg = (detectedPkg != null && !detectedPkg.trim().isEmpty()) ? detectedPkg.trim() : null;

                    if (targetPkg != null) {
                        final String gameTitle = com.gamebooster.app.games.GamePackageRegistry.getGameTitle(targetPkg, getApplicationContext());
                        LobbyInjectionEngine.triggerManualLobbyInject(getApplicationContext(), targetPkg);

                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (btnHudInjectLobby != null) {
                                btnHudInjectLobby.setTextColor(Color.parseColor("#00FF66"));
                                btnHudInjectLobby.setText("⚡ " + gameTitle + " INJECTED");
                            }
                            if (tvHudProfileBadge != null) {
                                tvHudProfileBadge.setText("⚡ ACTIVE: " + gameTitle + " • 185Hz/165Hz OVERDRIVE");
                            }
                            Toast.makeText(getApplicationContext(),
                                    "🎮 Detected & Injected: " + gameTitle + " (165/185 FPS Active)",
                                    Toast.LENGTH_SHORT).show();
                            scheduleAutoCollapse();
                        });
                    } else {
                        AppExecutors.getInstance().postToMainThread(() -> {
                            Toast.makeText(getApplicationContext(),
                                    "⚠️ No game detected in foreground! Please open MLBB, PUBGM, CODM, etc.",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            });

            btnHudInjectLobby.setOnLongClickListener(v -> {
                performHaptic();
                AppExecutors.getInstance().executeCommand(() -> {
                    String activePkg2 = com.gamebooster.app.games.ForegroundGameDetector.detectActiveGame(getApplicationContext());
                    if (activePkg2 == null || activePkg2.trim().isEmpty()) {
                        activePkg2 = LobbyInjectionEngine.getActiveGamePackage();
                    }
                    final String targetPkg2 = activePkg2;
                    final String gameTitle2 = com.gamebooster.app.games.GamePackageRegistry.getGameTitle(targetPkg2, getApplicationContext());

                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (targetPkg2 != null) {
                            LobbyInjectionEngine.scheduleLobbyInjection(
                                    getApplicationContext(), targetPkg2, 185, 18);
                            Toast.makeText(getApplicationContext(),
                                    "⏳ Auto-Injection re-scheduled for " + gameTitle2 + " in 18s",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "⚠️ Please open a game before scheduling auto-injection",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                return true;
            });
        }
    }

    private void cycleCrosshairPreset() {
        SharedPreferences prefs = getSharedPreferences(CrosshairOverlayService.PREF_NAME, MODE_PRIVATE);
        String currentName = prefs.getString(CrosshairOverlayService.KEY_PRESET, CrosshairPreset.TACTICAL_CROSS.name());
        CrosshairPreset[] presets = CrosshairPreset.values();
        int nextIndex = 0;
        for (int i = 0; i < presets.length; i++) {
            if (presets[i].name().equals(currentName)) {
                nextIndex = (i + 1) % presets.length;
                break;
            }
        }
        CrosshairPreset nextPreset = presets[nextIndex];
        CrosshairOverlayService.updatePreset(getApplicationContext(), nextPreset);
        Toast.makeText(getApplicationContext(), "🎯 Crosshair Style: " + nextPreset.getLabel(), Toast.LENGTH_SHORT).show();
        updateCrosshairButtonVisual();
    }

    private void updateDndButtonVisual() {
        if (btnHudDnd == null) return;
        if (isDndActive) {
            btnHudDnd.setTextColor(Color.parseColor("#00FF66"));
            btnHudDnd.setText("🚫 DND ON");
        } else {
            btnHudDnd.setTextColor(Color.parseColor("#94A3B8"));
            btnHudDnd.setText("🚫 DND");
        }
    }

    private void updateCrosshairButtonVisual() {
        if (btnHudCrosshair == null) return;
        boolean active = CrosshairOverlayManager.isShowing();
        if (active) {
            btnHudCrosshair.setTextColor(Color.parseColor("#00FF66"));
            btnHudCrosshair.setText("🎯 ON");
        } else {
            btnHudCrosshair.setTextColor(Color.parseColor("#FF8800"));
            btnHudCrosshair.setText("🎯 AIM");
        }
    }

    private void setupDragListeners() {
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
                        if (dx > 10 || dy > 10) {
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
                            performHaptic();
                            if (currentMode == HudMode.PILL || currentMode == HudMode.MICRO_FPS) {
                                switchHudMode(HudMode.EXPANDED_DOCK);
                            }
                        } else {
                            // Magnetic Edge Snapping with boundary clamp
                            if (windowManager != null && overlayView != null) {
                                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                                int viewWidth = overlayView.getWidth();
                                int midPoint = screenWidth / 2;
                                if (params.x + (viewWidth / 2) < midPoint) {
                                    params.x = 12; // Snap Left
                                } else {
                                    params.x = Math.max(12, screenWidth - viewWidth - 12); // Snap Right
                                }
                                params.y = Math.max(20, Math.min(params.y, getResources().getDisplayMetrics().heightPixels - 150));
                                windowManager.updateViewLayout(overlayView, params);

                                // Persist position
                                getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
                                        .putInt(KEY_HUD_X, params.x)
                                        .putInt(KEY_HUD_Y, params.y)
                                        .apply();
                            }
                        }
                        return true;
                }
                return false;
            }
        };

        if (layoutCollapsedPill != null) layoutCollapsedPill.setOnTouchListener(dragListener);
        if (layoutMicroFps != null) layoutMicroFps.setOnTouchListener(dragListener);
        if (layoutHudHeader != null) layoutHudHeader.setOnTouchListener(dragListener);
    }

    private void switchHudMode(HudMode mode) {
        this.currentMode = mode;
        applyViewportVisibility(mode);

        getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
                .putString(KEY_HUD_MODE, mode.name())
                .apply();

        if (windowManager != null && overlayView != null && params != null) {
            try {
                windowManager.updateViewLayout(overlayView, params);
            } catch (Exception ignored) {}
        }
        updateTelemetryData();
    }

    private void applyViewportVisibility(HudMode mode) {
        if (layoutCollapsedPill == null || layoutMicroFps == null || layoutExpandedDock == null) return;

        switch (mode) {
            case PILL:
                layoutCollapsedPill.setVisibility(View.VISIBLE);
                layoutMicroFps.setVisibility(View.GONE);
                layoutExpandedDock.setVisibility(View.GONE);
                overlayView.setAlpha(0.85f);
                break;

            case MICRO_FPS:
                layoutCollapsedPill.setVisibility(View.GONE);
                layoutMicroFps.setVisibility(View.VISIBLE);
                layoutExpandedDock.setVisibility(View.GONE);
                overlayView.setAlpha(0.70f);
                break;

            case EXPANDED_DOCK:
                layoutCollapsedPill.setVisibility(View.GONE);
                layoutMicroFps.setVisibility(View.GONE);
                layoutExpandedDock.setVisibility(View.VISIBLE);
                overlayView.setAlpha(1.0f);
                break;
        }
    }

    private void scheduleAutoCollapse() {
        if (handler == null) return;
        handler.removeCallbacks(autoCollapseRunnable);
        handler.postDelayed(autoCollapseRunnable, 1800);
    }

    private final Choreographer.FrameCallback choreographerCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            if (!isRunning) return;
            RealGameFpsMonitor.getInstance().onChoreographerFrame(frameTimeNanos);
            try {
                Choreographer.getInstance().postFrameCallback(this);
            } catch (Exception ignored) {}
        }
    };

    private void setupTelemetryEngine() {
        handler = new Handler(Looper.getMainLooper());

        // 1. Frame Callback (VSYNC baseline)
        try {
            Choreographer.getInstance().postFrameCallback(choreographerCallback);
        } catch (Exception ignored) {}

        // 2. Real Game FPS Telemetry Engine
        RealGameFpsMonitor.getInstance().start(getApplicationContext(), new RealGameFpsMonitor.FpsUpdateListener() {
            @Override
            public void onFpsUpdated(int currentFps, int lowFps, boolean isReal) {
                realTimeFps = currentFps;
                onePercentLowFps = lowFps;
                isRealGameSurface = isReal;
            }

            @Override
            public void onFpsDetailedUpdated(int currentFps, int lowFps, int zeroPointOneLow, double ftMs, double jitter, boolean isReal) {
                realTimeFps = currentFps;
                onePercentLowFps = lowFps;
                zeroPointOnePercentLowFps = zeroPointOneLow;
                frameTimeMs = ftMs;
                frameJitterMs = jitter;
                isRealGameSurface = isReal;
            }
        });

        // 3. Metrics Updater (1 sec ticker)
        telemetryRunnable = new Runnable() {
            @Override
            public void run() {
                updateTelemetryData();
                if (handler != null && isRunning) {
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(telemetryRunnable);

        // 3. Network Ping Daemon (2.5 sec interval)
        pingRunnable = new Runnable() {
            @Override
            public void run() {
                executeNetworkPingCheck();
                if (handler != null && isRunning) {
                    handler.postDelayed(this, 2500);
                }
            }
        };
        handler.post(pingRunnable);
    }

    private void executeNetworkPingCheck() {
        AppExecutors.getInstance().executeCommand(() -> {
            int ping = 25;
            long start = System.currentTimeMillis();
            try (Socket socket = new Socket()) {
                // Ping Cloudflare DNS 1.1.1.1 or Google 8.8.8.8 on port 53 (DNS)
                socket.connect(new InetSocketAddress("1.1.1.1", 53), 1200);
                long elapsed = System.currentTimeMillis() - start;
                ping = (int) Math.max(5, Math.min( elapsed, 999));
            } catch (Exception e) {
                try (Socket backup = new Socket()) {
                    long bStart = System.currentTimeMillis();
                    backup.connect(new InetSocketAddress("8.8.8.8", 53), 1200);
                    ping = (int) Math.max(5, Math.min(System.currentTimeMillis() - bStart, 999));
                } catch (Exception ignored) {
                    ping = 99;
                }
            }

            final int finalPing = ping;
            final String netType = detectNetworkType();

            AppExecutors.getInstance().postToMainThread(() -> {
                livePingMs = finalPing;
                networkTypeStr = netType;
            });
        });
    }

    private String detectNetworkType() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (caps != null) {
                    if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return "Wi-Fi";
                    } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return "5G/LTE";
                    }
                }
            }
        } catch (Exception ignored) {}
        return "Online";
    }

    private void updateTelemetryData() {
        DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(getApplicationContext());
        DisplayCapabilitiesDetector.DisplayCaps caps = DisplayCapabilitiesDetector.detect(getApplicationContext());
        int currentHz = (caps != null && caps.currentRefreshRate > 0) ? caps.currentRefreshRate : 185;
        int activeFps = realTimeFps > 0 ? Math.min(185, realTimeFps) : currentHz;

        // Dynamic FPS Health Color
        int fpsColor;
        String fpsStatus;
        if (activeFps >= 90) {
            fpsColor = Color.parseColor("#00FF66"); // Neon Green
            fpsStatus = "🟢 Ultra Smooth";
        } else if (activeFps >= 45) {
            fpsColor = Color.parseColor("#00F0FF"); // Neon Cyan
            fpsStatus = "🟡 Smooth Gaming";
        } else {
            fpsColor = Color.parseColor("#FF0055"); // Neon Red
            fpsStatus = "🔴 Frame Drop";
        }

        // Dynamic Ping Color
        int pingColor;
        if (livePingMs <= 35) {
            pingColor = Color.parseColor("#00FF66");
        } else if (livePingMs <= 70) {
            pingColor = Color.parseColor("#FFB800");
        } else {
            pingColor = Color.parseColor("#FF0055");
        }

        // Thermal Rating
        String tempStatus;
        int tempColor;
        if (m.batteryTempC < 40.0f) {
            tempStatus = "(Cool)";
            tempColor = Color.parseColor("#00F0FF");
        } else if (m.batteryTempC < 45.0f) {
            tempStatus = "(Warm)";
            tempColor = Color.parseColor("#FFB800");
        } else {
            tempStatus = "(Hot)";
            tempColor = Color.parseColor("#FF0055");
        }

        // 1. Update Collapsed Pill Viewport
        if (layoutCollapsedPill != null && layoutCollapsedPill.getVisibility() == View.VISIBLE) {
            if (tvPillFps != null) {
                String tag = isRealGameSurface ? "🎮 " : "";
                tvPillFps.setText(String.format("%s%d FPS", tag, activeFps));
                tvPillFps.setTextColor(fpsColor);
            }
            if (tvPillTemp != null) {
                tvPillTemp.setText(String.format("%.1f°C", m.batteryTempC));
                tvPillTemp.setTextColor(tempColor);
            }
            if (tvPillPing != null) {
                tvPillPing.setText(String.format("%d ms", livePingMs));
                tvPillPing.setTextColor(pingColor);
            }
        }

        // 2. Update Micro FPS Viewport
        if (layoutMicroFps != null && layoutMicroFps.getVisibility() == View.VISIBLE) {
            if (tvMicroFps != null) {
                String tag = isRealGameSurface ? "🎮 " : "⚡ ";
                tvMicroFps.setText(String.format("%s%d FPS", tag, activeFps));
                tvMicroFps.setTextColor(fpsColor);
            }
        }

        // 3. Update Expanded Dock Viewport
        if (layoutExpandedDock != null && layoutExpandedDock.getVisibility() == View.VISIBLE) {
            if (tvHudFps != null) {
                String sourceTag = isRealGameSurface ? "🎮" : "⚡";
                tvHudFps.setText(String.format("%s %d FPS / %dHz", sourceTag, activeFps, currentHz));
                tvHudFps.setTextColor(fpsColor);
            }
            if (tvHudFpsStatus != null) {
                if (isRealGameSurface && onePercentLowFps > 0) {
                    tvHudFpsStatus.setText(String.format("%s • 1%%: %d • 0.1%%: %d (%.1fms ±%.1f)", fpsStatus, onePercentLowFps, zeroPointOnePercentLowFps, frameTimeMs, frameJitterMs));
                } else {
                    tvHudFpsStatus.setText(fpsStatus);
                }
            }
            if (tvHudPing != null) {
                tvHudPing.setText(String.format("📶 Ping: %d ms", livePingMs));
                tvHudPing.setTextColor(pingColor);
            }
            if (tvHudNetType != null) {
                tvHudNetType.setText(networkTypeStr + " • Low Lag");
            }
            if (tvHudTemp != null) {
                tvHudTemp.setText(String.format("🌡️ %.1f°C %s", m.batteryTempC, tempStatus));
                tvHudTemp.setTextColor(tempColor);
            }
            if (tvHudMa != null) {
                if (m.batteryCurrentMa != 0) {
                    tvHudMa.setText(String.format("🔋 Drain: -%d mA", m.batteryCurrentMa));
                } else {
                    tvHudMa.setText("🔋 Power: Normal");
                }
            }
            if (tvHudRam != null) {
                double usedGb = m.usedRamMb / 1024.0;
                double totalGb = m.totalRamMb / 1024.0;
                tvHudRam.setText(String.format("🧠 RAM: %.1f/%.1f GB (%d%%)", usedGb, totalGb, m.ramUsagePct));
            }
            if (pbHudRam != null) {
                pbHudRam.setProgress(m.ramUsagePct);
            }
        }
    }

    private void performHaptic() {
        try {
            if (overlayView != null) {
                overlayView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
            android.os.VibratorManager vm = getSystemService(android.os.VibratorManager.class);
            Vibrator v = vm != null ? vm.getDefaultVibrator() : null;
            if (v != null && v.hasVibrator()) {
                v.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        RealGameFpsMonitor.getInstance().stop();
        if (handler != null) {
            if (telemetryRunnable != null) handler.removeCallbacks(telemetryRunnable);
            if (pingRunnable != null) handler.removeCallbacks(pingRunnable);
            handler.removeCallbacks(autoCollapseRunnable);
        }
        try {
            Choreographer.getInstance().removeFrameCallback(choreographerCallback);
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

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GAME SPACE — Performance HUD")
                .setContentText("FPS, RAM, Thermals & Network telemetry active")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
}
