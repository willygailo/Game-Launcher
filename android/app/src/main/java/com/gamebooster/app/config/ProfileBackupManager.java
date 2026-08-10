package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * ProfileBackupManager manages exporting and importing competitive configuration profiles
 * to and from JSON files on local storage (/sdcard/GameLauncherPro/backups/).
 */
public class ProfileBackupManager {

    private static final String TAG = "ProfileBackupManager";
    private static final String PREFS_NAME = "game_booster_cfg_profiles";
    private static final String BACKUP_DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/GameLauncherPro/backups";

    public static class BackupResult {
        public final boolean success;
        public final String message;
        public final String filePath;

        public BackupResult(boolean success, String message, String filePath) {
            this.success = success;
            this.message = message;
            this.filePath = filePath;
        }
    }

    public static BackupResult exportBackup(Context context) {
        if (context == null) {
            return new BackupResult(false, "Context is null", null);
        }

        try {
            File backupDir = new File(BACKUP_DIR_PATH);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
                CommandExecutor.executeSystemCommand("mkdir -p " + BACKUP_DIR_PATH);
            }

            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Map<String, ?> allEntries = prefs.getAll();

            JSONObject rootJson = new JSONObject();
            JSONObject entriesJson = new JSONObject();

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                entriesJson.put(entry.getKey(), entry.getValue());
            }

            rootJson.put("version", "2.0");
            rootJson.put("timestamp", System.currentTimeMillis());
            rootJson.put("profiles", entriesJson);

            String timeStampStr = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File backupFile = new File(backupDir, "profiles_backup_" + timeStampStr + ".json");

            try (FileOutputStream fos = new FileOutputStream(backupFile)) {
                fos.write(rootJson.toString(2).getBytes(StandardCharsets.UTF_8));
            }

            Log.i(TAG, "Exported competitive profiles to " + backupFile.getAbsolutePath());
            return new BackupResult(true, "Successfully exported " + allEntries.size() + " profile settings", backupFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Failed to export profile backup", e);
            return new BackupResult(false, "Failed to export: " + e.getMessage(), null);
        }
    }

    public static BackupResult importBackup(Context context, File backupFile) {
        if (context == null || backupFile == null || !backupFile.exists()) {
            return new BackupResult(false, "Backup file does not exist", null);
        }

        try {
            byte[] buffer = new byte[(int) backupFile.length()];
            try (FileInputStream fis = new FileInputStream(backupFile)) {
                fis.read(buffer);
            }
            String content = new String(buffer, StandardCharsets.UTF_8);
            JSONObject rootJson = new JSONObject(content);

            if (!rootJson.has("profiles")) {
                return new BackupResult(false, "Invalid backup format: missing 'profiles' key", null);
            }

            JSONObject entriesJson = rootJson.getJSONObject("profiles");
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();

            Iterator<String> keys = entriesJson.keys();
            int importedCount = 0;

            while (keys.hasNext()) {
                String key = keys.next();
                Object val = entriesJson.get(key);
                if (val instanceof Boolean) {
                    editor.putBoolean(key, (Boolean) val);
                } else if (val instanceof Integer) {
                    editor.putInt(key, (Integer) val);
                } else if (val instanceof Long) {
                    editor.putLong(key, (Long) val);
                } else if (val instanceof String) {
                    editor.putString(key, (String) val);
                }
                importedCount++;
            }

            editor.apply();
            Log.i(TAG, "Imported " + importedCount + " profile settings from " + backupFile.getName());
            return new BackupResult(true, "Successfully imported " + importedCount + " profile settings", backupFile.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Failed to import profile backup", e);
            return new BackupResult(false, "Failed to import: " + e.getMessage(), null);
        }
    }
}
