package com.gamebooster.app.engine;

import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * Phase 2.1 — SDK gates for privileged GameMode shell commands.
 *
 * <p>The AIDL/Shizuku commands issued during Tier 1 are gated by Android version:
 * one recommendation gate (Android 14+, the full command set) plus per-command minimums.
 * All sdk-parameterized overloads are pure and unit-tested.
 */
public final class GameModeApiSupport {

    private static final String TAG = "GameModeApiSupport";

    /** GameManager service (games framework) — Android 12. */
    public static final int MIN_GAME_MODE_API = 31;

    /** `device_config put game_overlay` namespace — Android 13. */
    public static final int MIN_GAME_OVERLAY_API = 33;

    /** `cmd game set --fps` and `cmd window set-app-refresh-rate` — Android 14. */
    public static final int MIN_APP_REFRESH_RATE_API = 34;

    /** Android 15 (Vanilla Ice Cream) API 35. */
    public static final int ANDROID_15_API = 35;

    /** Android 16 (Baklava) API 36+. */
    public static final int ANDROID_16_API = 36;

    private GameModeApiSupport() {
    }

    /** True when the full privileged GameMode command set is usable (Android 14+). */
    public static boolean isAvailable() {
        return isAvailable(Build.VERSION.SDK_INT);
    }

    /** Pure: full command set (game mode + fps override + overlay + refresh rate). */
    public static boolean isAvailable(int sdk) {
        return sdk >= MIN_APP_REFRESH_RATE_API;
    }

    /** `cmd game mode performance` (GameManager game modes). */
    public static boolean isGameModeApiAvailable(int sdk) {
        return sdk >= MIN_GAME_MODE_API;
    }

    /** `device_config put game_overlay` namespace. */
    public static boolean isGameOverlayApiAvailable(int sdk) {
        return sdk >= MIN_GAME_OVERLAY_API;
    }

    /** `cmd game set --fps` FPS override. */
    public static boolean isGameFpsOverrideAvailable(int sdk) {
        return sdk >= MIN_APP_REFRESH_RATE_API;
    }

    /** `cmd window set-app-refresh-rate`. */
    public static boolean isAppRefreshRateApiAvailable(int sdk) {
        return sdk >= MIN_APP_REFRESH_RATE_API;
    }

    /** Android 15 (API 35+) performance and battery constraints bypass. */
    public static boolean isAndroid15Api(int sdk) {
        return sdk >= ANDROID_15_API;
    }

    /** Android 16 (API 36+) performance class 3 extreme mode. */
    public static boolean isAndroid16Api(int sdk) {
        return sdk >= ANDROID_16_API;
    }

    /** Forces Android Game Mode Performance via Shizuku shell. */
    public static void setGameModePerformance(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        String pkg = packageName.trim();
        com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommands(
                "cmd game mode performance " + pkg,
                "cmd game mode 2 " + pkg,
                "cmd game set --fps 185 " + pkg,
                "cmd window set-app-refresh-rate " + pkg + " 185",
                "device_config put game_overlay " + pkg + " mode=2,useAngle=true,fps=185,downscaleFactor=1.0,cpuPriority=high,gpuPriority=high:mode=3,useAngle=true,fps=185,downscaleFactor=1.0,cpuPriority=high,gpuPriority=high"
        );
    }

    /**
     * Applies performance mode with automatic legal Android SDK fallback when Shizuku is absent.
     */
    public static boolean setGameModePerformanceWithFallback(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.trim();

        // 1. Legal native framework reflection
        boolean nativeOk = NativeFrameworkBridge.setGameModePerformance(context, pkg);

        // 2. Elevated shell if Shizuku is active
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            setGameModePerformance(pkg);
            return true;
        }

        return nativeOk;
    }

    /**
     * Applies full modern Android 13, 14, 15, and 16 system flags for maximum performance unlocking.
     */
    public static void applyModernAndroidPerformanceFlags(String packageName, int targetFps) {
        int fps = targetFps > 0 ? targetFps : 185;
        java.util.List<String> commands = new java.util.ArrayList<>();

        // 1. Android 11-16 Power & Fixed Performance Clocks
        commands.add("cmd power set-fixed-performance-mode-enabled true");
        commands.add("cmd power set-mode 0 1");
        commands.add("cmd power set-mode 2 1");

        // 2. Zero-Latency Animation Scale (0.0x Instant)
        commands.add("settings put global window_animation_scale 0.0");
        commands.add("settings put global transition_animation_scale 0.0");
        commands.add("settings put global animator_duration_scale 0.0");
        commands.add("settings put system window_animation_scale 0.0");
        commands.add("settings put system transition_animation_scale 0.0");
        commands.add("settings put system animator_duration_scale 0.0");
        commands.add("cmd activity update-configuration --anim-scale 0.0");

        // 3. JVM / Dalvik Runtime Performance Flags
        commands.add("setprop dalvik.vm.execution-mode int:jit");
        commands.add("setprop dalvik.vm.usejit true");
        commands.add("setprop dalvik.vm.usejitprofiles true");
        commands.add("setprop dalvik.vm.heapgrowthlimit 512m");
        commands.add("setprop dalvik.vm.heapsize 1024m");
        commands.add("setprop dalvik.vm.heaptargetutilization 0.75");
        commands.add("setprop dalvik.vm.jitthreshold 100");
        commands.add("setprop dalvik.vm.dex2oat-filter speed");
        commands.add("setprop pm.dexopt.boot speed-profile");
        commands.add("setprop pm.dexopt.install speed");
        commands.add("setprop pm.dexopt.bg-dexopt speed");

        // 4. Multi-Core CPU Scheduling & uclamp Boost (8/12/16+ cores)
        commands.add("setprop sys.games.cpu_affinity 1");
        commands.add("setprop sys.use_fifo 1");
        commands.add("setprop sys.perf.sched_uclamp_min 1024");
        commands.add("setprop sys.perf.sched_uclamp_min_rt 1024");
        commands.add("setprop sys.perf.sched_min_granularity_ns 250000");
        commands.add("setprop sys.perf.sched_latency_ns 1000000");
        commands.add("setprop sys.perf.sched_boost 1");

        // 5. Android 13-16 DeviceConfig Persistent Override (Prevents sync reversion)
        commands.add("cmd device_config set_sync_disabled_for_tests persistent");
        commands.add("device_config put runtime_native_boot use_app_image_startup_cache true");
        commands.add("device_config put runtime_native_boot pin_app_image_startup_cache true");
        commands.add("device_config put runtime_native_boot boost_sched_priority true");

        // 6. Android 12-16 Phantom Process Killer & Background Freezer Bypass
        commands.add("device_config put activity_manager max_phantom_processes 2147483647");
        commands.add("settings put global settings_enable_monitor_phantom_procs false");
        commands.add("settings put global cached_apps_freezer enabled");

        // 7. Android 13-16 ADPF & SurfaceFlinger Phase Acceleration
        commands.add("setprop debug.sf.enable_adpf_cpu_hint true");
        commands.add("setprop debug.hwui.use_hint_manager true");
        commands.add("setprop persist.sys.adpf.enable 1");
        commands.add("setprop persist.sys.adpf.mode 1");
        commands.add("setprop debug.adpf.hint.enabled 1");
        commands.add("setprop debug.adpf.cpu.boost 1");
        commands.add("setprop debug.adpf.gpu.boost 1");
        commands.add("setprop debug.sf.enable_gl_backpressure 0");
        commands.add("setprop debug.sf.predict_hwc_composition_strategy 1");
        commands.add("setprop debug.sf.high_fps_late_app_phase_offset_ns 0");
        commands.add("setprop debug.sf.high_fps_late_sf_phase_offset_ns 0");
        commands.add("setprop debug.sf.high_fps_early_phase_offset_ns 0");
        commands.add("setprop debug.sf.high_fps_early_gl_phase_offset_ns 0");

        // 8. Targeted Application Opt-in (Target Game ONLY)
        if (packageName != null && !packageName.trim().isEmpty()) {
            String pkg = packageName.trim();
            commands.add("settings put global game_driver_all_apps 0");
            commands.add("settings put global updatable_driver_all_apps 0");
            commands.add("settings put global game_driver_opt_in_apps " + pkg);
            commands.add("settings put global updatable_driver_production_opt_in_apps " + pkg);
            commands.add("cmd game mode performance " + pkg);
            commands.add("cmd game mode 2 " + pkg);
            commands.add("cmd game set --fps " + fps + " " + pkg);
            commands.add("cmd window set-app-refresh-rate " + pkg + " " + fps);
            commands.add("cmd package compile -m speed -f " + pkg);
            commands.add("device_config put game_overlay " + pkg + " mode=2,useAngle=true,fps=" + fps + ",downscaleFactor=1.0,cpuPriority=high,gpuPriority=high:mode=3,useAngle=true,fps=" + fps + ",downscaleFactor=1.0,cpuPriority=high,gpuPriority=high");
        } else {
            String targetCsv = com.gamebooster.app.booster.GpuTweaksChannel.getTargetGamesCsv();
            commands.add("settings put global game_driver_all_apps 0");
            commands.add("settings put global updatable_driver_all_apps 0");
            commands.add("settings put global game_driver_opt_in_apps " + targetCsv);
            commands.add("settings put global updatable_driver_production_opt_in_apps " + targetCsv);
            commands.add("cmd game mode performance global");
            commands.add("cmd game set --fps " + fps + " global");
            commands.add("cmd window set-app-refresh-rate global " + fps);
            commands.add("device_config put game_overlay global mode=2,fps=" + fps + ":mode=3,fps=" + fps);
        }

        for (String cmd : commands) {
            com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(cmd);
        }

        // Second pass: apply SDK-gated commands (Android 12-16) on top of the base set
        applyForCurrentSdk(packageName, fps);
    }

    /**
     * Applies performance flags strictly validated for the current device's Android SDK level.
     */
    public static void applyForCurrentSdk(String packageName, int targetFps) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        int fps = targetFps > 0 ? targetFps : 185;
        String pkg = packageName != null ? packageName.trim() : "";

        java.util.List<String> cmds = new java.util.ArrayList<>();

        if (sdk >= MIN_GAME_MODE_API && !pkg.isEmpty()) {
            cmds.add("cmd game mode performance " + pkg + " 2>/dev/null");
        }
        if (sdk >= MIN_GAME_OVERLAY_API && !pkg.isEmpty()) {
            cmds.add("device_config put game_overlay " + pkg + " mode=2,useAngle=true,fps=" + fps + ",downscaleFactor=1.0,cpuPriority=high,gpuPriority=high 2>/dev/null");
        }
        if (sdk >= MIN_APP_REFRESH_RATE_API && !pkg.isEmpty()) {
            cmds.add("cmd game set --fps " + fps + " " + pkg + " 2>/dev/null");
            cmds.add("cmd window set-app-refresh-rate " + pkg + " " + fps + " 2>/dev/null");
        }
        if (sdk >= ANDROID_15_API) {
            cmds.add("cmd power set-fixed-performance-mode-enabled true 2>/dev/null");
            // Android 15: disable battery-mode constraints for game package
            if (!pkg.isEmpty()) {
                cmds.add("cmd game set --battery-mode 0 " + pkg + " 2>/dev/null");
                // Android 15: prevent Game Mode from reducing render resolution
                cmds.add("cmd game set --allow-downscale false " + pkg + " 2>/dev/null");
            }
        }
        if (sdk >= ANDROID_16_API && !pkg.isEmpty()) {
            // Android 16: set game to EXTREME performance class (highest tier)
            cmds.add("cmd game set --performance-class 3 " + pkg + " 2>/dev/null");
        }

        // Android 13+ game_manager device_config flags
        if (sdk >= MIN_GAME_OVERLAY_API) {
            // Disable power throttling inside GameManager — lets GPU/CPU stay at ceiling
            cmds.add("device_config put game_manager game_power_optimization_enabled false 2>/dev/null");
            // Disable battery optimization inside GameManager sessions
            cmds.add("device_config put game_manager game_battery_optimization_enabled false 2>/dev/null");
            // Instant game mode switching (no delay between standard → performance mode)
            cmds.add("device_config put game_manager max_game_mode_switching_delay_ms 0 2>/dev/null");
            // Keep up to 256 processes cached — prevents GC jank on game re-entry
            cmds.add("device_config put activity_manager max_cached_processes 256 2>/dev/null");
        }

        for (String cmd : cmds) {
            com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(cmd);
        }
    }

    /**
     * Global FPS unlock applied system-wide.
     */
    public static void applyGlobalFpsUnlock(int targetFps) {
        int fps = targetFps > 0 ? targetFps : 185;
        applyModernAndroidPerformanceFlags(null, fps);
    }
}