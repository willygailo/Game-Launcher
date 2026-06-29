package com.gamelauncher.core

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.Display
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt

enum class NonRootCapabilityLevel {
    BASIC,
    WRITE_SETTINGS,
    ADB_SECURE_SETTINGS
}

data class DevicePerformanceSnapshot(
    val manufacturer: String,
    val model: String,
    val sdkInt: Int,
    val supportedRefreshRatesHz: List<Float>,
    val currentRefreshRateHz: Float,
    val maxRefreshRateHz: Float,
    val canWriteSystemSettings: Boolean,
    val hasSecureSettingsGrant: Boolean,
    val thermalStatus: Int,
    val thermalHeadroom: Float?,
    val nonRootCapabilityLevel: NonRootCapabilityLevel
)

data class FrameRatePlan(
    val requestedFps: Int?,
    val requestedHz: Float?,
    val gameMaxFps: Int?,
    val deviceMaxHz: Float,
    val targetFps: Int,
    val targetHz: Float,
    val thermalLimited: Boolean,
    val reason: String
)

@Singleton
class DevicePerformancePlanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val displayManager: DisplayManager
) {
    fun snapshot(thermalStatusOverride: Int? = null): DevicePerformanceSnapshot {
        val rates = getSupportedRefreshRates()
        val canWriteSystem = canWriteSystemSettings()
        val hasSecureSettings = hasSecureSettingsPermission()
        return DevicePerformanceSnapshot(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            model = Build.MODEL.orEmpty(),
            sdkInt = Build.VERSION.SDK_INT,
            supportedRefreshRatesHz = rates,
            currentRefreshRateHz = getCurrentRefreshRate(),
            maxRefreshRateHz = rates.maxOrNull() ?: 60f,
            canWriteSystemSettings = canWriteSystem,
            hasSecureSettingsGrant = hasSecureSettings,
            thermalStatus = thermalStatusOverride ?: getThermalStatus(),
            thermalHeadroom = getThermalHeadroom(),
            nonRootCapabilityLevel = when {
                hasSecureSettings -> NonRootCapabilityLevel.ADB_SECURE_SETTINGS
                canWriteSystem -> NonRootCapabilityLevel.WRITE_SETTINGS
                else -> NonRootCapabilityLevel.BASIC
            }
        )
    }

    fun planForGame(
        gameInfo: SupportedGames.GameInfo?,
        requestedFps: Int?,
        requestedHz: Float?,
        forceMaxRefreshRate: Boolean,
        thermalStatusOverride: Int? = null
    ): FrameRatePlan {
        val snapshot = snapshot(thermalStatusOverride)
        val thermalLimit = getThermalLimit(snapshot)
        val gameCap = gameInfo?.maxFps?.takeIf { it > 0 }
        val deviceCap = snapshot.maxRefreshRateHz.roundToInt().coerceAtLeast(30)
        val requestedCap = requestedFps?.takeIf { it > 0 }

        val uncappedTarget = listOfNotNull(requestedCap, gameCap, deviceCap).minOrNull() ?: deviceCap
        val targetFps = minOf(uncappedTarget, thermalLimit).coerceIn(30, deviceCap)
        val desiredHz = when {
            forceMaxRefreshRate -> snapshot.maxRefreshRateHz
            requestedHz != null && requestedHz > 0f -> requestedHz
            else -> targetFps.toFloat()
        }
        val targetHz = nearestSupportedRate(
            desired = minOf(desiredHz, thermalLimit.toFloat()),
            supportedRates = snapshot.supportedRefreshRatesHz
        )
        val thermalLimited = targetFps < uncappedTarget || targetHz < nearestSupportedRate(desiredHz, snapshot.supportedRefreshRatesHz)
        val reason = buildReason(snapshot, requestedCap, gameCap, thermalLimited)

        return FrameRatePlan(
            requestedFps = requestedCap,
            requestedHz = requestedHz,
            gameMaxFps = gameCap,
            deviceMaxHz = snapshot.maxRefreshRateHz,
            targetFps = targetFps,
            targetHz = targetHz,
            thermalLimited = thermalLimited,
            reason = reason
        )
    }

    fun getSupportedRefreshRates(): List<Float> {
        return runCatching {
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            val rawRates = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                display?.supportedModes?.map { normalizeRate(it.refreshRate) }.orEmpty()
            } else {
                listOfNotNull(display?.refreshRate?.let(::normalizeRate))
            }
            rawRates
                .filter { it >= 30f }
                .distinct()
                .sorted()
                .ifEmpty { listOf(60f) }
        }.getOrDefault(listOf(60f))
    }

    fun nearestSupportedRate(desired: Float, supportedRates: List<Float> = getSupportedRefreshRates()): Float {
        return supportedRates.minByOrNull { abs(it - desired) } ?: 60f
    }

    private fun getCurrentRefreshRate(): Float {
        return runCatching {
            displayManager.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate?.let(::normalizeRate) ?: 60f
        }.getOrDefault(60f)
    }

    private fun getThermalLimit(snapshot: DevicePerformanceSnapshot): Int {
        val statusLimit = when {
            snapshot.thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE -> 30
            snapshot.thermalStatus == PowerManager.THERMAL_STATUS_MODERATE -> 60
            snapshot.thermalStatus == PowerManager.THERMAL_STATUS_LIGHT -> 90
            else -> 240
        }
        val headroomLimit = when {
            snapshot.thermalHeadroom == null -> 240
            snapshot.thermalHeadroom >= 1.0f -> 60
            snapshot.thermalHeadroom >= 0.85f -> 90
            else -> 240
        }
        return minOf(statusLimit, headroomLimit)
    }

    private fun getThermalStatus(): Int {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getSystemService(PowerManager::class.java)?.currentThermalStatus
                    ?: PowerManager.THERMAL_STATUS_NONE
            } else {
                PowerManager.THERMAL_STATUS_NONE
            }
        }.getOrDefault(PowerManager.THERMAL_STATUS_NONE)
    }

    private fun getThermalHeadroom(): Float? {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                context.getSystemService(PowerManager::class.java)?.getThermalHeadroom(5)
            } else {
                null
            }
        }.getOrNull()
    }

    private fun canWriteSystemSettings(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(context)
    }

    private fun hasSecureSettingsPermission(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun normalizeRate(rate: Float): Float {
        return (rate * 100f).roundToInt() / 100f
    }

    private fun buildReason(
        snapshot: DevicePerformanceSnapshot,
        requestedFps: Int?,
        gameMaxFps: Int?,
        thermalLimited: Boolean
    ): String {
        val parts = mutableListOf<String>()
        parts += "device max ${snapshot.maxRefreshRateHz.roundToInt()}Hz"
        gameMaxFps?.let { parts += "game cap ${it}FPS" }
        requestedFps?.let { parts += "requested ${it}FPS" }
        if (thermalLimited) parts += "thermal safe cap"
        parts += when (snapshot.nonRootCapabilityLevel) {
            NonRootCapabilityLevel.ADB_SECURE_SETTINGS -> "ADB secure tweaks available"
            NonRootCapabilityLevel.WRITE_SETTINGS -> "WRITE_SETTINGS refresh request available"
            NonRootCapabilityLevel.BASIC -> "basic non-root mode"
        }
        return parts.joinToString(", ")
    }
}
