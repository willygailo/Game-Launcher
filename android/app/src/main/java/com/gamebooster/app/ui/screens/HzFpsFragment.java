package com.gamebooster.app.ui.screens;

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
import com.gamebooster.app.engine.DisplayOverrideController;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.DisplayCapabilitiesDetector;
import com.gamebooster.app.device.DisplayRefreshRatePreferences;

import java.util.List;

/**
 * HzFpsFragment — Display Refresh Rate Override Control Screen.
 *
 * <p>Queries the physical display panel dynamically via {@link DisplayCapabilitiesDetector}
 * and enables selection only for physical panel-supported rates. Selection is persisted via
 * {@link DisplayRefreshRatePreferences} and applied through the verified privilege backend.
 */
public class HzFpsFragment extends Fragment {

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
        btn90 = view.findViewById(R.id.btn_lock_90);
        btn120 = view.findViewById(R.id.btn_lock_120);
        btn144 = view.findViewById(R.id.btn_lock_144);
        btn165 = view.findViewById(R.id.btn_lock_165);
        Button btnUnlock = view.findViewById(R.id.btn_unlock_hz);
        tvDeviceRefreshSupport = view.findViewById(R.id.tv_device_refresh_support);

        if (btn90 != null)  btn90.setOnClickListener(v  -> applyHz(90));
        if (btn120 != null) btn120.setOnClickListener(v -> applyHz(120));
        if (btn144 != null) btn144.setOnClickListener(v -> applyHz(144));
        if (btn165 != null) btn165.setOnClickListener(v -> applyHz(165));
        if (btnUnlock != null) btnUnlock.setOnClickListener(v -> unlockHz());

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
        DisplayCapabilitiesDetector.DisplayCaps caps =
                DisplayCapabilitiesDetector.detect(getContext());

        List<Integer> recommended = caps.getRecommendedRates();
        int currentHz = caps.currentRefreshRate > 0 ? caps.currentRefreshRate : 0;
        int savedHz = DisplayRefreshRatePreferences.getSelectedHz(getContext());

        if (tvDeviceRefreshSupport != null) {
            String status = "Hardware Panel Modes: " + recommended.toString()
                    + " Hz  •  Active: " + currentHz + " Hz"
                    + (savedHz > 0 ? ("  •  Override: " + savedHz + " Hz") : "");
            tvDeviceRefreshSupport.setText(status);
        }

        // High-performance targets are available only when the exact native display mode exists.
        updateButton(btn90,  90,  recommended.contains(90));
        updateButton(btn120, 120, recommended.contains(120));
        updateButton(btn144, 144, recommended.contains(144));
        updateButton(btn165, 165, recommended.contains(165));
    }

    private void updateButton(Button btn, int hz, boolean supported) {
        if (btn == null) return;
        btn.setVisibility(supported ? View.VISIBLE : View.GONE);
    }

    private void applyHz(int hz) {
        if (getContext() == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            DisplayOverrideController.Result result =
                    DisplayOverrideController.applyDisplayRate(getContext(), hz, null);
            if (result.isSuccess()) {
                DisplayRefreshRatePreferences.saveSelectedHz(getContext(), result.selectedHz);
                GameProfileAutoConfigurator.setTargetFpsHz(getContext(), result.selectedHz);
            }

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), result.message, Toast.LENGTH_LONG).show();
                refreshSupportedRates();
            });
        });
    }

    private void unlockHz() {
        if (getContext() == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            DisplayOverrideController.Result result = DisplayOverrideController.restore(getContext());
            if (result.isSuccess()) DisplayRefreshRatePreferences.clearSelectedHz(getContext());

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), result.message, Toast.LENGTH_LONG).show();
                refreshSupportedRates();
            });
        });
    }
}
