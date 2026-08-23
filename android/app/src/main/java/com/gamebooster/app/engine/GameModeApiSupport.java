package com.gamebooster.app.engine;

import android.os.Build;

/**
 * Phase 2.1 — SDK gates for privileged GameMode shell commands.
 *
 * <p>The AIDL/Shizuku commands issued during Tier 1 were historically attempted
 * on every device (minSdk 24). On older Android versions several of them fail
 * silently or print to stderr yet were still counted as "applied". This class
 * centralizes the API-level knowledge: one recommendation gate (Android 14+,
 * the full command set) plus the per-command minimums for callers that want
 * granular behavior. All sdk-parameterized overloads are pure and unit-tested.
 */
public final class GameModeApiSupport {

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
     * Applies full modern Android 13, 14, 15, and 16 system flags for maximum performance unlocking.
     */
    public static void applyModernAndroidPerformanceFlags(String packageName, int targetFps) {
        int fps = targetFps > 0 ? targetFps : 185;
        java.util.List<String> commands = new java.util.ArrayList<>();

        // 1. Android 11-16 Power & Fixed Performance Clocks
        commands.add("cmd power set-fixed-performance-mode-enabled true");
        commands.add("cmd power set-mode 0 1");
        commands.add("cmd power set-mode 2 1");

        // 2. Android 13-16 DeviceConfig Persistent Override (Prevents sync reversion)
        commands.add("cmd device_config set_sync_disabled_for_tests persistent");
        commands.add("device_config put runtime_native_boot use_app_image_startup_cache true");
        commands.add("device_config put runtime_native_boot pin_app_image_startup_cache true");
        commands.add("device_config put runtime_native_boot boost_sched_priority true");

        // 3. Android 12-16 Phantom Process Killer & Background Freezer Bypass
        commands.add("device_config put activity_manager max_phantom_processes 2147483647");
        commands.add("settings put global settings_enable_monitor_phantom_procs false");
        commands.add("settings put global cached_apps_freezer enabled");

        // 4. Android 13-16 ADPF & SurfaceFlinger Phase Acceleration
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

        // 5. Per-Game Intervention (Android 13, 14, 15, 16)
        if (packageName != null && !packageName.trim().isEmpty()) {
            String pkg = packageName.trim();
            commands.add("cmd game mode performance " + pkg);
            commands.add("cmd game mode 2 " + pkg);
            commands.add("cmd game set --fps " + fps + " " + pkg);
            commands.add("cmd window set-app-refresh-rate " + pkg + " " + fps);
            commands.add("device_config put game_overlay " + pkg + " mode=2,useAngle=true,fps=" + fps + ",downscaleFactor=1.0,cpuPriority=high,gpuPriority=high:mode=3,useAngle=true,fps=" + fps + ",downscaleFactor=1.0,cpuPriority=high,gpuPriority=high");
        } else {
            commands.add("cmd game mode performance global");
            commands.add("cmd game set --fps " + fps + " global");
            commands.add("cmd window set-app-refresh-rate global " + fps);
            commands.add("device_config put game_overlay global mode=2,fps=" + fps + ":mode=3,fps=" + fps);
        }

        for (String cmd : commands) {
            com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(cmd);
        }
    }
}