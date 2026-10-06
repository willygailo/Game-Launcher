package com.gamebooster.app.engine;

import android.util.Log;

/**
 * AceCloakEngine — Java wrapper for native ACE (Anti-Cheat Expert) memory cloaking.
 * Intercepts libanort.so and libanogs.so report threads via /proc/self/mem.
 */
public final class AceCloakEngine {

    private static final String TAG = "AceCloakEngine";
    private static volatile boolean sLibLoaded = false;

    static {
        try {
            System.loadLibrary("gamebooster_native");
            sLibLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load gamebooster_native: " + e.getMessage());
        }
    }

    private AceCloakEngine() {}

    public static boolean isAvailable() {
        return sLibLoaded;
    }

    /**
     * Executes in-process cloaking of Tencent ACE modules (libanort.so / libanogs.so).
     * Returns true if one or more entry points were patched with RET_ZERO stubs.
     */
    public static native boolean cloakAceModules();
}
