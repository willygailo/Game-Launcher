// feature/tweaks/src/main/java/com/gamelauncher/feature/tweaks/ui/TweaksViewModel.kt
package com.gamelauncher.feature.tweaks.ui

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.shizuku.ShizukuAvailability
import com.gamelauncher.core.shizuku.ShizukuState
import com.gamelauncher.feature.tweaks.data.ITweaksRepository
import com.gamelauncher.feature.tweaks.domain.model.TweakItem
import com.gamelauncher.feature.tweaks.domain.model.TweakResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TweaksUiState {
    object Loading : TweaksUiState
    data class Success(
        val tweaks: List<TweakItem>,
        val userMessage: String? = null
    ) : TweaksUiState
    data class Error(val message: String) : TweaksUiState
}

@HiltViewModel
class TweaksViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ITweaksRepository,
    private val shizukuAvailability: ShizukuAvailability
) : ViewModel() {

    private val _uiState = MutableStateFlow<TweaksUiState>(TweaksUiState.Loading)
    val uiState: StateFlow<TweaksUiState> = _uiState.asStateFlow()

    private val _activeArmouryMode = MutableStateFlow("Dynamic")
    val activeArmouryMode: StateFlow<String> = _activeArmouryMode.asStateFlow()

    val shizukuState: StateFlow<ShizukuState> = shizukuAvailability.state


    init {
        loadTweaks()
    }

    fun loadTweaks() {
        viewModelScope.launch {
            repository.getAvailableTweaks()
                .onStart { _uiState.value = TweaksUiState.Loading }
                .catch { throwable ->
                    _uiState.value = TweaksUiState.Error(throwable.localizedMessage ?: "Failed to load tweaks")
                }
                .collect { list ->
                    _uiState.value = TweaksUiState.Success(tweaks = list)
                }
        }
    }

    private fun checkExecutionPermitted(): Boolean {
        val shizukuActive = shizukuAvailability.isReady
        val hasWriteSecure = context.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") == PackageManager.PERMISSION_GRANTED
        if (!shizukuActive && !hasWriteSecure) {
            val msg = "Notice: Running in standard non-rooted mode. Grant WRITE_SECURE_SETTINGS via ADB or start Shizuku for deep system tweaks."
            val currentState = _uiState.value
            if (currentState is TweaksUiState.Success) {
                _uiState.value = currentState.copy(userMessage = msg)
            }
        }
        return true
    }


    fun applyRogArmouryMode(modeName: String) {
        if (!checkExecutionPermitted()) return
        _activeArmouryMode.value = modeName
        viewModelScope.launch {
            val result = repository.applyRogArmouryMode(modeName)
            handleTweakResult("rog_armoury_mode", result, selectedValue = modeName)
        }
    }


    fun applyTouchUltra(enable: Boolean) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.applyTouchUltraTweaks(enable)
            handleTweakResult("touch_ultra", result, toggleState = enable)
        }
    }

    fun applySuperFastLaunch() {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.applySuperFastGameLaunch()
            handleTweakResult("super_fast_launch", result)
        }
    }

    fun applyRefreshRate(refreshRateHz: Float) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.applyRefreshRateTweak(refreshRateHz)
            handleTweakResult("refresh_rate", result, selectedValue = refreshRateHz.toInt().toString())
        }
    }

    fun applyFpsUnlock(fpsTarget: String) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.applyFpsUnlockTweak(fpsTarget)
            handleTweakResult("fps_unlock", result, selectedValue = fpsTarget)
        }
    }

    fun clearHighRefreshRateBlacklist() {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.clearHighRefreshRateBlacklist()
            handleTweakResult("high_refresh_rate_blacklist", result)
        }
    }

    fun applyGpuRendering(enable: Boolean) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.applyGpuRenderingTweak(enable)
            handleTweakResult("gpu_rendering", result, toggleState = enable)
        }
    }

    fun clearGameDriverConfig() {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.clearGameDriverConfig()
            handleTweakResult("game_driver_clear", result)
        }
    }

    fun applyCpuPerformanceBoost(enable: Boolean) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.applyCpuPerformanceBoost(enable)
            handleTweakResult("cpu_performance", result, toggleState = enable)
        }
    }

    fun applyThermalThrottlingBypass(enable: Boolean) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.applyThermalThrottlingBypass(enable)
            handleTweakResult("thermal_bypass", result, toggleState = enable)
        }
    }

    fun applyGameModeBooster(enable: Boolean) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.applyGameModeTweak(enable)
            handleTweakResult("game_mode", result, toggleState = enable)
        }
    }

    fun applyNetworkSpeedBoost(enable: Boolean) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.applyNetworkSpeedBoost(enable)
            handleTweakResult("network_speed", result, toggleState = enable)
        }
    }

    fun disablePhantomProcessKilling(disable: Boolean) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.disablePhantomProcessKilling(disable)
            handleTweakResult("phantom_procs", result, toggleState = disable)
        }
    }

    fun disableAdaptiveBattery(disable: Boolean) {
        if (!checkExecutionPermitted()) return
        viewModelScope.launch {
            val result = repository.disableAdaptiveBattery(disable)
            handleTweakResult("adaptive_battery", result, toggleState = disable)
        }
    }

    private fun handleTweakResult(
        tweakId: String,
        result: TweakResult,
        selectedValue: String? = null,
        toggleState: Boolean? = null
    ) {
        val currentState = _uiState.value
        if (currentState is TweaksUiState.Success) {
            val message = when (result) {
                is TweakResult.Confirmed -> "Applied tweak successfully."
                is TweakResult.SilentlyIgnored -> "Notice: Setting '${result.key}' was modified or stored locally."
                is TweakResult.Failed -> "Failed to apply tweak: ${result.reason}"
            }

            val updatedList = currentState.tweaks.map { tweak ->
                if (tweak.id == tweakId) {
                    tweak.copy(
                        isToggleActive = if (toggleState != null) toggleState else tweak.isToggleActive,
                        selectedValue = if (selectedValue != null) selectedValue else tweak.selectedValue,
                        lastResult = result
                    )
                } else {
                    tweak
                }
            }

            _uiState.value = TweaksUiState.Success(tweaks = updatedList, userMessage = message)
        }
    }

    fun clearUserMessage() {
        val currentState = _uiState.value
        if (currentState is TweaksUiState.Success) {
            _uiState.value = currentState.copy(userMessage = null)
        }
    }
}
