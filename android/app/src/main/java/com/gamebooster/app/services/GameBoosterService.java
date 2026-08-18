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
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
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
    public static final String ACTION_BOOST_GAME = "com.gamebooster.app.action.BOOST_GAME";
    public static final String EXTRA_PACKAGE_NAME = "extra_package_name";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        try {
            ShizukuUserServiceConnector.getInstance().bindService();
        } catch (Throwable ignored) {}
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
            showToast("🧹 RAM & Cache Purged — Memory Boosted");
        } else if (ACTION_TURBO_5G_WIFI.equals(action)) {
            turbo5gWifi();
            showToast("🚀 5G / 6G & Wi-Fi 6/7 Turbo Boost Active");
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
                if (!ShizukuManager.isShizukuRunningAndGranted()) {
                    Log.w(TAG, "Shizuku not active — skipping privileged Hz lock");
                    return;
                }
                ShizukuPermissionEnforcer.enforceAllPermissions(getApplicationContext());
                MaxHzForceChannel.forceApply(forcedHz);
                HzFpsChannel.forceSetRefreshRate(getApplicationContext(), forcedHz);
                PerformanceChannel.applyProfile(getApplicationContext(), PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                PerformanceChannel.writeAndExecuteRootTweaksScript(forcedHz);
                ShizukuUserServiceConnector.getInstance().forceDisplayRefreshRate(forcedHz);
                ShizukuUserServiceConnector.getInstance().setCpuGpuPerformanceGovernors();
                ShizukuUserServiceConnector.getInstance().applyThermalAndKernelBoost();
                Log.i(TAG, "Applied privileged " + forcedHz + "Hz lock via GameBoosterService");
            } catch (Exception e) {
                Log.w(TAG, "Error applying Hz lock", e);
            }
        });
    }

    private void boostSpecificGame(String packageName) {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                if (!ShizukuManager.isShizukuRunningAndGranted()) {
                    Log.w(TAG, "Shizuku not active — cannot privileged boost " + packageName);
                    return;
                }
                int targetHz = GameProfileAutoConfigurator.getTargetFpsHz(getApplicationContext());
                ShizukuUserServiceConnector.getInstance().enforceAppOpsAndPermissions(packageName);
                ShizukuUserServiceConnector.getInstance().setGameModeApi(packageName, targetHz);
                GameProfileAutoConfigurator.autoConfigGamePackage(getApplicationContext(), packageName, targetHz);
                Log.i(TAG, "Privileged boosted game: " + packageName);
            } catch (Exception e) {
                Log.w(TAG, "Error boosting game: " + packageName, e);
            }
        });
    }

    private void cleanMemory() {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                if (!ShizukuManager.isShizukuRunningAndGranted()) {
                    Log.w(TAG, "Shizuku not active — skipping memory drop");
                    return;
                }
                ShizukuUserServiceConnector.getInstance().trimCachesAndDropCaches();
                com.gamebooster.app.booster.RamZramChannel.trimMemoryAndCleanCache(getApplicationContext());
                com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommands(
                        "sync; echo 3 > /proc/sys/vm/drop_caches; am kill-all"
                );
            } catch (Exception e) {
                Log.w(TAG, "Error cleaning memory", e);
            }
        });
    }

    private void turbo5gWifi() {
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                if (!ShizukuManager.isShizukuRunningAndGranted()) {
                    Log.w(TAG, "Shizuku not active — skipping network turbo");
                    return;
                }
                ShizukuUserServiceConnector.getInstance().optimize5GAndWifi();
                com.gamebooster.app.booster.NetworkOptimizer.optimizeAllDataAndWifi(getApplicationContext());
            } catch (Exception e) {
                Log.w(TAG, "Error applying 5G/Wi-Fi turbo", e);
            }
        });
    }

    private void showToast(String msg) {
        AppExecutors.getInstance().postToMainThread(() ->
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
        );
    }

    private Notification buildNotification(int targetHz) {
        int flag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                : PendingIntent.FLAG_UPDATE_CURRENT;

        Intent lock185Intent = new Intent(this, GameBoosterService.class).setAction(ACTION_LOCK_185HZ);
        PendingIntent p185 = PendingIntent.getService(this, 1, lock185Intent, flag);

        Intent cleanRamIntent = new Intent(this, GameBoosterService.class).setAction(ACTION_CLEAN_RAM);
        PendingIntent pClean = PendingIntent.getService(this, 2, cleanRamIntent, flag);

        Intent turboNetIntent = new Intent(this, GameBoosterService.class).setAction(ACTION_TURBO_5G_WIFI);
        PendingIntent pTurboNet = PendingIntent.getService(this, 3, turboNetIntent, flag);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GAME SPACE — " + targetHz + " FPS/Hz Engine Active")
                .setContentText("Privileged Shizuku Engine Active • " + targetHz + "Hz Mode Lock")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .addAction(0, "⚡ 185Hz Lock", p185)
                .addAction(0, "🧹 Clean RAM", pClean)
                .addAction(0, "🚀 5G/Wi-Fi Turbo", pTurboNet)
                .build();
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
                    "Game Booster Active Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }
}
