package com.gamebooster.app.feature.games.detector;

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
import com.gamebooster.app.feature.games.GamePackageRegistry;

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
        Log.i(TAG, "ForegroundAppDetector stopped monitoring. Active lock settings preserved.");
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

        if (isTargetGame && profile != null) {
            Log.i(TAG, "Target game in foreground: " + packageName + ". Applying tuning profile.");
            settingsManager.applyProfile(profile);
        } else if (!isTargetGame) {
            Log.i(TAG, "Game exited foreground. Active user tuning locks remain intact.");
        }
    }

    private boolean isKnownGamePackage(String packageName) {
        return GamePackageRegistry.isKnownGame(packageName);
    }

    private String getForegroundPackageName() {
        String foregroundPackage = "";
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return foregroundPackage;

        long endTime = System.currentTimeMillis();
        long startTime = endTime - 5000;

        UsageEvents events = usm.queryEvents(startTime, endTime);
        if (events == null) return foregroundPackage;

        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                foregroundPackage = event.getPackageName();
            }
        }
        return foregroundPackage;
    }
}
