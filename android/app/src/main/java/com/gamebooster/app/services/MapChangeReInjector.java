package com.gamebooster.app.services;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.GameAutoInjectDispatcher;
import com.gamebooster.app.games.GamePackageRegistry;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MapChangeReInjector — Automatic Combat Re-Injection on Map/Match Change.
 *
 * THE FIX for the #1 pain point: game config files reset every new match/map.
 * Without this, combat features go silent after the first map.
 *
 * Strategy:
 *  1. Poll active foreground package every 30 seconds.
 *  2. Track last-seen config file modification timestamps per game.
 *  3. On timestamp change (new map/match detected) → reset session dedup cache → re-dispatch full combat suite.
 *  4. Debounce: minimum 60s between re-injections per package.
 *  5. Supports MLBB, CODM, PUBGM and all registered games.
 *
 * 2026.2 Edition — Combat Enhancement Suite Integration.
 */
public final class MapChangeReInjector {

    private static final String TAG = "MapChangeReInjector";

    /** Polling interval in seconds — checks for map change */
    private static final long POLL_INTERVAL_SECONDS = 30L;

    /** Minimum time between re-injections per package (debounce) */
    private static final long MIN_REINJECT_INTERVAL_MS = 60_000L;

    /** Paths watched per game package: last known modification time */
    private static final ConcurrentHashMap<String, Long> sLastConfigModTime = new ConcurrentHashMap<>();

    /** Last re-injection timestamp per package */
    private static final ConcurrentHashMap<String, Long> sLastReInjectTime = new ConcurrentHashMap<>();

    private static ScheduledExecutorService sScheduler;
    private static ScheduledFuture<?> sMonitorFuture;
    private static volatile boolean sRunning = false;
    private static volatile Context sAppContext;
    private static volatile String sActivePackage;

    private MapChangeReInjector() {}

    // ─── Public API ───────────────────────────────────────────────────────────

    /** Start monitoring. Call when a game is detected as launched. */
    public static synchronized void startMonitoring(Context context, String gamePackage) {
        if (context == null || gamePackage == null) return;
        sAppContext = context.getApplicationContext();
        sActivePackage = gamePackage;

        if (sRunning) {
            // Update active package without restarting the scheduler
            Log.d(TAG, "MapChangeReInjector already running — updating active package to " + gamePackage);
            return;
        }

        sRunning = true;
        if (sScheduler == null || sScheduler.isShutdown()) {
            sScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "MapChangeReInjector");
                t.setDaemon(true);
                t.setPriority(Thread.MIN_PRIORITY);
                return t;
            });
        }

        sMonitorFuture = sScheduler.scheduleWithFixedDelay(
                MapChangeReInjector::checkAndReInject,
                POLL_INTERVAL_SECONDS,
                POLL_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );
        Log.i(TAG, "🔄 MapChangeReInjector started for " + gamePackage + " (poll every " + POLL_INTERVAL_SECONDS + "s)");
    }

    /** Stop monitoring. Call when game exits. */
    public static synchronized void stopMonitoring() {
        sRunning = false;
        sActivePackage = null;
        if (sMonitorFuture != null) {
            sMonitorFuture.cancel(false);
            sMonitorFuture = null;
        }
        Log.i(TAG, "🛑 MapChangeReInjector stopped");
    }

    /** Force immediate re-injection for the currently active package. */
    public static void forceReInjectNow(Context context, String gamePackage) {
        if (gamePackage == null) return;
        Context ctx = context != null ? context.getApplicationContext() : sAppContext;
        if (ctx == null) return;

        Log.i(TAG, "🔴 FORCE RE-INJECT NOW for " + gamePackage);
        GameAutoInjectDispatcher.resetPackageInjectionState(gamePackage);
        sLastReInjectTime.put(gamePackage.toLowerCase(), System.currentTimeMillis());

        // Fire on background thread
        new Thread(() -> {
            try {
                GameAutoInjectDispatcher.dispatchForPackage(ctx, gamePackage, true);
                Log.i(TAG, "✅ Force re-inject complete for " + gamePackage);
            } catch (Throwable t) {
                Log.w(TAG, "Force re-inject error: " + t.getMessage());
            }
        }, "ForceReInject-" + gamePackage).start();
    }

    /** Check if re-injector is currently running. */
    public static boolean isRunning() {
        return sRunning;
    }

    // ─── Internal Monitor Loop ────────────────────────────────────────────────

    private static void checkAndReInject() {
        try {
            String pkg = sActivePackage;
            if (pkg == null || !sRunning) return;

            String pkgLower = pkg.toLowerCase();

            // ── Detect map change via config file modification timestamp ──────
            boolean mapChanged = checkConfigTimestampChanged(pkg);

            if (!mapChanged) {
                Log.v(TAG, "No map change detected for " + pkg);
                return;
            }

            // ── Debounce: don't re-inject more than once per 60 seconds ──────
            long now = System.currentTimeMillis();
            Long lastInject = sLastReInjectTime.get(pkgLower);
            if (lastInject != null && (now - lastInject) < MIN_REINJECT_INTERVAL_MS) {
                Log.d(TAG, "Re-inject debounced for " + pkg + " (" + (now - lastInject) + "ms since last inject)");
                return;
            }

            Log.i(TAG, "🗺️ MAP CHANGE DETECTED for " + pkg + " — re-injecting combat suite...");

            // ── Reset session dedup cache so dispatcher re-fires ──────────────
            GameAutoInjectDispatcher.resetPackageInjectionState(pkg);
            sLastReInjectTime.put(pkgLower, now);

            Context ctx = sAppContext;
            if (ctx == null) return;

            // ── Re-dispatch full combat suite (force=true bypasses dedup) ─────
            GameAutoInjectDispatcher.dispatchForPackage(ctx, pkg, true);

            Log.i(TAG, "✅ Re-injection complete after map change for " + pkg);

        } catch (Throwable t) {
            Log.w(TAG, "MapChangeReInjector error: " + t.getMessage());
        }
    }

    /**
     * Checks if the game's config files have been modified since last check.
     * Uses modification timestamp comparison — works without root/process hooks.
     */
    private static boolean checkConfigTimestampChanged(String packageName) {
        try {
            // Standard Android game data paths
            String[] basePaths = {
                "/sdcard/Android/data/" + packageName + "/files/",
                "/sdcard/Android/data/" + packageName + "/cache/",
                "/data/data/" + packageName + "/shared_prefs/",
                "/storage/emulated/0/Android/data/" + packageName + "/files/",
            };

            // File name patterns indicating match/lobby state changes
            String[] stateFileNames = {
                "GameSettings.ini", "UserCustom.ini", "GameProgress.json",
                "PlayerPrefs", "SharedSettings.json", "GameSessionData.json",
                "GameConfig.xml", "SettingsCache.bin", "SessionState.bin",
                "active.json", "lobby.json", "match.json", "room.json",
            };

            long latestModTime = 0;
            for (String base : basePaths) {
                File dir = new File(base);
                if (!dir.exists() || !dir.isDirectory()) continue;
                File[] files = dir.listFiles();
                if (files == null) continue;
                for (File f : files) {
                    if (f.isFile()) {
                        String name = f.getName();
                        for (String pattern : stateFileNames) {
                            if (name.equalsIgnoreCase(pattern) || name.startsWith(pattern.replace(".", ""))) {
                                long mod = f.lastModified();
                                if (mod > latestModTime) latestModTime = mod;
                            }
                        }
                    }
                }
                // Also check parent dir modification itself as a heuristic
                long dirMod = dir.lastModified();
                if (dirMod > latestModTime) latestModTime = dirMod;
            }

            if (latestModTime == 0) return false;

            String pkgLower = packageName.toLowerCase();
            Long lastKnown = sLastConfigModTime.get(pkgLower);
            sLastConfigModTime.put(pkgLower, latestModTime);

            if (lastKnown == null) {
                // First check — don't re-inject, just record baseline
                return false;
            }

            return latestModTime > lastKnown;

        } catch (Throwable t) {
            Log.w(TAG, "Timestamp check error for " + packageName + ": " + t.getMessage());
            return false;
        }
    }
}
