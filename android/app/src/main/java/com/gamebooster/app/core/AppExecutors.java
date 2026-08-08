package com.gamebooster.app.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutors {

    private static final AppExecutors INSTANCE = new AppExecutors();

    private final ExecutorService commandIO;
    private final ExecutorService scanIO;
    private final ExecutorService monitorIO;
    private final Handler mainThread;

    private AppExecutors() {
        this.commandIO = Executors.newFixedThreadPool(4);
        this.scanIO = Executors.newFixedThreadPool(2);
        this.monitorIO = Executors.newSingleThreadExecutor();
        this.mainThread = new Handler(Looper.getMainLooper());
    }

    public static AppExecutors getInstance() {
        return INSTANCE;
    }

    public void executeCommand(Runnable runnable) {
        commandIO.execute(runnable);
    }

    public void executeScan(Runnable runnable) {
        scanIO.execute(runnable);
    }

    public void executeMonitor(Runnable runnable) {
        monitorIO.execute(runnable);
    }

    public void postToMainThread(Runnable runnable) {
        mainThread.post(runnable);
    }
}
