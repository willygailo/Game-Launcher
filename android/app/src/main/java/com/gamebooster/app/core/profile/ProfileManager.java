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
        // PUBG Mobile Preset
        InputProfile pubgProfile = new InputProfile(
                "pubg_mobile",
                "PUBG Mobile Precision Aim",
                "com.tencent.ig",
                1000,   // debug.input.max_events_per_sec
                0,      // view.touch_slop
                1,      // touch_slop_reduction
                1000,   // debug.sensor.gyro.rate
                7,      // pointer_speed
                0.0001  // persist.sys.touch.pressure.scale
        );
        defaultProfiles.put("com.tencent.ig", pubgProfile);
        defaultProfiles.put("com.pubg.imobile", pubgProfile);
        defaultProfiles.put("com.pubg.krmobile", pubgProfile);
        defaultProfiles.put("com.vng.pubgmobile", pubgProfile);

        // COD Mobile Preset
        InputProfile codmProfile = new InputProfile(
                "cod_mobile",
                "COD Mobile Instant Response",
                "com.activision.callofduty.shooter",
                1000,   // max events per sec
                0,      // zero touch slop
                1,      // touch slop reduction
                1000,   // 1000Hz gyro rate
                7,      // 1:1 linear pointer
                0.0001  // pressure scale
        );
        defaultProfiles.put("com.activision.callofduty.shooter", codmProfile);
        defaultProfiles.put("com.garena.game.codm", codmProfile);
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
