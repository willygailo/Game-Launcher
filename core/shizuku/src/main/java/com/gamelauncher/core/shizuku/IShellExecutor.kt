// core/shizuku/src/main/java/com/gamelauncher/core/shizuku/IShellExecutor.kt
package com.gamelauncher.core.shizuku

/**
 * Interface defining ShellExecutor contract for typed privileged operations via Shizuku AIDL.
 */
interface IShellExecutor {
    suspend fun setPeakRefreshRate(hz: Float): Boolean
    suspend fun setMinRefreshRate(hz: Float): Boolean
    suspend fun setThermalOverride(disabled: Boolean): Boolean
    suspend fun writeSetting(namespace: String, key: String, value: String): Boolean
    suspend fun readSetting(namespace: String, key: String): String?
    suspend fun setDeviceConfig(namespace: String, key: String, value: String): Boolean
    suspend fun readDeviceConfig(namespace: String, key: String): String?
    suspend fun grantPermission(packageName: String, permissionName: String): Boolean
    suspend fun setAppOp(packageName: String, opName: String, mode: String): Boolean
    suspend fun executeCommand(command: String): String?
}

