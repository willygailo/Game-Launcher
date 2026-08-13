package com.gamebooster.app.feature.sensitivity;

import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * GyroTouchOptimizer — Dynamic sensor data filtering & touch polling rate optimizer.
 *
 * Provides a 1-pole Low-Pass Filter (LPF) for 3-axis gyroscope readings to eliminate micro-jitter
 * during aiming in eSports titles (PUBGM, CODM), along with system touch latency tuning.
 */
public class GyroTouchOptimizer {

    private final float alpha;
    private final float[] filteredGyro = new float[3];
    private boolean initialized = false;

    /**
     * Constructs a GyroTouchOptimizer with a smoothing factor alpha.
     *
     * @param alpha Filter smoothing constant (0.0 < alpha <= 1.0). Lower values offer smoother motion; higher values offer faster response.
     */
    public GyroTouchOptimizer(float alpha) {
        this.alpha = Math.max(0.01f, Math.min(1.0f, alpha));
    }

    /**
     * Default constructor with balanced alpha = 0.8f.
     */
    public GyroTouchOptimizer() {
        this(0.8f);
    }

    /**
     * Filters raw 3-axis gyroscope readings [x, y, z] using a Low-Pass Filter.
     *
     * @param rawReading Raw gyroscope sensor values array of length 3.
     * @return Filtered 3-axis gyroscope array.
     */
    public float[] filterGyro(float[] rawReading) {
        if (rawReading == null || rawReading.length < 3) {
            return new float[]{0f, 0f, 0f};
        }

        if (!initialized) {
            filteredGyro[0] = rawReading[0];
            filteredGyro[1] = rawReading[1];
            filteredGyro[2] = rawReading[2];
            initialized = true;
        } else {
            filteredGyro[0] = filteredGyro[0] + alpha * (rawReading[0] - filteredGyro[0]);
            filteredGyro[1] = filteredGyro[1] + alpha * (rawReading[1] - filteredGyro[1]);
            filteredGyro[2] = filteredGyro[2] + alpha * (rawReading[2] - filteredGyro[2]);
        }

        return new float[]{filteredGyro[0], filteredGyro[1], filteredGyro[2]};
    }

    /**
     * Resets the filter internal state.
     */
    public void reset() {
        initialized = false;
        filteredGyro[0] = 0f;
        filteredGyro[1] = 0f;
        filteredGyro[2] = 0f;
    }

    /**
     * Applies system touch responsiveness properties via Shizuku shell IPC.
     *
     * @param highPollingRate True to boost touch sampling rate to max (240Hz/360Hz where supported).
     * @return true if properties were injected successfully.
     */
    public static boolean applyTouchBoost(boolean highPollingRate) {
        String rate = highPollingRate ? "240" : "120";
        boolean p1 = CommandExecutor.setSystemProperty("persist.sys.touch.rate", rate);
        boolean p2 = CommandExecutor.setSystemSetting("system", "touch_prediction_latency", highPollingRate ? "0" : "10");
        return p1 && p2;
    }

    /**
     * Applies a complete TouchSensitivityPreset across view touch slop, digitizer event rate, and pressure scale properties.
     *
     * @param preset TouchSensitivityPreset to inject.
     * @return true if properties were applied cleanly.
     */
    public static boolean applyPreset(com.gamebooster.app.feature.sensitivity.model.TouchSensitivityPreset preset) {
        if (preset == null) return false;
        boolean p1 = CommandExecutor.setSystemProperty("view.touch_slop", String.valueOf(preset.touchSlop));
        boolean p2 = CommandExecutor.setSystemProperty("persist.sys.touch.pressure.scale", String.valueOf(preset.pressureScale));
        boolean p3 = CommandExecutor.setSystemProperty("debug.input.max_events_per_sec", String.valueOf(preset.maxEventsPerSec));
        boolean p4 = CommandExecutor.setSystemSetting("system", "touch_slop_reduction", preset.touchSlop == 0 ? "1" : "0");
        return p1 && p2 && p3 && p4;
    }
}
