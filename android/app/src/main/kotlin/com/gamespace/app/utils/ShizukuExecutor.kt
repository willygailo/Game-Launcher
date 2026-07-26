package com.gamespace.app.utils

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object ShizukuExecutor {

    private const val COMMAND_TIMEOUT_SECONDS = 5L
    const val REQUEST_CODE_SHIZUKU = 1001

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    fun isPermissionGranted(): Boolean {
        return try {
            if (!isShizukuAvailable()) return false
            if (Shizuku.isPreV11()) {
                false
            } else {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }
        } catch (e: Throwable) {
            false
        }
    }

    fun requestPermission(requestCode: Int = REQUEST_CODE_SHIZUKU): Boolean {
        return try {
            if (!isShizukuAvailable()) return false
            if (isPermissionGranted()) return true
            Shizuku.requestPermission(requestCode)
            true
        } catch (e: Throwable) {
            false
        }
    }

    fun grantAppPermissionsViaShizuku(packageName: String): Boolean {
        if (!isPermissionGranted() || packageName.isEmpty()) return false
        val permissions = listOf(
            "android.permission.WRITE_SECURE_SETTINGS",
            "android.permission.WRITE_SETTINGS",
            "android.permission.PACKAGE_USAGE_STATS"
        )
        var successCount = 0
        for (perm in permissions) {
            val res = executeShizukuCommand("pm grant $packageName $perm")
            if (res.success) successCount++
        }
        return successCount > 0
    }

    fun executeShizukuCommand(command: String): CommandResult {
        if (!isPermissionGranted()) {
            return CommandResult(
                success = false,
                exitCode = -1,
                stdout = "",
                stderr = "Shizuku permission not granted or service unavailable."
            )
        }

        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
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
                    stderr = "Shizuku command timed out after ${COMMAND_TIMEOUT_SECONDS}s"
                )
            }

            val exitCode = process.exitValue()
            CommandResult(
                success = exitCode == 0,
                exitCode = exitCode,
                stdout = stdoutSb.toString().trim(),
                stderr = stderrSb.toString().trim()
            )
        } catch (e: Throwable) {
            CommandResult(
                success = false,
                exitCode = -1,
                stdout = "",
                stderr = e.localizedMessage ?: "Unknown Shizuku Error"
            )
        }
    }
}
