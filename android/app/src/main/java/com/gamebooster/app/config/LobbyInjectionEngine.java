package com.gamebooster.app.config;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * LobbyInjectionEngine — Stealth In-Lobby Configuration & Overdrive Injector.
 *
 * Prevents anti-cheat and startup file verification detection by splitting injection into two stages:
 *  - Stage 1 (Pre-Launch Prep): Creates target directories, ensures POSIX permissions (chmod 777/666),
 *    and takes a safety backup while leaving game config files untouched during cold boot.
 *  - Stage 2 (In-Lobby Live Injection): Executes automatically after a smart delay (default 18 seconds)
 *    when the game has finished loading assets, passed splash checksum verification, and entered the
 *    main home screen / lobby.
 * Fully automated via background detection or programmatic trigger.
 */
public final class LobbyInjectionEngine {

    private static final String TAG = "LobbyInjectionEngine";
    private static final int DEFAULT_LOBBY_DELAY_SECONDS = 18;

    private static final ExecutorService sWorkerExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());
    private static final ConcurrentHashMap<String, Runnable> sPendingInjections = new ConcurrentHashMap<>();

    private static volatile String sActiveGamePackage = null;
    private static volatile int sActiveTargetFps = 185;

    private LobbyInjectionEngine() {
    }

    /**
     * Sets the currently active game package and target FPS.
     */
    public static void setActiveGame(String packageName, int targetFps) {
        sActiveGamePackage = packageName;
        sActiveTargetFps = targetFps;
    }

    /**
     * Gets the currently active game package.
     */
    public static String getActiveGamePackage() {
        return sActiveGamePackage;
    }

    /**
     * Schedules a lobby-safe injection for the target game package with default delay (18s).
     */
    public static void scheduleLobbyInjection(Context context, String packageName, int targetFps) {
        scheduleLobbyInjection(context, packageName, targetFps, DEFAULT_LOBBY_DELAY_SECONDS);
    }

    /**
     * Schedules a lobby-safe injection with custom delay in seconds.
     *
     * @param context Application context
     * @param packageName Target game package
     * @param targetFps Target FPS
     * @param delaySeconds Seconds to wait for game splash & loading screen to finish
     */
    public static void scheduleLobbyInjection(Context context, String packageName, int targetFps, int delaySeconds) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        final String pkg = packageName.trim();
        final Context appContext = (context != null) ? context.getApplicationContext() : null;

        setActiveGame(pkg, targetFps);

        // Cancel any previous pending injection for this package
        cancelPending(pkg);

        Log.i(TAG, "🛡️ [Stage 1] Pre-launch preparation for " + pkg + " (No config modification during boot)");

        sWorkerExecutor.execute(() -> {
            try {
                // Ensure storage access and take backup before game starts
                GameConfigStorageAccessEngine.grantAllPathsAccess(appContext, pkg);
                ConfigBackupManager.backupAllPaths(pkg);
            } catch (Throwable t) {
                Log.w(TAG, "Stage 1 prep warning: " + t.getMessage());
            }
        });

        // Stage 2: Schedule delayed in-lobby injection
        final int delayMs = Math.max(5, delaySeconds) * 1000;
        Log.i(TAG, "⏳ [Stage 2] Scheduled in-lobby stealth injection for " + pkg + " in " + delaySeconds + "s (waiting for game home screen)...");

        Runnable injectTask = new Runnable() {
            @Override
            public void run() {
                sPendingInjections.remove(pkg);
                executeInLobbyInjection(appContext, pkg, targetFps, false);
            }
        };

        sPendingInjections.put(pkg, injectTask);
        sMainHandler.postDelayed(injectTask, delayMs);
    }

    /**
     * Manually or programmatically triggers immediate in-lobby injection.
     */
    public static void triggerManualLobbyInject(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            packageName = com.gamebooster.app.games.ForegroundGameDetector.detectActiveGame(context);
        }
        if (packageName == null || packageName.trim().isEmpty()) {
            packageName = sActiveGamePackage;
        }
        if (packageName == null || packageName.trim().isEmpty()) {
            try {
                packageName = com.gamebooster.app.gamemanager.GameManagerStatus.getInstance().getActiveGamePackage();
            } catch (Throwable ignored) {}
        }
        if (packageName == null || packageName.trim().isEmpty()) {
            if (context != null) {
                sMainHandler.post(() ->
                        Toast.makeText(context, "⚠️ No active game detected! Please open MLBB, PUBGM, CODM, etc.", Toast.LENGTH_SHORT).show());
            }
            return;
        }

        final String pkg = packageName.trim();
        sActiveGamePackage = pkg; // Sync active package state
        final Context appContext = (context != null) ? context.getApplicationContext() : null;
        final int targetFps = sActiveTargetFps;

        cancelPending(pkg);

        Log.i(TAG, "⚡ [Manual Trigger] Executing forced instant In-Lobby Injection for " + pkg);
        sWorkerExecutor.execute(() -> executeInLobbyInjection(appContext, pkg, targetFps, true));
    }

    /**
     * Cancels any pending scheduled injection for the package.
     */
    public static void cancelPending(String packageName) {
        if (packageName == null) return;
        Runnable pending = sPendingInjections.remove(packageName);
        if (pending != null) {
            sMainHandler.removeCallbacks(pending);
        }
    }

    /**
     * Executes the actual in-lobby configuration write and notifications.
     */
    private static void executeInLobbyInjection(Context context, String pkg, int targetFps, boolean isManual) {
        sWorkerExecutor.execute(() -> {
            long startTime = System.currentTimeMillis();
            Log.i(TAG, "🚀 [Stage 2 EXECUTING] Applying 2026 Overdrive & Configs inside game lobby for " + pkg);

            try {
                // Apply FPS patches and format-aware configurations
                GameConfigPatcher.applyGameFpsPatch(context, pkg, targetFps);
                NativeConfigInjector.injectAllConfigsForPackage(pkg, targetFps);

                String gameKey = CfgProfileManager.resolveGameKey(pkg);
                CompetitiveCfgProfile profile = CfgProfileManager.loadProfile(context, gameKey);
                if (profile == null) {
                    profile = new CompetitiveCfgProfile(gameKey, targetFps, true, true);
                }
                CommonConfigTuningInjector.applyAllEnabledTunings(pkg, profile);

                // Dispatch full game-specific 2026 Overdrive suite with force=true to bypass previous cache
                GameAutoInjectDispatcher.dispatchForPackage(context, pkg, true);

                // Re-apply anti-tamper permissions, timestamp cloaking, and SELinux contexts right after in-lobby injection
                GameSecurityBypassEngine.postInjectionBypassAndLock(pkg);

                long duration = System.currentTimeMillis() - startTime;
                Log.i(TAG, "✅ [Stage 2 COMPLETE] In-Lobby Injection successful & locked for " + pkg + " in " + duration + "ms");

                final String gameTitle = com.gamebooster.app.games.GamePackageRegistry.getGameTitle(pkg, context);

                // Show visual confirmation on UI thread
                sMainHandler.post(() -> {
                    try {
                        if (context != null) {
                            String msg = isManual 
                                ? "⚡ Detected & Injected: " + gameTitle + " (" + targetFps + " FPS Overdrive)" 
                                : "⚡ " + gameTitle + " Overdrive Injected (Lobby Safe)";
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Throwable ignored) {}
                });

                // ── MLBB IN-LOBBY ENFORCEMENT & DRONE VIEW RE-PATCH ───────────
                if (pkg != null && (pkg.contains("mobile.legends") || pkg.contains("mobilelegends"))) {
                    try {
                        Context appCtx = (context != null) ? context.getApplicationContext() : null;
                        android.content.SharedPreferences prefs = appCtx != null
                                ? appCtx.getSharedPreferences("mlbb_drone_prefs", android.content.Context.MODE_PRIVATE)
                                : null;
                        boolean enabled = (prefs == null) || prefs.getBoolean("drone_enabled", true);
                        int tier = (prefs != null) ? prefs.getInt("drone_tier", MlbbDroneViewPatcher.DEFAULT_TIER) : MlbbDroneViewPatcher.DEFAULT_TIER;
                        if (enabled) {
                            boolean ok = MlbbDroneViewPatcher.applyDroneView(appCtx, pkg, tier);
                            Log.i(TAG, ok
                                    ? "🎯 [Stage 2 In-Lobby] MLBB Drone View [" + MlbbDroneViewPatcher.getTierLabel(tier) + "] locked successfully"
                                    : "⚠️ [Stage 2 In-Lobby] MLBB Drone View lock returned false");
                        }
                    } catch (Throwable droneErr) {
                        Log.w(TAG, "Stage 2 MLBB Drone View reinject warning: " + droneErr.getMessage());
                    }

                    scheduleDroneViewWave2(context, pkg);
                }

            } catch (Throwable t) {
                Log.e(TAG, "❌ In-lobby injection error for " + pkg + ": " + t.getMessage(), t);
            }
        });
    }

    /**
     * Schedules waves of MLBB drone view re-injection after lobby load.
     * Catches post-login mini_patch slot downloads and re-asserts read-only locks.
     */
    private static void scheduleDroneViewWave2(Context context, String pkg) {
        final Context appCtx = (context != null) ? context.getApplicationContext() : null;
        final String finalPkg = pkg;

        // Wave 2: 18 seconds after launch (when lobby UI is interactive)
        Runnable wave2 = () -> sWorkerExecutor.execute(() -> {
            try {
                Log.i(TAG, "🌊 [Wave-2] Re-patching MLBB Drone View at +18s for " + finalPkg);
                int tier = MlbbDroneViewPatcher.DEFAULT_TIER;
                boolean enabled = true;
                try {
                    android.content.SharedPreferences prefs = appCtx != null
                            ? appCtx.getSharedPreferences("mlbb_drone_prefs", android.content.Context.MODE_PRIVATE)
                            : null;
                    if (prefs != null) {
                        enabled = prefs.getBoolean("drone_enabled", true);
                        if (!enabled) return;
                        tier = prefs.getInt("drone_tier", MlbbDroneViewPatcher.DEFAULT_TIER);
                    }
                } catch (Throwable ignored) {}

                if (enabled) {
                    MlbbDroneViewPatcher.applyDroneView(appCtx, finalPkg, tier);
                }
            } catch (Throwable t) {
                Log.w(TAG, "[Wave-2] Drone View error: " + t.getMessage());
            }
        });
        sMainHandler.postDelayed(wave2, 18_000L);

        // Wave 3: 35 seconds (Final security reinforcement)
        Runnable wave3 = () -> sWorkerExecutor.execute(() -> {
            try {
                Log.i(TAG, "🌊 [Wave-3] Final reinforcement of MLBB Drone View at +35s for " + finalPkg);
                int tier = MlbbDroneViewPatcher.DEFAULT_TIER;
                boolean enabled = true;
                try {
                    android.content.SharedPreferences prefs = appCtx != null
                            ? appCtx.getSharedPreferences("mlbb_drone_prefs", android.content.Context.MODE_PRIVATE)
                            : null;
                    if (prefs != null) {
                        enabled = prefs.getBoolean("drone_enabled", true);
                        if (!enabled) return;
                        tier = prefs.getInt("drone_tier", MlbbDroneViewPatcher.DEFAULT_TIER);
                    }
                } catch (Throwable ignored) {}

                if (enabled) {
                    MlbbDroneViewPatcher.applyDroneView(appCtx, finalPkg, tier);
                }
            } catch (Throwable t) {
                Log.w(TAG, "[Wave-3] Drone View error: " + t.getMessage());
            }
        });
        sMainHandler.postDelayed(wave3, 35_000L);
    }
}
