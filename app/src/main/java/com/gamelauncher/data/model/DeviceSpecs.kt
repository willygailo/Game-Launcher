package com.gamelauncher.data.model

/**
 * Real-time hardware snapshot for dashboard display.
 */
data class DeviceSpecs(
    val socName: String = "Unknown SoC",
    val architecture: String = "arm64-v8a",
    val deviceRating: Int = 5,
    val isGamingOptimized: Boolean = false,

    val cpuUsagePercent: Float = 0f,
    val cpuFreqMhz: Long = 0L,
    val cpuCoreCount: Int = Runtime.getRuntime().availableProcessors(),
    val cpuGovernor: String = "unknown",

    val gpuUsagePercent: Float = 0f,
    val gpuFreqMhz: Long = 0L,
    val gpuRenderer: String = "unknown",

    val ramTotalMb: Long = 0L,
    val ramUsedMb: Long = 0L,
    val ramFreeMb: Long = 0L,

    val batteryLevel: Int = 100,
    val batteryTemperature: Float = 25f,
    val batteryChargingStatus: String = "Unknown",
    val batteryHealth: String = "Unknown",
    val batteryVoltage: Int = 0,

    val displayRefreshRateHz: Float = 60f,
    val supportedRefreshRates: List<Float> = listOf(60f),
    val screenWidthPx: Int = 1080,
    val screenHeightPx: Int = 2400,
    val adpfPreferredRate: Float = 60f,

    val networkType: String = "WiFi",
    val networkStrengthDbm: Int = -70,
    val wifiLinkSpeedMbps: Int = 0,

    val thermalStatus: Int = 0,   // 0=None, 1=Light, 2=Moderate, 3=Severe, 4=Critical
    val isBoostActive: Boolean = false,
    val boostTechniques: List<String> = emptyList(),
    val freedRamMb: Long = 0L,
    val currentFps: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Active boost session report returned after boost is applied.
 */
data class BoostReport(
    val freedRamMb: Long,
    val appliedTechniques: List<String>,
    val maxHzAchieved: Float,
    val targetFps: Int,
    val durationMs: Long = 0L,
    val success: Boolean = true
)
