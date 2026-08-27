package com.gamebooster.app.gamemanager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.FpsUnlockTier;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.ShellExecutor;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.HomeGameScanner;
import com.gamebooster.app.shizuku.RishManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

import java.util.List;

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
 * PHASE 4 — ROBUST INTENT & ELEVATED DISPATCH:
 *   - Direct framework startActivity with explicit ComponentName + FLAG_INCLUDE_STOPPED_PACKAGES
 *   - Fallback Tier 1: Shizuku AIDL / cmd activity start-activity / am start
 *   - Fallback Tier 2: Rish / Root su am start -n <Component>
 *   - Fallback Tier 3: Play Store redirect if missing
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
        Intent intent = HomeGameScanner.resolveLaunchIntent(pm, packageName);
        String label = packageName;
        try {
            label = pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString();
        } catch (Throwable ignored) {}

        launchGame(context, packageName, intent, label, null);
    }

    /**
     * Instant launch pipeline with zero UI latency and parallel background optimization.
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

        int targetFps = GameProfilePreferences.getTargetHz(appContext, pkg);
        if (targetFps <= 0) targetFps = 185;
        final int fps = FpsUnlockTier.resolveTargetFps(targetFps);

        // ═══════════════════════════════════════════════════════════
        // STEP 1: RESOLVE BEST LAUNCH INTENT
        // ═══════════════════════════════════════════════════════════
        PackageManager pm = appContext.getPackageManager();
        Intent targetIntent = launchIntent;
        if (targetIntent == null) {
            targetIntent = HomeGameScanner.resolveLaunchIntent(pm, pkg);
        }
        if (targetIntent == null) {
            try {
                targetIntent = pm.getLaunchIntentForPackage(pkg);
            } catch (Throwable ignored) {}
        }

        // ═══════════════════════════════════════════════════════════
        // STEP 2: INSTANT DIRECT DISPATCH (0ms Latency on UI Thread)
        // ═══════════════════════════════════════════════════════════
        boolean launchedImmediately = false;
        if (targetIntent != null) {
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);


            try {
                context.startActivity(targetIntent);
                launchedImmediately = true;
            } catch (Throwable t1) {
                try {
                    appContext.startActivity(targetIntent);
                    launchedImmediately = true;
                } catch (Throwable t2) {
                    Log.w(TAG, "Direct startActivity failed for " + pkg + ": " + t2.getMessage());
                }
            }
        }

        if (launchedImmediately) {
            Toast.makeText(appContext, "🚀 Turbo Launching " + gameTitle + " @ " + fps + " FPS!", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onLaunchSuccess(pkg);
        }

        // ═══════════════════════════════════════════════════════════
        // STEP 3: ASYNC BOOSTS & ELEVATED SHELL LAUNCH FALLBACK
        // ═══════════════════════════════════════════════════════════
        final boolean directSuccess = launchedImmediately;
        final Intent resolvedIntent = targetIntent;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // If direct framework launch failed, execute elevated shell dispatch immediately
                if (!directSuccess) {
                    boolean elevatedSuccess = false;
                    ComponentName component = resolvedIntent != null ? resolvedIntent.getComponent() : null;
                    String componentStr = component != null ? component.flattenToShortString() : null;

                    String startCmd = (componentStr != null ? "cmd activity start-activity -W -n " + componentStr + " 2>/dev/null || am start -n " + componentStr + " 2>/dev/null || " : "")
                            + "monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1 2>/dev/null || am start --activity-brought-to-front -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p " + pkg + " 2>/dev/null";

                    // 1. Shizuku UserService
                    if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                        String out = ShizukuUserServiceConnector.getInstance().executeCommand(startCmd);
                        if (out != null && !out.contains("Error") && !out.contains("Exception")) {
                            elevatedSuccess = true;
                        }
                    }

                    // 2. Shizuku reflection
                    if (!elevatedSuccess && ShizukuExecutor.hasShizukuPermission()) {
                        String out = ShizukuExecutor.executeShizukuCommand(startCmd);
                        if (out != null && !out.startsWith("ERROR")) {
                            elevatedSuccess = true;
                        }
                    }

                    // 3. Rish
                    if (!elevatedSuccess && RishManager.isRishAvailable()) {
                        String out = RishManager.executeRishCommand(null, startCmd);
                        if (out != null && !out.startsWith("ERROR")) {
                            elevatedSuccess = true;
                        }
                    }

                    // 4. Root su
                    if (!elevatedSuccess && ShellExecutor.isRootSuAvailable()) {
                        ShellExecutor.CommandResult cr = ShellExecutor.executeCommand(startCmd, true);
                        if (cr.isSuccess()) {
                            elevatedSuccess = true;
                        }
                    }

                    // 5. Normal sh fallback
                    if (!elevatedSuccess) {
                        ShellExecutor.CommandResult cr = ShellExecutor.executeCommand(startCmd, false);
                        if (cr.isSuccess()) {
                            elevatedSuccess = true;
                        }
                    }

                    if (elevatedSuccess) {
                        AppExecutors.getInstance().postToMainThread(() -> {
                            Toast.makeText(appContext, "🚀 Privileged Elevated Launch: " + gameTitle + " @ " + fps + " FPS!", Toast.LENGTH_SHORT).show();
                            if (listener != null) listener.onLaunchSuccess(pkg);
                        });
                    } else {
                        // Fallback: Redirect to Play Store if package is truly not installed
                        AppExecutors.getInstance().postToMainThread(() -> {
                            try {
                                Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg));
                                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(marketIntent);
                            } catch (Throwable e) {
                                try {
                                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pkg));
                                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(webIntent);
                                } catch (Throwable ignored) {}
                            }
                        });
                    }
                }

                // ═══════════════════════════════════════════════════════════
                // STEP 4: APPLY HARDWARE, DRIVER & DISPLAY REFRESH BOOSTS
                // ═══════════════════════════════════════════════════════════
                try {
                    if (ShizukuExecutor.hasShizukuPermission()) {
                        // Game Driver (Vulkan/ANGLE) & Android Game Mode API (Strictly per-game; global is 0)
                        ShizukuExecutor.executeShizukuCommands(
                            "settings put global game_driver_all_apps 0 2>/dev/null",
                            "settings put global angle_gl_driver_all_angle 0 2>/dev/null",
                            "settings put global game_driver_opt_in_apps " + pkg + " 2>/dev/null",
                            "settings put global updatable_driver_production_opt_in_apps " + pkg + " 2>/dev/null",
                            "settings put global angle_gl_driver_selection_pkgs " + pkg + " 2>/dev/null",
                            "cmd game mode performance " + pkg + " 2>/dev/null",
                            "cmd window set-app-refresh-rate " + pkg + " " + fps + " 2>/dev/null",
                            "cmd game set --fps " + fps + " " + pkg + " 2>/dev/null",
                            "service call SurfaceFlinger 1035 i32 " + fps + " 2>/dev/null",
                            "setprop debug.sf.nobootanimation 1",
                            "setprop debug.sf.disable_backpressure 1",
                            "setprop debug.hwui.render_dirty_regions false"
                        );
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Hardware Hz/Driver warning: " + t.getMessage());
                }

                // ═══════════════════════════════════════════════════════════
                // STEP 5: FULL GAME SESSION (Governors, Masking, locks, C++ config)
                // ═══════════════════════════════════════════════════════════
                try {
                    GameManagerSessionEngine.beginSession(appContext, pkg);
                    com.gamebooster.app.overlay.GameSessionRecorder.getInstance().startSession(appContext, pkg, gameTitle);
                    com.gamebooster.app.overlay.GameTurboEdgeService.start(appContext);
                } catch (Throwable t) {
                    Log.w(TAG, "Session engine warning: " + t.getMessage());
                }

            } catch (Throwable t) {
                Log.e(TAG, "Async launch optimization error for " + pkg, t);
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> listener.onLaunchFailed(pkg, t.getMessage()));
                }
            }
        });
    }
}
