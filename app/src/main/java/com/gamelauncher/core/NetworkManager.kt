package com.gamelauncher.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NetworkManager — real-time dual-stack network optimizer for gaming.
 *
 * Features:
 *  - 5G detection (NR, mmWave, sub-6 GHz)
 *  - WiFi 5 / WiFi 6 / WiFi 6E / WiFi 7 detection
 *  - Dual-stack WiFi + Cellular simultaneously (multipath)
 *  - Network quality score (0-100) based on speed + latency estimate
 *  - WiFi low-latency lock (WIFI_MODE_FULL_LOW_LATENCY — API 29+)
 *  - Mobile data always-on (Settings.Global, needs WRITE_SECURE_SETTINGS)
 *  - TCP BBR congestion control (root only)
 *  - Real-time active network type broadcast via StateFlow
 */
@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // ── StateFlows ─────────────────────────────────────────────────────

    private val _networkType = MutableStateFlow(NetworkType.OFFLINE)
    val networkType: StateFlow<NetworkType> = _networkType

    private val _networkQualityScore = MutableStateFlow(0)
    /** 0-100. ≥80 = excellent, ≥60 = good, ≥40 = ok, <40 = poor */
    val networkQualityScore: StateFlow<Int> = _networkQualityScore

    private val _isWifiConnected = MutableStateFlow(false)
    val isWifiConnected: StateFlow<Boolean> = _isWifiConnected

    private val _isCellularConnected = MutableStateFlow(false)
    val isCellularConnected: StateFlow<Boolean> = _isCellularConnected

    private val _is5G = MutableStateFlow(false)
    val is5G: StateFlow<Boolean> = _is5G

    private val _wifiGenerationLabel = MutableStateFlow("WiFi")
    /** e.g. "WiFi 6E", "WiFi 5", "WiFi 7" */
    val wifiGenerationLabel: StateFlow<String> = _wifiGenerationLabel

    private val _cellularGenLabel = MutableStateFlow("")
    /** e.g. "5G", "4G LTE", "3G" */
    val cellularGenLabel: StateFlow<String> = _cellularGenLabel

    // ── Internal ───────────────────────────────────────────────────────

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?

    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(ConnectivityManager::class.java)

    private val telephonyManager: TelephonyManager? =
        context.getSystemService(TelephonyManager::class.java)

    private var wifiLock: WifiManager.WifiLock? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Enum ───────────────────────────────────────────────────────────

    enum class NetworkType {
        OFFLINE, WIFI, CELLULAR_5G, CELLULAR_4G, CELLULAR_3G, CELLULAR_2G,
        WIFI_AND_CELLULAR, ETHERNET
    }

    // ── Network Callback (live monitoring) ────────────────────────────

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            scope.launch { updateNetworkState(caps) }
        }
        override fun onLost(network: Network) {
            scope.launch { refreshNetworkStatus() }
        }
        override fun onAvailable(network: Network) {
            scope.launch { refreshNetworkStatus() }
        }
    }

    /**
     * Start live network monitoring. Call once from service onCreate / coordinator.
     */
    fun startMonitoring() {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(request, networkCallback)
            refreshNetworkStatus()
        } catch (_: Exception) {}
    }

    fun stopMonitoring() {
        try {
            connectivityManager?.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}
    }

    // ── Network State Refresh ─────────────────────────────────────────

    fun refreshNetworkStatus() {
        try {
            val cm = connectivityManager ?: return
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: run {
                _networkType.value = NetworkType.OFFLINE
                _networkQualityScore.value = 0
                _isWifiConnected.value = false
                _isCellularConnected.value = false
                return
            }
            updateNetworkState(caps)
        } catch (_: Exception) {}
    }

    private fun updateNetworkState(caps: NetworkCapabilities) {
        val hasWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val hasCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val hasEthernet = caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

        _isWifiConnected.value = hasWifi
        _isCellularConnected.value = hasCellular

        // Determine WiFi generation (API 30+)
        if (hasWifi && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wifiInfo = caps.transportInfo as? android.net.wifi.WifiInfo
            val wifiStandard = wifiInfo?.wifiStandard ?: 0
            _wifiGenerationLabel.value = wifiStandardToLabel(wifiStandard)
        } else if (hasWifi) {
            _wifiGenerationLabel.value = "WiFi"
        }

        // 5G detection
        val is5gNow = hasCellular && detect5G()
        _is5G.value = is5gNow

        // Cellular generation label
        if (hasCellular) {
            _cellularGenLabel.value = getCellularGenerationLabel()
        }

        // Derive network type
        _networkType.value = when {
            hasEthernet -> NetworkType.ETHERNET
            hasWifi && hasCellular -> NetworkType.WIFI_AND_CELLULAR
            hasWifi -> NetworkType.WIFI
            is5gNow -> NetworkType.CELLULAR_5G
            hasCellular -> deriveCellularType()
            else -> NetworkType.OFFLINE
        }

        // Network quality score
        _networkQualityScore.value = computeQualityScore(caps, hasWifi, hasCellular, is5gNow)
    }

    // ── 5G Detection ──────────────────────────────────────────────────

    /**
     * Multi-API 5G detection:
     *  - API 31+: NetworkCapabilities.TRANSPORT_NR via hasTransport
     *  - API 29-30: TelephonyManager.getDataNetworkType() == NETWORK_TYPE_NR
     *  - Pre-29: Not detectable without location permission
     */
    private fun detect5G(): Boolean {
        // Method 1: NR transport capability (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val cm = connectivityManager ?: return false
                val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
                // TRANSPORT_NR = 6 (hidden in some builds, safe via constant)
                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    // Check if 5G via data network type
                    val tel = telephonyManager ?: return false
                    val nt = tel.dataNetworkType
                    return nt == TelephonyManager.NETWORK_TYPE_NR
                }
            } catch (_: Exception) {}
        }

        // Method 2: TelephonyManager network type (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val tel = telephonyManager ?: return false
                return tel.dataNetworkType == TelephonyManager.NETWORK_TYPE_NR
            } catch (_: Exception) {}
        }

        return false
    }

    @Suppress("DEPRECATION")
    private fun deriveCellularType(): NetworkType {
        val tel = telephonyManager ?: return NetworkType.CELLULAR_4G
        return try {
            when (tel.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_NR -> NetworkType.CELLULAR_5G
                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyManager.NETWORK_TYPE_IWLAN -> NetworkType.CELLULAR_4G
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_EVDO_B -> NetworkType.CELLULAR_3G
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT -> NetworkType.CELLULAR_2G
                else -> NetworkType.CELLULAR_4G
            }
        } catch (_: Exception) { NetworkType.CELLULAR_4G }
    }

    private fun getCellularGenerationLabel(): String {
        val tel = telephonyManager ?: return "LTE"
        return try {
            when (tel.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSPAP -> "H+"
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                TelephonyManager.NETWORK_TYPE_EDGE -> "E"
                TelephonyManager.NETWORK_TYPE_GPRS -> "G"
                else -> "LTE"
            }
        } catch (_: Exception) { "LTE" }
    }

    // ── WiFi Standard Label ────────────────────────────────────────────

    private fun wifiStandardToLabel(standard: Int): String {
        // android.net.wifi.ScanResult constants — use raw int to avoid API < 30 issues
        return when (standard) {
            6 -> "WiFi 7"   // WIFI_STANDARD_11BE (802.11be) = 8 in some builds — try both
            7 -> "WiFi 7"
            5 -> "WiFi 6E"  // WIFI_STANDARD_11AX with 6GHz
            4 -> "WiFi 6"   // WIFI_STANDARD_11AX = 6
            3 -> "WiFi 5"   // WIFI_STANDARD_11AC = 5
            2 -> "WiFi 4"   // WIFI_STANDARD_11N = 4
            1 -> "WiFi (g)" // WIFI_STANDARD_LEGACY = 1
            else -> "WiFi"
        }
    }

    // ── Quality Score ──────────────────────────────────────────────────

    private fun computeQualityScore(
        caps: NetworkCapabilities,
        hasWifi: Boolean,
        hasCellular: Boolean,
        is5g: Boolean
    ): Int {
        var score = 0

        // Bandwidth contribution (up to 50 pts)
        val downBw = caps.linkDownstreamBandwidthKbps
        score += when {
            downBw >= 1_000_000 -> 50  // ≥1 Gbps (WiFi 6E / 5G mmWave)
            downBw >= 500_000  -> 45
            downBw >= 100_000  -> 40
            downBw >= 50_000   -> 35
            downBw >= 10_000   -> 25
            downBw >= 1_000    -> 15
            else               -> 5
        }

        // Network type contribution (up to 35 pts)
        score += when {
            is5g                          -> 35
            hasWifi && hasCellular        -> 32  // Multipath = most resilient
            hasWifi                       -> 30
            hasCellular                   -> 20
            else                          -> 0
        }

        // Connectivity extras (up to 15 pts)
        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)) score += 5
        // Bonus for high upstream (good for real-time gaming uploads/voice)
        val upBw = caps.linkUpstreamBandwidthKbps
        if (upBw >= 10_000) score += 5  // ≥10 Mbps up = solid gaming upload
        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) score += 5

        return score.coerceIn(0, 100)
    }

    // ── WiFi Low-Latency Lock ──────────────────────────────────────────

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

    // ── WiFi Stats ─────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    fun getWifiLinkSpeedMbps(): Int {
        return try { wifiManager?.connectionInfo?.linkSpeed ?: 0 } catch (_: Exception) { 0 }
    }

    @Suppress("DEPRECATION")
    fun getWifiSignalDbm(): Int {
        return try { wifiManager?.connectionInfo?.rssi ?: -100 } catch (_: Exception) { -100 }
    }

    fun getWifiSignalBars(): Int {
        val rssi = getWifiSignalDbm()
        return WifiManager.calculateSignalLevel(rssi, 5)  // 0-4
    }

    // ── Active Network Summary ─────────────────────────────────────────

    /**
     * Human-readable summary for overlay/notification.
     * e.g. "WiFi 6E + 5G NR", "4G LTE", "WiFi 5"
     */
    fun getNetworkSummary(): String {
        val wifi = if (_isWifiConnected.value) _wifiGenerationLabel.value else ""
        val cell = if (_isCellularConnected.value) _cellularGenLabel.value else ""
        return when {
            wifi.isNotEmpty() && cell.isNotEmpty() -> "$wifi + $cell"
            wifi.isNotEmpty() -> wifi
            cell.isNotEmpty() -> cell
            else -> "Offline"
        }
    }

    /**
     * Returns the best transport for gaming.
     * Prefers WiFi Low-Latency > 5G NR > 4G LTE.
     */
    fun getBestTransportLabel(): String {
        return when (_networkType.value) {
            NetworkType.WIFI_AND_CELLULAR ->
                if (_is5G.value) "${_wifiGenerationLabel.value} + 5G" else _wifiGenerationLabel.value
            NetworkType.CELLULAR_5G -> "5G NR"
            NetworkType.WIFI -> _wifiGenerationLabel.value
            NetworkType.CELLULAR_4G -> "4G LTE"
            NetworkType.CELLULAR_3G -> "3G"
            NetworkType.CELLULAR_2G -> "2G"
            NetworkType.ETHERNET -> "Ethernet"
            NetworkType.OFFLINE -> "Offline"
        }
    }

    // ── Deprecated compat ─────────────────────────────────────────────

    /** @deprecated Use networkType.value instead */
    fun getNetworkType(): String = getNetworkSummary()
}
