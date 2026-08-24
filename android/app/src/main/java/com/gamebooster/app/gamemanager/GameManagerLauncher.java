package com.gamebooster.app.gamemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.FpsUnlockTier;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameLauncherHelper;
import com.gamebooster.app.games.GameManagerRepository;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;

/**
 * GameManagerLauncher — The unified, authoritative Game Launch Engine.
 *
 * Runs the full 4-phase pre-launch optimization pipeline before starting any game:
 *
 * PHASE 1 — PURGE & COLD START:
 *   - sync + echo 3 > /proc/sys/vm/drop_caches (RAM purge)
 *   - am kill-all (background process kill)
 *   - am force-stop <pkg> (guarantees cold-start; game re-reads all patched configs)
 *
 * PHASE 2 — FRAMEWORK DRIVER & Hz FORCING:
 *   - settings put global game_driver_opt_in_apps <pkg> (GPU Vulkan Game Driver)
 *   - settings put global updatable_driver_production_opt_in_apps <pkg>
 *   - settings put global angle_gl_driver_selection_pkgs <pkg>
 *   - cmd game mode performance <pkg> (Android Game Mode API)
 *   - cmd window set-app-refresh-rate <pkg> <fps>
 *   - cmd game set --fps <fps> <pkg>
 *   - service call SurfaceFlinger 1035 i32 <fps> (SurfaceFlinger direct Hz override)
 *   - setprop debug.sf.nobootanimation 1
 *
 * PHASE 3 — FULL GAME SESSION:
 *   - GameManagerSessionEngine.beginSession() (Shizuku AIDL, Spoofer, Config Injection,
 *     CPU/GPU governors, WiFi lock, DND, ADPF, NativeConfigInjector)
 *
 * PHASE 4 — INTENT DISPATCH:
 *   - startActivity with FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
 *   - Fallback: Shizuku monkey am start
 */
public final class GameManagerLauncher {

    private static final String TAG = "GameManagerLauncher";

    private GameManagerLauncher() {
    }

    public interface OnGameLaunchListener {
        void onPreLaunchProgress(String step);
        void onLaunchSuccess(String packageName);
        void onLaunchFailed(String packageName, String reason);
    }

    /**
     * Launches a game from GameAppInfo object with full pre-boost pipeline.
     */
    public static void launchGame(Context context, GameAppInfo game) {
        if (context == null || game == null) return;
        launchGame(context, game.getPackageName(), game.getLaunchIntent(), game.getLabel(), null);
    }

    /**
     * Launches a game by package name with full pre-boost pipeline.
     */
    public static void launchGame(Context context, String packageName) {
        if (context == null || packageName == null) return;
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        String label = packageName;
        try {
            label = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
        } catch (Throwable ignored) {}

        launchGame(context, packageName, intent, label, null);
    }

    /**
     * Full asynchronous 4-phase launch pipeline with optional callback.
     */
    public static void launchGame(Context context, String packageName, Intent launchIntent,
                                  String label, OnGameLaunchListener listener) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) {
            if (listener != null) listener.onLaunchFailed("", "Null context or package name");
            return;
        }

        final Context appContext = context.getApplicationContext();
        final String pkg = packageName.trim();
        final String gameTitle = (label != null && !label.isEmpty()) ? label : pkg;

        Toast.makeText(appContext, "🚀 Launching " + gameTitle + " via GAME-MANAGER...", Toast.LENGTH_SHORT).show();

        AppExecutors.getInstance().executeCommand(() -> {
            try {

                // ═══════════════════════════════════════════════════════════
                // PHASE 1 — PURGE & COLD START (Guaranteed fresh config reload)
                // ═══════════════════════════════════════════════════════════
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(
                        () -> listener.onPreLaunchProgress("Phase 1: Purging RAM & Cold-Starting " + gameTitle + "..."));
                }
                try {
                    // Drop OS page cache for maximum free RAM
                    CommandExecutor.executeSystemCommand("sync; echo 3 > /proc/sys/vm/drop_caches");

                    if (ShizukuExecutor.hasShizukuPermission()) {
                        // Kill all background processes to free RAM
                        ShizukuExecutor.executeShizukuCommand("am kill-all 2>/dev/null");

                        // CRITICAL: Force-stop the target game so it cold-starts and re-reads
                        // every config file patched by NativeConfigInjector / CfgProfileManager.
                        // Without this, games may skip config re-read on warm restart.
                        ShizukuExecutor.executeShizukuCommand("am force-stop " + pkg + " 2>/dev/null");

                        // Brief settle time for the process table to clean up
                        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Phase 1 purge warning: " + t.getMessage());
                }

                // ═══════════════════════════════════════════════════════════
                // PHASE 2 — FRAMEWORK DRIVER & SURFACEFLINGER Hz FORCING
                // ═══════════════════════════════════════════════════════════
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(
                        () -> listener.onPreLaunchProgress("Phase 2: Forcing GPU Driver & SurfaceFlinger Hz..."));
                }

                int targetFps = GameProfilePreferences.getTargetHz(appContext, pkg);
                if (targetFps <= 0) targetFps = 185;
                final int fps = FpsUnlockTier.resolveTargetFps(targetFps);

                if (ShizukuExecutor.hasShizukuPermission()) {
                    try {
                        // 2a. Game Driver opt-in (Vulkan / ANGLE / GLES)
                        ShizukuExecutor.executeShizukuCommands(
                            "settings put global game_driver_opt_in_apps " + pkg + " 2>/dev/null",
                            "settings put global updatable_driver_production_opt_in_apps " + pkg + " 2>/dev/null",
                            "settings put global angle_gl_driver_selection_pkgs " + pkg + " 2>/dev/null"
                        );

                        // 2b. Android Game Mode API (API 33+)
                        ShizukuExecutor.executeShizukuCommands(
                            "cmd game mode performance " + pkg + " 2>/dev/null",
                            "cmd window set-app-refresh-rate " + pkg + " " + fps + " 2>/dev/null",
                            "cmd game set --fps " + fps + " " + pkg + " 2>/dev/null"
                        );

                        // 2c. SurfaceFlinger direct override (deepest Hz lock path)
                        // Binder transaction 1035 = setDesiredDisplayModeSpecs
                        ShizukuExecutor.executeShizukuCommand(
                            "service call SurfaceFlinger 1035 i32 " + fps + " 2>/dev/null");

                        // 2d. Suppress boot animation & SurfaceFlinger debug jank
                        ShizukuExecutor.executeShizukuCommands(
                            "setprop debug.sf.nobootanimation 1",
                            "setprop debug.sf.disable_backpressure 1",
                            "setprop debug.hwui.render_dirty_regions false"
                        );
                    } catch (Throwable t) {
                        Log.w(TAG, "Phase 2 driver/Hz forcing warning: " + t.getMessage());
                    }
                }

                // ═══════════════════════════════════════════════════════════
                // PHASE 3 — FULL GAME SESSION (Shizuku AIDL + Native Injection)
                // ═══════════════════════════════════════════════════════════
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(
                        () -> listener.onPreLaunchProgress("Phase 3: Applying Shizuku & Android API Boosts..."));
                }

                GameManagerSessionEngine.beginSession(appContext, pkg);

                // ═══════════════════════════════════════════════════════════
                // PHASE 4 — INTENT DISPATCH
                // ═══════════════════════════════════════════════════════════
                Intent finalIntent = launchIntent;
                if (finalIntent == null) {
                    PackageManager pm = appContext.getPackageManager();
                    finalIntent = pm.getLaunchIntentForPackage(pkg);
                }

                if (finalIntent == null) {
                    finalIntent = new Intent(Intent.ACTION_MAIN);
                    finalIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    finalIntent.setPackage(pkg);
                }

                finalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                final Intent targetIntent = finalIntent;
                AppExecutors.getInstance().postToMainThread(() -> {
                    try {
                        appContext.startActivity(targetIntent);
                        Toast.makeText(appContext, "⚡ " + gameTitle + " is Running in Boosted Mode @ " + fps + " FPS!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onLaunchSuccess(pkg);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to start activity for " + pkg, e);
                        // Fallback via Shizuku am start
                        if (ShizukuExecutor.hasShizukuPermission()) {
                            AppExecutors.getInstance().executeCommand(() -> {
                                ShizukuExecutor.executeShizukuCommand("monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1");
                            });
                        }
                        if (listener != null) listener.onLaunchFailed(pkg, e.getMessage());
                    }
                });

            } catch (Throwable t) {
                Log.e(TAG, "Launch error for " + pkg, t);
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> listener.onLaunchFailed(pkg, t.getMessage()));
                }
            }
        });
    }
}
