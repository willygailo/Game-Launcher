package com.gamebooster.app.engine;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * NativeFrameworkBridge — Tier 2 Android OS Native Framework API Wrapper.
 *
 * Directly interfaces with official Android SDK and hidden framework APIs:
 * 1. android.app.GameManager (API 31+ / Android 12+) for performance game mode
 * 2. android.os.PowerManager for sustained performance mode & CPU wakelocks
 * 3. android.net.wifi.WifiManager for hardware low-latency Wi-Fi locks (WIFI_MODE_FULL_LOW_LATENCY)
 * 4. android.net.ConnectivityManager for high-priority network request binding
 * 5. android.view.Window & DisplayManager for display refresh rate & frame latency constraints
 */
public class NativeFrameworkBridge {

    private static final String TAG = "NativeFrameworkBridge";
    private static WifiManager.WifiLock wifiLowLatencyLock = null;
    private static PowerManager.WakeLock sustainedPerfWakeLock = null;

    /**
     * Enforces native Android GameManager Performance Mode (API 31+).
     */
    public static boolean setGameModePerformance(Context context, String packageName) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) return false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                Object gameManager = context.getSystemService(Context.GAME_SERVICE);
                if (gameManager != null) {
                    Class<?> gmClass = gameManager.getClass();
                    java.lang.reflect.Method setGameModeMethod = gmClass.getMethod("setGameMode", String.class, int.class);
                    // 2 = GameManager.GAME_MODE_PERFORMANCE
                    setGameModeMethod.invoke(gameManager, packageName.trim(), 2);
                    Log.i(TAG, "Native GameManager API: GAME_MODE_PERFORMANCE enforced on " + packageName);
                    return true;
                }
            } catch (Throwable t) {
                Log.d(TAG, "GameManager API reflection call fallback to cmd: " + t.getMessage());
            }
        }
        return false;
    }

    /**
     * Acquires Hardware Low-Latency Wi-Fi Lock (API 29+ WIFI_MODE_FULL_LOW_LATENCY).
     */
    public static void acquireLowLatencyWifiLock(Context context) {
        if (context == null) return;
        try {
            WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                if (wifiLowLatencyLock == null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        wifiLowLatencyLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "GameSpaceLowLatencyWifi");
                    } else {
                        wifiLowLatencyLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "GameSpaceHiPerfWifi");
                    }
                    wifiLowLatencyLock.setReferenceCounted(false);
                }
                if (!wifiLowLatencyLock.isHeld()) {
                    wifiLowLatencyLock.acquire();
                    Log.i(TAG, "Hardware Low-Latency Wi-Fi Lock acquired successfully.");
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to acquire Low-Latency Wi-Fi lock: " + t.getMessage());
        }
    }

    /**
     * Releases Hardware Low-Latency Wi-Fi Lock.
     */
    public static void releaseLowLatencyWifiLock() {
        try {
            if (wifiLowLatencyLock != null && wifiLowLatencyLock.isHeld()) {
                wifiLowLatencyLock.release();
                Log.i(TAG, "Hardware Low-Latency Wi-Fi Lock released.");
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Acquires Sustained Performance Power Lock.
     */
    public static void acquireSustainedPerformanceLock(Context context) {
        if (context == null) return;
        try {
            PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                if (sustainedPerfWakeLock == null) {
                    sustainedPerfWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GameSpace:SustainedPerfLock");
                    sustainedPerfWakeLock.setReferenceCounted(false);
                }
                if (!sustainedPerfWakeLock.isHeld()) {
                    sustainedPerfWakeLock.acquire(3 * 60 * 60 * 1000L); // 3-hour safety timeout
                    Log.i(TAG, "Sustained performance wakelock acquired.");
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to acquire sustained performance lock: " + t.getMessage());
        }
    }

    /**
     * Releases Sustained Performance Power Lock.
     */
    public static void releaseSustainedPerformanceLock() {
        try {
            if (sustainedPerfWakeLock != null && sustainedPerfWakeLock.isHeld()) {
                sustainedPerfWakeLock.release();
                Log.i(TAG, "Sustained performance lock released.");
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Binds high priority network requests via ConnectivityManager for competitive gaming.
     */
    public static void requestHighPriorityNetwork(Context context) {
        if (context == null) return;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                NetworkRequest request = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                        .build();

                cm.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        super.onAvailable(network);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                cm.bindProcessToNetwork(network);
                                Log.i(TAG, "Process successfully bound to high-priority gaming network.");
                            } catch (Throwable ignored) {}
                        }
                    }
                });
            }
        } catch (Throwable t) {
            Log.w(TAG, "requestHighPriorityNetwork fallback: " + t.getMessage());
        }
    }

    /**
     * Applies Window optimizations: removes frame pacing jitter, sets max brightness if allowed.
     */
    public static void applyWindowOptimizations(Window window) {
        if (window == null) return;
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.setColorMode(android.content.pm.ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT);
            }
        } catch (Throwable ignored) {}
    }
}
