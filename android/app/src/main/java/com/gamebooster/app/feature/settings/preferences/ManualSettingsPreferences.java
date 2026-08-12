package com.gamebooster.app.feature.settings.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class ManualSettingsPreferences {
    private static final String PREF_NAME = "game_booster_manual_settings";
    private static final String KEY_GPU_MODE = "pref_gpu_mode"; // "vulkan" or "skia"
    private static final String KEY_CPU_MODE = "pref_cpu_mode"; // "performance" or "schedutil"

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
}
