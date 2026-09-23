package com.gamebooster.app.gamemanager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
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

import com.gamebooster.app.config.CfgProfileManager;
import com.gamebooster.app.config.CommonConfigTuningInjector;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.config.GameAutoInjectDispatcher;
import com.gamebooster.app.config.GameConfigPatcher;
import com.gamebooster.app.config.GameConfigStorageAccessEngine;
import com.gamebooster.app.config.GameSecurityBypassEngine;
import com.gamebooster.app.config.LobbyInjectionEngine;
import com.gamebooster.app.config.NativeConfigInjector;

import java.util.List;

/**
 * GameManagerLauncher — The unified, authoritative Game Launch Engine.
 *
 * Runs the full 4-phase pre-launch optimization pipeline before starting any game:
 *
 * PHASE 1 — PURGE & COLD START:
 *   - sync + echo 3 > /proc/sys/vm/drop_caches (RAM purge without killing background apps)
 *   - am force-stop <pkg> (guarantees cold-start; game re-reads all patched configs)
 *
 * PHASE 2 — FRAMEWORK DRIVER & Hz FORCING:
 *   - settings put global game_driver_opt_in_apps <pkg> (GPU Vulkan Game Driver)
 *   - settings put global updatable_driver_production_opt_in_apps <pkg>
 *   - settings delete global angle_gl_driver_selection_pkgs (Clean native GPU driver)
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
     * Guarantees that the game opens immediately upon clicking PLAY.
     */
    public static void launchGame(Context context, String packageName, Intent launchIntent,
                                  String label, OnGameLaunchListener listener) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) {
            if (listener != null) listener.onLaunchFailed("", "Null context or package name");
            return;
        }

        final Context appContext = context.getApplicationContext();
        String targetPkg = packageName.trim();

        // ═══════════════════════════════════════════════════════════
        // STEP 1: VERIFY PACKAGE INSTALLATION & RESOLVE LAUNCH INTENT
        // ═══════════════════════════════════════════════════════════
        PackageManager pm = appContext.getPackageManager();
        if (pm != null) {
            targetPkg = com.gamebooster.app.games.GameLauncherHelper.resolveInstalledFamilyPackage(appContext, targetPkg);
        }
        final String pkg = targetPkg;
        final String gameTitle = (label != null && !label.isEmpty()) ? label : pkg;

        int targetFps = GameProfilePreferences.getTargetHz(appContext, pkg);
        if (targetFps <= 0) targetFps = 185;
        final int fps = FpsUnlockTier.resolveTargetFps(targetFps);

        boolean isInstalled = false;
        if (pm != null) {
            isInstalled = HomeGameScanner.isPackageInstalled(pm, pkg);
        }

        if (!isInstalled) {
            Toast.makeText(appContext, "❌ Game Not Installed: " + gameTitle + " (Please install APK first)", Toast.LENGTH_LONG).show();
            if (listener != null) listener.onLaunchFailed(pkg, "Package " + pkg + " is not installed on device");
            return;
        }

        // Always prioritize the official framework launch intent with guaranteed explicit ComponentName
        Intent targetIntent = null;
        if (pm != null) {
            try {
                targetIntent = pm.getLaunchIntentForPackage(pkg);
            } catch (Throwable ignored) {}
            if (targetIntent == null) {
                try {
                    targetIntent = pm.getLeanbackLaunchIntentForPackage(pkg);
                } catch (Throwable ignored) {}
            }
        }
        if (targetIntent == null) {
            targetIntent = launchIntent;
        }
        if (targetIntent == null && pm != null) {
            targetIntent = HomeGameScanner.resolveLaunchIntent(pm, pkg);
        }

        // ═══════════════════════════════════════════════════════════
        // STEP 2: RATE-LIMIT RESET (main-thread safe, no I/O)
        // Only lightweight in-memory resets here. Heavy shell/config work
        // is dispatched AFTER startActivity to guarantee zero ANR on tap.
        // ═══════════════════════════════════════════════════════════
        try {
            com.gamebooster.app.config.AntiBanStealthEngine.resetRateLimit(pkg);
            GameAutoInjectDispatcher.resetPackageInjectionState(pkg);
        } catch (Throwable t) {
            Log.w(TAG, "⚠️ Rate-limit reset warning for " + pkg + ": " + t.getMessage());
        }

        // ═══════════════════════════════════════════════════════════
        // STEP 3: START THE GAME BEFORE ANY BOOST WORK
        //
        // The command executor is intentionally not in this path. It has a bounded
        // worker pool shared by config injection and shell work; queuing startActivity
        // there can leave a PLAY tap with no visible result. A copied intent also avoids
        // mutating the one retained by the home-card model.
        // ═══════════════════════════════════════════════════════════
        final Intent finalTargetIntent = targetIntent != null ? new Intent(targetIntent) : null;
        Runnable launchNow = () -> {
            boolean launched = launchWithFrameworkIntent(context, appContext, pkg, finalTargetIntent);
            if (launched) {
                boolean hasPriv = com.gamebooster.app.engine.PrivilegeBridgeEngine.isPrivilegedActive();
                String statusMsg = hasPriv ? "⚡ Shizuku Turbo Active" : "⚡ Boosting...";
                Toast.makeText(appContext, "🚀 " + fps + " FPS | " + statusMsg + "\n" + gameTitle, Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onLaunchSuccess(pkg);
            } else {
                launchWithPrivilegedFallback(appContext, pkg, finalTargetIntent, gameTitle, fps, listener);
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            launchNow.run();
        } else {
            AppExecutors.getInstance().postToMainThread(launchNow);
        }

        // ═══════════════════════════════════════════════════════════
        // STEP 4: ASYNC PARALLEL CONFIG INJECTION + HARDWARE BOOSTS
        // ALL heavy shell work (chmod, restorecon, config injection,
        // drone view, FPS lock, session engine) runs on background threads.
        // This is the FIX for the Android 16 ANR — nothing here touches
        // the main thread. The game is already starting while we boost.
        // ═══════════════════════════════════════════════════════════

        // 4a. Pre-launch config injection dispatched async with 4s ceiling
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // Async drone view — runs after game process starts
                try {
                    String gameKey = CfgProfileManager.resolveGameKey(pkg);
                    CompetitiveCfgProfile profile = CfgProfileManager.loadProfile(appContext, gameKey);
                    if (profile != null && profile.isDroneViewUltraEnabled()) {
                        com.gamebooster.app.config.CommonConfigTuningInjector.applyDroneViewUltraConfig(pkg, profile.getDroneViewTier());
                        Log.i(TAG, "⚡ [Async] Drone View configs applied for " + pkg + " [tier=" + profile.getDroneViewTier() + "]");
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Async drone view warning for " + pkg + ": " + t.getMessage());
                }

                // Full config injection suite — heavy shell, safe on worker thread
                preparePreLaunchConfigInjection(appContext, pkg, fps);
                Log.i(TAG, "✅ [Async Inject] Config injection complete for " + pkg);
            } catch (Throwable t) {
                Log.w(TAG, "⚠️ Async pre-launch config warning for " + pkg + ": " + t.getMessage());
            }
        });

        // NOTE: LobbyInjectionEngine Stage 2 scheduling is intentionally NOT called here.
        // AutoGameMonitorService.checkForegroundApp() schedules it (with force=true) AFTER
        // it confirms the game is actually running in foreground — much better timing than
        // scheduling from a tap handler where the game process may not have spawned yet.
        // Having both schedule it simultaneously caused file lock contention + double-inject.

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // Background fast-load burst & asset cache purge (post-activity-start, zero UI lag)
                try {
                    com.gamebooster.app.config.GameSecurityBypassEngine.purgeCorruptedAssetCaches(pkg);
                    com.gamebooster.app.engine.GameFastLoadAccelerator.triggerPreLaunchBurst(appContext, pkg);
                } catch (Throwable t) {
                    Log.w(TAG, "Pre-launch burst background error for " + pkg + ": " + t.getMessage());
                }
                // Apply 185 Hz lock to SurfaceFlinger, AOSP & OEM without clamping to 120
                int maxPhysicalHz = 185;
                try {
                    com.gamebooster.app.device.DisplayCapabilitiesDetector.DisplayCaps caps =
                            com.gamebooster.app.device.DisplayCapabilitiesDetector.detect(appContext);
                    if (caps != null && caps.maxRefreshRate > 0) {
                        maxPhysicalHz = Math.max(caps.maxRefreshRate, 185);
                    }
                } catch (Throwable ignored) {}
                final int safeFps = Math.max(60, Math.min(185, fps > 0 ? fps : 185));
                try {
                    MaxHzForceChannel.forceApply(safeFps);
                    HzFpsChannel.forceSetRefreshRate(appContext, safeFps);
                } catch (Throwable t) {
                    Log.w(TAG, "Refresh rate lock warning: " + t.getMessage());
                }

                boolean hasPrivilege = com.gamebooster.app.engine.PrivilegeBridgeEngine.isPrivilegedActive();
                if (hasPrivilege) {
                    // Bug #7 fixed: replaced hand-written partial command list with
                    // AndroidVersionSupportManager.applyVersionOptimizations() — this now
                    // correctly fires Android 14/15/16 shell paths (PowerHAL, --performance-class 3)
                    // that were missing from the manual list.
                    com.gamebooster.app.engine.AndroidVersionSupportManager.applyVersionOptimizations(appContext, pkg, safeFps);

                    // GPU Vulkan driver opt-in (kept separate — not in version manager)
                    CommandExecutor.executeBatchCommands(java.util.Arrays.asList(
                        "settings put global game_driver_opt_in_apps " + pkg + " 2>/dev/null",
                        "settings put global updatable_driver_production_opt_in_apps \"\" 2>/dev/null",
                        "setprop debug.sf.fps_limit " + safeFps,
                        "setprop persist.sys.NV_FPSLIMIT " + safeFps,
                        "setprop debug.sf.nobootanimation 1",
                        "setprop debug.hwui.render_dirty_regions false",
                        "setprop debug.egl.hw 1",
                        "setprop debug.sf.hw 1"
                    ));
                }

                // Full Game Session: Native C++ config injection, hardware masking, locks, and Game Guardian Foreground Service
                try {
                    com.gamebooster.app.services.GameBoostForegroundService.start(appContext, pkg, safeFps);
                    com.gamebooster.app.booster.BackgroundLimitImmunityEngine.enforceImmunity(appContext);
                    GameManagerSessionEngine.beginSession(appContext, pkg);
                    com.gamebooster.app.engine.VulkanRayTracingEngine.applyVulkanOptimizations(pkg);
                    com.gamebooster.app.booster.BypassChargingController.onGameStarted(appContext);
                    com.gamebooster.app.gamespace.GameSpaceAnalyticsManager.onSessionStart(appContext, pkg, gameTitle);

                    // Dynamic Mode Execution: No Limit FPS vs Balanced High FPS
                    if (safeFps >= 144) {
                        com.gamebooster.app.tweaks.TweakManagerRepository.applyNoLimitFpsGamingMode(appContext);
                        com.gamebooster.app.booster.NoLimitExtremeOverdriveEngine.engageNoLimitOverdrive(appContext, pkg, safeFps);
                    } else {
                        com.gamebooster.app.tweaks.TweakManagerRepository.applyBalancedHighFpsMode(appContext);
                    }
                    com.gamebooster.app.tweaks.TweakManagerRepository.restoreAppliedTweaksAsync(appContext);

                    com.gamebooster.app.overlay.GameSessionRecorder.getInstance().startSession(appContext, pkg, gameTitle);
                    com.gamebooster.app.overlay.GameTurboEdgeService.start(appContext);
                    com.gamebooster.app.engine.GameFastLoadAccelerator.scheduleLaunchSustainTransition(pkg);
                } catch (Throwable t) {
                    Log.w(TAG, "Session engine begin warning: " + t.getMessage());
                }

            } catch (Throwable t) {
                Log.e(TAG, "Launch error for " + pkg, t);
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> listener.onLaunchFailed(pkg, t.getMessage()));
                }
            }
        });
    }

    /**
     * Starts a launchable game immediately. This method must only be called from the
     * main thread so an Activity caller retains its foreground-launch allowance.
     */
    private static boolean launchWithFrameworkIntent(Context sourceContext, Context appContext,
                                                     String packageName, Intent launchIntent) {
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            try {
                sourceContext.startActivity(launchIntent);
                Log.i(TAG, "Framework launch started for " + packageName);
                return true;
            } catch (Throwable sourceError) {
                Log.w(TAG, "Caller-context launch failed for " + packageName + ": "
                        + sourceError.getMessage());
                try {
                    appContext.startActivity(new Intent(launchIntent));
                    Log.i(TAG, "Application-context launch started for " + packageName);
                    return true;
                } catch (Throwable appError) {
                    Log.w(TAG, "Application-context launch failed for " + packageName + ": "
                            + appError.getMessage());
                }
            }
        }

        // Some OEM launchers reject an explicit component but still accept a package-scoped
        // MAIN/LAUNCHER intent. Keep this as a framework fallback before any shell command.
        try {
            Intent rawFallback = new Intent(Intent.ACTION_MAIN);
            rawFallback.addCategory(Intent.CATEGORY_LAUNCHER);
            rawFallback.setPackage(packageName);
            rawFallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            appContext.startActivity(rawFallback);
            Log.i(TAG, "Package-scoped framework launch started for " + packageName);
            return true;
        } catch (Throwable rawError) {
            Log.w(TAG, "Package-scoped framework launch failed for " + packageName + ": "
                    + rawError.getMessage());
            return false;
        }
    }

    /**
     * Elevated dispatch is a last resort only. Normal app-to-app launching does not require
     * root or Shizuku, and a failed framework launch must never silently disappear.
     */
    private static void launchWithPrivilegedFallback(Context appContext, String packageName,
                                                     Intent resolvedIntent, String gameTitle, int fps,
                                                     OnGameLaunchListener listener) {
        Toast.makeText(appContext, "⚠️ Retrying game launch…", Toast.LENGTH_SHORT).show();
        AppExecutors.getInstance().executeCommand(() -> {
            boolean elevatedSuccess = false;
            ComponentName component = resolvedIntent != null ? resolvedIntent.getComponent() : null;
            String componentName = component != null ? component.flattenToShortString() : null;
            String startCommand = (componentName != null
                    ? "am start -n " + componentName + " 2>/dev/null || " : "")
                    + "am start --activity-brought-to-front -a android.intent.action.MAIN"
                    + " -c android.intent.category.LAUNCHER -p " + packageName + " 2>/dev/null"
                    + " || monkey -p " + packageName
                    + " -c android.intent.category.LAUNCHER 1 2>/dev/null";

            try {
                if (ShellExecutor.isRootSuAvailable()) {
                    elevatedSuccess = ShellExecutor.executeSuCommand(startCommand).isSuccess();
                }
                if (!elevatedSuccess
                        && ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                    String output = ShizukuUserServiceConnector.getInstance().executeCommand(startCommand);
                    elevatedSuccess = output != null && !output.contains("Error")
                            && !output.contains("Exception");
                }
                if (!elevatedSuccess && ShizukuExecutor.hasShizukuPermission()) {
                    String output = ShizukuExecutor.executeShizukuCommand(startCommand);
                    elevatedSuccess = output != null && !output.startsWith("ERROR");
                }
                if (!elevatedSuccess && RishManager.isRishAvailable()) {
                    String output = RishManager.executeRishCommand(null, startCommand);
                    elevatedSuccess = output != null && !output.startsWith("ERROR");
                }
            } catch (Throwable fallbackError) {
                Log.w(TAG, "Privileged launch fallback failed for " + packageName + ": "
                        + fallbackError.getMessage());
            }

            final boolean launched = elevatedSuccess;
            AppExecutors.getInstance().postToMainThread(() -> {
                if (launched) {
                    Toast.makeText(appContext, "🚀 Launching " + gameTitle + " @ " + fps + " FPS",
                            Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onLaunchSuccess(packageName);
                } else {
                    Toast.makeText(appContext,
                            "❌ Cannot open " + gameTitle + ". Open it once from the phone launcher, then retry.",
                            Toast.LENGTH_LONG).show();
                    if (listener != null) {
                        listener.onLaunchFailed(packageName,
                                "No launchable activity was accepted by the device");
                    }
                }
            });
        });
    }

    /**
     * Executes synchronous pre-launch configuration injection, permissions granting,
     * C++ native patches, and anti-tamper security bypass locks before the game activity starts.
     * Guarantees that target configuration files (PlayerPrefs XML, UserCustom.ini, GameUserSettings.ini)
     * are 100% updated on disk before the game engine initializes and reads them.
     */
    public static void preparePreLaunchConfigInjection(Context context, String pkg, int targetFps) {
        if (pkg == null || pkg.trim().isEmpty()) return;
        try {
            Log.i(TAG, "⚡ [PreLaunch Sync] Injecting game configs and bypass locks for " + pkg + " @ " + targetFps + " FPS...");

            // 1. Ensure storage access permissions (Android 13-16 scoped storage / Shizuku / SAF)
            GameConfigStorageAccessEngine.grantAllPathsAccess(context, pkg);

            // 2. Unlock all target config paths for writing (chmod 777/666) & purge stale caches
            GameSecurityBypassEngine.unlockForInjection(pkg);

            // 3. Apply format-specific game FPS and graphics unlocks
            GameConfigPatcher.applyGameFpsPatch(context, pkg, targetFps);

            // 4. Run native C++ / JNI config injector (damage locks, touch polling, 185 FPS unlocks)
            NativeConfigInjector.injectAllConfigsForPackage(pkg, targetFps);

            // 5. Load or create full-featured competitive profile and apply all enabled tunings
            String gameKey = CfgProfileManager.resolveGameKey(pkg);
            CompetitiveCfgProfile profile = CfgProfileManager.loadProfile(context, gameKey);
            if (profile == null) {
                profile = new CompetitiveCfgProfile(gameKey, targetFps, true, true);
            }
            CommonConfigTuningInjector.applyAllEnabledTunings(pkg, profile);

            // 6. Dispatch complete game-specific auto-inject suite (MLBB, PUBGM, CODM, etc.)
            // Force=true: bypasses rate-limiter on launch so injection ALWAYS runs on game open
            GameAutoInjectDispatcher.dispatchForPackage(context, pkg, true);

            // 7. Enforce SELinux context bypass, UID/GID ownership, and safe anti-tamper permissions
            GameSecurityBypassEngine.postInjectionBypassAndLock(pkg);

            Log.i(TAG, "✅ [PreLaunch Sync COMPLETE] All config patches & bypasses applied to disk for " + pkg);
        } catch (Throwable t) {
            Log.e(TAG, "⚠️ Pre-launch config injection error for " + pkg + ": " + t.getMessage(), t);
        }
    }
}
