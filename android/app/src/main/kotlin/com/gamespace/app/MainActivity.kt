package com.gamespace.app

import com.gamespace.app.channels.DeviceInfoChannel
import com.gamespace.app.channels.GameLibraryChannel
import com.gamespace.app.channels.HzFpsChannel
import com.gamespace.app.channels.MagiskExporterChannel
import com.gamespace.app.channels.PerformanceChannel
import com.gamespace.app.channels.PermissionChannel
import com.gamespace.app.channels.RootCommandChannel
import com.gamespace.app.channels.ShizukuChannel
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, RootCommandChannel.CHANNEL)
            .setMethodCallHandler(RootCommandChannel())

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, ShizukuChannel.CHANNEL)
            .setMethodCallHandler(ShizukuChannel(applicationContext))

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, HzFpsChannel.CHANNEL)
            .setMethodCallHandler(HzFpsChannel(applicationContext))

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, DeviceInfoChannel.CHANNEL)
            .setMethodCallHandler(DeviceInfoChannel())

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PermissionChannel.CHANNEL)
            .setMethodCallHandler(PermissionChannel(applicationContext))

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PerformanceChannel.CHANNEL)
            .setMethodCallHandler(PerformanceChannel(applicationContext))

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, GameLibraryChannel.CHANNEL)
            .setMethodCallHandler(GameLibraryChannel(applicationContext))

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, MagiskExporterChannel.CHANNEL)
            .setMethodCallHandler(MagiskExporterChannel(applicationContext))
    }
}



