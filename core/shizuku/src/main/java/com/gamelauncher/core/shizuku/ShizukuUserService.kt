package com.gamelauncher.core.shizuku

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.gamelauncher.core.shizuku.aidl.IShellCommandService
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * Shizuku User Service running under Shizuku process context (ADB UID 2000).
 * Supports both string command execution and safe array-based execve execution.
 */
@Keep
class ShizukuUserService : IShellCommandService.Stub() {

    override fun destroy() {
        exitProcess(0)
    }

    override fun exec(command: String, output: Array<out String>?, timeoutMs: Long): Int {
        return execProcess(arrayOf("sh", "-c", command), output, timeoutMs)
    }

    override fun execArgs(args: Array<out String>?, output: Array<out String>?, timeoutMs: Long): Int {
        if (args == null || args.isEmpty()) {
            if (output != null && output.isNotEmpty()) {
                (output as Array<String>)[0] = "Empty command arguments array"
            }
            return -1
        }
        @Suppress("UNCHECKED_CAST")
        return execProcess(args as Array<String>, output, timeoutMs)
    }

    @Suppress("UNCHECKED_CAST")
    @SuppressLint("UnsafeOptInUsageError")
    private fun execProcess(cmdArray: Array<String>, output: Array<out String>?, timeoutMs: Long): Int {
        val outArray = output as? Array<String>
        var process: Process? = null
        val executor = Executors.newSingleThreadExecutor()

        return try {
            process = Runtime.getRuntime().exec(cmdArray)
            val activeProcess = process

            val sb = StringBuilder()
            var isTruncated = false

            // Read output concurrently in background thread to prevent OS pipe deadlock (64KB buffer overflow)
            val readFuture = executor.submit<Unit> {
                val reader = BufferedReader(InputStreamReader(activeProcess.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                    if (sb.length >= MAX_BINDER_OUTPUT_CHARS) {
                        isTruncated = true
                        break
                    }
                }
                reader.close()
            }

            // Enforce server-side execution timeout
            val completed = activeProcess.waitFor(timeoutMs, TimeUnit.MILLISECONDS)
            if (!completed) {
                try {
                    activeProcess.destroyForcibly()
                } catch (_: Exception) {}

                if (outArray != null && outArray.isNotEmpty()) {
                    outArray[0] = "Server-side command execution timed out after ${timeoutMs}ms"
                }
                return -1
            }

            // Wait for stream reader thread to finish reading captured output
            try {
                readFuture.get(1000L, TimeUnit.MILLISECONDS)
            } catch (_: Exception) {}

            val exitCode = activeProcess.exitValue()

            if (outArray != null && outArray.isNotEmpty()) {
                var finalOutput = sb.toString().trim()
                if (isTruncated || finalOutput.length > MAX_BINDER_OUTPUT_CHARS) {
                    if (finalOutput.length > MAX_BINDER_OUTPUT_CHARS) {
                        finalOutput = finalOutput.substring(0, MAX_BINDER_OUTPUT_CHARS)
                    }
                    finalOutput += "\n[Output truncated: Exceeded ${MAX_BINDER_OUTPUT_CHARS / 1024}KB Binder IPC buffer limit]"
                }
                outArray[0] = finalOutput
            }
            exitCode
        } catch (e: Exception) {
            try {
                process?.destroyForcibly()
            } catch (_: Exception) {}

            if (outArray != null && outArray.isNotEmpty()) {
                outArray[0] = "Error executing command: ${e.localizedMessage}"
            }
            -1
        } finally {
            executor.shutdownNow()
        }
    }

    companion object {
        const val MAX_BINDER_OUTPUT_CHARS = 200 * 1024
    }
}
