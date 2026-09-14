package com.gamebooster.app.shizuku

/**
 * ShizukuStatus — Comprehensive immutable snapshot of Shizuku binder,
 * permission, connection lifecycle, network mode, and engine state.
 */
data class ShizukuStatus(
    val isInstalled: Boolean = false,
    val isBinderAlive: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val isOnline: Boolean = false,
    val appStatus: AppStatus = AppStatus.RUNNING
) {
    enum class ConnectionState(val label: String) {
        CONNECTED("Connected"),
        ACTIVE("Active"),
        CONNECTING("Connecting"),
        DEAD("Dead"),
        DISCONNECTED("Disconnected")
    }

    enum class AppStatus(val label: String) {
        RUNNING("Running"),
        BOOSTING("Boosting Active"),
        PAUSED("Paused"),
        STANDBY("Standby")
    }

    val isServiceRunning: Boolean
        get() = isBinderAlive

    val statusSummary: String
        get() = when {
            isBinderAlive && isPermissionGranted -> "Active"
            isBinderAlive -> "Connected (Permission Needed)"
            isInstalled -> "Installed (Service Stopped)"
            else -> "Not Installed"
        }
}
