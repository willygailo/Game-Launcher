package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import com.gamebooster.app.engine.CommandExecutor;

public class TouchLatencyChannel {

    public static boolean enableUltraTouchResponse() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemSetting("system", "touch_slop_reduction", "1");
        ok &= CommandExecutor.setSystemProperty("view.touch_slop", "0");
        ok &= CommandExecutor.setSystemProperty("touch.distance.scale", "0");
        ok &= CommandExecutor.setSystemProperty("touch.pressure.scale", "0");
        ok &= CommandExecutor.setSystemProperty("touch.size.calibration", "none");
        ok &= CommandExecutor.setSystemProperty("touch.gestureMode", "0");
        ok &= CommandExecutor.setSystemProperty("debug.input.max_events_per_sec", "1000");
        ok &= CommandExecutor.setSystemProperty("windowsmgr.max_events_per_sec", "1000");
        ok &= CommandExecutor.setSystemProperty("sys.use_fifo", "1");
        ok &= CommandExecutor.setSystemProperty("persist.sys.touch_latency", "0");
        ok &= CommandExecutor.setSystemProperty("persist.sys.touch_response", "0");
        ok &= CommandExecutor.setSystemProperty("persist.sys.scrollingcache", "3");
        return ok;
    }
}
