package com.gamelauncher.core.oemflags

import com.gamelauncher.core.device.OemBrand
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OemFlagRegistry — Master dictionary of cross-OEM hidden performance flags
 * matching the approved implementation plan.
 */
@Singleton
class OemFlagRegistry @Inject constructor() {

    val ALL_FLAGS: List<OemFlag> = listOf(
        // INFINIX (XOS) & TECNO (HiOS)
        OemFlag(
            id = "transsion_game_booster",
            key = "transsion_game_booster",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.INFINIX,
            title = "Transsion X-Boost Engine",
            description = "Forces XOS X-Boost & Tecno Dar-Link kernel gaming priority engine.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "transsion_touch_boost",
            key = "transsion_touch_boost",
            scope = FlagScope.SECURE,
            targetOem = OemBrand.INFINIX,
            title = "Touch Slop Accelerator",
            description = "Enables high frequency touch sampling rate on XOS & HiOS display panels.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "xos_game_mode",
            key = "xos_game_mode",
            scope = FlagScope.SYSTEM,
            targetOem = OemBrand.INFINIX,
            title = "XOS Performance Governor",
            description = "Unlocks XOS system performance governor and thread priority.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "hios_performance_mode",
            key = "hios_performance_mode",
            scope = FlagScope.SYSTEM,
            targetOem = OemBrand.TECNO,
            title = "HiOS Game Accelerator",
            description = "Unlocks Tecno HiOS performance profile and memory priority.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "game_space_opt",
            key = "game_space_optimization",
            scope = FlagScope.SECURE,
            targetOem = OemBrand.INFINIX,
            title = "Game Space Resource Allocator",
            description = "Allocates dedicated system memory queues to foreground game process.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.NEEDS_TESTING
        ),

        // XIAOMI (MIUI / HYPEROS)
        OemFlag(
            id = "miui_performance_mode",
            key = "miui_performance_mode",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.XIAOMI,
            title = "HyperOS Performance Mode",
            description = "Forces Xiaomi HyperOS System Performance Profile for max FPS.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "xiaomi_thermal_limit",
            key = "thermal_limit_enabled",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.XIAOMI,
            title = "HyperOS Thermal Override",
            description = "Suppresses MIUI thermal throttling daemon limits during heavy gaming.",
            activeValue = "0",
            defaultValue = "1",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "xiaomi_secure_thermal",
            key = "thermal_throttling_disabled",
            scope = FlagScope.SECURE,
            targetOem = OemBrand.XIAOMI,
            title = "MIUI Thermal Daemon Bypass",
            description = "Disables thermal throttling daemon in secure settings scope.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "xiaomi_refresh_mode",
            key = "refresh_rate_mode",
            scope = FlagScope.SECURE,
            targetOem = OemBrand.XIAOMI,
            title = "HyperOS Panel Refresh Lock",
            description = "Forces MIUI display driver into locked high refresh rate mode (2=High).",
            activeValue = "2",
            defaultValue = "1",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "xiaomi_user_hz",
            key = "user_refresh_rate",
            scope = FlagScope.SYSTEM,
            targetOem = OemBrand.XIAOMI,
            title = "System User Refresh Rate",
            description = "Overrides panel target refresh rate to 120Hz/144Hz.",
            activeValue = "120",
            defaultValue = "60",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),

        // SAMSUNG (ONE UI)
        OemFlag(
            id = "samsung_enhanced_proc",
            key = "sem_enhanced_cpu_responsiveness",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.SAMSUNG,
            title = "One UI Enhanced Processing",
            description = "Unlocks Samsung High CPU Responsiveness / Enhanced Processing mode.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "samsung_gos_thermal",
            key = "game_auto_temperature_control",
            scope = FlagScope.SECURE,
            targetOem = OemBrand.SAMSUNG,
            title = "GOS Thermal Throttle Bypass",
            description = "Disables Samsung Game Optimizing Service (GOS) auto temperature control.",
            activeValue = "0",
            defaultValue = "1",
            confidence = FlagConfidence.NEEDS_TESTING
        ),
        OemFlag(
            id = "samsung_refresh_mode",
            key = "refresh_rate_mode",
            scope = FlagScope.SECURE,
            targetOem = OemBrand.SAMSUNG,
            title = "One UI High Refresh Rate",
            description = "Locks Samsung display controller into 120Hz High Refresh Mode (2=High).",
            activeValue = "2",
            defaultValue = "1",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "samsung_game_home",
            key = "game_home_enable",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.SAMSUNG,
            title = "Game Hub System Binding",
            description = "Enables Samsung Game Hub system service IPC bindings.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "samsung_ram_plus",
            key = "ram_plus_size",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.SAMSUNG,
            title = "Disable Virtual RAM Plus",
            description = "Disables RAM Plus swap file to eliminate storage I/O latency.",
            activeValue = "0",
            defaultValue = "4",
            confidence = FlagConfidence.NEEDS_TESTING
        ),

        // HUAWEI (EMUI / HARMONYOS FORKS)
        OemFlag(
            id = "huawei_game_mode",
            key = "hw_game_mode",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.HUAWEI,
            title = "EMUI Game Mode",
            description = "Triggers Huawei system Game Dock performance profile.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "huawei_perf_mode",
            key = "hw_performance_mode",
            scope = FlagScope.SYSTEM,
            targetOem = OemBrand.HUAWEI,
            title = "Huawei Performance Governor",
            description = "Forces Kirin/Snapdragon performance governor on EMUI builds.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "huawei_touch_opt",
            key = "hw_touch_optimization",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.HUAWEI,
            title = "Huawei Touch Latency Boost",
            description = "Unlocks low touch latency pipeline on Huawei displays.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.NEEDS_TESTING
        ),

        // GENERIC AOSP / MULTI-OEM FALLBACKS
        OemFlag(
            id = "aosp_window_anim",
            key = "window_animation_scale",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.GENERIC,
            title = "Instant Window Animations",
            description = "Disables window animation scaling for instant app launching.",
            activeValue = "0.0",
            defaultValue = "1.0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "aosp_gpu_render",
            key = "force_gpu_rendering",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.GENERIC,
            title = "Force 2D GPU Hardware Acceleration",
            description = "Forces GPU rendering (Legacy setting; HWUI Skia renderer active on Android 13-16).",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.NEEDS_TESTING
        ),

        OemFlag(
            id = "aosp_mobile_data",
            key = "mobile_data_always_on",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.GENERIC,
            title = "Mobile Data Always Active",
            description = "Eliminates Wi-Fi to cellular handover latency spikes during online gaming.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        )
    )

    fun getFlagsForBrand(brand: OemBrand): List<OemFlag> {
        return ALL_FLAGS.filter { flag ->
            flag.targetOem == brand ||
            flag.targetOem == OemBrand.GENERIC ||
            (brand == OemBrand.TECNO && flag.targetOem == OemBrand.INFINIX) ||
            (brand == OemBrand.TRANSSION && flag.targetOem == OemBrand.INFINIX)
        }
    }
}
