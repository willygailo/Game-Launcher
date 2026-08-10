package com.gamebooster.app.overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public final class CrosshairPreferences {

    private static final String PREF_NAME = "crosshair_overlay_prefs";

    private static final String KEY_ENABLED = "crosshair_enabled";
    private static final String KEY_PRESET = "crosshair_preset";
    private static final String KEY_COLOR = "crosshair_color";
    private static final String KEY_SIZE_PX = "crosshair_size_px";
    private static final String KEY_STROKE_WIDTH = "crosshair_stroke_width";
    private static final String KEY_OPACITY = "crosshair_opacity";

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isCrosshairEnabled(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_ENABLED, false);
    }

    public static void setCrosshairEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static CrosshairPreset getPreset(Context context) {
        if (context == null) return CrosshairPreset.TACTICAL_CROSS;
        String name = getPrefs(context).getString(KEY_PRESET, CrosshairPreset.TACTICAL_CROSS.name());
        try {
            return CrosshairPreset.valueOf(name);
        } catch (Exception e) {
            return CrosshairPreset.TACTICAL_CROSS;
        }
    }

    public static void setPreset(Context context, CrosshairPreset preset) {
        if (context == null || preset == null) return;
        getPrefs(context).edit().putString(KEY_PRESET, preset.name()).apply();
    }

    public static int getColor(Context context) {
        if (context == null) return Color.parseColor("#00FF66"); // Neon Green default
        return getPrefs(context).getInt(KEY_COLOR, Color.parseColor("#00FF66"));
    }

    public static void setColor(Context context, int color) {
        if (context == null) return;
        getPrefs(context).edit().putInt(KEY_COLOR, color).apply();
    }

    public static int getSizePx(Context context) {
        if (context == null) return 80;
        return getPrefs(context).getInt(KEY_SIZE_PX, 80);
    }

    public static void setSizePx(Context context, int sizePx) {
        if (context == null) return;
        getPrefs(context).edit().putInt(KEY_SIZE_PX, Math.max(30, sizePx)).apply();
    }

    public static float getStrokeWidth(Context context) {
        if (context == null) return 4f;
        return getPrefs(context).getFloat(KEY_STROKE_WIDTH, 4f);
    }

    public static void setStrokeWidth(Context context, float strokeWidth) {
        if (context == null) return;
        getPrefs(context).edit().putFloat(KEY_STROKE_WIDTH, Math.max(1.0f, strokeWidth)).apply();
    }

    public static float getOpacity(Context context) {
        if (context == null) return 1.0f;
        return getPrefs(context).getFloat(KEY_OPACITY, 1.0f);
    }

    public static void setOpacity(Context context, float opacity) {
        if (context == null) return;
        getPrefs(context).edit().putFloat(KEY_OPACITY, Math.max(0.1f, Math.min(1.0f, opacity))).apply();
    }
}
