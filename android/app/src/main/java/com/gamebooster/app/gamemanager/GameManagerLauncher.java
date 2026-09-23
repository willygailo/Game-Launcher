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

import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.HomeGameScanner;

/**
 * GameManagerLauncher — The unified, authoritative Game Launch Engine.
 *
 * Starts a game with its normal framework launch intent, records the launcher
 * session, and uses a stored display preference only when it maps to a physical
 * mode reported by Android.
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

        int targetHz = GameProfilePreferences.getTargetHz(appContext, pkg);
        final int displayHz = GameProfileAutoConfigurator.clampTargetFpsToDisplay(appContext, targetHz);

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
                Toast.makeText(appContext, "Launching " + gameTitle + "\nDisplay preference: " + displayHz + "Hz", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onLaunchSuccess(pkg);
            } else {
                reportLaunchFailure(appContext, pkg, gameTitle, listener);
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            launchNow.run();
        } else {
            AppExecutors.getInstance().postToMainThread(launchNow);
        }

        // ═══════════════════════════════════════════════════════════
        // STEP 4: DUAL-STAGE AUTO INJECTION PIPELINE
        // Stage 1 (Instant): Display Hz, CPU/GPU Turbo, Graphics Cfg & Hardware Spoof
        // Stage 2 (10s Lobby-Safe): In-Lobby Combat Overdrive & Hero Scripts Re-injection
        // ═══════════════════════════════════════════════════════════

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // STAGE 1: Instant full 3-tier master enforcement & initial configs
                com.gamebooster.app.engine.MasterOptimizationEnforcer.enforceGameLaunchOptimizations(appContext, pkg, targetHz);

                // STAGE 2: Schedule In-Lobby Stealth Overdrive re-injection at exactly 10 seconds
                // (Waits for splash screen / Moonton integrity check to complete, then locks combat mods)
                com.gamebooster.app.config.LobbyInjectionEngine.scheduleLobbyInjection(appContext, pkg, targetHz, 10);

                GameManagerSessionEngine.beginSession(appContext, pkg);
                com.gamebooster.app.gamespace.GameSpaceAnalyticsManager.onSessionStart(appContext, pkg, gameTitle);
                com.gamebooster.app.overlay.GameSessionRecorder.getInstance()
                        .startSession(appContext, pkg, gameTitle);
            } catch (Throwable t) {
                Log.w(TAG, "Game session start warning for " + pkg + ": " + t.getMessage());
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

    /** Reports a framework launch failure without executing shell fallbacks. */
    private static void reportLaunchFailure(Context appContext, String packageName,
                                            String gameTitle, OnGameLaunchListener listener) {
        Toast.makeText(appContext,
                "Cannot open " + gameTitle + ". Open it once from the phone launcher, then retry.",
                Toast.LENGTH_LONG).show();
        if (listener != null) {
            listener.onLaunchFailed(packageName, "No launchable activity was accepted by the device");
        }
    }

    /** Legacy hook retained for callers; it only records a display preference. */
    public static void preparePreLaunchConfigInjection(Context context, String pkg, int targetFps) {
        if (pkg == null || pkg.trim().isEmpty()) return;
        if (context != null) {
            GameProfilePreferences.setTargetHz(context, pkg, targetFps);
        }
    }
}
