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
        // CPU & GPU Tweaks
        TWEAKS.add(new TweakItem(
                "gpu_hw_composition",
                "Vulkan HWUI Renderer",
                "Forces Vulkan hardware graphics pipeline to boost rendering throughput",
                "setprop debug.hwui.renderer vulkan",
                "setprop debug.hwui.renderer skia",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "skia_vulkan_pipeline",
                "Skia Vulkan Graphics Pipeline & Render Thread Boost",
                "Enables Skia Vulkan pipeline with real-time render thread priority (-20)",
                "setprop debug.renderengine.skia_pipeline true; setprop debug.renderengine.backend vulkan; setprop debug.hwui.render_thread_priority -20; setprop debug.hwui.use_gpu_pixel_buffers true",
                "setprop debug.renderengine.skia_pipeline false; setprop debug.hwui.render_thread_priority 0",
                TweakCategory.CPU_GPU,
                true
        ));

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
                "fifo_audio_render",
                "FIFO Realtime Scheduling Queue",
                "Forces realtime FIFO scheduling queue for ultra-low audio & touch latency",
                "setprop sys.use_fifo 1",
                "setprop sys.use_fifo 0",
                TweakCategory.CPU_GPU,
                true
        ));

        // Touch & Display Tweaks
        TWEAKS.add(new TweakItem(
                "touch_1000hz_polling",
                "1000Hz Ultra Touch Polling & Zero Slop",
                "Forces 1000Hz touch input sampling, 0ms touch slop, and instant FIFO event dispatching",
                "setprop debug.input.max_events_per_sec 1000; setprop view.touch_slop 0; settings put system touch_slop_reduction 1; setprop persist.sys.touch.report_rate 1000; setprop persist.vendor.touch.sampling_rate 1000; settings put system touch_prediction_time 0",
                "setprop debug.input.max_events_per_sec 0; setprop view.touch_slop 8; settings put system touch_slop_reduction 0; setprop persist.sys.touch.report_rate 120; setprop persist.vendor.touch.sampling_rate 120",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gyro_1000hz_response",
                "1000Hz Ultra Gyroscope & Motion Latency Bypass",
                "Unlocks 1000Hz gyro sensor polling and sets motion sensor delay to 0ms for flawless recoil control",
                "setprop debug.sensor.gyro.sample_rate 1000; setprop persist.sys.gyro.delay 0; setprop debug.sensor.motion.rate 1000",
                "setprop debug.sensor.gyro.sample_rate 0; setprop persist.sys.gyro.delay 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));
        TWEAKS.add(new TweakItem(
                "scrolling_cache_boost",
                "Zero Scroll Cache Latency",
                "Disables scrolling cache compression to save CPU render cycles",
                "setprop persist.sys.scrollingcache 3",
                "setprop persist.sys.scrollingcache 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "aim_touch_precision",
                "Aim Precision Touch Latency Stabilizer",
                "Reduces input lag, touch slop, pressure threshold and FIFO scheduling for zero-delay crosshair control",
                "setprop view.touch_slop 1; settings put system touch_slop_reduction 1; setprop sys.use_fifo 1; setprop persist.sys.touch.pressure.scale 0.001",
                "setprop view.touch_slop 8; settings put system touch_slop_reduction 0; setprop sys.use_fifo 0; setprop persist.sys.touch.pressure.scale 1.0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "hz_185_unlock",
                "185Hz Extreme Max Refresh Rate Lock",
                "Forces 185Hz via Shizuku: system settings + Game Mode + SurfaceFlinger 1035/1036 + device_config",
                "settings put system peak_refresh_rate 185.0; settings put system min_refresh_rate 185.0; settings put system user_refresh_rate 185; settings put global peak_refresh_rate 185.0; settings put global min_refresh_rate 185.0; cmd game mode performance global; cmd window set-app-refresh-rate global 185; device_config put game_overlay global mode=2,fps=185:mode=3,fps=185; service call SurfaceFlinger 1035 i32 185; service call SurfaceFlinger 1036 i32 185; setprop debug.sf.fps_limit 185; setprop persist.sys.NV_FPSLIMIT 185; setprop persist.sys.NV_POWERMODE 1; setprop debug.gr.swapinterval 0",
                "settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete global peak_refresh_rate; settings delete global min_refresh_rate",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        // Shizuku / ADB System Tweaks
        TWEAKS.add(new TweakItem(
                "shizuku_fast_anim",
                "0.5x UI Speed Animations",
                "Reduces system window transition duration",
                "settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5",
                "settings put global window_animation_scale 1.0; settings put global transition_animation_scale 1.0; settings put global animator_duration_scale 1.0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "tcp_latency_tuning",
                "TCP Low Latency Buffer Tuning",
                "Optimizes Wi-Fi and Cellular TCP buffer limits to lower multiplayer gaming ping",
                "setprop net.tcp.buffersize.wifi 524288,1048576,2097152,262144,524288,1048576; setprop net.tcp.buffersize.mobile 524288,1048576,2097152,262144,524288,1048576",
                "setprop net.tcp.buffersize.wifi default; setprop net.tcp.buffersize.mobile default",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "force_4x_msaa",
                "Force 4x MSAA Anti-Aliasing",
                "Forces 4x Multi-Sample Anti-Aliasing for crisp 3D graphics rendering",
                "setprop debug.egl.force_msaa 1",
                "setprop debug.egl.force_msaa 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gpu_16bit_color",
                "GPU High-Speed 16-Bit Alpha",
                "Uses fast 16-bit texture format to boost rendering FPS",
                "setprop persist.sys.use_16bpp_alpha 1",
                "setprop persist.sys.use_16bpp_alpha 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "bypass_game_throttle",
                "Targeted Game Driver & Latency Uncap",
                "Disables default FPS caps and enables ANGLE/Game Driver explicitly for MLBB, PUBGM, CODM, Free Fire, Genshin, HOK & Roblox",
                "setprop debug.graphics.game_default_frame_rate.disabled 1; settings put global game_driver_all_apps 0; settings put global updatable_driver_all_apps 0; settings put global game_driver_opt_in_apps " + com.gamebooster.app.booster.GpuTweaksChannel.TARGET_GAMES_PACKAGES + "; settings put global updatable_driver_production_opt_in_apps " + com.gamebooster.app.booster.GpuTweaksChannel.TARGET_GAMES_PACKAGES,
                "setprop debug.graphics.game_default_frame_rate.disabled 0; settings put global game_driver_opt_in_apps \"\"; settings put global updatable_driver_production_opt_in_apps \"\"",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "wifi_latency_mode",
                "Low Latency Wi-Fi Packet Mode",
                "Forces high-power Wi-Fi lock to eliminate packet jitter during online games",
                "cmd wlan set-power-mode 0 || settings put global wifi_sleep_policy 2",
                "cmd wlan set-power-mode 2 || settings put global wifi_sleep_policy 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "cpu_affinity_priority",
                "CPU Thread Affinity Priority",
                "Directs game process threads to high-performance CPU cores",
                "setprop sys.games.cpu_affinity 1",
                "setprop sys.games.cpu_affinity 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "sf_latch_unsignaled",
                "Zero Frame Latency Latching",
                "Forces SurfaceFlinger to latch unsignaled buffers immediately for lower input latency",
                "setprop debug.sf.latch_unsignaled 1; setprop debug.performance.tuning 1",
                "setprop debug.sf.latch_unsignaled 0; setprop debug.performance.tuning 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "tethering_hw_offload",
                "Tethering Hardware Acceleration",
                "Enables hardware-offloaded tethering to reduce CPU overhead during hotspot gaming",
                "settings put global tether_offload_disabled 0",
                "settings put global tether_offload_disabled 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "force_full_gnss_raw",
                "Force Full GNSS Raw Measurements",
                "Forces raw GNSS/GPS measurements for high-precision location tracking in games",
                "settings put global development_settings_enabled 1; settings put global force_gnss_raw_measurements 1",
                "settings put global force_gnss_raw_measurements 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "sf_zero_vsync_phase",
                "Zero VSync Phase Frame Offsets",
                "Eliminates SurfaceFlinger VSync app/render phase offsets to render frames instantly",
                "setprop debug.sf.early_phase_offset_ns 0; setprop debug.sf.early_app_phase_offset_ns 0; setprop debug.sf.early_gl_phase_offset_ns 0",
                "setprop debug.sf.early_phase_offset_ns 1000000; setprop debug.sf.early_app_phase_offset_ns 1000000",
                TweakCategory.CPU_GPU,
                true
        ));

        // touch_pressure_scale_boost — slop line removed (covered by aim_touch_precision)
        // display_vsync_offset — removed: contradicts sf_zero_vsync_phase (0ns vs 500000ns conflict)

        TWEAKS.add(new TweakItem(
                "powerhal_sustained_perf",
                "PowerHAL Sustained Performance & Power Boost",
                "Forces PowerHAL sustained performance & extreme boost modes to prevent CPU clock drops",
                "cmd power set-mode 0 1; cmd power set-mode 2 1",
                "cmd power set-mode 0 0; cmd power set-mode 2 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "thermalservice_override",
                "Thermal Throttling Bypass Override",
                "Overrides thermal service status to cool (0) to eliminate thermal clock throttling",
                "cmd thermalservice override-status 0",
                "cmd thermalservice override-status -1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gpu_power_mode",
                "GPU Maximum Clocks & Power Mode",
                "Locks GPU frequency governor at maximum performance state",
                "setprop vendor.gpu.power_mode 1; setprop debug.gpu.performance 1",
                "setprop vendor.gpu.power_mode 0; setprop debug.gpu.performance 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "renderthread_vulkan_backend",
                "Vulkan RenderEngine & Skia Optimization",
                "Routes System UI & app rendering pipelines through Vulkan backend",
                "setprop debug.renderengine.backend vulkan; setprop renderthread.skia.reduceopstasksplitting true",
                "setprop debug.renderengine.backend gles; setprop renderthread.skia.reduceopstasksplitting false",
                TweakCategory.CPU_GPU,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // NEW GPU & RENDERING TWEAKS
        // ═══════════════════════════════════════════════════════════

        TWEAKS.add(new TweakItem(
                "disable_vsync",
                "Disable VSync Lock",
                "Removes VSync frame cap to unlock maximum GPU frame output",
                "setprop debug.egl.swapinterval 0",
                "setprop debug.egl.swapinterval 1",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gpu_overdraw_debug_off",
                "Disable GPU Overdraw Debug",
                "Turns off GPU overdraw visualization layer to reclaim rendering cycles",
                "setprop debug.hwui.overdraw false",
                "setprop debug.hwui.overdraw show",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_hw_vsync",
                "SurfaceFlinger Disable HW VSync",
                "Disables hardware VSync for virtual displays to reduce compositor overhead",
                "setprop debug.sf.disable_hwc_vds 1",
                "setprop debug.sf.disable_hwc_vds 0",
                TweakCategory.CPU_GPU,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // NEW TOUCH & DISPLAY TWEAKS
        // ═══════════════════════════════════════════════════════════

        TWEAKS.add(new TweakItem(
                "pointer_speed_max",
                "Max Pointer Speed",
                "Maximizes touch pointer tracking speed for faster cursor response",
                "settings put system pointer_speed 7",
                "settings put system pointer_speed 0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_screen_auto_bright",
                "Lock Screen Brightness",
                "Disables auto-brightness to prevent display dimming during gameplay",
                "settings put system screen_brightness_mode 0",
                "settings put system screen_brightness_mode 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // NEW SYSTEM & SHIZUKU ADB TWEAKS
        // ═══════════════════════════════════════════════════════════

        TWEAKS.add(new TweakItem(
                "force_gpu_rendering",
                "Force GPU Rendering",
                "Forces all UI elements to use hardware GPU acceleration",
                "settings put global force_hw_ui 1",
                "settings put global force_hw_ui 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_battery_saver",
                "Disable Low Power Mode",
                "Prevents battery saver from throttling CPU/GPU clocks during gaming",
                "settings put global low_power 0",
                "settings put global low_power 1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "kill_bg_processes",
                "Aggressive Background Kill",
                "Forces system to immediately destroy background activities to free RAM",
                "settings put global always_finish_activities 1",
                "settings put global always_finish_activities 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_blur_effects",
                "Disable Window Blur Effects",
                "Disables UI background blur effects on Android 12+ to save GPU rendering cycles",
                "settings put global window_blurs_enabled 0",
                "settings put global window_blurs_enabled 1",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "force_dark_mode",
                "Force System Dark UI Mode",
                "Enforces system dark theme to reduce OLED display power draw and GPU overdraw",
                "cmd uimode night yes",
                "cmd uimode night no",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_hw_overlay_compositor",
                "Disable HW Overlay Compositor",
                "Forces SurfaceFlinger GPU composition for consistent frame pacing",
                "setprop debug.sf.disable_hwc_overlay 1",
                "setprop debug.sf.disable_hwc_overlay 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gpu_buffer_count",
                "Triple Buffer Rendering",
                "Configures EGL 3-buffer pipeline to eliminate micro-stutter from swap waits",
                "setprop debug.egl.buffcount 3",
                "setprop debug.egl.buffcount 2",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_notification_alerts",
                "Gaming DND Notification Silence",
                "Enables Zen Mode DND to suppress popups and notification interruptions during gaming",
                "settings put global zen_mode 2",
                "settings put global zen_mode 0",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_haptic_feedback",
                "Disable Vibration Haptics",
                "Disables touch vibration motors to prevent tactile latency and save battery",
                "settings put system haptic_feedback_enabled 0",
                "settings put system haptic_feedback_enabled 1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // NEW NETWORK & LATENCY TWEAKS
        // ═══════════════════════════════════════════════════════════

        TWEAKS.add(new TweakItem(
                "dns_google_fast",
                "Google DNS Fast Resolve",
                "Routes DNS queries through Google 8.8.8.8 for fastest domain resolution",
                "setprop net.dns1 8.8.8.8; setprop net.dns2 8.8.4.4",
                "setprop net.dns1 \"\"; setprop net.dns2 \"\"",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "private_dns_cloudflare",
                "Cloudflare Private DNS 1.1.1.1",
                "Enables TLS-encrypted Cloudflare Private DNS for ultra-low gaming lookup ping",
                "settings put global private_dns_mode hostname; settings put global private_dns_specifier one.one.one.one",
                "settings put global private_dns_mode off",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "disable_captive_portal",
                "Disable Captive Portal Check",
                "Disables HTTP ping checks to prevent network latency spikes during game connections",
                "settings put global captive_portal_mode 0",
                "settings put global captive_portal_mode 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // COMPETITIVE GAMING TWEAKS
        // ═══════════════════════════════════════════════════════════

        TWEAKS.add(new TweakItem(
                "ram_turbo_mode",
                "RAM Turbo — Clear Hidden App Reserve",
                "Clears Android hidden app RAM reserves and lowers min memory floor to free maximum RAM for games",
                "settings put global min_hidden_apps 0; settings put global hidden_app_minmem_kb 0; settings put global background_process_limit 2",
                "settings put global min_hidden_apps 5; settings put global hidden_app_minmem_kb 512; settings put global background_process_limit -1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "io_scheduler_gaming",
                "Gaming I/O Scheduler & Low Swappiness",
                "Sets I/O scheduler to deadline for deterministic latency and lowers swappiness to 20 to keep game data in RAM",
                "setprop sys.io.scheduler deadline; setprop vm.swappiness 20; setprop vm.dirty_ratio 10; setprop vm.dirty_background_ratio 5",
                "setprop sys.io.scheduler cfq; setprop vm.swappiness 60; setprop vm.dirty_ratio 20; setprop vm.dirty_background_ratio 10",
                TweakCategory.CPU_GPU,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // ADVANCED REAL-WORLD GAMING TWEAKS (SHIZUKU TEMPORARY ROOT)
        // ═══════════════════════════════════════════════════════════

        // 1. Vulkan Shader Pipeline Pre-Cache
        TWEAKS.add(new TweakItem(
                "vulkan_pipeline_precache",
                "Vulkan Pipeline & Pixel Buffer Pre-Cache",
                "Pre-caches GPU pixel buffers and skips empty damage redraws to eliminate frame pacing jitter",
                "setprop debug.hwui.use_gpu_pixel_buffers true; setprop debug.renderengine.skia_pipeline true; setprop debug.hwui.skip_empty_damage true",
                "setprop debug.hwui.use_gpu_pixel_buffers false; setprop debug.renderengine.skia_pipeline false",
                TweakCategory.CPU_GPU,
                true
        ));

        // 2. OpenGL ES / Vulkan Multi-Threading Priority
        TWEAKS.add(new TweakItem(
                "gl_vulkan_multithreading",
                "RenderThread Realtime -20 Priority",
                "Forces graphics render thread priority to realtime -20 and enables multi-threaded EGL calls",
                "setprop debug.egl.multithread 1; setprop debug.hwui.render_thread_priority -20",
                "setprop debug.egl.multithread 0; setprop debug.hwui.render_thread_priority -10",
                TweakCategory.CPU_GPU,
                true
        ));

        // 3. SurfaceFlinger Frame Jitter Elimination
        TWEAKS.add(new TweakItem(
                "sf_jitter_elimination",
                "SurfaceFlinger Compositor Overhead Strip",
                "Disables compositor debug hooks, background visualizers, and frame overheads",
                "setprop debug.sf.showupdates 0; setprop debug.sf.showcpu 0; setprop debug.sf.showbackground 0; setprop debug.sf.showfps 0",
                "setprop debug.sf.showupdates 0",
                TweakCategory.CPU_GPU,
                true
        ));

        // 4. Zero-Deadzone Gyro Aim Filter
        TWEAKS.add(new TweakItem(
                "gyro_zero_filter_latency",
                "Zero-Deadzone Gyroscope Aim Filter",
                "Eliminates gyroscope deadzones and software filter delay for immediate pixel-level aim tracking",
                "setprop debug.sensor.gyro.filter_delay 0; setprop persist.sys.gyro.deadzone 0; setprop debug.sensor.gyro.bias_tracking 0",
                "setprop debug.sensor.gyro.filter_delay 1; setprop persist.sys.gyro.deadzone 2",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        // 7. SQLite In-Memory Gaming Cache
        TWEAKS.add(new TweakItem(
                "sqlite_wal_fast_cache",
                "SQLite In-Memory Cache Optimization",
                "Switches SQLite databases to memory journaling mode to eliminate micro-stutters during game asset streaming",
                "setprop db.log.slow_query_threshold 0; setprop debug.sqlite.wal.sync_mode 0; setprop debug.sqlite.journal_mode memory",
                "setprop debug.sqlite.wal.sync_mode 1; setprop debug.sqlite.journal_mode wal",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        // 8. ART Speed-Profile Dexopt Compilation
        TWEAKS.add(new TweakItem(
                "art_dexopt_speed_compile",
                "ART Speed-Profile AOT Game Compilation",
                "Ahead-of-Time compiles installed competitive games with compiler speed optimizations via Shizuku",
                "cmd package compile -m speed -f com.mobile.legends; cmd package compile -m speed -f com.tencent.ig; cmd package compile -m speed -f com.activision.callofduty.shooter; cmd package compile -m speed -f com.dts.freefireth",
                "cmd package compile -m quicken -f com.mobile.legends",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        // 9. Kernel CFS Scheduler Latency Reduction
        TWEAKS.add(new TweakItem(
                "kernel_sched_latency",
                "Kernel CFS Scheduler 1ms Latency Slice",
                "Shortens Linux CFS scheduling slice from 4ms down to 1ms for instant CPU thread dispatching",
                "setprop sys.perf.sched_min_granularity_ns 250000; setprop sys.perf.sched_latency_ns 1000000; setprop sys.perf.sched_wakeup_granularity_ns 500000",
                "setprop sys.perf.sched_min_granularity_ns 1000000; setprop sys.perf.sched_latency_ns 4000000",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        // 10. Google BBR TCP Congestion Algorithm Lock
        TWEAKS.add(new TweakItem(
                "tcp_bbr_congestion_lock",
                "Google BBR TCP Congestion Algorithm",
                "Locks TCP congestion algorithm to Google BBR to eliminate packet queue bufferbloat during online matches",
                "setprop net.ipv4.tcp_congestion_control bbr; setprop net.ipv4.tcp_notsent_lowat 16384",
                "setprop net.ipv4.tcp_congestion_control cubic",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        // 11. WLAN Hardware Low-Latency Mode
        TWEAKS.add(new TweakItem(
                "wlan_fast_roaming_boost",
                "WLAN Chipset Hardware Low-Latency Lock",
                "Enables hardware Wi-Fi low-latency mode and disables scan throttling during gameplay",
                "settings put global wifi_scan_throttle_enabled 0; setprop persist.vendor.wifi.low_latency 1; cmd wifi force-low-latency-mode enabled",
                "settings put global wifi_scan_throttle_enabled 1; cmd wifi force-low-latency-mode disabled",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        // 12. Packet Pacing Immediate Transmission (Anti-Corking)
        TWEAKS.add(new TweakItem(
                "tcp_packet_pacing_gaming",
                "Immediate Packet Pacing & Anti-Corking",
                "Disables socket corking so game packets transmit immediately without buffering delays",
                "setprop net.ipv4.tcp_pacing_ss_ratio 200; setprop net.ipv4.tcp_pacing_ca_ratio 120; setprop net.ipv4.tcp_autocorking 0",
                "setprop net.ipv4.tcp_autocorking 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        // 13. 5G & 6G NR Standalone Low-Latency Accelerator
        TWEAKS.add(new TweakItem(
                "5g_6g_nr_burst_buffers",
                "5G & 6G NR Standalone Low-Latency Accelerator",
                "Unlocks 8MB high-throughput TCP window buffers and prioritizes 5G SA/NSA frequencies",
                "setprop net.tcp.buffersize.5g 524288,1048576,8388608,262144,524288,4194304; setprop net.tcp.buffersize.nr 524288,1048576,8388608,262144,524288,4194304; setprop net.tcp.buffersize.6g 524288,1048576,8388608,262144,524288,4194304; setprop persist.vendor.radio.5g_mode_pref 1; settings put global mobile_data_always_on 1",
                "settings put global mobile_data_always_on 0",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        // 14. Wi-Fi 6/6E/7 Low-Latency & Anti-Lag Driver Lock
        TWEAKS.add(new TweakItem(
                "wifi_6_7_latency_lock",
                "Wi-Fi 6/6E/7 Low-Latency & Anti-Lag Driver Lock",
                "Forces Wi-Fi low-latency mode, suppresses AP background scan lag spikes, and disables power-save throttling",
                "cmd wifi force-low-latency-mode enabled; cmd wifi force-hi-perf-mode enabled; settings put global wifi_scan_always_enabled 0; settings put global wifi_sleep_policy 2; settings put global wifi_power_save 0; setprop debug.wifi.low_latency 1",
                "cmd wifi force-low-latency-mode disabled; settings put global wifi_scan_always_enabled 1; settings put global wifi_power_save 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        // 15. Dual Mobile Data + Wi-Fi Multipath Aggregation
        TWEAKS.add(new TweakItem(
                "dual_data_wifi_aggregation",
                "Dual Mobile Data + Wi-Fi Multipath Aggregation",
                "Maintains persistent cellular readiness during Wi-Fi connections for instantaneous zero-packet-drop failover",
                "settings put global mobile_data_always_on 1; settings put global data_stall_recovery_on_bad_network 1; settings put global tcp_default_init_rwnd 60",
                "settings put global mobile_data_always_on 0",
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

    public static boolean applyTweak(TweakItem tweak) {
        return applyTweak(null, tweak);
    }

    public static boolean applyTweak(Context context, TweakItem tweak) {
        if (tweak == null) return false;

        String res;
        if (com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            res = com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().executeCommand(tweak.getApplyCommand());
        } else if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(tweak.getApplyCommand());
        } else {
            res = CommandExecutor.executeSystemCommand(tweak.getApplyCommand());
        }

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

        String res;
        if (com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            res = com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().executeCommand(tweak.getRevertCommand());
        } else if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(tweak.getRevertCommand());
        } else {
            res = CommandExecutor.executeSystemCommand(tweak.getRevertCommand());
        }

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

        // Fast atomic execution via Shizuku AIDL
        if (com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
            com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().execBatchCommands(batchCmds);
        } else if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommands(String.join("; ", batchCmds));
        }

        // Execute master root performance tweaks script for 185Hz
        com.gamebooster.app.booster.PerformanceChannel.writeAndExecuteRootTweaksScript(185);

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

    public static void restoreAppliedTweaksAsync(Context context) {
        if (context == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            EngineMode mode = CommandExecutor.getActiveEngineMode();
            if (mode == EngineMode.READ_ONLY) return;

            // 1. Re-apply all enabled Shizuku ADB system tweaks
            for (TweakItem tweak : TWEAKS) {
                boolean wasSavedApplied = TweakPreferences.isTweakApplied(context, tweak.getId());
                if (wasSavedApplied) {
                    if (tweak.isRequiresShizuku() && mode != EngineMode.SHIZUKU) continue;
                    applyTweak(context, tweak);
                }
            }

            // 2. Re-apply manual hardware engine settings permanently (Zero Auto-Off)
            if (ManualSettingsPreferences.isAngleModeEnabled(context)) {
                com.gamebooster.app.booster.GpuTweaksChannel.setAngleMode(true);
            }
            if (ManualSettingsPreferences.isGameDriverEnabled(context)) {
                com.gamebooster.app.booster.GpuTweaksChannel.setGameDriverMode(true);
            }
            boolean isVulkan = "vulkan".equalsIgnoreCase(ManualSettingsPreferences.getGpuMode(context));
            com.gamebooster.app.booster.PerformanceChannel.setGpuRenderMode(isVulkan);

            boolean isPerfCpu = "performance".equalsIgnoreCase(ManualSettingsPreferences.getCpuMode(context));
            com.gamebooster.app.booster.CpuGovernorChannel.setGovernor(isPerfCpu ? "extreme" : "schedutil");

            if (ManualSettingsPreferences.isTetherHwEnabled(context)) {
                com.gamebooster.app.booster.NetworkOptimizer.setTetheringHwAcceleration(true);
            }
            if (ManualSettingsPreferences.isForceGnssEnabled(context)) {
                com.gamebooster.app.booster.NetworkOptimizer.setForceFullGnss(true);
            }

            // 3. Execute master root performance script for 185Hz
            com.gamebooster.app.booster.PerformanceChannel.writeAndExecuteRootTweaksScript(185);
        });
    }
}
