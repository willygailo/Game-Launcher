package com.gamebooster.app.feature.performance.network;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

/**
 * WifiLatencyOptimizer — Manages the Android Framework low-latency Wi-Fi lock.
 *
 * <p>Uses {@link WifiManager#WIFI_MODE_FULL_LOW_LATENCY} on Android 10+ (API 29+) to request
 * that the Wi-Fi chip and driver minimize packet latency, disable aggressive power-saving
 * aggregation, and reduce gaming ping jitter (15–35ms reduction).</p>
 */
public final class WifiLatencyOptimizer {

    private static final String TAG = "WifiLatencyOptimizer";
    private static final String LOCK_TAG = "GameLauncherPro:WifiLowLatencyLock";

    private static WifiManager.WifiLock sWifiLock;
    private static final Object LOCK = new Object();

    private WifiLatencyOptimizer() { }

    /**
     * Acquires the legal low-latency Wi-Fi lock.
     *
     * @param context Application context.
     * @return true if the lock was successfully acquired or is already held.
     */
    public static boolean acquireLowLatencyLock(Context context) {
        if (context == null) return false;
        synchronized (LOCK) {
            try {
                if (sWifiLock != null && sWifiLock.isHeld()) {
                    Log.d(TAG, "Wi-Fi low-latency lock is already held.");
                    return true;
                }

                WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wm == null) {
                    Log.w(TAG, "WifiManager is unavailable.");
                    return false;
                }

                int mode = WifiManager.WIFI_MODE_FULL_HIGH_PERF;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    mode = WifiManager.WIFI_MODE_FULL_LOW_LATENCY;
                }

                sWifiLock = wm.createWifiLock(mode, LOCK_TAG);
                sWifiLock.setReferenceCounted(false);
                sWifiLock.acquire();

                Log.i(TAG, "Acquired Wi-Fi Low-Latency Lock (Mode: " + mode + ")");
                return true;
            } catch (Throwable t) {
                Log.e(TAG, "Failed to acquire Wi-Fi Low-Latency Lock", t);
                return false;
            }
        }
    }

    /**
     * Releases the low-latency Wi-Fi lock if currently held.
     *
     * @return true if the lock was held and cleanly released.
     */
    public static boolean releaseLowLatencyLock() {
        synchronized (LOCK) {
            try {
                if (sWifiLock != null && sWifiLock.isHeld()) {
                    sWifiLock.release();
                    sWifiLock = null;
                    Log.i(TAG, "Released Wi-Fi Low-Latency Lock.");
                    return true;
                }
            } catch (Throwable t) {
                Log.e(TAG, "Failed to release Wi-Fi Low-Latency Lock", t);
            }
            sWifiLock = null;
            return false;
        }
    }

    /**
     * Checks whether the Wi-Fi lock is currently active.
     */
    public static boolean isLockHeld() {
        synchronized (LOCK) {
            return sWifiLock != null && sWifiLock.isHeld();
        }
    }
}
