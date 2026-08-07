package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;

public class ManualSettingsPreferences {
    private static final String PREF_NAME = "game_booster_manual_settings";
    private static final String KEY_GPU_MODE = "pref_gpu_mode"; // "vulkan" or "skia"
    private static final String KEY_CPU_MODE = "pref_cpu_mode"; // "performance" or "schedutil"

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private static final String KEY_MASTER_BOOST = "pref_master_boost_enabled";

    public static void setMasterBoostEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_MASTER_BOOST, enabled).apply();
    }

    public static boolean isMasterBoostEnabled(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_MASTER_BOOST, false);
    }

    public static void setGpuMode(Context context, String mode) {
        if (context == null) return;
        getPrefs(context).edit().putString(KEY_GPU_MODE, mode).apply();
    }

    public static String getGpuMode(Context context) {
        if (context == null) return "skia";
        return getPrefs(context).getString(KEY_GPU_MODE, "skia");
    }

    public static void setCpuMode(Context context, String mode) {
        if (context == null) return;
        getPrefs(context).edit().putString(KEY_CPU_MODE, mode).apply();
    }

    public static String getCpuMode(Context context) {
        if (context == null) return "schedutil";
        return getPrefs(context).getString(KEY_CPU_MODE, "schedutil");
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
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_GAME_DRIVER, false);
    }

    public static void setTetherHwEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_TETHER_HW, enabled).apply();
    }

    public static boolean isTetherHwEnabled(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_TETHER_HW, false);
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
