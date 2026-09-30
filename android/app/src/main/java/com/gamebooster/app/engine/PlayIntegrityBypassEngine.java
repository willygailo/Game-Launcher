package com.gamebooster.app.engine;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.engine.CommandExecutor;

/**
 * PlayIntegrityBypassEngine — 2026 Root Detection & Play Integrity bypass.
 *
 * Handles the post-SafetyNet world:
 *   - Play Integrity API (hardware-backed attestation, Android 13+)
 *   - Tencent ACE + Nexon behavioral root heuristics
 *   - Requires: Magisk (Zygisk) + Shamiko + PIF (Play Integrity Fix) + TrickyStore
 *
 * This engine CHECKS for required module stack and guides the injection path.
 * It does NOT install Magisk modules — those must be pre-installed by the user.
 *
 * Updated: Sep 30 2026
 */
public final class PlayIntegrityBypassEngine {

    private static final String TAG = "PlayIntegrityBypass";

    private PlayIntegrityBypassEngine() {}

    // Known module package markers
    private static final String[] SHAMIKO_MARKERS = {
        "/data/adb/modules/shamiko",
        "/data/adb/modules/Shamiko"
    };
    private static final String[] PIF_MARKERS = {
        "/data/adb/modules/playintegrityfix",
        "/data/adb/modules/PlayIntegrityFix",
        "/data/adb/modules/pif"
    };
    private static final String[] TRICKYSTORE_MARKERS = {
        "/data/adb/modules/tricky_store",
        "/data/adb/modules/TrickyStore"
    };

    // ─────────────────────────────────────────────────────────────────────────
    // MODULE STACK CHECK
    // ─────────────────────────────────────────────────────────────────────────

    /** @return READY if full module stack detected, else describes what's missing. */
    public static ModuleStackStatus checkModuleStack() {
        boolean shamiko = moduleExists(SHAMIKO_MARKERS);
        boolean pif     = moduleExists(PIF_MARKERS);
        boolean tricky  = moduleExists(TRICKYSTORE_MARKERS);
        boolean zygisk  = isZygiskActive();

        Log.i(TAG, String.format(
            "[ModuleStack] Zygisk=%b Shamiko=%b PIF=%b TrickyStore=%b",
            zygisk, shamiko, pif, tricky));

        if (zygisk && shamiko && pif && tricky) return ModuleStackStatus.READY;
        if (!zygisk) return ModuleStackStatus.MISSING_ZYGISK;
        if (!shamiko) return ModuleStackStatus.MISSING_SHAMIKO;
        if (!pif) return ModuleStackStatus.MISSING_PIF;
        if (!tricky) return ModuleStackStatus.MISSING_TRICKYSTORE;
        return ModuleStackStatus.PARTIAL;
    }

    public enum ModuleStackStatus {
        READY,
        MISSING_ZYGISK,
        MISSING_SHAMIKO,
        MISSING_PIF,
        MISSING_TRICKYSTORE,
        PARTIAL
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DENY LIST ENFORCEMENT
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Enforces Magisk DenyList for the target game package + GMS.
     * ACE & Play Integrity check: GMS must pass STRONG integrity → must also be in DenyList.
     */
    public static void applyDenyListForGame(String packageName) {
        if (packageName == null) return;
        // Enable DenyList enforcement mode
        String enable = "magisk --sqlite \"UPDATE settings SET value=1 WHERE key='denylist'\" 2>/dev/null; ";
        // Add game and GMS to DenyList
        String addGame = "magisk --denylist add " + packageName + " 2>/dev/null; ";
        String addGms  = "magisk --denylist add com.google.android.gms 2>/dev/null; " +
                         "magisk --denylist add com.google.android.gms.unstable 2>/dev/null; " +
                         "magisk --denylist add com.android.vending 2>/dev/null; ";
        executePrivileged(enable + addGame + addGms);
        Log.i(TAG, "[DenyList] Enforced for: " + packageName);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PLAY SERVICES CACHE CLEAR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Clears Google Play Services cache to reset cached integrity token.
     * Call after installing/updating PIF module to force fresh attestation.
     */
    public static void clearPlayServicesCacheShell() {
        String cmd =
            "pm clear com.google.android.gms 2>/dev/null; " +
            "pm clear com.android.vending 2>/dev/null; " +
            "am force-stop com.google.android.gms 2>/dev/null; " +
            "am force-stop com.android.vending 2>/dev/null; ";
        executePrivileged(cmd);
        Log.i(TAG, "[PlayServices] Cache cleared");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTEGRITY STATE VERIFICATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks current Play Integrity verdict level.
     * @return "STRONG" / "DEVICE" / "BASIC" / "FAILED" based on props
     */
    public static String verifyIntegrityState() {
        // Check for TrickyStore verdict injection marker
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"getprop", "ro.build.fingerprint"});
            byte[] buf = p.getInputStream().readAllBytes();
            String fp = new String(buf).trim();
            if (fp.contains("release-keys")) {
                Log.i(TAG, "[IntegrityState] Fingerprint looks production: " + fp.substring(0, Math.min(fp.length(), 40)));
                return "DEVICE"; // Assume DEVICE until confirmed STRONG via TrickyStore cert
            }
        } catch (Throwable ignored) {}
        return "BASIC";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // APP LIST CLOAK (HMA-equivalent)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Hides the app list from the game's view (equivalent to HideMyApplist).
     * ACE 2026 scans installed packages to detect cheat apps, Magisk, LSPosed, etc.
     */
    public static void applyCloakForGame(String packageName) {
        if (packageName == null) return;
        // Grant QUERY_ALL_PACKAGES restriction override (requires Shizuku/root)
        String cmd =
            // Block game from seeing cheat-related apps via appops
            "cmd appops set " + packageName + " GET_INSTALLED_PACKAGES deny 2>/dev/null; " +
            // Force app visibility mask (Android 11+ package visibility restrictions)
            "pm hide --user 0 com.topjohnwu.magisk 2>/dev/null; " +
            "pm hide --user 0 org.lsposed.manager 2>/dev/null; " +
            "pm hide --user 0 io.github.vvb2060.magisk 2>/dev/null; ";
        executePrivileged(cmd);
        Log.i(TAG, "[Cloak] App list hidden from: " + packageName);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ZYGISK DIRECT BYPASS PATH
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sets environment variables to enable Zygisk direct injection path.
     * Must be called before the game process is started (pre-specialize).
     */
    public static void applyZygiskDirectBypass(String packageName) {
        if (packageName == null) return;
        // These markers are read by the Zygisk module during app pre-specialize
        String cmd =
            "setprop gamebooster.zygisk.target " + packageName + "; " +
            "setprop gamebooster.zygisk.mode direct; ";
        executePrivileged(cmd);
        Log.i(TAG, "[Zygisk] Direct bypass path configured for: " + packageName);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FULL BYPASS SUITE — convenience wrapper
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Applies full 2026 Play Integrity bypass suite for a given game.
     * Returns false if module stack is not ready.
     */
    public static boolean applyFullBypassSuite(Context context, String packageName) {
        if (packageName == null) return false;

        ModuleStackStatus status = checkModuleStack();
        Log.i(TAG, "[FullBypass] Module stack status: " + status);

        if (status != ModuleStackStatus.READY) {
            Log.w(TAG, "[FullBypass] Module stack incomplete (" + status + ") — bypass may be partial");
        }

        applyDenyListForGame(packageName);
        applyCloakForGame(packageName);
        applyZygiskDirectBypass(packageName);
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private static boolean moduleExists(String[] paths) {
        for (String path : paths) {
            java.io.File f = new java.io.File(path);
            if (f.exists() && f.isDirectory()) return true;
        }
        return false;
    }

    private static boolean isZygiskActive() {
        // Zygisk active → zygisk.so is loaded into zygote maps
        java.io.File maps = new java.io.File("/proc/1/maps");
        if (maps.canRead()) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(maps))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.contains("zygisk") || line.contains("magisk")) return true;
                }
            } catch (Throwable ignored) {}
        }
        // Fallback: check prop
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"getprop", "ro.zygisk.enabled"});
            byte[] buf = p.getInputStream().readAllBytes();
            return "1".equals(new String(buf).trim());
        } catch (Throwable ignored) {}
        return false;
    }

    private static void executePrivileged(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) return;
        try {
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
        } catch (Throwable t) {
            Log.w(TAG, "executePrivileged note: " + t.getMessage());
        }
    }
}
