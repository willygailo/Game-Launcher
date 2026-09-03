package com.gamebooster.app.games;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.LobbyInjectionEngine;
import com.gamebooster.app.gamemanager.GameManagerStatus;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.List;
import java.util.Locale;

/**
 * ForegroundGameDetector — Ultra-Reliable Real-Time Foreground Game Detection Engine.
 *
 * Employs a 5-Tier multi-layered detection cascade to identify which game (e.g. MLBB, PUBGM,
 * CODM, Free Fire, Wild Rift, Delta Force, Genshin, etc.) is active on screen:
 *  1. Shizuku / ADB SurfaceFlinger & WindowManager focus (mCurrentFocus, mFocusedApp)
 *  2. Shizuku / ADB ActivityManager topResumedActivity
 *  3. Android UsageStatsManager event streaming + recent session lookup
 *  4. ActivityManager IMPORTANCE_FOREGROUND process inspection
 *  5. GameManagerSessionEngine and LobbyInjectionEngine active session state
 */
public final class ForegroundGameDetector {

    private static final String TAG = "ForegroundGameDetector";

    private ForegroundGameDetector() {}

    /**
     * Detects the currently active foreground game package.
     * Guaranteed to prioritize recognized games (MLBB, PUBGM, CODM, etc.) over launcher/system UI.
     *
     * @param context Application context
     * @return Package name of the active game, or null if no game is running in the foreground
     */
    public static String detectActiveGame(Context context) {
        // Tier 1: Fast Shizuku/Root Window Manager dumpsys
        String pkg = detectViaShizukuWindow();
        if (isRecognizedGame(context, pkg)) {
            Log.i(TAG, "🎯 [Tier 1 Shizuku Window] Active Game Detected: " + pkg);
            return pkg;
        }

        // Tier 2: Shizuku Activity Manager topResumedActivity
        pkg = detectViaShizukuActivity();
        if (isRecognizedGame(context, pkg)) {
            Log.i(TAG, "🎯 [Tier 2 Shizuku Activity] Active Game Detected: " + pkg);
            return pkg;
        }

        // Tier 3: UsageStatsManager (Events & Recent Usage)
        pkg = detectViaUsageStats(context);
        if (isRecognizedGame(context, pkg)) {
            Log.i(TAG, "🎯 [Tier 3 UsageStats] Active Game Detected: " + pkg);
            return pkg;
        }

        // Tier 4: ActivityManager running processes
        pkg = detectViaRunningProcesses(context);
        if (isRecognizedGame(context, pkg)) {
            Log.i(TAG, "🎯 [Tier 4 RunningProcs] Active Game Detected: " + pkg);
            return pkg;
        }

        // Tier 5: Fallback to registered active session
        String sessionPkg = GameManagerStatus.getInstance().getActiveGamePackage();
        if (isRecognizedGame(context, sessionPkg)) {
            Log.i(TAG, "🎯 [Tier 5 SessionState] Using Active Session Game: " + sessionPkg);
            return sessionPkg;
        }

        String lobbyPkg = LobbyInjectionEngine.getActiveGamePackage();
        if (isRecognizedGame(context, lobbyPkg)) {
            Log.i(TAG, "🎯 [Tier 5 LobbyEngine] Using Lobby Cached Game: " + lobbyPkg);
            return lobbyPkg;
        }

        return null;
    }

    /**
     * Detects the raw foreground package currently active on screen (game or non-game).
     */
    public static String detectForegroundPackage(Context context) {
        // 1. Shizuku Window
        String pkg = detectViaShizukuWindow();
        if (isValidForegroundPackage(context, pkg)) return pkg;

        // 2. Shizuku Activity
        pkg = detectViaShizukuActivity();
        if (isValidForegroundPackage(context, pkg)) return pkg;

        // 3. UsageStats
        pkg = detectViaUsageStats(context);
        if (isValidForegroundPackage(context, pkg)) return pkg;

        // 4. Running Procs
        pkg = detectViaRunningProcesses(context);
        if (isValidForegroundPackage(context, pkg)) return pkg;

        return null;
    }

    /**
     * Checks if a package is a recognized game (via GamePackageRegistry or installed games list).
     */
    public static boolean isRecognizedGame(Context context, String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) return false;
        String clean = pkg.trim();

        if (GamePackageRegistry.isKnownGame(clean)) {
            return true;
        }

        if (context != null) {
            try {
                List<GameAppInfo> installed = GameManagerRepository.getInstalledGames(context);
                for (GameAppInfo info : installed) {
                    if (clean.equalsIgnoreCase(info.getPackageName())) {
                        return true;
                    }
                }
            } catch (Throwable ignored) {}
        }

        return false;
    }

    private static boolean isValidForegroundPackage(Context context, String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) return false;
        String clean = pkg.trim();
        if (clean.equals("com.android.systemui")
                || clean.equals("com.google.android.gms")
                || clean.equals("com.google.android.play.games")
                || clean.equals("android")) {
            return false;
        }
        if (context != null && clean.equals(context.getPackageName())) {
            return false;
        }
        return clean.contains(".");
    }

    // ─── Detection Implementations ───────────────────────────────────────────

    private static String detectViaShizukuWindow() {
        try {
            if (!ShizukuExecutor.hasShizukuPermission()) return null;
            String out = ShizukuExecutor.executeShizukuCommand("dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'");
            if (out == null || out.trim().isEmpty()) return null;

            for (String line : out.split("\n")) {
                String parsed = extractPackageFromDumpsysLine(line);
                if (parsed != null && !parsed.isEmpty()) {
                    return parsed;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static String detectViaShizukuActivity() {
        try {
            if (!ShizukuExecutor.hasShizukuPermission()) return null;
            String out = ShizukuExecutor.executeShizukuCommand("dumpsys activity activities | grep -E 'topResumedActivity|mResumedActivity'");
            if (out == null || out.trim().isEmpty()) return null;

            for (String line : out.split("\n")) {
                String parsed = extractPackageFromDumpsysLine(line);
                if (parsed != null && !parsed.isEmpty()) {
                    return parsed;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static String extractPackageFromDumpsysLine(String line) {
        if (line == null) return null;
        int slash = line.indexOf('/');
        if (slash > 0) {
            int space = line.lastIndexOf(' ', slash);
            if (space >= 0 && slash > space + 1) {
                String pkg = line.substring(space + 1, slash).trim();
                // Strip leading { or u0
                if (pkg.contains("u0 ")) {
                    pkg = pkg.substring(pkg.indexOf("u0 ") + 3).trim();
                }
                if (!pkg.isEmpty() && !pkg.contains(" ") && pkg.contains(".")) {
                    return pkg;
                }
            }
        }
        return null;
    }

    private static String detectViaUsageStats(Context context) {
        if (context == null) return null;
        try {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm == null) return null;

            long time = System.currentTimeMillis();
            // 1. Check UsageEvents stream for most recent ACTIVITY_RESUMED
            UsageEvents events = usm.queryEvents(time - 30000, time);
            if (events != null) {
                UsageEvents.Event event = new UsageEvents.Event();
                String lastPkg = null;
                while (events.hasNextEvent()) {
                    events.getNextEvent(event);
                    if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                        lastPkg = event.getPackageName();
                    }
                }
                if (lastPkg != null && !lastPkg.equals(context.getPackageName())) {
                    return lastPkg;
                }
            }

            // 2. Query usage stats within last hour and find most recently used game
            List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 3600000, time);
            if (stats != null && !stats.isEmpty()) {
                UsageStats latest = null;
                for (UsageStats stat : stats) {
                    if (stat.getPackageName() == null || stat.getPackageName().equals(context.getPackageName())) continue;
                    if (latest == null || stat.getLastTimeUsed() > latest.getLastTimeUsed()) {
                        latest = stat;
                    }
                }
                if (latest != null && latest.getLastTimeUsed() > (time - 300000)) {
                    return latest.getPackageName();
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static String detectViaRunningProcesses(Context context) {
        if (context == null) return null;
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) return null;
            List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
            if (procs == null) return null;

            for (ActivityManager.RunningAppProcessInfo info : procs) {
                if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        || info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    if (info.processName != null && isRecognizedGame(context, info.processName)) {
                        return info.processName;
                    }
                    if (info.pkgList != null) {
                        for (String p : info.pkgList) {
                            if (isRecognizedGame(context, p)) {
                                return p;
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
