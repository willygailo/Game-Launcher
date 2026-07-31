package com.gamebooster.app.functions;

import com.gamebooster.app.root.CommandExecutor;

public class CpuGovernorChannel {

    public static boolean setGovernor(String governor) {
        if ("performance".equalsIgnoreCase(governor)) {
            CommandExecutor.executeSystemCommand("cmd power set-mode 0 1");
            CommandExecutor.executeSystemCommand("cmd power set-mode 1 0");
        } else {
            CommandExecutor.executeSystemCommand("cmd power set-mode 0 0");
        }
        return true;
    }

    public static boolean setPerformanceLock() {
        return setGovernor("performance");
    }
}
