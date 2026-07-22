// core/settings/src/main/java/com/gamelauncher/core/settings/SecureSettingsRepository.kt
package com.gamelauncher.core.settings

import com.gamelauncher.core.shizuku.IShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SecureSettingsRepository — Privileged settings reader and writer using Shizuku AIDL bridge.
 * Uses typed writeSetting and readSetting methods to prevent command injection vulnerabilities.
 */
@Singleton
class SecureSettingsRepository @Inject constructor(
    private val shellExecutor: IShellExecutor
) {
    /**
     * Writes a key-value setting entry to Global, System, or Secure scope via Shizuku AIDL.
     */
    suspend fun putString(
        scope: SettingsKeys.Scope,
        key: String,
        value: String
    ): Boolean = withContext(Dispatchers.IO) {
        shellExecutor.writeSetting(scope.namespace, key, value)
    }

    /**
     * Reads a setting value from Global, System, or Secure scope via Shizuku AIDL.
     */
    suspend fun getString(
        scope: SettingsKeys.Scope,
        key: String
    ): String? = withContext(Dispatchers.IO) {
        val result = shellExecutor.readSetting(scope.namespace, key)
        if (result != null && result.isNotBlank() && result != "null") {
            result.trim()
        } else {
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
     * Deletes a setting entry from Global, System, or Secure scope via Shizuku AIDL.
     */
    suspend fun delete(
        scope: SettingsKeys.Scope,
        key: String
    ): Boolean = withContext(Dispatchers.IO) {
        shellExecutor.writeSetting(scope.namespace, key, "")
    }
}
