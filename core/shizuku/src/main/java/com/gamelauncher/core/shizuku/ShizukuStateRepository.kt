package com.gamelauncher.core.shizuku

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ShizukuState — Represents explicit lifecycle and authorization states of Shizuku.
 */
sealed class ShizukuState {
    object NotInstalled : ShizukuState()
    object InstalledNotRunning : ShizukuState()
    object RunningNoPermission : ShizukuState()
    object Connected : ShizukuState()
    object Disconnected : ShizukuState()
}

/**
 * ShizukuStateRepository — Single source of truth for Shizuku connection and permission lifecycle.
 */
@Singleton
class ShizukuStateRepository @Inject constructor(
    private val shizukuManager: IShizukuManager
) {
    /**
     * Observable state flow representing current Shizuku lifecycle status.
     */
    val state: StateFlow<ShizukuState> = shizukuManager.state

    /**
     * Returns true if Shizuku is connected and ready for privileged execution.
     */
    val isConnected: Boolean get() = shizukuManager.isReady()
}

