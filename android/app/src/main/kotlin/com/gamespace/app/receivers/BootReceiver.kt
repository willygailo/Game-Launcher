package com.gamespace.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gamespace.app.utils.ShellExecutor
import org.json.JSONObject

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            if (ShellExecutor.isRootAvailable()) {
                val prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
                val activeTweaksJson = prefs.getString("flutter.active_boot_tweaks", null)

                if (!activeTweaksJson.isNullOrEmpty()) {
                    try {
                        val jsonObj = JSONObject(activeTweaksJson)
                        val keys = jsonObj.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = jsonObj.getString(key)
                            ShellExecutor.setSystemPropertyRoot(key, value)
                        }
                    } catch (e: Exception) {
                        applyFallbackTweaks()
                    }
                } else {
                    applyFallbackTweaks()
                }
            }
        }
    }

    private fun applyFallbackTweaks() {
        ShellExecutor.setSystemPropertyRoot("debug.composition.type", "gpu")
        ShellExecutor.setSystemPropertyRoot("debug.sf.hw", "1")
        ShellExecutor.setSystemPropertyRoot("debug.egl.hw", "1")
        ShellExecutor.setSystemPropertyRoot("windowsmgr.max_events_per_sec", "300")
    }
}

