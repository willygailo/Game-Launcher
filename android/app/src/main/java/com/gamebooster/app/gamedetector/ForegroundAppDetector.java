package com.gamebooster.app.gamedetector;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gamebooster.app.core.profile.InputProfile;
import com.gamebooster.app.core.profile.ProfileManager;
import com.gamebooster.app.core.settings.SettingsManager;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameManagerRepository;
import com.gamebooster.app.games.GamePackageRegistry;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Universal Multi-Tier Foreground Game Detector for Game Launcher PRO.
 * Compatible across Android 12, 13, 14, 15, and 16.
 *
 * Detection Tiers:
 * 1. Tier 1 (Instant Shizuku Privileged Detection): Dumpsys Window/Activity Manager (sub-second zero-lag)
 * 2. Tier 2 (UsageStatsManager Event Stream): System event stream polling
 * 3. Tier 3 (PackageManager CATEGORY_GAME & ApplicationInfo Detection): Auto-detects all installed games
 */
public class ForegroundAppDetector {

    private static final String TAG = "ForegroundAppDetector";
    private static final long FAST_POLL_INTERVAL_MS = 1200;
    private static final long RELAXED_POLL_INTERVAL_MS = 2500;

    private final Context context;
    private final SettingsManager settingsManager;
    private final ProfileManager profileManager;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String lastForegroundPackage = "";
    private boolean isMonitoring = false;
    private final Set<String> customInstalledGameCache = new HashSet<>();
    private long lastCacheRefreshTime = 0;

    public interface ForegroundListener {
        void onForegroundAppChanged(String packageName, boolean isGame);
    }

    private ForegroundListener listener;

    public ForegroundAppDetector(Context context, SettingsManager settingsManager, ProfileManager profileManager) {
        this.context = context.getApplicationContext();
        this.settingsManager = settingsManager;
        this.profileManager = profileManager;
        refreshGameCache();
    }

    public void setListener(ForegroundListener listener) {
        this.listener = listener;
    }

    public boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (appOps == null) return false;
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public void startMonitoring() {
        if (isMonitoring) return;
        isMonitoring = true;
        handler.post(monitorRunnable);
        Log.i(TAG, "ForegroundAppDetector started monitoring with multi-tier detection.");
    }

    public void stopMonitoring() {
        if (!isMonitoring) return;
        isMonitoring = false;
        handler.removeCallbacks(monitorRunnable);
        if (settingsManager != null && settingsManager.isDeviceTuned()) {
            settingsManager.restoreOriginalValues();
        }
        Log.i(TAG, "ForegroundAppDetector stopped monitoring.");
    }

    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMonitoring) return;

            String currentPackage = detectCurrentForegroundPackage();
            if (currentPackage != null && !currentPackage.isEmpty() && !currentPackage.equals(lastForegroundPackage)) {
                Log.d(TAG, "Foreground app changed: " + currentPackage);
                lastForegroundPackage = currentPackage;
                handleForegroundAppChanged(currentPackage);
            }

            long nextDelay = isKnownGamePackage(currentPackage) ? RELAXED_POLL_INTERVAL_MS : FAST_POLL_INTERVAL_MS;
            handler.postDelayed(this, nextDelay);
        }
    };

    private void handleForegroundAppChanged(String packageName) {
        InputProfile profile = profileManager != null ? profileManager.getProfileForPackage(packageName) : null;
        boolean isTargetGame = isKnownGamePackage(packageName);

        if (listener != null) {
            listener.onForegroundAppChanged(packageName, isTargetGame);
        }

        if (isTargetGame) {
            Log.i(TAG, "Target game detected in foreground (" + packageName + "). Applying input & tuning profile...");
            if (settingsManager != null && profile != null) {
                settingsManager.applyProfile(profile);
            }
        } else {
            if (settingsManager != null && settingsManager.isDeviceTuned()) {
                Log.i(TAG, "Non-game app in foreground (" + packageName + "). Restoring original settings...");
                settingsManager.restoreOriginalValues();
            }
        }
    }

    /**
     * Determines whether a package name belongs to a known game using 3 complementary methods:
     * 1. Built-in registry & known popular games
     * 2. User installed game list from GameManagerRepository
     * 3. Android PackageManager CATEGORY_GAME flag
     */
    public boolean isKnownGamePackage(String packageName) {
        if (packageName == null || packageName.isEmpty() || packageName.equals(context.getPackageName())) {
            return false;
        }

        // 1. Check cached installed game packages
        if (System.currentTimeMillis() - lastCacheRefreshTime > 30000) {
            refreshGameCache();
        }
        if (customInstalledGameCache.contains(packageName)) {
            return true;
        }

        // 2. Check GamePackageRegistry
        if (GamePackageRegistry.isKnownGame(packageName)) {
            return true;
        }

        // 3. Known package name patterns
        String pkg = packageName.toLowerCase();
        if (pkg.contains("tencent.ig") || pkg.contains("pubg") || pkg.contains("activision.callofduty")
                || pkg.contains("codm") || pkg.contains("bloodstrike") || pkg.contains("freefire")
                || pkg.contains("mobile.legends") || pkg.contains("mobilelegends") || pkg.contains("genshin")
                || pkg.contains("hkrpg") || pkg.contains("honkai") || pkg.contains("cognosphere")
                || pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor")
                || pkg.contains("roblox") || pkg.contains("wildrift") || pkg.contains("projectc")
                || pkg.contains("valorant") || pkg.contains("farlight") || pkg.contains("solarland")
                || pkg.contains("standoff2") || pkg.contains("arenabreakout") || pkg.contains("deltaforce")
                || pkg.contains("carxstreet") || pkg.contains("ea.gp.fifa") || pkg.contains("brawlstars")) {
            return true;
        }

        // 4. PackageManager Category Inspection
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                    customInstalledGameCache.add(packageName);
                    return true;
                }
            }
            if ((appInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                customInstalledGameCache.add(packageName);
                return true;
            }
        } catch (Throwable ignored) {}

        return false;
    }

    private void refreshGameCache() {
        try {
            customInstalledGameCache.clear();
            List<GameAppInfo> installed = GameManagerRepository.getInstalledGames(context);
            if (installed != null) {
                for (GameAppInfo g : installed) {
                    if (g.getPackageName() != null) {
                        customInstalledGameCache.add(g.getPackageName());
                    }
                }
            }
            lastCacheRefreshTime = System.currentTimeMillis();
        } catch (Throwable ignored) {}
    }

    /**
     * Multi-tier foreground app detection.
     */
    public String detectCurrentForegroundPackage() {
        // Tier 1: UsageStatsManager Events (Instant & Native)
        String usmPkg = getForegroundPackageViaUsageStats();
        if (usmPkg != null && !usmPkg.isEmpty()) {
            return usmPkg;
        }

        // Tier 2: ActivityManager Running Process fallback
        String amPkg = getForegroundPackageViaActivityManager();
        if (amPkg != null && !amPkg.isEmpty()) {
            return amPkg;
        }

        // Tier 3: Lightweight Shizuku Activity Command
        if (ShizukuExecutor.hasShizukuPermission()) {
            return getForegroundPackageViaShizuku();
        }

        return "";
    }

    private String getForegroundPackageViaShizuku() {
        try {
            // Lightweight top-resumed-activity query (1-line output instead of 50,000 line dumpsys)
            String out = ShizukuExecutor.executeShizukuCommand("cmd activity top-resumed-activity");
            if (out != null && !out.isEmpty()) {
                int slashIdx = out.indexOf('/');
                if (slashIdx > 0) {
                    int startIdx = out.lastIndexOf(' ', slashIdx);
                    if (startIdx >= 0) {
                        String candidate = out.substring(startIdx + 1, slashIdx).trim();
                        if (candidate.contains(".") && !candidate.startsWith("android") && !candidate.contains("launcher")) {
                            return candidate;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public String getForegroundPackageViaUsageStats() {
        try {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm == null) return "";

            long time = System.currentTimeMillis();
            UsageEvents events = usm.queryEvents(time - 8000, time);
            if (events == null) return "";

            UsageEvents.Event event = new UsageEvents.Event();
            String lastPkg = "";

            while (events.hasNextEvent()) {
                events.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND
                        || event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    lastPkg = event.getPackageName();
                }
            }
            return lastPkg;
        } catch (Throwable ignored) {
            return "";
        }
    }

    private String getForegroundPackageViaActivityManager() {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
                if (processes != null) {
                    for (ActivityManager.RunningAppProcessInfo process : processes) {
                        if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            return process.pkgList != null && process.pkgList.length > 0 ? process.pkgList[0] : process.processName;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return "";
    }
}
