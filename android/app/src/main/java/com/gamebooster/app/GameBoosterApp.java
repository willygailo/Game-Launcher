package com.gamebooster.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.ConfigBackupManager;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.games.HomeGameScanner;
import com.gamebooster.app.shizuku.RishManager;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.terminal.TerminalFolderManager;

/**
 * GameBoosterApp — Central Application Class.
 *
 * Provides safe application-wide initialization, global uncaught exception handling
 * to prevent crashes and startup ANRs, background service pre-warming (Rish, Shizuku AIDL,
 * Terminal Scripts, Game Library Scanner), and cold-start in-app identity spoofing restoration.
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

        // 2. Early safety initialization (App-private storage & backup paths)
        try {
            ConfigBackupManager.setAppContext(getApplicationContext());
        } catch (Throwable t) {
            Log.w(TAG, "ConfigBackupManager init error: " + t.getMessage());
        }

        // 3. Pre-warm background engines on dedicated worker thread (Zero Main-Thread Block)
        final Context appCtx = getApplicationContext();
        AppExecutors.getInstance().executeCommand(() -> {
            // A. Pre-warm Terminal Scripts & Folder structure
            try {
                TerminalFolderManager.getInstance(appCtx).initTerminalFolder();
            } catch (Throwable t) {
                Log.w(TAG, "TerminalFolderManager background warm-up error: " + t.getMessage());
            }

            // B. Extract and prepare Rish executable & dex
            try {
                RishManager.initialize(appCtx);
            } catch (Throwable t) {
                Log.w(TAG, "RishManager background initialization error: " + t.getMessage());
            }

            // C. Pre-bind Shizuku AIDL UserService
            try {
                ShizukuUserServiceConnector.getInstance().bindService();
            } catch (Throwable t) {
                Log.w(TAG, "ShizukuUserServiceConnector pre-bind error: " + t.getMessage());
            }

            // D. Cold-Start Identity Restoration: apply in-app reflection if spoofing is active
            try {
                if (SpoofPreferences.isSpoofEnabled(appCtx)) {
                    String activeId = SpoofPreferences.getActiveProfileId(appCtx);
                    if (activeId != null && !activeId.isEmpty()) {
                        SpoofProfile profile = DeviceSpooferEngine.getProfileById(activeId);
                        if (profile != null) {
                            DeviceSpooferEngine.applyInAppBuildSpoof(profile);
                            Log.i(TAG, "Cold-start spoof identity restored: " + profile.displayName);
                        }
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "Cold-start spoof restoration error: " + t.getMessage());
            }

            // E. Pre-scan installed target games for instant Home screen rendering
            try {
                HomeGameScanner.scanTargetGames(appCtx);
            } catch (Throwable t) {
                Log.w(TAG, "HomeGameScanner pre-cache error: " + t.getMessage());
            }
        });

        Log.i(TAG, "Game Booster Application initialized successfully.");
    }
}
