package com.gamespace.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gamespace.app.utils.ShellExecutor

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-apply critical non-persist gaming tweaks at device startup if root is available
            if (ShellExecutor.isRootAvailable()) {
                ShellExecutor.setSystemPropertyRoot("debug.composition.type", "gpu")
                ShellExecutor.setSystemPropertyRoot("debug.sf.hw", "1")
                ShellExecutor.setSystemPropertyRoot("debug.egl.hw", "1")
                ShellExecutor.setSystemPropertyRoot("windowsmgr.max_events_per_sec", "300")
            }
        }
    }
}
