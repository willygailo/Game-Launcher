package com.gamebooster.app.engine;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * PermissionBatchBuilder — Single source of truth for all Shizuku ADB permission grant commands.
 *
 * <p>Eliminates the duplicate grant batch that existed in both {@code ShizukuExecutor} and
 * {@code ShizukuForceApplyEngine}. Both callers now delegate here, guaranteeing consistency.
 *
 * <p>Grants issued via Shizuku (uid 2000 = ADB shell) are persistent across reboots and cannot
 * be revoked without another ADB-level command. This is legal and standard behavior for ADB
 * shell privilege elevation on unrooted developer devices.
 */
public class PermissionBatchBuilder {

    private static final String TAG = "PermBatchBuilder";

    // -----------------------------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------------------------

    /**
     * Builds the full pm grant + appops batch for the given package.
     *
     * <p>Includes:
     * <ul>
     *   <li>All standard privileged pm grants for system/performance features</li>
     *   <li>AppOps unrestricted background/start overrides</li>
     * </ul>
     *
     * @param packageName The app package to grant permissions to
     * @return Ordered list of shell commands ready for Shizuku batch execution
     */
    public static List<String> buildGrantBatch(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Log.e(TAG, "buildGrantBatch: null/empty packageName");
            return new ArrayList<>();
        }

        List<String> batch = new ArrayList<>();

        // --- PM GRANTS — System Privileged Permissions ---
        batch.add("pm grant " + packageName + " android.permission.WRITE_SECURE_SETTINGS");
        batch.add("pm grant " + packageName + " android.permission.WRITE_SETTINGS");
        batch.add("pm grant " + packageName + " android.permission.PACKAGE_USAGE_STATS");
        batch.add("pm grant " + packageName + " android.permission.MANAGE_EXTERNAL_STORAGE");
        batch.add("pm grant " + packageName + " android.permission.READ_EXTERNAL_STORAGE");
        batch.add("pm grant " + packageName + " android.permission.WRITE_EXTERNAL_STORAGE");
        batch.add("pm grant " + packageName + " android.permission.ACCESS_NOTIFICATION_POLICY");
        batch.add("pm grant " + packageName + " android.permission.DUMP");
        batch.add("pm grant " + packageName + " android.permission.BATTERY_STATS");
        batch.add("pm grant " + packageName + " android.permission.MANAGE_GAME_MODE");
        batch.add("pm grant " + packageName + " android.permission.OVERRIDE_WIFI_CONFIG");
        batch.add("pm grant " + packageName + " android.permission.CHANGE_COMPONENT_ENABLED_STATE");
        batch.add("pm grant " + packageName + " android.permission.CHANGE_NETWORK_STATE");
        batch.add("pm grant " + packageName + " android.permission.FORCE_STOP_PACKAGES");
        batch.add("pm grant " + packageName + " android.permission.CLEAR_APP_CACHE");
        batch.add("pm grant " + packageName + " android.permission.REAL_GET_TASKS");
        batch.add("pm grant " + packageName + " android.permission.SET_PROCESS_LIMIT");
        batch.add("pm grant " + packageName + " android.permission.MODIFY_PHONE_STATE");
        batch.add("pm grant " + packageName + " android.permission.READ_PRIVILEGED_PHONE_STATE"); // Bug fix: was missing
        batch.add("pm grant " + packageName + " android.permission.HARDWARE_TEST");
        batch.add("pm grant " + packageName + " android.permission.INTERNET");
        batch.add("pm grant " + packageName + " android.permission.SYSTEM_ALERT_WINDOW");
        batch.add("pm grant " + packageName + " android.permission.SCHEDULE_EXACT_ALARM");
        batch.add("pm grant " + packageName + " android.permission.USE_EXACT_ALARM");

        // --- APPOPS OVERRIDES — Unrestricted System & Background Access ---
        batch.add("cmd appops set " + packageName + " MANAGE_EXTERNAL_STORAGE allow");
        batch.add("cmd appops set " + packageName + " SYSTEM_ALERT_WINDOW allow");
        batch.add("cmd appops set " + packageName + " GET_USAGE_STATS allow");
        batch.add("cmd appops set " + packageName + " WRITE_SETTINGS allow");
        batch.add("cmd appops set " + packageName + " MANAGE_GAME_MODE allow");
        batch.add("cmd appops set " + packageName + " RUN_IN_BACKGROUND allow");
        batch.add("cmd appops set " + packageName + " RUN_ANY_IN_BACKGROUND allow");
        batch.add("cmd appops set " + packageName + " AUTO_START allow");
        batch.add("cmd appops set " + packageName + " TURN_SCREEN_ON allow");
        batch.add("cmd appops set " + packageName + " PROJECT_MEDIA allow");
        batch.add("cmd appops set " + packageName + " ACCESS_RESTRICTED_SETTINGS allow");
        batch.add("cmd appops set " + packageName + " NO_ISOLATED_STORAGE allow");
        batch.add("cmd appops set " + packageName + " SCHEDULE_EXACT_ALARM allow");

        Log.d(TAG, "buildGrantBatch: " + batch.size() + " commands for " + packageName);
        return batch;
    }

    /**
     * Builds a minimal per-game batch for performance mode and background operation.
     * Applied to each target game package (not the app itself).
     *
     * @param gamePkg  The target game package name
     * @param targetHz The max Hz to configure for this game
     * @return List of commands for this game
     */
    public static List<String> buildPerGameBatch(String gamePkg, int targetHz) {
        if (gamePkg == null || gamePkg.isEmpty()) return new ArrayList<>();

        List<String> b = new ArrayList<>();
        b.add("cmd game mode performance " + gamePkg);
        b.add("cmd game set --fps " + targetHz + " " + gamePkg);
        b.add("cmd window set-app-refresh-rate " + gamePkg + " " + targetHz);
        b.add("device_config put game_overlay " + gamePkg
                + " mode=2,fps=" + targetHz + ":mode=3,fps=" + targetHz);

        // Android 14+ also uses game_manager namespace
        b.add("device_config put game_manager " + gamePkg
                + " mode=2,fps=" + targetHz + ":mode=3,fps=" + targetHz);

        b.add("cmd appops set " + gamePkg + " RUN_IN_BACKGROUND allow");
        b.add("cmd appops set " + gamePkg + " RUN_ANY_IN_BACKGROUND allow");
        b.add("cmd appops set " + gamePkg + " AUTO_START allow");
        b.add("cmd appops set " + gamePkg + " SYSTEM_ALERT_WINDOW allow");
        b.add("pm grant " + gamePkg + " android.permission.WRITE_SETTINGS");
        b.add("pm grant " + gamePkg + " android.permission.MANAGE_EXTERNAL_STORAGE");
        return b;
    }
}
