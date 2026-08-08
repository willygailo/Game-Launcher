package com.gamebooster.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            com.gamebooster.app.core.settings.SettingsStateRestorer.restoreAllSettings(context);
            // Auto-start background game monitor service on boot/update so games are auto-detected without opening app
            com.gamebooster.app.gamespace.AutoGameMonitorService.start(context);
        }
    }
}
