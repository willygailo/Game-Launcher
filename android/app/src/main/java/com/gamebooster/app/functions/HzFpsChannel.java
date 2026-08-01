package com.gamebooster.app.functions;

import android.os.Build;
import android.util.Log;
import com.gamebooster.app.root.CommandExecutor;

public class HzFpsChannel {

    private static final String TAG = "HzFpsChannel";

    public static boolean setRefreshRate(float hz) {
        String hzStr = String.valueOf(hz);
        int hzInt = (int) hz;
        boolean ok = true;

        // Stock AOSP / Pixel Standard settings (Android 11+)
        ok &= CommandExecutor.setSystemSetting("system", "peak_refresh_rate", hzStr);
        ok &= CommandExecutor.setSystemSetting("system", "min_refresh_rate", hzStr);
        ok &= CommandExecutor.setSystemSetting("system", "user_refresh_rate", hzStr);
        CommandExecutor.setSystemSetting("global", "peak_refresh_rate", hzStr);
        CommandExecutor.setSystemSetting("global", "min_refresh_rate", hzStr);
        CommandExecutor.executeSystemCommand("cmd game mode performance global");
        CommandExecutor.setSystemProperty("debug.graphics.game_default_frame_rate.disabled", "1");

        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase() : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase() : "";
        Log.d(TAG, "setRefreshRate: targetHz=" + hz + " manufacturer='" + manufacturer + "' brand='" + brand + "'");

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
        }

        // SurfaceFlinger High FPS phase offsets for zero-stutter rendering
        CommandExecutor.setSystemProperty("debug.sf.high_fps_early_phase_offset_ns", "1000000");
        CommandExecutor.setSystemProperty("debug.sf.high_fps_early_app_phase_offset_ns", "1000000");

        return ok;
    }

    public static boolean forceGameFps(String packageName, int targetFps) {
        if (packageName == null || packageName.isEmpty()) return false;
        String cmd = "cmd game set --fps " + targetFps + " " + packageName;
        String res = CommandExecutor.executeSystemCommand(cmd);
        return CommandExecutor.isSuccessOutput(res);
    }
}
