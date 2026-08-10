package com.gamebooster.app.booster;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class NetworkTweaksChannel {

    private static final String TAG = "NetworkTweaksChannel";

    public static boolean enableLowLatencyNetwork() {
        Log.d(TAG, "Activating low-latency network optimizations across system");

        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("net.tcp.buffersize.wifi", "524288,1048576,2097152,262144,524288,1048576");
        ok &= CommandExecutor.setSystemProperty("net.tcp.buffersize.lte", "524288,1048576,2097152,262144,524288,1048576");

        ShizukuExecutor.executeShizukuCommand("setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576");
        ShizukuExecutor.executeShizukuCommand("setprop net.tcp.buffersize.lte 524288,1048576,2097152,262144,524288,1048576");

        // Disable Wi-Fi sleep policy (2 = Never sleep)
        CommandExecutor.setSystemSetting("global", "wifi_sleep_policy", "2");
        ShizukuExecutor.executeShizukuCommand("settings put global wifi_sleep_policy 2");

        // Mobile data always-on prevents reconnect delay when switching Wi-Fi / Cell
        CommandExecutor.setSystemSetting("global", "mobile_data_always_on", "1");
        ShizukuExecutor.executeShizukuCommand("settings put global mobile_data_always_on 1");

        // Disable TCP slow start after idle for instant packet dispatch
        CommandExecutor.setSystemProperty("net.ipv4.tcp_slow_start_after_idle", "0");
        ShizukuExecutor.executeShizukuCommand("setprop net.ipv4.tcp_slow_start_after_idle 0");

        // UDP socket buffer optimization for low-latency game packets
        CommandExecutor.executeSystemCommand("sysctl -w net.core.rmem_max=2097152 2>/dev/null");
        CommandExecutor.executeSystemCommand("sysctl -w net.core.wmem_max=2097152 2>/dev/null");

        return ok;
    }

    public static boolean applyGameNetworkPriority(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.trim().toLowerCase();

        Log.d(TAG, "Setting high-priority network routing for " + pkg);

        // Force low latency mode via Wi-Fi manager ADB command
        NetworkOptimizer.enableWifiLowLatencyGameMode();

        // Mark game socket priority via cmd netd
        ShizukuExecutor.executeShizukuCommand("cmd netd traffic-mark " + pkg + " 0x1000");

        return true;
    }

    public static boolean revertNetworkTweaks() {
        Log.d(TAG, "Reverting network tweaks to stock defaults");
        CommandExecutor.setSystemSetting("global", "wifi_sleep_policy", "2");
        CommandExecutor.setSystemSetting("global", "mobile_data_always_on", "0");
        NetworkOptimizer.disableWifiLowLatencyGameMode();
        return true;
    }
}
