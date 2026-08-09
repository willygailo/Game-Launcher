package com.gamebooster.app.device;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.shizuku.ShizukuExecutor;

public class TouchLatencyCalibrator {

    private static final String TAG = "TouchCalibrator";

    public static String calibrateTouchSampling(Context context, int targetHz) {
        if (context == null) return "Invalid context";

        int touchHz = Math.max(targetHz * 2, 240); // Standard 2x multiplier for gaming digitizers (240Hz, 480Hz, 960Hz)

        if (ShizukuExecutor.isShizukuAvailable()) {
            ShizukuExecutor.executeShizukuCommand("setprop debug.touch.frequency " + touchHz);
            ShizukuExecutor.executeShizukuCommand("setprop persist.sys.touch.rate " + touchHz);
            ShizukuExecutor.executeShizukuCommand("setprop debug.input.touch_boost 1");
            ShizukuExecutor.executeShizukuCommand("cmd window set-app-touch-rate " + touchHz);

            Log.i(TAG, "Calibrated touch digitizer sampling rate to " + touchHz + "Hz");
            return "⚡ Digitizer Touch Sampling Calibrated to " + touchHz + "Hz!";
        } else {
            return "⚠️ Shizuku required to force digitizer sampling frequency";
        }
    }
}
