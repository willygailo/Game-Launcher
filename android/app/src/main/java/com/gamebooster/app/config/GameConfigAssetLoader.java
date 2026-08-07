package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GameConfigAssetLoader {

    private static final String TAG = "GameConfigAssetLoader";

    public static JSONObject loadGameConfigAsset(Context context, String gameKey) {
        if (context == null || gameKey == null) return null;
        String fileName = "config/" + gameKey.toLowerCase() + "_config.json";
        try (InputStream is = context.getAssets().open(fileName)) {
            byte[] buffer = new byte[is.available()];
            int read = is.read(buffer);
            if (read > 0) {
                String jsonStr = new String(buffer, StandardCharsets.UTF_8);
                return new JSONObject(jsonStr);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not load asset config for " + gameKey + ": " + e.getMessage());
        }
        return null;
    }

    public static List<String> getPackagesForGame(Context context, String gameKey) {
        List<String> packages = new ArrayList<>();
        JSONObject config = loadGameConfigAsset(context, gameKey);
        if (config != null && config.has("packages")) {
            try {
                JSONArray array = config.getJSONArray("packages");
                for (int i = 0; i < array.length(); i++) {
                    packages.add(array.getString(i));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing packages array for " + gameKey, e);
            }
        }
        return packages;
    }
}
