package com.gamespace.app.channels;

import com.gamespace.app.data.CommandExecutor;

public class HzFpsChannel {

    public static boolean setRefreshRate(float hz) {
        String hzStr = String.valueOf(hz);
        int hzInt = (int) hz;
        boolean ok = true;

        // Stock / Standard Android settings
        ok &= CommandExecutor.setSystemSetting("system", "peak_refresh_rate", hzStr);
        ok &= CommandExecutor.setSystemSetting("system", "min_refresh_rate", hzStr);
        ok &= CommandExecutor.setSystemSetting("system", "user_refresh_rate", hzStr);

        // Multi-OEM settings overrides
        // Xiaomi / MIUI / Poco
        CommandExecutor.setSystemSetting("secure", "user_refresh_rate", String.valueOf(hzInt));
        CommandExecutor.setSystemSetting("global", "surface_flinger_peak_refresh_rate", hzStr);

        // Samsung OneUI
        CommandExecutor.setSystemSetting("secure", "refresh_rate_mode", hzInt >= 90 ? "2" : "1");

        // OnePlus / Realme / Oppo
        CommandExecutor.setSystemSetting("global", "oneplus_screen_refresh_rate", hzInt >= 90 ? "2" : "1");

        // SurfaceFlinger High FPS phase offsets
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
