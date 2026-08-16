package com.gamebooster.app.shizuku;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;

/**
 * ShizukuPermissionEnforcer — Unlocks all Android storage, data, obb,
 * restricted settings, overlay, and system permissions via elevated Shizuku shell.
 * Compatible with Android 13, 14, 15, and 16.
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

                // 1. Core Storage Permissions
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

                // 3. AppOps: Bypass Scoped Storage & Sandbox
                setAppOp(pkg, "MANAGE_EXTERNAL_STORAGE", "allow");
                setAppOp(pkg, "READ_EXTERNAL_STORAGE", "allow");
                setAppOp(pkg, "WRITE_EXTERNAL_STORAGE", "allow");
                setAppOp(pkg, "NO_ISOLATED_STORAGE", "allow");
                setAppOp(pkg, "LEGACY_STORAGE", "allow");
                setAppOp(pkg, "ACCESS_RESTRICTED_SETTINGS", "allow");

                // 4. System Tuning & UI Control
                grantPermission(pkg, "android.permission.WRITE_SECURE_SETTINGS");
                setAppOp(pkg, "WRITE_SETTINGS", "allow");
                setAppOp(pkg, "SYSTEM_ALERT_WINDOW", "allow");
                setAppOp(pkg, "PACKAGE_USAGE_STATS", "allow");
                setAppOp(pkg, "DUMP", "allow");

                // 5. Battery Optimization Whitelist (prevents background kills)
                ShizukuExecutor.executeShizukuCommand("dumpsys deviceidle whitelist +" + pkg);
                ShizukuExecutor.executeShizukuCommand("cmd deviceidle whitelist +" + pkg);

                Log.i(TAG, "All Shizuku system & storage privileges enforced successfully!");

            } catch (Throwable t) {
                Log.e(TAG, "Error enforcing Shizuku permissions", t);
            }
        });
    }

    /**
     * Unlocks storage permissions for target game packages to allow config file manipulation.
     */
    public static void enforceGamePermissions(String gamePackageName) {
        if (gamePackageName == null || gamePackageName.trim().isEmpty()) return;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                grantPermission(gamePackageName, "android.permission.READ_EXTERNAL_STORAGE");
                grantPermission(gamePackageName, "android.permission.WRITE_EXTERNAL_STORAGE");
                grantPermission(gamePackageName, "android.permission.MANAGE_EXTERNAL_STORAGE");
                setAppOp(gamePackageName, "MANAGE_EXTERNAL_STORAGE", "allow");
                setAppOp(gamePackageName, "LEGACY_STORAGE", "allow");
                setAppOp(gamePackageName, "NO_ISOLATED_STORAGE", "allow");
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
