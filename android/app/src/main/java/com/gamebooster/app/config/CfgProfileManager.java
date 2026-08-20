package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;

import java.util.Arrays;
import java.util.List;

/**
 * CfgProfileManager — Manages saving, loading, and applying per-game competitive profiles.
 *
 * Each profile (MLBB / PUBGM / CODM / ALL) is independently stored in SharedPreferences
 * and applied via Shizuku (temporary full root) using the per-game patchers + Hz commands.
 *
 * Apply pipeline per profile:
 *   1. Resolve dynamic config paths and ensure directory scaffolding via GameConfigPathResolver
 *   2. Force-write game config files via patcher.patchCompetitive()
 *   3. Inject super-fast zero-delay touch into game config via patcher.applySuperFastTouch()
 *   4. Apply Aim Assist, Recoil Control, and Damage Scripts
 *   5. Inject active hardware spoof profile if enabled
 *   6. Apply Shizuku system-level Hz force commands (if forceWriteSystemHz is enabled)
 *   7. Persist profile to SharedPreferences
 */
public class CfgProfileManager {

    private static final String TAG              = "CfgProfileManager";
    private static final String PREFS_NAME       = "game_booster_cfg_profiles";
    private static final String KEY_FPS_SUFFIX   = "_fps";
    private static final String KEY_TOUCH_SUFFIX = "_super_touch";
    private static final String KEY_HZ_SUFFIX    = "_force_hz";
    private static final String KEY_AIM_SUFFIX   = "_aim_assist";
    private static final String KEY_DMG_SUFFIX   = "_damage_script";
    private static final String KEY_RECOIL_SUFFIX = "_recoil_control";
    private static final String KEY_MASK_SUFFIX  = "_hardware_mask";
    private static final String KEY_ANTILOG_SUFFIX = "_anti_log";

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
            "com.garena.game.kgid"
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

    private static final List<String> BLOODSTRIKE_PACKAGES = Arrays.asList(
            "com.netease.bloodstrike",
            "com.netease.newspike"
    );

    private static final List<String> STANDOFF2_PACKAGES = Arrays.asList(
            "com.axlebolt.standoff2"
    );

    private static final List<String> WILDRIFT_PACKAGES = Arrays.asList(
            "com.riotgames.league.wildrift",
            "com.riotgames.league.wildrifttw",
            "com.riotgames.league.wildriftvn"
    );

    private static final List<String> CARX_PACKAGES = Arrays.asList(
            "com.h20.carxstreet",
            "com.gameloft.anmp.android.glofta9hm",
            "com.ea.games.r3_row",
            "com.garena.game.fdtw"
    );

    private static final List<String> ARENABREAKOUT_PACKAGES = Arrays.asList(
            "com.proximabeta.mf.uamo",
            "com.levelinfinite.deltaforce"
    );

    private static final List<String> SUPERCELL_PACKAGES = Arrays.asList(
            "com.supercell.brawlstars",
            "com.supercell.clashroyale",
            "com.supercell.clashofclans",
            "com.supercell.squad"
    );

    // ─── Save / Load ─────────────────────────────────────────────────────────

    /**
     * Resolves the competitive game key for a package name using the registered
     * per-game package lists. Returns GAME_ALL when no game matches.
     */
    public static String resolveGameKey(String packageName) {
        if (packageName == null) return CompetitiveCfgProfile.GAME_ALL;
        String pkg = packageName.toLowerCase();
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) return CompetitiveCfgProfile.GAME_MLBB;
        if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) return CompetitiveCfgProfile.GAME_PUBGM;
        if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone")) return CompetitiveCfgProfile.GAME_CODM;
        if (pkg.contains("freefire") || pkg.contains("dts.freefire")) return CompetitiveCfgProfile.GAME_FREEFIRE;
        if (pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") || pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap")) return CompetitiveCfgProfile.GAME_GENSHIN;
        if (pkg.contains("wildrift") || pkg.contains("riotgames.league")) return CompetitiveCfgProfile.GAME_WILDRIFT;
        if (pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor") || pkg.contains("kgtw") || pkg.contains("kgvn")) return CompetitiveCfgProfile.GAME_HOK;
        if (pkg.contains("bloodstrike") || pkg.contains("newspike")) return CompetitiveCfgProfile.GAME_BLOODSTRIKE;
        if (pkg.contains("standoff2") || pkg.contains("axlebolt")) return CompetitiveCfgProfile.GAME_STANDOFF2;
        if (pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row")) return CompetitiveCfgProfile.GAME_CARX;
        if (pkg.contains("uamo") || pkg.contains("arenabreakout") || pkg.contains("deltaforce")) return CompetitiveCfgProfile.GAME_ARENABREAKOUT;
        if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashroyale") || pkg.contains("clashofclans")) return CompetitiveCfgProfile.GAME_SUPERCELL;
        if (pkg.contains("roblox")) return CompetitiveCfgProfile.GAME_ROBLOX;
        if (pkg.contains("projectc") || pkg.contains("valorant")) return CompetitiveCfgProfile.GAME_VALORANT;
        if (pkg.contains("farlight") || pkg.contains("solarland")) return CompetitiveCfgProfile.GAME_FARLIGHT;
        return CompetitiveCfgProfile.GAME_ALL;
    }

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
        ed.putBoolean(key + KEY_MASK_SUFFIX,   profile.isHardwareMaskEnabled());
        ed.putBoolean(key + KEY_ANTILOG_SUFFIX, profile.isAntiLogEnabled());
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
        boolean mask     = prefs.getBoolean(key + KEY_MASK_SUFFIX, true);
        boolean antiLog  = prefs.getBoolean(key + KEY_ANTILOG_SUFFIX, true);
        return new CompetitiveCfgProfile(gameKey, fps, touch, forceHz, aim, dmg, recoil, true, true, mask, antiLog);
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
            // Ensure parent directory scaffolding via GameConfigPathResolver
            List<String> resolvedPaths = GameConfigPathResolver.getPathsForGame(pkg);
            GameConfigPathResolver.ensureDirectoriesForPaths(resolvedPaths);

            // Safety net: capture true originals before competitive patching overwrites them
            ConfigBackupManager.backupPackage(pkg, resolvedPaths);

            // Anti-Cheat Auto-Bypass & Telemetry Neutralization
            com.gamebooster.app.anticheat.GameAntiCheatBypassEngine.applyBypassAndNeutralize(context, pkg);

            boolean ok = applyToPackage(pkg, profile);
            if (ok) {
                patched++;
                if (profile.isHardwareMaskEnabled() && context != null && com.gamebooster.app.spoofer.SpoofPreferences.isSpoofEnabled(context)) {
                    DeviceSpooferEngine.applySpoofing(context, pkg);
                }
                // Stealth permission normalization
                com.gamebooster.app.anticheat.GameAntiCheatBypassEngine.applyBypassAndNeutralize(context, pkg);
            }
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
        final int effectiveFps = FpsUnlockTier.resolveTargetFps(targetFps);
        // Purge system logcat and background log friction
        AntiLogPatcher.applySystemAntiLog();

        for (String gameKey : new String[]{
                CompetitiveCfgProfile.GAME_MLBB,
                CompetitiveCfgProfile.GAME_PUBGM,
                CompetitiveCfgProfile.GAME_CODM,
                CompetitiveCfgProfile.GAME_FREEFIRE,
                CompetitiveCfgProfile.GAME_GENSHIN,
                CompetitiveCfgProfile.GAME_HOK,
                CompetitiveCfgProfile.GAME_ROBLOX,
                CompetitiveCfgProfile.GAME_VALORANT,
                CompetitiveCfgProfile.GAME_FARLIGHT,
                CompetitiveCfgProfile.GAME_BLOODSTRIKE,
                CompetitiveCfgProfile.GAME_STANDOFF2,
                CompetitiveCfgProfile.GAME_WILDRIFT,
                CompetitiveCfgProfile.GAME_CARX,
                CompetitiveCfgProfile.GAME_ARENABREAKOUT,
                CompetitiveCfgProfile.GAME_SUPERCELL}) {
            CompetitiveCfgProfile p = new CompetitiveCfgProfile(gameKey, effectiveFps, superTouch, forceHz, true, true, true, true, true, true, true);
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
        final int fps = FpsUnlockTier.resolveTargetFps(profile.getTargetFps());

        if (CompetitiveCfgProfile.GAME_MLBB.equals(key)) {
            result = MlbbConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                MlbbConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                MlbbConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                MlbbConfigPatcher.applyRecoilControlConfig(pkg);
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
            if (profile.isMlbbDamageScriptEnabled()) {
                PubgConfigPatcher.applyDamageScriptConfig(pkg);
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
            if (profile.isMlbbDamageScriptEnabled()) {
                CodmConfigPatcher.applyDamageScriptConfig(pkg);
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
            if (profile.isMlbbDamageScriptEnabled()) {
                FreeFireConfigPatcher.applyDamageScriptConfig(pkg);
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
            if (profile.isMlbbDamageScriptEnabled()) {
                GenshinConfigPatcher.applyDamageScriptConfig(pkg);
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
            if (profile.isMlbbDamageScriptEnabled()) {
                HokConfigPatcher.applyDamageScriptConfig(pkg);
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
            if (profile.isMlbbDamageScriptEnabled()) {
                RobloxConfigPatcher.applyDamageScriptConfig(pkg);
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
            if (profile.isMlbbDamageScriptEnabled()) {
                ValorantConfigPatcher.applyDamageScriptConfig(pkg);
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
            if (profile.isMlbbDamageScriptEnabled()) {
                FarlightConfigPatcher.applyDamageScriptConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_BLOODSTRIKE.equals(key)) {
            result = BloodStrikeConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                BloodStrikeConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                BloodStrikeConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                BloodStrikeConfigPatcher.applyRecoilControlConfig(pkg);
            }
            if (profile.isMlbbDamageScriptEnabled()) {
                BloodStrikeConfigPatcher.applyDamageScriptConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_STANDOFF2.equals(key)) {
            result = Standoff2ConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                Standoff2ConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                Standoff2ConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                Standoff2ConfigPatcher.applyRecoilControlConfig(pkg);
            }
            if (profile.isMlbbDamageScriptEnabled()) {
                Standoff2ConfigPatcher.applyDamageScriptConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_WILDRIFT.equals(key)) {
            result = WildRiftConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                WildRiftConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                WildRiftConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                WildRiftConfigPatcher.applyRecoilControlConfig(pkg);
            }
            if (profile.isMlbbDamageScriptEnabled()) {
                WildRiftConfigPatcher.applyDamageScriptConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_CARX.equals(key)) {
            result = CarXConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                CarXConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                CarXConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                CarXConfigPatcher.applyRecoilControlConfig(pkg);
            }
            if (profile.isMlbbDamageScriptEnabled()) {
                CarXConfigPatcher.applyDamageScriptConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_ARENABREAKOUT.equals(key)) {
            result = ArenaBreakoutConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                ArenaBreakoutConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                ArenaBreakoutConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                ArenaBreakoutConfigPatcher.applyRecoilControlConfig(pkg);
            }
            if (profile.isMlbbDamageScriptEnabled()) {
                ArenaBreakoutConfigPatcher.applyDamageScriptConfig(pkg);
            }
        } else if (CompetitiveCfgProfile.GAME_SUPERCELL.equals(key)) {
            result = SupercellConfigPatcher.patchCompetitive(pkg, fps);
            if (profile.isSuperFastTouchEnabled()) {
                SupercellConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                SupercellConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                SupercellConfigPatcher.applyRecoilControlConfig(pkg);
            }
            if (profile.isMlbbDamageScriptEnabled()) {
                SupercellConfigPatcher.applyDamageScriptConfig(pkg);
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
            result |= BloodStrikeConfigPatcher.patchCompetitive(pkg, fps);
            result |= Standoff2ConfigPatcher.patchCompetitive(pkg, fps);
            result |= WildRiftConfigPatcher.patchCompetitive(pkg, fps);
            result |= CarXConfigPatcher.patchCompetitive(pkg, fps);
            result |= ArenaBreakoutConfigPatcher.patchCompetitive(pkg, fps);
            result |= SupercellConfigPatcher.patchCompetitive(pkg, fps);

            if (profile.isSuperFastTouchEnabled()) {
                MlbbConfigPatcher.applySuperFastTouch(pkg);
                PubgConfigPatcher.applySuperFastTouch(pkg);
                CodmConfigPatcher.applySuperFastTouch(pkg);
                FreeFireConfigPatcher.applySuperFastTouch(pkg);
            }
            if (profile.isAimAssistEnabled()) {
                MlbbConfigPatcher.applyAimAssistConfig(pkg);
                PubgConfigPatcher.applyAimAssistConfig(pkg);
                CodmConfigPatcher.applyAimAssistConfig(pkg);
                FreeFireConfigPatcher.applyAimAssistConfig(pkg);
            }
            if (profile.isRecoilControlEnabled()) {
                MlbbConfigPatcher.applyRecoilControlConfig(pkg);
                PubgConfigPatcher.applyRecoilControlConfig(pkg);
                CodmConfigPatcher.applyRecoilControlConfig(pkg);
                FreeFireConfigPatcher.applyRecoilControlConfig(pkg);
            }
            if (profile.isMlbbDamageScriptEnabled()) {
                MlbbConfigPatcher.applyDamageScriptConfig(pkg);
                PubgConfigPatcher.applyDamageScriptConfig(pkg);
                CodmConfigPatcher.applyDamageScriptConfig(pkg);
            }
        }

        if (profile.isAntiLogEnabled()) {
            AntiLogPatcher.applyAntiLog(pkg);
        }

        return result;
    }

    /** Builds and applies the dynamic Shizuku force command for the target Hz. */
    private static void applyShizukuHzForce(int hz) {
        final int forcedHz = FpsUnlockTier.resolveTargetFps(hz);
        if (ShizukuExecutor.hasShizukuPermission()) {
            MaxHzForceChannel.forceApply(forcedHz);
        }
        Log.i(TAG, "Shizuku force applied: " + forcedHz + "Hz");
    }

    private static List<String> getPackagesForKey(String gameKey) {
        switch (gameKey) {
            case CompetitiveCfgProfile.GAME_MLBB:          return MLBB_PACKAGES;
            case CompetitiveCfgProfile.GAME_PUBGM:         return PUBGM_PACKAGES;
            case CompetitiveCfgProfile.GAME_CODM:          return CODM_PACKAGES;
            case CompetitiveCfgProfile.GAME_FREEFIRE:      return FREEFIRE_PACKAGES;
            case CompetitiveCfgProfile.GAME_GENSHIN:       return GENSHIN_PACKAGES;
            case CompetitiveCfgProfile.GAME_HOK:           return HOK_PACKAGES;
            case CompetitiveCfgProfile.GAME_ROBLOX:        return ROBLOX_PACKAGES;
            case CompetitiveCfgProfile.GAME_VALORANT:      return VALORANT_PACKAGES;
            case CompetitiveCfgProfile.GAME_FARLIGHT:      return FARLIGHT_PACKAGES;
            case CompetitiveCfgProfile.GAME_BLOODSTRIKE:   return BLOODSTRIKE_PACKAGES;
            case CompetitiveCfgProfile.GAME_STANDOFF2:     return STANDOFF2_PACKAGES;
            case CompetitiveCfgProfile.GAME_WILDRIFT:      return WILDRIFT_PACKAGES;
            case CompetitiveCfgProfile.GAME_CARX:          return CARX_PACKAGES;
            case CompetitiveCfgProfile.GAME_ARENABREAKOUT: return ARENABREAKOUT_PACKAGES;
            case CompetitiveCfgProfile.GAME_SUPERCELL:     return SUPERCELL_PACKAGES;
            case CompetitiveCfgProfile.GAME_ALL:
            default:
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
                all.addAll(BLOODSTRIKE_PACKAGES);
                all.addAll(STANDOFF2_PACKAGES);
                all.addAll(WILDRIFT_PACKAGES);
                all.addAll(CARX_PACKAGES);
                all.addAll(ARENABREAKOUT_PACKAGES);
                all.addAll(SUPERCELL_PACKAGES);
                return all;
        }
    }
}
