package com.gamebooster.app.gamespace;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.GameConfigPatcher;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;

/**
 * GameLaunchInterceptor — Intercepts game launches in real-time.
 * Pre-applies game-specific spoofing overrides, device_config performance modes,
 * and patches game configuration files before the game UI fully loads.
 */
public class GameLaunchInterceptor {

    private static final String TAG = "GameLaunchInterceptor";

    public static void preApplyForGame(Context context, String packageName) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) {
            return;
        }

        String pkg = packageName.trim();
        Log.i(TAG, "⚡ Pre-applying launch optimizations & graphics spoof for: " + pkg);

        try {
            int targetHz = 165;
            try {
                com.gamebooster.app.device.DevicePerformanceCapabilities caps =
                        com.gamebooster.app.device.DevicePerformanceCapabilities.detect(context);
                if (caps != null && caps.getMaxRefreshRate() > 0) {
                    targetHz = caps.getMaxRefreshRate();
                }
            } catch (Throwable ignored) {}

            // 1. Shizuku Game Mode & System Refresh Rate Override
            if (ShizukuExecutor.hasShizukuPermission()) {
                DeviceSpooferEngine.applyGameGraphicsSpoof(context, pkg, targetHz);
            }

            // 2. Patch game-specific configuration files (INI / JSON / Dat)
            GameConfigPatcher.applyGameFpsPatch(pkg, targetHz);

            Log.i(TAG, "✅ Launch interceptor completed cleanly for " + pkg);
        } catch (Throwable t) {
            Log.e(TAG, "Error running launch interceptor for " + pkg, t);
        }
    }
}
