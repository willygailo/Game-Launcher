package com.gamebooster.app.ui.layout;

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
import com.gamebooster.app.core.DeviceInfoChannel;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameManagerRepository;
import com.gamebooster.app.games.GameProfileAutoConfigurator;
import com.gamebooster.app.metadata.GameBoosterService;
import com.gamebooster.app.root.CommandExecutor;
import com.gamebooster.app.root.EngineMode;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvEngineMode;
    private TextView tvDeviceInfo;
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
        tvRamUsage = view.findViewById(R.id.tv_ram_usage);
        tvBatteryTemp = view.findViewById(R.id.tv_battery_temp);
        Button btnSettings = view.findViewById(R.id.btn_open_settings);

        Switch switchOverlay = view.findViewById(R.id.switch_overlay_hud);
        Switch switchDnd = view.findViewById(R.id.switch_gaming_dnd);
        Button btnDns = view.findViewById(R.id.btn_network_dns);

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
            switchDnd.setChecked(com.gamebooster.app.functions.GameSpaceDndManager.isDndActive(getContext()));
            switchDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.functions.GameSpaceDndManager.setGamingDndMode(getContext(), isChecked);
                Toast.makeText(getContext(), "Gaming DND & Banner Blocker: " + (isChecked ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
            });
        }

        if (btnDns != null) {
            btnDns.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnDns.setEnabled(false);
                AppExecutors.getInstance().executeCommand(() -> {
                    boolean ok = com.gamebooster.app.functions.NetworkOptimizer.applyGamingDns(getContext(), com.gamebooster.app.functions.NetworkOptimizer.DnsMode.CLOUDFLARE_1_1_1_1);
                    com.gamebooster.app.functions.NetworkOptimizer.flushDnsCache();
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        btnDns.setEnabled(true);
                        if (ok) {
                            Toast.makeText(getContext(), "🌐 Cloudflare 1.1.1.1 Gaming DNS & TCP Tuned!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        // Setup Embedded Games Launcher controls
        tvGamesHeader = view.findViewById(R.id.tv_games_header);
        rvGames = view.findViewById(R.id.rv_games_list);
        TextView tvTargetLabel = view.findViewById(R.id.tv_target_fps_label);
        Button btnTarget60 = view.findViewById(R.id.btn_target_60);
        Button btnTarget90 = view.findViewById(R.id.btn_target_90);
        Button btnTarget120 = view.findViewById(R.id.btn_target_120);
        Button btnTarget144 = view.findViewById(R.id.btn_target_144);
        Button btnTarget165 = view.findViewById(R.id.btn_target_165);
        Button btnAutoConfig = view.findViewById(R.id.btn_auto_config_games);
        Button btnBoostRam = view.findViewById(R.id.btn_boost_ram);

        if (getContext() != null && tvTargetLabel != null) {
            int currentTarget = GameProfileAutoConfigurator.getTargetFpsHz(getContext());
            tvTargetLabel.setText("TARGET RATE: " + currentTarget + " FPS / HZ");
        }

        View.OnClickListener hzClickListener = v -> {
            if (getContext() == null) return;
            int targetHz = 120;
            int id = v.getId();
            if (id == R.id.btn_target_60) targetHz = 60;
            else if (id == R.id.btn_target_90) targetHz = 90;
            else if (id == R.id.btn_target_120) targetHz = 120;
            else if (id == R.id.btn_target_144) targetHz = 144;
            else if (id == R.id.btn_target_165) targetHz = 165;

            GameProfileAutoConfigurator.setTargetFpsHz(getContext(), targetHz);
            if (tvTargetLabel != null) {
                tvTargetLabel.setText("TARGET RATE: " + targetHz + " FPS / HZ");
            }
            Toast.makeText(getContext(), "Target FPS/Hz set to " + targetHz + " FPS", Toast.LENGTH_SHORT).show();
        };

        if (btnTarget60 != null) btnTarget60.setOnClickListener(hzClickListener);
        if (btnTarget90 != null) btnTarget90.setOnClickListener(hzClickListener);
        if (btnTarget120 != null) btnTarget120.setOnClickListener(hzClickListener);
        if (btnTarget144 != null) btnTarget144.setOnClickListener(hzClickListener);
        if (btnTarget165 != null) btnTarget165.setOnClickListener(hzClickListener);

        if (btnAutoConfig != null) {
            btnAutoConfig.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnAutoConfig.setEnabled(false);
                Toast.makeText(getContext(), "⚡ Auto-configuring all games for max FPS/Hz...", Toast.LENGTH_SHORT).show();

                GameProfileAutoConfigurator.autoConfigAllInstalledGamesAsync(getContext(), (count, fps) -> {
                    if (isAdded() && getContext() != null) {
                        btnAutoConfig.setEnabled(true);
                        Toast.makeText(getContext(), "✅ Auto-Configured " + count + " games to " + fps + " FPS/Hz!", Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

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
        tvRamUsage.setText("RAM Usage: " + m.ramUsagePct + "% (" + m.usedRamMb + " MB / " + m.totalRamMb + " MB)");
        tvBatteryTemp.setText("Battery Temp: " + m.batteryTempC + " °C");
    }
}
