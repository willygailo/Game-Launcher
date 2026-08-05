package com.gamebooster.app.overlay;

public enum CrosshairPreset {
    DOT("Dot"),
    TACTICAL_CROSS("Tactical Cross"),
    SCOPE_RING("Scope Ring"),
    SNIPER_CROSS("Sniper Cross");

    private final String label;

    CrosshairPreset(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
