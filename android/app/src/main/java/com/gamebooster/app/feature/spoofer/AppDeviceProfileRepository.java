package com.gamebooster.app.feature.spoofer;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;

/**
 * AppDeviceProfileRepository — Hardware device profile resolver & per-game mapping repository.
 *
 * Maps games to target eSports hardware profiles (ASUS ROG Phone 8 Pro, RedMagic 9 Pro,
 * Galaxy S24 Ultra, iPad Pro 12.9) and persists selections per package.
 */
public class AppDeviceProfileRepository {

    private static final Map<String, String> DEFAULT_GAME_PROFILES = new HashMap<>();

    static {
        // High-End eSports Hardware Profile Defaults
        // PUBG Mobile & Regional Variants (165Hz ROG Phone 9 Pro)
        DEFAULT_GAME_PROFILES.put("com.tencent.ig", "asus_rog9_pro");
        DEFAULT_GAME_PROFILES.put("com.pubg.krmobile", "asus_rog9_pro");
        DEFAULT_GAME_PROFILES.put("com.pubg.imobile", "asus_rog9_pro");
        DEFAULT_GAME_PROFILES.put("com.vng.pubgmobile", "asus_rog9_pro");
        DEFAULT_GAME_PROFILES.put("com.tencent.iglite", "asus_rog8_pro");
        DEFAULT_GAME_PROFILES.put("com.pubg.newstate", "asus_rog9_pro");

        // Call of Duty: Mobile & Warzone (165Hz RedMagic 9 Pro)
        DEFAULT_GAME_PROFILES.put("com.garena.game.codm", "nubia_redmagic9_pro");
        DEFAULT_GAME_PROFILES.put("com.activision.callofduty.shooter", "nubia_redmagic9_pro");
        DEFAULT_GAME_PROFILES.put("com.vng.codmvn", "nubia_redmagic9_pro");
        DEFAULT_GAME_PROFILES.put("com.activision.callofduty.warzone", "nubia_redmagic9_pro");

        // Mobile Legends: Bang Bang & Regional Variants (165Hz ROG Phone 9 Pro)
        DEFAULT_GAME_PROFILES.put("com.mobile.legends", "asus_rog9_pro");
        DEFAULT_GAME_PROFILES.put("com.mobile.legends.vng", "asus_rog9_pro");
        DEFAULT_GAME_PROFILES.put("com.mobile.legends.kr", "asus_rog9_pro");
        DEFAULT_GAME_PROFILES.put("com.mobile.legends.jp", "asus_rog9_pro");

        // Honor of Kings & Arena of Valor (144Hz iQOO 12 Pro)
        DEFAULT_GAME_PROFILES.put("com.levelinfinite.sgameGlobal", "iqoo_12_pro");
        DEFAULT_GAME_PROFILES.put("com.tencent.tmgp.sgame", "iqoo_12_pro");
        DEFAULT_GAME_PROFILES.put("com.garena.game.kgtw", "iqoo_12_pro");
        DEFAULT_GAME_PROFILES.put("com.garena.game.kgvn", "iqoo_12_pro");

        // HoYoverse & Kuro Games (120Hz iPad Pro M4)
        DEFAULT_GAME_PROFILES.put("com.miHoYo.GenshinImpact", "apple_ipad_pro_m4");
        DEFAULT_GAME_PROFILES.put("com.cognosphere.GenshinImpact", "apple_ipad_pro_m4");
        DEFAULT_GAME_PROFILES.put("com.HoYoverse.hkrpgoversea", "apple_ipad_pro_m4");
        DEFAULT_GAME_PROFILES.put("com.HoYoverse.nap", "apple_ipad_pro_m4");
        DEFAULT_GAME_PROFILES.put("com.kurogame.wutheringwaves.global", "apple_ipad_pro_m4");

        // Free Fire, Wild Rift, Delta Force, Blood Strike, Standoff 2, Farlight 84, Roblox
        DEFAULT_GAME_PROFILES.put("com.dts.freefireth", "infinix_gt_20_pro");
        DEFAULT_GAME_PROFILES.put("com.dts.freefiremax", "infinix_gt_20_pro");
        DEFAULT_GAME_PROFILES.put("com.riotgames.league.wildrift", "realme_gt_5_pro");
        DEFAULT_GAME_PROFILES.put("com.tencent.dfm", "poco_f6_pro");
        DEFAULT_GAME_PROFILES.put("com.proxima.deltaforce", "poco_f6_pro");
        DEFAULT_GAME_PROFILES.put("com.ofg.bloodstrike", "blackshark5_pro");
        DEFAULT_GAME_PROFILES.put("com.netease.bloodstrike", "blackshark5_pro");
        DEFAULT_GAME_PROFILES.put("com.axlebolt.standoff2", "asus_rog9_pro");
        DEFAULT_GAME_PROFILES.put("com.miracle.farlight84", "asus_rog9_pro");
        DEFAULT_GAME_PROFILES.put("com.roblox.client", "asus_rog9_pro");
    }

    /**
     * Resolves the target SpoofProfile for a specific game package.
     *
     * @param context Application context.
     * @param packageName Target package name.
     * @return Selected or default SpoofProfile object.
     */
    public static SpoofProfile resolveProfileForGame(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return SpoofProfileRegistry.getById("asus_rog9_pro");
        }

        // 1. Check user saved custom profile for package
        String customId = SpoofPreferences.getGameSpoofProfileId(context, packageName);
        if (customId != null) {
            SpoofProfile profile = SpoofProfileRegistry.getById(customId);
            if (profile != null) return profile;
        }

        // 2. Check default game hardware binding
        if (DEFAULT_GAME_PROFILES.containsKey(packageName)) {
            String defaultId = DEFAULT_GAME_PROFILES.get(packageName);
            SpoofProfile profile = SpoofProfileRegistry.getById(defaultId);
            if (profile != null) return profile;
        }

        // 3. Fallback to active global profile or ROG 8 Pro 165Hz
        String activeGlobalId = SpoofPreferences.getActiveProfileId(context);
        if (activeGlobalId != null) {
            SpoofProfile profile = SpoofProfileRegistry.getById(activeGlobalId);
            if (profile != null) return profile;
        }

        return SpoofProfileRegistry.getById("asus_rog8_pro");
    }

    /**
     * Binds a target device profile ID to a game package.
     */
    public static void setProfileForGame(Context context, String packageName, String profileId) {
        if (context == null || packageName == null || profileId == null) return;
        SpoofPreferences.setGameSpoofProfileId(context, packageName, profileId);
        SpoofPreferences.setGameSpoofEnabled(context, packageName, true);
    }
}
