package com.gamespace.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gamespace.app.R;
import com.gamespace.app.channels.HzFpsChannel;
import com.gamespace.app.channels.ThermalChannel;

public class HzFpsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hz_fps, container, false);

        Button btn60 = view.findViewById(R.id.btn_lock_60);
        Button btn90 = view.findViewById(R.id.btn_lock_90);
        Button btn120 = view.findViewById(R.id.btn_lock_120);
        Button btn144 = view.findViewById(R.id.btn_lock_144);
        Switch switchThermal = view.findViewById(R.id.switch_thermal);

        btn60.setOnClickListener(v -> setHz(60.0f));
        btn90.setOnClickListener(v -> setHz(90.0f));
        btn120.setOnClickListener(v -> setHz(120.0f));
        btn144.setOnClickListener(v -> setHz(144.0f));

        switchThermal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean ok = ThermalChannel.setThermalOverride(isChecked);
            if (ok) {
                Toast.makeText(getContext(), "Thermal Bypass: " + (isChecked ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Requires Shizuku or Root to override thermal caps", Toast.LENGTH_LONG).show();
                buttonView.setChecked(!isChecked);
            }
        });

        return view;
    }

    private void setHz(float hz) {
        boolean ok = HzFpsChannel.setRefreshRate(hz);
        if (ok) {
            Toast.makeText(getContext(), "Display refresh rate locked to " + (int) hz + "Hz", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Requires Shizuku or Write Settings permission to lock refresh rate", Toast.LENGTH_LONG).show();
        }
    }
}
