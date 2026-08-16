package com.gamebooster.app.ui.screens;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gamebooster.app.R;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.DeviceInfoChannel;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.EngineMode;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.HomeGameScanner;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvEngineMode;
    private TextView tvRamUsage;
    private TextView tvGamesHeader;
    private TextView tvTargetHzBadge;
    private Button btnTarget120;
    private Button btnTarget144;
    private Button btnTarget165;
    private Button btnTarget185;
    private LinearLayout layoutEmptyState;
    private RecyclerView rvGames;
    private HomeGamesAdapter adapter;
    private final List<GameAppInfo> gameList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvEngineMode = view.findViewById(R.id.tv_engine_mode);
        tvRamUsage = view.findViewById(R.id.tv_ram_usage);
        tvGamesHeader = view.findViewById(R.id.tv_games_header);
        tvTargetHzBadge = view.findViewById(R.id.tv_home_target_hz_badge);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        rvGames = view.findViewById(R.id.rv_games_list);

        btnTarget120 = view.findViewById(R.id.btn_home_target_120);
        btnTarget144 = view.findViewById(R.id.btn_home_target_144);
        btnTarget165 = view.findViewById(R.id.btn_home_target_165);
        btnTarget185 = view.findViewById(R.id.btn_home_target_185);

        ImageView ivHeroBanner = view.findViewById(R.id.iv_hero_banner);
        if (ivHeroBanner != null && getContext() != null) {
            Glide.with(this).load(R.drawable.hero_banner).into(ivHeroBanner);
        }

        Button btnSettings = view.findViewById(R.id.btn_open_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(1);
                }
            });
        }

        setupTargetRateButtons();

        if (rvGames != null) {
            rvGames.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new HomeGamesAdapter(getContext(), gameList);
            rvGames.setAdapter(adapter);
        }

        updateStatusStrip();
        updateTargetRateUI(GameProfileAutoConfigurator.getTargetFpsHz(getContext()));
        loadAndScanGamesZeroDelay();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatusStrip();
        updateTargetRateUI(GameProfileAutoConfigurator.getTargetFpsHz(getContext()));
        loadAndScanGamesZeroDelay();
    }

    private void setupTargetRateButtons() {
        if (btnTarget120 != null) {
            btnTarget120.setOnClickListener(v -> applyTargetRate(120));
        }
        if (btnTarget144 != null) {
            btnTarget144.setOnClickListener(v -> applyTargetRate(144));
        }
        if (btnTarget165 != null) {
            btnTarget165.setOnClickListener(v -> applyTargetRate(165));
        }
        if (btnTarget185 != null) {
            btnTarget185.setOnClickListener(v -> applyTargetRate(185));
        }
    }

    private void applyTargetRate(int targetHz) {
        if (getContext() == null) return;

        updateTargetRateUI(targetHz);
        Toast.makeText(getContext(), "⚡ Forcing " + targetHz + " FPS/Hz to all games & display...", Toast.LENGTH_SHORT).show();

        GameProfileAutoConfigurator.autoConfigAllGamesAsync(getContext(), targetHz, (count, fps) -> {
            if (!isAdded() || getContext() == null) return;
            Toast.makeText(getContext(), "✅ " + fps + " FPS/Hz applied to " + count + " games & display!", Toast.LENGTH_SHORT).show();
            loadAndScanGamesZeroDelay();
        });
    }

    private void updateTargetRateUI(int targetHz) {
        if (tvTargetHzBadge != null) {
            tvTargetHzBadge.setText("⚡ " + targetHz + "Hz ACTIVE");
        }

        if (btnTarget120 != null) {
            boolean active = (targetHz == 120);
            btnTarget120.setBackgroundResource(active ? R.drawable.btn_cyber_green : R.drawable.btn_cyber_dark);
            btnTarget120.setTextColor(active ? Color.parseColor("#0B0E14") : Color.parseColor("#00FF66"));
        }

        if (btnTarget144 != null) {
            boolean active = (targetHz == 144);
            btnTarget144.setBackgroundResource(active ? R.drawable.btn_cyber_cyan : R.drawable.btn_cyber_dark);
            btnTarget144.setTextColor(active ? Color.parseColor("#000000") : Color.parseColor("#00F0FF"));
        }

        if (btnTarget165 != null) {
            boolean active = (targetHz == 165);
            btnTarget165.setBackgroundResource(active ? R.drawable.btn_cyber_cyan : R.drawable.btn_cyber_dark);
            btnTarget165.setTextColor(active ? Color.parseColor("#000000") : Color.parseColor("#00F0FF"));
        }

        if (btnTarget185 != null) {
            boolean active = (targetHz == 185);
            btnTarget185.setBackgroundResource(active ? R.drawable.btn_cyber_green : R.drawable.btn_cyber_dark);
            btnTarget185.setTextColor(active ? Color.parseColor("#0B0E14") : Color.parseColor("#00FF66"));
        }
    }

    private void updateStatusStrip() {
        if (getContext() == null) return;

        EngineMode engineMode = CommandExecutor.getActiveEngineMode();
        if (tvEngineMode != null) {
            tvEngineMode.setText("⚡ " + engineMode.getDisplayName());
            tvEngineMode.setTextColor(engineMode.getColorHex());
        }

        if (tvRamUsage != null) {
            DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(getContext());
            tvRamUsage.setText("RAM: " + m.ramUsagePct + "% (" + m.usedRamMb + "/" + m.totalRamMb + " MB)");
        }
    }

    /**
     * Zero-Delay Architecture:
     * 1. Synchronously perform fast ML/PUBG/CODM target scan (~10ms)
     * 2. Update UI instantly with zero flicker
     */
    private void loadAndScanGamesZeroDelay() {
        if (getContext() == null || rvGames == null) return;

        // Fast target scan (scans ONLY MLBB, PUBG, CODM packages directly)
        List<GameAppInfo> scannedGames = HomeGameScanner.scanTargetGames(getContext());

        gameList.clear();
        gameList.addAll(scannedGames);

        if (adapter != null) {
            adapter.updateList(scannedGames);
        }

        // Update header count
        if (tvGamesHeader != null) {
            tvGamesHeader.setText("INSTALLED GAMES (" + scannedGames.size() + " DETECTED)");
        }

        // Toggle empty state vs games list
        if (scannedGames.isEmpty()) {
            rvGames.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvGames.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        }
    }
}
