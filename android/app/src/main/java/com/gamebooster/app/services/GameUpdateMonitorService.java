package com.gamebooster.app.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gamebooster.app.R;
import com.gamebooster.app.config.ConfigBackupManager;
import com.gamebooster.app.config.GameAutoInjectDispatcher;
import com.gamebooster.app.config.MlbbConfigPatcher;
import com.gamebooster.app.config.MlbbDroneViewPatcher;
import com.gamebooster.app.config.PubgConfigPatcher;
import com.gamebooster.app.config.CodmConfigPatcher;
import com.gamebooster.app.config.FreeFireConfigPatcher;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * GameUpdateMonitorService — Auto-Patch-on-Update Background Service.
 *
 * Listens for ACTION_PACKAGE_REPLACED / ACTION_PACKAGE_ADDED broadcasts for
 * all supported game packages. When a game update is detected:
 *   1. Waits for Shizuku readiness (up to 3 retries, exponential backoff).
 *   2. Dispatches the full injection pipeline via GameAutoInjectDispatcher.
 *   3. For MLBB: also calls applyMlbbNewMapUpdateConfig() for new-map sync.
 *   4. Shows a notification confirming the auto-patch status.
 *
 * Registration: The service registers a dynamic BroadcastReceiver at startup
 * (required for API 26+ — static receivers cannot catch PACKAGE_REPLACED).
 * Register this service in AndroidManifest.xml and start it from GameBoosterApp.
 */
@android.annotation.SuppressLint("ObsoleteSdkInt")
public class GameUpdateMonitorService extends Service {

    private static final String TAG          = "GameUpdateMonitor";
    private static final String CHANNEL_ID   = "game_update_monitor_channel";
    private static final int    NOTIF_ID     = 2026;
    private static final long   RETRY_DELAY_MS = 5_000L;
    private static final int    MAX_RETRIES  = 3;

    /** Per-package cooldown map: prevents double-injection within 30s of update */
    private final Map<String, Long> lastInjectedAt = new HashMap<>();
    private static final long COOLDOWN_MS = 30_000L;

    private Handler mainHandler;
    private PackageUpdateReceiver packageReceiver;
    private static boolean sRunning = false;

    public static boolean isRunning() { return sRunning; }

    // ── Service Lifecycle ─────────────────────────────────────────────────────

    @Override
    public void onCreate() {
        super.onCreate();
        sRunning = true;
        mainHandler = new Handler(Looper.getMainLooper());
        ConfigBackupManager.setAppContext(getApplicationContext());
        createNotificationChannel();
        startForeground(NOTIF_ID, buildPersistentNotification("Monitoring for game updates..."));
        registerPackageReceiver();
        Log.i(TAG, "GameUpdateMonitorService started.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sRunning = false;
        try {
            if (packageReceiver != null) unregisterReceiver(packageReceiver);
        } catch (Throwable ignored) {}
        Log.i(TAG, "GameUpdateMonitorService destroyed.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    // ── BroadcastReceiver Registration (dynamic — required API 26+) ───────────

    private void registerPackageReceiver() {
        packageReceiver = new PackageUpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(packageReceiver, filter);
            }
            Log.i(TAG, "Package update receiver registered.");
        } catch (Throwable t) {
            Log.w(TAG, "Receiver registration error: " + t.getMessage());
        }
    }

    // ── Package Update Receiver ───────────────────────────────────────────────

    private class PackageUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getData() == null) return;
            String pkg = intent.getData().getSchemeSpecificPart();
            if (pkg == null || pkg.isEmpty()) return;
            if (!GamePackageRegistry.isKnownGame(pkg)) return;

            boolean isUpdate = Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction());
            Log.i(TAG, "Game " + (isUpdate ? "UPDATED" : "INSTALLED") + ": " + pkg);

            // Cooldown check
            long now = System.currentTimeMillis();
            Long last = lastInjectedAt.get(pkg);
            if (last != null && (now - last) < COOLDOWN_MS) return;
            lastInjectedAt.put(pkg, now);

            showNotification("Game update detected", pkg + " updated — re-patching...");
            AppExecutors.getInstance().executeCommand(() ->
                    dispatchWithRetry(context, pkg, isUpdate, 0));
        }
    }

    // ── Patch Dispatch with Exponential Backoff Retry ─────────────────────────

    private void dispatchWithRetry(Context context, String pkg, boolean isUpdate, int attempt) {
        try {
            boolean shizukuReady = ShizukuExecutor.hasShizukuPermission();
            if (!shizukuReady && attempt < MAX_RETRIES) {
                long delay = RETRY_DELAY_MS * (1L << attempt);
                Log.w(TAG, "Shizuku not ready for " + pkg + " — retry #" + (attempt + 1) + " in " + (delay / 1000) + "s");
                mainHandler.postDelayed(() ->
                        AppExecutors.getInstance().executeCommand(() ->
                                dispatchWithRetry(context, pkg, isUpdate, attempt + 1)),
                        delay);
                return;
            }

            // Full injection pipeline
            GameAutoInjectDispatcher.dispatchForPackage(context, pkg, true);

            // MLBB: new-map sync after update
            if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
                MlbbConfigPatcher.applyMlbbNewMapUpdateConfig(pkg);
                int tier = MlbbDroneViewPatcher.DEFAULT_TIER;
                try {
                    tier = MlbbDroneViewPatcher.normalizeTier(context.getSharedPreferences("mlbb_drone_prefs", Context.MODE_PRIVATE)
                            .getInt("drone_tier", MlbbDroneViewPatcher.DEFAULT_TIER));
                } catch (Throwable ignored) {}
                MlbbDroneViewPatcher.applyNewMapUpdate(context, pkg, tier);
                Log.i(TAG, "[Auto] MLBB new-map sync applied after update: " + pkg);
            }

            // PUBG
            if (pkg.contains("tencent.ig") || pkg.contains("pubg") || pkg.contains("imobile")) {
                PubgConfigPatcher.applyPubgmGodModeFullOverdrive(pkg);
                PubgConfigPatcher.deployPakPatch(pkg);
            }

            // CODM
            if (pkg.contains("callofduty") || pkg.contains("cod")) {
                CodmConfigPatcher.applyCodmGodModeFullOverdrive(pkg);
            }

            // Free Fire
            if (pkg.contains("freefire") || pkg.contains("dts.freefire")) {
                FreeFireConfigPatcher.applyFreeFireMasterSuite(pkg);
            }

            // Auto Device & Hardware Profile Spoofing
            try {
                com.gamebooster.app.spoofer.DeviceSpooferEngine.applyWorkingSpoofForGame(context, pkg);
            } catch (Throwable ignored) {}

            showNotification("Auto-patch complete", pkg + " patched successfully after update.");
            Log.i(TAG, "[Auto-Patch Complete] " + pkg);

        } catch (Throwable t) {
            Log.e(TAG, "Auto-patch error for " + pkg + " (attempt " + attempt + "): " + t.getMessage(), t);
            if (attempt < MAX_RETRIES) {
                long delay = RETRY_DELAY_MS * (1L << attempt);
                mainHandler.postDelayed(() ->
                        AppExecutors.getInstance().executeCommand(() ->
                                dispatchWithRetry(context, pkg, isUpdate, attempt + 1)),
                        delay);
            } else {
                showNotification("Auto-patch failed", pkg + " — open launcher to re-patch manually.");
            }
        }
    }

    // ── Notification Helpers ──────────────────────────────────────────────────

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Game Update Auto-Patch", NotificationManager.IMPORTANCE_LOW);
            ch.setDescription("Auto-applies patches when games are updated");
            ch.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private Notification buildPersistentNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GameBooster — Update Monitor")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
                .build();
    }

    private void showNotification(String title, String text) {
        try {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm == null) return;
            nm.notify(NOTIF_ID + 1, new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(android.R.drawable.stat_sys_warning)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build());
        } catch (Throwable ignored) {}
    }

    // ── Static Helpers ────────────────────────────────────────────────────────

    /** Starts the service if it's not already running. */
    public static void startIfNeeded(Context context) {
        if (sRunning) return;
        try {
            Intent intent = new Intent(context, GameUpdateMonitorService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to start GameUpdateMonitorService: " + t.getMessage());
        }
    }

    /** Stops the service. */
    public static void stop(Context context) {
        try { context.stopService(new Intent(context, GameUpdateMonitorService.class)); }
        catch (Throwable ignored) {}
    }
}
