package com.gamespace.app

import com.gamespace.app.channels.DeviceInfoChannel
import com.gamespace.app.channels.PermissionChannel
import com.gamespace.app.channels.RootCommandChannel
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, RootCommandChannel.CHANNEL)
            .setMethodCallHandler(RootCommandChannel())

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, DeviceInfoChannel.CHANNEL)
            .setMethodCallHandler(DeviceInfoChannel())

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PermissionChannel.CHANNEL)
            .setMethodCallHandler(PermissionChannel())
    }
}
