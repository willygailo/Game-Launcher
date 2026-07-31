package com.gamebooster.app.functions;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.gamebooster.app.root.CommandExecutor;

public class GameSpaceDndManager {

    private static final String PREF_NAME = "game_space_dnd_prefs";
    private static final String KEY_DND_ENABLED = "gaming_dnd_enabled";
    private static final String KEY_BRIGHTNESS_LOCK = "brightness_lock_enabled";

    public static boolean isDndActive(Context context) {
        if (context == null) return false;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_DND_ENABLED, false);
    }

    public static boolean setGamingDndMode(Context context, boolean enable) {
        if (context == null) return false;

        // Persist setting
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DND_ENABLED, enable)
                .apply();

        // Toggle heads-up notifications (banner popups) via ADB/Shizuku or System Settings
        CommandExecutor.executeSystemCommand(
                "settings put global heads_up_notifications_enabled " + (enable ? "0" : "1")
        );

        // Toggle system DND if permission granted
        try {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (nm.isNotificationPolicyAccessGranted()) {
                    nm.setInterruptionFilter(enable ? NotificationManager.INTERRUPTION_FILTER_PRIORITY : NotificationManager.INTERRUPTION_FILTER_ALL);
                }
            }
        } catch (Throwable ignored) {}

        return true;
    }

    public static boolean isBrightnessLockActive(Context context) {
        if (context == null) return false;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_BRIGHTNESS_LOCK, false);
    }

    public static boolean setBrightnessLock(Context context, boolean enable) {
        if (context == null) return false;

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_BRIGHTNESS_LOCK, enable)
                .apply();

        if (enable) {
            // Disable auto brightness via ADB/Shizuku or system settings
            CommandExecutor.executeSystemCommand("settings put system screen_brightness_mode 0");
        }
        return true;
    }
}
