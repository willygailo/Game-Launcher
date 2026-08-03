package com.gamebooster.app.ui.screens;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.DeviceInfoChannel;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameManagerRepository;
import com.gamebooster.app.games.GameProfileAutoConfigurator;
import com.gamebooster.app.services.GameBoosterService;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.EngineMode;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvEngineMode;
    private TextView tvDeviceInfo;
    private TextView tvDeviceCompatibility;
    private TextView tvRamUsage;
    private TextView tvBatteryTemp;
    private TextView tvGamesHeader;
    private RecyclerView rvGames;
    private GamesAdapter adapter;
    private final List<GameAppInfo> gameList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvEngineMode = view.findViewById(R.id.tv_engine_mode);
        tvDeviceInfo = view.findViewById(R.id.tv_device_info);
        tvDeviceCompatibility = view.findViewById(R.id.tv_device_compatibility);
        tvRamUsage = view.findViewById(R.id.tv_ram_usage);
        tvBatteryTemp = view.findViewById(R.id.tv_battery_temp);
        Button btnSettings = view.findViewById(R.id.btn_open_settings);

        Switch switchOverlay = view.findViewById(R.id.switch_overlay_hud);
        Switch switchDnd = view.findViewById(R.id.switch_gaming_dnd);

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(1); // Navigate to Settings
                }
            });
        }

        if (switchOverlay != null) {
            switchOverlay.setChecked(com.gamebooster.app.overlay.FloatingOverlayService.isOverlayRunning());
            switchOverlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(getContext())) {
                    switchOverlay.setChecked(false);
                    Toast.makeText(getContext(), "Please grant 'Draw over other apps' permission first", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(android.net.Uri.parse("package:" + getContext().getPackageName()));
                    startActivity(intent);
                    return;
                }
                if (isChecked) {
                    com.gamebooster.app.overlay.FloatingOverlayService.startOverlay(getContext());
                    Toast.makeText(getContext(), "⚡ Performance HUD Overlay Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    com.gamebooster.app.overlay.FloatingOverlayService.stopOverlay(getContext());
                    Toast.makeText(getContext(), "Overlay Disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchDnd != null) {
            switchDnd.setChecked(com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getContext()));
            switchDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.gamespace.GameSpaceDndManager.setGamingDndMode(getContext(), isChecked);
                Toast.makeText(getContext(), "Gaming DND & Banner Blocker: " + (isChecked ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
            });
        }

        // Setup Embedded Games Launcher controls
        tvGamesHeader = view.findViewById(R.id.tv_games_header);
        rvGames = view.findViewById(R.id.rv_games_list);
        Button btnBoostRam = view.findViewById(R.id.btn_boost_ram);

        if (btnBoostRam != null) {
            btnBoostRam.setOnClickListener(v -> {
                if (getContext() != null) {
                    btnBoostRam.setEnabled(false);
                    AppExecutors.getInstance().executeCommand(() -> {
                        String resultMsg = GameManagerRepository.boostRamAndOptimize(getContext());
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (isAdded() && getContext() != null) {
                                btnBoostRam.setEnabled(true);
                                Toast.makeText(getContext(), resultMsg, Toast.LENGTH_LONG).show();
                                updateDashboard();
                            }
                        });
                    });
                }
            });
        }

        if (rvGames != null) {
            rvGames.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new GamesAdapter(getContext(), gameList);
            rvGames.setAdapter(adapter);
        }

        updateDashboard();
        loadInstalledGames();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDashboard();
        loadInstalledGames();
    }

    private void loadInstalledGames() {
        if (getContext() == null || rvGames == null) return;

        AppExecutors.getInstance().executeScan(() -> {
            List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(getContext());

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;

                if (tvGamesHeader != null) {
                    tvGamesHeader.setText("🎮 INSTALLED GAMES LIBRARY (" + installedGames.size() + "):");
                }
                gameList.clear();
                gameList.addAll(installedGames);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }

    private void updateDashboard() {
        if (getContext() == null) return;

        EngineMode engineMode = CommandExecutor.getActiveEngineMode();
        tvEngineMode.setText("EXECUTION ENGINE: " + engineMode.getDisplayName());
        tvEngineMode.setTextColor(engineMode.getColorHex());

        DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(getContext());
        tvDeviceInfo.setText("Hardware: " + m.deviceSummary);
        DevicePerformanceCapabilities caps = DevicePerformanceCapabilities.detect(getContext());
        String access = engineMode == EngineMode.SHIZUKU
                ? "Advanced controls ready (Shizuku connected)"
                : "Standard controls only; connect Shizuku for supported advanced controls";
        tvDeviceCompatibility.setText("Compatibility: " + caps.getOemFamilyLabel()
                + " • Android " + Build.VERSION.RELEASE
                + "\nDisplay: " + caps.getCurrentRefreshRate() + "Hz now • "
                + caps.getSupportedRefreshRates() + "Hz supported"
                + "\nRecommended profile: " + caps.getRecommendedProfileLabel()
                + " (up to " + caps.getMaxRefreshRate() + "Hz)"
                + "\nAccess: " + access);
        tvRamUsage.setText("RAM Usage: " + m.ramUsagePct + "% (" + m.usedRamMb + " MB / " + m.totalRamMb + " MB)");
        tvBatteryTemp.setText("Battery Temp: " + m.batteryTempC + " °C");
    }
}
