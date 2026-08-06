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
                "hz_120_unlock",
                "120Hz Ultra Refresh Rate Lock",
                "Forces 120Hz via Shizuku: system settings + Game Mode + SurfaceFlinger + device_config",
                "settings put system peak_refresh_rate 120.0; settings put system min_refresh_rate 120.0; settings put system user_refresh_rate 120; settings put global peak_refresh_rate 120.0; settings put global min_refresh_rate 120.0; cmd game mode performance global; cmd window set-app-refresh-rate global 120; device_config put game_overlay global mode=2,fps=120:mode=3,fps=120; service call SurfaceFlinger 1035 i32 120; service call SurfaceFlinger 1036 i32 120; setprop debug.sf.fps_limit 120; setprop persist.sys.NV_FPSLIMIT 120; setprop persist.sys.NV_POWERMODE 1",
                "settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete global peak_refresh_rate; settings delete global min_refresh_rate",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "hz_144_unlock",
                "144Hz Extreme Refresh Rate Lock",
                "Forces 144Hz via Shizuku: system settings + Game Mode + SurfaceFlinger + device_config",
                "settings put system peak_refresh_rate 144.0; settings put system min_refresh_rate 144.0; settings put system user_refresh_rate 144; settings put global peak_refresh_rate 144.0; settings put global min_refresh_rate 144.0; cmd game mode performance global; cmd window set-app-refresh-rate global 144; device_config put game_overlay global mode=2,fps=144:mode=3,fps=144; service call SurfaceFlinger 1035 i32 144; service call SurfaceFlinger 1036 i32 144; setprop debug.sf.fps_limit 144; setprop persist.sys.NV_FPSLIMIT 144; setprop persist.sys.NV_POWERMODE 1",
                "settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete global peak_refresh_rate; settings delete global min_refresh_rate",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "hz_165_unlock",
                "165Hz Max Hardware Refresh Rate Lock",
                "Forces 165Hz via Shizuku: system settings + Game Mode + SurfaceFlinger 1035/1036 + device_config",
                "settings put system peak_refresh_rate 165.0; settings put system min_refresh_rate 165.0; settings put system user_refresh_rate 165; settings put global peak_refresh_rate 165.0; settings put global min_refresh_rate 165.0; cmd game mode performance global; cmd window set-app-refresh-rate global 165; device_config put game_overlay global mode=2,fps=165:mode=3,fps=165; service call SurfaceFlinger 1035 i32 165; service call SurfaceFlinger 1036 i32 165; setprop debug.sf.fps_limit 165; setprop persist.sys.NV_FPSLIMIT 165; setprop persist.sys.NV_POWERMODE 1; setprop debug.gr.swapinterval 0",
                "settings delete system peak_refresh_rate; settings delete system min_refresh_rate; settings delete system user_refresh_rate; settings delete global peak_refresh_rate; settings delete global min_refresh_rate",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "mlbb_120fps_unlock",
                "Mobile Legends 120/165 FPS Unlock",
                "Forces MLBB package to run at 120/165Hz with Game Mode performance intervention via Shizuku",
                "cmd game mode performance com.mobile.legends; cmd game set --fps 165 com.mobile.legends; cmd window set-app-refresh-rate com.mobile.legends 165; device_config put game_overlay com.mobile.legends mode=2,fps=165:mode=3,fps=165",
                "cmd game mode standard com.mobile.legends; cmd window set-app-refresh-rate com.mobile.legends 0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "pubgm_120fps_unlock",
                "PUBG Mobile / BGMI 120 FPS Unlock",
                "Forces PUBGM & BGMI packages to 120/165 FPS with Game Overlay override via Shizuku",
                "cmd game mode performance com.tencent.ig; cmd game set --fps 165 com.tencent.ig; cmd window set-app-refresh-rate com.tencent.ig 165; device_config put game_overlay com.tencent.ig mode=2,fps=165:mode=3,fps=165; cmd game mode performance com.pubg.imobile; cmd game set --fps 165 com.pubg.imobile; cmd window set-app-refresh-rate com.pubg.imobile 165",
                "cmd game mode standard com.tencent.ig; cmd window set-app-refresh-rate com.tencent.ig 0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "codm_120fps_unlock",
                "COD Mobile 120/165 FPS Unlock",
                "Forces CODM package to 120/165 FPS with high-priority surface flinger refresh rate via Shizuku",
                "cmd game mode performance com.activision.callofduty.shooter; cmd game set --fps 165 com.activision.callofduty.shooter; cmd window set-app-refresh-rate com.activision.callofduty.shooter 165; device_config put game_overlay com.activision.callofduty.shooter mode=2,fps=165:mode=3,fps=165; cmd game mode performance com.garena.game.codm; cmd game set --fps 165 com.garena.game.codm; cmd window set-app-refresh-rate com.garena.game.codm 165",
                "cmd game mode standard com.activision.callofduty.shooter; cmd window set-app-refresh-rate com.activision.callofduty.shooter 0",
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
                "Disables default FPS caps and enables ANGLE/Game Driver explicitly for MLBB, PUBGM & CODM",
                "setprop debug.graphics.game_default_frame_rate.disabled 1; settings put global game_driver_all_apps 0; settings put global game_driver_opt_in_apps com.mobile.legends,com.mobilelegends.win,com.tencent.ig,com.pubg.krmobile,com.vng.pubgmobile,com.pubg.imobile,com.activision.callofduty.shooter,com.garena.game.codm",
                "setprop debug.graphics.game_default_frame_rate.disabled 0; settings put global game_driver_opt_in_apps \"\"",
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

        TWEAKS.add(new TweakItem(
                "max_background_limit",
                "Limit Background Apps",
                "Restricts cached background processes to 2, releasing 200-400MB RAM for games",
                "settings put global background_process_limit 2",
                "settings put global background_process_limit -1",
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

        TWEAKS.add(new TweakItem(
                "disable_mobile_data_always",
                "Disable Always-On Mobile Data",
                "Stops background mobile data drain when connected to Wi-Fi to save battery",
                "settings put global mobile_data_always_on 0",
                "settings put global mobile_data_always_on 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // COMPETITIVE GAMING TWEAKS (NEW — NO DUPLICATES)
        // ═══════════════════════════════════════════════════════════

        TWEAKS.add(new TweakItem(
                "super_fast_touch_165",
                "Super Fast Touch 165Hz Competitive Mode",
                "Ultra-low slop (0), max digitizer input rate 1000/s, min pressure scale — tuned for 165Hz competitive play",
                "setprop view.touch_slop 0; setprop persist.sys.touch.pressure.scale 0.0001; setprop debug.input.max_events_per_sec 1000; setprop sys.use_fifo 1; settings put system touch_slop_reduction 1",
                "setprop view.touch_slop 8; setprop persist.sys.touch.pressure.scale 1.0; setprop debug.input.max_events_per_sec 150; setprop sys.use_fifo 0; settings put system touch_slop_reduction 0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

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

        EngineMode engineMode = CommandExecutor.getActiveEngineMode();
        if (tweak.isRequiresShizuku() && engineMode == EngineMode.READ_ONLY) {
            return false;
        }

        String res;
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(tweak.getApplyCommand());
        } else {
            res = CommandExecutor.executeSystemCommand(tweak.getApplyCommand());
        }

        boolean success = CommandExecutor.isSuccessOutput(res);
        if (success) {
            tweak.setApplied(true);
            if (context != null) {
                TweakPreferences.saveTweakState(context, tweak.getId(), true);
            }
        }
        return success;
    }

    public static boolean revertTweak(TweakItem tweak) {
        return revertTweak(null, tweak);
    }

    public static boolean revertTweak(Context context, TweakItem tweak) {
        if (tweak == null) return false;

        String res;
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            res = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(tweak.getRevertCommand());
        } else {
            res = CommandExecutor.executeSystemCommand(tweak.getRevertCommand());
        }

        boolean success = CommandExecutor.isSuccessOutput(res);
        if (success) {
            tweak.setApplied(false);
            if (context != null) {
                TweakPreferences.saveTweakState(context, tweak.getId(), false);
            }
        }
        return success;
    }

    public static int applyAllSupportedTweaks() {
        return applyAllSupportedTweaks(null);
    }

    public static int applyAllSupportedTweaks(Context context) {
        int appliedCount = 0;
        EngineMode engineMode = CommandExecutor.getActiveEngineMode();

        for (TweakItem tweak : TWEAKS) {
            if (tweak.isRequiresShizuku() && engineMode == EngineMode.READ_ONLY) continue;

            if (applyTweak(context, tweak)) {
                appliedCount++;
            }
        }
        return appliedCount;
    }

    public static void applyAllSupportedTweaksAsync(Context context, OnBatchCompleteListener listener) {
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

            // 3. Execute master root performance script
            com.gamebooster.app.booster.PerformanceChannel.writeAndExecuteRootTweaksScript();
        });
    }
}
