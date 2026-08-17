package com.gamebooster.app.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {

    private static final AppExecutors INSTANCE = new AppExecutors();

    private final ExecutorService commandIO;
    private final ExecutorService scanIO;
    private final Handler mainThread;

    private AppExecutors() {
        this.commandIO = Executors.newFixedThreadPool(4);
        this.scanIO = Executors.newFixedThreadPool(2);
        this.mainThread = new Handler(Looper.getMainLooper());
    }

    public static AppExecutors getInstance() {
        return INSTANCE;
    }

    public void executeCommand(Runnable runnable) {
        if (runnable != null) {
            commandIO.execute(runnable);
        }
    }

    public void executeScan(Runnable runnable) {
        if (runnable != null) {
            scanIO.execute(runnable);
        }
    }

    public void postToMainThread(Runnable runnable) {
        if (runnable != null) {
            mainThread.post(runnable);
        }
    }
}
