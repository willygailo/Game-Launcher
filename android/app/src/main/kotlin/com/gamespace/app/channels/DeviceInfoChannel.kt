package com.gamespace.app.channels

import com.gamespace.app.utils.DeviceDetector
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class DeviceInfoChannel : MethodChannel.MethodCallHandler {
    companion object {
        const val CHANNEL = "com.gamespace.app/device_info"
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getDeviceInfo" -> {
                result.success(DeviceDetector.getDeviceInfoMap())
            }
            else -> result.notImplemented()
        }
    }
}
