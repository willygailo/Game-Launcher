package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.io.File;

public class GameConfigAutoEngine {

    private static final String TAG = "GameConfigAutoEngine";

    public static void autoApplyGameConfigAsync(Context context, String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                autoApplyGameConfigInternal(context, packageName);
            } catch (Throwable e) {
                Log.e(TAG, "Failed auto-forcing config for " + packageName, e);
            }
        });
    }

    private static void autoApplyGameConfigInternal(Context context, String packageName) {
        TargetConfigPathResolver.TargetPathInfo target = TargetConfigPathResolver.resolveTargetPath(packageName);
        Log.i(TAG, "⚡ Auto-Forcing Game Config for [" + packageName + "] -> Target: " + target.primaryPath);

        // 1. Shizuku Shell Directory Setup
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand("mkdir -p " + target.primaryPath);
            for (String alt : target.alternativePaths) {
                ShizukuExecutor.executeShizukuCommand("mkdir -p " + alt);
            }
        }

        // 2. Check Local User Custom Config Directory (/sdcard/GameLauncherPro/configs/<pkg>/)
        File localCustomDir = new File("/sdcard/GameLauncherPro/configs/" + packageName + "/");
        if (localCustomDir.exists() && localCustomDir.isDirectory() && ShizukuExecutor.hasShizukuPermission()) {
            File[] patchFiles = localCustomDir.listFiles();
            if (patchFiles != null) {
                for (File patch : patchFiles) {
                    if (patch.isFile()) {
                        String dest = target.primaryPath + patch.getName();
                        ShizukuExecutor.executeShizukuCommand("cp -f " + patch.getAbsolutePath() + " " + dest);
                        ShizukuExecutor.executeShizukuCommand("chmod 666 " + dest);
                        Log.i(TAG, "Injected custom user patch: " + patch.getName() + " -> " + dest);
                    }
                }
            }
        } else if (ShizukuExecutor.hasShizukuPermission()) {
            // 3. Inject High-Performance Default Config File if none exists
            String primaryFile = target.primaryPath + target.primaryConfigFile;
            String defaultCfgContent = "[GamePerformance]\nMaxFps=120\nTargetHz=120\nGraphicQuality=3\nSuperTouchBoost=1\nAntiAliasing=0\nShadows=0\n";
            ShizukuExecutor.executeShizukuCommand("echo \"" + defaultCfgContent + "\" > " + primaryFile);
            ShizukuExecutor.executeShizukuCommand("chmod 666 " + primaryFile);
        }

        // 4. Force System ANGLE Driver & 120Hz Hardware Override
        if (ShizukuExecutor.hasShizukuPermission()) {
            ShizukuExecutor.executeShizukuCommand("settings put global angle_gl_driver_selection_pkgs " + packageName);
            ShizukuExecutor.executeShizukuCommand("settings put global angle_gl_driver_selection_values angle");
            ShizukuExecutor.executeShizukuCommand("cmd game mode performance " + packageName);
            ShizukuExecutor.executeShizukuCommand("setprop debug.angle.backend vulkan");
        }

        Log.i(TAG, "✅ Auto-Forcing Game Config Completed Successfully for: " + packageName);
    }
}
