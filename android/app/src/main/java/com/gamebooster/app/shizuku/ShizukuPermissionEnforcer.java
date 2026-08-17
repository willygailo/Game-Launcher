package com.gamebooster.app.shizuku;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;

/**
 * ShizukuPermissionEnforcer — Unlocks all Android storage, data, obb,
 * restricted settings, overlay, and system permissions via elevated Shizuku shell.
 * Fully compatible with Android 11, 12, 13, 14, 15, and 16.
 */
public class ShizukuPermissionEnforcer {

    private static final String TAG = "ShizukuPermEnforcer";

    /**
     * Unlocks full file system access, legacy storage, appops, and system permissions.
     */
    public static void enforceAllPermissions(Context context) {
        if (context == null) return;
        if (!ShizukuExecutor.hasShizukuPermission() && !RishManager.isAvailable(context)) {
            Log.w(TAG, "Cannot enforce permissions: Shizuku is not available or granted.");
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                String pkg = context.getPackageName();
                Log.i(TAG, "Enforcing full system & storage permissions for: " + pkg);

                // 1. Core Storage & File System Permissions
                grantPermission(pkg, "android.permission.MANAGE_EXTERNAL_STORAGE");
                grantPermission(pkg, "android.permission.READ_EXTERNAL_STORAGE");
                grantPermission(pkg, "android.permission.WRITE_EXTERNAL_STORAGE");

                // 2. Android 13+ Granular Media Permissions
                grantPermission(pkg, "android.permission.READ_MEDIA_IMAGES");
                grantPermission(pkg, "android.permission.READ_MEDIA_VIDEO");
                grantPermission(pkg, "android.permission.READ_MEDIA_AUDIO");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    grantPermission(pkg, "android.permission.POST_NOTIFICATIONS");
                }

                // 3. System Tuning, Secure Settings & UI Control
                grantPermission(pkg, "android.permission.WRITE_SECURE_SETTINGS");
                grantPermission(pkg, "android.permission.WRITE_SETTINGS");
                grantPermission(pkg, "android.permission.PACKAGE_USAGE_STATS");
                grantPermission(pkg, "android.permission.DUMP");
                grantPermission(pkg, "android.permission.BATTERY_STATS");
                grantPermission(pkg, "android.permission.MANAGE_GAME_MODE");
                grantPermission(pkg, "android.permission.OVERRIDE_WIFI_CONFIG");
                grantPermission(pkg, "android.permission.CHANGE_COMPONENT_ENABLED_STATE");
                grantPermission(pkg, "android.permission.CHANGE_NETWORK_STATE");
                grantPermission(pkg, "android.permission.FORCE_STOP_PACKAGES");
                grantPermission(pkg, "android.permission.CLEAR_APP_CACHE");
                grantPermission(pkg, "android.permission.REAL_GET_TASKS");
                grantPermission(pkg, "android.permission.SET_PROCESS_LIMIT");
                grantPermission(pkg, "android.permission.ACCESS_NOTIFICATION_POLICY");
                grantPermission(pkg, "android.permission.SCHEDULE_EXACT_ALARM");
                grantPermission(pkg, "android.permission.USE_EXACT_ALARM");
                grantPermission(pkg, "android.permission.SYSTEM_ALERT_WINDOW");
                grantPermission(pkg, "android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                grantPermission(pkg, "android.permission.REQUEST_INSTALL_PACKAGES");
                grantPermission(pkg, "android.permission.REQUEST_DELETE_PACKAGES");
                grantPermission(pkg, "android.permission.WAKE_LOCK");
                grantPermission(pkg, "android.permission.VIBRATE");
                grantPermission(pkg, "android.permission.DISABLE_KEYGUARD");
                grantPermission(pkg, "android.permission.REORDER_TASKS");
                grantPermission(pkg, "android.permission.EXPAND_STATUS_BAR");
                grantPermission(pkg, "android.permission.HIGH_SAMPLING_RATE_SENSORS");
                grantPermission(pkg, "android.permission.STATUS_BAR");
                grantPermission(pkg, "android.permission.INTERACT_ACROSS_USERS");

                // 4. Complete AppOps Overrides (Scoped Storage Bypass & Unrestricted Background Execution)
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
                    setAppOp(pkg, op, "allow");
                }

                // 5. Battery Optimization / Doze Mode Bypass
                ShizukuExecutor.executeShizukuCommand("dumpsys deviceidle whitelist +" + pkg);
                ShizukuExecutor.executeShizukuCommand("cmd deviceidle whitelist +" + pkg);

                // 6. Enforce permissions across all registered game packages
                for (String gamePkg : com.gamebooster.app.games.GamePackageRegistry.getAllKnownGames().keySet()) {
                    enforceGamePermissions(gamePkg);
                }

                Log.i(TAG, "All Shizuku system & storage privileges enforced successfully!");

            } catch (Throwable t) {
                Log.e(TAG, "Error enforcing Shizuku permissions", t);
            }
        });
    }

    /**
     * Unlocks storage permissions and AppOps for target game packages to allow config file manipulation and 185 FPS unlock.
     */
    public static void enforceGamePermissions(String gamePackageName) {
        if (gamePackageName == null || gamePackageName.trim().isEmpty()) return;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                grantPermission(gamePackageName, "android.permission.READ_EXTERNAL_STORAGE");
                grantPermission(gamePackageName, "android.permission.WRITE_EXTERNAL_STORAGE");
                grantPermission(gamePackageName, "android.permission.MANAGE_EXTERNAL_STORAGE");
                grantPermission(gamePackageName, "android.permission.WRITE_SETTINGS");
                grantPermission(gamePackageName, "android.permission.SYSTEM_ALERT_WINDOW");

                setAppOp(gamePackageName, "MANAGE_EXTERNAL_STORAGE", "allow");
                setAppOp(gamePackageName, "READ_EXTERNAL_STORAGE", "allow");
                setAppOp(gamePackageName, "WRITE_EXTERNAL_STORAGE", "allow");
                setAppOp(gamePackageName, "LEGACY_STORAGE", "allow");
                setAppOp(gamePackageName, "NO_ISOLATED_STORAGE", "allow");
                setAppOp(gamePackageName, "RUN_IN_BACKGROUND", "allow");
                setAppOp(gamePackageName, "RUN_ANY_IN_BACKGROUND", "allow");
                setAppOp(gamePackageName, "AUTO_START", "allow");
                setAppOp(gamePackageName, "SYSTEM_ALERT_WINDOW", "allow");
                setAppOp(gamePackageName, "SYSTEM_EXEMPT_FROM_HIBERNATION", "allow");
                setAppOp(gamePackageName, "SYSTEM_EXEMPT_FROM_POWER_RESTRICTIONS", "allow");

                // Elevate Standby Bucket to ACTIVE on Android 12-16
                ShizukuExecutor.executeShizukuCommand("cmd usage-stats set-app-standby-bucket " + gamePackageName + " active");
                ShizukuExecutor.executeShizukuCommand("cmd deviceidle whitelist +" + gamePackageName);

                // Enable Game Mode performance intervention
                ShizukuExecutor.executeShizukuCommand("cmd game mode performance " + gamePackageName);
                ShizukuExecutor.executeShizukuCommand("cmd game set --fps 185 " + gamePackageName);
                ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate " + gamePackageName + " 185");
            } catch (Throwable ignored) {}
        });
    }

    private static void grantPermission(String pkg, String permission) {
        ShizukuExecutor.executeShizukuCommand("pm grant " + pkg + " " + permission);
    }

    private static void setAppOp(String pkg, String op, String mode) {
        ShizukuExecutor.executeShizukuCommand("cmd appops set " + pkg + " " + op + " " + mode);
    }
}
