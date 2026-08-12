package com.gamebooster.app.booster;

import android.util.Log;

/** Safe facade: CPU governors and thermal policy remain under Android/OEM control. */
public final class CpuGovernorChannel {
    private CpuGovernorChannel() { }

    public static boolean setGovernor(String governor) {
        Log.i("CpuGovernorChannel", "Ignoring unsupported governor override: " + governor);
        return false;
    }

    public static boolean setPerformanceLock() {
        return setGovernor("performance");
    }
}
