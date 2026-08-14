package com.gamebooster.app.feature.permissions.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.core.EngineUIHelper;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;
import com.gamebooster.app.platform.shizuku.ShizukuManager;
import com.gamebooster.app.platform.shizuku.ShizukuPermissionGranter;

public class PermissionsFragment extends Fragment {

    private TextView tvEngineStatus;
    private TextView tvSystemSettingsStatus;
    private TextView tvBatteryOptStatus;
    private TextView tvOverlayStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);

        Button btnShizuku = view.findViewById(R.id.btn_grant_shizuku);
        Button btnSettings = view.findViewById(R.id.btn_open_settings);
        Button btnBattery = view.findViewById(R.id.btn_battery_unrestricted);
        Button btnOverlay = view.findViewById(R.id.btn_open_overlay_settings);

        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        tvSystemSettingsStatus = view.findViewById(R.id.tv_root_status);
        tvBatteryOptStatus = view.findViewById(R.id.tv_battery_opt_status);
        tvOverlayStatus = view.findViewById(R.id.tv_overlay_status);

        btnShizuku.setOnClickListener(v -> {
            if (getContext() != null) {
                if (ShizukuExecutor.hasShizukuPermission()) {
                    AppExecutors.getInstance().executeCommand(() -> {
                        boolean ok = ShizukuPermissionGranter.grantAllPermissions(getContext().getPackageName());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), ok
                                        ? "⚡ All System & Elevated Permissions Granted via Shizuku!"
                                        : "Shizuku authorization pending. Please check Shizuku app.",
                                        Toast.LENGTH_LONG).show());
                        }
                    });
                } else {
                    ShizukuManager.requestShizukuPermission();
                    if (!ShizukuExecutor.isShizukuAvailable()) {
                        ShizukuManager.openOrInstallShizukuManager(getContext());
                    }
                }
                refreshAllStatuses();
            }
        });

        btnSettings.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to open write settings screen", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (btnBattery != null) {
            btnBattery.setOnClickListener(v -> {
                if (getContext() != null) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Exception ignored) {
                            Toast.makeText(getContext(), "Unable to open battery optimization settings", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        if (btnOverlay != null) {
            btnOverlay.setOnClickListener(v -> {
                if (getContext() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Unable to open overlay permission screen", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        refreshAllStatuses();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAllStatuses();
    }

    private void refreshAllStatuses() {
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        updateSystemSettingsStatus();
        updateBatteryOptStatus();
        updateOverlayStatus();
    }

    private void updateSystemSettingsStatus() {
        if (tvSystemSettingsStatus == null || getContext() == null) return;
        boolean canWrite = Settings.System.canWrite(getContext());
        if (canWrite) {
            tvSystemSettingsStatus.setText("WRITE_SETTINGS Permission: GRANTED (Active)");
            tvSystemSettingsStatus.setTextColor(0xFF00FF66);
        } else {
            tvSystemSettingsStatus.setText("WRITE_SETTINGS Permission: REQUIRED");
            tvSystemSettingsStatus.setTextColor(0xFFFFB800);
        }
    }

    private void updateBatteryOptStatus() {
        if (tvBatteryOptStatus == null || getContext() == null) return;
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        boolean isIgnoring = (pm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pm.isIgnoringBatteryOptimizations(getContext().getPackageName()));
        if (isIgnoring) {
            tvBatteryOptStatus.setText("Battery Optimization: UNRESTRICTED (Protected from background kills)");
            tvBatteryOptStatus.setTextColor(0xFF00FF66);
        } else {
            tvBatteryOptStatus.setText("Battery Optimization: OPTIMIZED (May be killed by system in background)");
            tvBatteryOptStatus.setTextColor(0xFFFFCC00);
        }
    }

    private void updateOverlayStatus() {
        if (tvOverlayStatus == null || getContext() == null) return;
        boolean canDraw = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getContext()));
        if (canDraw) {
            tvOverlayStatus.setText("Overlay HUD Permission: GRANTED (Active)");
            tvOverlayStatus.setTextColor(0xFF00FF66);
        } else {
            tvOverlayStatus.setText("Overlay HUD Permission: REQUIRED for in-game telemetry dock");
            tvOverlayStatus.setTextColor(0xFFFF99FF);
        }
    }
}
