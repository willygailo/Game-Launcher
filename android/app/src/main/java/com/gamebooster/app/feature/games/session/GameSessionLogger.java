package com.gamebooster.app.feature.games.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * GameSessionLogger manages game session telemetry, recording launch timestamps,
 * active spoof profiles, target FPS, duration, and tweaks applied per session.
 */
public class GameSessionLogger {

    private static final String TAG = "GameSessionLogger";
    private static final String PREF_NAME = "game_session_prefs";
    private static final String KEY_SESSIONS = "session_history_json";
    private static final int MAX_SESSIONS = 50;

    public static class SessionEntry {
        public String packageName;
        public String gameName;
        public int targetFps;
        public String spoofProfile;
        public long startTimeMs;
        public long endTimeMs;
        public long durationSec;

        public SessionEntry(String packageName, String gameName, int targetFps, String spoofProfile, long startTimeMs) {
            this.packageName = packageName;
            this.gameName = gameName;
            this.targetFps = targetFps;
            this.spoofProfile = spoofProfile;
            this.startTimeMs = startTimeMs;
            this.endTimeMs = startTimeMs;
            this.durationSec = 0;
        }

        public JSONObject toJsonObject() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("packageName", packageName);
                obj.put("gameName", gameName);
                obj.put("targetFps", targetFps);
                obj.put("spoofProfile", spoofProfile);
                obj.put("startTimeMs", startTimeMs);
                obj.put("endTimeMs", endTimeMs);
                obj.put("durationSec", durationSec);
            } catch (Exception e) {
                Log.e(TAG, "Error serializing session entry", e);
            }
            return obj;
        }

        public static SessionEntry fromJsonObject(JSONObject obj) {
            try {
                String pkg = obj.optString("packageName", "");
                String name = obj.optString("gameName", "");
                int fps = obj.optInt("targetFps", 60);
                String profile = obj.optString("spoofProfile", "Default");
                long start = obj.optLong("startTimeMs", System.currentTimeMillis());
                SessionEntry entry = new SessionEntry(pkg, name, fps, profile, start);
                entry.endTimeMs = obj.optLong("endTimeMs", start);
                entry.durationSec = obj.optLong("durationSec", 0);
                return entry;
            } catch (Exception e) {
                Log.e(TAG, "Error deserializing session entry", e);
                return null;
            }
        }
    }

    private static SessionEntry activeSession = null;

    public static void startSession(Context context, String packageName, String gameName, int targetFps, String spoofProfile) {
        activeSession = new SessionEntry(packageName, gameName, targetFps, spoofProfile, System.currentTimeMillis());
        Log.d(TAG, "Started Game Session for " + gameName + " (" + packageName + ") @ " + targetFps + " FPS with profile " + spoofProfile);
    }

    public static void endActiveSession(Context context) {
        if (activeSession == null) return;

        activeSession.endTimeMs = System.currentTimeMillis();
        activeSession.durationSec = Math.max(1, (activeSession.endTimeMs - activeSession.startTimeMs) / 1000);

        Log.d(TAG, "Ended Game Session for " + activeSession.gameName + ". Duration: " + activeSession.durationSec + "s");

        saveSession(context, activeSession);
        activeSession = null;
    }

    private static synchronized void saveSession(Context context, SessionEntry entry) {
        if (context == null || entry == null) return;

        List<SessionEntry> history = getSessionHistory(context);
        history.add(0, entry); // Add newest at beginning

        // Cap to MAX_SESSIONS
        if (history.size() > MAX_SESSIONS) {
            history = history.subList(0, MAX_SESSIONS);
        }

        JSONArray array = new JSONArray();
        for (SessionEntry e : history) {
            array.put(e.toJsonObject());
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SESSIONS, array.toString()).apply();
    }

    public static synchronized List<SessionEntry> getSessionHistory(Context context) {
        List<SessionEntry> list = new ArrayList<>();
        if (context == null) return list;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_SESSIONS, "[]");

        try {
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                SessionEntry entry = SessionEntry.fromJsonObject(obj);
                if (entry != null) {
                    list.add(entry);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading session history", e);
        }
        return list;
    }

    public static void clearHistory(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_SESSIONS).apply();
        Log.d(TAG, "Cleared session history.");
    }
}
