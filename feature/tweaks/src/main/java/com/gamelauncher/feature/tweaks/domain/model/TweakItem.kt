// feature/tweaks/src/main/java/com/gamelauncher/feature/tweaks/domain/model/TweakItem.kt
package com.gamelauncher.feature.tweaks.domain.model

/**
 * TweakResult — Represents the explicit outcome of applying a system tweak.
 */
sealed interface TweakResult {
    object Confirmed : TweakResult
    data class SilentlyIgnored(val key: String) : TweakResult
    data class Failed(val reason: String) : TweakResult
}

/**
 * TweakCategory — System category classification for performance tweaks.
 */
enum class TweakCategory {
    ROG_MODE,
    TOUCH,
    SUPER_FAST_LAUNCH,
    REFRESH_RATE,
    FPS_UNLOCK,
    GPU_RENDERING,
    CPU_PERFORMANCE,
    NETWORK_SPEED,
    THERMAL_THROTTLING,
    GAME_MODE,
    MEMORY,
    POWER
}

/**
 * TweakItem — Represents a system performance tweak configuration.
 */
data class TweakItem(
    val id: String,
    val title: String,
    val description: String,
    val category: TweakCategory,
    val isToggleActive: Boolean = false,
    val selectedValue: String? = null,
    val supportedValues: List<String> = emptyList(),
    val isSupportedByDevice: Boolean = true,
    val lastResult: TweakResult? = null,
    val badgeNote: String? = null
)

