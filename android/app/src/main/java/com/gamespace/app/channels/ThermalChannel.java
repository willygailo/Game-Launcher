package com.gamespace.app.channels;

import com.gamespace.app.data.CommandExecutor;

public class ThermalChannel {

    public static boolean setThermalOverride(boolean bypass) {
        String status = bypass ? "0" : "-1";
        boolean ok = false;

        // Primary thermalservice override via Shizuku ADB
        String res1 = CommandExecutor.executeSystemCommand("cmd thermalservice override-status " + status);
        if (CommandExecutor.isSuccessOutput(res1)) ok = true;

        // Fallback to legacy thermal service command
        String res2 = CommandExecutor.executeSystemCommand("cmd thermal override-status " + status);
        if (CommandExecutor.isSuccessOutput(res2)) ok = true;

        return ok;
    }
}
