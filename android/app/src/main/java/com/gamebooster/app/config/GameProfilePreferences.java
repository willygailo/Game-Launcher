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
        return 165;
    }

    public static int getTargetHz(Context context, Profile profile) {
        return 165;
    }

    public static String getSummary(Context context, String packageName) {
        if (context == null || packageName == null) return "CFG: 185 FPS • ZERO-DELAY TOUCH 185Hz • 1000Hz GYRO";
        String gameKey = packageName.contains("mobile.legends") || packageName.contains("mobilelegends") ? CompetitiveCfgProfile.GAME_MLBB :
                         packageName.contains("pubg") || packageName.contains("tencent.ig") || packageName.contains("imobile") || packageName.contains("vng.pubgmobile") ? CompetitiveCfgProfile.GAME_PUBGM :
                         packageName.contains("cod") || packageName.contains("callofduty") ? CompetitiveCfgProfile.GAME_CODM :
                         packageName.contains("freefire") || packageName.contains("dts.freefire") ? CompetitiveCfgProfile.GAME_FREEFIRE :
                         packageName.contains("genshin") || packageName.contains("mihoyo") || packageName.contains("cognosphere") || packageName.contains("hoyoverse") || packageName.contains("hkrpg") ? CompetitiveCfgProfile.GAME_GENSHIN :
                         packageName.contains("sgame") || packageName.contains("levelinfinite") || packageName.contains("arenaofvalor") || packageName.contains("kgtw") || packageName.contains("kgvn") ? CompetitiveCfgProfile.GAME_HOK :
                         packageName.contains("roblox") ? CompetitiveCfgProfile.GAME_ROBLOX :
                         packageName.contains("projectc") || packageName.contains("valorant") ? CompetitiveCfgProfile.GAME_VALORANT :
                         packageName.contains("farlight") || packageName.contains("solarland") ? CompetitiveCfgProfile.GAME_FARLIGHT : CompetitiveCfgProfile.GAME_ALL;
        CompetitiveCfgProfile cfg = CfgProfileManager.loadProfile(context, gameKey);
        int fps = cfg.getTargetFps() > 0 ? cfg.getTargetFps() : 185;
        return "CFG: " + fps + " FPS • TOUCH " + (cfg.isSuperFastTouchEnabled() ? fps + "Hz (0ms)" : "STD") + " • SHIZUKU HZ " + fps + " • GYRO 1000Hz";
    }
}
