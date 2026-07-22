package com.gamelauncher.core.settings

/**
 * SettingsKeys — Centralized system settings keys for Global, System, Secure,
 * Developer Options, and OEM-specific HiOS/XOS keys.
 */
object SettingsKeys {

    // ── Developer Options & Animation Scales (Settings.Global) ────────────
    const val WINDOW_ANIMATION_SCALE = "window_animation_scale"
    const val TRANSITION_ANIMATION_SCALE = "transition_animation_scale"
    const val ANIMATOR_DURATION_SCALE = "animator_duration_scale"

    // ── Process & Performance Settings (Settings.Global) ──────────────────
    const val MAX_HIDDEN_APPS = "max_hidden_apps"
    const val GAME_DRIVER_ALL_APPS = "game_driver_all_apps"
    const val DEVICE_IDLE_CONSTANTS = "device_idle_constants"

    // ── Display & Touch Response (Settings.System) ────────────────────────
    const val PEAK_REFRESH_RATE = "peak_refresh_rate"
    const val MIN_REFRESH_RATE = "min_refresh_rate"
    const val TOUCH_SENSITIVITY = "touch_sensitivity"

    // ── OEM Specific: Infinix / Tecno (HiOS / XOS Settings.System & Global) ──
    const val HIOS_HIGH_PERFORMANCE_MODE = "high_performance_mode"
    const val XOS_GAME_BOOSTER_MODE = "xos_game_booster_mode"
    const val THERMAL_THROTTLING_DISABLED = "thermal_throttling_disabled"
    const val TECNO_GAMING_PROFILE = "tecno_gaming_profile"

    // ── Scope Namespaces ──────────────────────────────────────────────────
    enum class Scope(val namespace: String) {
        GLOBAL("global"),
        SYSTEM("system"),
        SECURE("secure")
    }
}
