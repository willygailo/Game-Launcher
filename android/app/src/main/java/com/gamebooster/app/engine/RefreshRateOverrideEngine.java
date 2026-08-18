package com.gamebooster.app.engine;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * RefreshRateOverrideEngine manages 60Hz, 90Hz, 120Hz, 144Hz, and 165Hz display refresh rate overrides.
 * It applies system settings and per-app refresh rate constraints via Shizuku ADB.
 */
public class RefreshRateOverrideEngine {

    private static final String TAG = "RefreshRateOverrideEngine";

    public enum RefreshRateMode {
        MODE_185HZ(185.0f, "185 Hz / 185 FPS (Extreme Max)");

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
     * @param mode Selected refresh rate mode (185 Hz)
     * @return true if commands executed via Shizuku
     */
    public static boolean applyRefreshRate(Context context, String packageName, RefreshRateMode mode) {
        if (!ShizukuExecutor.isShizukuAvailable()) {
            Log.w(TAG, "Shizuku ADB unavailable. Cannot set refresh rate override.");
            return false;
        }

        try {
            int rateInt = Math.round(mode != null ? mode.fps : 185.0f);
            Log.d(TAG, "Applying Refresh Rate Override: 185Hz for package: " + packageName);

            // Delegate global system forcing to MaxHzForceChannel (17+ commands, 6 layers)
            MaxHzForceChannel.ForceResult forceResult = MaxHzForceChannel.forceApply(rateInt);

            // Apply per-package constraints if a specific game package was provided
            if (packageName != null && !packageName.trim().isEmpty()) {
                ShizukuExecutor.executeShizukuCommand(
                    "cmd window set-app-refresh-rate " + packageName + " " + rateInt);
                ShizukuExecutor.executeShizukuCommand(
                    "device_config put game_overlay " + packageName
                    + " mode=2,fps=" + rateInt + ":mode=3,fps=" + rateInt);
                ShizukuExecutor.executeShizukuCommand(
                    "cmd game set --fps " + rateInt + " " + packageName);
            }

            Log.i(TAG, "RefreshRateOverrideEngine: " + forceResult.message);
            return forceResult.success;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply refresh rate override", e);
            return false;
        }
    }

    /**
     * Convenience method: force max refresh rate globally without per-package constraints.
     *
     * @param targetHz Target Hz value (hard-locked to 185)
     * @return true if Shizuku commands fired successfully
     */
    public static boolean applyMaxRefreshRateForce(int targetHz) {
        if (!ShizukuExecutor.hasShizukuPermission()) return false;
        RefreshRateMode mode = RefreshRateMode.MODE_185HZ;
        return applyRefreshRate(null, null, mode);
    }

    /**
     * Resets system refresh rate to auto/adaptive.
     */
    public static boolean resetRefreshRate() {
        if (!ShizukuExecutor.isShizukuAvailable()) return false;
        try {
            ShizukuExecutor.executeShizukuCommand("settings delete system peak_refresh_rate");
            ShizukuExecutor.executeShizukuCommand("settings delete system min_refresh_rate");
            ShizukuExecutor.executeShizukuCommand("settings delete system user_refresh_rate");
            ShizukuExecutor.executeShizukuCommand("settings delete global peak_refresh_rate");
            ShizukuExecutor.executeShizukuCommand("settings delete global min_refresh_rate");
            Log.i(TAG, "Reset system refresh rate settings to default.");
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to reset system refresh rate settings", e);
            return false;
        }
    }
}
