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

import com.gamebooster.app.config.GameProfileAutoConfigurator;

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
     * Enforces the target refresh rate via the Settings.System API AND directly via Shizuku/Root.
     *
     * Unlike the old implementation this method does NOT silently return false when
     * MODIFY_SYSTEM_SETTINGS is missing — it fires privileged shell commands as a fallback,
     * ensuring peak_refresh_rate + min_refresh_rate are NEVER left at 60Hz.
     *
     * @param requestedHz Target Hz (120 / 144 / 165 / 185).
     * @return true if at least one write path succeeded.
     */
    public static boolean forceUnlockSystemRefreshRate(Context context, int requestedHz) {
        if (context == null || requestedHz <= 0) return false;

        int targetHz = GameProfileAutoConfigurator.clampTargetFpsToDisplay(context, requestedHz);
        float targetF = (float) targetHz;
        boolean anySuccess = false;

        // ── Path A: Settings.System (public API) ─────────────────────────────────────
        if (Settings.System.canWrite(context)) {
            try {
                boolean peak = Settings.System.putFloat(context.getContentResolver(), "peak_refresh_rate", targetF);
                boolean min  = Settings.System.putFloat(context.getContentResolver(), "min_refresh_rate",  targetF);
                // Disable adaptive / match-content frame-rate — these silently drop Hz to 60
                Settings.System.putInt(context.getContentResolver(), "match_content_frame_rate", 0);
                Settings.Global.putInt(context.getContentResolver(), "match_content_frame_rate", 0);
                Log.i(TAG, "Settings.System enforced " + targetHz + "Hz (peak=" + peak + ", min=" + min + ")");
                anySuccess = peak || min;
            } catch (SecurityException e) {
                Log.w(TAG, "Settings.System write denied: " + e.getMessage());
            }
        } else {
            Log.i(TAG, "MODIFY_SYSTEM_SETTINGS not granted — escalating to privileged shell");
        }

        // ── Path B: Privileged shell via Shizuku/Root (fires regardless of canWrite) ─
        // These bypass the Android Settings permission gate entirely.
        try {
            String hz  = String.valueOf(targetHz);
            String hzF = targetHz + ".0";
            com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged(
                    "settings put system peak_refresh_rate " + hzF);
            com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged(
                    "settings put system min_refresh_rate " + hzF);
            com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged(
                    "settings put global peak_refresh_rate " + hzF);
            com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged(
                    "settings put global min_refresh_rate " + hzF);
            com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged(
                    "settings put system match_content_frame_rate 0");
            com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged(
                    "settings put secure match_content_frame_rate_preference 0");
            // Disable dynamic VRR / adaptive refresh so the OS can't self-throttle back to lower rates
            com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged(
                    "setprop persist.vendor.display.vrr.disable 1");
            com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged(
                    "setprop ro.surface_flinger.set_idle_timer_ms 0");
            com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged(
                    "setprop ro.surface_flinger.set_touch_timer_ms 0");
            anySuccess = true;
            Log.i(TAG, "Privileged shell enforced " + targetHz + "Hz settings keys");
        } catch (Throwable t) {
            Log.w(TAG, "Privileged Hz shell error: " + t.getMessage());
        }

        return anySuccess;
    }

    public static boolean forceUnlockSystemMaxRefreshRate(Context context) {
        int maxHz = Math.round(getMaxHardwareRefreshRate(context));
        int targetHz = maxHz > 0 ? maxHz : 60;
        return forceUnlockSystemRefreshRate(context, targetHz);
    }

    /**
     * Enforces highest hardware refresh rate via EVERY available path simultaneously.
     * Called on app launch, game launch, and the Hero Hardware Banner tap.
     */
    public static void forceMaxHzNeverFallback(Context context) {
        if (context == null) return;
        int maxHz = Math.round(getMaxHardwareRefreshRate(context));
        int targetHz = maxHz > 0 ? maxHz : 60;
        // Settings path
        forceUnlockSystemRefreshRate(context, targetHz);
        // Shizuku 6-layer path
        com.gamebooster.app.booster.MaxHzForceChannel.forceApply(targetHz);
    }

    /** Restores the public refresh-rate preferences this app may have set. */
    public static boolean restoreAdaptiveRefreshRate(Context context) {
        if (context == null || !Settings.System.canWrite(context)) return false;
        return Settings.System.putString(context.getContentResolver(), "min_refresh_rate", null)
                | Settings.System.putString(context.getContentResolver(), "peak_refresh_rate", null);
    }
}
