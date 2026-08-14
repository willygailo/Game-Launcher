package com.gamebooster.app.feature.performance.network;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.platform.shizuku.ShizukuExecutor;
import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * GamingDnsOptimizer configures ultra-low latency Gaming Private DNS
 * (Cloudflare 1.1.1.1, Quad9, Google, or AdGuard) via Shizuku shell to reduce ping jitter during gaming.
 */
public class GamingDnsOptimizer {

    private static final String TAG = "GamingDnsOptimizer";

    public static final String CLOUDFLARE_DNS_HOSTNAME = "1dot1dot1dot1.cloudflare-dns.com";
    public static final String GOOGLE_DNS_HOSTNAME = "dns.google";
    public static final String QUAD9_DNS_HOSTNAME = "dns.quad9.net";
    public static final String ADGUARD_DNS_HOSTNAME = "dns.adguard-dns.com";

    public enum DnsPreset {
        CLOUDFLARE("Cloudflare 1.1.1.1 (eSports / Fast)", CLOUDFLARE_DNS_HOSTNAME),
        GOOGLE("Google Public DNS (Global Low Latency)", GOOGLE_DNS_HOSTNAME),
        QUAD9("Quad9 DNS (Secure / Anti-Jitter)", QUAD9_DNS_HOSTNAME),
        ADGUARD("AdGuard Gaming (Ad & Tracker Block)", ADGUARD_DNS_HOSTNAME);

        public final String label;
        public final String hostname;

        DnsPreset(String label, String hostname) {
            this.label = label;
            this.hostname = hostname;
        }
    }

    /**
     * Enables gaming DNS using default Cloudflare hostname.
     */
    public static boolean enableGamingDns(String hostname) {
        return enableGamingDns(hostname, null);
    }

    /**
     * Enables gaming DNS and acquires low-latency Wi-Fi lock if context is provided.
     *
     * @param hostname Target DNS-over-TLS hostname.
     * @param context Application context for Wi-Fi lock (optional).
     * @return true if DNS settings were applied.
     */
    public static boolean enableGamingDns(String hostname, Context context) {
        String targetHost = (hostname == null || hostname.trim().isEmpty())
                ? CLOUDFLARE_DNS_HOSTNAME
                : hostname.trim();
        Log.i(TAG, "Enabling Gaming Private DNS: " + targetHost);

        boolean setMode = CommandExecutor.setSystemSetting("global", "private_dns_mode", "hostname");
        boolean setSpec = CommandExecutor.setSystemSetting("global", "private_dns_specifier", targetHost);

        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                ShizukuExecutor.executeShizukuCommand("cmd connectivity set-private-dns-mode hostname " + targetHost);
            } catch (Throwable ignored) {}
        }

        if (context != null) {
            WifiLatencyOptimizer.acquireLowLatencyLock(context);
        }

        return setMode && setSpec;
    }

    /**
     * Reverts Private DNS mode to standard opportunistic default and releases Wi-Fi lock.
     */
    public static boolean revertPrivateDns() {
        Log.i(TAG, "Reverting Private DNS mode to opportunistic default");
        boolean setMode = CommandExecutor.setSystemSetting("global", "private_dns_mode", "opportunistic");
        CommandExecutor.setSystemSetting("global", "private_dns_specifier", "");

        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                ShizukuExecutor.executeShizukuCommand("cmd connectivity set-private-dns-mode opportunistic");
            } catch (Throwable ignored) {}
        }

        WifiLatencyOptimizer.releaseLowLatencyLock();

        return setMode;
    }
}
