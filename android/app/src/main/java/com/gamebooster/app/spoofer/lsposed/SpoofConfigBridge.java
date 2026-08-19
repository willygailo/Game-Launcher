package com.gamebooster.app.spoofer.lsposed;

import de.robv.android.xposed.XSharedPreferences;

/**
 * SpoofConfigBridge — in-game process bridge that reads the active spoof profile
 * from the launcher's SharedPreferences via LSPosed XSharedPreferences (root read).
 *
 * The launcher (com.gamebooster.app) persists user selection in
 * "device_spoofer_prefs"; the module runs inside the target game process and
 * resolves the effective profile: per-package override -> global profile.
 */
public final class SpoofConfigBridge {

    private static final String LAUNCHER_PACKAGE = "com.gamebooster.app";
    private static final String PREFS_NAME = "device_spoofer_prefs";
    private static final String KEY_SPOOF_ENABLED = "spoof_enabled";
    private static final String KEY_ACTIVE_PROFILE_ID = "active_profile_id";
    private static final String KEY_SPOOF_ALL_APPS = "spoof_all_apps";
    private static final String PREFIX_PKG_PROFILE = "pkg_profile_";

    private static XSharedPreferences cachedPrefs;
    private static long lastReloadMs = 0L;

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
        try {
            return prefs().getBoolean(KEY_SPOOF_ENABLED, false);
        } catch (Throwable t) {
            return false;
        }
    }

    /** True when the user wants spoofing applied to every app, not just games. */
    public static boolean isSpoofAllApps() {
        try {
            return prefs().getBoolean(KEY_SPOOF_ALL_APPS, false);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Resolves the effective SpoofProfile for the target package, or null when
     * spoofing is disabled or no profile is selected.
     */
    public static com.gamebooster.app.spoofer.SpoofProfile resolveProfile(String packageName) {
        if (!isSpoofEnabled()) return null;
        try {
            XSharedPreferences p = prefs();
            String profileId = null;
            if (packageName != null) {
                profileId = p.getString(PREFIX_PKG_PROFILE + packageName, null);
            }
            if (profileId == null || profileId.trim().isEmpty()) {
                profileId = p.getString(KEY_ACTIVE_PROFILE_ID, null);
            }
            if (profileId == null || profileId.trim().isEmpty()) return null;
            return com.gamebooster.app.spoofer.SpoofProfileRegistry.getById(profileId.trim());
        } catch (Throwable t) {
            return null;
        }
    }
}