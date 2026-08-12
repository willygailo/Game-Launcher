package com.gamebooster.app.booster;

import android.util.Log;

/**
 * Safe compatibility facade for the former global input-property tuner.
 * Input sampling, slop, pressure, and prediction are device/OEM-owned and are
 * not rewritten globally by the launcher.
 */
public final class TouchLatencyChannel {
    private static final String TAG = "TouchLatencyChannel";

    private TouchLatencyChannel() { }

    public static boolean enableUltraTouchResponse() {
        Log.i(TAG, "Using Android/OEM input configuration; no global touch properties changed.");
        return true;
    }
}
