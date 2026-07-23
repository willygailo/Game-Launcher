// feature/network/src/test/java/com/gamelauncher/feature/network/data/NetworkRepositoryImplTest.kt
package com.gamelauncher.feature.network.data

import android.content.Context
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.feature.network.domain.model.DnsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class NetworkRepositoryImplTest {

    @Test
    fun testGetAvailableDnsProviders_ReturnsDefaultList() {
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

        val mockContext = Mockito.mock(Context::class.java)
        val repository = NetworkRepositoryImpl(
            settingsRepository = SecureSettingsRepository(mockContext, fakeShellExecutor),
            ioDispatcher = Dispatchers.Unconfined
        )

        val providers = repository.getAvailableDnsProviders()
        assertNotNull(providers)
        assertTrue(providers.isNotEmpty())
        assertEquals("default", providers[0].id)
    }

    @Test
    fun testApplyPrivateDns_CustomHostname_SetsHostnameAndMode() = runTest {
        val writtenSettings = mutableMapOf<String, String>()

        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun setPeakRefreshRate(hz: Float): Boolean = true
            override suspend fun setMinRefreshRate(hz: Float): Boolean = true
            override suspend fun setThermalOverride(disabled: Boolean): Boolean = true
            override suspend fun writeSetting(namespace: String, key: String, value: String): Boolean {
                writtenSettings[key] = value
                return true
            }
            override suspend fun readSetting(namespace: String, key: String): String? = writtenSettings[key]
            override suspend fun setDeviceConfig(namespace: String, key: String, value: String): Boolean = true
            override suspend fun readDeviceConfig(namespace: String, key: String): String? = null
            override suspend fun grantPermission(packageName: String, permissionName: String): Boolean = true
            override suspend fun setAppOp(packageName: String, opName: String, mode: String): Boolean = true
        }

        val mockContext = Mockito.mock(Context::class.java)
        val repository = NetworkRepositoryImpl(
            settingsRepository = SecureSettingsRepository(mockContext, fakeShellExecutor),
            ioDispatcher = Dispatchers.Unconfined
        )

        val cloudflareDns = DnsProvider(
            id = "cloudflare",
            name = "Cloudflare 1.1.1.1",
            description = "Test DNS",
            hostname = "one.one.one.one"
        )

        val success = repository.applyPrivateDns(cloudflareDns)
        assertTrue(success)

        assertEquals("one.one.one.one", writtenSettings["private_dns_specifier"])
        assertEquals("hostname", writtenSettings["private_dns_mode"])
    }

    @Test
    fun testMeasureHostLatency_EmitsRequestedSamples() = runTest {
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

        val mockContext = Mockito.mock(Context::class.java)
        val repository = NetworkRepositoryImpl(
            settingsRepository = SecureSettingsRepository(mockContext, fakeShellExecutor),
            ioDispatcher = Dispatchers.Unconfined
        )

        val results = repository.measureHostLatency("127.0.0.1", samples = 2).toList()
        assertEquals(2, results.size)
    }
}

