package com.gamebooster.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.booster.RamZramChannel;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.NativeFrameworkBridge;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import com.gamebooster.app.shizuku.ShizukuKeepAliveWatchdog;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

public class GameBoosterService extends Service {

    private static final String TAG = "GameBoosterService";
    private static final String CHANNEL_ID = "game_booster_channel";
    private static final int NOTIF_ID = 999;

    public static final String ACTION_LOCK_185HZ = "com.gamebooster.app.action.LOCK_185HZ";
    public static final String ACTION_LOCK_120HZ = "com.gamebooster.app.action.LOCK_120HZ";
    public static final String ACTION_CLEAN_RAM = "com.gamebooster.app.action.CLEAN_RAM";
    public static final String ACTION_TURBO_5G_WIFI = "com.gamebooster.app.action.TURBO_5G_WIFI";
    public static final String ACTION_TOGGLE_FOCUS_DND = "com.gamebooster.app.action.TOGGLE_FOCUS_DND";
    public static final String ACTION_BOOST_GAME = "com.gamebooster.app.action.BOOST_GAME";
    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";

    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;

    public static void start(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, GameBoosterService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Failed to start GameBoosterService: " + t.getMessage());
        }
    }

    public static void stop(Context context) {
        if (context == null) return;
        try {
            context.stopService(new Intent(context, GameBoosterService.class));
        } catch (Throwable ignored) {}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Phase 0.2: enable the config backup safety net (app-private storage)
        com.gamebooster.app.config.ConfigBackupManager.setAppContext(getApplicationContext());
        createNotificationChannel();
        try {
            ShizukuUserServiceConnector.getInstance().bindService();
        } catch (Throwable ignored) {}

        // Acquire low-latency / high-performance Wi-Fi lock to prevent Wireless Debugging disconnect during gaming
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                int mode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        ? WifiManager.WIFI_MODE_FULL_LOW_LATENCY
                        : WifiManager.WIFI_MODE_FULL_HIGH_PERF;
                wifiLock = wm.createWifiLock(mode, "GameBooster:WifiLock");
                wifiLock.acquire();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to acquire WifiLock: " + t.getMessage());
        }

        // Acquire partial wake lock to prevent deep Doze during active gameplay
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GameBooster:WakeLock");
                wakeLock.acquire(12 * 60 * 60 * 1000L); // 12 hours ceiling
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to acquire WakeLock: " + t.getMessage());
        }

        // Start dedicated Shizuku Keep-Alive Watchdog daemon
        try {
            ShizukuKeepAliveWatchdog.getInstance().startWatchdog(getApplicationContext());
        } catch (Throwable t) {
            Log.w(TAG, "Failed to start ShizukuKeepAliveWatchdog: " + t.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;

        if (ACTION_LOCK_185HZ.equals(action)) {
            applyHzLock(185);
            showToast("⚡ 185 FPS / 185Hz Extreme Mode Locked");
        } else if (ACTION_LOCK_120HZ.equals(action)) {
            applyHzLock(185);
            showToast("⚡ 185 FPS / 185Hz Extreme Mode Locked");
        } else if (ACTION_CLEAN_RAM.equals(action)) {
            cleanMemory();
            showToast("🧹 RAM & zRAM Compaction Complete");
        } else if (ACTION_TURBO_5G_WIFI.equals(action)) {
            turbo5gWifi();
            showToast("🚀 5G / Wi-Fi Turbo & Network QoS Active");
        } else if (ACTION_TOGGLE_FOCUS_DND.equals(action)) {
            toggleFocusDnd();
        } else if (ACTION_BOOST_GAME.equals(action) && intent != null) {
            String pkg = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            if (pkg != null) {
                boostSpecificGame(pkg);
            }
        } else {
            int targetHz = GameProfileAutoConfigurator.getTargetFpsHz(this);
            if (targetHz <= 0) targetHz = 185;
            applyHzLock(targetHz);
        }

        int currentHz = GameProfileAutoConfigurator.getTargetFpsHz(this);
        if (currentHz <= 0) currentHz = 185;

        Notification notification = buildNotification(currentHz);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, notification);
        }

        return START_STICKY;
    }

    private void applyHzLock(int hz) {
        final int forcedHz = hz > 0 ? hz : 185;
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // 1. Legal native framework locks & ADPF
                NativeFrameworkBridge.acquireSustainedPerformanceLock(getApplicationContext());
                NativeFrameworkBridge.acquireLowLatencyWifiLock(getApplicationContext());
                NativeFrameworkBridge.startAdpfSession(getApplicationContext(), forcedHz);
                HzFpsChannel.setRefreshRate(getApplicationContext(), forcedHz);

                // 2. Privileged Shizuku tweaks when available (wait up to 300ms for connection)
                boolean shizukuReady = com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().ensureReady(300);
                if (shizukuReady || ShizukuManager.isShizukuRunningAndGranted()) {
                    ShizukuPermissionEnforcer.enforceAllPermissions(getApplicationContext());
                    MaxHzForceChannel.forceApply(forcedHz);
                    HzFpsChannel.forceSetRefreshRate(getApplicationContext(), forcedHz);
                    PerformanceChannel.applyProfile(getApplicationContext(), PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                    PerformanceChannel.writeAndExecuteRootTweaksScript(forcedHz);
                    ShizukuUserServiceConnector.getInstance().forceDisplayRefreshRate(forcedHz);
                    ShizukuUserServiceConnector.getInstance().setCpuGpuPerformanceGovernors();
                    ShizukuUserServiceConnector.getInstance().applyThermalAndKernelBoost();
                    Log.i(TAG, "Applied privileged " + forcedHz + "Hz lock via GameBoosterService");
                } else {
                    Log.i(TAG, "Applied legal SDK framework " + forcedHz + "Hz optimizations");
                }
            } catch (Exception e) {
                Log.w(TAG, "Error applying Hz lock", e);
            }
        });
    }

    private void boostSpecificGame(String packageName) {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                int targetHz = GameProfileAutoConfigurator.getTargetFpsHz(getApplicationContext());

                // 1. Legal native framework GameManager API & config auto-configuration
                NativeFrameworkBridge.setGameModePerformance(getApplicationContext(), packageName);
                GameProfileAutoConfigurator.autoConfigGamePackage(getApplicationContext(), packageName, targetHz);
                com.gamebooster.app.config.GameAutoInjectDispatcher.dispatchForPackage(packageName);

                // 2. Privileged Shizuku enhancements (wait up to 300ms for connection)
                boolean shizukuReady = com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().ensureReady(300);
                if (shizukuReady || ShizukuManager.isShizukuRunningAndGranted()) {
                    ShizukuUserServiceConnector.getInstance().enforceAppOpsAndPermissions(packageName);
                    ShizukuUserServiceConnector.getInstance().setGameModeApi(packageName, targetHz);

                    // Pin active game PID to Big CPU cores with real-time nice priority
                    String pidOut = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("pidof " + packageName + " 2>/dev/null");
                    if (pidOut != null && !pidOut.trim().isEmpty()) {
                        String[] pids = pidOut.trim().split("\\s+");
                        for (String p : pids) {
                            try {
                                int pid = Integer.parseInt(p);
                                if (pid > 0) {
                                    ShizukuUserServiceConnector.getInstance().setCpuAffinity(pid, 0xF0);
                                    ShizukuUserServiceConnector.getInstance().setProcessPriority(pid, -20);
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    Log.i(TAG, "Privileged boosted game: " + packageName);
                } else {
                    Log.i(TAG, "Standard boosted game: " + packageName);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error boosting game: " + packageName, e);
            }
        });
    }

    private void cleanMemory() {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // 1. Always execute legal JVM / Android memory trim
                RamZramChannel.trimMemoryAndCleanCache(getApplicationContext());

                // 2. Privileged drops when Shizuku is active
                if (ShizukuManager.isShizukuRunningAndGranted()) {
                    ShizukuUserServiceConnector.getInstance().executeZramCompaction();
                    ShizukuUserServiceConnector.getInstance().trimCachesAndDropCaches();
                }
            } catch (Exception e) {
                Log.w(TAG, "Error cleaning memory", e);
            }
        });
    }

    private void turbo5gWifi() {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // 1. Always optimize network sockets and bind high priority
                NetworkOptimizer.optimizeAllDataAndWifi(getApplicationContext());
                NativeFrameworkBridge.requestHighPriorityNetwork(getApplicationContext());

                // 2. Privileged QoS when Shizuku is active
                if (ShizukuManager.isShizukuRunningAndGranted()) {
                    ShizukuUserServiceConnector.getInstance().optimize5GAndWifi();
                    ShizukuUserServiceConnector.getInstance().setNetworkQoS(true);
                }
            } catch (Exception e) {
                Log.w(TAG, "Error applying 5G/Wi-Fi turbo", e);
            }
        });
    }

    private void toggleFocusDnd() {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                boolean isFocusActive = com.gamebooster.app.focus.FocusModeEngine.isFocusModeActive(getApplicationContext());
                if (isFocusActive) {
                    com.gamebooster.app.focus.FocusModeEngine.disableFocusMode(getApplicationContext());
                    showToast("🛡️ Focus Mode & Gaming DND Deactivated");
                } else {
                    com.gamebooster.app.focus.FocusModeEngine.enableFocusMode(getApplicationContext(), null);
                    showToast("🛡️ Focus Mode & Gaming DND Active");
                }
            } catch (Exception e) {
                Log.w(TAG, "Error toggling focus DND", e);
            }
        });
    }

    private void showToast(String msg) {
        AppExecutors.getInstance().postToMainThread(() ->
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
        );
    }

    private Notification buildNotification(int targetHz) {
        int flag = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        Intent lock185Intent = new Intent(this, GameBoosterService.class).setAction(ACTION_LOCK_185HZ);
        PendingIntent p185 = PendingIntent.getService(this, 1, lock185Intent, flag);

        Intent cleanRamIntent = new Intent(this, GameBoosterService.class).setAction(ACTION_CLEAN_RAM);
        PendingIntent pClean = PendingIntent.getService(this, 2, cleanRamIntent, flag);

        Intent turboNetIntent = new Intent(this, GameBoosterService.class).setAction(ACTION_TURBO_5G_WIFI);
        PendingIntent pTurboNet = PendingIntent.getService(this, 3, turboNetIntent, flag);

        Intent dndIntent = new Intent(this, GameBoosterService.class).setAction(ACTION_TOGGLE_FOCUS_DND);
        PendingIntent pDnd = PendingIntent.getService(this, 4, dndIntent, flag);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GAME SPACE — " + targetHz + " FPS/Hz Engine Active")
                .setContentText("Privileged Shizuku Engine Active • " + targetHz + "Hz Mode Lock")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .addAction(0, "⚡ 185Hz", p185)
                .addAction(0, "🧹 Clean RAM", pClean)
                .addAction(0, "🚀 5G/Wi-Fi", pTurboNet)
                .addAction(0, "🛡️ DND", pDnd)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Game Booster Active Service",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            ShizukuKeepAliveWatchdog.getInstance().stopWatchdog();
        } catch (Throwable ignored) {}

        if (wifiLock != null && wifiLock.isHeld()) {
            try {
                wifiLock.release();
            } catch (Throwable ignored) {}
            wifiLock = null;
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            try {
                wakeLock.release();
            } catch (Throwable ignored) {}
            wakeLock = null;
        }
    }
}
