package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class ManualSettingsPreferences {
    private static final String TAG = "ManualSettingsPrefs";
    private static final String PREF_NAME = "game_booster_manual_settings";
    private static final String KEY_GPU_MODE = "pref_gpu_mode"; // "vulkan" or "skia"
    private static final String KEY_CPU_MODE = "pref_cpu_mode"; // "performance" or "schedutil"

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void setGpuMode(Context context, String mode) {
        if (context == null) return;
        getPrefs(context).edit().putString(KEY_GPU_MODE, mode).apply();
    }

    public static String getGpuMode(Context context) {
        if (context == null) return "vulkan";
        return getPrefs(context).getString(KEY_GPU_MODE, "vulkan");
    }

    public static void setCpuMode(Context context, String mode) {
        if (context == null) return;
        getPrefs(context).edit().putString(KEY_CPU_MODE, mode).apply();
    }

    public static String getCpuMode(Context context) {
        if (context == null) return "performance";
        return getPrefs(context).getString(KEY_CPU_MODE, "performance");
    }

    private static final String KEY_ANGLE_MODE = "pref_angle_mode";
    private static final String KEY_GAME_DRIVER = "pref_game_driver";
    private static final String KEY_TETHER_HW = "pref_tether_hw";
    private static final String KEY_FORCE_GNSS = "pref_force_gnss";

    public static void setAngleMode(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_ANGLE_MODE, enabled).apply();
    }

    public static boolean isAngleModeEnabled(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_ANGLE_MODE, false);
    }

    public static void setGameDriverEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_GAME_DRIVER, enabled).apply();
    }

    public static boolean isGameDriverEnabled(Context context) {
        if (context == null) return true;
        return getPrefs(context).getBoolean(KEY_GAME_DRIVER, true);
    }

    public static void setTetherHwEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_TETHER_HW, enabled).apply();
    }

    public static boolean isTetherHwEnabled(Context context) {
        if (context == null) return true;
        return getPrefs(context).getBoolean(KEY_TETHER_HW, true);
    }

    public static void setForceGnssEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_FORCE_GNSS, enabled).apply();
    }

    public static boolean isForceGnssEnabled(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_FORCE_GNSS, false);
    }

    private static final String KEY_5G_6G_DATA = "pref_5g_6g_data";
    private static final String KEY_WIFI_LOW_LATENCY = "pref_wifi_low_latency";
    private static final String KEY_DUAL_DATA_WIFI = "pref_dual_data_wifi";

    public static void set5g6gDataEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_5G_6G_DATA, enabled).apply();
    }

    public static boolean is5g6gDataEnabled(Context context) {
        if (context == null) return true;
        return getPrefs(context).getBoolean(KEY_5G_6G_DATA, true);
    }

    public static void setWifiLowLatencyEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_WIFI_LOW_LATENCY, enabled).apply();
    }

    public static boolean isWifiLowLatencyEnabled(Context context) {
        if (context == null) return true;
        return getPrefs(context).getBoolean(KEY_WIFI_LOW_LATENCY, true);
    }

    public static void setDualDataWifiEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_DUAL_DATA_WIFI, enabled).apply();
    }

    public static boolean isDualDataWifiEnabled(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_DUAL_DATA_WIFI, false);
    }

    /**
     * Applies all stored manual hardware, kernel, network, and driver settings via Shizuku.
     */
    public static void applyManualSettings(Context context) {
        applyManualSettings(context, null);
    }

    /**
     * Applies all stored manual hardware, kernel, network, and driver settings for a target package.
     */
    public static void applyManualSettings(Context context, String packageName) {
        if (context == null) return;

        try {
            // 1. GPU Render Mode
            String gpuMode = getGpuMode(context);
            if ("vulkan".equalsIgnoreCase(gpuMode)) {
                executeCmd("setprop debug.hwui.renderer vulkan");
                executeCmd("setprop renderthread.skia.glcontext 0");
            } else {
                executeCmd("setprop debug.hwui.renderer skiagl");
            }

            // 2. CPU Governor
            String cpuMode = getCpuMode(context);
            if ("performance".equalsIgnoreCase(cpuMode)) {
                executeCmd("for g in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > $g 2>/dev/null; done");
            }

            // 3. ANGLE Graphics Backend
            if (isAngleModeEnabled(context) && packageName != null) {
                executeCmd("settings put global angle_gl_driver_selection_pkgs " + packageName);
                executeCmd("settings put global angle_gl_driver_selection_values angle");
            }

            // 4. Game Driver / Updatable Driver
            if (isGameDriverEnabled(context) && packageName != null) {
                executeCmd("settings put global updatable_driver_production_opt_in_apps " + packageName);
                executeCmd("settings put global updatable_driver_all_apps 1");
            }

            // 5. Network Low-Latency & Dual WiFi/Cellular Boost
            if (isWifiLowLatencyEnabled(context)) {
                executeCmd("cmd wifi set-low-latency-mode enabled");
                executeCmd("setprop net.tcp.delack.default 1");
            }
            if (isDualDataWifiEnabled(context)) {
                executeCmd("settings put global mobile_data_always_on 1");
            }

            Log.i(TAG, "Manual hardware & kernel settings applied successfully.");
        } catch (Throwable e) {
            Log.w(TAG, "Failed to apply manual settings: " + e.getMessage());
        }
    }

    private static void executeCmd(String cmd) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            CommandExecutor.executeSystemCommand(cmd);
        }
    }
}
