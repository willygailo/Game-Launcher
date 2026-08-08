package com.gamebooster.app.ui.screens;
import com.gamebooster.app.config.*;

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
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.config.GameProfileAutoConfigurator;

/** Shows only physical display modes reported by the device. */
public class HzFpsFragment extends Fragment {

    private Button btn60;
    private Button btn90;
    private Button btn120;
    private Button btn144;
    private Button btn165;
    private TextView tvDeviceRefreshSupport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hz_fps, container, false);
        btn60 = view.findViewById(R.id.btn_lock_60);
        btn90 = view.findViewById(R.id.btn_lock_90);
        btn120 = view.findViewById(R.id.btn_lock_120);
        btn144 = view.findViewById(R.id.btn_lock_144);
        btn165 = view.findViewById(R.id.btn_lock_165);
        tvDeviceRefreshSupport = view.findViewById(R.id.tv_device_refresh_support);

        btn60.setOnClickListener(v -> setHz(60));
        btn90.setOnClickListener(v -> setHz(90));
        btn120.setOnClickListener(v -> setHz(120));
        btn144.setOnClickListener(v -> setHz(144));
        btn165.setOnClickListener(v -> setHz(165));
        refreshSupportedRates();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSupportedRates();
    }

    private void refreshSupportedRates() {
        if (getContext() == null) return;
        DevicePerformanceCapabilities caps = DevicePerformanceCapabilities.detect(getContext());
        boolean hasShizuku = com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission();
        if (tvDeviceRefreshSupport != null) {
            String modeInfo = hasShizuku ? "⚡ Shizuku Direct Force Active (120/144/165Hz Unlocked)" : "Standard OS Modes";
            tvDeviceRefreshSupport.setText("Detected: " + caps.getSupportedRefreshRates()
                    + " Hz  •  Max: " + caps.getMaxRefreshRate() + " Hz\n" + modeInfo);
        }
        setRateVisible(btn60, caps, 60, hasShizuku);
        setRateVisible(btn90, caps, 90, hasShizuku);
        setRateVisible(btn120, caps, 120, hasShizuku);
        setRateVisible(btn144, caps, 144, hasShizuku);
        setRateVisible(btn165, caps, 165, hasShizuku);
    }

    private void setRateVisible(Button button, DevicePerformanceCapabilities caps, int rate, boolean hasShizuku) {
        if (button != null) {
            // Keep all high refresh rate buttons visible if Shizuku is active or rate is supported
            button.setVisibility((hasShizuku || caps.supportsRefreshRate(rate)) ? View.VISIBLE : View.GONE);
        }
    }

    private void setHz(int hz) {
        if (getContext() == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            boolean hasShizuku = com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission();
            HzFpsChannel.RefreshRateResult result = hasShizuku
                    ? HzFpsChannel.forceSetRefreshRate(getContext(), hz)
                    : HzFpsChannel.setRefreshRate(getContext(), hz);
            if (result.success) GameProfileAutoConfigurator.setTargetFpsHz(getContext(), result.appliedHz);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), result.message, Toast.LENGTH_LONG).show();
            });
        });
    }
}
