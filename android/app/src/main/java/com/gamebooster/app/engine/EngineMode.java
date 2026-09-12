package com.gamebooster.app.engine;

public enum EngineMode {
    ROOT("FULL ACCESS: ROOT (UID 0) SUPERUSER", 0xFFFF0055),
    DUAL_ENGINE("DUAL ENGINE: ROOT + SHIZUKU ACTIVE", 0xFF00FFCC),
    SHIZUKU("FULL ACCESS: SHIZUKU API ACTIVE", 0xFF00FF66),
    SYSTEM_SETTINGS("SYSTEM SETTINGS ENGINE", 0xFF00F0FF),
    READ_ONLY("HARDWARE MONITOR MODE", 0xFFFFB800);

    private final String displayName;
    private final int colorHex;

    EngineMode(String displayName, int colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColorHex() {
        return colorHex;
    }
}
