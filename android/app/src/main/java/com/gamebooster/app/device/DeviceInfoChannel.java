package com.gamebooster.app.device;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import com.gamebooster.app.device.DeviceSpecModel;
import com.gamebooster.app.device.DeviceDetector;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofProfile;

public class DeviceInfoChannel {

    private static volatile Metrics sCachedMetrics;
    private static volatile long sLastMetricsFetch = 0L;
    private static final long METRICS_CACHE_TTL_MS = 1500L;

    public static class Metrics {
        public final String deviceSummary;
        public final int ramUsagePct;
        public final long usedRamMb;
        public final long totalRamMb;
        public final float batteryTempC;
        public final int batteryCurrentMa;

        public Metrics(String summary, int pct, long used, long total, float temp, int currentMa) {
            this.deviceSummary = summary;
            this.ramUsagePct = pct;
            this.usedRamMb = used;
            this.totalRamMb = total;
            this.batteryTempC = temp;
            this.batteryCurrentMa = currentMa;
        }
    }

    public static Metrics getMetrics(Context context) {
        long now = System.currentTimeMillis();
        if (sCachedMetrics != null && (now - sLastMetricsFetch < METRICS_CACHE_TTL_MS)) {
            return sCachedMetrics;
        }

        Metrics metrics = computeMetrics(context);
        sCachedMetrics = metrics;
        sLastMetricsFetch = now;
        return metrics;
    }

    private static Metrics computeMetrics(Context context) {
        DeviceSpecModel specs = DeviceDetector.getDeviceSpecModel();
        String summary = specs.getFormattedSummary();

        int ramPct = 0;
        long used = 0, total = 0;

        SpoofProfile activeSpoof = DeviceSpooferEngine.getActiveProfile();

        if (context != null) {
            ActivityManager actMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (actMgr != null) {
                ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
                actMgr.getMemoryInfo(memInfo);
                long realTotal = memInfo.totalMem / (1024 * 1024);
                long realAvail = memInfo.availMem / (1024 * 1024);
                long realUsed = realTotal - realAvail;

                if (activeSpoof != null && activeSpoof.ramTotalMb > 0) {
                    total = activeSpoof.ramTotalMb;
                    float usageRatio = realTotal > 0 ? ((float) realUsed / realTotal) : 0.45f;
                    used = (long) (total * usageRatio);
                    ramPct = total > 0 ? (int) ((used * 100) / total) : 0;
                } else {
                    total = realTotal;
                    used = realUsed;
                    ramPct = total > 0 ? (int) ((used * 100) / total) : 0;
                }
            }
        }

        float tempC = 0.0f;
        int currentMa = 0;
        if (context != null) {
            try {
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryIntent = context.registerReceiver(null, filter);
                if (batteryIntent != null) {
                    int tempInt = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                    tempC = tempInt / 10.0f;
                }
            } catch (Throwable ignored) {}

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

        return new Metrics(summary, ramPct, used, total, tempC, currentMa);
    }
}
