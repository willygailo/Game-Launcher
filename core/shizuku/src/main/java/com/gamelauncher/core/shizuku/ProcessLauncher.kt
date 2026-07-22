// core/shizuku/src/main/java/com/gamelauncher/core/shizuku/ProcessLauncher.kt
package com.gamelauncher.core.shizuku

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * ProcessLauncher — Handles deadlock-safe child process execution and output capture.
 * Merges stdout & stderr via ProcessBuilder.redirectErrorStream(true) to prevent OS pipe buffer deadlocks.
 */
class ProcessLauncher(
    private val processFactory: (cmdArray: Array<String>, redirectErr: Boolean) -> Process = { cmdArray, redirectErr ->
        ProcessBuilder(*cmdArray).redirectErrorStream(redirectErr).start()
    },
    private val timeoutMs: Long = 3000L
) {
    fun execProcess(cmdArray: Array<String>): Int {
        return execProcessWithOutput(cmdArray, null)
    }

    fun execProcessWithOutput(cmdArray: Array<String>, output: Array<String?>?): Int {
        return try {
            val proc = processFactory(cmdArray, true)
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (output != null) {
                    sb.append(line).append("\n")
                }
            }
            if (proc.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) {
                if (output != null && output.isNotEmpty()) {
                    output[0] = sb.toString()
                }
                proc.exitValue()
            } else {
                proc.destroyForcibly()
                -1
            }
        } catch (_: Exception) {
            -1
        }
    }
}
