package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.Arrays;
import java.util.List;

/**
 * CfgProfileManager — Manages saving, loading, and applying per-game competitive profiles.
 *
 * Each profile (MLBB / PUBGM / CODM / FREEFIRE / GENSHIN_WILDRIFT / ALL) is independently stored in SharedPreferences
 * and applied via Shizuku/Root using dedicated per-game patchers + MaxHzForceChannel for zero duplication.
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

    private static final List<String> MLBB_PACKAGES = Arrays.asList(
            "com.mobile.legends",
            "com.mobile.legends.vng",
            "com.mobile.legends.id",
            "com.mobilelegends.win"
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

    private static final List<String> FREEFIRE_PACKAGES = Arrays.asList(
            "com.dts.freefireth",
            "com.dts.freefiremax"
    );

    private static final List<String> GENSHIN_WILDRIFT_PACKAGES = Arrays.asList(
            "com.riotgames.league.wildrift",
            "com.cognosphere.GenshinImpact",
            "com.HoYoverse.hkrpgoversea"
    );

    public static void saveProfile(Context context, CompetitiveCfgProfile profile) {
        if (context == null || profile == null) return;
        SharedPreferences.Editor ed = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        String key = profile.getPrefsKey();
        ed.putInt    (key + KEY_FPS_SUFFIX,    profile.getTargetFps());
        ed.putBoolean(key + KEY_TOUCH_SUFFIX,  profile.isSuperFastTouchEnabled());
        ed.putBoolean(key + KEY_HZ_SUFFIX,     profile.isForceWriteSystemHz());
        ed.putBoolean(key + KEY_AIM_SUFFIX,    profile.isAimAssistEnabled());
        ed.putBoolean(key + KEY_DMG_SUFFIX,    profile.isMlbbDamageScriptEnabled());
        ed.putBoolean(key + KEY_RECOIL_SUFFIX, profile.isRecoilControlEnabled());
        ed.apply();
        Log.i(TAG, "Saved profile: " + profile);
    }

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
        boolean aim      = prefs.getBoolean(key + KEY_AIM_SUFFIX, true);
        boolean dmg      = prefs.getBoolean(key + KEY_DMG_SUFFIX, true);
        boolean recoil   = prefs.getBoolean(key + KEY_RECOIL_SUFFIX, true);
        return new CompetitiveCfgProfile(gameKey, fps, touch, forceHz, aim, dmg, recoil);
    }

    public static int applyProfile(Context context, String gameKey, CompetitiveCfgProfile profile) {
        if (profile == null) return 0;
        int patched = 0;

        List<String> packages = getPackagesForKey(gameKey);
        for (String pkg : packages) {
            boolean ok = applyToPackage(pkg, profile);
            if (ok) patched++;
        }

        if (profile.isForceWriteSystemHz()) {
            MaxHzForceChannel.forceApply(profile.getTargetFps());
        }

        if (context != null) saveProfile(context, profile);

        Log.i(TAG, "CfgProfileManager applied " + gameKey + " profile to " + patched + " packages @ " + profile.getTargetFps() + "fps");
        return patched;
    }

    public static int applyAllGames(Context context, int targetFps, boolean superTouch, boolean forceHz) {
        int total = 0;
        for (String gameKey : new String[]{
                CompetitiveCfgProfile.GAME_MLBB,
                CompetitiveCfgProfile.GAME_PUBGM,
                CompetitiveCfgProfile.GAME_CODM,
                "FREEFIRE",
                "GENSHIN_WILDRIFT"}) {
            CompetitiveCfgProfile p = new CompetitiveCfgProfile(gameKey, targetFps, superTouch, forceHz, true, true, true);
            total += applyProfile(context, gameKey, p);
        }
        if (forceHz) MaxHzForceChannel.forceApply(targetFps);
        return total;
    }

    private static boolean applyToPackage(String pkg, CompetitiveCfgProfile profile) {
        boolean result = false;
        String key = profile.getGameKey();
        int fps = profile.getTargetFps();

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
        } else if ("FREEFIRE".equals(key)) {
            result = FreeFireConfigPatcher.patchCompetitive(pkg, fps);
        } else if ("GENSHIN_WILDRIFT".equals(key)) {
            result = GenshinWildRiftConfigPatcher.patchCompetitive(pkg, fps);
        }
        return result;
    }

    private static List<String> getPackagesForKey(String gameKey) {
        switch (gameKey) {
            case CompetitiveCfgProfile.GAME_MLBB:  return MLBB_PACKAGES;
            case CompetitiveCfgProfile.GAME_PUBGM: return PUBGM_PACKAGES;
            case CompetitiveCfgProfile.GAME_CODM:  return CODM_PACKAGES;
            case "FREEFIRE":                       return FREEFIRE_PACKAGES;
            case "GENSHIN_WILDRIFT":              return GENSHIN_WILDRIFT_PACKAGES;
            case CompetitiveCfgProfile.GAME_ALL:
                List<String> all = new java.util.ArrayList<>();
                all.addAll(MLBB_PACKAGES);
                all.addAll(PUBGM_PACKAGES);
                all.addAll(CODM_PACKAGES);
                all.addAll(FREEFIRE_PACKAGES);
                all.addAll(GENSHIN_WILDRIFT_PACKAGES);
                return all;
            default: return new java.util.ArrayList<>();
        }
    }
}
