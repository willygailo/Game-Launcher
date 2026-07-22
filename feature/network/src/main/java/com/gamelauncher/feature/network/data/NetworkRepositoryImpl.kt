package com.gamelauncher.feature.network.data

import com.gamelauncher.core.di.IoDispatcher
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.settings.SettingsKeys
import com.gamelauncher.feature.network.domain.model.DnsProvider
import com.gamelauncher.feature.network.domain.model.NetworkPingResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

/**
 * NetworkRepositoryImpl — Manages low-latency Private DNS switching via SecureSettingsRepository
 * and performs non-privileged socket RTT latency measurements to gaming endpoints.
 */
class NetworkRepositoryImpl @Inject constructor(
    private val settingsRepository: SecureSettingsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : INetworkRepository {

    companion object {
        private const val PRIVATE_DNS_MODE_KEY = "private_dns_mode"
        private const val PRIVATE_DNS_SPECIFIER_KEY = "private_dns_specifier"
    }

    override fun getAvailableDnsProviders(): List<DnsProvider> = DnsProvider.DEFAULT_PROVIDERS

    override suspend fun getActivePrivateDnsHostname(): String? = withContext(ioDispatcher) {
        settingsRepository.getString(SettingsKeys.Scope.GLOBAL, PRIVATE_DNS_SPECIFIER_KEY)
    }

    override suspend fun applyPrivateDns(provider: DnsProvider): Boolean = withContext(ioDispatcher) {
        if (provider.isSystemDefault || provider.hostname == null) {
            // Restore standard Android automatic Private DNS mode ("opportunistic")
            val modeResult = settingsRepository.putString(
                scope = SettingsKeys.Scope.GLOBAL,
                key = PRIVATE_DNS_MODE_KEY,
                value = "opportunistic"
            )
            settingsRepository.putString(
                scope = SettingsKeys.Scope.GLOBAL,
                key = PRIVATE_DNS_SPECIFIER_KEY,
                value = ""
            )
            modeResult
        } else {
            // Set custom Private DNS TLS hostname ("hostname")
            val specifierResult = settingsRepository.putString(
                scope = SettingsKeys.Scope.GLOBAL,
                key = PRIVATE_DNS_SPECIFIER_KEY,
                value = provider.hostname
            )
            val modeResult = settingsRepository.putString(
                scope = SettingsKeys.Scope.GLOBAL,
                key = PRIVATE_DNS_MODE_KEY,
                value = "hostname"
            )
            specifierResult && modeResult
        }
    }

    override fun measureHostLatency(host: String, samples: Int): Flow<NetworkPingResult> = flow {
        repeat(samples) {
            val pingResult = probeSocketLatency(host)
            emit(pingResult)
            if (it < samples - 1) {
                delay(300L)
            }
        }
    }.flowOn(ioDispatcher)

    private fun probeSocketLatency(host: String, port: Int = 53, timeoutMs: Int = 2000): NetworkPingResult {
        val startTime = System.currentTimeMillis()
        var isReachable = false
        var latencyMs = -1L

        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                val endTime = System.currentTimeMillis()
                latencyMs = endTime - startTime
                isReachable = true
            }
        } catch (_: Exception) {
            isReachable = false
            latencyMs = -1L
        }

        return NetworkPingResult(
            host = host,
            latencyMs = if (isReachable) latencyMs else -1L,
            isReachable = isReachable
        )
    }
}
