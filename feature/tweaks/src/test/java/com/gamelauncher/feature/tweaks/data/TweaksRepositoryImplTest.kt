package com.gamelauncher.feature.tweaks.data

import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.device.OemCapabilityMap
import com.gamelauncher.core.permissions.RuntimePermissionManager
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.shizuku.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class TweaksRepositoryImplTest {

    @Test
    fun testGetAvailableTweaks_ReturnsNonEmptyList() = runTest {
        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult = ShellResult(0, "")
            override suspend fun executeArgs(vararg args: String, timeoutMs: Long): ShellResult = ShellResult(0, "")
        }

        val detector = DeviceProfileDetector(fakeShellExecutor)
        val capabilityMap = OemCapabilityMap(detector)
        val mockPermissionManager = Mockito.mock(RuntimePermissionManager::class.java)

        val repository = TweaksRepositoryImpl(
            settingsRepository = SecureSettingsRepository(fakeShellExecutor),
            deviceProfileDetector = detector,
            capabilityMap = capabilityMap,
            permissionManager = mockPermissionManager,
            shellExecutor = fakeShellExecutor,
            ioDispatcher = Dispatchers.Unconfined
        )

        val tweaks = repository.getAvailableTweaks().first()
        assertNotNull(tweaks)
        assertEquals(4, tweaks.size)
    }

    @Test
    fun testApplyCpuGovernorTweak_UsesExecuteCommandWithRedirection_AndNeverPassesLiteralRedirectionArgs() = runTest {
        val executedCommands = mutableListOf<String>()
        val executedArgsList = mutableListOf<List<String>>()

        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult {
                executedCommands.add(command)
                return ShellResult(0, "")
            }

            override suspend fun executeArgs(vararg args: String, timeoutMs: Long): ShellResult {
                executedArgsList.add(args.toList())
                return ShellResult(0, "")
            }
        }

        val detector = DeviceProfileDetector(fakeShellExecutor)
        val capabilityMap = OemCapabilityMap(detector)
        val mockPermissionManager = Mockito.mock(RuntimePermissionManager::class.java)

        val repository = TweaksRepositoryImpl(
            settingsRepository = SecureSettingsRepository(fakeShellExecutor),
            deviceProfileDetector = detector,
            capabilityMap = capabilityMap,
            permissionManager = mockPermissionManager,
            shellExecutor = fakeShellExecutor,
            ioDispatcher = Dispatchers.Unconfined
        )

        val success = repository.applyCpuGovernorTweak("performance")

        assertTrue(success)

        // Verify executeCommand was invoked for shell redirection
        assertEquals(1, executedCommands.size)
        assertEquals("echo performance > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", executedCommands[0])

        // Verify executeArgs NEVER received '>' as a literal string argument
        val containsLiteralRedirectionArg = executedArgsList.any { args -> args.contains(">") }
        assertFalse("executeArgs must not receive '>' as a literal argument!", containsLiteralRedirectionArg)
    }

    @Test
    fun testApplyCpuGovernorTweak_RejectsInvalidGovernorString() = runTest {
        var commandExecuted = false

        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult {
                commandExecuted = true
                return ShellResult(0, "")
            }

            override suspend fun executeArgs(vararg args: String, timeoutMs: Long): ShellResult = ShellResult(0, "")
        }

        val detector = DeviceProfileDetector(fakeShellExecutor)
        val capabilityMap = OemCapabilityMap(detector)
        val mockPermissionManager = Mockito.mock(RuntimePermissionManager::class.java)

        val repository = TweaksRepositoryImpl(
            settingsRepository = SecureSettingsRepository(fakeShellExecutor),
            deviceProfileDetector = detector,
            capabilityMap = capabilityMap,
            permissionManager = mockPermissionManager,
            shellExecutor = fakeShellExecutor,
            ioDispatcher = Dispatchers.Unconfined
        )

        val result = repository.applyCpuGovernorTweak("invalid_gov; rm -rf /")

        assertFalse(result)
        assertFalse(commandExecuted)
    }
}
