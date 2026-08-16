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
        // Wi-Fi 5GHz/6GHz TCP Buffer Tuning
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576");
        // 4G LTE Buffer Tuning
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.lte 524288,1048576,2097152,262144,524288,1048576");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.mobile 524288,1048576,2097152,262144,524288,1048576");
        // 5G / 6G NR (New Radio) Low Latency Buffer Tuning
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.5g 524288,1048576,4194304,262144,524288,2097152");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.nr 524288,1048576,4194304,262144,524288,2097152");
        CommandExecutor.executeSystemCommand("setprop net.tcp.delack.mode 1");
    }

    /**
     * Enables 5G/6G Mobile Data + Wi-Fi Dual Acceleration.
     * Keeps cellular data active during Wi-Fi connection to allow instantaneous handover
     * and multipath dual-connection acceleration for zero packet loss during gaming.
     */
    public static boolean setDualDataAndWifiAcceleration(boolean enabled) {
        boolean ok = true;
        if (enabled) {
            // Keep mobile data on for dual acceleration & zero packet-drop handoff
            CommandExecutor.executeSystemCommand("settings put global mobile_data_always_on 1");
            // Force Wi-Fi chip into low-latency mode (Android 10+)
            CommandExecutor.executeSystemCommand("cmd wifi force-low-latency-mode enabled");
            // Wi-Fi High Performance Active Power Mode (disable sleep policy)
            CommandExecutor.executeSystemCommand("cmd wlan set-power-mode 0 || settings put global wifi_sleep_policy 2");
            // Optimize 5G/6G & Wi-Fi TCP buffers
            optimizeTcpBuffers();
        } else {
            CommandExecutor.executeSystemCommand("settings put global mobile_data_always_on 0");
            CommandExecutor.executeSystemCommand("cmd wifi force-low-latency-mode disabled");
            CommandExecutor.executeSystemCommand("cmd wlan set-power-mode 2 || settings put global wifi_sleep_policy 0");
        }
        return ok;
    }

    public static boolean setTetheringHwAcceleration(boolean enabled) {
        String res = CommandExecutor.executeSystemCommand("settings put global tether_offload_disabled " + (enabled ? "0" : "1"));
        return CommandExecutor.isSuccessOutput(res);
    }

    public static boolean setForceFullGnss(boolean enabled) {
        CommandExecutor.executeSystemCommand("settings put global development_settings_enabled 1");
        String res = CommandExecutor.executeSystemCommand("settings put global force_gnss_raw_measurements " + (enabled ? "1" : "0"));
        return CommandExecutor.isSuccessOutput(res);
    }
}
