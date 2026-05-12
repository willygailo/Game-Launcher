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

    // Setters
    suspend fun setDarkTheme(enabled: Boolean) { dataStore.edit { it[KEY_DARK_THEME] = enabled } }
    suspend fun setOnboardingCompleted() { dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = true } }
    suspend fun setGlobalAutoBoost(enabled: Boolean) { dataStore.edit { it[KEY_GLOBAL_AUTO_BOOST] = enabled } }
    suspend fun setOverlayEnabled(enabled: Boolean) { dataStore.edit { it[KEY_OVERLAY_ENABLED] = enabled } }
    suspend fun setGameDetectorEnabled(enabled: Boolean) { dataStore.edit { it[KEY_GAME_DETECTOR_ENABLED] = enabled } }
    suspend fun setThermalAwareBoost(enabled: Boolean) { dataStore.edit { it[KEY_THERMAL_AWARE] = enabled } }

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
    }
}
