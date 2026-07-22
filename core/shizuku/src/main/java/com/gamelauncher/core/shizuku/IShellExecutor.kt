package com.gamelauncher.core.shizuku

/**
 * Interface defining ShellExecutor contract for privileged shell execution.
 */
interface IShellExecutor {
    suspend fun executeCommand(command: String, timeoutMs: Long = 10000L): ShellResult
    suspend fun executeArgs(vararg args: String, timeoutMs: Long = 10000L): ShellResult
}
