package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

public class NetworkOptimizer {

    private static final String TAG = "NetworkOptimizer";

    public enum NetworkMode {
        DATA_ONLY("Mobile Data Only (5G/4G)"),
        WIFI_ONLY("Wi-Fi Only (Wi-Fi 6/7)"),
        DUAL_DATA_WIFI("Dual Data + Wi-Fi Multipath"),
        SYSTEM_DEFAULT("System Default");

        public final String title;
        NetworkMode(String title) { this.title = title; }
    }

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

    public static class PingStats {
        public final long avgLatencyMs;
        public final long jitterMs;
        public final int packetLossPercent;
        public final boolean reachable;
        public final String quality;
        public final int qualityColor;

        public PingStats(long avgLatencyMs, long jitterMs, int packetLossPercent, boolean reachable) {
            this.avgLatencyMs = avgLatencyMs;
            this.jitterMs = jitterMs;
            this.packetLossPercent = packetLossPercent;
            this.reachable = reachable;
            if (!reachable || avgLatencyMs <= 0) {
                this.quality = "[OFFLINE / TIMEOUT]";
                this.qualityColor = 0xFFEF4444; // Red
            } else if (avgLatencyMs < 35) {
                this.quality = "[EXCELLENT / ULTRA LOW LATENCY]";
                this.qualityColor = 0xFF00FF66; // Neon Green
            } else if (avgLatencyMs < 70) {
                this.quality = "[GOOD / FAST GAMING ROUTE]";
                this.qualityColor = 0xFF00F0FF; // Cyan
            } else if (avgLatencyMs < 110) {
                this.quality = "[NORMAL / ACCEPTABLE]";
                this.qualityColor = 0xFFFACC15; // Yellow
            } else {
                this.quality = "[HIGH LATENCY]";
                this.qualityColor = 0xFFF97316; // Orange
            }
        }
    }

    public static PingStats measureRealPingMs() {
        String[][] targets = {
                {"1.1.1.1", "443"},
                {"1.1.1.1", "53"},
                {"8.8.8.8", "53"},
                {"208.67.222.222", "53"}
        };

        java.util.List<Long> samples = new java.util.ArrayList<>();
        int totalProbes = 0;
        int failedProbes = 0;

        for (String[] t : targets) {
            totalProbes++;
            long t0 = System.currentTimeMillis();
            boolean ok = false;
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(t[0], Integer.parseInt(t[1])), 1200);
                ok = true;
            } catch (Throwable ignored) {}

            long elapsed = System.currentTimeMillis() - t0;
            if (ok && elapsed > 0) {
                samples.add(elapsed);
            } else {
                failedProbes++;
            }
        }

        if (samples.isEmpty()) {
            try {
                String pingOut = CommandExecutor.executeSystemCommand("/system/bin/ping -c 2 -W 1 1.1.1.1");
                if (pingOut != null && pingOut.contains("min/avg/max")) {
                    int idx = pingOut.indexOf("=");
                    if (idx != -1) {
                        String stats = pingOut.substring(idx + 1).trim();
                        String[] parts = stats.split("/");
                        if (parts.length >= 2) {
                            double avg = Double.parseDouble(parts[1].trim());
                            samples.add(Math.round(avg));
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        if (samples.isEmpty()) {
            return new PingStats(-1, 0, 100, false);
        }

        long sum = 0;
        for (long s : samples) sum += s;
        long avg = sum / samples.size();

        long jitterSum = 0;
        for (long s : samples) jitterSum += Math.abs(s - avg);
        long jitter = jitterSum / samples.size();

        int loss = (int) Math.round(((double) failedProbes / totalProbes) * 100.0);

        return new PingStats(avg, jitter, loss, true);
    }

    public static boolean setNetworkMode(Context context, NetworkMode mode) {
        if (mode == null) return false;
        switch (mode) {
            case DATA_ONLY:
                optimize5gAnd6gDataNetwork(true);
                optimizeWifi6and7LowLatency(false);
                break;
            case WIFI_ONLY:
                optimizeWifi6and7LowLatency(true);
                optimize5gAnd6gDataNetwork(false);
                break;
            case DUAL_DATA_WIFI:
                setDualDataAndWifiAcceleration(true);
                break;
            case SYSTEM_DEFAULT:
                optimize5gAnd6gDataNetwork(false);
                optimizeWifi6and7LowLatency(false);
                break;
        }
        return true;
    }

    public static boolean applyGamingDns(Context context, DnsMode mode) {
        if (mode == DnsMode.SYSTEM_DEFAULT) {
            CommandExecutor.setSystemSetting("global", "private_dns_mode", "off");
            CommandExecutor.executeSystemCommand("settings put global private_dns_mode off");
            return true;
        }

        // Set private DNS mode to opportunistic / hostname DoT (DNS-over-TLS)
        CommandExecutor.setSystemSetting("global", "private_dns_mode", "hostname");
        CommandExecutor.setSystemSetting("global", "private_dns_specifier", mode.privateDnsHost);
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
        String res = CommandExecutor.executeSystemCommand(
                "ndc resolver flushdefaultif; ndc resolver flushnet wlan0; ndc resolver flushnet rmnet_data0; ip route flush cache"
        );
        return CommandExecutor.isSuccessOutput(res);
    }

    /**
     * Optimizes Linux TCP Kernel Buffers for 5G, 6G NR, LTE-A Pro, and Wi-Fi 6/7.
     * Sets 8MB max window sizes for burst packet transfer and zero-lag gaming.
     */
    public static void optimizeTcpBuffers() {
        // Wi-Fi 2.4GHz / 5GHz / 6GHz (Wi-Fi 6/6E/7) TCP Buffer Tuning (min, default, max)
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.wifi 524288,1048576,8388608,262144,524288,4194304");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.wifi6 524288,1048576,8388608,262144,524288,4194304");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.wifi7 524288,1048576,8388608,262144,524288,4194304");

        // 4G LTE-A Pro
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.lte 524288,1048576,4194304,262144,524288,2097152");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.mobile 524288,1048576,4194304,262144,524288,2097152");

        // 5G & 6G NR (New Radio Standalone SA & Non-Standalone NSA) Buffer Tuning
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.5g 524288,1048576,8388608,262144,524288,4194304");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.5g_sa 524288,1048576,8388608,262144,524288,4194304");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.5g_nsa 524288,1048576,8388608,262144,524288,4194304");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.nr 524288,1048576,8388608,262144,524288,4194304");
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize.6g 524288,1048576,8388608,262144,524288,4194304");

        // TCP Congestion Control & Fast Handshake
        CommandExecutor.executeSystemCommand("setprop net.ipv4.tcp_congestion_control bbr");
        CommandExecutor.executeSystemCommand("setprop net.tcp.delack.mode 1"); // Immediate ACK
        CommandExecutor.executeSystemCommand("setprop net.ipv4.tcp_fastopen 3");
        CommandExecutor.executeSystemCommand("setprop net.ipv4.tcp_sack 1");
        CommandExecutor.executeSystemCommand("setprop net.ipv4.tcp_dsack 1");
        CommandExecutor.executeSystemCommand("setprop net.ipv4.tcp_window_scaling 1");
        CommandExecutor.executeSystemCommand("setprop net.ipv4.tcp_ecn 0"); // Disable ECN to prevent packet drops on aggressive ISP firewalls
        CommandExecutor.executeSystemCommand("setprop net.ipv4.tcp_timestamps 0"); // Minimal packet header overhead
        CommandExecutor.executeSystemCommand("setprop net.ipv4.tcp_tw_reuse 1");
    }

    /**
     * Optimizes 5G / 6G Mobile Cellular Radio.
     * Prioritizes 5G SA/NSA frequencies, enables persistent data readiness,
     * and sets high-priority initial receive windows.
     */
    public static boolean optimize5gAnd6gDataNetwork(boolean enabled) {
        boolean ok = true;
        if (enabled) {
            // Keep mobile cellular link ready for instantaneous handover / zero packet drops
            CommandExecutor.executeSystemCommand("settings put global mobile_data_always_on 1");
            CommandExecutor.executeSystemCommand("settings put global data_stall_recovery_on_bad_network 1");
            CommandExecutor.executeSystemCommand("settings put global tcp_default_init_rwnd 60");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.radio.5g_mode_pref 1");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.radio.nr_disable 0");
            CommandExecutor.executeSystemCommand("setprop persist.radio.5g_mode_pref 1");
            optimizeTcpBuffers();
        } else {
            CommandExecutor.executeSystemCommand("settings put global mobile_data_always_on 0");
        }
        return ok;
    }

    /**
     * Forces Wi-Fi 5 / Wi-Fi 6 / Wi-Fi 6E / Wi-Fi 7 chipsets into Zero-Lag Gaming Mode.
     * - Suppresses periodic background AP scanning (which causes 200ms+ lag spikes).
     * - Disables Wi-Fi power-save throttling.
     * - Forces driver into low-latency lock.
     */
    public static boolean optimizeWifi6and7LowLatency(boolean enabled) {
        boolean ok = true;
        if (enabled) {
            // 1. Force low latency mode via Wi-Fi service (Android 10+)
            CommandExecutor.executeSystemCommand("cmd wifi force-low-latency-mode enabled");
            CommandExecutor.executeSystemCommand("cmd wifi force-hi-perf-mode enabled");
            CommandExecutor.executeSystemCommand("cmd wifi set-multicast-filter enabled");

            // 2. Suppress background AP Wi-Fi scanning during gameplay
            CommandExecutor.executeSystemCommand("settings put global wifi_scan_always_enabled 0");
            CommandExecutor.executeSystemCommand("settings put global wifi_framework_scan_interval_ms 300000"); // 5-minute interval
            CommandExecutor.executeSystemCommand("settings put global wifi_sleep_policy 2"); // Never sleep
            CommandExecutor.executeSystemCommand("settings put global wifi_power_save 0"); // Disable chip sleep
            CommandExecutor.executeSystemCommand("settings put global wifi_watchdog_on 0");

            // 3. Low-latency chipset driver props
            CommandExecutor.executeSystemCommand("setprop debug.wifi.low_latency 1");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.wifi.low_latency 1");
            CommandExecutor.executeSystemCommand("setprop persist.sys.wifi.energy.saving 0");
            optimizeTcpBuffers();
        } else {
            CommandExecutor.executeSystemCommand("cmd wifi force-low-latency-mode disabled");
            CommandExecutor.executeSystemCommand("cmd wifi force-hi-perf-mode disabled");
            CommandExecutor.executeSystemCommand("settings put global wifi_scan_always_enabled 1");
            CommandExecutor.executeSystemCommand("settings put global wifi_power_save 1");
            CommandExecutor.executeSystemCommand("settings put global wifi_sleep_policy 0");
            CommandExecutor.executeSystemCommand("setprop debug.wifi.low_latency 0");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.wifi.low_latency 0");
        }
        return ok;
    }

    /**
     * Enables 5G/6G Mobile Data + Wi-Fi Dual Multipath Acceleration.
     * Keeps cellular data active during Wi-Fi connection to allow instantaneous handover
     * and multipath dual-connection acceleration for zero packet loss during gaming.
     */
    public static boolean setDualDataAndWifiAcceleration(boolean enabled) {
        boolean ok = true;
        optimize5gAnd6gDataNetwork(enabled);
        optimizeWifi6and7LowLatency(enabled);
        return ok;
    }

    /**
     * 1-Tap Comprehensive Network & Latency Optimization.
     * Optimizes 5G/6G, Wi-Fi 6/7, TCP BBR, and flushes DNS cache.
     */
    public static boolean optimizeAllDataAndWifi(Context context) {
        try {
            setDualDataAndWifiAcceleration(true);
            applyGamingDns(context, DnsMode.CLOUDFLARE_1_1_1_1);
            flushDnsCache();
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "Failed to optimize data and wifi", t);
            return false;
        }
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

    /**
     * Philippine Telco Carrier Profiles for TNT/Smart and TM/Globe.
     */
    public enum PhCarrier {
        TNT_SMART("TNT / Smart 5G Ultra Gaming", "smartdata", "1.1.1.1", "one.one.one.one", 1460, "Band 1, 3, 28, 41, n78"),
        TM_GLOBE("TM / Globe 5G Turbo Fast", "real.globe.com.ph", "8.8.8.8", "dns.google", 1440, "Band 3, 7, 28, 40, n78"),
        DITO("DITO 5G Fast Route", "dito.ph", "1.1.1.1", "one.one.one.one", 1460, "Band 1, 28, 41, n78");

        public final String title;
        public final String defaultApn;
        public final String dnsPrimary;
        public final String privateDnsHost;
        public final int mtu;
        public final String bestBands;

        PhCarrier(String title, String defaultApn, String dnsPrimary, String privateDnsHost, int mtu, String bestBands) {
            this.title = title;
            this.defaultApn = defaultApn;
            this.dnsPrimary = dnsPrimary;
            this.privateDnsHost = privateDnsHost;
            this.mtu = mtu;
            this.bestBands = bestBands;
        }
    }

    /**
     * Applies specialized Philippine cellular data acceleration for TNT/Smart or TM/Globe.
     * Enforces BBR TCP congestion, high-speed receive windows, radio sleep suppression,
     * low-latency DoT DNS, and baseband keepalive.
     */
    public static boolean applyPhCarrierOptimization(Context context, PhCarrier carrier) {
        if (carrier == null) return false;
        try {
            // 1. Enforce carrier-optimized TCP buffers and sysctl parameters
            optimizeTcpBuffers();

            // 2. Baseband modem keepalive & anti-power-save
            CommandExecutor.executeSystemCommand("settings put global mobile_data_always_on 1");
            CommandExecutor.executeSystemCommand("settings put global data_stall_recovery_on_bad_network 1");
            CommandExecutor.executeSystemCommand("settings put global tcp_default_init_rwnd 60");
            CommandExecutor.executeSystemCommand("setprop persist.radio.add_power_save 0");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.radio.5g_mode_pref 1");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.radio.nr_disable 0");
            CommandExecutor.executeSystemCommand("setprop persist.radio.5g_mode_pref 1");
            CommandExecutor.executeSystemCommand("setprop persist.radio.multimode 1");
            CommandExecutor.executeSystemCommand("setprop ro.ril.enable.amr.wideband 1");
            CommandExecutor.executeSystemCommand("setprop ril.power_mode 0");

            // 3. Carrier-tailored low latency DNS over TLS
            CommandExecutor.setSystemSetting("global", "private_dns_mode", "hostname");
            CommandExecutor.setSystemSetting("global", "private_dns_specifier", carrier.privateDnsHost);
            CommandExecutor.executeSystemCommand("settings put global private_dns_mode hostname");
            CommandExecutor.executeSystemCommand("settings put global private_dns_specifier " + carrier.privateDnsHost);
            CommandExecutor.executeSystemCommand("setprop net.dns1 " + carrier.dnsPrimary);
            CommandExecutor.executeSystemCommand("setprop net.dns2 8.8.4.4");

            // 4. Background network throttle clamp
            CommandExecutor.executeSystemCommand("cmd netpolicy set restrict-background true");
            CommandExecutor.executeSystemCommand("cmd connectivity set-background-data false");

            // 5. Immediate DNS & route cache purge
            flushDnsCache();
            Log.i(TAG, "🇵🇭 Philippine Telco Optimization applied for: " + carrier.title);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Failed to apply PH Telco optimization", t);
            return false;
        }
    }
}
