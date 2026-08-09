package com.gamebooster.app.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * LogItem — Represents a single system optimization action entry for history audit logs.
 */
public class LogItem {

    public final String id;
    public final String actionName;
    public final String description;
    public final long timestamp;
    public final boolean success;
    public final String previousValue;
    public final String newValue;
    public final String errorInfo;

    public LogItem(String actionName, String description, boolean success,
                   String previousValue, String newValue, String errorInfo) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.actionName = actionName;
        this.description = description;
        this.timestamp = System.currentTimeMillis();
        this.success = success;
        this.previousValue = previousValue != null ? previousValue : "N/A";
        this.newValue = newValue != null ? newValue : "N/A";
        this.errorInfo = errorInfo;
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
