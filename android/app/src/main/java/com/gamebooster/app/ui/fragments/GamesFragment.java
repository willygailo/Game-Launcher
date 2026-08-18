package com.gamebooster.app.ui.fragments;
import com.gamebooster.app.ui.adapters.GamesAdapter;
import com.gamebooster.app.config.*;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.DevicePerformanceCapabilities;
import com.gamebooster.app.core.EngineUIHelper;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.GameManagerRepository;

import java.util.ArrayList;
import java.util.List;

public class GamesFragment extends Fragment {

    private TextView tvEngineStatus;
    private TextView tvHeader;
    private RecyclerView rvGames;
    private GamesAdapter adapter;
    private final List<GameAppInfo> gameList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        Button btnBoost = view.findViewById(R.id.btn_boost_ram);
        tvHeader = view.findViewById(R.id.tv_games_header);
        rvGames = view.findViewById(R.id.rv_games_list);

        TextView tvTargetLabel = view.findViewById(R.id.tv_target_fps_label);
        Button btnTarget60 = view.findViewById(R.id.btn_target_60);
        Button btnTarget90 = view.findViewById(R.id.btn_target_90);
        Button btnTarget120 = view.findViewById(R.id.btn_target_120);
        Button btnTarget144 = view.findViewById(R.id.btn_target_144);
        Button btnTarget165 = view.findViewById(R.id.btn_target_165);
        Button btnTarget185 = view.findViewById(R.id.btn_target_185);
        Button btnAutoConfig = view.findViewById(R.id.btn_auto_config_games);

        if (getContext() != null && tvTargetLabel != null) {
            int currentTarget = com.gamebooster.app.config.GameProfileAutoConfigurator.getTargetFpsHz(getContext());
            tvTargetLabel.setText("TARGET RATE: " + currentTarget + " FPS / HZ");
            DevicePerformanceCapabilities caps = DevicePerformanceCapabilities.detect(getContext());
            setTargetRateVisible(btnTarget60, caps, 60);
            setTargetRateVisible(btnTarget90, caps, 90);
            setTargetRateVisible(btnTarget120, caps, 120);
            setTargetRateVisible(btnTarget144, caps, 144);
            setTargetRateVisible(btnTarget165, caps, 165);
            setTargetRateVisible(btnTarget185, caps, 185);
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
            else if (id == R.id.btn_target_185) targetHz = 185;

            com.gamebooster.app.config.GameProfileAutoConfigurator.setTargetFpsHz(getContext(), targetHz);
            int appliedTarget = com.gamebooster.app.config.GameProfileAutoConfigurator.getTargetFpsHz(getContext());
            if (tvTargetLabel != null) {
                tvTargetLabel.setText("TARGET RATE: " + appliedTarget + " FPS / HZ");
            }
            Toast.makeText(getContext(), "Supported game target set to " + appliedTarget + " FPS / Hz", Toast.LENGTH_SHORT).show();
        };

        if (btnTarget60 != null) btnTarget60.setOnClickListener(hzClickListener);
        if (btnTarget90 != null) btnTarget90.setOnClickListener(hzClickListener);
        if (btnTarget120 != null) btnTarget120.setOnClickListener(hzClickListener);
        if (btnTarget144 != null) btnTarget144.setOnClickListener(hzClickListener);
        if (btnTarget165 != null) btnTarget165.setOnClickListener(hzClickListener);
        if (btnTarget185 != null) btnTarget185.setOnClickListener(hzClickListener);

        if (btnAutoConfig != null) {
            btnAutoConfig.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnAutoConfig.setEnabled(false);
                Toast.makeText(getContext(), "⚡ Auto-configuring all games for max FPS/Hz...", Toast.LENGTH_SHORT).show();

                com.gamebooster.app.config.GameProfileAutoConfigurator.autoConfigAllInstalledGamesAsync(getContext(), (count, fps) -> {
                    if (isAdded() && getContext() != null) {
                        btnAutoConfig.setEnabled(true);
                        Toast.makeText(getContext(), "✅ Auto-Configured " + count + " games to " + fps + " FPS/Hz!", Toast.LENGTH_LONG).show();
                    }
                });
            });
        }

        rvGames.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GamesAdapter(getContext(), gameList);
        rvGames.setAdapter(adapter);

        btnBoost.setOnClickListener(v -> {
            if (getContext() != null) {
                btnBoost.setEnabled(false);
                AppExecutors.getInstance().executeCommand(() -> {
                    String resultMsg = GameManagerRepository.boostRamAndOptimize(getContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            btnBoost.setEnabled(true);
                            Toast.makeText(getContext(), resultMsg, Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
        });

        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        loadInstalledGames();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        loadInstalledGames();
    }

    private void loadInstalledGames() {
        if (getContext() == null) return;

        // Offload package scanning to AppExecutors scanIO pool
        AppExecutors.getInstance().executeScan(() -> {
            List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(getContext());

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;

                if (tvHeader != null) {
                    tvHeader.setText("INSTALLED GAMES DETECTED (" + installedGames.size() + "):");
                }
                gameList.clear();
                gameList.addAll(installedGames);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }

    private void setTargetRateVisible(Button button, DevicePerformanceCapabilities caps, int rate) {
        if (button != null) button.setVisibility(caps.supportsRefreshRate(rate) ? View.VISIBLE : View.GONE);
    }
}
