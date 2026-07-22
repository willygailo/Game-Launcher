package com.gamelauncher.core.shizuku

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String = ""
)

/**
 * ShellExecutor — Bridges privileged ADB shell execution via Shizuku.
 * Primary Path: Persistent AIDL ShizukuUserService (array or string).
 * Fallback Path: Ad-hoc Shizuku.newProcess() with AtomicReference process cleanup.
 */
@Singleton
class ShellExecutor @Inject constructor(
    private val shizukuManager: IShizukuManager
) : IShellExecutor {
    @Volatile private var newProcessMethod: Method? = null

    /**
     * WARNING: Executing commands via [executeCommand] uses a shell wrapper ("sh -c <command>").
     * This method is STRICTLY RESERVED for static, developer-authored, hardcoded command strings.
     *
     * DO NOT use [executeCommand] with dynamic, variable, or user-derived inputs as it is vulnerable
     * to shell command injection. Always use [executeArgs] for commands with dynamic values.
     *
     * @see executeArgs
     */
    override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult =
        executeArgs("sh", "-c", command, timeoutMs = timeoutMs)

    /**
     * Executes argument array directly (execve), immune to shell command injection metacharacters.
     * Recommended for all commands containing dynamic parameters or user inputs.
     */
    @Suppress("UNCHECKED_CAST")
    override suspend fun executeArgs(vararg args: String, timeoutMs: Long): ShellResult =
        withContext(Dispatchers.IO) {
            if (args.isEmpty()) {
                return@withContext ShellResult(-1, "", "Empty command arguments array")
            }

            if (!shizukuManager.isReady()) {
                return@withContext ShellResult(-1, "", "Shizuku is not ready or permission is denied")
            }

            // Primary Path: Use persistent AIDL UserService if bound
            val service = shizukuManager.getUserService()
            if (service != null && service.asBinder().isBinderAlive) {
                return@withContext executeViaAidlArgs(service, args as Array<String>, timeoutMs)
            }

            // Fallback Path: Ad-hoc process via reflection with AtomicReference cleanup
            executeViaNewProcessArgs(args as Array<String>, timeoutMs)
        }

    private suspend fun executeViaAidlArgs(
        service: com.gamelauncher.core.shizuku.aidl.IShellCommandService,
        args: Array<String>,
        timeoutMs: Long
    ): ShellResult = withContext(Dispatchers.IO) {
        try {
            val result = withTimeoutOrNull(timeoutMs + 2000L) {
                val output = arrayOfNulls<String>(1)
                val exitCode = service.execArgs(args, output, timeoutMs)
                ShellResult(exitCode, output[0] ?: "", "")
            }
            result ?: ShellResult(-1, "", "AIDL execution timed out after ${timeoutMs}ms")
        } catch (e: Exception) {
            // Fall back to ad-hoc process execution if AIDL IPC fails
            executeViaNewProcessArgs(args, timeoutMs)
        }
    }

    private suspend fun executeViaNewProcessArgs(
        args: Array<String>,
        timeoutMs: Long
    ): ShellResult = withContext(Dispatchers.IO) {
        val processRef = AtomicReference<Process?>(null)

        try {
            val result = withTimeoutOrNull(timeoutMs) {
                val method = resolveNewProcess()
                    ?: return@withTimeoutOrNull ShellResult(-1, "", "Shizuku newProcess method not accessible")

                val proc = method.invoke(null, args, null, null) as? Process
                    ?: return@withTimeoutOrNull ShellResult(-1, "", "Failed to launch Shizuku process")

                processRef.set(proc)

                val stdout = proc.inputStream.bufferedReader().use { it.readText() }.trim()
                val stderr = proc.errorStream.bufferedReader().use { it.readText() }.trim()
                val exitCode = proc.waitFor()

                ShellResult(exitCode, stdout, stderr)
            }

            result ?: ShellResult(-1, "", "Command execution timed out after ${timeoutMs}ms")
        } catch (e: Exception) {
            ShellResult(-1, "", e.localizedMessage ?: "Unknown execution error")
        } finally {
            processRef.getAndSet(null)?.let { proc ->
                try {
                    proc.destroy()
                } catch (_: Exception) {}
            }
        }
    }

    private fun resolveNewProcess(): Method? {
        newProcessMethod?.let { return it }
        return try {
            val clazz = Class.forName("rikka.shizuku.Shizuku")
            val method = clazz.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            newProcessMethod = method
            method
        } catch (e: Exception) {
            null
        }
    }
}
