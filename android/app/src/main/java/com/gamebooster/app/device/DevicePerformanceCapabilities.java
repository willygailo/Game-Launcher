package com.gamebooster.app.device;

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
    private final int currentRefreshRate;
    private final OemFamily oemFamily;

    private DevicePerformanceCapabilities(List<Integer> rates, int currentRefreshRate, OemFamily family) {
        List<Integer> sorted = new ArrayList<>(rates);
        if (sorted.isEmpty()) sorted.add(60);
        Collections.sort(sorted);
        this.supportedRefreshRates = Collections.unmodifiableList(sorted);
        this.maxRefreshRate = sorted.get(sorted.size() - 1);
        this.currentRefreshRate = currentRefreshRate > 0 ? currentRefreshRate : sorted.get(0);
        this.oemFamily = family;
    }

    public static DevicePerformanceCapabilities detect(Context context) {
        DisplayCapabilitiesDetector.DisplayCaps caps = DisplayCapabilitiesDetector.detect(context);
        return new DevicePerformanceCapabilities(caps.getRecommendedRates(), caps.currentRefreshRate, detectOemFamily());
    }

    public List<Integer> getSupportedRefreshRates() {
        return supportedRefreshRates;
    }

    public int getMaxRefreshRate() {
        return maxRefreshRate;
    }

    public int getCurrentRefreshRate() {
        return currentRefreshRate;
    }

    public OemFamily getOemFamily() {
        return oemFamily;
    }

    public boolean supportsRefreshRate(int hz) {
        return supportedRefreshRates.contains(hz) || hz == 185 || hz == 165 || hz == 144 || hz == 120 || hz == maxRefreshRate;
    }

    /** Returns the target refresh rate (185Hz max target or highest requested rate). */
    public int resolveRefreshRate(int requestedHz) {
        if (requestedHz >= 185) {
            return 185;
        } else if (requestedHz >= 165) {
            return 165;
        } else if (requestedHz >= 144) {
            return 144;
        } else if (requestedHz >= 120) {
            return 120;
        }
        int resolved = supportedRefreshRates.get(0);
        for (int rate : supportedRefreshRates) {
            if (rate > requestedHz) break;
            resolved = rate;
        }
        return resolved;
    }

    public String getCompatibilitySummary() {
        return getOemFamilyLabel() + " device: " + maxRefreshRate + "Hz max (supported: "
                + supportedRefreshRates + ")";
    }

    public String getOemFamilyLabel() {
        switch (oemFamily) {
            case TRANSSION: return "Transsion (Infinix / Tecno / itel)";
            case XIAOMI: return "Xiaomi / Redmi / POCO";
            case SAMSUNG: return "Samsung";
            case OPPO_FAMILY: return "OPPO / realme / OnePlus";
            case VIVO_IQOO: return "vivo / iQOO";
            case ASUS: return "ASUS";
            case MOTOROLA: return "Motorola";
            case GOOGLE: return "Google Pixel";
            default: return "Generic Android";
        }
    }

    public String getRecommendedProfileLabel() {
        if (maxRefreshRate >= 120) return "MAX SUPPORTED";
        if (maxRefreshRate >= 90) return "COMPETITIVE";
        return "BALANCED";
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
