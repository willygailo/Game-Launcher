package com.gamebooster.app.spoofer.lsposed;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;

/**
 * SpoofConfigBridge — in-game process bridge that reads the active spoof profile
 * from the launcher's SharedPreferences.
 *
 * Two sources, in order:
 *  1. LSPosed XSharedPreferences (root): reads "device_spoofer_prefs" of the
 *     launcher directly (world-readable, xposedsharedprefs=true).
 *  2. SpoofPrefsProvider (non-root / LSPatch): the launcher exposes the same
 *     prefs through an exported ContentProvider, queryable from any app
 *     process without root. This is what makes the module work under LSPatch.
 *
 * The launcher (com.gamebooster.app) persists user selection in
 * "device_spoofer_prefs"; the module resolves the effective profile:
 * per-package override -> global profile.
 */
public final class SpoofConfigBridge {

    private static final String LAUNCHER_PACKAGE = "com.gamebooster.app";
    private static final String PREFS_NAME = "device_spoofer_prefs";
    private static final String PROVIDER_AUTHORITY = "com.gamebooster.app.spoofprefs";
    private static final String KEY_SPOOF_ENABLED = "spoof_enabled";
    private static final String KEY_ACTIVE_PROFILE_ID = "active_profile_id";
    private static final String KEY_SPOOF_ALL_APPS = "spoof_all_apps";
    private static final String PREFIX_PKG_PROFILE = "pkg_profile_";

    private static XSharedPreferences cachedPrefs;
    private static long lastReloadMs = 0L;
    private static Boolean providerReachable;

    private SpoofConfigBridge() {}

    private static XSharedPreferences prefs() {
        if (cachedPrefs == null) {
            cachedPrefs = new XSharedPreferences(LAUNCHER_PACKAGE, PREFS_NAME);
            cachedPrefs.makeWorldReadable();
        }
        long now = System.currentTimeMillis();
        if (now - lastReloadMs > 3000L) {
            cachedPrefs.reload();
            lastReloadMs = now;
        }
        return cachedPrefs;
    }

    /** True when the user enabled device spoofing in the launcher. */
    public static boolean isSpoofEnabled() {
        return "true".equalsIgnoreCase(resolveValue(KEY_SPOOF_ENABLED));
    }

    /** True when the user wants spoofing applied to every app, not just games. */
    public static boolean isSpoofAllApps() {
        return "true".equalsIgnoreCase(resolveValue(KEY_SPOOF_ALL_APPS));
    }

    /**
     * Resolves the effective SpoofProfile for the target package, or null when
     * spoofing is disabled or no profile is selected.
     */
    public static com.gamebooster.app.spoofer.SpoofProfile resolveProfile(String packageName) {
        if (!isSpoofEnabled()) return null;
        String profileId = null;
        if (packageName != null) {
            profileId = resolveValue(PREFIX_PKG_PROFILE + packageName);
        }
        if (profileId == null || profileId.trim().isEmpty()) {
            profileId = resolveValue(KEY_ACTIVE_PROFILE_ID);
        }
        if (profileId == null || profileId.trim().isEmpty()) return null;
        return com.gamebooster.app.spoofer.SpoofProfileRegistry.getById(profileId.trim());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Value resolution: XSharedPreferences (root) → ContentProvider (non-root)
    // ─────────────────────────────────────────────────────────────────────────

    private static String resolveValue(String key) {
        try {
            String v = prefs().getString(key, null);
            if (v != null && !v.isEmpty()) return v;
        } catch (Throwable ignored) {}
        try {
            String v = queryProvider(key);
            if (v != null && !v.isEmpty()) return v;
        } catch (Throwable ignored) {}
        return null;
    }

    private static String queryProvider(String key) {
        Context context = appContext();
        if (context == null) return null;
        Uri uri = Uri.parse("content://" + PROVIDER_AUTHORITY + "/spoof?key=" + Uri.encode(key));
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int valueIdx = cursor.getColumnIndex("value");
                if (valueIdx >= 0) return cursor.getString(valueIdx);
            }
        }
        return null;
    }

    private static Context appContext() {
        try {
            Class<?> at = XposedHelpers.findClass("android.app.ActivityThread", null);
            if (at != null) {
                Context ctx = (Context) XposedHelpers.callStaticMethod(at, "currentApplication");
                if (ctx != null) return ctx;
                Object thread = XposedHelpers.callStaticMethod(at, "currentActivityThread");
                if (thread != null) {
                    return (Context) XposedHelpers.callMethod(thread, "getSystemContext");
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }
}