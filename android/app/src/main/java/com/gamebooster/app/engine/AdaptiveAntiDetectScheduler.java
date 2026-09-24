package com.gamebooster.app.engine;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.gamebooster.app.config.GameAutoInjectDispatcher;
import com.gamebooster.app.config.NativeConfigInjector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AdaptiveAntiDetectScheduler — Dynamic Game Lifecycle & Anti-Sweep Watchdog.
 *
 * Monitors game process CPU activity to schedule config/memory synchronizations
 * strictly during asset-loading screen transitions (when anti-cheat integrity
 * threads are dormant) with randomized jitter, avoiding fixed-cadence sweeps.
 *
 * Zero UI footprint — runs entirely in the background.
 */
public class AdaptiveAntiDetectScheduler {

    private static final String TAG = "AdaptiveScheduler";
    private static volatile AdaptiveAntiDetectScheduler sInstance;

    private HandlerThread mWatcherThread;
    private Handler mWatcherHandler;
    private final AtomicBoolean mIsRunning = new AtomicBoolean(false);
    private final SecureRandom mRandom = new SecureRandom();

    private String mMonitoredPackage = null;
    private int mMonitoredPid = -1;
    private long mLastUtime = 0;
    private long mLastStime = 0;
    private long mLastSampleTime = 0;

    private AdaptiveAntiDetectScheduler() {}

    public static AdaptiveAntiDetectScheduler getInstance() {
        if (sInstance == null) {
            synchronized (AdaptiveAntiDetectScheduler.class) {
                if (sInstance == null) {
                    sInstance = new AdaptiveAntiDetectScheduler();
                }
            }
        }
        return sInstance;
    }

    /**
     * Starts watchdog for the targeted game package.
     */
    public synchronized void startWatchdog(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        mMonitoredPackage = packageName.trim();

        // 1. Arm native stealth layers
        NativeConfigInjector.cloakProcessIdentity("surfaceflinger");
        NativeConfigInjector.armSignalTrapGuard();

        if (mWatcherThread == null || !mWatcherThread.isAlive()) {
            mWatcherThread = new HandlerThread("ac-stealth-watchdog");
            mWatcherThread.start();
            mWatcherHandler = new Handler(mWatcherThread.getLooper());
        }

        mIsRunning.set(true);
        mMonitoredPid = resolvePid(mMonitoredPackage);
        mLastUtime = 0;
        mLastStime = 0;
        mLastSampleTime = System.currentTimeMillis();

        Log.i(TAG, "Watchdog started for " + mMonitoredPackage + " (PID: " + mMonitoredPid + ")");
        scheduleNextCheck(3000);
    }

    /**
     * Stops the active watchdog.
     */
    public synchronized void stopWatchdog() {
        mIsRunning.set(false);
        if (mWatcherHandler != null) {
            mWatcherHandler.removeCallbacksAndMessages(null);
        }
        if (mWatcherThread != null) {
            mWatcherThread.quitSafely();
            mWatcherThread = null;
            mWatcherHandler = null;
        }
        NativeConfigInjector.disarmSignalTrapGuard();
        Log.i(TAG, "Watchdog stopped for " + mMonitoredPackage);
        mMonitoredPackage = null;
        mMonitoredPid = -1;
    }

    private void scheduleNextCheck(long delayMs) {
        if (!mIsRunning.get() || mWatcherHandler == null) return;

        // Add randomized jitter (±500ms) to evade periodic detection sweeps
        long jitter = (long) (mRandom.nextInt(1000) - 500);
        long actualDelay = Math.max(1500, delayMs + jitter);

        mWatcherHandler.postDelayed(this::checkAndSync, actualDelay);
    }

    private void checkAndSync() {
        if (!mIsRunning.get() || mMonitoredPackage == null) return;

        if (mMonitoredPid <= 0) {
            mMonitoredPid = resolvePid(mMonitoredPackage);
            if (mMonitoredPid <= 0) {
                // Game not yet running or exited
                scheduleNextCheck(4000);
                return;
            }
        }

        float cpuUsage = calculateProcessCpuUsage(mMonitoredPid);

        // Anti-cheat heuristic: Loading screens spike CPU > 70% while asset-streaming
        // In this window, AC sweep threads are suspended or deprioritized.
        if (cpuUsage > 70.0f) {
            Log.d(TAG, "High CPU spike detected (" + cpuUsage + "%) — opportunistic stealth sync window");
            performStealthSync();
            // Backoff after injection so we don't repeat during the same transition
            scheduleNextCheck(15000);
        } else {
            // Normal in-game or idle lobby: check periodically
            scheduleNextCheck(5000);
        }
    }

    private void performStealthSync() {
        try {
            if (mMonitoredPackage != null) {
                GameAutoInjectDispatcher.dispatchForPackage(mMonitoredPackage);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Stealth sync warning: " + t.getMessage());
        }
    }

    private int resolvePid(String packageName) {
        try {
            String out = CommandExecutor.executeSystemCommand("pidof " + packageName);
            if (out != null && !out.trim().isEmpty()) {
                String[] parts = out.trim().split("\\s+");
                return Integer.parseInt(parts[0]);
            }
        } catch (Throwable ignored) {}
        return -1;
    }

    private float calculateProcessCpuUsage(int pid) {
        if (pid <= 0) return 0f;
        String statPath = "/proc/" + pid + "/stat";
        try (BufferedReader reader = new BufferedReader(new FileReader(statPath))) {
            String line = reader.readLine();
            if (line == null) return 0f;

            String[] fields = line.split("\\s+");
            if (fields.length > 14) {
                long utime = Long.parseLong(fields[13]);
                long stime = Long.parseLong(fields[14]);
                long now = System.currentTimeMillis();

                if (mLastSampleTime > 0 && now > mLastSampleTime) {
                    long timeDeltaMs = now - mLastSampleTime;
                    long tickDelta = (utime - mLastUtime) + (stime - mLastStime);

                    mLastUtime = utime;
                    mLastStime = stime;
                    mLastSampleTime = now;

                    // Convert clock ticks (~100Hz on Android) to rough percentage
                    return Math.min(100f, Math.max(0f, (tickDelta * 1000f) / (timeDeltaMs * 10f)));
                } else {
                    mLastUtime = utime;
                    mLastStime = stime;
                    mLastSampleTime = now;
                }
            }
        } catch (Throwable ignored) {
            // Process might have terminated
            mMonitoredPid = -1;
        }
        return 0f;
    }
}
