package com.gamelauncher.feature.network.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gamelauncher.feature.network.data.INetworkRepository
import com.gamelauncher.feature.network.domain.model.DnsProvider
import com.gamelauncher.feature.network.domain.model.NetworkPingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gamelauncher.core.shizuku.ShizukuState
import com.gamelauncher.core.shizuku.ShizukuStateRepository

sealed interface NetworkUiState {
    object Loading : NetworkUiState
    data class Success(
        val dnsProviders: List<DnsProvider>,
        val activeHostname: String?,
        val latestPing: NetworkPingResult? = null,
        val isPinging: Boolean = false,
        val message: String? = null
    ) : NetworkUiState
    data class Error(val message: String) : NetworkUiState
}

@HiltViewModel

class NetworkViewModel @Inject constructor(
    private val repository: INetworkRepository,
    private val shizukuStateRepository: ShizukuStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NetworkUiState>(NetworkUiState.Loading)
    val uiState: StateFlow<NetworkUiState> = _uiState.asStateFlow()

    val shizukuState: StateFlow<ShizukuState> = shizukuStateRepository.state


    init {
        loadNetworkState()
    }

    fun loadNetworkState() {
        viewModelScope.launch {
            try {
                val providers = repository.getAvailableDnsProviders()
                val activeHostname = repository.getActivePrivateDnsHostname()
                _uiState.value = NetworkUiState.Success(
                    dnsProviders = providers,
                    activeHostname = activeHostname
                )
                runPingProbe("1.1.1.1")
            } catch (e: Exception) {
                _uiState.value = NetworkUiState.Error(e.localizedMessage ?: "Failed to initialize network feature")
            }
        }
    }

    fun applyDnsProvider(provider: DnsProvider) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is NetworkUiState.Success) {
                val success = repository.applyPrivateDns(provider)
                val newActiveHostname = if (success) provider.hostname else currentState.activeHostname
                val userMsg = if (success) {
                    "Applied ${provider.name} successfully"
                } else {
                    "Failed to set Private DNS via Shizuku"
                }
                _uiState.value = currentState.copy(
                    activeHostname = newActiveHostname,
                    message = userMsg
                )
                if (success) {
                    val probeHost = provider.hostname ?: "1.1.1.1"
                    runPingProbe(probeHost)
                }
            }
        }
    }

    fun runPingProbe(host: String = "1.1.1.1") {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is NetworkUiState.Success) {
                _uiState.value = currentState.copy(isPinging = true)
                repository.measureHostLatency(host = host, samples = 3).collect { pingResult ->
                    val latestState = _uiState.value
                    if (latestState is NetworkUiState.Success) {
                        _uiState.value = latestState.copy(
                            latestPing = pingResult,
                            isPinging = false
                        )
                    }
                }
            }
        }
    }

    fun clearMessage() {
        val currentState = _uiState.value
        if (currentState is NetworkUiState.Success) {
            _uiState.value = currentState.copy(message = null)
        }
    }
}
