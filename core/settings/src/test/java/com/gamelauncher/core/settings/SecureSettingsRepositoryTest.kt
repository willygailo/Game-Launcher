package com.gamelauncher.core.settings

import com.gamelauncher.core.shizuku.ShizukuUserService
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
    fun testExecArgs_MaliciousInjectionPayload_TreatedAsSingleLiteralArgument() {
        val service = ShizukuUserService()
        val output = arrayOf("")

        // Malicious injection payload attempt: x'; rm -rf /data/data/com.gamelauncher; echo '
        val payload = "x'; rm -rf /data/data/com.gamelauncher; echo '"
        val args = arrayOf("echo", payload)

        val exitCode = service.execArgs(args, output, 5000L)

        assertEquals(0, exitCode)
        // Verify output treats the entire string as a single literal value without executing injected commands
        assertEquals(payload, output[0])
    }
}
