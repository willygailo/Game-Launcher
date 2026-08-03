package com.gamebooster.app.booster;

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
                targetHz = caps.getMaxRefreshRate();
                break;

            case PERFORMANCE:
                targetHz = caps.resolveRefreshRate(120);
                break;

            case BALANCED:
                targetHz = caps.resolveRefreshRate(90);
                break;

            default:
                return new ProfileResult(false, 0, "Unknown performance profile");
        }

        HzFpsChannel.RefreshRateResult refreshResult = HzFpsChannel.setRefreshRate(context, targetHz);
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
        } else {
            CpuGovernorChannel.setGovernor("schedutil");
            TouchLatencyChannel.enableUltraTouchResponse();
        }
        return new ProfileResult(true, targetHz, refreshResult.message + " • Thermal protection stays active");
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
}
