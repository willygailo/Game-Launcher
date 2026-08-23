package com.gamebooster.app.anticheat;

import android.util.Log;

import androidx.annotation.NonNull;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.util.List;

/**
 * TelemetrySinkhole — Suppresses anti-cheat telemetry uploads, crash dump collectors,
 * and background loggers to protect game sessions and eliminate background I/O lag.
 */
public final class TelemetrySinkhole {

    private static final String TAG = "TelemetrySinkhole";

    private TelemetrySinkhole() {}

    /**
     * Executes telemetry suppression and log sanitization for a target game package.
     */
    public static boolean applySinkholeForPackage(@NonNull String packageName) {
        if (packageName.isEmpty()) return false;

        GameAntiCheatRegistry.GameSecurityProfile profile = GameAntiCheatRegistry.getProfile(packageName);

        StringBuilder sb = new StringBuilder();

        // 1. Flush system logcat buffers so games cannot inspect past launcher activity
        if (profile.requiresLogcatFlush) {
            sb.append("logcat -c 2>/dev/null; ");
            sb.append("logcat -b all -c 2>/dev/null; ");
            sb.append("setprop persist.logd.logpersistd \"\" 2>/dev/null; ");
        }

        // 2. Null-route game crash reporting and telemetry log directories on disk
        String[] targetDirs = new String[]{
                "/sdcard/Android/data/" + packageName + "/files/Logs",
                "/sdcard/Android/data/" + packageName + "/files/logs",
                "/sdcard/Android/data/" + packageName + "/files/CrashDumps",
                "/sdcard/Android/data/" + packageName + "/files/Telemetry",
                "/sdcard/Android/data/" + packageName + "/files/Bugly",
                "/data/data/" + packageName + "/files/logs",
                "/data/data/" + packageName + "/files/crash"
        };

        for (String dir : targetDirs) {
            sb.append("rm -rf \"").append(dir).append("/*\" 2>/dev/null; ");
            sb.append("mkdir -p \"").append(dir).append("\" 2>/dev/null; ");
            sb.append("touch \"").append(dir).append("/.nomedia\" 2>/dev/null; ");
        }

        // 3. Drop anr / tombstones buffers in /data/
        sb.append("rm -rf /data/anr/* 2>/dev/null; ");
        sb.append("rm -rf /data/tombstones/* 2>/dev/null; ");

        executeCommand(sb.toString());
        Log.i(TAG, "Telemetry sinkhole active for " + packageName + " (" + profile.antiCheatType.displayName + ")");
        return true;
    }

    /**
     * Flushes global OS log buffers.
     */
    public static void flushGlobalLogcat() {
        executeCommand("logcat -c 2>/dev/null; logcat -b all -c 2>/dev/null");
    }

    private static void executeCommand(String command) {
        if (ShizukuFileManager.hasFullAccess()) {
            ShizukuExecutor.executeShizukuCommand(command);
        } else {
            CommandExecutor.executeSystemCommand(command);
        }
    }
}
