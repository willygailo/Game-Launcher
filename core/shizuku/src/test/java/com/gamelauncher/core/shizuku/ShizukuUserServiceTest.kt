// core/shizuku/src/test/java/com/gamelauncher/core/shizuku/ShizukuUserServiceTest.kt
package com.gamelauncher.core.shizuku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class ShizukuUserServiceTest {

    private val testUid = 10042
    private val selfPackage = "com.gamelauncher"
    private val thirdPartyPackage = "com.arbitrary.thirdparty.app"

    private fun createTestService(
        execResult: Int = 0,
        onExec: ((Array<String>) -> Unit)? = null
    ): ShizukuUserService {
        val testLauncher = ProcessLauncher(
            processFactory = { cmdArray, _ ->
                onExec?.invoke(cmdArray)
                object : Process() {
                    override fun getOutputStream() = error("Not needed")
                    override fun getInputStream(): InputStream = ByteArrayInputStream(byteArrayOf())
                    override fun getErrorStream(): InputStream = ByteArrayInputStream(byteArrayOf())
                    override fun waitFor(): Int = execResult
                    override fun waitFor(timeout: Long, unit: TimeUnit): Boolean = true
                    override fun exitValue(): Int = execResult
                    override fun destroy() {}
                }
            }
        )
        return ShizukuUserService(
            callingUidProvider = { testUid },
            packageResolver = { uid ->
                if (uid == testUid) listOf(selfPackage) else null
            },
            processLauncher = testLauncher
        )
    }

    @Test
    fun testGrantPermission_SelfPackageAndWhitelistedPermission_Success() {
        var executedCmd: Array<String>? = null
        val service = createTestService(execResult = 0) { cmd -> executedCmd = cmd }

        val result = service.grantPermission(selfPackage, "android.permission.WRITE_SECURE_SETTINGS")

        assertTrue(result)
        assertEquals("pm", executedCmd?.get(0))
        assertEquals("grant", executedCmd?.get(1))
        assertEquals(selfPackage, executedCmd?.get(2))
        assertEquals("android.permission.WRITE_SECURE_SETTINGS", executedCmd?.get(3))
    }

    @Test
    fun testGrantPermission_ThirdPartyPackage_RejectedByUidPackageCheck() {
        var execCalled = false
        val service = createTestService(execResult = 0) { execCalled = true }

        val result = service.grantPermission(thirdPartyPackage, "android.permission.WRITE_SECURE_SETTINGS")

        assertFalse(result)
        assertFalse(execCalled)
    }

    @Test
    fun testGrantPermission_UnwhitelistedPermission_RejectedByAllowlist() {
        var execCalled = false
        val service = createTestService(execResult = 0) { execCalled = true }

        val result = service.grantPermission(selfPackage, "android.permission.READ_SMS")

        assertFalse(result)
        assertFalse(execCalled)
    }

    @Test
    fun testSetAppOp_SelfPackageAndWhitelistedOp_Success() {
        var executedCmd: Array<String>? = null
        val service = createTestService(execResult = 0) { cmd -> executedCmd = cmd }

        val result = service.setAppOp(selfPackage, "SYSTEM_ALERT_WINDOW", "allow")

        assertTrue(result)
        assertEquals("appops", executedCmd?.get(0))
        assertEquals("set", executedCmd?.get(1))
        assertEquals(selfPackage, executedCmd?.get(2))
        assertEquals("SYSTEM_ALERT_WINDOW", executedCmd?.get(3))
    }

    @Test
    fun testSetAppOp_ThirdPartyPackage_RejectedByUidPackageCheck() {
        var execCalled = false
        val service = createTestService(execResult = 0) { execCalled = true }

        val result = service.setAppOp(thirdPartyPackage, "SYSTEM_ALERT_WINDOW", "allow")

        assertFalse(result)
        assertFalse(execCalled)
    }

    @Test
    fun testSetAppOp_UnwhitelistedOp_RejectedByAllowlist() {
        var execCalled = false
        val service = createTestService(execResult = 0) { execCalled = true }

        val result = service.setAppOp(selfPackage, "RECORD_AUDIO", "allow")

        assertFalse(result)
        assertFalse(execCalled)
    }

    @Test
    fun testExecProcess_TimeoutOrFailure_ReturnsNegativeOne() {
        val service = createTestService(execResult = -1)

        val result = service.grantPermission(selfPackage, "android.permission.WRITE_SECURE_SETTINGS")

        assertFalse(result)
    }
}
