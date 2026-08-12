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
        DEFAULT_GAME_PROFILES.put("com.tencent.ig", "asus_rog8_pro");
        DEFAULT_GAME_PROFILES.put("com.pubg.krmobile", "asus_rog8_pro");
        DEFAULT_GAME_PROFILES.put("com.pubg.imobile", "asus_rog8_pro");
        DEFAULT_GAME_PROFILES.put("com.garena.game.codm", "nubia_redmagic9_pro");
        DEFAULT_GAME_PROFILES.put("com.activision.callofduty.shooter", "nubia_redmagic9_pro");
        DEFAULT_GAME_PROFILES.put("com.mobile.legends", "asus_rog8_pro");
        DEFAULT_GAME_PROFILES.put("com.levelinfinite.sgameGlobal", "samsung_s24_ultra");
        DEFAULT_GAME_PROFILES.put("com.miHoYo.GenshinImpact", "apple_ipad_pro_m4");
        DEFAULT_GAME_PROFILES.put("com.cognosphere.GenshinImpact", "apple_ipad_pro_m4");
        DEFAULT_GAME_PROFILES.put("com.dts.freefireth", "asus_rog8_pro");
        DEFAULT_GAME_PROFILES.put("com.roblox.client", "asus_rog8_pro");
    }

    /**
     * Resolves the target SpoofProfile for a specific game package.
     *
     * @param context Application context.
     * @param packageName Target package name.
     * @return Selected or default SpoofProfile object.
     */
    public static SpoofProfile resolveProfileForGame(Context context, String packageName) {
        if (context == null || packageName == null || packageName.isEmpty()) {
            return SpoofProfileRegistry.getById("asus_rog8_pro");
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
