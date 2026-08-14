package com.gamebooster.app.feature.performance.device;

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
        INFINIX,
        TECNO,
        TRANSSION,
        XIAOMI,
        SAMSUNG,
        OPPO_FAMILY,
        VIVO_IQOO,
        ASUS_ROG,
        NUBIA_REDMAGIC,
        HONOR_HUAWEI,
        BLACK_SHARK,
        MOTOROLA,
        GOOGLE,
        GENERIC
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
        if (hz == 60 || hz == 90 || hz == 120 || hz == 144 || hz == 165 || hz == 240) return true;
        return supportedRefreshRates.contains(hz);
    }

    /** Returns the requested rate when valid, allowing unclamped 144Hz/165Hz overrides. */
    public int resolveRefreshRate(int requestedHz) {
        if (requestedHz <= 0) return maxRefreshRate > 0 ? maxRefreshRate : 120;
        // If the user or profile explicitly requests 144Hz or 165Hz, honor the request
        if (requestedHz == 144 || requestedHz == 165 || requestedHz == 240) {
            return requestedHz;
        }
        if (supportedRefreshRates.contains(requestedHz)) {
            return requestedHz;
        }
        int resolved = supportedRefreshRates.get(0);
        for (int rate : supportedRefreshRates) {
            if (rate > requestedHz) break;
            resolved = rate;
        }
        return Math.max(resolved, requestedHz);
    }

    public String getCompatibilitySummary() {
        return getOemFamilyLabel() + " device: " + maxRefreshRate + "Hz max (supported: "
                + supportedRefreshRates + ")";
    }

    public String getOemFamilyLabel() {
        switch (oemFamily) {
            case INFINIX: return "Infinix XOS";
            case TECNO: return "Tecno HiOS";
            case TRANSSION: return "Transsion (itel)";
            case XIAOMI: return "Xiaomi / Redmi / POCO";
            case SAMSUNG: return "Samsung Galaxy";
            case OPPO_FAMILY: return "OPPO / realme / OnePlus";
            case VIVO_IQOO: return "vivo / iQOO";
            case ASUS_ROG: return "ASUS ROG Phone";
            case NUBIA_REDMAGIC: return "Nubia REDMAGIC";
            case HONOR_HUAWEI: return "Honor MagicOS / Huawei EMUI";
            case BLACK_SHARK: return "Black Shark";
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

        if (manufacturer.contains("infinix") || brand.contains("infinix")) {
            return OemFamily.INFINIX;
        }
        if (manufacturer.contains("tecno") || brand.contains("tecno")) {
            return OemFamily.TECNO;
        }
        if (manufacturer.contains("transsion") || manufacturer.contains("itel")) {
            return OemFamily.TRANSSION;
        }
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")
                || brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco")) {
            return OemFamily.XIAOMI;
        }
        if (manufacturer.contains("samsung") || brand.contains("samsung")) {
            return OemFamily.SAMSUNG;
        }
        if (manufacturer.contains("oppo") || manufacturer.contains("oneplus") || manufacturer.contains("realme")
                || brand.contains("oppo") || brand.contains("oneplus") || brand.contains("realme")) {
            return OemFamily.OPPO_FAMILY;
        }
        if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")
                || brand.contains("vivo") || brand.contains("iqoo")) {
            return OemFamily.VIVO_IQOO;
        }
        if (manufacturer.contains("asus") || brand.contains("asus")) {
            return OemFamily.ASUS_ROG;
        }
        if (manufacturer.contains("nubia") || manufacturer.contains("redmagic")
                || brand.contains("nubia") || brand.contains("redmagic")) {
            return OemFamily.NUBIA_REDMAGIC;
        }
        if (manufacturer.contains("honor") || manufacturer.contains("huawei")
                || brand.contains("honor") || brand.contains("huawei")) {
            return OemFamily.HONOR_HUAWEI;
        }
        if (manufacturer.contains("blackshark") || brand.contains("blackshark")) {
            return OemFamily.BLACK_SHARK;
        }
        if (manufacturer.contains("motorola") || brand.contains("moto")) {
            return OemFamily.MOTOROLA;
        }
        if (manufacturer.contains("google") || brand.contains("google")) {
            return OemFamily.GOOGLE;
        }
        return OemFamily.GENERIC;
    }
}
