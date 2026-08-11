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
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.DisplayCapabilitiesDetector;
import com.gamebooster.app.device.DisplayRefreshRatePreferences;

import java.util.List;

/**
 * HzFpsFragment — Display Refresh Rate Override Control Screen.
 *
 * <p>Queries the physical display panel dynamically via {@link DisplayCapabilitiesDetector}
 * and enables selection for all panel-supported rates. Selection is persisted via
 * {@link DisplayRefreshRatePreferences} and applied via Shizuku across all 6 forcing layers.
 */
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

        if (btn60 != null)  btn60.setOnClickListener(v  -> applyHz(60));
        if (btn90 != null)  btn90.setOnClickListener(v  -> applyHz(90));
        if (btn120 != null) btn120.setOnClickListener(v -> applyHz(120));
        if (btn144 != null) btn144.setOnClickListener(v -> applyHz(144));
        if (btn165 != null) btn165.setOnClickListener(v -> applyHz(165));

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
        int maxHz = caps.maxRefreshRate > 0 ? caps.maxRefreshRate : 0;
        int currentHz = caps.currentRefreshRate > 0 ? caps.currentRefreshRate : 0;
        int savedHz = DisplayRefreshRatePreferences.getSelectedHz(getContext());

        if (tvDeviceRefreshSupport != null) {
            String status = "Hardware Panel Modes: " + recommended.toString()
                    + " Hz  •  Active: " + currentHz + " Hz"
                    + (savedHz > 0 ? ("  •  Override: " + savedHz + " Hz") : "");
            tvDeviceRefreshSupport.setText(status);
        }

        // Show buttons based on whether rate is supported by hardware panel
        updateButton(btn60,  60,  recommended.contains(60)  || maxHz >= 60);
        updateButton(btn90,  90,  recommended.contains(90)  || maxHz >= 90);
        updateButton(btn120, 120, recommended.contains(120) || maxHz >= 120);
        updateButton(btn144, 144, recommended.contains(144) || maxHz >= 144);
        updateButton(btn165, 165, recommended.contains(165) || maxHz >= 165);
    }

    private void updateButton(Button btn, int hz, boolean supported) {
        if (btn == null) return;
        btn.setVisibility(supported ? View.VISIBLE : View.GONE);
    }

    private void applyHz(int hz) {
        if (getContext() == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            MaxHzForceChannel.ForceResult result =
                    MaxHzForceChannel.forceApply(getContext(), hz, null);
            DisplayRefreshRatePreferences.saveSelectedHz(getContext(), hz);
            GameProfileAutoConfigurator.setTargetFpsHz(getContext(), hz);

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), result.message, Toast.LENGTH_LONG).show();
                refreshSupportedRates();
            });
        });
    }
}
