package com.gamebooster.app.feature.performance.booster;

import android.util.Log;
import com.gamebooster.app.platform.shell.CommandExecutor;

/** CPU performance power mode and governor control via privileged shell. */
public final class CpuGovernorChannel {
    private CpuGovernorChannel() { }

    public static boolean setGovernor(String governor) {
        Log.i("CpuGovernorChannel", "Applying CPU performance mode for governor request: " + governor);
        String res1 = CommandExecutor.executeSystemCommand("cmd power set-mode 0 1");
        String res2 = CommandExecutor.executeSystemCommand("cmd power set-mode 2 1");
        CommandExecutor.setSystemProperty("sys.power.cpu.boost", "1");
        return CommandExecutor.isSuccessOutput(res1) || CommandExecutor.isSuccessOutput(res2);
    }

    public static boolean setPerformanceLock() {
        return setGovernor("performance");
    }
}
