package com.gamebooster.app.booster;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class NetworkTweaksChannel {

    public static boolean enableLowLatencyNetwork() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("net.tcp.buffersize.wifi", "524288,1048576,2097152,262144,524288,1048576");
        ok &= CommandExecutor.setSystemProperty("net.tcp.buffersize.lte", "524288,1048576,2097152,262144,524288,1048576");
        ok &= CommandExecutor.setSystemProperty("net.ipv4.tcp_congestion_control", "bbr");
        ok &= CommandExecutor.setSystemProperty("net.ipv4.tcp_ecn", "0");
        ok &= CommandExecutor.setSystemProperty("net.ipv4.tcp_sack", "1");
        
        // Disable Wi-Fi sleep policy (2 = Never sleep)
        CommandExecutor.setSystemSetting("global", "wifi_sleep_policy", "2");
        CommandExecutor.executeSystemCommand("cmd wifi force-low-latency-mode enabled");
        return ok;
    }

    public static boolean restoreLowLatencyNetwork() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("net.ipv4.tcp_congestion_control", "cubic");
        ok &= CommandExecutor.setSystemProperty("net.ipv4.tcp_ecn", "1");
        CommandExecutor.setSystemSetting("global", "wifi_sleep_policy", "1");
        CommandExecutor.executeSystemCommand("cmd wifi force-low-latency-mode disabled");
        CommandExecutor.executeSystemCommand("cmd wifi force-hi-perf-mode disabled");
        return ok;
    }
}
