package com.gamelauncher.core.permissions

/**
 * PermissionState — Sealed hierarchy modeling runtime permission states.
 */
sealed interface PermissionState {
    data object Granted : PermissionState
    data object Denied : PermissionState
    data object ShizukuRequired : PermissionState
}
