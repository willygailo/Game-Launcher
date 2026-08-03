package com.gamebooster.app.device;

import android.content.Context;
import com.gamebooster.app.core.PropertyResolver;
import android.hardware.display.DisplayManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class DisplayCapabilitiesDetector {

    public static final class DisplayCaps {
        public final List<Integer> supportedRefreshRates;
        public final int maxRefreshRate;
        public final int minRefreshRate;
        public final int currentRefreshRate;
        public final boolean hasPeakRefreshRate;
        public final boolean hasMinRefreshRate;
        public final boolean hasUserRefreshRate;
        public final boolean hasDisplayModes;
        public final Display.Mode[] modes;
        public final int width;
        public final int height;
        public final float density;
        public final int densityDpi;
        public final boolean supportsHdr;
        public final boolean supportsWideColorGamut;
        public final String vendorInfo;

        DisplayCaps(List<Integer> rates, int max, int min, int current,
                    boolean peak, boolean minR, boolean userR, boolean modes,
                    Display.Mode[] modeArray, int w, int h, float dens, int densDpi,
                    boolean hdr, boolean wideColor, String vendor) {
            this.supportedRefreshRates = Collections.unmodifiableList(rates);
            this.maxRefreshRate = max;
            this.minRefreshRate = min;
            this.currentRefreshRate = current;
            this.hasPeakRefreshRate = peak;
            this.hasMinRefreshRate = minR;
            this.hasUserRefreshRate = userR;
            this.hasDisplayModes = modes;
            this.modes = modeArray;
            this.width = w;
            this.height = h;
            this.density = dens;
            this.densityDpi = densDpi;
            this.supportsHdr = hdr;
            this.supportsWideColorGamut = wideColor;
            this.vendorInfo = vendor;
        }

        public boolean supportsRate(int hz) { return supportedRefreshRates.contains(hz); }
        public List<Integer> getRecommendedRates() {
            return supportedRefreshRates.stream()
                .filter(r -> r >= 60)
                .sorted()
                .collect(Collectors.toList());
        }
    }

    public static DisplayCaps detect(Context ctx) {
        if (ctx == null) return empty();

        DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = dm != null ? dm.getDisplay(Display.DEFAULT_DISPLAY) : null;

        if (defaultDisplay == null) return empty();

        Display.Mode[] modes = defaultDisplay.getSupportedModes();
        DisplayMetrics metrics = new DisplayMetrics();
        defaultDisplay.getRealMetrics(metrics);

        List<Integer> rates = new ArrayList<>();
        int maxRate = 60, minRate = 60, currentRate = 60;
        if (modes != null && modes.length > 0) {
            for (Display.Mode m : modes) {
                int r = Math.round(m.getRefreshRate());
                if (r > 0 && !rates.contains(r)) rates.add(r);
            }
            if (!rates.isEmpty()) {
                maxRate = Collections.max(rates);
                minRate = Collections.min(rates);
            }
        }

        try {
            if (defaultDisplay.getMode() != null && defaultDisplay.getMode().getRefreshRate() > 0) {
                currentRate = Math.round(defaultDisplay.getMode().getRefreshRate());
            } else {
                currentRate = Math.round(defaultDisplay.getRefreshRate());
            }

            // Check system peak/user refresh rate settings
            String peakStr = Settings.System.getString(ctx.getContentResolver(), "peak_refresh_rate");
            if (peakStr != null) {
                float p = Float.parseFloat(peakStr);
                if (p > 0) currentRate = Math.max(currentRate, Math.round(p));
            }
            String userStr = Settings.System.getString(ctx.getContentResolver(), "user_refresh_rate");
            if (userStr != null) {
                float u = Float.parseFloat(userStr);
                if (u > 0) currentRate = Math.max(currentRate, Math.round(u));
            }
        } catch (Exception ignored) {}

        boolean hasPeak = Settings.System.canWrite(ctx);
        boolean hasMin = hasPeak;
        boolean hasUser = hasPeak;
        boolean hasModes = modes != null && modes.length > 0;

        boolean hdr = false, wideColor = false;
        try {
            hdr = defaultDisplay.getHdrCapabilities() != null;
            wideColor = defaultDisplay.isWideColorGamut();
        } catch (Exception ignored) {}

        String vendor = "";
        try {
            vendor = android.os.Build.DISPLAY + " | " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
        } catch (Exception ignored) {}

        return new DisplayCaps(
            rates, maxRate, minRate, currentRate,
            hasPeak, hasMin, hasUser, hasModes,
            modes, metrics.widthPixels, metrics.heightPixels,
            metrics.density, metrics.densityDpi,
            hdr, wideColor, vendor
        );
    }

    private static DisplayCaps empty() {
        return new DisplayCaps(
            List.of(60), 60, 60, 60,
            false, false, false, false,
            new Display.Mode[0], 0, 0, 1f, 160,
            false, false, ""
        );
    }

    public static class RefreshRateController {
        private final Context context;
        private final PropertyResolver props;
        private final DisplayCaps caps;

        public RefreshRateController(Context ctx, PropertyResolver resolver, DisplayCaps caps) {
            this.context = ctx;
            this.props = resolver;
            this.caps = caps;
        }

        public boolean setRefreshRate(int hz, Mode mode) {
            if (!caps.supportsRate(hz)) return false;
            String key;
            switch (mode) {
                case PEAK: key = "system:peak_refresh_rate"; break;
                case MIN: key = "system:min_refresh_rate"; break;
                case USER: key = "system:user_refresh_rate"; break;
                case EXACT:
                default: key = "system:peak_refresh_rate"; break;
            }
            return props.set(key, String.valueOf(hz)).success;
        }

        public boolean unlockMaxRefreshRate() {
            return setRefreshRate(caps.maxRefreshRate, Mode.PEAK) &&
                   setRefreshRate(caps.maxRefreshRate, Mode.MIN) &&
                   setRefreshRate(caps.maxRefreshRate, Mode.USER);
        }

        public boolean setMinRefreshRate(int hz) {
            if (!caps.supportsRate(hz)) return false;
            return props.set("system:min_refresh_rate", String.valueOf(hz)).success;
        }

        public boolean enableGameMode(String packageName) {
            boolean ok = true;
            ok &= props.set("global:game_mode", "1").success;
            ok &= props.set("global:game_driver_all_apps", "1").success;
            if (!packageName.isEmpty()) {
                ok &= props.set("global:game_mode_" + packageName, "1").success;
            }
            return ok;
        }

        public boolean enableHighPerformanceMode() {
            boolean ok = true;
            ok &= props.set("system:peak_refresh_rate", String.valueOf(caps.maxRefreshRate)).success;
            ok &= props.set("system:min_refresh_rate", String.valueOf(caps.maxRefreshRate)).success;
            ok &= props.set("system:user_refresh_rate", String.valueOf(caps.maxRefreshRate)).success;
            ok &= props.set("global:window_animation_scale", "0.5").success;
            ok &= props.set("global:transition_animation_scale", "0.5").success;
            ok &= props.set("global:animator_duration_scale", "0.5").success;
            ok &= props.set("system:touch_slop_reduction", "1").success;
            return ok;
        }

        public int getCurrentRefreshRate() { return caps.currentRefreshRate; }
        public int getMaxRefreshRate() { return caps.maxRefreshRate; }
        public List<Integer> getSupportedRates() { return caps.getRecommendedRates(); }
        public DisplayCaps getCapabilities() { return caps; }

        public enum Mode { PEAK, MIN, USER, EXACT }
    }
}
