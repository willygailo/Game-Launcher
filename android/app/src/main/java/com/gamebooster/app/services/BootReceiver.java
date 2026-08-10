package com.gamebooster.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.gamespace.AutoGameMonitorService;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;

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
                    GpuTweaksChannel.enableVulkanRenderer();
                    TouchLatencyChannel.enableUltraTouchResponse();

                    if (ShizukuExecutor.hasShizukuPermission()) {
                        ShizukuExecutor.grantAppPermissionsViaShizuku(context);
                    }

                    if (com.gamebooster.app.spoofer.SpoofPreferences.isSpoofEnabled(context)) {
                        String activeId = com.gamebooster.app.spoofer.SpoofPreferences.getActiveProfileId(context);
                        if (activeId != null) {
                            com.gamebooster.app.spoofer.SpoofProfile profile = com.gamebooster.app.spoofer.DeviceSpooferEngine.getProfileById(activeId);
                            if (profile != null) {
                                com.gamebooster.app.spoofer.DeviceSpooferEngine.applyProfile(context, profile, null);
                            }
                        }
                    }
                    AutoGameMonitorService.start(context);
                    Log.i(TAG, "Boot optimizations, Spoofer, and AutoGameMonitorService initialized cleanly!");
                } catch (Throwable t) {
                    Log.e(TAG, "BootReceiver initialization error", t);
                }
            });
        }
    }
}
