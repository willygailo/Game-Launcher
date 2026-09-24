package com.gamebooster.app.engine;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * DroneViewInjector — MLBB Mini-Patch Stealth Delivery Engine.
 *
 * WHY THE OLD METHOD BROKE ON PATCH 42
 * ─────────────────────────────────────
 * MLBB patch 42 (build 1232.1) tightened __fix_rescheck validation:
 *  1. It now hashes ALL files listed in _load_res.bytes against a server-side
 *     manifest — any mismatch triggers "mod detected -> patch rollback".
 *  2. The ZC folder UID rotated. Old patches targeting a stale ZC ID are
 *     silently ignored (files land in a dead folder, never loaded).
 *  3. __active / __ready sentinel files must be written AFTER all payload
 *     files are in place, or the engine skips the patch folder entirely.
 *
 * 3-LAYER STEALTH FIX
 * ────────────────────
 *  Layer 1  Correct ZC path:   detect the live ZC_* folder from MLBB's own
 *           mini_patch index so we always land in the right place.
 *  Layer 2  Checksum ghost:    rewrite __fix_rescheck protection_level from
 *           'secure' -> 'standard' so the server manifest validator skips
 *           deep hash verification on our swapped binary files.
 *  Layer 3  Sentinel ordering: __ready -> payload files -> __active.
 *           MLBB's loader polls __active as the "commit" flag; we only set
 *           it after every binary is confirmed in place.
 *
 * ZOOM VARIANTS (from zip):
 *   X1_5  "Drone Vertical x1,5"  — 1.5x (safest, least detectable)
 *   X2    "Drone Vertical x2"    — 2x classic drone (most popular)
 *   X3    "Drone Vertical x3"    — 3x wide recon
 *   X4    "Drone Vertical x4"    — 4x strategic overview
 *   X5    "Drone Vertical x5"    — 5x max pull
 *
 * Usage:
 *   DroneViewInjector.inject(context, zipPath, ZoomVariant.X2, callback);
 * Revert:
 *   DroneViewInjector.revert(context, callback);
 */
public final class DroneViewInjector {

    private static final String TAG = "DroneViewInjector";

    public static final String MLBB_PKG = "com.mobile.legends";
    private static final String MLBB_INTERNAL = "/data/data/" + MLBB_PKG;
    private static final String MLBB_EXTERNAL  = "/sdcard/Android/data/" + MLBB_PKG;
    private static final String MINI_PATCH_SUB = "/files/mini_patch";

    /** Update when MLBB ships a new major patch that changes this path. */
    public static final String PATCH_VERSION = "1232.1";

    // ── Zoom variants ────────────────────────────────────────────────────────

    public enum ZoomVariant {
        X1_5("Drone Vertical x1,5"),
        X2  ("Drone Vertical x2"),
        X3  ("Drone Vertical x3"),
        X4  ("Drone Vertical x4"),
        X5  ("Drone Vertical x5");

        public final String folderName;

        ZoomVariant(String fn) { this.folderName = fn; }

        public String label() {
            return folderName.replace("Drone Vertical x", "") + "x Drone View";
        }
    }

    // ── Callback ─────────────────────────────────────────────────────────────

    public interface InjectCallback {
        /** Always called on the main thread. */
        void onResult(boolean success, String message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Injects drone-view camera mod into MLBB's mini_patch, surviving patch-42
     * detection via the 3-layer stealth plan described in the class header.
     *
     * @param context   App context
     * @param zipPath   Absolute path to "DRONE VIEW ML 1,5X - 5X.zip"
     * @param zoom      Zoom level to inject
     * @param callback  Result delivered on the main thread
     */
    public static void inject(Context context, String zipPath,
                              ZoomVariant zoom, InjectCallback callback) {
        if (context == null || zipPath == null || zoom == null) {
            fire(callback, false, "Null argument: context/zipPath/zoom required.");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                fire(callback, true, doInject(context, zipPath, zoom));
            } catch (Throwable t) {
                Log.e(TAG, "inject() crashed", t);
                fire(callback, false, "Injection failed: " + t.getMessage());
            }
        });
    }

    /**
     * Reverts drone view by removing the injected patch files so MLBB reloads
     * its stock camera on the next launch.
     */
    public static void revert(Context context, InjectCallback callback) {
        if (context == null) { fire(callback, false, "Null context."); return; }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                fire(callback, true, doRevert());
            } catch (Throwable t) {
                Log.e(TAG, "revert() crashed", t);
                fire(callback, false, "Revert failed: " + t.getMessage());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INJECTION CORE
    // ─────────────────────────────────────────────────────────────────────────

    private static String doInject(Context context, String zipPath, ZoomVariant zoom)
            throws Exception {

        Log.i(TAG, "[DroneView] Injecting: " + zoom.folderName);

        // Layer 1 — find the correct live ZC folder
        String miniPatch  = resolveMiniPatchBase();
        String zcDir      = resolveZcDirectory(miniPatch);
        String bundleBase = zcDir + "/2"; // MLBB hot-patch always lives in slot "2"
        Log.d(TAG, "[DroneView] Bundle target: " + bundleBase);

        // Extract selected zoom variant from zip into app's private cache
        File stageDir = new File(context.getCacheDir(), "dv_stage_" + zoom.name());
        deleteRecursive(stageDir);
        stageDir.mkdirs();

        String zipPrefix = "DRONE VIEW ML 1,5X - 5X/" + zoom.folderName
                + "/" + MLBB_PKG + MINI_PATCH_SUB + "/" + PATCH_VERSION + "/";
        extractZipPrefix(zipPath, zipPrefix, stageDir);

        // Locate ZC_* bundle inside the stage
        File zcStage = findZcFolder(stageDir);
        if (zcStage == null) throw new IOException("ZC_* folder not found in stage — corrupt zip?");
        File bundleStage = new File(zcStage, "2");
        if (!bundleStage.isDirectory()) throw new IOException("Bundle slot '2' missing from stage.");

        // Layer 3 — write __ready sentinel FIRST
        writeSentinel(bundleBase + "/__ready", "");

        // Push all payload files (sentinels handled separately for ordering)
        int pushed = pushDir(bundleStage, bundleBase, true);
        Log.i(TAG, "[DroneView] Pushed " + pushed + " files");

        // Layer 2 — ghost the rescheck so server manifest skips hash verification
        patchRescheck(bundleBase + "/__fix_rescheck");

        // Layer 3 — write __active LAST (commit flag)
        writeSentinel(bundleBase + "/__active", "");

        deleteRecursive(stageDir);

        return "Drone View " + zoom.label() + " active. Restart MLBB to apply.";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REVERT
    // ─────────────────────────────────────────────────────────────────────────

    private static String doRevert() {
        String bundleBase = resolveZcDirectory(resolveMiniPatchBase()) + "/2";

        // Kill commit flag first so MLBB stops reading the patch mid-flight
        ShizukuFileManager.deleteFile(bundleBase + "/__active");

        // BattleSystemConfig.bytes is the ONLY zoom-variant file (unique per zoom level).
        // Define.bytes is identical across all 5 variants — safe to leave, no zoom effect.
        ShizukuFileManager.deleteFile(bundleBase + "/Document/android/BattleSystemConfig.bytes");

        // Invalidate the whole bundle by removing __ready
        ShizukuFileManager.deleteFile(bundleBase + "/__ready");

        return "Drone View removed. Stock camera restored on next MLBB launch.";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATH RESOLUTION
    // ─────────────────────────────────────────────────────────────────────────

    /** Prefers internal /data/data (faster I/O, root/Shizuku); falls back to external. */
    private static String resolveMiniPatchBase() {
        String internal = MLBB_INTERNAL + MINI_PATCH_SUB;
        return ShizukuFileManager.isDirectory(internal) ? internal : MLBB_EXTERNAL + MINI_PATCH_SUB;
    }

    /**
     * Finds or creates the ZC_* versioned bundle directory.
     * Layer 1: scans the live mini_patch tree so we always use the active ZC ID.
     */
    private static String resolveZcDirectory(String miniPatchBase) {
        String versionDir = miniPatchBase + "/" + PATCH_VERSION;
        ShizukuFileManager.makeDirectory(versionDir);

        for (String entry : ShizukuFileManager.listDirectory(versionDir)) {
            if (entry.startsWith("ZC_")) {
                Log.d(TAG, "[DroneView] Live ZC folder: " + entry);
                return versionDir + "/" + entry;
            }
        }
        // Fallback to ZC ID from the zip — MLBB will accept it on next sync
        String fallback = versionDir + "/ZC_7108472971";
        ShizukuFileManager.makeDirectory(fallback + "/2");
        Log.d(TAG, "[DroneView] Created ZC fallback: " + fallback);
        return fallback;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ZIP EXTRACTION
    // ─────────────────────────────────────────────────────────────────────────

    private static void extractZipPrefix(String zipPath, String prefix, File destDir)
            throws IOException {
        try (ZipFile zf = new ZipFile(zipPath)) {
            Enumeration<? extends ZipEntry> it = zf.entries();
            while (it.hasMoreElements()) {
                ZipEntry e = it.nextElement();
                String name = e.getName();
                if (!name.startsWith(prefix)) continue;
                String rel = name.substring(prefix.length());
                if (rel.isEmpty()) continue;
                File out = new File(destDir, rel);
                if (e.isDirectory()) { out.mkdirs(); continue; }
                out.getParentFile().mkdirs();
                try (InputStream is = zf.getInputStream(e);
                     FileOutputStream fos = new FileOutputStream(out)) {
                    byte[] buf = new byte[8192]; int n;
                    while ((n = is.read(buf)) != -1) fos.write(buf, 0, n);
                }
            }
        }
    }

    private static File findZcFolder(File base) {
        File[] kids = base.listFiles();
        if (kids == null) return null;
        for (File f : kids) if (f.isDirectory() && f.getName().startsWith("ZC_")) return f;
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILE PUSH ENGINE
    // ─────────────────────────────────────────────────────────────────────────

    private static int pushDir(File src, String destBase, boolean skipSentinels)
            throws IOException {
        int count = 0;
        File[] files = src.listFiles();
        if (files == null) return 0;
        for (File f : files) {
            if (f.isDirectory()) {
                count += pushDir(f, destBase + "/" + f.getName(), skipSentinels);
                continue;
            }
            String name = f.getName();
            if (skipSentinels && isSentinel(name)) continue;

            byte[] data = java.nio.file.Files.readAllBytes(f.toPath());
            String dest = destBase + "/" + name;
            ShizukuFileManager.FileOpResult res =
                    ShizukuFileManager.uploadBytes(dest, data, "666");

            if (res.success) {
                count++;
                Log.v(TAG, "  OK  " + dest + " (" + data.length + "b)");
            } else {
                Log.w(TAG, "  ERR " + dest + " -> " + res.message);
                if (isCritical(name)) {
                    throw new IOException("Critical push failed: " + name + " -> " + res.message);
                }
            }
        }
        return count;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LAYER 2 — CHECKSUM GHOST
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Rewrites __fix_rescheck so MLBB's server manifest validator classifies
     * this bundle as a normal system patch instead of a user modification:
     *   protection_level: 'secure'  -> 'standard'  (disables hash deep-scan)
     *   patch class:      HIDDEN    -> SYSTEM       (trust escalation)
     */
    private static void patchRescheck(String path) {
        try {
            String orig = ShizukuFileManager.readFile(path);
            if (orig == null || orig.isEmpty()) {
                // File might not exist yet if we created the ZC folder ourselves — write a clean stub
                String stub = "-- Flags: FIXED, VERIFIED, SYSTEM\n"
                        + "-- Timestamp: CURRENT_TIMESTAMP\n\n"
                        + "INSERT OR REPLACE INTO resources (name, type, path_id, status, protection_level, created_at)\n"
                        + "VALUES ('load_res', 4, 123003241734, 'active', 'standard', CURRENT_TIMESTAMP);\n";
                ShizukuFileManager.writeFile(path, stub, "666");
                Log.i(TAG, "[DroneView] patchRescheck: wrote fresh stub");
                return;
            }
            String patched = orig
                    .replace("'secure'", "'standard'")
                    .replace("protection_level, 'secure'", "protection_level, 'standard'")
                    .replace("HIDDEN", "SYSTEM");
            ShizukuFileManager.FileOpResult res =
                    ShizukuFileManager.writeFile(path, patched, "666");
            Log.i(TAG, "[DroneView] patchRescheck: " + (res.success ? "OK" : "FAILED " + res.message));
        } catch (Throwable t) {
            Log.w(TAG, "[DroneView] patchRescheck exception", t);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private static void writeSentinel(String path, String content) {
        ShizukuFileManager.makeDirectory(path.substring(0, path.lastIndexOf('/')));
        ShizukuFileManager.FileOpResult r = ShizukuFileManager.writeFile(path, content, "666");
        Log.d(TAG, "[DroneView] Sentinel " + path + ": " + (r.success ? "OK" : r.message));
    }

    private static boolean isSentinel(String name) {
        return name.equals("__active") || name.equals("__ready") || name.equals("__fix_rescheck");
    }

    private static boolean isCritical(String name) {
        // .bytes config files are critical; art/audio misses are cosmetic only
        return name.endsWith(".bytes") && !name.contains("AudioBank");
    }

    private static void deleteRecursive(File f) {
        if (f == null || !f.exists()) return;
        if (f.isDirectory()) { File[] k = f.listFiles(); if (k != null) for (File c : k) deleteRecursive(c); }
        f.delete();
    }

    private static void fire(InjectCallback cb, boolean ok, String msg) {
        if (cb == null) return;
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> cb.onResult(ok, msg));
    }

    private DroneViewInjector() {}
}
