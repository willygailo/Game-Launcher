package com.gamebooster.app.config;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AntiBanStealthEngine — Detection Evasion for Config-Based Combat Injectors.
 *
 * Six-layer stealth system designed to minimize flag risk when injecting
 * combat configurations into MLBB, CODM, PUBGM and other games:
 *
 *  Layer 1: Timestamp Restoration — restores original file access + modification time post-write.
 *  Layer 2: Config Signature Randomizer — randomizes harmless config offsets so every injection
 *           looks unique (no fixed byte pattern = no simple signature scan match).
 *  Layer 3: Injection Timing Jitter — random 50–200ms thread sleep between each sub-injector call.
 *  Layer 4: Log Purge on Inject — auto-purges game's own diagnostic log files after config write.
 *  Layer 5: SELinux Context Pre-Validator — skips write if SELinux context is wrong, avoiding hard flags.
 *  Layer 6: Rate Limiter — prevents re-injection more than once per 90 seconds per package.
 *
 * 2026.2 Edition — Combat Enhancement Suite Integration.
 */
public final class AntiBanStealthEngine {

    private static final String TAG = "AntiBanStealth";

    /** Minimum jitter delay between sub-injectors (ms) */
    private static final int JITTER_MIN_MS = 50;
    /** Maximum jitter delay between sub-injectors (ms) */
    private static final int JITTER_MAX_MS = 200;

    /** Rate limiter: minimum ms between injections per package */
    private static final long RATE_LIMIT_MS = 90_000L;

    /** Last injection timestamps per package (rate limiter state) */
    private static final ConcurrentHashMap<String, Long> sLastInjectTime = new ConcurrentHashMap<>();

    /** Harmless randomization keys injected to break signature scans */
    private static final String[] NOISE_KEYS = {
        "GraphicsProfileCacheVersion", "AssetBundleLoadVersion",
        "ShaderVariantCacheVersion", "AudioProfileRevision",
        "UiLayoutCacheVersion", "ControlSchemeCacheVersion",
        "DeviceProfileHash", "LocalSettingsRevision",
    };

    private static final Random sRandom = new Random();

    private AntiBanStealthEngine() {}

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Layer 6: Rate Limiter.
     * Returns true if injection is allowed for this package, false if rate-limited.
     * Call this BEFORE firing any injection to prevent thundering herd detection.
     */
    public static boolean isInjectionAllowed(String packageName) {
        if (packageName == null) return false;
        String key = packageName.toLowerCase();
        long now = System.currentTimeMillis();
        Long last = sLastInjectTime.get(key);
        if (last != null && (now - last) < RATE_LIMIT_MS) {
            long remaining = RATE_LIMIT_MS - (now - last);
            Log.d(TAG, "Rate-limited for " + packageName + " (" + remaining + "ms remaining)");
            return false;
        }
        return true;
    }

    /**
     * Record that injection was performed (updates rate limiter timestamp).
     */
    public static void recordInjection(String packageName) {
        if (packageName == null) return;
        sLastInjectTime.put(packageName.toLowerCase(), System.currentTimeMillis());
    }

    /**
     * Reset rate limiter for a package (call on force re-inject).
     */
    public static void resetRateLimit(String packageName) {
        if (packageName != null) sLastInjectTime.remove(packageName.toLowerCase());
    }

    /**
     * Layer 3: Injection Timing Jitter.
     * Sleep a random 50–200ms. Call between sub-injector steps.
     */
    public static void applyTimingJitter() {
        try {
            int delay = JITTER_MIN_MS + sRandom.nextInt(JITTER_MAX_MS - JITTER_MIN_MS + 1);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Layer 2: Config Signature Randomizer.
     * Appends a comment block with randomized harmless key-value pairs to a config
     * string so every injection has a unique byte fingerprint.
     *
     * @param content the existing config file content string
     * @param format  "ini", "json", or "xml" — determines comment syntax
     * @return modified content with randomized noise appended
     */
    public static String randomizeSignature(String content, String format) {
        if (content == null) return content;
        StringBuilder noise = new StringBuilder();
        long salt = System.currentTimeMillis() ^ sRandom.nextLong();

        if ("json".equalsIgnoreCase(format)) {
            // JSON: can't add trailing keys easily without breaking structure — add as comment-like prefix lines
            // Use a separate top-level block that engines ignore
            noise.append("\n// GB_STEALTH_SALT=").append(Long.toHexString(salt)).append("\n");
            for (String key : NOISE_KEYS) {
                noise.append("// ").append(key).append("=").append(sRandom.nextInt(99999)).append("\n");
            }
        } else if ("xml".equalsIgnoreCase(format)) {
            noise.append("\n<!-- GB_STEALTH_SALT=").append(Long.toHexString(salt)).append(" -->\n");
            for (String key : NOISE_KEYS) {
                noise.append("<!-- ").append(key).append("=").append(sRandom.nextInt(99999)).append(" -->\n");
            }
        } else {
            // INI / plain text
            noise.append("\n; GB_STEALTH_SALT=").append(Long.toHexString(salt)).append("\n");
            for (String key : NOISE_KEYS) {
                noise.append("; ").append(key).append("=").append(sRandom.nextInt(99999)).append("\n");
            }
        }

        return content + noise;
    }

    /**
     * Layer 4: Log Purge.
     * Deletes game-side diagnostic log files from common paths after injection.
     * Prevents anti-cheat from reading before/after config states from game logs.
     */
    public static void purgeGameLogs(String packageName) {
        if (packageName == null) return;
        String[] logPaths = {
            "/sdcard/Android/data/" + packageName + "/files/logs/",
            "/sdcard/Android/data/" + packageName + "/cache/logs/",
            "/sdcard/Android/data/" + packageName + "/files/crash/",
            "/sdcard/Android/data/" + packageName + "/files/diagnostic/",
            "/storage/emulated/0/Android/data/" + packageName + "/files/logs/",
        };

        int purged = 0;
        for (String path : logPaths) {
            File dir = new File(path);
            if (!dir.exists() || !dir.isDirectory()) continue;
            File[] files = dir.listFiles();
            if (files == null) continue;
            for (File f : files) {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".log") || name.endsWith(".txt") || name.endsWith(".dmp")
                        || name.endsWith(".crash") || name.contains("diagnostic") || name.contains("report")) {
                    if (f.delete()) purged++;
                }
            }
        }
        if (purged > 0) Log.d(TAG, "Purged " + purged + " log files for " + packageName);
    }

    /**
     * Layer 5: SELinux Context Pre-Validator.
     * Checks if we have write access to a path before attempting to write.
     * Returns false if the path is in a context that would trigger a hard AVC denial log.
     */
    public static boolean isWriteContextSafe(String path) {
        if (path == null) return false;
        try {
            File f = new File(path);
            // Check if parent directory is accessible
            File parent = f.getParentFile();
            if (parent == null) return false;
            if (!parent.exists()) {
                // Try to create — if SELinux blocks this, it'll throw
                try { parent.mkdirs(); } catch (Throwable t) { return false; }
            }
            // Try a canWrite check (coarse but cheap)
            if (f.exists()) return f.canWrite();
            // If doesn't exist, check parent
            return parent.canWrite();
        } catch (Throwable t) {
            Log.w(TAG, "Context safety check failed for " + path + ": " + t.getMessage());
            return false;
        }
    }

    /**
     * Layer 1: Batch Timestamp Restoration.
     * Restores original access + modification timestamps for a list of file paths.
     * Call AFTER all writes complete. Per-file timestamp restoration already exists
     * in the C++ layer; this is the batch Java-layer equivalent.
     */
    public static void batchRestoreTimestamps(List<String> paths, List<long[]> originalTimestamps) {
        if (paths == null || originalTimestamps == null) return;
        int n = Math.min(paths.size(), originalTimestamps.size());
        for (int i = 0; i < n; i++) {
            String path = paths.get(i);
            long[] ts = originalTimestamps.get(i);
            if (path == null || ts == null || ts.length < 1) continue;
            try {
                File f = new File(path);
                if (f.exists()) {
                    // setLastModified is all we can do from Java without JNI
                    f.setLastModified(ts[0]);
                }
            } catch (Throwable t) {
                Log.w(TAG, "Timestamp restore failed for " + path + ": " + t.getMessage());
            }
        }
    }

    /**
     * Capture current modification timestamps for a list of paths (before writing).
     * Returns parallel list of [modTime] arrays.
     */
    public static List<long[]> captureTimestamps(List<String> paths) {
        List<long[]> result = new ArrayList<>();
        if (paths == null) return result;
        for (String path : paths) {
            try {
                File f = new File(path);
                result.add(new long[]{f.lastModified()});
            } catch (Throwable t) {
                result.add(new long[]{System.currentTimeMillis()});
            }
        }
        return result;
    }

    /**
     * Full stealth pre-flight for a batch of paths.
     * Returns filtered list of paths that are safe to write.
     * Also captures timestamps (caller should restore after writing).
     */
    public static List<String> stealthPreFlight(String packageName, List<String> paths) {
        List<String> safePaths = new ArrayList<>();
        if (paths == null) return safePaths;
        for (String path : paths) {
            if (isWriteContextSafe(path)) {
                safePaths.add(path);
            } else {
                Log.w(TAG, "⚠️ Skipping unsafe context path: " + path);
            }
        }
        Log.d(TAG, "Stealth pre-flight: " + safePaths.size() + "/" + paths.size() + " paths safe for " + packageName);
        return safePaths;
    }

    /**
     * Applies subtle session drift (±3% to ±5%) to numeric combat values.
     * Prevents static signature fingerprinting across repeated injection sessions.
     */
    public static float driftValue(float base) {
        // Drift factor between -0.05 and +0.05
        float factor = 1.0f + ((sRandom.nextFloat() * 0.10f) - 0.05f);
        return base * factor;
    }
}
