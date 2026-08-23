package com.gamebooster.app.ui.fragments;
import com.gamebooster.app.ui.adapters.HomeGamesAdapter;
import com.gamebooster.app.ui.activities.MainActivity;

import android.content.Context;
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
import com.gamebooster.app.shizuku.ShizukuExecutor;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import com.gamebooster.app.shizuku.ShizukuFileManager;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;

public class HomeFragment extends Fragment implements ShizukuManager.ShizukuStateListener {

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
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(this::updateStatusStrip);
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

        View chipEngineMode = view.findViewById(R.id.chip_engine_mode);
        if (chipEngineMode != null) {
            chipEngineMode.setOnClickListener(v -> {
                if (getContext() == null) return;
                if (ShizukuExecutor.hasShizukuPermission() || ShizukuManager.isShizukuRunningAndGranted()) {
                    Toast.makeText(getContext(), "⚡ Shizuku API Full Access is Active & Privileged!", Toast.LENGTH_SHORT).show();
                    com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                        ShizukuPermissionEnforcer.enforceAllPermissionsForAllApps(getContext().getApplicationContext());
                        ShizukuExecutor.grantAppPermissionsViaShizuku(getContext().getApplicationContext());
                        ShizukuFileManager.grantAllStoragePermissions(getContext().getApplicationContext());
                    });
                } else if (ShizukuExecutor.isShizukuAvailable()) {
                    Toast.makeText(getContext(), "⚡ Requesting Shizuku Permission...", Toast.LENGTH_SHORT).show();
                    ShizukuManager.requestShizukuPermission();
                } else {
                    ShizukuManager.showShizukuPermissionDialog(getContext(), "Full Shizuku API Engine");
                }
                updateStatusStrip();
            });
        }

        ImageView ivHeroBanner = view.findViewById(R.id.iv_hero_banner);
        if (ivHeroBanner != null && getContext() != null && isAdded()) {
            try {
                Glide.with(this)
                        .load(R.drawable.hero_banner)
                        .error(R.drawable.home_bg_new)
                        .into(ivHeroBanner);
            } catch (Throwable t) {
                ivHeroBanner.setImageResource(R.drawable.home_bg_new);
            }
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
            rvGames.setHasFixedSize(true);
            rvGames.setItemViewCacheSize(25);
            rvGames.setItemAnimator(null);
            adapter = new HomeGamesAdapter(getContext(), gameList);
            rvGames.setAdapter(adapter);
        }

        updateStatusStrip();
        loadAndScanGamesZeroDelay();
        return view;
    }

    private volatile boolean isScanning = false;
    private volatile long lastScanTime = 0L;
    private static final long SCAN_DEBOUNCE_MS = 2500L;

    @Override
    public void onResume() {
        super.onResume();
        com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().addConnectionListener(connListener);
        ShizukuManager.addStateListener(this);
        updateStatusStrip();
        loadAndScanGamesZeroDelay();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            updateStatusStrip();
            loadAndScanGamesZeroDelay();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().removeConnectionListener(connListener);
        ShizukuManager.removeStateListener(this);
    }

    @Override
    public void onBinderStateChanged(boolean alive) {
        if (isAdded() && getContext() != null) {
            com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(this::updateStatusStrip);
        }
    }

    private void updateStatusStrip() {
        if (!isAdded() || getContext() == null) return;

        com.gamebooster.app.shizuku.ShizukuConnectionManager.State conn =
                com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().getState();
        boolean isShizukuActive = ShizukuExecutor.hasShizukuPermission()
                || com.gamebooster.app.shizuku.ShizukuManager.isShizukuRunningAndGranted()
                || com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().isReady()
                || conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.READY;

        if (isShizukuActive) {
            if (tvEngineMode != null) {
                tvEngineMode.setText("⚡ FULL ACCESS: SHIZUKU API ACTIVE");
                tvEngineMode.setTextColor(android.graphics.Color.parseColor("#00FF66"));
            }
        } else if (ShizukuExecutor.isShizukuAvailable()) {
            if (tvEngineMode != null) {
                tvEngineMode.setText("⚡ SHIZUKU API: GRANT PERMISSION");
                tvEngineMode.setTextColor(android.graphics.Color.parseColor("#FFCC00"));
            }
        } else if (tvEngineMode != null
                && (conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.BINDING
                || conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.RETRY)) {
            tvEngineMode.setText("🔄 Shizuku connecting…");
            tvEngineMode.setTextColor(android.graphics.Color.parseColor("#FCA5A5"));
        } else if (tvEngineMode != null && conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.DEAD) {
            tvEngineMode.setText("⚠️ Shizuku Disconnected");
            tvEngineMode.setTextColor(android.graphics.Color.parseColor("#EF4444"));
        } else {
            EngineMode engineMode = CommandExecutor.getActiveEngineMode();
            if (tvEngineMode != null) {
                tvEngineMode.setText("⚡ " + engineMode.getDisplayName());
                tvEngineMode.setTextColor(engineMode.getColorHex());
            }
        }

        if (tvRamUsage != null && getContext() != null) {
            DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(getContext());
            tvRamUsage.setText("RAM: " + m.ramUsagePct + "% (" + m.usedRamMb + "/" + m.totalRamMb + " MB)");
        }
    }

    /**
     * Non-Blocking Architecture:
     * 1. If cached games exist, display them instantly
     * 2. Perform fresh scan asynchronously on background scan thread
     * 3. Update UI on the main thread with zero freeze / flicker
     */
    private void loadAndScanGamesZeroDelay() {
        if (!isAdded() || getContext() == null) return;
        final Context ctx = getContext().getApplicationContext();

        // If games already in memory, display them right away
        if (!gameList.isEmpty()) {
            if (adapter != null) {
                adapter.updateList(gameList);
            }
            if (tvGamesHeader != null) {
                tvGamesHeader.setText("INSTALLED GAMES (" + gameList.size() + " DETECTED)");
            }
            if (rvGames != null) rvGames.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        }

        long now = System.currentTimeMillis();
        if (isScanning || (now - lastScanTime < SCAN_DEBOUNCE_MS)) {
            return;
        }

        isScanning = true;
        lastScanTime = now;

        com.gamebooster.app.core.AppExecutors.getInstance().executeScan(() -> {
            try {
                List<GameAppInfo> scannedGames = HomeGameScanner.scanTargetGames(ctx);

                com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                    isScanning = false;
                    if (!isAdded() || getContext() == null) return;

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
                        if (rvGames != null) rvGames.setVisibility(View.GONE);
                        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        if (rvGames != null) rvGames.setVisibility(View.VISIBLE);
                        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                    }
                });
            } catch (Throwable t) {
                isScanning = false;
            }
        });
    }
}
