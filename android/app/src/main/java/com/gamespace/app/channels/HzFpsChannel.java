package com.gamespace.app.channels;

import android.content.Context;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.gamespace.app.utils.ShellExecutor;
import com.gamespace.app.utils.ShizukuExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class HzFpsChannel implements MethodChannel.MethodCallHandler {
    private static final String CHANNEL = "com.gamespace.app/hz_fps";
    private final Context context;
    private final MethodChannel channel;

    public HzFpsChannel(BinaryMessenger messenger, Context context) {
        this.context = context;
        this.channel = new MethodChannel(messenger, CHANNEL);
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "getDisplayRefreshRates":
                result.success(getDisplayRefreshRates());
                break;

            case "setRefreshRate":
                Double hzDouble = call.argument("hz");
                Boolean isShizuku = call.argument("isShizuku");
                if (hzDouble != null) {
                    float hz = hzDouble.floatValue();
                    boolean shizukuMode = Boolean.TRUE.equals(isShizuku);
                    setRefreshRate(hz, shizukuMode);
                    result.success(true);
                } else {
                    result.error("INVALID_ARGUMENT", "Hz value required", null);
                }
                break;

            case "setGameModePerformance":
                String pkg = call.argument("packageName");
                Boolean isShizukuMode = call.argument("isShizuku");
                if (pkg != null) {
                    setGameModePerformance(pkg, Boolean.TRUE.equals(isShizukuMode));
                    result.success(true);
                } else {
                    result.error("INVALID_ARGUMENT", "Package name required", null);
                }
                break;

            case "toggleThermalBypass":
                Boolean enable = call.argument("enable");
                Boolean isShizukuThermal = call.argument("isShizuku");
                if (enable != null) {
                    toggleThermalBypass(enable, Boolean.TRUE.equals(isShizukuThermal));
                    result.success(true);
                } else {
                    result.error("INVALID_ARGUMENT", "Enable flag required", null);
                }
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    private Map<String, Object> getDisplayRefreshRates() {
        Map<String, Object> data = new HashMap<>();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();
            float currentHz = display.getRefreshRate();
            data.put("currentHz", (double) currentHz);

            List<Double> rates = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Display.Mode[] modes = display.getSupportedModes();
                for (Display.Mode mode : modes) {
                    double r = mode.getRefreshRate();
                    if (!rates.contains(r)) {
                        rates.add(r);
                    }
                }
            } else {
                rates.add((double) currentHz);
            }
            data.put("supportedRates", rates);
        } else {
            data.put("currentHz", 60.0);
            List<Double> rates = new ArrayList<>();
            rates.add(60.0);
            data.put("supportedRates", rates);
        }
        return data;
    }

    private void setRefreshRate(float hz, boolean isShizuku) {
        String hzStr = String.valueOf(hz);
        if (isShizuku) {
            ShizukuExecutor.executeShizukuCommand("settings put system peak_refresh_rate " + hzStr);
            ShizukuExecutor.executeShizukuCommand("settings put system min_refresh_rate " + hzStr);
            ShizukuExecutor.executeShizukuCommand("settings put system user_refresh_rate " + hzStr);
        } else {
            ShellExecutor.executeCommand("settings put system peak_refresh_rate " + hzStr, true);
            ShellExecutor.executeCommand("settings put system min_refresh_rate " + hzStr, true);
            ShellExecutor.executeCommand("settings put system user_refresh_rate " + hzStr, true);
        }
    }

    private void setGameModePerformance(String packageName, boolean isShizuku) {
        String cmd = "cmd game mode performance " + packageName;
        if (isShizuku) {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            ShellExecutor.executeCommand(cmd, true);
        }
    }

    private void toggleThermalBypass(boolean enable, boolean isShizuku) {
        String status = enable ? "0" : "-1";
        String cmd = "cmd thermal override-status " + status;
        if (isShizuku) {
            ShizukuExecutor.executeShizukuCommand(cmd);
        } else {
            ShellExecutor.executeCommand(cmd, true);
        }
    }
}
