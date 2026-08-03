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
}
