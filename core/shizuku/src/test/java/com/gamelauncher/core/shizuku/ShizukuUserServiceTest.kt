package com.gamelauncher.core.shizuku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShizukuUserServiceTest {

    @Test
    fun testExec_EchoCommand_ReturnsOutputInArray() {
        val service = ShizukuUserService()
        val output = arrayOf("")

        val exitCode = service.exec("echo test123", output, 5000L)

        assertEquals(0, exitCode)
        assertEquals("test123", output[0])
    }

    @Test
    fun testExec_LargeOutput_TruncatesSafelyAt200KB() {
        val service = ShizukuUserService()
        val output = arrayOf("")

        // Generate output far larger than 200KB (100,000 lines ~ 4.7MB)
        service.exec("yes 'Long output line for Binder IPC truncation test' | head -n 100000", output, 10000L)

        assertTrue(output[0].contains("[Output truncated: Exceeded 200KB Binder IPC buffer limit]"))
    }

    @Test
    fun testExecArgs_EmptyArgs_HandledSafelyWithoutException() {
        val service = ShizukuUserService()
        val output = arrayOf("")

        val exitCode = service.execArgs(emptyArray(), output, 5000L)

        assertEquals(-1, exitCode)
        assertEquals("Empty command arguments array", output[0])
    }
}
