package com.gamebooster.app.gamespace;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * Universal Game Space DND & Anti-Interruption Manager for Game Launcher PRO.
 * Manages heads-up notification blocking, Do-Not-Disturb state, adaptive brightness locks,
 * and gesture interference prevention across Android 12, 13, 14, 15, and 16.
 */
public class GameSpaceDndManager {

    private static final String PREF_NAME = "game_space_dnd_prefs";
    private static final String KEY_DND_ENABLED = "gaming_dnd_enabled";
    private static final String KEY_BRIGHTNESS_LOCK = "brightness_lock_enabled";
    private static final String KEY_GESTURE_GUARD = "gesture_guard_enabled";

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

        // 1. Toggle heads-up notifications (banner popups) via Shizuku or System Settings
        String headsUpCmd = "settings put global heads_up_notifications_enabled " + (enable ? "0" : "1");
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(headsUpCmd);
        } else {
            CommandExecutor.executeSystemCommand(headsUpCmd);
        }

        // 2. Toggle system DND via Shizuku or NotificationManager policy
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand("cmd notification set_zen_mode " + (enable ? "1" : "0"));
            }
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
            String brightCmd = "settings put system screen_brightness_mode 0";
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(brightCmd);
            } else {
                CommandExecutor.executeSystemCommand(brightCmd);
            }
        }
        return true;
    }

    public static boolean isGestureGuardActive(Context context) {
        if (context == null) return false;
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_GESTURE_GUARD, false);
    }

    public static boolean setGestureGuard(Context context, boolean enable) {
        if (context == null) return false;

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_GESTURE_GUARD, enable)
                .apply();

        String guardCmd = enable ?
                "setprop sys.gamespace.gesture_guard 1; settings put system three_finger_screenshot 0 2>/dev/null" :
                "setprop sys.gamespace.gesture_guard 0; settings put system three_finger_screenshot 1 2>/dev/null";

        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(guardCmd);
        } else {
            CommandExecutor.executeSystemCommand(guardCmd);
        }
        return true;
    }
}
