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
        Button btnExtreme = view.findViewById(R.id.btn_apply_pubg_profile);
        Button btnPerformance = view.findViewById(R.id.btn_apply_2d_profile);
        Button btnBalanced = view.findViewById(R.id.btn_apply_balanced_profile);

        if (btnExtreme != null) {
            btnExtreme.setOnClickListener(v -> {
                if (getContext() != null) {
                    boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                    if (ok) {
                        Toast.makeText(getContext(), "🔥 Extreme Performance Profile Applied!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Profile could not be fully applied", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        if (btnPerformance != null) {
            btnPerformance.setOnClickListener(v -> {
                if (getContext() != null) {
                    boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.PERFORMANCE);
                    if (ok) {
                        Toast.makeText(getContext(), "⚡ Performance Profile Applied!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Profile could not be fully applied", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        if (btnBalanced != null) {
            btnBalanced.setOnClickListener(v -> {
                if (getContext() != null) {
                    boolean ok = PerformanceChannel.applyProfile(getContext(), PerformanceChannel.Profile.BALANCED);
                    if (ok) {
                        Toast.makeText(getContext(), "⚖️ Balanced Game Profile Applied!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Profile could not be fully applied", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
    }
}
