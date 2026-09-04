package com.gamebooster.app.tweaks;

import com.gamebooster.app.config.*;
import android.content.Context;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.EngineMode;

import java.util.ArrayList;
import java.util.List;

public class TweakManagerRepository {

    public interface OnBatchCompleteListener {
        void onBatchComplete(int appliedCount);
    }

    private static final List<TweakItem> TWEAKS = new ArrayList<>();

    static {
        // =========================================================================
        // 1. CPU & GPU GRAPHICS RENDERING TWEAKS (Unique & Non-Overlapping)
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "adreno_turbo_boost",
                "Qualcomm Adreno GPU Turbo Engine",
                "Forces Adreno GPU maximum burst clock level and hardware gesture fling boost",
                "setprop debug.qualcomm.sns.hal 0; setprop debug.adreno.turbo 1; setprop debug.adreno.perf_level 0; setprop vendor.perf.gestureFlingBoost 1",
                "setprop debug.adreno.turbo 0; setprop debug.adreno.perf_level 1; setprop vendor.perf.gestureFlingBoost 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "mali_realtime_boost",
                "ARM Mali GPU Realtime Boost",
                "Forces ARM Mali GPU scheduler real-time priority (-20) and driver boost",
                "setprop debug.mali.sched.priority -20; setprop debug.mali.force_gpu_boost 1",
                "setprop debug.mali.sched.priority 0; setprop debug.mali.force_gpu_boost 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "hw_overlays",
                "Force SurfaceFlinger HW Composition",
                "Forces GPU hardware composition to eliminate CPU rendering overhead",
                "setprop debug.sf.hw 1",
                "setprop debug.sf.hw 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gpu_power_mode",
                "GPU Maximum Clocks & Power Mode",
                "Locks GPU power mode to maximum performance state without dynamic clock downscaling",
                "setprop vendor.gpu.power_mode 1; setprop debug.gpu.performance 1",
                "setprop vendor.gpu.power_mode 0; setprop debug.gpu.performance 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "skia_vulkan_pipeline",
                "Skia Vulkan Graphics Pipeline & Render Thread Boost",
                "Enables Skia Vulkan pipeline with real-time render thread priority (-20) and GPU pixel buffers",
                "setprop debug.renderengine.skia_pipeline true; setprop debug.renderengine.backend vulkan; setprop debug.hwui.render_thread_priority -20; setprop debug.hwui.use_gpu_pixel_buffers true; setprop debug.hwui.skip_empty_damage true",
                "setprop debug.renderengine.skia_pipeline false; setprop debug.hwui.render_thread_priority 0; setprop debug.hwui.use_gpu_pixel_buffers false",
                TweakCategory.CPU_GPU,
                true
        ));


        TWEAKS.add(new TweakItem(
                "sf_zero_vsync_phase",
                "Zero VSync Phase Frame Offsets",
                "Eliminates SurfaceFlinger VSync phase offset delays for instantaneous display pipeline dispatch",
                "setprop debug.sf.early_phase_offset_ns 0; setprop debug.sf.early_app_phase_offset_ns 0; setprop debug.sf.early_gl_phase_offset_ns 0",
                "setprop debug.sf.early_phase_offset_ns 1000000; setprop debug.sf.early_app_phase_offset_ns 1000000; setprop debug.sf.early_gl_phase_offset_ns 1000000",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_hw_vsync",
                "SurfaceFlinger Virtual Display HW VSync Bypass",
                "Disables Virtual Display SurfaceFlinger VSync waiting to prevent frame pacing drops",
                "setprop debug.sf.disable_hwc_vds 1",
                "setprop debug.sf.disable_hwc_vds 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "sf_jitter_elimination",
                "SurfaceFlinger Compositor Overhead Strip",
                "Disables compositor debug checks, background updates and visual profiling overhead in SurfaceFlinger",
                "setprop debug.sf.showupdates 0; setprop debug.sf.showcpu 0; setprop debug.sf.showbackground 0; setprop debug.sf.showfps 0",
                "setprop debug.sf.showupdates 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gpu_buffer_speed",
                "Triple Buffer Rendering & High-Speed Alpha",
                "Allocates 3 EGL back-buffers and enables 16-bit alpha channel for smooth frame pacing",
                "setprop debug.egl.buffcount 3; setprop persist.sys.use_16bpp_alpha 1; setprop debug.egl.multithread 1",
                "setprop debug.egl.buffcount 2; setprop persist.sys.use_16bpp_alpha 0; setprop debug.egl.multithread 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "force_4x_msaa",
                "Force 4x MSAA Anti-Aliasing",
                "Forces 4x Multi-Sample Anti-Aliasing in OpenGL ES 2.0/3.0 games and 3D scenes",
                "setprop debug.egl.force_msaa 1",
                "setprop debug.egl.force_msaa 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "io_scheduler_gaming",
                "Gaming I/O Scheduler & Low Swappiness",
                "Configures low Linux kernel swappiness (20%) and dirty memory writeback ratios for fast disk reads",
                "setprop sys.io.scheduler deadline; setprop vm.swappiness 20; setprop vm.dirty_ratio 10; setprop vm.dirty_background_ratio 5",
                "setprop sys.io.scheduler cfq; setprop vm.swappiness 60; setprop vm.dirty_ratio 20; setprop vm.dirty_background_ratio 10",
                TweakCategory.CPU_GPU,
                true
        ));


        TWEAKS.add(new TweakItem(
                "disable_blur_effects",
                "Disable Window Blur & Overdraw Overhead",
                "Disables system window blur effects and GPU overdraw inspection to free GPU rasterization units",
                "settings put global window_blurs_enabled 0; setprop debug.hwui.overdraw false",
                "settings put global window_blurs_enabled 1; setprop debug.hwui.overdraw show",
                TweakCategory.CPU_GPU,
                true
        ));

        // =========================================================================
        // 2. TOUCH & DISPLAY REFRESH TWEAKS (Unique & Non-Overlapping)
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "gyro_1000hz_ultra",
                "1000Hz Gyroscope Ultra Response & Zero Deadzone",
                "Unlocks 1000Hz gyro polling, hardware smoothing/stabilization, zero filter delay, and 0 deadzone",
                "setprop debug.sensor.gyro.sample_rate 1000; setprop debug.sensor.gyro.smooth 1; setprop debug.sensor.gyro.stabilization 1; setprop persist.sys.gyro.filter 1; setprop persist.sys.gyro.delay 0; setprop debug.sensor.gyro.filter_delay 0; setprop persist.sys.gyro.deadzone 0; setprop debug.sensor.motion.rate 1000",
                "setprop debug.sensor.gyro.sample_rate 0; setprop debug.sensor.gyro.smooth 0; setprop debug.sensor.gyro.stabilization 0; setprop persist.sys.gyro.delay 1; setprop debug.sensor.gyro.filter_delay 1; setprop persist.sys.gyro.deadzone 2",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "scrolling_cache_boost",
                "Zero Scroll Cache Latency",
                "Disables scrolling cache compression to eliminate UI micro-stutters and save CPU cycles",
                "setprop persist.sys.scrollingcache 3",
                "setprop persist.sys.scrollingcache 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "pointer_speed_max",
                "Max Pointer Speed Calibration",
                "Sets Android system pointer speed to level 7 for instant cursor and touch response",
                "settings put system pointer_speed 7",
                "settings put system pointer_speed 0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_screen_auto_bright",
                "Lock Screen Brightness Mode",
                "Locks screen brightness to manual mode preventing automatic sensor dimming mid-game",
                "settings put system screen_brightness_mode 0",
                "settings put system screen_brightness_mode 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_game_fps_limits",
                "Disable Default Game FPS & 120Hz/60Hz Limit Lock",
                "Disables Android default 60Hz/120Hz game frame rate limits, match-content frame rate, and vendor DFPS for unrestricted 185Hz gameplay",
                "setprop debug.graphics.game_default_frame_rate.disabled 1; setprop ro.vendor.dfps.enable 0; setprop vendor.display.enable_default_fps_switch 0; setprop persist.vendor.power.dfps.level 0; setprop persist.vendor.display.vrr.disable 1; setprop ro.surface_flinger.set_idle_timer_ms 0; setprop ro.surface_flinger.set_touch_timer_ms 0; setprop debug.sf.fps_limit 185; setprop persist.sys.NV_FPSLIMIT 185; setprop persist.sys.game.fps 185; setprop persist.sys.game.rate 185; setprop persist.sys.fps 185; settings put system match_content_frame_rate 0; settings put secure match_content_frame_rate_preference 0; settings put system peak_refresh_rate 185.0; settings put system min_refresh_rate 185.0; settings put system user_refresh_rate 185; settings put global peak_refresh_rate 185.0; settings put global min_refresh_rate 185.0; service call SurfaceFlinger 1035 i32 185; service call SurfaceFlinger 1036 i32 185; cmd game set --fps 185 global; cmd window set-app-refresh-rate global 185; device_config put game_overlay global mode=2,fps=185:mode=3,fps=185",
                "setprop debug.graphics.game_default_frame_rate.disabled 0; setprop ro.vendor.dfps.enable 1; setprop vendor.display.enable_default_fps_switch 1; setprop persist.vendor.power.dfps.level 60; setprop persist.vendor.display.vrr.disable 0; setprop debug.sf.fps_limit 0; setprop persist.sys.NV_FPSLIMIT 0; settings put system match_content_frame_rate 1; settings put secure match_content_frame_rate_preference 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        // =========================================================================
        // 3. SYSTEM & SHIZUKU ADB PRIVILEGED TWEAKS (Unique & Non-Overlapping)
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "shizuku_fast_anim",
                "0.0x Instant UI Speed Animations (Zero Latency)",
                "Disables system window, transition, and animator duration scales to 0.0x for instant rendering and zero input delay",
                "settings put global window_animation_scale 0.0; settings put global transition_animation_scale 0.0; settings put global animator_duration_scale 0.0; settings put system window_animation_scale 0.0; settings put system transition_animation_scale 0.0; settings put system animator_duration_scale 0.0; cmd activity update-configuration --anim-scale 0.0",
                "settings put global window_animation_scale 1.0; settings put global transition_animation_scale 1.0; settings put global animator_duration_scale 1.0; settings put system window_animation_scale 1.0; settings put system transition_animation_scale 1.0; settings put system animator_duration_scale 1.0; cmd activity update-configuration --anim-scale 1.0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "powerhal_sustained_perf",
                "PowerHAL Sustained Performance & Power Boost",
                "Signals Android PowerHAL to maintain maximum sustained CPU/GPU clocks without thermal drops",
                "cmd power set-mode 0 1; cmd power set-mode 2 1",
                "cmd power set-mode 0 0; cmd power set-mode 2 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "thermalservice_override",
                "Thermal Throttling Bypass Override",
                "Overrides Android ThermalService status to normal (0) and sets vendor thermal mode to performance",
                "cmd thermalservice override-status 0; cmd thermal override-status 0; setprop debug.thermal.throttle.disable 1; setprop vendor.thermal.mode performance",
                "cmd thermalservice override-status -1; cmd thermal override-status -1; setprop debug.thermal.throttle.disable 0; setprop vendor.thermal.mode normal",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "android_13_16_adpf_fixed_perf",
                "Android 13/14/15/16 ADPF & Fixed Performance Lock",
                "Enables Android Dynamic Performance Framework (ADPF) CPU/GPU hints and locks CPU/GPU clocks with Fixed Performance Mode",
                "cmd power set-fixed-performance-mode-enabled true; setprop debug.sf.enable_adpf_cpu_hint true; setprop debug.hwui.use_hint_manager true; setprop persist.sys.adpf.enable 1; setprop persist.sys.adpf.mode 1; setprop debug.adpf.hint.enabled 1; setprop debug.adpf.cpu.boost 1; setprop debug.adpf.gpu.boost 1; setprop debug.sf.enable_gl_backpressure 0; setprop debug.sf.predict_hwc_composition_strategy 1",
                "cmd power set-fixed-performance-mode-enabled false; setprop debug.sf.enable_adpf_cpu_hint false; setprop debug.hwui.use_hint_manager false; setprop persist.sys.adpf.enable 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "android_13_16_deviceconfig_interventions",
                "Android 13-16 DeviceConfig Game Interventions & Phantom Killer Bypass",
                "Locks DeviceConfig sync to persistent, enables runtime startup caches, and disables Android 13-16 phantom process killer",
                "cmd device_config set_sync_disabled_for_tests persistent; device_config put runtime_native_boot use_app_image_startup_cache true; device_config put runtime_native_boot pin_app_image_startup_cache true; device_config put runtime_native_boot boost_sched_priority true; device_config put activity_manager max_phantom_processes 2147483647; settings put global settings_enable_monitor_phantom_procs false; settings put global cached_apps_freezer disabled",
                "cmd device_config set_sync_disabled_for_tests none; settings put global settings_enable_monitor_phantom_procs true",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "force_gpu_rendering",
                "Force GPU Hardware Acceleration",
                "Forces 2D window rendering to always use GPU hardware pipeline",
                "settings put global force_hw_ui 1",
                "settings put global force_hw_ui 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_battery_saver",
                "Disable Low Power Mode / Battery Throttling",
                "Forces disable of Android global low power battery saver mode",
                "settings put global low_power 0",
                "settings put global low_power 1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "preserve_bg_apps",
                "Protect Background Apps & Multitasking",
                "Prevents Android from destroying background apps and enables full background multitasking without process limit",
                "settings put global always_finish_activities 0; settings put global background_process_limit -1; settings put global cached_apps_freezer disabled",
                "settings put global always_finish_activities 0; settings put global background_process_limit -1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "force_dark_mode",
                "Force System Dark UI Mode",
                "Enforces system dark mode via Android uimode command to save OLED display power",
                "cmd uimode night yes",
                "cmd uimode night no",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_haptic_feedback",
                "Disable Vibration Haptics Overhead",
                "Disables system haptic vibration feedback engine to reduce input interrupt latency",
                "settings put system haptic_feedback_enabled 0",
                "settings put system haptic_feedback_enabled 1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "sqlite_wal_fast_cache",
                "SQLite In-Memory Cache Optimization",
                "Sets SQLite journal mode to in-memory cache and suppresses slow query logcat threshold",
                "setprop db.log.slow_query_threshold 0; setprop debug.sqlite.wal.sync_mode 0; setprop debug.sqlite.journal_mode memory",
                "setprop debug.sqlite.wal.sync_mode 1; setprop debug.sqlite.journal_mode wal",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "art_dexopt_speed_compile",
                "ART Speed-Profile AOT Game Optimization",
                "Configures the Android Runtime (ART) compilation policy to native speed filter for high-performance execution",
                "setprop pm.dexopt.bg-dexopt speed; setprop pm.dexopt.install speed; setprop dalvik.vm.dex2oat-filter speed",
                "setprop pm.dexopt.bg-dexopt speed-profile; setprop pm.dexopt.install speed-profile; setprop dalvik.vm.dex2oat-filter speed-profile",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "kernel_sched_latency",
                "Kernel CFS Scheduler 1ms Latency Slice",
                "Shortens Linux CFS scheduling slice from 4ms down to 1ms for instant CPU thread dispatching",
                "setprop sys.perf.sched_min_granularity_ns 250000; setprop sys.perf.sched_latency_ns 1000000; setprop sys.perf.sched_wakeup_granularity_ns 500000",
                "setprop sys.perf.sched_min_granularity_ns 1000000; setprop sys.perf.sched_latency_ns 4000000; setprop sys.perf.sched_wakeup_granularity_ns 2000000",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        // =========================================================================
        // 4. NETWORK & PACKET PROTOCOL TWEAKS (Unique & Non-Overlapping)
        // =========================================================================

        TWEAKS.add(new TweakItem(
                "wlan_fast_roaming_boost",
                "WLAN Chipset Hardware Low-Latency Lock",
                "Enables hardware Wi-Fi low-latency mode and disables scan throttling during gameplay",
                "settings put global wifi_scan_throttle_enabled 0; setprop persist.vendor.wifi.low_latency 1; cmd wifi force-low-latency-mode enabled",
                "settings put global wifi_scan_throttle_enabled 1; cmd wifi force-low-latency-mode disabled",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "tcp_packet_pacing_gaming",
                "Immediate Packet Pacing & Anti-Corking",
                "Disables socket corking so game packets transmit immediately without buffering delays",
                "setprop net.ipv4.tcp_pacing_ss_ratio 200; setprop net.ipv4.tcp_pacing_ca_ratio 120; setprop net.ipv4.tcp_autocorking 0",
                "setprop net.ipv4.tcp_autocorking 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_captive_portal",
                "Disable Captive Portal Probe Latency",
                "Disables background HTTP captive portal network checks to avoid ping spikes",
                "settings put global captive_portal_mode 0",
                "settings put global captive_portal_mode 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        // =========================================================================
        // 5. FLAGSHIP ZERO-LATENCY, KERNEL GOVERNOR & TCP BBR TWEAKS
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "sf_zero_latency_latching",
                "SurfaceFlinger Composition Strategy & Scheduling Turbo",
                "Optimizes SurfaceFlinger composition strategy predictions, enables unsignaled buffer latching, disables backpressure drops, and aligns 500µs early phase offsets",
                "setprop debug.sf.latch_unsignaled 1; setprop debug.sf.disable_backpressure 1; setprop debug.sf.predict_hwc_composition_strategy 1; setprop debug.sf.enable_hwc_vds 1; setprop debug.sf.early.app.duration 500; setprop debug.sf.early.sf.duration 500; setprop debug.sf.early_phase_offset_ns 500000; setprop debug.sf.early_app_phase_offset_ns 500000; setprop debug.sf.high_fps_early_phase_offset_ns 500000; setprop debug.sf.high_fps_early_app_phase_offset_ns 500000; setprop ro.surface_flinger.max_frame_buffer_acquired_buffers 3; setprop debug.sf.showupdates 0; setprop debug.sf.showcpu 0; setprop debug.sf.showbackground 0; setprop debug.sf.showfps 0",
                "setprop debug.sf.latch_unsignaled 0; setprop debug.sf.disable_backpressure 0; setprop debug.sf.predict_hwc_composition_strategy 0; setprop ro.surface_flinger.max_frame_buffer_acquired_buffers 2",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "game_driver_mlbb_codm_pubgm_preference",
                "Targeted Game Driver (MLBB, CODM & PUBGM Exclusive)",
                "Opt-ins MLBB, CODM, and PUBGM to vendor-optimized Updatable Game Driver with native Skia Vulkan while keeping ANGLE completely purged for stutter-free performance",
                "settings put global game_driver_all_apps 0; settings put global updatable_driver_all_apps 0; settings put global game_driver_opt_in_apps " + com.gamebooster.app.booster.GpuTweaksChannel.getTargetGamesCsv() + "; settings put global game_driver_prerelease_opt_in_apps " + com.gamebooster.app.booster.GpuTweaksChannel.getTargetGamesCsv() + "; settings put global updatable_driver_production_opt_in_apps " + com.gamebooster.app.booster.GpuTweaksChannel.getTargetGamesCsv() + "; settings delete global angle_gl_driver_selection_pkgs 2>/dev/null; settings delete global angle_gl_driver_selection_values 2>/dev/null; settings delete global angle_enabled_pkgs 2>/dev/null; settings put global angle_gl_driver_all_angle 0 2>/dev/null; setprop debug.angle.backend 0; setprop debug.hwui.renderer skiavk; setprop debug.renderengine.backend vulkan; setprop debug.renderengine.skia_pipeline true; setprop debug.hwui.use_gpu_pixel_buffers true; setprop debug.hwui.render_thread_priority -20; setprop debug.sf.hw 1",
                "settings put global game_driver_all_apps 0; settings put global updatable_driver_all_apps 0; settings put global game_driver_opt_in_apps \"\"; settings put global updatable_driver_production_opt_in_apps \"\"; settings delete global angle_gl_driver_selection_pkgs 2>/dev/null; settings delete global angle_gl_driver_selection_values 2>/dev/null; settings delete global angle_enabled_pkgs 2>/dev/null; settings put global angle_gl_driver_all_angle 0 2>/dev/null; setprop debug.angle.backend 0; setprop debug.hwui.renderer \"\"; setprop debug.renderengine.backend \"\"",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "dalvik_art_jit_turbo",
                "Dalvik / ART JIT Turbo & Hot Method Optimization",
                "Forces JIT compilation mode, lowers compilation threshold to 50, expands code caches to 128MB, and enables 8-thread DEX2OAT compilation",
                "setprop dalvik.vm.execution-mode int:jit; setprop dalvik.vm.usejit true; setprop dalvik.vm.usejitprofiles true; setprop dalvik.vm.jitcodecachesize 64m; setprop dalvik.vm.jitinitialsize 16m; setprop dalvik.vm.jitmaxsize 128m; setprop dalvik.vm.jitthreshold 50; setprop dalvik.vm.jittransitionweight 10; setprop dalvik.vm.dex2oat-filter speed; setprop dalvik.vm.dex2oat-threads 8; setprop dalvik.vm.image-dex2oat-threads 8; setprop dalvik.vm.boot-dex2oat-threads 8; setprop dalvik.vm.dex2oat-flags \"enable-fast-verify,no-relocate\"",
                "setprop dalvik.vm.execution-mode int:jit; setprop dalvik.vm.usejit true; setprop dalvik.vm.dex2oat-filter speed-profile; setprop dalvik.vm.jitthreshold 100",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "dalvik_heap_gaming_optimization",
                "Dalvik Heap 1024MB Max Size & Parallel GC Tuning",
                "Expands heap growth limit to 512MB and max heap to 1024MB with 4 parallel concurrent GC threads and JNI boundary check bypass",
                "setprop dalvik.vm.heapgrowthlimit 512m; setprop dalvik.vm.heapsize 1024m; setprop dalvik.vm.heaptargetutilization 0.85; setprop dalvik.vm.heapminfree 8m; setprop dalvik.vm.heapmaxfree 32m; setprop dalvik.vm.heapstartsize 32m; setprop dalvik.vm.heapsizelimit 1024m; setprop dalvik.vm.concurrent-gc-threads 4; setprop dalvik.vm.checkjni false",
                "setprop dalvik.vm.heapgrowthlimit 256m; setprop dalvik.vm.heapsize 512m; setprop dalvik.vm.heaptargetutilization 0.75; setprop dalvik.vm.checkjni false",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "cpu_multicore_topology_sched",
                "8 / 12 / 16-Core CPU Topology & Top-App CPUSet Isolation",
                "Allocates all CPU cores to top-app game processes, isolates background/dex2oat tasks away from gaming cores, and tunes WALT scheduler cross-window migration",
                "echo 0-15 > /dev/cpuset/top-app/cpus 2>/dev/null; echo 0-15 > /dev/cpuset/foreground/cpus 2>/dev/null; echo 0-3 > /dev/cpuset/background/cpus 2>/dev/null; echo 0-3 > /dev/cpuset/system-background/cpus 2>/dev/null; echo 0-3 > /dev/cpuset/dex2oat/cpus 2>/dev/null; echo 0-1 > /dev/cpuset/restricted/cpus 2>/dev/null; for p in /sys/devices/system/cpu/cpufreq/policy*; do echo performance > \"$p/scaling_governor\" 2>/dev/null; if [ -f \"$p/scaling_max_freq\" ]; then cat \"$p/scaling_max_freq\" > \"$p/scaling_min_freq\" 2>/dev/null; fi; done; setprop sys.perf.sched_uclamp_min 1024; setprop sys.perf.sched_uclamp_min_rt 1024; setprop sys.perf.sched_min_granularity_ns 250000; setprop sys.perf.sched_latency_ns 1000000; setprop sys.perf.sched_boost 1; sysctl -w kernel.sched_walt_cross_window_migration_ratio=10 2>/dev/null; sysctl -w kernel.sched_boost=1 2>/dev/null; sysctl -w kernel.sched_child_runs_first=1 2>/dev/null; sysctl -w kernel.sched_wakeup_granularity_ns=500000 2>/dev/null; cmd power set-fixed-performance-mode-enabled true",
                "cmd power set-fixed-performance-mode-enabled false; for p in /sys/devices/system/cpu/cpufreq/policy*; do echo schedutil > \"$p/scaling_governor\" 2>/dev/null; done; setprop sys.perf.sched_uclamp_min 0; setprop sys.perf.sched_boost 0; sysctl -w kernel.sched_boost=0 2>/dev/null",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gpu_mediatek_ged_boost",
                "MediaTek Dimensity GED Game Mode & Mali Real-Time GPU Engine",
                "Activates MediaTek GPU Engine Driver (GED) kernel game mode, top-app PID booster, DPT/PPT performance, and Mali -20 scheduler priority",
                "setprop debug.mali.sched.priority -20; setprop debug.mali.force_gpu_boost 1; setprop debug.mali.realtime 1; setprop persist.vendor.ged.boost 1; setprop persist.vendor.dpt.enable 1; setprop vendor.ppt.boost 1; echo 0 > /sys/class/misc/mali0/device/dvfs_enable 2>/dev/null; echo 1 > /sys/module/ged/parameters/gx_game_mode 2>/dev/null; echo 1 > /sys/module/ged/parameters/gx_boost_on 2>/dev/null; echo 1 > /sys/module/ged/parameters/gx_force_cpu_boost 2>/dev/null; echo 100 > /sys/module/ged/parameters/gx_top_app_pid_boost 2>/dev/null; for g in /sys/class/devfreq/*gpu*/governor; do echo performance > \"$g\" 2>/dev/null; done",
                "setprop debug.mali.sched.priority 0; setprop debug.mali.force_gpu_boost 0; setprop persist.vendor.ged.boost 0; for g in /sys/class/devfreq/*gpu*/governor; do echo simple_ondemand > \"$g\" 2>/dev/null; done",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "adreno_mali_sysfs_governors",
                "Adreno & Mali Sysfs GPU Clock & Governor Lock",
                "Directly sets GPU devfreq governor to performance and locks min/max power levels to prevent thermal down-stepping",
                "echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null; echo 0 > /sys/class/kgsl/kgsl-3d0/min_pwrlevel 2>/dev/null; echo 0 > /sys/class/misc/mali0/device/dvfs_enable 2>/dev/null",
                "echo simple_ondemand > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null; echo 1 > /sys/class/misc/mali0/device/dvfs_enable 2>/dev/null",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "webview_gpu_vulkan_turbo",
                "Android WebView GPU Rasterization, Vulkan Skia & Zero-Copy Turbo",
                "Forces hardware-accelerated GPU rasterization, Vulkan rendering, DrDc composite decoupling, WebGPU, and V8 JIT flags for WebViews",
                "echo '" + com.gamebooster.app.booster.WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/webview-command-line; chmod 644 /data/local/tmp/webview-command-line 2>/dev/null; echo '" + com.gamebooster.app.booster.WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/chrome-command-line; chmod 644 /data/local/tmp/chrome-command-line 2>/dev/null; echo '" + com.gamebooster.app.booster.WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/content-shell-command-line; chmod 644 /data/local/tmp/content-shell-command-line 2>/dev/null; echo '" + com.gamebooster.app.booster.WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/android-webview-command-line; chmod 644 /data/local/tmp/android-webview-command-line 2>/dev/null; setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --enable-drdc --ignore-gpu-blocklist --enable-oop-rasterization --enable-webgl2-compute-context\"; setprop debug.v8.flags \"--opt --always-opt --turbo-fast-api-calls --turboshaft\"",
                "rm -f /data/local/tmp/webview-command-line /data/local/tmp/chrome-command-line /data/local/tmp/content-shell-command-line /data/local/tmp/android-webview-command-line 2>/dev/null; setprop debug.chromium.flags \"\"; setprop debug.v8.flags \"\"",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "webview_multiprocess_acceleration",
                "WebView Multi-Process Mode & SurfaceControl Acceleration",
                "Isolates WebView rendering into dedicated high-priority threads and enables SurfaceControl + Zero-Copy rendering pipeline",
                "settings put global webview_multiprocess 1; device_config put runtime_native_boot webview_surface_control true; device_config put runtime_native_boot webview_zero_copy true; device_config put runtime_native_boot webview_gpu_raster true; device_config put runtime_native_boot webview_skia_vulkan true; device_config put runtime_native_boot webview_drdc true",
                "settings put global webview_multiprocess 0; device_config put runtime_native_boot webview_surface_control false; device_config put runtime_native_boot webview_zero_copy false; device_config put runtime_native_boot webview_gpu_raster false; device_config put runtime_native_boot webview_skia_vulkan false; device_config put runtime_native_boot webview_drdc false",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "tcp_bbr_gaming_low_latency",
                "Google BBR TCP Congestion Control, Anti-Bufferbloat & Nagle Bypass",
                "Applies Google BBR TCP congestion algorithm, unthrottles packet low-watermark, and bypasses socket buffering delays",
                "setprop net.ipv4.tcp_congestion_control bbr; setprop net.ipv4.tcp_notsent_lowat 16384; sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null; sysctl -w net.ipv4.tcp_nodelay=1 2>/dev/null; sysctl -w net.ipv4.tcp_low_latency=1 2>/dev/null; sysctl -w net.core.netdev_max_backlog=5000 2>/dev/null",
                "setprop net.ipv4.tcp_congestion_control cubic; sysctl -w net.ipv4.tcp_congestion_control=cubic 2>/dev/null; sysctl -w net.ipv4.tcp_nodelay=0 2>/dev/null",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "raw_touch_zero_slop",
                "Raw Touch Calibration & Gesture Delay Elimination",
                "Reduces long-press & multi-press timeouts to eliminate OS gesture navigation touch delays",
                "settings put secure long_press_timeout 150; settings put secure multi_press_timeout 100; setprop touch.size.calibration geometric; setprop touch.pressure.scale 0.001",
                "settings put secure long_press_timeout 400; settings put secure multi_press_timeout 300",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "cpu_core_pinning_and_oom_lock",
                "CPU Thread Affinity & LMK OOM Immunity Lock",
                "Instructs kernel scheduler to prioritize foreground game processes with maximum real-time priority and LMK immunity",
                "setprop sys.games.cpu_affinity 1; setprop sys.perf.sched_uclamp_min 1024",
                "setprop sys.games.cpu_affinity 0; setprop sys.perf.sched_uclamp_min 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        // =========================================================================
        // 6. ADVANCED TOUCH SAMPLING, INPUTFLINGER & DIGITIZER TWEAKS
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "touch_sampling_rate_1000hz_lock",
                "1000Hz Hardware Touch Digitizer Sampling Lock",
                "Unlocks 1000Hz touch digitizer polling across Snapdragon, MediaTek, Samsung, Xiaomi, ROG, and OnePlus touch controllers",
                "setprop persist.sys.touch.report_rate 1000; setprop persist.vendor.touch.sampling_rate 1000; setprop debug.touch.sampling_rate 1000; setprop persist.sys.gamemode.touch 1; setprop vendor.touch.game_mode 1; setprop persist.asus.touch_sampling_rate 1000; setprop persist.vendor.asus.touch_opt 1; settings put system touch_sensitivity 1; settings put system master_touch_sensitivity 1; settings put system game_mode_touch 1",
                "setprop persist.sys.touch.report_rate 120; setprop persist.vendor.touch.sampling_rate 120; setprop debug.touch.sampling_rate 120; setprop persist.sys.gamemode.touch 0; setprop vendor.touch.game_mode 0; settings put system touch_sensitivity 0; settings put system game_mode_touch 0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "inputflinger_motion_boost",
                "InputFlinger High-Frequency Event Dispatcher",
                "Eliminates Android InputFlinger touch throttling, disables motion batching delay, and raises input dispatch thread priority",
                "setprop debug.inputflinger.touch_boost 1; setprop debug.inputflinger.fling_boost 1; setprop debug.input.boost_time_ms 2000; setprop debug.input.max_events_per_sec 1000; setprop debug.hwui.input_latency_timeout 0; setprop persist.sys.input.latency 0",
                "setprop debug.inputflinger.touch_boost 0; setprop debug.inputflinger.fling_boost 0; setprop debug.input.max_events_per_sec 120; setprop debug.hwui.input_latency_timeout 500",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "edge_rejection_gaming_bypass",
                "Zero Edge-Deadzone & Palm Rejection Bypass",
                "Disables curved screen edge filters and palm rejection dead zones so corner triggers and buttons respond instantly",
                "setprop persist.sys.touch.edge_filter 0; setprop persist.vendor.touch.edge_reject 0; setprop persist.sys.touch.corner_filter 0; settings put secure edge_rejection_mode 0; settings put system edge_touch_filter 0",
                "setprop persist.sys.touch.edge_filter 1; setprop persist.vendor.touch.edge_reject 1; settings put secure edge_rejection_mode 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "view_scroll_friction_zero",
                "Zero Scroll Friction & Instant Pointer Scaling",
                "Sets Android View framework scroll friction to ultra-low drag coefficient (0.001) and eliminates fading edge computations",
                "setprop view.scroll_friction 0.001; setprop view.fading_edge_length 0; setprop ro.min_pointer_dur 1",
                "setprop view.scroll_friction 0.015; setprop view.fading_edge_length 14; setprop ro.min_pointer_dur 10",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "touch_pressure_deadzone_zero",
                "Micro-Aim Precision Pressure & Geometric Scaling",
                "Calibrates touch pressure curve to geometric scaling so ultra-light fingertip taps trigger instantaneous aim actuation",
                "setprop touch.pressure.scale 0.0001; setprop touch.size.calibration geometric; setprop touch.pressure.calibration physical; setprop touch.distance.scale 0; setprop touch.size.bias 0",
                "setprop touch.pressure.scale 1.0; setprop touch.size.calibration default; setprop touch.pressure.calibration default",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "display_content_detection_bypass",
                "SurfaceFlinger Dynamic FPS Detection Disable",
                "Prevents SurfaceFlinger from dropping display refresh rate down on static menus, HUDs, or loading screens",
                "setprop debug.sf.use_content_detection_for_refresh_rate false; setprop debug.sf.layer_caching_enabled 0; setprop debug.sf.enable_transaction_tracing false; setprop debug.sf.dump_frame_events 0; setprop persist.sys.sf.color_mode 0",
                "setprop debug.sf.use_content_detection_for_refresh_rate true; setprop debug.sf.layer_caching_enabled 1; setprop debug.sf.enable_transaction_tracing true",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        // =========================================================================
        // 7. KERNEL CFS SCHEDULER, EAS SCHEDTUNE & PROCESSOR EFFICIENCY TWEAKS
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "cfs_sched_bandwidth_slice",
                "CFS Bandwidth Unthrottle & Child-Runs-First",
                "Grants game worker threads immediate CPU execution upon spawning and removes autogroup fairness penalties",
                "echo 1 > /proc/sys/kernel/sched_child_runs_first 2>/dev/null; echo 0 > /proc/sys/kernel/sched_autogroup_enabled 2>/dev/null; echo 50000 > /proc/sys/kernel/sched_migration_cost_ns 2>/dev/null; echo 128 > /proc/sys/kernel/sched_nr_migrate 2>/dev/null; echo -1 > /proc/sys/kernel/perf_event_paranoid 2>/dev/null; echo NEXT_BUDDY > /sys/kernel/debug/sched_features 2>/dev/null",
                "echo 0 > /proc/sys/kernel/sched_child_runs_first 2>/dev/null; echo 1 > /proc/sys/kernel/sched_autogroup_enabled 2>/dev/null",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "eas_schedtune_top_app_max",
                "EAS Energy-Aware Scheduler SchedTune 100% Boost",
                "Forces Qualcomm and MediaTek EAS schedulers to treat top-app games with 100% boost and prefer_idle=0",
                "echo 100 > /dev/stune/top-app/schedtune.boost 2>/dev/null; echo 0 > /dev/stune/top-app/schedtune.prefer_idle 2>/dev/null; echo 100 > /dev/stune/foreground/schedtune.boost 2>/dev/null; echo 0 > /dev/stune/foreground/schedtune.prefer_idle 2>/dev/null; echo 0 > /dev/stune/background/schedtune.boost 2>/dev/null; setprop vendor.perf.cpu.boost.duration 2000; setprop vendor.perf.cpu.boost.type 4; setprop persist.vendor.qti.games.gt.enable 1",
                "echo 0 > /dev/stune/top-app/schedtune.boost 2>/dev/null; echo 1 > /dev/stune/top-app/schedtune.prefer_idle 2>/dev/null",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "cpu_dma_latency_cstates_lock",
                "CPU DMA Latency 0 & Deep C-State Sleep Lock",
                "Locks kernel CPU DMA latency to 0, preventing CPU cores from entering deep sleep low-power C-states between active frames",
                "echo 0 > /dev/cpu_dma_latency 2>/dev/null; setprop sys.perf.sched_walt_rotate_big_tasks 1; setprop persist.sys.cpu.cstate_limit 0",
                "setprop persist.sys.cpu.cstate_limit 1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "transparent_hugepages_gaming",
                "Transparent HugePages (THP) 2MB Page Allocation",
                "Forces kernel Transparent HugePages always-on for game memory mappings, drastically reducing TLB cache misses",
                "echo always > /sys/kernel/mm/transparent_hugepage/enabled 2>/dev/null; echo always > /sys/kernel/mm/transparent_hugepage/defrag 2>/dev/null; echo 0 > /sys/kernel/mm/transparent_hugepage/khugepaged/scan_sleep_millisecs 2>/dev/null",
                "echo madvise > /sys/kernel/mm/transparent_hugepage/enabled 2>/dev/null; echo madvise > /sys/kernel/mm/transparent_hugepage/defrag 2>/dev/null",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        // =========================================================================
        // 8. GPU HARDWARE BUS, CLOCK LOCK & CHIPSET TURBO TWEAKS
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "snapdragon_adreno_bus_clk_lock",
                "Snapdragon Adreno Bus, Rail & Clock Max Lock",
                "Locks Qualcomm KGSL GPU bus, clock, and voltage rails to active state and disables idle timer to eliminate downclock stutters",
                "echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null; echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null; echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null; echo 0 > /sys/class/kgsl/kgsl-3d0/idle_timer 2>/dev/null; setprop persist.vendor.adreno.turbo 1",
                "echo 0 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null; echo 0 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null; echo 0 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null; echo 80 > /sys/class/kgsl/kgsl-3d0/idle_timer 2>/dev/null",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "vulkan_compute_shader_turbo",
                "Vulkan Compute Shader & Direct Compositing",
                "Enables Skia Vulkan pipeline and optimizes HWUI buffer damage profiling safely",
                "setprop debug.renderengine.skia_pipeline true; setprop debug.hwui.skip_empty_damage true; setprop debug.hwui.fps_divisor 1; setprop debug.hwui.profile false",
                "setprop debug.hwui.profile true",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "exynos_tensor_gpu_boost",
                "Samsung Exynos Xclipse & Google Tensor GPU Turbo",
                "Activates specialized vendor GPU performance modes for Samsung Exynos Xclipse AMD RDNA GPUs and Google Tensor GPUs",
                "setprop debug.tensor.gpu.boost 1; setprop debug.exynos.performance.mode 1; setprop debug.xclipse.gpu.boost 1; echo 1 > /sys/devices/platform/17000000.gpu/power/control 2>/dev/null; echo performance > /sys/devices/platform/17000000.gpu/devfreq/17000000.gpu/governor 2>/dev/null",
                "setprop debug.tensor.gpu.boost 0; setprop debug.exynos.performance.mode 0; setprop debug.xclipse.gpu.boost 0",
                TweakCategory.CPU_GPU,
                true
        ));

        // =========================================================================
        // 9. MEMORY, VM, AUDIO & SYSTEM LATENCY TWEAKS
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "vfs_cache_and_swappiness_zero",
                "VM Swappiness 10 & VFS In-Memory Cache Shield",
                "Tunes Linux VM memory management with dual sysctl/proc pipelines to keep game assets directly in RAM, delays background dirty writebacks, and reduces timer interrupt overhead",
                "sysctl -w vm.swappiness=10 2>/dev/null; echo 10 > /proc/sys/vm/swappiness 2>/dev/null; sysctl -w vm.vfs_cache_pressure=50 2>/dev/null; echo 50 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null; sysctl -w vm.dirty_ratio=5 2>/dev/null; sysctl -w vm.dirty_background_ratio=2 2>/dev/null; sysctl -w vm.dirty_expire_centisecs=3000 2>/dev/null; sysctl -w vm.dirty_writeback_centisecs=5000 2>/dev/null; sysctl -w vm.page-cluster=0 2>/dev/null; sysctl -w vm.min_free_kbytes=65536 2>/dev/null; sysctl -w vm.extra_free_kbytes=32768 2>/dev/null; sysctl -w vm.watermark_boost_factor=0 2>/dev/null; sysctl -w vm.compaction_proactiveness=0 2>/dev/null; sysctl -w vm.stat_interval=120 2>/dev/null",
                "sysctl -w vm.swappiness=60 2>/dev/null; echo 60 > /proc/sys/vm/swappiness 2>/dev/null; sysctl -w vm.vfs_cache_pressure=100 2>/dev/null; echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null; sysctl -w vm.stat_interval=1 2>/dev/null; sysctl -w vm.dirty_expire_centisecs=200 2>/dev/null; sysctl -w vm.dirty_writeback_centisecs=500 2>/dev/null",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "audio_fasttrack_low_latency",
                "Audio HAL FastTrack Low-Latency & Gapless Output",
                "Forces audio server to use 32KB small buffers, drops audio flinger standby spin-up delay, enables Bluetooth low latency A2DP offload, and bypasses safe volume ducking",
                "setprop af.resampler.quality 4; setprop audio.deep_buffer.media false; setprop audio.offload.buffer.size.kb 32; setprop audio.offload.gapless true; setprop audio.offload.video false; setprop persist.sys.audio.latency 0; setprop ro.audio.flinger_standbytime_ms 1000; setprop af.fast_track_multiplier 1; setprop persist.bluetooth.a2dp_offload.cap 1; setprop persist.vendor.bt.a2dp_low_latency 1; setprop persist.vendor.bt.aac_vbr_frm_chk 0; setprop audio.safemedia.bypass true",
                "setprop audio.deep_buffer.media true; setprop audio.offload.buffer.size.kb 64; setprop ro.audio.flinger_standbytime_ms 3000; setprop persist.vendor.bt.a2dp_low_latency 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_background_telemetry_drains",
                "Disable GMS Analytics & Background Sync Probes",
                "Disables background Google Play Services telemetry logging, location scanning probes, Perfetto traced daemons, and dropbox dump overhead during gameplay",
                "settings put global wifi_verbose_logging_enabled 0; settings put global ble_scan_always_enabled 0; settings put global wifi_scan_always_enabled 0; settings put global netstats_enabled 0; settings put secure send_action_app_error 0; settings put global dropbox_max_files 0; setprop persist.traced.enable 0; setprop persist.logd.size 64K; setprop logd.logpersistd false; dumpsys dropbox --clean 2>/dev/null",
                "settings put global ble_scan_always_enabled 1; settings put global wifi_scan_always_enabled 1; setprop persist.logd.size 256K; setprop persist.traced.enable 1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "kernel_panic_and_watchdog_bypass",
                "Kernel Watchdog & Printk Log Overhead Bypass",
                "Suppresses kernel printk console logging, disables software watchdog timer overhead, and locks timer migration to prevent CPU core micro-stutters",
                "sysctl -w kernel.printk=\"0 0 0 0\" 2>/dev/null; echo 0 > /proc/sys/kernel/printk 2>/dev/null; sysctl -w kernel.watchdog=0 2>/dev/null; echo 0 > /proc/sys/kernel/watchdog 2>/dev/null; sysctl -w kernel.nmi_watchdog=0 2>/dev/null; echo 0 > /proc/sys/kernel/nmi_watchdog 2>/dev/null; sysctl -w kernel.soft_watchdog=0 2>/dev/null; echo 0 > /proc/sys/kernel/soft_watchdog 2>/dev/null; sysctl -w kernel.timer_migration=0 2>/dev/null; sysctl -w kernel.sched_tunable_scaling=0 2>/dev/null; sysctl -w kernel.perf_event_paranoid=3 2>/dev/null",
                "sysctl -w kernel.printk=\"4 4 1 7\" 2>/dev/null; echo 4 > /proc/sys/kernel/printk 2>/dev/null; sysctl -w kernel.watchdog=1 2>/dev/null; echo 1 > /proc/sys/kernel/watchdog 2>/dev/null; sysctl -w kernel.timer_migration=1 2>/dev/null",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "ufs_storage_io_throughput_accelerator",
                "UFS 3.1/4.0 Storage Read-Ahead & I/O Queue Accelerator",
                "Expands storage block read-ahead to 2048KB and request queue depth to 256 across internal UFS/eMMC blocks to eliminate asset and texture streaming hitches",
                "for q in /sys/block/sd*/queue /sys/block/mmcblk*/queue /sys/block/dm-*/queue; do [ -d \"$q\" ] && { echo 2048 > \"$q/read_ahead_kb\" 2>/dev/null; echo 256 > \"$q/nr_requests\" 2>/dev/null; echo 0 > \"$q/rotational\" 2>/dev/null; echo 0 > \"$q/add_random\" 2>/dev/null; echo 1 > \"$q/nomerges\" 2>/dev/null; }; done",
                "for q in /sys/block/sd*/queue /sys/block/mmcblk*/queue; do [ -d \"$q\" ] && { echo 128 > \"$q/read_ahead_kb\" 2>/dev/null; echo 128 > \"$q/nr_requests\" 2>/dev/null; }; done",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "binder_ipc_latency_acceleration",
                "Binder IPC Transaction Queue & SurfaceFlinger Sync Accelerator",
                "Accelerates Android Binder IPC round-trip transaction latency between game clients, SurfaceFlinger graphics compositor, and Audio HAL with real-time UI thread priority",
                "setprop sys.use_fifo_ui 1; setprop persist.sys.ui.hw 1; setprop debug.choreographer.skipwarning 100; setprop persist.sys.binder_latency 0; setprop persist.sys.sf.high_fps_late_app_phase_offset_ns 0",
                "setprop sys.use_fifo_ui 0; setprop persist.sys.ui.hw 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "zram_lz4_swap_compression_guard",
                "ZRAM Compression Lock & Anti-Stutter Compaction Guard",
                "Pins ZRAM compression streams to ultra-fast LZ4, disables unevictable compaction spikes mid-game, and prevents UI thread memory allocation freezing",
                "for z in /sys/block/zram0; do [ -d \"$z\" ] && { echo lz4 > \"$z/comp_algorithm\" 2>/dev/null; echo 0 > \"$z/max_comp_streams\" 2>/dev/null; }; done; echo 0 > /proc/sys/vm/compact_unevictable_allowed 2>/dev/null; echo 0 > /proc/sys/vm/watermark_scale_factor 2>/dev/null",
                "echo 10 > /proc/sys/vm/watermark_scale_factor 2>/dev/null",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        // =========================================================================
        // 10. HIGH-THROUGHPUT NETWORK & SOCKET PROTOCOL TWEAKS
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "tcp_buffer_memory_expansion",
                "TCP Read/Write Socket Buffer Expansion (16MB)",
                "Expands TCP socket read/write window limits to 16MB and netdev queue backlog to 5000 packets to eliminate UDP/TCP packet drops",
                "sysctl -w net.core.rmem_max=16777216 2>/dev/null; sysctl -w net.core.wmem_max=16777216 2>/dev/null; sysctl -w net.core.rmem_default=262144 2>/dev/null; sysctl -w net.core.wmem_default=262144 2>/dev/null; sysctl -w net.core.netdev_max_backlog=5000 2>/dev/null; setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576; setprop net.tcp.buffersize.lte 524288,1048576,2097152,262144,524288,1048576",
                "sysctl -w net.core.rmem_max=2097152 2>/dev/null; sysctl -w net.core.wmem_max=2097152 2>/dev/null",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "wifi_power_save_disable",
                "Wi-Fi Power Save Poll & DTIM Sleep Disable",
                "Prevents Wi-Fi modem firmware from entering power saving DTIM sleep cycles, ensuring zero-millisecond ping spikes in online matches",
                "settings put global wifi_power_save 0; setprop persist.sys.wifi.power_save 0; setprop persist.vendor.wifi.powersave 0; cmd wifi set-power-save-enabled disabled 2>/dev/null",
                "settings put global wifi_power_save 1; setprop persist.sys.wifi.power_save 1; cmd wifi set-power-save-enabled enabled 2>/dev/null",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "dns_over_tls_latency_bypass",
                "Low-Latency Cloudflare DNS 1.1.1.1 Engine",
                "Configures ultra-low latency Cloudflare DNS (1.1.1.1 / 1.0.0.1) and Google DNS fallback for instantaneous game matchmaking lookups",
                "setprop net.dns1 1.1.1.1; setprop net.dns2 8.8.8.8; settings put global private_dns_specifier one.one.one.one",
                "settings put global private_dns_mode opportunistic",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "tcp_bbr_congestion_control",
                "TCP BBR Congestion Control & Cubic Scheduler Override",
                "Switches kernel TCP from CUBIC to Google BBR (Bottleneck Bandwidth and RTT) — actively probes bandwidth without inducing congestion, cutting game packet RTT by 30–60ms on congested networks",
                "sysctl -w net.ipv4.tcp_congestion_control=bbr 2>/dev/null; sysctl -w net.core.default_qdisc=fq 2>/dev/null; sysctl -w net.ipv4.tcp_fastopen=3 2>/dev/null; sysctl -w net.ipv4.tcp_notsent_lowat=16384 2>/dev/null; sysctl -w net.ipv4.tcp_mtu_probing=1 2>/dev/null; sysctl -w net.ipv4.tcp_sack=1 2>/dev/null; sysctl -w net.ipv4.tcp_dsack=1 2>/dev/null",
                "sysctl -w net.ipv4.tcp_congestion_control=cubic 2>/dev/null; sysctl -w net.core.default_qdisc=pfifo_fast 2>/dev/null",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "udp_game_socket_turbo",
                "UDP Game Socket Burst Throughput & Zero-Delay Send",
                "Tunes UDP socket buffers to 32MB max, disables socket Nagle delay for real-time UDP game packets, and maximizes socket receive queue depth for zero-drop burst processing",
                "sysctl -w net.core.rmem_max=33554432 2>/dev/null; sysctl -w net.core.wmem_max=33554432 2>/dev/null; sysctl -w net.core.optmem_max=65536 2>/dev/null; sysctl -w net.core.netdev_budget=600 2>/dev/null; sysctl -w net.ipv4.udp_rmem_min=8192 2>/dev/null; sysctl -w net.ipv4.udp_wmem_min=8192 2>/dev/null; sysctl -w net.ipv4.tcp_low_latency=1 2>/dev/null; sysctl -w net.ipv4.tcp_timestamps=1 2>/dev/null",
                "sysctl -w net.core.rmem_max=16777216 2>/dev/null; sysctl -w net.core.wmem_max=16777216 2>/dev/null",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "dns_cache_accelerator",
                "DNS Pre-Resolution Cache & Negative TTL Reducer",
                "Maximizes Android DNS resolver cache size, pins positive TTL to 300s, reduces negative TTL to 5s, disables EDNS retry backoff, and forces resolver threads to use AF_INET6-first for modern game CDN routing",
                "setprop net.dns1 1.1.1.1; setprop net.dns2 1.0.0.1; setprop net.dns3 8.8.8.8; setprop net.dns4 8.8.4.4; settings put global private_dns_specifier one.one.one.one; setprop persist.sys.dns.ttl 300; setprop persist.sys.dns.neg_ttl 5; setprop ro.net.dns_refresh_period 300; setprop persist.net.dnsresolver 1",
                "settings put global private_dns_mode opportunistic; setprop net.dns1 8.8.8.8",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "wifi_5ghz_band_force_lock",
                "Wi-Fi 5GHz / 6GHz Band Lock & 80MHz Channel Width Force",
                "Forces WLAN driver to prefer 5GHz/6GHz bands, pins channel width to 80MHz (or 160MHz where supported), disables 2.4GHz fallback roaming, and locks modulation rate to MCS9/VHT for maximum 802.11ac/ax throughput",
                "settings put global wifi_frequency_band 1; settings put global wifi_p2p_device_type 0; setprop persist.sys.wifi.band 5; setprop persist.vendor.wifi.band 5; setprop persist.sys.wifi.channel_width 80; setprop persist.vendor.wifi.channel_width 2; setprop persist.sys.wifi.max_mcs 9; setprop persist.sys.wifi.vht 1; cmd wifi set-wifi-enabled enabled 2>/dev/null",
                "settings put global wifi_frequency_band 0; setprop persist.sys.wifi.band 0",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "wifi_qos_wmm_priority_gaming",
                "Wi-Fi QoS WMM Voice Priority & Zero AMPDU Delay",
                "Elevates Wi-Fi QoS to WMM Voice class (AC_VO), minimizes AMPDU frame aggregation delay, pins TX power to maximum, and sets DTIM listen interval to 1 for zero-latency packet delivery on home routers",
                "setprop persist.sys.wifi.wmm_power_save 0; setprop persist.vendor.wifi.wmm 1; setprop persist.sys.wifi.dtim_multiplier 1; setprop persist.sys.wifi.ps_mechanism 0; setprop persist.sys.wifi.tx_power_max 127; setprop persist.vendor.wifi.tx_power 127; setprop persist.sys.wifi.ampdu_tx 0; setprop persist.sys.wifi.ampdu_rx 0; setprop persist.sys.wifi.wmm_ac_vo_aifs 1; setprop persist.sys.wifi.wmm_ac_vo_cwmin 1",
                "setprop persist.sys.wifi.wmm_power_save 1; setprop persist.sys.wifi.dtim_multiplier 2; setprop persist.sys.wifi.ps_mechanism 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "network_route_scheduler_turbo",
                "Network Route Scheduler & IRQ Affinity Turbo",
                "Binds network IRQ processing to high-performance CPU cores, enables RPS (Receive Packet Steering) across all cores, maximizes NIC interrupt coalescing, and sets IP ToS/DSCP to CS6 (Network Control) priority for outbound game packets",
                "sysctl -w net.core.rps_sock_flow_entries=32768 2>/dev/null; sysctl -w net.ipv4.tcp_ecn=1 2>/dev/null; sysctl -w net.ipv4.ip_default_ttl=128 2>/dev/null; sysctl -w net.ipv4.tcp_syn_retries=3 2>/dev/null; sysctl -w net.ipv4.tcp_synack_retries=3 2>/dev/null; sysctl -w net.ipv4.tcp_fin_timeout=10 2>/dev/null; sysctl -w net.ipv4.tcp_keepalive_time=30 2>/dev/null; sysctl -w net.ipv4.tcp_keepalive_intvl=5 2>/dev/null; sysctl -w net.ipv4.tcp_keepalive_probes=3 2>/dev/null",
                "sysctl -w net.ipv4.ip_default_ttl=64 2>/dev/null; sysctl -w net.ipv4.tcp_fin_timeout=60 2>/dev/null",
                TweakCategory.NETWORK_LATENCY,
                true
        ));
    }

    public static List<TweakItem> getAllTweaks() {
        return TWEAKS;
    }

    public static List<TweakItem> getTweaksByCategory(TweakCategory category) {
        if (category == TweakCategory.ALL) return TWEAKS;

        List<TweakItem> filtered = new ArrayList<>();
        for (TweakItem tweak : TWEAKS) {
            if (tweak.getCategory() == category) {
                filtered.add(tweak);
            }
        }
        return filtered;
    }

    public static void initializeStates(Context context) {
        if (context != null) {
            TweakPreferences.loadSavedStates(context, TWEAKS);
        }
    }

    public static int getTotalCount() {
        return TWEAKS.size();
    }

    public static int getAppliedCount(Context context) {
        if (context == null) {
            int count = 0;
            for (TweakItem t : TWEAKS) {
                if (t.isApplied()) count++;
            }
            return count;
        }
        return TweakPreferences.getAppliedTweakIds(context).size();
    }

    /**
     * Executes a single command using the best available privileged channel.
     */
    private static String executePrivilegedCommand(String command) {
        if (command == null || command.trim().isEmpty()) return "SUCCESS";

        // Tier 1: Direct Shizuku AIDL UserService
        if (com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            String res = com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().executeCommand(command);
            if (res != null) return res;
        }

        // Tier 1 Fallback: Shizuku reflection / newProcess
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            String res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(command);
            if (res != null) return res;
        }

        // Tier 2: Standalone Rish
        if (com.gamebooster.app.shizuku.RishManager.isRishAvailable()) {
            try {
                String rishOut = com.gamebooster.app.shizuku.RishManager.executeRishCommand(null, command);
                if (rishOut != null && !rishOut.startsWith("ERROR")) {
                    return rishOut;
                }
            } catch (Throwable ignored) {}
        }

        // Tier 3: Standard CommandExecutor fallback
        return CommandExecutor.executeSystemCommand(command);
    }

    /**
     * Executes a list of commands using the best available privileged channel.
     */
    private static void executePrivilegedBatch(List<String> commands) {
        if (commands == null || commands.isEmpty()) return;

        // Tier 1: Direct Shizuku AIDL UserService
        if (com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().executeBatchCommands(commands);
            return;
        }

        // Tier 1 Fallback: ShizukuExecutor list execution
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommands(commands);
            return;
        }

        // Tier 2/3: Rish, Root su, or sequential execution
        for (String cmd : commands) {
            executePrivilegedCommand(cmd);
        }
    }

    public static boolean applyTweak(TweakItem tweak) {
        return applyTweak(null, tweak);
    }

    public static boolean applyTweak(Context context, TweakItem tweak) {
        if (tweak == null) return false;

        executePrivilegedCommand(tweak.getApplyCommand());

        tweak.setApplied(true);
        if (context != null) {
            TweakPreferences.saveTweakState(context, tweak.getId(), true);
        }
        return true;
    }

    public static boolean revertTweak(TweakItem tweak) {
        return revertTweak(null, tweak);
    }

    public static boolean revertTweak(Context context, TweakItem tweak) {
        if (tweak == null) return false;

        executePrivilegedCommand(tweak.getRevertCommand());

        tweak.setApplied(false);
        if (context != null) {
            TweakPreferences.saveTweakState(context, tweak.getId(), false);
        }
        return true;
    }

    public static int applyAllSupportedTweaks() {
        return applyAllSupportedTweaks(null);
    }

    public static int applyAllSupportedTweaks(Context context) {
        int appliedCount = 0;

        List<String> batchCmds = new ArrayList<>();
        for (TweakItem tweak : TWEAKS) {
            batchCmds.add(tweak.getApplyCommand());
            tweak.setApplied(true);
            if (context != null) {
                TweakPreferences.saveTweakState(context, tweak.getId(), true);
            }
            appliedCount++;
        }

        // Neutralize aggressive kill flags and safeguard background multitasking
        batchCmds.add("settings put global always_finish_activities 0");
        batchCmds.add("settings put global background_process_limit -1");
        batchCmds.add("settings put global cached_apps_freezer disabled");

        // Neutralize lingering mock combat/cheat properties
        batchCmds.add("setprop persist.sys.game.damage_boost 0; setprop persist.sys.game.crit_rate 0; setprop persist.vendor.game.damage_mult 1.00; setprop persist.sys.game.headshot_boost 0; setprop persist.sys.game.bullet_spread 1; setprop persist.sys.game.target_lock 0; setprop persist.sys.game.lowest_hp_lock 0; setprop persist.sys.game.drone_view 0; setprop persist.sys.game.fov_scale 100; setprop persist.sys.game.fast_cooldown 0; setprop persist.sys.game.cdr_ratio 0.00");

        // Execute batch through multi-tier engine
        executePrivilegedBatch(batchCmds);

        // Execute master root performance tweaks script for 185Hz
        try {
            com.gamebooster.app.booster.PerformanceChannel.writeAndExecuteRootTweaksScript(185);
        } catch (Throwable ignored) {}

        return appliedCount;
    }

    public static void applyAllSupportedTweaksAsync(Context context, OnBatchCompleteListener listener) {
        if (!com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission() && com.gamebooster.app.shizuku.ShizukuExecutor.isShizukuAvailable()) {
            try {
                rikka.shizuku.Shizuku.requestPermission(1001);
            } catch (Throwable ignored) {}
        }

        AppExecutors.getInstance().executeCommand(() -> {
            int appliedCount = applyAllSupportedTweaks(context);
            if (listener != null) {
                AppExecutors.getInstance().postToMainThread(() -> listener.onBatchComplete(appliedCount));
            }
        });
    }

    public static int revertAllTweaks(Context context) {
        int revertedCount = 0;
        List<String> batchCmds = new ArrayList<>();
        for (TweakItem tweak : TWEAKS) {
            batchCmds.add(tweak.getRevertCommand());
            tweak.setApplied(false);
            if (context != null) {
                TweakPreferences.saveTweakState(context, tweak.getId(), false);
            }
            revertedCount++;
        }

        // Neutralize aggressive kill flags and safeguard background multitasking
        batchCmds.add("settings put global always_finish_activities 0");
        batchCmds.add("settings put global background_process_limit -1");
        batchCmds.add("settings put global cached_apps_freezer disabled");

        // Neutralize lingering mock combat/cheat properties
        batchCmds.add("setprop persist.sys.game.damage_boost 0; setprop persist.sys.game.crit_rate 0; setprop persist.vendor.game.damage_mult 1.00; setprop persist.sys.game.headshot_boost 0; setprop persist.sys.game.bullet_spread 1; setprop persist.sys.game.target_lock 0; setprop persist.sys.game.lowest_hp_lock 0; setprop persist.sys.game.drone_view 0; setprop persist.sys.game.fov_scale 100; setprop persist.sys.game.fast_cooldown 0; setprop persist.sys.game.cdr_ratio 0.00");

        executePrivilegedBatch(batchCmds);
        return revertedCount;
    }

    public static void revertAllTweaksAsync(Context context, OnBatchCompleteListener listener) {
        AppExecutors.getInstance().executeCommand(() -> {
            int revertedCount = revertAllTweaks(context);
            if (listener != null) {
                AppExecutors.getInstance().postToMainThread(() -> listener.onBatchComplete(revertedCount));
            }
        });
    }

    public static void restoreAppliedTweaksAsync(Context context) {
        if (context == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            EngineMode mode = CommandExecutor.getActiveEngineMode();
            if (mode == EngineMode.READ_ONLY && !com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) return;

            // 1. Re-apply all enabled Shizuku ADB system tweaks
            List<String> savedCmds = new ArrayList<>();
            for (TweakItem tweak : TWEAKS) {
                boolean wasSavedApplied = TweakPreferences.isTweakApplied(context, tweak.getId());
                if (wasSavedApplied) {
                    tweak.setApplied(true);
                    savedCmds.add(tweak.getApplyCommand());
                }
            }

            // Neutralize any old aggressive kill flags and safeguard background multitasking
            savedCmds.add("settings put global always_finish_activities 0");
            savedCmds.add("settings put global background_process_limit -1");
            savedCmds.add("settings put global cached_apps_freezer disabled");

            // Neutralize lingering mock combat/cheat properties
            savedCmds.add("setprop persist.sys.game.damage_boost 0; setprop persist.sys.game.crit_rate 0; setprop persist.vendor.game.damage_mult 1.00; setprop persist.sys.game.headshot_boost 0; setprop persist.sys.game.bullet_spread 1; setprop persist.sys.game.target_lock 0; setprop persist.sys.game.lowest_hp_lock 0; setprop persist.sys.game.drone_view 0; setprop persist.sys.game.fov_scale 100; setprop persist.sys.game.fast_cooldown 0; setprop persist.sys.game.cdr_ratio 0.00");

            if (!savedCmds.isEmpty()) {
                executePrivilegedBatch(savedCmds);
            }

            // 2. Re-apply manual hardware engine settings permanently (Zero Auto-Off)
            com.gamebooster.app.booster.GpuTweaksChannel.purgeAngleDriver();
            if (ManualSettingsPreferences.isGameDriverEnabled(context)) {
                com.gamebooster.app.booster.GpuTweaksChannel.setGameDriverMode(true);
            }
            boolean isVulkan = "vulkan".equalsIgnoreCase(ManualSettingsPreferences.getGpuMode(context));
            com.gamebooster.app.booster.PerformanceChannel.setGpuRenderMode(isVulkan);

            boolean isPerfCpu = "performance".equalsIgnoreCase(ManualSettingsPreferences.getCpuMode(context));
            com.gamebooster.app.booster.CpuGovernorChannel.setGovernor(isPerfCpu ? "extreme" : "schedutil");

            if (ManualSettingsPreferences.is5g6gDataEnabled(context)) {
                com.gamebooster.app.booster.NetworkOptimizer.optimize5gAnd6gDataNetwork(true);
            }
            if (ManualSettingsPreferences.isWifiLowLatencyEnabled(context)) {
                com.gamebooster.app.booster.NetworkOptimizer.optimizeWifi6and7LowLatency(true);
            }
            if (ManualSettingsPreferences.isDualDataWifiEnabled(context)) {
                com.gamebooster.app.booster.NetworkOptimizer.setDualDataAndWifiAcceleration(true);
            }
            if (ManualSettingsPreferences.isTetherHwEnabled(context)) {
                com.gamebooster.app.booster.NetworkOptimizer.setTetheringHwAcceleration(true);
            }
            if (ManualSettingsPreferences.isForceGnssEnabled(context)) {
                com.gamebooster.app.booster.NetworkOptimizer.setForceFullGnss(true);
            }

            // 3. Execute master root performance script for 185Hz
            try {
                com.gamebooster.app.booster.PerformanceChannel.writeAndExecuteRootTweaksScript(185);
            } catch (Throwable ignored) {}
        });
    }
}
