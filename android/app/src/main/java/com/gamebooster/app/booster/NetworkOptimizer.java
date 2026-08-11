package com.gamebooster.app.booster;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class NetworkOptimizer {

    private static final String TAG = "NetworkOptimizer";

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
            ShizukuExecutor.executeShizukuCommand("settings put global private_dns_mode off");
            return true;
        }

        CommandExecutor.executeSystemCommand("settings put global private_dns_mode hostname");
        CommandExecutor.executeSystemCommand("settings put global private_dns_specifier " + mode.privateDnsHost);
        ShizukuExecutor.executeShizukuCommand("settings put global private_dns_mode hostname");
        ShizukuExecutor.executeShizukuCommand("settings put global private_dns_specifier " + mode.privateDnsHost);

        optimizeNetworkStack(context);
        return true;
    }

    public static void optimizeNetworkStack(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
        
        if (nc != null) {
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                optimizeTcpBuffers("wifi");
            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                if (nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING)) {
                    optimizeTcpBuffers("5g");
                } else {
                    optimizeTcpBuffers("lte");
                }
            }
        }
    }

    private static void optimizeTcpBuffers(String type) {
        String base = "524288,1048576,2097152,262144,524288,1048576";
        String fast = "1048576,2097152,4194304,524288,1048576,2097152";
        String values = type.equals("5g") ? fast : base;
        
        CommandExecutor.executeSystemCommand("setprop net.tcp.buffersize." + type + " " + values);
        ShizukuExecutor.executeShizukuCommand("setprop net.tcp.buffersize." + type + " " + values);
    }

    public static boolean enableWifiLowLatencyGameMode() {
        Log.d(TAG, "Enabling Low-Latency WiFi Gaming Mode");
        CommandExecutor.executeSystemCommand("cmd wifi force-low-latency-mode enable");
        ShizukuExecutor.executeShizukuCommand("cmd wifi force-low-latency-mode enable");

        CommandExecutor.executeSystemCommand("settings put global wifi_scan_throttle_enabled 0");
        ShizukuExecutor.executeShizukuCommand("settings put global wifi_scan_throttle_enabled 0");
        return true;
    }

    public static boolean disableWifiLowLatencyGameMode() {
        Log.d(TAG, "Disabling Low-Latency WiFi Gaming Mode");
        CommandExecutor.executeSystemCommand("cmd wifi force-low-latency-mode disable");
        ShizukuExecutor.executeShizukuCommand("cmd wifi force-low-latency-mode disable");

        CommandExecutor.executeSystemCommand("settings put global wifi_scan_throttle_enabled 1");
        ShizukuExecutor.executeShizukuCommand("settings put global wifi_scan_throttle_enabled 1");
        return true;
    }

    public static boolean flushDnsCache() {
        String res = CommandExecutor.executeSystemCommand("ndc resolver flushdefaultif; ndc resolver flushnet wlan0; ip route flush cache");
        return CommandExecutor.isSuccessOutput(res);
    }

    public static boolean setTetheringHwAcceleration(boolean enabled) {
        String res = CommandExecutor.executeSystemCommand("settings put global tether_offload_disabled " + (enabled ? "0" : "1"));
        ShizukuExecutor.executeShizukuCommand("settings put global tether_offload_disabled " + (enabled ? "0" : "1"));
        return CommandExecutor.isSuccessOutput(res);
    }

    public static boolean setForceFullGnss(boolean enabled) {
        CommandExecutor.executeSystemCommand("settings put global development_settings_enabled 1");
        String res = CommandExecutor.executeSystemCommand("settings put global force_gnss_raw_measurements " + (enabled ? "1" : "0"));
        ShizukuExecutor.executeShizukuCommand("settings put global force_gnss_raw_measurements " + (enabled ? "1" : "0"));
        return CommandExecutor.isSuccessOutput(res);
    }

    public static int measureNetworkPingMs() {
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 -w 2 1.1.1.1");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("time=")) {
                    int index = line.indexOf("time=");
                    String timePart = line.substring(index + 5);
                    int spaceIndex = timePart.indexOf(" ");
                    if (spaceIndex > 0) timePart = timePart.substring(0, spaceIndex);
                    return Math.round(Float.parseFloat(timePart));
                }
            }
            process.waitFor();
        } catch (Exception e) {
            Log.w(TAG, "Ping measurement error", e);
        }
        return -1;
    }
}
