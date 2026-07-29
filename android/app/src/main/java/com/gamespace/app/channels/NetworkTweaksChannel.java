package com.gamespace.app.channels;

import com.gamespace.app.data.CommandExecutor;

public class NetworkTweaksChannel {

    public static boolean enableLowLatencyNetwork() {
        boolean ok = true;
        // TCP Buffer tuning
        ok &= CommandExecutor.setSystemProperty("net.tcp.buffersize.wifi", "524288,1048576,2097152,262144,524288,1048576");
        ok &= CommandExecutor.setSystemProperty("net.tcp.buffersize.lte", "524288,1048576,2097152,262144,524288,1048576");

        if (RootCommandChannel.isAvailable()) {
            RootCommandChannel.writeSysfs("/proc/sys/net/ipv4/tcp_congestion_control", "bbr");
            RootCommandChannel.writeSysfs("/proc/sys/net/ipv4/tcp_low_latency", "1");
        }
        return ok;
    }
}
