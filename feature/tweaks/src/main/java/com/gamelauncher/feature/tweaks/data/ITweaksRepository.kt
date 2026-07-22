package com.gamelauncher.feature.tweaks.data

import com.gamelauncher.feature.tweaks.domain.model.TweakItem
import kotlinx.coroutines.flow.Flow

/**
 * ITweaksRepository — Repository interface contract for applying and observing system performance tweaks.
 */
interface ITweaksRepository {
    fun getAvailableTweaks(): Flow<List<TweakItem>>
    suspend fun applyRefreshRateTweak(refreshRateHz: Float): Boolean
    suspend fun applyCpuGovernorTweak(governor: String): Boolean
    suspend fun applyGpuRenderingTweak(enableGpuRendering: Boolean): Boolean
    suspend fun applyThermalThrottlingBypass(enableBypass: Boolean): Boolean
    suspend fun applyGameModeTweak(enableGameMode: Boolean): Boolean
}
