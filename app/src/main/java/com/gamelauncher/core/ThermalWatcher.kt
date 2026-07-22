package com.gamelauncher.core

import android.content.Context
import android.os.Build
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Push-based thermal status watcher using PowerManager.OnThermalStatusChangedListener
 * (Android 10+ / API 29+). Replaces polling with instant-reaction callbacks,
 * firing immediately when the kernel reports a thermal zone change — no 1-second delay.
 *
 * Usage:
 *   thermalWatcher.thermalStatus.collect { status -> adaptFpsToThermal(status) }
 */
@Singleton
class ThermalWatcher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _thermalStatus = MutableStateFlow(PowerManager.THERMAL_STATUS_NONE)
    val thermalStatus: StateFlow<Int> = _thermalStatus.asStateFlow()

    private var isListening = false
    private var powerManager: PowerManager? = null

    private val thermalListener = PowerManager.OnThermalStatusChangedListener { status ->
        _thermalStatus.value = status
    }

    /**
     * Start listening for thermal events.
     * Call from GameBoosterService.onCreate() or GameOptimizationCoordinator.startOptimization().
     * Safe to call multiple times — idempotent.
     */
    fun start() {
        if (isListening) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return  // API 29 minimum
        try {
            val pm = context.getSystemService(PowerManager::class.java) ?: return
            powerManager = pm
            pm.addThermalStatusListener(thermalListener)
            // Immediately populate current state so collectors get an initial value
            _thermalStatus.value = pm.currentThermalStatus
            isListening = true
        } catch (_: Exception) {}
    }

    /**
     * Stop listening. Call when gaming session ends to save battery.
     */
    fun stop() {
        if (!isListening) return
        try {
            powerManager?.removeThermalStatusListener(thermalListener)
        } catch (_: Exception) {}
        isListening = false
        _thermalStatus.value = PowerManager.THERMAL_STATUS_NONE
    }

    /**
     * Human-readable status label for UI display.
     * Returns emoji + label pairs for the thermal badge.
     */
    fun statusLabel(status: Int): Pair<String, String> = when (status) {
        PowerManager.THERMAL_STATUS_NONE      -> "🟢" to "Normal"
        PowerManager.THERMAL_STATUS_LIGHT     -> "🟡" to "Warm"
        PowerManager.THERMAL_STATUS_MODERATE  -> "🟠" to "Moderate"
        PowerManager.THERMAL_STATUS_SEVERE    -> "🔴" to "Hot"
        PowerManager.THERMAL_STATUS_CRITICAL  -> "🔥" to "Critical"
        PowerManager.THERMAL_STATUS_EMERGENCY -> "⛔" to "Emergency"
        PowerManager.THERMAL_STATUS_SHUTDOWN  -> "💀" to "Shutdown"
        else -> "⚪" to "Unknown"
    }

    /** True if device should throttle FPS (moderate or worse). */
    fun isThrottling(status: Int): Boolean =
        status >= PowerManager.THERMAL_STATUS_MODERATE

    /** Suggested max FPS cap for this thermal state. */
    fun suggestedMaxFps(status: Int): Int = when {
        status >= PowerManager.THERMAL_STATUS_CRITICAL  -> 30
        status >= PowerManager.THERMAL_STATUS_SEVERE    -> 45
        status >= PowerManager.THERMAL_STATUS_MODERATE  -> 60
        else -> Int.MAX_VALUE  // no cap at light/none
    }
}
