package com.gamebooster.app.tweaks;
import com.gamebooster.app.config.*;

public enum TweakCategory {
    ALL("ALL OPTIMIZATIONS"),
    CPU_GPU("GRAPHICS & RENDERING"),
    TOUCH_DISPLAY("TOUCH & REFRESH RATE"),
    SHIZUKU_SYSTEM("SYSTEM & SHIZUKU ADB"),
    NETWORK_LATENCY("NETWORK & LATENCY");

    private final String title;

    TweakCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getDisplayName() {
        return title;
    }

    public static TweakCategory fromName(String name) {
        if (name == null || name.trim().isEmpty()) return ALL;
        for (TweakCategory cat : values()) {
            if (cat.name().equalsIgnoreCase(name.trim())) {
                return cat;
            }
        }
        return ALL;
    }
}
