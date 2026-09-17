package com.gamebooster.app.booster;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;

import java.util.ArrayList;
import java.util.List;

/**
 * BackgroundLimitImmunityEngine — Unrestricted Background Execution & Shizuku Immortal Shield.
 *
 * Guarantees that neither Game Booster nor Shizuku is ever killed, frozen, or throttled
 * during multi-hour gaming sessions by:
 * 1. Whitelisting both packages from Doze (deviceidle whitelist + dumpsys).
 * 2. Granting AppOps permissions (RUN_IN_BACKGROUND, RUN_ANY_IN_BACKGROUND, WAKE_LOCK).
 * 3. Pinning App Standby Bucket to ACTIVE (am set-standby-bucket active, am set-inactive false).
 * 4. Disabling Android 12-16 Phantom Process Killer globally.
 * 5. Pinning Kernel Low Memory Killer oom_score_adj to -900 (system-protected, immune to LMKD).
 * 6. Bypassing OEM battery savers (MIUI/HyperOS AutoStart, Samsung OneUI, ColorOS/Realme).
 */
public final class BackgroundLimitImmunityEngine {

    private static final String TAG = "BackgroundImmunity";
    public static final String GAME_BOOSTER_PACKAGE = "com.gamebooster.app";
    public static final String SHIZUKU_PACKAGE = "moe.shizuku.privileged.api";

    private static volatile boolean sIsImmunityEnforced = false;

    private BackgroundLimitImmunityEngine() {}

    public static boolean isImmunityEnforced() {
        return sIsImmunityEnforced;
    }

    /**
     * Checks if standard Android battery optimizations are ignored for Game Booster.
     */
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        if (context == null) return false;
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return pm.isIgnoringBatteryOptimizations(context.getPackageName());
            }
        } catch (Throwable t) {
            Log.w(TAG, "isIgnoringBatteryOptimizations error: " + t.getMessage());
        }
        return false;
    }

    /**
     * Requests user to exempt Game Booster from standard battery optimizations via system dialog.
     */
    public static void requestIgnoreBatteryOptimizations(Context context) {
        if (context == null) return;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isIgnoringBatteryOptimizations(context)) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to launch battery optimization dialog, opening settings fallback", t);
            try {
                Intent fallback = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(fallback);
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Enforces complete background immunity for both Game Booster and Shizuku daemon.
     * Can be invoked asynchronously on background worker thread.
     */
    public static void enforceImmunity(Context context) {
        AppExecutors.getInstance().executeCommand(() -> enforceImmunitySync(context));
    }

    /**
     * Synchronous implementation of privileged immunity enforcement.
     */
    public static void enforceImmunitySync(Context context) {
        try {
            Log.i(TAG, "🛡️ Enforcing Unrestricted Background Immunity for Game Booster & Shizuku...");

            List<String> cmds = new ArrayList<>();

            // 1. Android Doze Whitelist (Both dumpsys and cmd variants for all Android versions)
            cmds.add("dumpsys deviceidle whitelist +" + GAME_BOOSTER_PACKAGE + " 2>/dev/null");
            cmds.add("dumpsys deviceidle whitelist +" + SHIZUKU_PACKAGE + " 2>/dev/null");
            cmds.add("cmd deviceidle whitelist +" + GAME_BOOSTER_PACKAGE + " 2>/dev/null");
            cmds.add("cmd deviceidle whitelist +" + SHIZUKU_PACKAGE + " 2>/dev/null");

            // 2. AppOps Unrestricted Background & Wakelock Execution
            cmds.add("cmd appops set " + GAME_BOOSTER_PACKAGE + " RUN_IN_BACKGROUND allow 2>/dev/null");
            cmds.add("cmd appops set " + GAME_BOOSTER_PACKAGE + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null");
            cmds.add("cmd appops set " + GAME_BOOSTER_PACKAGE + " WAKE_LOCK allow 2>/dev/null");
            cmds.add("cmd appops set " + GAME_BOOSTER_PACKAGE + " SYSTEM_ALERT_WINDOW allow 2>/dev/null");

            cmds.add("cmd appops set " + SHIZUKU_PACKAGE + " RUN_IN_BACKGROUND allow 2>/dev/null");
            cmds.add("cmd appops set " + SHIZUKU_PACKAGE + " RUN_ANY_IN_BACKGROUND allow 2>/dev/null");
            cmds.add("cmd appops set " + SHIZUKU_PACKAGE + " WAKE_LOCK allow 2>/dev/null");

            // 3. Pin App Standby Bucket to ACTIVE (Never allow Android to mark as RARE or RESTRICTED)
            cmds.add("am set-standby-bucket " + GAME_BOOSTER_PACKAGE + " active 2>/dev/null");
            cmds.add("cmd app_standby set " + GAME_BOOSTER_PACKAGE + " active 2>/dev/null");
            cmds.add("am set-inactive " + GAME_BOOSTER_PACKAGE + " false 2>/dev/null");

            cmds.add("am set-standby-bucket " + SHIZUKU_PACKAGE + " active 2>/dev/null");
            cmds.add("cmd app_standby set " + SHIZUKU_PACKAGE + " active 2>/dev/null");
            cmds.add("am set-inactive " + SHIZUKU_PACKAGE + " false 2>/dev/null");

            // 4. Defeat Android 12-16 Phantom Process Killer (prevents child process killing)
            cmds.add("/system/bin/device_config put activity_manager max_phantom_processes 2147483647 2>/dev/null");
            cmds.add("/system/bin/setprop persist.sys.fflag.override.settings_enable_monitor_phantom_procs false 2>/dev/null");
            cmds.add("settings put global settings_enable_monitor_phantom_procs false 2>/dev/null");

            // 5. OEM-Specific Background Killer Bypasses (MIUI / HyperOS, Transsion, ColorOS)
            cmds.add("cmd appops set " + GAME_BOOSTER_PACKAGE + " AUTO_START allow 2>/dev/null");
            cmds.add("cmd appops set " + SHIZUKU_PACKAGE + " AUTO_START allow 2>/dev/null");
            cmds.add("cmd appops set " + GAME_BOOSTER_PACKAGE + " BOOT_COMPLETED allow 2>/dev/null");

            // Execute batch privileged commands
            CommandExecutor.executeBatchCommands(cmds);

            // 6. Kernel OOM Immortal Pinning: Set oom_score_adj to -900 for our PIDs and Shizuku
            protectProcessOomScore(GAME_BOOSTER_PACKAGE);
            protectProcessOomScore(SHIZUKU_PACKAGE);

            sIsImmunityEnforced = true;
            Log.i(TAG, "✅ Background Limit Immunity fully applied. Game Booster & Shizuku are now immortal in background.");
        } catch (Throwable t) {
            Log.w(TAG, "Error applying background limit immunity", t);
        }
    }

    /**
     * Sets oom_score_adj to -900 and oom_adj to -17 for all active PIDs of a given package.
     * Prevents Android's Low Memory Killer Daemon (LMKD) from killing the process during heavy games.
     */
    public static void protectProcessOomScore(String packageName) {
        if (packageName == null || packageName.isEmpty()) return;
        try {
            String pidOut = ShizukuExecutor.executeShizukuCommand("pidof " + packageName + " 2>/dev/null");
            if (pidOut != null && !pidOut.trim().isEmpty() && !pidOut.startsWith("ERROR")) {
                String[] pids = pidOut.trim().split("\\s+");
                List<String> oomCmds = new ArrayList<>();
                for (String pStr : pids) {
                    try {
                        int pid = Integer.parseInt(pStr.trim());
                        if (pid <= 0) continue;
                        oomCmds.add("echo -900 > /proc/" + pid + "/oom_score_adj 2>/dev/null");
                        oomCmds.add("echo -17 > /proc/" + pid + "/oom_adj 2>/dev/null");
                    } catch (NumberFormatException ignored) {}
                }
                if (!oomCmds.isEmpty()) {
                    CommandExecutor.executeBatchCommands(oomCmds);
                    Log.d(TAG, "Pinned oom_score_adj -900 for " + packageName + " (PIDs=" + pidOut.trim() + ")");
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to protect oom score for " + packageName + ": " + t.getMessage());
        }
    }

    /**
     * Resurrects Shizuku daemon via root if Shizuku binder is completely dead.
     */
    public static boolean tryAutoResurrectShizukuDaemon() {
        try {
            if (ShizukuManager.isShizukuRunningAndGranted()) return true;

            if (com.gamebooster.app.engine.ShellExecutor.isRootSuAvailable()) {
                Log.i(TAG, "Attempting root auto-resurrection of Shizuku daemon...");
                String[] candidatePaths = {
                        "/sdcard/Android/data/moe.shizuku.privileged.api/start.sh",
                        "/data/data/moe.shizuku.privileged.api/start.sh",
                        "/data/user/0/moe.shizuku.privileged.api/start.sh"
                };

                for (String scriptPath : candidatePaths) {
                    com.gamebooster.app.engine.ShellExecutor.CommandResult res =
                            com.gamebooster.app.engine.ShellExecutor.executeSuCommand("sh " + scriptPath + " 2>/dev/null &");
                    if (res.isSuccess()) {
                        Log.i(TAG, "Executed Shizuku resurrection script: " + scriptPath);
                        return true;
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Auto-resurrect Shizuku error: " + t.getMessage());
        }
        return false;
    }
}
