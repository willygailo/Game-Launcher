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

    private static final List<String> VALORANT_PACKAGES = Arrays.asList(
            "com.tencent.tmgp.projectc",
            "com.riotgames.valorantmobile",
            "com.tencent.tmgp.valorant",
            "com.riotgames.valorant"
    );

    private static final List<String> FARLIGHT_PACKAGES = Arrays.asList(
            "com.miracle.farlight84",
            "com.miraclegames.farlight84",
            "com.farlightgames.farlight84.gp",
            "com.farlightgames.farlight84.global"
    );

    private static final List<String> DELTAFORCE_PACKAGES = Arrays.asList(
            "com.proximabeta.mf.uamo"
    );

    private static final List<String> WUTHERING_PACKAGES = Arrays.asList(
            "com.kurogame.wutheringwaves.global"
    );

    private static final List<String> CARX_PACKAGES = Arrays.asList(
            "com.carxtech.sr",
            "com.h20.carxstreet"
    );

    private static final List<String> APEX_PACKAGES = Arrays.asList(
            "com.ea.gp.apexlegendsmobilecms"
    );

    // ─── Save / Load ─────────────────────────────────────────────────────────

    /** Saves a competitive profile to SharedPreferences. */
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

    /** Loads a competitive profile from SharedPreferences; returns default if not yet saved. */
    public static CompetitiveCfgProfile loadProfile(Context context, String gameKey) {
        if (context == null || gameKey == null) {
            return CompetitiveCfgProfile.defaultCompetitive(gameKey != null ? gameKey : CompetitiveCfgProfile.GAME_ALL);
        }
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = "cfg_profile_" + gameKey.toLowerCase();
        int fps          = prefs.getInt(key + KEY_FPS_SUFFIX, CompetitiveCfgProfile.FPS_185);
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

        // System-level refresh rate force via Shizuku (applies globally, not per-package)
        if (profile.isForceWriteSystemHz()) {
            applyShizukuHzForce(profile.getTargetFps());
        }

        // Persist
        if (context != null) saveProfile(context, profile);

        Log.i(TAG, "CfgProfileManager applied " + gameKey + " profile to " + patched + " packages @ " + profile.getTargetFps() + "fps");
        return patched;
    }

    /**
     * Applies ALL game profiles across all target games in one shot.
     *
     * @return total packages patched across all games
     */
    public static int applyAllGames(Context context, int targetFps, boolean superTouch, boolean forceHz) {
        int total = 0;
        int effectiveFps = targetFps > 0 ? targetFps : CompetitiveCfgProfile.FPS_185;
        for (String gameKey : new String[]{
                CompetitiveCfgProfile.GAME_MLBB,
                CompetitiveCfgProfile.GAME_PUBGM,
                CompetitiveCfgProfile.GAME_CODM,
                CompetitiveCfgProfile.GAME_FREEFIRE,
                CompetitiveCfgProfile.GAME_GENSHIN,
                CompetitiveCfgProfile.GAME_HOK,
                CompetitiveCfgProfile.GAME_ROBLOX,
                CompetitiveCfgProfile.GAME_VALORANT,
                CompetitiveCfgProfile.GAME_FARLIGHT}) {
            CompetitiveCfgProfile p = new CompetitiveCfgProfile(gameKey, effectiveFps, superTouch, forceHz, true, true, true);
            total += applyProfile(context, gameKey, p);
        }
        // One global Hz force for all
        if (forceHz) applyShizukuHzForce(effectiveFps);
        return total;
    }

    // ─── Internal helpers ────────────────────────────────────────────────────

    private static boolean applyToPackage(String pkg, CompetitiveCfgProfile profile) {
        boolean result = false;
        String key = profile.getGameKey();
        final int fps = profile.getTargetFps() > 0 ? profile.getTargetFps() : 185;

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
        } else if (CompetitiveCfgProfile.GAME_HOK.equals(key)) {
            result = HokConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                HokConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                HokConfigPatcher.applyAimAssistConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_ROBLOX.equals(key)) {
            result = RobloxConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                RobloxConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                RobloxConfigPatcher.applyAimAssistConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_VALORANT.equals(key)) {
            result = ValorantConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                ValorantConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                ValorantConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                ValorantConfigPatcher.applyRecoilControlConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_FARLIGHT.equals(key)) {
            result = FarlightConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                FarlightConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                FarlightConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                FarlightConfigPatcher.applyRecoilControlConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_DELTAFORCE.equals(key)) {
            result = ValorantConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                ValorantConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                ValorantConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                ValorantConfigPatcher.applyRecoilControlConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_WUTHERING.equals(key)) {
            result = GenshinConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                GenshinConfigPatcher.applySuperFastTouch(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_CARX.equals(key)) {
            result = FarlightConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                FarlightConfigPatcher.applySuperFastTouch(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_APEX.equals(key)) {
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
        } else {
            // GAME_ALL: patch all target games with this profile
            result  = MlbbConfigPatcher.patchCompetitive(pkg, fps);
            result |= PubgConfigPatcher.patchCompetitive(pkg, fps);
            result |= CodmConfigPatcher.patchCompetitive(pkg, fps);
            result |= FreeFireConfigPatcher.patchCompetitive(pkg, fps);
            result |= GenshinConfigPatcher.patchCompetitive(pkg, fps);
            result |= HokConfigPatcher.patchCompetitive(pkg, fps);
            result |= RobloxConfigPatcher.patchCompetitive(pkg, fps);
            result |= ValorantConfigPatcher.patchCompetitive(pkg, fps);
            result |= FarlightConfigPatcher.patchCompetitive(pkg, fps);
        }

        return result;
    }

    /** Builds and applies the dynamic Shizuku force command for target Hz (120/144/165/185). */
    private static void applyShizukuHzForce(int hz) {
        final int forcedHz = hz > 0 ? hz : 185;
        if (ShizukuExecutor.hasShizukuPermission()) {
            MaxHzForceChannel.forceApply(forcedHz);
        }
        Log.i(TAG, "Shizuku force applied: " + forcedHz + "Hz");
    }

    private static List<String> getPackagesForKey(String gameKey) {
        switch (gameKey) {
            case CompetitiveCfgProfile.GAME_MLBB:       return MLBB_PACKAGES;
            case CompetitiveCfgProfile.GAME_PUBGM:      return PUBGM_PACKAGES;
            case CompetitiveCfgProfile.GAME_CODM:       return CODM_PACKAGES;
            case CompetitiveCfgProfile.GAME_FREEFIRE:   return FREEFIRE_PACKAGES;
            case CompetitiveCfgProfile.GAME_GENSHIN:    return GENSHIN_PACKAGES;
            case CompetitiveCfgProfile.GAME_HOK:        return HOK_PACKAGES;
            case CompetitiveCfgProfile.GAME_ROBLOX:     return ROBLOX_PACKAGES;
            case CompetitiveCfgProfile.GAME_VALORANT:   return VALORANT_PACKAGES;
            case CompetitiveCfgProfile.GAME_FARLIGHT:   return FARLIGHT_PACKAGES;
            case CompetitiveCfgProfile.GAME_DELTAFORCE: return DELTAFORCE_PACKAGES;
            case CompetitiveCfgProfile.GAME_WUTHERING:  return WUTHERING_PACKAGES;
            case CompetitiveCfgProfile.GAME_CARX:       return CARX_PACKAGES;
            case CompetitiveCfgProfile.GAME_APEX:       return APEX_PACKAGES;
            case CompetitiveCfgProfile.GAME_ALL:
                List<String> all = new java.util.ArrayList<>();
                all.addAll(MLBB_PACKAGES);
                all.addAll(PUBGM_PACKAGES);
                all.addAll(CODM_PACKAGES);
                all.addAll(FREEFIRE_PACKAGES);
                all.addAll(GENSHIN_PACKAGES);
                all.addAll(HOK_PACKAGES);
                all.addAll(ROBLOX_PACKAGES);
                all.addAll(VALORANT_PACKAGES);
                all.addAll(FARLIGHT_PACKAGES);
                all.addAll(DELTAFORCE_PACKAGES);
                all.addAll(WUTHERING_PACKAGES);
                all.addAll(CARX_PACKAGES);
                all.addAll(APEX_PACKAGES);
                return all;
            default: return new java.util.ArrayList<>();
        }
    }
}
