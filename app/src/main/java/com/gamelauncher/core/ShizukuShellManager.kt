// app/src/main/java/com/gamelauncher/core/ShizukuShellManager.kt
package com.gamelauncher.core

import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.shizuku.IShizukuManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ShizukuShellManager — Thin wrapper checking Shizuku availability and toggling thermal overrides via AIDL.
 * String-based raw command execution and un-whitelisted permission grants are strictly removed.
 */
@Singleton
class ShizukuShellManager @Inject constructor(
    private val shizukuManager: IShizukuManager,
    private val shellExecutor: IShellExecutor
) {

    fun isShizukuRunning(): Boolean = shizukuManager.isReady()

    fun hasShizukuPermission(): Boolean = shizukuManager.isReady()

    fun isAvailable(): Boolean = shizukuManager.isReady()

    fun requestPermission() {
        try {
            shizukuManager.requestPermission()
        } catch (_: Exception) {}
    }

    suspend fun suspendThermalEngines(): Boolean {
        return shellExecutor.setThermalOverride(true)
    }

    suspend fun resumeThermalEngines(): Boolean {
        return shellExecutor.setThermalOverride(false)
    }
}
