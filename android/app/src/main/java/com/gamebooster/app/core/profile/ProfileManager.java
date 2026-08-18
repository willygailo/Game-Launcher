package com.gamebooster.app.core.profile;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages game-specific tuning profiles, custom profile creation, and JSON import/export.
 */
public class ProfileManager {

    private static final String PREF_NAME = "precision_aim_profiles";
    private static final String KEY_CUSTOM_PROFILES = "custom_profiles_json";

    private final SharedPreferences prefs;
    private final Map<String, InputProfile> defaultProfiles = new HashMap<>();

    public ProfileManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        initDefaults();
    }

    private void initDefaults() {
        // PUBG Mobile & BGMI
        InputProfile pubgProfile = new InputProfile(
                "pubg_mobile", "PUBG Mobile Precision Aim", "com.tencent.ig",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.tencent.ig", pubgProfile);
        defaultProfiles.put("com.pubg.imobile", pubgProfile);
        defaultProfiles.put("com.pubg.krmobile", pubgProfile);
        defaultProfiles.put("com.vng.pubgmobile", pubgProfile);
        defaultProfiles.put("com.rekoo.pubgm", pubgProfile);
        defaultProfiles.put("com.pubg.newstate", pubgProfile);

        // COD Mobile
        InputProfile codmProfile = new InputProfile(
                "cod_mobile", "COD Mobile Instant Response", "com.activision.callofduty.shooter",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.activision.callofduty.shooter", codmProfile);
        defaultProfiles.put("com.garena.game.codm", codmProfile);
        defaultProfiles.put("com.vng.codmvn", codmProfile);

        // Free Fire
        InputProfile ffProfile = new InputProfile(
                "free_fire", "Free Fire Headshot Zero Latency", "com.dts.freefireth",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.dts.freefireth", ffProfile);
        defaultProfiles.put("com.dts.freefiremax", ffProfile);

        // Mobile Legends (MLBB)
        InputProfile mlbbProfile = new InputProfile(
                "mlbb", "MLBB Fast Skill Cast & Micro-Touch", "com.mobile.legends",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.mobile.legends", mlbbProfile);
        defaultProfiles.put("com.mobilelegends.mi", mlbbProfile);
        defaultProfiles.put("com.vng.mlbbvn", mlbbProfile);

        // Blood Strike
        InputProfile bloodStrikeProfile = new InputProfile(
                "blood_strike", "Blood Strike 1000Hz Gyro & Aim", "com.netease.bloodstrike",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.netease.bloodstrike", bloodStrikeProfile);
        defaultProfiles.put("com.netease.newspike", bloodStrikeProfile);

        // Standoff 2
        InputProfile standoffProfile = new InputProfile(
                "standoff2", "Standoff 2 CS Pro Aim", "com.axlebolt.standoff2",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.axlebolt.standoff2", standoffProfile);

        // Genshin Impact
        InputProfile genshinProfile = new InputProfile(
                "genshin", "Genshin Impact Ultra Smooth Touch", "com.miHoYo.GenshinImpact",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.miHoYo.GenshinImpact", genshinProfile);
        defaultProfiles.put("com.cognosphere.GenshinImpact", genshinProfile);

        // Honor of Kings
        InputProfile hokProfile = new InputProfile(
                "hok", "HOK eSports Fast Cast", "com.levelinfinite.sgameGlobal",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.levelinfinite.sgameGlobal", hokProfile);
        defaultProfiles.put("com.tencent.tmgp.sgame", hokProfile);

        // Valorant Mobile
        InputProfile valProfile = new InputProfile(
                "valorant", "Valorant Mobile Tactical Precision", "com.tencent.tmgp.projectc",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.tencent.tmgp.projectc", valProfile);
        defaultProfiles.put("com.riotgames.valorantmobile", valProfile);

        // Farlight 84
        InputProfile farlightProfile = new InputProfile(
                "farlight", "Farlight 84 Fast Tracking", "com.miracle.farlight84",
                1000, 0, 1, 1000, 7, 0.0001
        );
        defaultProfiles.put("com.miracle.farlight84", farlightProfile);
        defaultProfiles.put("com.farlightgames.farlight84.global", farlightProfile);
    }

    public InputProfile getProfileForPackage(String packageName) {
        if (packageName == null) return getGeneralGamingProfile();

        // Check custom profiles first
        List<InputProfile> customs = getCustomProfiles();
        for (InputProfile p : customs) {
            if (packageName.equalsIgnoreCase(p.getPackageName())) {
                return p;
            }
        }

        // Return default profile if available
        if (defaultProfiles.containsKey(packageName)) {
            return defaultProfiles.get(packageName);
        }

        return getGeneralGamingProfile();
    }

    public InputProfile getGeneralGamingProfile() {
        return new InputProfile(
                "general_gaming",
                "General eSports Input Profile",
                "*",
                1000,
                0,
                1,
                1000,
                7,
                0.0001
        );
    }

    public List<InputProfile> getCustomProfiles() {
        List<InputProfile> list = new ArrayList<>();
        String jsonStr = prefs.getString(KEY_CUSTOM_PROFILES, "[]");
        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                InputProfile profile = InputProfile.fromJson(array.getJSONObject(i));
                if (profile != null) list.add(profile);
            }
        } catch (Exception ignored) {}
        return list;
    }

    public void saveCustomProfile(InputProfile profile) {
        List<InputProfile> current = getCustomProfiles();
        current.removeIf(p -> p.getProfileId().equals(profile.getProfileId()));
        current.add(profile);

        JSONArray array = new JSONArray();
        for (InputProfile p : current) {
            array.put(p.toJson());
        }
        prefs.edit().putString(KEY_CUSTOM_PROFILES, array.toString()).apply();
    }

    public String exportProfilesToJson() {
        JSONArray array = new JSONArray();
        for (InputProfile p : defaultProfiles.values()) {
            array.put(p.toJson());
        }
        for (InputProfile p : getCustomProfiles()) {
            array.put(p.toJson());
        }
        return array.toString();
    }

    public boolean importProfilesFromJson(String jsonString) {
        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                InputProfile p = InputProfile.fromJson(array.getJSONObject(i));
                if (p != null) {
                    saveCustomProfile(p);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
