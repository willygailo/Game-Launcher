package com.gamebooster.app.device;

import android.app.Activity;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * HardwareDisplayController — Handles display modes enumeration and forces hardware refresh rates (60Hz -> 165Hz+).
 *
 * Implements:
 *  - DisplayManager.getSupportedModes() enumeration
 *  - Activity preferredDisplayModeId injection
 *  - Surface.setFrameRate() and Surface.setFrameRateCategory()
 *  - Shizuku privileged settings overrides: peak_refresh_rate, min_refresh_rate, user_refresh_rate
 */
public final class HardwareDisplayController {

    private static final String TAG = "HardwareDisplayCtrl";

    private HardwareDisplayController() {}

    /**
     * Finds the maximum physical refresh rate supported by the default display hardware.
     */
    public static float getMaxHardwareRefreshRate(Context context) {
        if (context == null) return 60f;
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        if (dm == null) return 60f;
        Display display = dm.getDisplay(Display.DEFAULT_DISPLAY);
        if (display == null) return 60f;
        Display.Mode[] modes = display.getSupportedModes();
        if (modes == null) return 60f;

        float maxRate = 60f;
        for (Display.Mode mode : modes) {
            if (mode != null && mode.getRefreshRate() > maxRate) {
                maxRate = mode.getRefreshRate();
            }
        }
        return maxRate;
    }

    /**
     * Returns the Display.Mode with the highest refresh rate.
     */
    public static Display.Mode getMaxHardwareMode(Context context) {
        if (context == null) return null;
        DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        if (dm == null) return null;
        Display display = dm.getDisplay(Display.DEFAULT_DISPLAY);
        if (display == null) return null;
        Display.Mode[] modes = display.getSupportedModes();
        if (modes == null) return null;

        Display.Mode maxMode = null;
        for (Display.Mode mode : modes) {
            if (mode != null) {
                if (maxMode == null || mode.getRefreshRate() > maxMode.getRefreshRate()) {
                    maxMode = mode;
                }
            }
        }
        return maxMode;
    }

    /**
     * Injects the preferred display mode into the window attributes to enforce max Hz on this Activity.
     */
    public static void applyMaxRefreshRateToWindow(Activity activity) {
        if (activity == null) return;
        try {
            Display.Mode maxMode = getMaxHardwareMode(activity);
            if (maxMode == null) return;
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            params.preferredDisplayModeId = maxMode.getModeId();
            activity.getWindow().setAttributes(params);
            Log.i(TAG, "Window preferred display mode set to modeId=" + maxMode.getModeId() + " (" + maxMode.getRefreshRate() + "Hz)");
        } catch (Throwable t) {
            Log.w(TAG, "Failed to apply preferred display mode to window: " + t.getMessage());
        }
    }

    /**
     * Applies frame rate hint directly to a Surface instance (Android 13+).
     */
    public static void applySurfaceFrameRate(Surface surface, float targetFps) {
        if (surface == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                surface.setFrameRate(
                        targetFps,
                        Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE,
                        Surface.CHANGE_FRAME_RATE_ALWAYS
                );
                Log.d(TAG, "Surface frame rate hint applied: " + targetFps + "fps");
            } catch (Throwable t) {
                Log.w(TAG, "Failed to set Surface frame rate: " + t.getMessage());
            }
        }

        if (Build.VERSION.SDK_INT >= 34) {
            try {
                // API 34 FRAME_RATE_CATEGORY_HIGH = 3
                Method method = surface.getClass().getMethod("setFrameRateCategory", int.class);
                method.invoke(surface, 3);
                Log.d(TAG, "Surface FRAME_RATE_CATEGORY_HIGH applied via reflection");
            } catch (Throwable t) {
                Log.d(TAG, "setFrameRateCategory not available on this platform");
            }
        }
    }

    /**
     * Force-locks the system display refresh rate to the hardware maximum using Shizuku privileged shell.
     * Prevents OEM adaptive refresh stepping down to 60Hz during gaming.
     */
    public static boolean forceUnlockSystemMaxRefreshRate(Context context) {
        if (context == null) return false;
        int maxHz = Math.round(getMaxHardwareRefreshRate(context));
        Log.i(TAG, "Hardware ceiling detected: " + maxHz + "Hz. Pushing system overrides via Shizuku...");

        List<String> commands = Arrays.asList(
                "settings put system peak_refresh_rate " + maxHz,
                "settings put system min_refresh_rate " + maxHz,
                "settings put system user_refresh_rate " + maxHz,
                "settings put global low_power 0",
                "settings put global low_power_sticky 0",
                "settings put global adaptive_battery_management_enabled 0"
        );

        boolean allSuccess = true;
        for (String cmd : commands) {
            String res = ShizukuExecutor.executeShizukuCommand(cmd);
            if (res == null || res.startsWith("ERROR")) {
                allSuccess = false;
                Log.w(TAG, "Command warning: " + cmd + " (" + res + ")");
            }
        }
        return allSuccess;
    }
}
