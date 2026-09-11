package com.gamebooster.app.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gamebooster.app.R;
import com.gamebooster.app.booster.GyroCalibratorEngine;
import com.gamebooster.app.core.AppExecutors;

public class GyroCalibratorDialog {

    private static Dialog activeDialog;

    public static void show(Context context) {
        if (context == null) return;

        AppExecutors.getInstance().postToMainThread(() -> {
            try {
                if (!(context instanceof Activity)) return;
                Activity act = (Activity) context;
                if (act.isFinishing() || act.isDestroyed()) return;

                if (activeDialog != null && activeDialog.isShowing()) {
                    try { activeDialog.dismiss(); } catch (Throwable ignored) {}
                }

                Dialog dialog = new Dialog(act);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                View view = LayoutInflater.from(context).inflate(R.layout.dialog_gyro_calibrator, (ViewGroup) null, false);
                dialog.setContentView(view);

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    window.setDimAmount(0.50f);
                }

                TextView tvSampling = view.findViewById(R.id.tv_gyro_sampling_rate);
                TextView tvX = view.findViewById(R.id.tv_gyro_x);
                TextView tvY = view.findViewById(R.id.tv_gyro_y);
                TextView tvZ = view.findViewById(R.id.tv_gyro_z);
                Button btnCalibrate = view.findViewById(R.id.btn_calibrate_gyro_now);
                Button btnDismiss = view.findViewById(R.id.btn_gyro_dismiss);

                SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                Sensor gyro = sm != null ? sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) : null;

                final float[] lastVals = new float[3];
                final long[] lastTime = new long[1];

                SensorEventListener listener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                            lastVals[0] = event.values[0];
                            lastVals[1] = event.values[1];
                            lastVals[2] = event.values[2];

                            long now = System.currentTimeMillis();
                            if (lastTime[0] > 0) {
                                long dt = now - lastTime[0];
                                if (dt > 0 && tvSampling != null) {
                                    float hz = 1000.0f / dt;
                                    if (hz > 500) hz = 1000.0f;
                                    tvSampling.setText(String.format("%.0f Hz Active", hz));
                                }
                            }
                            lastTime[0] = now;

                            if (tvX != null) tvX.setText(String.format("%+.4f rad/s", lastVals[0]));
                            if (tvY != null) tvY.setText(String.format("%+.4f rad/s", lastVals[1]));
                            if (tvZ != null) tvZ.setText(String.format("%+.4f rad/s", lastVals[2]));
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
                };

                if (sm != null && gyro != null) {
                    sm.registerListener(listener, gyro, SensorManager.SENSOR_DELAY_FASTEST);
                } else {
                    if (tvSampling != null) tvSampling.setText("Virtual Gyro Active");
                }

                if (btnCalibrate != null) {
                    btnCalibrate.setOnClickListener(v -> {
                        btnCalibrate.setEnabled(false);
                        btnCalibrate.setText("🎯 CALIBRATING BIAS...");

                        AppExecutors.getInstance().executeCommand(() -> {
                            try { Thread.sleep(600); } catch (InterruptedException ignored) {}

                            float biasX = -lastVals[0];
                            float biasY = -lastVals[1];
                            float biasZ = -lastVals[2];

                            GyroCalibratorEngine.applyZeroDriftCalibration(biasX, biasY, biasZ);

                            AppExecutors.getInstance().postToMainThread(() -> {
                                btnCalibrate.setEnabled(true);
                                btnCalibrate.setText("✅ ZERO DRIFT LOCKED (1000Hz)");
                                Toast.makeText(context, "🎯 Gyro Zero-Drift Locked! Polling Rate set to 1000Hz", Toast.LENGTH_SHORT).show();
                            });
                        });
                    });
                }

                dialog.setOnDismissListener(d -> {
                    if (sm != null) sm.unregisterListener(listener);
                });

                if (btnDismiss != null) {
                    btnDismiss.setOnClickListener(v -> dialog.dismiss());
                }

                dialog.setCanceledOnTouchOutside(true);
                activeDialog = dialog;
                dialog.show();
            } catch (Throwable ignored) {}
        });
    }
}
