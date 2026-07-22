// core/permissions/src/test/java/com/gamelauncher/core/permissions/RuntimePermissionManagerTest.kt
package com.gamelauncher.core.permissions

import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.shizuku.IShizukuManager
import com.gamelauncher.core.shizuku.ShizukuState
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
    fun testPermissionAllowlist_EnforcesAuthorizedPermissionsOnly() {
        assertTrue(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.WRITE_SECURE_SETTINGS"))
        assertTrue(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.DUMP"))
        assertTrue(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.PACKAGE_USAGE_STATS"))

        assertFalse(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.INSTALL_PACKAGES"))
        assertFalse(RuntimePermissionManager.ALLOWED_PERMISSIONS.contains("android.permission.READ_SMS"))
    }

    @Test
    fun testGrantPermission_UnauthorizedPermission_RejectedBeforeShellExecution() = runBlocking {
        var grantCallCount = 0

        val fakeExecutor = object : IShellExecutor {
            override suspend fun setPeakRefreshRate(hz: Float): Boolean = false
            override suspend fun setMinRefreshRate(hz: Float): Boolean = false
            override suspend fun setThermalOverride(disabled: Boolean): Boolean = false
            override suspend fun writeSetting(namespace: String, key: String, value: String): Boolean = false
            override suspend fun readSetting(namespace: String, key: String): String? = null
            override suspend fun setDeviceConfig(namespace: String, key: String, value: String): Boolean = false
            override suspend fun readDeviceConfig(namespace: String, key: String): String? = null
            override suspend fun grantPermission(packageName: String, permissionName: String): Boolean {
                grantCallCount++
                return true
            }
            override suspend fun setAppOp(packageName: String, opName: String, mode: String): Boolean = true
        }

        val stubManager = object : IShizukuManager {
            override val state: StateFlow<ShizukuState> = MutableStateFlow(ShizukuState.Connected)
            override fun isShizukuInstalled(): Boolean = true
            override fun checkAvailability() {}
            override fun requestPermission() {}
            override fun isReady(): Boolean = true
            override fun getUserService(): IShellCommandService? = null
            override fun bindUserService() {}
            override fun unbindUserService() {}
            override fun cleanup() {}
        }

        val dummyContext = object : android.content.ContextWrapper(null) {
            override fun getPackageName(): String = "com.gamelauncher"
        }

        val permissionManager = RuntimePermissionManager(dummyContext, fakeExecutor, stubManager)
        val result = permissionManager.grantPermissionViaShizuku("android.permission.INSTALL_PACKAGES")

        assertFalse(result)
        assertEquals(0, grantCallCount)
    }
}
