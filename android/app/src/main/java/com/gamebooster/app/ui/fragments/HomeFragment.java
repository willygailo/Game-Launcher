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

import com.gamebooster.app.api.GameApiClient;
import com.gamebooster.app.api.OnlineGameSearchResult;
import com.gamebooster.app.ui.adapters.OnlineGamesAdapter;
import com.gamebooster.app.ui.activities.WebBrowserActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class HomeFragment extends Fragment implements ShizukuManager.ShizukuStateListener {

    private TextView tvGamesHeader;
    private LinearLayout layoutEmptyState;
    private RecyclerView rvGames;
    private HomeGamesAdapter adapter;
    private RecyclerView rvOnlineGames;
    private OnlineGamesAdapter onlineAdapter;
    private EditText etHomeSearch;
    private Button btnHomeSearchClear;
    private Button btnHomeOpenBrowser;
    private Button btnFilterInstalled;
    private Button btnFilterOnline;
    private boolean isOnlineTab = false;

    private LoopingVideoBackgroundView videoHomeBg;
    private LoopingVideoBackgroundView videoHeroBanner;
    private final List<GameAppInfo> gameList = new ArrayList<>();
    private boolean lastKnownShizukuActive = false;

    private void handleShizukuStateUpdate() {
        if (!isAdded() || getContext() == null) return;
        boolean nowActive = ShizukuExecutor.hasShizukuPermission()
                || com.gamebooster.app.shizuku.ShizukuManager.isShizukuRunningAndGranted()
                || com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().isReady()
                || com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().getState() == com.gamebooster.app.shizuku.ShizukuConnectionManager.State.READY;

        if (nowActive && !lastKnownShizukuActive) {
            lastKnownShizukuActive = true;
            loadAndScanGames(true);
        } else if (!nowActive) {
            lastKnownShizukuActive = false;
        }
    }

    private final com.gamebooster.app.shizuku.ShizukuConnectionManager.ConnectionListener connListener =
            state -> {
                if (isAdded() && getContext() != null) {
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(this::handleShizukuStateUpdate);
                }
            };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

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

        Button btnSettings = view.findViewById(R.id.btn_open_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(1);
                } else if (isAdded() && getParentFragmentManager() != null) {
                    try {
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new SettingsFragment(), "tab_settings")
                                .commitAllowingStateLoss();
                    } catch (Throwable ignored) {}
                }
            });
        }

        // Hero Hardware Engine Banner Click: Instant Hardware Boost & Max Hz Refresh
        View cardHeroBanner = view.findViewById(R.id.card_hero_hardware_engine);
        if (cardHeroBanner != null) {
            cardHeroBanner.setOnClickListener(v -> {
                Context c = getContext();
                if (c == null) return;
                Toast.makeText(c, "🚀 Hardware Turbo Active: Maximum 185Hz & Zero Throttling Engaged!", Toast.LENGTH_SHORT).show();
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    try {
                        com.gamebooster.app.engine.NativeFrameworkBridge.acquireSustainedPerformanceLock(c);
                        if (getActivity() != null) {
                            com.gamebooster.app.device.HardwareDisplayController.applyMaxRefreshRateToWindow(getActivity());
                        }
                        com.gamebooster.app.engine.PrivilegeBridgeEngine.executePrivileged("echo 3 > /proc/sys/vm/drop_caches 2>/dev/null");
                        com.gamebooster.app.booster.PerformanceChannel.applyProfile(c, com.gamebooster.app.booster.PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                    } catch (Throwable t) {
                        android.util.Log.w("HomeFragment", "Hardware turbo error: " + t.getMessage());
                    }
                });
            });
        }

        // Header & Empty State Action Buttons
        Button btnHomeAddGame = view.findViewById(R.id.btn_home_add_game);
        Button btnHomeClearGames = view.findViewById(R.id.btn_home_clear_games);
        Button btnHomeApkManager = view.findViewById(R.id.btn_home_apk_manager);
        Button btnEmptyAddGame = view.findViewById(R.id.btn_empty_add_game);
        Button btnEmptyScanApks = view.findViewById(R.id.btn_empty_scan_apks);

        View.OnClickListener openAddGameAction = v -> {
            android.app.Activity act = getActivity();
            if (act != null && !act.isFinishing()) {
                com.gamebooster.app.ui.dialogs.AddGameDialog.show(act, () -> loadAndScanGames(true));
            }
        };

        View.OnClickListener openClearGamesAction = v -> {
            android.app.Activity act = getActivity();
            if (act != null && !act.isFinishing()) {
                com.gamebooster.app.ui.dialogs.ClearGameDialog.show(act, () -> loadAndScanGames(true));
            }
        };

        View.OnClickListener openApkManagerAction = v -> {
            android.app.Activity act = getActivity();
            Context ctxToUse = act != null ? act : getContext();
            if (ctxToUse != null) {
                com.gamebooster.app.apk.ApkManagerDialog.show(ctxToUse, () -> loadAndScanGames(true));
            }
        };

        if (btnHomeAddGame != null) btnHomeAddGame.setOnClickListener(openAddGameAction);
        if (btnHomeClearGames != null) btnHomeClearGames.setOnClickListener(openClearGamesAction);
        if (btnEmptyAddGame != null) btnEmptyAddGame.setOnClickListener(openAddGameAction);
        if (btnHomeApkManager != null) btnHomeApkManager.setOnClickListener(openApkManagerAction);
        if (btnEmptyScanApks != null) btnEmptyScanApks.setOnClickListener(openApkManagerAction);

        if (tvGamesHeader != null) {
            tvGamesHeader.setOnClickListener(v -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "🔄 Rescanning installed games library...", Toast.LENGTH_SHORT).show();
                }
                loadAndScanGames(true);
            });
        }

        if (rvGames != null) {
            rvGames.setLayoutManager(new LinearLayoutManager(getContext()));
            rvGames.setNestedScrollingEnabled(false);
            rvGames.setItemViewCacheSize(25);
            rvGames.setItemAnimator(null);
            adapter = new HomeGamesAdapter(getContext(), gameList);
            rvGames.setAdapter(adapter);
        }

        // Online & Web Games Search Setup
        rvOnlineGames = view.findViewById(R.id.rv_online_games_list);
        if (rvOnlineGames != null) {
            rvOnlineGames.setLayoutManager(new LinearLayoutManager(getContext()));
            rvOnlineGames.setNestedScrollingEnabled(false);
            onlineAdapter = new OnlineGamesAdapter(getContext());
            rvOnlineGames.setAdapter(onlineAdapter);
        }

        etHomeSearch = view.findViewById(R.id.et_home_search);
        btnHomeSearchClear = view.findViewById(R.id.btn_home_search_clear);
        btnHomeOpenBrowser = view.findViewById(R.id.btn_home_open_browser);
        btnFilterInstalled = view.findViewById(R.id.btn_filter_installed);
        btnFilterOnline = view.findViewById(R.id.btn_filter_online);

        if (btnHomeOpenBrowser != null) {
            btnHomeOpenBrowser.setOnClickListener(v -> {
                String query = etHomeSearch != null ? etHomeSearch.getText().toString().trim() : "";
                String targetUrl = query.isEmpty() ? "https://html.duckduckgo.com/" : query;
                WebBrowserActivity.openUrl(getContext(), targetUrl, "Web Game Search");
            });
        }

        if (btnHomeSearchClear != null) {
            btnHomeSearchClear.setOnClickListener(v -> {
                if (etHomeSearch != null) etHomeSearch.setText("");
            });
        }

        if (btnFilterInstalled != null) {
            btnFilterInstalled.setOnClickListener(v -> selectTabInstalled());
        }

        if (btnFilterOnline != null) {
            btnFilterOnline.setOnClickListener(v -> selectTabOnline());
        }

        if (etHomeSearch != null) {
            etHomeSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s != null ? s.toString().trim() : "";
                    if (btnHomeSearchClear != null) {
                        btnHomeSearchClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    if (isOnlineTab) {
                        searchOnline(query);
                    } else {
                        filterInstalledGames(query);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            etHomeSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_GO) {
                    String query = etHomeSearch.getText().toString().trim();
                    if (isOnlineTab) {
                        searchOnline(query);
                    } else {
                        if (!query.isEmpty() && adapter != null && adapter.getItemCount() == 0) {
                            // If no local games matched, automatically switch to online search
                            selectTabOnline();
                            searchOnline(query);
                        }
                    }
                    return true;
                }
                return false;
            });
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

        com.gamebooster.app.ui.views.ShizukuStatusDashboardView dashboardView = view.findViewById(R.id.shizuku_dashboard_view);
        if (dashboardView != null) {
            dashboardView.bindLifecycle(getViewLifecycleOwner());
        }

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
        if (getContext() != null) {
            com.gamebooster.app.shizuku.ShizukuLifecycleManager.getInstance(getContext()).onResumeCheck();
        }

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
        loadAndScanGames(false); // Non-forced: show cached list instantly; only re-scan on package events
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
            if (getContext() != null) {
                com.gamebooster.app.shizuku.ShizukuLifecycleManager.getInstance(getContext()).onResumeCheck();
            }
            applyVideoBackgroundState();
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
            com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(this::handleShizukuStateUpdate);
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

    private void selectTabInstalled() {
        isOnlineTab = false;
        if (btnFilterInstalled != null) {
            btnFilterInstalled.setBackgroundResource(R.drawable.btn_cyber_cyan);
            btnFilterInstalled.setTextColor(0xFF080B11);
        }
        if (btnFilterOnline != null) {
            btnFilterOnline.setBackgroundResource(R.drawable.btn_cyber_dark);
            btnFilterOnline.setTextColor(0xFF00F0FF);
        }
        if (rvOnlineGames != null) rvOnlineGames.setVisibility(View.GONE);
        if (rvGames != null) rvGames.setVisibility(View.VISIBLE);
        if (tvGamesHeader != null) {
            tvGamesHeader.setText("INSTALLED GAMES (" + gameList.size() + " DETECTED)");
        }
        String query = etHomeSearch != null ? etHomeSearch.getText().toString().trim() : "";
        filterInstalledGames(query);
    }

    private void selectTabOnline() {
        isOnlineTab = true;
        if (btnFilterOnline != null) {
            btnFilterOnline.setBackgroundResource(R.drawable.btn_cyber_cyan);
            btnFilterOnline.setTextColor(0xFF080B11);
        }
        if (btnFilterInstalled != null) {
            btnFilterInstalled.setBackgroundResource(R.drawable.btn_cyber_dark);
            btnFilterInstalled.setTextColor(0xFF00F0FF);
        }
        if (rvGames != null) rvGames.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        if (rvOnlineGames != null) rvOnlineGames.setVisibility(View.VISIBLE);
        if (tvGamesHeader != null) {
            tvGamesHeader.setText("WEB & ONLINE GAMES");
        }
        String query = etHomeSearch != null ? etHomeSearch.getText().toString().trim() : "";
        searchOnline(query);
    }

    private void filterInstalledGames(String query) {
        if (adapter == null) return;
        if (query == null || query.isEmpty()) {
            adapter.updateList(new ArrayList<>(gameList));
            if (tvGamesHeader != null) {
                tvGamesHeader.setText("INSTALLED GAMES (" + gameList.size() + " DETECTED)");
            }
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(gameList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            return;
        }

        String lower = query.toLowerCase();
        List<GameAppInfo> filtered = new ArrayList<>();
        for (GameAppInfo game : gameList) {
            if (game != null) {
                String label = game.getLabel() != null ? game.getLabel().toLowerCase() : "";
                String pkg = game.getPackageName() != null ? game.getPackageName().toLowerCase() : "";
                if (label.contains(lower) || pkg.contains(lower)) {
                    filtered.add(game);
                }
            }
        }
        adapter.updateList(filtered);
        if (tvGamesHeader != null) {
            tvGamesHeader.setText("FILTERED GAMES (" + filtered.size() + " FOUND)");
        }
    }

    private void searchOnline(String query) {
        if (onlineAdapter == null) return;
        if (tvGamesHeader != null) {
            tvGamesHeader.setText("SEARCHING ONLINE: " + (query.isEmpty() ? "FEATURED" : query) + "...");
        }
        GameApiClient.searchOnlineGames(query, new GameApiClient.SearchCallback() {
            @Override
            public void onSuccess(List<OnlineGameSearchResult> results) {
                if (!isAdded()) return;
                onlineAdapter.updateList(results);
                if (tvGamesHeader != null) {
                    tvGamesHeader.setText("ONLINE SEARCH (" + results.size() + " RESULTS)");
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (!isAdded()) return;
                if (tvGamesHeader != null) {
                    tvGamesHeader.setText("ONLINE SEARCH: ERROR");
                }
            }
        });
    }
}
