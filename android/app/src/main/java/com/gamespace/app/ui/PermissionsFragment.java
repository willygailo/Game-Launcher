package com.gamespace.app.ui;

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

import com.gamespace.app.R;
import com.gamespace.app.core.EngineMode;
import com.gamespace.app.data.CommandExecutor;
import com.gamespace.app.utils.ShizukuExecutor;

public class PermissionsFragment extends Fragment {

    private TextView tvRootStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);

        Button btnShizuku = view.findViewById(R.id.btn_grant_shizuku);
        tvRootStatus = view.findViewById(R.id.tv_root_status);

        btnShizuku.setOnClickListener(v -> {
            if (getContext() != null) {
                if (ShizukuExecutor.hasShizukuPermission()) {
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getContext());
                    Toast.makeText(getContext(), "Shizuku 1-Tap Permissions Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Please open Shizuku app & authorize GAME SPACE first", Toast.LENGTH_LONG).show();
                }
            }
        });

        EngineMode mode = CommandExecutor.getActiveEngineMode();
        tvRootStatus.setText("Engine Status: " + mode.getDisplayName());
        tvRootStatus.setTextColor(mode.getColorHex());

        return view;
    }
}
