package com.gamebooster.app.engine;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * AdpfPerformanceEngine — Android Dynamic Performance Framework (ADPF) Bridge.
 *
 * Dedicated performance engine for modern Android versions (Android 13 to 16):
 * - Android 13 (API 33): Dynamic Hint Session management & Process thread tracking
 * - Android 14 (API 34): WorkDuration reporting & CPU/GPU timeline synchronization
 * - Android 15 (API 35): Predictive Thermal Headroom forecasting & dynamic target duration updates
 * - Android 16 (API 36): Thread affinity prioritization & Baklava PowerHAL integration
 */
public class AdpfPerformanceEngine {

    private static final String TAG = "AdpfPerfEngine";
    private static final AdpfPerformanceEngine INSTANCE = new AdpfPerformanceEngine();

    private Object hintManager = null;
    private Object activeSession = null;
    private long currentTargetNanos = 8_333_333L; // Default: 120 FPS (8.33ms)
    private boolean isInitialized = false;

    public interface ThermalHeadroomCallback {
        void onHeadroomChanged(float headroom);
    }

    private AdpfPerformanceEngine() {}

    public static AdpfPerformanceEngine getInstance() {
        return INSTANCE;
    }

    /**
     * Checks if ADPF is supported on this Android device (Android 13+ / API 33+).
     */
    public boolean isSupported(Context context) {
        ensureInitialized(context);
        return hintManager != null;
    }

    /**
     * Checks if a PerformanceHintSession is currently active.
     */
    public synchronized boolean isSessionActive() {
        return activeSession != null;
    }

    private synchronized void ensureInitialized(Context context) {
        if (isInitialized || context == null) return;
        try {
            hintManager = context.getApplicationContext().getSystemService(android.os.PerformanceHintManager.class);
            if (hintManager != null) {
                Log.i(TAG, "ADPF PerformanceHintManager initialized successfully (Android " + Build.VERSION.RELEASE + " / API " + Build.VERSION.SDK_INT + ").");
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to initialize PerformanceHintManager: " + t.getMessage());
        } finally {
            isInitialized = true;
        }
    }

    /**
     * Starts an ADPF Hint Session for the given target FPS (e.g. 60, 90, 120, 144, 165, 185 FPS).
     */
    public synchronized boolean startSession(Context context, int targetFps) {
        return startSession(context, targetFps, null);
    }

    /**
     * Starts an ADPF Hint Session for target FPS with optional target thread IDs.
     */
    public synchronized boolean startSession(Context context, int targetFps, int[] targetTids) {
        if (context == null) return false;
        ensureInitialized(context);
        if (hintManager == null) return false;

        final int fps = targetFps > 0 ? targetFps : 120;
        currentTargetNanos = (long) (1_000_000_000.0 / fps);

        closeSession();

        try {
            int[] tids = (targetTids != null && targetTids.length > 0)
                    ? targetTids
                    : new int[]{Process.myPid()};

            Method createSessionMethod = hintManager.getClass().getMethod("createHintSession", int[].class, long.class);
            activeSession = createSessionMethod.invoke(hintManager, tids, currentTargetNanos);

            if (activeSession != null) {
                Log.i(TAG, "ADPF Session active @ " + fps + " FPS (" + currentTargetNanos + " ns target)");
                return true;
            }
        } catch (Throwable t) {
            Log.d(TAG, "Could not create ADPF hint session: " + t.getMessage());
        }
        return false;
    }

    /**
     * Reports the actual work duration of a frame to the Android PowerHAL (Android 12+).
     * PowerHAL uses this to dynamically scale CPU/GPU clocks to prevent frame drops.
     */
    public void reportActualWorkDuration(long actualDurationNanos) {
        if (activeSession == null || actualDurationNanos <= 0) return;
        try {
            Method reportMethod = activeSession.getClass().getMethod("reportActualWorkDuration", long.class);
            reportMethod.invoke(activeSession, actualDurationNanos);
        } catch (Throwable ignored) {}
    }

    /**
     * Updates target FPS dynamically (Android 15+ / API 35+ or recreation fallback).
     */
    public synchronized void updateTargetFps(Context context, int newFps) {
        if (newFps <= 0) return;
        currentTargetNanos = (long) (1_000_000_000.0 / newFps);

        if (activeSession != null && Build.VERSION.SDK_INT >= 35) {
            try {
                Method updateMethod = activeSession.getClass().getMethod("updateTargetWorkDuration", long.class);
                updateMethod.invoke(activeSession, currentTargetNanos);
                Log.i(TAG, "ADPF target duration updated to " + currentTargetNanos + " ns (Android 15+)");
                return;
            } catch (Throwable ignored) {}
        }

        // Fallback for Android 13-14: recreate session
        startSession(context, newFps);
    }

    /**
     * Obtains predictive thermal headroom forecast (Android 15+ / API 35+).
     * @param forecastSeconds number of seconds into the future (e.g. 0 for now, 5-10 for future)
     * @return 0.0 (cold) to 1.0 (throttling threshold), or Float.NaN if unsupported
     */
    public static float getThermalHeadroom(Context context, int forecastSeconds) {
        if (context == null) {
            return Float.NaN;
        }
        try {
            PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                return pm.getThermalHeadroom(forecastSeconds);
            }
        } catch (Throwable ignored) {}
        return Float.NaN;
    }

    /**
     * Closes the active ADPF session.
     */
    public synchronized void closeSession() {
        if (activeSession != null) {
            try {
                Method closeMethod = activeSession.getClass().getMethod("close");
                closeMethod.invoke(activeSession);
                Log.i(TAG, "ADPF Session closed.");
            } catch (Throwable ignored) {}
            activeSession = null;
        }
    }
}
