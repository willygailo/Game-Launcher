package com.gamebooster.app.spoofer.lsposed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * BatteryManagerHooks — Prevents game engines from throttling FPS or lowering resolution
 * due to simulated battery temperature, low charge, or thermal throttling status.
 *
 * Emulates flagship gaming phone thermal and battery conditions:
 * - Optimal cool battery temperature: 28.0 °C (reported as 280)
 * - Battery level: 95%
 * - Power state: AC plugged (Bypass Charging mode)
 * - Thermal status: PowerManager.THERMAL_STATUS_NONE (0)
 */
public final class BatteryManagerHooks {

    private static final int OPTIMAL_BATTERY_TEMP_TENTHS = 280; // 28.0 °C
    private static final int OPTIMAL_BATTERY_LEVEL = 95;
    private static final int OPTIMAL_BATTERY_SCALE = 100;

    private BatteryManagerHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (profile == null) return;

        // 1. Hook PowerManager.getCurrentThermalStatus() (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Class<?> powerManagerClass = XposedHelpers.findClass("android.os.PowerManager", lpparam.classLoader);
            if (powerManagerClass != null) {
                try {
                    XposedHelpers.findAndHookMethod(powerManagerClass, "getCurrentThermalStatus", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(PowerManager.THERMAL_STATUS_NONE);
                        }
                    });
                } catch (Throwable ignored) {}

                try {
                    XposedHelpers.findAndHookMethod(powerManagerClass, "getThermalHeadroom", int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(0.0f); // 0.0f = max thermal headroom available
                        }
                    });
                } catch (Throwable ignored) {}
            }
        }

        // 2. Hook BatteryManager.getIntProperty()
        Class<?> batteryManagerClass = XposedHelpers.findClass("android.os.BatteryManager", lpparam.classLoader);
        if (batteryManagerClass != null) {
            try {
                XposedHelpers.findAndHookMethod(batteryManagerClass, "getIntProperty", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        int prop = (int) param.args[0];
                        if (prop == BatteryManager.BATTERY_PROPERTY_CAPACITY) {
                            param.setResult(OPTIMAL_BATTERY_LEVEL);
                        } else if (prop == BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) {
                            param.setResult(6000000); // 6000 mAh
                        } else if (prop == BatteryManager.BATTERY_PROPERTY_STATUS) {
                            param.setResult(BatteryManager.BATTERY_STATUS_CHARGING);
                        }
                    }
                });
            } catch (Throwable ignored) {}
        }

        // 3. Hook ContextWrapper.registerReceiver for Intent.ACTION_BATTERY_CHANGED
        Class<?> contextWrapperClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
        if (contextWrapperClass != null) {
            try {
                XposedHelpers.findAndHookMethod(contextWrapperClass, "registerReceiver",
                        BroadcastReceiver.class, IntentFilter.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                IntentFilter filter = (IntentFilter) param.args[1];
                                if (filter != null && filter.hasAction(Intent.ACTION_BATTERY_CHANGED)) {
                                    Intent stickyIntent = (Intent) param.getResult();
                                    if (stickyIntent != null) {
                                        stickyIntent.putExtra(BatteryManager.EXTRA_TEMPERATURE, OPTIMAL_BATTERY_TEMP_TENTHS);
                                        stickyIntent.putExtra(BatteryManager.EXTRA_LEVEL, OPTIMAL_BATTERY_LEVEL);
                                        stickyIntent.putExtra(BatteryManager.EXTRA_SCALE, OPTIMAL_BATTERY_SCALE);
                                        stickyIntent.putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING);
                                        stickyIntent.putExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC);
                                        stickyIntent.putExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD);
                                        stickyIntent.putExtra(BatteryManager.EXTRA_PRESENT, true);
                                    }
                                }
                            }
                        });
            } catch (Throwable ignored) {}
        }

        XposedBridge.log("[GameBooster] BatteryManagerHooks installed for " + lpparam.packageName);
    }
}
