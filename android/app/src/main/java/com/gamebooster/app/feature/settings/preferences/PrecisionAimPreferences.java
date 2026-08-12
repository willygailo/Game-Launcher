package com.gamebooster.app.feature.settings.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * PrecisionAimPreferences — Persistent toggle state store for Precision Aim Input & Gyro Tuner.
 *
 * Stores USER INTENT (what the user set), not live system state.
 * On fragment create, we restore intent and re-apply if Shizuku is available.
 */
public class PrecisionAimPreferences {

    private static final String PREFS_NAME = "precision_aim_prefs";

    private static final String KEY_INPUT_TUNER       = "aim_input_tuner_enabled";
    private static final String KEY_CROSSHAIR_OVERLAY = "aim_crosshair_overlay_enabled";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ─── Precision Input Tuner ───────────────────────────────────────────────
    public static void setInputTunerEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_INPUT_TUNER, enabled).apply();
    }

    public static boolean isInputTunerEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_INPUT_TUNER, false);
    }

    // ─── Crosshair Overlay ───────────────────────────────────────────────────
    public static void setCrosshairOverlayEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_CROSSHAIR_OVERLAY, enabled).apply();
    }

    public static boolean isCrosshairOverlayEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_CROSSHAIR_OVERLAY, false);
    }
}
