package com.gamebooster.app.booster;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * EsportsProcessPatcher — Gives target game process un-killable priority (oom_score_adj = -1000),
 * real-time scheduler CPU priority (renice -20), and purges system page caches prior to match entry.
 */
public class EsportsProcessPatcher {

    private static final String TAG = "EsportsProcessPatcher";

    /**
     * Elevates the target package process priority to maximum non-killable status.
     */
    public static boolean boostGameProcessPriority(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.trim();

        try {
            // 1. Purge Linux kernel page cache and dentries before game execution
            exec("sync; echo 3 > /proc/sys/vm/drop_caches");

            // 2. Find process ID (PID) of the game package
            String pidOut = execWithOutput("pidof " + pkg);
            if (pidOut != null && !pidOut.trim().isEmpty()) {
                String[] pids = pidOut.trim().split("\\s+");
                for (String pidStr : pids) {
                    try {
                        int pid = Integer.parseInt(pidStr.trim());
                        // Give un-killable OOM score (-1000 = SYSTEM/CRITICAL priority)
                        exec("echo -1000 > /proc/" + pid + "/oom_score_adj");
                        // Set highest CPU nice level (-20)
                        exec("renice -20 -p " + pid);
                        Log.i(TAG, "✔ Process PID " + pid + " (" + pkg + ") set to oom_score_adj -1000 & renice -20");
                    } catch (NumberFormatException ignored) {}
                }
            }

            // 3. Set global game driver opt-in and process affinity
            exec("settings put global game_driver_opt_in_apps " + pkg);
            exec("cmd game mode performance " + pkg);
            return true;

        } catch (Throwable e) {
            Log.e(TAG, "Failed to elevate game process priority for " + pkg, e);
            return false;
        }
    }

    private static void exec(String cmd) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            CommandExecutor.executeSystemCommand(cmd);
        }
    }

    private static String execWithOutput(String cmd) {
        if (ShizukuExecutor.hasShizukuPermission()) {
            return ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            return CommandExecutor.executeSystemCommand(cmd);
        }
    }
}
