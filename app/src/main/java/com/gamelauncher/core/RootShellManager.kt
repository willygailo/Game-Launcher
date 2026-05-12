package com.gamelauncher.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import kotlin.concurrent.Volatile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootShellManager @Inject constructor() {

    @Volatile private var hasRootCache: Boolean? = null

    /**
     * Checks if the device has root access and the user grants permission.
     */
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        hasRootCache?.let { return@withContext it }
        val isAvailable = checkRoot()
        hasRootCache = isAvailable
        isAvailable
    }

    private fun checkRoot(): Boolean {
        var process: Process? = null
        var os: DataOutputStream? = null
        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes("id\n")
            os.writeBytes("exit\n")
            os.flush()
            
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            
            val exitValue = process.waitFor()
            return exitValue == 0 && output?.contains("uid=0") == true
        } catch (e: Exception) {
            return false
        } finally {
            os?.close()
            process?.destroy()
        }
    }

    /**
     * Executes a shell command as root.
     * @return Pair of (isSuccess, outputOrError)
     */
    suspend fun executeCommand(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (!isRootAvailable()) return@withContext Pair(false, "Root not available")

        var process: Process? = null
        var os: DataOutputStream? = null
        var reader: BufferedReader? = null
        var errorReader: BufferedReader? = null
        val output = StringBuilder()
        val errorOutput = StringBuilder()

        try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()

            reader = BufferedReader(InputStreamReader(process.inputStream))
            errorReader = BufferedReader(InputStreamReader(process.errorStream))
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            while (errorReader.readLine().also { line = it } != null) {
                errorOutput.append(line).append("\n")
            }

            val exitCode = process.waitFor()
            val success = exitCode == 0
            val resultText = if (success) output.toString().trim() else errorOutput.toString().trim()
            
            Pair(success, resultText)
        } catch (e: Exception) {
            Pair(false, e.message ?: "Unknown error")
        } finally {
            os?.close()
            reader?.close()
            errorReader?.close()
            process?.destroy()
        }
    }
}
