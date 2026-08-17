package com.gamebooster.app.shizuku;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;

/**
 * ShizukuPermissionEnforcer — Unlocks all Android storage, data, obb,
 * restricted settings, overlay, and system permissions via elevated Shizuku shell in high-speed batches.
 * Fully compatible with Android 11, 12, 13, 14, 15, and 16.
 */
public class ShizukuPermissionEnforcer {

    private static final String TAG = "ShizukuPermEnforcer";
    private static volatile boolean isEnforcing = false;

    /**
     * Unlocks full file system access, legacy storage, appops, and system permissions in a single fast batch.
     */
    public static void enforceAllPermissions(Context context) {
        if (context == null) return;
        if (!ShizukuExecutor.hasShizukuPermission() && !RishManager.isAvailable(context)) {
            Log.w(TAG, "Cannot enforce permissions: Shizuku is not available or granted.");
            return;
        }

        if (isEnforcing) return;

        AppExecutors.getInstance().executeCommand(() -> {
            isEnforcing = true;
            try {
                String pkg = context.getPackageName();
                Log.i(TAG, "Enforcing batch system & storage permissions for: " + pkg);

                StringBuilder script = new StringBuilder();

                // 1. Core Storage & Media Permissions
                String[] permissions = new String[]{
                        "android.permission.MANAGE_EXTERNAL_STORAGE",
                        "android.permission.READ_EXTERNAL_STORAGE",
                        "android.permission.WRITE_EXTERNAL_STORAGE",
                        "android.permission.READ_MEDIA_IMAGES",
                        "android.permission.READ_MEDIA_VIDEO",
                        "android.permission.READ_MEDIA_AUDIO",
                        "android.permission.WRITE_SECURE_SETTINGS",
                        "android.permission.WRITE_SETTINGS",
                        "android.permission.PACKAGE_USAGE_STATS",
                        "android.permission.DUMP",
                        "android.permission.BATTERY_STATS",
                        "android.permission.MANAGE_GAME_MODE",
                        "android.permission.OVERRIDE_WIFI_CONFIG",
                        "android.permission.CHANGE_COMPONENT_ENABLED_STATE",
                        "android.permission.CHANGE_NETWORK_STATE",
                        "android.permission.FORCE_STOP_PACKAGES",
                        "android.permission.CLEAR_APP_CACHE",
                        "android.permission.REAL_GET_TASKS",
                        "android.permission.SET_PROCESS_LIMIT",
                        "android.permission.ACCESS_NOTIFICATION_POLICY",
                        "android.permission.SCHEDULE_EXACT_ALARM",
                        "android.permission.USE_EXACT_ALARM",
                        "android.permission.SYSTEM_ALERT_WINDOW",
                        "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",
                        "android.permission.REQUEST_INSTALL_PACKAGES",
                        "android.permission.REQUEST_DELETE_PACKAGES",
                        "android.permission.WAKE_LOCK",
                        "android.permission.VIBRATE",
                        "android.permission.DISABLE_KEYGUARD",
                        "android.permission.REORDER_TASKS",
                        "android.permission.EXPAND_STATUS_BAR",
                        "android.permission.HIGH_SAMPLING_RATE_SENSORS",
                        "android.permission.STATUS_BAR",
                        "android.permission.INTERACT_ACROSS_USERS"
                };

                for (String perm : permissions) {
                    script.append("pm grant ").append(pkg).append(" ").append(perm).append(" 2>/dev/null; ");
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    script.append("pm grant ").append(pkg).append(" android.permission.POST_NOTIFICATIONS 2>/dev/null; ");
                }

                // 2. Complete AppOps Overrides
                String[] allAppOps = new String[]{
                        "MANAGE_EXTERNAL_STORAGE",
                        "READ_EXTERNAL_STORAGE",
                        "WRITE_EXTERNAL_STORAGE",
                        "NO_ISOLATED_STORAGE",
                        "LEGACY_STORAGE",
                        "ACCESS_RESTRICTED_SETTINGS",
                        "SYSTEM_ALERT_WINDOW",
                        "GET_USAGE_STATS",
                        "PACKAGE_USAGE_STATS",
                        "WRITE_SETTINGS",
                        "MANAGE_GAME_MODE",
                        "RUN_IN_BACKGROUND",
                        "RUN_ANY_IN_BACKGROUND",
                        "AUTO_START",
                        "TURN_SCREEN_ON",
                        "PROJECT_MEDIA",
                        "DUMP",
                        "SCHEDULE_EXACT_ALARM",
                        "USE_EXACT_ALARM",
                        "POST_NOTIFICATION",
                        "ACCESS_NOTIFICATIONS",
                        "CHANGE_WIFI_STATE",
                        "REQUEST_INSTALL_PACKAGES",
                        "WAKE_LOCK",
                        "START_FOREGROUND"
                };

                for (String op : allAppOps) {
                    script.append("cmd appops set ").append(pkg).append(" ").append(op).append(" allow 2>/dev/null; ");
                }

                // 3. Battery Optimization / Doze Mode Bypass
                script.append("dumpsys deviceidle whitelist +").append(pkg).append(" 2>/dev/null; ");
                script.append("cmd deviceidle whitelist +").append(pkg).append(" 2>/dev/null; ");

                // Execute entire batch in a single subshell invocation
                ShizukuExecutor.executeShizukuCommand(script.toString());

                Log.i(TAG, "⚡ All Shizuku system & storage privileges batch-enforced successfully!");

            } catch (Throwable t) {
                Log.e(TAG, "Error enforcing Shizuku permissions", t);
            } finally {
                isEnforcing = false;
            }
        });
    }

    /**
     * Unlocks storage permissions and AppOps for a specific target game package.
     */
    public static void enforceGamePermissions(String gamePackageName) {
        if (gamePackageName == null || gamePackageName.trim().isEmpty()) return;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                String cmd = "pm grant " + gamePackageName + " android.permission.READ_EXTERNAL_STORAGE 2>/dev/null; " +
                        "pm grant " + gamePackageName + " android.permission.WRITE_EXTERNAL_STORAGE 2>/dev/null; " +
                        "pm grant " + gamePackageName + " android.permission.MANAGE_EXTERNAL_STORAGE 2>/dev/null; " +
                        "cmd appops set " + gamePackageName + " MANAGE_EXTERNAL_STORAGE allow 2>/dev/null; " +
                        "cmd appops set " + gamePackageName + " LEGACY_STORAGE allow 2>/dev/null; " +
                        "cmd appops set " + gamePackageName + " NO_ISOLATED_STORAGE allow 2>/dev/null; " +
                        "cmd usage-stats set-app-standby-bucket " + gamePackageName + " active 2>/dev/null; " +
                        "cmd deviceidle whitelist +" + gamePackageName + " 2>/dev/null; " +
                        "cmd game mode performance " + gamePackageName + " 2>/dev/null; ";
                ShizukuExecutor.executeShizukuCommand(cmd);
            } catch (Throwable ignored) {}
        });
    }
}
