package com.gamelauncher.core

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat

fun Context.startManagedService(intent: Intent) {
    val action = intent.action.orEmpty()
    val shouldUseRegularService = Build.VERSION.SDK_INT < Build.VERSION_CODES.O || action.contains("STOP")

    runCatching {
        if (shouldUseRegularService) {
            startService(intent)
        } else {
            ContextCompat.startForegroundService(this, intent)
        }
    }.recoverCatching {
        startService(intent)
    }
}
