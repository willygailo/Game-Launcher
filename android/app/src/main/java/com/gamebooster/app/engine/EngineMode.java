package com.gamebooster.app.engine;

public enum EngineMode {
    SHIZUKU("SHIZUKU ADB ENGINE", 0xFF00FF66),
    ROOT("ROOT PRIVILEGED ENGINE", 0xFFFF0055),
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
