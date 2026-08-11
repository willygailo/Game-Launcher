package com.gamebooster.app.device;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * DisplayRefreshRatePreferences — persists the user-selected display refresh rate
 * across app restarts and reboots.
 *
 * <p>The chosen Hz is stored in SharedPreferences and re-applied via Shizuku
 * whenever the app starts or Shizuku reconnects.
 *
 * <p>A value of 0 means "no override set — use system default".
 */
public final class DisplayRefreshRatePreferences {

    private static final String PREFS_NAME  = "display_refresh_rate_prefs";
    private static final String KEY_USER_HZ = "user_selected_hz";
    private static final int    NO_OVERRIDE = 0;

    private DisplayRefreshRatePreferences() {}

    /**
     * Saves the user-chosen refresh rate.
     *
     * @param context Application context
     * @param hz      Refresh rate in Hz, or 0 to clear the override
     */
    public static void saveSelectedHz(Context context, int hz) {
        if (context == null) return;
        prefs(context).edit().putInt(KEY_USER_HZ, hz).apply();
    }

    /**
     * Returns the user-saved refresh rate, or 0 if no override has been set.
     *
     * @param context Application context
     */
    public static int getSelectedHz(Context context) {
        if (context == null) return NO_OVERRIDE;
        return prefs(context).getInt(KEY_USER_HZ, NO_OVERRIDE);
    }

    /**
     * Returns true if the user has set an explicit refresh rate override.
     */
    public static boolean hasOverride(Context context) {
        return getSelectedHz(context) > 0;
    }

    /**
     * Clears the saved override (resets to system-adaptive behaviour).
     */
    public static void clearOverride(Context context) {
        if (context == null) return;
        prefs(context).edit().remove(KEY_USER_HZ).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
