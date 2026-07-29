package com.gamespace.app.channels;

import com.gamespace.app.data.CommandExecutor;

public class TouchLatencyChannel {

    public static boolean enableUltraTouchResponse() {
        boolean ok = true;
        // Modern touch response settings
        ok &= CommandExecutor.setSystemSetting("system", "touch_slop_reduction", "1");
        ok &= CommandExecutor.setSystemProperty("view.touch_slop", "2");
        ok &= CommandExecutor.setSystemProperty("persist.sys.scrollingcache", "3");

        // Vendor touch node write if root is present
        if (RootCommandChannel.isAvailable()) {
            RootCommandChannel.writeSysfs("/sys/class/touch/touch_dev/game_mode", "1");
            RootCommandChannel.writeSysfs("/sys/touchpanel/game_mode", "1");
            RootCommandChannel.writeSysfs("/sys/devices/platform/soc/*.touch/game_mode", "1");
        }
        return ok;
    }
}
