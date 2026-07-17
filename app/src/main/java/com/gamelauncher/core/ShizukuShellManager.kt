package com.gamelauncher.core

import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ShizukuShellManager — executes adb-shell-level commands via Shizuku.
 *
 * Shizuku grants the same privilege level as `adb shell` — no root required.
 * One-time user setup: enable Wireless Debugging → start Shizuku.
 *
 * Implementation note: Shizuku 13.x moved newProcess() to package-private in Kotlin.
 * We access it via reflection so it works regardless of visibility on any version (11–13+).
 *
 * Falls back to RootShellManager if Shizuku is unavailable and root exists.
 */
@Singleton
class ShizukuShellManager @Inject constructor(
    private val rootShellManager: RootShellManager
) {

    companion object {
        private const val SHIZUKU_CODE = 1001
        private const val SHIZUKU_PKG = "rikka.shizuku.Shizuku"
    }

    // Lazily resolved reflection handle — cached after first successful lookup
    @Volatile private var newProcessMethod: Method? = null

    // ── Availability ───────────────────────────────────────────────────

    fun isShizukuRunning(): Boolean = try {
        Shizuku.pingBinder() && Shizuku.getVersion() >= 10
    } catch (_: Exception) { false }

    fun hasShizukuPermission(): Boolean = try {
        if (Shizuku.isPreV11() || Shizuku.getVersion() < 11) false
        else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: Exception) { false }

    fun isAvailable(): Boolean = isShizukuRunning() && hasShizukuPermission()

    fun requestPermission() {
        try {
            if (isShizukuRunning() && !Shizuku.isPreV11()) {
                Shizuku.requestPermission(SHIZUKU_CODE)
            }
        } catch (_: Exception) {}
    }

    // ── Command Execution ─────────────────────────────────────────────

    /**
     * Executes a shell command at adb-shell level via Shizuku.
     * If Shizuku is unavailable, falls back to root shell.
     *
     * @return Pair(success, output/error)
     */
    suspend fun executeCommand(command: String): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            if (isAvailable()) {
                executeViaShizuku(command)
            } else {
                // Transparent fallback to root shell if available
                rootShellManager.executeCommand(command)
            }
        }

    /**
     * Runs multiple commands sequentially.
     * Returns true if at least one command succeeded.
     */
    suspend fun executeAny(commands: List<String>): Pair<Boolean, String> =
        withContext(Dispatchers.IO) {
            var anyOk = false
            val results = StringBuilder()
            for (cmd in commands) {
                val (ok, out) = executeCommand(cmd)
                if (ok) anyOk = true
                results.appendLine("[$cmd] → $out")
            }
            Pair(anyOk, results.toString().trim())
        }

    suspend fun suspendThermalEngines(): Pair<Boolean, String> =
        executeAny(listOf(
            "stop thermal-engine",
            "stop mi_thermald",
            "stop thermald"
        ))

    /**
     * Resumes thermal engines that were suspended during gaming.
     * MUST be called in stopOptimization() to restore thermal protection.
     * Without this, device has no thermal management until reboot.
     */
    suspend fun resumeThermalEngines(): Pair<Boolean, String> =
        executeAny(listOf(
            "start thermal-engine",
            "start mi_thermald",
            "start thermald"
        ))

    // ── Private: Shizuku execution via reflection ─────────────────────

    /**
     * newProcess() was moved to package-private in Shizuku 13.x Kotlin rewrite.
     * Reflection bypasses compile-time visibility — still works perfectly at runtime.
     */
    private fun executeViaShizuku(command: String): Pair<Boolean, String> {
        var process: Process? = null
        var stdout: java.io.BufferedReader? = null
        var stderr: java.io.BufferedReader? = null
        return try {
            val method = resolveNewProcess()
                ?: return Pair(false, "Shizuku newProcess not accessible")

            val args = arrayOf("sh", "-c", command)
            process = method.invoke(null, args, null, null) as? Process
                ?: return Pair(false, "Shizuku process launch returned null")

            stdout = java.io.BufferedReader(java.io.InputStreamReader(process.inputStream))
            stderr = java.io.BufferedReader(java.io.InputStreamReader(process.errorStream))

            val outText = stdout.readText().trim()
            val errText = stderr.readText().trim()

            val exitCode = process.waitFor()
            val success = exitCode == 0
            Pair(success, if (success) outText else errText)
        } catch (e: Exception) {
            Pair(false, e.message ?: "Shizuku execution failed")
        } finally {
            // Close streams BEFORE destroy to prevent FD leaks
            try { stdout?.close() } catch (_: Exception) {}
            try { stderr?.close() } catch (_: Exception) {}
            process?.destroy()
        }
    }

    private fun resolveNewProcess(): Method? {
        newProcessMethod?.let { return it }
        return try {
            val clazz = Class.forName(SHIZUKU_PKG)
            val m = clazz.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            m.isAccessible = true
            newProcessMethod = m
            m
        } catch (_: Exception) { null }
    }
}
