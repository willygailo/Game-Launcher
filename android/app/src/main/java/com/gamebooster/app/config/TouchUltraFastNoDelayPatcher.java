package com.gamebooster.app.config;

import android.util.Log;

/**
 * Compatibility boundary for the retired global touch-property injector.
 * Android/OEM input calibration is device-owned and is not rewritten by the
 * launcher. Games may still use their own supported sensitivity controls.
 */
public final class TouchUltraFastNoDelayPatcher {
    private static final String TAG = "TouchNoDelayPatcher";

    private TouchUltraFastNoDelayPatcher() { }

    public static void applyTouchNoDelay(String packageName) {
        Log.i(TAG, "Skipped unsupported global touch-property changes for "
                + (packageName == null ? "system" : packageName));
    }
}
