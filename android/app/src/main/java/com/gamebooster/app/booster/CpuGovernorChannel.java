package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import com.gamebooster.app.engine.CommandExecutor;

public class CpuGovernorChannel {

    public static boolean setGovernor(String governor) {
        if ("extreme".equalsIgnoreCase(governor) || "performance".equalsIgnoreCase(governor)) {
            CommandExecutor.executeSystemCommand("cmd power set-mode 2 1");
            CommandExecutor.executeSystemCommand("cmd power set-mode 0 1");
        } else {
            CommandExecutor.executeSystemCommand("cmd power set-mode 2 0");
            CommandExecutor.executeSystemCommand("cmd power set-mode 0 0");
        }
        return true;
    }

    public static boolean setPerformanceLock() {
        return setGovernor("extreme");
    }
}
