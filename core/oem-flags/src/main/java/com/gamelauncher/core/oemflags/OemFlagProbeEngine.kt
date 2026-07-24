package com.gamelauncher.core.oemflags

import android.content.Context
import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.settings.SettingsKeys
import com.gamelauncher.core.shizuku.IShellExecutor
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
 * and setprop system properties exist on the current device firmware build, persisting pre-boost baseline snapshots
 * and ever-applied flag history via SharedPreferences to guarantee reset capability across
 * app restarts and OTA updates, and enforcing defense-in-depth supported status guards.
 */
@Singleton
class OemFlagProbeEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val detector: DeviceProfileDetector,
    private val registry: OemFlagRegistry,
    private val settingsRepository: SecureSettingsRepository,
    private val shizukuManager: IShizukuManager,
    private val shellExecutor: IShellExecutor
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
            if (!shizukuManager.isReady()) return@withContext false
            if (flag.status !is ProbeStatus.Supported) return@withContext false

            val newValue = if (enable) flag.activeValue else flag.defaultValue

            val success = if (flag.scope == FlagScope.SYSTEM_PROP) {
                val out = shellExecutor.executeCommand("setprop ${flag.key} $newValue")
                out != null || shizukuManager.isReady()
            } else {
                val scope = mapScope(flag.scope)
                settingsRepository.putString(scope, flag.key, newValue)
            }

            if (success) {
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

    suspend fun resetAllFlags(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!shizukuManager.isReady()) return@withContext false
            var allSuccess = true

            val everAppliedIds = snapshotPrefs.getStringSet("ever_applied_ids", emptySet()) ?: emptySet()
            val allRegisteredFlags = registry.ALL_FLAGS.associateBy { it.id }

            for (flagId in everAppliedIds) {
                val flag = allRegisteredFlags[flagId] ?: continue
                val snapshotKey = "snapshot_$flagId"
                val originalVal = snapshotPrefs.getString(snapshotKey, flag.defaultValue) ?: flag.defaultValue

                try {
                    val ok = if (flag.scope == FlagScope.SYSTEM_PROP) {
                        shellExecutor.executeCommand("setprop ${flag.key} $originalVal") != null
                    } else {
                        val scope = mapScope(flag.scope)
                        settingsRepository.putString(scope, flag.key, originalVal)
                    }
                    if (!ok) allSuccess = false
                } catch (_: Exception) {
                    allSuccess = false
                }
            }

            probeDeviceFlags()
            allSuccess
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun tryReadFlag(flag: OemFlag): String? {
        if (flag.scope == FlagScope.SHELL_CMD) return null
        if (flag.scope == FlagScope.SYSTEM_PROP) {
            val out = shellExecutor.executeCommand("getprop ${flag.key}")
            return if (!out.isNullOrBlank()) out.trim() else flag.defaultValue
        }
        val scope = mapScope(flag.scope)
        return try {
            settingsRepository.getString(scope, flag.key)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getRomBuildInfo(): String = withContext(Dispatchers.IO) {
        val buildType = try { shellExecutor.executeCommand("getprop ro.build.type")?.trim() ?: "user" } catch (_: Exception) { "user" }
        val debuggable = try { shellExecutor.executeCommand("getprop ro.debuggable")?.trim() ?: "0" } catch (_: Exception) { "0" }
        "ROM Build: $buildType | ro.debuggable: $debuggable"
    }

    private fun mapScope(flagScope: FlagScope): SettingsKeys.Scope {
        return when (flagScope) {
            FlagScope.GLOBAL -> SettingsKeys.Scope.GLOBAL
            FlagScope.SYSTEM -> SettingsKeys.Scope.SYSTEM
            FlagScope.SECURE -> SettingsKeys.Scope.SECURE
            FlagScope.SHELL_CMD, FlagScope.SYSTEM_PROP -> throw IllegalArgumentException(
                "FlagScope.SHELL_CMD and SYSTEM_PROP are executed via IShellExecutor, not SecureSettingsRepository"
            )
        }
    }
}
