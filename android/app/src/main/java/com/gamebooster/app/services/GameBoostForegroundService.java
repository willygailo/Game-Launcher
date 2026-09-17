package com.gamebooster.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.BackgroundLimitImmunityEngine;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuConnectionManager;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.ui.activities.MainActivity;

import rikka.shizuku.Shizuku;

/**
 * GameBoostForegroundService — Persistent Game Guardian & Shizuku Keep-Alive Service.
 *
 * Keeps Game Booster in an un-killable, non-cached foreground state with:
 * 1. WakeLock (PARTIAL_WAKE_LOCK) preventing Android CPU sleep during heavy gaming.
 * 2. 20-Second Heartbeat Keep-Alive Loop that pings Shizuku Binder, rebinds AIDL UserService,
 *    and refreshes OOM priority (-900) so LMKD never kills the process after hours of gaming.
 * 3. Proactive resurrection of Shizuku daemon via root/shell if killed.
 */
public class GameBoostForegroundService extends Service {

    private static final String TAG = "GameBoostFgService";
    public static final String CHANNEL_ID = "game_boost_foreground_channel";
    public static final int NOTIFICATION_ID = 9021;

    public static final String ACTION_START = "com.gamebooster.app.action.START_BOOST";
    public static final String ACTION_STOP = "com.gamebooster.app.action.STOP_BOOST";
    public static final String EXTRA_PACKAGE_NAME = "extra_target_package";
    public static final String EXTRA_TARGET_FPS = "extra_target_fps";

    private static final long HEARTBEAT_INTERVAL_MS = 20_000L; // 20-second watchdog
    private static volatile boolean sIsRunning = false;

    private String targetPackage = null;
    private int targetFps = 185;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    private HandlerThread heartbeatThread;
    private Handler heartbeatHandler;
    private Runnable heartbeatRunnable;

    public static boolean isRunning() {
        return sIsRunning;
    }

    public static void start(Context context, String packageName, int targetFps) {
        if (context == null) return;
        try {
            Intent intent = new Intent(context, GameBoostForegroundService.class);
            intent.setAction(ACTION_START);
            intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
            intent.putExtra(EXTRA_TARGET_FPS, targetFps);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to start GameBoostForegroundService: " + t.getMessage());
        }
    }

    public static void stop(Context context) {
        if (context == null) return;
        try {
            Intent intent = new Intent(context, GameBoostForegroundService.class);
            intent.setAction(ACTION_STOP);
            context.startService(intent);
        } catch (Throwable t) {
            Log.w(TAG, "Failed to stop GameBoostForegroundService: " + t.getMessage());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sIsRunning = true;
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        createNotificationChannel();
        acquireWakeLock();
        startHeartbeatLoop();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_STOP.equals(action)) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            if (intent.hasExtra(EXTRA_PACKAGE_NAME)) {
                targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME);
            }
            if (intent.hasExtra(EXTRA_TARGET_FPS)) {
                targetFps = intent.getIntExtra(EXTRA_TARGET_FPS, targetFps);
            }
        }

        Notification notification = buildNotification(targetPackage, targetFps, ShizukuManager.isShizukuRunningAndGranted());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        // Immediately enforce privileged background immunity upon startup
        BackgroundLimitImmunityEngine.enforceImmunity(getApplicationContext());

        Log.i(TAG, "🛡️ GameBoostForegroundService (Game Guardian) active for pkg=" + targetPackage + " @ " + targetFps + " FPS");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sIsRunning = false;
        stopHeartbeatLoop();
        releaseWakeLock();
        Log.i(TAG, "GameBoostForegroundService destroyed.");
    }

    private void acquireWakeLock() {
        try {
            if (wakeLock == null && powerManager != null) {
                wakeLock = powerManager.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        "GameBooster:GameGuardianWakeLock"
                );
                if (wakeLock != null) {
                    wakeLock.setReferenceCounted(false);
                    wakeLock.acquire(8 * 60 * 60 * 1000L); // 8 hours max safe ceiling
                    Log.i(TAG, "⚡ Acquired Partial WakeLock (8h ceiling) for zero-sleep gaming background guarantee.");
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to acquire WakeLock: " + t.getMessage());
        }
    }

    private void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
                Log.i(TAG, "Released Partial WakeLock.");
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error releasing WakeLock: " + t.getMessage());
        }
    }

    private void startHeartbeatLoop() {
        heartbeatThread = new HandlerThread("GameBoosterHeartbeatThread", Process.THREAD_PRIORITY_BACKGROUND);
        heartbeatThread.start();
        heartbeatHandler = new Handler(heartbeatThread.getLooper());

        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                if (!sIsRunning) return;
                performKeepAlivePulse();
                if (heartbeatHandler != null) {
                    heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
                }
            }
        };

        heartbeatHandler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS);
        Log.i(TAG, "Started 20s Shizuku Keep-Alive Heartbeat watchdog.");
    }

    private void stopHeartbeatLoop() {
        if (heartbeatHandler != null && heartbeatRunnable != null) {
            heartbeatHandler.removeCallbacks(heartbeatRunnable);
            heartbeatRunnable = null;
        }
        if (heartbeatThread != null) {
            heartbeatThread.quitSafely();
            heartbeatThread = null;
        }
        heartbeatHandler = null;
    }

    private void performKeepAlivePulse() {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                boolean binderAlive = false;
                try {
                    binderAlive = Shizuku.pingBinder();
                } catch (Throwable ignored) {}

                if (binderAlive) {
                    if (!ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                        Log.w(TAG, "Heartbeat: Shizuku binder alive but UserService disconnected! Proactively rebinding...");
                        ShizukuUserServiceConnector.getInstance().bindService();
                    }
                } else {
                    Log.e(TAG, "Heartbeat: Shizuku binder DEAD during gaming session! Resurrecting daemon...");
                    BackgroundLimitImmunityEngine.tryAutoResurrectShizukuDaemon();
                    ShizukuConnectionManager.getInstance().forceReconnectCheck();
                }

                // Re-pin process OOM score adj to -900 to beat any Android LMKD background sweeps
                BackgroundLimitImmunityEngine.protectProcessOomScore(BackgroundLimitImmunityEngine.GAME_BOOSTER_PACKAGE);

                // Refresh notification status silently
                updateNotification();
            } catch (Throwable t) {
                Log.w(TAG, "Heartbeat keep-alive pulse exception: " + t.getMessage());
            }
        });
    }

    private void updateNotification() {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                Notification notif = buildNotification(targetPackage, targetFps, ShizukuManager.isShizukuRunningAndGranted());
                nm.notify(NOTIFICATION_ID, notif);
            }
        } catch (Throwable ignored) {}
    }

    private Notification buildNotification(String pkg, int fps, boolean shizukuActive) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        String title = "🛡️ Game Guardian Active • " + fps + " FPS Mode";
        String statusText = (pkg != null && !pkg.isEmpty() ? pkg : "Game Session") + " • Shizuku: " + (shizukuActive ? "Connected 🟢" : "Disconnected 🔴");

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(statusText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Game Booster Active Shield",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Maintains background immunity and keeps Shizuku connection alive during gameplay.");
            channel.setShowBadge(false);
            channel.enableVibration(false);
            channel.setSound(null, null);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
