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
        ),

        // SYSTEM PROPERTY (SETPROP) DEBUG & GRAPHICS TWEAKS
        OemFlag(
            id = "prop_sf_latch_unsignaled",
            key = "debug.sf.latch_unsignaled",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "SurfaceFlinger Unsignaled Latching",
            description = "Eliminates buffer latching latency for smooth 60-120+ FPS frame pacing.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_hwui_renderer",
            key = "debug.hwui.renderer",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "HWUI Skia Vulkan Renderer",
            description = "Forces Vulkan graphics pipeline for 2D UI, Compose & 3D game viewport rendering.",
            activeValue = "skiavk",
            defaultValue = "skiagl",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_renderengine_vulkan",
            key = "debug.renderengine.backend",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Vulkan 3D Composition Engine",
            description = "Forces SurfaceFlinger to execute 3D display composition using Vulkan backend.",
            activeValue = "vulkan",
            defaultValue = "gles",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_game_framerate_unthrottle",
            key = "debug.graphics.game_default_frame_rate.disabled",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "3D Game Viewport Unthrottle",
            description = "Disables default OS frame rate caps on 3D game viewports to unlock max GPU FPS.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_sf_backpressure",
            key = "debug.sf.disable_backpressure",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "SurfaceFlinger Backpressure Bypass",
            description = "Prevents buffer backpressure stalling during high frame rate rendering.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_fifo_ui",
            key = "sys.use_fifo_ui",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Realtime UI Thread Priority",
            description = "Assigns Realtime FIFO scheduling priority to main UI rendering threads.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_egl_hw",
            key = "debug.egl.hw",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Force EGL Hardware Acceleration",
            description = "Enforces 100% hardware-accelerated EGL rendering pipeline.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_swapinterval",
            key = "debug.gr.swapinterval",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Graphics Swap Interval Override",
            description = "Overrides graphics buffer swap interval to minimize VSync wait latency.",
            activeValue = "0",
            defaultValue = "1",
            confidence = FlagConfidence.NEEDS_TESTING
        ),
        OemFlag(
            id = "prop_dex2oat_threads",
            key = "dalvik.vm.dex2oat-threads",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "JIT Compiler Parallelism",
            description = "Allocates 4 parallel threads for runtime JIT DEX compilation.",
            activeValue = "4",
            defaultValue = "2",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_hwui_profile_visual",
            key = "debug.hwui.profile",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "HWUI Visual Timing Bars",
            description = "Renders on-screen GPU frame timing visual overlay bars.",
            activeValue = "visual_bars",
            defaultValue = "false",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_hwui_overdraw",
            key = "debug.hwui.overdraw",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "HWUI Overdraw Heatmap Visualizer",
            description = "Displays color-coded overdraw heatmap to identify redundant rendering cost.",
            activeValue = "show",
            defaultValue = "false",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_hwui_dirty_regions",
            key = "debug.hwui.show_dirty_regions",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "HWUI Redraw Region Visualizer",
            description = "Highlights dirty screen regions receiving redraw updates.",
            activeValue = "true",
            defaultValue = "false",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_atrace_disable",
            key = "debug.atrace.tags.enableflags",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Disable Systrace Overhead",
            description = "Disables system tracing tag overhead during gaming sessions.",
            activeValue = "0",
            defaultValue = "1",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_choreographer_skip_warn",
            key = "debug.choreographer.skipwarning",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Frame Skip Warning Threshold",
            description = "Sets Choreographer diagnostic warning threshold for skipped frames.",
            activeValue = "30",
            defaultValue = "1",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_jit_debug_info_skip",
            key = "debug.generate-debug-info",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Skip JIT Debug Symbol Generation",
            description = "Disables JIT debug symbol table creation overhead for maximum runtime speed.",
            activeValue = "false",
            defaultValue = "true",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_wifi_verbose_disable",
            key = "debug.wifi.enableWifiVerboseLogging",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Disable Wi-Fi Verbose Logging",
            description = "Suppresses Wi-Fi driver debug logging to reduce network latency overhead.",
            activeValue = "0",
            defaultValue = "1",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),
        OemFlag(
            id = "prop_sf_early_app_phase",
            key = "debug.sf.early_app_phase_offset_ns",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "SurfaceFlinger Early App Phase Offset",
            description = "Advances app frame render phase timing to reduce input-to-display latency.",
            activeValue = "5000000",
            defaultValue = "0",
            confidence = FlagConfidence.NEEDS_TESTING
        ),
        OemFlag(
            id = "prop_sf_early_sf_phase",
            key = "debug.sf.early_sf_phase_offset_ns",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "SurfaceFlinger Early SF Phase Offset",
            description = "Advances display composition phase timing for high FPS panel output.",
            activeValue = "3000000",
            defaultValue = "0",
            confidence = FlagConfidence.NEEDS_TESTING
        ),
        OemFlag(
            id = "prop_omx_hw_rank",
            key = "debug.stagefright.omx-default-rank",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Force HW Video Decoder Rank",
            description = "Prioritizes hardware accelerated OMX video decoding pipeline.",
            activeValue = "0",
            defaultValue = "1",
            confidence = FlagConfidence.CONFIRMED_WORKING
        ),

        // ANDROID 13 (API 33) ENHANCEMENTS
        OemFlag(
            id = "a13_phantom_proc_killer_disable",
            key = "settings_enable_monitor_phantom_procs",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.GENERIC,
            title = "Disable Phantom Process Killer",
            description = "Prevents Android 13 from terminating background game processes / emulator sub-threads.",
            activeValue = "0",
            defaultValue = "1",
            confidence = FlagConfidence.CONFIRMED_WORKING,
            minSdk = 33
        ),
        OemFlag(
            id = "a13_device_config_sync_disable",
            key = "device_config_sync_disabled",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.GENERIC,
            title = "Disable Cloud Config Overrides",
            description = "Blocks OEM/Google server-side throttling overrides during gameplay.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING,
            minSdk = 33
        ),

        // ANDROID 14 (API 34) ENHANCEMENTS
        OemFlag(
            id = "a14_app_standby_disable",
            key = "app_standby_enabled",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.GENERIC,
            title = "Disable App Standby Throttling",
            description = "Prevents CPU frequency drops when switching focus between game and launcher.",
            activeValue = "0",
            defaultValue = "1",
            confidence = FlagConfidence.CONFIRMED_WORKING,
            minSdk = 34
        ),
        OemFlag(
            id = "a14_game_dashboard_shortcut",
            key = "game_dashboard_shortcut",
            scope = FlagScope.SECURE,
            targetOem = OemBrand.GENERIC,
            title = "System Game Dashboard Shortcut",
            description = "Enables Android 14 native system Game Dashboard floating HUD shortcut.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING,
            minSdk = 34
        ),

        // ANDROID 15 (API 35) ENHANCEMENTS
        OemFlag(
            id = "a15_cached_apps_freezer_disable",
            key = "cached_apps_freezer",
            scope = FlagScope.GLOBAL,
            targetOem = OemBrand.GENERIC,
            title = "Disable Cached Apps Freezer",
            description = "Prevents Android 15 from freezing companion voice/overlay apps during gaming.",
            activeValue = "disabled",
            defaultValue = "enabled",
            confidence = FlagConfidence.CONFIRMED_WORKING,
            minSdk = 35
        ),

        // ANDROID 16 (API 36) ENHANCEMENTS
        OemFlag(
            id = "a16_sf_adpf_cpu_hint",
            key = "debug.sf.enable_adpf_cpu_hint",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "SurfaceFlinger ADPF CPU Hinting",
            description = "Integrates SurfaceFlinger directly with kernel ADPF CPU scheduling on Android 16.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING,
            minSdk = 36
        ),
        OemFlag(
            id = "a16_sf_native_vulkan",
            key = "debug.sf.use_vulkan",
            scope = FlagScope.SYSTEM_PROP,
            targetOem = OemBrand.GENERIC,
            title = "Native Vulkan System Compositor",
            description = "Forces native Vulkan compositor path for Android 16 display pipeline.",
            activeValue = "1",
            defaultValue = "0",
            confidence = FlagConfidence.CONFIRMED_WORKING,
            minSdk = 36
        )
    )

    fun getFlagsForBrand(brand: OemBrand): List<OemFlag> {
        val currentSdk = android.os.Build.VERSION.SDK_INT
        return ALL_FLAGS.filter { flag ->
            currentSdk >= flag.minSdk && (
                flag.targetOem == brand ||
                flag.targetOem == OemBrand.GENERIC ||
                (brand == OemBrand.TECNO && flag.targetOem == OemBrand.INFINIX) ||
                (brand == OemBrand.TRANSSION && flag.targetOem == OemBrand.INFINIX)
            )
        }
    }
}
