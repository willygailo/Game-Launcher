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
     * Full asynchronous 4-phase launch pipeline with multi-tier fallback.
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
                    CommandExecutor.executeSystemCommand("sync; echo 3 > /proc/sys/vm/drop_caches 2>/dev/null");

                    if (ShizukuExecutor.hasShizukuPermission()) {
                        ShizukuExecutor.executeShizukuCommand("am kill-all 2>/dev/null");
                        ShizukuExecutor.executeShizukuCommand("am force-stop " + pkg + " 2>/dev/null");
                        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                    } else if (ShellExecutor.isRootSuAvailable()) {
                        ShellExecutor.executeCommand("am force-stop " + pkg + " 2>/dev/null", true);
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

                try {
                    if (ShizukuExecutor.hasShizukuPermission()) {
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
                        ShizukuExecutor.executeShizukuCommand(
                            "service call SurfaceFlinger 1035 i32 " + fps + " 2>/dev/null");

                        // 2d. Suppress debug jank
                        ShizukuExecutor.executeShizukuCommands(
                            "setprop debug.sf.nobootanimation 1",
                            "setprop debug.sf.disable_backpressure 1",
                            "setprop debug.hwui.render_dirty_regions false"
                        );
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Phase 2 driver/Hz forcing warning: " + t.getMessage());
                }

                // ═══════════════════════════════════════════════════════════
                // PHASE 3 — FULL GAME SESSION (Shizuku AIDL + Native Injection)
                // ═══════════════════════════════════════════════════════════
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(
                        () -> listener.onPreLaunchProgress("Phase 3: Applying Shizuku & Android API Boosts..."));
                }

                try {
                    GameManagerSessionEngine.beginSession(appContext, pkg);
                } catch (Throwable t) {
                    Log.w(TAG, "Phase 3 session engine warning: " + t.getMessage());
                }

                // ═══════════════════════════════════════════════════════════
                // PHASE 4 — ROBUST INTENT & ELEVATED DISPATCH
                // ═══════════════════════════════════════════════════════════
                PackageManager pm = appContext.getPackageManager();
                Intent finalIntent = launchIntent;
                if (finalIntent == null) {
                    finalIntent = HomeGameScanner.resolveLaunchIntent(pm, pkg);
                }

                if (finalIntent == null) {
                    finalIntent = pm.getLaunchIntentForPackage(pkg);
                }

                if (finalIntent == null) {
                    finalIntent = new Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_LAUNCHER)
                            .setPackage(pkg);
                }

                finalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_INCLUDE_STOPPED_PACKAGES
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                final Intent targetIntent = finalIntent;
                final ComponentName component = targetIntent.getComponent();

                AppExecutors.getInstance().postToMainThread(() -> {
                    boolean launched = false;

                    // Tier 1: Direct Framework startActivity
                    try {
                        context.startActivity(targetIntent);
                        launched = true;
                    } catch (Throwable t1) {
                        try {
                            appContext.startActivity(targetIntent);
                            launched = true;
                        } catch (Throwable t2) {
                            Log.w(TAG, "Standard startActivity failed for " + pkg + ": " + t2.getMessage());
                        }
                    }

                    // Tier 2: Privileged Elevated Shell Launch (Shizuku / AIDL / Rish / Root)
                    if (!launched) {
                        final String componentStr = component != null ? component.flattenToShortString() : null;
                        AppExecutors.getInstance().executeCommand(() -> {
                            boolean elevatedSuccess = false;

                            String startCmd = componentStr != null
                                    ? "cmd activity start-activity -W -n " + componentStr + " 2>/dev/null || am start -n " + componentStr + " 2>/dev/null"
                                    : "am start --activity-brought-to-front -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -p " + pkg + " 2>/dev/null || monkey -p " + pkg + " -c android.intent.category.LAUNCHER 1";

                            // Try Shizuku UserService
                            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                                String out = ShizukuUserServiceConnector.getInstance().executeCommand(startCmd);
                                if (out != null && !out.contains("Error") && !out.contains("Exception")) {
                                    elevatedSuccess = true;
                                }
                            }

                            // Try Shizuku reflection
                            if (!elevatedSuccess && ShizukuExecutor.hasShizukuPermission()) {
                                String out = ShizukuExecutor.executeShizukuCommand(startCmd);
                                if (out != null && !out.startsWith("ERROR")) {
                                    elevatedSuccess = true;
                                }
                            }

                            // Try Rish
                            if (!elevatedSuccess && RishManager.isRishAvailable()) {
                                String out = RishManager.executeRishCommand(null, startCmd);
                                if (out != null && !out.startsWith("ERROR")) {
                                    elevatedSuccess = true;
                                }
                            }

                            // Try Root su
                            if (!elevatedSuccess && ShellExecutor.isRootSuAvailable()) {
                                ShellExecutor.CommandResult cr = ShellExecutor.executeCommand(startCmd, true);
                                if (cr.isSuccess()) {
                                    elevatedSuccess = true;
                                }
                            }

                            // Tier 3: Play Store fallback if app is completely missing
                            if (!elevatedSuccess) {
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
                        });
                        launched = true;
                    }

                    if (launched) {
                        Toast.makeText(appContext, "⚡ " + gameTitle + " is Running in Boosted Mode @ " + fps + " FPS!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onLaunchSuccess(pkg);
                    } else {
                        if (listener != null) listener.onLaunchFailed(pkg, "Failed to dispatch intent");
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
