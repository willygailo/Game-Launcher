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
    private TextView tvSocDetectedName;
    private TextView tvOemBypassStatus;
    private TextView tvChipsetOemBadge;
    private Button btnDexoptSpeedCompile;
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
        tvSocDetectedName = view.findViewById(R.id.tv_soc_detected_name);
        tvOemBypassStatus = view.findViewById(R.id.tv_oem_bypass_status);
        tvChipsetOemBadge = view.findViewById(R.id.tv_chipset_oem_badge);
        btnDexoptSpeedCompile = view.findViewById(R.id.btn_dexopt_speed_compile);
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

        if (btnDexoptSpeedCompile != null) {
            btnDexoptSpeedCompile.setOnClickListener(v -> handleDexoptSpeedCompile());
        }

        setupTargetRateButtons();

        if (rvGames != null) {
            rvGames.setLayoutManager(new LinearLayoutManager(getContext()));
            rvGames.setHasFixedSize(true);
            rvGames.setNestedScrollingEnabled(false);
            rvGames.setItemViewCacheSize(20);
            adapter = new HomeGamesAdapter(getContext(), gameList);
            rvGames.setAdapter(adapter);
        }

        updateStatusStrip();
        updateTargetRateUI(GameProfileAutoConfigurator.getTargetFpsHz(getContext()));
        loadAndScanGamesAsync();
        return view;
    }


    private void handleDexoptSpeedCompile() {
        if (getContext() == null) return;
        if (com.gamebooster.app.dexopt.DexoptCompilationEngine.isCompiling()) {
            Toast.makeText(getContext(), "⚡ Compilation in progress...", Toast.LENGTH_SHORT).show();
            return;
        }

        btnDexoptSpeedCompile.setEnabled(false);
        btnDexoptSpeedCompile.setText("⏳ Compiling DEX & Shaders...");
        Toast.makeText(getContext(), "⚡ Starting AOT Speed Compilation across installed games...", Toast.LENGTH_SHORT).show();

        com.gamebooster.app.dexopt.DexoptCompilationEngine.compileAllGamesSpeedAsync(getContext(), new com.gamebooster.app.dexopt.DexoptCompilationEngine.CompileCallback() {
            @Override
            public void onProgress(String packageName, int current, int total) {
                if (btnDexoptSpeedCompile != null && isAdded()) {
                    btnDexoptSpeedCompile.setText("⏳ Compiling [" + current + "/" + total + "]...");
                }
            }

            @Override
            public void onComplete(boolean success, String message) {
                if (btnDexoptSpeedCompile != null && isAdded()) {
                    btnDexoptSpeedCompile.setEnabled(true);
                    btnDexoptSpeedCompile.setText("⚡ AOT Speed Compile (Zero In-Game JIT Stutter)");
                }
                if (getContext() != null) {
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatusStrip();
        updateTargetRateUI(GameProfileAutoConfigurator.getTargetFpsHz(getContext()));
        loadAndScanGamesAsync();
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
            loadAndScanGamesAsync();
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
        final Context ctx = getContext().getApplicationContext();

        AppExecutors.getInstance().executeScan(() -> {
            EngineMode engineMode = CommandExecutor.getActiveEngineMode();
            DeviceInfoChannel.Metrics m = DeviceInfoChannel.getMetrics(ctx);
            String socDesc = com.gamebooster.app.device.DeviceDetector.getDetailedSocDescription();
            String oem = com.gamebooster.app.device.DeviceDetector.detectOemBrand().name();

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;

                if (tvEngineMode != null) {
                    tvEngineMode.setText("⚡ " + engineMode.getDisplayName());
                    tvEngineMode.setTextColor(engineMode.getColorHex());
                }

                if (tvRamUsage != null) {
                    tvRamUsage.setText("RAM: " + m.ramUsagePct + "% (" + m.usedRamMb + "/" + m.totalRamMb + " MB)");
                }

                if (tvSocDetectedName != null) {
                    tvSocDetectedName.setText("🔥 " + socDesc);
                }

                if (tvOemBypassStatus != null) {
                    tvOemBypassStatus.setText("🛡️ " + oem + " Throttling Bypass Active • Android " + android.os.Build.VERSION.RELEASE);
                }
            });
        });
    }

    /**
     * High-speed non-blocking asynchronous game scanner:
     * 1. Performs targeted app detection in background thread
     * 2. Posts results to UI thread cleanly without ANY ANR / freezing
     */
    private void loadAndScanGamesAsync() {
        if (getContext() == null) return;
        final Context ctx = getContext().getApplicationContext();

        AppExecutors.getInstance().executeScan(() -> {
            List<GameAppInfo> scannedGames = HomeGameScanner.scanTargetGames(ctx);

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;

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
                    if (rvGames != null) rvGames.setVisibility(View.GONE);
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    if (rvGames != null) rvGames.setVisibility(View.VISIBLE);
                    if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                }
            });
        });
    }
}
