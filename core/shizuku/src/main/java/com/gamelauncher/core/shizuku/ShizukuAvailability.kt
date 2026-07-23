package com.gamelauncher.core.shizuku

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ShizukuAvailability — Backward-compatible facade delegating 100% to ShizukuStateRepository.
 */
@Singleton
class ShizukuAvailability @Inject constructor(
    private val repository: ShizukuStateRepository
) {
    val state: StateFlow<ShizukuState> get() = repository.state
    val isReady: Boolean get() = repository.isConnected
}

