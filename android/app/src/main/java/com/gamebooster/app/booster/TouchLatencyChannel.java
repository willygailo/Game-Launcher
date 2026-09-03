package com.gamebooster.app.booster;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * TouchLatencyChannel — Precision Aim, Zero-Slop Touch, and 1000Hz Gyroscope Tuner.
 * Directly enforces low-latency gaming touch pipeline through elevated Shizuku API.
 */
public class TouchLatencyChannel {

    public static boolean enableUltraTouchResponse() {
        boolean ok = true;
        // 1. Android System Settings & Gesture Slop Elimination
        ok &= CommandExecutor.setSystemSetting("system", "touch_slop_reduction", "1");
        ok &= CommandExecutor.setSystemSetting("system", "pointer_speed", "7");
        ok &= CommandExecutor.setSystemSetting("system", "touch_sensitivity", "1");
        ok &= CommandExecutor.setSystemSetting("system", "master_touch_sensitivity", "1");
        ok &= CommandExecutor.setSystemSetting("system", "game_mode_touch", "1");
        ok &= CommandExecutor.setSystemSetting("system", "edge_touch_filter", "0");
        ok &= CommandExecutor.setSystemSetting("secure", "long_press_timeout", "150");
        ok &= CommandExecutor.setSystemSetting("secure", "multi_press_timeout", "100");
        ok &= CommandExecutor.setSystemSetting("secure", "edge_rejection_mode", "0");

        // 2. View Framework & Motion Friction
        ok &= CommandExecutor.setSystemProperty("view.touch_slop", "0");
        ok &= CommandExecutor.setSystemProperty("view.scroll_friction", "0.001");
        ok &= CommandExecutor.setSystemProperty("view.fading_edge_length", "0");
        ok &= CommandExecutor.setSystemProperty("ro.min_pointer_dur", "1");

        // 3. 1000Hz Hardware Touch Digitizer & Sampling Rates
        ok &= CommandExecutor.setSystemProperty("persist.sys.touch.report_rate", "1000");
        ok &= CommandExecutor.setSystemProperty("persist.vendor.touch.sampling_rate", "1000");
        ok &= CommandExecutor.setSystemProperty("debug.touch.sampling_rate", "1000");
        ok &= CommandExecutor.setSystemProperty("persist.sys.gamemode.touch", "1");
        ok &= CommandExecutor.setSystemProperty("vendor.touch.game_mode", "1");
        ok &= CommandExecutor.setSystemProperty("persist.asus.touch_sampling_rate", "1000");
        ok &= CommandExecutor.setSystemProperty("persist.vendor.asus.touch_opt", "1");

        // 4. InputFlinger & Input Dispatch Thread Latency Elimination
        ok &= CommandExecutor.setSystemProperty("debug.inputflinger.touch_boost", "1");
        ok &= CommandExecutor.setSystemProperty("debug.inputflinger.fling_boost", "1");
        ok &= CommandExecutor.setSystemProperty("debug.input.boost_time_ms", "2000");
        ok &= CommandExecutor.setSystemProperty("debug.input.max_events_per_sec", "1000");
        ok &= CommandExecutor.setSystemProperty("debug.hwui.input_latency_timeout", "0");
        ok &= CommandExecutor.setSystemProperty("persist.sys.input.latency", "0");

        // 5. Palm Rejection & Edge Deadzone Bypass
        ok &= CommandExecutor.setSystemProperty("persist.sys.touch.edge_filter", "0");
        ok &= CommandExecutor.setSystemProperty("persist.vendor.touch.edge_reject", "0");
        ok &= CommandExecutor.setSystemProperty("persist.sys.touch.corner_filter", "0");

        // 6. Touch Pressure, Calibration & Gyro Sensor 1000Hz
        ok &= CommandExecutor.setSystemProperty("touch.pressure.scale", "0.0001");
        ok &= CommandExecutor.setSystemProperty("touch.size.calibration", "geometric");
        ok &= CommandExecutor.setSystemProperty("touch.pressure.calibration", "physical");
        ok &= CommandExecutor.setSystemProperty("touch.distance.scale", "0");
        ok &= CommandExecutor.setSystemProperty("touch.size.bias", "0");
        ok &= CommandExecutor.setSystemProperty("debug.sensor.gyro.sample_rate", "1000");
        ok &= CommandExecutor.setSystemProperty("debug.sensor.motion.rate", "1000");
        ok &= CommandExecutor.setSystemProperty("debug.sensor.gyro.smooth", "1");
        ok &= CommandExecutor.setSystemProperty("debug.sensor.gyro.stabilization", "1");
        ok &= CommandExecutor.setSystemProperty("persist.sys.gyro.delay", "0");
        ok &= CommandExecutor.setSystemProperty("persist.sys.scrollingcache", "3");

        // Enforce direct batch execution via Shizuku API (UID 2000 / UID 0)
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommands(
                "settings put system touch_slop_reduction 1",
                "settings put system pointer_speed 7",
                "settings put system touch_sensitivity 1",
                "settings put system master_touch_sensitivity 1",
                "settings put system game_mode_touch 1",
                "settings put system edge_touch_filter 0",
                "settings put secure long_press_timeout 150",
                "settings put secure multi_press_timeout 100",
                "settings put secure edge_rejection_mode 0",
                "setprop view.touch_slop 0",
                "setprop view.scroll_friction 0.001",
                "setprop view.fading_edge_length 0",
                "setprop ro.min_pointer_dur 1",
                "setprop persist.sys.touch.report_rate 1000",
                "setprop persist.vendor.touch.sampling_rate 1000",
                "setprop debug.touch.sampling_rate 1000",
                "setprop persist.sys.gamemode.touch 1",
                "setprop vendor.touch.game_mode 1",
                "setprop persist.asus.touch_sampling_rate 1000",
                "setprop persist.vendor.asus.touch_opt 1",
                "setprop debug.inputflinger.touch_boost 1",
                "setprop debug.inputflinger.fling_boost 1",
                "setprop debug.input.boost_time_ms 2000",
                "setprop debug.input.max_events_per_sec 1000",
                "setprop debug.hwui.input_latency_timeout 0",
                "setprop persist.sys.input.latency 0",
                "setprop persist.sys.touch.edge_filter 0",
                "setprop persist.vendor.touch.edge_reject 0",
                "setprop persist.sys.touch.corner_filter 0",
                "setprop touch.pressure.scale 0.0001",
                "setprop touch.size.calibration geometric",
                "setprop touch.pressure.calibration physical",
                "setprop touch.distance.scale 0",
                "setprop touch.size.bias 0",
                "setprop debug.sensor.gyro.sample_rate 1000",
                "setprop debug.sensor.motion.rate 1000",
                "setprop debug.sensor.gyro.smooth 1",
                "setprop debug.sensor.gyro.stabilization 1",
                "setprop persist.sys.gyro.filter 1",
                "setprop persist.sys.gyro.delay 0",
                "setprop persist.sys.scrollingcache 3"
            );
        }
        return ok;
    }

    public static boolean restoreDefaultTouchResponse() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemSetting("system", "touch_slop_reduction", "0");
        ok &= CommandExecutor.setSystemSetting("system", "pointer_speed", "0");
        ok &= CommandExecutor.setSystemSetting("system", "touch_sensitivity", "0");
        ok &= CommandExecutor.setSystemSetting("system", "game_mode_touch", "0");
        ok &= CommandExecutor.setSystemSetting("secure", "long_press_timeout", "400");
        ok &= CommandExecutor.setSystemSetting("secure", "multi_press_timeout", "300");
        ok &= CommandExecutor.setSystemSetting("secure", "edge_rejection_mode", "1");
        ok &= CommandExecutor.setSystemProperty("view.touch_slop", "8");
        ok &= CommandExecutor.setSystemProperty("view.scroll_friction", "0.015");
        ok &= CommandExecutor.setSystemProperty("persist.sys.touch.report_rate", "120");
        ok &= CommandExecutor.setSystemProperty("persist.vendor.touch.sampling_rate", "120");
        ok &= CommandExecutor.setSystemProperty("debug.touch.sampling_rate", "120");
        ok &= CommandExecutor.setSystemProperty("persist.sys.gamemode.touch", "0");
        ok &= CommandExecutor.setSystemProperty("vendor.touch.game_mode", "0");
        ok &= CommandExecutor.setSystemProperty("debug.inputflinger.touch_boost", "0");
        ok &= CommandExecutor.setSystemProperty("debug.inputflinger.fling_boost", "0");
        ok &= CommandExecutor.setSystemProperty("debug.input.max_events_per_sec", "120");
        ok &= CommandExecutor.setSystemProperty("debug.hwui.input_latency_timeout", "500");
        ok &= CommandExecutor.setSystemProperty("persist.sys.touch.edge_filter", "1");
        ok &= CommandExecutor.setSystemProperty("persist.vendor.touch.edge_reject", "1");

        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommands(
                "settings put system touch_slop_reduction 0",
                "settings put system pointer_speed 0",
                "settings put system touch_sensitivity 0",
                "settings put system game_mode_touch 0",
                "settings put secure long_press_timeout 400",
                "settings put secure multi_press_timeout 300",
                "settings put secure edge_rejection_mode 1",
                "setprop view.touch_slop 8",
                "setprop view.scroll_friction 0.015",
                "setprop persist.sys.touch.report_rate 120",
                "setprop persist.vendor.touch.sampling_rate 120",
                "setprop debug.touch.sampling_rate 120",
                "setprop persist.sys.gamemode.touch 0",
                "setprop vendor.touch.game_mode 0",
                "setprop debug.inputflinger.touch_boost 0",
                "setprop debug.inputflinger.fling_boost 0",
                "setprop debug.input.max_events_per_sec 120",
                "setprop debug.hwui.input_latency_timeout 500",
                "setprop persist.sys.touch.edge_filter 1",
                "setprop persist.vendor.touch.edge_reject 1"
            );
        }
        return ok;
    }
}
