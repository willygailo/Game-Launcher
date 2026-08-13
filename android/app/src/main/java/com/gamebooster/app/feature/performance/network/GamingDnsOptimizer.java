package com.gamebooster.app.feature.performance.network;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.platform.shizuku.ShizukuExecutor;
import com.gamebooster.app.platform.shell.CommandExecutor;

/**
 * GamingDnsOptimizer configures ultra-low latency Gaming Private DNS
 * (Cloudflare 1.1.1.1 or Quad9) via Shizuku shell to reduce ping jitter during gaming.
 */
public class GamingDnsOptimizer {

    private static final String TAG = "GamingDnsOptimizer";
    public static final String CLOUDFLARE_DNS_HOSTNAME = "1dot1dot1dot1.cloudflare-dns.com";
    public static final String GOOGLE_DNS_HOSTNAME = "dns.google";

    public static boolean enableGamingDns(String hostname) {
        String targetHost = (hostname == null || hostname.trim().isEmpty()) ? CLOUDFLARE_DNS_HOSTNAME : hostname.trim();
        Log.i(TAG, "Enabling Gaming Private DNS: " + targetHost);

        boolean setMode = CommandExecutor.setSystemSetting("global", "private_dns_mode", "hostname");
        boolean setSpec = CommandExecutor.setSystemSetting("global", "private_dns_specifier", targetHost);

        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                ShizukuExecutor.executeShizukuCommand("cmd connectivity set-private-dns-mode hostname " + targetHost);
            } catch (Throwable ignored) {}
        }

        return setMode && setSpec;
    }

    public static boolean revertPrivateDns() {
        Log.i(TAG, "Reverting Private DNS mode to opportunistic default");
        boolean setMode = CommandExecutor.setSystemSetting("global", "private_dns_mode", "opportunistic");
        CommandExecutor.setSystemSetting("global", "private_dns_specifier", "");

        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                ShizukuExecutor.executeShizukuCommand("cmd connectivity set-private-dns-mode opportunistic");
            } catch (Throwable ignored) {}
        }

        return setMode;
    }
}
