package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * EsportsPreferences — Persistent toggle state store for Esports Gaming Control.
 *
 * Fixes the auto-reset-to-OFF bug: we store what the USER last set,
 * not what the live service reports. On screen open we restore user intent.
 */
public class EsportsPreferences {

    private static final String PREFS_NAME = "esports_gaming_prefs";

    // Keys
    private static final String KEY_OVERLAY_HUD      = "esports_overlay_hud";
    private static final String KEY_GAMING_DND        = "esports_gaming_dnd";
    private static final String KEY_AUTO_GAME_BOOST   = "esports_auto_game_boost";
    private static final String KEY_ESPORTS_AUDIO     = "esports_audio_boost";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ─── Overlay HUD ────────────────────────────────────────────────────────
    public static void setOverlayHud(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_OVERLAY_HUD, enabled).apply();
    }

    public static boolean isOverlayHudEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_OVERLAY_HUD, false);
    }

    // ─── Gaming DND ─────────────────────────────────────────────────────────
    public static void setGamingDnd(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_GAMING_DND, enabled).apply();
    }

    public static boolean isGamingDndEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_GAMING_DND, false);
    }

    // ─── Auto Game Boost Monitor ─────────────────────────────────────────────
    public static void setAutoGameBoost(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_GAME_BOOST, enabled).apply();
    }

    public static boolean isAutoGameBoostEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_AUTO_GAME_BOOST, false);
    }

    // ─── Esports Audio Boost ─────────────────────────────────────────────────
    public static void setEsportsAudio(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_ESPORTS_AUDIO, enabled).apply();
    }

    public static boolean isEsportsAudioEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_ESPORTS_AUDIO, false);
    }
}
