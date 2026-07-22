package com.gamelauncher.feature.tweaks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.feature.tweaks.data.ITweaksRepository
import com.gamelauncher.feature.tweaks.domain.model.TweakCategory
import com.gamelauncher.feature.tweaks.domain.model.TweakItem
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val repository: ITweaksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TweaksUiState>(TweaksUiState.Loading)
    val uiState: StateFlow<TweaksUiState> = _uiState.asStateFlow()

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

    fun applyRefreshRate(refreshRateHz: Float) {
        viewModelScope.launch {
            val success = repository.applyRefreshRateTweak(refreshRateHz)
            updateTweakState("refresh_rate", success, selectedValue = refreshRateHz.toInt().toString())
        }
    }

    fun applyCpuGovernor(governor: String) {
        viewModelScope.launch {
            val success = repository.applyCpuGovernorTweak(governor)
            updateTweakState("cpu_governor", success, selectedValue = governor)
        }
    }

    fun applyThermalThrottlingBypass(enable: Boolean) {
        viewModelScope.launch {
            val success = repository.applyThermalThrottlingBypass(enable)
            updateTweakState("thermal_bypass", success, toggleState = enable)
        }
    }

    fun applyGameModeBooster(enable: Boolean) {
        viewModelScope.launch {
            val success = repository.applyGameModeTweak(enable)
            updateTweakState("game_mode", success, toggleState = enable)
        }
    }

    private fun updateTweakState(
        tweakId: String,
        success: Boolean,
        selectedValue: String? = null,
        toggleState: Boolean? = null
    ) {
        val currentState = _uiState.value
        if (currentState is TweaksUiState.Success) {
            val updatedList = currentState.tweaks.map { tweak ->
                if (tweak.id == tweakId) {
                    tweak.copy(
                        isToggleActive = if (success && toggleState != null) toggleState else tweak.isToggleActive,
                        selectedValue = if (success && selectedValue != null) selectedValue else tweak.selectedValue,
                        lastApplySuccessful = success
                    )
                } else {
                    tweak
                }
            }
            val message = if (success) {
                "Applied tweak successfully"
            } else {
                "Failed to apply tweak via Shizuku"
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
