package com.gamebooster.app.ui.fragments;
import com.gamebooster.app.ui.adapters.HomeGamesAdapter;
import com.gamebooster.app.ui.activities.MainActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gamebooster.app.R;
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
    private LinearLayout layoutEmptyState;
    private RecyclerView rvGames;
    private HomeGamesAdapter adapter;
    private final List<GameAppInfo> gameList = new ArrayList<>();
    private final com.gamebooster.app.shizuku.ShizukuConnectionManager.ConnectionListener connListener =
            state -> {
                if (isAdded() && getContext() != null) {
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> updateStatusStrip());
                }
            };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvEngineMode = view.findViewById(R.id.tv_engine_mode);
        tvRamUsage = view.findViewById(R.id.tv_ram_usage);
        tvGamesHeader = view.findViewById(R.id.tv_games_header);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        rvGames = view.findViewById(R.id.rv_games_list);

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

        if (rvGames != null) {
            rvGames.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new HomeGamesAdapter(getContext(), gameList);
            rvGames.setAdapter(adapter);
        }

        updateStatusStrip();
        loadAndScanGamesZeroDelay();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().addConnectionListener(connListener);
        updateStatusStrip();
        loadAndScanGamesZeroDelay();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().removeConnectionListener(connListener);
    }

    private void updateStatusStrip() {
        if (getContext() == null) return;

        // Phase 1.1: surface Shizuku reconnection states instead of a silent no-op
        com.gamebooster.app.shizuku.ShizukuConnectionManager.State conn =
                com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().getState();
        if (tvEngineMode != null
                && (conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.BINDING
                || conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.RETRY
                || conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.DEAD)) {
            tvEngineMode.setText("🔄 Shizuku reconnecting… (" + conn + ")");
            tvEngineMode.setTextColor(android.graphics.Color.parseColor("#FCA5A5"));
        } else {
            EngineMode engineMode = CommandExecutor.getActiveEngineMode();
            if (tvEngineMode != null) {
                tvEngineMode.setText("⚡ " + engineMode.getDisplayName());
                tvEngineMode.setTextColor(engineMode.getColorHex());
            }
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
