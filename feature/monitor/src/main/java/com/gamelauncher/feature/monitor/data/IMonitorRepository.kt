package com.gamelauncher.feature.monitor.data

import com.gamelauncher.feature.monitor.domain.model.FpsMetrics
import com.gamelauncher.feature.monitor.domain.model.SystemHardwareStats
import kotlinx.coroutines.flow.Flow

/**
 * IMonitorRepository — Contract for observing real-time system telemetry and frame rate statistics.
 */
interface IMonitorRepository {
    fun observeSystemHardwareStats(sampleIntervalMs: Long = 1000L): Flow<SystemHardwareStats>
    fun observeFpsMetrics(): Flow<FpsMetrics>
}
