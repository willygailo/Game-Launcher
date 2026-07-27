package com.gamespace.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gamespace.app.R;
import com.gamespace.app.data.CommandExecutor;

public class ProfilesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profiles, container, false);

        Button btn2D = view.findViewById(R.id.btn_apply_2d_profile);
        Button btnPubg = view.findViewById(R.id.btn_apply_pubg_profile);

        btn2D.setOnClickListener(v -> apply2DProfile());
        btnPubg.setOnClickListener(v -> applyPubgProfile());

        return view;
    }

    private void apply2DProfile() {
        CommandExecutor.setSystemProperty("windowsmgr.max_events_per_sec", "300");
        CommandExecutor.setSystemProperty("debug.sf.hw", "1");
        CommandExecutor.setSystemProperty("persist.sys.scrollingcache", "3");
        Toast.makeText(getContext(), "2D & Pixel Games Profile Applied!", Toast.LENGTH_SHORT).show();
    }

    private void applyPubgProfile() {
        CommandExecutor.setSystemProperty("debug.composition.type", "gpu");
        CommandExecutor.setSystemProperty("windowsmgr.max_events_per_sec", "300");
        CommandExecutor.setSystemProperty("debug.sf.hw", "1");
        Toast.makeText(getContext(), "PUBG Extreme Profile Applied!", Toast.LENGTH_SHORT).show();
    }
}
