package com.gamebooster.app.core;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single source of truth for performance choices that can safely be offered on
 * the current device. A requested rate is never allowed to exceed a physical
 * display mode reported by Android.
 */
public final class DevicePerformanceCapabilities {

    public enum OemFamily {
        TRANSSION, XIAOMI, SAMSUNG, OPPO_FAMILY, VIVO_IQOO, ASUS, MOTOROLA, GOOGLE, GENERIC
    }

    private final List<Integer> supportedRefreshRates;
    private final int maxRefreshRate;
    private final OemFamily oemFamily;

    private DevicePerformanceCapabilities(List<Integer> rates, OemFamily family) {
        List<Integer> sorted = new ArrayList<>(rates);
        if (sorted.isEmpty()) sorted.add(60);
        Collections.sort(sorted);
        this.supportedRefreshRates = Collections.unmodifiableList(sorted);
        this.maxRefreshRate = sorted.get(sorted.size() - 1);
        this.oemFamily = family;
    }

    public static DevicePerformanceCapabilities detect(Context context) {
        DisplayCapabilitiesDetector.DisplayCaps caps = DisplayCapabilitiesDetector.detect(context);
        return new DevicePerformanceCapabilities(caps.getRecommendedRates(), detectOemFamily());
    }

    public List<Integer> getSupportedRefreshRates() {
        return supportedRefreshRates;
    }

    public int getMaxRefreshRate() {
        return maxRefreshRate;
    }

    public OemFamily getOemFamily() {
        return oemFamily;
    }

    public boolean supportsRefreshRate(int hz) {
        return supportedRefreshRates.contains(hz);
    }

    /** Returns the highest physical refresh rate that does not exceed the request. */
    public int resolveRefreshRate(int requestedHz) {
        int resolved = supportedRefreshRates.get(0);
        for (int rate : supportedRefreshRates) {
            if (rate > requestedHz) break;
            resolved = rate;
        }
        return resolved;
    }

    public String getCompatibilitySummary() {
        return oemFamily.name() + " device: " + maxRefreshRate + "Hz max (supported: "
                + supportedRefreshRates + ")";
    }

    private static OemFamily detectOemFamily() {
        String manufacturer = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.toLowerCase();
        String brand = Build.BRAND == null ? "" : Build.BRAND.toLowerCase();
        String identity = manufacturer + " " + brand;
        if (identity.contains("infinix") || identity.contains("tecno") || identity.contains("itel")
                || identity.contains("transsion")) return OemFamily.TRANSSION;
        if (identity.contains("xiaomi") || identity.contains("redmi") || identity.contains("poco")) return OemFamily.XIAOMI;
        if (identity.contains("samsung")) return OemFamily.SAMSUNG;
        if (identity.contains("oppo") || identity.contains("realme") || identity.contains("oneplus")) return OemFamily.OPPO_FAMILY;
        if (identity.contains("vivo") || identity.contains("iqoo")) return OemFamily.VIVO_IQOO;
        if (identity.contains("asus")) return OemFamily.ASUS;
        if (identity.contains("motorola") || identity.contains("moto")) return OemFamily.MOTOROLA;
        if (identity.contains("google") || identity.contains("pixel")) return OemFamily.GOOGLE;
        return OemFamily.GENERIC;
    }
}
