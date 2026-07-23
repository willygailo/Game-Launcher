package com.gamelauncher.core.oemflags

import android.content.Context
import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.settings.SettingsKeys
import com.gamelauncher.core.shizuku.IShizukuManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OemFlagProbeEngine — Probe-Before-Present service validating which OEM hidden flags
 * exist on the current device firmware build, persisting pre-boost baseline snapshots
 * and ever-applied flag history via SharedPreferences to guarantee reset capability across
 * app restarts and OTA updates, and enforcing defense-in-depth supported status guards.
 */
@Singleton
class OemFlagProbeEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val detector: DeviceProfileDetector,
    private val registry: OemFlagRegistry,
    private val settingsRepository: SecureSettingsRepository,
    private val shizukuManager: IShizukuManager
) {
    private val _probedFlags = MutableStateFlow<List<OemFlag>>(emptyList())
    val probedFlags: StateFlow<List<OemFlag>> = _probedFlags.asStateFlow()

    private val snapshotPrefs by lazy {
        context.getSharedPreferences("oem_flag_snapshots_v1", Context.MODE_PRIVATE)
    }

    suspend fun probeDeviceFlags(): List<OemFlag> = withContext(Dispatchers.IO) {
        val detectedBrand = detector.detectOemBrand()
        val candidateFlags = registry.getFlagsForBrand(detectedBrand)
        val isShizukuReady = try { shizukuManager.isReady() } catch (_: Exception) { false }

        val results = candidateFlags.map { flag ->
            if (!isShizukuReady) {
                flag.copy(status = ProbeStatus.Unsupported("Shizuku privileged service not connected"))
            } else {
                val probedVal = tryReadFlag(flag)
                if (probedVal != null) {
                    // Read or persist pre-boost baseline snapshot
                    val snapshotKey = "snapshot_${flag.id}"
                    if (!snapshotPrefs.contains(snapshotKey)) {
                        snapshotPrefs.edit().putString(snapshotKey, probedVal).apply()
                    }
                    val initialSnapshot = snapshotPrefs.getString(snapshotKey, probedVal) ?: probedVal

                    flag.copy(
                        status = ProbeStatus.Supported(probedVal),
                        initialSnapshotValue = initialSnapshot
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
        try {
            // Defense-in-depth Guard 1: Verify Shizuku Binder readiness
            if (!shizukuManager.isReady()) return@withContext false

            // Defense-in-depth Guard 2: Only proceed if status is confirmed Supported on this ROM build
            if (flag.status !is ProbeStatus.Supported) return@withContext false

            val newValue = if (enable) flag.activeValue else flag.defaultValue
            val scope = mapScope(flag.scope)

            val success = settingsRepository.putString(scope, flag.key, newValue)
            if (success) {
                // Record in persistent ever-applied flag history for guaranteed rollback
                val currentEverApplied = snapshotPrefs.getStringSet("ever_applied_ids", emptySet()) ?: emptySet()
                val updatedSet = currentEverApplied.toMutableSet().apply { add(flag.id) }
                snapshotPrefs.edit().putStringSet("ever_applied_ids", updatedSet).apply()

                _probedFlags.value = _probedFlags.value.map { item ->
                    if (item.id == flag.id) {
                        item.copy(status = ProbeStatus.Supported(newValue))
                    } else item
                }
            }
            success
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Reverts every flag ever modified (tracked in persistent SharedPreferences history)
     * back to its original pre-boost baseline snapshot value.
     */
    suspend fun resetAllFlags(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!shizukuManager.isReady()) return@withContext false
            var allSuccess = true

            // Retrieve persisted set of all flag IDs ever applied on this device
            val everAppliedIds = snapshotPrefs.getStringSet("ever_applied_ids", emptySet()) ?: emptySet()
            val allRegisteredFlags = registry.ALL_FLAGS.associateBy { it.id }

            for (flagId in everAppliedIds) {
                val flag = allRegisteredFlags[flagId] ?: continue
                val snapshotKey = "snapshot_$flagId"
                val originalVal = snapshotPrefs.getString(snapshotKey, flag.defaultValue) ?: flag.defaultValue
                val scope = mapScope(flag.scope)

                try {
                    val ok = settingsRepository.putString(scope, flag.key, originalVal)
                    if (!ok) allSuccess = false
                } catch (_: Exception) {
                    allSuccess = false
                }
            }

            // Refresh state after reset
            probeDeviceFlags()
            allSuccess
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun tryReadFlag(flag: OemFlag): String? {
        if (flag.scope == FlagScope.SHELL_CMD) return null
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
            FlagScope.SHELL_CMD -> throw IllegalArgumentException(
                "FlagScope.SHELL_CMD must be executed via IShellExecutor, not SecureSettingsRepository"
            )
        }
    }
}
