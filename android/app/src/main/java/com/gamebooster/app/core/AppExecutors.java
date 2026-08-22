package com.gamebooster.app.core;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {

    private static final String TAG = "AppExecutors";
    private static final AppExecutors INSTANCE = new AppExecutors();

    private final ExecutorService commandIO;
    private final ExecutorService scanIO;
    private final Handler mainThread;

    private AppExecutors() {
        this.commandIO = Executors.newSingleThreadExecutor();
        this.scanIO = Executors.newFixedThreadPool(4);
        this.mainThread = new Handler(Looper.getMainLooper());
    }

    public static AppExecutors getInstance() {
        return INSTANCE;
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
        mainThread.post(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                Log.e(TAG, "Main thread execution error: " + t.getMessage(), t);
            }
        });
    }
}
