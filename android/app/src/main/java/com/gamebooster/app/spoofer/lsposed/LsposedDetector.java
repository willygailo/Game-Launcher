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
 *
 * LSPosed v1.7+ stores module enablement in SQLite
 * (/data/adb/lspd/config/modules_config.db, tables modules/scope/configs);
 * older versions used file-based modules.list. The detection probes both,
 * preferring the SQLite enabled=1 flag when sqlite3 is available.
 *
 * Results are cached for a short TTL (10s), NOT for the process lifetime —
 * probing before Shizuku permission is granted, or before the user enables
 * the module, must not poison the result forever.
 */
public final class LsposedDetector {

    private static final String TAG = "LsposedDetector";
    private static final String LSPOSED_MANAGER_PKG = "org.lsposed.manager";
    private static final String MODULE_PKG = "com.gamebooster.app";
    private static final String CONFIG_DB = "/data/adb/lspd/config/modules_config.db";
    private static final String LEGACY_MODULES_LIST = "/data/adb/lspd/config/modules.list";

    private static final long CACHE_TTL_MS = 10_000L;

    private static volatile boolean cachedInstalled = false;
    private static volatile long cachedInstalledAt = 0L;
    private static volatile boolean cachedEnabled = false;
    private static volatile long cachedEnabledAt = 0L;

    private LsposedDetector() {}

    /** True when the LSPosed framework directory exists on this device. */
    public static boolean isLsposedInstalled() {
        long now = System.currentTimeMillis();
        if (now - cachedInstalledAt < CACHE_TTL_MS) return cachedInstalled;
        cachedInstalled = probe("ls -d /data/adb/lspd 2>/dev/null");
        cachedInstalledAt = now;
        return cachedInstalled;
    }

    /** True when this app is enabled as a module in LSPosed (any user profile). */
    public static boolean isModuleEnabled() {
        long now = System.currentTimeMillis();
        if (now - cachedEnabledAt < CACHE_TTL_MS) return cachedEnabled;
        cachedEnabled = probeModuleEnabled();
        cachedEnabledAt = now;
        return cachedEnabled;
    }

    /**
     * Probes the LSPosed config store for module enablement:
     * 1. SQLite modules_config.db enabled=1 (LSPosed v1.7+, requires sqlite3 binary).
     * 2. Binary grep of the DB for the package name (enablement-agnostic presence).
     * 3. Legacy modules.list (LSPosed pre-1.7).
     */
    private static boolean probeModuleEnabled() {
        String sqlite = "sqlite3 " + CONFIG_DB
                + " \"SELECT count(*) FROM modules WHERE module_pkg_name='" + MODULE_PKG + "' AND enabled=1;\" 2>/dev/null";
        String res = probeRaw(sqlite);
        if (res != null && "1".equals(res.trim())) {
            Log.i(TAG, "LSPosed module enabled (SQLite modules_config.db)");
            return true;
        }

        String grep = "grep -a -c '" + MODULE_PKG + "' " + CONFIG_DB + " 2>/dev/null";
        String g = probeRaw(grep);
        if (g != null && !g.trim().isEmpty() && !"0".equals(g.trim())) {
            Log.i(TAG, "LSPosed module present in modules_config.db (enablement unverified)");
            return true;
        }

        String legacy = "grep -x '" + MODULE_PKG + "' " + LEGACY_MODULES_LIST + " 2>/dev/null";
        String l = probeRaw(legacy);
        if (l != null && !l.trim().isEmpty()) {
            Log.i(TAG, "LSPosed module enabled (legacy modules.list)");
            return true;
        }

        Log.i(TAG, "LSPosed module NOT detected as enabled");
        return false;
    }

    private static boolean probe(String cmd) {
        String res = probeRaw(cmd);
        return res != null && !res.startsWith("ERROR:") && !res.trim().isEmpty();
    }

    private static String probeRaw(String cmd) {
        try {
            return ShizukuExecutor.executeShizukuCommand(cmd);
        } catch (Throwable t) {
            Log.w(TAG, "probe failed: " + t.getMessage());
            return null;
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