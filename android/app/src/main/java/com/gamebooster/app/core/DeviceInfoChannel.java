package com.gamebooster.app.core;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import com.gamebooster.app.core.DeviceSpecModel;
import com.gamebooster.app.core.DeviceDetector;

public class DeviceInfoChannel {

    public static class Metrics {
        public final String deviceSummary;
        public final int ramUsagePct;
        public final long usedRamMb;
        public final long totalRamMb;
        public final float batteryTempC;

        public Metrics(String summary, int pct, long used, long total, float temp) {
            this.deviceSummary = summary;
            this.ramUsagePct = pct;
            this.usedRamMb = used;
            this.totalRamMb = total;
            this.batteryTempC = temp;
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
        if (context != null) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryIntent = context.registerReceiver(null, filter);
            if (batteryIntent != null) {
                int tempInt = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                tempC = tempInt / 10.0f;
            }
        }

        return new Metrics(summary, ramPct, used, total, tempC);
    }
}
