package com.gamebooster.app.ui.layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.widget.TextView;

import com.gamebooster.app.R;
import com.gamebooster.app.functions.PerformanceChannel;
import com.gamebooster.app.core.EngineUIHelper;

public class ProfilesFragment extends Fragment {

    private TextView tvEngineStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profiles, container, false);

        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        Button btn2D = view.findViewById(R.id.btn_apply_2d_profile);
        Button btnPubg = view.findViewById(R.id.btn_apply_pubg_profile);
        Button btnBalanced = view.findViewById(R.id.btn_apply_balanced_profile);
        Button btnBattery = view.findViewById(R.id.btn_apply_battery_profile);

        btn2D.setOnClickListener(v -> {
            if (getContext() != null) {
                boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.ULTRA_SMOOTH_2D);
                if (ok) {
                    Toast.makeText(getContext(), "2D & Pixel Games Profile Applied!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Profile could not be fully applied — check permissions", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnPubg.setOnClickListener(v -> {
            if (getContext() != null) {
                boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.EXTREME_3D_FPS);
                if (ok) {
                    Toast.makeText(getContext(), "PUBG / 3D FPS Extreme Profile Applied!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Profile could not be fully applied — check permissions", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnBalanced.setOnClickListener(v -> {
            if (getContext() != null) {
                boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.BALANCED);
                if (ok) {
                    Toast.makeText(getContext(), "Balanced Game Profile Applied!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Profile could not be fully applied — check permissions", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnBattery.setOnClickListener(v -> {
            if (getContext() != null) {
                boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.BATTERY_SAVER);
                if (ok) {
                    Toast.makeText(getContext(), "Battery Saver Gaming Profile Applied!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Profile could not be fully applied — check permissions", Toast.LENGTH_LONG).show();
                }
            }
        });

        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
    }
}
