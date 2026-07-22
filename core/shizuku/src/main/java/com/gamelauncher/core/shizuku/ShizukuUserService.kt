// core/shizuku/src/main/java/com/gamelauncher/core/shizuku/ShizukuUserService.kt
package com.gamelauncher.core.shizuku

import android.content.ContentResolver
import android.content.Context
import android.os.Binder
import android.provider.DeviceConfig
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import com.gamelauncher.core.shizuku.aidl.IShellCommandService
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

/**
 * Shizuku User Service running under Shizuku process context (ADB UID 2000).
 * Implements typed AIDL methods for Android settings manipulation and system commands.
 * Features server-side Binder caller package verification, permission allowlists, and deadlock-safe stream draining.
 */
@Keep
class ShizukuUserService : IShellCommandService.Stub() {

    companion object {
        /**
         * Server-side defense-in-depth allowlist for permission grants.
         */
        private val ALLOWED_PERMISSIONS = setOf(
            "android.permission.WRITE_SECURE_SETTINGS",
            "android.permission.DUMP",
            "android.permission.PACKAGE_USAGE_STATS",
            "android.permission.CHANGE_CONFIGURATION"
        )

        /**
         * Server-side defense-in-depth allowlist for AppOps modifications.
         */
        private val ALLOWED_APPOPS = setOf(
            "GET_USAGE_STATS",
            "android:get_usage_stats",
            "SYSTEM_ALERT_WINDOW"
        )
    }

    private val systemContext: Context? by lazy {
        try {
            val activityThreadClass = Class.forName("android.app.ActivityThread")
            val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread")
            val currentActivityThread = currentActivityThreadMethod.invoke(null)
            val getSystemContextMethod = activityThreadClass.getMethod("getSystemContext")
            getSystemContextMethod.invoke(currentActivityThread) as Context
        } catch (_: Exception) {
            null
        }
    }

    private val contentResolver: ContentResolver?
        get() = systemContext?.contentResolver

    override fun destroy() {
        exitProcess(0)
    }

    override fun setPeakRefreshRate(hz: Float): Boolean {
        return writeSetting("system", "peak_refresh_rate", hz.toString())
    }

    override fun setMinRefreshRate(hz: Float): Boolean {
        return writeSetting("system", "min_refresh_rate", hz.toString())
    }

    override fun setThermalOverride(disabled: Boolean): Boolean {
        val ok1 = try { writeSetting("global", "thermal_limit_enabled", if (disabled) "0" else "1") } catch (_: Exception) { false }
        val ok2 = try { writeSetting("secure", "thermal_throttling_disabled", if (disabled) "1" else "0") } catch (_: Exception) { false }
        val cmdArgs = if (disabled) {
            arrayOf("cmd", "thermalservice", "override-status", "0")
        } else {
            arrayOf("cmd", "thermalservice", "reset")
        }
        val cmdExitCode = execProcess(cmdArgs)

        Log.d(
            "ShizukuUserService",
            "setThermalOverride(disabled=$disabled) results -> global.thermal_limit_enabled=$ok1, secure.thermal_throttling_disabled=$ok2, cmd thermalservice exitCode=$cmdExitCode"
        )

        return ok1 || ok2 || (cmdExitCode == 0)
    }

    override fun writeSetting(namespace: String, key: String, value: String): Boolean {
        val resolver = contentResolver
        if (resolver != null) {
            try {
                val success = when (namespace.lowercase()) {
                    "system" -> Settings.System.putString(resolver, key, value)
                    "secure" -> Settings.Secure.putString(resolver, key, value)
                    "global" -> Settings.Global.putString(resolver, key, value)
                    else -> false
                }
                if (success) return true
            } catch (e: SecurityException) {
                throw e
            } catch (_: Exception) {}
        }
        val exitCode = execProcess(arrayOf("settings", "put", namespace.lowercase(), key, value))
        return exitCode == 0
    }

    override fun readSetting(namespace: String, key: String): String? {
        val resolver = contentResolver
        if (resolver != null) {
            try {
                val result = when (namespace.lowercase()) {
                    "system" -> Settings.System.getString(resolver, key)
                    "secure" -> Settings.Secure.getString(resolver, key)
                    "global" -> Settings.Global.getString(resolver, key)
                    else -> null
                }
                if (result != null) return result
            } catch (e: SecurityException) {
                throw e
            } catch (_: Exception) {}
        }
        val output = arrayOfNulls<String>(1)
        val exitCode = execProcessWithOutput(arrayOf("settings", "get", namespace.lowercase(), key), output)
        if (exitCode == 0) {
            val out = output[0]?.trim()
            if (out != null && out != "null") return out
            return ""
        }
        return null
    }

    override fun setDeviceConfig(namespace: String, key: String, value: String): Boolean {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val success = DeviceConfig.setProperty(namespace, key, value, false)
                if (success) return true
            }
        } catch (e: SecurityException) {
            throw e
        } catch (_: Exception) {}
        val exitCode = execProcess(arrayOf("device_config", "put", namespace, key, value))
        return exitCode == 0
    }

    override fun readDeviceConfig(namespace: String, key: String): String? {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val valProp = DeviceConfig.getProperty(namespace, key)
                if (valProp != null) return valProp
            }
        } catch (e: SecurityException) {
            throw e
        } catch (_: Exception) {}
        val output = arrayOfNulls<String>(1)
        val exitCode = execProcessWithOutput(arrayOf("device_config", "get", namespace, key), output)
        if (exitCode == 0) {
            val out = output[0]?.trim()
            if (out != null && out != "null") return out
            return ""
        }
        return null
    }

    override fun grantPermission(packageName: String, permissionName: String): Boolean {
        val callingUid = Binder.getCallingUid()
        val callerPackages = systemContext?.packageManager?.getPackagesForUid(callingUid)
        if (callerPackages == null || packageName !in callerPackages) {
            Log.w("ShizukuUserService", "Rejected grantPermission: packageName mismatch for uid $callingUid (target: $packageName)")
            return false
        }
        if (!ALLOWED_PERMISSIONS.contains(permissionName)) {
            Log.w("ShizukuUserService", "grantPermission denied by server-side allowlist for: $permissionName")
            return false
        }
        val exitCode = execProcess(arrayOf("pm", "grant", packageName, permissionName))
        return exitCode == 0
    }

    override fun setAppOp(packageName: String, opName: String, mode: String): Boolean {
        val callingUid = Binder.getCallingUid()
        val callerPackages = systemContext?.packageManager?.getPackagesForUid(callingUid)
        if (callerPackages == null || packageName !in callerPackages) {
            Log.w("ShizukuUserService", "Rejected setAppOp: packageName mismatch for uid $callingUid (target: $packageName)")
            return false
        }
        if (!ALLOWED_APPOPS.contains(opName)) {
            Log.w("ShizukuUserService", "setAppOp denied by server-side allowlist for: $opName")
            return false
        }
        val exitCode = execProcess(arrayOf("appops", "set", packageName, opName, mode))
        return exitCode == 0
    }

    private fun execProcess(cmdArray: Array<String>): Int {
        return try {
            val pb = ProcessBuilder(*cmdArray).redirectErrorStream(true)
            val proc = pb.start()
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            while (reader.readLine() != null) {
                // Drain combined stdout/stderr to prevent pipe buffer deadlock
            }
            if (proc.waitFor(3000L, TimeUnit.MILLISECONDS)) {
                proc.exitValue()
            } else {
                proc.destroyForcibly()
                -1
            }
        } catch (_: Exception) {
            -1
        }
    }

    private fun execProcessWithOutput(cmdArray: Array<String>, output: Array<String?>): Int {
        return try {
            val pb = ProcessBuilder(*cmdArray).redirectErrorStream(true)
            val proc = pb.start()
            val reader = BufferedReader(InputStreamReader(proc.inputStream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            if (proc.waitFor(3000L, TimeUnit.MILLISECONDS)) {
                output[0] = sb.toString()
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
