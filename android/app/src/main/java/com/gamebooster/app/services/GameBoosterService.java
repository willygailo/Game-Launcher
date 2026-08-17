package com.gamebooster.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.PerformanceChannel;

public class GameBoosterService extends Service {

    private static final String CHANNEL_ID = "game_booster_channel";
    private static final int NOTIF_ID = 999;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int targetHz = com.gamebooster.app.config.GameProfileAutoConfigurator.getTargetFpsHz(this);
        if (targetHz <= 0) targetHz = 185;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GAME SPACE — " + targetHz + " FPS/Hz Auto Booster")
                .setContentText("Background Gaming Engine Active • " + targetHz + "Hz Mode Lock")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, notification);
        }

        // Apply background auto-boost optimizations and enforce privileged Shizuku permissions
        final int forcedHz = targetHz;
        try {
            com.gamebooster.app.shizuku.ShizukuPermissionEnforcer.enforceAllPermissions(getApplicationContext());
            com.gamebooster.app.booster.MaxHzForceChannel.forceApply(forcedHz);
            com.gamebooster.app.booster.HzFpsChannel.forceSetRefreshRate(getApplicationContext(), forcedHz);
            PerformanceChannel.applyProfile(getApplicationContext(), PerformanceChannel.Profile.EXTREME_PERFORMANCE);
            PerformanceChannel.writeAndExecuteRootTweaksScript(forcedHz);

            // Execute Chipset, OEM Bypass, and Version-Specific engines
            com.gamebooster.app.chipset.ChipsetOptimizerEngine.applyChipsetOptimization(getApplicationContext(), forcedHz);
            com.gamebooster.app.oem.OemBypassEngine.applyOemBypass(getApplicationContext(), forcedHz);
            com.gamebooster.app.version.AndroidVersionOptimizer.applyVersionOptimizations(getApplicationContext(), null, forcedHz);

            // Trigger background AOT speed compilation to eliminate JIT stutter
            com.gamebooster.app.dexopt.DexoptCompilationEngine.compileAllGamesSpeedAsync(getApplicationContext(), null);
        } catch (Exception ignored) {}

        return START_STICKY;
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
