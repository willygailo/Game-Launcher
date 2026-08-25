package com.gamebooster.app.cleaner.model;

import com.gamebooster.app.R;

public enum JunkCategory {
    APP_CACHE(
            "App & Game Caches",
            "Temporary app data, HTTP cache, and game asset bundles",
            "🧹",
            R.color.accent_cyan,
            true
    ),
    SYSTEM_CACHE(
            "System & Shader Caches",
            "Android runtime caches, GPU compiled shaders, and package cache trims",
            "⚡",
            R.color.accent_neon_green,
            true
    ),
    TEMP_FILES(
            "Temp Files & Crash Logs",
            "Logcat dumps, ANR crash reports, tombstones, and /data/local/tmp buffers",
            "🛡️",
            R.color.accent_purple,
            true
    ),
    THUMBNAILS(
            "Media & Thumbnail Caches",
            "Outdated image and video thumbnails taking up hidden space (.thumbnails)",
            "🖼️",
            R.color.accent_amber,
            true
    ),
    OBSOLETE_APKS(
            "Obsolete APK Installers",
            "Already-installed or stale APK package installation files in storage",
            "📦",
            R.color.accent_orange,
            false
    ),
    EMPTY_FOLDERS(
            "Empty Directories",
            "Stale empty directory trees left behind in storage",
            "📁",
            R.color.accent_ml_blue,
            false
    ),
    GAME_RESIDUALS(
            "Game Residuals & Log Dumps",
            "Diagnostic logs, debug traces, and telemetry dumps inside game directories",
            "🎮",
            R.color.accent_pubg_orange,
            true
    ),
    RESIDUAL_UNINSTALLED(
            "Uninstalled App Residuals",
            "Leftover data and media directories from uninstalled apps",
            "🗑️",
            R.color.accent_red,
            false
    ),
    SOCIAL_CACHE(
            "Messaging & Social Cache",
            "WhatsApp, Telegram, TikTok & browser temp caches and media previews",
            "💬",
            R.color.accent_ml_blue,
            true
    );

    private final String title;
    private final String description;
    private final String iconEmoji;
    private final int colorResId;
    private final boolean defaultSelected;

    JunkCategory(String title, String description, String iconEmoji, int colorResId, boolean defaultSelected) {
        this.title = title;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.colorResId = colorResId;
        this.defaultSelected = defaultSelected;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public int getColorResId() {
        return colorResId;
    }

    public boolean isDefaultSelected() {
        return defaultSelected;
    }
}
