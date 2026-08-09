package com.gamebooster.app.device;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.gamebooster.app.shizuku.ShizukuExecutor;

public class ThermalGovernorService extends Service {

    private static final String TAG = "ThermalGovernor";
    private static final float THERMAL_THRESHOLD_CELSIUS = 42.0f;
    private static boolean isMonitoring = false;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) return;

            int tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            float tempCelsius = tempTenths / 10.0f;

            if (tempCelsius >= THERMAL_THRESHOLD_CELSIUS) {
                Log.w(TAG, "BATTERY OVERHEAT DETECTED: " + tempCelsius + "°C — Throttling background processes!");
                if (ShizukuExecutor.isShizukuAvailable()) {
                    ShizukuExecutor.executeShizukuCommand("cmd device_config put game_overlay thermal_protection=1");
                    ShizukuExecutor.executeShizukuCommand("setprop debug.thermal.throttle 1");
                }
            }
        }
    };

    public static boolean isMonitoring() {
        return isMonitoring;
    }

    public static void start(Context context) {
        if (context == null || isMonitoring) return;
        Intent intent = new Intent(context, ThermalGovernorService.class);
        context.startService(intent);
    }

    public static void stop(Context context) {
        if (context == null || !isMonitoring) return;
        Intent intent = new Intent(context, ThermalGovernorService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isMonitoring = true;
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
        Log.i(TAG, "Thermal Governor Service started.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isMonitoring = false;
        try {
            unregisterReceiver(batteryReceiver);
        } catch (Exception ignored) {}
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
