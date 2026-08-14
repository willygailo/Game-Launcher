package com.gamebooster.app.feature.home.ui;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.feature.performance.device.DeviceInfoChannel;
import com.gamebooster.app.platform.shell.CommandExecutor;
import com.gamebooster.app.feature.performance.display.EngineMode;
import com.gamebooster.app.feature.games.GameAppInfo;
import com.gamebooster.app.feature.games.GameLauncherHelper;
import com.gamebooster.app.feature.games.HomeGameScanner;
import com.gamebooster.app.platform.shizuku.ShizukuExecutor;
import com.gamebooster.app.app.MainActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvEngineSubtitle;
    private FrameLayout layoutEngineRing;
    private TextView tvLightningBolt;
    private TextView tvBoostSub;

    private TextView tvCpuVal;
    private ProgressBar pbCpu;
    private TextView tvGpuVal;
    private ProgressBar pbGpu;
    private TextView tvRamVal;
    private ProgressBar pbRam;
    private TextView tvPingVal;
    private ProgressBar pbPing;

    private TextView tvGamesHeader;
    private LinearLayout layoutEmptyState;
    private RecyclerView rvGames;
    private HomeGamesAdapter adapter;
    private final List<GameAppInfo> gameList = new ArrayList<>();

    private Button btnLaunchHero;
    private TextView btnGameMode;
    private TextView btnNetworkBoost;

    private final Handler telemetryHandler = new Handler(Looper.getMainLooper());
    private Runnable telemetryRunnable;
    private int currentModeIndex = 0;
    private final String[] gameModes = {"Pro Gamer ▾", "Extreme Boost ▾", "Balanced ▾"};
    private boolean networkBoostOn = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvEngineSubtitle = view.findViewById(R.id.tv_engine_status_subtitle);
        layoutEngineRing = view.findViewById(R.id.layout_engine_ring);
        tvLightningBolt = view.findViewById(R.id.tv_lightning_bolt);
        tvBoostSub = view.findViewById(R.id.tv_boost_status_sub);

        tvCpuVal = view.findViewById(R.id.tv_cpu_val);
        pbCpu = view.findViewById(R.id.pb_cpu);
        tvGpuVal = view.findViewById(R.id.tv_gpu_val);
        pbGpu = view.findViewById(R.id.pb_gpu);
        tvRamVal = view.findViewById(R.id.tv_ram_val);
        pbRam = view.findViewById(R.id.pb_ram);
        tvPingVal = view.findViewById(R.id.tv_ping_val);
        pbPing = view.findViewById(R.id.pb_ping);

        tvGamesHeader = view.findViewById(R.id.tv_games_header);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        rvGames = view.findViewById(R.id.rv_games_list);

        btnLaunchHero = view.findViewById(R.id.btn_launch_game_hero);
        btnGameMode = view.findViewById(R.id.btn_game_mode);
        btnNetworkBoost = view.findViewById(R.id.btn_network_boost);

        View btnSettings = view.findViewById(R.id.btn_open_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(1);
                }
            });
        }

        View btnRefresh = view.findViewById(R.id.btn_refresh_scan);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                Toast.makeText(getContext(), "🔄 Rescanning installed games...", Toast.LENGTH_SHORT).show();
                loadAndScanGamesZeroDelay();
            });
        }

        View btnDeepSearch = view.findViewById(R.id.btn_deep_search);
        if (btnDeepSearch != null) {
            btnDeepSearch.setOnClickListener(v -> {
                DeepSearchDialog dialog = new DeepSearchDialog();
                dialog.setOnGamesUpdatedListener(this::loadAndScanGamesZeroDelay);
                dialog.show(getParentFragmentManager(), "DeepSearchDialog");
            });
        }

        // Horizontal Carousel Recycler Setup with LinearSnapHelper
        if (rvGames != null) {
            LinearLayoutManager lm = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            rvGames.setLayoutManager(lm);

            LinearSnapHelper snapHelper = new LinearSnapHelper();
            rvGames.setOnFlingListener(null);
            snapHelper.attachToRecyclerView(rvGames);

            adapter = new HomeGamesAdapter(getContext(), gameList);
            adapter.setOnGameSelectedListener((game, position) -> updateHeroLaunchButton(game));
            rvGames.setAdapter(adapter);

            rvGames.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        View snapView = snapHelper.findSnapView(lm);
                        if (snapView != null) {
                            int pos = lm.getPosition(snapView);
                            adapter.setSelectedPosition(pos);
                        }
                    }
                }
            });
        }

        // Central Boost Dial Ring Interaction
        if (layoutEngineRing != null) {
            layoutEngineRing.setOnClickListener(v -> performBoostEngineAction());
        }

        // Hero Launch Game Button Interaction
        if (btnLaunchHero != null) {
            btnLaunchHero.setOnClickListener(v -> launchActiveSelectedGame());
        }

        // Game Mode Toggle
        if (btnGameMode != null) {
            btnGameMode.setOnClickListener(v -> {
                currentModeIndex = (currentModeIndex + 1) % gameModes.length;
                btnGameMode.setText("🎮 Game Mode\n" + gameModes[currentModeIndex]);
                Toast.makeText(getContext(), "🎮 Game Mode set to " + gameModes[currentModeIndex].replace(" ▾", ""), Toast.LENGTH_SHORT).show();
            });
        }

        // Network Boost Toggle
        if (btnNetworkBoost != null) {
            btnNetworkBoost.setOnClickListener(v -> {
                networkBoostOn = !networkBoostOn;
                btnNetworkBoost.setText("📡 Network Boost\n" + (networkBoostOn ? "On ▾" : "Off ▾"));
                btnNetworkBoost.setTextColor(networkBoostOn ? 0xFF00FF66 : 0x80FFFFFF);
                Toast.makeText(getContext(), networkBoostOn ? "⚡ Network Acceleration & Low Latency ON" : "Network Boost OFF", Toast.LENGTH_SHORT).show();
            });
        }

        updateStatusStrip();
        loadAndScanGamesZeroDelay();
        startTelemetryUpdates();

        return view;
    }

    private void updateHeroLaunchButton(GameAppInfo game) {
        if (btnLaunchHero != null) {
            if (game != null) {
                btnLaunchHero.setText("LAUNCH " + game.getLabel().toUpperCase());
            } else {
                btnLaunchHero.setText("LAUNCH GAME");
            }
        }
    }

    private void launchActiveSelectedGame() {
        if (adapter == null) return;
        GameAppInfo selectedGame = adapter.getSelectedGame();
        if (selectedGame != null && getContext() != null) {
            Toast.makeText(getContext(), "🚀 Launching " + selectedGame.getLabel() + " with Extreme Hardware Engine...", Toast.LENGTH_SHORT).show();
            GameLauncherHelper.launchGameWithAutoBoost(getContext(), selectedGame);
        } else {
            Toast.makeText(getContext(), "🎯 No target game selected. Scan or install a game to launch.", Toast.LENGTH_SHORT).show();
        }
    }

    private void performBoostEngineAction() {
        if (layoutEngineRing != null) {
            ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                    layoutEngineRing,
                    PropertyValuesHolder.ofFloat("scaleX", 0.92f, 1.05f, 1.0f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.92f, 1.05f, 1.0f)
            );
            scaleDown.setDuration(400);
            scaleDown.start();
        }

        if (tvBoostSub != null) tvBoostSub.setText("BOOSTING...");

        AppExecutors.getInstance().executeCommand(() -> {
            com.gamebooster.app.feature.performance.booster.RamZramChannel.MemoryStats stats =
                    com.gamebooster.app.feature.performance.booster.RamZramChannel.optimizeMemory(getContext());
            com.gamebooster.app.feature.performance.booster.CpuGovernorChannel.setPerformanceLock();

            AppExecutors.getInstance().postToMainThread(() -> {
                if (tvBoostSub != null) {
                    if (stats.freedRamMb > 0) {
                        tvBoostSub.setText("+" + stats.freedRamMb + "MB FREED");
                    } else {
                        tvBoostSub.setText("MAX BOOSTED");
                    }
                }
                if (getContext() != null) {
                    String msg = "⚡ HARDWARE ENGINE BOOSTED!\n" +
                            (stats.freedRamMb > 0 ? "Freed " + stats.freedRamMb + "MB RAM (" + stats.availRamMbAfter + "MB Free)"
                                                  : "Memory trimmed & CPU Governor locked to Max Performance!");
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (tvBoostSub != null) tvBoostSub.setText("Tap to Boost");
                }, 3000);
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatusStrip();
        loadAndScanGamesZeroDelay();
        startTelemetryUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTelemetryUpdates();
    }

    private void updateStatusStrip() {
        if (getContext() == null) return;
        EngineMode engineMode = CommandExecutor.getActiveEngineMode();
        if (tvEngineSubtitle != null) {
            tvEngineSubtitle.setText("Mode: " + engineMode.getDisplayName() + " • Extreme Hardware Engine Active");
        }
    }

    private void startTelemetryUpdates() {
        stopTelemetryUpdates();
        telemetryRunnable = new Runnable() {
            @Override
            public void run() {
                updateTelemetryMetrics();
                telemetryHandler.postDelayed(this, 1000);
            }
        };
        telemetryHandler.post(telemetryRunnable);
    }

    private void stopTelemetryUpdates() {
        if (telemetryRunnable != null) {
            telemetryHandler.removeCallbacks(telemetryRunnable);
        }
    }

    private void updateTelemetryMetrics() {
        if (getContext() == null) return;

        AppExecutors.getInstance().executeCommand(() -> {
            DeviceInfoChannel.Metrics metrics = DeviceInfoChannel.getMetrics(getContext());
            int cpuPct = metrics.cpuUsagePct;
            int gpuPct = metrics.gpuUsagePct;
            int ramPct = metrics.ramUsagePct;

            AppExecutors.getInstance().postToMainThread(() -> {
                if (tvCpuVal != null) tvCpuVal.setText(cpuPct + "%");
                if (pbCpu != null) pbCpu.setProgress(cpuPct);

                if (tvGpuVal != null) tvGpuVal.setText(gpuPct + "%");
                if (pbGpu != null) pbGpu.setProgress(gpuPct);

                if (tvRamVal != null) tvRamVal.setText(ramPct + "%");
                if (pbRam != null) pbRam.setProgress(ramPct);
            });
        });

        AppExecutors.getInstance().executeScan(() -> {
            int pingMs = DeviceInfoChannel.measureRealPingMs();
            AppExecutors.getInstance().postToMainThread(() -> {
                if (tvPingVal != null) tvPingVal.setText(pingMs + "ms");
                if (pbPing != null) pbPing.setProgress(Math.min(100, pingMs));
            });
        });
    }

    private void loadAndScanGamesZeroDelay() {
        if (getContext() == null || rvGames == null) return;

        List<GameAppInfo> scannedGames = HomeGameScanner.scanTargetGames(getContext());
        gameList.clear();
        gameList.addAll(scannedGames);

        if (adapter != null) {
            adapter.updateList(scannedGames);
        }

        if (tvGamesHeader != null) {
            tvGamesHeader.setText(scannedGames.size() + " GAMES DETECTED");
        }

        if (scannedGames.isEmpty()) {
            rvGames.setVisibility(View.GONE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
            updateHeroLaunchButton(null);
        } else {
            rvGames.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
            if (adapter != null) {
                updateHeroLaunchButton(adapter.getSelectedGame());
            }
        }
    }
}
