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
    private static final String KEY_NETWORK_MODE = "pref_network_mode"; // "data_only", "wifi_only", "dual", "default"
    private static final String KEY_ANTI_LOG = "pref_anti_log";

    public static void setNetworkMode(Context context, String mode) {
        if (context == null) return;
        getPrefs(context).edit().putString(KEY_NETWORK_MODE, mode).apply();
    }

    public static String getNetworkMode(Context context) {
        if (context == null) return "dual";
        return getPrefs(context).getString(KEY_NETWORK_MODE, "dual");
    }

    public static void setAntiLogEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_ANTI_LOG, enabled).apply();
    }

    public static boolean isAntiLogEnabled(Context context) {
        if (context == null) return true;
        return getPrefs(context).getBoolean(KEY_ANTI_LOG, true);
    }

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

        if (packageName != null && !packageName.trim().isEmpty() && !ShellSafety.isSafePackageName(packageName.trim())) {
            Log.w(TAG, "Unsafe package name rejected for manual settings: " + packageName);
            return;
        }
        String cleanPkg = packageName != null ? packageName.trim() : null;

        try {
            // 1. Android Game Mode API & Per-App Refresh Rate (Official Android 11/12/13/14/15/16)
            if (cleanPkg != null) {
                executeCmd("cmd game mode performance " + cleanPkg);
                executeCmd("cmd game set --fps 185 " + cleanPkg);
                executeCmd("cmd window set-app-refresh-rate " + cleanPkg + " 185");
            }

            // 2. GPU Render Mode
            String gpuMode = getGpuMode(context);
            if ("vulkan".equalsIgnoreCase(gpuMode)) {
                executeCmd("setprop debug.hwui.renderer vulkan");
                executeCmd("setprop renderthread.skia.glcontext 0");
            } else {
                executeCmd("setprop debug.hwui.renderer skiagl");
            }

            // 3. CPU Governor & Thermal Throttling Mitigation
            String cpuMode = getCpuMode(context);
            if ("performance".equalsIgnoreCase(cpuMode)) {
                executeCmd("for g in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do echo performance > $g 2>/dev/null; done");
            }

            // 4. ANGLE Graphics Backend
            if (isAngleModeEnabled(context) && cleanPkg != null) {
                executeCmd("settings put global angle_gl_driver_selection_pkgs " + cleanPkg);
                executeCmd("settings put global angle_gl_driver_selection_values angle");
            }

            // 5. Game Driver / Updatable Driver (Legal Android Settings.Global)
            if (isGameDriverEnabled(context) && cleanPkg != null) {
                executeCmd("settings put global game_driver_all_apps 1");
                executeCmd("settings put global game_driver_opt_in_apps " + cleanPkg);
                executeCmd("settings put global updatable_driver_all_apps 1");
                executeCmd("settings put global updatable_driver_production_opt_in_apps " + cleanPkg);
            }

            // 6. Network Low-Latency, 5G/6G & Dual WiFi/Cellular Boost
            if (isWifiLowLatencyEnabled(context)) {
                executeCmd("cmd wifi set-low-latency-mode enabled");
                executeCmd("setprop net.tcp.delack.default 1");
            }
            if (is5g6gDataEnabled(context)) {
                executeCmd("setprop persist.vendor.radio.5g_mode_pref 1");
                executeCmd("settings put global mobile_data_always_on 1");
            }
            if (isDualDataWifiEnabled(context)) {
                executeCmd("settings put global mobile_data_always_on 1");
            }
            if (isTetherHwEnabled(context)) {
                executeCmd("settings put global tether_offload_disabled 0");
            }
            if (isForceGnssEnabled(context)) {
                executeCmd("settings put global force_gnss_raw_measurements 1");
            }

            // 7. Ultra Extreme Graphics & Max FPS System Props (120/144/165/185 FPS)
            executeCmd("setprop debug.egl.force_msaa 1");
            executeCmd("setprop debug.egl.swapinterval 0");
            executeCmd("setprop debug.hwui.fps_divisor 1");
            executeCmd("setprop debug.graphics.game_default_frame_rate.disabled 1");
            executeCmd("setprop ro.vendor.dfps.enable 0");
            executeCmd("setprop vendor.display.enable_default_fps_switch 0");
            executeCmd("setprop persist.sys.game.fps 185");
            executeCmd("setprop persist.sys.game.rate 185");
            executeCmd("setprop persist.sys.power.game_mode 1");
            executeCmd("setprop debug.sf.disable_backpressure 1");
            executeCmd("setprop debug.sf.latch_unsignaled 1");

            // 8. Anti-Log & Telemetry Suppression
            if (isAntiLogEnabled(context)) {
                AntiLogPatcher.applySystemAntiLog();
                if (cleanPkg != null) {
                    AntiLogPatcher.applyAntiLog(cleanPkg);
                }
            }

            Log.i(TAG, "Manual hardware, kernel, Ultra Extreme & Max FPS settings applied successfully.");
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
