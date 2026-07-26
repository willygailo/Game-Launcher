package com.gamespace.app.channels;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class PerformanceChannel implements MethodChannel.MethodCallHandler {
    private static final String CHANNEL = "com.gamespace.app/performance";
    private final Context context;
    private final MethodChannel channel;

    public PerformanceChannel(BinaryMessenger messenger, Context context) {
        this.context = context;
        this.channel = new MethodChannel(messenger, CHANNEL);
        this.channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        switch (call.method) {
            case "getPerformanceMetrics":
                Map<String, Object> metrics = new HashMap<>();
                metrics.put("cpuUsage", getCpuUsage());

                ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                if (actMgr != null) {
                    ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
                    actMgr.getMemoryInfo(memInfo);
                    long totalMemMB = memInfo.totalMem / (1024 * 1024);
                    long availMemMB = memInfo.availMem / (1024 * 1024);
                    long usedMemMB = totalMemMB - availMemMB;
                    double ramPercentage = totalMemMB > 0 ? ((double) usedMemMB / totalMemMB) * 100.0 : 0.0;

                    metrics.put("ramUsagePercentage", ramPercentage);
                    metrics.put("totalRamMB", totalMemMB);
                    metrics.put("usedRamMB", usedMemMB);
                }

                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryIntent = context.registerReceiver(null, filter);
                if (batteryIntent != null) {
                    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int tempInt = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);

                    float battPct = (level >= 0 && scale > 0) ? ((float) level / (float) scale) * 100.0f : 0.0f;
                    float battTempC = tempInt / 10.0f;

                    metrics.put("batteryPercentage", (double) battPct);
                    metrics.put("batteryTemperatureC", (double) battTempC);
                }

                result.success(metrics);
                break;

            default:
                result.notImplemented();
                break;
        }
    }

    private double getCpuUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            reader.close();

            if (load != null) {
                String[] toks = load.split("\\s+");
                long idle = Long.parseLong(toks[4]);
                long cpu = Long.parseLong(toks[1]) + Long.parseLong(toks[2]) + Long.parseLong(toks[3])
                        + Long.parseLong(toks[5]) + Long.parseLong(toks[6]) + Long.parseLong(toks[7]);
                long total = idle + cpu;
                return total > 0 ? ((double) cpu / total) * 100.0 : 0.0;
            }
        } catch (Exception ignored) {}
        return 0.0;
    }
}
