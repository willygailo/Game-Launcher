package com.gamebooster.app.platform.shizuku;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * ShizukuSystemDeviceController enables elevated Android OS & phone system control
 * via Shizuku ADB shell privileges (uid 2000 / shell).
 *
 * Capabilities:
 *  - Global / Secure / System Settings database manipulation
 *  - Refresh Rate & FPS overrides (cmd display set-user-preferred-display-mode / SurfaceFlinger)
 *  - RAM cache purging (cmd activity compact / trim-caches)
 *  - ANGLE / Graphics driver selection per game
 *  - Thermal throttling mitigation & Game Turbo spoofing
 *  - Private DNS & Network optimizations
 */
public class ShizukuSystemDeviceController {

    private static final String TAG = "ShizukuDeviceController";

    /**
     * Puts a value into Android Settings (global, secure, system).
     */
    public static boolean putSetting(String namespace, String key, String value) {
        if (namespace == null || key == null || value == null) return false;
        String cmd = "settings put " + namespace.trim() + " " + key.trim() + " " + value.trim();
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        return res != null && !res.toLowerCase().contains("error");
    }

    /**
     * Reads a value from Android Settings.
     */
    public static String getSetting(String namespace, String key) {
        if (namespace == null || key == null) return null;
        String cmd = "settings get " + namespace.trim() + " " + key.trim();
        String res = ShizukuExecutor.executeShizukuCommand(cmd);
        if (res == null || res.startsWith("ERROR:") || res.equals("null")) return null;
        return res.trim();
    }

    /**
     * Forces display refresh rate mode (e.g. 120Hz, 144Hz, 165Hz).
     */
    public static boolean forceDisplayRefreshRate(float refreshRate) {
        List<String> commands = new ArrayList<>();
        // Set system properties for peak & min refresh rate
        commands.add("settings put system peak_refresh_rate " + refreshRate);
        commands.add("settings put system min_refresh_rate " + refreshRate);
        commands.add("settings put secure refresh_rate_mode 2");
        commands.add("settings put system user_refresh_rate " + (int) refreshRate);
        // SurfaceFlinger dynamic frame latching
        commands.add("service call SurfaceFlinger 1035 i32 1");
        
        String res = ShizukuExecutor.executeShizukuBatchCommands(commands);
        Log.d(TAG, "forceDisplayRefreshRate (" + refreshRate + "Hz) -> " + res);
        return res != null && !res.toLowerCase().contains("error");
    }

    /**
     * Purges background memory and compacts active RAM caches.
     */
    public static boolean purgeRamAndCaches() {
        List<String> commands = new ArrayList<>();
        commands.add("pm trim-caches 1000G");
        commands.add("cmd activity compact system");
        commands.add("cmd activity compact some");
        commands.add("cmd activity compact full");
        String res = ShizukuExecutor.executeShizukuBatchCommands(commands);
        return res != null && !res.toLowerCase().contains("error");
    }

    /**
     * Configures ANGLE / System Graphics driver for a game.
     */
    public static boolean setGameGraphicsDriver(String packageName, String driver) {
        if (packageName == null) return false;
        String d = driver != null ? driver : "angle";
        List<String> commands = new ArrayList<>();
        commands.add("settings put global angle_gl_driver_selection_pkgs " + packageName.trim());
        commands.add("settings put global angle_gl_driver_selection_values " + d);
        String res = ShizukuExecutor.executeShizukuBatchCommands(commands);
        return res != null && !res.toLowerCase().contains("error");
    }

    /**
     * Configures Ultra-Low Latency Gaming DNS.
     */
    public static boolean setGamingPrivateDns(String dnsHostname) {
        if (dnsHostname == null || dnsHostname.trim().isEmpty()) {
            return putSetting("global", "private_dns_mode", "off");
        }
        putSetting("global", "private_dns_mode", "hostname");
        return putSetting("global", "private_dns_specifier", dnsHostname.trim());
    }

    /**
     * Injects Game Turbo / High Performance touch responsiveness settings.
     */
    public static boolean enableUltraTouchResponsiveness() {
        List<String> commands = new ArrayList<>();
        commands.add("settings put system touch_responsiveness_level 3");
        commands.add("settings put secure long_press_timeout 250");
        commands.add("settings put secure multi_press_timeout 200");
        commands.add("setprop debug.touch.frequency 1000");
        String res = ShizukuExecutor.executeShizukuBatchCommands(commands);
        return res != null && !res.toLowerCase().contains("error");
    }
}
