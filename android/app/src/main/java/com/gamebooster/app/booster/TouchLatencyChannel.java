package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import com.gamebooster.app.engine.CommandExecutor;

public class TouchLatencyChannel {

    public static boolean enableUltraTouchResponse() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemSetting("system", "touch_slop_reduction", "1");
        ok &= CommandExecutor.setSystemProperty("view.touch_slop", "2");
        ok &= CommandExecutor.setSystemProperty("persist.sys.scrollingcache", "3");
        return ok;
    }
}
