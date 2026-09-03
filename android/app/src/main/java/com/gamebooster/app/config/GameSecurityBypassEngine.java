package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * GameSecurityBypassEngine — Multi-Tier Game Security, Anti-Tamper & SELinux Bypass Engine.
 *
 * Exclusively engineered for Android 13, 14, 15, and 16 (API 33-36).
 *
 * Neutralizes the 4 primary game security barriers that prevent configuration overrides:
 * 1. SELinux DAC & MAC Context Checks: Restores app_data_file labels and assigns native game UID ownership.
 * 2. Anti-Tamper Startup Revert: Applies read-only chmod 444 file locks so game CDNs cannot overwrite configs.
 * 3. File Timestamp Integrity Audits: Clones official APK timestamps (touch -r) to spoof installation age.
 * 4. Anti-Cheat Security Logging: Null-routes and locks Tencent ACE, Bugly, Moonton, and Crashlytics telemetry folders.
 */
public final class GameSecurityBypassEngine {

    private static final String TAG = "SecurityBypassEngine";

    private GameSecurityBypassEngine() {}

    /**
     * Data structure holding resolved UID and GID for a game package.
     */
    public static final class GameIdentity {
        public final int uid;
        public final int gid;
        public final boolean isValid;

        public GameIdentity(int uid, int gid, boolean isValid) {
            this.uid = uid;
            this.gid = gid;
            this.isValid = isValid;
        }

        public static GameIdentity fallback() {
            return new GameIdentity(10000, 10000, false);
        }
    }

    /**
     * Resolves the real Linux UID and GID of the game package via privileged stat or pm inspection.
     */
    public static GameIdentity resolveGameUidGid(String packageName) {
        if (!ShellSafety.isSafePackageName(packageName)) {
            return GameIdentity.fallback();
        }
        String pkg = packageName.trim().toLowerCase(Locale.ROOT);

        // 1. Check /data/data/<pkg> directory ownership
        if (ShizukuExecutor.hasShizukuPermission()) {
            try {
                String res = ShizukuExecutor.executeShizukuCommand("stat -c \"%u:%g\" '/data/data/" + pkg + "' 2>/dev/null");
                if (res != null && res.contains(":")) {
                    String[] parts = res.trim().split(":");
                    int u = Integer.parseInt(parts[0].trim());
                    int g = Integer.parseInt(parts[1].trim());
                    if (u >= 10000) {
                        return new GameIdentity(u, g, true);
                    }
                }
            } catch (Throwable ignored) {}

            // 2. Query package manager uid via cmd package
            try {
                String pmRes = ShizukuExecutor.executeShizukuCommand("cmd package list packages -U " + pkg + " 2>/dev/null");
                if (pmRes != null && pmRes.contains("uid:")) {
                    int idx = pmRes.indexOf("uid:");
                    String sub = pmRes.substring(idx + 4).trim();
                    int end = sub.indexOf(" ");
                    if (end < 0) end = sub.indexOf("\n");
                    String uidStr = (end > 0) ? sub.substring(0, end).trim() : sub;
                    int u = Integer.parseInt(uidStr);
                    if (u >= 10000) {
                        return new GameIdentity(u, u, true);
                    }
                }
            } catch (Throwable ignored) {}
        }

        return GameIdentity.fallback();
    }

    /**
     * Unlocks all config files for injection by lifting read-only locks and enabling write permissions (chmod 666 / 777).
     * Must be called in Phase 1 before patching files.
     */
    public static boolean unlockForInjection(String packageName) {
        if (!ShellSafety.isSafePackageName(packageName)) return false;
        String pkg = packageName.trim().toLowerCase(Locale.ROOT);
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);

        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) continue;
            File f = new File(path);
            File parent = f.getParentFile();
            if (parent != null) {
                sb.append("mkdir -p '").append(parent.getAbsolutePath()).append("' 2>/dev/null; ");
                sb.append("chmod 777 '").append(parent.getAbsolutePath()).append("' 2>/dev/null; ");
            }
            sb.append("chmod 666 '").append(path).append("' 2>/dev/null; ");
        }

        String cmd = sb.toString();
        if (!cmd.trim().isEmpty()) {
            executePrivileged(cmd);
        }
        Log.i(TAG, "Unlocked " + paths.size() + " config paths for injection: " + pkg);
        return true;
    }

    /**
     * Enforces correct SELinux labels and matches Linux UID/GID ownership on all injected files.
     * Prevents Android 13-16 SELinux policy denials (avc: denied).
     */
    public static boolean enforceSelinuxAndOwnershipBypass(String packageName, List<String> paths) {
        if (!ShellSafety.isSafePackageName(packageName)) return false;
        String pkg = packageName.trim().toLowerCase(Locale.ROOT);
        GameIdentity identity = resolveGameUidGid(pkg);

        StringBuilder sb = new StringBuilder();

        // 1. Restore SELinux contexts for the package directory trees
        sb.append("restorecon -F -R '/data/data/").append(pkg).append("' 2>/dev/null; ");
        sb.append("restorecon -F -R '/storage/emulated/0/Android/data/").append(pkg).append("' 2>/dev/null; ");
        sb.append("restorecon -F -R '/sdcard/Android/data/").append(pkg).append("' 2>/dev/null; ");

        // 2. Chown files to the game's actual UID/GID and force app_data_file SELinux label
        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) continue;
            if (identity.isValid) {
                sb.append("chown ").append(identity.uid).append(":").append(identity.gid).append(" '").append(path).append("' 2>/dev/null; ");
            }
            sb.append("chcon u:object_r:app_data_file:s0 '").append(path).append("' 2>/dev/null; ");
        }

        String cmd = sb.toString();
        if (!cmd.trim().isEmpty()) {
            executePrivileged(cmd);
        }
        Log.i(TAG, "Enforced SELinux & Ownership bypass for " + pkg + " [uid=" + (identity.isValid ? identity.uid : "default") + "]");
        return true;
    }

    /**
     * Applies Anti-Tamper Read-Only Lock (chmod 444) to injected config files.
     * The game process can read the configuration with full fidelity, but its startup routines
     * are strictly prevented from overwriting or reverting the file to server defaults.
     */
    public static boolean enforceAntiTamperFileLock(String packageName, List<String> paths) {
        if (!ShellSafety.isSafePackageName(packageName)) return false;
        String pkg = packageName.trim().toLowerCase(Locale.ROOT);

        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) continue;
            // Ensure file exists before locking
            sb.append("test -f '").append(path).append("' && chmod 444 '").append(path).append("' 2>/dev/null; ");
        }

        String cmd = sb.toString();
        if (!cmd.trim().isEmpty()) {
            executePrivileged(cmd);
        }
        Log.i(TAG, "Anti-Tamper Read-Only Lock (chmod 444) armed for " + pkg);
        return true;
    }

    /**
     * Cloaks file modification timestamps (mtime and atime) to match the official APK or parent directory.
     * Neutralizes game anti-cheat integrity scanners that flag recently modified config files.
     */
    public static boolean cloakFileTimestamps(String packageName, List<String> paths) {
        if (!ShellSafety.isSafePackageName(packageName)) return false;
        String pkg = packageName.trim().toLowerCase(Locale.ROOT);

        StringBuilder sb = new StringBuilder();
        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) continue;
            File parent = new File(path).getParentFile();
            String parentDir = parent != null ? parent.getAbsolutePath() : "/sdcard/Android/data/" + pkg;

            // Priority: Touch against official base.apk; fallback to parent directory
            sb.append("touch -r \"$(ls /data/app/*/'").append(pkg).append("'*/base.apk 2>/dev/null | head -n 1)\" '")
              .append(path).append("' 2>/dev/null || touch -r '").append(parentDir).append("' '").append(path).append("' 2>/dev/null; ");
        }

        String cmd = sb.toString();
        if (!cmd.trim().isEmpty()) {
            executePrivileged(cmd);
        }
        Log.i(TAG, "Timestamp cloaking enforced for " + pkg);
        return true;
    }

    /**
     * Purges and locks down anti-cheat security and crash telemetry directories across MLBB, PUBGM, CODM, Free Fire, etc.
     * Prevents games from generating or transmitting file tamper reports to their security servers.
     */
    public static boolean suppressSecurityTelemetryReporting(String packageName) {
        if (!ShellSafety.isSafePackageName(packageName)) return false;
        String pkg = packageName.trim().toLowerCase(Locale.ROOT);

        List<String> telemetryDirs = Arrays.asList(
                // PUBG Mobile / Tencent ACE & Bugly
                "/sdcard/Android/data/" + pkg + "/files/Saved/Logs",
                "/sdcard/Android/data/" + pkg + "/files/Saved/Crashes",
                "/data/data/" + pkg + "/files/tlog",
                "/data/data/" + pkg + "/files/Logs",
                "/data/data/" + pkg + "/files/crash_report",
                "/data/data/" + pkg + "/files/turing_log",
                "/data/data/" + pkg + "/app_bugly",
                // Mobile Legends / Moonton Telemetry
                "/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/android/Logs",
                "/data/data/" + pkg + "/files/apm_logs",
                "/data/data/" + pkg + "/files/common_log",
                // CODM / Activision / Tencent
                "/sdcard/Android/data/" + pkg + "/files/tss_log",
                "/data/data/" + pkg + "/files/tss_log",
                "/data/data/" + pkg + "/files/anr",
                // Free Fire / Garena
                "/sdcard/Android/data/" + pkg + "/files/report",
                "/data/data/" + pkg + "/files/report"
        );

        StringBuilder sb = new StringBuilder();
        for (String dir : telemetryDirs) {
            sb.append("rm -rf '").append(dir).append("/*' 2>/dev/null; ");
            sb.append("mkdir -p '").append(dir).append("' 2>/dev/null; ");
            sb.append("touch '").append(dir).append("/.nomedia' 2>/dev/null; ");
            sb.append("chmod 000 '").append(dir).append("' 2>/dev/null; ");
        }

        String cmd = sb.toString();
        if (!cmd.trim().isEmpty()) {
            executePrivileged(cmd);
        }
        Log.i(TAG, "Security telemetry reporting directories neutralized for " + pkg);
        return true;
    }

    /**
     * Orchestrates the complete post-injection security bypass sequence:
     * 1. Aligns Linux UID/GID ownership.
     * 2. Restores SELinux security context.
     * 3. Cloaks file timestamps.
     * 4. Applies Anti-Tamper Read-Only Lock (chmod 444).
     * 5. Neutralizes anti-cheat telemetry directories.
     */
    public static boolean postInjectionBypassAndLock(String packageName) {
        if (!ShellSafety.isSafePackageName(packageName)) return false;
        String pkg = packageName.trim().toLowerCase(Locale.ROOT);
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);

        Log.i(TAG, "🛡️ [SecurityBypass] Running post-injection bypass & anti-tamper lock for " + pkg);

        // 1. Fix ownership & SELinux context
        enforceSelinuxAndOwnershipBypass(pkg, paths);

        // 2. Cloak file timestamps to installation baseline
        cloakFileTimestamps(pkg, paths);

        // 3. Apply Anti-Tamper Read-Only Lock (chmod 444)
        enforceAntiTamperFileLock(pkg, paths);

        // 4. Suppress and null-route anti-cheat telemetry reporting
        suppressSecurityTelemetryReporting(pkg);

        Log.i(TAG, "✅ [SecurityBypass] 4-layer security bypass successfully enforced for " + pkg);
        return true;
    }

    /**
     * Executes shell command via elevated Shizuku shell or fallback CommandExecutor.
     */
    private static void executePrivileged(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) return;
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        } catch (Throwable t) {
            Log.w(TAG, "executePrivileged non-fatal note: " + t.getMessage());
        }
    }
}
