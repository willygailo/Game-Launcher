package com.gamebooster.app.functions;

import android.content.Context;

public class PerformanceChannel {

    public enum Profile {
        EXTREME_PERFORMANCE("Extreme Performance Mode"),
        PERFORMANCE("High Performance Mode"),
        BALANCED("Balanced Game Performance");

        public final String title;
        Profile(String title) { this.title = title; }
    }

    public static boolean applyProfile(Context context, Profile profile) {
        boolean ok = true;
        switch (profile) {
            case EXTREME_PERFORMANCE:
                ok &= CpuGovernorChannel.setPerformanceLock();
                ok &= GpuTweaksChannel.setGpuMaxPerformance();
                ok &= GpuTweaksChannel.enableVulkanRenderer();
                ok &= TouchLatencyChannel.enableUltraTouchResponse();
                ok &= NetworkTweaksChannel.enableLowLatencyNetwork();
                ok &= ThermalChannel.setThermalOverride(true);
                ok &= HzFpsChannel.setRefreshRate(165.0f);
                RamZramChannel.trimMemoryAndCleanCache(context);
                return ok;

            case PERFORMANCE:
                ok &= CpuGovernorChannel.setGovernor("performance");
                ok &= GpuTweaksChannel.enableVulkanRenderer();
                ok &= TouchLatencyChannel.enableUltraTouchResponse();
                ok &= HzFpsChannel.setRefreshRate(120.0f);
                RamZramChannel.trimMemoryAndCleanCache(context);
                return ok;

            case BALANCED:
                ok &= CpuGovernorChannel.setGovernor("schedutil");
                ok &= TouchLatencyChannel.enableUltraTouchResponse();
                ok &= HzFpsChannel.setRefreshRate(90.0f);
                ok &= ThermalChannel.setThermalOverride(false);
                return ok;

            default:
                return false;
        }
    }

    public static boolean executeOneTapBoost(Context context) {
        RamZramChannel.trimMemoryAndCleanCache(context);
        CpuGovernorChannel.setPerformanceLock();
        GpuTweaksChannel.enableVulkanRenderer();
        TouchLatencyChannel.enableUltraTouchResponse();
        ThermalChannel.setThermalOverride(true);
        HzFpsChannel.setRefreshRate(120.0f);
        return true;
    }
}
