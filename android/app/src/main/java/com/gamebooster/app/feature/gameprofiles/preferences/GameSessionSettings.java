package com.gamebooster.app.feature.gameprofiles.preferences;

import android.content.Context;

import com.gamebooster.app.feature.performance.device.DevicePerformanceCapabilities;
import com.gamebooster.app.feature.performance.display.DisplayOverrideController;
import com.gamebooster.app.feature.games.space.GameSpaceDndManager;
import com.gamebooster.app.feature.performance.booster.HzFpsChannel;

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

        DisplayOverrideController.Result restore = DisplayOverrideController.restore(context);
        preferences.edit().clear().apply();
        return restore.isSuccess();
    }
}
