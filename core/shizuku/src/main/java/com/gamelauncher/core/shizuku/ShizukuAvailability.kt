package com.gamelauncher.core.shizuku

/**
 * State representing Shizuku service availability on the host device.
 */
sealed interface ShizukuAvailability {
    object NotInstalled : ShizukuAvailability
    object Stopped : ShizukuAvailability
    object PermissionDenied : ShizukuAvailability
    object Ready : ShizukuAvailability
}
