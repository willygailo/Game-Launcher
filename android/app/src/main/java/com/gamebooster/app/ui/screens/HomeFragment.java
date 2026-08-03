package com.gamebooster.app.ui.screens;
import com.gamebooster.app.config.*;

import android.os.Build;
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
import com.gamebooster.app.games.GameManagerRepository;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvEngineMode;
    private TextView tvRamUsage;
    private TextView tvGamesHeader;
    private LinearLayout layoutEmptyState;
    private RecyclerView rvGames;
    private GamesAdapter adapter;
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
            adapter = new GamesAdapter(getContext(), gameList);
            rvGames.setAdapter(adapter);
        }

        updateStatusStrip();
        loadInstalledGames();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatusStrip();
        loadInstalledGames();
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

    private void loadInstalledGames() {
        if (getContext() == null || rvGames == null) return;

        AppExecutors.getInstance().executeScan(() -> {
            List<GameAppInfo> installedGames = GameManagerRepository.getInstalledGames(getContext());

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;

                gameList.clear();
                gameList.addAll(installedGames);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                // Update header count
                if (tvGamesHeader != null) {
                    tvGamesHeader.setText("INSTALLED GAMES (" + installedGames.size() + ")");
                }

                // Toggle empty state vs games list
                if (installedGames.isEmpty()) {
                    rvGames.setVisibility(View.GONE);
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    rvGames.setVisibility(View.VISIBLE);
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                }
            });
        });
    }
}
