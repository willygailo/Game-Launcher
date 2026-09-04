package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.GameModeApiSupport;
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
    private static final String KEY_GAMING_DNS = "pref_gaming_dns";

    public static void setGamingDns(Context context, String dnsMode) {
        if (context == null) return;
        getPrefs(context).edit().putString(KEY_GAMING_DNS, dnsMode != null ? dnsMode : "CLOUDFLARE_1_1_1_1").apply();
    }

    public static String getGamingDns(Context context) {
        if (context == null) return "CLOUDFLARE_1_1_1_1";
        return getPrefs(context).getString(KEY_GAMING_DNS, "CLOUDFLARE_1_1_1_1");
    }

    @Deprecated
    public static void setAngleMode(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_ANGLE_MODE, false).apply();
    }

    @Deprecated
    public static boolean isAngleModeEnabled(Context context) {
        return false;
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
    private static final String KEY_THERMAL_BYPASS = "pref_thermal_bypass";
    private static final String KEY_FOCUS_MODE = "pref_focus_mode";
    private static final String KEY_FOCUS_WHITELIST = "pref_focus_whitelist";

    public static void setFocusModeEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_FOCUS_MODE, enabled).apply();
    }

    public static boolean isFocusModeEnabled(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_FOCUS_MODE, false);
    }

    public static void setFocusWhitelist(Context context, java.util.Set<String> whitelist) {
        if (context == null) return;
        getPrefs(context).edit().putStringSet(KEY_FOCUS_WHITELIST, whitelist != null ? whitelist : new java.util.HashSet<>()).apply();
    }

    public static java.util.Set<String> getFocusWhitelist(Context context) {
        if (context == null) return new java.util.HashSet<>();
        java.util.Set<String> set = getPrefs(context).getStringSet(KEY_FOCUS_WHITELIST, null);
        return set != null ? new java.util.HashSet<>(set) : new java.util.HashSet<>();
    }

    public static void setThermalBypassEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_THERMAL_BYPASS, enabled).apply();
    }

    public static boolean isThermalBypassEnabled(Context context) {
        if (context == null) return true;
        return getPrefs(context).getBoolean(KEY_THERMAL_BYPASS, true);
    }

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

    private static final String KEY_ADPF_ENGINE = "pref_adpf_engine";
    private static final String KEY_VIDEO_SAVER = "pref_video_saver";
    private static final String KEY_AOT_SPEED = "pref_aot_speed";

    public static void setAdpfEngineEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_ADPF_ENGINE, enabled).apply();
    }

    public static boolean isAdpfEngineEnabled(Context context) {
        if (context == null) return true;
        return getPrefs(context).getBoolean(KEY_ADPF_ENGINE, true);
    }

    public static void setVideoSaverEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_VIDEO_SAVER, enabled).apply();
    }

    public static boolean isVideoSaverEnabled(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_VIDEO_SAVER, false);
    }

    public static void setAotSpeedEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_AOT_SPEED, enabled).apply();
    }

    public static boolean isAotSpeedEnabled(Context context) {
        if (context == null) return true;
        return getPrefs(context).getBoolean(KEY_AOT_SPEED, true);
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
            // 1. Android Game Mode API & Per-App Refresh Rate (Official Android 13/14/15/16)
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
            if (isThermalBypassEnabled(context)) {
                com.gamebooster.app.booster.ThermalChannel.setThermalOverride(true);
            }

            // 4. ANGLE Graphics Backend (Strictly Purged for stability)
            executeCmd("settings delete global angle_gl_driver_selection_pkgs 2>/dev/null");
            executeCmd("settings delete global angle_gl_driver_selection_values 2>/dev/null");
            executeCmd("settings delete global angle_enabled_pkgs 2>/dev/null");
            executeCmd("settings put global angle_gl_driver_all_angle 0 2>/dev/null");
            executeCmd("setprop debug.angle.backend 0");

            // 5. Game Driver / Updatable Driver (Strictly MLBB, CODM, and PUBGM only)
            if (isGameDriverEnabled(context)) {
                String targetPkgs;
                if (cleanPkg != null) {
                    targetPkgs = com.gamebooster.app.booster.GpuTweaksChannel.isGameDriverEligible(cleanPkg) ? cleanPkg : "";
                } else {
                    targetPkgs = com.gamebooster.app.booster.GpuTweaksChannel.getTargetGamesCsv();
                }
                executeCmd("settings put global game_driver_all_apps 0");
                executeCmd("settings put global updatable_driver_all_apps 0");
                executeCmd("settings put global game_driver_opt_in_apps \"" + targetPkgs + "\"");
                executeCmd("settings put global game_driver_prerelease_opt_in_apps \"" + targetPkgs + "\"");
                executeCmd("settings put global updatable_driver_production_opt_in_apps \"" + targetPkgs + "\"");
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
            // Disable Android Default Frame Rate & Disable 120Hz/60Hz Game Limiting
            executeCmd("setprop debug.egl.force_msaa 1");
            executeCmd("setprop debug.egl.swapinterval 0");
            executeCmd("setprop debug.gr.swapinterval 0");
            executeCmd("setprop debug.hwui.fps_divisor 1");
            executeCmd("setprop debug.graphics.game_default_frame_rate.disabled 1");
            executeCmd("setprop ro.vendor.dfps.enable 0");
            executeCmd("setprop vendor.display.enable_default_fps_switch 0");
            executeCmd("setprop persist.vendor.power.dfps.level 0");
            executeCmd("setprop persist.vendor.display.vrr.disable 1");
            executeCmd("setprop ro.surface_flinger.set_idle_timer_ms 0");
            executeCmd("setprop ro.surface_flinger.set_touch_timer_ms 0");
            executeCmd("setprop debug.sf.fps_limit 185");
            executeCmd("setprop persist.sys.NV_FPSLIMIT 185");
            executeCmd("setprop persist.sys.game.fps 185");
            executeCmd("setprop persist.sys.game.rate 185");
            executeCmd("setprop persist.sys.fps 185");
            executeCmd("setprop debug.cpurend.fps 185");
            executeCmd("setprop persist.sys.power.game_mode 1");

            // Disable Android Match Content Frame Rate & System Refresh Rate Limiters
            executeCmd("settings put system match_content_frame_rate 0");
            executeCmd("settings put secure match_content_frame_rate_preference 0");
            executeCmd("settings put system peak_refresh_rate 185.0");
            executeCmd("settings put system min_refresh_rate 185.0");
            executeCmd("settings put system user_refresh_rate 185");
            executeCmd("settings put global peak_refresh_rate 185.0");
            executeCmd("settings put global min_refresh_rate 185.0");
            executeCmd("service call SurfaceFlinger 1035 i32 185");
            executeCmd("service call SurfaceFlinger 1036 i32 185");

            // Disable OEM-specific 120Hz/60Hz game throttling (MIUI/HyperOS Joyose, Samsung GOS, ColorOS/OxygenOS, ROG, iQOO, RedMagic)
            executeCmd("settings put secure user_refresh_rate 185");
            executeCmd("settings put global surface_flinger_peak_refresh_rate 185");
            executeCmd("settings put secure refresh_rate_mode 2");
            executeCmd("settings put system sec_display_fps 185");
            executeCmd("settings put secure game_auto_temperature_control 0");
            executeCmd("settings put global oneplus_screen_refresh_rate 2");
            executeCmd("settings put global realme_screen_refresh_rate 185");
            executeCmd("settings put global oppo_screen_refresh_rate 185");
            executeCmd("settings put system asus_option_display_refresh_rate 185");
            executeCmd("settings put system asus_hfr_mode 1");
            executeCmd("settings put system screen_refresh_rate 185");
            executeCmd("settings put system iqoo_refresh_rate 185");
            executeCmd("settings put system display_refresh_rate 185");
            executeCmd("settings put system redmagic_refresh_rate 185");

            // 8. Android 13, 14, 15, 16 Modern OS & ADPF Flags (Zero Fallback)
            GameModeApiSupport.applyModernAndroidPerformanceFlags(cleanPkg, 185);

            // 9. Anti-Log & Telemetry Suppression
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
