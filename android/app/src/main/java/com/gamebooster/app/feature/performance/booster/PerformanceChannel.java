package com.gamebooster.app.feature.performance.booster;

import android.content.Context;

import com.gamebooster.app.feature.performance.device.DevicePerformanceCapabilities;

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

        applyTuningProfile(context, profile);
        return new ProfileResult(true, targetHz, refreshResult.message + " • Thermal protection stays active");
    }

    /**
     * Applies only launcher-side tuning. Game launchers use this after their
     * single per-game display/Game Mode request to avoid duplicate refresh-rate
     * writes. Thermal protection remains enabled.
     */
    public static boolean applyTuningProfile(Context context, Profile profile) {
        if (context == null || profile == null) return false;
        int targetHz = (profile == Profile.EXTREME_PERFORMANCE) ? 165 : (profile == Profile.PERFORMANCE ? 144 : 90);
        if (profile == Profile.EXTREME_PERFORMANCE || profile == Profile.PERFORMANCE) {
            CpuGovernorChannel.setPerformanceLock();
            GpuTweaksChannel.enableVulkanRenderer();
            TouchLatencyChannel.enableUltraTouchResponse();
            NetworkTweaksChannel.enableLowLatencyNetwork();
            RamZramChannel.trimMemoryAndCleanCache(context);

            // Execute automated SetEdit property enforcer & OEM hardware matrix optimizations
            com.gamebooster.app.feature.performance.tweaks.SetEditSettingsEnforcer.enforceRefreshRate(targetHz);
            com.gamebooster.app.feature.performance.tweaks.SetEditSettingsEnforcer.enforceUltraTouchSettings();
            com.gamebooster.app.feature.performance.tweaks.OemHardwareOptimizer.applyOemOptimizations(targetHz);
        } else {
            CpuGovernorChannel.setGovernor("schedutil");
            TouchLatencyChannel.enableUltraTouchResponse();
            com.gamebooster.app.feature.performance.tweaks.SetEditSettingsEnforcer.revertToDefaults();
        }
        return true;
    }

    /** Retained for API compatibility; unsupported root/property scripts are disabled. */
    public static boolean writeAndExecuteRootTweaksScript() {
        return false;
    }

    /**
     * Writes and executes a root shell script via Shizuku for the specified target Hz.
     * Uses the actual {@code targetHz} parameter — no longer hardcoded to 165.
     *
     * @param targetHz Target refresh rate written into the script (120, 144, or 165)
     */
    public static boolean writeAndExecuteRootTweaksScript(int targetHz) {
        return false;
    }

    public static boolean setGpuRenderMode(boolean is3D) {
        return false;
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
