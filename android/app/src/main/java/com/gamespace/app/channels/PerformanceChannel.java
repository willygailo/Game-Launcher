package com.gamespace.app.channels;

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
        switch (profile) {
            case EXTREME_3D_FPS:
                CpuGovernorChannel.setPerformanceLock();
                GpuTweaksChannel.setGpuMaxPerformance();
                TouchLatencyChannel.enableUltraTouchResponse();
                NetworkTweaksChannel.enableLowLatencyNetwork();
                ThermalChannel.setThermalOverride(true);
                HzFpsChannel.setRefreshRate(120.0f);
                RamZramChannel.trimMemoryAndCleanCache(context);
                return true;

            case ULTRA_SMOOTH_2D:
                CpuGovernorChannel.setGovernor("performance");
                TouchLatencyChannel.enableUltraTouchResponse();
                HzFpsChannel.setRefreshRate(90.0f);
                RamZramChannel.trimMemoryAndCleanCache(context);
                return true;

            case BALANCED:
                CpuGovernorChannel.setGovernor("schedutil");
                TouchLatencyChannel.enableUltraTouchResponse();
                HzFpsChannel.setRefreshRate(90.0f);
                ThermalChannel.setThermalOverride(false);
                return true;

            case BATTERY_SAVER:
                CpuGovernorChannel.setGovernor("powersave");
                ThermalChannel.setThermalOverride(false);
                HzFpsChannel.setRefreshRate(60.0f);
                return true;

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
