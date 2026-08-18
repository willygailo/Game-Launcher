package com.gamebooster.app.core;

import android.content.pm.ServiceInfo;
import android.os.Build;

public class CompatHelper {

    public static boolean isAtLeast(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    public static boolean isAndroid13OrHigher() {
        return Build.VERSION.SDK_INT >= 33; // Build.VERSION_CODES.TIRAMISU
    }

    public static boolean isAndroid14OrHigher() {
        return Build.VERSION.SDK_INT >= 34; // Build.VERSION_CODES.UPSIDE_DOWN_CAKE
    }

    public static boolean isAndroid15OrHigher() {
        return Build.VERSION.SDK_INT >= 35; // Android 15 (Vanilla Ice Cream)
    }

    public static boolean isAndroid16OrHigher() {
        return Build.VERSION.SDK_INT >= 36; // Android 16 (Baklava)
    }

    public static boolean requiresNotificationPermission() {
        return Build.VERSION.SDK_INT >= 33; // API 33+
    }

    public static boolean requiresGranularStorage() {
        return Build.VERSION.SDK_INT >= 33; // API 33+
    }

    public static boolean requiresEdgeToEdge() {
        return Build.VERSION.SDK_INT >= 35; // API 35 (Android 15+)
    }

    public static int getForegroundServiceTypeFlag() {
        if (Build.VERSION.SDK_INT >= 34) { // API 34+
            return ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
        }
        return 0;
    }
}
