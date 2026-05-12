package com.gamelauncher.core

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages WiFi low-latency lock for online gaming.
 * WIFI_MODE_FULL_LOW_LATENCY disables power save, dramatically cuts ping.
 */
@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wifiManager = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as WifiManager?

    private var wifiLock: WifiManager.WifiLock? = null

    fun acquireWifiLowLatencyLock(tag: String = "GameLauncher:WifiLock") {
        try {
            if (wifiLock?.isHeld == true) return
            val lockType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                WifiManager.WIFI_MODE_FULL_LOW_LATENCY
            else
                @Suppress("DEPRECATION") WifiManager.WIFI_MODE_FULL_HIGH_PERF

            wifiLock = wifiManager?.createWifiLock(lockType, tag)
            wifiLock?.setReferenceCounted(false)
            wifiLock?.acquire()
        } catch (_: Exception) {}
    }

    fun releaseWifiLock() {
        try {
            if (wifiLock?.isHeld == true) wifiLock?.release()
            wifiLock = null
        } catch (_: Exception) {}
    }

    fun isWifiLockHeld(): Boolean = wifiLock?.isHeld ?: false

    @Suppress("DEPRECATION")
    fun getWifiLinkSpeedMbps(): Int {
        return try {
            wifiManager?.connectionInfo?.linkSpeed ?: 0
        } catch (_: Exception) { 0 }
    }

    @Suppress("DEPRECATION")
    fun getWifiSignalDbm(): Int {
        return try {
            wifiManager?.connectionInfo?.rssi ?: -100
        } catch (_: Exception) { -100 }
    }

    fun getNetworkType(): String {
        return try {
            val cm = context.getSystemService(android.net.ConnectivityManager::class.java)
            val nc = cm?.getNetworkCapabilities(cm.activeNetwork)
            when {
                nc == null -> "Offline"
                nc.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)     -> "WiFi"
                nc.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
                nc.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Unknown"
            }
        } catch (_: Exception) { "Unknown" }
    }
}
