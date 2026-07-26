package com.gamespace.app.channels;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.gamespace.app.utils.ShellExecutor;
import com.gamespace.app.utils.ShizukuExecutor;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class PermissionChannel implements MethodChannel.MethodCallHandler {
    private static final String CHANNEL = "com.gamespace.app/permission";
    private final Context context;
    private final MethodChannel channel;

    public PermissionChannel(BinaryMessenger messenger, Context context) {
        this.context = context;
        this.channel = new MethodChannel(messenger, CHANNEL);
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "checkPermissions":
                Map<String, Object> status = new HashMap<>();
                boolean isRooted = ShellExecutor.isRootAvailable();
                boolean isShizukuAvail = ShizukuExecutor.isShizukuAvailable();
                boolean isShizukuGranted = ShizukuExecutor.hasShizukuPermission();
                boolean canWriteSettings = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    canWriteSettings = Settings.System.canWrite(context);
                } else {
                    canWriteSettings = true;
                }

                status.put("isRootGranted", isRooted);
                status.put("isShizukuAvailable", isShizukuAvail);
                status.put("isShizukuGranted", isShizukuGranted);
                status.put("isWriteSettingsGranted", canWriteSettings);

                String executionMode = "readOnly";
                if (isRooted) {
                    executionMode = "root";
                } else if (isShizukuGranted) {
                    executionMode = "shizuku";
                }
                status.put("executionMode", executionMode);

                result.success(status);
                break;

            case "requestWriteSettings":
                result.success(true);
                break;

            default:
                result.notImplemented();
                break;
        }
    }
}
