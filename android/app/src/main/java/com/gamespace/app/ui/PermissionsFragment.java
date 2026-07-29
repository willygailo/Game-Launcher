package com.gamespace.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

    private TextView tvEngineStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);

        Button btnShizuku = view.findViewById(R.id.btn_grant_shizuku);
        Button btnSettings = view.findViewById(R.id.btn_open_settings);
        tvEngineStatus = view.findViewById(R.id.tv_root_status);

        btnShizuku.setOnClickListener(v -> {
            if (getContext() != null) {
                if (ShizukuExecutor.hasShizukuPermission()) {
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getContext());
                    Toast.makeText(getContext(), "Shizuku 1-Tap Permissions Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Please authorize GAME SPACE in Shizuku app first", Toast.LENGTH_LONG).show();
                }
                updateStatus();
            }
        });

        btnSettings.setOnClickListener(v -> {
            if (getContext() != null) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to open write settings screen", Toast.LENGTH_SHORT).show();
                }
            }
        });

        updateStatus();
        return view;
    }

    private void updateStatus() {
        EngineMode mode = CommandExecutor.getActiveEngineMode();
        tvEngineStatus.setText("Engine Access: " + mode.getDisplayName());
        tvEngineStatus.setTextColor(mode.getColorHex());
    }
}
