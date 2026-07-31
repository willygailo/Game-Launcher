package com.gamebooster.app.functions;

import com.gamebooster.app.root.CommandExecutor;

public class NetworkTweaksChannel {

    public static boolean enableLowLatencyNetwork() {
        boolean ok = true;
        ok &= CommandExecutor.setSystemProperty("net.tcp.buffersize.wifi", "524288,1048576,2097152,262144,524288,1048576");
        ok &= CommandExecutor.setSystemProperty("net.tcp.buffersize.lte", "524288,1048576,2097152,262144,524288,1048576");
        // Disable Wi-Fi sleep policy (2 = Never sleep)
        CommandExecutor.setSystemSetting("global", "wifi_sleep_policy", "2");
        return ok;
    }
}
