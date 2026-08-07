package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * HonorOfKingsConfigPatcher — Config patcher & 120 FPS unlocker for Honor of Kings (HoK).
 *
 * Target package: `com.levelinfinite.hok.global` / `com.tencent.tmgp.sgame`
 */
public class HonorOfKingsConfigPatcher {

    private static final String TAG = "HonorOfKingsConfigPatcher";
    public static final String PACKAGE_GLOBAL = "com.levelinfinite.hok.global";
    public static final String PACKAGE_CN = "com.tencent.tmgp.sgame";

    public static boolean patchConfig(Context context, String packageName) {
        String targetPkg = (packageName != null && !packageName.isEmpty()) ? packageName : PACKAGE_GLOBAL;
        Log.i(TAG, "▶ Patching Honor of Kings config for 120 FPS / Extreme Graphics on " + targetPkg);

        if (!ShizukuExecutor.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission not available for HoK config patcher");
            return false;
        }

        try {
            // Apply Android Game Mode performance rules
            ShizukuExecutor.executeShizukuCommand("cmd game mode performance " + targetPkg);
            ShizukuExecutor.executeShizukuCommand("cmd game set --fps 120 " + targetPkg);
            ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate " + targetPkg + " 120");

            // Hardware GL Driver opt-in
            ShizukuExecutor.executeShizukuCommand("settings put global game_driver_opt_in_apps " + targetPkg);

            Log.i(TAG, "✔ Honor of Kings 120 FPS configuration applied successfully");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Error applying HoK config patch", e);
            return false;
        }
    }
}
