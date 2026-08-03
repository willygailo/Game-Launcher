package com.gamebooster.app.games;

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
        if (context == null) return false;
        android.content.SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!preferences.contains(KEY_ACTIVE_PACKAGE)) return false;

        int previousHz = preferences.getInt(KEY_PREVIOUS_HZ, 60);
        boolean previousDnd = preferences.getBoolean(KEY_PREVIOUS_DND, false);
        boolean restored = HzFpsChannel.setRefreshRate(context, previousHz).success;
        GameSpaceDndManager.setGamingDndMode(context, previousDnd);
        preferences.edit().clear().apply();
        return restored;
    }
}
