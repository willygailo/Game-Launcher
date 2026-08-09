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

    public static boolean patchCompetitive(String packageName, int targetFps) {
        if (packageName == null) return false;
        Log.i(TAG, "▶ Patching Honor of Kings / AoV config for " + targetFps + " FPS on " + packageName);

        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand("cmd game mode performance " + packageName);
                ShizukuExecutor.executeShizukuCommand("cmd game set --fps " + targetFps + " " + packageName);
                ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate " + packageName + " " + targetFps);
                ShizukuExecutor.executeShizukuCommand("settings put global game_driver_opt_in_apps " + packageName);
            }
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Error applying HoK config patch", e);
            return false;
        }
    }
}
