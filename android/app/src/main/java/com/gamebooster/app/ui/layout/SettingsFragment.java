package com.gamebooster.app.ui.layout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gamebooster.app.R;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.core.EngineUIHelper;
import com.gamebooster.app.functions.PerformanceChannel;
import com.gamebooster.app.functions.TweakCategory;
import com.gamebooster.app.functions.TweakManagerRepository;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;

public class SettingsFragment extends Fragment implements ShizukuManager.ShizukuStateListener {

    private TextView tvEngineStatus;
    private TextView tvRootStatus;
    private TextView tvTweaksStatus;
    private View bannerDisconnect;
    private TweaksAdapter tweaksAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Bind Shizuku & System Permissions Views
        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        tvRootStatus = view.findViewById(R.id.tv_root_status);
        Button btnGrantShizuku = view.findViewById(R.id.btn_grant_shizuku);
        Button btnOpenSettings = view.findViewById(R.id.btn_open_settings);

        if (btnGrantShizuku != null) {
            btnGrantShizuku.setOnClickListener(v -> {
                if (getContext() != null) {
                    if (ShizukuExecutor.hasShizukuPermission()) {
                        ShizukuExecutor.grantAppPermissionsViaShizuku(getContext());
                        Toast.makeText(getContext(), "⚡ Shizuku 1-Tap Permissions Granted!", Toast.LENGTH_SHORT).show();
                    } else {
                        ShizukuManager.openOrInstallShizukuManager(getContext());
                    }
                    refreshAllStatuses();
                }
            });
        }

        Button btnGithubReleases = view.findViewById(R.id.btn_github_releases);
        Button btnFacebookProfile = view.findViewById(R.id.btn_facebook_profile);

        if (btnGithubReleases != null) {
            btnGithubReleases.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/willygailo/Game-Launcher/releases"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to open browser", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnFacebookProfile != null) {
            btnFacebookProfile.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027"));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to open browser", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Bind GPU Render Engine & Preset Profiles Views
        Button btn3dVulkan = view.findViewById(R.id.btn_engine_3d_vulkan);
        Button btn2dSkia = view.findViewById(R.id.btn_engine_2d_skia);
        Button btnExtreme = view.findViewById(R.id.btn_apply_pubg_profile);
        Button btnPerformance = view.findViewById(R.id.btn_apply_2d_profile);
        Button btnBalanced = view.findViewById(R.id.btn_apply_balanced_profile);

        if (btn3dVulkan != null) {
            btn3dVulkan.setOnClickListener(v -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    PerformanceChannel.setGpuRenderMode(true);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "⚡ 3D Vulkan HWUI Render Engine Enabled!", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        if (btn2dSkia != null) {
            btn2dSkia.setOnClickListener(v -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    PerformanceChannel.setGpuRenderMode(false);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "🎮 2D Skia Render Engine Enabled!", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        if (btnExtreme != null) {
            btnExtreme.setOnClickListener(v -> applyPresetProfile(btnExtreme, PerformanceChannel.Profile.EXTREME_PERFORMANCE, "🔥 Extreme Performance Profile Applied!"));
        }
        if (btnPerformance != null) {
            btnPerformance.setOnClickListener(v -> applyPresetProfile(btnPerformance, PerformanceChannel.Profile.PERFORMANCE, "⚡ Performance Profile Applied!"));
        }
        if (btnBalanced != null) {
            btnBalanced.setOnClickListener(v -> applyPresetProfile(btnBalanced, PerformanceChannel.Profile.BALANCED, "⚖️ Balanced Profile Applied!"));
        }

        // Bind Tweaks Views & RecyclerView
        tvTweaksStatus = view.findViewById(R.id.tv_tweaks_status);
        bannerDisconnect = view.findViewById(R.id.banner_shizuku_disconnect);
        Button btnApplyAll = view.findViewById(R.id.btn_apply_all_tweaks);
        RecyclerView rvTweaks = view.findViewById(R.id.rv_tweaks_list);

        Button btnFilterAll = view.findViewById(R.id.btn_filter_all);
        Button btnFilterCpuGpu = view.findViewById(R.id.btn_filter_cpugpu);
        Button btnFilterTouch = view.findViewById(R.id.btn_filter_touch);
        Button btnFilterShizuku = view.findViewById(R.id.btn_filter_shizuku);
        Button btnFilterNetwork = view.findViewById(R.id.btn_filter_network);

        TweakManagerRepository.initializeStates(getContext());

        if (rvTweaks != null) {
            rvTweaks.setLayoutManager(new LinearLayoutManager(getContext()));
            tweaksAdapter = new TweaksAdapter(getContext(), TweakManagerRepository.getAllTweaks());
            rvTweaks.setAdapter(tweaksAdapter);
        }

        if (btnApplyAll != null) {
            btnApplyAll.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnApplyAll.setEnabled(false);
                Toast.makeText(getContext(), "Applying system optimizations...", Toast.LENGTH_SHORT).show();

                TweakManagerRepository.applyAllSupportedTweaksAsync(getContext(), appliedCount -> {
                    if (getContext() != null && isAdded()) {
                        if (tweaksAdapter != null) {
                            tweaksAdapter.notifyDataSetChanged();
                        }
                        btnApplyAll.setEnabled(true);
                        Toast.makeText(getContext(), "Applied " + appliedCount + " system optimizations!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (btnFilterAll != null && tweaksAdapter != null) btnFilterAll.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getAllTweaks()));
        if (btnFilterCpuGpu != null && tweaksAdapter != null) btnFilterCpuGpu.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.CPU_GPU)));
        if (btnFilterTouch != null && tweaksAdapter != null) btnFilterTouch.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.TOUCH_DISPLAY)));
        if (btnFilterShizuku != null && tweaksAdapter != null) btnFilterShizuku.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.SHIZUKU_SYSTEM)));
        if (btnFilterNetwork != null && tweaksAdapter != null) btnFilterNetwork.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.NETWORK_LATENCY)));

        refreshAllStatuses();
        return view;
    }

    private void applyPresetProfile(Button button, PerformanceChannel.Profile profile, String successMsg) {
        if (getContext() == null || button == null) return;
        button.setEnabled(false);
        Toast.makeText(getContext(), "Applying performance profile...", Toast.LENGTH_SHORT).show();
        AppExecutors.getInstance().executeCommand(() -> {
            boolean ok = PerformanceChannel.applyProfile(getContext(), profile);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                button.setEnabled(true);
                Toast.makeText(getContext(), ok ? successMsg : "Profile applied with system setting fallbacks", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ShizukuManager.addStateListener(this);
        boolean alive = ShizukuExecutor.hasShizukuPermission();
        onBinderStateChanged(alive);
    }

    @Override
    public void onStop() {
        super.onStop();
        ShizukuManager.removeStateListener(this);
    }

    @Override
    public void onBinderStateChanged(boolean alive) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (tweaksAdapter != null) {
                    tweaksAdapter.setShizukuAlive(alive);
                }
                if (bannerDisconnect != null) {
                    bannerDisconnect.setVisibility(alive ? View.GONE : View.VISIBLE);
                }
                refreshAllStatuses();
            });
            if (alive && getContext() != null) {
                TweakManagerRepository.restoreAppliedTweaksAsync(getContext());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAllStatuses();
    }

    private void refreshAllStatuses() {
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        EngineUIHelper.refreshEngineStatus(tvTweaksStatus);
        updateSystemSettingsStatus();
        boolean alive = ShizukuExecutor.hasShizukuPermission();
        if (tweaksAdapter != null) {
            tweaksAdapter.setShizukuAlive(alive);
        }
        if (bannerDisconnect != null) {
            bannerDisconnect.setVisibility(alive ? View.GONE : View.VISIBLE);
        }
    }

    private void updateSystemSettingsStatus() {
        if (tvRootStatus == null || getContext() == null) return;
        boolean canWrite = Settings.System.canWrite(getContext());
        if (canWrite) {
            tvRootStatus.setText("WRITE_SETTINGS Permission: GRANTED");
            tvRootStatus.setTextColor(0xFF00FF66);
        } else {
            tvRootStatus.setText("WRITE_SETTINGS Permission: REQUIRED");
            tvRootStatus.setTextColor(0xFFFFB800);
        }
    }
}
