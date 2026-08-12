package com.gamebooster.app.platform.shizuku;

import android.content.Context;
import android.content.SharedPreferences;

public class ForceApplyPreferences {

    private static final String PREF_NAME = "force_apply_prefs";
    private static final String KEY_FORCE_APPLIED = "is_force_applied";
    private static final String KEY_LAST_APPLIED_TIME = "last_applied_timestamp";
    private static final String KEY_TARGET_HZ = "applied_target_hz";

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isForceApplied(Context context) {
        if (context == null) return false;
        return getPrefs(context).getBoolean(KEY_FORCE_APPLIED, false);
    }

    public static void setForceApplied(Context context, boolean applied, int targetHz) {
        if (context == null) return;
        getPrefs(context).edit()
                .putBoolean(KEY_FORCE_APPLIED, applied)
                .putLong(KEY_LAST_APPLIED_TIME, System.currentTimeMillis())
                .putInt(KEY_TARGET_HZ, targetHz)
                .apply();
    }

    public static long getLastAppliedTimestamp(Context context) {
        if (context == null) return 0L;
        return getPrefs(context).getLong(KEY_LAST_APPLIED_TIME, 0L);
    }

    public static int getAppliedTargetHz(Context context) {
        if (context == null) return 165;
        return getPrefs(context).getInt(KEY_TARGET_HZ, 165);
    }
}
