package com.gamebooster.app.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuFileManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ConfigBackupManager — SHA-256 verified safety-net backups of original game config files.
 *
 * Before any patcher overwrites a game config file, the true original is captured once
 * (idempotent, per path) into app-private storage: files/config_backups/&lt;pkg&gt;/&lt;sha256(path)&gt;.bin.
 * Each record stores the original content hash and timestamp in SharedPreferences:
 *   prefs "game_booster_backups" key "backup_&lt;pkg&gt;" = JSON array of {path, file, hash, time}.
 *
 * Restore writes the exact original bytes back via Shizuku (Base64 stream) and removes
 * the record, so a later patch re-armors the restored original. If the app context is
 * unavailable the backup is skipped silently (best effort — never fails a patch).
 */
public class ConfigBackupManager {

    private static final String TAG             = "ConfigBackupManager";
    private static final String PREFS_NAME      = "game_booster_backups";
    private static final String KEY_PREFIX      = "backup_";
    private static final String ROOT_DIR        = "config_backups";
    private static final String DEFAULT_CHMOD   = "666";

    private static volatile Context appContext;

    private ConfigBackupManager() {}

    /**
     * Registers the application context used when callers have none (e.g. static patchers).
     * Called from MainActivity / GameBoosterService onCreate.
     */
    public static void setAppContext(Context context) {
        if (context != null && appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    // ─── Backup ──────────────────────────────────────────────────────────────

    /**
     * Captures the original bytes of a single game config path exactly once.
     *
     * @return true if the original is now safely backed up (or already was)
     */
    public static boolean backupPath(String pkg, String path) {
        Context ctx = resolveContext(null);
        if (ctx == null || pkg == null || path == null || path.trim().isEmpty()) return false;
        if (hasRecord(ctx, pkg, path)) return true;

        byte[] original = ShizukuFileManager.readFileBytes(path);
        if (original.length == 0 && !ShizukuFileManager.fileExists(path)) {
            return false; // nothing to back up
        }

        String contentHash = sha256Hex(original);
        String fileName = sha256Hex(path) + ".bin";
        File dir = new File(new File(ctx.getFilesDir(), ROOT_DIR), sanitize(pkg));
        if (!dir.exists() && !dir.mkdirs()) return false;

        File backupFile = new File(dir, fileName);
        if (!writeLocalBytes(backupFile, original)) return false;

        // Verify the round trip before trusting the copy
        if (!contentHash.equals(sha256Hex(readLocalBytes(backupFile)))) {
            Log.e(TAG, "Backup verification failed for " + path);
            backupFile.delete();
            return false;
        }

        boolean recorded = addRecord(ctx, pkg, path, fileName, contentHash);
        if (recorded) {
            Log.i(TAG, "Backed up original " + path + " (" + original.length + " bytes, " + contentHash.substring(0, 8) + ")");
        }
        return recorded;
    }

    /**
     * Captures originals for every candidate config path of a game package.
     *
     * @return number of paths successfully backed up
     */
    public static int backupPackage(String pkg, List<String> paths) {
        if (paths == null || paths.isEmpty()) return 0;
        int count = 0;
        for (String path : paths) {
            if (path != null && !path.trim().isEmpty() && backupPath(pkg, path)) {
                count++;
            }
        }
        return count;
    }

    // ─── Restore ─────────────────────────────────────────────────────────────

    /**
     * Restores all backed-up original config files for a game package.
     *
     * @return number of files successfully restored
     */
    public static int restorePackage(Context context, String pkg) {
        Context ctx = resolveContext(context);
        if (ctx == null || pkg == null) return 0;

        SharedPreferences prefs = prefs(ctx);
        JSONArray records = loadRecordsKey(prefs, keyFor(pkg));
        if (records == null || records.length() == 0) return 0;

        File dir = new File(new File(ctx.getFilesDir(), ROOT_DIR), sanitize(pkg));
        JSONArray remaining = new JSONArray();
        int restored = 0;

        for (int i = 0; i < records.length(); i++) {
            try {
                JSONObject rec = records.getJSONObject(i);
                String path = rec.optString("path");
                String file = rec.optString("file");
                String hash = rec.optString("hash");
                byte[] bytes = readLocalBytes(new File(dir, file));
                if (bytes.length == 0 || !hash.equals(sha256Hex(bytes))) {
                    remaining.put(rec); // keep record: backup unreadable, retry later
                    continue;
                }
                boolean ok = ShizukuFileManager.uploadBytes(path, bytes, DEFAULT_CHMOD).success;
                if (ok) {
                    restored++;
                    new File(dir, file).delete();
                } else {
                    remaining.put(rec);
                }
            } catch (Throwable t) {
                Log.w(TAG, "restore record exception", t);
                remaining.put(records.optJSONObject(i));
            }
        }

        saveRecords(prefs, keyFor(pkg), remaining);
        if (remaining.length() == 0 && dir.exists()) {
            File[] files = dir.listFiles();
            if (files == null || files.length == 0) dir.delete();
        }
        if (restored > 0) {
            Log.i(TAG, "Restored " + restored + " original file(s) for " + pkg);
        }
        return restored;
    }

    /**
     * Searches all packages' records for the given path and restores it.
     * Used for auto-recovery when a patch write fails but the package is unknown.
     */
    public static boolean restorePath(Context context, String path) {
        Context ctx = resolveContext(context);
        if (ctx == null || path == null) return false;
        SharedPreferences prefs = prefs(ctx);
        for (String key : new ArrayList<>(prefs.getAll().keySet())) {
            if (!key.startsWith(KEY_PREFIX)) continue;
            JSONArray records = loadRecordsKey(prefs, key);
            if (containsPath(records, path)) {
                restorePackage(ctx, key.substring(KEY_PREFIX.length()));
                return true;
            }
        }
        return false;
    }

    // ─── Queries / management ────────────────────────────────────────────────

    public static boolean hasBackups(Context context, String pkg) {
        return getBackupCount(context, pkg) > 0;
    }

    public static int getBackupCount(Context context, String pkg) {
        Context ctx = resolveContext(context);
        if (ctx == null || pkg == null) return 0;
        JSONArray records = loadRecordsKey(prefs(ctx), keyFor(pkg));
        return records == null ? 0 : records.length();
    }

    /** Returns the original file paths currently backed up for a package. */
    public static List<String> getBackupPaths(Context context, String pkg) {
        Context ctx = resolveContext(context);
        if (ctx == null || pkg == null) return Collections.emptyList();
        JSONArray records = loadRecordsKey(prefs(ctx), keyFor(pkg));
        if (records == null) return Collections.emptyList();
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < records.length(); i++) {
            String p = records.optJSONObject(i) == null ? null : records.optJSONObject(i).optString("path");
            if (p != null && !p.isEmpty()) paths.add(p);
        }
        return paths;
    }

    public static List<String> getBackedUpPackages(Context context) {
        Context ctx = resolveContext(context);
        if (ctx == null) return Collections.emptyList();
        SharedPreferences prefs = prefs(ctx);
        List<String> pkgs = new ArrayList<>();
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(KEY_PREFIX)) {
                pkgs.add(key.substring(KEY_PREFIX.length()));
            }
        }
        return pkgs;
    }

    public static int restoreAll(Context context) {
        Context ctx = resolveContext(context);
        if (ctx == null) return 0;
        List<String> pkgs = getBackedUpPackages(ctx);
        int totalRestored = 0;
        for (String pkg : pkgs) {
            totalRestored += restorePackage(ctx, pkg);
        }
        return totalRestored;
    }

    public static boolean clearBackups(Context context, String pkg) {
        Context ctx = resolveContext(context);
        if (ctx == null || pkg == null) return false;
        File dir = new File(new File(ctx.getFilesDir(), ROOT_DIR), sanitize(pkg));
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) f.delete();
            }
            dir.delete();
        }
        saveRecords(prefs(ctx), keyFor(pkg), new JSONArray());
        return true;
    }

    // ─── Records (SharedPreferences JSON) ────────────────────────────────────

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String keyFor(String pkg) {
        return KEY_PREFIX + sanitize(pkg);
    }

    private static JSONArray loadRecordsKey(SharedPreferences prefs, String key) {
        String raw = prefs.getString(key, null);
        if (raw == null || raw.isEmpty()) return null;
        try {
            return new JSONArray(raw);
        } catch (Throwable t) {
            Log.w(TAG, "Corrupt backup records for " + key, t);
            return null;
        }
    }

    private static void saveRecords(SharedPreferences prefs, String key, JSONArray records) {
        SharedPreferences.Editor ed = prefs.edit();
        if (records == null || records.length() == 0) {
            ed.remove(key);
        } else {
            ed.putString(key, records.toString());
        }
        ed.apply();
    }

    private static boolean hasRecord(Context ctx, String pkg, String path) {
        JSONArray records = loadRecordsKey(prefs(ctx), keyFor(pkg));
        return containsPath(records, path);
    }

    private static boolean containsPath(JSONArray records, String path) {
        if (records == null) return false;
        for (int i = 0; i < records.length(); i++) {
            JSONObject o = records.optJSONObject(i);
            if (o != null && path.equals(o.optString("path"))) return true;
        }
        return false;
    }

    private static boolean addRecord(Context ctx, String pkg, String path, String fileName, String contentHash) {
        SharedPreferences prefs = prefs(ctx);
        String key = keyFor(pkg);
        JSONArray records = loadRecordsKey(prefs, key);
        if (records == null) records = new JSONArray();
        try {
            JSONObject rec = new JSONObject();
            rec.put("path", path);
            rec.put("file", fileName);
            rec.put("hash", contentHash);
            rec.put("time", System.currentTimeMillis());
            records.put(rec);
        } catch (Throwable t) {
            Log.w(TAG, "addRecord failed for " + path, t);
            return false;
        }
        saveRecords(prefs, key, records);
        return true;
    }

    // ─── Local file I/O (app-private storage, no Shizuku required) ───────────

    private static boolean writeLocalBytes(File file, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.flush();
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "writeLocalBytes failed for " + file.getAbsolutePath(), t);
            return false;
        }
    }

    private static byte[] readLocalBytes(File file) {
        if (file == null || !file.exists()) return new byte[0];
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            int read = 0;
            while (read < data.length) {
                int n = fis.read(data, read, data.length - read);
                if (n < 0) break;
                read += n;
            }
            return data;
        } catch (Throwable t) {
            Log.w(TAG, "readLocalBytes failed for " + file.getAbsolutePath(), t);
            return new byte[0];
        }
    }

    static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Throwable t) {
            Log.w(TAG, "sha256 unavailable", t);
            return "";
        }
    }

    static String sha256Hex(String text) {
        return text == null ? "" : sha256Hex(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    static String sanitize(String value) {
        if (value == null) return "unknown";
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private static Context resolveContext(Context context) {
        if (context != null) return context.getApplicationContext();
        return appContext;
    }
}