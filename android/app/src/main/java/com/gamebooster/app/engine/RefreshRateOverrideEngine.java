package com.gamebooster.app.engine;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * RefreshRateOverrideEngine manages 60Hz, 90Hz, 120Hz, 144Hz, and 165Hz display refresh rate overrides.
 * It applies system settings and per-app refresh rate constraints via Shizuku ADB.
 */
public class RefreshRateOverrideEngine {

    private static final String TAG = "RefreshRateOverrideEngine";

    public enum RefreshRateMode {
        MODE_60HZ(60.0f, "60 Hz (Standard Battery Saver)"),
        MODE_90HZ(90.0f, "90 Hz (Smooth Performance)"),
        MODE_120HZ(120.0f, "120 Hz (Ultra Esports)"),
        MODE_144HZ(144.0f, "144 Hz (Pro Gaming)"),
        MODE_165HZ(165.0f, "165 Hz (Extreme ROG/RedMagic)");

        public final float fps;
        public final String label;

        RefreshRateMode(float fps, String label) {
            this.fps = fps;
            this.label = label;
        }
    }

    /**
     * Applies system-wide and app-specific high refresh rate overrides.
     *
     * @param context App context
     * @param packageName Target game package (or null for global setting)
     * @param mode Selected refresh rate mode (60/90/120/144/165 Hz)
     * @return true if commands executed via Shizuku
     */
    public static boolean applyRefreshRate(Context context, String packageName, RefreshRateMode mode) {
        if (!ShizukuExecutor.isShizukuAvailable()) {
            Log.w(TAG, "Shizuku ADB unavailable. Cannot set refresh rate override.");
            return false;
        }

        try {
            int rateInt = Math.round(mode.fps);
            String rateStr = String.valueOf(mode.fps);
            Log.d(TAG, "Applying Refresh Rate Override: " + mode.label + " for package: " + packageName);

            String[] commands = new String[] {
                    "settings put system peak_refresh_rate " + rateStr,
                    "settings put system user_refresh_rate " + rateInt,
                    "settings put global min_refresh_rate " + rateStr
            };

            for (String cmd : commands) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            }

            if (packageName != null && !packageName.trim().isEmpty()) {
                ShizukuExecutor.executeShizukuCommand("cmd window set-app-refresh-rate " + packageName + " " + rateInt);
                ShizukuExecutor.executeShizukuCommand("device_config put game_overlay " + packageName + " mode=2,fps=" + rateInt + ":mode=3,fps=" + rateInt);
            }

            Log.i(TAG, "Refresh Rate Override active: " + rateInt + " Hz");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply refresh rate override", e);
            return false;
        }
    }

    /**
     * Resets system refresh rate to auto/adaptive.
     */
    public static boolean resetRefreshRate() {
        if (!ShizukuExecutor.isShizukuAvailable()) return false;
        try {
            ShizukuExecutor.executeShizukuCommand("settings delete system peak_refresh_rate");
            ShizukuExecutor.executeShizukuCommand("settings delete system user_refresh_rate");
            ShizukuExecutor.executeShizukuCommand("settings delete global min_refresh_rate");
            Log.i(TAG, "Reset system refresh rate settings to default.");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to reset system refresh rate settings", e);
            return false;
        }
    }
}
