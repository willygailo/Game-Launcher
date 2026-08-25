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

    private void setupMonitorLoop() {
        monitorThread = new HandlerThread("AutoGameMonitor", Process.THREAD_PRIORITY_BACKGROUND);
        monitorThread.start();
        handler = new Handler(monitorThread.getLooper());
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

            boolean isGameActive = gamePackages.contains(currentPackage)
                    || com.gamebooster.app.games.GamePackageRegistry.isKnownGame(currentPackage);

            if (isGameActive && !currentPackage.equals(lastActiveGamePackage)) {
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

            } else if (!isGameActive && lastActiveGamePackage != null) {
                Log.i(TAG, "Game exited — ending GameManager Session for: " + lastActiveGamePackage);
                String exitingPkg = lastActiveGamePackage;
                lastActiveGamePackage = null;
                com.gamebooster.app.overlay.RealGameFpsMonitor.getInstance().setTargetPackage(null);

                // End GameManager Session and revert to baseline
                com.gamebooster.app.gamemanager.GameManagerSessionEngine.endSession(getApplicationContext(), exitingPkg);

                AppExecutors.getInstance().postToMainThread(() ->
                        android.widget.Toast.makeText(getApplicationContext(),
                                "↩ Stock Baseline Restored (Game Exited)",
                                android.widget.Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String getForegroundPackage() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                UsageEvents events = usm != null ? usm.queryEvents(time - 10000, time) : null;
                if (events != null) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    String lastPkg = null;
                    while (events.hasNextEvent()) {
                        events.getNextEvent(event);
                        if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            lastPkg = event.getPackageName();
                        }
                    }
                    if (lastPkg != null) return lastPkg;
                }
            }
        } catch (Exception ignored) {}

        // Fallback: Shizuku dumpsys window inspection
        try {
            if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
                String out = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'");
                if (out != null && !out.isEmpty()) {
                    for (String line : out.split("\n")) {
                        int slash = line.indexOf('/');
                        if (slash > 0) {
                            int space = line.lastIndexOf(' ', slash);
                            if (space >= 0 && slash > space + 1) {
                                String pkg = line.substring(space + 1, slash).trim();
                                if (!pkg.isEmpty() && !pkg.contains(" ") && pkg.contains(".")) {
                                    return pkg;
                                }
                            }
                        }
                    }
                }
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
