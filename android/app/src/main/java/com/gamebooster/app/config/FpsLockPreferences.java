package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * FpsLockPreferences — Persists per-game FPS lock settings in SharedPreferences.
 *
 * Hard-locked to 185 FPS only. All reads return 185 and all writes store 185
 * regardless of the value provided by the caller.
 *
 * Supported game keys:
 *   "mlbb", "pubgm", "codm", "freefire", "genshin", "hok", "roblox",
 *   "valorant", "farlight", "bloodstrike", "standoff2", "wildrift",
 *   "carx", "arenabreakout", "supercell", "all"
 */
public class FpsLockPreferences {

    private static final String PREFS_NAME = "game_booster_fps_locks";
    private static final String KEY_PREFIX = "fps_lock_";
    public static final int DEFAULT_FPS = FpsUnlockTier.FPS_185.fps;

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves the FPS lock setting for a given game key.
     * Always stores 185 regardless of the fps parameter.
     *
     * @param context Application context
     * @param gameKey Identifier for the game (e.g. "pubgm", "mlbb")
     * @param fps     Ignored — always stored as 185
     */
    public static void saveFpsLock(Context context, String gameKey, int fps) {
        if (context == null || gameKey == null) return;
        getPrefs(context).edit()
                .putInt(KEY_PREFIX + gameKey.toLowerCase().trim(), DEFAULT_FPS)
                .apply();
    }

    /**
     * Saves the FPS tier for a given game key.
     * Always saves FPS_185 regardless of the tier provided.
     */
    public static void saveFpsTier(Context context, String gameKey, FpsUnlockTier tier) {
        if (context == null || gameKey == null) return;
        saveFpsLock(context, gameKey, DEFAULT_FPS);
    }

    /**
     * Retrieves the saved FPS lock for a given game key.
     * Always returns 185 — hard-locked.
     */
    public static int getFpsLock(Context context, String gameKey) {
        return DEFAULT_FPS;
    }

    /**
     * Retrieves the {@link FpsUnlockTier} for a given game key.
     * Always returns FPS_185.
     */
    public static FpsUnlockTier getFpsTier(Context context, String gameKey) {
        return FpsUnlockTier.FPS_185;
    }

    /**
     * Returns all available FPS tier values as an array.
     * Always returns {185}.
     */
    public static int[] getAvailableTiers() {
        return FpsUnlockTier.getAllFpsValues();
    }

    /**
     * Returns all available FPS tier labels for UI display.
     * Always returns {"185fps"}.
     */
    public static String[] getAvailableLabels() {
        return FpsUnlockTier.getAllLabels();
    }

    /**
     * Resets the FPS lock for a specific game key back to default (185 FPS).
     */
    public static void resetFpsLock(Context context, String gameKey) {
        if (context == null || gameKey == null) return;
        getPrefs(context).edit().remove(KEY_PREFIX + gameKey.toLowerCase().trim()).apply();
    }

    /**
     * Clears all saved FPS locks for all games.
     */
    public static void clearAll(Context context) {
        if (context == null) return;
        getPrefs(context).edit().clear().apply();
    }
}
