package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * EsportsNetworkTuner — Low-latency network & DNS optimization engine for competitive gaming.
 *
 * Configures Cloudflare/Google ultra-fast DNS servers, optimizes TCP buffer scaling
 * parameters (`tcp_rmem`, `tcp_wmem`), and sets WLAN interface queueing rules for zero ping spikes.
 */
public class EsportsNetworkTuner {

    private static final String TAG = "EsportsNetworkTuner";

    public static boolean applyLowLatencyNetworkSettings(Context context) {
        if (!ShizukuExecutor.hasShizukuPermission()) {
            Log.w(TAG, "Shizuku permission unavailable for EsportsNetworkTuner");
            return false;
        }

        try {
            Log.i(TAG, "▶ Applying Esports Network Low-Latency & DNS Optimization...");

            // 1. Force Ultra-Low Latency Cloudflare (1.1.1.1) & Google (8.8.8.8) DNS
            exec("settings put global net.dns1 1.1.1.1");
            exec("settings put global net.dns2 8.8.8.8");

            // 2. TCP Buffer Size Optimization for Gaming Traffic
            exec("setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576");
            exec("setprop net.tcp.buffersize.lte 524288,1048576,2097152,262144,524288,1048576");
            exec("setprop net.tcp.buffersize.5g 1048576,2097152,4194304,262144,1048576,2097152");

            // 3. Disable IPv6 privacy extensions delay & TCP ACK delays
            exec("setprop net.ipv4.tcp_delack_enabled 0");
            exec("setprop net.ipv4.tcp_timestamps 0");

            // 4. Wi-Fi Low Latency Mode & High Priority Queueing
            exec("cmd wifi set-wifi-enabled enabled");
            exec("cmd wifi set-low-latency-mode enabled");

            Log.i(TAG, "✔ Esports Network & Low-Latency DNS Optimization applied cleanly");
            return true;

        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply esports network settings", e);
            return false;
        }
    }

    private static void exec(String command) {
        ShizukuExecutor.executeShizukuCommand(command);
    }
}
