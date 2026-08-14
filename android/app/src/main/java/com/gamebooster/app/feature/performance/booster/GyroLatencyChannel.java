package com.gamebooster.app.feature.performance.booster;

import android.util.Log;
import com.gamebooster.app.feature.performance.tweaks.SetEditSettingsEnforcer;

/**
 * GyroLatencyChannel manages 1000Hz sampling rate and zero-delay gyroscope/accelerometer tuning.
 */
public final class GyroLatencyChannel {
    private static final String TAG = "GyroLatencyChannel";

    private GyroLatencyChannel() { }

    public static boolean enableZeroDelayGyro() {
        Log.i(TAG, "Enforcing 1000Hz Gyroscope Sampling & Zero HAL Batching Delay...");
        return SetEditSettingsEnforcer.enforceZeroDelayGyroSettings();
    }
}
