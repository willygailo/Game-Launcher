package com.gamebooster.app.platform.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.feature.performance.booster.PerformanceChannel;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.feature.games.space.AutoGameMonitorService;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuManager;

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
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GAME SPACE — Performance Profile")
                .setContentText("Monitoring active • native display requests are capability-checked")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, notification);
        }

        // Register Shizuku Binder listeners
        try {
            ShizukuManager.registerBinderListeners();
        } catch (Exception ignored) {}

        // Apply background auto-boost optimizations and grant permissions
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                if (ShizukuExecutor.hasShizukuPermission()) {
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getApplicationContext());
                }
                int maxHz = com.gamebooster.app.feature.performance.display.DisplayOverrideController.highestSupportedRate(getApplicationContext());
                com.gamebooster.app.feature.performance.booster.HzFpsChannel.setRefreshRate(getApplicationContext(), maxHz);
                PerformanceChannel.applyProfile(getApplicationContext(), PerformanceChannel.Profile.EXTREME_PERFORMANCE);
            } catch (Exception ignored) {}
        });

        // Ensure AutoGameMonitorService is running
        if (!AutoGameMonitorService.isRunning()) {
            AutoGameMonitorService.start(getApplicationContext());
        }

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
