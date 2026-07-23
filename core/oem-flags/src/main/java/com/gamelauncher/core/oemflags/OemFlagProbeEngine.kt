package com.gamelauncher.core.oemflags

import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.settings.SettingsKeys
import com.gamelauncher.core.shizuku.IShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OemFlagProbeEngine — Probe-Before-Present service validating which OEM hidden flags
 * exist on the current device firmware build, capturing pre-boost snapshot values,
 * and supporting snapshot-based full resets via resetAllFlags().
 */
@Singleton
class OemFlagProbeEngine @Inject constructor(
    private val detector: DeviceProfileDetector,
    private val registry: OemFlagRegistry,
    private val settingsRepository: SecureSettingsRepository,
    private val shizukuManager: IShizukuManager
) {
    private val _probedFlags = MutableStateFlow<List<OemFlag>>(emptyList())
    val probedFlags: StateFlow<List<OemFlag>> = _probedFlags.asStateFlow()

    // Map storing initial pre-boost snapshot values captured during first probe
    private val initialSnapshots = ConcurrentHashMap<String, String>()

    suspend fun probeDeviceFlags(): List<OemFlag> = withContext(Dispatchers.IO) {
        val detectedBrand = detector.detectOemBrand()
        val candidateFlags = registry.getFlagsForBrand(detectedBrand)
        val isShizukuReady = shizukuManager.isReady()

        val results = candidateFlags.map { flag ->
            if (!isShizukuReady) {
                flag.copy(status = ProbeStatus.Unsupported("Shizuku privileged service not connected"))
            } else {
                val probedVal = tryReadFlag(flag)
                if (probedVal != null) {
                    // Record pre-boost snapshot value during initial probe if not already recorded
                    initialSnapshots.putIfAbsent(flag.id, probedVal)
                    val snapshotVal = initialSnapshots[flag.id] ?: probedVal
                    flag.copy(
                        status = ProbeStatus.Supported(probedVal),
                        initialSnapshotValue = snapshotVal
                    )
                } else {
                    flag.copy(status = ProbeStatus.Unsupported("Key not found on this ROM build"))
                }
            }
        }

        _probedFlags.value = results
        results
    }

    suspend fun applyFlagState(flag: OemFlag, enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        val newValue = if (enable) flag.activeValue else flag.defaultValue
        val scope = mapScope(flag.scope)

        val success = settingsRepository.putString(scope, flag.key, newValue)
        if (success) {
            _probedFlags.value = _probedFlags.value.map { item ->
                if (item.id == flag.id) {
                    item.copy(status = ProbeStatus.Supported(newValue))
                } else item
            }
        }
        success
    }

    /**
     * Reverts every probed flag to its original pre-boost initial snapshot value
     * captured during the first probe run (not hardcoded OS defaults).
     */
    suspend fun resetAllFlags(): Boolean = withContext(Dispatchers.IO) {
        if (!shizukuManager.isReady()) return@withContext false
        var allSuccess = true

        val currentFlags = _probedFlags.value
        for (flag in currentFlags) {
            if (flag.status is ProbeStatus.Supported) {
                val originalVal = initialSnapshots[flag.id] ?: flag.defaultValue
                val scope = mapScope(flag.scope)
                val ok = settingsRepository.putString(scope, flag.key, originalVal)
                if (!ok) allSuccess = false
            }
        }

        // Refresh state after reset
        probeDeviceFlags()
        allSuccess
    }

    private suspend fun tryReadFlag(flag: OemFlag): String? {
        val scope = mapScope(flag.scope)
        return try {
            settingsRepository.getString(scope, flag.key)
        } catch (_: Exception) {
            null
        }
    }

    private fun mapScope(flagScope: FlagScope): SettingsKeys.Scope {
        return when (flagScope) {
            FlagScope.GLOBAL -> SettingsKeys.Scope.GLOBAL
            FlagScope.SYSTEM -> SettingsKeys.Scope.SYSTEM
            FlagScope.SECURE -> SettingsKeys.Scope.SECURE
            FlagScope.SHELL_CMD -> SettingsKeys.Scope.GLOBAL
        }
    }
}
