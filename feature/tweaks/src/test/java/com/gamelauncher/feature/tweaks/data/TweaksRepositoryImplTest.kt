package com.gamelauncher.feature.tweaks.data

import com.gamelauncher.core.device.DeviceProfileDetector
import com.gamelauncher.core.device.OemCapabilityMap
import com.gamelauncher.core.permissions.RuntimePermissionManager
import com.gamelauncher.core.settings.SecureSettingsRepository
import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.shizuku.ShellResult
import com.gamelauncher.feature.tweaks.domain.model.TweakCategory
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
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult {
                return if (command == "id") ShellResult(0, "uid=0(root)") else ShellResult(0, "")
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

        val tweaks = repository.getAvailableTweaks().first()
        assertNotNull(tweaks)
        assertEquals(5, tweaks.size)
    }

    @Test
    fun testGetAvailableTweaks_ShizukuOnly_ReportsGranularRootBadgeNote() = runTest {
        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult {
                return if (command == "id") ShellResult(0, "uid=2000(shell) gid=2000(shell)") else ShellResult(0, "")
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

        val tweaks = repository.getAvailableTweaks().first()
        val cpuGovernorTweak = tweaks.first { it.category == TweakCategory.CPU_GOVERNOR }
        
        assertFalse(cpuGovernorTweak.isSupportedByDevice)
        assertEquals("Requires Root (Shizuku active)", cpuGovernorTweak.badgeNote)
    }

    @Test
    fun testApplyCpuGovernorTweak_UsesExecuteCommandWithRedirection_AndNeverPassesLiteralRedirectionArgs() = runTest {
        val executedCommands = mutableListOf<String>()
        val executedArgsList = mutableListOf<List<String>>()

        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult {
                executedCommands.add(command)
                return if (command == "id") ShellResult(0, "uid=0(root)") else ShellResult(0, "")
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

        // Verify executeCommand was invoked for root check, chmod, and multi-core governor loop
        assertTrue(executedCommands.size >= 2)
        assertTrue(executedCommands.any { it.contains("scaling_governor") })

        // Verify executeArgs NEVER received '>' as a literal string argument
        val containsLiteralRedirectionArg = executedArgsList.any { args -> args.contains(">") }
        assertFalse("executeArgs must not receive '>' as a literal argument!", containsLiteralRedirectionArg)
    }

    @Test
    fun testApplyCpuGovernorTweak_RejectsInvalidGovernorString() = runTest {
        var commandExecuted = false

        val fakeShellExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult {
                if (command != "id") commandExecuted = true
                return ShellResult(0, "uid=0(root)")
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
