package com.gamelauncher.core.permissions

import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.shizuku.IShizukuManager
import com.gamelauncher.core.shizuku.ShellResult
import com.gamelauncher.core.shizuku.ShizukuAvailability
import com.gamelauncher.core.shizuku.aidl.IShellCommandService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimePermissionManagerTest {

    @Test
    fun testPermissionState_SealedHierarchy() {
        val granted: PermissionState = PermissionState.Granted
        val denied: PermissionState = PermissionState.Denied
        val shizuku: PermissionState = PermissionState.ShizukuRequired

        assertEquals(PermissionState.Granted, granted)
        assertEquals(PermissionState.Denied, denied)
        assertEquals(PermissionState.ShizukuRequired, shizuku)
    }

    @Test
    fun testPermissionAllowlist_EnforcesAuthorizedPermissionsOnly() {
        assertTrue(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.WRITE_SECURE_SETTINGS"))
        assertTrue(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.DUMP"))
        assertTrue(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.PACKAGE_USAGE_STATS"))

        assertFalse(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.INSTALL_PACKAGES"))
        assertFalse(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.READ_SMS"))
    }

    @Test
    fun testGrantPermission_UnauthorizedPermission_RejectedBeforeShellExecution() = runBlocking {
        var executeArgsCallCount = 0

        val fakeExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult =
                ShellResult(0, "", "")

            override suspend fun executeArgs(vararg args: String, timeoutMs: Long): ShellResult {
                executeArgsCallCount++
                return ShellResult(0, "", "")
            }
        }

        val stubManager = object : IShizukuManager {
            override val availability: StateFlow<ShizukuAvailability> =
                MutableStateFlow(ShizukuAvailability.Ready)
            override fun isShizukuInstalled(): Boolean = true
            override fun checkAvailability() {}
            override fun requestPermission() {}
            override fun isReady(): Boolean = true
            override fun getUserService(): IShellCommandService? = null
            override fun bindUserService() {}
            override fun unbindUserService() {}
        }

        val dummyContext = object : android.content.ContextWrapper(null) {
            override fun getPackageName(): String = "com.gamelauncher"
        }

        val permissionManager = RuntimePermissionManager(dummyContext, fakeExecutor, stubManager)

        val result = permissionManager.grantPermissionViaShizuku("android.permission.INSTALL_PACKAGES")

        assertFalse(result)
        assertEquals(0, executeArgsCallCount)
    }
}
