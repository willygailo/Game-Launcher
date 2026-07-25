package com.gamespace.app.channels

import com.gamespace.app.utils.ShellExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class PermissionChannel : MethodChannel.MethodCallHandler {
    companion object {
        const val CHANNEL = "com.gamespace.app/permissions"
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "checkRootPermission" -> {
                result.success(ShellExecutor.isRootAvailable())
            }
            "requestRootPermission" -> {
                val hasRoot = ShellExecutor.isRootAvailable()
                result.success(hasRoot)
            }
            else -> result.notImplemented()
        }
    }
}
