package com.gamelauncher.feature.monitor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.feature.monitor.data.IMonitorRepository
import com.gamelauncher.feature.monitor.domain.model.FpsMetrics
import com.gamelauncher.feature.monitor.domain.model.SystemHardwareStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MonitorUiState {
    object Loading : MonitorUiState
    data class Success(
        val fpsMetrics: FpsMetrics,
        val hardwareStats: SystemHardwareStats,
        val isOverlayEnabled: Boolean = false
    ) : MonitorUiState
    data class Error(val message: String) : MonitorUiState
}

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val repository: IMonitorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MonitorUiState>(MonitorUiState.Loading)
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    init {
        startTelemetryObservability()
    }

    fun startTelemetryObservability() {
        viewModelScope.launch {
            try {
                var currentFps = FpsMetrics(currentFps = 60)
                var currentStats = SystemHardwareStats(
                    cpuUsagePercent = 0f,
                    ramUsedMb = 0L,
                    ramTotalMb = 1000L,
                    batteryTemperatureCelsius = 32f,
                    batteryLevelPercent = 100
                )

                launch {
                    repository.observeFpsMetrics().collect { fps ->
                        currentFps = fps
                        updateState(currentFps, currentStats)
                    }
                }

                launch {
                    repository.observeSystemHardwareStats().collect { stats ->
                        currentStats = stats
                        updateState(currentFps, currentStats)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MonitorUiState.Error(e.localizedMessage ?: "Failed to initialize hardware monitor")
            }
        }
    }

    private fun updateState(fps: FpsMetrics, stats: SystemHardwareStats) {
        val currentState = _uiState.value
        val isOverlay = if (currentState is MonitorUiState.Success) currentState.isOverlayEnabled else false
        _uiState.value = MonitorUiState.Success(
            fpsMetrics = fps,
            hardwareStats = stats,
            isOverlayEnabled = isOverlay
        )
    }

    fun toggleOverlay(enabled: Boolean) {
        val currentState = _uiState.value
        if (currentState is MonitorUiState.Success) {
            _uiState.value = currentState.copy(isOverlayEnabled = enabled)
        }
    }
}
