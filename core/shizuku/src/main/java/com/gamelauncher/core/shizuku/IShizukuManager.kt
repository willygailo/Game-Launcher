package com.gamelauncher.core.shizuku

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining Shizuku manager contract for lifecycle and permission availability.
 */
interface IShizukuManager {
    val availability: StateFlow<ShizukuAvailability>
    fun isShizukuInstalled(): Boolean
    fun checkAvailability()
    fun requestPermission()
    fun isReady(): Boolean
    fun getUserService(): com.gamelauncher.core.shizuku.aidl.IShellCommandService?
    fun bindUserService()
    fun unbindUserService()
}
