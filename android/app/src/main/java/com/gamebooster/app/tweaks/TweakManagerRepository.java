package com.gamebooster.app.tweaks;

import android.content.Context;

import com.gamebooster.app.config.TweakPreferences;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.EngineMode;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Universal Tweak Manager Repository for Game Launcher PRO.
 * Curated, deduplicated, and CTS-compliant privileged system tweaks.
 */
public class TweakManagerRepository {

    public interface OnBatchCompleteListener {
        void onBatchComplete(int appliedCount);
    }

    private static final List<TweakItem> TWEAKS = new ArrayList<>();

    static {
        // ═══════════════════════════════════════════════════════════
        // 1. CPU, GPU & GRAPHICS RENDERING ENGINES
        // ═══════════════════════════════════════════════════════════
        TWEAKS.add(new TweakItem(
                "gpu_hw_composition",
                "Vulkan 3D HWUI Graphics Pipeline",
                "Forces Vulkan hardware graphics pipeline and disables backpressure for high rendering throughput",
                "setprop debug.hwui.renderer vulkan; setprop debug.renderengine.backend skiagl; setprop debug.sf.disable_backpressure 1",
                "setprop debug.hwui.renderer default; setprop debug.renderengine.backend default; setprop debug.sf.disable_backpressure 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "hw_overlays",
                "Force SurfaceFlinger HW Composition",
                "Forces 100% GPU hardware composition overlays to eliminate CPU rendering overhead",
                "setprop debug.sf.hw 1; service call SurfaceFlinger 1008 i32 1",
                "setprop debug.sf.hw 0; service call SurfaceFlinger 1008 i32 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "adreno_turbo_boost",
                "Qualcomm Adreno & Mali GPU Turbo Boost",
                "Enables GPU maximum burst clock level, vendor PowerHAL turbo, and gesture fling boost",
                "setprop debug.adreno.turbo 1; setprop debug.adreno.perf_level 0; setprop vendor.mali.gpu.power_policy performance; setprop vendor.perf.gestureFlingBoost 1",
                "setprop debug.adreno.turbo 0; setprop debug.adreno.perf_level 1; setprop vendor.mali.gpu.power_policy default",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gl_vulkan_multithreading",
                "RenderThread Realtime Priority (-20)",
                "Forces graphics render thread priority to realtime -20 and enables multi-threaded EGL calls",
                "setprop debug.egl.multithread 1; setprop debug.hwui.render_thread_priority -20",
                "setprop debug.egl.multithread 0; setprop debug.hwui.render_thread_priority -10",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "vulkan_pipeline_precache",
                "Vulkan Pipeline & Pixel Buffer Pre-Cache",
                "Pre-caches GPU pixel buffers and skips empty damage redraws to eliminate frame pacing jitter",
                "setprop debug.hwui.use_gpu_pixel_buffers true; setprop debug.renderengine.skia_pipeline true; setprop debug.hwui.skip_empty_damage true",
                "setprop debug.hwui.use_gpu_pixel_buffers false; setprop debug.renderengine.skia_pipeline false",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "sf_jitter_elimination",
                "SurfaceFlinger Compositor Overhead Strip",
                "Disables compositor debug hooks, background visualizers, and frame overheads for zero micro-stutter",
                "setprop debug.sf.showupdates 0; setprop debug.sf.showcpu 0; setprop debug.sf.showbackground 0; setprop debug.sf.showfps 0",
                "setprop debug.sf.showupdates 0",
                TweakCategory.CPU_GPU,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // 2. TOUCH, GYROSCOPE & SENSORS (1000Hz DEDICATED)
        // ═══════════════════════════════════════════════════════════
        TWEAKS.add(new TweakItem(
                "digitizer_1000hz_touch",
                "1000Hz Hardware Digitizer & Zero Touch Slop",
                "Overclocks touchscreen digitizer polling to 1000Hz, reduces touch slop to 1px, and minimizes pressure latency",
                "setprop persist.sys.touch.report_rate 1000; setprop persist.vendor.touch.sampling_rate 1000; setprop view.touch_slop 1; settings put system touch_slop_reduction 1; setprop debug.input.max_events_per_sec 1000; setprop persist.sys.touch.pressure.scale 0.001",
                "setprop persist.sys.touch.report_rate 120; setprop persist.vendor.touch.sampling_rate 120; setprop view.touch_slop 8; settings put system touch_slop_reduction 0; setprop debug.input.max_events_per_sec 240; setprop persist.sys.touch.pressure.scale 1.0",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gyro_1000hz_engine",
                "1000Hz Ultra Gyroscope & Zero-Deadzone Aim Filter",
                "Sets sensor batch rate to 1000Hz, eliminates gyro deadzones, and enables zero-delay precision motion tracking",
                "setprop debug.sensor.gyro.sample_rate 1000; setprop persist.sys.gyro.delay 0; setprop debug.sensor.motion.rate 1000; setprop debug.sensor.gyro.filter_delay 0; setprop persist.sys.gyro.deadzone 0",
                "setprop debug.sensor.gyro.sample_rate 200; setprop persist.sys.gyro.delay 1; setprop debug.sensor.gyro.filter_delay 1; setprop persist.sys.gyro.deadzone 2",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "scrolling_cache_boost",
                "Zero Scroll Cache Latency",
                "Disables scrolling cache compression to save CPU cycles and accelerate screen swipe responsiveness",
                "setprop persist.sys.scrollingcache 3",
                "setprop persist.sys.scrollingcache 1",
                TweakCategory.TOUCH_DISPLAY,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // 3. KERNEL, CPU SCHEDULER & REALTIME DISPATCH
        // ═══════════════════════════════════════════════════════════
        TWEAKS.add(new TweakItem(
                "fifo_realtime_scheduler",
                "Realtime FIFO Kernel Task Dispatcher",
                "Forces realtime FIFO scheduling queue for game threads and audio render pipelines",
                "setprop sys.use_fifo 1; setprop sys.use_fifo_ui 1; setprop persist.sys.sched_boost 1",
                "setprop sys.use_fifo 0; setprop sys.use_fifo_ui 0; setprop persist.sys.sched_boost 0",
                TweakCategory.CPU_GPU,
                true
        ));

        TWEAKS.add(new TweakItem(
                "kernel_cfs_latency",
                "Linux Kernel CFS 1ms Scheduler Slice",
                "Shortens CFS scheduling slice from 4ms down to 1ms for instant CPU thread dispatching",
                "setprop sys.perf.sched_min_granularity_ns 250000; setprop sys.perf.sched_latency_ns 1000000; setprop sys.perf.sched_wakeup_granularity_ns 500000",
                "setprop sys.perf.sched_min_granularity_ns 1000000; setprop sys.perf.sched_latency_ns 4000000; setprop sys.perf.sched_wakeup_granularity_ns 2000000",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "gaming_io_scheduler",
                "Low-Latency Gaming I/O & Memory Swappiness",
                "Sets I/O scheduler to deadline and lowers swappiness to 20 to keep active game textures in fast RAM",
                "setprop sys.io.scheduler deadline; setprop vm.swappiness 20; setprop vm.dirty_ratio 10; setprop vm.dirty_background_ratio 5",
                "setprop sys.io.scheduler cfq; setprop vm.swappiness 60; setprop vm.dirty_ratio 20; setprop vm.dirty_background_ratio 10",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // 4. NETWORK, PING & PACKET TRANSMISSION
        // ═══════════════════════════════════════════════════════════
        TWEAKS.add(new TweakItem(
                "tcp_bbr_congestion_lock",
                "Google BBR TCP Congestion Algorithm Lock",
                "Locks TCP congestion algorithm to Google BBR to eliminate packet queue bufferbloat during matches",
                "setprop net.ipv4.tcp_congestion_control bbr; setprop net.ipv4.tcp_notsent_lowat 16384",
                "setprop net.ipv4.tcp_congestion_control cubic",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "tcp_packet_pacing_gaming",
                "Zero Bufferbloat Packet Pacing (Anti-Corking)",
                "Disables socket corking so game packets transmit immediately without buffering delays",
                "setprop net.ipv4.tcp_pacing_ss_ratio 200; setprop net.ipv4.tcp_pacing_ca_ratio 120; setprop net.ipv4.tcp_autocorking 0",
                "setprop net.ipv4.tcp_autocorking 1",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        TWEAKS.add(new TweakItem(
                "wlan_hardware_low_latency",
                "Wi-Fi Hardware Low-Latency Mode & Multicast Filter",
                "Enables hardware Wi-Fi low-latency chip mode, disables background scan throttling, and filters multicast discovery packets",
                "settings put global wifi_scan_throttle_enabled 0; setprop persist.vendor.wifi.low_latency 1; cmd wifi force-low-latency-mode enabled; cmd wifi set-multicast-filter enabled",
                "settings put global wifi_scan_throttle_enabled 1; cmd wifi force-low-latency-mode disabled; cmd wifi set-multicast-filter disabled",
                TweakCategory.NETWORK_LATENCY,
                true
        ));

        // ═══════════════════════════════════════════════════════════
        // 5. SHIZUKU SYSTEM, AOT SPEED & OEM THROTTLE BYPASS
        // ═══════════════════════════════════════════════════════════
        TWEAKS.add(new TweakItem(
                "oem_legal_bypass",
                "Universal OEM Game Throttling Bypass (Joyose, GOS, GPA, Dar-Link)",
                "Safely bypasses Xiaomi Joyose, Samsung GOS, OnePlus/Oppo GPA, and Transsion Dar-Link power caps",
                "pm disable-user --user 0 com.xiaomi.joyose; pm disable-user --user 0 com.samsung.android.game.gos; pm disable-user --user 0 com.oplus.games; cmd thermalservice override-status 0",
                "pm enable com.xiaomi.joyose; pm enable com.samsung.android.game.gos; pm enable com.oplus.games; cmd thermalservice override-status -1",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "aot_dexopt_speed_compile",
                "Ahead-Of-Time (AOT) Speed-Profile DEX Compilation",
                "Ahead-of-time compiles installed games with compiler speed optimizations via Shizuku to eliminate in-game JIT spikes",
                "cmd package compile -m speed -f com.mobile.legends; cmd package compile -m speed -f com.tencent.ig; cmd package compile -m speed -f com.activision.callofduty.shooter; cmd package compile -m speed -f com.dts.freefireth; cmd package compile -m speed -f com.miHoYo.GenshinImpact",
                "cmd package compile -m speed-profile -f com.mobile.legends",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "android_adpf_hub",
                "Android 12–16 ADPF & Active Standby Bucket Elevation",
                "Elevates game processes to ACTIVE standby bucket and enforces Game Mode API across Android 12, 13, 14, 15, and 16",
                "cmd game mode performance global; cmd activity set-process-limit 32; setprop persist.sys.game.fps 185; cmd power set-mode 0 1; cmd power set-mode 2 1",
                "cmd game mode standard global",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));

        TWEAKS.add(new TweakItem(
                "sqlite_wal_fast_cache",
                "SQLite In-Memory Cache Optimization",
                "Switches SQLite databases to memory journaling mode to eliminate micro-stutters during game asset streaming",
                "setprop db.log.slow_query_threshold 0; setprop debug.sqlite.wal.sync_mode 0; setprop debug.sqlite.journal_mode memory",
                "setprop debug.sqlite.wal.sync_mode 1; setprop debug.sqlite.journal_mode wal",
                TweakCategory.SHIZUKU_SYSTEM,
                true
        ));
    }

    public static List<TweakItem> getAllTweaks() {
        return Collections.unmodifiableList(TWEAKS);
    }

    public static List<TweakItem> getTweaksByCategory(TweakCategory category) {
        if (category == TweakCategory.ALL) return getAllTweaks();

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
        if (ShizukuExecutor.hasShizukuPermission()) {
            res = ShizukuExecutor.executeShizukuCommand(tweak.getApplyCommand());
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
        if (ShizukuExecutor.hasShizukuPermission()) {
            res = ShizukuExecutor.executeShizukuCommand(tweak.getRevertCommand());
        } else {
            res = CommandExecutor.executeSystemCommand(tweak.getRevertCommand());
        }

        tweak.setApplied(false);
        if (context != null) {
            TweakPreferences.saveTweakState(context, tweak.getId(), false);
        }
        return true;
    }

    public static void applyBatchAsync(Context context, List<TweakItem> tweaks, OnBatchCompleteListener listener) {
        AppExecutors.getInstance().executeCommand(() -> {
            int count = 0;
            for (TweakItem t : tweaks) {
                if (t != null && !t.isApplied()) {
                    applyTweak(context, t);
                    count++;
                }
            }
            final int finalCount = count;
            if (listener != null) {
                AppExecutors.getInstance().postToMainThread(() -> listener.onBatchComplete(finalCount));
            }
        });
    }

    public static void revertBatchAsync(Context context, List<TweakItem> tweaks, OnBatchCompleteListener listener) {
        AppExecutors.getInstance().executeCommand(() -> {
            int count = 0;
            for (TweakItem t : tweaks) {
                if (t != null && t.isApplied()) {
                    revertTweak(context, t);
                    count++;
                }
            }
            final int finalCount = count;
            if (listener != null) {
                AppExecutors.getInstance().postToMainThread(() -> listener.onBatchComplete(finalCount));
            }
        });
    }

    public static void restoreAppliedTweaksAsync(Context context) {
        if (context == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            initializeStates(context);
            for (TweakItem tweak : TWEAKS) {
                if (tweak.isApplied()) {
                    if (ShizukuExecutor.hasShizukuPermission()) {
                        ShizukuExecutor.executeShizukuCommand(tweak.getApplyCommand());
                    } else {
                        CommandExecutor.executeSystemCommand(tweak.getApplyCommand());
                    }
                }
            }
        });
    }
}
