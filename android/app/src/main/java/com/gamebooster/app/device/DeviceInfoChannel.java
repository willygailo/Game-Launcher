package com.gamebooster.app.device;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class DeviceInfoChannel {

    public static class Metrics {
        public final String deviceSummary;
        public final int ramUsagePct;
        public final long usedRamMb;
        public final long totalRamMb;
        public final float batteryTempC;
        public final float cpuTempC;
        public final int batteryCurrentMa;

        public Metrics(String summary, int pct, long used, long total, float temp, float cpuTempC, int currentMa) {
            this.deviceSummary = summary;
            this.ramUsagePct = pct;
            this.usedRamMb = used;
            this.totalRamMb = total;
            this.batteryTempC = temp;
            this.cpuTempC = cpuTempC;
            this.batteryCurrentMa = currentMa;
        }

        public JSONObject toJsonObject() {
            JSONObject obj = new JSONObject();
            try {
                obj.put("deviceSummary", deviceSummary);
                obj.put("ramUsagePct", ramUsagePct);
                obj.put("usedRamMb", usedRamMb);
                obj.put("totalRamMb", totalRamMb);
                obj.put("batteryTempC", batteryTempC);
                obj.put("cpuTempC", cpuTempC);
                obj.put("batteryCurrentMa", batteryCurrentMa);
            } catch (JSONException ignored) {}
            return obj;
        }
    }

    public static Metrics getMetrics(Context context) {
        DeviceSpecModel specs = DeviceDetector.getDeviceSpecModel();
        String summary = specs.getFormattedSummary();

        int ramPct = 0;
        long used = 0, total = 0;

        if (context != null) {
            ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (actMgr != null) {
                ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
                actMgr.getMemoryInfo(memInfo);
                total = memInfo.totalMem / (1024 * 1024);
                used = total - (memInfo.availMem / (1024 * 1024));
                ramPct = total > 0 ? (int) ((used * 100) / total) : 0;
            }
        }

        float tempC = 0.0f;
        int currentMa = 0;
        if (context != null) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryIntent = context.registerReceiver(null, filter);
            if (batteryIntent != null) {
                int tempInt = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                tempC = tempInt / 10.0f;
            }
            try {
                BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                if (bm != null) {
                    int val = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                    if (val != Integer.MIN_VALUE) {
                        currentMa = Math.abs(val / 1000);
                    }
                }
            } catch (Throwable ignored) {}
        }

        float cpuTemp = readCpuTemperature();
        if (cpuTemp <= 0) cpuTemp = tempC + 4.5f; // Fallback estimate

        return new Metrics(summary, ramPct, used, total, tempC, cpuTemp, currentMa);
    }

    private static float readCpuTemperature() {
        String[] thermalPaths = new String[] {
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp"
        };
        for (String path : thermalPaths) {
            File file = new File(path);
            if (file.exists() && file.canRead()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line = br.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        float val = Float.parseFloat(line.trim());
                        if (val > 1000) val /= 1000.0f;
                        if (val > 20 && val < 100) return val;
                    }
                } catch (Exception ignored) {}
            }
        }
        return 0.0f;
    }
}
