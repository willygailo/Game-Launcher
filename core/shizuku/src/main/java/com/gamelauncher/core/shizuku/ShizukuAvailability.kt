// core/shizuku/src/main/java/com/gamelauncher/core/shizuku/ShizukuAvailability.kt
package com.gamelauncher.core.shizuku

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ShizukuState — Represents the explicit lifecycle and authorization states of Shizuku.
 */
sealed class ShizukuState {
    object NotInstalled : ShizukuState()
    object InstalledNotRunning : ShizukuState()
    object RunningNoPermission : ShizukuState()
    object Connected : ShizukuState()
    object Disconnected : ShizukuState()
}

/**
 * ShizukuAvailability — Single source of truth for Shizuku service status consumed by ViewModels.
 */
@Singleton
class ShizukuAvailability @Inject constructor(
    private val shizukuManager: IShizukuManager
) {
    /**
     * Observable state flow representing current Shizuku lifecycle status.
     */
    val state: StateFlow<ShizukuState> = shizukuManager.state

    /**
     * Returns whether Shizuku is currently connected and AIDL user service is ready.
     */
    val isReady: Boolean get() = shizukuManager.isReady()
}
