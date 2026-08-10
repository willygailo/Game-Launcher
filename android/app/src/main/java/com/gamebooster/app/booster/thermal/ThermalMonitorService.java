package com.gamebooster.app.booster.thermal;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.gamebooster.app.engine.CommandExecutor;
import java.io.File;
import java.io.RandomAccessFile;

/**
 * ThermalMonitorService continuously monitors CPU/SOC and Battery temperature.
 * If temperature reaches dangerous levels (>45°C), it triggers OEM thermal optimization,
 * applies performance mode tweaks, and alerts the user.
 */
public class ThermalMonitorService extends Service {

    private static final String TAG = "ThermalMonitorService";
    private static final long MONITOR_INTERVAL_MS = 5000; // 5 seconds
    private static final float WARNING_TEMP_THRESHOLD = 45.0f; // 45°C

    private Handler handler;
    private Runnable monitorRunnable;
    private float currentBatteryTemp = 0.0f;
    private float currentSocTemp = 0.0f;
    private boolean isMonitoring = false;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                currentBatteryTemp = tempTenths / 10.0f;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);

        monitorRunnable = new Runnable() {
            @Override
            public void run() {
                checkTemperatures();
                if (isMonitoring) {
                    handler.postDelayed(this, MONITOR_INTERVAL_MS);
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isMonitoring) {
            isMonitoring = true;
            handler.post(monitorRunnable);
            Log.d(TAG, "Thermal Monitoring Service started.");
        }
        return START_STICKY;
    }

    private void checkTemperatures() {
        currentSocTemp = readSocTemperatureSysfs();
        float maxTemp = Math.max(currentBatteryTemp, currentSocTemp);

        Log.d(TAG, String.format("Thermal Check: SOC=%.1f°C, Battery=%.1f°C", currentSocTemp, currentBatteryTemp));

        if (maxTemp >= WARNING_TEMP_THRESHOLD) {
            Log.w(TAG, String.format("WARNING: High Thermal Detected! Temp=%.1f°C. Applying thermal mitigation.", maxTemp));
            String result = ThermalManager.getInstance().applyThermalOptimization();
            Log.d(TAG, "Thermal mitigation applied: " + result);

            handler.post(() -> Toast.makeText(getApplicationContext(),
                    String.format("🔥 Thermal Alert: %.1f°C! Optimization applied.", maxTemp),
                    Toast.LENGTH_SHORT).show());
        }
    }

    public static float readSocTemperatureSysfs() {
        // Try common Linux/Android thermal zone sysfs paths
        String[] thermalPaths = new String[] {
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/thermal/thermal_zone2/temp",
                "/sys/devices/virtual/thermal/thermal_zone0/temp"
        };

        for (String path : thermalPaths) {
            File file = new File(path);
            if (file.exists()) {
                try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                    String line = raf.readLine();
                    if (line != null) {
                        float val = Float.parseFloat(line.trim());
                        if (val > 1000) val /= 1000.0f; // Sysfs often reports in millidegrees C
                        if (val > 10.0f && val < 110.0f) {
                            return val;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return 0.0f;
    }

    @Override
    public void onDestroy() {

        isMonitoring = false;
        if (handler != null) {
            handler.removeCallbacks(monitorRunnable);
        }
        try {
            unregisterReceiver(batteryReceiver);
        } catch (Exception ignored) {
        }
        Log.d(TAG, "Thermal Monitoring Service stopped.");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
