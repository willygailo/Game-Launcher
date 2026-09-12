package com.gamebooster.app.gamespace;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GameSpaceAnalyticsManager — Tracks Playtime, Battery Efficiency & Gaming Analytics.
 *
 * Inspired by OEM Game Space data labs:
 * Captures per-session duration, battery drain rate (%/hour), and game usage history.
 */
public final class GameSpaceAnalyticsManager {

    private static final String TAG = "GameAnalyticsMgr";
    private static final String PREF_NAME = "game_analytics_stats_prefs";
    private static final String KEY_STATS_JSON = "key_stats_json";

    public static class GameStatRecord {
        public String packageName;
        public String gameLabel;
        public long totalPlaytimeMs;
        public int totalSessions;
        public float totalBatteryPercentDrained;
        public long lastPlayedTimestamp;

        public GameStatRecord(String packageName, String gameLabel) {
            this.packageName = packageName;
            this.gameLabel = gameLabel;
            this.totalPlaytimeMs = 0;
            this.totalSessions = 0;
            this.totalBatteryPercentDrained = 0f;
            this.lastPlayedTimestamp = System.currentTimeMillis();
        }

        public String getFormattedPlaytime() {
            long minutes = (totalPlaytimeMs / 1000) / 60;
            long hours = minutes / 60;
            long remMinutes = minutes % 60;
            if (hours > 0) {
                return hours + "h " + remMinutes + "m";
            }
            return Math.max(1, remMinutes) + "m";
        }

        public float getDrainRatePerHour() {
            if (totalPlaytimeMs <= 0) return 0f;
            float hours = (float) totalPlaytimeMs / (1000f * 60f * 60f);
            if (hours <= 0.01f) return 0f;
            return totalBatteryPercentDrained / hours;
        }
    }

    private static final Map<String, Long> sSessionStartTimes = new HashMap<>();
    private static final Map<String, Integer> sSessionStartBatteries = new HashMap<>();

    private GameSpaceAnalyticsManager() {}

    public static void onSessionStart(Context context, String packageName, String gameLabel) {
        if (packageName == null) return;
        sSessionStartTimes.put(packageName, System.currentTimeMillis());
        sSessionStartBatteries.put(packageName, getBatteryLevel(context));
        Log.d(TAG, "Analytics session started for " + packageName);
    }

    public static void onSessionEnd(Context context, String packageName) {
        if (packageName == null || !sSessionStartTimes.containsKey(packageName)) return;

        long startTime = sSessionStartTimes.remove(packageName);
        long duration = System.currentTimeMillis() - startTime;
        if (duration < 5000) return; // Ignore accidental launches < 5 sec

        int startBattery = sSessionStartBatteries.containsKey(packageName)
                ? sSessionStartBatteries.remove(packageName)
                : getBatteryLevel(context);
        int endBattery = getBatteryLevel(context);
        int batteryDelta = Math.max(0, startBattery - endBattery);

        updateRecord(context, packageName, duration, batteryDelta);
    }

    private static synchronized void updateRecord(Context context, String packageName, long duration, int batteryDelta) {
        if (context == null) return;
        List<GameStatRecord> records = getAllStats(context);
        GameStatRecord target = null;
        for (GameStatRecord r : records) {
            if (r.packageName.equals(packageName)) {
                target = r;
                break;
            }
        }

        if (target == null) {
            target = new GameStatRecord(packageName, packageName);
            records.add(target);
        }

        target.totalPlaytimeMs += duration;
        target.totalSessions += 1;
        target.totalBatteryPercentDrained += batteryDelta;
        target.lastPlayedTimestamp = System.currentTimeMillis();

        saveAllStats(context, records);
    }

    public static synchronized List<GameStatRecord> getAllStats(Context context) {
        List<GameStatRecord> list = new ArrayList<>();
        if (context == null) return list;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_STATS_JSON, null);
        if (raw == null || raw.isEmpty()) return list;

        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                GameStatRecord r = new GameStatRecord(
                        obj.getString("pkg"),
                        obj.optString("label", obj.getString("pkg"))
                );
                r.totalPlaytimeMs = obj.optLong("time", 0);
                r.totalSessions = obj.optInt("sessions", 0);
                r.totalBatteryPercentDrained = (float) obj.optDouble("drain", 0.0);
                r.lastPlayedTimestamp = obj.optLong("last", System.currentTimeMillis());
                list.add(r);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading stats", e);
        }

        Collections.sort(list, (a, b) -> Long.compare(b.lastPlayedTimestamp, a.lastPlayedTimestamp));
        return list;
    }

    public static synchronized void saveAllStats(Context context, List<GameStatRecord> list) {
        if (context == null || list == null) return;
        try {
            JSONArray arr = new JSONArray();
            for (GameStatRecord r : list) {
                JSONObject obj = new JSONObject();
                obj.put("pkg", r.packageName);
                obj.put("label", r.gameLabel);
                obj.put("time", r.totalPlaytimeMs);
                obj.put("sessions", r.totalSessions);
                obj.put("drain", r.totalBatteryPercentDrained);
                obj.put("last", r.lastPlayedTimestamp);
                arr.put(obj);
            }
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_STATS_JSON, arr.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving stats", e);
        }
    }

    public static long getTotalPlaytimeMillis(Context context) {
        long total = 0;
        for (GameStatRecord r : getAllStats(context)) {
            total += r.totalPlaytimeMs;
        }
        return total;
    }

    private static int getBatteryLevel(Context context) {
        if (context == null) return 100;
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus == null) return 100;
            return batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
        } catch (Throwable t) {
            return 100;
        }
    }
}
