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
import com.gamebooster.app.ui.views.LoopingVideoBackgroundView;

public class HomeFragment extends Fragment implements ShizukuManager.ShizukuStateListener {

    private TextView tvEngineMode;
    private TextView tvRamUsage;
    private TextView tvGamesHeader;
    private LinearLayout layoutEmptyState;
    private RecyclerView rvGames;
    private HomeGamesAdapter adapter;
    private LoopingVideoBackgroundView videoHomeBg;
    private LoopingVideoBackgroundView videoHeroBanner;
    private final List<GameAppInfo> gameList = new ArrayList<>();
    private final com.gamebooster.app.shizuku.ShizukuConnectionManager.ConnectionListener connListener =
            state -> {
                if (isAdded() && getContext() != null) {
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                        updateStatusStrip();
                        if (state == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.READY) {
                            loadAndScanGames(true);
                        }
                    });
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

        // Background Looping Video
        videoHomeBg = view.findViewById(R.id.video_home_bg);
        if (videoHomeBg != null) {
            videoHomeBg.setMuted(true);
            videoHomeBg.setVideoRawResource(R.raw.home_bg_video);
        }

        // Hero Hardware Banner Looping Video (Video Only - Silent)
        videoHeroBanner = view.findViewById(R.id.video_hero_banner);
        if (videoHeroBanner != null) {
            videoHeroBanner.setMuted(true);
            videoHeroBanner.setVideoRawResource(R.raw.banner_video);
        }

        View chipEngineMode = view.findViewById(R.id.chip_engine_mode);
        if (chipEngineMode != null) {
            chipEngineMode.setOnClickListener(v -> {
                if (getContext() != null) {
                    ShizukuManager.handleShizukuCardClick(getContext());
                    updateStatusStrip();
                }
            });
        }
        if (tvEngineMode != null) {
            tvEngineMode.setOnClickListener(v -> {
                if (getContext() != null) {
                    ShizukuManager.handleShizukuCardClick(getContext());
                    updateStatusStrip();
                }
            });
        }

        Button btnSettings = view.findViewById(R.id.btn_open_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(1);
                }
            });
        }

        // Header & Empty State Action Buttons
        Button btnHomeAddGame = view.findViewById(R.id.btn_home_add_game);
        Button btnHomeClearGames = view.findViewById(R.id.btn_home_clear_games);
        Button btnHomeApkManager = view.findViewById(R.id.btn_home_apk_manager);
        Button btnEmptyAddGame = view.findViewById(R.id.btn_empty_add_game);
        Button btnEmptyScanApks = view.findViewById(R.id.btn_empty_scan_apks);

        View.OnClickListener openAddGameAction = v -> {
            if (isAdded() && getActivity() != null) {
                com.gamebooster.app.ui.dialogs.AddGameDialog.show(requireActivity(), () -> loadAndScanGames(true));
            }
        };

        View.OnClickListener openClearGamesAction = v -> {
            if (isAdded() && getActivity() != null) {
                com.gamebooster.app.ui.dialogs.ClearGameDialog.show(requireActivity(), () -> loadAndScanGames(true));
            }
        };

        View.OnClickListener openApkManagerAction = v -> {
            if (getContext() != null) {
                com.gamebooster.app.apk.ApkManagerDialog.show(getContext(), () -> loadAndScanGames(true));
            }
        };

        if (btnHomeAddGame != null) btnHomeAddGame.setOnClickListener(openAddGameAction);
        if (btnHomeClearGames != null) btnHomeClearGames.setOnClickListener(openClearGamesAction);
        if (btnEmptyAddGame != null) btnEmptyAddGame.setOnClickListener(openAddGameAction);
        if (btnHomeApkManager != null) btnHomeApkManager.setOnClickListener(openApkManagerAction);
        if (btnEmptyScanApks != null) btnEmptyScanApks.setOnClickListener(openApkManagerAction);

        if (rvGames != null) {
            rvGames.setLayoutManager(new LinearLayoutManager(getContext()));
            rvGames.setNestedScrollingEnabled(false);
            rvGames.setItemViewCacheSize(25);
            rvGames.setItemAnimator(null);
            adapter = new HomeGamesAdapter(getContext(), gameList);
            rvGames.setAdapter(adapter);
        }

        androidx.core.widget.NestedScrollView scrollHome = view.findViewById(R.id.scroll_home);
        if (scrollHome != null) {
            scrollHome.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (getContext() != null && com.gamebooster.app.config.ManualSettingsPreferences.isVideoSaverEnabled(getContext())) {
                    return;
                }
                if (Math.abs(scrollY - oldScrollY) > 10) {
                    if (videoHomeBg != null && videoHomeBg.isPlaying()) videoHomeBg.pause();
                    if (videoHeroBanner != null && videoHeroBanner.isPlaying()) videoHeroBanner.pause();
                }
            });
        }

        updateStatusStrip();
        loadAndScanGames(true);
        return view;
    }

    private volatile boolean isScanning = false;
    private volatile long lastScanTime = 0L;
    private static final long SCAN_DEBOUNCE_MS = 2000L;
    private long lastPackageEventTime = 0L;

    private final android.content.BroadcastReceiver packageReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if (intent != null && intent.getAction() != null) {
                long now = System.currentTimeMillis();
                if (now - lastPackageEventTime < 1500L) {
                    return;
                }
                lastPackageEventTime = now;
                com.gamebooster.app.core.AppExecutors.getInstance().postDelayed(() -> {
                    if (isAdded() && getContext() != null) {
                        loadAndScanGames(true);
                    }
                }, 300L);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().addConnectionListener(connListener);
        ShizukuManager.addStateListener(this);

        try {
            if (getContext() != null) {
                android.content.IntentFilter filter = new android.content.IntentFilter();
                filter.addAction(android.content.Intent.ACTION_PACKAGE_ADDED);
                filter.addAction(android.content.Intent.ACTION_PACKAGE_REMOVED);
                filter.addAction(android.content.Intent.ACTION_PACKAGE_REPLACED);
                filter.addDataScheme("package");
                getContext().registerReceiver(packageReceiver, filter);
            }
        } catch (Throwable ignored) {}

        applyVideoBackgroundState();
        updateStatusStrip();
        loadAndScanGames(true);
    }

    private void applyVideoBackgroundState() {
        if (getContext() == null) return;
        boolean videoSaver = com.gamebooster.app.config.ManualSettingsPreferences.isVideoSaverEnabled(getContext());
        if (videoSaver) {
            if (videoHomeBg != null) {
                videoHomeBg.pause();
                videoHomeBg.setVisibility(View.GONE);
            }
            if (videoHeroBanner != null) {
                videoHeroBanner.pause();
                videoHeroBanner.setVisibility(View.GONE);
            }
        } else {
            if (videoHomeBg != null) {
                videoHomeBg.setVisibility(View.VISIBLE);
                videoHomeBg.play();
            }
            if (videoHeroBanner != null) {
                videoHeroBanner.setVisibility(View.VISIBLE);
                videoHeroBanner.play();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            if (getContext() != null) {
                getContext().unregisterReceiver(packageReceiver);
            }
        } catch (Throwable ignored) {}
        if (videoHomeBg != null) videoHomeBg.pause();
        if (videoHeroBanner != null) videoHeroBanner.pause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (videoHomeBg != null) videoHomeBg.pause();
            if (videoHeroBanner != null) videoHeroBanner.pause();
        } else {
            applyVideoBackgroundState();
            updateStatusStrip();
            loadAndScanGames(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (getContext() != null) {
                getContext().unregisterReceiver(packageReceiver);
            }
        } catch (Throwable ignored) {}
        com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().removeConnectionListener(connListener);
        ShizukuManager.removeStateListener(this);
        if (videoHomeBg != null) {
            videoHomeBg.release();
            videoHomeBg = null;
        }
        if (videoHeroBanner != null) {
            videoHeroBanner.release();
            videoHeroBanner = null;
        }
    }

    @Override
    public void onBinderStateChanged(boolean alive) {
        if (isAdded() && getContext() != null) {
            com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                updateStatusStrip();
                if (alive) {
                    loadAndScanGames(true);
                }
            });
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
                String osBadge = "SHIZUKU API ACTIVE";
                if (android.os.Build.VERSION.SDK_INT >= 36) {
                    osBadge = "ANDROID 16 BAKLAVA • ADPF 3.0";
                } else if (android.os.Build.VERSION.SDK_INT >= 35) {
                    osBadge = "ANDROID 15 • ADPF 3.0 + 16KB";
                } else if (android.os.Build.VERSION.SDK_INT >= 34) {
                    osBadge = "ANDROID 14 • ADPF 2.0";
                } else if (android.os.Build.VERSION.SDK_INT >= 33) {
                    osBadge = "ANDROID 13 • GAME ENGINE";
                }
                tvEngineMode.setText("⚡ " + osBadge);
                tvEngineMode.setTextColor(android.graphics.Color.parseColor("#00FF66"));
            }
        } else if (ShizukuExecutor.isShizukuAvailable() || rikka.shizuku.Shizuku.pingBinder()) {
            if (tvEngineMode != null) {
                tvEngineMode.setText("⚡ SHIZUKU API: GRANT PERMISSION (TAP)");
                tvEngineMode.setTextColor(android.graphics.Color.parseColor("#FFCC00"));
            }
        } else if (tvEngineMode != null
                && (conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.BINDING
                || conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.RETRY)) {
            tvEngineMode.setText("🔄 Shizuku connecting…");
            tvEngineMode.setTextColor(android.graphics.Color.parseColor("#FCA5A5"));
        } else if (tvEngineMode != null && conn == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.DEAD) {
            tvEngineMode.setText("⚠️ Shizuku Disconnected (Tap to connect)");
            tvEngineMode.setTextColor(android.graphics.Color.parseColor("#EF4444"));
        } else {
            EngineMode engineMode = CommandExecutor.getActiveEngineMode();
            if (tvEngineMode != null) {
                tvEngineMode.setText("⚡ " + engineMode.getDisplayName() + " (Tap to connect Shizuku)");
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
    public void loadAndScanGamesZeroDelay() {
        loadAndScanGames(false);
    }

    public void loadAndScanGames(boolean forceScan) {
        if (!isAdded() || getContext() == null) return;
        final Context ctx = getContext().getApplicationContext();

        // If games already in memory and not forceScan, display them right away
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
        if (!forceScan && (isScanning || (now - lastScanTime < SCAN_DEBOUNCE_MS))) {
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

                    // Safety guard: if scan returned empty but we previously had games and context was still valid,
                    // do not clear the list unless verified empty by PackageManager
                    if ((scannedGames == null || scannedGames.isEmpty()) && !gameList.isEmpty()) {
                        android.util.Log.w("HomeFragment", "Scanned games was unexpectedly empty, keeping previous cached list (" + gameList.size() + " games)");
                        return;
                    }

                    gameList.clear();
                    if (scannedGames != null) {
                        gameList.addAll(scannedGames);
                    }

                    if (adapter != null) {
                        adapter.updateList(new ArrayList<>(gameList));
                    }

                    // Update header count
                    if (tvGamesHeader != null) {
                        tvGamesHeader.setText("INSTALLED GAMES (" + gameList.size() + " DETECTED)");
                    }

                    // Toggle empty state vs games list
                    if (gameList.isEmpty()) {
                        if (rvGames != null) rvGames.setVisibility(View.GONE);
                        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        if (rvGames != null) rvGames.setVisibility(View.VISIBLE);
                        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
                    }
                });
            } catch (Throwable t) {
                isScanning = false;
                android.util.Log.e("HomeFragment", "Error in loadAndScanGames: " + t.getMessage(), t);
            }
        });
    }
}
