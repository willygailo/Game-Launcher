package com.gamebooster.app.engine;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.device.DisplayRefreshRatePreferences;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * RefreshRateOverrideEngine — applies, persists, and restores user-selected display refresh
 * rate overrides via Shizuku.
 *
 * <p>Supported Hz values are NOT assumed or hardcoded. They are queried at runtime from
 * {@link DisplayCapabilitiesDetector} which reads the physical panel's supported display
 * modes via {@code Display.getSupportedModes()}.
 *
 * <p>The user-chosen Hz is persisted in {@link DisplayRefreshRatePreferences} so it
 * survives app restarts and reboots, and is automatically re-applied on Shizuku reconnect.
 */
public class RefreshRateOverrideEngine {

    private static final String TAG = "RefreshRateOverrideEngine";

    /**
     * Applies a system-wide refresh rate override for the given Hz value via Shizuku.
     * Also persists the choice for automatic re-application on the next app start or
     * Shizuku reconnect.
     *
     * @param context     App context
     * @param packageName Target game package (or null for global setting)
     * @param targetHz    The exact Hz to set — must be supported by the physical panel
     * @return true if Shizuku commands were fired successfully
     */
    public static boolean applyRefreshRate(Context context, String packageName, int targetHz) {
        if (!ShizukuExecutor.isShizukuAvailable()) {
            Log.w(TAG, "Shizuku ADB unavailable. Cannot set refresh rate override.");
            return false;
        }

        try {
            Log.d(TAG, "Applying Refresh Rate Override: " + targetHz + "Hz for package: " + packageName);

            DisplayOverrideController.Result display =
                    DisplayOverrideController.applyDisplayRate(context, targetHz, packageName);
            if (!display.isSuccess()) {
                Log.w(TAG, "Refresh-rate request rejected: " + display.message);
                return false;
            }
            if (packageName != null && !packageName.trim().isEmpty()) {
                DisplayOverrideController.Result game =
                        DisplayOverrideController.applyGameProfile(context, packageName, targetHz);
                Log.i(TAG, "Game FPS request: " + game.message);
            }
            if (context != null) DisplayRefreshRatePreferences.saveSelectedHz(context, display.selectedHz);
            Log.i(TAG, "RefreshRateOverrideEngine: " + display.message);
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply refresh rate override", e);
            return false;
        }
    }

    /**
     * Convenience method: force max refresh rate globally without per-package constraints.
     *
     * @param targetHz Target Hz value (any rate supported by the physical panel)
     * @return true if Shizuku commands fired successfully
     */
    public static boolean applyMaxRefreshRateForce(int targetHz) {
        return applyRefreshRate(null, null, targetHz);
    }

    /**
     * Re-applies the user-saved refresh rate override (if any) via Shizuku.
     * Call this on app startup or whenever Shizuku reconnects.
     *
     * @param context App context
     * @return true if an override was persisted and successfully re-applied
     */
    public static boolean restorePersistedRefreshRate(Context context) {
        if (context == null) return false;
        int savedHz = DisplayRefreshRatePreferences.getSelectedHz(context);
        if (savedHz <= 0) {
            Log.d(TAG, "No persisted refresh rate override to restore.");
            return false;
        }
        Log.i(TAG, "Restoring persisted refresh rate: " + savedHz + "Hz");
        return applyRefreshRate(context, null, savedHz);
    }

    /**
     * Resets system refresh rate to auto/adaptive and clears the persisted override.
     */
    public static boolean resetRefreshRate(Context context) {
        try {
            DisplayOverrideController.Result restore = DisplayOverrideController.restore(context);
            if (context != null) {
                DisplayRefreshRatePreferences.clearOverride(context);
            }
            Log.i(TAG, restore.message);
            return restore.isSuccess();
        } catch (Throwable e) {
            Log.e(TAG, "Failed to reset system refresh rate settings", e);
            return false;
        }
    }
}
