package com.gamespace.app.tweaks;

public enum TweakCategory {
    ALL("ALL TWEAKS"),
    CPU_GPU("CPU & GPU"),
    TOUCH_DISPLAY("TOUCH & DISPLAY"),
    ROOT_KERNEL("ROOT KERNEL"),
    SHIZUKU_SYSTEM("SHIZUKU / ADB");

    private final String title;

    TweakCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
