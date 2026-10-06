package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import java.io.File;

/**
 * CodmAceBypassPatcher — Dedicated in-storage patcher for Tencent Anti-Cheat Expert & CrashSight.
 * Operates on sandboxed preferences and configuration files to suppress telemetry and security flags.
 */
public final class CodmAceBypassPatcher {

    private static final String TAG = "CodmAceBypassPatcher";

    private CodmAceBypassPatcher() {}

    /**
     * Applies in-storage ACE and CrashSight suppression patches.
     */
    public static void applyBypass(String packageName) {
        if (packageName == null || packageName.isEmpty()) return;

        Log.i(TAG, "⚡ Applying ACE & Telemetry suppression for " + packageName);

        try {
            // Nullify known CrashSight and telemetry preference files
            String[] targetPrefs = {
                "/data/data/" + packageName + "/shared_prefs/CrashSight.xml",
                "/data/data/" + packageName + "/shared_prefs/BuglySdkInfos.xml",
                "/data/data/" + packageName + "/shared_prefs/tprt_preference.xml",
                "/data/data/" + packageName + "/shared_prefs/anogs_conf.xml"
            };

            for (String prefPath : targetPrefs) {
                String cmd = "if [ -f \"" + prefPath + "\" ]; then echo '<?xml version=\"1.0\" encoding=\"utf-8\"?><map><boolean name=\"is_disabled\" value=\"true\" /></map>' > \"" + prefPath + "\"; chmod 444 \"" + prefPath + "\"; fi";
                exec(cmd);
            }

            // Suppress ACE background worker directories
            String[] blockedDirs = {
                "/data/data/" + packageName + "/files/anogs",
                "/data/data/" + packageName + "/files/tprt",
                "/data/data/" + packageName + "/files/crashsight"
            };

            for (String dir : blockedDirs) {
                exec("rm -rf " + dir + "; mkdir -p " + dir + "; chmod 000 " + dir);
            }

            Log.i(TAG, "✅ ACE in-storage suppression completed for " + packageName);
        } catch (Throwable t) {
            Log.w(TAG, "ACE bypass patch warning: " + t.getMessage());
        }
    }

    private static void exec(String cmd) {
        try {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } catch (Throwable ignored) {
            try {
                Runtime.getRuntime().exec(new String[]{"su", "-c", cmd}).waitFor();
            } catch (Exception e) {
                Log.w(TAG, "exec failed: " + cmd + " → " + e.getMessage());
            }
        }
    }
}
