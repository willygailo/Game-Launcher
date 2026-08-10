package com.gamebooster.app.services;

import android.content.Context;
import android.content.SharedPreferences;

public class GameSchedulerPreferences {

    private static final String PREF_NAME = "game_scheduler_prefs";
    private static final String KEY_ENABLED = "scheduler_enabled";
    private static final String KEY_START_HOUR = "scheduler_start_hour"; // 0-23
    private static final String KEY_END_HOUR = "scheduler_end_hour";     // 0-23

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isSchedulerEnabled(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_ENABLED, false);
    }

    public static void setSchedulerEnabled(Context context, boolean enabled) {
        if (context == null) return;
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    public static int getStartHour(Context context) {
        if (context == null) return 20; // 8 PM default
        return getPrefs(context).getInt(KEY_START_HOUR, 20);
    }

    public static void setStartHour(Context context, int hour) {
        if (context == null) return;
        getPrefs(context).edit().putInt(KEY_START_HOUR, Math.max(0, Math.min(23, hour))).apply();
    }

    public static int getEndHour(Context context) {
        if (context == null) return 23; // 11 PM default
        return getPrefs(context).getInt(KEY_END_HOUR, 23);
    }

    public static void setEndHour(Context context, int hour) {
        if (context == null) return;
        getPrefs(context).edit().putInt(KEY_END_HOUR, Math.max(0, Math.min(23, hour))).apply();
    }
}
