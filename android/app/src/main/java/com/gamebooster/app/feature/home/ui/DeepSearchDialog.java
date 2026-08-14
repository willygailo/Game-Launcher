package com.gamebooster.app.feature.home.ui;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.feature.games.GameLauncherHelper;
import com.gamebooster.app.feature.games.search.DeepSearchScanner;
import com.gamebooster.app.feature.games.search.DiscoveredGameItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeepSearchDialog extends BottomSheetDialogFragment {

    public interface OnGamesUpdatedListener {
        void onGamesUpdated();
    }

    private ProgressBar progressBar;
    private TextView tvStatus;
    private TextView tvCount;
    private EditText etFilter;
    private RecyclerView rvResults;
    private DeepSearchAdapter adapter;
    private final List<DiscoveredGameItem> allDiscovered = new ArrayList<>();
    private final List<DiscoveredGameItem> filteredList = new ArrayList<>();
    private OnGamesUpdatedListener listener;

    public void setOnGamesUpdatedListener(OnGamesUpdatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_deep_search, container, false);

        progressBar = v.findViewById(R.id.pb_deep_search);
        tvStatus = v.findViewById(R.id.tv_scan_stage_status);
        tvCount = v.findViewById(R.id.tv_discovered_count);
        etFilter = v.findViewById(R.id.et_deep_search_filter);
        rvResults = v.findViewById(R.id.rv_deep_search_results);

        v.findViewById(R.id.btn_close_deep_search).setOnClickListener(view -> dismiss());
        v.findViewById(R.id.btn_rescan_deep_search).setOnClickListener(view -> startScan());

        v.findViewById(R.id.btn_batch_add_all).setOnClickListener(view -> {
            if (getContext() == null) return;
            for (DiscoveredGameItem item : allDiscovered) {
                if (!item.isAddedToLibrary()) {
                    GameLauncherHelper.addCustomPackage(getContext(), item.getPackageName());
                    item.setAddedToLibrary(true);
                }
            }
            adapter.notifyDataSetChanged();
            if (listener != null) listener.onGamesUpdated();
            Toast.makeText(getContext(), "✅ Added " + allDiscovered.size() + " games to library!", Toast.LENGTH_SHORT).show();
        });

        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DeepSearchAdapter(getContext(), item -> {
            if (getContext() != null) {
                GameLauncherHelper.addCustomPackage(getContext(), item.getPackageName());
                if (listener != null) listener.onGamesUpdated();
                Toast.makeText(getContext(), "Added " + item.getLabel() + " to Game Launcher!", Toast.LENGTH_SHORT).show();
            }
        });
        rvResults.setAdapter(adapter);

        etFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterResults(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        startScan();
        return v;
    }

    private void startScan() {
        if (getContext() == null) return;
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Scanning device with Shizuku multi-user ADB & storage heuristics...");
        allDiscovered.clear();

        AppExecutors.getInstance().executeCommand(() -> {
            Set<String> discoveredPkgs = DeepSearchScanner.performDeepSearch(getContext());
            PackageManager pm = getContext().getPackageManager();

            for (String pkg : discoveredPkgs) {
                String label = pkg;
                Drawable icon = null;
                try {
                    ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                    label = pm.getApplicationLabel(ai).toString();
                    icon = pm.getApplicationIcon(ai);
                } catch (Throwable ignored) {}

                DiscoveredGameItem.EngineType engineType = DiscoveredGameItem.EngineType.STANDARD_GAME;
                String lower = pkg.toLowerCase();
                if (lower.contains("pubg") || lower.contains("fortnite") || lower.contains("bloodstrike")) {
                    engineType = DiscoveredGameItem.EngineType.UNREAL;
                } else if (lower.contains("mobile.legends") || lower.contains("genshin") || lower.contains("codm")) {
                    engineType = DiscoveredGameItem.EngineType.UNITY;
                } else if (lower.contains("ppsspp") || lower.contains("aether") || lower.contains("nether") || lower.contains("dolphin") || lower.contains("winlator")) {
                    engineType = DiscoveredGameItem.EngineType.EMULATOR;
                }

                allDiscovered.add(new DiscoveredGameItem(pkg, label, icon, engineType, "SHIZUKU SCAN"));
            }

            AppExecutors.getInstance().postToMainThread(() -> {
                progressBar.setVisibility(View.GONE);
                tvStatus.setText("Scan complete! Discovered " + allDiscovered.size() + " games.");
                tvCount.setText("DISCOVERED: " + allDiscovered.size() + " GAMES");
                filterResults(etFilter != null ? etFilter.getText().toString() : "");
            });
        });
    }

    private void filterResults(String query) {
        filteredList.clear();
        String q = query.trim().toLowerCase();
        for (DiscoveredGameItem item : allDiscovered) {
            if (q.isEmpty() || item.getLabel().toLowerCase().contains(q) || item.getPackageName().toLowerCase().contains(q)) {
                filteredList.add(item);
            }
        }
        adapter.setItems(filteredList);
    }
}
