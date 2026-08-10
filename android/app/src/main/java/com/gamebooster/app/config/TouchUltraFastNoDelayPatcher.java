package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class TouchUltraFastNoDelayPatcher {

    private static final String TAG = "TouchNoDelayPatcher";

    /**
     * Applies ultra-fast zero touch delay tweaks for Android 13, 14, 15, 16.
     * Boosts touch polling rate, eliminates touch slop delay, and aligns input event rendering.
     */
    public static void applyTouchNoDelay(String packageName) {
        Log.d(TAG, "Applying Ultra-Fast Zero Touch Delay tweaks for " + (packageName != null ? packageName : "System"));

        // 1. Android Touch Response & Pointer Speed System Settings
        ShizukuExecutor.executeShizukuCommand("cmd settings put system touch_response_speed 1");
        ShizukuExecutor.executeShizukuCommand("cmd settings put system pointer_speed 7");
        ShizukuExecutor.executeShizukuCommand("cmd settings put secure touch_blocking_period 0");
        ShizukuExecutor.executeShizukuCommand("cmd settings put secure long_press_timeout 100");
        ShizukuExecutor.executeShizukuCommand("cmd settings put secure multi_press_timeout 100");

        // 2. Input Native Boot & Touch Slop Elimination (Android 13/14/15/16)
        ShizukuExecutor.executeShizukuCommand("device_config put input_native_boot touch_slop 0");
        ShizukuExecutor.executeShizukuCommand("device_config put input_native_boot touch_velocity_tracking 1");
        ShizukuExecutor.executeShizukuCommand("device_config put input_native_boot touch_high_sensitivity 1");
        ShizukuExecutor.executeShizukuCommand("device_config put input_native_boot input_event_rate_max 1000");

        // 3. Touch Pressure & Size Calibration Properties
        CommandExecutor.executeSystemCommand("setprop touch.pressure.scale 0.001");
        CommandExecutor.executeSystemCommand("setprop touch.size.calibration geometric");
        CommandExecutor.executeSystemCommand("setprop touch.size.scale 1.0");
        CommandExecutor.executeSystemCommand("setprop touch.size.bias 0.0");
        CommandExecutor.executeSystemCommand("setprop touch.orientation.calibration none");
        CommandExecutor.executeSystemCommand("setprop view.touch_slop 0");

        ShizukuExecutor.executeShizukuCommand("setprop touch.pressure.scale 0.001");
        ShizukuExecutor.executeShizukuCommand("setprop touch.size.calibration geometric");
        ShizukuExecutor.executeShizukuCommand("setprop touch.size.scale 1.0");
        ShizukuExecutor.executeShizukuCommand("setprop view.touch_slop 0");

        // 4. SurfaceFlinger Input Latency & VSYNC Alignment
        ShizukuExecutor.executeShizukuCommand("setprop debug.sf.early_app_phase_offset_ns 500000");
        ShizukuExecutor.executeShizukuCommand("setprop debug.sf.early_gl_phase_offset_ns 500000");
        ShizukuExecutor.executeShizukuCommand("setprop debug.sf.high_fps_early_phase_offset_ns 500000");
        ShizukuExecutor.executeShizukuCommand("setprop debug.sf.high_fps_early_gl_phase_offset_ns 500000");
        ShizukuExecutor.executeShizukuCommand("setprop debug.sf.latch_unsignaled 1");

        // 5. If specific game package provided, set app window refresh rate & touch priority
        if (packageName != null && !packageName.trim().isEmpty()) {
            String pkg = packageName.trim();
            ShizukuExecutor.executeShizukuCommand("cmd appops set " + pkg + " SYSTEM_ALERT_WINDOW allow");
            ShizukuExecutor.executeShizukuCommand("cmd appops set " + pkg + " RUN_IN_BACKGROUND allow");
        }
    }
}
