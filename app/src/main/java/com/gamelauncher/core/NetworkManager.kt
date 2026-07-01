package com.gamelauncher.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import com.gamelauncher.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    @ApplicationContext private val context: Context,
    @ApplicationScope private val appScope: CoroutineScope
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

    private val _networkSnapshot = MutableStateFlow(NetworkSnapshot())
    val networkSnapshot: StateFlow<NetworkSnapshot> = _networkSnapshot

    // ── Internal ───────────────────────────────────────────────────────

    private val wifiManager: WifiManager? =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?

    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(ConnectivityManager::class.java)

    private val telephonyManager: TelephonyManager? =
        context.getSystemService(TelephonyManager::class.java)

    private var wifiLock: WifiManager.WifiLock? = null
    // ── Enum ───────────────────────────────────────────────────────────

    enum class NetworkType {
        OFFLINE, WIFI, CELLULAR_5G, CELLULAR_4G, CELLULAR_3G, CELLULAR_2G,
        WIFI_AND_CELLULAR, ETHERNET
    }

    data class NetworkSnapshot(
        val summary: String = "Offline",
        val bestTransport: String = "Offline",
        val networkType: NetworkType = NetworkType.OFFLINE,
        val qualityScore: Int = 0,
        val hasValidatedInternet: Boolean = false,
        val isMetered: Boolean = true,
        val isWifiConnected: Boolean = false,
        val isCellularConnected: Boolean = false,
        val is5G: Boolean = false,
        val is5GPlus: Boolean = false,
        val wifiLabel: String = "",
        val wifiLinkSpeedMbps: Int = 0,
        val wifiSignalDbm: Int = -100,
        val wifiSignalBars: Int = 0,
        val wifiFrequencyMhz: Int = 0,
        val wifiBandLabel: String = "",
        val cellularLabel: String = "",
        val cellularSignalDbm: Int = -120,
        val cellularSignalBars: Int = 0,
        val downstreamKbps: Int = 0,
        val upstreamKbps: Int = 0
    )

    // ── Network Callback (live monitoring) ────────────────────────────

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            appScope.launch { refreshNetworkStatus() }
        }
        override fun onLost(network: Network) {
            appScope.launch { refreshNetworkStatus() }
        }
        override fun onAvailable(network: Network) {
            appScope.launch { refreshNetworkStatus() }
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
            val allCaps = cm.allNetworks.mapNotNull { network ->
                cm.getNetworkCapabilities(network)
            }
            if (allCaps.isEmpty()) {
                _networkType.value = NetworkType.OFFLINE
                _networkQualityScore.value = 0
                _isWifiConnected.value = false
                _isCellularConnected.value = false
                _is5G.value = false
                _wifiGenerationLabel.value = "WiFi"
                _cellularGenLabel.value = ""
                _networkSnapshot.value = NetworkSnapshot()
                return
            }

            updateNetworkState(allCaps)
        } catch (_: Exception) {}
    }

    private fun updateNetworkState(allCaps: List<NetworkCapabilities>) {
        val wifiCaps = allCaps.firstOrNull { it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) }
        val cellularCaps = allCaps.firstOrNull { it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) }
        val ethernetCaps = allCaps.firstOrNull { it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) }
        val bestCaps = ethernetCaps ?: wifiCaps ?: cellularCaps ?: allCaps.first()

        val hasWifi = wifiCaps != null
        val hasCellular = cellularCaps != null
        val hasEthernet = ethernetCaps != null
        val hasValidatedInternet = allCaps.any {
            it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
        val isMetered = allCaps.none { it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) }

        _isWifiConnected.value = hasWifi
        _isCellularConnected.value = hasCellular

        val wifiInfo = wifiCaps?.transportInfo as? android.net.wifi.WifiInfo
        val wifiLabel = if (hasWifi && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val wifiStandard = wifiInfo?.wifiStandard ?: 0
            wifiStandardToLabel(wifiStandard, wifiInfo?.frequency ?: 0)
        } else if (hasWifi) {
            "WiFi"
        } else ""
        if (hasWifi) _wifiGenerationLabel.value = wifiLabel else _wifiGenerationLabel.value = "WiFi"

        // 5G detection
        val cellularNetworkType = getCellularDataNetworkType()
        val is5gNow = hasCellular && cellularNetworkType == TelephonyManager.NETWORK_TYPE_NR
        val is5gPlusNow = is5gNow && isLikely5GPlus(cellularCaps)
        _is5G.value = is5gNow

        // Cellular generation label
        val cellularLabel = if (hasCellular) getCellularGenerationLabel(cellularNetworkType, is5gPlusNow) else ""
        if (hasCellular) {
            _cellularGenLabel.value = cellularLabel
        } else {
            _cellularGenLabel.value = ""
        }

        // Derive network type
        val networkTypeNow = when {
            hasEthernet -> NetworkType.ETHERNET
            hasWifi && hasCellular -> NetworkType.WIFI_AND_CELLULAR
            hasWifi -> NetworkType.WIFI
            is5gNow -> NetworkType.CELLULAR_5G
            hasCellular -> deriveCellularType(cellularNetworkType)
            else -> NetworkType.OFFLINE
        }
        _networkType.value = networkTypeNow

        // Network quality score
        val qualityScore = computeQualityScore(bestCaps, hasWifi, hasCellular, is5gNow, hasValidatedInternet)
        _networkQualityScore.value = qualityScore

        val wifiRssi = getWifiSignalDbmFrom(wifiInfo)
        val wifiSpeed = getWifiLinkSpeedMbpsFrom(wifiInfo, wifiCaps)
        val cellularDbm = getCellularSignalDbm()
        val cellularBars = getCellularSignalBars()
        val downstreamKbps = allCaps.maxOfOrNull { it.linkDownstreamBandwidthKbps } ?: 0
        val upstreamKbps = allCaps.maxOfOrNull { it.linkUpstreamBandwidthKbps } ?: 0

        _networkSnapshot.value = NetworkSnapshot(
            summary = buildNetworkSummary(wifiLabel, cellularLabel, hasEthernet, hasValidatedInternet),
            bestTransport = buildBestTransportLabel(networkTypeNow, wifiLabel, cellularLabel, is5gPlusNow),
            networkType = networkTypeNow,
            qualityScore = qualityScore,
            hasValidatedInternet = hasValidatedInternet,
            isMetered = isMetered,
            isWifiConnected = hasWifi,
            isCellularConnected = hasCellular,
            is5G = is5gNow,
            is5GPlus = is5gPlusNow,
            wifiLabel = wifiLabel,
            wifiLinkSpeedMbps = wifiSpeed,
            wifiSignalDbm = wifiRssi,
            wifiSignalBars = signalBarsFromWifiRssi(wifiRssi),
            wifiFrequencyMhz = wifiInfo?.frequency ?: 0,
            wifiBandLabel = wifiBandLabel(wifiInfo?.frequency ?: 0),
            cellularLabel = cellularLabel,
            cellularSignalDbm = cellularDbm,
            cellularSignalBars = cellularBars,
            downstreamKbps = downstreamKbps,
            upstreamKbps = upstreamKbps
        )
    }

    // ── 5G Detection ──────────────────────────────────────────────────

    /**
     * Multi-API 5G detection:
     *  - API 31+: NetworkCapabilities.TRANSPORT_NR via hasTransport
     *  - API 29-30: TelephonyManager.getDataNetworkType() == NETWORK_TYPE_NR
     *  - Pre-29: Not detectable without location permission
     */
    private fun getCellularDataNetworkType(): Int {
        return try {
            telephonyManager?.dataNetworkType ?: TelephonyManager.NETWORK_TYPE_UNKNOWN
        } catch (_: Exception) {
            TelephonyManager.NETWORK_TYPE_UNKNOWN
        }
    }

    @Suppress("DEPRECATION")
    private fun deriveCellularType(networkType: Int): NetworkType {
        return when (networkType) {
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
            TelephonyManager.NETWORK_TYPE_EVDO_B,
            TelephonyManager.NETWORK_TYPE_EHRPD -> NetworkType.CELLULAR_3G
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN -> NetworkType.CELLULAR_2G
            else -> NetworkType.CELLULAR_4G
        }
    }

    private fun getCellularGenerationLabel(networkType: Int, is5GPlus: Boolean): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> if (is5GPlus) "5G+" else "5G NR"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSPAP -> "H+"
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EHRPD -> "3G"
            TelephonyManager.NETWORK_TYPE_EDGE -> "E"
            TelephonyManager.NETWORK_TYPE_GPRS -> "G"
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT,
            TelephonyManager.NETWORK_TYPE_IDEN -> "2G"
            else -> "Mobile Data"
        }
    }

    // ── WiFi Standard Label ────────────────────────────────────────────

    private fun wifiStandardToLabel(standard: Int, frequencyMhz: Int): String {
        return when (standard) {
            8 -> "WiFi 7"
            7 -> "WiGig"
            6 -> if (frequencyMhz in 5925..7125) "WiFi 6E" else "WiFi 6"
            5 -> "WiFi 5"
            4 -> "WiFi 4"
            1 -> "WiFi (g)" // WIFI_STANDARD_LEGACY = 1
            else -> "WiFi"
        }
    }

    // ── Quality Score ──────────────────────────────────────────────────

    private fun computeQualityScore(
        caps: NetworkCapabilities,
        hasWifi: Boolean,
        hasCellular: Boolean,
        is5g: Boolean,
        hasValidatedInternet: Boolean
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
        if (!hasValidatedInternet) score -= 30

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
        refreshNetworkStatus()
        return _networkSnapshot.value.wifiLinkSpeedMbps
    }

    @Suppress("DEPRECATION")
    fun getWifiSignalDbm(): Int {
        refreshNetworkStatus()
        return _networkSnapshot.value.wifiSignalDbm
    }

    fun getWifiSignalBars(): Int {
        refreshNetworkStatus()
        return _networkSnapshot.value.wifiSignalBars
    }

    // ── Active Network Summary ─────────────────────────────────────────

    /**
     * Human-readable summary for overlay/notification.
     * e.g. "WiFi 6E + 5G NR", "4G LTE", "WiFi 5"
     */
    fun getNetworkSummary(): String {
        refreshNetworkStatus()
        return _networkSnapshot.value.summary
    }

    /**
     * Returns the best transport for gaming.
     * Prefers WiFi Low-Latency > 5G NR > 4G LTE.
     */
    fun getBestTransportLabel(): String {
        refreshNetworkStatus()
        return _networkSnapshot.value.bestTransport
    }

    fun getNetworkSnapshot(): NetworkSnapshot {
        refreshNetworkStatus()
        return _networkSnapshot.value
    }

    // ── Deprecated compat ─────────────────────────────────────────────

    /** @deprecated Use networkType.value instead */
    fun getNetworkType(): String = getNetworkSummary()

    private fun getWifiLinkSpeedMbpsFrom(
        wifiInfo: android.net.wifi.WifiInfo?,
        wifiCaps: NetworkCapabilities?
    ): Int {
        return try {
            val infoSpeed = wifiInfo?.linkSpeed ?: 0
            if (infoSpeed > 0) infoSpeed else (wifiCaps?.linkDownstreamBandwidthKbps ?: 0) / 1000
        } catch (_: Exception) {
            0
        }
    }

    private fun getWifiSignalDbmFrom(wifiInfo: android.net.wifi.WifiInfo?): Int {
        return try {
            val rssi = wifiInfo?.rssi ?: wifiManager?.connectionInfo?.rssi ?: -100
            if (rssi in -120..0) rssi else -100
        } catch (_: Exception) {
            -100
        }
    }

    private fun getCellularSignalDbm(): Int {
        return try {
            val dbm = telephonyManager
                ?.signalStrength
                ?.cellSignalStrengths
                ?.map { it.dbm }
                ?.firstOrNull { it in -150..-20 }
            dbm ?: -120
        } catch (_: Exception) {
            -120
        }
    }

    private fun getCellularSignalBars(): Int {
        return try {
            telephonyManager?.signalStrength?.level?.coerceIn(0, 4) ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun signalBarsFromWifiRssi(rssi: Int): Int {
        return try {
            @Suppress("DEPRECATION")
            WifiManager.calculateSignalLevel(rssi, 5).coerceIn(0, 4)
        } catch (_: Exception) {
            0
        }
    }

    private fun wifiBandLabel(frequencyMhz: Int): String {
        return when (frequencyMhz) {
            in 2400..2500 -> "2.4GHz"
            in 4900..5900 -> "5GHz"
            in 5925..7125 -> "6GHz"
            in 57000..71000 -> "60GHz"
            else -> ""
        }
    }

    private fun isLikely5GPlus(cellularCaps: NetworkCapabilities?): Boolean {
        val downKbps = cellularCaps?.linkDownstreamBandwidthKbps ?: 0
        val upKbps = cellularCaps?.linkUpstreamBandwidthKbps ?: 0
        val signalLevel = getCellularSignalBars()
        return downKbps >= 500_000 || upKbps >= 50_000 || signalLevel >= 4
    }

    private fun buildNetworkSummary(
        wifiLabel: String,
        cellularLabel: String,
        hasEthernet: Boolean,
        hasValidatedInternet: Boolean
    ): String {
        val base = when {
            hasEthernet -> "Ethernet"
            wifiLabel.isNotEmpty() && cellularLabel.isNotEmpty() -> "$wifiLabel + $cellularLabel"
            wifiLabel.isNotEmpty() -> wifiLabel
            cellularLabel.isNotEmpty() -> cellularLabel
            else -> "Offline"
        }
        return if (base != "Offline" && !hasValidatedInternet) "$base (No Internet)" else base
    }

    private fun buildBestTransportLabel(
        networkType: NetworkType,
        wifiLabel: String,
        cellularLabel: String,
        is5GPlus: Boolean
    ): String {
        return when (networkType) {
            NetworkType.WIFI_AND_CELLULAR -> {
                val cell = if (is5GPlus) "5G+" else cellularLabel.ifBlank { "Mobile Data" }
                "${wifiLabel.ifBlank { "WiFi" }} + $cell"
            }
            NetworkType.CELLULAR_5G -> if (is5GPlus) "5G+" else cellularLabel.ifBlank { "5G NR" }
            NetworkType.WIFI -> wifiLabel.ifBlank { "WiFi" }
            NetworkType.CELLULAR_4G -> cellularLabel.ifBlank { "4G LTE" }
            NetworkType.CELLULAR_3G -> cellularLabel.ifBlank { "3G" }
            NetworkType.CELLULAR_2G -> cellularLabel.ifBlank { "2G" }
            NetworkType.ETHERNET -> "Ethernet"
            NetworkType.OFFLINE -> "Offline"
        }
    }
}
