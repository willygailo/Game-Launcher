// core/settings/src/main/java/com/gamelauncher/core/settings/SecureSettingsRepository.kt
package com.gamelauncher.core.settings

import android.content.Context
import android.provider.Settings
import com.gamelauncher.core.shizuku.IShellExecutor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SecureSettingsRepository — Privileged settings reader and writer supporting
 * dual-engine execution: Shizuku AIDL bridge and direct ADB ContentResolver fallback.
 */
@Singleton
class SecureSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val shellExecutor: IShellExecutor
) {
    /**
     * Writes a key-value setting entry to Global, System, or Secure scope.
     * Tries Shizuku AIDL first; falls back to direct ContentResolver if WRITE_SECURE_SETTINGS is granted.
     */
    suspend fun putString(
        scope: SettingsKeys.Scope,
        key: String,
        value: String
    ): Boolean = withContext(Dispatchers.IO) {
        val shizukuSuccess = try {
            shellExecutor.writeSetting(scope.namespace, key, value)
        } catch (_: Exception) {
            false
        }
        if (shizukuSuccess) return@withContext true

        // Fallback: Direct ContentResolver write for ADB-granted permissions on non-rooted devices
        return@withContext try {
            val resolver = context.contentResolver
            when (scope) {
                SettingsKeys.Scope.SYSTEM -> Settings.System.putString(resolver, key, value)
                SettingsKeys.Scope.SECURE -> Settings.Secure.putString(resolver, key, value)
                SettingsKeys.Scope.GLOBAL -> Settings.Global.putString(resolver, key, value)
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Reads a setting value from Global, System, or Secure scope.
     */
    suspend fun getString(
        scope: SettingsKeys.Scope,
        key: String
    ): String? = withContext(Dispatchers.IO) {
        val shizukuResult = try {
            shellExecutor.readSetting(scope.namespace, key)
        } catch (_: Exception) {
            null
        }
        if (shizukuResult != null && shizukuResult.isNotBlank() && shizukuResult != "null") {
            return@withContext shizukuResult.trim()
        }

        // Fallback: Direct ContentResolver read
        return@withContext try {
            val resolver = context.contentResolver
            val raw = when (scope) {
                SettingsKeys.Scope.SYSTEM -> Settings.System.getString(resolver, key)
                SettingsKeys.Scope.SECURE -> Settings.Secure.getString(resolver, key)
                SettingsKeys.Scope.GLOBAL -> Settings.Global.getString(resolver, key)
            }
            if (raw != null && raw.isNotBlank() && raw != "null") raw.trim() else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Reads a float setting value (e.g. animation scales). Returns fallback if missing or invalid.
     */
    suspend fun getFloat(
        scope: SettingsKeys.Scope,
        key: String,
        fallback: Float
    ): Float {
        val raw = getString(scope, key)
        return raw?.toFloatOrNull() ?: fallback
    }

    /**
     * Writes a float setting value.
     */
    suspend fun putFloat(
        scope: SettingsKeys.Scope,
        key: String,
        value: Float
    ): Boolean {
        return putString(scope, key, value.toString())
    }

    /**
     * Reads an integer setting value. Returns fallback if missing or invalid.
     */
    suspend fun getInt(
        scope: SettingsKeys.Scope,
        key: String,
        fallback: Int
    ): Int {
        val raw = getString(scope, key)
        return raw?.toIntOrNull() ?: fallback
    }

    /**
     * Writes an integer setting value.
     */
    suspend fun putInt(
        scope: SettingsKeys.Scope,
        key: String,
        value: Int
    ): Boolean {
        return putString(scope, key, value.toString())
    }

    /**
     * Deletes a setting entry from Global, System, or Secure scope.
     */
    suspend fun delete(
        scope: SettingsKeys.Scope,
        key: String
    ): Boolean = withContext(Dispatchers.IO) {
        putString(scope, key, "")
    }
}

