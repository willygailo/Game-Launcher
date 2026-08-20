package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * FpsLockPreferences — Persists per-game FPS lock and Ultra Extreme Graphics settings in SharedPreferences.
 *
 * Supported game keys:
 *   "mlbb", "pubgm", "codm", "freefire", "genshin", "hok", "roblox",
 *   "valorant", "farlight", "bloodstrike", "standoff2", "wildrift",
 *   "carx", "arenabreakout", "supercell", "all"
 */
public class FpsLockPreferences {

    private static final String PREFS_NAME = "game_booster_fps_locks";
    private static final String KEY_PREFIX = "fps_lock_";
    private static final String KEY_GRAPHICS_PREFIX = "graphics_mode_";
    public static final String GRAPHICS_ULTRA_EXTREME = "ultra_extreme";

    public static final int DEFAULT_FPS = FpsUnlockTier.FPS_185.fps;

    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Saves the FPS lock setting for a given game key.
     *
     * @param context Application context
     * @param gameKey Identifier for the game (e.g. "pubgm", "mlbb")
     * @param fps     Target FPS; aligned to the nearest supported tier (120, 144, 165, 185)
     */
    public static void saveFpsLock(Context context, String gameKey, int fps) {
        if (context == null || gameKey == null) return;
        getPrefs(context).edit()
                .putInt(KEY_PREFIX + gameKey.toLowerCase().trim(), FpsUnlockTier.resolveTargetFps(fps))
                .apply();
    }

    /**
     * Saves the FPS tier for a given game key.
     */
    public static void saveFpsTier(Context context, String gameKey, FpsUnlockTier tier) {
        if (context == null || gameKey == null || tier == null) return;
        saveFpsLock(context, gameKey, tier.fps);
    }

    /**
     * Retrieves the saved FPS lock for a given game key.
     * Falls back to the default (185 FPS) when nothing was saved.
     */
    public static int getFpsLock(Context context, String gameKey) {
        if (context == null || gameKey == null) return DEFAULT_FPS;
        return getPrefs(context).getInt(KEY_PREFIX + gameKey.toLowerCase().trim(), DEFAULT_FPS);
    }

    /**
     * Retrieves the {@link FpsUnlockTier} for a given game key.
     */
    public static FpsUnlockTier getFpsTier(Context context, String gameKey) {
        return FpsUnlockTier.fromFps(getFpsLock(context, gameKey));
    }

    /**
     * Saves the Graphics Mode for a given game key (e.g. "ultra_extreme").
     */
    public static void saveGraphicsMode(Context context, String gameKey, String mode) {
        if (context == null || gameKey == null) return;
        getPrefs(context).edit()
                .putString(KEY_GRAPHICS_PREFIX + gameKey.toLowerCase().trim(), mode != null ? mode : GRAPHICS_ULTRA_EXTREME)
                .apply();
    }

    /**
     * Retrieves the Graphics Mode for a given game key. Defaults to "ultra_extreme".
     */
    public static String getGraphicsMode(Context context, String gameKey) {
        if (context == null || gameKey == null) return GRAPHICS_ULTRA_EXTREME;
        return getPrefs(context).getString(KEY_GRAPHICS_PREFIX + gameKey.toLowerCase().trim(), GRAPHICS_ULTRA_EXTREME);
    }

    /**
     * Returns true if Ultra Extreme Graphics is active for the given game key.
     */
    public static boolean isUltraExtreme(Context context, String gameKey) {
        return GRAPHICS_ULTRA_EXTREME.equalsIgnoreCase(getGraphicsMode(context, gameKey));
    }

    /**
     * Returns all available FPS tier values as an array.
     */
    public static int[] getAvailableTiers() {
        return FpsUnlockTier.getAllFpsValues();
    }

    /**
     * Returns all available FPS tier labels for UI display.
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
