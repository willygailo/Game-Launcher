package com.gamebooster.app.spoofer.lsposed;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

/**
 * LsposedDetector — launcher-side utilities to detect whether LSPosed is
 * installed and whether THIS app is enabled as a module (via Shizuku shell
 * probing /data/adb/lspd), plus a shortcut to open the LSPosed Manager.
 */
public final class LsposedDetector {

    private static final String TAG = "LsposedDetector";
    private static final String LSPOSED_MANAGER_PKG = "org.lsposed.manager";
    private static final String MODULE_PKG = "com.gamebooster.app";

    private static volatile Boolean cachedInstalled;
    private static volatile Boolean cachedEnabled;

    private LsposedDetector() {}

    /** True when the LSPosed framework directory exists on this device. */
    public static boolean isLsposedInstalled() {
        if (cachedInstalled != null) return cachedInstalled;
        boolean result = probe("ls -d /data/adb/lspd 2>/dev/null");
        cachedInstalled = result;
        return result;
    }

    /** True when this app is enabled as a module in LSPosed (any user profile). */
    public static boolean isModuleEnabled() {
        if (cachedEnabled != null) return cachedEnabled;
        boolean result = probe("ls /data/adb/lspd/config/*/modules/" + MODULE_PKG + " 2>/dev/null");
        cachedEnabled = result;
        return result;
    }

    private static boolean probe(String cmd) {
        try {
            String res = ShizukuExecutor.executeShizukuCommand(cmd);
            return res != null && !res.startsWith("ERROR:") && !res.trim().isEmpty();
        } catch (Throwable t) {
            Log.w(TAG, "probe failed: " + t.getMessage());
            return false;
        }
    }

    /** Opens the LSPosed Manager app, or the Play/Web page if missing. */
    public static void openLsposedManager(Context context) {
        if (context == null) return;
        try {
            Intent launch = context.getPackageManager().getLaunchIntentForPackage(LSPOSED_MANAGER_PKG);
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launch);
                return;
            }
        } catch (Throwable ignored) {}
        try {
            Intent store = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/LSPosed/LSPosed/releases"));
            store.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(store);
        } catch (Throwable ignored) {}
    }
}