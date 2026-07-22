// core/shizuku/src/main/java/com/gamelauncher/core/shizuku/IShizukuManager.kt
package com.gamelauncher.core.shizuku

import com.gamelauncher.core.shizuku.aidl.IShellCommandService
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface defining Shizuku manager contract for lifecycle, binder death listeners, and permission availability.
 */
interface IShizukuManager {
    val state: StateFlow<ShizukuState>
    fun isShizukuInstalled(): Boolean
    fun checkAvailability()
    fun requestPermission()
    fun isReady(): Boolean
    fun getUserService(): IShellCommandService?
    fun bindUserService()
    fun unbindUserService()
    fun cleanup()
}
