package com.gamebooster.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.gamespace.AutoGameMonitorService;
import com.gamebooster.app.shizuku.ShizukuExecutor;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || context == null) return;

        String action = intent.getAction();
        Log.i(TAG, "BootReceiver triggered with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            // Re-apply low-latency GPU & touch performance tweaks
            try {
                GpuTweaksChannel.enableVulkanRenderer();
                TouchLatencyChannel.enableUltraTouchResponse();
                ShizukuExecutor.grantAppPermissionsViaShizuku(context);
                AutoGameMonitorService.start(context);
                Log.i(TAG, "Boot optimizations and AutoGameMonitorService initialized cleanly!");
            } catch (Throwable t) {
                Log.e(TAG, "BootReceiver initialization error", t);
            }
        }
    }
}
