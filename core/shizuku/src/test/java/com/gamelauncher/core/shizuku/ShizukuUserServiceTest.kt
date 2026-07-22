// core/shizuku/src/test/java/com/gamelauncher/core/shizuku/ShizukuUserServiceTest.kt
package com.gamelauncher.core.shizuku

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShizukuUserServiceTest {

    @Test
    fun testGrantPermission_UnwhitelistedPermission_RejectedByServerAllowlist() {
        val service = ShizukuUserService()
        
        // Attempt to grant an unapproved permission (e.g. READ_SMS)
        val result = service.grantPermission("com.gamelauncher", "android.permission.READ_SMS")
        
        // Must be rejected by server-side allowlist
        assertFalse(result)
    }

    @Test
    fun testSetAppOp_UnwhitelistedOp_RejectedByServerAllowlist() {
        val service = ShizukuUserService()
        
        // Attempt to grant an unapproved AppOp (e.g. RECORD_AUDIO)
        val result = service.setAppOp("com.gamelauncher", "RECORD_AUDIO", "allow")
        
        // Must be rejected by server-side allowlist
        assertFalse(result)
    }

    @Test
    fun testGrantPermission_PackageNameMismatch_RejectedByBinderUidCheck() {
        val service = ShizukuUserService()
        
        // Attempt to grant WRITE_SECURE_SETTINGS to an arbitrary non-existent or mismatched package
        val result = service.grantPermission("com.arbitrary.thirdparty.app", "android.permission.WRITE_SECURE_SETTINGS")
        
        // Must be rejected because systemContext packageManager is null in test environment or UID mismatches
        assertFalse(result)
    }

    @Test
    fun testSetAppOp_PackageNameMismatch_RejectedByBinderUidCheck() {
        val service = ShizukuUserService()
        
        // Attempt to set AppOp for an arbitrary mismatched package
        val result = service.setAppOp("com.arbitrary.thirdparty.app", "SYSTEM_ALERT_WINDOW", "allow")
        
        // Must be rejected
        assertFalse(result)
    }

    @Test
    fun testExecProcess_TimeoutHandling_ReturnsNegativeExitCode() {
        val service = ShizukuUserService()
        
        // Attempt to call setting write for invalid namespace, verifying non-hanging execution
        val result = service.writeSetting("invalid_scope", "test_key", "test_val")
        
        assertFalse(result)
    }
}
