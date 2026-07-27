package com.gamespace.app.core;

public enum EngineMode {
    ROOT("ROOT MODE (su)", 0xFF7000FF),
    SHIZUKU("SHIZUKU ADB MODE", 0xFF00FF88),
    READ_ONLY("READ-ONLY MODE", 0xFFFFCC00);

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
