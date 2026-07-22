package com.gamelauncher.feature.network.domain.model

/**
 * NetworkPingResult — Encapsulates latency probe measurements to gaming endpoints.
 */
data class NetworkPingResult(
    val host: String,
    val latencyMs: Long,
    val isReachable: Boolean,
    val timestampMs: Long = System.currentTimeMillis()
)
