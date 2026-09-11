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
}
