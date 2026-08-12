package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import android.content.Context;

import com.gamebooster.app.device.DevicePerformanceCapabilities;

public class PerformanceChannel {

    public enum Profile {
        EXTREME_PERFORMANCE("Extreme Performance Mode"),
        PERFORMANCE("High Performance Mode"),
        BALANCED("Balanced Game Performance");

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

    /** Applies the strongest refresh rate that is physically supported by this device. */
    public static ProfileResult applyProfileWithResult(Context context, Profile profile) {
        if (context == null) return new ProfileResult(false, 0, "Device context is unavailable");

        DevicePerformanceCapabilities caps = DevicePerformanceCapabilities.detect(context);
        int targetHz;
        switch (profile) {
            case EXTREME_PERFORMANCE:
                targetHz = caps.resolveRefreshRate(165);
                break;

            case PERFORMANCE:
                targetHz = caps.resolveRefreshRate(144);
                break;

            case BALANCED:
                targetHz = caps.resolveRefreshRate(90);
                break;

            default:
                return new ProfileResult(false, 0, "Unknown performance profile");
        }

        HzFpsChannel.RefreshRateResult refreshResult;
        refreshResult = HzFpsChannel.setRefreshRate(context, targetHz);
        if (!refreshResult.success) {
            return new ProfileResult(false, targetHz, refreshResult.message);
        }

        // Thermal protection remains enabled. It prevents heat-related frame drops in longer sessions.
        if (profile == Profile.EXTREME_PERFORMANCE || profile == Profile.PERFORMANCE) {
            CpuGovernorChannel.setPerformanceLock();
            GpuTweaksChannel.enableVulkanRenderer();
            TouchLatencyChannel.enableUltraTouchResponse();
            NetworkTweaksChannel.enableLowLatencyNetwork();
            RamZramChannel.trimMemoryAndCleanCache(context);
            // Root-only tuning is intentionally not auto-executed. It must be an explicit,
            // device-specific action because kernel paths and thermal policies are OEM-specific.
        } else {
            CpuGovernorChannel.setGovernor("schedutil");
            TouchLatencyChannel.enableUltraTouchResponse();
        }
        return new ProfileResult(true, targetHz, refreshResult.message + " • Thermal protection stays active");
    }

    /** Writes and executes a root shell script to apply the maximum boost at 165Hz. */
    public static boolean writeAndExecuteRootTweaksScript() {
        return writeAndExecuteRootTweaksScript(165);
    }

    /**
     * Writes and executes a root shell script via Shizuku for the specified target Hz.
     * Uses the actual {@code targetHz} parameter — no longer hardcoded to 165.
     *
     * @param targetHz Target refresh rate written into the script (120, 144, or 165)
     */
    public static boolean writeAndExecuteRootTweaksScript(int targetHz) {
        try {
            String scriptPath = "/data/local/tmp/gamebooster_tweaks.sh";
            String scriptContent = "#!/system/bin/sh\\n" +
                    "sync; echo 3 > /proc/sys/vm/drop_caches\\n" +
                    "setprop debug.sf.hw 1\\n" +
                    "setprop debug.hwui.renderer vulkan\\n" +
                    "setprop debug.renderengine.backend vulkan\\n" +
                    "setprop debug.sf.early_app_phase_offset_ns 500000\\n" +
                    "setprop debug.sf.fps_limit " + targetHz + "\\n" +
                    "setprop persist.sys.NV_FPSLIMIT " + targetHz + "\\n" +
                    "setprop persist.sys.NV_POWERMODE 1\\n" +
                    "service call SurfaceFlinger 1035 i32 " + targetHz + "\\n" +
                    "service call SurfaceFlinger 1036 i32 " + targetHz + "\\n" +
                    "cmd power set-mode 0 1\\n" +
                    "cmd power set-mode 2 1\\n" +
                    "cmd thermalservice override-status 0\\n";

            String cmd = String.format("printf '%s' > %s && chmod 755 %s && sh %s",
                    scriptContent, scriptPath, scriptPath, scriptPath);
            String res = com.gamebooster.app.engine.CommandExecutor.executeSystemCommand(cmd);
            return com.gamebooster.app.engine.CommandExecutor.isSuccessOutput(res);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean setGpuRenderMode(boolean is3D) {
        if (is3D) {
            boolean ok = GpuTweaksChannel.enableVulkanRenderer();
            ok &= com.gamebooster.app.engine.CommandExecutor.setSystemProperty("debug.sf.hw", "1");
            return ok;
        } else {
            boolean ok = com.gamebooster.app.engine.CommandExecutor.setSystemProperty("debug.hwui.renderer", "skia");
            ok &= com.gamebooster.app.engine.CommandExecutor.setSystemProperty("debug.sf.hw", "0");
            return ok;
        }
    }

    public static boolean executeOneTapBoost(Context context) {
        return applyProfile(context, Profile.PERFORMANCE);
    }

    public static final String PREFS_PERF_STATE  = "perf_state";
    public static final String PREFS_KEY_MAX_PERF_LOCKED = "max_perf_locked";

    /**
     * Locks maximum extreme performance profile (Pinaka-Taas kung kaya).
     */
    public static ProfileResult lockMaxPerformance(Context context) {
        ProfileResult result = applyProfileWithResult(context, Profile.EXTREME_PERFORMANCE);
        if (context != null) {
            try {
                context.getApplicationContext()
                       .getSharedPreferences(PREFS_PERF_STATE, Context.MODE_PRIVATE)
                       .edit()
                       .putBoolean(PREFS_KEY_MAX_PERF_LOCKED, true)
                       .apply();
            } catch (Throwable ignored) {}
        }
        return result;
    }

    /**
     * Unlocks maximum performance mode and restores balanced state.
     */
    public static boolean unlockMaxPerformance(Context context) {
        if (context != null) {
            try {
                context.getApplicationContext()
                       .getSharedPreferences(PREFS_PERF_STATE, Context.MODE_PRIVATE)
                       .edit()
                       .putBoolean(PREFS_KEY_MAX_PERF_LOCKED, false)
                       .apply();
                CpuGovernorChannel.setGovernor("schedutil");
                return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    public static boolean isMaxPerformanceLocked(Context context) {
        if (context == null) return false;
        try {
            return context.getApplicationContext()
                          .getSharedPreferences(PREFS_PERF_STATE, Context.MODE_PRIVATE)
                          .getBoolean(PREFS_KEY_MAX_PERF_LOCKED, false);
        } catch (Throwable t) {
            return false;
        }
    }
}
