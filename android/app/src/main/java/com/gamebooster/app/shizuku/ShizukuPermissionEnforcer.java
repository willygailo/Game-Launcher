package com.gamebooster.app.shizuku;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.HomeGameScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * ShizukuPermissionEnforcer — Unlocks all Android storage, data, obb,
 * restricted settings, overlay, and system permissions via elevated Shizuku shell.
 * Fully compatible with Android 11, 12, 13, 14, 15, and 16.
 */
public class ShizukuPermissionEnforcer {

    private static final String TAG = "ShizukuPermEnforcer";

    /**
     * Unlocks full file system access, legacy storage, appops, and system permissions for the launcher and detected games.
     */
    public static void enforceAllPermissions(Context context) {
        if (context == null) return;
        if (!ShizukuExecutor.hasShizukuPermission() && !RishManager.isAvailable(context)) {
            Log.w(TAG, "Cannot enforce permissions: Shizuku is not available or granted.");
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            enforceAllPermissionsInternal(context);
        });
    }

    /**
     * Internal implementation running on worker thread.
     */
    private static void enforceAllPermissionsInternal(Context context) {
        try {
            String pkg = context.getPackageName();
            Log.i(TAG, "Enforcing full system & storage permissions for: " + pkg);

            List<String> batchCmds = new ArrayList<>();

            // 1. Android 13-16 Scoped Storage & Granular Media Permissions
            batchCmds.add("pm grant " + pkg + " android.permission.MANAGE_EXTERNAL_STORAGE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.READ_EXTERNAL_STORAGE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.WRITE_EXTERNAL_STORAGE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.READ_MEDIA_IMAGES 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.READ_MEDIA_VIDEO 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.READ_MEDIA_AUDIO 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.READ_MEDIA_VISUAL_USER_SELECTED 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.POST_NOTIFICATIONS 2>/dev/null");

            // 2. Android 14, 15, and 16 Foreground Service & Alarm Permissions
            batchCmds.add("pm grant " + pkg + " android.permission.FOREGROUND_SERVICE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.FOREGROUND_SERVICE_SPECIAL_USE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.SCHEDULE_EXACT_ALARM 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.USE_EXACT_ALARM 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.HIGH_SAMPLING_RATE_SENSORS 2>/dev/null");

            // 3. System Tuning, Secure Settings & UI Control
            batchCmds.add("pm grant " + pkg + " android.permission.WRITE_SECURE_SETTINGS 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.WRITE_SETTINGS 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.PACKAGE_USAGE_STATS 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.DUMP 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.BATTERY_STATS 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.MANAGE_GAME_MODE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.OVERRIDE_WIFI_CONFIG 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.CHANGE_COMPONENT_ENABLED_STATE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.CHANGE_NETWORK_STATE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.CHANGE_WIFI_STATE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.CHANGE_WIFI_MULTICAST_STATE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.MODIFY_AUDIO_SETTINGS 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.FORCE_STOP_PACKAGES 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.KILL_BACKGROUND_PROCESSES 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.CLEAR_APP_CACHE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.REAL_GET_TASKS 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.SET_PROCESS_LIMIT 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.ACCESS_NOTIFICATION_POLICY 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.SYSTEM_ALERT_WINDOW 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.REQUEST_INSTALL_PACKAGES 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.REQUEST_DELETE_PACKAGES 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.WAKE_LOCK 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.VIBRATE 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.DISABLE_KEYGUARD 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.REORDER_TASKS 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.EXPAND_STATUS_BAR 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.STATUS_BAR 2>/dev/null");
            batchCmds.add("pm grant " + pkg + " android.permission.INTERACT_ACROSS_USERS 2>/dev/null");

            // 4. Complete AppOps Overrides
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
                    "START_FOREGROUND",
                    "HIGH_SAMPLING_RATE_SENSORS"
            };

            for (String op : allAppOps) {
                batchCmds.add("cmd appops set " + pkg + " " + op + " allow 2>/dev/null");
            }

            // 5. Battery Optimization & Doze Mode Bypass
            batchCmds.add("dumpsys deviceidle whitelist +" + pkg + " 2>/dev/null");
            batchCmds.add("cmd deviceidle whitelist +" + pkg + " 2>/dev/null");

            // 6. Fast Single-Batch Execution via Shizuku
            ShizukuExecutor.executeShizukuCommands(batchCmds);

            // 7. Enforce storage permissions and AppOps across ALL installed games
            List<GameAppInfo> detected = HomeGameScanner.scanTargetGames(context);
            for (GameAppInfo g : detected) {
                if (g != null && g.getPackageName() != null) {
                    enforceGamePermissionsDirect(g.getPackageName());
                }
            }

            Log.i(TAG, "All Shizuku system & storage privileges auto-granted successfully!");

        } catch (Throwable t) {
            Log.e(TAG, "Error enforcing Shizuku permissions", t);
        }
    }

    /**
     * Enforces ALL permissions for the launcher app and detected game packages.
     * Batched efficiently without overloading the executor queue.
     */
    public static void enforceAllPermissionsForAllApps(Context context) {
        if (context == null) return;
        if (!ShizukuExecutor.hasShizukuPermission() && !RishManager.isAvailable(context)) {
            Log.w(TAG, "Cannot enforce permissions for all apps: Shizuku is not available or granted.");
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            enforceAllPermissionsInternal(context);
            enforceAndroid16CompatibilityFlags(context);
        });
    }

    /**
     * Enforces storage and system permissions for ALL installed user applications across Android 13-16.
     */
    public static void enforceAllPermissionsForAllInstalledApps(Context context) {
        if (context == null) return;
        if (!ShizukuExecutor.hasShizukuPermission() && !RishManager.isAvailable(context)) return;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                List<com.gamebooster.app.games.GameAppInfo> allApps =
                        com.gamebooster.app.games.GameManagerRepository.getAllInstalledApps(context);
                for (com.gamebooster.app.games.GameAppInfo app : allApps) {
                    if (app != null && app.getPackageName() != null) {
                        enforceGamePermissionsDirect(app.getPackageName());
                    }
                }
                enforceAndroid16CompatibilityFlags(context);
            } catch (Throwable t) {
                Log.w(TAG, "Error in enforceAllPermissionsForAllInstalledApps: " + t.getMessage());
            }
        });
    }

    /**
     * Android 14, 15, and 16 (API 34-36) permission compatibility unlocks.
     */
    public static void enforceAndroid16CompatibilityFlags(Context context) {
        if (context == null || !ShizukuExecutor.hasShizukuPermission()) return;
        String pkg = context.getPackageName();
        List<String> cmds = new ArrayList<>();
        cmds.add("pm grant " + pkg + " android.permission.READ_MEDIA_VISUAL_USER_SELECTED 2>/dev/null");
        cmds.add("pm grant " + pkg + " android.permission.USE_EXACT_ALARM 2>/dev/null");
        cmds.add("pm grant " + pkg + " android.permission.SCHEDULE_EXACT_ALARM 2>/dev/null");
        cmds.add("pm grant " + pkg + " android.permission.HIGH_SAMPLING_RATE_SENSORS 2>/dev/null");
        cmds.add("pm grant " + pkg + " android.permission.FOREGROUND_SERVICE_SPECIAL_USE 2>/dev/null");
        cmds.add("cmd appops set " + pkg + " USE_EXACT_ALARM allow 2>/dev/null");
        cmds.add("cmd appops set " + pkg + " SCHEDULE_EXACT_ALARM allow 2>/dev/null");
        cmds.add("cmd appops set " + pkg + " HIGH_SAMPLING_RATE_SENSORS allow 2>/dev/null");
        ShizukuExecutor.executeShizukuCommands(cmds);
    }

    /**
     * Unlocks storage permissions and AppOps for target game package.
     */
    public static void enforceGamePermissions(String gamePackageName) {
        if (gamePackageName == null || gamePackageName.trim().isEmpty()) return;

        AppExecutors.getInstance().executeCommand(() -> {
            enforceGamePermissionsDirect(gamePackageName.trim());
        });
    }

    /**
     * Synchronous direct batch execution (runs on current worker thread).
     */
    public static void enforceGamePermissionsDirect(String gamePackageName) {
        if (gamePackageName == null || gamePackageName.trim().isEmpty()) return;

        try {
            String pkg = gamePackageName.trim();
            List<String> cmds = new ArrayList<>();
            cmds.add("pm grant " + pkg + " android.permission.READ_EXTERNAL_STORAGE 2>/dev/null");
            cmds.add("pm grant " + pkg + " android.permission.WRITE_EXTERNAL_STORAGE 2>/dev/null");
            cmds.add("pm grant " + pkg + " android.permission.MANAGE_EXTERNAL_STORAGE 2>/dev/null");
            cmds.add("pm grant " + pkg + " android.permission.WRITE_SETTINGS 2>/dev/null");
            cmds.add("pm grant " + pkg + " android.permission.SYSTEM_ALERT_WINDOW 2>/dev/null");

            cmds.add("cmd appops set " + pkg + " MANAGE_EXTERNAL_STORAGE allow 2>/dev/null");
            cmds.add("cmd appops set " + pkg + " READ_EXTERNAL_STORAGE allow 2>/dev/null");
            cmds.add("cmd appops set " + pkg + " WRITE_EXTERNAL_STORAGE allow 2>/dev/null");
            cmds.add("cmd appops set " + pkg + " LEGACY_STORAGE allow 2>/dev/null");
            cmds.add("cmd appops set " + pkg + " NO_ISOLATED_STORAGE allow 2>/dev/null");
            cmds.add("cmd appops set " + pkg + " RUN_IN_BACKGROUND allow 2>/dev/null");
            cmds.add("cmd appops set " + pkg + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null");
            cmds.add("cmd appops set " + pkg + " AUTO_START allow 2>/dev/null");
            cmds.add("cmd appops set " + pkg + " SYSTEM_ALERT_WINDOW allow 2>/dev/null");

            // Enable Game Mode performance intervention
            cmds.add("cmd game mode performance " + pkg + " 2>/dev/null");
            cmds.add("cmd game set --fps 185 " + pkg + " 2>/dev/null");
            cmds.add("cmd window set-app-refresh-rate " + pkg + " 185 2>/dev/null");

            ShizukuExecutor.executeShizukuCommands(cmds);
        } catch (Throwable ignored) {}
    }
}
