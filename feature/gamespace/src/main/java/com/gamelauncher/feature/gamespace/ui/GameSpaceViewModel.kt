package com.gamelauncher.feature.gamespace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.device.OemBrand
import com.gamelauncher.core.oemflags.OemFlag
import com.gamelauncher.core.oemflags.OemFlagProbeEngine
import com.gamelauncher.core.shizuku.IShizukuManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.gamelauncher.core.shizuku.ShizukuState
import com.gamelauncher.core.shizuku.ShizukuStateRepository

data class GameSpaceUiState(
    val detectedOemBrand: OemBrand = OemBrand.GENERIC,
    val isShizukuReady: Boolean = false,
    val isLoading: Boolean = true,
    val flags: List<OemFlag> = emptyList(),
    val activeProfileMode: String = "TURBO",
    val showResetConfirmationDialog: Boolean = false,
    val statusMessage: String? = null
)

@HiltViewModel
class GameSpaceViewModel @Inject constructor(
    private val detector: DeviceProfileDetector,
    private val probeEngine: OemFlagProbeEngine,
    private val shizukuStateRepository: ShizukuStateRepository,
    private val shizukuManager: IShizukuManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameSpaceUiState())
    val uiState: StateFlow<GameSpaceUiState> = _uiState.asStateFlow()

    init {
        observeShizukuState()
        refreshState()
    }

    private fun observeShizukuState() {
        viewModelScope.launch {
            shizukuStateRepository.state.collect { state ->
                _uiState.value = _uiState.value.copy(
                    isShizukuReady = state is ShizukuState.Connected
                )
            }
        }
    }


    fun refreshState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                detectedOemBrand = detector.detectOemBrand(),
                isShizukuReady = shizukuStateRepository.isConnected,
                isLoading = true
            )
            val probedFlags = probeEngine.probeDeviceFlags()
            _uiState.value = _uiState.value.copy(
                flags = probedFlags,
                isLoading = false
            )
        }
    }

    fun requestShizukuPermission() {
        try {
            shizukuManager.requestPermission()
        } catch (_: Exception) {}
    }



    fun toggleFlag(flag: OemFlag, enable: Boolean) {
        viewModelScope.launch {
            val success = probeEngine.applyFlagState(flag, enable)
            if (success) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Applied ${flag.title}"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Failed to update ${flag.title}"
                )
            }
        }
    }

    fun promptResetAllTweaks() {
        _uiState.value = _uiState.value.copy(showResetConfirmationDialog = true)
    }

    fun dismissResetDialog() {
        _uiState.value = _uiState.value.copy(showResetConfirmationDialog = false)
    }

    fun confirmResetAllTweaks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(showResetConfirmationDialog = false, isLoading = true)
            val ok = probeEngine.resetAllFlags()
            val refreshed = probeEngine.probeDeviceFlags()
            _uiState.value = _uiState.value.copy(
                flags = refreshed,
                isLoading = false,
                statusMessage = if (ok) "Reverted all flags to pre-boost baseline snapshot" else "Reset completed with warnings"
            )
        }
    }

    fun selectProfileMode(mode: String) {
        _uiState.value = _uiState.value.copy(activeProfileMode = mode)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }
}
