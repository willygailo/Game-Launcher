package com.gamespace.app.channels;

import android.content.Context;
import com.gamespace.app.utils.ShizukuExecutor;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class ShizukuChannel implements MethodChannel.MethodCallHandler {
    private static final String CHANNEL = "com.gamespace.app/shizuku";
    private final Context context;
    private final MethodChannel channel;

    public ShizukuChannel(BinaryMessenger messenger, Context context) {
        this.context = context;
        this.channel = new MethodChannel(messenger, CHANNEL);
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "isShizukuAvailable":
                result.success(ShizukuExecutor.isShizukuAvailable());
                break;
            case "hasShizukuPermission":
                result.success(ShizukuExecutor.hasShizukuPermission());
                break;
            case "executeShizukuCommand":
                String cmd = call.argument("command");
                if (cmd != null) {
                    result.success(ShizukuExecutor.executeShizukuCommand(cmd));
                } else {
                    result.error("INVALID_ARGUMENT", "Command cannot be null", null);
                }
                break;
            case "grantAppPermissions":
                ShizukuExecutor.grantAppPermissionsViaShizuku(context);
                result.success(true);
                break;
            default:
                result.notImplemented();
                break;
        }
    }
}
