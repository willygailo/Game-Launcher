package com.gamebooster.app.ui.layout;

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
import com.gamebooster.app.core.DevicePerformanceCapabilities;
import com.gamebooster.app.functions.HzFpsChannel;
import com.gamebooster.app.games.GameProfileAutoConfigurator;

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
        if (tvDeviceRefreshSupport != null) {
            tvDeviceRefreshSupport.setText("Detected: " + caps.getSupportedRefreshRates()
                    + " Hz  •  Max: " + caps.getMaxRefreshRate() + " Hz");
        }
        setRateVisible(btn60, caps, 60);
        setRateVisible(btn90, caps, 90);
        setRateVisible(btn120, caps, 120);
        setRateVisible(btn144, caps, 144);
        setRateVisible(btn165, caps, 165);
    }

    private void setRateVisible(Button button, DevicePerformanceCapabilities caps, int rate) {
        if (button != null) button.setVisibility(caps.supportsRefreshRate(rate) ? View.VISIBLE : View.GONE);
    }

    private void setHz(int hz) {
        if (getContext() == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            HzFpsChannel.RefreshRateResult result = HzFpsChannel.setRefreshRate(getContext(), hz);
            if (result.success) GameProfileAutoConfigurator.setTargetFpsHz(getContext(), result.appliedHz);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), result.message, Toast.LENGTH_LONG).show();
            });
        });
    }
}
