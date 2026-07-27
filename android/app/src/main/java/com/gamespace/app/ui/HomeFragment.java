package com.gamespace.app.ui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gamespace.app.R;
import com.gamespace.app.core.DeviceSpecModel;
import com.gamespace.app.core.EngineMode;
import com.gamespace.app.data.CommandExecutor;
import com.gamespace.app.utils.DeviceDetector;

public class HomeFragment extends Fragment {

    private TextView tvEngineMode;
    private TextView tvDeviceInfo;
    private TextView tvRamUsage;
    private TextView tvBatteryTemp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvEngineMode = view.findViewById(R.id.tv_engine_mode);
        tvDeviceInfo = view.findViewById(R.id.tv_device_info);
        tvRamUsage = view.findViewById(R.id.tv_ram_usage);
        tvBatteryTemp = view.findViewById(R.id.tv_battery_temp);

        updateDashboard();
        return view;
    }

    private void updateDashboard() {
        if (getContext() == null) return;

        EngineMode engineMode = CommandExecutor.getActiveEngineMode();
        tvEngineMode.setText("EXECUTION ENGINE: " + engineMode.getDisplayName());
        tvEngineMode.setTextColor(engineMode.getColorHex());

        DeviceSpecModel specs = DeviceDetector.getDeviceSpecModel();
        tvDeviceInfo.setText("Device: " + specs.getFormattedSummary());

        ActivityManager actMgr = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (actMgr != null) {
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actMgr.getMemoryInfo(memInfo);
            long totalMB = memInfo.totalMem / (1024 * 1024);
            long usedMB = totalMB - (memInfo.availMem / (1024 * 1024));
            long pct = totalMB > 0 ? (usedMB * 100) / totalMB : 0;
            tvRamUsage.setText("RAM Memory Usage: " + pct + "% (" + usedMB + " MB / " + totalMB + " MB)");
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = getContext().registerReceiver(null, filter);
        if (batteryIntent != null) {
            int tempInt = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            float tempC = tempInt / 10.0f;
            tvBatteryTemp.setText("Battery Temperature: " + tempC + " °C");
        }
    }
}
