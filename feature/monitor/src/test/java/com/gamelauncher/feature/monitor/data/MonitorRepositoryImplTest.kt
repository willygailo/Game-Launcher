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

import com.gamelauncher.core.shizuku.IShellExecutor

class MonitorRepositoryImplTest {

    private val fakeShellExecutor = object : IShellExecutor {
        override suspend fun setPeakRefreshRate(hz: Float): Boolean = true
        override suspend fun setMinRefreshRate(hz: Float): Boolean = true
        override suspend fun setThermalOverride(disabled: Boolean): Boolean = true
        override suspend fun writeSetting(namespace: String, key: String, value: String): Boolean = true
        override suspend fun readSetting(namespace: String, key: String): String? = null
        override suspend fun setDeviceConfig(namespace: String, key: String, value: String): Boolean = true
        override suspend fun readDeviceConfig(namespace: String, key: String): String? = null
        override suspend fun grantPermission(packageName: String, permissionName: String): Boolean = true
        override suspend fun setAppOp(packageName: String, opName: String, mode: String): Boolean = true
        override suspend fun executeCommand(command: String): String? = null
    }

    @Test
    fun testObserveFpsMetrics_EmitsValidMetrics() = runTest {
        val mockContext = mock(Context::class.java)
        val repository = MonitorRepositoryImpl(
            context = mockContext,
            ioDispatcher = Dispatchers.Unconfined,
            shellExecutor = fakeShellExecutor
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
            ioDispatcher = Dispatchers.Unconfined,
            shellExecutor = fakeShellExecutor
        )

        val stats = repository.observeSystemHardwareStats(sampleIntervalMs = 100L).first()
        assertNotNull(stats)
        assertTrue((stats.cpuUsagePercent ?: 0f) >= 0f)
    }
}
