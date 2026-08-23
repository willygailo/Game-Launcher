package com.gamebooster.app.spoofer.lsposed;

import android.hardware.Sensor;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.gamebooster.app.spoofer.SpoofProfile;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * SensorAndInputHooks — Hooks InputDevice and Sensor subsystems to report
 * flagship eSports high-polling touch digitizers (720Hz / 960Hz touch sampling)
 * and lowest-latency sensor hardware.
 */
public final class SensorAndInputHooks {

    private SensorAndInputHooks() {}

    public static void apply(LoadPackageParam lpparam, SpoofProfile profile) {
        if (profile == null) return;

        // 1. Hook InputDevice Motion Range & Resolution
        Class<?> inputDeviceClass = XposedHelpers.findClass("android.view.InputDevice", lpparam.classLoader);
        if (inputDeviceClass != null) {
            try {
                XposedHelpers.findAndHookMethod(inputDeviceClass, "getMotionRange", int.class, int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        int axis = (int) param.args[0];
                        InputDevice.MotionRange range = (InputDevice.MotionRange) param.getResult();
                        if (range != null && (axis == MotionEvent.AXIS_X || axis == MotionEvent.AXIS_Y)) {
                            // Set 0.001mm resolution for ultra-high touch precision
                            try {
                                XposedHelpers.setFloatField(range, "mResolution", 0.001f);
                            } catch (Throwable ignored) {}
                        }
                    }
                });
            } catch (Throwable ignored) {}
        }

        // 2. Hook Sensor.getMinDelay() (Forces lowest possible latency reporting)
        Class<?> sensorClass = XposedHelpers.findClass("android.hardware.Sensor", lpparam.classLoader);
        if (sensorClass != null) {
            try {
                XposedHelpers.findAndHookMethod(sensorClass, "getMinDelay", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(1000); // 1000 microseconds = 1ms (1000Hz rate)
                    }
                });
            } catch (Throwable ignored) {}
        }

        XposedBridge.log("[GameBooster] SensorAndInputHooks installed for " + lpparam.packageName);
    }
}
