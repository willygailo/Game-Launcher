package com.gamebooster.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.chipset.ChipsetOptimizerEngine;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.gamespace.AutoGameMonitorService;
import com.gamebooster.app.oem.OemBypassEngine;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.version.AndroidVersionOptimizer;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        final Context appContext = context.getApplicationContext();
        AppExecutors.getInstance().executeCommand(() -> {
            try {
                // 1. Basic booster channels
                GpuTweaksChannel.enableVulkanRenderer();
                TouchLatencyChannel.enableUltraTouchResponse();

                // 2. Enforce elevated Shizuku permissions & apply hardware optimizations
                ShizukuPermissionEnforcer.enforceAllPermissions(appContext);

                int targetHz = GameProfileAutoConfigurator.getTargetFpsHz(appContext);
                if (targetHz <= 0) targetHz = 185;

                ChipsetOptimizerEngine.applyChipsetOptimization(appContext, targetHz);
                OemBypassEngine.applyOemBypass(appContext, targetHz);
                AndroidVersionOptimizer.applyVersionOptimizations(appContext, null, targetHz);

                // 3. Start Foreground Auto Game Monitor Service
                Intent monitorIntent = new Intent(appContext, AutoGameMonitorService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(monitorIntent);
                } else {
                    appContext.startService(monitorIntent);
                }
            } catch (Throwable ignored) {}
        });
    }
}
