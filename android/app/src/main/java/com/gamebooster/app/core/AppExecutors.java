package com.gamebooster.app.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppExecutors {

    private static final String TAG = "AppExecutors";
    private static final AppExecutors INSTANCE = new AppExecutors();

    private final ExecutorService commandIO;
    private final ExecutorService settingsIO;
    private final ExecutorService scanIO;
    private final ScheduledExecutorService scheduledIO;
    private final Handler mainThread;

    private AppExecutors() {
        this.commandIO = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(() -> {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
                r.run();
            }, "GameBooster-CommandIO");
            t.setDaemon(true);
            return t;
        });
        this.settingsIO = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(() -> {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
                r.run();
            }, "GameBooster-SettingsIO");
            t.setDaemon(true);
            return t;
        });
        this.scanIO = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(() -> {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                r.run();
            }, "GameBooster-ScanIO");
            t.setDaemon(true);
            return t;
        });
        this.scheduledIO = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(() -> {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
                r.run();
            }, "GameBooster-ScheduledIO");
            t.setDaemon(true);
            return t;
        });
        this.mainThread = new Handler(Looper.getMainLooper());
    }

    public static AppExecutors getInstance() {
        return INSTANCE;
    }

    public ExecutorService getCommandIO() {
        return commandIO;
    }

    public ExecutorService getSettingsIO() {
        return settingsIO;
    }

    public ExecutorService getScanIO() {
        return scanIO;
    }

    public ScheduledExecutorService getScheduledIO() {
        return scheduledIO;
    }

    public void schedule(Runnable runnable, long delay, TimeUnit unit) {
        if (runnable == null) return;
        scheduledIO.schedule(runnable, delay, unit);
    }

    public void executeCommand(Runnable runnable) {
        if (runnable == null) return;
        commandIO.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                Log.e(TAG, "Command execution error: " + t.getMessage(), t);
            }
        });
    }

    public void executeSettings(Runnable runnable) {
        if (runnable == null) return;
        settingsIO.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                Log.e(TAG, "Settings execution error: " + t.getMessage(), t);
            }
        });
    }

    public void executeScan(Runnable runnable) {
        if (runnable == null) return;
        scanIO.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                Log.e(TAG, "Scan execution error: " + t.getMessage(), t);
            }
        });
    }

    public void postToMainThread(Runnable runnable) {
        if (runnable == null) return;
        boolean posted = false;
        try {
            if (mainThread != null && mainThread.getLooper() != null) {
                posted = mainThread.post(() -> {
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        Log.e(TAG, "Main thread execution error: " + t.getMessage(), t);
                    }
                });
            }
        } catch (Throwable ignored) {
            posted = false;
        }

        if (!posted) {
            try {
                runnable.run();
            } catch (Throwable t) {
                Log.e(TAG, "Fallback direct execution error: " + t.getMessage(), t);
            }
        }
    }

    public void postDelayed(Runnable runnable, long delayMillis) {
        if (runnable == null) return;
        boolean posted = false;
        try {
            if (mainThread != null && mainThread.getLooper() != null) {
                posted = mainThread.postDelayed(() -> {
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        Log.e(TAG, "Main thread delayed execution error: " + t.getMessage(), t);
                    }
                }, delayMillis);
            }
        } catch (Throwable ignored) {
            posted = false;
        }

        if (!posted) {
            scheduledIO.schedule(runnable, delayMillis, TimeUnit.MILLISECONDS);
        }
    }
}
