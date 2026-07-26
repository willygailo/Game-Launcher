package com.gamespace.app.channels;

import com.gamespace.app.utils.DeviceDetector;

import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class DeviceInfoChannel implements MethodChannel.MethodCallHandler {
    private static final String CHANNEL = "com.gamespace.app/device_info";
    private final MethodChannel channel;

    public DeviceInfoChannel(BinaryMessenger messenger) {
        this.channel = new MethodChannel(messenger, CHANNEL);
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        if ("getDeviceSpecs".equals(call.method)) {
            Map<String, String> specs = DeviceDetector.getDeviceSpecs();
            result.success(specs);
        } else {
            result.notImplemented();
        }
    }
}
