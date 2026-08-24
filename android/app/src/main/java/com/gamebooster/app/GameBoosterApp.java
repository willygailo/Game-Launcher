package com.gamebooster.app;

import android.app.Application;
import android.util.Log;

import com.gamebooster.app.config.ConfigBackupManager;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.terminal.TerminalFolderManager;

/**
 * GameBoosterApp — Central Application Class.
 *
 * Provides safe application-wide initialization, global uncaught exception handling
 * to prevent crashes and startup ANRs, and background service pre-warming.
 */
public class GameBoosterApp extends Application {

    private static final String TAG = "GameBoosterApp";
    private static GameBoosterApp sInstance;

    public static GameBoosterApp getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // 1. Global crash protection & logging: install CrashLog early
        try {
            com.gamebooster.app.diagnostics.CrashLog.install(this);
        } catch (Throwable t) {
            Log.w(TAG, "CrashLog install error: " + t.getMessage());
        }

        // 2. Early safety initialization (App-private storage & executors)
        try {
            ConfigBackupManager.setAppContext(getApplicationContext());
        } catch (Throwable t) {
            Log.w(TAG, "ConfigBackupManager init error: " + t.getMessage());
        }

        // 3. Pre-warm terminal folder and background engine in worker thread (Zero Main-Thread Block)
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                TerminalFolderManager.getInstance(getApplicationContext()).initTerminalFolder();
            } catch (Throwable t) {
                Log.w(TAG, "TerminalFolderManager background warm-up error: " + t.getMessage());
            }
        });

        Log.i(TAG, "Game Booster Application initialized successfully.");
    }
}
