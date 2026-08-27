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
        ok &= CommandExecutor.setSystemSetting("system", "touch_slop_reduction", "1");
        ok &= CommandExecutor.setSystemSetting("system", "pointer_speed", "7");
        ok &= CommandExecutor.setSystemProperty("view.touch_slop", "0");
        ok &= CommandExecutor.setSystemProperty("persist.sys.touch.pressure.scale", "0.0001");
        ok &= CommandExecutor.setSystemProperty("debug.input.max_events_per_sec", "1000");
        ok &= CommandExecutor.setSystemProperty("debug.sensor.gyro.sample_rate", "1000");
        ok &= CommandExecutor.setSystemProperty("debug.sensor.motion.rate", "1000");
        ok &= CommandExecutor.setSystemProperty("debug.sensor.gyro.smooth", "1");
        ok &= CommandExecutor.setSystemProperty("debug.sensor.gyro.stabilization", "1");
        ok &= CommandExecutor.setSystemProperty("persist.sys.gyro.filter", "1");
        ok &= CommandExecutor.setSystemProperty("persist.sys.gyro.delay", "0");
        ok &= CommandExecutor.setSystemProperty("sys.use_fifo", "1");
        ok &= CommandExecutor.setSystemProperty("persist.sys.scrollingcache", "3");

        // Enforce direct batch execution via Shizuku API (UID 2000 / UID 0)
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommands(
                "settings put system touch_slop_reduction 1",
                "settings put system pointer_speed 7",
                "setprop view.touch_slop 0",
                "setprop persist.sys.touch.pressure.scale 0.0001",
                "setprop debug.input.max_events_per_sec 1000",
                "setprop debug.sensor.gyro.sample_rate 1000",
                "setprop debug.sensor.motion.rate 1000",
                "setprop debug.sensor.gyro.smooth 1",
                "setprop debug.sensor.gyro.stabilization 1",
                "setprop persist.sys.gyro.filter 1",
                "setprop persist.sys.gyro.delay 0",
                "setprop sys.use_fifo 1",
                "setprop persist.sys.scrollingcache 3"
            );
        }
        return ok;
    }

    public static boolean restoreDefaultTouchResponse() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemSetting("system", "touch_slop_reduction", "0");
        ok &= CommandExecutor.setSystemSetting("system", "pointer_speed", "0");
        ok &= CommandExecutor.setSystemProperty("view.touch_slop", "8");
        ok &= CommandExecutor.setSystemProperty("debug.input.max_events_per_sec", "120");

        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommands(
                "settings put system touch_slop_reduction 0",
                "settings put system pointer_speed 0",
                "setprop view.touch_slop 8",
                "setprop debug.input.max_events_per_sec 120"
            );
        }
        return ok;
    }
}
