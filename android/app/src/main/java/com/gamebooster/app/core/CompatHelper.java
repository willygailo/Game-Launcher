package com.gamebooster.app.core;

import android.content.pm.ServiceInfo;
import android.os.Build;

/**
 * Android 13 (API 33), Android 14 (API 34), Android 15 (API 35), and Android 16 (API 36)
 * SDK version compatibility helper.
 */
public class CompatHelper {

    public static boolean isAtLeast(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    public static boolean isAndroid13OrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU; // API 33+ (Android 13)
    }

    public static boolean isAndroid14OrHigher() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE; // API 34+ (Android 14)
    }

    public static boolean isAndroid15OrHigher() {
        return Build.VERSION.SDK_INT >= 35; // API 35+ (Android 15)
    }

    public static boolean isAndroid16OrHigher() {
        return Build.VERSION.SDK_INT >= 36; // API 36+ (Android 16)
    }

    public static boolean requiresNotificationPermission() {
        return isAndroid13OrHigher();
    }

    public static boolean requiresGranularStorage() {
        return isAndroid13OrHigher();
    }

    public static boolean requiresEdgeToEdge() {
        return isAndroid15OrHigher();
    }

    public static int getForegroundServiceTypeFlag() {
        if (isAndroid14OrHigher()) {
            return ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE;
        }
        return 0;
    }
}
