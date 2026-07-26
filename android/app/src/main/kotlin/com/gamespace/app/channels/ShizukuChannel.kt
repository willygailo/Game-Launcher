package com.gamespace.app.channels

import android.content.Context
import com.gamespace.app.utils.ShizukuExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShizukuChannel(private val context: Context) : MethodChannel.MethodCallHandler {
    companion object {
        const val CHANNEL = "com.gamespace.app/shizuku"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "isShizukuAvailable" -> {
                result.success(ShizukuExecutor.isShizukuAvailable())
            }
            "isPermissionGranted" -> {
                val isGranted = ShizukuExecutor.isPermissionGranted()
                if (isGranted) {
                    scope.launch {
                        ShizukuExecutor.grantAppPermissionsViaShizuku(context.packageName)
                    }
                }
                result.success(isGranted)
            }
            "requestPermission" -> {
                val requested = ShizukuExecutor.requestPermission()
                if (ShizukuExecutor.isPermissionGranted()) {
                    scope.launch {
                        ShizukuExecutor.grantAppPermissionsViaShizuku(context.packageName)
                    }
                }
                result.success(requested)
            }
            "grantSelfPermissions" -> {
                scope.launch {
                    val granted = ShizukuExecutor.grantAppPermissionsViaShizuku(context.packageName)
                    result.success(granted)
                }
            }
            "executeCommand" -> {
                val command = call.argument<String>("command")
                if (command != null) {
                    scope.launch {
                        val res = ShizukuExecutor.executeShizukuCommand(command)
                        result.success(
                            mapOf(
                                "success" to res.success,
                                "exitCode" to res.exitCode,
                                "stdout" to res.stdout,
                                "stderr" to res.stderr
                            )
                        )
                    }
                } else {
                    result.error("INVALID_ARGS", "Command is null", null)
                }
            }
            "executeBatchCommands" -> {
                val commands = call.argument<List<String>>("commands")
                if (commands != null) {
                    scope.launch {
                        var appliedCount = 0
                        for (cmd in commands) {
                            val res = ShizukuExecutor.executeShizukuCommand(cmd)
                            if (res.success) {
                                appliedCount++
                            }
                        }
                        result.success(appliedCount)
                    }
                } else {
                    result.error("INVALID_ARGS", "Commands list is null", null)
                }
            }
            else -> result.notImplemented()
        }
    }
}
