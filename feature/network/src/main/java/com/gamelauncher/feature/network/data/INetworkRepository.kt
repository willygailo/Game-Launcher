package com.gamelauncher.feature.network.data

import com.gamelauncher.feature.network.domain.model.DnsProvider
import com.gamelauncher.feature.network.domain.model.NetworkPingResult
import kotlinx.coroutines.flow.Flow

/**
 * INetworkRepository — Data repository contract for network latency probes and Private DNS switching.
 */
interface INetworkRepository {
    fun getAvailableDnsProviders(): List<DnsProvider>
    suspend fun getActivePrivateDnsHostname(): String?
    suspend fun applyPrivateDns(provider: DnsProvider): Boolean
    fun measureHostLatency(host: String = "1.1.1.1", samples: Int = 3): Flow<NetworkPingResult>
}
