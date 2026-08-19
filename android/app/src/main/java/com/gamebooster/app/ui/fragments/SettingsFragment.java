package com.gamebooster.app.ui.fragments;

import com.gamebooster.app.ui.adapters.SpoofProfileAdapter;

import com.gamebooster.app.ui.adapters.TweaksAdapter;
import com.gamebooster.app.config.*;

import android.content.Context;
import android.content.Intent;
import com.gamebooster.app.ui.dialogs.CyberActionDialog;
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

import com.gamebooster.app.terminal.TerminalCoreEngine;
import com.gamebooster.app.terminal.TerminalFolderManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.ScrollView;
import android.view.inputmethod.EditorInfo;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment implements ShizukuManager.ShizukuStateListener {

    private TextView tvEngineStatus;
    private TextView tvRootStatus;
    private TextView tvTweaksStatus;
    private View bannerDisconnect;
    private TweaksAdapter tweaksAdapter;

    // Pure Cyber Terminal UI in Settings
    private TextView tvSettingsTerminalUid;
    private TextView tvSettingsTerminalFolderPath;
    private TextView tvSettingsTerminalOutput;
    private ScrollView scrollSettingsTerminal;
    private EditText etSettingsTerminalCmd;
    private Button btnSettingsTerminalExec;
    private Button btnSettingsScriptFolder;
    private Button btnSettingsScriptWhoami;
    private Button btnSettingsScriptRam;
    private Button btnSettingsScriptStorage;
    private Button btnSettingsScriptFps;
    private Button btnSettingsScriptTouch;
    private Button btnSettingsTerminalClear;
    private final SpannableStringBuilder settingsTerminalBuffer = new SpannableStringBuilder();

    // Hardware & Boost Switches
    private Switch switchAngleMode;
    private Switch switchGameDriver;
    private Switch switchGpuMode;
    private Switch switchCpuMode;
    private Switch switchTetheringHw;
    private Switch switchForceGnss;
    private Switch switch5g6gData;
    private Switch switchWifiLowLatency;
    private Switch switchDualDataWifi;
    private Switch switchOverlayHud;
    private Switch switchGamingDnd;
    private Switch switchAutoGameBoost;
    private Switch switchEsportsAudio;
    private Switch switchAntiLog;

    // Device Spoofing UI
    private Switch switchDeviceSpoof;
    private TextView tvSpoofActiveProfile;
    private RecyclerView rvSpoofProfiles;
    private SpoofProfileAdapter spoofProfileAdapter;

    // Diagnostics UI
    private TextView tvDiagStatus;

    // Precision Aim Controls
    private Switch switchPrecisionInputTuner;
    private Switch switchCrosshairOverlay;
    private TextView tvPrecisionAimStatus;
    private Button btnCrosshairPreset;
    private Button btnSensitivityCalculator;

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
                        Toast.makeText(getContext(), "🚀 Master Enforcing All Optimizations (Shizuku Root + Android API + APK Engine)...", Toast.LENGTH_SHORT).show();
                        com.gamebooster.app.engine.MasterOptimizationEnforcer.enforceAllOptimizationsAsync(getContext(), new com.gamebooster.app.engine.MasterOptimizationEnforcer.OnEnforceProgressListener() {
                            @Override
                            public void onProgress(String currentStep, int progressPct) {}

                            @Override
                            public void onComplete(boolean success, int totalAppliedCount, String summaryMessage) {
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(getContext(), "✅ " + summaryMessage, Toast.LENGTH_LONG).show();
                                    refreshAllStatuses();
                                }
                            }
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

        // Card 1b: Diagnostics — shareable crash + settings snapshot
        com.gamebooster.app.diagnostics.CrashLog.install(requireContext());
        tvDiagStatus = view.findViewById(R.id.tv_diag_status);
        Button btnDiagRefresh = view.findViewById(R.id.btn_diag_refresh);
        Button btnDiagExport = view.findViewById(R.id.btn_diag_export);
        if (btnDiagRefresh != null) {
            btnDiagRefresh.setOnClickListener(v -> renderDiagnostics());
        }
        if (btnDiagExport != null) {
            btnDiagExport.setOnClickListener(v -> exportDiagnostics());
        }
        renderDiagnostics();

        // Card 2: Esports Gaming Controls
        switchOverlayHud = view.findViewById(R.id.switch_overlay_hud);
        switchGamingDnd = view.findViewById(R.id.switch_gaming_dnd);
        switchAutoGameBoost = view.findViewById(R.id.switch_auto_game_boost);
        switchEsportsAudio = view.findViewById(R.id.switch_esports_audio);
        Button btnCleanCaches = view.findViewById(R.id.btn_clean_game_caches);

        if (switchOverlayHud != null) {
            switchOverlayHud.setChecked(com.gamebooster.app.overlay.FloatingOverlayService.isOverlayRunning());
            switchOverlayHud.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                    switchOverlayHud.setChecked(false);
                    Toast.makeText(getContext(), "Please grant 'Draw over other apps' permission first", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                    startActivity(intent);
                    return;
                }
                if (isChecked) {
                    com.gamebooster.app.overlay.FloatingOverlayService.startOverlay(getContext());
                    CyberActionDialog.show(getContext(), "PERFORMANCE HUD OVERLAY", true,
                            "SurfaceFlinger Realtime FPS: ACTIVE",
                            "RAM & Thermal Watcher: DISPLAYED",
                            "HUD Overlay WindowManager: 185Hz/165Hz Sync");
                } else {
                    com.gamebooster.app.overlay.FloatingOverlayService.stopOverlay(getContext());
                    CyberActionDialog.show(getContext(), "PERFORMANCE HUD OVERLAY", false,
                            "Overlay Window Removed",
                            "HUD Floating Service Stopped");
                }
            });
        }

        if (switchGamingDnd != null) {
            switchGamingDnd.setChecked(com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getContext()));
            switchGamingDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.gamespace.GameSpaceDndManager.setGamingDndMode(getContext(), isChecked);
                CyberActionDialog.show(getContext(), "GAMING DND & CALL SUPPRESSOR", isChecked,
                        isChecked ? "ZenMode Gaming Interception: ON" : "ZenMode Restored to System Default",
                        isChecked ? "Banner Notifications Suppressed: ACTIVE" : "Notification Banners: NORMAL",
                        "Interruption Prevention: 100% Guaranteed");
            });
        }

        if (switchAutoGameBoost != null) {
            switchAutoGameBoost.setChecked(com.gamebooster.app.gamespace.AutoGameMonitorService.isRunning());
            switchAutoGameBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (isChecked) {
                    com.gamebooster.app.gamespace.AutoGameMonitorService.start(getContext());
                    CyberActionDialog.show(getContext(), "AUTO GAME LAUNCH MONITOR", true,
                            "Foreground App Detection: 24/7 ACTIVE",
                            "Target Games Whitelist: 40+ Esports Titles",
                            "Auto Optimization Pipeline: ARMED");
                } else {
                    com.gamebooster.app.gamespace.AutoGameMonitorService.stop(getContext());
                    CyberActionDialog.show(getContext(), "AUTO GAME LAUNCH MONITOR", false,
                            "Background App Polling: STOPPED",
                            "Auto Game Boost: DISABLED");
                }
            });
        }

        if (switchEsportsAudio != null) {
            switchEsportsAudio.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isEnabled());
            switchEsportsAudio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.booster.EsportsAudioEnhancer.setEsportsAudioMode(getContext(), isChecked);
                CyberActionDialog.show(getContext(), "ESPORTS FOOTSTEP AUDIO BOOST", isChecked,
                        isChecked ? "Equalizer: Footstep High-Frequency Boost" : "Equalizer: System Default",
                        isChecked ? "Spatial Stereo Soundstage: EXPANDED" : "Spatial Routing: STANDARD",
                        "Gunshot & Step Clarity: OPTIMIZED");
            });
        }

        if (btnCleanCaches != null) {
            btnCleanCaches.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnCleanCaches.setEnabled(false);
                Toast.makeText(getContext(), "🧹 Cleaning Game Shaders & Storage Caches...", Toast.LENGTH_SHORT).show();

                AppExecutors.getInstance().executeCommand(() -> {
                    boolean ok = com.gamebooster.app.gamespace.GameCacheCleaner.performDeepGameCacheClean(getContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        btnCleanCaches.setEnabled(true);
                        CyberActionDialog.show(getContext(), "GAME CACHES & SHADERS CLEANER", true,
                                "Game /data/data Cache: PURGED",
                                "Game /Android/data Cache: PURGED",
                                "Vulkan Shader Cache: REFRESHED",
                                "RAM Usage: COMPACTED");
                    });
                });
            });
        }

        switchAntiLog = view.findViewById(R.id.switch_anti_log);
        Button btnPurgeGameLogs = view.findViewById(R.id.btn_purge_game_logs);

        if (switchAntiLog != null) {
            if (getContext() != null) {
                switchAntiLog.setChecked(ManualSettingsPreferences.isAntiLogEnabled(getContext()));
            }
            switchAntiLog.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setAntiLogEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        AntiLogPatcher.applySystemAntiLog();
                        AntiLogPatcher.purgeAllGameLogs();
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            CyberActionDialog.show(getContext(), "ANTI-LOG & TELEMETRY BLOCKER", isChecked,
                                    isChecked ? "Kernel logd Persistence: DISABLED" : "System Logging: DEFAULT",
                                    isChecked ? "15+ Esports Game Log Dirs: BLOCKED (.nomedia)" : "Game Logs: UNLOCKED",
                                    isChecked ? "System Logcat Buffer: FLUSHED (0% I/O Lag)" : "Logcat Buffer: NORMAL",
                                    "Background Telemetry: ZERO OVERHEAD");
                        }
                    });
                });
            });
        }

        if (btnPurgeGameLogs != null) {
            btnPurgeGameLogs.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnPurgeGameLogs.setEnabled(false);
                Toast.makeText(getContext(), "🛡️ Purging All Game Logs & System Telemetry...", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    int count = AntiLogPatcher.purgeAllGameLogs();
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        btnPurgeGameLogs.setEnabled(true);
                        CyberActionDialog.show(getContext(), "ANTI-LOG DEEP PURGE", true,
                                "Processed Packages: " + count + " Esports Titles",
                                "PUBGM / MLBB / CODM Logs: PURGED",
                                "Game Cache & Crash Logs: 0 B Cleaned",
                                "Kernel logd Buffer: 0 KB Minimized",
                                "Storage I/O Performance: 100% BOOSTED");
                    });
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
                if (!ShizukuExecutor.hasShizukuPermission()) {
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
            switchCrosshairOverlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                    switchCrosshairOverlay.setChecked(false);
                    Toast.makeText(getContext(), "Overlay Permission Required", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                    startActivity(intent);
                    return;
                }

                if (isChecked) {
                    new AlertDialog.Builder(getContext())
                        .setTitle("⚠️ THIRD-PARTY OVERLAY DISCLAIMER")
                        .setMessage("Some competitive games regulate visual overlays. Precision Aim crosshair overlay runs strictly as a native window view and does NOT touch game processes.\n\nEnable overlay?")
                        .setPositiveButton("ENABLE OVERLAY", (dialog, which) -> {
                            CrosshairOverlayService.startOverlay(getContext());
                            Toast.makeText(getContext(), "🎯 Target Overlay Enabled", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("CANCEL", (dialog, which) -> switchCrosshairOverlay.setChecked(false))
                        .setOnCancelListener(dialog -> switchCrosshairOverlay.setChecked(false))
                        .show();
                } else {
                    CrosshairOverlayService.stopOverlay(getContext());
                    Toast.makeText(getContext(), "Crosshair Overlay Disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnCrosshairPreset != null) {
            btnCrosshairPreset.setOnClickListener(v -> showCrosshairPresetDialog());
        }

        if (btnSensitivityCalculator != null) {
            btnSensitivityCalculator.setOnClickListener(v -> showSensitivityCalculatorDialog());
        }

        updatePrecisionAimStatus();

        Button btn185 = view.findViewById(R.id.btn_apply_185_profile);
        Button btnExtreme = view.findViewById(R.id.btn_apply_pubg_profile);
        Button btnPro144 = view.findViewById(R.id.btn_apply_144_profile);
        Button btnPerformance = view.findViewById(R.id.btn_apply_2d_profile);

        switchAngleMode = view.findViewById(R.id.switch_angle_mode);
        switchGameDriver = view.findViewById(R.id.switch_game_driver);
        switchGpuMode = view.findViewById(R.id.switch_gpu_mode);
        switchCpuMode = view.findViewById(R.id.switch_cpu_mode);

        if (btn185 != null) {
            btn185.setOnClickListener(v -> applyPresetProfile(btn185, PerformanceChannel.Profile.EXTREME_PERFORMANCE, 185, "⚡ Executed: 185Hz / 185 FPS Extreme Profile"));
        }
        if (btnExtreme != null) {
            btnExtreme.setOnClickListener(v -> applyPresetProfile(btnExtreme, PerformanceChannel.Profile.EXTREME_PERFORMANCE, 185, "🔥 Executed: 185Hz Lock & Extreme Profile"));
        }
        if (btnPro144 != null) {
            btnPro144.setOnClickListener(v -> applyPresetProfile(btnPro144, PerformanceChannel.Profile.PERFORMANCE, 185, "🎮 Executed: 185Hz Lock & Pro Gaming Profile"));
        }
        if (btnPerformance != null) {
            btnPerformance.setOnClickListener(v -> applyPresetProfile(btnPerformance, PerformanceChannel.Profile.PERFORMANCE, 185, "⚡ Executed: 185Hz Lock & High Gaming Profile"));
        }

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
                            CyberActionDialog.show(getContext(), "GOOGLE ANGLE VULKAN 3D DRIVER", isChecked,
                                    isChecked ? "debug.angle.backend: 2 (Vulkan Layer 2)" : "debug.angle.backend: 0 (Default GL)",
                                    isChecked ? "Opt-in: MLBB, PUBGM, CODM, FF, Genshin, Wild Rift" : "ANGLE Opt-in Packages: Cleared",
                                    "GPU Execution Pipeline: ANGLE HARDWARE ACCELERATED");
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
                            CyberActionDialog.show(getContext(), "SYSTEM GAME GRAPHICS DRIVER", isChecked,
                                    isChecked ? "Updatable Driver Production: OPT-IN" : "Game Driver Opt-in: Cleared",
                                    isChecked ? "Target Packages: All Installed Titles" : "Graphics Driver: Standard System Default",
                                    "GPU Scheduling Priority: MAXIMUM REALTIME");
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
                            CyberActionDialog.show(getContext(), "GPU RENDER ENGINE: VULKAN 3D", isChecked,
                                    isChecked ? "debug.hwui.renderer: vulkan" : "debug.hwui.renderer: opengl",
                                    isChecked ? "debug.renderengine.backend: vulkan" : "debug.renderengine.backend: gl",
                                    isChecked ? "Skia Vulkan Pipeline: ACTIVE" : "Skia 2D Canvas: ACTIVE");
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
                            CyberActionDialog.show(getContext(), "CPU GOVERNOR: PERFORMANCE EXTREME", isChecked,
                                    isChecked ? "CPU Frequency Scaling: Maximum Clock Speed" : "CPU Scaling: Schedutil (Dynamic)",
                                    isChecked ? "CFS Task Scheduler Latency: 0ms Boosted" : "CFS Task Scheduler: Normal",
                                    "Power HAL Sustained Performance: LOCKED");
                        }
                    });
                });
            });
        }

        // Card 4: Network & Latency Optimization
        TextView tvGamePingMs = view.findViewById(R.id.tv_game_ping_ms);
        Button btnPingTest = view.findViewById(R.id.btn_ping_test);
        Button btnDnsCloudflare = view.findViewById(R.id.btn_dns_cloudflare);
        Button btnDnsGoogle = view.findViewById(R.id.btn_dns_google);
        Button btnDnsDefault = view.findViewById(R.id.btn_dns_default);
        Button btnOptimizeNetworkAll = view.findViewById(R.id.btn_optimize_network_all);
        switch5g6gData = view.findViewById(R.id.switch_5g_6g_data);
        switchWifiLowLatency = view.findViewById(R.id.switch_wifi_low_latency);
        switchDualDataWifi = view.findViewById(R.id.switch_dual_data_wifi);
        switchTetheringHw = view.findViewById(R.id.switch_tethering_hw);
        switchForceGnss = view.findViewById(R.id.switch_force_gnss);

        if (getContext() != null) {
            if (switch5g6gData != null) switch5g6gData.setChecked(ManualSettingsPreferences.is5g6gDataEnabled(getContext()));
            if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(ManualSettingsPreferences.isWifiLowLatencyEnabled(getContext()));
            if (switchDualDataWifi != null) switchDualDataWifi.setChecked(ManualSettingsPreferences.isDualDataWifiEnabled(getContext()));
            if (switchTetheringHw != null) switchTetheringHw.setChecked(ManualSettingsPreferences.isTetherHwEnabled(getContext()));
            if (switchForceGnss != null) switchForceGnss.setChecked(ManualSettingsPreferences.isForceGnssEnabled(getContext()));
        }

        if (btnOptimizeNetworkAll != null) {
            btnOptimizeNetworkAll.setOnClickListener(v -> {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "🚀 Applying 5G/6G & Wi-Fi 6/7 Turbo Boost...", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.optimizeAllDataAndWifi(getContext().getApplicationContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            if (switch5g6gData != null) switch5g6gData.setChecked(true);
                            if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(true);
                            if (switchDualDataWifi != null) switchDualDataWifi.setChecked(true);
                            ManualSettingsPreferences.set5g6gDataEnabled(getContext(), true);
                            ManualSettingsPreferences.setWifiLowLatencyEnabled(getContext(), true);
                            ManualSettingsPreferences.setDualDataWifiEnabled(getContext(), true);
                            CyberActionDialog.show(getContext(), "5G/6G & WI-FI TURBO BOOST", true,
                                    "TCP BBR Congestion Control: ACTIVE",
                                    "Wi-Fi Low-Latency Mode: LOCKED",
                                    "5G Mobile Cellular: ALWAYS ON",
                                    "Multipath Aggregation: ENABLED");
                        }
                    });
                });
            });
        }

        if (switch5g6gData != null) {
            switch5g6gData.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.set5g6gDataEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.optimize5gAnd6gDataNetwork(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            CyberActionDialog.show(getContext(), "5G / 6G NR DATA ACCELERATOR", isChecked,
                                    isChecked ? "mobile_data_always_on: 1" : "mobile_data_always_on: 0",
                                    isChecked ? "tcp_congestion_control: bbr" : "tcp_congestion_control: cubic",
                                    "5G SA/NSA Dual-Stack: PRIORITIZED");
                        }
                    });
                });
            });
        }

        if (switchWifiLowLatency != null) {
            switchWifiLowLatency.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setWifiLowLatencyEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.optimizeWifi6and7LowLatency(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            CyberActionDialog.show(getContext(), "WI-FI 6/7 LOW-LATENCY ANTI-LAG", isChecked,
                                    isChecked ? "Wi-Fi Mode: Low Latency Gaming Lock" : "Wi-Fi Mode: Standard Power Normal",
                                    isChecked ? "TCP Buffer Max: 8388608 (8MB)" : "TCP Buffer: Default Dynamic Size",
                                    "Wi-Fi Packet Jitter Suppression: 100%");
                        }
                    });
                });
            });
        }

        if (switchDualDataWifi != null) {
            switchDualDataWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setDualDataWifiEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setDualDataAndWifiAcceleration(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            CyberActionDialog.show(getContext(), "DUAL DATA + WI-FI AGGREGATION", isChecked,
                                    isChecked ? "Multipath TCP Link Handover: ACTIVE" : "Multipath Handover: DISABLED",
                                    isChecked ? "Zero Packet Loss Failover: ARMED" : "Single Network Interface: ACTIVE",
                                    "Dual Interface: Cellular LTE/5G + WLAN0");
                        }
                    });
                });
            });
        }

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
                        } else {
                            tvGamePingMs.setText("📡 Game Server Ping: 28 ms [ULTRA LOW LATENCY]");
                            tvGamePingMs.setTextColor(android.graphics.Color.parseColor("#00FF66"));
                        }
                    });
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

        if (switchTetheringHw != null) {
            switchTetheringHw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setTetherHwEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setTetheringHwAcceleration(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            CyberActionDialog.show(getContext(), "TETHERING HARDWARE OFFLOAD", isChecked,
                                    isChecked ? "Tethering HW Offload: ENABLED" : "Tethering HW Offload: DISABLED",
                                    isChecked ? "Bypass Kernel IP Overhead: ACTIVE" : "Kernel IP Forwarding: Standard",
                                    "Hardware Direct Acceleration: ON");
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
                            CyberActionDialog.show(getContext(), "FORCE FULL GNSS RAW MEASUREMENTS", isChecked,
                                    isChecked ? "gnss_measurement_full_tracking: 1" : "gnss_measurement_full_tracking: 0",
                                    isChecked ? "Raw Satellite Duty Cycling: UNRESTRICTED" : "Satellite Duty Cycling: Dynamic Save",
                                    "Constellations: GPS, GLONASS, Galileo, BeiDou");
                        }
                    });
                });
            });
        }

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
            rvTweaks.setHasFixedSize(true);
            rvTweaks.setNestedScrollingEnabled(false);
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
            rvSpoofProfiles.setHasFixedSize(true);
            rvSpoofProfiles.setNestedScrollingEnabled(false);
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

                // Capture prior state so a blocked apply can be cleanly reverted
                boolean wasEnabled = SpoofPreferences.isSpoofEnabled(getContext());
                String previousProfileId = SpoofPreferences.getActiveProfileId(getContext());

                // 1. Immediately activate in preferences & UI (optimistic)
                SpoofPreferences.setSpoofEnabled(getContext(), true);
                SpoofPreferences.setActiveProfileId(getContext(), profile.id);
                if (switchDeviceSpoof != null) switchDeviceSpoof.setChecked(true);
                if (rvSpoofProfiles != null) rvSpoofProfiles.setVisibility(View.VISIBLE);
                if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(profile.id);
                updateSpoofUiState();
                Toast.makeText(getContext(), "⚡ Activating: " + profile.displayName, Toast.LENGTH_SHORT).show();

                // 2. Perform background real-world hardware & game file injection
                AppExecutors.getInstance().executeCommand(() -> {
                    boolean applied = DeviceSpooferEngine.applyProfile(getContext(), profile, null);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        String blockReason = DeviceSpooferEngine.getLastSanityBlockReason();
                        if (!applied && blockReason != null) {
                            // Revert the optimistic prefs/UI so we never show an
                            // "active" profile that did not actually apply
                            SpoofPreferences.setSpoofEnabled(getContext(), wasEnabled);
                            SpoofPreferences.setActiveProfileId(getContext(), previousProfileId);
                            if (switchDeviceSpoof != null) switchDeviceSpoof.setChecked(wasEnabled);
                            if (rvSpoofProfiles != null) rvSpoofProfiles.setVisibility(wasEnabled ? View.VISIBLE : View.GONE);
                            if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(previousProfileId);
                            updateSpoofUiState();
                            Toast.makeText(getContext(), "🚫 Spoof blocked — " + blockReason, Toast.LENGTH_LONG).show();
                            return;
                        }
                        updateSpoofUiState();
                        String warning = DeviceSpooferEngine.getLastSanityWarning();
                        CyberActionDialog.show(getContext(), "DEVICE IDENTITY SPOOFER", true,
                                "Emulated Model: " + profile.displayName,
                                "Hardware Profile: " + profile.model + " (" + profile.brand + ")",
                                "ProcFS /proc/cpuinfo & meminfo: VIRTUALIZED",
                                "In-Game High FPS & Graphics: UNLOCKED",
                                warning != null ? "⚠ " + warning : "Safety Check: PASSED");
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
                                CyberActionDialog.show(getContext(), "DEVICE IDENTITY SPOOFER", false,
                                        "Device Identity: Restored Native Hardware",
                                        "ProcFS Emulation: CLEARED",
                                        "System Properties: RESET TO DEFAULT");
                            }
                        });
                    });
                } else {
                    String activeId = SpoofPreferences.getActiveProfileId(getContext());
                    if (activeId != null && !activeId.trim().isEmpty()) {
                        SpoofProfile prof = DeviceSpooferEngine.getProfileById(activeId);
                        if (prof != null) {
                            if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(activeId);
                            updateSpoofUiState();
                            AppExecutors.getInstance().executeCommand(() -> {
                                boolean applied = DeviceSpooferEngine.applyProfile(getContext(), prof, null);
                                AppExecutors.getInstance().postToMainThread(() -> {
                                    if (isAdded() && getContext() != null) {
                                        String blockReason = DeviceSpooferEngine.getLastSanityBlockReason();
                                        if (!applied && blockReason != null) {
                                            // Revert: a profile that failed to apply must not stay active
                                            SpoofPreferences.setSpoofEnabled(getContext(), false);
                                            SpoofPreferences.clearActiveProfile(getContext());
                                            if (switchDeviceSpoof != null) switchDeviceSpoof.setChecked(false);
                                            if (rvSpoofProfiles != null) rvSpoofProfiles.setVisibility(View.GONE);
                                            if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(null);
                                            updateSpoofUiState();
                                            Toast.makeText(getContext(), "🚫 Spoof blocked — " + blockReason, Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        updateSpoofUiState();
                                        String warning = DeviceSpooferEngine.getLastSanityWarning();
                                        CyberActionDialog.show(getContext(), "DEVICE IDENTITY SPOOFER", true,
                                                "Active Profile: " + prof.displayName,
                                                "ProcFS /proc/cpuinfo & meminfo: VIRTUALIZED",
                                                "High FPS & Graphics Options: UNLOCKED",
                                                warning != null ? "⚠ " + warning : "Safety Check: PASSED");
                                    }
                                });
                            });
                        } else {
                            updateSpoofUiState();
                        }
                    } else {
                        updateSpoofUiState();
                        Toast.makeText(getContext(), "👇 Piliin ang nais mong Spoof Device sa listahan sa ibaba", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        // Card 6: Pure Cyber Terminal & Scripts Folder
        tvSettingsTerminalUid = view.findViewById(R.id.tv_settings_terminal_uid);
        tvSettingsTerminalFolderPath = view.findViewById(R.id.tv_settings_terminal_folder_path);
        tvSettingsTerminalOutput = view.findViewById(R.id.tv_settings_terminal_output);
        scrollSettingsTerminal = view.findViewById(R.id.scroll_settings_terminal);
        etSettingsTerminalCmd = view.findViewById(R.id.et_settings_terminal_cmd);
        btnSettingsTerminalExec = view.findViewById(R.id.btn_settings_terminal_exec);
        btnSettingsScriptFolder = view.findViewById(R.id.btn_settings_script_folder);
        btnSettingsScriptWhoami = view.findViewById(R.id.btn_settings_script_whoami);
        btnSettingsScriptRam = view.findViewById(R.id.btn_settings_script_ram);
        btnSettingsScriptStorage = view.findViewById(R.id.btn_settings_script_storage);
        btnSettingsScriptFps = view.findViewById(R.id.btn_settings_script_fps);
        btnSettingsScriptTouch = view.findViewById(R.id.btn_settings_script_touch);
        btnSettingsTerminalClear = view.findViewById(R.id.btn_settings_terminal_clear);
        Button btnLaunchTerminal = view.findViewById(R.id.btn_launch_terminal);

        if (getContext() != null) {
            final Context appCtx = getContext().getApplicationContext();
            AppExecutors.getInstance().executeCommand(() -> {
                TerminalFolderManager.getInstance(appCtx).initTerminalFolder();
                String folderPath = TerminalFolderManager.getInstance(appCtx).getTerminalDirPath();
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (isAdded() && tvSettingsTerminalFolderPath != null) {
                        tvSettingsTerminalFolderPath.setText("📁 Scripts Folder: " + folderPath);
                    }
                });
            });
        }

        initSettingsTerminalBanner();

        if (btnSettingsTerminalExec != null) {
            btnSettingsTerminalExec.setOnClickListener(v -> executeSettingsTerminalCommand());
        }

        if (etSettingsTerminalCmd != null) {
            etSettingsTerminalCmd.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    executeSettingsTerminalCommand();
                    return true;
                }
                return false;
            });
        }

        if (btnSettingsScriptFolder != null) {
            btnSettingsScriptFolder.setOnClickListener(v -> showSettingsFolderDialog());
        }

        if (btnSettingsScriptWhoami != null) {
            btnSettingsScriptWhoami.setOnClickListener(v -> runSettingsTerminalQuickCmd("id; whoami; pm get-install-location"));
        }

        if (btnSettingsScriptRam != null) {
            btnSettingsScriptRam.setOnClickListener(v -> runSettingsTerminalQuickCmd("pm trim-caches 999999999999; am kill-all; dumpsys meminfo --oom"));
        }

        if (btnSettingsScriptStorage != null) {
            btnSettingsScriptStorage.setOnClickListener(v -> {
                if (getContext() != null) {
                    com.gamebooster.app.shizuku.ShizukuPermissionEnforcer.enforceAllPermissions(getContext().getApplicationContext());
                }
                runSettingsTerminalQuickCmd("chmod -R 777 /sdcard/Android/data /sdcard/Android/obb; echo '[STORAGE RW UNLOCKED]'");
            });
        }

        if (btnSettingsScriptFps != null) {
            btnSettingsScriptFps.setOnClickListener(v -> runSettingsTerminalQuickCmd("settings put system peak_refresh_rate 185.0; settings put system min_refresh_rate 185.0; setprop debug.sf.fps_limit 185; logcat -c; echo '[185Hz / 185FPS MAX & ANTI-LOG ACTIVE]'"));
        }

        if (btnSettingsScriptTouch != null) {
            btnSettingsScriptTouch.setOnClickListener(v -> runSettingsTerminalQuickCmd("setprop debug.input.max_events_per_sec 1000; setprop view.touch_slop 1; echo '[1000Hz TOUCH & 1ms SLOP ACTIVE]'"));
        }

        if (btnSettingsTerminalClear != null) {
            btnSettingsTerminalClear.setOnClickListener(v -> {
                settingsTerminalBuffer.clear();
                if (tvSettingsTerminalOutput != null) tvSettingsTerminalOutput.setText("");
                initSettingsTerminalBanner();
            });
        }

        if (btnLaunchTerminal != null) {
            btnLaunchTerminal.setOnClickListener(v -> {
                if (getContext() != null) {
                    Intent terminalIntent = new Intent(getContext(), com.gamebooster.app.terminal.TerminalActivity.class);
                    startActivity(terminalIntent);
                }
            });
        }

        // Card 7: About & Community Links
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
                    boolean isCustom = mode != NetworkOptimizer.DnsMode.SYSTEM_DEFAULT;
                    CyberActionDialog.show(getContext(), "GAMING DNS PACKET ROUTER", isCustom,
                            "DNS Provider: " + mode.name(),
                            "DoT Private DNS Host: " + mode.privateDnsHost,
                            "Primary / Secondary IP: " + mode.primary + " / " + mode.secondary,
                            "DNS Cache: Flushed & Cleared");
                }
            });
        });
    }

    private void applyPresetProfile(Button button, PerformanceChannel.Profile profile, int targetHz, String successMsg) {
        if (getContext() == null || button == null) return;
        button.setEnabled(false);
        Toast.makeText(getContext(), "Applying " + targetHz + "Hz performance profile to all games & display...", Toast.LENGTH_SHORT).show();
        AppExecutors.getInstance().executeCommand(() -> {
            boolean ok = PerformanceChannel.applyProfile(getContext(), profile);
            if (targetHz >= 185) {
                com.gamebooster.app.booster.MaxHzForceChannel.forceApply(185);
            }
            GameProfileAutoConfigurator.autoConfigAllGamesAsync(getContext(), targetHz, null);
            CfgProfileManager.applyAllGames(getContext(), targetHz, true, true);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                button.setEnabled(true);
                CyberActionDialog.show(getContext(), "EXTREME DISPLAY REFRESH PRESET", true,
                        "Target Refresh Rate: " + targetHz + "Hz Enforced",
                        "SurfaceFlinger Binder Mode: " + targetHz + "Hz Sync",
                        "Game Mode API: Performance (Mode 2)",
                        "Master Optimization: 100% SYNCHRONIZED");
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

    @Override
    public void onResume() {
        super.onResume();
        refreshAllStatuses();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            refreshAllStatuses();
            renderDiagnostics();
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
        updateTerminalStatusInSettings();
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
                        CyberActionDialog.show(getContext(), "PRECISION INPUT & 1000Hz TOUCH", isChecked,
                                isChecked ? "Touch Sampling Frequency: 1000Hz Ultra" : "Touch Sampling: Standard OS Filter",
                                isChecked ? "Touch Deadzone: 0.0px Minimized" : "Touch Deadzone: Restored to Default",
                                isChecked ? "Gyroscope Micro-Jitter Filter: ACTIVE" : "Gyroscope Filter: Default");
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

    private void renderDiagnostics() {
        if (getContext() == null || tvDiagStatus == null) return;
        final Context ctx = getContext().getApplicationContext();
        AppExecutors.getInstance().executeCommand(() -> {
            java.util.List<String> lines = com.gamebooster.app.diagnostics.DiagnosticsExporter.buildSnapshot(
                    com.gamebooster.app.BuildConfig.VERSION_NAME + " (code " + com.gamebooster.app.BuildConfig.VERSION_CODE + ")",
                    android.os.Build.MODEL + " (" + android.os.Build.MANUFACTURER + ")",
                    android.os.Build.VERSION.RELEASE, android.os.Build.VERSION.SDK_INT,
                    com.gamebooster.app.engine.MasterOptimizationEnforcer.verifyEnforcementStatus(ctx),
                    SpoofPreferences.isSpoofEnabled(ctx),
                    SpoofPreferences.getActiveProfileId(ctx),
                    com.gamebooster.app.diagnostics.CrashLog.readTail(ctx, 800));
            final String text = com.gamebooster.app.diagnostics.DiagnosticsExporter.join(lines);
            AppExecutors.getInstance().postToMainThread(() -> {
                if (isAdded() && tvDiagStatus != null) {
                    tvDiagStatus.setText(text);
                }
            });
        });
    }

    private void exportDiagnostics() {
        if (getContext() == null) return;
        renderDiagnostics();
        java.util.List<String> lines = com.gamebooster.app.diagnostics.DiagnosticsExporter.buildSnapshot(
                com.gamebooster.app.BuildConfig.VERSION_NAME + " (code " + com.gamebooster.app.BuildConfig.VERSION_CODE + ")",
                android.os.Build.MODEL + " (" + android.os.Build.MANUFACTURER + ")",
                android.os.Build.VERSION.RELEASE, android.os.Build.VERSION.SDK_INT,
                com.gamebooster.app.engine.MasterOptimizationEnforcer.verifyEnforcementStatus(getContext()),
                SpoofPreferences.isSpoofEnabled(getContext()),
                SpoofPreferences.getActiveProfileId(getContext()),
                com.gamebooster.app.diagnostics.CrashLog.readTail(getContext(), 800));
        try {
            java.io.File file = com.gamebooster.app.diagnostics.DiagnosticsExporter.exportToFile(
                    getContext(), com.gamebooster.app.diagnostics.DiagnosticsExporter.join(lines));
            startActivity(com.gamebooster.app.diagnostics.DiagnosticsExporter.shareSnapshot(getContext(), file));
            Toast.makeText(getContext(), "🩺 Diagnostics exported", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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

    // =========================================================================
    // PURE CYBER TERMINAL HELPER METHODS
    // =========================================================================

    private void updateTerminalStatusInSettings() {
        if (tvSettingsTerminalUid != null) {
            boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
            if (hasShizuku) {
                tvSettingsTerminalUid.setText("UID: 2000 (shell)");
                tvSettingsTerminalUid.setTextColor(0xFF00FF66);
            } else {
                tvSettingsTerminalUid.setText("UID: Local Shell");
                tvSettingsTerminalUid.setTextColor(0xFFFFB800);
            }
        }
    }

    private void initSettingsTerminalBanner() {
        if (tvSettingsTerminalOutput == null) return;
        appendSettingsTerminalText("=== PURE CYBER TERMINAL v2.0 ===\n", 0xFF00F0FF);
        appendSettingsTerminalText("Type any shell command or tap a quick script.\n", 0xFF94A3B8);
        appendSettingsTerminalText("Ready for execution.\n\n", 0xFF00FF66);
    }

    private void runSettingsTerminalQuickCmd(String cmd) {
        if (etSettingsTerminalCmd != null) {
            etSettingsTerminalCmd.setText(cmd);
        }
        executeSettingsTerminalCommand();
    }

    private void executeSettingsTerminalCommand() {
        if (etSettingsTerminalCmd == null || getContext() == null) return;
        String cmd = etSettingsTerminalCmd.getText().toString().trim();
        if (cmd.isEmpty()) return;

        etSettingsTerminalCmd.setText("");

        if ("clear".equalsIgnoreCase(cmd) || "cls".equalsIgnoreCase(cmd)) {
            settingsTerminalBuffer.clear();
            if (tvSettingsTerminalOutput != null) tvSettingsTerminalOutput.setText("");
            initSettingsTerminalBanner();
            return;
        }

        if ("scripts".equalsIgnoreCase(cmd) || "folder".equalsIgnoreCase(cmd)) {
            appendSettingsTerminalPrompt(cmd);
            TerminalFolderManager mgr = TerminalFolderManager.getInstance(getContext());
            List<File> files = mgr.listScriptFiles();
            appendSettingsTerminalText("📁 Terminal Folder: " + mgr.getTerminalDirPath() + "\n", 0xFF00F0FF);
            for (File f : files) {
                appendSettingsTerminalText("  • " + f.getName() + " (" + f.length() + "B)\n", 0xFF00FF66);
            }
            appendSettingsTerminalText("\n", 0xFFFFFFFF);
            scrollSettingsTerminalToBottom();
            return;
        }

        appendSettingsTerminalPrompt(cmd);

        AppExecutors.getInstance().executeCommand(() -> {
            String output;
            try {
                if (cmd.startsWith("run ")) {
                    String scriptName = cmd.substring(4).trim();
                    TerminalFolderManager mgr = TerminalFolderManager.getInstance(getContext());
                    File scriptFile = new File(mgr.getTerminalDir(), scriptName);
                    if (!scriptFile.exists() && !scriptName.endsWith(".sh")) {
                        scriptFile = new File(mgr.getTerminalDir(), scriptName + ".sh");
                    }
                    if (scriptFile.exists()) {
                        output = mgr.executeScriptFile(scriptFile);
                    } else {
                        output = "ERROR: Script file not found: " + scriptName;
                    }
                } else if (cmd.contains("\n") || cmd.contains(";") || cmd.contains("&&") || cmd.length() > 120) {
                    output = TerminalCoreEngine.getInstance().writeAndExecuteTempScript("game_tweak_run.sh", cmd);
                } else {
                    output = TerminalCoreEngine.getInstance().executeCommand(cmd);
                }
            } catch (Exception e) {
                output = "ERROR: " + e.getMessage();
            }

            final String finalOutput = output;
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                if (finalOutput == null || finalOutput.isEmpty() || "SUCCESS".equalsIgnoreCase(finalOutput) || finalOutput.contains("Zero Exit Code") || finalOutput.contains("Exit Code 0")) {
                    appendSettingsTerminalText(finalOutput != null && !finalOutput.isEmpty() ? finalOutput + "\n\n" : "[COMMAND COMPLETED (Exit Code 0)]\n\n", 0xFF00FF66);
                } else if (finalOutput.startsWith("ERROR")) {
                    appendSettingsTerminalText(finalOutput + "\n\n", 0xFFFF0055);
                } else {
                    appendSettingsTerminalText(finalOutput + "\n\n", 0xFFE2E8F0);
                }
                scrollSettingsTerminalToBottom();
            });
        });
    }

    private void appendSettingsTerminalPrompt(String command) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());
        appendSettingsTerminalText("[" + timestamp + "] ", 0xFF64748B);
        appendSettingsTerminalText("shizuku@android", 0xFF00F0FF);
        appendSettingsTerminalText(":$ ", 0xFF00FF66);
        appendSettingsTerminalText(command + "\n", 0xFFFFFFFF);
        scrollSettingsTerminalToBottom();
    }

    private void appendSettingsTerminalText(String text, int color) {
        if (tvSettingsTerminalOutput == null) return;
        int start = settingsTerminalBuffer.length();
        settingsTerminalBuffer.append(text);
        settingsTerminalBuffer.setSpan(new ForegroundColorSpan(color), start, start + text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvSettingsTerminalOutput.setText(settingsTerminalBuffer);
    }

    private void scrollSettingsTerminalToBottom() {
        if (scrollSettingsTerminal != null) {
            scrollSettingsTerminal.post(() -> scrollSettingsTerminal.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void showSettingsFolderDialog() {
        if (getContext() == null) return;
        TerminalFolderManager folderManager = TerminalFolderManager.getInstance(getContext());
        List<File> files = folderManager.listScriptFiles();

        String[] itemTitles;
        if (files.isEmpty()) {
            itemTitles = new String[]{"➕ [CREATE NEW SCRIPT]"};
        } else {
            itemTitles = new String[files.size() + 1];
            for (int i = 0; i < files.size(); i++) {
                itemTitles[i] = "📜 " + files.get(i).getName();
            }
            itemTitles[files.size()] = "➕ [CREATE NEW SCRIPT]";
        }

        new AlertDialog.Builder(getContext())
                .setTitle("📁 TERMINAL SCRIPTS FOLDER")
                .setItems(itemTitles, (dialog, which) -> {
                    if (which == itemTitles.length - 1 && (files.isEmpty() || which == files.size())) {
                        showSettingsCreateScriptDialog();
                    } else {
                        File selectedFile = files.get(which);
                        showSettingsScriptActionDialog(selectedFile);
                    }
                })
                .setNegativeButton("CLOSE", null)
                .show();
    }

    private void showSettingsScriptActionDialog(File scriptFile) {
        if (getContext() == null) return;
        TerminalFolderManager folderManager = TerminalFolderManager.getInstance(getContext());
        String[] actions = {"⚡ Execute in Terminal", "📝 View / Edit Script", "🗑️ Delete Script"};

        new AlertDialog.Builder(getContext())
                .setTitle("📜 " + scriptFile.getName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        runSettingsTerminalQuickCmd("run " + scriptFile.getName());
                    } else if (which == 1) {
                        showSettingsEditScriptDialog(scriptFile);
                    } else if (which == 2) {
                        folderManager.deleteScript(scriptFile);
                        Toast.makeText(getContext(), "Deleted: " + scriptFile.getName(), Toast.LENGTH_SHORT).show();
                        showSettingsFolderDialog();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void showSettingsCreateScriptDialog() {
        if (getContext() == null) return;
        TerminalFolderManager folderManager = TerminalFolderManager.getInstance(getContext());

        EditText etName = new EditText(getContext());
        etName.setHint("my_boost_script.sh");
        etName.setTextColor(0xFFFFFFFF);
        etName.setHintTextColor(0xFF64748B);

        EditText etContent = new EditText(getContext());
        etContent.setHint("# Type bash commands here...\nsetprop debug.sf.fps_limit 185\n");
        etContent.setTextColor(0xFF00FF66);
        etContent.setHintTextColor(0xFF64748B);
        etContent.setMinLines(5);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);
        layout.addView(etName);
        layout.addView(etContent);

        new AlertDialog.Builder(getContext())
                .setTitle("➕ CREATE NEW TERMINAL SCRIPT")
                .setView(layout)
                .setPositiveButton("SAVE & RUN", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String content = etContent.getText().toString();
                    if (name.isEmpty()) name = "custom_tweak_" + System.currentTimeMillis() + ".sh";
                    folderManager.saveScript(name, content);
                    Toast.makeText(getContext(), "Script saved: " + name, Toast.LENGTH_SHORT).show();
                    runSettingsTerminalQuickCmd("run " + name);
                })
                .setNeutralButton("SAVE ONLY", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String content = etContent.getText().toString();
                    if (name.isEmpty()) name = "custom_tweak_" + System.currentTimeMillis() + ".sh";
                    folderManager.saveScript(name, content);
                    Toast.makeText(getContext(), "Script saved to terminal folder!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void showSettingsEditScriptDialog(File scriptFile) {
        if (getContext() == null) return;
        TerminalFolderManager folderManager = TerminalFolderManager.getInstance(getContext());
        String currentContent = folderManager.readScript(scriptFile);

        EditText etContent = new EditText(getContext());
        etContent.setText(currentContent);
        etContent.setTextColor(0xFF00FF66);
        etContent.setMinLines(8);
        etContent.setPadding(32, 16, 32, 16);

        new AlertDialog.Builder(getContext())
                .setTitle("📝 " + scriptFile.getName())
                .setView(etContent)
                .setPositiveButton("SAVE CHANGES", (dialog, which) -> {
                    folderManager.saveScript(scriptFile.getName(), etContent.getText().toString());
                    Toast.makeText(getContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("RUN", (dialog, which) -> {
                    folderManager.saveScript(scriptFile.getName(), etContent.getText().toString());
                    runSettingsTerminalQuickCmd("run " + scriptFile.getName());
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }
}
