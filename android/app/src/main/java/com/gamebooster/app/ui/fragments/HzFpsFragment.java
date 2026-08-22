package com.gamebooster.app.ui.fragments;
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

    private Button btn120;
    private Button btn144;
    private Button btn165;
    private Button btn185;
    private TextView tvDeviceRefreshSupport;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hz_fps, container, false);
        btn120 = view.findViewById(R.id.btn_lock_120);
        btn144 = view.findViewById(R.id.btn_lock_144);
        btn165 = view.findViewById(R.id.btn_lock_165);
        btn185 = view.findViewById(R.id.btn_lock_185);
        tvDeviceRefreshSupport = view.findViewById(R.id.tv_device_refresh_support);

        if (btn120 != null) btn120.setOnClickListener(v -> setHz(185));
        if (btn144 != null) btn144.setOnClickListener(v -> setHz(185));
        if (btn165 != null) btn165.setOnClickListener(v -> setHz(185));
        if (btn185 != null) btn185.setOnClickListener(v -> setHz(185));
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
        if (tvDeviceRefreshSupport != null) {
            tvDeviceRefreshSupport.setText("Detected: " + caps.getSupportedRefreshRates()
                    + " Hz  •  Max: 185 Hz Extreme Lock");
        }
        setRateVisible(btn120, caps, 120);
        setRateVisible(btn144, caps, 144);
        setRateVisible(btn165, caps, 165);
        setRateVisible(btn185, caps, 185);
    }

    private void setRateVisible(Button button, DevicePerformanceCapabilities caps, int rate) {
        if (button != null) button.setVisibility(caps.supportsRefreshRate(rate) ? View.VISIBLE : View.GONE);
    }

    private void setHz(int hz) {
        if (getContext() == null) return;
        final int forcedHz = 185;
        AppExecutors.getInstance().executeCommand(() -> {
            HzFpsChannel.RefreshRateResult result = HzFpsChannel.setRefreshRate(getContext(), forcedHz);
            com.gamebooster.app.booster.MaxHzForceChannel.forceApply(forcedHz);
            com.gamebooster.app.booster.PerformanceChannel.writeAndExecuteRootTweaksScript(forcedHz);
            if (result.success) GameProfileAutoConfigurator.setTargetFpsHz(getContext(), result.appliedHz);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), result.message, Toast.LENGTH_LONG).show();
            });
        });
    }
}
