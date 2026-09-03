package com.gamebooster.app.engine;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.gamebooster.app.shizuku.RishManager;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

/**
 * ResolutionScalerEngine — Per-Game Custom Display Resolution & Proportional DPI Scaler.
 *
 * Utilizes privileged Shizuku execution of `wm size` and `wm density` to scale the internal
 * rendering canvas for demanding 3D games (e.g. downscale from 1080p to 900p or 720p).
 * Proportional DPI scaling guarantees zero touch misalignment or UI distortion.
 * Safely restores original display dimensions on game session exit.
 */
public final class ResolutionScalerEngine {

    private static final String TAG = "ResolutionScalerEngine";

    public enum ScalePreset {
        NATIVE_100(1.0f, "100% Native Display"),
        HIGH_900P(0.833f, "900p Balanced Boost (~83%)"),
        ESPORTS_720P(0.667f, "720p Esports Turbo (~67%)"),
        EXTREME_540P(0.500f, "540p Extreme FPS & Cool (~50%)");

        public final float scaleFactor;
        public final String label;

        ScalePreset(float scaleFactor, String label) {
            this.scaleFactor = scaleFactor;
            this.label = label;
        }

        public static ScalePreset fromScaleFactor(float factor) {
            if (factor >= 0.95f) return NATIVE_100;
            if (factor >= 0.78f) return HIGH_900P;
            if (factor >= 0.58f) return ESPORTS_720P;
            return EXTREME_540P;
        }
    }

    private static int sNativeWidth = 0;
    private static int sNativeHeight = 0;
    private static int sNativeDensity = 0;
    private static boolean sIsScaled = false;

    private ResolutionScalerEngine() {}

    private static String executePrivileged(String command) {
        if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            String out = ShizukuUserServiceConnector.getInstance().executeCommand(command);
            if (out != null) return out;
        }

        if (ShizukuExecutor.hasShizukuPermission()) {
            String out = ShizukuExecutor.executeShizukuCommand(command);
            if (out != null && !out.startsWith("ERROR")) return out;
        }

        if (RishManager.isRishAvailable()) {
            String out = RishManager.executeRishCommand(null, command);
            if (out != null && !out.startsWith("ERROR")) return out;
        }

        ShellExecutor.CommandResult cr = ShellExecutor.executeCommand(command);
        return cr != null ? cr.stdout : "";
    }

    /**
     * Initializes and caches the true physical display resolution and density.
     */
    public static synchronized void probeNativeDisplay(Context context) {
        if (sNativeWidth > 0 && sNativeHeight > 0 && sNativeDensity > 0) return;

        // 1. Try querying wm size & wm density
        String sizeOut = executePrivileged("wm size");
        if (sizeOut != null && sizeOut.contains("Physical size:")) {
            try {
                int idx = sizeOut.indexOf("Physical size:");
                String part = sizeOut.substring(idx + 14).trim();
                int newline = part.indexOf('\n');
                if (newline > 0) part = part.substring(0, newline).trim();
                String[] dims = part.split("x");
                if (dims.length == 2) {
                    sNativeWidth = Integer.parseInt(dims[0].trim());
                    sNativeHeight = Integer.parseInt(dims[1].trim());
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed parsing wm size physical size: " + e.getMessage());
            }
        }

        String densityOut = executePrivileged("wm density");
        if (densityOut != null && densityOut.contains("Physical density:")) {
            try {
                int idx = densityOut.indexOf("Physical density:");
                String part = densityOut.substring(idx + 17).trim();
                int newline = part.indexOf('\n');
                if (newline > 0) part = part.substring(0, newline).trim();
                sNativeDensity = Integer.parseInt(part.trim());
            } catch (Exception e) {
                Log.w(TAG, "Failed parsing wm density physical density: " + e.getMessage());
            }
        }

        // 2. Fallback to WindowMetrics if shell parsing returned 0
        if (context != null && (sNativeWidth <= 0 || sNativeHeight <= 0 || sNativeDensity <= 0)) {
            try {
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                if (wm != null) {
                    android.graphics.Rect bounds = wm.getCurrentWindowMetrics().getBounds();
                    if (sNativeWidth <= 0) sNativeWidth = Math.min(bounds.width(), bounds.height());
                    if (sNativeHeight <= 0) sNativeHeight = Math.max(bounds.width(), bounds.height());
                    if (sNativeDensity <= 0) {
                        sNativeDensity = context.getResources().getConfiguration().densityDpi;
                    }
                }
            } catch (Exception ignored) {}
        }

        Log.i(TAG, "Cached Native Display: " + sNativeWidth + "x" + sNativeHeight + " @ " + sNativeDensity + "dpi");
    }

    /**
     * Applies scaled resolution and density.
     */
    public static boolean applyResolutionScale(Context context, float scaleFactor) {
        if (scaleFactor >= 0.99f || scaleFactor <= 0.3f) {
            return resetResolutionSync();
        }

        probeNativeDisplay(context);
        if (sNativeWidth <= 0 || sNativeHeight <= 0 || sNativeDensity <= 0) {
            Log.w(TAG, "Native display metrics unavailable; cannot scale resolution.");
            return false;
        }

        int targetW = (int) (sNativeWidth * scaleFactor);
        int targetH = (int) (sNativeHeight * scaleFactor);
        int targetDensity = (int) (sNativeDensity * scaleFactor);

        // Ensure even dimensions for video codecs and Vulkan framebuffers
        if (targetW % 2 != 0) targetW--;
        if (targetH % 2 != 0) targetH--;

        String cmd = "wm size " + targetW + "x" + targetH + "; wm density " + targetDensity;
        executePrivileged(cmd);
        sIsScaled = true;
        Log.i(TAG, "Applied scaled resolution: " + targetW + "x" + targetH + " @ " + targetDensity + "dpi");
        return true;
    }

    public static boolean resetResolutionSync() {
        if (!sIsScaled) {
            Log.d(TAG, "Screen resolution was not scaled; skipping wm size reset to prevent game surface disruption.");
            return true;
        }
        executePrivileged("wm size reset; wm density reset");
        sIsScaled = false;
        Log.i(TAG, "Resolution reset to stock physical display.");
        return true;
    }

    public static boolean forceResetResolutionSync() {
        executePrivileged("wm size reset; wm density reset");
        sIsScaled = false;
        Log.i(TAG, "Forced resolution reset to stock physical display.");
        return true;
    }

    public static boolean isResolutionScaled() {
        return sIsScaled;
    }

    public static int getNativeWidth() {
        return sNativeWidth;
    }

    public static int getNativeHeight() {
        return sNativeHeight;
    }

    public static int getNativeDensity() {
        return sNativeDensity;
    }
}
