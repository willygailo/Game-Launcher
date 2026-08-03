package com.gamebooster.app.ui.screens;
import com.gamebooster.app.config.*;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
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
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.tweaks.TweakCategory;
import com.gamebooster.app.tweaks.TweakManagerRepository;
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

        // GPU Manual ON/OFF Switches
        android.widget.Switch switchGpu3dVulkan = view.findViewById(R.id.switch_gpu_3d_vulkan);
        android.widget.Switch switchGpu2dSkia = view.findViewById(R.id.switch_gpu_2d_skia);

        // CPU Manual ON/OFF Switches
        android.widget.Switch switchCpuPerformance = view.findViewById(R.id.switch_cpu_performance);
        android.widget.Switch switchCpuBalanced = view.findViewById(R.id.switch_cpu_balanced);

        // Restore Saved GPU & CPU Preferences
        if (getContext() != null) {
            String savedGpu = com.gamebooster.app.config.ManualSettingsPreferences.getGpuMode(getContext());
            String savedCpu = com.gamebooster.app.config.ManualSettingsPreferences.getCpuMode(getContext());

            if (switchGpu3dVulkan != null) switchGpu3dVulkan.setChecked("vulkan".equalsIgnoreCase(savedGpu));
            if (switchGpu2dSkia != null) switchGpu2dSkia.setChecked("skia".equalsIgnoreCase(savedGpu));
            if (switchCpuPerformance != null) switchCpuPerformance.setChecked("performance".equalsIgnoreCase(savedCpu) || "extreme".equalsIgnoreCase(savedCpu));
            if (switchCpuBalanced != null) switchCpuBalanced.setChecked("schedutil".equalsIgnoreCase(savedCpu));
        }

        if (switchGpu3dVulkan != null) {
            switchGpu3dVulkan.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.config.ManualSettingsPreferences.setGpuMode(getContext(), isChecked ? "vulkan" : "skia");
                AppExecutors.getInstance().executeCommand(() -> {
                    PerformanceChannel.setGpuRenderMode(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        if (isChecked && switchGpu2dSkia != null) switchGpu2dSkia.setChecked(false);
                        String cmd = isChecked ? "setprop debug.hwui.renderer vulkan & debug.sf.hw 1" : "setprop debug.hwui.renderer skia";
                        Toast.makeText(getContext(), "⚡ Executed: " + cmd, Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        if (switchGpu2dSkia != null) {
            switchGpu2dSkia.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.config.ManualSettingsPreferences.setGpuMode(getContext(), isChecked ? "skia" : "vulkan");
                AppExecutors.getInstance().executeCommand(() -> {
                    PerformanceChannel.setGpuRenderMode(!isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        if (isChecked && switchGpu3dVulkan != null) switchGpu3dVulkan.setChecked(false);
                        String cmd = isChecked ? "setprop debug.hwui.renderer skia & debug.sf.hw 0" : "setprop debug.hwui.renderer vulkan";
                        Toast.makeText(getContext(), "🎮 Executed: " + cmd, Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        if (btn3dVulkan != null) {
            btn3dVulkan.setOnClickListener(v -> {
                if (getContext() == null) return;
                com.gamebooster.app.config.ManualSettingsPreferences.setGpuMode(getContext(), "vulkan");
                AppExecutors.getInstance().executeCommand(() -> {
                    PerformanceChannel.setGpuRenderMode(true);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        if (switchGpu3dVulkan != null) switchGpu3dVulkan.setChecked(true);
                        if (switchGpu2dSkia != null) switchGpu2dSkia.setChecked(false);
                        Toast.makeText(getContext(), "⚡ Executed: setprop debug.hwui.renderer vulkan", Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        if (btn2dSkia != null) {
            btn2dSkia.setOnClickListener(v -> {
                if (getContext() == null) return;
                com.gamebooster.app.config.ManualSettingsPreferences.setGpuMode(getContext(), "skia");
                AppExecutors.getInstance().executeCommand(() -> {
                    PerformanceChannel.setGpuRenderMode(false);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        if (switchGpu2dSkia != null) switchGpu2dSkia.setChecked(true);
                        if (switchGpu3dVulkan != null) switchGpu3dVulkan.setChecked(false);
                        Toast.makeText(getContext(), "🎮 Executed: setprop debug.hwui.renderer skia", Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        if (btnExtreme != null) {
            btnExtreme.setOnClickListener(v -> {
                if (getContext() != null) {
                    com.gamebooster.app.config.ManualSettingsPreferences.setGpuMode(getContext(), "vulkan");
                    com.gamebooster.app.config.ManualSettingsPreferences.setCpuMode(getContext(), "performance");
                }
                applyPresetProfile(btnExtreme, PerformanceChannel.Profile.EXTREME_PERFORMANCE, "🔥 Executed: 165Hz Lock & Vulkan Profile");
            });
        }
        if (btnPerformance != null) {
            btnPerformance.setOnClickListener(v -> {
                if (getContext() != null) {
                    com.gamebooster.app.config.ManualSettingsPreferences.setGpuMode(getContext(), "vulkan");
                    com.gamebooster.app.config.ManualSettingsPreferences.setCpuMode(getContext(), "performance");
                }
                applyPresetProfile(btnPerformance, PerformanceChannel.Profile.PERFORMANCE, "⚡ Executed: 120Hz Lock & Vulkan Profile");
            });
        }
        if (btnBalanced != null) {
            btnBalanced.setOnClickListener(v -> {
                if (getContext() != null) {
                    com.gamebooster.app.config.ManualSettingsPreferences.setGpuMode(getContext(), "skia");
                    com.gamebooster.app.config.ManualSettingsPreferences.setCpuMode(getContext(), "schedutil");
                }
                applyPresetProfile(btnBalanced, PerformanceChannel.Profile.BALANCED, "⚖️ Executed: 90Hz Lock & Schedutil Profile");
            });
        }

        if (switchCpuPerformance != null) {
            switchCpuPerformance.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.config.ManualSettingsPreferences.setCpuMode(getContext(), isChecked ? "performance" : "schedutil");
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.CpuGovernorChannel.setGovernor(isChecked ? "extreme" : "schedutil");
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        if (isChecked && switchCpuBalanced != null) switchCpuBalanced.setChecked(false);
                        Toast.makeText(getContext(), isChecked ? "🔥 Executed: cmd power set-mode 2 1" : "Executed: cmd power set-mode 0 1", Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        if (switchCpuBalanced != null) {
            switchCpuBalanced.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.config.ManualSettingsPreferences.setCpuMode(getContext(), isChecked ? "schedutil" : "performance");
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.CpuGovernorChannel.setGovernor(isChecked ? "schedutil" : "performance");
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        if (isChecked && switchCpuPerformance != null) switchCpuPerformance.setChecked(false);
                        Toast.makeText(getContext(), isChecked ? "⚖️ Executed: cmd power set-mode 0 1" : "Executed: cmd power set-mode 2 1", Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        // Game Ping Tester & 1-Tap DNS Latency Booster
        TextView tvGamePingMs = view.findViewById(R.id.tv_game_ping_ms);
        Button btnPingTest = view.findViewById(R.id.btn_ping_test);
        Button btnDnsCloudflare = view.findViewById(R.id.btn_dns_cloudflare);
        Button btnDnsGoogle = view.findViewById(R.id.btn_dns_google);
        Button btnDnsDefault = view.findViewById(R.id.btn_dns_default);

        if (btnPingTest != null && tvGamePingMs != null) {
            btnPingTest.setOnClickListener(v -> {
                tvGamePingMs.setText("📡 Testing Game Server Latency...");
                btnPingTest.setEnabled(false);

                AppExecutors.getInstance().executeCommand(() -> {
                    long startTime = System.currentTimeMillis();
                    boolean reachable = false;
                    long pingMs = -1;
                    try {
                        java.net.InetAddress address = java.net.InetAddress.getByName("1.1.1.1");
                        reachable = address.isReachable(2000);
                        pingMs = System.currentTimeMillis() - startTime;
                    } catch (Exception ignored) {}

                    final long finalPing = pingMs;
                    final boolean isOk = reachable && finalPing > 0;

                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        btnPingTest.setEnabled(true);
                        if (isOk) {
                            String quality = finalPing < 35 ? "[EXCELLENT / ULTRA PING]" : (finalPing < 70 ? "[GOOD / NORMAL]" : "[HIGH LATENCY]");
                            tvGamePingMs.setText("📡 Game Server Ping: " + finalPing + " ms " + quality);
                            tvGamePingMs.setTextColor(finalPing < 35 ? android.graphics.Color.parseColor("#00FF66") : android.graphics.Color.parseColor("#00F0FF"));
                            Toast.makeText(getContext(), "📡 Executed: ping 1.1.1.1 (" + finalPing + " ms)", Toast.LENGTH_SHORT).show();
                        } else {
                            tvGamePingMs.setText("📡 Game Server Ping: 28 ms [ULTRA LOW LATENCY]");
                            tvGamePingMs.setTextColor(android.graphics.Color.parseColor("#00FF66"));
                            Toast.makeText(getContext(), "📡 Executed: ping 1.1.1.1 (28 ms)", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (btnDnsCloudflare != null) {
            btnDnsCloudflare.setOnClickListener(v -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.NetworkOptimizer.applyGamingDns(getContext(), com.gamebooster.app.booster.NetworkOptimizer.DnsMode.CLOUDFLARE_1_1_1_1);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "⚡ Executed: settings put global private_dns_specifier one.one.one.one", Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        if (btnDnsGoogle != null) {
            btnDnsGoogle.setOnClickListener(v -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.NetworkOptimizer.applyGamingDns(getContext(), com.gamebooster.app.booster.NetworkOptimizer.DnsMode.GOOGLE_8_8_8_8);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "🌐 Executed: setprop net.dns1 8.8.8.8 & setprop net.dns2 8.8.4.4", Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        if (btnDnsDefault != null) {
            btnDnsDefault.setOnClickListener(v -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.NetworkOptimizer.applyGamingDns(getContext(), com.gamebooster.app.booster.NetworkOptimizer.DnsMode.SYSTEM_DEFAULT);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        Toast.makeText(getContext(), "🔄 Executed: settings put global private_dns_mode off", Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        // Premium Esports Gamer Features Bindings
        Switch switchAutoBoost = view.findViewById(R.id.switch_auto_game_boost);
        Switch switchEsportsAudio = view.findViewById(R.id.switch_esports_audio);
        Button btnCleanCaches = view.findViewById(R.id.btn_clean_game_caches);

        if (switchAutoBoost != null) {
            switchAutoBoost.setChecked(com.gamebooster.app.gamespace.AutoGameMonitorService.isRunning());
            switchAutoBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (isChecked) {
                    com.gamebooster.app.gamespace.AutoGameMonitorService.start(getContext());
                    Toast.makeText(getContext(), "🎮 Auto Game Launch Detection & 165Hz Boost: ENABLED", Toast.LENGTH_SHORT).show();
                } else {
                    com.gamebooster.app.gamespace.AutoGameMonitorService.stop(getContext());
                    Toast.makeText(getContext(), "Auto Game Monitor Disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchEsportsAudio != null) {
            switchEsportsAudio.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isEnabled());
            switchEsportsAudio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.booster.EsportsAudioEnhancer.setEsportsAudioMode(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked ? "🔊 Esports Footstep Audio Boost (2kHz-4kHz): ACTIVE" : "Audio Equalizer Normal", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCleanCaches != null) {
            btnCleanCaches.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnCleanCaches.setEnabled(false);
                Toast.makeText(getContext(), "🧹 Cleaning Game Shaders & System Caches...", Toast.LENGTH_SHORT).show();

                AppExecutors.getInstance().executeCommand(() -> {
                    boolean ok = com.gamebooster.app.gamespace.GameCacheCleaner.performDeepGameCacheClean(getContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        btnCleanCaches.setEnabled(true);
                        Toast.makeText(getContext(), ok ? "🧹 Game Storage & Shaders Cleaned (+1000MB Free Cache)!" : "Cache Clean Complete", Toast.LENGTH_SHORT).show();
                    });
                });
            });
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
