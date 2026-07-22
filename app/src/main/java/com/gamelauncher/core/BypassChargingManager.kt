// app/src/main/java/com/gamelauncher/core/BypassChargingManager.kt
package com.gamelauncher.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BypassChargingManager — Non-root bypass charging state holder.
 */
@Singleton
class BypassChargingManager @Inject constructor(
    private val shellManager: ShizukuShellManager
) {

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isShellAvailable = MutableStateFlow(false)
    val isShellAvailable: StateFlow<Boolean> = _isShellAvailable.asStateFlow()

    suspend fun refreshAvailability() {
        _isShellAvailable.value = shellManager.isAvailable()
    }

    suspend fun setBypassCharging(enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        _isEnabled.value = enable
        enable
    }

    suspend fun isHardwareBypassActive(): Boolean = withContext(Dispatchers.IO) {
        _isEnabled.value
    }
}
