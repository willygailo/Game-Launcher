package com.gamebooster.app.overlay;

import android.content.Context;

/**
 * Manager delegating crosshair overlay actions directly to CrosshairOverlayService.
 * Eliminates duplicate view instances and prevents orphaned overlay windows.
 */
public class CrosshairOverlayManager {

    private static boolean isShowing = false;

    public static boolean isShowing() {
        return CrosshairOverlayService.isRunning() || isShowing;
    }

    public static boolean toggleCrosshair(Context context) {
        if (isShowing()) {
            hideCrosshair(context);
            return false;
        } else {
            showCrosshair(context);
            return true;
        }
    }

    public static void showCrosshair(Context context) {
        if (context == null) return;
        CrosshairOverlayService.startOverlay(context);
        isShowing = true;
    }

    public static void hideCrosshair(Context context) {
        if (context == null) return;
        CrosshairOverlayService.stopOverlay(context);
        isShowing = false;
    }

    public static void forceHide(Context context) {
        isShowing = false;
    }
}
