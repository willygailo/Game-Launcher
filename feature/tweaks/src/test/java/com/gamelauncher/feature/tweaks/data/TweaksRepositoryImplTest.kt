// feature/tweaks/src/test/java/com/gamelauncher/feature/tweaks/data/TweaksRepositoryImplTest.kt
package com.gamelauncher.feature.tweaks.data

import android.content.Context
import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.device.OemCapabilityMap
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.feature.tweaks.domain.model.TweakResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class TweaksRepositoryImplTest {

    @Test
    fun testGetAvailableTweaks_ReturnsNonEmptyList() = runTest {
        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun setPeakRefreshRate(hz: Float): Boolean = true
            override suspend fun setMinRefreshRate(hz: Float): Boolean = true
            override suspend fun setThermalOverride(disabled: Boolean): Boolean = true
            override suspend fun writeSetting(namespace: String, key: String, value: String): Boolean = true
            override suspend fun readSetting(namespace: String, key: String): String? = null
            override suspend fun setDeviceConfig(namespace: String, key: String, value: String): Boolean = true
            override suspend fun readDeviceConfig(namespace: String, key: String): String? = null
            override suspend fun grantPermission(packageName: String, permissionName: String): Boolean = true
            override suspend fun setAppOp(packageName: String, opName: String, mode: String): Boolean = true
        }

        val detector = DeviceProfileDetector()
        val capabilityMap = OemCapabilityMap(detector)
        val mockContext = Mockito.mock(Context::class.java)

        val secureSettingsRepo = com.gamelauncher.core.settings.SecureSettingsRepository(mockContext, fakeShellExecutor)

        val repository = TweaksRepositoryImpl(
            deviceProfileDetector = detector,
            capabilityMap = capabilityMap,
            shellExecutor = fakeShellExecutor,
            secureSettingsRepository = secureSettingsRepo,
            ioDispatcher = Dispatchers.Unconfined,
            context = mockContext
        )

        val tweaks = repository.getAvailableTweaks().first()
        assertNotNull(tweaks)
        assertTrue(tweaks.isNotEmpty())
    }

    @Test
    fun testApplyRefreshRateTweak_ConfirmedWhenReadBackMatches() = runTest {
        var writtenVal = ""
        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun setPeakRefreshRate(hz: Float): Boolean = true
            override suspend fun setMinRefreshRate(hz: Float): Boolean = true
            override suspend fun setThermalOverride(disabled: Boolean): Boolean = true
            override suspend fun writeSetting(namespace: String, key: String, value: String): Boolean {
                writtenVal = value
                return true
            }
            override suspend fun readSetting(namespace: String, key: String): String? = writtenVal
            override suspend fun setDeviceConfig(namespace: String, key: String, value: String): Boolean = true
            override suspend fun readDeviceConfig(namespace: String, key: String): String? = null
            override suspend fun grantPermission(packageName: String, permissionName: String): Boolean = true
            override suspend fun setAppOp(packageName: String, opName: String, mode: String): Boolean = true
        }

        val detector = DeviceProfileDetector()
        val capabilityMap = OemCapabilityMap(detector)
        val mockContext = Mockito.mock(Context::class.java)

        val secureSettingsRepo = com.gamelauncher.core.settings.SecureSettingsRepository(mockContext, fakeShellExecutor)

        val repository = TweaksRepositoryImpl(
            deviceProfileDetector = detector,
            capabilityMap = capabilityMap,
            shellExecutor = fakeShellExecutor,
            secureSettingsRepository = secureSettingsRepo,
            ioDispatcher = Dispatchers.Unconfined,
            context = mockContext
        )

        val result = repository.applyRefreshRateTweak(120f)
        assertEquals(TweakResult.Confirmed, result)
    }
}
