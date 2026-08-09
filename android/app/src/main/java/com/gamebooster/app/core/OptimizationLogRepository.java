package com.gamebooster.app.core;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * OptimizationLogRepository — Thread-safe persistent JSON logger for storing action history.
 */
public class OptimizationLogRepository {

    private static final String PREF_NAME = "optimization_history_logs_prefs";
    private static final String KEY_LOGS_ARRAY = "history_logs_json";
    private static final int MAX_LOGS = 100;

    public static synchronized void addLog(Context context, LogItem logItem) {
        if (context == null || logItem == null) return;

        try {
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String rawJson = prefs.getString(KEY_LOGS_ARRAY, "[]");
            JSONArray array = new JSONArray(rawJson);

            JSONObject obj = new JSONObject();
            obj.put("id", logItem.id);
            obj.put("actionName", logItem.actionName);
            obj.put("description", logItem.description);
            obj.put("timestamp", logItem.timestamp);
            obj.put("success", logItem.success);
            obj.put("previousValue", logItem.previousValue);
            obj.put("newValue", logItem.newValue);
            obj.put("errorInfo", logItem.errorInfo != null ? logItem.errorInfo : "");

            // Insert new log at head (newest first)
            JSONArray updatedArray = new JSONArray();
            updatedArray.put(obj);
            for (int i = 0; i < array.length() && i < (MAX_LOGS - 1); i++) {
                updatedArray.put(array.get(i));
            }

            prefs.edit().putString(KEY_LOGS_ARRAY, updatedArray.toString()).apply();
        } catch (Throwable ignored) {}
    }

    public static synchronized List<LogItem> getLogs(Context context) {
        List<LogItem> list = new ArrayList<>();
        if (context == null) return list;

        try {
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String rawJson = prefs.getString(KEY_LOGS_ARRAY, "[]");
            JSONArray array = new JSONArray(rawJson);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String actionName = obj.optString("actionName", "Optimization Action");
                String description = obj.optString("description", "");
                boolean success = obj.optBoolean("success", true);
                String previousValue = obj.optString("previousValue", "Default");
                String newValue = obj.optString("newValue", "Applied");
                String errorInfo = obj.optString("errorInfo", null);

                LogItem item = new LogItem(actionName, description, success, previousValue, newValue, errorInfo);
                list.add(item);
            }
        } catch (Throwable ignored) {}

        return list;
    }

    public static synchronized void clearLogs(Context context) {
        if (context == null) return;
        context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().remove(KEY_LOGS_ARRAY).apply();
    }
}
