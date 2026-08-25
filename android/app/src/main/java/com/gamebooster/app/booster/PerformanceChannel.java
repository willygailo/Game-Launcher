package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;

public class PerformanceChannel {

    private static final String TAG = "PerformanceChannel";

    public enum Profile {
        EXTREME_PERFORMANCE("Extreme Performance"),
        PERFORMANCE("High Performance"),
        BALANCED("Balanced");

        public final String title;
        Profile(String title) { this.title = title; }
    }

    public static final class ProfileResult {
        public final boolean refreshRateApplied;
        public final int appliedHz;
        public final String message;

        private ProfileResult(boolean refreshRateApplied, int appliedHz, String message) {
            this.refreshRateApplied = refreshRateApplied;
            this.appliedHz = appliedHz;
            this.message = message;
        }
    }

    public static boolean applyProfile(Context context, Profile profile) {
        return applyProfileWithResult(context, profile).refreshRateApplied;
    }

    /** Applies the strongest refresh rate with full GPU, CPU, Touch, and Network optimizations. */
    public static ProfileResult applyProfileWithResult(Context context, Profile profile) {
        if (context == null) return new ProfileResult(false, 0, "Device context is unavailable");

        final int targetHz = GameProfileAutoConfigurator.getTargetFpsHz(context);

        HzFpsChannel.RefreshRateResult refreshResult;
        if (profile == Profile.EXTREME_PERFORMANCE) {
            // EXTREME: force unconditionally via Shizuku — no capability gate, no fallback
            refreshResult = HzFpsChannel.forceSetRefreshRate(context, targetHz);
        } else {
            refreshResult = HzFpsChannel.setRefreshRate(context, targetHz);
        }

        // Apply all hardware and software performance channels
        CpuGovernorChannel.setPerformanceLock();
        GpuTweaksChannel.setGpuMaxPerformance();
        GpuTweaksChannel.setGameDriverMode(true);
        TouchLatencyChannel.enableUltraTouchResponse();
        NetworkTweaksChannel.enableLowLatencyNetwork();
        ThermalChannel.setThermalOverride(true);
        RamZramChannel.trimMemoryAndCleanCache(context);
        RamZramChannel.applyIOAndMemoryFlags();
        writeAndExecuteRootTweaksScript(targetHz);

        return new ProfileResult(true, targetHz, "⚡ " + profile.title + " Locked @ " + targetHz + "Hz");
    }

    /** Writes and executes root shell script at 185Hz. */
    public static boolean writeAndExecuteRootTweaksScript() {
        return writeAndExecuteRootTweaksScript(185);
    }

    /**
     * Writes and executes an elevated performance script for the specified target Hz.
     *
     * @param targetHz Target refresh rate (120, 144, 165, or 185)
     */
    public static boolean writeAndExecuteRootTweaksScript(int targetHz) {
        final int hz = targetHz > 0 ? targetHz : 185;
        try {
            String targetGamesCsv = GpuTweaksChannel.getTargetGamesCsv();
            int coreCount = CpuGovernorChannel.detectCpuCoreCount();
            int maxCore = Math.max(7, coreCount - 1);
            int bgCore = Math.min(3, maxCore / 2);

            String scriptPath = "/data/local/tmp/gamebooster_tweaks.sh";
            String scriptContent = "#!/system/bin/sh\n" +
                    "sync; echo 3 > /proc/sys/vm/drop_caches\n" +
                    // 1. Zero Animation Scale (0.0x Instant UI Speed)
                    "settings put global window_animation_scale 0.0\n" +
                    "settings put global transition_animation_scale 0.0\n" +
                    "settings put global animator_duration_scale 0.0\n" +
                    "settings put system window_animation_scale 0.0\n" +
                    "settings put system transition_animation_scale 0.0\n" +
                    "settings put system animator_duration_scale 0.0\n" +
                    "cmd activity update-configuration --anim-scale 0.0\n" +
                    // 2. Targeted Game Driver & ANGLE Driver (Target Games ONLY)
                    "settings put global game_driver_all_apps 0\n" +
                    "settings put global updatable_driver_all_apps 0\n" +
                    "settings put global game_driver_opt_in_apps " + targetGamesCsv + "\n" +
                    "settings put global game_driver_prerelease_opt_in_apps " + targetGamesCsv + "\n" +
                    "settings put global updatable_driver_production_opt_in_apps " + targetGamesCsv + "\n" +
                    "settings put global angle_gl_driver_all_angle 0\n" +
                    "settings put global angle_enabled_pkgs 1\n" +
                    "settings put global angle_gl_driver_selection_pkgs " + targetGamesCsv + "\n" +
                    // 3. JVM / Dalvik / ART Runtime Performance Turbo
                    "setprop dalvik.vm.execution-mode int:jit\n" +
                    "setprop dalvik.vm.usejit true\n" +
                    "setprop dalvik.vm.usejitprofiles true\n" +
                    "setprop dalvik.vm.heapgrowthlimit 512m\n" +
                    "setprop dalvik.vm.heapsize 1024m\n" +
                    "setprop dalvik.vm.heaptargetutilization 0.75\n" +
                    "setprop dalvik.vm.heapminfree 8m\n" +
                    "setprop dalvik.vm.heapmaxfree 32m\n" +
                    "setprop dalvik.vm.heapstartsize 32m\n" +
                    "setprop dalvik.vm.jitcodecachesize 64m\n" +
                    "setprop dalvik.vm.jitinitialsize 16m\n" +
                    "setprop dalvik.vm.jitthreshold 100\n" +
                    "setprop dalvik.vm.dex2oat-filter speed\n" +
                    "setprop pm.dexopt.boot speed-profile\n" +
                    "setprop pm.dexopt.install speed\n" +
                    "setprop pm.dexopt.bg-dexopt speed\n" +
                    // 4. Multi-Core Processor Architecture Tuning (8-core, 12-core, 16-core+)
                    "echo 0-" + maxCore + " > /dev/cpuset/top-app/cpus 2>/dev/null\n" +
                    "echo 0-" + maxCore + " > /dev/cpuset/foreground/cpus 2>/dev/null\n" +
                    "echo 0-" + bgCore + " > /dev/cpuset/background/cpus 2>/dev/null\n" +
                    "echo 0-" + bgCore + " > /dev/cpuset/system-background/cpus 2>/dev/null\n" +
                    "echo 0-" + maxCore + " > /dev/cpuset/restricted/cpus 2>/dev/null\n" +
                    "for p in /sys/devices/system/cpu/cpufreq/policy*; do echo performance > \"$p/scaling_governor\" 2>/dev/null; if [ -f \"$p/scaling_max_freq\" ]; then cat \"$p/scaling_max_freq\" > \"$p/scaling_min_freq\" 2>/dev/null; fi; done\n" +
                    "setprop sys.games.cpu_affinity 1\n" +
                    "setprop sys.use_fifo 1\n" +
                    "setprop sys.perf.sched_uclamp_min 1024\n" +
                    "setprop sys.perf.sched_uclamp_min_rt 1024\n" +
                    "setprop sys.perf.sched_min_granularity_ns 250000\n" +
                    "setprop sys.perf.sched_latency_ns 1000000\n" +
                    "setprop sys.perf.sched_wakeup_granularity_ns 500000\n" +
                    "setprop sys.perf.sched_boost 1\n" +
                    "cmd power set-fixed-performance-mode-enabled true\n" +
                    // 5. Universal & Chipset GPU Graphics Processing (Snapdragon, MediaTek, Tensor, Exynos)
                    "setprop debug.sf.hw 1\n" +
                    "setprop debug.hwui.renderer vulkan\n" +
                    "setprop debug.renderengine.backend vulkan\n" +
                    "setprop debug.renderengine.skia_pipeline true\n" +
                    "setprop debug.hwui.use_gpu_pixel_buffers true\n" +
                    "setprop debug.hwui.render_thread_priority -20\n" +
                    "setprop debug.hwui.skip_empty_damage true\n" +
                    "setprop debug.sf.latch_unsignaled 1\n" +
                    "setprop debug.sf.disable_backpressure 1\n" +
                    "setprop debug.sf.enable_gl_backpressure 0\n" +
                    "setprop debug.sf.predict_hwc_composition_strategy 1\n" +
                    "setprop debug.sf.enable_adpf_cpu_hint true\n" +
                    "setprop debug.hwui.use_hint_manager true\n" +
                    "setprop persist.sys.adpf.enable 1\n" +
                    "setprop debug.adpf.hint.enabled 1\n" +
                    "setprop debug.adpf.cpu.boost 1\n" +
                    "setprop debug.adpf.gpu.boost 1\n" +
                    "setprop debug.adreno.turbo 1\n" +
                    "setprop debug.adreno.perf_level 0\n" +
                    "setprop debug.qualcomm.sns.hal 0\n" +
                    "setprop vendor.perf.gestureFlingBoost 1\n" +
                    "setprop persist.vendor.qti.games.gt.enable 1\n" +
                    "setprop vendor.gpu.power_mode 1\n" +
                    "echo performance > /sys/class/kgsl/kgsl-3d0/devfreq/governor 2>/dev/null\n" +
                    "echo 0 > /sys/class/kgsl/kgsl-3d0/min_pwrlevel 2>/dev/null\n" +
                    "echo 1 > /sys/class/kgsl/kgsl-3d0/force_bus_on 2>/dev/null\n" +
                    "echo 1 > /sys/class/kgsl/kgsl-3d0/force_clk_on 2>/dev/null\n" +
                    "echo 1 > /sys/class/kgsl/kgsl-3d0/force_rail_on 2>/dev/null\n" +
                    "setprop debug.mali.sched.priority -20\n" +
                    "setprop debug.mali.force_gpu_boost 1\n" +
                    "setprop debug.mali.realtime 1\n" +
                    "setprop persist.vendor.ged.boost 1\n" +
                    "setprop persist.vendor.dpt.enable 1\n" +
                    "setprop vendor.ppt.boost 1\n" +
                    "echo 0 > /sys/class/misc/mali0/device/dvfs_enable 2>/dev/null\n" +
                    "echo 1 > /sys/module/ged/parameters/gx_game_mode 2>/dev/null\n" +
                    "echo 1 > /sys/module/ged/parameters/gx_boost_on 2>/dev/null\n" +
                    "echo 1 > /sys/module/ged/parameters/gx_force_cpu_boost 2>/dev/null\n" +
                    "echo 100 > /sys/module/ged/parameters/gx_top_app_pid_boost 2>/dev/null\n" +
                    "setprop debug.tensor.gpu.boost 1\n" +
                    "setprop debug.exynos.performance.mode 1\n" +
                    "setprop debug.xclipse.gpu.boost 1\n" +
                    "echo 1 > /sys/devices/platform/17000000.gpu/power/control 2>/dev/null\n" +
                    "setprop debug.egl.force_msaa 1\n" +
                    "setprop debug.egl.buffcount 3\n" +
                    "setprop persist.sys.use_16bpp_alpha 1\n" +
                    "setprop debug.egl.multithread 1\n" +
                    "setprop debug.graphics.game_default_frame_rate.disabled 1\n" +
                    "setprop ro.vendor.dfps.enable 0\n" +
                    "setprop vendor.display.enable_default_fps_switch 0\n" +
                    "setprop persist.vendor.display.vrr.disable 1\n" +
                    "setprop ro.surface_flinger.set_idle_timer_ms 0\n" +
                    "setprop ro.surface_flinger.set_touch_timer_ms 0\n" +
                    "setprop debug.sf.fps_limit " + hz + "\n" +
                    "setprop persist.sys.NV_FPSLIMIT " + hz + "\n" +
                    "setprop persist.sys.game.fps " + hz + "\n" +
                    "setprop persist.sys.game.rate " + hz + "\n" +
                    "setprop persist.sys.fps " + hz + "\n" +
                    "setprop debug.cpurend.fps " + hz + "\n" +
                    "settings put system match_content_frame_rate 0\n" +
                    "settings put secure match_content_frame_rate_preference 0\n" +
                    "settings put system peak_refresh_rate " + hz + ".0\n" +
                    "settings put system min_refresh_rate " + hz + ".0\n" +
                    "settings put system user_refresh_rate " + hz + "\n" +
                    "settings put global peak_refresh_rate " + hz + ".0\n" +
                    "settings put global min_refresh_rate " + hz + ".0\n" +
                    "settings put secure user_refresh_rate " + hz + "\n" +
                    "settings put global surface_flinger_peak_refresh_rate " + hz + "\n" +
                    "settings put secure refresh_rate_mode 2\n" +
                    "settings put system sec_display_fps " + hz + "\n" +
                    "settings put secure game_auto_temperature_control 0\n" +
                    "settings put global oneplus_screen_refresh_rate 2\n" +
                    "settings put global realme_screen_refresh_rate " + hz + "\n" +
                    "settings put global oppo_screen_refresh_rate " + hz + "\n" +
                    "settings put system asus_option_display_refresh_rate " + hz + "\n" +
                    "settings put system asus_hfr_mode 1\n" +
                    "settings put system screen_refresh_rate " + hz + "\n" +
                    "settings put system iqoo_refresh_rate " + hz + "\n" +
                    "settings put system display_refresh_rate " + hz + "\n" +
                    "settings put system redmagic_refresh_rate " + hz + "\n" +
                    "cmd window set-app-refresh-rate global " + hz + "\n" +
                    "cmd game set --fps " + hz + " global\n" +
                    "cmd game mode performance global\n" +
                    "device_config put game_overlay global mode=2,fps=" + hz + ":mode=3,fps=" + hz + "\n" +
                    "service call SurfaceFlinger 1035 i32 " + hz + "\n" +
                    "service call SurfaceFlinger 1036 i32 " + hz + "\n" +
                    "cmd power set-mode 0 1\n" +
                    "cmd power set-mode 2 1\n" +
                    "cmd thermalservice override-status 0\n" +
                    "cmd thermal override-status 0\n" +
                    "setprop debug.thermal.throttle.disable 1\n" +
                    "setprop view.touch_slop 0\n" +
                    "setprop debug.input.max_events_per_sec 1000\n" +
                    "setprop persist.sys.touch.report_rate 1000\n" +
                    "setprop persist.vendor.touch.sampling_rate 1000\n" +
                    "setprop debug.sensor.gyro.sample_rate 1000\n" +
                    "setprop debug.sensor.gyro.smooth 1\n" +
                    "setprop debug.sensor.gyro.stabilization 1\n" +
                    "setprop persist.sys.gyro.filter 1\n" +
                    "setprop persist.sys.gyro.delay 0\n" +
                    // 6. Android WebView & Chromium GPU Acceleration Flags
                    "echo '" + WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/webview-command-line\n" +
                    "chmod 644 /data/local/tmp/webview-command-line 2>/dev/null\n" +
                    "echo '" + WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/chrome-command-line\n" +
                    "chmod 644 /data/local/tmp/chrome-command-line 2>/dev/null\n" +
                    "echo '" + WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/content-shell-command-line\n" +
                    "chmod 644 /data/local/tmp/content-shell-command-line 2>/dev/null\n" +
                    "echo '" + WebViewBoosterChannel.getWebViewCommandLineFlags() + "' > /data/local/tmp/android-webview-command-line\n" +
                    "chmod 644 /data/local/tmp/android-webview-command-line 2>/dev/null\n" +
                    "settings put global webview_multiprocess 1\n" +
                    "device_config put runtime_native_boot webview_surface_control true\n" +
                    "device_config put runtime_native_boot webview_zero_copy true\n" +
                    "device_config put runtime_native_boot webview_gpu_raster true\n" +
                    "device_config put runtime_native_boot webview_skia_vulkan true\n" +
                    "device_config put runtime_native_boot webview_drdc true\n" +
                    "setprop debug.chromium.flags \"--enable-gpu-rasterization --enable-zero-copy --enable-drdc --ignore-gpu-blocklist --enable-oop-rasterization --enable-webgl2-compute-context\"\n" +
                    "setprop debug.v8.flags \"--opt --always-opt --turbo-fast-api-calls --turboshaft\"\n" +
                    "setprop net.ipv4.tcp_congestion_control bbr\n" +
                    "cmd wifi force-low-latency-mode enabled\n" +
                    // 7. SurfaceFlinger display pipeline — missing flags
                    // Stop SF from down-clocking refresh rate when detecting "static" game content
                    "setprop debug.sf.use_content_detection_for_refresh_rate false\n" +
                    // Disable HWC virtual display — forces direct composition path
                    "setprop debug.sf.enable_hwc_vds 0\n" +
                    // Disable SF layer caching (can cause stale frame presentation)
                    "setprop debug.sf.layer_caching_enabled 0\n" +
                    // Disable SF transaction tracing on Android 14+ (reduces SF CPU load)
                    "setprop debug.sf.enable_transaction_tracing false\n" +
                    // Disable debug frame event dumping — reduces SurfaceFlinger CPU overhead
                    "setprop debug.sf.dump_frame_events 0\n" +
                    // Force native color mode — avoids color transform overhead in game frames
                    "setprop persist.sys.sf.color_mode 0\n" +
                    // SurfaceFlinger setTransactionFlags (1008): force immediate frame composition
                    "service call SurfaceFlinger 1008 2>/dev/null\n" +
                    // Disable SF idle timer — prevents refresh rate drops between game frames
                    "setprop ro.surface_flinger.set_idle_timer_ms 0\n" +
                    "setprop ro.surface_flinger.set_touch_timer_ms 0\n" +
                    // 8. Extended vendor-specific display FPS keys
                    "settings put system nubia_display_refresh_rate " + hz + "\n" +
                    "settings put system iqoo_ultra_refresh_rate " + hz + "\n" +
                    "settings put system blackshark_display_fps " + hz + "\n" +
                    "settings put system legion_display_rate " + hz + "\n" +
                    "settings put system vivo_refresh_rate " + hz + "\n" +
                    "settings put system xiaomi_display_fps " + hz + "\n" +
                    "settings put global honor_screen_refresh_rate " + hz + "\n" +
                    "settings put global tecno_display_refresh_rate " + hz + "\n";

            String cmd = String.format("printf '%s' > %s && chmod 755 %s && sh %s",
                    scriptContent.replace("'", "'\\''"), scriptPath, scriptPath, scriptPath);

            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                String out = ShizukuUserServiceConnector.getInstance().executeCommand(cmd);
                return out != null && !out.startsWith("ERROR");
            } else if (ShizukuExecutor.hasShizukuPermission()) {
                String out = ShizukuExecutor.executeShizukuCommand(cmd);
                return out != null && !out.startsWith("ERROR");
            } else {
                String res = CommandExecutor.executeSystemCommand(cmd);
                return CommandExecutor.isSuccessOutput(res);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Error executing root tweaks script", t);
            return false;
        }
    }

    public static boolean setGpuRenderMode(boolean is3D) {
        if (is3D) {
            boolean ok = GpuTweaksChannel.enableVulkanRenderer();
            ok &= CommandExecutor.setSystemProperty("debug.sf.hw", "1");
            return ok;
        } else {
            boolean ok = CommandExecutor.setSystemProperty("debug.hwui.renderer", "skia");
            ok &= CommandExecutor.setSystemProperty("debug.sf.hw", "0");
            return ok;
        }
    }

    public static boolean executeOneTapBoost(Context context) {
        return applyProfile(context, Profile.EXTREME_PERFORMANCE);
    }
}
