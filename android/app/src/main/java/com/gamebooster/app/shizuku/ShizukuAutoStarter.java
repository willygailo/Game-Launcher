package com.gamebooster.app.shizuku;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.engine.ShellExecutor;

import java.io.File;
import java.io.IOException;

/**
 * ShizukuAutoStarter — Dynamically resolves the native starter binary path (libshizuku.so)
 * of the installed moe.shizuku.privileged.api package via PackageManager and executes
 * daemon startup via root shell (su) or privileged command.
 */
public class ShizukuAutoStarter {

    private static final String TAG = "ShizukuAutoStarter";
    public static final String SHIZUKU_PKG = "moe.shizuku.privileged.api";

    public static class StartResult {
        public final boolean success;
        public final String starterPath;
        public final String output;

        public StartResult(boolean success, String starterPath, String output) {
            this.success = success;
            this.starterPath = starterPath;
            this.output = output;
        }
    }

    /**
     * Dynamically locates the libshizuku.so binary or starter script path
     * from PackageManager without hardcoding package hash directories.
     */
    public static String getShizukuStarterPath(Context context) {
        if (context == null) return null;

        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(SHIZUKU_PKG, 0);

            if (appInfo != null) {
                // 1. Dynamic nativeLibraryDir lookup (e.g. /data/app/~~<hash>/moe.shizuku.../lib/arm64)
                if (appInfo.nativeLibraryDir != null) {
                    File nativeLib = new File(appInfo.nativeLibraryDir, "libshizuku.so");
                    if (nativeLib.exists()) {
                        Log.i(TAG, "Found libshizuku.so via PackageManager nativeLibraryDir: " + nativeLib.getAbsolutePath());
                        return nativeLib.getAbsolutePath();
                    }
                }

                // 2. Direct internal app data starter fallback
                File internalStarter = new File("/data/data/" + SHIZUKU_PKG + "/starter");
                if (internalStarter.exists()) {
                    Log.i(TAG, "Found internal Shizuku starter at: " + internalStarter.getAbsolutePath());
                    return internalStarter.getAbsolutePath();
                }

                // 3. External storage script fallback
                File sdcardStarter = new File("/sdcard/Android/data/" + SHIZUKU_PKG + "/files/start.sh");
                if (sdcardStarter.exists()) {
                    Log.i(TAG, "Found SD card Shizuku start script at: " + sdcardStarter.getAbsolutePath());
                    return "sh " + sdcardStarter.getAbsolutePath();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Shizuku package " + SHIZUKU_PKG + " is not installed on this device.");
        } catch (Throwable t) {
            Log.e(TAG, "Error looking up Shizuku starter path", t);
        }

        return null;
    }

    /**
     * Executes the Shizuku server daemon starter command via Root (su) or elevated shell.
     */
    public static StartResult startShizukuDaemon(Context context) {
        if (context == null) {
            return new StartResult(false, null, "ERROR: Context is null");
        }

        // Primary: use the terminal script-based approach (dynamic, hash-independent)
        Log.i(TAG, "⚡ Attempting Shizuku start via ShizukuTerminalManager script...");
        try {
            ShellExecutor.CommandResult scriptResult =
                    ShizukuTerminalManager.startShizukuViaScript(context);
            if (scriptResult.isSuccess()) {
                String path = ShizukuTerminalManager.START_SCRIPT_PATH;
                Log.i(TAG, "✅ Shizuku started via terminal script: " + path);
                return new StartResult(true, path, scriptResult.stdout);
            }
            Log.w(TAG, "Terminal script start failed, falling back to PackageManager path...");
        } catch (Exception e) {
            Log.w(TAG, "Terminal script exception, falling back: " + e.getMessage());
        }

        // Fallback: direct PackageManager path resolution
        String starterPath = getShizukuStarterPath(context);
        if (starterPath == null) {
            return new StartResult(false, null,
                    "ERROR: Shizuku starter binary (libshizuku.so) not found. Ensure Shizuku is installed.");
        }

        Log.i(TAG, "⚡ Attempting Shizuku daemon start via PackageManager path: " + starterPath);

        // BUG FIX: was 'starterPath.startsWith("sh ") ? starterPath : starterPath' — both branches
        // returned the same value, so the 'sh' prefix was never prepended for .sh script paths.
        String cmd = starterPath.startsWith("sh ") ? starterPath : "sh " + starterPath;
        ShellExecutor.CommandResult res = ShellExecutor.executeRootCommand(cmd);

        boolean success = res.isSuccess();
        Log.i(TAG, "Shizuku Auto-Start result exitCode=" + res.exitCode
                + " output='" + res.stdout + "' err='" + res.stderr + "'");

        return new StartResult(success, starterPath, success ? res.stdout : res.stderr);
    }
}
