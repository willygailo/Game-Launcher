package com.gamebooster.app.gamedetector;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gamebooster.app.core.profile.InputProfile;
import com.gamebooster.app.core.profile.ProfileManager;
import com.gamebooster.app.core.settings.SettingsManager;

/**
 * Monitors foreground application changes using UsageStatsManager.
 * Applies tuning profiles when target games enter foreground and restores system defaults when exiting.
 */
public class ForegroundAppDetector {

    private static final String TAG = "ForegroundAppDetector";
    private static final long MONITOR_INTERVAL_MS = 2000;

    private final Context context;
    private final SettingsManager settingsManager;
    private final ProfileManager profileManager;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private String lastForegroundPackage = "";
    private boolean isMonitoring = false;

    public interface ForegroundListener {
        void onForegroundAppChanged(String packageName, boolean isGame);
    }

    private ForegroundListener listener;

    public ForegroundAppDetector(Context context, SettingsManager settingsManager, ProfileManager profileManager) {
        this.context = context.getApplicationContext();
        this.settingsManager = settingsManager;
        this.profileManager = profileManager;
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
        if (!hasUsageStatsPermission()) {
            Log.w(TAG, "UsageStats permission not granted. Cannot monitor foreground apps.");
            return;
        }
        isMonitoring = true;
        handler.post(monitorRunnable);
        Log.i(TAG, "ForegroundAppDetector started monitoring.");
    }

    public void stopMonitoring() {
        if (!isMonitoring) return;
        isMonitoring = false;
        handler.removeCallbacks(monitorRunnable);
        if (settingsManager.isDeviceTuned()) {
            settingsManager.restoreOriginalValues();
        }
        Log.i(TAG, "ForegroundAppDetector stopped monitoring.");
    }

    private final Runnable monitorRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMonitoring) return;

            String currentPackage = getForegroundPackageName();
            if (!currentPackage.isEmpty() && !currentPackage.equals(lastForegroundPackage)) {
                Log.d(TAG, "Foreground app changed: " + currentPackage);
                lastForegroundPackage = currentPackage;
                handleForegroundAppChanged(currentPackage);
            }

            handler.postDelayed(this, MONITOR_INTERVAL_MS);
        }
    };

    private void handleForegroundAppChanged(String packageName) {
        InputProfile profile = profileManager.getProfileForPackage(packageName);
        boolean isTargetGame = isKnownGamePackage(packageName);

        if (listener != null) {
            listener.onForegroundAppChanged(packageName, isTargetGame);
        }

        if (isTargetGame) {
            Log.i(TAG, "Target game detected in foreground (" + packageName + "). Applying input profile...");
            settingsManager.applyProfile(profile);
        } else {
            if (settingsManager.isDeviceTuned()) {
                Log.i(TAG, "Non-game app in foreground (" + packageName + "). Restoring original settings...");
                settingsManager.restoreOriginalValues();
            }
        }
    }

    private boolean isKnownGamePackage(String packageName) {
        if (packageName == null) return false;
        String pkg = packageName.toLowerCase();
        return pkg.contains("tencent.ig")
                || pkg.contains("pubg")
                || pkg.contains("activision.callofduty")
                || pkg.contains("codm")
                || pkg.contains("warzone")
                || pkg.contains("bloodstrike")
                || pkg.contains("newspike")
                || pkg.contains("standoff2")
                || pkg.contains("carxstreet")
                || pkg.contains("uamo")
                || pkg.contains("deltaforce")
                || pkg.contains("supercell")
                || pkg.contains("brawlstars")
                || pkg.contains("clashofclans")
                || pkg.contains("clashroyale")
                || pkg.contains("freefire")
                || pkg.contains("mobile.legends")
                || pkg.contains("mobilelegends")
                || pkg.contains("genshin")
                || pkg.contains("hkrpg")
                || pkg.contains("honkai")
                || pkg.contains("cognosphere")
                || pkg.contains("mihoyo")
                || pkg.contains("hoyoverse")
                || pkg.contains("wutheringwaves")
                || pkg.contains("sgame")
                || pkg.contains("levelinfinite")
                || pkg.contains("arenaofvalor")
                || pkg.contains("roblox")
                || pkg.contains("wildrift")
                || pkg.contains("projectc")
                || pkg.contains("valorant")
                || pkg.contains("farlight")
                || pkg.contains("solarland");
    }

    public String getForegroundPackageName() {
        // Tier 1: Instant sub-millisecond detection via Shizuku
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            try {
                String dump = com.gamebooster.app.engine.CommandExecutor.executeSystemCommand("dumpsys window | grep -E 'mCurrentFocus|mFocusedApp'");
                if (dump != null && !dump.isEmpty()) {
                    for (String line : dump.split("\n")) {
                        int slash = line.indexOf('/');
                        if (slash > 0) {
                            int space = line.lastIndexOf(' ', slash);
                            if (space >= 0 && slash > space) {
                                String pkg = line.substring(space + 1, slash).trim();
                                if (!pkg.isEmpty() && !pkg.contains(" ") && pkg.contains(".")) {
                                    return pkg;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        // Tier 2: UsageStatsManager query
        try {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm != null) {
                long time = System.currentTimeMillis();
                UsageEvents events = usm.queryEvents(time - 10000, time);
                if (events != null) {
                    UsageEvents.Event event = new UsageEvents.Event();
                    String lastPkg = "";
                    while (events.hasNextEvent()) {
                        events.getNextEvent(event);
                        if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                            lastPkg = event.getPackageName();
                        }
                    }
                    if (!lastPkg.isEmpty()) return lastPkg;
                }
            }
        } catch (Exception ignored) {}

        return "";
    }
}
