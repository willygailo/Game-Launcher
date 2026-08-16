package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import com.gamebooster.app.engine.CommandExecutor;

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
        ok &= CommandExecutor.setSystemProperty("persist.sys.gyro.delay", "0");
        ok &= CommandExecutor.setSystemProperty("sys.use_fifo", "1");
        ok &= CommandExecutor.setSystemProperty("persist.sys.scrollingcache", "3");
        return ok;
    }
}

