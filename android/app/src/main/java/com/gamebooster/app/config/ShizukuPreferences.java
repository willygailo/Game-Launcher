package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ShizukuPreferences — Persists Shizuku authorization, grant history, and offline state.
 * Ensures the APK remembers that Shizuku was granted even when Wi-Fi is disconnected
 * or during transient network switches between Wi-Fi and mobile data.
 */
public class ShizukuPreferences {

    private static final String PREF_NAME = "shizuku_state_prefs";
    private static final String KEY_EVER_GRANTED = "shizuku_ever_granted";
    private static final String KEY_LAST_ONLINE_TIME = "shizuku_last_online_time";
    private static final String KEY_AUTO_IMMUNITY_APPLIED = "shizuku_auto_immunity_applied";

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isShizukuEverGranted(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_EVER_GRANTED, false);
    }

    public static void setShizukuEverGranted(Context context, boolean granted) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_EVER_GRANTED, granted).apply();
    }

    public static boolean isAutoImmunityApplied(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_AUTO_IMMUNITY_APPLIED, false);
    }

    public static void setAutoImmunityApplied(Context context, boolean applied) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_AUTO_IMMUNITY_APPLIED, applied).apply();
    }

    public static void recordOnlineTimestamp(Context context) {
        if (context == null) return;
        getPrefs(context).edit().putLong(KEY_LAST_ONLINE_TIME, System.currentTimeMillis()).apply();
    }

    public static long getLastOnlineTimestamp(Context context) {
        if (context == null) return 0L;
        return getPrefs(context).getLong(KEY_LAST_ONLINE_TIME, 0L);
    }
}
