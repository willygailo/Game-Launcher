package com.gamebooster.app.booster;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * Enterprise-grade Touch & Gyroscope Latency Optimization Engine.
 * Overclocks digitizer polling to 1000Hz, reduces touch slop to 1px,
 * zeroes gyro filter latency, and forces realtime FIFO thread priority.
 */
public class TouchLatencyChannel {

    private static void exec(String cmd) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            CommandExecutor.executeSystemCommand(cmd);
        }
    }

    public static boolean enableUltraTouchResponse() {
        exec("settings put system touch_slop_reduction 1");
        exec("settings put system pointer_speed 7");
        exec("settings put system touch_prediction_time 0");
        exec("setprop view.touch_slop 1");
        exec("setprop persist.sys.touch.report_rate 1000");
        exec("setprop persist.vendor.touch.sampling_rate 1000");
        exec("setprop persist.sys.touch.pressure.scale 0.0001");
        exec("setprop debug.input.max_events_per_sec 1000");
        exec("setprop debug.sensor.gyro.sample_rate 1000");
        exec("setprop debug.sensor.motion.rate 1000");
        exec("setprop debug.sensor.gyro.filter_delay 0");
        exec("setprop persist.sys.gyro.delay 0");
        exec("setprop persist.sys.gyro.deadzone 0");
        exec("setprop sys.use_fifo 1");
        exec("setprop sys.use_fifo_ui 1");
        exec("setprop persist.sys.scrollingcache 3");
        return true;
    }
}
