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
                int savedHz = com.gamebooster.app.games.GameProfileAutoConfigurator.getTargetFpsHz(context);
                float extremeHz = (savedHz >= 144) ? (float) savedHz : 165.0f;
                ok &= HzFpsChannel.setRefreshRate(extremeHz);
                CpuGovernorChannel.setPerformanceLock();
                GpuTweaksChannel.setGpuMaxPerformance();
                GpuTweaksChannel.enableVulkanRenderer();
                TouchLatencyChannel.enableUltraTouchResponse();
                NetworkTweaksChannel.enableLowLatencyNetwork();
                ThermalChannel.setThermalOverride(true);
                RamZramChannel.trimMemoryAndCleanCache(context);
                return true;

            case PERFORMANCE:
                ok &= HzFpsChannel.setRefreshRate(120.0f);
                CpuGovernorChannel.setGovernor("performance");
                GpuTweaksChannel.enableVulkanRenderer();
                TouchLatencyChannel.enableUltraTouchResponse();
                RamZramChannel.trimMemoryAndCleanCache(context);
                return true;

            case BALANCED:
                ok &= HzFpsChannel.setRefreshRate(90.0f);
                CpuGovernorChannel.setGovernor("schedutil");
                TouchLatencyChannel.enableUltraTouchResponse();
                ThermalChannel.setThermalOverride(false);
                return true;

            default:
                return false;
        }
    }

    public static boolean setGpuRenderMode(boolean is3D) {
        if (is3D) {
            boolean ok = GpuTweaksChannel.enableVulkanRenderer();
            ok &= com.gamebooster.app.root.CommandExecutor.setSystemProperty("debug.sf.hw", "1");
            return ok;
        } else {
            boolean ok = com.gamebooster.app.root.CommandExecutor.setSystemProperty("debug.hwui.renderer", "skia");
            ok &= com.gamebooster.app.root.CommandExecutor.setSystemProperty("debug.sf.hw", "0");
            return ok;
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
