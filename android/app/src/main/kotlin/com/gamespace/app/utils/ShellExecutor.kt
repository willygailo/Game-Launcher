package com.gamespace.app.utils

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

data class CommandResult(
    val success: Boolean,
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

object ShellExecutor {

    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            process.waitFor()
            output != null && output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Sanitizes input to prevent shell command injection.
     */
    private fun sanitizeInput(input: String): String {
        // Allow alphanumeric, underscores, dots, hyphens, commas, and equal signs
        return input.replace(Regex("[^a-zA-Z0-9_\\.\\,-=]"), "")
    }

    fun setSystemPropertyRoot(key: String, value: String): Boolean {
        val cleanKey = sanitizeInput(key)
        val cleanValue = sanitizeInput(value)

        if (cleanKey.isEmpty() || cleanKey.startsWith("ro.")) {
            // Reject empty or read-only ro.* properties
            return false
        }

        val result = executeRootCommand("setprop $cleanKey $cleanValue")
        return result.success && result.exitCode == 0 && result.stderr.isEmpty()
    }

    fun executeRootCommand(command: String): CommandResult {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()

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

            val exitCode = process.waitFor()
            val stderrStr = stderrSb.toString().trim()

            CommandResult(
                success = (exitCode == 0 && stderrStr.isEmpty()),
                exitCode = exitCode,
                stdout = stdoutSb.toString().trim(),
                stderr = stderrStr
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
            process.waitFor()
            value?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
