package com.gamebooster.app.spoofer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SpoofPreferences handles persistence of the master Device Spoof toggle
 * and active SpoofProfile selection.
 */
public class SpoofPreferences {

    private static final String PREF_NAME = "device_spoofer_prefs";
    private static final String KEY_SPOOF_ENABLED = "spoof_enabled";
    private static final String KEY_ACTIVE_PROFILE_ID = "active_profile_id";

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
}
