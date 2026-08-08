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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, createNotification());
        }

        setupMonitorLoop();
    }

    private boolean isChecking = false;

    private void setupMonitorLoop() {
        handler = new Handler(Looper.getMainLooper());
        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                checkForegroundApp();
                if (handler != null && isRunning) {
                    // Adaptive battery-saving interval: 2s during active game session, 3.5s when idle
                    long delay = (lastActiveGamePackage != null) ? 2000L : 3500L;
                    handler.postDelayed(this, delay);
                }
            }
        };
        handler.post(monitorRunnable);
    }

    private void checkForegroundApp() {
        if (isChecking) return;
        isChecking = true;
        AppExecutors.getInstance().executeMonitor(() -> {
            try {
                String currentPackage = getForegroundPackage();
                if (currentPackage == null || currentPackage.isEmpty()) return;

                List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(getApplicationContext());
                Set<String> gamePackages = new TreeSet<>();
                for (GameAppInfo info : installedGames) {
                    gamePackages.add(info.getPackageName());
                }

                boolean isGameActive = gamePackages.contains(currentPackage) 
                        || com.gamebooster.app.games.GamePackageRegistry.isKnownGame(currentPackage);

                if (isGameActive && !currentPackage.equals(lastActiveGamePackage)) {
                    lastActiveGamePackage = currentPackage;
                    GameSessionSettings.begin(getApplicationContext(), currentPackage);
                    GameProfilePreferences.Profile profile = GameProfilePreferences.getProfile(
                            getApplicationContext(), currentPackage);
                    int targetHz = GameProfilePreferences.getTargetHz(getApplicationContext(), currentPackage);
                    Log.i(TAG, "GAME LAUNCH DETECTED (AUTOMATIC): " + currentPackage + " — Applying "
                            + profile.label + " up to " + targetHz + "Hz");

                    com.gamebooster.app.spoofer.DeviceSpooferEngine.applySpoofing(getApplicationContext(), currentPackage);
                    com.gamebooster.app.booster.EsportsNetworkTuner.applyLowLatencyNetworkSettings(getApplicationContext());
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
                            android.widget.Toast.makeText(getApplicationContext(), "🎮 AUTO BOOST: " + profile.label
                                    + " profile active (up to " + targetHz + "Hz)", android.widget.Toast.LENGTH_LONG).show());

                } else if (!isGameActive && lastActiveGamePackage != null) {
                    Log.i(TAG, "Game exited — reverting session boosts for daily apps compatibility");
                    lastActiveGamePackage = null;
                    GameSessionSettings.restore(getApplicationContext());
                    com.gamebooster.app.spoofer.DeviceSpooferEngine.resetSpoofing();
                    
                    // Revert session-specific GPU / rendering overrides back to clean user state
                    com.gamebooster.app.core.settings.SettingsStateRestorer.restoreAllSettings(getApplicationContext());
                    
                    AppExecutors.getInstance().postToMainThread(() ->
                            android.widget.Toast.makeText(getApplicationContext(), "⚡ Game Session Ended — System Standard Mode Restored", android.widget.Toast.LENGTH_SHORT).show());
                }
            } finally {
                isChecking = false;
            }
        });
    }

    private String getForegroundPackage() {
        // 1. Primary: UsageStatsManager query
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                UsageEvents events = usm.queryEvents(time - 10000, time);
                if (events != null) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    String lastPkg = null;
                    while (events.hasNextEvent()) {
                        events.getNextEvent(event);
                        if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            lastPkg = event.getPackageName();
                        }
                    }
                    if (lastPkg != null && !lastPkg.isEmpty()) {
                        return lastPkg;
                    }
                }
            }
        } catch (Exception ignored) {}

        // 2. Secondary: Shizuku ADB dumpsys fallback for 100% background auto-detection without UsageStats permission
        if (com.gamebooster.app.shizuku.ShizukuExecutor.isShizukuAvailable()) {
            try {
                String output = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("dumpsys window visible-apps");
                if (output != null && output.contains("package=")) {
                    int idx = output.indexOf("package=");
                    while (idx != -1) {
                        int end = output.indexOf(" ", idx);
                        if (end == -1) end = output.indexOf("\n", idx);
                        if (end != -1) {
                            String pkg = output.substring(idx + 8, end).trim();
                            if (!pkg.isEmpty() && !pkg.equals(getPackageName())) {
                                return pkg;
                            }
                        }
                        idx = output.indexOf("package=", idx + 8);
                    }
                }
            } catch (Throwable ignored) {}
        }
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
