package com.gamebooster.app.core.profile;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gamebooster.app.feature.games.CustomGameManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Set;

/**
 * ProfileBackupEngine exports and restores user custom profiles, settings,
 * and game preferences into structured JSON backups.
 */
public class ProfileBackupEngine {

    private static final String TAG = "ProfileBackupEngine";
    private static final String BACKUP_FILE_NAME = "game_launcher_backup.json";

    public static JSONObject exportBackupJsonObject(Context context) {
        JSONObject backupJson = new JSONObject();
        try {
            backupJson.put("version", "2.7.1.0");
            backupJson.put("timestamp", System.currentTimeMillis());

            // Export Custom Games
            Set<String> customGames = CustomGameManager.getCustomGames(context);
            JSONArray customArray = new JSONArray();
            for (String pkg : customGames) {
                customArray.put(pkg);
            }
            backupJson.put("custom_games", customArray);

        } catch (JSONException e) {
            Log.e(TAG, "Error exporting backup JSON", e);
        }
        return backupJson;
    }

    public static boolean exportToFile(Context context, File targetFile) {
        if (context == null || targetFile == null) return false;
        try {
            JSONObject json = exportBackupJsonObject(context);
            try (FileWriter writer = new FileWriter(targetFile)) {
                writer.write(json.toString(4));
            }
            Log.i(TAG, "Backup successfully exported to " + targetFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to write backup to file", e);
            return false;
        }
    }

    public static boolean importFromFile(Context context, File sourceFile) {
        if (context == null || sourceFile == null || !sourceFile.exists()) return false;
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JSONObject backupJson = new JSONObject(sb.toString());
            if (backupJson.has("custom_games")) {
                JSONArray customArray = backupJson.getJSONArray("custom_games");
                for (int i = 0; i < customArray.length(); i++) {
                    String pkg = customArray.getString(i);
                    CustomGameManager.addCustomGame(context, pkg);
                }
            }

            Log.i(TAG, "Backup successfully restored from " + sourceFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to restore backup from file", e);
            return false;
        }
    }
}
