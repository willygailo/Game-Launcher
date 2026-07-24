package com.gamelauncher.feature.monitor.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.core.settings.SettingsPreferences
import com.gamelauncher.feature.monitor.data.IMonitorRepository
import com.gamelauncher.feature.monitor.domain.model.FpsMetrics
import com.gamelauncher.feature.monitor.domain.model.SystemHardwareStats
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    private val repository: IMonitorRepository,
    private val settingsPreferences: SettingsPreferences,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<MonitorUiState>(MonitorUiState.Loading)
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    private var cachedOverlayEnabled: Boolean = false

    init {
        observeOverlayPreference()
        startTelemetryObservability()
    }

    private fun observeOverlayPreference() {
        viewModelScope.launch {
            settingsPreferences.isOverlayEnabled.collect { enabled ->
                cachedOverlayEnabled = enabled
                val currentState = _uiState.value
                if (currentState is MonitorUiState.Success) {
                    _uiState.value = currentState.copy(isOverlayEnabled = enabled)
                }
            }
        }
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
        val isOverlay = if (currentState is MonitorUiState.Success) currentState.isOverlayEnabled else cachedOverlayEnabled
        _uiState.value = MonitorUiState.Success(
            fpsMetrics = fps,
            hardwareStats = stats,
            isOverlayEnabled = isOverlay
        )
    }

    fun toggleOverlay(enabled: Boolean) {
        cachedOverlayEnabled = enabled
        val currentState = _uiState.value
        if (currentState is MonitorUiState.Success) {
            _uiState.value = currentState.copy(isOverlayEnabled = enabled)
        }
        viewModelScope.launch {
            settingsPreferences.setOverlayEnabled(enabled)
            runCatching {
                val overlayClass = Class.forName("com.gamelauncher.services.OverlayService")
                val intent = Intent(context, overlayClass)
                if (enabled) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                        context.startService(intent)
                    }
                } else {
                    intent.action = "com.gamelauncher.services.OverlayService.ACTION_STOP_OVERLAY"
                    context.startService(intent)
                }
            }
        }
    }
}
