package com.gamebooster.app.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gamebooster.app.booster.CpuGovernorChannel;
import com.gamebooster.app.booster.EsportsAudioEnhancer;
import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.NetworkTweaksChannel;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.tweaks.TweakManagerRepository;

/**
 * OptimizationRestoreManager — Persists initial system state and provides a single-tap
 * Undo / Revert feature to restore display refresh rates, touch response, CPU governors,
 * network properties, audio settings, and spoof profiles back to device defaults.
 */
public class OptimizationRestoreManager {

    private static final String TAG = "OptimizationRestoreManager";
    private static final String PREF_NAME = "optimization_restore_backup_prefs";
    private static final String KEY_HAS_BACKUP = "has_backup_state";

    public static void backupCurrentSystemState(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_HAS_BACKUP, false)) return;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_HAS_BACKUP, true);
        editor.apply();
        Log.i(TAG, "Initial system state backed up for Undo/Restore capability.");
    }

    public static boolean restorePreviousSystemState(Context context) {
        if (context == null) return false;
        Context appContext = context.getApplicationContext();

        try {
            Log.i(TAG, "▶ Executing UNDO / RESTORE — Reverting all optimizations to system defaults...");

            // 1. Revert display refresh rate settings
            PerformanceChannel.revertRootTweaksScript();
            CommandExecutorExec.exec("settings delete system peak_refresh_rate");
            CommandExecutorExec.exec("settings delete system min_refresh_rate");
            CommandExecutorExec.exec("settings delete system user_refresh_rate");

            // 2. Revert CPU governor to standard schedutil
            CpuGovernorChannel.setGovernor("schedutil");

            // 3. Revert GPU & Vulkan / SurfaceFlinger render tweaks
            GpuTweaksChannel.disableVulkanRenderer();
            GpuTweaksChannel.disableForceMsaa();
            GpuTweaksChannel.setAngleMode(false);
            GpuTweaksChannel.setGameDriverMode(false);

            // 4. Revert Touch latency & animation scales
            TouchLatencyChannel.disableUltraTouchResponse();

            // 5. Revert Low latency network & TCP buffer tuning
            NetworkTweaksChannel.disableLowLatencyNetwork();

            // 6. Disable Footstep Audio EQ
            EsportsAudioEnhancer.setEsportsAudioMode(appContext, false);

            // 7. Disable Gaming DND & Restore Back Gestures
            GameSpaceDndManager.setGamingDndMode(appContext, false);
            GameSpaceDndManager.setBrightnessLock(appContext, false);

            // 8. Revert Device Identity Spoofing
            DeviceSpooferEngine.resetSpoofing(appContext);

            // 9. Revert Tweak Repository items
            TweakManagerRepository.revertAllTweaks(appContext);

            // 10. Record Undo execution log
            OptimizationLogRepository.addLog(appContext, new LogItem(
                    "Undo / Restore All Optimizations",
                    "System Defaults Restored",
                    true,
                    "Previous Game Optimizations",
                    "AOSP System Defaults",
                    null
            ));

            Log.i(TAG, "✔ UNDO / RESTORE Complete: All system settings reverted successfully.");
            return true;

        } catch (Throwable e) {
            Log.e(TAG, "Failed to restore previous system state", e);
            OptimizationLogRepository.addLog(appContext, new LogItem(
                    "Undo / Restore All Optimizations",
                    "System Restoration Failed",
                    false,
                    "Optimized State",
                    "AOSP System Defaults",
                    e.getMessage()
            ));
            return false;
        }
    }

    private static class CommandExecutorExec {
        static void exec(String cmd) {
            if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
                com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                com.gamebooster.app.engine.CommandExecutor.executeSystemCommand(cmd);
            }
        }
    }
}
