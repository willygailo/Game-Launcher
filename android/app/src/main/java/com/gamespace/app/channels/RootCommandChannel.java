package com.gamespace.app.channels;

import com.gamespace.app.utils.ShellExecutor;
import com.gamespace.app.utils.ShizukuExecutor;

import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class RootCommandChannel implements MethodChannel.MethodCallHandler {
    private static final String CHANNEL = "com.gamespace.app/root_command";
    private final MethodChannel channel;

    public RootCommandChannel(BinaryMessenger messenger) {
        this.channel = new MethodChannel(messenger, CHANNEL);
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "executeCommand":
                String command = call.argument("command");
                Boolean asRoot = call.argument("asRoot");
                if (command != null) {
                    boolean runAsRoot = Boolean.TRUE.equals(asRoot);
                    if (runAsRoot && !ShellExecutor.isRootAvailable() && ShizukuExecutor.hasShizukuPermission()) {
                        String output = ShizukuExecutor.executeShizukuCommand(command);
                        Map<String, Object> map = new HashMap<>();
                        map.put("exitCode", 0);
                        map.put("stdout", output);
                        map.put("stderr", "");
                        result.success(map);
                    } else {
                        ShellExecutor.CommandResult cmdResult = ShellExecutor.executeCommand(command, runAsRoot);
                        Map<String, Object> map = new HashMap<>();
                        map.put("exitCode", cmdResult.exitCode);
                        map.put("stdout", cmdResult.stdout);
                        map.put("stderr", cmdResult.stderr);
                        result.success(map);
                    }
                } else {
                    result.error("INVALID_ARGUMENT", "Command required", null);
                }
                break;

            case "setProp":
                String key = call.argument("key");
                String value = call.argument("value");
                if (key != null && value != null) {
                    String setpropCmd = "setprop " + key + " " + value;
                    if (!ShellExecutor.isRootAvailable() && ShizukuExecutor.hasShizukuPermission()) {
                        ShizukuExecutor.executeShizukuCommand(setpropCmd);
                        result.success(true);
                    } else {
                        ShellExecutor.CommandResult setRes = ShellExecutor.executeCommand(setpropCmd, true);
                        result.success(setRes.isSuccess());
                    }
                } else {
                    result.error("INVALID_ARGUMENT", "Key and Value required", null);
                }
                break;

            case "isRootAvailable":
                result.success(ShellExecutor.isRootAvailable());
                break;

            default:
                result.notImplemented();
                break;
        }
    }
}
