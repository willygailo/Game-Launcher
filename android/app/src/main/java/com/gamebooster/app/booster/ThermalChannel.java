package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class ThermalChannel {

    public static boolean setThermalOverride(boolean bypass) {
        String status = bypass ? "0" : "-1";
        String disableProp = bypass ? "1" : "0";
        boolean ok = false;

        // 1. Primary thermalservice override via Shizuku ADB
        String res1 = CommandExecutor.executeSystemCommand("cmd thermalservice override-status " + status);
        if (CommandExecutor.isSuccessOutput(res1)) ok = true;

        // 2. Fallback to legacy thermal service command
        String res2 = CommandExecutor.executeSystemCommand("cmd thermal override-status " + status);
        if (CommandExecutor.isSuccessOutput(res2)) ok = true;

        // 3. System properties to suppress kernel & vendor thermal throttling
        CommandExecutor.setSystemProperty("debug.thermal.throttle.disable", disableProp);
        CommandExecutor.setSystemProperty("vendor.thermal.mode", bypass ? "performance" : "normal");
        CommandExecutor.setSystemProperty("debug.thermal.suppress_throttle", disableProp);

        return ok;
    }
}
