package com.gamebooster.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && context != null) {
            com.gamebooster.app.core.settings.SettingsStateRestorer.restoreAllSettings(context);
        }
    }
}
