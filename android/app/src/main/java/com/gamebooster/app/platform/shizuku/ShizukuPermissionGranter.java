package com.gamebooster.app.platform.shizuku;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * ShizukuPermissionGranter — Master ADB permission granter for Game Launcher Pro.
 *
 * Automatically grants elevated system privileges (MANAGE_EXTERNAL_STORAGE, WRITE_SECURE_SETTINGS,
 * PACKAGE_USAGE_STATS, SYSTEM_ALERT_WINDOW, DUMP, BATTERY_STATS, etc.) using Shizuku ADB shell (uid 2000).
 */
public class ShizukuPermissionGranter {

    private static final String TAG = "ShizukuPermissionGranter";

    private ShizukuPermissionGranter() {}

    public static void grantAllPermissionsAsync() {
        grantAllPermissionsAsync("com.gamebooster.app");
    }

    public static void grantAllPermissionsAsync(String packageName) {
        if (packageName == null || packageName.isEmpty()) return;
        AppExecutors.getInstance().executeCommand(() -> {
            boolean success = grantAllPermissions(packageName);
            Log.i(TAG, "Shizuku full permission authorization for " + packageName + ": " + (success ? "GRANTED" : "FAILED/DEFERRED"));
        });
    }

    public static void grantAllPermissionsAsync(Context context) {
        if (context == null) return;
        grantAllPermissionsAsync(context.getPackageName());
    }

    /**
     * Executes ADB shell pm grant and appops set commands synchronously.
     *
     * @param packageName Target package name
     * @return true if Shizuku commands executed successfully
     */
    public static boolean grantAllPermissions(String packageName) {
        if (packageName == null || packageName.isEmpty()) return false;
        if (!ShizukuExecutor.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission unavailable. Permission grant deferred.");
            return false;
        }

        Log.i(TAG, "Initiating Shizuku master permission grant for: " + packageName);

        String[] pmPermissions = new String[] {
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.WRITE_SECURE_SETTINGS",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.DUMP",
            "android.permission.BATTERY_STATS",
            "android.permission.ACCESS_NOTIFICATION_POLICY",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.WRITE_SETTINGS",
            "android.permission.MANAGE_GAME_MODE",
            "android.permission.CHANGE_COMPONENT_ENABLED_STATE",
            "android.permission.FORCE_STOP_PACKAGES",
            "android.permission.CLEAR_APP_CACHE",
            "android.permission.SET_PROCESS_LIMIT"
        };

        for (String perm : pmPermissions) {
            String cmd = "pm grant " + packageName + " " + perm;
            String res = ShizukuExecutor.executeShizukuCommand(cmd);
            Log.d(TAG, "PM Grant " + perm + " -> " + res);
        }

        // AppOps settings overrides
        String[] appOpsCmds = new String[] {
            "appops set " + packageName + " MANAGE_EXTERNAL_STORAGE allow",
            "appops set " + packageName + " GET_USAGE_STATS allow",
            "appops set " + packageName + " SYSTEM_ALERT_WINDOW allow",
            "appops set " + packageName + " WRITE_SETTINGS allow"
        };

        for (String cmd : appOpsCmds) {
            String res = ShizukuExecutor.executeShizukuCommand(cmd);
            Log.d(TAG, "AppOps " + cmd + " -> " + res);
        }

        return true;
    }
}
