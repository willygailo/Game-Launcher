package com.gamebooster.app.feature.overlay;

import android.content.Context;

/**
 * Unified static facade for Crosshair Overlay state management.
 * Delegates lifecycle to CrosshairOverlayService to guarantee a single
 * consistent crosshair overlay view across HUD, Settings, and Web Dashboard.
 */
public class CrosshairOverlayManager {

    public static boolean isShowing() {
        return CrosshairOverlayService.isOverlayRunning();
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
        CrosshairOverlayService.startOverlay(context);
    }

    public static void hideCrosshair(Context context) {
        CrosshairOverlayService.stopOverlay(context);
    }

    public static void updateCrosshair(Context context) {
        CrosshairOverlayService.updateOverlay(context);
    }
}
