package com.gamebooster.app.functions;

import android.content.Context;

public class PerformanceChannel {

    public enum Profile {
        EXTREME_3D_FPS("Extreme 3D FPS Booster"),
        ULTRA_SMOOTH_2D("Ultra Smooth 2D Gamer"),
        BALANCED("Balanced Game Performance"),
        BATTERY_SAVER("Battery Saver Gaming");

        public final String title;
        Profile(String title) { this.title = title; }
    }

    public static boolean applyProfile(Context context, Profile profile) {
        boolean ok = true;
        switch (profile) {
            case EXTREME_3D_FPS:
                ok &= CpuGovernorChannel.setPerformanceLock();
                ok &= GpuTweaksChannel.setGpuMaxPerformance();
                ok &= TouchLatencyChannel.enableUltraTouchResponse();
                ok &= NetworkTweaksChannel.enableLowLatencyNetwork();
                ok &= ThermalChannel.setThermalOverride(true);
                ok &= HzFpsChannel.setRefreshRate(120.0f);
                RamZramChannel.trimMemoryAndCleanCache(context);
                return ok;

            case ULTRA_SMOOTH_2D:
                ok &= CpuGovernorChannel.setGovernor("performance");
                ok &= TouchLatencyChannel.enableUltraTouchResponse();
                ok &= HzFpsChannel.setRefreshRate(90.0f);
                RamZramChannel.trimMemoryAndCleanCache(context);
                return ok;

            case BALANCED:
                ok &= CpuGovernorChannel.setGovernor("schedutil");
                ok &= TouchLatencyChannel.enableUltraTouchResponse();
                ok &= HzFpsChannel.setRefreshRate(90.0f);
                ok &= ThermalChannel.setThermalOverride(false);
                return ok;

            case BATTERY_SAVER:
                ok &= CpuGovernorChannel.setGovernor("powersave");
                ok &= ThermalChannel.setThermalOverride(false);
                ok &= HzFpsChannel.setRefreshRate(60.0f);
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
