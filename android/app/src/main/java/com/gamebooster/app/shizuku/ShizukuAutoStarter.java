package com.gamebooster.app.shizuku;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.gamebooster.app.engine.ShellExecutor;

import java.io.File;

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

        String starterPath = getShizukuStarterPath(context);
        if (starterPath == null) {
            return new StartResult(false, null, "ERROR: Shizuku starter binary (libshizuku.so) not found. Ensure Shizuku is installed.");
        }

        Log.i(TAG, "⚡ Attempting 1-Tap Auto-Start of Shizuku daemon via: " + starterPath);

        // Build root command: su -c <starterPath>
        String cmd = starterPath.startsWith("sh ") ? starterPath : starterPath;
        ShellExecutor.CommandResult res = ShellExecutor.executeRootCommand(cmd);

        boolean success = res.isSuccess();
        Log.i(TAG, "Shizuku Auto-Start result exitCode=" + res.exitCode + " output='" + res.stdout + "' err='" + res.stderr + "'");

        return new StartResult(success, starterPath, success ? res.stdout : res.stderr);
    }
}
