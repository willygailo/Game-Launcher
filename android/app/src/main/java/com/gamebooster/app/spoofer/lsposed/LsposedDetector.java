package com.gamebooster.app.spoofer.lsposed;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LsposedDetector — Unified detection engine for both LSPosed (Root)
 * and LSPatch (Non-Root) ART-level hooking frameworks on Android 12–16.
 */
public final class LsposedDetector {

    private static final String TAG = "LsposedDetector";
    private static final String LSPOSED_MANAGER_PKG = "org.lsposed.manager";
    private static final String MODULE_PKG = "com.gamebooster.app";
    private static final String CONFIG_DB = "/data/adb/lspd/config/modules_config.db";
    private static final String LEGACY_MODULES_LIST = "/data/adb/lspd/config/modules.list";

    public enum FrameworkType {
        NONE("Standard Engine", "#94A3B8"),
        LSPOSED_ROOT("LSPosed Module (Root)", "#00F0FF"),
        LSPATCH_NON_ROOT("LSPatch Module (Non-Root)", "#00FF66");

        public final String displayName;
        public final String colorHex;

        FrameworkType(String displayName, String colorHex) {
            this.displayName = displayName;
            this.colorHex = colorHex;
        }
    }

    private static final long CACHE_TTL_MS = 10_000L;
    private static final long HEARTBEAT_TIMEOUT_MS = 120_000L; // 2 minutes

    private static volatile boolean cachedInstalled = false;
    private static volatile long cachedInstalledAt = 0L;
    private static volatile boolean cachedEnabled = false;
    private static volatile long cachedEnabledAt = 0L;

    // Package-to-last-seen timestamp map populated by SpoofPrefsProvider
    private static final Map<String, Long> HOOKED_GAMES_HEARTBEATS = new ConcurrentHashMap<>();

    private LsposedDetector() {}

    /**
     * Records an active query heartbeat from a game running our hooked SpoofModule.
     */
    public static void recordGameHeartbeat(String packageName) {
        if (packageName != null && !packageName.isEmpty()) {
            HOOKED_GAMES_HEARTBEATS.put(packageName, System.currentTimeMillis());
            Log.d(TAG, "Recorded active hook heartbeat from: " + packageName);
        }
    }

    /**
     * Returns true if any game process recently queried the spoof config (within 2 mins).
     */
    public static boolean isAnyGameHookedActive() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Long> entry : HOOKED_GAMES_HEARTBEATS.entrySet()) {
            if (now - entry.getValue() < HEARTBEAT_TIMEOUT_MS) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if LSPatch is installed on this device.
     */
    public static boolean isLspatchInstalled(Context context) {
        return LspatchHelper.isLspatchInstalled(context);
    }

    /** True when the LSPosed framework directory exists on this device (Root). */
    public static boolean isLsposedInstalled() {
        long now = System.currentTimeMillis();
        if (now - cachedInstalledAt < CACHE_TTL_MS) return cachedInstalled;
        
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            // Never block the main UI thread with shell commands
            refreshAsync(null, null);
            return cachedInstalled;
        }

        cachedInstalled = probe("ls -d /data/adb/lspd 2>/dev/null");
        cachedInstalledAt = now;
        return cachedInstalled;
    }

    /** True when this app is enabled as a module in LSPosed (Root). */
    public static boolean isModuleEnabled() {
        long now = System.currentTimeMillis();
        if (now - cachedEnabledAt < CACHE_TTL_MS) return cachedEnabled;

        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            // Never block the main UI thread with shell commands
            refreshAsync(null, null);
            return cachedEnabled;
        }

        cachedEnabled = probeModuleEnabled();
        cachedEnabledAt = now;
        return cachedEnabled;
    }

    /**
     * Explicitly refreshes LSPosed detection status asynchronously on a background worker thread.
     */
    public static void refreshAsync(Context context, Runnable onComplete) {
        com.gamebooster.app.core.AppExecutors.getInstance().executeScan(() -> {
            try {
                boolean installed = probe("ls -d /data/adb/lspd 2>/dev/null");
                boolean enabled = probeModuleEnabled();
                long now = System.currentTimeMillis();
                cachedInstalled = installed;
                cachedInstalledAt = now;
                cachedEnabled = enabled;
                cachedEnabledAt = now;
            } catch (Throwable t) {
                Log.w(TAG, "refreshAsync error: " + t.getMessage());
            } finally {
                if (onComplete != null) {
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(onComplete);
                }
            }
        });
    }

    /**
     * True when either LSPosed (Root) is enabled or an LSPatch (Non-Root) hooked game is active.
     */
    public static boolean isHookingActive(Context context) {
        return isModuleEnabled() || isAnyGameHookedActive() || (context != null && isLspatchInstalled(context) && isSpoofPrefsConfigured(context));
    }

    private static boolean isSpoofPrefsConfigured(Context context) {
        return com.gamebooster.app.spoofer.SpoofPreferences.isSpoofEnabled(context);
    }

    /**
     * Returns the detected active framework type.
     */
    public static FrameworkType getFrameworkType(Context context) {
        if (isModuleEnabled()) {
            return FrameworkType.LSPOSED_ROOT;
        }
        if (isAnyGameHookedActive() || isLspatchInstalled(context)) {
            return FrameworkType.LSPATCH_NON_ROOT;
        }
        return FrameworkType.NONE;
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