package com.gamebooster.app.platform.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gamebooster.app.feature.performance.booster.GpuTweaksChannel;
import com.gamebooster.app.feature.performance.booster.TouchLatencyChannel;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.feature.games.space.AutoGameMonitorService;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuManager;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || context == null) return;

        String action = intent.getAction();
        Log.i(TAG, "BootReceiver triggered with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            AppExecutors.getInstance().executeCommand(() -> {
                try {
                    ShizukuManager.registerBinderListeners();
                    ShizukuManager.attemptAutoStartShizuku(context);
                    GpuTweaksChannel.enableVulkanRenderer();
                    TouchLatencyChannel.enableUltraTouchResponse();

                    if (ShizukuExecutor.hasShizukuPermission()) {
                        ShizukuExecutor.grantAppPermissionsViaShizuku(context);
                        if (com.gamebooster.app.platform.shizuku.ForceApplyPreferences.isForceApplied(context)) {
                            int hz = com.gamebooster.app.platform.shizuku.ForceApplyPreferences.getAppliedTargetHz(context);
                            Log.i(TAG, "⚡ Re-executing ShizukuForceApplyEngine for " + hz + " Hz on boot!");
                            com.gamebooster.app.platform.shizuku.ShizukuForceApplyEngine.forceApplyAll(context, hz);
                        }
                    }

                    if (com.gamebooster.app.feature.spoofer.SpoofPreferences.isSpoofEnabled(context)) {
                        String activeId = com.gamebooster.app.feature.spoofer.SpoofPreferences.getActiveProfileId(context);
                        if (activeId != null) {
                            com.gamebooster.app.feature.spoofer.SpoofProfile profile = com.gamebooster.app.feature.spoofer.DeviceSpooferEngine.getProfileById(activeId);
                            if (profile != null) {
                                com.gamebooster.app.feature.spoofer.DeviceSpooferEngine.applyProfile(context, profile, null);
                            }
                        }
                    }
                    AutoGameMonitorService.start(context);
                    Log.i(TAG, "Boot optimizations, Shizuku Force Engine, Spoofer, and AutoGameMonitorService initialized cleanly!");
                } catch (Throwable t) {
                    Log.e(TAG, "BootReceiver initialization error", t);
                }
            });
        }
    }
}
