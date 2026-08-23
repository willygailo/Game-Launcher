package com.gamebooster.app.anticheat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * GameAntiCheatRegistry — Identifies anti-cheat engines, risk tiers, and telemetry profiles
 * for top mobile esports titles.
 */
public final class GameAntiCheatRegistry {

    public enum AntiCheatType {
        TENCENT_ACE("Tencent Anti-Cheat Expert (ACE / MTP)", 3),
        NETEASE_SAFE("NetEase Safe Engine / Anti-Tamper", 2),
        HOYOVERSE_PROTECT("HoYoverse Anti-Cheat / miHoYo Protect", 3),
        GARENA_PROTECT("Garena Anti-Cheat & Packet Guard", 2),
        BATTLEYE_MOBILE("BattlEye Mobile / Unreal Engine", 3),
        EAC_MOBILE("Easy Anti-Cheat Mobile", 3),
        UNITY_PROTECT("Unity Anti-Cheat & Asset Integrity Guard", 1),
        CUSTOM_SIGNATURE("Custom Game Signature & Inode Check", 1),
        GENERIC_SAFE("Generic Safe / Standard Android Sandbox", 0);

        public final String displayName;
        public final int strictnessLevel; // 0 (none) to 3 (kernel/aggressive)

        AntiCheatType(String displayName, int strictnessLevel) {
            this.displayName = displayName;
            this.strictnessLevel = strictnessLevel;
        }
    }

    public static class GameSecurityProfile {
        public final String packageName;
        public final String gameTitle;
        public final AntiCheatType antiCheatType;
        public final boolean requiresTimestampPreservation;
        public final boolean requiresSelinuxRestore;
        public final boolean requiresLogcatFlush;
        public final String[] telemetryEndpoints;

        public GameSecurityProfile(String packageName, String gameTitle, AntiCheatType antiCheatType,
                                   boolean requiresTimestampPreservation, boolean requiresSelinuxRestore,
                                   boolean requiresLogcatFlush, String[] telemetryEndpoints) {
            this.packageName = packageName;
            this.gameTitle = gameTitle;
            this.antiCheatType = antiCheatType;
            this.requiresTimestampPreservation = requiresTimestampPreservation;
            this.requiresSelinuxRestore = requiresSelinuxRestore;
            this.requiresLogcatFlush = requiresLogcatFlush;
            this.telemetryEndpoints = telemetryEndpoints != null ? telemetryEndpoints : new String[0];
        }
    }

    private static final Map<String, GameSecurityProfile> REGISTRY = new HashMap<>();

    static {
        // Mobile Legends: Bang Bang
        register(new GameSecurityProfile(
                "com.mobile.legends", "Mobile Legends: Bang Bang", AntiCheatType.UNITY_PROTECT,
                true, true, true,
                new String[]{"log.mobilelegends.com", "crashlytics.moonton.com", "sdk.moonton.com"}
        ));
        register(new GameSecurityProfile("com.mobilelegends.mi", "MLBB (Xiaomi)", AntiCheatType.UNITY_PROTECT, true, true, true, null));
        register(new GameSecurityProfile("com.vng.mlbbvn", "MLBB VNG", AntiCheatType.UNITY_PROTECT, true, true, true, null));

        // PUBG Mobile / BGMI / New State
        register(new GameSecurityProfile(
                "com.tencent.ig", "PUBG Mobile (Global)", AntiCheatType.TENCENT_ACE,
                true, true, true,
                new String[]{"tpns.qq.com", "bugly.qq.com", "beacon.qq.com", "gcloud.qq.com"}
        ));
        register(new GameSecurityProfile("com.pubg.imobile", "BGMI (Battlegrounds Mobile India)", AntiCheatType.TENCENT_ACE, true, true, true, null));
        register(new GameSecurityProfile("com.vng.pubgmobile", "PUBG Mobile (VNG)", AntiCheatType.TENCENT_ACE, true, true, true, null));
        register(new GameSecurityProfile("com.pubg.krmobile", "PUBG Mobile (KR/JP)", AntiCheatType.TENCENT_ACE, true, true, true, null));
        register(new GameSecurityProfile("com.pubg.newstate", "PUBG: NEW STATE", AntiCheatType.BATTLEYE_MOBILE, true, true, true, null));

        // Call of Duty: Mobile / Warzone Mobile
        register(new GameSecurityProfile(
                "com.activision.callofduty.shooter", "Call of Duty: Mobile", AntiCheatType.TENCENT_ACE,
                true, true, true,
                new String[]{"report.codm.activision.com", "telemetry.activision.com", "crashlytics.activision.com"}
        ));
        register(new GameSecurityProfile("com.garena.game.codm", "CODM (Garena)", AntiCheatType.TENCENT_ACE, true, true, true, null));
        register(new GameSecurityProfile("com.activision.callofduty.warzone", "COD: Warzone Mobile", AntiCheatType.BATTLEYE_MOBILE, true, true, true, null));

        // Free Fire / Free Fire MAX
        register(new GameSecurityProfile(
                "com.dts.freefireth", "Free Fire", AntiCheatType.GARENA_PROTECT,
                true, true, true,
                new String[]{"log.freefiremobile.com", "report.garena.com", "crash.garena.com"}
        ));
        register(new GameSecurityProfile("com.dts.freefiremax", "Free Fire MAX", AntiCheatType.GARENA_PROTECT, true, true, true, null));

        // Genshin Impact & HoYoverse
        register(new GameSecurityProfile(
                "com.miHoYo.GenshinImpact", "Genshin Impact (Global)", AntiCheatType.HOYOVERSE_PROTECT,
                true, true, true,
                new String[]{"log-upload.mihoyo.com", "overseauspider.yuanshen.com", "apm.hoyoverse.com"}
        ));
        register(new GameSecurityProfile("com.cognosphere.GenshinImpact", "Genshin Impact (Cognosphere)", AntiCheatType.HOYOVERSE_PROTECT, true, true, true, null));
        register(new GameSecurityProfile("com.HoYoverse.hkrpgoversea", "Honkai: Star Rail", AntiCheatType.HOYOVERSE_PROTECT, true, true, true, null));
        register(new GameSecurityProfile("com.HoYoverse.nap", "Zenless Zone Zero", AntiCheatType.HOYOVERSE_PROTECT, true, true, true, null));

        // Arena Breakout / Delta Force
        register(new GameSecurityProfile(
                "com.proximabeta.mf.uamo", "Arena Breakout", AntiCheatType.TENCENT_ACE,
                true, true, true,
                new String[]{"beacon.qq.com", "tpns.qq.com", "log.arenabreakout.com"}
        ));
        register(new GameSecurityProfile("com.levelinfinite.deltaforce", "Delta Force Mobile", AntiCheatType.TENCENT_ACE, true, true, true, null));

        // Blood Strike (NetEase)
        register(new GameSecurityProfile(
                "com.netease.bloodstrike", "Blood Strike", AntiCheatType.NETEASE_SAFE,
                true, true, true,
                new String[]{"crash.netease.com", "log.bloodstrike.netease.com", "unisdk.netease.com"}
        ));

        // Standoff 2
        register(new GameSecurityProfile(
                "com.axlebolt.standoff2", "Standoff 2", AntiCheatType.CUSTOM_SIGNATURE,
                true, true, true,
                new String[]{"analytics.axlebolt.com", "crashlytics.axlebolt.com"}
        ));

        // League of Legends: Wild Rift
        register(new GameSecurityProfile(
                "com.riotgames.league.wildrift", "League of Legends: Wild Rift", AntiCheatType.TENCENT_ACE,
                true, true, true,
                new String[]{"telemetry.riotgames.com", "crashlytics.riotgames.com"}
        ));

        // CarX Street
        register(new GameSecurityProfile("com.h20.carxstreet", "CarX Street", AntiCheatType.CUSTOM_SIGNATURE, true, true, true, null));

        // Roblox
        register(new GameSecurityProfile("com.roblox.client", "Roblox", AntiCheatType.CUSTOM_SIGNATURE, true, true, true, null));

        // Farlight 84
        register(new GameSecurityProfile("com.miracle.farlight84", "Farlight 84", AntiCheatType.EAC_MOBILE, true, true, true, null));
    }

    private static void register(GameSecurityProfile profile) {
        REGISTRY.put(profile.packageName.toLowerCase(), profile);
    }

    @NonNull
    public static GameSecurityProfile getProfile(@Nullable String packageName) {
        if (packageName != null) {
            GameSecurityProfile p = REGISTRY.get(packageName.toLowerCase().trim());
            if (p != null) return p;
        }
        return new GameSecurityProfile(
                packageName != null ? packageName : "unknown",
                "Generic Application",
                AntiCheatType.GENERIC_SAFE,
                false,
                false,
                false,
                new String[0]
        );
    }

    public static boolean isAggressiveAntiCheat(@Nullable String packageName) {
        GameSecurityProfile profile = getProfile(packageName);
        return profile.antiCheatType.strictnessLevel >= 2;
    }

    public static Map<String, GameSecurityProfile> getAllProfiles() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
