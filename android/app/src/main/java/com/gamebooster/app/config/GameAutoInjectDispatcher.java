package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

/**
 * Compatibility bridge for older callers. Game data is never changed by the
 * launcher; callers can retain this API without mutating another app's files,
 * process state, or network configuration.
 */
public final class GameAutoInjectDispatcher {

    private static final String TAG = "GameAutoInject";

    private GameAutoInjectDispatcher() {
    }

    public static boolean isPackageInjected(String packageName) {
        return false;
    }

    public static void resetPackageInjectionState(String packageName) {
        // No state is retained.
    }

    public static void resetAll() {
        // No state is retained.
    }

    public static void dispatchForPackage(String packageName) {
        dispatchForPackage(null, packageName, false);
    }

    public static void dispatchForPackage(String packageName, boolean force) {
        dispatchForPackage(null, packageName, force);
    }

    public static void dispatchForPackage(Context context, String packageName) {
        dispatchForPackage(context, packageName, false);
    }

    public static void dispatchForPackage(Context context, String packageName, boolean force) {
        if (packageName != null && !packageName.trim().isEmpty()) {
            Log.i(TAG, "Skipped legacy game-data injection for " + packageName.trim());
        }
    }

    public static void scheduleLobbySafeInjection(Context context, String packageName, int targetFps) {
        dispatchForPackage(context, packageName, false);
    }

    public static void scheduleLobbySafeInjection(Context context, String packageName,
                                                  int targetFps, int delaySeconds) {
        dispatchForPackage(context, packageName, false);
    }
}
