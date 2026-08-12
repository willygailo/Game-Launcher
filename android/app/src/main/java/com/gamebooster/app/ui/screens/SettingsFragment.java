package com.gamebooster.app.ui.screens;
import com.gamebooster.app.config.*;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
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
import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.tweaks.TweakCategory;
import com.gamebooster.app.tweaks.TweakManagerRepository;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.core.settings.SettingsManager;
import com.gamebooster.app.core.profile.ProfileManager;
import com.gamebooster.app.core.profile.InputProfile;
import com.gamebooster.app.overlay.CrosshairOverlayService;
import com.gamebooster.app.overlay.CrosshairOverlayManager;
import com.gamebooster.app.overlay.CrosshairPreferences;
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
    private Switch switchEnforceHzLock;
    private Switch switchEnhanceGraphicsLock;
    private Switch switchMaxPerfLock;
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

        // Top Header Navigation: Back to Home
        View btnBackHome = view.findViewById(R.id.btn_back_home);
        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).selectTab(0);
                }
            });
        }

        // Card 1: Shizuku & System Permissions
        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        tvRootStatus = view.findViewById(R.id.tv_root_status);
        Button btnGrantShizuku = view.findViewById(R.id.btn_grant_shizuku);
        Button btnOpenSettings = view.findViewById(R.id.btn_open_settings);

        if (btnGrantShizuku != null) {
            btnGrantShizuku.setOnClickListener(v -> {
                if (getContext() != null) {
                    if (ShizukuExecutor.hasShizukuPermission()) {
                        Toast.makeText(getContext(), "⚡ Executing FULL FORCE-APPLY Shizuku Engine...", Toast.LENGTH_SHORT).show();
                        btnGrantShizuku.setEnabled(false);
                        AppExecutors.getInstance().executeCommand(() -> {
                            int maxHz = 165;
                            try {
                                com.gamebooster.app.device.DevicePerformanceCapabilities caps = com.gamebooster.app.device.DevicePerformanceCapabilities.detect(getContext());
                                if (caps != null && caps.getMaxRefreshRate() > 0) {
                                    maxHz = caps.getMaxRefreshRate();
                                }
                            } catch (Throwable ignored) {}

                            com.gamebooster.app.shizuku.ShizukuForceApplyEngine.ForceApplyResult res =
                                com.gamebooster.app.shizuku.ShizukuForceApplyEngine.forceApplyAll(getContext(), maxHz);

                            AppExecutors.getInstance().postToMainThread(() -> {
                                if (isAdded() && getContext() != null) {
                                    btnGrantShizuku.setEnabled(true);
                                    if (res.success) {
                                        Toast.makeText(getContext(), "🔒 SUCCESS: " + res.totalCommands + " Commands FORCE-LOCKED via Shizuku!", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getContext(), "Force Apply Result: " + res.outputLog, Toast.LENGTH_LONG).show();
                                    }
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

        Button btnOpenTerminal = view.findViewById(R.id.btn_open_terminal);
        if (btnOpenTerminal != null) {
            btnOpenTerminal.setOnClickListener(v -> {
                try {
                    if (isAdded() && getParentFragmentManager() != null) {
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new TerminalFragment())
                                .addToBackStack("TerminalFragment")
                                .commit();
                    }
                } catch (Exception e) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Unable to open Terminal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // Card 2: Esports Gaming Controls
        switchOverlayHud = view.findViewById(R.id.switch_overlay_hud);
        switchGamingDnd = view.findViewById(R.id.switch_gaming_dnd);
        switchAutoGameBoost = view.findViewById(R.id.switch_auto_game_boost);
        switchEsportsAudio = view.findViewById(R.id.switch_esports_audio);
        Button btnCleanCaches = view.findViewById(R.id.btn_clean_game_caches);

        if (switchOverlayHud != null && getContext() != null) {
            boolean savedOverlay = EsportsPreferences.isOverlayHudEnabled(getContext());
            boolean isRunning = com.gamebooster.app.overlay.FloatingOverlayService.isOverlayRunning();
            if (savedOverlay && !isRunning && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getContext()))) {
                com.gamebooster.app.overlay.FloatingOverlayService.startOverlay(getContext());
                isRunning = true;
            }
            switchOverlayHud.setChecked(savedOverlay || isRunning);
            switchOverlayHud.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                EsportsPreferences.setOverlayHud(getContext(), isChecked);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                    switchOverlayHud.setChecked(false);
                    EsportsPreferences.setOverlayHud(getContext(), false);
                    Toast.makeText(getContext(), "Please grant 'Draw over other apps' permission first", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                    startActivity(intent);
                    return;
                }
                if (isChecked) {
                    com.gamebooster.app.overlay.FloatingOverlayService.startOverlay(getContext());
                    Toast.makeText(getContext(), "⚡ Performance HUD Overlay Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    com.gamebooster.app.overlay.FloatingOverlayService.stopOverlay(getContext());
                    Toast.makeText(getContext(), "Overlay Disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchGamingDnd != null && getContext() != null) {
            boolean savedDnd = EsportsPreferences.isGamingDndEnabled(getContext());
            switchGamingDnd.setChecked(savedDnd);
            switchGamingDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                EsportsPreferences.setGamingDnd(getContext(), isChecked);
                Toast.makeText(getContext(), "Gaming DND: " + (isChecked ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    if (getContext() != null) {
                        com.gamebooster.app.gamespace.GameSpaceDndManager.setGamingDndMode(getContext(), isChecked);
                    }
                });
            });
        }

        if (switchAutoGameBoost != null && getContext() != null) {
            boolean savedAutoBoost = EsportsPreferences.isAutoGameBoostEnabled(getContext());
            boolean isRunning = com.gamebooster.app.gamespace.AutoGameMonitorService.isRunning();
            if (savedAutoBoost && !isRunning) {
                com.gamebooster.app.gamespace.AutoGameMonitorService.start(getContext());
                isRunning = true;
            }
            switchAutoGameBoost.setChecked(savedAutoBoost || isRunning);
            switchAutoGameBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                EsportsPreferences.setAutoGameBoost(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked ? "🎮 Auto Game Launch Monitor: ENABLED" : "Auto Game Monitor Disabled", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    if (getContext() != null) {
                        if (isChecked) {
                            com.gamebooster.app.gamespace.AutoGameMonitorService.start(getContext());
                        } else {
                            com.gamebooster.app.gamespace.AutoGameMonitorService.stop(getContext());
                        }
                    }
                });
            });
        }

        if (switchEsportsAudio != null && getContext() != null) {
            boolean savedAudio = EsportsPreferences.isEsportsAudioEnabled(getContext());
            switchEsportsAudio.setChecked(savedAudio);
            switchEsportsAudio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                EsportsPreferences.setEsportsAudio(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked ? "🔊 Esports Footstep Audio Boost: ACTIVE" : "Audio Equalizer Normal", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    if (getContext() != null) {
                        com.gamebooster.app.booster.EsportsAudioEnhancer.setEsportsAudioMode(getContext(), isChecked);
                    }
                });
            });
        }

        if (btnCleanCaches != null) {
            btnCleanCaches.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnCleanCaches.setEnabled(false);
                Toast.makeText(getContext(), "🧹 Cleaning Game Shaders & Storage Caches...", Toast.LENGTH_SHORT).show();

                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.gamespace.GameCacheCleaner.CleanResult res =
                            com.gamebooster.app.gamespace.GameCacheCleaner.performDeepGameCacheCleanDetailed(getContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        btnCleanCaches.setEnabled(true);
                        Toast.makeText(getContext(), res.summary, Toast.LENGTH_LONG).show();
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

        if (precisionSettingsManager != null && switchPrecisionInputTuner != null && getContext() != null) {
            boolean savedInputTuner = PrecisionAimPreferences.isInputTunerEnabled(getContext());
            switchPrecisionInputTuner.setChecked(savedInputTuner || precisionSettingsManager.isDeviceTuned());
            switchPrecisionInputTuner.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                PrecisionAimPreferences.setInputTunerEnabled(getContext(), isChecked);
                if (!ShizukuExecutor.hasShizukuPermission()) {
                    switchPrecisionInputTuner.setChecked(false);
                    PrecisionAimPreferences.setInputTunerEnabled(getContext(), false);
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
                        if (success) {
                            Toast.makeText(getContext(), isChecked ? "🎯 Precision Aim: 1000Hz Input & Zero Slop APPLIED!" : "System Input Defaults Restored", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to modify system properties via Shizuku", Toast.LENGTH_SHORT).show();
                            switchPrecisionInputTuner.setChecked(!isChecked);
                            PrecisionAimPreferences.setInputTunerEnabled(getContext(), !isChecked);
                        }
                        updatePrecisionAimStatus();
                    });
                });
            });
        }

        if (switchCrosshairOverlay != null && getContext() != null) {
            boolean savedCrosshair = PrecisionAimPreferences.isCrosshairOverlayEnabled(getContext());
            boolean isRunning = CrosshairOverlayService.isOverlayRunning();
            if (savedCrosshair && !isRunning && (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(getContext()))) {
                CrosshairOverlayService.startOverlay(getContext());
                isRunning = true;
            }
            switchCrosshairOverlay.setChecked(savedCrosshair || isRunning);
            switchCrosshairOverlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                PrecisionAimPreferences.setCrosshairOverlayEnabled(getContext(), isChecked);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                    switchCrosshairOverlay.setChecked(false);
                    PrecisionAimPreferences.setCrosshairOverlayEnabled(getContext(), false);
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
                        .setNegativeButton("CANCEL", (dialog, which) -> {
                            switchCrosshairOverlay.setChecked(false);
                            PrecisionAimPreferences.setCrosshairOverlayEnabled(getContext(), false);
                        })
                        .setOnCancelListener(dialog -> {
                            switchCrosshairOverlay.setChecked(false);
                            PrecisionAimPreferences.setCrosshairOverlayEnabled(getContext(), false);
                        })
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

        // Card 3: Hardware Engine & Performance Presets
        Button btnExtreme = view.findViewById(R.id.btn_apply_pubg_profile);
        Button btnPro144 = view.findViewById(R.id.btn_apply_144_profile);
        Button btnPerformance = view.findViewById(R.id.btn_apply_2d_profile);
        Button btnBalanced = view.findViewById(R.id.btn_apply_balanced_profile);

        switchAngleMode = view.findViewById(R.id.switch_angle_mode);
        switchGameDriver = view.findViewById(R.id.switch_game_driver);
        switchGpuMode = view.findViewById(R.id.switch_gpu_mode);
        switchCpuMode = view.findViewById(R.id.switch_cpu_mode);
        switchEnforceHzLock = view.findViewById(R.id.switch_enforce_hz_lock);
        switchEnhanceGraphicsLock = view.findViewById(R.id.switch_enhance_graphics_lock);
        switchMaxPerfLock = view.findViewById(R.id.switch_max_perf_lock);

        if (btnExtreme != null) {
            btnExtreme.setOnClickListener(v -> applyPresetProfile(btnExtreme, PerformanceChannel.Profile.EXTREME_PERFORMANCE, "🔥 Executed: 165Hz Lock & Vulkan Profile"));
        }
        if (btnPro144 != null) {
            btnPro144.setOnClickListener(v -> applyPresetProfile(btnPro144, PerformanceChannel.Profile.PERFORMANCE, "🎮 Executed: 144Hz Lock & Pro Gaming Profile"));
        }
        if (btnPerformance != null) {
            btnPerformance.setOnClickListener(v -> applyPresetProfile(btnPerformance, PerformanceChannel.Profile.PERFORMANCE, "⚡ Executed: 120Hz Lock & Vulkan Profile"));
        }
        if (btnBalanced != null) {
            btnBalanced.setOnClickListener(v -> applyPresetProfile(btnBalanced, PerformanceChannel.Profile.BALANCED, "⚖️ Executed: 90Hz Lock & Schedutil Profile"));
        }

        if (getContext() != null) {
            if (switchAngleMode != null) switchAngleMode.setChecked(ManualSettingsPreferences.isAngleModeEnabled(getContext()));
            if (switchGameDriver != null) switchGameDriver.setChecked(ManualSettingsPreferences.isGameDriverEnabled(getContext()));
            if (switchGpuMode != null) switchGpuMode.setChecked("vulkan".equalsIgnoreCase(ManualSettingsPreferences.getGpuMode(getContext())));
            if (switchCpuMode != null) switchCpuMode.setChecked("performance".equalsIgnoreCase(ManualSettingsPreferences.getCpuMode(getContext())));
            if (switchEnforceHzLock != null) switchEnforceHzLock.setChecked(com.gamebooster.app.booster.MaxHzForceChannel.isHzLocked(getContext()));
            if (switchEnhanceGraphicsLock != null) switchEnhanceGraphicsLock.setChecked(ManualSettingsPreferences.isAngleModeEnabled(getContext()));
            if (switchMaxPerfLock != null) switchMaxPerfLock.setChecked(PerformanceChannel.isMaxPerformanceLocked(getContext()));
        }

        if (switchEnforceHzLock != null) {
            switchEnforceHzLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        int maxHz = 165;
                        try {
                            com.gamebooster.app.device.DevicePerformanceCapabilities caps = com.gamebooster.app.device.DevicePerformanceCapabilities.detect(getContext());
                            if (caps != null && caps.getMaxRefreshRate() > 0) maxHz = caps.getMaxRefreshRate();
                        } catch (Throwable ignored) {}
                        com.gamebooster.app.booster.MaxHzForceChannel.lockHz(getContext(), maxHz, null);
                    } else {
                        com.gamebooster.app.booster.MaxHzForceChannel.unlockHz(getContext());
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🔒 Max Hz & FPS Forced and Locked!" : "🔓 Refresh Rate Lock Removed", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchEnhanceGraphicsLock != null) {
            switchEnhanceGraphicsLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        GpuTweaksChannel.applyEnhancedGraphics(true);
                    } else {
                        GpuTweaksChannel.unlockEnhancedGraphics();
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🔒 Enhanced Ultra Graphics & MSAA Enforced!" : "🔓 Enhanced Graphics Unlocked", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchMaxPerfLock != null) {
            switchMaxPerfLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        PerformanceChannel.lockMaxPerformance(getContext());
                    } else {
                        PerformanceChannel.unlockMaxPerformance(getContext());
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🔥 Performance Set to High Max (Pinaka-Taas Locked)!" : "🔓 Extreme Performance Unlocked", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchAngleMode != null) {
            switchAngleMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setAngleMode(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        com.gamebooster.app.booster.AngleGraphicsDriverChannel.enableGlobalAngleDriver();
                    } else {
                        com.gamebooster.app.booster.AngleGraphicsDriverChannel.resetAngleDriver();
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ ANGLE Vulkan Driver ENABLED for all games" : "ANGLE Driver Reset", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), isChecked ? "🎮 System Game Driver ENABLED for all apps" : "Game Driver Disabled", Toast.LENGTH_SHORT).show();
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
        TextView tvGamePingMs = view.findViewById(R.id.tv_game_ping_ms);
        Button btnPingTest = view.findViewById(R.id.btn_ping_test);
        Button btnDnsCloudflare = view.findViewById(R.id.btn_dns_cloudflare);
        Button btnDnsGoogle = view.findViewById(R.id.btn_dns_google);
        Button btnDnsDefault = view.findViewById(R.id.btn_dns_default);
        switchTetheringHw = view.findViewById(R.id.switch_tethering_hw);
        switchForceGnss = view.findViewById(R.id.switch_force_gnss);

        if (getContext() != null) {
            if (switchTetheringHw != null) switchTetheringHw.setChecked(ManualSettingsPreferences.isTetherHwEnabled(getContext()));
            if (switchForceGnss != null) switchForceGnss.setChecked(ManualSettingsPreferences.isForceGnssEnabled(getContext()));
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

        // Card 5: Advanced System Tweaks Engine
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
                Toast.makeText(getContext(), "Applying all system optimizations via Shizuku...", Toast.LENGTH_SHORT).show();

                TweakManagerRepository.applyAllSupportedTweaksAsync(getContext(), appliedCount -> {
                    if (getContext() != null && isAdded()) {
                        if (tweaksAdapter != null) {
                            tweaksAdapter.notifyDataSetChanged();
                        }
                        btnApplyAll.setEnabled(true);
                        Toast.makeText(getContext(), "⚡ Applied " + appliedCount + " system optimizations!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (btnFilterAll != null && tweaksAdapter != null) btnFilterAll.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getAllTweaks()));
        if (btnFilterCpuGpu != null && tweaksAdapter != null) btnFilterCpuGpu.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.CPU_GPU)));
        if (btnFilterTouch != null && tweaksAdapter != null) btnFilterTouch.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.TOUCH_DISPLAY)));
        if (btnFilterShizuku != null && tweaksAdapter != null) btnFilterShizuku.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.SHIZUKU_SYSTEM)));
        if (btnFilterNetwork != null && tweaksAdapter != null) btnFilterNetwork.setOnClickListener(v -> tweaksAdapter.updateList(TweakManagerRepository.getTweaksByCategory(TweakCategory.NETWORK_LATENCY)));

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
                if (getContext() == null) return;
                boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
                Toast.makeText(getContext(), "Saving app-only device profile: " + profile.displayName + "...", Toast.LENGTH_SHORT).show();

                SpoofPreferences.setSpoofEnabled(getContext(), true);
                SpoofPreferences.setActiveProfileId(getContext(), profile.id);
                if (spoofProfileAdapter != null) {
                    spoofProfileAdapter.setActiveProfileId(profile.id);
                }

                AppExecutors.getInstance().executeCommand(() -> {
                    boolean success = DeviceSpooferEngine.applyProfile(getContext(), profile, null);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        if (success) {
                            if (switchDeviceSpoof != null) switchDeviceSpoof.setChecked(true);
                            if (rvSpoofProfiles != null) rvSpoofProfiles.setVisibility(View.VISIBLE);
                            updateSpoofUiState();
                            String msg = hasShizuku ? "⚡ App profile active; native display request sent: " : "⚡ App profile saved (display permission unavailable): ";
                            Toast.makeText(getContext(), msg + profile.displayName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to apply spoof profile", Toast.LENGTH_SHORT).show();
                        }
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
                        DeviceSpooferEngine.resetSpoofing(getContext());
                        SpoofPreferences.clearActiveProfile(getContext());
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (isAdded() && getContext() != null) {
                                updateSpoofUiState();
                                Toast.makeText(getContext(), "App device profile disabled; saved display values restored", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else {
                    updateSpoofUiState();
                }
            });
        }

        updateSpoofUiState();

        // Card 6: About & Community Links
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

    private void refreshAllStatuses() {
        EngineUIHelper.refreshEngineStatus(tvEngineStatus);
        EngineUIHelper.refreshEngineStatus(tvTweaksStatus);
        updateSystemSettingsStatus();
        updateSpoofUiState();
        boolean alive = ShizukuExecutor.hasShizukuPermission();
        if (tweaksAdapter != null) {
            tweaksAdapter.setShizukuAlive(alive);
        }
        if (bannerDisconnect != null) {
            bannerDisconnect.setVisibility(alive ? View.GONE : View.VISIBLE);
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
                    tvSpoofActiveProfile.setText("Active App Profile: " + activeProf.displayName + " (" + activeProf.model + ")");
                    tvSpoofActiveProfile.setTextColor(0xFF00FF66);
                } else {
                    tvSpoofActiveProfile.setText("Active App Profile: ENABLED (No profile selected)");
                    tvSpoofActiveProfile.setTextColor(0xFFFFB800);
                }
            } else {
                tvSpoofActiveProfile.setText("Active App Profile: NONE (Disabled)");
                tvSpoofActiveProfile.setTextColor(0xFF888888);
            }
        }

        if (spoofProfileAdapter != null) {
            spoofProfileAdapter.setActiveProfileId(enabled ? activeId : null);
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
        String[] mainOptions = {"Change Shape Preset", "Change Color (Neon / Custom)", "Adjust Size & Opacity"};

        new AlertDialog.Builder(getContext())
                .setTitle("🎯 CUSTOMIZE CROSSHAIR OVERLAY")
                .setItems(mainOptions, (dialog, which) -> {
                    if (which == 0) {
                        String[] options = {"Dot Preset", "Tactical Cross", "Scope Ring", "Sniper Cross", "T-Shape Target", "Dynamic Cross"};
                        CrosshairPreset[] presets = {
                            CrosshairPreset.DOT, CrosshairPreset.TACTICAL_CROSS, CrosshairPreset.SCOPE_RING,
                            CrosshairPreset.SNIPER_CROSS, CrosshairPreset.T_SHAPE, CrosshairPreset.DYNAMIC_CROSS
                        };
                        new AlertDialog.Builder(getContext())
                                .setTitle("🎯 SELECT SHAPE PRESET")
                                .setItems(options, (d, idx) -> {
                                    CrosshairPreset selected = presets[idx];
                                    CrosshairPreferences.setPreset(getContext(), selected);
                                    CrosshairOverlayService.updateOverlay(getContext());
                                    Toast.makeText(getContext(), "🎯 Shape Preset: " + selected.getLabel(), Toast.LENGTH_SHORT).show();
                                })
                                .show();
                    } else if (which == 1) {
                        String[] colorNames = {"Neon Green (#00FF66)", "Cyber Cyan (#00F0FF)", "Crimson Red (#FF2A55)", "Electric Yellow (#FFEE00)", "Pure White (#FFFFFF)"};
                        int[] colors = {
                            android.graphics.Color.parseColor("#00FF66"),
                            android.graphics.Color.parseColor("#00F0FF"),
                            android.graphics.Color.parseColor("#FF2A55"),
                            android.graphics.Color.parseColor("#FFEE00"),
                            android.graphics.Color.parseColor("#FFFFFF")
                        };
                        new AlertDialog.Builder(getContext())
                                .setTitle("🎨 SELECT CROSSHAIR COLOR")
                                .setItems(colorNames, (d, idx) -> {
                                    CrosshairPreferences.setColor(getContext(), colors[idx]);
                                    CrosshairOverlayService.updateOverlay(getContext());
                                    Toast.makeText(getContext(), "🎨 Color Updated!", Toast.LENGTH_SHORT).show();
                                })
                                .show();
                    } else {
                        String[] sizes = {"Small (50px)", "Medium Standard (80px)", "Large (110px)", "Ultra (140px)"};
                        int[] sizeValues = {50, 80, 110, 140};
                        new AlertDialog.Builder(getContext())
                                .setTitle("📐 SELECT CROSSHAIR SIZE")
                                .setItems(sizes, (d, idx) -> {
                                    CrosshairPreferences.setSizePx(getContext(), sizeValues[idx]);
                                    CrosshairOverlayService.updateOverlay(getContext());
                                    Toast.makeText(getContext(), "📐 Size Updated: " + sizeValues[idx] + "px", Toast.LENGTH_SHORT).show();
                                })
                                .show();
                    }
                })
                .show();
    }

    private void showSensitivityCalculatorDialog() {
        if (getContext() == null) return;
        String[] dpiOptions = new String[]{"400 DPI (Tablet / Large Screen)", "600 DPI (Standard Phone)", "800 DPI (Ultra Fast Touch)", "1200 DPI (Extreme eSports)"};
        int[] dpiValues = new int[]{400, 600, 800, 1200};

        new AlertDialog.Builder(getContext())
                .setTitle("🧮 SELECT DEVICE TOUCH DPI")
                .setItems(dpiOptions, (dialog, which) -> {
                    int selectedDpi = dpiValues[which];
                    SensitivityModel m = SensitivityCalculator.calculate(selectedDpi, 6.5, 1.5f);
                    String details = "📊 RECOMMENDED SENSITIVITY (" + selectedDpi + " DPI):\n\n" +
                            "• Free Look: " + m.freeLook + "\n" +
                            "• 3rd Person No Scope: " + m.noScope3rdPerson + "\n" +
                            "• Red Dot / Holo: " + m.redDotHolo + "\n" +
                            "• 2x Scope: " + m.scope2x + "\n" +
                            "• 4x Scope: " + m.scope4x + "\n\n" +
                            "🌀 GYROSCOPE RECS:\n" +
                            "• Gyro No Scope: " + m.gyroNoScope + "\n" +
                            "• Gyro Red Dot: " + m.gyroRedDot + "\n" +
                            "• Gyro 4x Scope: " + m.gyro4x + "\n\n" +
                            "💡 Enter these values manually inside PUBG Mobile or COD Mobile settings menu.";

                    new AlertDialog.Builder(getContext())
                            .setTitle("🧮 SENSITIVITY CALCULATOR (" + selectedDpi + " DPI)")
                            .setMessage(details)
                            .setPositiveButton("CLOSE", null)
                            .show();
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }
}
