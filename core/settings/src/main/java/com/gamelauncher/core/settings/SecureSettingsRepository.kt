package com.gamelauncher.core.settings

import com.gamelauncher.core.shizuku.IShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SecureSettingsRepository — Privileged settings reader and writer using Shizuku ADB bridge.
 * Uses array-based execve execution via IShellExecutor.executeArgs() to prevent command injection vulnerabilities.
 */
@Singleton
class SecureSettingsRepository @Inject constructor(
    private val shellExecutor: IShellExecutor
) {
    /**
     * Writes a key-value setting entry to Global, System, or Secure scope via ADB shell.
     * Uses array arguments ("settings", "put", scope, key, value) to prevent shell command injection.
     */
    suspend fun putString(
        scope: SettingsKeys.Scope,
        key: String,
        value: String
    ): Boolean = withContext(Dispatchers.IO) {
        val result = shellExecutor.executeArgs("settings", "put", scope.namespace, key, value)
        result.exitCode == 0
    }

    /**
     * Reads a setting value from Global, System, or Secure scope via ADB shell.
     * Uses array arguments ("settings", "get", scope, key) to prevent shell command injection.
     */
    suspend fun getString(
        scope: SettingsKeys.Scope,
        key: String
    ): String? = withContext(Dispatchers.IO) {
        val result = shellExecutor.executeArgs("settings", "get", scope.namespace, key)
        if (result.exitCode == 0 && result.stdout.isNotBlank() && result.stdout != "null") {
            result.stdout.trim()
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
     * Deletes a setting entry from Global, System, or Secure scope via ADB shell.
     * Uses array arguments ("settings", "delete", scope, key) to prevent shell command injection.
     */
    suspend fun delete(
        scope: SettingsKeys.Scope,
        key: String
    ): Boolean = withContext(Dispatchers.IO) {
        val result = shellExecutor.executeArgs("settings", "delete", scope.namespace, key)
        result.exitCode == 0
    }
}
