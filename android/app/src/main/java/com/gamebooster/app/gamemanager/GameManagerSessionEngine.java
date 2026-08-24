package com.gamebooster.app.gamemanager;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.config.GameConfigPatcher;
import com.gamebooster.app.config.GameConfigStorageAccessEngine;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.GameSessionSettings;
import com.gamebooster.app.config.NativeConfigInjector;
import com.gamebooster.app.engine.GameModeApiSupport;
import com.gamebooster.app.engine.NativeFrameworkBridge;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.gamespace.GameStateReverter;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.spoofer.HardwareMaskEngine;

/**
 * GameManagerSessionEngine — Manages the lifecycle of an active game session.
 *
 * Ensures all optimizations (Shizuku, Android API, hardware masking, storage unlock,
 * high refresh rate, and CPU/GPU performance governors) are enforced when a game starts,
 * and cleanly reverted to stock baseline when the game session ends.
 */
public final class GameManagerSessionEngine {

    private static final String TAG = "GameManagerSession";

    private GameManagerSessionEngine() {
    }

    /**
     * Begins an optimized game session for the target package.
     */
    public static void beginSession(Context context, String packageName) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) {
            return;
        }
        final String pkg = packageName.trim();
        final Context appContext = context.getApplicationContext();

        Log.i(TAG, "⚡ Starting GameManager Session for: " + pkg);

        // 1. Capture baseline session state
        int targetFps = GameProfilePreferences.getTargetHz(appContext, pkg);
        if (targetFps <= 0) targetFps = 185;
        GameSessionSettings.begin(appContext, pkg);
        GameManagerStatus.getInstance().setActiveSession(pkg);

        // 2. Grant full combo storage access
        GameConfigStorageAccessEngine.grantAllPathsAccess(appContext, pkg);

        // 3. Apply modern Android 13-16 performance API flags
        GameModeApiSupport.applyModernAndroidPerformanceFlags(pkg, targetFps);

        // 4. Enforce Hardware Device Masking for target game
        HardwareMaskEngine.maskPackage(appContext, pkg);

        // 5. Force High Hardware Display Refresh Rate
        try {
            MaxHzForceChannel.forceApply(targetFps);
            HzFpsChannel.forceSetRefreshRate(appContext, targetFps);
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                ShizukuUserServiceConnector.getInstance().forceDisplayRefreshRate(targetFps);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Refresh rate lock warning: " + t.getMessage());
        }

        // 6. Elevate CPU/GPU Performance Governors via AIDL/Shizuku
        try {
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                ShizukuUserServiceConnector.getInstance().setCpuGpuPerformanceGovernors();
                ShizukuUserServiceConnector.getInstance().applyThermalAndKernelBoost();
            }
            PerformanceChannel.applyProfile(appContext, PerformanceChannel.Profile.EXTREME_PERFORMANCE);
        } catch (Throwable t) {
            Log.w(TAG, "Performance channel warning: " + t.getMessage());
        }

        // 7. Acquire Sustained Performance Lock & Low-Latency WiFi Lock
        try {
            NativeFrameworkBridge.acquireSustainedPerformanceLock(appContext);
            NativeFrameworkBridge.acquireLowLatencyWifiLock(appContext);
            NativeFrameworkBridge.startAdpfSession(appContext, targetFps);
        } catch (Throwable t) {
            Log.w(TAG, "Native framework lock warning: " + t.getMessage());
        }

        // 8. Auto-patch game config files if configured
        try {
            GameConfigPatcher.applyGameFpsPatch(appContext, pkg, targetFps);
            GameProfileAutoConfigurator.autoConfigGamePackage(appContext, pkg, targetFps);
        } catch (Throwable t) {
            Log.w(TAG, "Config patch warning: " + t.getMessage());
        }

        // 9. DND & Network Turbo
        try {
            NetworkOptimizer.optimizeAllDataAndWifi(appContext);
            GameSpaceDndManager.setGamingDndMode(appContext, true);
        } catch (Throwable t) {
            Log.w(TAG, "DND/Network warning: " + t.getMessage());
        }

        // 10. Native CPU Core Pinning & Realtime Priority Scheduling
        try {
            com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                try {
                    Thread.sleep(1200);
                    String pidOut = com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand("pidof " + pkg);
                    if (pidOut != null && !pidOut.trim().isEmpty() && !pidOut.startsWith("ERROR")) {
                        String[] pids = pidOut.trim().split("\\s+");
                        for (String pStr : pids) {
                            try {
                                int pid = Integer.parseInt(pStr);
                                if (pid > 0) {
                                    NativeConfigInjector.setProcessCpuAffinity(pid, 0xf0);
                                    com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommands(
                                            "taskset -p f0 " + pid + " 2>/dev/null",
                                            "renice -n -20 -p " + pid + " 2>/dev/null"
                                    );
                                    Log.i(TAG, "Pinned Big/Prime CPU cores for PID: " + pid);
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                } catch (Throwable ignored) {}
            });
        } catch (Throwable t) {
            Log.w(TAG, "Core affinity warning: " + t.getMessage());
        }

        GameManagerStatus.getInstance().recordApply(18, "Game Session Activated for " + pkg + " @ " + targetFps + " FPS");
    }

    /**
     * Ends the active game session and restores baseline state.
     */
    public static void endSession(Context context, String packageName) {
        if (context == null) return;
        final Context appContext = context.getApplicationContext();

        Log.i(TAG, "Restoring baseline — ending GameManager Session for: " + packageName);
        GameStateReverter.revertToBaseline(appContext);
        GameManagerStatus.getInstance().setActiveSession(null);
    }

    /**
     * Checks if a session is currently active.
     */
    public static boolean isSessionActive(Context context) {
        if (context == null) return false;
        return GameSessionSettings.hasActiveSession(context) || GameManagerStatus.getInstance().hasActiveSession();
    }
}
