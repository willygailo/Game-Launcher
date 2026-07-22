package com.gamelauncher.feature.network.data

import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.shizuku.ShellResult
import com.gamelauncher.feature.network.domain.model.DnsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkRepositoryImplTest {

    @Test
    fun testGetAvailableDnsProviders_ReturnsDefaultList() {
        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult = ShellResult(0, "")
            override suspend fun executeArgs(vararg args: String, timeoutMs: Long): ShellResult = ShellResult(0, "")
        }

        val repository = NetworkRepositoryImpl(
            settingsRepository = SecureSettingsRepository(fakeShellExecutor),
            ioDispatcher = Dispatchers.Unconfined
        )

        val providers = repository.getAvailableDnsProviders()
        assertNotNull(providers)
        assertTrue(providers.isNotEmpty())
        assertEquals("default", providers[0].id)
    }

    @Test
    fun testApplyPrivateDns_CustomHostname_SetsHostnameAndMode() = runTest {
        val executedArgs = mutableListOf<List<String>>()

        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult = ShellResult(0, "")
            override suspend fun executeArgs(vararg args: String, timeoutMs: Long): ShellResult {
                executedArgs.add(args.toList())
                return ShellResult(0, "")
            }
        }

        val repository = NetworkRepositoryImpl(
            settingsRepository = SecureSettingsRepository(fakeShellExecutor),
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

        // Verify settings.global put private_dns_specifier one.one.one.one AND private_dns_mode hostname
        val containsSpecifier = executedArgs.any { args -> args.contains("private_dns_specifier") && args.contains("one.one.one.one") }
        val containsMode = executedArgs.any { args -> args.contains("private_dns_mode") && args.contains("hostname") }

        assertTrue(containsSpecifier)
        assertTrue(containsMode)
    }

    @Test
    fun testMeasureHostLatency_EmitsRequestedSamples() = runTest {
        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult = ShellResult(0, "")
            override suspend fun executeArgs(vararg args: String, timeoutMs: Long): ShellResult = ShellResult(0, "")
        }

        val repository = NetworkRepositoryImpl(
            settingsRepository = SecureSettingsRepository(fakeShellExecutor),
            ioDispatcher = Dispatchers.Unconfined
        )

        val results = repository.measureHostLatency("127.0.0.1", samples = 2).toList()
        assertEquals(2, results.size)
    }
}
