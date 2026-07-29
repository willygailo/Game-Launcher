package com.gamespace.app.channels;

import com.gamespace.app.data.CommandExecutor;

public class CpuGovernorChannel {

    public static boolean setGovernor(String governor) {
        if (!RootCommandChannel.isAvailable()) {
            // PowerHAL / Game Mode fallback for non-rooted devices via Shizuku/ADB
            if (governor.equals("performance")) {
                CommandExecutor.executeSystemCommand("cmd power set-mode 0 1");
                CommandExecutor.executeSystemCommand("cmd power set-mode 1 0");
            } else {
                CommandExecutor.executeSystemCommand("cmd power set-mode 0 0");
            }
            return true;
        }

        String cmd = "for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo " + governor + " > $f 2>/dev/null || true; done";
        return RootCommandChannel.execute(cmd).isSuccess();
    }

    public static boolean setPerformanceLock() {
        boolean ok = setGovernor("performance");
        if (RootCommandChannel.isAvailable()) {
            RootCommandChannel.execute("for f in /sys/devices/system/cpu/cpu*/cpufreq/scaling_min_freq; do cat ${f/scaling_min_freq/cpuinfo_max_freq} > $f 2>/dev/null || true; done");
        }
        return ok;
    }
}
