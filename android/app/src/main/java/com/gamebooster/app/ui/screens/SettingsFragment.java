package com.gamebooster.app.ui.screens;
import com.gamebooster.app.config.*;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.tweaks.TweakCategory;
import com.gamebooster.app.tweaks.TweakManagerRepository;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.core.settings.SettingsManager;
import com.gamebooster.app.core.profile.ProfileManager;
import com.gamebooster.app.core.profile.InputProfile;
import com.gamebooster.app.overlay.CrosshairOverlayService;
import com.gamebooster.app.overlay.CrosshairPreset;
import com.gamebooster.app.ui.sensitivity.SensitivityCalculator;
import com.gamebooster.app.ui.sensitivity.SensitivityModel;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment implements ShizukuManager.ShizukuStateListener {

    private TextView tvEngineStatus;
    private TextView tvRootStatus;
    private TextView tvTweaksStatus;
    private View bannerDisconnect;
    private TweaksAdapter tweaksAdapter;

    // Hardware & Boost Switches
    private Switch switchAngleMode;
    private Switch switchGameDriver;
    private Switch switchGpuMode;
    private Switch switchCpuMode;
    private Switch switchTetheringHw;
    private Switch switchForceGnss;
    private Switch switchOverlayHud;
    private Switch switchGamingDnd;
    private Switch switchAutoGameBoost;
    private Switch switchEsportsAudio;

    // Device Spoofing UI
    private Switch switchDeviceSpoof;
    private TextView tvSpoofActiveProfile;
    private RecyclerView rvSpoofProfiles;
    private SpoofProfileAdapter spoofProfileAdapter;

    // Precision Aim Controls
    private Switch switchPrecisionInputTuner;
    private Switch switchCrosshairOverlay;
    private TextView tvPrecisionAimStatus;
    private Button btnCrosshairPreset;
    private Button btnSensitivityCalculator;

    // Network & Latency Optimization UI
    private Button btnModeDual;
    private Button btnMode5g6g;
    private Button btnModeWifi;
    private TextView tvActiveNetworkModeStatus;
    private Switch switchDualDataWifi;
    private Switch switch5g6gBoost;
    private Switch switchWifiLowLatency;

    private SettingsManager precisionSettingsManager;
    private ProfileManager precisionProfileManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        if (getContext() != null) {
            precisionSettingsManager = new SettingsManager(getContext());
            precisionProfileManager = new ProfileManager(getContext());
        }

        // Card 1: Shizuku & System Permissions
        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        tvRootStatus = view.findViewById(R.id.tv_storage_access_status);
        Button btnGrantShizuku = view.findViewById(R.id.btn_grant_shizuku);
        Button btnGrantStorage = view.findViewById(R.id.btn_grant_storage_access);
        Button btnOpenSettings = view.findViewById(R.id.btn_open_settings);

        if (btnGrantShizuku != null) {
            btnGrantShizuku.setOnClickListener(v -> {
                if (getContext() != null) {
                    if (ShizukuExecutor.hasShizukuPermission()) {
                        AppExecutors.getInstance().executeCommand(() -> {
                            ShizukuExecutor.grantAppPermissionsViaShizuku(getContext());
                            ShizukuFileManager.grantAllStoragePermissions(getContext());
                            AppExecutors.getInstance().postToMainThread(() -> {
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(getContext(), "⚡ Shizuku 1-Tap Permissions Granted!", Toast.LENGTH_SHORT).show();
                                    refreshAllStatuses();
                                }
                            });
                        });
                    } else {
                        ShizukuManager.openOrInstallShizukuManager(getContext());
                    }
                }
            });
        }

        if (btnGrantStorage != null) {
            btnGrantStorage.setOnClickListener(v -> {
                if (getContext() != null) {
                    if (ShizukuFileManager.hasFullAccess()) {
                        AppExecutors.getInstance().executeCommand(() -> {
                            ShizukuFileManager.grantAllStoragePermissions(getContext());
                            AppExecutors.getInstance().postToMainThread(() -> {
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(getContext(), "📁 Full Game Data & Storage Control UNLOCKED!", Toast.LENGTH_SHORT).show();
                                    refreshAllStatuses();
                                }
                            });
                        });
                    } else {
                        ShizukuManager.openOrInstallShizukuManager(getContext());
                    }
                }
            });
        }

        if (btnOpenSettings != null) {
            btnOpenSettings.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception ignored) {}
            });
        }

        // Card 2: Esports Gaming Controls
        switchOverlayHud = view.findViewById(R.id.switch_overlay_hud);
        switchGamingDnd = view.findViewById(R.id.switch_gaming_dnd);
        switchAutoGameBoost = view.findViewById(R.id.switch_auto_game_boost);
        switchEsportsAudio = view.findViewById(R.id.switch_esports_audio);
        Button btnCleanCaches = view.findViewById(R.id.btn_clean_game_caches);

        if (switchOverlayHud != null) {
            switchOverlayHud.setOnCheckedChangeListener(null);
            switchOverlayHud.setChecked(com.gamebooster.app.overlay.FloatingOverlayService.isOverlayEnabled(getContext()));
            switchOverlayHud.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                    switchOverlayHud.setOnCheckedChangeListener(null);
                    switchOverlayHud.setChecked(false);
                    switchOverlayHud.setOnCheckedChangeListener((bv, ic) -> handleOverlayToggle(ic));
                    Toast.makeText(getContext(), "Please grant 'Draw over other apps' permission first", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                    startActivity(intent);
                    return;
                }
                handleOverlayToggle(isChecked);
            });
        }

        if (switchGamingDnd != null) {
            switchGamingDnd.setOnCheckedChangeListener(null);
            switchGamingDnd.setChecked(com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getContext()));
            switchGamingDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.gamespace.GameSpaceDndManager.setGamingDndMode(getContext(), isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "Gaming DND: " + (isChecked ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchAutoGameBoost != null) {
            switchAutoGameBoost.setOnCheckedChangeListener(null);
            switchAutoGameBoost.setChecked(com.gamebooster.app.gamespace.AutoGameMonitorService.isMonitorEnabled(getContext()));
            switchAutoGameBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        com.gamebooster.app.gamespace.AutoGameMonitorService.start(getContext());
                    } else {
                        com.gamebooster.app.gamespace.AutoGameMonitorService.stop(getContext());
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🎮 Auto Game Launch Monitor: ENABLED" : "Auto Game Monitor Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchEsportsAudio != null) {
            switchEsportsAudio.setOnCheckedChangeListener(null);
            switchEsportsAudio.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isAudioBoostEnabled(getContext()));
            switchEsportsAudio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.EsportsAudioEnhancer.setEsportsAudioMode(getContext(), isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🔊 Esports Footstep Audio Boost: ACTIVE" : "Audio Equalizer Normal", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (btnCleanCaches != null) {
            btnCleanCaches.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnCleanCaches.setEnabled(false);
                btnCleanCaches.setText("⏳ Purging Jank & Caches...");
                Toast.makeText(getContext(), "🧹 Starting 1-Tap Legal Jank & Cache Elimination...", Toast.LENGTH_SHORT).show();

                com.gamebooster.app.gamespace.JankAndCacheCleanerEngine.cleanJankAndCacheAsync(getContext(), new com.gamebooster.app.gamespace.JankAndCacheCleanerEngine.CleanCallback() {
                    @Override
                    public void onProgress(String message) {
                        if (isAdded() && getContext() != null && btnCleanCaches != null) {
                            btnCleanCaches.setText(message);
                        }
                    }

                    @Override
                    public void onComplete(boolean success, String summary) {
                        if (isAdded() && getContext() != null && btnCleanCaches != null) {
                            btnCleanCaches.setEnabled(true);
                            btnCleanCaches.setText("🧹 PURGE JANK & CLEAR ALL CACHES");
                            Toast.makeText(getContext(), summary, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            });
        }

        // Card 2.5: Precision Aim - Input & Gyro Tuner
        tvPrecisionAimStatus = view.findViewById(R.id.tv_precision_aim_status);
        switchPrecisionInputTuner = view.findViewById(R.id.switch_precision_input_tuner);
        switchCrosshairOverlay = view.findViewById(R.id.switch_crosshair_overlay);
        btnCrosshairPreset = view.findViewById(R.id.btn_crosshair_preset);
        btnSensitivityCalculator = view.findViewById(R.id.btn_sensitivity_calculator);

        if (precisionSettingsManager != null && switchPrecisionInputTuner != null) {
            switchPrecisionInputTuner.setOnCheckedChangeListener(null);
            switchPrecisionInputTuner.setChecked(precisionSettingsManager.isDeviceTuned());
            switchPrecisionInputTuner.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (isChecked && !ShizukuExecutor.hasShizukuPermission()) {
                    switchPrecisionInputTuner.setOnCheckedChangeListener(null);
                    switchPrecisionInputTuner.setChecked(false);
                    switchPrecisionInputTuner.setOnCheckedChangeListener((bv, ic) -> handlePrecisionTunerToggle(ic));
                    ShizukuManager.showShizukuPermissionDialog(getContext(), "Precision Input Tuner");
                    return;
                }
                handlePrecisionTunerToggle(isChecked);
            });
        }

        if (switchCrosshairOverlay != null) {
            switchCrosshairOverlay.setOnCheckedChangeListener(null);
            switchCrosshairOverlay.setChecked(CrosshairOverlayService.isCrosshairEnabled(getContext()));
            switchCrosshairOverlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                    switchCrosshairOverlay.setOnCheckedChangeListener(null);
                    switchCrosshairOverlay.setChecked(false);
                    switchCrosshairOverlay.setOnCheckedChangeListener((bv, ic) -> handleCrosshairToggle(ic));
                    Toast.makeText(getContext(), "Overlay Permission Required", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                    startActivity(intent);
                    return;
                }
                handleCrosshairToggle(isChecked);
            });
        }

        if (btnCrosshairPreset != null) {
            btnCrosshairPreset.setOnClickListener(v -> showCrosshairPresetDialog());
        }

        if (btnSensitivityCalculator != null) {
            btnSensitivityCalculator.setOnClickListener(v -> showSensitivityCalculatorDialog());
        }

        updatePrecisionAimStatus();

        // Card 3: Hardware & Driver Engines
        switchAngleMode = view.findViewById(R.id.switch_angle_mode);
        switchGameDriver = view.findViewById(R.id.switch_game_driver);
        switchGpuMode = view.findViewById(R.id.switch_gpu_mode);
        switchCpuMode = view.findViewById(R.id.switch_cpu_mode);

        if (getContext() != null) {
            if (switchAngleMode != null) switchAngleMode.setChecked(ManualSettingsPreferences.isAngleModeEnabled(getContext()));
            if (switchGameDriver != null) switchGameDriver.setChecked(ManualSettingsPreferences.isGameDriverEnabled(getContext()));
            if (switchGpuMode != null) switchGpuMode.setChecked("vulkan".equalsIgnoreCase(ManualSettingsPreferences.getGpuMode(getContext())));
            if (switchCpuMode != null) switchCpuMode.setChecked("performance".equalsIgnoreCase(ManualSettingsPreferences.getCpuMode(getContext())));
        }

        if (switchAngleMode != null) {
            switchAngleMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setAngleMode(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    GpuTweaksChannel.setAngleMode(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Google ANGLE Vulkan Driver ENABLED (Per-App: MLBB, PUBGM, CODM, Free Fire, Genshin, HOK, Roblox)" : "ANGLE Driver Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchGameDriver != null) {
            switchGameDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setGameDriverEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    GpuTweaksChannel.setGameDriverMode(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🎮 System Game Driver ENABLED (Per-App: MLBB, PUBGM, CODM, Free Fire, Genshin, HOK, Roblox)" : "Game Driver Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchGpuMode != null) {
            switchGpuMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setGpuMode(getContext(), isChecked ? "vulkan" : "skia");
                AppExecutors.getInstance().executeCommand(() -> {
                    PerformanceChannel.setGpuRenderMode(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ GPU Render Engine: Vulkan 3D" : "GPU Render Engine: Skia 2D", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchCpuMode != null) {
            switchCpuMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setCpuMode(getContext(), isChecked ? "performance" : "schedutil");
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.CpuGovernorChannel.setGovernor(isChecked ? "extreme" : "schedutil");
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🔥 CPU Governor: Performance Extreme" : "CPU Governor: Schedutil", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        // Card 4: Network & Latency Optimization
        btnModeDual = view.findViewById(R.id.btn_mode_dual);
        btnMode5g6g = view.findViewById(R.id.btn_mode_5g_6g);
        btnModeWifi = view.findViewById(R.id.btn_mode_wifi);
        tvActiveNetworkModeStatus = view.findViewById(R.id.tv_active_network_mode_status);

        TextView tvGamePingMs = view.findViewById(R.id.tv_game_ping_ms);
        Button btnPingTest = view.findViewById(R.id.btn_ping_test);
        Button btnDnsCloudflare = view.findViewById(R.id.btn_dns_cloudflare);
        Button btnDnsGoogle = view.findViewById(R.id.btn_dns_google);
        Button btnDnsDefault = view.findViewById(R.id.btn_dns_default);

        switchDualDataWifi = view.findViewById(R.id.switch_dual_data_wifi);
        switch5g6gBoost = view.findViewById(R.id.switch_5g_6g_boost);
        switchWifiLowLatency = view.findViewById(R.id.switch_wifi_low_latency);
        switchTetheringHw = view.findViewById(R.id.switch_tethering_hw);
        switchForceGnss = view.findViewById(R.id.switch_force_gnss);

        if (btnModeDual != null) {
            btnModeDual.setOnClickListener(v -> applyNetworkModeSelection(NetworkOptimizer.NetworkMode.DUAL_ACCELERATION));
        }
        if (btnMode5g6g != null) {
            btnMode5g6g.setOnClickListener(v -> applyNetworkModeSelection(NetworkOptimizer.NetworkMode.CELLULAR_5G_6G_ONLY));
        }
        if (btnModeWifi != null) {
            btnModeWifi.setOnClickListener(v -> applyNetworkModeSelection(NetworkOptimizer.NetworkMode.WIFI_LOW_LATENCY_ONLY));
        }

        if (btnPingTest != null && tvGamePingMs != null) {
            btnPingTest.setOnClickListener(v -> {
                tvGamePingMs.setText("📡 Pinging Game Servers (Cloudflare / Google / Asia Relay)...");
                btnPingTest.setEnabled(false);
                NetworkOptimizer.testGameServerPingAsync(getContext(), (pingMs, serverName, success) -> {
                    if (!isAdded() || getContext() == null) return;
                    btnPingTest.setEnabled(true);
                    String quality = pingMs < 30 ? "[ULTRA LOW LATENCY]" : (pingMs < 60 ? "[EXCELLENT / ESPORTS]" : "[NORMAL]");
                    tvGamePingMs.setText("📡 Game Server Ping: " + pingMs + " ms " + quality + " via " + serverName);
                    tvGamePingMs.setTextColor(pingMs < 30 ? android.graphics.Color.parseColor("#00FF66") : android.graphics.Color.parseColor("#00F0FF"));
                });
            });
        }

        if (btnDnsCloudflare != null) {
            btnDnsCloudflare.setOnClickListener(v -> applyGamingDns(NetworkOptimizer.DnsMode.CLOUDFLARE_1_1_1_1, "⚡ 1.1.1.1 Cloudflare Gaming DNS Applied"));
        }
        if (btnDnsGoogle != null) {
            btnDnsGoogle.setOnClickListener(v -> applyGamingDns(NetworkOptimizer.DnsMode.GOOGLE_8_8_8_8, "🌐 8.8.8.8 Google Gaming DNS Applied"));
        }
        if (btnDnsDefault != null) {
            btnDnsDefault.setOnClickListener(v -> applyGamingDns(NetworkOptimizer.DnsMode.SYSTEM_DEFAULT, "🔄 System Default DNS Restored"));
        }

        if (switchDualDataWifi != null) {
            switchDualDataWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setDualDataAndWifiAcceleration(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Dual Channel Data + Wi-Fi Acceleration ACTIVE" : "Dual Acceleration Disabled", Toast.LENGTH_SHORT).show();
                            updateNetworkModeUi();
                        }
                    });
                });
            });
        }

        if (switch5g6gBoost != null) {
            switch5g6gBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                NetworkOptimizer.set5g6gTurboEnabled(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked ? "📶 5G / 6G NR Low-Latency Radio Mode ACTIVE" : "5G Turbo Disabled", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchWifiLowLatency != null) {
            switchWifiLowLatency.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                NetworkOptimizer.setWifiLowLatencyEnabled(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked ? "🌐 Wi-Fi 6E/7 Low-Latency Chip Mode ACTIVE" : "Wi-Fi Standard Mode", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchTetheringHw != null) {
            switchTetheringHw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setTetherHwEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setTetheringHwAcceleration(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Tethering HW Offload ENABLED" : "Tethering HW Offload Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchForceGnss != null) {
            switchForceGnss.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setForceGnssEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setForceFullGnss(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🛰️ Force Full GNSS Raw Measurements ENABLED" : "GNSS Raw Measurements Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        updateNetworkModeUi();

        // Card 5: Advanced System Tweaks Engine
        tvTweaksStatus = view.findViewById(R.id.tv_tweaks_status);
        bannerDisconnect = view.findViewById(R.id.banner_shizuku_disconnect);
        RecyclerView rvTweaks = view.findViewById(R.id.rv_tweaks_list);

        EditText etSearchTweaks = view.findViewById(R.id.et_search_tweaks);
        ImageView ivClearSearch = view.findViewById(R.id.iv_clear_search);

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

        final TweakCategory[] selectedCategory = {TweakCategory.ALL};

        if (etSearchTweaks != null) {
            etSearchTweaks.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = (s != null) ? s.toString() : "";
                    if (ivClearSearch != null) {
                        ivClearSearch.setVisibility(query.trim().isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    if (tweaksAdapter != null) {
                        tweaksAdapter.filter(query, selectedCategory[0]);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (ivClearSearch != null && etSearchTweaks != null) {
            ivClearSearch.setOnClickListener(v -> etSearchTweaks.setText(""));
        }

        Button[] filterButtons = {btnFilterAll, btnFilterCpuGpu, btnFilterTouch, btnFilterShizuku, btnFilterNetwork};
        TweakCategory[] categories = {TweakCategory.ALL, TweakCategory.CPU_GPU, TweakCategory.TOUCH_DISPLAY, TweakCategory.SHIZUKU_SYSTEM, TweakCategory.NETWORK_LATENCY};

        for (int i = 0; i < filterButtons.length; i++) {
            final int index = i;
            Button btn = filterButtons[i];
            if (btn != null) {
                btn.setOnClickListener(v -> {
                    selectedCategory[0] = categories[index];
                    for (int j = 0; j < filterButtons.length; j++) {
                        if (filterButtons[j] != null) {
                            filterButtons[j].setTextColor(j == index ? android.graphics.Color.parseColor("#00F0FF") : android.graphics.Color.parseColor("#94A3B8"));
                        }
                    }
                    String currentQuery = (etSearchTweaks != null && etSearchTweaks.getText() != null) ? etSearchTweaks.getText().toString() : "";
                    if (tweaksAdapter != null) {
                        tweaksAdapter.filter(currentQuery, selectedCategory[0]);
                    }
                });
            }
        }

        // Card Spoof: Hardware Device Spoofing
        switchDeviceSpoof = view.findViewById(R.id.switch_device_spoof);
        tvSpoofActiveProfile = view.findViewById(R.id.tv_spoof_active_profile);
        rvSpoofProfiles = view.findViewById(R.id.rv_spoof_profiles);

        boolean spoofEnabled = getContext() != null && SpoofPreferences.isSpoofEnabled(getContext());
        if (switchDeviceSpoof != null) {
            switchDeviceSpoof.setChecked(spoofEnabled);
        }
        if (rvSpoofProfiles != null) {
            rvSpoofProfiles.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSpoofProfiles.setVisibility(spoofEnabled ? View.VISIBLE : View.GONE);
            List<SpoofProfile> profileList = new ArrayList<>(DeviceSpooferEngine.getAllProfiles().values());
            spoofProfileAdapter = new SpoofProfileAdapter(getContext(), profileList, profile -> {
                if (getContext() == null || profile == null) return;

                // Request Shizuku permission if available but not granted
                if (!ShizukuExecutor.hasShizukuPermission() && ShizukuExecutor.isShizukuAvailable()) {
                    try {
                        rikka.shizuku.Shizuku.requestPermission(1001);
                    } catch (Throwable ignored) {}
                }

                // 1. Immediately activate in preferences & UI
                SpoofPreferences.setSpoofEnabled(getContext(), true);
                SpoofPreferences.setActiveProfileId(getContext(), profile.id);
                if (switchDeviceSpoof != null) switchDeviceSpoof.setChecked(true);
                if (rvSpoofProfiles != null) rvSpoofProfiles.setVisibility(View.VISIBLE);
                if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(profile.id);
                updateSpoofUiState();
                Toast.makeText(getContext(), "⚡ Activating: " + profile.displayName, Toast.LENGTH_SHORT).show();

                // 2. Perform background real-world hardware & game file injection
                AppExecutors.getInstance().executeCommand(() -> {
                    DeviceSpooferEngine.applyProfile(getContext(), profile, null);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        updateSpoofUiState();
                    });
                });
            });
            rvSpoofProfiles.setAdapter(spoofProfileAdapter);
        }

        if (switchDeviceSpoof != null) {
            switchDeviceSpoof.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                SpoofPreferences.setSpoofEnabled(getContext(), isChecked);
                if (rvSpoofProfiles != null) {
                    rvSpoofProfiles.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
                if (!isChecked) {
                    AppExecutors.getInstance().executeCommand(() -> {
                        DeviceSpooferEngine.resetSpoofing();
                        SpoofPreferences.clearActiveProfile(getContext());
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (isAdded() && getContext() != null) {
                                if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(null);
                                updateSpoofUiState();
                                Toast.makeText(getContext(), "Device Spoofing Disabled", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else {
                    String activeId = SpoofPreferences.getActiveProfileId(getContext());
                    if (activeId == null || activeId.trim().isEmpty()) {
                        activeId = "asus_rog9_pro";
                        SpoofPreferences.setActiveProfileId(getContext(), activeId);
                    }
                    final String targetId = activeId;
                    SpoofProfile prof = DeviceSpooferEngine.getProfileById(targetId);
                    if (prof != null) {
                        if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(targetId);
                        updateSpoofUiState();
                        AppExecutors.getInstance().executeCommand(() -> {
                            DeviceSpooferEngine.applyProfile(getContext(), prof, null);
                            AppExecutors.getInstance().postToMainThread(() -> {
                                if (isAdded() && getContext() != null) {
                                    updateSpoofUiState();
                                    Toast.makeText(getContext(), "⚡ Device Spoof Active: " + prof.displayName, Toast.LENGTH_SHORT).show();
                                }
                            });
                        });
                    } else {
                        updateSpoofUiState();
                    }
                }
            });
        }

        // Card 6: Shizuku Root & ADB Terminal Emulator
        Button btnLaunchTerminal = view.findViewById(R.id.btn_launch_terminal);
        if (btnLaunchTerminal != null) {
            btnLaunchTerminal.setOnClickListener(v -> {
                if (getContext() != null) {
                    Intent terminalIntent = new Intent(getContext(), com.gamebooster.app.terminal.TerminalActivity.class);
                    startActivity(terminalIntent);
                }
            });
        }

        // Card 7: About & Community Links
        TextView tvSettingsVersion = view.findViewById(R.id.tv_settings_version);
        if (tvSettingsVersion != null) {
            tvSettingsVersion.setText("Version " + com.gamebooster.app.BuildConfig.VERSION_NAME + " PRO • Compatible with Android 13, 14, 15, 16");
        }

        Button btnGithubReleases = view.findViewById(R.id.btn_github_releases);
        Button btnFacebookProfile = view.findViewById(R.id.btn_facebook_profile);

        if (btnGithubReleases != null) {
            btnGithubReleases.setOnClickListener(v -> openUrl("https://github.com/willygailo/Game-Launcher/releases"));
        }
        if (btnFacebookProfile != null) {
            btnFacebookProfile.setOnClickListener(v -> openUrl("https://www.facebook.com/https.willy.jr.carnasa.gailo2026.2027"));
        }

        refreshAllStatuses();
        return view;
    }

    private void applyGamingDns(NetworkOptimizer.DnsMode mode, String msg) {
        if (getContext() == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            NetworkOptimizer.applyGamingDns(getContext(), mode);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to open link", Toast.LENGTH_SHORT).show();
        }
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

    private void refreshAllStatuses() {
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        EngineUIHelper.refreshEngineStatus(tvTweaksStatus);
        updateSystemSettingsStatus();
        updateSpoofUiState();
        updatePrecisionAimStatus();
        if (switchPrecisionInputTuner != null && precisionSettingsManager != null) {
            switchPrecisionInputTuner.setOnCheckedChangeListener(null);
            switchPrecisionInputTuner.setChecked(precisionSettingsManager.isDeviceTuned());
            switchPrecisionInputTuner.setOnCheckedChangeListener((bv, ic) -> handlePrecisionTunerToggle(ic));
        }
        boolean alive = ShizukuExecutor.hasShizukuPermission();
        if (tweaksAdapter != null) {
            tweaksAdapter.setShizukuAlive(alive);
        }
        if (bannerDisconnect != null) {
            bannerDisconnect.setVisibility(alive ? View.GONE : View.VISIBLE);
        }
    }

    private void handlePrecisionTunerToggle(boolean isChecked) {
        if (getContext() == null) return;
        if (!ShizukuExecutor.hasShizukuPermission()) {
            if (switchPrecisionInputTuner != null) {
                switchPrecisionInputTuner.setOnCheckedChangeListener(null);
                switchPrecisionInputTuner.setChecked(false);
                switchPrecisionInputTuner.setOnCheckedChangeListener((bv, ic) -> handlePrecisionTunerToggle(ic));
            }
            ShizukuManager.showShizukuPermissionDialog(getContext(), "Precision Input Tuner");
            return;
        }

        AppExecutors.getInstance().executeCommand(() -> {
            boolean success;
            if (isChecked) {
                InputProfile generalProfile = precisionProfileManager.getGeneralGamingProfile();
                success = precisionSettingsManager.applyProfile(generalProfile);
            } else {
                success = precisionSettingsManager.restoreOriginalValues();
            }
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                if (switchPrecisionInputTuner != null) {
                    switchPrecisionInputTuner.setOnCheckedChangeListener(null);
                    if (success) {
                        switchPrecisionInputTuner.setChecked(isChecked);
                        Toast.makeText(getContext(), isChecked ? "🎯 Precision Aim: 1000Hz Input & Zero Slop APPLIED!" : "System Input Defaults Restored", Toast.LENGTH_SHORT).show();
                    } else {
                        switchPrecisionInputTuner.setChecked(!isChecked);
                        Toast.makeText(getContext(), "Failed to modify system properties via Shizuku", Toast.LENGTH_SHORT).show();
                    }
                    switchPrecisionInputTuner.setOnCheckedChangeListener((bv, ic) -> handlePrecisionTunerToggle(ic));
                }
                updatePrecisionAimStatus();
            });
        });
    }

    private void updateSpoofUiState() {
        if (getContext() == null) return;
        boolean enabled = SpoofPreferences.isSpoofEnabled(getContext());
        String activeId = SpoofPreferences.getActiveProfileId(getContext());

        if (tvSpoofActiveProfile != null) {
            if (enabled && activeId != null) {
                SpoofProfile activeProf = DeviceSpooferEngine.getProfileById(activeId);
                if (activeProf != null) {
                    tvSpoofActiveProfile.setText("Active Spoof Profile: " + activeProf.displayName + " (" + activeProf.model + ")");
                    tvSpoofActiveProfile.setTextColor(0xFF00FF66);
                } else {
                    tvSpoofActiveProfile.setText("Active Spoof Profile: ENABLED (No profile selected)");
                    tvSpoofActiveProfile.setTextColor(0xFFFFB800);
                }
            } else {
                tvSpoofActiveProfile.setText("Active Spoof Profile: NONE (Disabled)");
                tvSpoofActiveProfile.setTextColor(0xFF888888);
            }
        }

        if (spoofProfileAdapter != null) {
            spoofProfileAdapter.setActiveProfileId(enabled ? activeId : null);
        }
    }

    private void updateSystemSettingsStatus() {
        if (tvRootStatus == null || getContext() == null) return;
        boolean hasAccess = ShizukuFileManager.hasFullAccess();
        if (hasAccess) {
            tvRootStatus.setText("📁 Shizuku File & Data Control: FULL ACCESS (UNLOCKED)");
            tvRootStatus.setTextColor(0xFF00FF66);
        } else {
            tvRootStatus.setText("📁 Shizuku File & Data Control: DISCONNECTED");
            tvRootStatus.setTextColor(0xFFFFB800);
        }
    }

    private void updatePrecisionAimStatus() {
        if (tvPrecisionAimStatus == null || precisionSettingsManager == null) return;
        boolean tuned = precisionSettingsManager.isDeviceTuned();
        if (tuned) {
            tvPrecisionAimStatus.setText("⚡ OPTIMIZED: 1000Hz Input & 1000Hz Gyro Active");
            tvPrecisionAimStatus.setTextColor(0xFF00FF66);
        } else {
            tvPrecisionAimStatus.setText("Status: Device Input Stock / Default");
            tvPrecisionAimStatus.setTextColor(0xFF888888);
        }
    }

    private void showCrosshairPresetDialog() {
        if (getContext() == null) return;
        CrosshairPreset[] presets = CrosshairPreset.values();
        String[] options = new String[presets.length];
        for (int i = 0; i < presets.length; i++) {
            options[i] = presets[i].getLabel();
        }

        new AlertDialog.Builder(getContext())
                .setTitle("🎯 SELECT CROSSHAIR PRESET")
                .setItems(options, (dialog, which) -> {
                    CrosshairPreset selected = presets[which];
                    CrosshairOverlayService.updatePreset(getContext(), selected);
                    Toast.makeText(getContext(), "🎯 Preset Applied: " + selected.getLabel(), Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showSensitivityCalculatorDialog() {
        if (getContext() == null) return;

        final Context context = getContext();
        String[] games = {"PUBG Mobile / BGMI", "Call of Duty Mobile", "Free Fire / Free Fire Max", "Mobile Legends"};
        String[] modes = {"Balanced / Standard", "Low Recoil / Precision Micro-Aim", "Pro Gyro 400% Ultra Response"};

        android.widget.LinearLayout layout = new android.widget.LinearLayout(context);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        final android.widget.EditText inputDpi = new android.widget.EditText(context);
        inputDpi.setHint("Target Device DPI (e.g. 400, 480, 600)");
        inputDpi.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputDpi.setText("400");
        layout.addView(inputDpi);

        final android.widget.Spinner spinnerGame = new android.widget.Spinner(context);
        android.widget.ArrayAdapter<String> gameAdapter = new android.widget.ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, games);
        spinnerGame.setAdapter(gameAdapter);
        layout.addView(spinnerGame);

        final android.widget.Spinner spinnerMode = new android.widget.Spinner(context);
        android.widget.ArrayAdapter<String> modeAdapter = new android.widget.ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, modes);
        spinnerMode.setAdapter(modeAdapter);
        layout.addView(spinnerMode);

        new AlertDialog.Builder(context)
                .setTitle("🧮 INTERACTIVE GYRO & RECOIL TUNER")
                .setView(layout)
                .setPositiveButton("CALCULATE", (dialog, which) -> {
                    int dpi = 400;
                    try { dpi = Integer.parseInt(inputDpi.getText().toString()); } catch (Exception ignored) {}

                    int gameIdx = spinnerGame.getSelectedItemPosition();
                    int modeIdx = spinnerMode.getSelectedItemPosition();

                    SensitivityCalculator.GameProfile gameProfile = SensitivityCalculator.GameProfile.values()[gameIdx];
                    SensitivityCalculator.RecoilMode recoilMode = SensitivityCalculator.RecoilMode.values()[modeIdx];

                    SensitivityModel m = SensitivityCalculator.calculate(dpi, 6.5, gameProfile, recoilMode);

                    String details = "📊 " + m.summary.toUpperCase() + "\n\n" +
                            "🎯 VIEW & MOVEMENT CALIBRATION:\n" +
                            "• TPP View FOV: " + m.tppFov + "\n" +
                            "• FPP View FOV: " + m.fppFov + "\n" +
                            "• Sprint Sensitivity: " + m.sprintSensitivity + "\n" +
                            "• Aim Assist Tuning: " + m.aimAssistStrength + "%\n\n" +
                            "🎮 CAMERA SENSITIVITY:\n" +
                            "• Free Look: " + m.freeLook + "\n" +
                            "• TPP No Scope: " + m.noScope3rdPerson + "\n" +
                            "• FPP No Scope: " + m.noScope1stPerson + "\n" +
                            "• Red Dot / Holo: " + m.redDotHolo + "\n" +
                            "• 2x Scope: " + m.scope2x + "\n" +
                            "• 4x Scope: " + m.scope4x + "\n\n" +
                            "🌀 GYROSCOPE RECOIL VALUES:\n" +
                            "• Gyro No Scope: " + m.gyroNoScope + "\n" +
                            "• Gyro Red Dot: " + m.gyroRedDot + "\n" +
                            "• Gyro 4x Scope: " + m.gyro4x + "\n\n" +
                            "💡 Enter these values inside game sensitivity settings for optimal legal recoil control.";

                    new AlertDialog.Builder(context)
                            .setTitle("🎯 CALCULATED RECOIL & GYRO PROFILE")
                            .setMessage(details)
                            .setPositiveButton("OK", null)
                            .show();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void handleOverlayToggle(boolean isChecked) {
        if (getContext() == null) return;
        if (isChecked) {
            com.gamebooster.app.overlay.FloatingOverlayService.startOverlay(getContext());
            Toast.makeText(getContext(), "⚡ Performance HUD Overlay Enabled", Toast.LENGTH_SHORT).show();
        } else {
            com.gamebooster.app.overlay.FloatingOverlayService.stopOverlay(getContext());
            Toast.makeText(getContext(), "Overlay Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleCrosshairToggle(boolean isChecked) {
        if (getContext() == null) return;
        if (isChecked) {
            CrosshairOverlayService.startOverlay(getContext());
            Toast.makeText(getContext(), "🎯 Target Overlay Enabled", Toast.LENGTH_SHORT).show();
        } else {
            CrosshairOverlayService.stopOverlay(getContext());
            Toast.makeText(getContext(), "Crosshair Overlay Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyNetworkModeSelection(NetworkOptimizer.NetworkMode mode) {
        if (getContext() == null || mode == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            NetworkOptimizer.setNetworkMode(getContext(), mode);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "⚡ Network Mode: " + mode.label + " ACTIVE", Toast.LENGTH_SHORT).show();
                updateNetworkModeUi();
            });
        });
    }

    private void updateNetworkModeUi() {
        if (getContext() == null) return;
        NetworkOptimizer.NetworkMode mode = NetworkOptimizer.getSavedNetworkMode(getContext());

        if (btnModeDual != null && btnMode5g6g != null && btnModeWifi != null) {
            btnModeDual.setBackgroundResource(mode == NetworkOptimizer.NetworkMode.DUAL_ACCELERATION ? R.drawable.btn_cyber_cyan : R.drawable.btn_cyber_dark);
            btnModeDual.setTextColor(mode == NetworkOptimizer.NetworkMode.DUAL_ACCELERATION ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);

            btnMode5g6g.setBackgroundResource(mode == NetworkOptimizer.NetworkMode.CELLULAR_5G_6G_ONLY ? R.drawable.btn_cyber_green : R.drawable.btn_cyber_dark);
            btnMode5g6g.setTextColor(mode == NetworkOptimizer.NetworkMode.CELLULAR_5G_6G_ONLY ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);

            btnModeWifi.setBackgroundResource(mode == NetworkOptimizer.NetworkMode.WIFI_LOW_LATENCY_ONLY ? R.drawable.btn_cyber_cyan : R.drawable.btn_cyber_dark);
            btnModeWifi.setTextColor(mode == NetworkOptimizer.NetworkMode.WIFI_LOW_LATENCY_ONLY ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
        }

        if (tvActiveNetworkModeStatus != null) {
            tvActiveNetworkModeStatus.setText("⚡ Active Mode: " + mode.label);
            tvActiveNetworkModeStatus.setTextColor(mode == NetworkOptimizer.NetworkMode.DUAL_ACCELERATION
                    ? android.graphics.Color.parseColor("#00FF66")
                    : (mode == NetworkOptimizer.NetworkMode.CELLULAR_5G_6G_ONLY
                    ? android.graphics.Color.parseColor("#00F0FF")
                    : android.graphics.Color.parseColor("#38BDF8")));
        }

        if (switchDualDataWifi != null) {
            switchDualDataWifi.setOnCheckedChangeListener(null);
            switchDualDataWifi.setChecked(mode == NetworkOptimizer.NetworkMode.DUAL_ACCELERATION);
            switchDualDataWifi.setOnCheckedChangeListener((bv, ic) -> {
                if (getContext() == null) return;
                applyNetworkModeSelection(ic ? NetworkOptimizer.NetworkMode.DUAL_ACCELERATION : NetworkOptimizer.NetworkMode.SYSTEM_DEFAULT);
            });
        }

        if (switch5g6gBoost != null) {
            switch5g6gBoost.setOnCheckedChangeListener(null);
            switch5g6gBoost.setChecked(NetworkOptimizer.is5g6gTurboEnabled(getContext()));
            switch5g6gBoost.setOnCheckedChangeListener((bv, ic) -> {
                if (getContext() == null) return;
                NetworkOptimizer.set5g6gTurboEnabled(getContext(), ic);
            });
        }

        if (switchWifiLowLatency != null) {
            switchWifiLowLatency.setOnCheckedChangeListener(null);
            switchWifiLowLatency.setChecked(NetworkOptimizer.isWifiLowLatencyEnabled(getContext()));
            switchWifiLowLatency.setOnCheckedChangeListener((bv, ic) -> {
                if (getContext() == null) return;
                NetworkOptimizer.setWifiLowLatencyEnabled(getContext(), ic);
            });
        }
    }

    private void refreshAllSettingsSwitches() {
        if (getContext() == null) return;

        if (switchOverlayHud != null) {
            switchOverlayHud.setOnCheckedChangeListener(null);
            switchOverlayHud.setChecked(com.gamebooster.app.overlay.FloatingOverlayService.isOverlayEnabled(getContext()));
            switchOverlayHud.setOnCheckedChangeListener((bv, ic) -> handleOverlayToggle(ic));
        }

        if (switchGamingDnd != null) {
            switchGamingDnd.setOnCheckedChangeListener(null);
            switchGamingDnd.setChecked(com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getContext()));
            switchGamingDnd.setOnCheckedChangeListener((bv, ic) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() ->
                    com.gamebooster.app.gamespace.GameSpaceDndManager.setGamingDndMode(getContext(), ic));
            });
        }

        if (switchAutoGameBoost != null) {
            switchAutoGameBoost.setOnCheckedChangeListener(null);
            switchAutoGameBoost.setChecked(com.gamebooster.app.gamespace.AutoGameMonitorService.isMonitorEnabled(getContext()));
            switchAutoGameBoost.setOnCheckedChangeListener((bv, ic) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    if (ic) {
                        com.gamebooster.app.gamespace.AutoGameMonitorService.start(getContext());
                    } else {
                        com.gamebooster.app.gamespace.AutoGameMonitorService.stop(getContext());
                    }
                });
            });
        }

        if (switchEsportsAudio != null) {
            switchEsportsAudio.setOnCheckedChangeListener(null);
            switchEsportsAudio.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isAudioBoostEnabled(getContext()));
            switchEsportsAudio.setOnCheckedChangeListener((bv, ic) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() ->
                    com.gamebooster.app.booster.EsportsAudioEnhancer.setEsportsAudioMode(getContext(), ic));
            });
        }

        if (switchPrecisionInputTuner != null && precisionSettingsManager != null) {
            switchPrecisionInputTuner.setOnCheckedChangeListener(null);
            switchPrecisionInputTuner.setChecked(precisionSettingsManager.isDeviceTuned());
            switchPrecisionInputTuner.setOnCheckedChangeListener((bv, ic) -> handlePrecisionTunerToggle(ic));
        }

        if (switchCrosshairOverlay != null) {
            switchCrosshairOverlay.setOnCheckedChangeListener(null);
            switchCrosshairOverlay.setChecked(CrosshairOverlayService.isCrosshairEnabled(getContext()));
            switchCrosshairOverlay.setOnCheckedChangeListener((bv, ic) -> handleCrosshairToggle(ic));
        }

        if (switchTetheringHw != null) {
            switchTetheringHw.setOnCheckedChangeListener(null);
            switchTetheringHw.setChecked(ManualSettingsPreferences.isTetherHwEnabled(getContext()));
            switchTetheringHw.setOnCheckedChangeListener((bv, ic) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setTetherHwEnabled(getContext(), ic);
                AppExecutors.getInstance().executeCommand(() -> NetworkOptimizer.setTetheringHwAcceleration(ic));
            });
        }

        if (switchForceGnss != null) {
            switchForceGnss.setOnCheckedChangeListener(null);
            switchForceGnss.setChecked(ManualSettingsPreferences.isForceGnssEnabled(getContext()));
            switchForceGnss.setOnCheckedChangeListener((bv, ic) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setForceGnssEnabled(getContext(), ic);
                AppExecutors.getInstance().executeCommand(() -> NetworkOptimizer.setForceFullGnss(ic));
            });
        }

        updateNetworkModeUi();
        updatePrecisionAimStatus();
        updateSystemSettingsStatus();
        updateSpoofUiState();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAllStatuses();
        refreshAllSettingsSwitches();
    }
}
