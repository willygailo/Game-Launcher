package com.gamelauncher.feature.monitor.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class MonitorRepositoryImplTest {

    @Test
    fun testObserveFpsMetrics_EmitsValidMetrics() = runTest {
        val mockContext = mock(Context::class.java)
        val repository = MonitorRepositoryImpl(
            context = mockContext,
            ioDispatcher = Dispatchers.Unconfined
        )

        val fps = repository.observeFpsMetrics().first()
        assertNotNull(fps)
        assertTrue(fps.currentFps in 30..120)
        assertEquals(60, fps.targetFps)
    }

    @Test
    fun testObserveSystemHardwareStats_EmitsStatsObject() = runTest {
        val mockContext = mock(Context::class.java)
        val repository = MonitorRepositoryImpl(
            context = mockContext,
            ioDispatcher = Dispatchers.Unconfined
        )

        val stats = repository.observeSystemHardwareStats(sampleIntervalMs = 100L).first()
        assertNotNull(stats)
        assertTrue(stats.cpuUsagePercent >= 0f)
    }
}
