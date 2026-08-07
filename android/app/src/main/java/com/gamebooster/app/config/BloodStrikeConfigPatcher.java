package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * BloodStrikeConfigPatcher — Config patcher & 120 FPS unlocker for Blood Strike.
 *
 * Target package: `com.payless.hk` / `com.dts.freefireth`
 */
public class BloodStrikeConfigPatcher {

    private static final String TAG = "BloodStrikeConfigPatcher";
    public static final String PACKAGE_BLOOD_STRIKE = "com.payless.hk";

    public static boolean patchConfig(Context context, String packageName) {
        String targetPkg = (packageName != null && !packageName.isEmpty()) ? packageName : PACKAGE_BLOOD_STRIKE;
        Log.i(TAG, "▶ Patching Blood Strike config for 120 FPS on " + targetPkg);

        if (!ShizukuExecutor.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission not available for Blood Strike config patcher");
            return false;
        }

        try {
            // Apply Android Game Mode performance rules
            ShizukuExecutor.executeShizukuCommand("cmd game mode performance " + targetPkg);
            ShizukuExecutor.executeShizukuCommand("cmd game set --fps 120 " + targetPkg);
            ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate " + targetPkg + " 120");

            // Hardware GL Driver opt-in
            ShizukuExecutor.executeShizukuCommand("settings put global game_driver_opt_in_apps " + targetPkg);

            Log.i(TAG, "✔ Blood Strike 120 FPS configuration applied successfully");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Error applying Blood Strike config patch", e);
            return false;
        }
    }
}
