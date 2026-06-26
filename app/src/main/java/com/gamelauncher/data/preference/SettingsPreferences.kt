package com.gamelauncher.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Existing settings
    val globalAutoBoost: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_GLOBAL_AUTO_BOOST] ?: true }

    val isOverlayEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_OVERLAY_ENABLED] ?: false }

    val gameDetectorEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_GAME_DETECTOR_ENABLED] ?: false }

    val dndEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_DND_ENABLED] ?: true }

    val touchOptimizationEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_TOUCH_OPTIMIZATION_ENABLED] ?: true }

    val memoryCleanupEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_MEMORY_CLEANUP_ENABLED] ?: true }

    val networkOptimizationEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_NETWORK_OPTIMIZATION_ENABLED] ?: true }

    val isDarkTheme: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_DARK_THEME] ?: true }

    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_ONBOARDING_COMPLETED] ?: false }

    // NEW: Adaptive performance settings
    val adaptivePerformanceEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_ADAPTIVE_PERF] ?: true }

    val thermalAwareBoost: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_THERMAL_AWARE] ?: true }

    val secureSettingsAnimScale: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_SECURE_SETTINGS_ANIM_SCALE] ?: true }

    val secureSettingsGameDriver: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_SECURE_SETTINGS_GAME_DRIVER] ?: true }

    val secureSettingsSyncOff: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_SECURE_SETTINGS_SYNC_OFF] ?: true }

    val secureSettingsMobileData: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_SECURE_SETTINGS_MOBILE_DATA] ?: true }

    val secureSettingsBatterySaver: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_SECURE_SETTINGS_BATTERY_SAVER] ?: true }

    val secureSettingsLocationOff: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_SECURE_SETTINGS_LOCATION_OFF] ?: true }

    // ── NEW: Battery Saver + Network + Thermal ────────────────────────

    /** When true, battery saver is automatically killed when boost starts */
    val disableBatterySaverOnBoost: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_DISABLE_BATTERY_SAVER_ON_BOOST] ?: true }

    /** When true, enable mobile_data_always_on for WiFi+Data dual stack during gaming */
    val networkDualStackEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_NETWORK_DUAL_STACK] ?: true }

    /** When true, whitelists the game package from Doze mode */
    val dozeWhitelistEnabled: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_DOZE_WHITELIST] ?: true }

    /** When true (root only), suspends thermal-engine during gaming for max perf */
    val suspendThermalOnBoost: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_SUSPEND_THERMAL] ?: false }

    /** Force max Hz on display during boost */
    val forceMaxHzOnBoost: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_FORCE_MAX_HZ] ?: true }

    // Setters
    suspend fun setDarkTheme(enabled: Boolean) { dataStore.edit { it[KEY_DARK_THEME] = enabled } }
    suspend fun setOnboardingCompleted() { dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = true } }
    suspend fun setGlobalAutoBoost(enabled: Boolean) { dataStore.edit { it[KEY_GLOBAL_AUTO_BOOST] = enabled } }
    suspend fun setOverlayEnabled(enabled: Boolean) { dataStore.edit { it[KEY_OVERLAY_ENABLED] = enabled } }
    suspend fun setGameDetectorEnabled(enabled: Boolean) { dataStore.edit { it[KEY_GAME_DETECTOR_ENABLED] = enabled } }
    suspend fun setThermalAwareBoost(enabled: Boolean) { dataStore.edit { it[KEY_THERMAL_AWARE] = enabled } }

    suspend fun setSecureSettingsAnimScale(enabled: Boolean) { dataStore.edit { it[KEY_SECURE_SETTINGS_ANIM_SCALE] = enabled } }
    suspend fun setSecureSettingsGameDriver(enabled: Boolean) { dataStore.edit { it[KEY_SECURE_SETTINGS_GAME_DRIVER] = enabled } }
    suspend fun setSecureSettingsSyncOff(enabled: Boolean) { dataStore.edit { it[KEY_SECURE_SETTINGS_SYNC_OFF] = enabled } }
    suspend fun setSecureSettingsMobileData(enabled: Boolean) { dataStore.edit { it[KEY_SECURE_SETTINGS_MOBILE_DATA] = enabled } }
    suspend fun setSecureSettingsBatterySaver(enabled: Boolean) { dataStore.edit { it[KEY_SECURE_SETTINGS_BATTERY_SAVER] = enabled } }
    suspend fun setSecureSettingsLocationOff(enabled: Boolean) { dataStore.edit { it[KEY_SECURE_SETTINGS_LOCATION_OFF] = enabled } }
    suspend fun setDisableBatterySaverOnBoost(enabled: Boolean) { dataStore.edit { it[KEY_DISABLE_BATTERY_SAVER_ON_BOOST] = enabled } }
    suspend fun setNetworkDualStackEnabled(enabled: Boolean) { dataStore.edit { it[KEY_NETWORK_DUAL_STACK] = enabled } }
    suspend fun setDozeWhitelistEnabled(enabled: Boolean) { dataStore.edit { it[KEY_DOZE_WHITELIST] = enabled } }
    suspend fun setSuspendThermalOnBoost(enabled: Boolean) { dataStore.edit { it[KEY_SUSPEND_THERMAL] = enabled } }
    suspend fun setForceMaxHzOnBoost(enabled: Boolean) { dataStore.edit { it[KEY_FORCE_MAX_HZ] = enabled } }

    companion object {
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_GLOBAL_AUTO_BOOST = booleanPreferencesKey("global_auto_boost")
        private val KEY_DND_ENABLED = booleanPreferencesKey("dnd_enabled")
        private val KEY_TOUCH_OPTIMIZATION_ENABLED = booleanPreferencesKey("touch_optimization_enabled")
        private val KEY_MEMORY_CLEANUP_ENABLED = booleanPreferencesKey("memory_cleanup_enabled")
        private val KEY_NETWORK_OPTIMIZATION_ENABLED = booleanPreferencesKey("network_optimization_enabled")
        private val KEY_ADAPTIVE_PERF = booleanPreferencesKey("adaptive_performance")
        private val KEY_OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        private val KEY_GAME_DETECTOR_ENABLED = booleanPreferencesKey("game_detector_enabled")
        private val KEY_THERMAL_AWARE = booleanPreferencesKey("thermal_aware_boost")

        private val KEY_SECURE_SETTINGS_ANIM_SCALE = booleanPreferencesKey("secure_settings_anim_scale")
        private val KEY_SECURE_SETTINGS_GAME_DRIVER = booleanPreferencesKey("secure_settings_game_driver")
        private val KEY_SECURE_SETTINGS_SYNC_OFF = booleanPreferencesKey("secure_settings_sync_off")
        private val KEY_SECURE_SETTINGS_MOBILE_DATA = booleanPreferencesKey("secure_settings_mobile_data")
        private val KEY_SECURE_SETTINGS_BATTERY_SAVER = booleanPreferencesKey("secure_settings_battery_saver")
        private val KEY_SECURE_SETTINGS_LOCATION_OFF = booleanPreferencesKey("secure_settings_location_off")
        // New keys
        private val KEY_DISABLE_BATTERY_SAVER_ON_BOOST = booleanPreferencesKey("disable_battery_saver_on_boost")
        private val KEY_NETWORK_DUAL_STACK = booleanPreferencesKey("network_dual_stack_enabled")
        private val KEY_DOZE_WHITELIST = booleanPreferencesKey("doze_whitelist_enabled")
        private val KEY_SUSPEND_THERMAL = booleanPreferencesKey("suspend_thermal_on_boost")
        private val KEY_FORCE_MAX_HZ = booleanPreferencesKey("force_max_hz_on_boost")
    }
}
