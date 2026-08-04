package com.gamebooster.app.booster;
import com.gamebooster.app.config.*;

import android.os.Build;
import android.util.Log;
import android.content.Context;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.engine.CommandExecutor;

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
        String hzFloatStr = requestedHz + ".0";
        int hzInt = requestedHz;
        boolean ok = true;

        // Stock AOSP / Pixel Standard settings (Android 11+)
        ok &= CommandExecutor.setSystemSetting("system", "peak_refresh_rate", hzFloatStr);
        ok &= CommandExecutor.setSystemSetting("system", "min_refresh_rate", hzFloatStr);
        ok &= CommandExecutor.setSystemSetting("system", "user_refresh_rate", hzStr);
        CommandExecutor.setSystemSetting("global", "peak_refresh_rate", hzFloatStr);
        CommandExecutor.setSystemSetting("global", "min_refresh_rate", hzFloatStr);
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
            // Samsung OneUI (2 = Dynamic 120/144/165Hz, 1 = Standard 60Hz)
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
        } else if (manufacturer.contains("nubia") || brand.contains("redmagic") || manufacturer.contains("zte")) {
            // RedMagic / Nubia gaming devices
            CommandExecutor.setSystemSetting("system", "display_refresh_rate", String.valueOf(hzInt));
            CommandExecutor.setSystemSetting("system", "redmagic_refresh_rate", String.valueOf(hzInt));
        } else if (manufacturer.contains("infinix") || manufacturer.contains("tecno") || manufacturer.contains("itel") || manufacturer.contains("transsion")) {
            // Transsion Holdings (Infinix XOS / Tecno HiOS / iTel)
            CommandExecutor.setSystemSetting("system", "screen_refresh_rate_mode", hzInt >= 90 ? "1" : "0");
            CommandExecutor.setSystemSetting("system", "infinix_refresh_rate", String.valueOf(hzInt));
        }

        // Direct SurfaceFlinger Binder Override & Swap Interval Cap Removal (Supports 165Hz)
        CommandExecutor.executeSystemCommand("service call SurfaceFlinger 1035 i32 " + hzInt);
        CommandExecutor.executeSystemCommand("service call SurfaceFlinger 1036 i32 " + hzInt);
        CommandExecutor.setSystemProperty("debug.gr.swapinterval", "0");
        CommandExecutor.setSystemProperty("debug.sf.fps_limit", hzStr);
        CommandExecutor.setSystemProperty("persist.sys.NV_FPSLIMIT", hzStr);
        CommandExecutor.setSystemProperty("persist.sys.NV_POWERMODE", "1");

        return ok ? RefreshRateResult.success(requestedHz, requestedHz)
                : RefreshRateResult.failed(requestedHz, requestedHz);
    }

    public static boolean forceGameFps(Context context, String packageName, int targetFps) {
        if (packageName == null || packageName.isEmpty()) return false;
        
        boolean ok = true;
        // 1. Android Game Mode API per-app FPS set
        String cmdFps = "cmd game set --fps " + targetFps + " " + packageName;
        String resFps = CommandExecutor.executeSystemCommand(cmdFps);
        ok &= CommandExecutor.isSuccessOutput(resFps);

        // 2. Android Window Manager per-app refresh rate override
        CommandExecutor.executeSystemCommand("cmd window set-app-refresh-rate " + packageName + " " + targetFps);

        // 3. Android Game Mode Performance mode
        CommandExecutor.executeSystemCommand("cmd game mode performance " + packageName);

        return ok;
    }
}
