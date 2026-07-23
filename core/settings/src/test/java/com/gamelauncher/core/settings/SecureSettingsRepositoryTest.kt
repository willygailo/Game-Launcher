// core/settings/src/test/java/com/gamelauncher/core/settings/SecureSettingsRepositoryTest.kt
package com.gamelauncher.core.settings

import android.content.Context
import com.gamelauncher.core.shizuku.IShellExecutor
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureSettingsRepositoryTest {

    @Test
    fun testSettingsKeys_ConstantsFormat() {
        assertEquals("window_animation_scale", SettingsKeys.WINDOW_ANIMATION_SCALE)
        assertEquals("transition_animation_scale", SettingsKeys.TRANSITION_ANIMATION_SCALE)
        assertEquals("animator_duration_scale", SettingsKeys.ANIMATOR_DURATION_SCALE)
        assertEquals("high_performance_mode", SettingsKeys.HIOS_HIGH_PERFORMANCE_MODE)
        assertEquals("global", SettingsKeys.Scope.GLOBAL.namespace)
        assertEquals("system", SettingsKeys.Scope.SYSTEM.namespace)
        assertEquals("secure", SettingsKeys.Scope.SECURE.namespace)
    }

    @Test
    fun testSecureSettingsRepository_PutAndGetFloat() = runTest {
        val settingsMap = mutableMapOf<String, String>()

        val fakeExecutor = object : IShellExecutor {
            override suspend fun setPeakRefreshRate(hz: Float): Boolean = true
            override suspend fun setMinRefreshRate(hz: Float): Boolean = true
            override suspend fun setThermalOverride(disabled: Boolean): Boolean = true
            override suspend fun writeSetting(namespace: String, key: String, value: String): Boolean {
                settingsMap["$namespace:$key"] = value
                return true
            }
            override suspend fun readSetting(namespace: String, key: String): String? {
                return settingsMap["$namespace:$key"]
            }
            override suspend fun setDeviceConfig(namespace: String, key: String, value: String): Boolean = true
            override suspend fun readDeviceConfig(namespace: String, key: String): String? = null
            override suspend fun grantPermission(packageName: String, permissionName: String): Boolean = true
            override suspend fun setAppOp(packageName: String, opName: String, mode: String): Boolean = true
        }

        val mockContext = org.mockito.Mockito.mock(Context::class.java)
        val repository = SecureSettingsRepository(mockContext, fakeExecutor)

        val writeSuccess = repository.putFloat(SettingsKeys.Scope.GLOBAL, "window_animation_scale", 0.5f)
        assertTrue(writeSuccess)

        val readValue = repository.getFloat(SettingsKeys.Scope.GLOBAL, "window_animation_scale", 1.0f)
        assertEquals(0.5f, readValue, 0.001f)
    }
}
