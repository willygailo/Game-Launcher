// feature/tweaks/src/main/java/com/gamelauncher/feature/tweaks/data/ITweaksRepository.kt
package com.gamelauncher.feature.tweaks.data

import com.gamelauncher.feature.tweaks.domain.model.TweakItem
import com.gamelauncher.feature.tweaks.domain.model.TweakResult
import kotlinx.coroutines.flow.Flow

/**
 * ITweaksRepository — Repository interface contract for applying and observing system performance tweaks.
 */
interface ITweaksRepository {
    fun getAvailableTweaks(): Flow<List<TweakItem>>
    suspend fun applyRogArmouryMode(modeName: String): TweakResult
    suspend fun applyTouchUltraTweaks(enable: Boolean): TweakResult
    suspend fun applySuperFastGameLaunch(): TweakResult
    suspend fun applyRefreshRateTweak(refreshRateHz: Float): TweakResult
    suspend fun applyFpsUnlockTweak(fpsTarget: String): TweakResult
    suspend fun clearHighRefreshRateBlacklist(): TweakResult
    suspend fun applyGpuRenderingTweak(enableGpuRendering: Boolean): TweakResult
    suspend fun clearGameDriverConfig(): TweakResult
    suspend fun applyCpuPerformanceBoost(enable: Boolean): TweakResult
    suspend fun applyThermalThrottlingBypass(enableBypass: Boolean): TweakResult
    suspend fun applyGameModeTweak(enableGameMode: Boolean): TweakResult
    suspend fun applyNetworkSpeedBoost(enable: Boolean): TweakResult
    suspend fun disablePhantomProcessKilling(disable: Boolean): TweakResult
    suspend fun disableAdaptiveBattery(disable: Boolean): TweakResult
}

