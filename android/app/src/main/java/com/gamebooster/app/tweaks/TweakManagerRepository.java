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
                "sf_latch_unsignaled",
                "Zero Frame Latency Latching & Backpressure Bypass",
                "Latches graphics buffers immediately without waiting for fence signaling and disables buffer backpressure",
                "setprop debug.sf.latch_unsignaled 1; setprop debug.sf.disable_backpressure 1; setprop debug.performance.tuning 1",
                "setprop debug.sf.latch_unsignaled 0; setprop debug.sf.disable_backpressure 0; setprop debug.performance.tuning 0",
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
                "cpu_affinity_priority",
                "CPU Thread Affinity Priority",
                "Pins game processes and render threads to high-performance Big/Prime CPU cores",
                "setprop sys.games.cpu_affinity 1; setprop sys.use_fifo 1",
                "setprop sys.games.cpu_affinity 0; setprop sys.use_fifo 0",
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

        // =========================================================================
        // 3. SYSTEM & SHIZUKU ADB PRIVILEGED TWEAKS (Unique & Non-Overlapping)
        // =========================================================================
        TWEAKS.add(new TweakItem(
                "shizuku_fast_anim",
                "0.5x UI Speed Animations",
                "Reduces system window and transition duration scale to 0.5x for snappy OS response",
                "settings put global window_animation_scale 0.5; settings put global transition_animation_scale 0.5; settings put global animator_duration_scale 0.5",
                "settings put global window_animation_scale 1.0; settings put global transition_animation_scale 1.0; settings put global animator_duration_scale 1.0",
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
                "kill_bg_processes",
                "Aggressive Background Kill On Exit",
                "Instructs ActivityManager to destroy activities as soon as the user leaves them",
                "settings put global always_finish_activities 1",
                "settings put global always_finish_activities 0",
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
                "ram_turbo_mode",
                "RAM Turbo — Clear Hidden App Reserve",
                "Reduces cached background app retention to 2 processes to maximize free RAM for active games",
                "settings put global min_hidden_apps 0; settings put global hidden_app_minmem_kb 0; settings put global background_process_limit 2",
                "settings put global min_hidden_apps 5; settings put global hidden_app_minmem_kb 512; settings put global background_process_limit -1",
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
                "ART Speed-Profile AOT Game Compilation",
                "Ahead-of-Time compiles installed competitive games with compiler speed optimizations via Shizuku",
                "cmd package compile -m speed -f com.mobile.legends; cmd package compile -m speed -f com.tencent.ig; cmd package compile -m speed -f com.activision.callofduty.shooter; cmd package compile -m speed -f com.dts.freefireth; cmd package compile -m speed -f com.proximabeta.mf.uamo; cmd package compile -m speed -f com.riotgames.league.wildrift",
                "cmd package compile -m quicken -f com.mobile.legends",
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
                "tcp_bbr_congestion_lock",
                "Google BBR TCP Congestion Algorithm",
                "Locks TCP congestion algorithm to Google BBR to eliminate packet queue bufferbloat during matches",
                "setprop net.ipv4.tcp_congestion_control bbr; setprop net.ipv4.tcp_notsent_lowat 16384",
                "setprop net.ipv4.tcp_congestion_control cubic",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

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
            com.gamebooster.app.booster.PerformanceChannel.writeAndExecuteRootTweaksScript(185);
        });
    }
}
