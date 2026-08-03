package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AppConfigLoader {

    private static final String TAG = "AppConfigLoader";

    public static JSONObject loadAssetConfig(Context context, String fileName) {
        if (context == null || fileName == null) return new JSONObject();
        try {
            InputStream is = context.getAssets().open("config/" + fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String jsonStr = new String(buffer, StandardCharsets.UTF_8);
            return new JSONObject(jsonStr);
        } catch (Throwable e) {
            Log.w(TAG, "Error loading asset config: " + fileName + " - " + e.getMessage());
            return new JSONObject();
        }
    }

    public static JSONObject getGameProfiles(Context context) {
        return loadAssetConfig(context, "game_profiles.json");
    }

    public static JSONObject getBoosterPresets(Context context) {
        return loadAssetConfig(context, "booster_presets.json");
    }
}
