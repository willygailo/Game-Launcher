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
import com.gamespace.app.utils.EngineUIHelper;
import com.gamespace.app.utils.ShizukuExecutor;
import com.gamespace.app.utils.ShizukuUtils;

public class PermissionsFragment extends Fragment {

    private TextView tvEngineStatus;
    private TextView tvSystemSettingsStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);

        Button btnShizuku = view.findViewById(R.id.btn_grant_shizuku);
        Button btnSettings = view.findViewById(R.id.btn_open_settings);
        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        tvSystemSettingsStatus = view.findViewById(R.id.tv_root_status);

        btnShizuku.setOnClickListener(v -> {
            if (getContext() != null) {
                if (ShizukuExecutor.hasShizukuPermission()) {
                    ShizukuExecutor.grantAppPermissionsViaShizuku(getContext());
                    Toast.makeText(getContext(), "Shizuku 1-Tap Permissions Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    ShizukuUtils.openOrInstallShizukuManager(getContext());
                }
                EngineUIHelper.refreshEngineStatus(tvEngineStatus);
                updateSystemSettingsStatus();
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

        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        updateSystemSettingsStatus();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        updateSystemSettingsStatus();
    }

    private void updateSystemSettingsStatus() {
        if (tvSystemSettingsStatus == null || getContext() == null) return;
        boolean canWrite = Settings.System.canWrite(getContext());
        if (canWrite) {
            tvSystemSettingsStatus.setText("WRITE_SETTINGS Permission: GRANTED");
            tvSystemSettingsStatus.setTextColor(0xFF00FF66);
        } else {
            tvSystemSettingsStatus.setText("WRITE_SETTINGS Permission: REQUIRED");
            tvSystemSettingsStatus.setTextColor(0xFFFFB800);
        }
    }
}
