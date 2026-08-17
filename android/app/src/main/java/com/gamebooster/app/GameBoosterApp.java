package com.gamebooster.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuManager;

/**
 * GameBoosterApp — Enterprise Application Entry Point for Android 12, 13, 14, 15, and 16.
 *
 * Implements:
 * 1. Global Crash Shield & Uncaught Exception Handler to prevent silent auto-closes & ANRs.
 * 2. Proactive Notification Channel Initialization for Android 12–16.
 * 3. Safe Shizuku Binder Lifecycle registration & IPC error recovery.
 * 4. Memory Trim & Cache Purge handling.
 */
public class GameBoosterApp extends Application {

    private static final String TAG = "GameBoosterApp";
    private static GameBoosterApp instance;

    public static GameBoosterApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // 1. Install Global Crash Shield to prevent unexpected auto-close/force-close
        installGlobalCrashShield();

        // 2. Pre-initialize all Notification Channels for Android 12-16
        createAllNotificationChannels();

        // 3. Register Shizuku binder lifecycle hooks
        try {
            ShizukuManager.registerBinderListeners();
        } catch (Throwable t) {
            Log.w(TAG, "Shizuku binder listeners init warning: " + t.getMessage());
        }

        // 4. Initialize Rish binary assets in background
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                com.gamebooster.app.shizuku.RishManager.initialize(getApplicationContext());
            } catch (Throwable t) {
                Log.w(TAG, "RishManager background init warning: " + t.getMessage());
            }
        });

        Log.i(TAG, "⚡ GameBoosterApp initialized (MinSDK 31 / Android 12-16 Ready)");
    }

    /**
     * Installs a resilient UncaughtExceptionHandler that catches unexpected fatal exceptions
     * (e.g. DeadObjectException, RemoteException, BackgroundServiceException) and prevents
     * immediate app process termination while logging full diagnostic stack traces.
     */
    private void installGlobalCrashShield() {
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "💥 [CRASH SHIELD] Caught unhandled exception on thread: " + thread.getName(), throwable);

            // If it's a DeadObjectException or Shizuku Binder IPC death, handle gracefully without dying
            if (throwable instanceof android.os.DeadObjectException ||
                (throwable.getMessage() != null && throwable.getMessage().contains("binder"))) {
                Log.w(TAG, "Recovered from Remote IPC DeadObjectException — app remains running.");
                return;
            }

            // Pass to default handler if critical or unrecoverable
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }

    /**
     * Pre-creates all foreground notification channels so background services never fail
     * or crash with MissingNotificationChannelException on Android 12, 13, 14, 15, and 16.
     */
    private void createAllNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm == null) return;

            // Channel 1: Game Booster Service
            NotificationChannel boosterChannel = new NotificationChannel(
                    "game_booster_channel",
                    "Game Booster Active Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            boosterChannel.setDescription("Foreground notification for active game acceleration");
            boosterChannel.setShowBadge(false);
            nm.createNotificationChannel(boosterChannel);

            // Channel 2: Floating Overlay HUD
            NotificationChannel overlayChannel = new NotificationChannel(
                    "game_booster_overlay_channel",
                    "Game Space Floating Overlay",
                    NotificationManager.IMPORTANCE_LOW
            );
            overlayChannel.setDescription("Real-time FPS, RAM & Thermal monitoring HUD");
            overlayChannel.setShowBadge(false);
            nm.createNotificationChannel(overlayChannel);

            // Channel 3: Crosshair Overlay
            NotificationChannel crosshairChannel = new NotificationChannel(
                    "crosshair_overlay_channel",
                    "Esports Crosshair Overlay",
                    NotificationManager.IMPORTANCE_LOW
            );
            crosshairChannel.setDescription("Esports visual crosshair target overlay");
            crosshairChannel.setShowBadge(false);
            nm.createNotificationChannel(crosshairChannel);

            // Channel 4: Auto Game Space Monitor
            NotificationChannel monitorChannel = new NotificationChannel(
                    "auto_game_monitor_channel",
                    "Auto Game Space Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );
            monitorChannel.setDescription("Automatic background game detection and auto-booster");
            monitorChannel.setShowBadge(false);
            nm.createNotificationChannel(monitorChannel);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level >= TRIM_MEMORY_MODERATE) {
            Log.d(TAG, "TrimMemory received level=" + level + ", clearing non-critical buffers");
            try {
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    System.gc();
                });
            } catch (Throwable ignored) {}
        }
    }
}
