package com.gamebooster.app.spoofer;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * SpoofPreferences handles persistence of the master Device Spoof toggle,
 * active global SpoofProfile selection, and per-package profile overrides.
 *
 * Per-package keys are stored as "pkg_profile_<packageName>" in the same SharedPreferences.
 * Per-package profile takes priority over the global profile when applySpoofing() is called.
 */
public class SpoofPreferences {

    private static final String PREF_NAME = "device_spoofer_prefs";
    private static final String KEY_SPOOF_ENABLED = "spoof_enabled";
    private static final String KEY_ACTIVE_PROFILE_ID = "active_profile_id";
    private static final String KEY_SPOOF_ALL_APPS = "spoof_all_apps";
    private static final String PREFIX_PKG_PROFILE = "pkg_profile_";

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ── Global Spoof Toggle ──

    public static boolean isSpoofEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_SPOOF_ENABLED, false);
    }

    public static void setSpoofEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_SPOOF_ENABLED, enabled).apply();
    }

    // ── Spoof All Apps Toggle ──

    public static boolean isSpoofAllApps(Context context) {
        return getPrefs(context).getBoolean(KEY_SPOOF_ALL_APPS, false);
    }

    public static void setSpoofAllApps(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_SPOOF_ALL_APPS, enabled).apply();
    }

    // ── Global Active Profile ──

    public static String getActiveProfileId(Context context) {
        return getPrefs(context).getString(KEY_ACTIVE_PROFILE_ID, null);
    }

    public static void setActiveProfileId(Context context, String profileId) {
        getPrefs(context).edit().putString(KEY_ACTIVE_PROFILE_ID, profileId).apply();
    }

    public static void clearActiveProfile(Context context) {
        getPrefs(context).edit().remove(KEY_ACTIVE_PROFILE_ID).apply();
    }

    // ── Per-Package Profile Override ──

    /**
     * Returns the spoof profile ID saved for a specific game package.
     * Returns null if no per-package override is set (falls back to global profile).
     */
    public static String getProfileIdForPackage(Context context, String packageName) {
        if (packageName == null || packageName.isEmpty()) return null;
        return getPrefs(context).getString(PREFIX_PKG_PROFILE + packageName, null);
    }

    /**
     * Saves a spoof profile ID for a specific game package.
     * This overrides the global profile when that game is launched.
     */
    public static void setProfileIdForPackage(Context context, String packageName, String profileId) {
        if (packageName == null || packageName.isEmpty()) return;
        getPrefs(context).edit().putString(PREFIX_PKG_PROFILE + packageName, profileId).apply();
    }

    /**
     * Removes the per-package override for a game, so it falls back to the global profile.
     */
    public static void clearProfileForPackage(Context context, String packageName) {
        if (packageName == null || packageName.isEmpty()) return;
        getPrefs(context).edit().remove(PREFIX_PKG_PROFILE + packageName).apply();
    }

    /**
     * Resolves the effective profile ID for a package:
     * per-package override → global active profile → null.
     */
    public static String resolveProfileId(Context context, String packageName) {
        String pkgProfile = getProfileIdForPackage(context, packageName);
        if (pkgProfile != null && !pkgProfile.isEmpty()) return pkgProfile;
        return getActiveProfileId(context);
    }

    /**
     * Exposes all persisted spoof preferences as a key→value map.
     */
    public static Map<String, String> readAllPrefs(Context context) {
        Map<String, String> out = new HashMap<>();
        try {
            Map<String, ?> all = getPrefs(context).getAll();
            for (Map.Entry<String, ?> e : all.entrySet()) {
                Object v = e.getValue();
                if (v != null) out.put(e.getKey(), String.valueOf(v));
            }
        } catch (Throwable ignored) {}
        return out;
    }
}
