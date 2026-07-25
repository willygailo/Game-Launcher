package com.gamespace.app.channels

import com.gamespace.app.utils.ShellExecutor
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
                        val success = ShellExecutor.setSystemPropertyRoot(key, value)
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
                            if (ShellExecutor.setSystemPropertyRoot(k, v)) {
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
}
