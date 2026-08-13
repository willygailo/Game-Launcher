package com.gamebooster.app.feature.performance.booster;

import android.util.Log;
import com.gamebooster.app.feature.performance.tweaks.SetEditSettingsEnforcer;

/** Input prediction, latency tuning, and touch sampling response facade. */
public final class TouchLatencyChannel {
    private static final String TAG = "TouchLatencyChannel";

    private TouchLatencyChannel() { }

    public static boolean enableUltraTouchResponse() {
        Log.i(TAG, "Enforcing Ultra Low Touch Latency & High Sampling properties...");
        return SetEditSettingsEnforcer.enforceUltraTouchSettings();
    }
}
