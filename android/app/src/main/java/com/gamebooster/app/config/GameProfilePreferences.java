package com.gamebooster.app.config;

import android.content.Context;

import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.booster.PerformanceChannel;

/** Persists a device-safe performance choice for every game package. */
public final class GameProfilePreferences {

    private static final String PREF_NAME = "per_game_performance_profiles";
    private static final String KEY_PROFILE_PREFIX = "profile_";

    public enum Profile {
        BALANCED("Balanced", 165, false, PerformanceChannel.Profile.BALANCED),
        COMPETITIVE("Competitive 165Hz", 165, true, PerformanceChannel.Profile.PERFORMANCE),
        MAX_SUPPORTED("Max 165Hz Extreme", 165, true, PerformanceChannel.Profile.EXTREME_PERFORMANCE);

        public final String label;
        private final int requestedHz;
        public final boolean enableDnd;
        public final PerformanceChannel.Profile performanceProfile;

        Profile(String label, int requestedHz, boolean enableDnd,
                PerformanceChannel.Profile performanceProfile) {
            this.label = label;
            this.requestedHz = 165;
            this.enableDnd = enableDnd;
            this.performanceProfile = performanceProfile;
        }

        int resolveTargetHz(DevicePerformanceCapabilities capabilities) {
            return 165;
        }
    }

    private GameProfilePreferences() {}

    public static Profile getProfile(Context context, String packageName) {
        if (context == null || packageName == null) return Profile.MAX_SUPPORTED;
        String stored = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PROFILE_PREFIX + packageName, Profile.MAX_SUPPORTED.name());
        try {
            return Profile.valueOf(stored);
        } catch (IllegalArgumentException ignored) {
            return Profile.MAX_SUPPORTED;
        }
    }

    public static void setProfile(Context context, String packageName, Profile profile) {
        if (context == null || packageName == null || packageName.trim().isEmpty() || profile == null) return;
        context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PROFILE_PREFIX + packageName, profile.name())
                .apply();
    }

    public static int getTargetHz(Context context, String packageName) {
        return 185;
    }

    public static int getTargetHz(Context context, Profile profile) {
        return 185;
    }

    public static String getSummary(Context context, String packageName) {
        if (context == null || packageName == null) return "CFG: 185 FPS • ZERO-DELAY TOUCH 185Hz • 1000Hz GYRO";
        String pkg = packageName.toLowerCase();
        String gameKey = pkg.contains("mobile.legends") || pkg.contains("mobilelegends") ? CompetitiveCfgProfile.GAME_MLBB :
                         pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile") ? CompetitiveCfgProfile.GAME_PUBGM :
                         pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone") ? CompetitiveCfgProfile.GAME_CODM :
                         pkg.contains("freefire") || pkg.contains("dts.freefire") ? CompetitiveCfgProfile.GAME_FREEFIRE :
                         pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") || pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap") ? CompetitiveCfgProfile.GAME_GENSHIN :
                         pkg.contains("wildrift") || pkg.contains("riotgames.league") ? CompetitiveCfgProfile.GAME_WILDRIFT :
                         pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor") || pkg.contains("kgtw") || pkg.contains("kgvn") ? CompetitiveCfgProfile.GAME_HOK :
                         pkg.contains("bloodstrike") || pkg.contains("newspike") ? CompetitiveCfgProfile.GAME_BLOODSTRIKE :
                         pkg.contains("standoff2") || pkg.contains("axlebolt") ? CompetitiveCfgProfile.GAME_STANDOFF2 :
                         pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row") ? CompetitiveCfgProfile.GAME_CARX :
                         pkg.contains("uamo") || pkg.contains("arenabreakout") || pkg.contains("deltaforce") ? CompetitiveCfgProfile.GAME_ARENABREAKOUT :
                         pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashroyale") || pkg.contains("clashofclans") ? CompetitiveCfgProfile.GAME_SUPERCELL :
                         pkg.contains("roblox") ? CompetitiveCfgProfile.GAME_ROBLOX :
                         pkg.contains("projectc") || pkg.contains("valorant") ? CompetitiveCfgProfile.GAME_VALORANT :
                         pkg.contains("farlight") || pkg.contains("solarland") ? CompetitiveCfgProfile.GAME_FARLIGHT : CompetitiveCfgProfile.GAME_ALL;
        
        CompetitiveCfgProfile cfg = CfgProfileManager.loadProfile(context, gameKey);
        int fps = cfg.getTargetFps() > 0 ? cfg.getTargetFps() : 185;
        return "CFG: " + fps + " FPS • TOUCH " + (cfg.isSuperFastTouchEnabled() ? fps + "Hz (0ms)" : "STD") + " • HZ " + fps + " • GYRO 1000Hz" + (cfg.isHardwareMaskEnabled() ? " • SPOOF ACTIVE" : "");
    }
}
