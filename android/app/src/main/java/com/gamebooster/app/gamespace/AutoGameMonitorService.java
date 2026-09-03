package com.gamebooster.app.gamespace;
import com.gamebooster.app.config.*;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameManagerRepository;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.GameSessionSettings;
import com.gamebooster.app.overlay.FloatingOverlayService;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AutoGameMonitorService extends Service {

    private static final String TAG = "AutoGameMonitor";
    private static final String CHANNEL_ID = "auto_game_monitor_channel";
    private static final int NOTIF_ID = 777;

    private static boolean isRunning = false;
    private HandlerThread monitorThread;
    private Handler handler;
    private Runnable monitorRunnable;
    private String lastActiveGamePackage = null;
    /** Timestamp of the last game session end. Used to debounce rapid back+return cycles. */
    private long lastSessionEndTimeMs = 0;
    private static final long SESSION_RESTART_COOLDOWN_MS = 5_000; // 5 seconds

    private int consecutiveUnfocusedCount = 0;

    public static boolean isRunning() {
        return isRunning;
    }

    public static void start(Context context) {
        if (context == null || isRunning) return;
        Intent intent = new Intent(context, AutoGameMonitorService.class);
        context.startForegroundService(intent);
    }

    public static void stop(Context context) {
        if (context == null || !isRunning) return;
        Intent intent = new Intent(context, AutoGameMonitorService.class);
        context.stopService(intent);
    }

    private boolean isScreenOn = true;
    private android.content.BroadcastReceiver screenReceiver;

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

        registerScreenReceiver();
        setupMonitorLoop();
    }

    private void registerScreenReceiver() {
        screenReceiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || intent.getAction() == null) return;
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                    isScreenOn = false;
                    if (handler != null && monitorRunnable != null) {
                        handler.removeCallbacks(monitorRunnable);
                    }
                    Log.d(TAG, "Screen OFF — Paused game monitoring loop to save battery");
                } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    isScreenOn = true;
                    if (handler != null && monitorRunnable != null && isRunning) {
                        handler.removeCallbacks(monitorRunnable);
                        handler.post(monitorRunnable);
                    }
                    Log.d(TAG, "Screen ON — Resumed game monitoring loop");
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenReceiver, filter);
    }

    private void setupMonitorLoop() {
        monitorThread = new HandlerThread("AutoGameMonitor", Process.THREAD_PRIORITY_BACKGROUND);
        monitorThread.start();
        handler = new Handler(monitorThread.getLooper());
        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isScreenOn) return;
                checkForegroundApp();
                if (handler != null && isRunning && isScreenOn) {
                    handler.postDelayed(this, 2500); // Check every 2.5s
                }
            }
        };
        handler.post(monitorRunnable);
    }

    private void checkForegroundApp() {
        AppExecutors.getInstance().executeCommand(() -> {
            String currentPackage = getForegroundPackage();
            if (currentPackage == null) return;

            List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(getApplicationContext());
            Set<String> gamePackages = new TreeSet<>();
            for (GameAppInfo info : installedGames) {
                gamePackages.add(info.getPackageName());
            }

            boolean isGameActive = gamePackages.contains(currentPackage)
                    || com.gamebooster.app.games.GamePackageRegistry.isKnownGame(currentPackage);

            if (isGameActive) {
                consecutiveUnfocusedCount = 0;
                if (!currentPackage.equals(lastActiveGamePackage)) {
                    long now = System.currentTimeMillis();
                    // Debounce: if the game was just exited < 5s ago (e.g. brief back press),
                    // don't re-trigger a full beginSession — let the game resume naturally.
                    if (now - lastSessionEndTimeMs < SESSION_RESTART_COOLDOWN_MS) {
                        Log.d(TAG, "Session cooldown active — skipping re-trigger for: " + currentPackage);
                        lastActiveGamePackage = currentPackage; // still update so we track it
                        return;
                    }
                    lastActiveGamePackage = currentPackage;
                    Log.i(TAG, "GAME LAUNCH DETECTED: " + currentPackage + " — Starting GameManager Session");

                    // Execute full GameManager Session Engine
                    com.gamebooster.app.gamemanager.GameManagerSessionEngine.beginSession(getApplicationContext(), currentPackage);

                    // Auto-Start Floating Gaming HUD & Bind Real FPS Target
                    com.gamebooster.app.overlay.RealGameFpsMonitor.getInstance().setTargetPackage(currentPackage);
                    if (!FloatingOverlayService.isOverlayRunning()) {
                        FloatingOverlayService.startOverlay(getApplicationContext());
                    }

                    AppExecutors.getInstance().postToMainThread(() ->
                            android.widget.Toast.makeText(getApplicationContext(), "🎮 GAME-MANAGER: " + currentPackage
                                    + " is Boosted & Optimized!", android.widget.Toast.LENGTH_LONG).show());
                }
            } else if (lastActiveGamePackage != null) {
                // Ignore transient system packages (in-game overlays, keyboards, Google Play login, dialogs, webviews)
                if (isTransientPackage(currentPackage)) {
                    return;
                }

                // Debounce exit: verify game process liveness before resetting refresh rates & governors
                consecutiveUnfocusedCount++;
                if (consecutiveUnfocusedCount < 4 || isProcessAlive(lastActiveGamePackage)) {
                    Log.d(TAG, "Game PID/Process still active or debouncing (" + consecutiveUnfocusedCount + "/4) for: " + lastActiveGamePackage);
                    return;
                }

                Log.i(TAG, "Game confirmed exited — ending GameManager Session for: " + lastActiveGamePackage);
                String exitingPkg = lastActiveGamePackage;
                lastActiveGamePackage = null;
                consecutiveUnfocusedCount = 0;
                lastSessionEndTimeMs = System.currentTimeMillis(); // record exit time for cooldown
                com.gamebooster.app.overlay.RealGameFpsMonitor.getInstance().setTargetPackage(null);

                // End GameManager Session and revert to baseline
                com.gamebooster.app.gamemanager.GameManagerSessionEngine.endSession(getApplicationContext(), exitingPkg);
                com.gamebooster.app.overlay.GameTurboEdgeService.stop(getApplicationContext());
                com.gamebooster.app.overlay.VisualFilterOverlayService.stopFilter(getApplicationContext());
                com.gamebooster.app.engine.ResolutionScalerEngine.resetResolutionSync();

                final com.gamebooster.app.overlay.GameSessionReport report =
                        com.gamebooster.app.overlay.GameSessionRecorder.getInstance().endSession(getApplicationContext());

                AppExecutors.getInstance().postToMainThread(() -> {
                    android.widget.Toast.makeText(getApplicationContext(),
                            "↩ Stock Baseline Restored",
                            android.widget.Toast.LENGTH_SHORT).show();

                    if (report != null && report.getPlaytimeSeconds() >= 5) {
                        try {
                            Intent reportIntent = new Intent(getApplicationContext(), com.gamebooster.app.ui.activities.MainActivity.class);
                            reportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            reportIntent.putExtra("EXTRA_SHOW_POST_GAME_REPORT", report);
                            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                                    getApplicationContext(),
                                    2002,
                                    reportIntent,
                                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
                            );

                            NotificationManager nm = getSystemService(NotificationManager.class);
                            if (nm != null) {
                                Notification notif = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                                        .setContentTitle("🎮 Game Session Complete (" + (report.gameTitle != null ? report.gameTitle : exitingPkg) + ")")
                                        .setContentText("Avg FPS: " + report.averageFps + " | Tap to view session stats")
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentIntent(pendingIntent)
                                        .setAutoCancel(true)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                        .build();
                                nm.notify(2002, notif);
                            }
                        } catch (Exception ignored) {}
                    }
                });
            }
        });
    }

    private boolean isTransientPackage(String pkg) {
        if (pkg == null || pkg.isEmpty()) return true;
        // Game packages are NEVER transient!
        if (com.gamebooster.app.games.GamePackageRegistry.isKnownGame(pkg)) {
            return false;
        }
        if (pkg.equals("com.android.systemui")
                || pkg.equals("com.google.android.gms")
                || pkg.equals("com.google.android.play.games")
                || pkg.equals("com.android.vending")
                || pkg.equals("android")
                || pkg.equals(getPackageName())) {
            return true;
        }
        String lower = pkg.toLowerCase(java.util.Locale.US);
        // Exclude UI dialogues, keyboards, and overlays (do NOT exclude game publishers like tencent, garena, or hoyoverse)
        return lower.contains("inputmethod")
                || lower.contains("permissioncontroller")
                || lower.contains("dialog")
                || lower.contains("overlay")
                || lower.contains("keyboard")
                || lower.contains("admob")
                || lower.contains("systemui");
    }

    private boolean isProcessAlive(String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;
        try {
            if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
                String pid = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("pidof " + packageName);
                if (pid != null && !pid.trim().isEmpty() && !pid.startsWith("ERROR")) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                java.util.List<android.app.ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
                if (procs != null) {
                    for (android.app.ActivityManager.RunningAppProcessInfo info : procs) {
                        if (packageName.equals(info.processName)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private String getForegroundPackage() {
        return com.gamebooster.app.games.ForegroundGameDetector.detectForegroundPackage(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (screenReceiver != null) {
            try {
                unregisterReceiver(screenReceiver);
            } catch (Throwable ignored) {}
            screenReceiver = null;
        }
        if (handler != null && monitorRunnable != null) {
            handler.removeCallbacks(monitorRunnable);
        }
        if (monitorThread != null) {
            monitorThread.quitSafely();
            monitorThread = null;
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
                "Auto Game Monitor Active",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GAME SPACE — Auto Detection Active")
                .setContentText("Monitoring installed games for their selected performance profile...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
}
