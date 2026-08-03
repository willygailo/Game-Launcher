package com.gamebooster.app.functions;

import android.os.Build;
import android.util.Log;
import android.content.Context;
import com.gamebooster.app.core.DevicePerformanceCapabilities;
import com.gamebooster.app.root.CommandExecutor;

public class HzFpsChannel {

    private static final String TAG = "HzFpsChannel";

    public static final class RefreshRateResult {
        public final boolean success;
        public final int requestedHz;
        public final int appliedHz;
        public final String message;

        private RefreshRateResult(boolean success, int requestedHz, int appliedHz, String message) {
            this.success = success;
            this.requestedHz = requestedHz;
            this.appliedHz = appliedHz;
            this.message = message;
        }

        public static RefreshRateResult success(int requestedHz, int appliedHz) {
            String note = requestedHz == appliedHz ? "Applied " + appliedHz + "Hz"
                    : "Applied supported " + appliedHz + "Hz instead of requested " + requestedHz + "Hz";
            return new RefreshRateResult(true, requestedHz, appliedHz, note);
        }

        public static RefreshRateResult unsupported(int requestedHz, int maxHz) {
            return new RefreshRateResult(false, requestedHz, 0,
                    requestedHz + "Hz is not supported on this device (max " + maxHz + "Hz)");
        }

        public static RefreshRateResult failed(int requestedHz, int appliedHz) {
            return new RefreshRateResult(false, requestedHz, appliedHz,
                    "Android did not allow the " + appliedHz + "Hz setting. Connect Shizuku or allow Modify system settings.");
        }
    }

    /**
     * Applies only a refresh rate exposed by Android for the current display.
     * This controls display refresh rate, not an individual game's internal FPS cap.
     */
    public static RefreshRateResult setRefreshRate(Context context, int requestedHz) {
        if (context == null) return RefreshRateResult.failed(requestedHz, 0);

        DevicePerformanceCapabilities capabilities = DevicePerformanceCapabilities.detect(context);
        if (!capabilities.supportsRefreshRate(requestedHz)) {
            return RefreshRateResult.unsupported(requestedHz, capabilities.getMaxRefreshRate());
        }

        String hzStr = String.valueOf(requestedHz);
        int hzInt = requestedHz;
        boolean ok = true;

        // Stock AOSP / Pixel Standard settings (Android 11+)
        ok &= CommandExecutor.setSystemSetting("system", "peak_refresh_rate", hzStr);
        ok &= CommandExecutor.setSystemSetting("system", "min_refresh_rate", hzStr);
        ok &= CommandExecutor.setSystemSetting("system", "user_refresh_rate", hzStr);
        CommandExecutor.setSystemSetting("global", "peak_refresh_rate", hzStr);
        CommandExecutor.setSystemSetting("global", "min_refresh_rate", hzStr);
        CommandExecutor.executeSystemCommand("cmd game mode performance global");

        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase() : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase() : "";
        Log.d(TAG, "setRefreshRate: targetHz=" + requestedHz + " manufacturer='" + manufacturer + "' brand='" + brand + "'");

        // Vendor-Gated Specific Keys
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || brand.contains("poco")) {
            // Xiaomi / MIUI / Poco
            CommandExecutor.setSystemSetting("secure", "user_refresh_rate", String.valueOf(hzInt));
            CommandExecutor.setSystemSetting("global", "surface_flinger_peak_refresh_rate", hzStr);
        } else if (manufacturer.contains("samsung")) {
            // Samsung OneUI (2 = Dynamic 120/144Hz, 1 = Standard 60Hz)
            CommandExecutor.setSystemSetting("secure", "refresh_rate_mode", hzInt >= 90 ? "2" : "1");
        } else if (manufacturer.contains("oneplus") || manufacturer.contains("oppo") || manufacturer.contains("realme")) {
            // OnePlus / Realme / Oppo
            CommandExecutor.setSystemSetting("global", "oneplus_screen_refresh_rate", hzInt >= 90 ? "2" : "1");
            CommandExecutor.setSystemSetting("global", "realme_screen_refresh_rate", String.valueOf(hzInt));
        } else if (manufacturer.contains("asus")) {
            // ASUS ROG Phone / Gaming Devices
            CommandExecutor.setSystemSetting("system", "asus_option_display_refresh_rate", String.valueOf(hzInt));
        } else if (manufacturer.contains("vivo") || brand.contains("iqoo")) {
            // vivo / iQOO Funtouch OS / Origin OS
            CommandExecutor.setSystemSetting("system", "screen_refresh_rate", String.valueOf(hzInt));
            CommandExecutor.setSystemSetting("system", "iqoo_refresh_rate", String.valueOf(hzInt));
        } else if (manufacturer.contains("motorola") || brand.contains("moto")) {
            // Motorola MyUX
            CommandExecutor.setSystemSetting("global", "peak_refresh_rate", hzStr);
            CommandExecutor.setSystemSetting("system", "min_refresh_rate", hzStr);
        } else if (manufacturer.contains("infinix") || manufacturer.contains("tecno") || manufacturer.contains("itel") || manufacturer.contains("transsion")) {
            // Transsion Holdings (Infinix XOS / Tecno HiOS / iTel)
            CommandExecutor.setSystemSetting("system", "screen_refresh_rate_mode", hzInt >= 90 ? "1" : "0");
            CommandExecutor.setSystemSetting("system", "infinix_refresh_rate", String.valueOf(hzInt));
        }

        return ok ? RefreshRateResult.success(requestedHz, requestedHz)
                : RefreshRateResult.failed(requestedHz, requestedHz);
    }

    public static boolean forceGameFps(Context context, String packageName, int targetFps) {
        if (packageName == null || packageName.isEmpty()) return false;
        if (context == null || !DevicePerformanceCapabilities.detect(context).supportsRefreshRate(targetFps)) {
            return false;
        }
        String cmd = "cmd game set --fps " + targetFps + " " + packageName;
        String res = CommandExecutor.executeSystemCommand(cmd);
        return CommandExecutor.isSuccessOutput(res);
    }
}
