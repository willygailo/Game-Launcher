package com.gamebooster.app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.gamespace.GameSpaceAnalyticsManager;
import com.gamebooster.app.ui.adapters.GameAnalyticsAdapter;

import java.util.List;
import java.util.Locale;

/**
 * GameAnalyticsFragment — Cyberpunk Game Space HQ & Telemetry Dashboard.
 */
public class GameAnalyticsFragment extends Fragment {

    private TextView tvTotalPlaytime;
    private TextView tvBatteryEfficiency;
    private TextView tvEmptyAnalytics;
    private RecyclerView rvAnalytics;
    private GameAnalyticsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_analytics, container, false);

        tvTotalPlaytime = view.findViewById(R.id.tv_total_playtime_summary);
        tvBatteryEfficiency = view.findViewById(R.id.tv_battery_efficiency_summary);
        tvEmptyAnalytics = view.findViewById(R.id.tv_empty_analytics);
        rvAnalytics = view.findViewById(R.id.rv_game_analytics);

        if (rvAnalytics != null && getContext() != null) {
            rvAnalytics.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new GameAnalyticsAdapter(getContext());
            rvAnalytics.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAnalyticsData();
    }

    private void loadAnalyticsData() {
        if (getContext() == null) return;

        List<GameSpaceAnalyticsManager.GameStatRecord> records = GameSpaceAnalyticsManager.getAllStats(getContext());

        if (records.isEmpty()) {
            if (tvEmptyAnalytics != null) tvEmptyAnalytics.setVisibility(View.VISIBLE);
            if (rvAnalytics != null) rvAnalytics.setVisibility(View.GONE);
            if (tvTotalPlaytime != null) tvTotalPlaytime.setText("0h 0m");
            if (tvBatteryEfficiency != null) tvBatteryEfficiency.setText("100% HEALTH");
            return;
        }

        if (tvEmptyAnalytics != null) tvEmptyAnalytics.setVisibility(View.GONE);
        if (rvAnalytics != null) rvAnalytics.setVisibility(View.VISIBLE);

        if (adapter != null) {
            adapter.setRecords(records);
        }

        // Compute Total Playtime
        long totalMs = GameSpaceAnalyticsManager.getTotalPlaytimeMillis(getContext());
        long minutes = (totalMs / 1000) / 60;
        long hours = minutes / 60;
        long remMinutes = minutes % 60;
        if (tvTotalPlaytime != null) {
            tvTotalPlaytime.setText(hours + "h " + remMinutes + "m");
        }

        // Compute Average Battery Drain Rate
        float totalDrain = 0f;
        float totalHours = (float) totalMs / (1000f * 60f * 60f);
        for (GameSpaceAnalyticsManager.GameStatRecord r : records) {
            totalDrain += r.totalBatteryPercentDrained;
        }

        if (tvBatteryEfficiency != null) {
            if (totalHours > 0.05f) {
                float avgRate = totalDrain / totalHours;
                tvBatteryEfficiency.setText(String.format(Locale.US, "%.1f%% / hr", avgRate));
            } else {
                tvBatteryEfficiency.setText("OPTIMAL");
            }
        }
    }
}
