package com.gamebooster.app.device;

import android.app.Activity;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Reads Android-reported display modes and applies display preferences only
 * where the user has granted Modify system settings. It never claims to change
 * a separate game's frame cap or to create a mode the panel does not expose.
 */
public final class HardwareDisplayController {

    private static final String TAG = "HardwareDisplayCtrl";

    private HardwareDisplayController() {}

    public static float getMaxHardwareRefreshRate(Context context) {
        Display.Mode mode = getMaxHardwareMode(context);
        return mode != null ? mode.getRefreshRate() : 0f;
    }

    public static Display.Mode getMaxHardwareMode(Context context) {
        if (context == null) return null;
        DisplayManager manager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display display = manager != null ? manager.getDisplay(Display.DEFAULT_DISPLAY) : null;
        if (display == null) return null;

        Display.Mode best = null;
        for (Display.Mode mode : display.getSupportedModes()) {
            if (mode != null && (best == null || mode.getRefreshRate() > best.getRefreshRate())) {
                best = mode;
            }
        }
        return best;
    }

    /** Returns a physical display mode reported by Android, or zero if unavailable. */
    public static int resolveSupportedRefreshRate(Context context, int requestedHz) {
        if (context == null) return 0;
        DisplayManager manager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        Display display = manager != null ? manager.getDisplay(Display.DEFAULT_DISPLAY) : null;
        if (display == null) return 0;

        ArrayList<Integer> rates = new ArrayList<>();
        for (Display.Mode mode : display.getSupportedModes()) {
            if (mode != null) rates.add(Math.round(mode.getRefreshRate()));
        }
        return RefreshRatePolicy.resolveRate(rates, requestedHz);
    }

    /** Applies the highest physical display mode to this app's own window. */
    public static void applyMaxRefreshRateToWindow(Activity activity) {
        if (activity == null) return;
        try {
            Display.Mode maxMode = getMaxHardwareMode(activity);
            if (maxMode == null) return;
            WindowManager.LayoutParams parameters = activity.getWindow().getAttributes();
            parameters.preferredDisplayModeId = maxMode.getModeId();
            activity.getWindow().setAttributes(parameters);
            Log.i(TAG, "Launcher window prefers " + maxMode.getRefreshRate() + "Hz");
        } catch (Throwable t) {
            Log.w(TAG, "Unable to set launcher display preference", t);
        }
    }

    /** Applies a frame-rate hint to a Surface owned by this app. */
    public static void applySurfaceFrameRate(Surface surface, float targetFps) {
        if (surface == null || targetFps <= 0) return;
        try {
            surface.setFrameRate(targetFps, Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE,
                    Surface.CHANGE_FRAME_RATE_ONLY_IF_SEAMLESS);
        } catch (Throwable t) {
            Log.d(TAG, "Surface frame-rate hint unavailable: " + t.getMessage());
        }

        if (Build.VERSION.SDK_INT >= 34) {
            try {
                Method method = Surface.class.getMethod("setFrameRateCategory", int.class);
                method.invoke(surface, 3 /* FRAME_RATE_CATEGORY_HIGH */);
            } catch (Throwable ignored) {
                // Category hints are optional and device-dependent.
            }
        }
    }

    /**
     * Requests a system display preference using public Settings APIs. The user
     * must have explicitly granted Modify system settings. Returns false when
     * the permission or requested physical mode is unavailable.
     */
    public static boolean forceUnlockSystemRefreshRate(Context context, int requestedHz) {
        int targetHz = resolveSupportedRefreshRate(context, requestedHz);
        if (context == null || targetHz <= 0 || !Settings.System.canWrite(context)) return false;
        try {
            boolean peak = Settings.System.putFloat(context.getContentResolver(), "peak_refresh_rate", targetHz);
            boolean min = Settings.System.putFloat(context.getContentResolver(), "min_refresh_rate", targetHz);
            Log.i(TAG, "Requested supported system display preference: " + targetHz + "Hz");
            return peak || min;
        } catch (SecurityException e) {
            Log.w(TAG, "Modify system settings is not granted", e);
            return false;
        }
    }

    public static boolean forceUnlockSystemMaxRefreshRate(Context context) {
        return forceUnlockSystemRefreshRate(context, 0);
    }

    /** Restores the public refresh-rate preferences this app may have set. */
    public static boolean restoreAdaptiveRefreshRate(Context context) {
        if (context == null || !Settings.System.canWrite(context)) return false;
        return Settings.System.putString(context.getContentResolver(), "min_refresh_rate", null)
                | Settings.System.putString(context.getContentResolver(), "peak_refresh_rate", null);
    }
}
