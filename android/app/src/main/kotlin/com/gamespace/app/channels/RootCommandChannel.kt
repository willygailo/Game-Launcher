package com.gamespace.app.channels

import com.gamespace.app.utils.ShellExecutor
import com.gamespace.app.utils.ShizukuExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RootCommandChannel : MethodChannel.MethodCallHandler {
    companion object {
        const val CHANNEL = "com.gamespace.app/root_command"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "isRootAvailable" -> {
                scope.launch {
                    val available = ShellExecutor.isRootAvailable()
                    result.success(available)
                }
            }
            "setSystemProperty" -> {
                val key = call.argument<String>("key")
                val value = call.argument<String>("value")
                if (key != null && value != null) {
                    scope.launch {
                        val success = setSystemPropUnified(key, value)
                        result.success(success)
                    }
                } else {
                    result.error("INVALID_ARGS", "Key or Value is null", null)
                }
            }
            "getSystemProperty" -> {
                val key = call.argument<String>("key")
                if (key != null) {
                    scope.launch {
                        val value = ShellExecutor.getSystemProperty(key)
                        result.success(value)
                    }
                } else {
                    result.error("INVALID_ARGS", "Key is null", null)
                }
            }
            "executeBatchTweaks" -> {
                val tweaks = call.argument<Map<String, String>>("tweaks")
                if (tweaks != null) {
                    scope.launch {
                        var appliedCount = 0
                        for ((k, v) in tweaks) {
                            if (setSystemPropUnified(k, v)) {
                                appliedCount++
                            }
                        }
                        result.success(appliedCount)
                    }
                } else {
                    result.error("INVALID_ARGS", "Tweaks map is null", null)
                }
            }
            else -> result.notImplemented()
        }
    }

    private fun setSystemPropUnified(key: String, value: String): Boolean {
        if (key.startsWith("ro.")) return false

        if (ShellExecutor.isRootAvailable()) {
            return ShellExecutor.setSystemPropertyRoot(key, value)
        } else if (ShizukuExecutor.isPermissionGranted()) {
            val res = ShizukuExecutor.executeShizukuCommand("setprop $key $value")
            return res.success
        }
        return false
    }
}
