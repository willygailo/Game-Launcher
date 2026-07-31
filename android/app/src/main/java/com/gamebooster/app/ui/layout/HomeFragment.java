package com.gamebooster.app.ui.layout;

import com.gamebooster.app.core.DeviceInfoChannel;
import com.gamebooster.app.root.EngineMode;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.gamebooster.app.core.DeviceInfoChannel;
import com.gamebooster.app.functions.PerformanceChannel;
import com.gamebooster.app.root.EngineMode;
import com.gamebooster.app.root.CommandExecutor;
import com.gamebooster.app.metadata.GameBoosterService;

public class HomeFragment extends Fragment {

    private TextView tvEngineMode;
    private TextView tvDeviceInfo;
    private TextView tvRamUsage;
    private TextView tvBatteryTemp;
    private TextView tvBoostStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvEngineMode = view.findViewById(R.id.tv_engine_mode);
        tvDeviceInfo = view.findViewById(R.id.tv_device_info);
        tvRamUsage = view.findViewById(R.id.tv_ram_usage);
        tvBatteryTemp = view.findViewById(R.id.tv_battery_temp);
        tvBoostStatus = view.findViewById(R.id.tv_boost_status);
        Button btnHeroBoost = view.findViewById(R.id.btn_hero_boost);

        btnHeroBoost.setOnClickListener(v -> {
            if (getContext() != null) {
                tvBoostStatus.setText("⚡ BOOSTING: Trimming RAM, Locking Hz & Overriding Thermal Caps...");
                boolean ok = PerformanceChannel.executeOneTapBoost(getContext());
                startBoosterService();
                if (ok) {
                    tvBoostStatus.setText("✅ ULTRA BOOST ACTIVE: System Max Performance Locked!");
                    Toast.makeText(getContext(), "1-Tap Boost Executed Successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    tvBoostStatus.setText("⚠️ Boost Active with System Settings");
                }
                updateDashboard();
            }
        });

        updateDashboard();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDashboard();
    }

    private void startBoosterService() {
        if (getContext() == null) return;
        try {
            Intent serviceIntent = new Intent(getContext(), GameBoosterService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getContext().startForegroundService(serviceIntent);
            } else {
                getContext().startService(serviceIntent);
            }
        } catch (Exception ignored) {}
    }

    private void updateDashboard() {
        if (getContext() == null) return;

        EngineMode engineMode = CommandExecutor.getActiveEngineMode();
        tvEngineMode.setText("EXECUTION ENGINE: " + engineMode.getDisplayName());
        tvEngineMode.setTextColor(engineMode.getColorHex());

        DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(getContext());
        tvDeviceInfo.setText("Hardware: " + m.deviceSummary);
        tvRamUsage.setText("RAM Usage: " + m.ramUsagePct + "% (" + m.usedRamMb + " MB / " + m.totalRamMb + " MB)");
        tvBatteryTemp.setText("Battery Temp: " + m.batteryTempC + " °C");
    }
}
