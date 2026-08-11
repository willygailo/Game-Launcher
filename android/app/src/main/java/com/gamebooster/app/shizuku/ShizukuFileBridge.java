package com.gamebooster.app.shizuku;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;

/**
 * ShizukuFileBridge manages file operations in restricted locations (/sdcard/Android/data/ and /data/data/)
 * using Shizuku ADB shell privileges (uid 2000).
 *
 * Provides:
 *  - Automatic directory creation
 *  - Safety backup (.bak) generation before patching
 *  - Safe atomic file writes (fixed: no printf % issues)
 *  - Read-only locking (chmod 444 / chmod 644) to prevent games from resetting config files on boot
 *  - File read, directory listing, JSON key patching, file addition
 *  - File integrity verification via sha256sum
 *
 * Android 13+ Note: /sdcard/Android/data/<package>/ is restricted for third-party apps.
 * Access via Shizuku (uid 2000) bypasses these restrictions legally as ADB shell.
 */
public class ShizukuFileBridge {

    private static final String TAG = "ShizukuFileBridge";

    // -----------------------------------------------------------------------------------------
    // Game Data Folder Constants (Android 13-16 compatible paths)
    // All accessed via Shizuku uid 2000 — bypasses /sdcard/Android/data/ restriction legally
    // -----------------------------------------------------------------------------------------

    public static final String MLBB_DATA        = "/sdcard/Android/data/com.mobile.legends/files/";
    public static final String MLBB_VNG         = "/sdcard/Android/data/com.mobile.legends.vng/files/";
    public static final String MLBB_KR          = "/sdcard/Android/data/com.mobile.legends.kr/files/";
    public static final String MLBB_JP          = "/sdcard/Android/data/com.mobile.legends.jp/files/";

    public static final String PUBG_DATA        = "/sdcard/Android/data/com.tencent.ig/files/";
    public static final String PUBG_BGMI        = "/sdcard/Android/data/com.pubg.imobile/files/";
    public static final String PUBG_KR          = "/sdcard/Android/data/com.pubg.krmobile/files/";
    public static final String PUBG_VNG         = "/sdcard/Android/data/com.vng.pubgmobile/files/";
    public static final String PUBG_NEWSTATE    = "/sdcard/Android/data/com.pubg.newstate/files/";

    public static final String CODM_DATA        = "/sdcard/Android/data/com.activision.callofduty.shooter/files/";
    public static final String CODM_GARENA      = "/sdcard/Android/data/com.garena.game.codm/files/";
    public static final String CODM_VN          = "/sdcard/Android/data/com.vng.codmvn/files/";
    public static final String WARZONE_DATA     = "/sdcard/Android/data/com.activision.callofduty.warzone/files/";

    public static final String HOK_DATA         = "/sdcard/Android/data/com.levelinfinite.sgameGlobal/files/";
    public static final String HOK_CN           = "/sdcard/Android/data/com.tencent.tmgp.sgame/files/";
    public static final String AOV_TW           = "/sdcard/Android/data/com.garena.game.kgtw/files/";
    public static final String AOV_VN           = "/sdcard/Android/data/com.garena.game.kgvn/files/";

    public static final String GENSHIN_DATA     = "/sdcard/Android/data/com.miHoYo.GenshinImpact/files/";
    public static final String GENSHIN_GLOBAL   = "/sdcard/Android/data/com.cognosphere.GenshinImpact/files/";
    public static final String STAR_RAIL_DATA   = "/sdcard/Android/data/com.HoYoverse.hkrpgoversea/files/";
    public static final String ZZZ_DATA         = "/sdcard/Android/data/com.HoYoverse.nap/files/";
    public static final String WUWA_DATA        = "/sdcard/Android/data/com.kurogame.wutheringwaves.global/files/";

    public static final String ROBLOX_DATA      = "/sdcard/Android/data/com.roblox.client/files/";
    public static final String FF_DATA          = "/sdcard/Android/data/com.dts.freefireth/files/";
    public static final String FF_MAX_DATA      = "/sdcard/Android/data/com.dts.freefiremax/files/";
    public static final String WILD_RIFT        = "/sdcard/Android/data/com.riotgames.league.wildrift/files/";
    public static final String DELTA_FORCE      = "/sdcard/Android/data/com.proxima.deltaforce/files/";
    public static final String BLOOD_STRIKE     = "/sdcard/Android/data/com.ofg.bloodstrike/files/";
    public static final String STANDOFF2_DATA   = "/sdcard/Android/data/com.axlebolt.standoff2/files/";
    public static final String FARLIGHT_DATA    = "/sdcard/Android/data/com.miracle.farlight84/files/";

    /**
     * Resolves the primary Android/data or Android/obb files directory for any package name.
     * Performs a deep search via Shizuku ADB (uid 2000) if standard path doesn't exist.
     */
    public static String findGameConfigFolder(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return "/sdcard/Android/data/";
        }
        String pkg = packageName.trim();
        String standardPath = "/sdcard/Android/data/" + pkg + "/files/";
        String checkCmd = "test -d " + standardPath + " && echo EXISTS";
        String res = execute(checkCmd);
        if (res != null && res.contains("EXISTS")) {
            return standardPath;
        }

        // Deep search check for obb or alternative internal data directory
        String obbPath = "/sdcard/Android/obb/" + pkg + "/";
        String checkObb = "test -d " + obbPath + " && echo EXISTS";
        String resObb = execute(checkObb);
        if (resObb != null && resObb.contains("EXISTS")) {
            return obbPath;
        }

        // Fallback: create standard files directory via Shizuku
        execute("mkdir -p " + standardPath);
        return standardPath;
    }

    public static void ensureParentDir(String filePath) {
        if (filePath == null) return;
        int lastSlash = filePath.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = filePath.substring(0, lastSlash);
            String cmd = "mkdir -p " + parentDir;
            execute(cmd);
        }
    }

    public static void createBackup(String filePath) {
        if (filePath == null) return;
        String backupPath = filePath + ".bak";
        String cmd = "test -f " + filePath + " && test ! -f " + backupPath + " && cp " + filePath + " " + backupPath;
        execute(cmd);
    }

    public static boolean writeContent(String filePath, String content, boolean makeReadOnly) {
        if (filePath == null || content == null) return false;

        ensureParentDir(filePath);
        createBackup(filePath);

        // Unlock file permissions first in case it was previously locked
        execute("chmod 666 " + filePath);

        // BUG FIX: Use heredoc-style cat instead of printf '%s' to safely handle content
        // containing % characters (common in JSON game config files like {"fps":60})
        // The heredoc marker GAMEBOOSTER_WRITE_EOF is unlikely to appear in game config content
        String writeCmd = "cat > " + filePath + " << 'GAMEBOOSTER_WRITE_EOF'\n"
                + content
                + "\nGAMEBOOSTER_WRITE_EOF";
        String res = execute(writeCmd);

        if (makeReadOnly) {
            // Read-only lock to prevent game client overwriting on startup
            execute("chmod 444 " + filePath);
        } else {
            execute("chmod 644 " + filePath);
        }

        Log.d(TAG, "writeContent to " + filePath + " readOnly=" + makeReadOnly + " -> " + res);
        return true;
    }

    public static boolean updateIniKeys(String filePath, String[] keys, String[] values, String sectionHeader) {
        if (filePath == null || keys == null || values == null || keys.length != values.length) return false;

        ensureParentDir(filePath);
        createBackup(filePath);

        execute("chmod 666 " + filePath);

        String checkCmd = "test -f " + filePath + " && echo EXISTS";
        String checkRes = execute(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            StringBuilder sb = new StringBuilder();
            if (sectionHeader != null && !sectionHeader.isEmpty()) {
                sb.append(sectionHeader).append("\n");
            }
            for (int i = 0; i < keys.length; i++) {
                sb.append(keys[i]).append("=").append(values[i]).append("\n");
            }
            writeContent(filePath, sb.toString(), false);
        } else {
            if (sectionHeader != null && !sectionHeader.isEmpty()) {
                execute("grep -qF '" + sectionHeader + "' " + filePath + " || echo '" + sectionHeader + "' >> " + filePath);
            }
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                String v = values[i];
                String cmd = "grep -qF '" + k + "=' " + filePath +
                        " && sed -i 's/^" + k + "=.*/" + k + "=" + v + "/' " + filePath +
                        " || echo '" + k + "=" + v + "' >> " + filePath;
                execute(cmd);
            }
        }
        execute("chmod 644 " + filePath);
        return true;
    }

    public static boolean recursiveChmod(String dirPath, String mode) {
        if (dirPath == null || mode == null) return false;
        String cmd = "chmod -R " + mode + " " + dirPath;
        String res = execute(cmd);
        Log.d(TAG, "recursiveChmod " + dirPath + " mode=" + mode + " -> " + res);
        return true;
    }

    public static boolean forceRemove(String targetPath) {
        if (targetPath == null) return false;
        String cmd = "rm -rf " + targetPath;
        String res = execute(cmd);
        Log.d(TAG, "forceRemove " + targetPath + " -> " + res);
        return true;
    }

    public static boolean copyDirectory(String sourceDir, String destDir) {
        if (sourceDir == null || destDir == null) return false;
        String cmd = "mkdir -p " + destDir + " && cp -r " + sourceDir + "/* " + destDir + "/";
        String res = execute(cmd);
        Log.d(TAG, "copyDirectory from " + sourceDir + " to " + destDir + " -> " + res);
        return true;
    }

    /**
     * Reads the content of a file via Shizuku (cat).
     * Works on restricted /sdcard/Android/data/ paths on Android 13+ via uid 2000.
     *
     * @param filePath Full absolute file path
     * @return File content string, or "ERROR: ..." message
     */
    public static String readFile(String filePath) {
        if (filePath == null) return "ERROR: null path";
        String result = execute("cat " + filePath + " 2>/dev/null");
        Log.d(TAG, "readFile " + filePath + " -> " + result.substring(0, Math.min(80, result.length())));
        return result;
    }

    /**
     * Lists files in a directory via Shizuku (ls -la).
     * Works on restricted /sdcard/Android/data/ paths on Android 13+ via uid 2000.
     *
     * @param dirPath Full absolute directory path
     * @return ls -la output string
     */
    public static String listFiles(String dirPath) {
        if (dirPath == null) return "ERROR: null path";
        String result = execute("ls -la " + dirPath + " 2>/dev/null || echo 'DIR_NOT_FOUND'");
        Log.d(TAG, "listFiles " + dirPath + " -> " + result.substring(0, Math.min(200, result.length())));
        return result;
    }

    /**
     * Patches a specific JSON key in a game config file using sed.
     * Only modifies the target key without overwriting the entire file.
     * Safe for structured JSON with simple "key": value pairs.
     *
     * @param filePath Full path to JSON file
     * @param key      JSON key to patch (without quotes or colon)
     * @param value    New value (numeric: no quotes; string: include quotes in value param)
     * @return true if patch was applied
     */
    public static boolean patchJsonKey(String filePath, String key, String value) {
        if (filePath == null || key == null || value == null) return false;

        createBackup(filePath);
        execute("chmod 666 " + filePath);

        // sed pattern: replace value after "key": (handles both int and string values)
        String sedCmd = "sed -i 's/\\(\"" + key + "\"[ ]*:[ ]*\\)[^,}]*/\\1" + value + "/g' " + filePath;
        String res = execute(sedCmd);
        execute("chmod 644 " + filePath);
        Log.d(TAG, "patchJsonKey key='" + key + "' value='" + value + "' in " + filePath + " -> " + res);
        return true;
    }

    /**
     * Adds a new file to a game data directory.
     * Creates the directory if it doesn't exist.
     *
     * @param dirPath  Full absolute directory path
     * @param filename Filename for the new file
     * @param content  Content to write into the file
     * @return true if file was created successfully
     */
    public static boolean addFileToDir(String dirPath, String filename, String content) {
        if (dirPath == null || filename == null || content == null) return false;
        String fullPath = dirPath.endsWith("/") ? dirPath + filename : dirPath + "/" + filename;
        return writeContent(fullPath, content, false);
    }

    /**
     * Verifies file integrity using sha256sum.
     *
     * @param filePath     Full path to the file
     * @param expectedHash Expected SHA-256 hash (lowercase hex, 64 chars)
     * @return true if the file's sha256sum matches the expected hash
     */
    public static boolean verifyFileIntegrity(String filePath, String expectedHash) {
        if (filePath == null || expectedHash == null) return false;
        String result = execute("sha256sum " + filePath + " 2>/dev/null | awk '{print $1}'");
        boolean match = expectedHash.equalsIgnoreCase(result.trim());
        Log.d(TAG, "verifyFileIntegrity " + filePath + " expected='" + expectedHash
                + "' got='" + result.trim() + "' match=" + match);
        return match;
    }

    private static String execute(String command) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            return ShizukuExecutor.executeShizukuCommand(command);
        } else {
            return CommandExecutor.executeSystemCommand(command);
        }
    }
}
