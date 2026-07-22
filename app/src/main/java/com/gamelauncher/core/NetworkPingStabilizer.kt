// app/src/main/java/com/gamelauncher/core/NetworkPingStabilizer.kt
package com.gamelauncher.core

import android.content.Context
import android.util.Log
import com.gamelauncher.core.shizuku.IShellExecutor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NetworkPingStabilizer — locks low-latency Wi-Fi / 5G parameters and restricts
 * background data drain during gaming sessions using IShellExecutor AIDL.
 */
@Singleton
class NetworkPingStabilizer @Inject constructor(
    private val shellExecutor: IShellExecutor
) {
    companion object {
        private const val TAG = "NetworkPingStabilizer"
        const val DNS_CLOUDFLARE = "one.one.one.one"
        const val DNS_GOOGLE = "dns.google"
        const val DNS_QUAD9 = "dns.quad9.net"
    }

    private var wifiLock: Any? = null

    suspend fun enablePingStabilization(context: Context, preferredDns: String = DNS_CLOUDFLARE) {
        shellExecutor.writeSetting("global", "private_dns_mode", "hostname")
        shellExecutor.writeSetting("global", "private_dns_specifier", preferredDns)

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

    suspend fun disablePingStabilization() {
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
