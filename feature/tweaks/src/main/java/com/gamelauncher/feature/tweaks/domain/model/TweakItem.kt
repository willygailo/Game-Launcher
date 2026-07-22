package com.gamelauncher.feature.tweaks.domain.model

enum class TweakCategory {
    REFRESH_RATE,
    CPU_GOVERNOR,
    GPU_RENDERING,
    THERMAL_THROTTLING,
    GAME_MODE
}

/**
 * TweakItem — Represents a system performance tweak configuration.
 *
 * @param isToggleActive Active state for binary toggle category switches (GPU_RENDERING, THERMAL_THROTTLING, GAME_MODE).
 * @param selectedValue Active selected option string for dropdown categories (REFRESH_RATE, CPU_GOVERNOR).
 * @param lastApplySuccessful Outcome of the most recent apply operation, or null if un-attempted.
 * @param badgeNote Custom UI badge label when unsupported (e.g., "Requires Root (Shizuku active)").
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
    val lastApplySuccessful: Boolean? = null,
    val badgeNote: String? = null
)
