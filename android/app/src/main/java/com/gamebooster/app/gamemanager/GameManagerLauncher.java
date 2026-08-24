package com.gamebooster.app.gamemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

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
 * Runs the full pre-launch optimization pipeline before starting any game:
 * 1. Cache drop & RAM purge
 * 2. Full internal/external storage path combo unlock
 * 3. Hardware device identity & GPU spoofer masking
 * 4. Android 13-16 privileged GameMode, GameOverlay, and AppRefreshRate APIs
 * 5. Game session registration & high refresh rate locking
 * 6. Intent dispatch with FLAG_ACTIVITY_NEW_TASK
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
     * Full asynchronous launch pipeline with optional callback.
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
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> listener.onPreLaunchProgress("Purging RAM & Freeing Caches..."));
                }

                // 1. Drop caches and free RAM
                try {
                    CommandExecutor.executeSystemCommand("sync; echo 3 > /proc/sys/vm/drop_caches");
                    if (ShizukuExecutor.hasShizukuPermission()) {
                        ShizukuExecutor.executeShizukuCommand("am kill-all 2>/dev/null");
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Cache purge warning: " + t.getMessage());
                }

                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> listener.onPreLaunchProgress("Applying Shizuku & Android API Boosts..."));
                }

                // 2. Begin full game session
                GameManagerSessionEngine.beginSession(appContext, pkg);

                // 3. Prepare launch intent
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
                        Toast.makeText(appContext, "⚡ " + gameTitle + " is Running in Boosted Mode!", Toast.LENGTH_SHORT).show();
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
