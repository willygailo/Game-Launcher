package com.gamespace.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.gamespace.app.utils.ShellExecutor;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (ShellExecutor.isRootAvailable()) {
                ShellExecutor.executeCommand("setprop debug.composition.type gpu", true);
                ShellExecutor.executeCommand("setprop debug.sf.hw 1", true);
                ShellExecutor.executeCommand("setprop windowsmgr.max_events_per_sec 300", true);
            }
        }
    }
}
