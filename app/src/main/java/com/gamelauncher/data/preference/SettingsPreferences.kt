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

    val globalAutoBoost: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_GLOBAL_AUTO_BOOST] ?: true }

    val globalTargetFps: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_GLOBAL_TARGET_FPS] ?: 60 }

    val globalTargetHz: Flow<Float> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_GLOBAL_TARGET_HZ] ?: 60f }
        
    val isOverlayEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_OVERLAY_ENABLED] ?: false }

    val gameDetectorEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_GAME_DETECTOR_ENABLED] ?: false }

    val dndEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_DND_ENABLED] ?: true }

    val touchOptimizationEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_TOUCH_OPTIMIZATION_ENABLED] ?: true }

    val memoryCleanupEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_MEMORY_CLEANUP_ENABLED] ?: true }

    val networkOptimizationEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_NETWORK_OPTIMIZATION_ENABLED] ?: true }

    val smartPerformanceEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_SMART_PERFORMANCE_ENABLED] ?: true }

    val autoDetectRefreshRate: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_AUTO_DETECT_REFRESH_RATE] ?: true }

    val temperatureMonitoringEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.map { it[KEY_TEMPERATURE_MONITORING_ENABLED] ?: true }

    suspend fun setGlobalAutoBoost(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_GLOBAL_AUTO_BOOST] = enabled }
    }

    suspend fun setGlobalTargetFps(fps: Int) {
        dataStore.edit { preferences -> preferences[KEY_GLOBAL_TARGET_FPS] = fps }
    }

    suspend fun setGlobalTargetHz(hz: Float) {
        dataStore.edit { preferences -> preferences[KEY_GLOBAL_TARGET_HZ] = hz }
    }
    
    suspend fun setOverlayEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_OVERLAY_ENABLED] = enabled }
    }

    suspend fun setGameDetectorEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_GAME_DETECTOR_ENABLED] = enabled }
    }

    suspend fun setDndEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_DND_ENABLED] = enabled }
    }

    suspend fun setTouchOptimizationEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_TOUCH_OPTIMIZATION_ENABLED] = enabled }
    }

    suspend fun setMemoryCleanupEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_MEMORY_CLEANUP_ENABLED] = enabled }
    }

    suspend fun setNetworkOptimizationEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_NETWORK_OPTIMIZATION_ENABLED] = enabled }
    }

    suspend fun setSmartPerformanceEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_SMART_PERFORMANCE_ENABLED] = enabled }
    }

    suspend fun setAutoDetectRefreshRate(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_AUTO_DETECT_REFRESH_RATE] = enabled }
    }

    suspend fun setTemperatureMonitoringEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_TEMPERATURE_MONITORING_ENABLED] = enabled }
    }

    companion object {
        private val KEY_GLOBAL_AUTO_BOOST = booleanPreferencesKey("global_auto_boost")
        private val KEY_GLOBAL_TARGET_FPS = intPreferencesKey("global_target_fps")
        private val KEY_GLOBAL_TARGET_HZ = floatPreferencesKey("global_target_hz")
        private val KEY_OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        private val KEY_GAME_DETECTOR_ENABLED = booleanPreferencesKey("game_detector_enabled")
        private val KEY_DND_ENABLED = booleanPreferencesKey("dnd_enabled")
        private val KEY_TOUCH_OPTIMIZATION_ENABLED = booleanPreferencesKey("touch_optimization_enabled")
        private val KEY_MEMORY_CLEANUP_ENABLED = booleanPreferencesKey("memory_cleanup_enabled")
        private val KEY_NETWORK_OPTIMIZATION_ENABLED = booleanPreferencesKey("network_optimization_enabled")
        private val KEY_SMART_PERFORMANCE_ENABLED = booleanPreferencesKey("smart_performance_enabled")
        private val KEY_AUTO_DETECT_REFRESH_RATE = booleanPreferencesKey("auto_detect_refresh_rate")
        private val KEY_TEMPERATURE_MONITORING_ENABLED = booleanPreferencesKey("temperature_monitoring_enabled")
    }
}