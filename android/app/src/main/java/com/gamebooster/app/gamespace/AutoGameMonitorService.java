package com.gamebooster.app.gamespace;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.config.GameConfigPatcher;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.GameSessionSettings;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.RefreshRateOverrideEngine;
import com.gamebooster.app.gamedetector.ForegroundAppDetector;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameManagerRepository;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.overlay.FloatingOverlayService;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.ui.screens.MainActivity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Enterprise-Grade Auto Game Space Monitor Service.
 * Continuously monitors foreground application changes, triggers deep hardware masking,
 * unlocks maximum display refresh rates (120Hz–185Hz ROG Mode), applies per-game configs,
 * activates Gaming DND, and purges RAM buffers automatically on Android 12, 13, 14, 15, and 16.
 */
public class AutoGameMonitorService extends Service {

    private static final String TAG = "AutoGameMonitor";
    private static final String CHANNEL_ID = "auto_game_monitor_channel";
    private static final int NOTIF_ID = 777;

    public static final String PREF_NAME = "auto_game_monitor_prefs";
    public static final String KEY_MONITOR_ENABLED = "auto_monitor_enabled";
    public static final String KEY_AUTO_CLEAN_RAM = "auto_clean_ram_on_launch";
    public static final String KEY_TARGET_HZ = "auto_monitor_target_hz";

    private static boolean isRunning = false;
    private Handler handler;
    private Runnable monitorRunnable;
    private ForegroundAppDetector foregroundDetector;
    private String lastActiveGamePackage = null;
    private final Set<String> installedGamePackages = new HashSet<>();
    private long lastGameListRefreshTime = 0;

    public static boolean isRunning() {
        return isRunning;
    }

    public static boolean isMonitorEnabled(Context context) {
        if (context == null) return false;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_MONITOR_ENABLED, false);
    }

    public static void setMonitorEnabledPref(Context context, boolean enabled) {
        if (context == null) return;
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_MONITOR_ENABLED, enabled)
                .apply();
    }

    public static void start(Context context) {
        if (context == null) return;
        setMonitorEnabledPref(context, true);
        if (isRunning) return;
        Intent intent = new Intent(context, AutoGameMonitorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        if (context == null) return;
        setMonitorEnabledPref(context, false);
        if (!isRunning) return;
        Intent intent = new Intent(context, AutoGameMonitorService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, createNotification(null, false), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIF_ID, createNotification(null, false));
        }

        foregroundDetector = new ForegroundAppDetector(getApplicationContext(), null, null);
        refreshInstalledGames();
        setupMonitorLoop();
        Log.i(TAG, "AutoGameMonitorService initialized successfully.");
    }

    private void setupMonitorLoop() {
        handler = new Handler(Looper.getMainLooper());
        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                checkForegroundApp();
                if (handler != null && isRunning) {
                    long delay = (lastActiveGamePackage != null) ? 2200 : 1200;
                    handler.postDelayed(this, delay);
                }
            }
        };
        handler.post(monitorRunnable);
    }

    private void checkForegroundApp() {
        AppExecutors.getInstance().executeCommand(() -> {
            if (foregroundDetector == null) return;
            String currentPackage = foregroundDetector.detectCurrentForegroundPackage();
            if (currentPackage == null || currentPackage.isEmpty() || currentPackage.equals(getPackageName())) {
                return;
            }

            if (System.currentTimeMillis() - lastGameListRefreshTime > 30000) {
                refreshInstalledGames();
            }

            boolean isGameActive = isGamePackage(currentPackage);

            // CASE 1: Game Launch Detected
            if (isGameActive && !currentPackage.equals(lastActiveGamePackage)) {
                lastActiveGamePackage = currentPackage;
                handleGameLaunch(currentPackage);
            }
            // CASE 2: Game Exited
            else if (!isGameActive && lastActiveGamePackage != null) {
                String exitedPackage = lastActiveGamePackage;
                lastActiveGamePackage = null;
                handleGameExit(exitedPackage);
            }
            // CASE 3: Background Home / Active Session Refresh Lock
            else if (!isGameActive && lastActiveGamePackage == null) {
                MaxHzForceChannel.forceApply(165);
            }
        });
    }

    private void handleGameLaunch(String packageName) {
        try {
            Context appCtx = getApplicationContext();
            GameSessionSettings.begin(appCtx, packageName);
            GameProfilePreferences.Profile profile = GameProfilePreferences.getProfile(appCtx, packageName);

            int targetHz = getTargetRefreshRate(packageName, profile);
            String gameTitle = getGameTitle(packageName);

            Log.i(TAG, "🎮 GAME LAUNCH DETECTED: " + gameTitle + " (" + packageName + ") — Applying " + profile.label + " @ " + targetHz + "Hz");

            // 1. Hardware Identity Spoofing & 6-Layer Deep Masking (Shizuku)
            DeviceSpooferEngine.applySpoofing(appCtx, packageName);

            // 2. Direct Game Config Patching (FPS, FOV, 1000Hz Gyro & Touch)
            GameConfigPatcher.patchGame(packageName, targetHz);

            // 3. Multi-layer Display Refresh Rate Force (120Hz/144Hz/165Hz/185Hz)
            MaxHzForceChannel.forceApply(targetHz);
            RefreshRateOverrideEngine.applyRefreshRate(appCtx, packageName,
                    RefreshRateOverrideEngine.RefreshRateMode.MODE_165HZ);

            // 4. Dedicated Chipset Optimizer (Snapdragon, Dimensity, Exynos, Tensor, Unisoc, Kirin)
            com.gamebooster.app.chipset.ChipsetOptimizerEngine.applyChipsetOptimization(appCtx, targetHz);

            // 5. Legal OEM Throttling Bypass (Joyose, GOS, ColorOS GPA, Dar-Link)
            com.gamebooster.app.oem.OemBypassEngine.applyOemBypass(appCtx, targetHz);

            // 6. Android Version Specific Optimizations (Android 12–16 / API 31–36)
            com.gamebooster.app.version.AndroidVersionOptimizer.applyVersionOptimizations(appCtx, packageName, targetHz);

            // 7. Ahead-Of-Time (AOT) DEX Speed Compilation (Zero In-Game JIT Stutter)
            com.gamebooster.app.dexopt.DexoptCompilationEngine.compileGameSpeedAsync(packageName, null);

            // 8. Extreme Performance Profile & Tweaks Script
            PerformanceChannel.applyProfile(appCtx, PerformanceChannel.Profile.EXTREME_PERFORMANCE);
            PerformanceChannel.writeAndExecuteRootTweaksScript(targetHz);

            // 9. Gaming DND & Heads-up Notification Suppression
            GameSpaceDndManager.setGamingDndMode(appCtx, profile.enableDnd);

            // 10. Deep RAM & Shader Cache Purge
            boolean autoCleanRam = appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                    .getBoolean(KEY_AUTO_CLEAN_RAM, true);
            if (autoCleanRam) {
                GameCacheCleaner.performDeepGameCacheClean(appCtx);
            }

            // 11. Auto-Launch Floating Gaming HUD
            if (!FloatingOverlayService.isOverlayRunning()) {
                FloatingOverlayService.startOverlay(appCtx);
            }

            // 12. Update Live Foreground Notification
            updateNotification(gameTitle, true, targetHz);

            // 13. Display User Toast
            AppExecutors.getInstance().postToMainThread(() ->
                    Toast.makeText(appCtx, "⚡ " + gameTitle + " Boosted (" + targetHz + " FPS & " + targetHz + "Hz Locked)", Toast.LENGTH_LONG).show());

        } catch (Throwable t) {
            Log.e(TAG, "Error handling game launch for: " + packageName, t);
        }
    }

    private void handleGameExit(String packageName) {
        try {
            Context appCtx = getApplicationContext();
            Log.i(TAG, "Game exited: " + packageName + " — Maintaining performance & background smoothness.");

            GameSessionSettings.restore(appCtx);
            DeviceSpooferEngine.resetSpoofing();
            GameSpaceDndManager.setGamingDndMode(appCtx, false);

            // Enforce Background Home 165Hz Refresh Rate & Performance state
            MaxHzForceChannel.forceApply(165);
            HzFpsChannel.forceSetRefreshRate(appCtx, 165);
            PerformanceChannel.applyProfile(appCtx, PerformanceChannel.Profile.EXTREME_PERFORMANCE);

            // Restore Notification to Idle Monitoring state
            updateNotification(null, false, 165);

            AppExecutors.getInstance().postToMainThread(() ->
                    Toast.makeText(appCtx, "⚡ Game Space: Performance Profile Restored", Toast.LENGTH_SHORT).show());

        } catch (Throwable t) {
            Log.e(TAG, "Error handling game exit for: " + packageName, t);
        }
    }

    private int getTargetRefreshRate(String packageName, GameProfilePreferences.Profile profile) {
        int customHz = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_TARGET_HZ, 185);
        if (customHz == 120 || customHz == 144 || customHz == 165 || customHz == 185) {
            return customHz;
        }
        return 185;
    }

    private boolean isGamePackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;
        if (installedGamePackages.contains(packageName)) return true;
        if (GamePackageRegistry.isKnownGame(packageName)) return true;
        return foregroundDetector != null && foregroundDetector.isKnownGamePackage(packageName);
    }

    private void refreshInstalledGames() {
        try {
            installedGamePackages.clear();
            List<GameAppInfo> games = GameManagerRepository.getInstalledGames(getApplicationContext());
            if (games != null) {
                for (GameAppInfo g : games) {
                    if (g.getPackageName() != null) {
                        installedGamePackages.add(g.getPackageName());
                    }
                }
            }
            lastGameListRefreshTime = System.currentTimeMillis();
        } catch (Throwable ignored) {}
    }

    private String getGameTitle(String packageName) {
        GamePackageRegistry.GameInfoSpec spec = GamePackageRegistry.getSpec(packageName);
        if (spec != null && spec.title != null) {
            return spec.title;
        }
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            CharSequence label = pm.getApplicationLabel(ai);
            if (label != null) return label.toString();
        } catch (Throwable ignored) {}
        return packageName;
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
        if (foregroundDetector != null) {
            foregroundDetector.stopMonitoring();
        }
        Log.i(TAG, "AutoGameMonitorService destroyed.");
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
                    "Game Space Auto Monitor",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows active game optimization and monitor status");
            channel.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification(String activeGameTitle, boolean isGameActive) {
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notifIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0
        );

        String title = isGameActive ? "🎮 GAME SPACE ACTIVE — " + activeGameTitle : "⚡ GAME SPACE — Auto Monitor Active";
        String content = isGameActive ? "Running in Extreme Performance Mode @ 185Hz / 185 FPS Lock" : "Monitoring installed games for auto-boost & hardware masking...";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void updateNotification(String gameTitle, boolean isGameActive, int targetHz) {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.notify(NOTIF_ID, createNotification(gameTitle, isGameActive));
            }
        } catch (Throwable ignored) {}
    }
}
