package com.gamebooster.app.ui.screens;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.device.DeviceInfoChannel;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.engine.EngineMode;
import com.gamebooster.app.games.GameAppInfo;
import com.gamebooster.app.games.HomeGameScanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private static final String PREF_HOME_CACHE = "home_game_cache_prefs";
    private static final String KEY_CACHED_PACKAGES = "cached_home_packages";

    private TextView tvEngineMode;
    private TextView tvRamUsage;
    private TextView tvGamesHeader;
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
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        rvGames = view.findViewById(R.id.rv_games_list);

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
        updateStatusStrip();
        loadAndScanGamesZeroDelay();
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
            tvGamesHeader.setText("SUPPORTED GAMES (" + scannedGames.size() + " DETECTED)");
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
