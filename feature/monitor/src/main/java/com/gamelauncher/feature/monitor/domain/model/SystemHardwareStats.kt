package com.gamelauncher.feature.monitor.domain.model

/**
 * SystemHardwareStats — Encapsulates real-time system hardware telemetry.
 *
 * @property cpuUsagePercent Overall CPU utilization percentage (0-100%)
 * @property ramUsedMb Allocated RAM in MB
 * @property ramTotalMb Total physical RAM in MB
 * @property batteryTemperatureCelsius Battery thermal status in °C
 * @property batteryLevelPercent Battery state of charge percentage (0-100%)
 * @property timestampMs Sample collection timestamp
 */
data class SystemHardwareStats(
    val cpuUsagePercent: Float,
    val ramUsedMb: Long,
    val ramTotalMb: Long,
    val batteryTemperatureCelsius: Float,
    val batteryLevelPercent: Int,
    val timestampMs: Long = System.currentTimeMillis()
)
