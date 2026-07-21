package com.gamelauncher.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NetworkPingStabilizer — locks low-latency Wi-Fi / 5G parameters and restricts
 * background data drain during gaming sessions.
 */
@Singleton
class NetworkPingStabilizer @Inject constructor(
    private val shizukuShellManager: ShizukuShellManager
) {
    companion object {
        private const val TAG = "NetworkPingStabilizer"
        const val DNS_CLOUDFLARE = "one.one.one.one"
        const val DNS_GOOGLE = "dns.google"
        const val DNS_QUAD9 = "dns.quad9.net"
    }

    private var wifiLock: Any? = null

    /**
     * Enables low-latency Wi-Fi lock and sets gaming Private DNS via Shizuku.
     */
    suspend fun enablePingStabilization(context: Context, preferredDns: String = DNS_CLOUDFLARE) {
        // 1. Set low-latency Private DNS via Shizuku/ADB if available
        if (shizukuShellManager.isAvailable()) {
            shizukuShellManager.executeAny(
                listOf(
                    "settings put global private_dns_mode hostname",
                    "settings put global private_dns_specifier $preferredDns",
                    "cmd netpolicy set restrict-background true"
                )
            )
            Log.d(TAG, "Applied private DNS: $preferredDns and background network policy lock")
        }

        // 2. Acquire Wi-Fi low latency lock
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
            if (wifiManager != null && wifiLock == null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    wifiLock = wifiManager.createWifiLock(
                        android.net.wifi.WifiManager.WIFI_MODE_FULL_LOW_LATENCY,
                        "GameLauncherLowLatency"
                    ).apply {
                        acquire()
                    }
                    Log.d(TAG, "Wi-Fi Full Low Latency Lock acquired")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire Wi-Fi Low Latency Lock", e)
        }
    }

    /**
     * Restores default background network policy when game session ends.
     */
    suspend fun disablePingStabilization() {
        if (shizukuShellManager.isAvailable()) {
            shizukuShellManager.executeCommand("cmd netpolicy set restrict-background false")
        }

        try {
            (wifiLock as? android.net.wifi.WifiManager.WifiLock)?.let {
                if (it.isHeld) it.release()
            }
            wifiLock = null
            Log.d(TAG, "Wi-Fi Low Latency Lock released")
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing Wi-Fi lock", e)
        }
    }
}
