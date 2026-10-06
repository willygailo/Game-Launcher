package com.gamebooster.app.engine;

import android.util.Log;

/**
 * Il2cppDirectScanner — Java wrapper for native memory pattern scanning in libil2cpp.so.
 */
public final class Il2cppDirectScanner {

    private static final String TAG = "Il2cppDirectScanner";
    private static volatile boolean sLibLoaded = false;

    static {
        try {
            System.loadLibrary("gamebooster_native");
            sLibLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load gamebooster_native: " + e.getMessage());
        }
    }

    private Il2cppDirectScanner() {}

    public static boolean isAvailable() {
        return sLibLoaded;
    }

    /**
     * Resolves the runtime base virtual address of libil2cpp.so from /proc/self/maps.
     */
    public static native long getIl2cppBase();

    /**
     * Scans libil2cpp.so memory segments for the specified signature pattern and mask.
     * Mask format: 'x' for exact match, '?' for wildcard.
     */
    public static native long scanPattern(byte[] pattern, String mask);
}
