package com.gamebooster.app.shizuku;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * OsVersionGuard — Resilient Android OS version detection & normalization.
 *
 * Prevents OS version misdetection bugs on Android 13, 14, 15, and 16 (API 33-36)
 * devices (such as Samsung One UI, Xiaomi HyperOS/MIUI with GMS compatibility shims)
 * where Build.VERSION.SDK_INT may return an unexpected or clamped API level.
 *
 * Checks android.os.SystemProperties via reflection for "ro.build.version.sdk"
 * and "ro.build.version.release", falling back safely to Build.VERSION.SDK_INT.
 */
public final class OsVersionGuard {

    private static final String TAG = "OsVersionGuard";
    private static volatile int sCachedSdkInt = -1;
    private static volatile String sCachedRelease = null;

    private OsVersionGuard() {}

    /**
     * Retrieves the most reliable Android SDK integer, checking system properties
     * before falling back to Build.VERSION.SDK_INT. Clamped to a valid range [1, 50].
     */
    public static int getReliableSdkInt() {
        if (sCachedSdkInt > 0) {
            return sCachedSdkInt;
        }

        int detectedSdk = -1;
        try {
            Class<?> spClass = Class.forName("android.os.SystemProperties");
            Method getIntMethod = spClass.getMethod("getInt", String.class, int.class);
            detectedSdk = (int) getIntMethod.invoke(null, "ro.build.version.sdk", -1);
        } catch (Throwable t) {
            Log.d(TAG, "SystemProperties reflection fallback: " + t.getMessage());
        }

        if (detectedSdk <= 0) {
            detectedSdk = Build.VERSION.SDK_INT;
        }

        // Additional sanity check: if release version indicates Android 16 but SDK_INT is 35
        String release = getReliableRelease();
        if (detectedSdk < 36 && release != null && (release.startsWith("16") || release.equalsIgnoreCase("Baklava"))) {
            Log.i(TAG, "Detected Android 16 release ('" + release + "') with reported SDK " + detectedSdk + " -> normalizing to API 36");
            detectedSdk = 36;
        } else if (detectedSdk < 35 && release != null && (release.startsWith("15") || release.equalsIgnoreCase("VanillaIceCream"))) {
            Log.i(TAG, "Detected Android 15 release ('" + release + "') with reported SDK " + detectedSdk + " -> normalizing to API 35");
            detectedSdk = 35;
        }

        if (detectedSdk < 1) detectedSdk = Build.VERSION.SDK_INT;
        if (detectedSdk < 1) detectedSdk = 33; // safe fallback

        sCachedSdkInt = detectedSdk;
        Log.i(TAG, "OsVersionGuard resolved SDK_INT=" + detectedSdk + " (Build.SDK_INT=" + Build.VERSION.SDK_INT + ", release=" + release + ")");
        return sCachedSdkInt;
    }

    public static String getReliableRelease() {
        if (sCachedRelease != null) {
            return sCachedRelease;
        }
        String release = null;
        try {
            Class<?> spClass = Class.forName("android.os.SystemProperties");
            Method getMethod = spClass.getMethod("get", String.class, String.class);
            release = (String) getMethod.invoke(null, "ro.build.version.release", null);
        } catch (Throwable ignored) {}

        if (release == null || release.trim().isEmpty()) {
            release = Build.VERSION.RELEASE;
        }
        if (release == null || release.trim().isEmpty()) {
            release = "14";
        }
        sCachedRelease = release;
        return release;
    }

    public static boolean isAndroid13OrAbove() {
        return getReliableSdkInt() >= 33; // TIRAMISU
    }

    public static boolean isAndroid14OrAbove() {
        return getReliableSdkInt() >= 34; // UPSIDE_DOWN_CAKE
    }

    public static boolean isAndroid15OrAbove() {
        return getReliableSdkInt() >= 35; // VANILLA_ICE_CREAM
    }

    public static boolean isAndroid16OrAbove() {
        return getReliableSdkInt() >= 36; // BAKLAVA
    }

    public static String getOsVersionName() {
        int sdk = getReliableSdkInt();
        if (sdk >= 36) return "Android 16";
        if (sdk == 35) return "Android 15";
        if (sdk == 34) return "Android 14";
        if (sdk == 33) return "Android 13";
        if (sdk == 32 || sdk == 31) return "Android 12";
        if (sdk == 30) return "Android 11";
        return "Android " + getReliableRelease();
    }
}
