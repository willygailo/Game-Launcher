package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.Arrays;
import java.util.List;

/**
 * CfgProfileManager — Manages saving, loading, and applying per-game competitive profiles.
 *
 * Each profile (MLBB / PUBGM / CODM / ALL) is independently stored in SharedPreferences
 * and translated into launcher-owned per-game preferences plus capability-checked Android
 * display/Game Mode requests.
 *
 * Apply pipeline per profile:
 *   1. Save the selected launcher profile for installed packages
 *   2. Request a supported native display mode/Game Mode
 *   3. Persist the profile to SharedPreferences
 */
public class CfgProfileManager {

    private static final String TAG            = "CfgProfileManager";
    private static final String PREFS_NAME     = "game_booster_cfg_profiles";
    private static final String KEY_FPS_SUFFIX   = "_fps";
    private static final String KEY_TOUCH_SUFFIX = "_super_touch";
    private static final String KEY_HZ_SUFFIX    = "_force_hz";

    // ─── Supported game packages per game key ────────────────────────────────

    private static final List<String> MLBB_PACKAGES = Arrays.asList(
            "com.mobile.legends",
            "com.mobilelegends.mi",
            "com.vng.mlbbvn",
            "com.mobilelegends.na"
    );

    private static final List<String> PUBGM_PACKAGES = Arrays.asList(
            "com.tencent.ig",
            "com.pubg.imobile",
            "com.vng.pubgmobile",
            "com.pubg.krmobile",
            "com.rekoo.pubgm",
            "com.tencent.tmgp.pubgmhd"
    );

    private static final List<String> CODM_PACKAGES = Arrays.asList(
            "com.activision.callofduty.shooter",
            "com.garena.game.codm",
            "com.tencent.tmgp.kr.codm",
            "com.vng.codmvn"
    );

    private static final List<String> HOK_PACKAGES = Arrays.asList(
            "com.levelinfinite.sgameGlobal",
            "com.tencent.tmgp.sgame"
    );

    private static final List<String> GENSHIN_PACKAGES = Arrays.asList(
            "com.miHoYo.GenshinImpact",
            "com.HoYoverse.hkrpg",
            "com.miHoYo.hkrpg"
    );

    private static final List<String> ROBLOX_PACKAGES = Arrays.asList(
            "com.roblox.client"
    );

    private static final List<String> FREEFIRE_PACKAGES = Arrays.asList(
            "com.dts.freefireth",
            "com.dts.freefiremax"
    );

    private static final List<String> WILDRIFT_PACKAGES = Arrays.asList(
            "com.riotgames.league.wildrift"
    );

    // ─── Save / Load ─────────────────────────────────────────────────────────

    /** Saves a competitive profile to SharedPreferences. */
    public static void saveProfile(Context context, CompetitiveCfgProfile profile) {
        if (context == null || profile == null) return;
        SharedPreferences.Editor ed = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        String key = profile.getPrefsKey();
        ed.putInt    (key + KEY_FPS_SUFFIX,   profile.getTargetFps());
        ed.putBoolean(key + KEY_TOUCH_SUFFIX, profile.isSuperFastTouchEnabled());
        ed.putBoolean(key + KEY_HZ_SUFFIX,    profile.isForceWriteSystemHz());
        ed.apply();
        Log.i(TAG, "Saved profile: " + profile);
    }

    /** Loads a competitive profile from SharedPreferences; returns default if not yet saved. */
    public static CompetitiveCfgProfile loadProfile(Context context, String gameKey) {
        if (context == null || gameKey == null) {
            return CompetitiveCfgProfile.defaultCompetitive(gameKey != null ? gameKey : CompetitiveCfgProfile.GAME_ALL);
        }
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "cfg_profile_" + gameKey.toLowerCase();
        int fps          = prefs.getInt(key + KEY_FPS_SUFFIX, CompetitiveCfgProfile.FPS_165);
        boolean touch    = prefs.getBoolean(key + KEY_TOUCH_SUFFIX, true);
        boolean forceHz  = prefs.getBoolean(key + KEY_HZ_SUFFIX, true);
        return new CompetitiveCfgProfile(gameKey, fps, touch, forceHz);
    }

    // ─── Apply ───────────────────────────────────────────────────────────────

    /**
     * Applies a competitive profile for ALL packages of the given game key.
     * Runs via Shizuku (temporary full root). Saves the profile on completion.
     *
     * @return number of packages successfully patched
     */
    public static int applyProfile(Context context, String gameKey, CompetitiveCfgProfile profile) {
        if (profile == null) return 0;
        int patched = 0;

        List<String> packages = getPackagesForKey(gameKey);
        for (String pkg : packages) {
            boolean ok = context != null &&
                    GameConfigPatcher.applyGameFpsPatch(context, pkg, profile.getTargetFps()).success;
            if (ok) patched++;
        }

        // Request only a native display rate; per-package Game Mode is handled by the launcher.
        if (profile.isForceWriteSystemHz()) {
            applyShizukuHzForce(context, profile.getTargetFps());
        }

        // Persist
        if (context != null) saveProfile(context, profile);

        Log.i(TAG, "CfgProfileManager applied " + gameKey + " profile to " + patched + " packages @ " + profile.getTargetFps() + "fps");
        return patched;
    }

    /**
     * Applies ALL game profiles (MLBB + PUBGM + CODM + HOK + Genshin + Roblox) in one shot.
     *
     * @return total packages patched across all games
     */
    public static int applyAllGames(Context context, int targetFps, boolean superTouch, boolean forceHz) {
        int total = 0;
        for (String gameKey : new String[]{
                CompetitiveCfgProfile.GAME_MLBB,
                CompetitiveCfgProfile.GAME_PUBGM,
                CompetitiveCfgProfile.GAME_CODM,
                CompetitiveCfgProfile.GAME_HOK,
                CompetitiveCfgProfile.GAME_GENSHIN,
                CompetitiveCfgProfile.GAME_ROBLOX,
                CompetitiveCfgProfile.GAME_FREEFIRE,
                CompetitiveCfgProfile.GAME_WILDRIFT}) {
            CompetitiveCfgProfile p = new CompetitiveCfgProfile(gameKey, targetFps, superTouch, forceHz);
            total += applyProfile(context, gameKey, p);
        }
        // One global Hz force for all
        if (forceHz) applyShizukuHzForce(context, targetFps);
        return total;
    }

    // ─── Internal helpers ────────────────────────────────────────────────────

    /** Applies a display preference only after confirming it is a native panel mode. */
    private static void applyShizukuHzForce(Context context, int hz) {
        if (context == null) return;
        com.gamebooster.app.engine.DisplayOverrideController.Result result =
                com.gamebooster.app.engine.DisplayOverrideController.applyDisplayRate(context, hz, null);
        Log.i(TAG, "Display-rate request: " + result.message);
    }

    private static List<String> getPackagesForKey(String gameKey) {
        switch (gameKey) {
            case CompetitiveCfgProfile.GAME_MLBB:     return MLBB_PACKAGES;
            case CompetitiveCfgProfile.GAME_PUBGM:    return PUBGM_PACKAGES;
            case CompetitiveCfgProfile.GAME_CODM:     return CODM_PACKAGES;
            case CompetitiveCfgProfile.GAME_HOK:      return HOK_PACKAGES;
            case CompetitiveCfgProfile.GAME_GENSHIN:  return GENSHIN_PACKAGES;
            case CompetitiveCfgProfile.GAME_ROBLOX:   return ROBLOX_PACKAGES;
            case CompetitiveCfgProfile.GAME_FREEFIRE: return FREEFIRE_PACKAGES;
            case CompetitiveCfgProfile.GAME_WILDRIFT: return WILDRIFT_PACKAGES;
            case CompetitiveCfgProfile.GAME_ALL:
                List<String> all = new java.util.ArrayList<>();
                all.addAll(MLBB_PACKAGES);
                all.addAll(PUBGM_PACKAGES);
                all.addAll(CODM_PACKAGES);
                all.addAll(HOK_PACKAGES);
                all.addAll(GENSHIN_PACKAGES);
                all.addAll(ROBLOX_PACKAGES);
                all.addAll(FREEFIRE_PACKAGES);
                all.addAll(WILDRIFT_PACKAGES);
                return all;
            default: return new java.util.ArrayList<>();
        }
    }
}
