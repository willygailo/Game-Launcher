package com.gamebooster.app.spoofer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SpoofPreferences handles persistence of master Device Spoof toggles,
 * active global SpoofProfile selection, and per-game package spoofer overrides.
 */
public class SpoofPreferences {

    private static final String PREF_NAME = "device_spoofer_prefs";
    private static final String KEY_SPOOF_ENABLED = "spoof_enabled";
    private static final String KEY_ACTIVE_PROFILE_ID = "active_profile_id";
    private static final String PREFIX_GAME_ENABLED = "game_spoof_enabled_";
    private static final String PREFIX_GAME_PROFILE = "game_spoof_profile_";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isSpoofEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_SPOOF_ENABLED, false);
    }

    public static void setSpoofEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_SPOOF_ENABLED, enabled).apply();
    }

    public static String getActiveProfileId(Context context) {
        return getPrefs(context).getString(KEY_ACTIVE_PROFILE_ID, null);
    }

    public static void setActiveProfileId(Context context, String profileId) {
        getPrefs(context).edit().putString(KEY_ACTIVE_PROFILE_ID, profileId).apply();
    }

    public static void clearActiveProfile(Context context) {
        getPrefs(context).edit().remove(KEY_ACTIVE_PROFILE_ID).apply();
    }

    // ── Per-Game Package Overrides ──

    public static boolean isGameSpoofEnabled(Context context, String packageName) {
        if (packageName == null) return isSpoofEnabled(context);
        return getPrefs(context).getBoolean(PREFIX_GAME_ENABLED + packageName, true);
    }

    public static void setGameSpoofEnabled(Context context, String packageName, boolean enabled) {
        if (packageName == null) return;
        getPrefs(context).edit().putBoolean(PREFIX_GAME_ENABLED + packageName, enabled).apply();
    }

    public static String getGameSpoofProfileId(Context context, String packageName) {
        if (packageName == null) return getActiveProfileId(context);
        return getPrefs(context).getString(PREFIX_GAME_PROFILE + packageName, null);
    }

    public static void setGameSpoofProfileId(Context context, String packageName, String profileId) {
        if (packageName == null) return;
        getPrefs(context).edit().putString(PREFIX_GAME_PROFILE + packageName, profileId).apply();
    }

    public static void clearGameSpoof(Context context, String packageName) {
        if (packageName == null) return;
        getPrefs(context).edit()
                .remove(PREFIX_GAME_ENABLED + packageName)
                .remove(PREFIX_GAME_PROFILE + packageName)
                .apply();
    }
}
