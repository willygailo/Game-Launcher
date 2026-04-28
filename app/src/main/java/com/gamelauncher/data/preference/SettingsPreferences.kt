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

    companion object {
        private val KEY_GLOBAL_AUTO_BOOST = booleanPreferencesKey("global_auto_boost")
        private val KEY_GLOBAL_TARGET_FPS = intPreferencesKey("global_target_fps")
        private val KEY_GLOBAL_TARGET_HZ = floatPreferencesKey("global_target_hz")
        private val KEY_OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
    }
}
