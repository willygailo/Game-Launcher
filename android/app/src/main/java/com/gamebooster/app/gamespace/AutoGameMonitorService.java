package com.gamebooster.app.gamespace;
import com.gamebooster.app.config.*;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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
    private Handler handler;
    private Runnable monitorRunnable;
    private String lastActiveGamePackage = null;

    public static boolean isRunning() {
        return isRunning;
    }

    public static void start(Context context) {
        if (context == null || isRunning) return;
        Intent intent = new Intent(context, AutoGameMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        if (context == null || !isRunning) return;
        Intent intent = new Intent(context, AutoGameMonitorService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        createNotificationChannel();
        startForeground(NOTIF_ID, createNotification());

        setupMonitorLoop();
    }

    private void setupMonitorLoop() {
        handler = new Handler(Looper.getMainLooper());
        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                checkForegroundApp();
                if (handler != null && isRunning) {
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

            boolean isGameActive = gamePackages.contains(currentPackage);

            if (isGameActive && !currentPackage.equals(lastActiveGamePackage)) {
                lastActiveGamePackage = currentPackage;
                GameSessionSettings.begin(getApplicationContext(), currentPackage);
                GameProfilePreferences.Profile profile = GameProfilePreferences.getProfile(
                        getApplicationContext(), currentPackage);
                int targetHz = GameProfilePreferences.getTargetHz(getApplicationContext(), currentPackage);
                Log.i(TAG, "GAME LAUNCH DETECTED: " + currentPackage + " — Applying "
                        + profile.label + " up to " + targetHz + "Hz");

                com.gamebooster.app.spoofer.DeviceSpooferEngine.applySpoofing(getApplicationContext(), currentPackage);
                com.gamebooster.app.engine.RefreshRateOverrideEngine.applyRefreshRate(getApplicationContext(), currentPackage,
                        targetHz >= 165 ? com.gamebooster.app.engine.RefreshRateOverrideEngine.RefreshRateMode.MODE_165HZ :
                        targetHz >= 144 ? com.gamebooster.app.engine.RefreshRateOverrideEngine.RefreshRateMode.MODE_144HZ :
                        targetHz >= 120 ? com.gamebooster.app.engine.RefreshRateOverrideEngine.RefreshRateMode.MODE_120HZ :
                        targetHz >= 90 ? com.gamebooster.app.engine.RefreshRateOverrideEngine.RefreshRateMode.MODE_90HZ :
                        com.gamebooster.app.engine.RefreshRateOverrideEngine.RefreshRateMode.MODE_60HZ);
                PerformanceChannel.applyProfile(getApplicationContext(), profile.performanceProfile);
                GameSpaceDndManager.setGamingDndMode(getApplicationContext(), profile.enableDnd);
                
                // Auto-Start Floating Gaming HUD
                if (!FloatingOverlayService.isOverlayRunning()) {
                    FloatingOverlayService.startOverlay(getApplicationContext());
                }

                AppExecutors.getInstance().postToMainThread(() ->
                        android.widget.Toast.makeText(getApplicationContext(), "🎮 " + profile.label
                                + " profile active (up to " + targetHz + "Hz)", android.widget.Toast.LENGTH_LONG).show());

            } else if (!isGameActive && lastActiveGamePackage != null) {
                Log.i(TAG, "Game exited — maintaining active performance settings (Zero Auto-Off & Background Home 165Hz Lock)");
                lastActiveGamePackage = null;
                GameSessionSettings.restore(getApplicationContext());
                com.gamebooster.app.spoofer.DeviceSpooferEngine.resetSpoofing();
                
                // Enforce Background Home 165Hz Refresh Rate & Performance state
                com.gamebooster.app.booster.HzFpsChannel.setRefreshRate(getApplicationContext(), 165);
                PerformanceChannel.applyProfile(getApplicationContext(), PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                
                AppExecutors.getInstance().postToMainThread(() ->
                        android.widget.Toast.makeText(getApplicationContext(), "⚡ Background Home Active — 165Hz & Performance Locked", android.widget.Toast.LENGTH_SHORT).show());
            } else if (!isGameActive && lastActiveGamePackage == null) {
                // Background Home continuous refresh rate check
                com.gamebooster.app.booster.HzFpsChannel.setRefreshRate(getApplicationContext(), 165);
            }
        });
    }

    private String getForegroundPackage() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                UsageEvents events = usm.queryEvents(time - 10000, time);
                UsageEvents.Event event = new UsageEvents.Event();
                String lastPkg = null;
                while (events.hasNextEvent()) {
                    events.getNextEvent(event);
                    if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        lastPkg = event.getPackageName();
                    }
                }
                return lastPkg;
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (handler != null && monitorRunnable != null) {
            handler.removeCallbacks(monitorRunnable);
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
                    "Auto Game Monitor Active",
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
                .setContentTitle("GAME SPACE — Auto Detection Active")
                .setContentText("Monitoring installed games for their selected performance profile...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }
}
