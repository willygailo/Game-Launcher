// core/shizuku/src/test/java/com/gamelauncher/core/shizuku/ProcessLauncherTest.kt
package com.gamelauncher.core.shizuku

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ProcessLauncherTest {

    @Test
    fun testExecProcessWithOutput_CapturesStdout_ReturnsZeroExitCode() {
        val launcher = ProcessLauncher()
        val output = arrayOfNulls<String>(1)

        val exitCode = launcher.execProcessWithOutput(arrayOf("sh", "-c", "echo hello_shizuku"), output)

        assertEquals(0, exitCode)
        assertTrue(output[0]?.contains("hello_shizuku") == true)
    }

    @Test
    fun testExecProcess_TimeoutExceeded_CallsDestroyForciblyAndReturnsNegativeOne() {
        val destroyForciblyCalled = AtomicBoolean(false)

        val mockProcess = object : Process() {
            override fun getOutputStream() = error("Not needed")
            override fun getInputStream(): InputStream = ByteArrayInputStream(byteArrayOf())
            override fun getErrorStream(): InputStream = ByteArrayInputStream(byteArrayOf())

            override fun waitFor(): Int = 0

            override fun waitFor(timeout: Long, unit: TimeUnit): Boolean {
                return false
            }

            override fun exitValue(): Int = 0

            override fun destroy() {}

            override fun destroyForcibly(): Process {
                destroyForciblyCalled.set(true)
                return this
            }
        }

        val launcher = ProcessLauncher(
            processFactory = { _, _ -> mockProcess },
            timeoutMs = 100L
        )

        val exitCode = launcher.execProcess(arrayOf("sleep", "10"))

        assertEquals(-1, exitCode)
        assertTrue("destroyForcibly() must be invoked when process times out", destroyForciblyCalled.get())
    }

    @Test
    fun testExecProcess_LargeStderrOutput_DrainsWithoutDeadlock() {
        val launcher = ProcessLauncher(timeoutMs = 5000L)
        val output = arrayOfNulls<String>(1)

        val cmd = arrayOf(
            "sh", "-c",
            "for i in \$(seq 1 2000); do echo 'Large error line output string for buffer test 12345678901234567890' >&2; done"
        )

        val exitCode = launcher.execProcessWithOutput(cmd, output)

        assertEquals(0, exitCode)
        assertTrue("Stderr output must be drained without deadlocking pipe buffer", output[0]?.isNotEmpty() == true)
    }
}
