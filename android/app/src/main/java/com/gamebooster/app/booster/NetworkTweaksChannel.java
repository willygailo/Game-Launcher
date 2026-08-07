package com.gamebooster.app.booster;

import com.gamebooster.app.engine.CommandExecutor;

/**
 * NetworkTweaksChannel delegates low latency network configuration directly to NetworkOptimizer.
 */
public class NetworkTweaksChannel {

    public static boolean enableLowLatencyNetwork() {
        NetworkOptimizer.optimizeTcpBuffers();
        // Disable Wi-Fi sleep policy (2 = Never sleep)
        CommandExecutor.setSystemSetting("global", "wifi_sleep_policy", "2");
        return true;
    }

    public static boolean disableLowLatencyNetwork() {
        CommandExecutor.setSystemProperty("net.tcp.buffersize.wifi", "default");
        CommandExecutor.setSystemProperty("net.tcp.buffersize.mobile", "default");
        CommandExecutor.setSystemSetting("global", "wifi_sleep_policy", "0");
        return true;
    }
}
