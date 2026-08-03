package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import android.content.Context;

import com.gamebooster.app.engine.CommandExecutor;

public class NetworkOptimizer {

    public enum DnsMode {
        CLOUDFLARE_1_1_1_1("1.1.1.1", "1.0.0.1", "one.one.one.one"),
        GOOGLE_8_8_8_8("8.8.8.8", "8.8.4.4", "dns.google"),
        SYSTEM_DEFAULT("default", "default", "off");

        public final String primary;
        public final String secondary;
        public final String privateDnsHost;

        DnsMode(String primary, String secondary, String privateDnsHost) {
            this.primary = primary;
            this.secondary = secondary;
            this.privateDnsHost = privateDnsHost;
        }
    }

    public static boolean applyGamingDns(Context context, DnsMode mode) {
        if (mode == DnsMode.SYSTEM_DEFAULT) {
            CommandExecutor.executeSystemCommand("settings put global private_dns_mode off");
            return true;
        }

        // Set private DNS mode to opportunistic / hostname
        CommandExecutor.executeSystemCommand("settings put global private_dns_mode hostname");
        CommandExecutor.executeSystemCommand("settings put global private_dns_specifier " + mode.privateDnsHost);

        // System property DNS fallback
        CommandExecutor.executeSystemCommand("setprop net.dns1 " + mode.primary);
        CommandExecutor.executeSystemCommand("setprop net.dns2 " + mode.secondary);

        // TCP buffer tuning for gaming
        optimizeTcpBuffers();
        return true;
    }

    public static boolean flushDnsCache() {
        String res = CommandExecutor.executeSystemCommand("ndc resolver flushdefaultif; ndc resolver flushnet wlan0; ip route flush cache");
        return CommandExecutor.isSuccessOutput(res);
    }

    public static void optimizeTcpBuffers() {
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.lte 524288,1048576,2097152,262144,524288,1048576");
    }
}
