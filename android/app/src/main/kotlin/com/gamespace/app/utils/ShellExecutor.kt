package com.gamespace.app.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class CommandResult(
    val success: Boolean,
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

object ShellExecutor {

    private const val COMMAND_TIMEOUT_SECONDS = 5L

    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            val finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroy()
                return false
            }
            output != null && output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sanitizes input to prevent shell command injection.
     * Allows alphanumeric, underscores, dots, hyphens, commas, equals signs, and forward slashes.
     */
    private fun sanitizeInput(input: String): String {
        return input.replace(Regex("[^a-zA-Z0-9_\\.,\\-=/]"), "")
    }

    fun setSystemPropertyRoot(key: String, value: String): Boolean {
        val cleanKey = sanitizeInput(key)
        val cleanValue = sanitizeInput(value)

        if (cleanKey.isEmpty() || cleanKey.startsWith("ro.")) {
            // Reject empty or read-only ro.* properties
            return false
        }

        val result = executeRootCommand("setprop $cleanKey $cleanValue")
        // Some Magisk versions write a benign line to stderr even on success — check exit code only.
        return result.exitCode == 0
    }

    fun executeRootCommand(command: String): CommandResult {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            os.close()

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            val stdoutSb = StringBuilder()
            val stderrSb = StringBuilder()

            var line: String?
            while (stdoutReader.readLine().also { line = it } != null) {
                stdoutSb.append(line).append("\n")
            }
            while (stderrReader.readLine().also { line = it } != null) {
                stderrSb.append(line).append("\n")
            }

            val finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroy()
                return CommandResult(
                    success = false,
                    exitCode = -1,
                    stdout = stdoutSb.toString().trim(),
                    stderr = "Command timed out after ${COMMAND_TIMEOUT_SECONDS}s"
                )
            }

            val exitCode = process.exitValue()
            CommandResult(
                success = exitCode == 0,
                exitCode = exitCode,
                stdout = stdoutSb.toString().trim(),
                stderr = stderrSb.toString().trim()
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                exitCode = -1,
                stdout = "",
                stderr = e.localizedMessage ?: "Unknown Error"
            )
        }
    }

    fun getSystemProperty(key: String): String {
        val cleanKey = sanitizeInput(key)
        if (cleanKey.isEmpty()) return ""

        return try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", cleanKey))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val value = reader.readLine()
            process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            value?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
