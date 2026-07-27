package com.gamespace.app.core;

import android.content.Context;
import android.provider.Settings;

import com.gamespace.app.utils.ShellExecutor;
import com.gamespace.app.utils.ShizukuExecutor;

public final class RootChecker {

    public enum AccessLevel {
        FULL_ROOT,
        SHIZUKU,
        SETTINGS_ONLY
    }

    private RootChecker() {}

    public static boolean isRootAvailable() {
        return ShellExecutor.isRootAvailable();
    }

    public static boolean isShizukuAvailable() {
        return ShizukuExecutor.isShizukuAvailable();
    }

    public static boolean isSettingsWriteAvailable(Context context) {
        return Settings.Global.canWrite(context);
    }

    public static AccessLevel getBestAccessLevel(Context context) {
        if (isRootAvailable()) return AccessLevel.FULL_ROOT;
        if (isShizukuAvailable()) return AccessLevel.SHIZUKU;
        if (isSettingsWriteAvailable(context)) return AccessLevel.SETTINGS_ONLY;
        return null;
    }

    public static boolean canExecuteCommand(Context context, boolean requiresRoot, boolean requiresShizuku) {
        if (requiresRoot) return isRootAvailable();
        if (requiresShizuku) return isShizukuAvailable();
        return true;
    }
}
