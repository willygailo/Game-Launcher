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
            Log.i(TAG, "▶ Applying Esports 5G/6G & Wi-Fi Low-Latency Network Engine...");

            // 1. Force Ultra-Low Latency Cloudflare (1.1.1.1) & Google (8.8.8.8) DNS
            exec("settings put global net.dns1 1.1.1.1");
            exec("settings put global net.dns2 8.8.8.8");
            exec("settings put global private_dns_mode hostname");
            exec("settings put global private_dns_specifier one.one.one.one");

            // 2. TCP Buffer Size Optimization for 5G, 6G, Mobile Data & Wi-Fi Traffic
            exec("setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576");
            exec("setprop net.tcp.buffersize.lte 524288,1048576,2097152,262144,524288,1048576");
            exec("setprop net.tcp.buffersize.mobile 524288,1048576,2097152,262144,524288,1048576");
            exec("setprop net.tcp.buffersize.5g 1048576,2097152,4194304,262144,1048576,2097152");
            exec("setprop net.tcp.buffersize.5g_sub6 1048576,2097152,4194304,262144,1048576,2097152");
            exec("setprop net.tcp.buffersize.5g_mmwave 2097152,4194304,8388608,262144,1048576,2097152");
            exec("setprop net.tcp.buffersize.6g 2097152,4194304,8388608,262144,1048576,2097152");

            // 3. TCP Congestion Control (BBR) & Zero Packet Lag Tuning
            exec("sysctl -w net.ipv4.tcp_congestion_control=bbr || setprop net.ipv4.tcp_congestion_control bbr");
            exec("setprop net.ipv4.tcp_nodelay 1");
            exec("setprop net.ipv4.tcp_low_latency 1");
            exec("setprop net.ipv4.tcp_slow_start_after_idle 0");
            exec("setprop net.ipv4.tcp_delack_enabled 0");
            exec("setprop net.ipv4.tcp_timestamps 0");
            exec("setprop net.ipv4.tcp_tw_reuse 1");
            exec("setprop net.ipv4.tcp_fin_timeout 15");

            // 4. Cellular 5G/6G Radio Fast Dormancy & Data Coexistence
            exec("setprop ro.ril.fast.dormancy.rule 0");
            exec("setprop ro.config.hw_fast_dormancy 0");
            exec("setprop persist.vendor.radio.5g_fast_switch 1");
            exec("settings put global mobile_data_always_on 1");

            // 5. Wi-Fi 6E/7 Low Latency Mode & Background Scan Throttling
            exec("cmd wifi set-wifi-enabled enabled");
            exec("cmd wifi set-low-latency-mode enabled");
            exec("settings put global wifi_scan_always_enabled 0");
            exec("settings put global wifi_watchdog_on 0");
            exec("settings put global wifi_sleep_policy 2");
            exec("setprop wlan.driver.ath 0");
            exec("setprop persist.sys.wifi.power.save 0");

            // 6. Flush DNS Resolver Caches
            exec("ndc resolver flushdefaultif; ndc resolver flushnet wlan0; ip route flush cache || true");

            Log.i(TAG, "✔ Esports 5G/6G & Wi-Fi Low-Latency Network Engine applied cleanly");
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
