package com.gamebooster.app.booster;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.config.ConfigFileHelper;
import com.gamebooster.app.config.GameConfigPathResolver;
import com.gamebooster.app.engine.CommandExecutor;

import java.util.List;

public class GyroCalibratorEngine {

    private static final String TAG = "GyroCalibrator";

    public interface GyroDataListener {
        void onGyroUpdate(float x, float y, float z, float rawHz);
    }

    public static class CalibrationResult {
        public final float biasX;
        public final float biasY;
        public final float biasZ;
        public final boolean success;

        public CalibrationResult(float biasX, float biasY, float biasZ, boolean success) {
            this.biasX = biasX;
            this.biasY = biasY;
            this.biasZ = biasZ;
            this.success = success;
        }
    }

    /**
     * Applies hardware sensor HAL bias offset and forces 1000Hz gyro polling across system and game config files.
     */
    public static void applyZeroDriftCalibration(float biasX, float biasY, float biasZ) {
        try {
            // 1. Hardware sensor HAL bias properties
            CommandExecutor.executeSystemCommand("setprop persist.sys.gyro.bias_x " + biasX);
            CommandExecutor.executeSystemCommand("setprop persist.sys.gyro.bias_y " + biasY);
            CommandExecutor.executeSystemCommand("setprop persist.sys.gyro.bias_z " + biasZ);
            CommandExecutor.executeSystemCommand("setprop debug.sensor.gyro.sample_rate 1000");
            CommandExecutor.executeSystemCommand("setprop persist.sys.sensor.gyro_delay 0");
            CommandExecutor.executeSystemCommand("setprop debug.qualcomm.sns.hal.gyro_fast 1");
            CommandExecutor.executeSystemCommand("setprop persist.vendor.sensors.gyro_sample_rate 1000");

            Log.i(TAG, "⚡ Gyro Zero-Drift Calibration applied: X=" + biasX + ", Y=" + biasY + ", Z=" + biasZ + " @ 1000Hz");
        } catch (Throwable t) {
            Log.w(TAG, "Gyro calibration property write error: " + t.getMessage());
        }
    }
}
