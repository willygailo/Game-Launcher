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
 * and applied via Shizuku (temporary full root) using the per-game patchers + Hz commands.
 *
 * Apply pipeline per profile:
 *   1. Force-write game config files via patcher.patchCompetitive()
 *   2. Inject super-fast touch into game config via patcher.applySuperFastTouch()
 *   3. Apply Shizuku system-level Hz force commands (if forceWriteSystemHz is enabled)
 *   4. Persist profile to SharedPreferences
 */
public class CfgProfileManager {

    private static final String TAG            = "CfgProfileManager";
    private static final String PREFS_NAME     = "game_booster_cfg_profiles";
    private static final String KEY_FPS_SUFFIX   = "_fps";
    private static final String KEY_TOUCH_SUFFIX = "_super_touch";
    private static final String KEY_HZ_SUFFIX    = "_force_hz";
    private static final String KEY_AIM_SUFFIX   = "_aim_assist";
    private static final String KEY_DMG_SUFFIX   = "_damage_script";
    private static final String KEY_RECOIL_SUFFIX = "_recoil_control";

    // ─── Supported game packages per game key ────────────────────────────────

    private static final List<String> MLBB_PACKAGES = Arrays.asList(
            "com.mobile.legends",
            "com.mobilelegends.mi",
            "com.vng.mlbbvn",
            "com.mobilelegends.na",
            "com.mobilelegends.hw",
            "com.mobile.legends.moonton",
            "com.mobile.legends.kr",
            "com.mobile.legends.jp"
    );

    private static final List<String> PUBGM_PACKAGES = Arrays.asList(
            "com.tencent.ig",
            "com.pubg.imobile",
            "com.vng.pubgmobile",
            "com.pubg.krmobile",
            "com.rekoo.pubgm",
            "com.tencent.tmgp.pubgmhd",
            "com.tencent.iglite",
            "com.pubg.newstate"
    );

    private static final List<String> CODM_PACKAGES = Arrays.asList(
            "com.activision.callofduty.shooter",
            "com.garena.game.codm",
            "com.tencent.tmgp.kr.codm",
            "com.vng.codmvn",
            "com.tencent.tmgp.cod"
    );

    private static final List<String> FREEFIRE_PACKAGES = Arrays.asList(
            "com.dts.freefireth",
            "com.dts.freefiremax"
    );

    private static final List<String> GENSHIN_PACKAGES = Arrays.asList(
            "com.miHoYo.GenshinImpact",
            "com.cognosphere.GenshinImpact",
            "com.HoYoverse.hkrpgoversea",
            "com.HoYoverse.nap",
            "com.miHoYo.bh3oversea"
    );

    private static final List<String> HOK_PACKAGES = Arrays.asList(
            "com.levelinfinite.sgameGlobal",
            "com.levelinfinite.sgameGlobal.gpkg",
            "com.tencent.tmgp.sgame",
            "com.garena.game.kgtw",
            "com.garena.game.kgvn",
            "com.garena.game.kgid",
            "com.riotgames.league.wildrift"
    );

    private static final List<String> ROBLOX_PACKAGES = Arrays.asList(
            "com.roblox.client"
    );

    // ─── Save / Load ─────────────────────────────────────────────────────────

    /** Saves a competitive profile to SharedPreferences. */
    public static void saveProfile(Context context, CompetitiveCfgProfile profile) {
        if (context == null || profile == null) return;
        SharedPreferences.Editor ed = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        String key = profile.getPrefsKey();
        ed.putInt    (key + KEY_FPS_SUFFIX,    CompetitiveCfgProfile.FPS_165);
        ed.putBoolean(key + KEY_TOUCH_SUFFIX,  profile.isSuperFastTouchEnabled());
        ed.putBoolean(key + KEY_HZ_SUFFIX,     profile.isForceWriteSystemHz());
        ed.putBoolean(key + KEY_AIM_SUFFIX,    profile.isAimAssistEnabled());
        ed.putBoolean(key + KEY_DMG_SUFFIX,    profile.isMlbbDamageScriptEnabled());
        ed.putBoolean(key + KEY_RECOIL_SUFFIX, profile.isRecoilControlEnabled());
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
        int fps          = CompetitiveCfgProfile.FPS_165;
        boolean touch    = prefs.getBoolean(key + KEY_TOUCH_SUFFIX, true);
        boolean forceHz  = prefs.getBoolean(key + KEY_HZ_SUFFIX, true);
        boolean aim      = prefs.getBoolean(key + KEY_AIM_SUFFIX, true);
        boolean dmg      = prefs.getBoolean(key + KEY_DMG_SUFFIX, true);
        boolean recoil   = prefs.getBoolean(key + KEY_RECOIL_SUFFIX, true);
        return new CompetitiveCfgProfile(gameKey, fps, touch, forceHz, aim, dmg, recoil);
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
            boolean ok = applyToPackage(pkg, profile);
            if (ok) patched++;
        }

        // System-level 165Hz force via Shizuku (applies globally, not per-package)
        if (profile.isForceWriteSystemHz()) {
            applyShizukuHzForce(165);
        }

        // Persist
        if (context != null) saveProfile(context, profile);

        Log.i(TAG, "CfgProfileManager applied " + gameKey + " profile to " + patched + " packages @ 165fps");
        return patched;
    }

    /**
     * Applies ALL game profiles across all 7 target games in one shot.
     *
     * @return total packages patched across all games
     */
    public static int applyAllGames(Context context, int targetFps, boolean superTouch, boolean forceHz) {
        int total = 0;
        for (String gameKey : new String[]{
                CompetitiveCfgProfile.GAME_MLBB,
                CompetitiveCfgProfile.GAME_PUBGM,
                CompetitiveCfgProfile.GAME_CODM,
                CompetitiveCfgProfile.GAME_FREEFIRE,
                CompetitiveCfgProfile.GAME_GENSHIN,
                CompetitiveCfgProfile.GAME_HOK,
                CompetitiveCfgProfile.GAME_ROBLOX}) {
            CompetitiveCfgProfile p = new CompetitiveCfgProfile(gameKey, 165, superTouch, forceHz, true, true, true);
            total += applyProfile(context, gameKey, p);
        }
        // One global Hz force for all
        if (forceHz) applyShizukuHzForce(165);
        return total;
    }

    // ─── Internal helpers ────────────────────────────────────────────────────

    private static boolean applyToPackage(String pkg, CompetitiveCfgProfile profile) {
        boolean result = false;
        String key = profile.getGameKey();
        final int fps = 165;

        if (CompetitiveCfgProfile.GAME_MLBB.equals(key)) {
            result = MlbbConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                MlbbConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isMlbbDamageScriptEnabled()) {
                MlbbConfigPatcher.applyDamageScriptConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_PUBGM.equals(key)) {
            result = PubgConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                PubgConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                PubgConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                PubgConfigPatcher.applyRecoilControlConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_CODM.equals(key)) {
            result = CodmConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                CodmConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                CodmConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                CodmConfigPatcher.applyRecoilControlConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_FREEFIRE.equals(key)) {
            result = FreeFireConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                FreeFireConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                FreeFireConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                FreeFireConfigPatcher.applyRecoilControlConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_GENSHIN.equals(key)) {
            result = GenshinConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                GenshinConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                GenshinConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                GenshinConfigPatcher.applyRecoilControlConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_HOK.equals(key)) {
            result = HokConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                HokConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                HokConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                HokConfigPatcher.applyRecoilControlConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_ROBLOX.equals(key)) {
            result = RobloxConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                RobloxConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                RobloxConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                RobloxConfigPatcher.applyRecoilControlConfig(pkg);
            }
        }
        return result;
    }

    /** Builds the 165Hz Shizuku force command string. */
    private static void applyShizukuHzForce(int hz) {
        final int forcedHz = 165;
        String cmd =
            "settings put system peak_refresh_rate " + forcedHz + ".0; " +
            "settings put system min_refresh_rate "  + forcedHz + ".0; " +
            "settings put system user_refresh_rate " + forcedHz + "; "   +
            "settings put global peak_refresh_rate " + forcedHz + ".0; " +
            "settings put global min_refresh_rate "  + forcedHz + ".0; " +
            "cmd game mode performance global; " +
            "cmd window set-app-refresh-rate global " + forcedHz + "; "  +
            "device_config put game_overlay global mode=2,fps=" + forcedHz + ":mode=3,fps=" + forcedHz + "; " +
            "service call SurfaceFlinger 1035 i32 " + forcedHz + "; "    +
            "service call SurfaceFlinger 1036 i32 " + forcedHz + "; "    +
            "setprop debug.sf.fps_limit "           + forcedHz + "; "    +
            "setprop persist.sys.NV_FPSLIMIT "      + forcedHz + "; "    +
            "setprop persist.sys.NV_POWERMODE 1; "                       +
            "setprop debug.gr.swapinterval 0";

        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(cmd);
        }
        Log.i(TAG, "Shizuku 165Hz force applied: " + forcedHz + "Hz");
    }

    private static List<String> getPackagesForKey(String gameKey) {
        switch (gameKey) {
            case CompetitiveCfgProfile.GAME_MLBB:     return MLBB_PACKAGES;
            case CompetitiveCfgProfile.GAME_PUBGM:    return PUBGM_PACKAGES;
            case CompetitiveCfgProfile.GAME_CODM:     return CODM_PACKAGES;
            case CompetitiveCfgProfile.GAME_FREEFIRE: return FREEFIRE_PACKAGES;
            case CompetitiveCfgProfile.GAME_GENSHIN:  return GENSHIN_PACKAGES;
            case CompetitiveCfgProfile.GAME_HOK:      return HOK_PACKAGES;
            case CompetitiveCfgProfile.GAME_ROBLOX:   return ROBLOX_PACKAGES;
            case CompetitiveCfgProfile.GAME_ALL:
                List<String> all = new java.util.ArrayList<>();
                all.addAll(MLBB_PACKAGES);
                all.addAll(PUBGM_PACKAGES);
                all.addAll(CODM_PACKAGES);
                all.addAll(FREEFIRE_PACKAGES);
                all.addAll(GENSHIN_PACKAGES);
                all.addAll(HOK_PACKAGES);
                all.addAll(ROBLOX_PACKAGES);
                return all;
            default: return new java.util.ArrayList<>();
        }
    }
}
