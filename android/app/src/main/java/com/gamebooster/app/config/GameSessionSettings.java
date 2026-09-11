package com.gamebooster.app.config;

import android.content.Context;

import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.booster.HzFpsChannel;

/** Saves the user's display and DND preferences before a configured game starts. */
public final class GameSessionSettings {

    private static final String PREF_NAME = "game_session_restore";
    private static final String KEY_ACTIVE_PACKAGE = "active_package";
    private static final String KEY_PREVIOUS_HZ = "previous_hz";
    private static final String KEY_PREVIOUS_DND = "previous_dnd";

    private GameSessionSettings() {}

    public static void begin(Context context, String packageName) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) return;
        android.content.SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (preferences.contains(KEY_ACTIVE_PACKAGE)) return;

        int currentHz = DevicePerformanceCapabilities.detect(context).getCurrentRefreshRate();
        preferences.edit()
                .putString(KEY_ACTIVE_PACKAGE, packageName)
                .putInt(KEY_PREVIOUS_HZ, currentHz)
                .putBoolean(KEY_PREVIOUS_DND, GameSpaceDndManager.isDndActive(context))
                .apply();
    }

    public static boolean restore(Context context) {
        closeSession(context);
        return true;
    }

    public static boolean hasActiveSession(Context context) {
        if (context == null) return false;
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .contains(KEY_ACTIVE_PACKAGE);
    }

    public static int getStoredPreviousHz(Context context) {
        if (context == null) return 0;
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_PREVIOUS_HZ, 0);
    }

    public static boolean getStoredPreviousDnd(Context context) {
        if (context == null) return false;
        return context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_PREVIOUS_DND, false);
    }

    public static void closeSession(Context context) {
        if (context == null) return;
        context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }
}
