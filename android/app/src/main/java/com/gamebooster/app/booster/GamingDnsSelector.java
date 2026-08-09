package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

public class GamingDnsSelector {

    private static final String TAG = "GamingDnsSelector";

    public enum DnsMode {
        DEFAULT("System Standard", "AOSP System Default", "", ""),
        CLOUDFLARE("Cloudflare 1.1.1.1", "Ultra-Low Latency Gaming", "1.1.1.1", "1.0.0.1"),
        GOOGLE("Google 8.8.8.8", "Global High Availability", "8.8.8.8", "8.8.4.4"),
        QUAD9("Quad9 9.9.9.9", "DDoS Protected Gaming", "9.9.9.9", "149.112.112.112"),
        ADGUARD("AdGuard Gaming", "Ad & Tracker Blocking", "94.140.14.14", "94.140.15.15");

        public final String label;
        public final String description;
        public final String primaryDns;
        public final String secondaryDns;

        DnsMode(String label, String description, String primaryDns, String secondaryDns) {
            this.label = label;
            this.description = description;
            this.primaryDns = primaryDns;
            this.secondaryDns = secondaryDns;
        }
    }

    public static String applyDnsMode(Context context, DnsMode mode) {
        if (context == null || mode == null) return "Invalid context or mode";

        if (mode == DnsMode.DEFAULT) {
            if (ShizukuExecutor.isShizukuAvailable()) {
                ShizukuExecutor.executeShizukuCommand("setprop net.dns1 \"\"");
                ShizukuExecutor.executeShizukuCommand("setprop net.dns2 \"\"");
                ShizukuExecutor.executeShizukuCommand("cmd connectivity set-private-dns-mode automatic");
            }
            return "✅ Reset DNS to System Standard Default";
        }

        try {
            if (ShizukuExecutor.isShizukuAvailable()) {
                ShizukuExecutor.executeShizukuCommand("setprop net.dns1 " + mode.primaryDns);
                ShizukuExecutor.executeShizukuCommand("setprop net.dns2 " + mode.secondaryDns);
                ShizukuExecutor.executeShizukuCommand("cmd connectivity set-private-dns-mode hostname " + mode.primaryDns);
                Log.i(TAG, "Applied " + mode.label + " (" + mode.primaryDns + ", " + mode.secondaryDns + ")");
                return "⚡ Applied " + mode.label + " (" + mode.primaryDns + ")";
            } else {
                return "⚠️ Shizuku required to switch system DNS servers dynamically";
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply DNS mode", e);
            return "❌ Failed to set DNS: " + e.getMessage();
        }
    }
}
