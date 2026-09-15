package com.gamebooster.app.ui.fragments;

import com.gamebooster.app.ui.adapters.SpoofProfileAdapter;

import com.gamebooster.app.config.*;
import com.gamebooster.app.engine.CommandExecutor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import com.gamebooster.app.shizuku.ShizukuManager;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
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
import com.gamebooster.app.terminal.AnsiColorParser;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import androidx.core.widget.NestedScrollView;
import android.view.inputmethod.EditorInfo;
import com.gamebooster.app.ui.dialogs.CyberActionDialog;
import com.gamebooster.app.spoofer.SpoofProfileRegistry;
import com.gamebooster.app.ui.dialogs.SpoofBrandSelectorDialog;
import com.gamebooster.app.ui.views.LoopingVideoBackgroundView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment implements ShizukuManager.ShizukuStateListener {

    private LoopingVideoBackgroundView videoSettingsBg;

    // Pure Cyber Terminal UI in Settings
    private TextView tvSettingsTerminalUid;
    private TextView tvSettingsTerminalFolderPath;
    private TextView tvSettingsTerminalOutput;
    private NestedScrollView scrollSettingsTerminal;
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
    private Switch switchGameDriver;
    private Switch switchGpuMode;
    private Switch switchCpuMode;
    private Switch switchThermalBypass;
    private Switch switchAdpfEngine;
    private Switch switchUclampBoost;
    private Switch switchTouch1000hzLock;
    private Switch switchPhantomFreezerKill;
    private Switch switchVulkanSkiavk;
    private Switch switchMemory16kbShield;
    private Switch switchWebviewBoost;
    private Switch switchTetheringHw;
    private Switch switchForceGnss;
    private Switch switch5g6gData;
    private Switch switchWifiLowLatency;
    private Switch switchDualDataWifi;
    private Switch switchOverlayHud;
    private Switch switchGamingDnd;
    private Switch switchAutoGameBoost;
    private Switch switchEsportsAudio;
    private Switch switchSpeakerBypassBoost;
    private Switch switchAntiLog;
    private Switch switchAutoPurgeLogs;
    private Switch switchArtSpeedCompile;

    // Network Settings UI (Pure Manual ON/OFF Switches - Android 13 to 16)
    private TextView tvLiveNetworkTelemetry;
    private Switch switchTcpBbrBuffers;
    private Switch switchDnsCloudflare;
    private Switch switchDnsGoogle;
    private Switch switchPhTelcoSupercharger;
    private Switch switchAutoDnsFlush;

    // Card 4.5: System & Kernel Tweaks UI
    private TextView tvSettingsTweaksBadgeCount;
    private EditText etSettingsTweaksSearch;
    private RecyclerView rvSettingsTweaks;
    private com.gamebooster.app.ui.adapters.TweaksAdapter settingsTweaksAdapter;
    private Button btnTweakFilterAll;
    private Button btnTweakFilterCpuGpu;
    private Button btnTweakFilterTouch;
    private Button btnTweakFilterShizuku;
    private Button btnTweakFilterNetwork;
    private Button btnSettingsTweaksApplyAll;
    private Button btnSettingsTweaksResetAll;
    private com.gamebooster.app.tweaks.TweakCategory currentTweakCategory = com.gamebooster.app.tweaks.TweakCategory.ALL;

    // Device Spoofing UI
    private Switch switchDeviceSpoof;
    private TextView tvSpoofActiveProfile;
    private TextView tvSettingsSpoofBrandInfo;
    private View hsvSettingsSpoofBrands;
    private RecyclerView rvSpoofProfiles;
    private SpoofProfileAdapter spoofProfileAdapter;

    // Diagnostics UI
    private TextView tvDiagStatus;
    private TextView tvDiagBadgeShizuku;
    private TextView tvDiagBadgeAidl;
    private TextView tvDiagBadgeNet;
    private TextView tvDiagBadgeThermal;
    private TextView tvDiagLiveIndicator;

    // Precision Aim Controls
    private Switch switchPrecisionInputTuner;
    private Switch switchCrosshairOverlay;
    private TextView tvPrecisionAimStatus;
    private Button btnCrosshairPreset;
    private Button btnSensitivityCalculator;

    // ─── 2026.2 Combat Enhancement Suite UI ────────────────────────────────────
    private Switch switchAdaptiveAimAssist;
    private Switch switchAdaptiveNoRecoil;
    private Switch switchRankedCombatSuite;
    private Switch switchAimLock;
    private Switch switchDamageOverdrive;
    private Switch switchFastReload;
    private Switch switchFastRun;
    private Switch switchFastCooldown;
    private Switch switchAutoReInject;

    private SettingsManager precisionSettingsManager;
    private ProfileManager precisionProfileManager;

    private boolean isProgrammaticToggle = false;

    private boolean isPrivilegedExecutionAvailable() {
        boolean active = com.gamebooster.app.engine.PrivilegeBridgeEngine.isPrivilegedActive();
        if (!active && getContext() != null) {
            com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().forceReconnectCheck();
            active = com.gamebooster.app.engine.PrivilegeBridgeEngine.isPrivilegedActive();
        }
        return active;
    }

    private boolean checkShizukuOrRevert(android.widget.CompoundButton button, String featureName) {
        if (isProgrammaticToggle) return true;
        if (!isPrivilegedExecutionAvailable()) {
            if (getContext() != null) {
                isProgrammaticToggle = true;
                if (button != null) button.setChecked(false);
                isProgrammaticToggle = false;
                ShizukuManager.showShizukuPermissionDialog(getContext(), featureName);
            }
            return false;
        }
        return true;
    }

    private boolean requireShizukuForAction(String featureName) {
        if (!isPrivilegedExecutionAvailable()) {
            if (getContext() != null) {
                ShizukuManager.showShizukuPermissionDialog(getContext(), featureName);
            }
            return false;
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        if (getContext() != null) {
            precisionSettingsManager = new SettingsManager(getContext());
            precisionProfileManager = new ProfileManager(getContext());
        }

        // Settings Background Looping Video
        videoSettingsBg = view.findViewById(R.id.video_settings_bg);
        if (videoSettingsBg != null) {
            videoSettingsBg.setMuted(true);
            videoSettingsBg.setVideoRawResource(R.raw.settings_bg_video);
        }

        // Card 1b: Diagnostics — shareable crash + settings snapshot
        if (getContext() != null) {
            com.gamebooster.app.diagnostics.CrashLog.install(getContext().getApplicationContext());
        }
        tvDiagStatus = view.findViewById(R.id.tv_diag_status);
        tvDiagBadgeShizuku = view.findViewById(R.id.tv_diag_badge_shizuku);
        tvDiagBadgeAidl = view.findViewById(R.id.tv_diag_badge_aidl);
        tvDiagBadgeNet = view.findViewById(R.id.tv_diag_badge_net);
        tvDiagBadgeThermal = view.findViewById(R.id.tv_diag_badge_thermal);
        tvDiagLiveIndicator = view.findViewById(R.id.tv_diag_live_indicator);

        Button btnDiagRefresh = view.findViewById(R.id.btn_diag_refresh);
        Button btnDiagExport = view.findViewById(R.id.btn_diag_export);
        Button btnDiagClear = view.findViewById(R.id.btn_diag_clear);
        if (btnDiagRefresh != null) {
            btnDiagRefresh.setOnClickListener(v -> {
                if (tvDiagStatus != null) {
                    tvDiagStatus.setText("⚡ Rescanning all hardware bridges, network & system connections...");
                }
                renderDiagnostics();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "🔄 Refreshing system diagnostics...", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (btnDiagExport != null) {
            btnDiagExport.setOnClickListener(v -> exportDiagnostics());
        }
        if (btnDiagClear != null) {
            btnDiagClear.setOnClickListener(v -> {
                if (getContext() != null) {
                    boolean cleared = com.gamebooster.app.diagnostics.DiagnosticsExporter.clearAllDiagnosticsData(getContext());
                    Toast.makeText(getContext(), cleared ? "🧹 All crash logs & diagnostic cache cleared!" : "🧹 Diagnostic logs reset", Toast.LENGTH_SHORT).show();
                    renderDiagnostics();
                }
            });
        }

        // Auto-Open: Immediately start live diagnostics scan on view creation (Zero Wait)
        renderDiagnostics();

        // Card 2: Esports Gaming Controls
        switchOverlayHud = view.findViewById(R.id.switch_overlay_hud);
        switchGamingDnd = view.findViewById(R.id.switch_gaming_dnd);
        switchAutoGameBoost = view.findViewById(R.id.switch_auto_game_boost);
        switchEsportsAudio = view.findViewById(R.id.switch_esports_audio);
        switchSpeakerBypassBoost = view.findViewById(R.id.switch_speaker_bypass_boost);

        if (switchOverlayHud != null) {
            isProgrammaticToggle = true;
            switchOverlayHud.setChecked(com.gamebooster.app.overlay.FloatingOverlayService.isOverlayRunning());
            isProgrammaticToggle = false;
            switchOverlayHud.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Performance HUD Overlay")) return;
                if (!Settings.canDrawOverlays(getContext())) {
                    isProgrammaticToggle = true;
                    switchOverlayHud.setChecked(false);
                    isProgrammaticToggle = false;
                    Toast.makeText(getContext(), "Please grant 'Draw over other apps' permission first", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                    startActivity(intent);
                    return;
                }
                try {
                    if (isChecked) {
                        com.gamebooster.app.overlay.FloatingOverlayService.startOverlay(getContext());
                        Toast.makeText(getContext(), "⚡ Performance HUD Overlay Enabled", Toast.LENGTH_SHORT).show();
                    } else {
                        com.gamebooster.app.overlay.FloatingOverlayService.stopOverlay(getContext());
                        Toast.makeText(getContext(), "Performance HUD Overlay Disabled", Toast.LENGTH_SHORT).show();
                    }
                } catch (Throwable t) {
                    android.util.Log.e("SettingsFragment", "Overlay service toggle error", t);
                    Toast.makeText(getContext(), "Overlay note: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchGamingDnd != null) {
            isProgrammaticToggle = true;
            switchGamingDnd.setChecked(com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getContext()));
            isProgrammaticToggle = false;
            switchGamingDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Gaming DND & Call Suppressor")) return;
                com.gamebooster.app.gamespace.GameSpaceDndManager.setGamingDndMode(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked ? "🔕 Gaming DND & Call Suppressor Enabled" : "Gaming DND Disabled", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchAutoGameBoost != null) {
            isProgrammaticToggle = true;
            switchAutoGameBoost.setChecked(com.gamebooster.app.gamespace.AutoGameMonitorService.isRunning());
            isProgrammaticToggle = false;
            switchAutoGameBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Auto Game Launch Monitor")) return;
                if (isChecked) {
                    com.gamebooster.app.gamespace.AutoGameMonitorService.start(getContext());
                    Toast.makeText(getContext(), "🚀 Auto Game Launch Monitor Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    com.gamebooster.app.gamespace.AutoGameMonitorService.stop(getContext());
                    Toast.makeText(getContext(), "Auto Game Launch Monitor Disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchEsportsAudio != null) {
            isProgrammaticToggle = true;
            switchEsportsAudio.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isEnabled(getContext()));
            isProgrammaticToggle = false;
            switchEsportsAudio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                com.gamebooster.app.booster.EsportsAudioEnhancer.setEsportsAudioMode(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked ? "🎧 Esports Footstep Audio Boost Enabled" : "Esports Audio Disabled", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchSpeakerBypassBoost != null) {
            isProgrammaticToggle = true;
            switchSpeakerBypassBoost.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isSpeakerBypassEnabled(getContext()));
            isProgrammaticToggle = false;
            switchSpeakerBypassBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Speaker Audio Limiter Bypass")) return;
                com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.EsportsAudioEnhancer.setSpeakerBypassMode(getContext(), isChecked);
                    com.gamebooster.app.core.AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🔊 Speaker Audio Limiter Bypassed & Hardware Boost (+15dB) Enabled" : "Speaker Boost Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        switchAntiLog = view.findViewById(R.id.switch_anti_log);
        switchAutoPurgeLogs = view.findViewById(R.id.switch_auto_purge_logs);
        switchArtSpeedCompile = view.findViewById(R.id.switch_art_speed_compile);

        if (switchAntiLog != null) {
            if (getContext() != null) {
                isProgrammaticToggle = true;
                switchAntiLog.setChecked(ManualSettingsPreferences.isAntiLogEnabled(getContext()));
                isProgrammaticToggle = false;
            }
            switchAntiLog.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Anti-Log & Telemetry Blocker")) return;
                ManualSettingsPreferences.setAntiLogEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        AntiLogPatcher.applySystemAntiLog();
                        AntiLogPatcher.purgeAllGameLogs();
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🛡️ Anti-Log & Telemetry Blocker Enabled" : "Anti-Log Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchAutoPurgeLogs != null) {
            if (getContext() != null) {
                isProgrammaticToggle = true;
                switchAutoPurgeLogs.setChecked(true);
                isProgrammaticToggle = false;
            }
            switchAutoPurgeLogs.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Auto Purge Game Logs")) return;
                if (isChecked) {
                    AppExecutors.getInstance().executeCommand(() -> {
                        int count = AntiLogPatcher.purgeAllGameLogs();
                        AntiLogPatcher.applySystemAntiLog();
                        CommandExecutor.executeSystemCommand("logcat -b all -c 2>/dev/null; dumpsys dropbox --clean 2>/dev/null; rm -rf /data/tombstones/* 2>/dev/null; rm -rf /data/anr/* 2>/dev/null; rm -rf /sdcard/Android/data/*/files/tlog/* 2>/dev/null; rm -rf /sdcard/Android/data/*/files/ano_tmp/* 2>/dev/null; rm -rf /sdcard/Android/data/*/files/tp_log/* 2>/dev/null");
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (isAdded() && getContext() != null) {
                                Toast.makeText(getContext(), "🧹 Game Logs & Telemetry Purged (" + count + " packages)", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            });
        }

        if (switchArtSpeedCompile != null) {
            if (getContext() != null) {
                isProgrammaticToggle = true;
                switchArtSpeedCompile.setChecked(ManualSettingsPreferences.isAotSpeedEnabled(getContext()));
                isProgrammaticToggle = false;
            }
            switchArtSpeedCompile.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "ART Speed Compilation")) return;
                ManualSettingsPreferences.setAotSpeedEnabled(getContext(), isChecked);
                if (isChecked) {
                    Toast.makeText(getContext(), "⚡ ART Speed-Compile Triggered via Shizuku...", Toast.LENGTH_SHORT).show();
                    com.gamebooster.app.engine.ArtCompilerEngine.compileAllInstalledGamesAsync(
                            getContext(),
                            com.gamebooster.app.engine.ArtCompilerEngine.CompileFilter.SPEED,
                            new com.gamebooster.app.engine.ArtCompilerEngine.BatchCompileCallback() {
                                @Override
                                public void onGameStarted(String packageName, String gameLabel, int currentIndex, int totalGames) {}

                                @Override
                                public void onGameFinished(String packageName, boolean success) {}

                                @Override
                                public void onAllFinished(int successCount, int totalGames) {
                                    if (isAdded() && getContext() != null) {
                                        Toast.makeText(getContext(), "⚡ ART Compilation Complete: " + successCount + "/" + totalGames + " games compiled!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                    );
                }
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
                if (getContext() == null || isProgrammaticToggle) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Precision 1000Hz Input Tuner")) return;
                handlePrecisionTunerToggle(isChecked);
            });
        }

        if (switchCrosshairOverlay != null) {
            switchCrosshairOverlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null || isProgrammaticToggle) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Target Crosshair Overlay")) return;
                if (!Settings.canDrawOverlays(getContext())) {
                    isProgrammaticToggle = true;
                    switchCrosshairOverlay.setChecked(false);
                    isProgrammaticToggle = false;
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
                            isProgrammaticToggle = true;
                            switchCrosshairOverlay.setChecked(false);
                            isProgrammaticToggle = false;
                        })
                        .setOnCancelListener(dialog -> {
                            isProgrammaticToggle = true;
                            switchCrosshairOverlay.setChecked(false);
                            isProgrammaticToggle = false;
                        })
                        .show();
                } else {
                    CrosshairOverlayService.stopOverlay(getContext());
                    Toast.makeText(getContext(), "Crosshair Overlay Disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnCrosshairPreset != null) {
            btnCrosshairPreset.setOnClickListener(v -> {
                if (!requireShizukuForAction("Target Crosshair Preset")) return;
                showCrosshairPresetDialog();
            });
        }

        if (btnSensitivityCalculator != null) {
            btnSensitivityCalculator.setOnClickListener(v -> {
                showSensitivityCalculatorDialog();
            });
        }

        Button btnGyroCalibrator = view.findViewById(R.id.btn_gyro_calibrator);
        if (btnGyroCalibrator != null) {
            btnGyroCalibrator.setOnClickListener(v -> {
                if (getContext() != null) {
                    com.gamebooster.app.ui.dialogs.GyroCalibratorDialog.show(getContext());
                }
            });
        }

        updatePrecisionAimStatus();

        Button btn185 = view.findViewById(R.id.btn_apply_185_profile);
        Button btnExtreme = view.findViewById(R.id.btn_apply_pubg_profile);
        Button btnPro144 = view.findViewById(R.id.btn_apply_144_profile);
        Button btnPerformance = view.findViewById(R.id.btn_apply_2d_profile);

        switchGameDriver = view.findViewById(R.id.switch_game_driver);
        switchGpuMode = view.findViewById(R.id.switch_gpu_mode);
        switchCpuMode = view.findViewById(R.id.switch_cpu_mode);
        switchThermalBypass = view.findViewById(R.id.switch_thermal_bypass);
        switchAdpfEngine = view.findViewById(R.id.switch_adpf_engine);

        switchUclampBoost = view.findViewById(R.id.switch_uclamp_boost);
        switchTouch1000hzLock = view.findViewById(R.id.switch_touch_1000hz_lock);
        switchPhantomFreezerKill = view.findViewById(R.id.switch_phantom_freezer_kill);
        switchVulkanSkiavk = view.findViewById(R.id.switch_vulkan_skiavk);
        switchMemory16kbShield = view.findViewById(R.id.switch_memory_16kb_shield);
        switchWebviewBoost = view.findViewById(R.id.switch_webview_boost);

        // Always ensure ANGLE driver is permanently purged from Android Settings
        AppExecutors.getInstance().executeCommand(GpuTweaksChannel::purgeAngleDriver);

        if (btn185 != null) {
            btn185.setOnClickListener(v -> applyPresetProfile(btn185, PerformanceChannel.Profile.EXTREME_PERFORMANCE, 185, "⚡ Executed: 185Hz / 185 FPS Ultra-Extreme Profile"));
        }
        if (btnExtreme != null) {
            btnExtreme.setOnClickListener(v -> applyPresetProfile(btnExtreme, PerformanceChannel.Profile.EXTREME_PERFORMANCE, 165, "🔥 Executed: 165Hz Lock & eSports Max Profile"));
        }
        if (btnPro144 != null) {
            btnPro144.setOnClickListener(v -> applyPresetProfile(btnPro144, PerformanceChannel.Profile.PERFORMANCE, 144, "🎮 Executed: 144Hz Lock & Pro Gaming Profile"));
        }
        if (btnPerformance != null) {
            btnPerformance.setOnClickListener(v -> applyPresetProfile(btnPerformance, PerformanceChannel.Profile.PERFORMANCE, 120, "⚡ Executed: 120Hz Lock & High Gaming Profile"));
        }

        if (getContext() != null) {
            isProgrammaticToggle = true;
            if (switchGameDriver != null) switchGameDriver.setChecked(ManualSettingsPreferences.isGameDriverEnabled(getContext()));
            if (switchGpuMode != null) switchGpuMode.setChecked("vulkan".equalsIgnoreCase(ManualSettingsPreferences.getGpuMode(getContext())));
            if (switchCpuMode != null) switchCpuMode.setChecked("performance".equalsIgnoreCase(ManualSettingsPreferences.getCpuMode(getContext())));
            if (switchThermalBypass != null) switchThermalBypass.setChecked(ManualSettingsPreferences.isThermalBypassEnabled(getContext()));
            if (switchAdpfEngine != null) switchAdpfEngine.setChecked(ManualSettingsPreferences.isAdpfEngineEnabled(getContext()));

            if (switchUclampBoost != null) switchUclampBoost.setChecked(ManualSettingsPreferences.isUclampBoostEnabled(getContext()));
            if (switchTouch1000hzLock != null) switchTouch1000hzLock.setChecked(ManualSettingsPreferences.isTouch1000HzLockEnabled(getContext()));
            if (switchPhantomFreezerKill != null) switchPhantomFreezerKill.setChecked(ManualSettingsPreferences.isPhantomFreezerKillEnabled(getContext()));
            if (switchVulkanSkiavk != null) switchVulkanSkiavk.setChecked(ManualSettingsPreferences.isVulkanSkiaVkEnabled(getContext()));
            if (switchMemory16kbShield != null) switchMemory16kbShield.setChecked(ManualSettingsPreferences.isMemory16kbShieldEnabled(getContext()));
            if (switchWebviewBoost != null) switchWebviewBoost.setChecked(ManualSettingsPreferences.isWebViewBoostEnabled(getContext()));
            isProgrammaticToggle = false;
        }

        if (switchGameDriver != null) {
            switchGameDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "System Game Graphics Driver")) return;
                ManualSettingsPreferences.setGameDriverEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    GpuTweaksChannel.setGameDriverMode(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ System Game Driver Applied (MLBB, CODM, PUBGM)" : "Game Driver Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchGpuMode != null) {
            switchGpuMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Vulkan 3D HWUI & Skia Pipeline")) return;
                ManualSettingsPreferences.setGpuMode(getContext(), isChecked ? "vulkan" : "skia");
                AppExecutors.getInstance().executeCommand(() -> {
                    PerformanceChannel.setGpuRenderMode(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Vulkan 3D HWUI & Skia Pipeline Applied" : "Default OpenGL Engine Restored", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchCpuMode != null) {
            switchCpuMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "CPU Extreme Governor")) return;
                ManualSettingsPreferences.setCpuMode(getContext(), isChecked ? "performance" : "schedutil");
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.CpuGovernorChannel.setGovernor(isChecked ? "extreme" : "schedutil");
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ CPU Extreme Governor & ADPF Boost Locked" : "CPU Dynamic Governor Restored", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchThermalBypass != null) {
            switchThermalBypass.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Thermal Throttling Bypass")) return;
                ManualSettingsPreferences.setThermalBypassEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.booster.ThermalChannel.setThermalOverride(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked
                                    ? "🔥 Temperature Limit Disabled: Full Android 13–16 Thermal Bypass Active!"
                                    : "Thermal Throttling Normal Mitigation Restored", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchAdpfEngine != null) {
            switchAdpfEngine.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "ADPF Power Hint Engine")) return;
                ManualSettingsPreferences.setAdpfEngineEnabled(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked
                        ? "⚡ Android 13–16 ADPF Power Hint Engine Enabled"
                        : "ADPF Engine Disabled", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchUclampBoost != null) {
            switchUclampBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Kernel uclamp 1024 Boost")) return;
                ManualSettingsPreferences.setUclampBoostEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        com.gamebooster.app.booster.CpuGovernorChannel.tuneMultiCoreTopology();
                        com.gamebooster.app.booster.CpuGovernorChannel.applyExtendedKernelFlags();
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Linux Kernel uclamp 1024 Boost Locked" : "uclamp Boost Reverted", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchTouch1000hzLock != null) {
            switchTouch1000hzLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "1000Hz Hardware Digitizer")) return;
                ManualSettingsPreferences.setTouch1000HzLockEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        com.gamebooster.app.booster.TouchLatencyChannel.enableUltraTouchResponse();
                    } else {
                        com.gamebooster.app.booster.TouchLatencyChannel.restoreDefaultTouchResponse();
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🎯 Universal 1000Hz Digitizer & LSQ2 Tracking Locked" : "Touch Sampling Reverted to Default", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchPhantomFreezerKill != null) {
            switchPhantomFreezerKill.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Phantom Process Killer Bypass")) return;
                ManualSettingsPreferences.setPhantomFreezerKillEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        CommandExecutor.executeSystemCommand("device_config put activity_manager max_phantom_processes 2147483647 2>/dev/null; settings put global settings_enable_monitor_phantom_procs false 2>/dev/null; settings put global cached_apps_freezer disabled 2>/dev/null; cmd device_config put activity_manager freeze_debounce_timeout 86400000 2>/dev/null");
                    } else {
                        CommandExecutor.executeSystemCommand("settings put global settings_enable_monitor_phantom_procs true 2>/dev/null; settings put global cached_apps_freezer enabled 2>/dev/null");
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🛡️ Android 13–16 Phantom Killer & App Freezer Disabled!" : "Process Freezer Restored", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchVulkanSkiavk != null) {
            switchVulkanSkiavk.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Vulkan SkiaVK Pipeline")) return;
                ManualSettingsPreferences.setVulkanSkiaVkEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        com.gamebooster.app.booster.GpuTweaksChannel.enableVulkanRenderer();
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🔥 Native Vulkan SkiaVK Pipeline Active (ANGLE Purged)" : "Vulkan SkiaVK Reverted", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchMemory16kbShield != null) {
            switchMemory16kbShield.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "16KB Page Buffer Shield")) return;
                ManualSettingsPreferences.setMemory16kbShieldEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        CommandExecutor.executeSystemCommand("sysctl -w vm.max_map_count=1048576 2>/dev/null; echo 1048576 > /proc/sys/vm/max_map_count 2>/dev/null; sysctl -w vm.swappiness=10 2>/dev/null; echo 10 > /proc/sys/vm/swappiness 2>/dev/null; sysctl -w vm.vfs_cache_pressure=50 2>/dev/null; echo 50 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null; sysctl -w vm.dirty_ratio=5 2>/dev/null; sysctl -w vm.dirty_background_ratio=2 2>/dev/null; sysctl -w vm.compaction_proactiveness=0 2>/dev/null; sysctl -w vm.watermark_boost_factor=0 2>/dev/null; sysctl -w vm.min_free_kbytes=65536 2>/dev/null");
                    } else {
                        CommandExecutor.executeSystemCommand("sysctl -w vm.swappiness=60 2>/dev/null; echo 60 > /proc/sys/vm/swappiness 2>/dev/null; sysctl -w vm.vfs_cache_pressure=100 2>/dev/null");
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "💾 16KB Page Buffer Shield & Swappiness 10 Active!" : "Memory Tunings Reset", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchWebviewBoost != null) {
            switchWebviewBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "WebView Hardware Acceleration")) return;
                ManualSettingsPreferences.setWebViewBoostEnabled(getContext(), isChecked);
                final Context appCtx = getContext().getApplicationContext();
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        com.gamebooster.app.booster.WebViewBoosterChannel.applyWebViewPerformanceBoost(appCtx);
                    } else {
                        com.gamebooster.app.booster.WebViewBoosterChannel.restoreWebViewDefaults();
                    }
                    com.gamebooster.app.booster.WebViewBoosterChannel.DeviceTier tier =
                            com.gamebooster.app.booster.WebViewBoosterChannel.detectDeviceTier(appCtx);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ WebView Boost Active: " + tier.description : "WebView Flags Reset to Default", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        // Card 4: Network & Latency Optimization (Pure Manual ON/OFF Toggles - Android 13 to 16)
        tvLiveNetworkTelemetry = view.findViewById(R.id.tv_live_network_telemetry);
        switchTcpBbrBuffers = view.findViewById(R.id.switch_tcp_bbr_buffers);
        switch5g6gData = view.findViewById(R.id.switch_5g_6g_data);
        switchWifiLowLatency = view.findViewById(R.id.switch_wifi_low_latency);
        switchDualDataWifi = view.findViewById(R.id.switch_dual_data_wifi);
        switchDnsCloudflare = view.findViewById(R.id.switch_dns_cloudflare);
        switchDnsGoogle = view.findViewById(R.id.switch_dns_google);
        switchPhTelcoSupercharger = view.findViewById(R.id.switch_ph_telco_supercharger);
        switchAutoDnsFlush = view.findViewById(R.id.switch_auto_dns_flush);
        switchTetheringHw = view.findViewById(R.id.switch_tethering_hw);
        switchForceGnss = view.findViewById(R.id.switch_force_gnss);

        if (getContext() != null) {
            isProgrammaticToggle = true;
            if (switchTcpBbrBuffers != null) switchTcpBbrBuffers.setChecked(ManualSettingsPreferences.isTcpBbrBuffersEnabled(getContext()));
            if (switch5g6gData != null) switch5g6gData.setChecked(ManualSettingsPreferences.is5g6gDataEnabled(getContext()));
            if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(ManualSettingsPreferences.isWifiLowLatencyEnabled(getContext()));
            if (switchDualDataWifi != null) switchDualDataWifi.setChecked(ManualSettingsPreferences.isDualDataWifiEnabled(getContext()));
            if (switchPhTelcoSupercharger != null) switchPhTelcoSupercharger.setChecked(ManualSettingsPreferences.isPhTelcoSuperchargerEnabled(getContext()));
            if (switchAutoDnsFlush != null) switchAutoDnsFlush.setChecked(ManualSettingsPreferences.isAutoDnsFlushEnabled(getContext()));
            if (switchTetheringHw != null) switchTetheringHw.setChecked(ManualSettingsPreferences.isTetherHwEnabled(getContext()));
            if (switchForceGnss != null) switchForceGnss.setChecked(ManualSettingsPreferences.isForceGnssEnabled(getContext()));

            updateDnsUiState(ManualSettingsPreferences.getGamingDns(getContext()));
            updateLiveTelemetryUi();
            isProgrammaticToggle = false;
        }

        if (switchTcpBbrBuffers != null) {
            switchTcpBbrBuffers.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "TCP BBR & 8MB Buffers")) return;
                ManualSettingsPreferences.setTcpBbrBuffersEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        NetworkOptimizer.optimizeTcpBuffers();
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            updateLiveTelemetryUi();
                            Toast.makeText(getContext(), isChecked ? "🚀 TCP BBR & 8MB Buffers Enforced" : "TCP Normal Buffers Restored", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switch5g6gData != null) {
            switch5g6gData.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "5G/6G Data Accelerator")) return;
                ManualSettingsPreferences.set5g6gDataEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.optimize5gAnd6gDataNetwork(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            updateLiveTelemetryUi();
                            Toast.makeText(getContext(), isChecked ? "📱 5G/6G Data Accelerator Enabled" : "5G/6G Data Accelerator Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchWifiLowLatency != null) {
            switchWifiLowLatency.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Wi-Fi Low-Latency Lock")) return;
                ManualSettingsPreferences.setWifiLowLatencyEnabled(getContext(), isChecked);
                if (isChecked) {
                    com.gamebooster.app.engine.NativeFrameworkBridge.acquireLowLatencyWifiLock(getContext());
                } else {
                    com.gamebooster.app.engine.NativeFrameworkBridge.releaseLowLatencyWifiLock();
                }
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.optimizeWifi6and7LowLatency(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            updateLiveTelemetryUi();
                            Toast.makeText(getContext(), isChecked ? "📶 Wi-Fi Low-Latency Lock Active" : "Wi-Fi Normal Mode Restored", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchDualDataWifi != null) {
            switchDualDataWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Dual Data + Wi-Fi Aggregation")) return;
                ManualSettingsPreferences.setDualDataWifiEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setDualDataAndWifiAcceleration(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            updateLiveTelemetryUi();
                            Toast.makeText(getContext(), isChecked ? "⚡ Dual Multipath Aggregation Active" : "Dual Multipath Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchDnsCloudflare != null) {
            switchDnsCloudflare.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked) {
                    if (!checkShizukuOrRevert(buttonView, "Cloudflare 1.1.1.1 Gaming DNS")) return;
                    isProgrammaticToggle = true;
                    if (switchDnsGoogle != null) switchDnsGoogle.setChecked(false);
                    isProgrammaticToggle = false;
                    applyGamingDns(NetworkOptimizer.DnsMode.CLOUDFLARE_1_1_1_1, "⚡ 1.1.1.1 Cloudflare Gaming DNS Applied");
                } else {
                    if (switchDnsGoogle == null || !switchDnsGoogle.isChecked()) {
                        applyGamingDns(NetworkOptimizer.DnsMode.SYSTEM_DEFAULT, "🔄 System Default DNS Restored");
                    }
                }
            });
        }

        if (switchDnsGoogle != null) {
            switchDnsGoogle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked) {
                    if (!checkShizukuOrRevert(buttonView, "Google 8.8.8.8 Gaming DNS")) return;
                    isProgrammaticToggle = true;
                    if (switchDnsCloudflare != null) switchDnsCloudflare.setChecked(false);
                    isProgrammaticToggle = false;
                    applyGamingDns(NetworkOptimizer.DnsMode.GOOGLE_8_8_8_8, "🌐 8.8.8.8 Google Gaming DNS Applied");
                } else {
                    if (switchDnsCloudflare == null || !switchDnsCloudflare.isChecked()) {
                        applyGamingDns(NetworkOptimizer.DnsMode.SYSTEM_DEFAULT, "🔄 System Default DNS Restored");
                    }
                }
            });
        }

        if (switchPhTelcoSupercharger != null) {
            switchPhTelcoSupercharger.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "PH Telco Supercharger")) return;
                ManualSettingsPreferences.setPhTelcoSuperchargerEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    if (isChecked) {
                        NetworkOptimizer.applyPhCarrierOptimization(getContext(), NetworkOptimizer.PhCarrier.TNT_SMART);
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            updateLiveTelemetryUi();
                            Toast.makeText(getContext(), isChecked ? "🇵🇭 PH Telco Supercharger Active (Smart / Globe / DITO)" : "PH Telco Supercharger Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchAutoDnsFlush != null) {
            switchAutoDnsFlush.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                ManualSettingsPreferences.setAutoDnsFlushEnabled(getContext(), isChecked);
                if (isChecked) {
                    AppExecutors.getInstance().executeCommand(() -> {
                        NetworkOptimizer.flushDnsCache();
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (isAdded() && getContext() != null) {
                                updateLiveTelemetryUi();
                                Toast.makeText(getContext(), "🧹 DNS & Route Cache Flushed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                }
            });
        }

        if (switchTetheringHw != null) {
            switchTetheringHw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Tethering Hardware Offload")) return;
                ManualSettingsPreferences.setTetherHwEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setTetheringHwAcceleration(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🔥 Tethering Hardware Offload Enabled" : "Tethering Offload Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchForceGnss != null) {
            switchForceGnss.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Force Full GNSS Measurements")) return;
                ManualSettingsPreferences.setForceGnssEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setForceFullGnss(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "🛰️ Force Full GNSS Measurements Enabled" : "GNSS Raw Measurements Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        // Card 4.5: System & Kernel Tweaks Repository
        View tweaksHeaderTitle = view.findViewById(R.id.tv_settings_tweaks_title);
        if (tweaksHeaderTitle != null) {
            tweaksHeaderTitle.setOnClickListener(v -> {
                if (getContext() != null) {
                    ShizukuManager.showShizukuOfflineGuide(getContext());
                }
            });
        }
        tvSettingsTweaksBadgeCount = view.findViewById(R.id.tv_settings_tweaks_badge_count);
        etSettingsTweaksSearch = view.findViewById(R.id.et_settings_tweaks_search);
        rvSettingsTweaks = view.findViewById(R.id.rv_settings_tweaks);
        btnTweakFilterAll = view.findViewById(R.id.btn_tweak_filter_all);
        btnTweakFilterCpuGpu = view.findViewById(R.id.btn_tweak_filter_cpugpu);
        btnTweakFilterTouch = view.findViewById(R.id.btn_tweak_filter_touch);
        btnTweakFilterShizuku = view.findViewById(R.id.btn_tweak_filter_shizuku);
        btnTweakFilterNetwork = view.findViewById(R.id.btn_tweak_filter_network);
        btnSettingsTweaksApplyAll = view.findViewById(R.id.btn_settings_tweaks_apply_all);
        btnSettingsTweaksResetAll = view.findViewById(R.id.btn_settings_tweaks_reset_all);

        if (getContext() != null) {
            com.gamebooster.app.tweaks.TweakManagerRepository.initializeStates(getContext());
        }

        if (rvSettingsTweaks != null && getContext() != null) {
            rvSettingsTweaks.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSettingsTweaks.setHasFixedSize(false);
            rvSettingsTweaks.setNestedScrollingEnabled(false);
            settingsTweaksAdapter = new com.gamebooster.app.ui.adapters.TweaksAdapter(
                    getContext(),
                    com.gamebooster.app.tweaks.TweakManagerRepository.getAllTweaks()
            );
            settingsTweaksAdapter.setShizukuAlive(isPrivilegedExecutionAvailable());
            settingsTweaksAdapter.setOnTweakStateChangeListener((item, isApplied, totalAppliedCount) -> {
                updateTweaksBadgeCount();
            });
            rvSettingsTweaks.setAdapter(settingsTweaksAdapter);
            updateTweaksBadgeCount();
        }

        setupTweakFilters();

        if (etSettingsTweaksSearch != null) {
            etSettingsTweaksSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (settingsTweaksAdapter != null) {
                        settingsTweaksAdapter.setSearchQuery(s != null ? s.toString() : "");
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnSettingsTweaksApplyAll != null) {
            btnSettingsTweaksApplyAll.setOnClickListener(v -> {
                if (getContext() == null) return;
                if (!requireShizukuForAction("Batch Apply Tweaks")) return;
                btnSettingsTweaksApplyAll.setEnabled(false);
                btnSettingsTweaksApplyAll.setText("⏳ APPLYING TWEAKS...");
                Toast.makeText(getContext(), "⚡ Applying tweaks in batch...", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    int applied = com.gamebooster.app.tweaks.TweakManagerRepository.applyAllSupportedTweaks(getContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            btnSettingsTweaksApplyAll.setEnabled(true);
                            btnSettingsTweaksApplyAll.setText("⚡ 1-TAP APPLY ALL");
                            if (settingsTweaksAdapter != null) {
                                settingsTweaksAdapter.notifyAllStatesChanged();
                            }
                            updateTweaksBadgeCount();
                            Toast.makeText(getContext(), "✅ " + applied + " Tweaks Applied & Locked!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (btnSettingsTweaksResetAll != null) {
            btnSettingsTweaksResetAll.setOnClickListener(v -> {
                if (getContext() == null) return;
                btnSettingsTweaksResetAll.setEnabled(false);
                btnSettingsTweaksResetAll.setText("⏳ RESETTING...");
                Toast.makeText(getContext(), "🔄 Reverting tweaks to stock...", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.tweaks.TweakManagerRepository.revertAllTweaks(getContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            btnSettingsTweaksResetAll.setEnabled(true);
                            btnSettingsTweaksResetAll.setText("🔄 RESET TO STOCK");
                            if (settingsTweaksAdapter != null) {
                                settingsTweaksAdapter.notifyAllStatesChanged();
                            }
                            updateTweaksBadgeCount();
                            Toast.makeText(getContext(), "🔄 All Tweaks Reset to Stock Defaults", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        // Card Spoof: Hardware Device Spoofing
        switchDeviceSpoof = view.findViewById(R.id.switch_device_spoof);
        tvSpoofActiveProfile = view.findViewById(R.id.tv_spoof_active_profile);
        tvSettingsSpoofBrandInfo = view.findViewById(R.id.tv_settings_spoof_brand_info);
        hsvSettingsSpoofBrands = view.findViewById(R.id.hsv_settings_spoof_brands);
        rvSpoofProfiles = view.findViewById(R.id.rv_spoof_profiles);

        boolean spoofEnabled = getContext() != null && SpoofPreferences.isSpoofEnabled(getContext());
        if (switchDeviceSpoof != null) {
            isProgrammaticToggle = true;
            switchDeviceSpoof.setChecked(spoofEnabled);
            isProgrammaticToggle = false;
        }

        if (hsvSettingsSpoofBrands != null) {
            hsvSettingsSpoofBrands.setVisibility(View.VISIBLE);
        }
        if (tvSettingsSpoofBrandInfo != null) {
            tvSettingsSpoofBrandInfo.setVisibility(View.VISIBLE);
        }

        if (rvSpoofProfiles != null) {
            rvSpoofProfiles.setLayoutManager(new LinearLayoutManager(getContext()));
            rvSpoofProfiles.setHasFixedSize(false);
            rvSpoofProfiles.setNestedScrollingEnabled(false);
            rvSpoofProfiles.setVisibility(View.VISIBLE);
            List<SpoofProfile> initialProfiles = SpoofProfileRegistry.getByBrand("ASUS ROG");
            spoofProfileAdapter = new SpoofProfileAdapter(getContext(), initialProfiles, profile -> {
                if (getContext() == null || profile == null) return;

                if (!com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("🛡️ SHIZUKU API REQUIRED")
                            .setMessage("Device Spoofing requires active Shizuku API (UID 2000 Privileged Shell).\n\nPlease authorize Shizuku to activate.")
                            .setPositiveButton("GRANT SHIZUKU", (d, w) -> {
                                com.gamebooster.app.shizuku.ShizukuExecutor.requestPermission();
                            })
                            .setNegativeButton("CANCEL", null)
                            .show();
                    return;
                }

                // 1. Persist selected profile ID and enable spoofing
                SpoofPreferences.setSpoofEnabled(getContext(), true);
                SpoofPreferences.setActiveProfileId(getContext(), profile.id);
                if (switchDeviceSpoof != null) {
                    isProgrammaticToggle = true;
                    switchDeviceSpoof.setChecked(true);
                    isProgrammaticToggle = false;
                }
                if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(profile.id);
                updateSpoofUiState();

                Toast.makeText(getContext(), "⚡ Activating & Forcing: " + profile.displayName, Toast.LENGTH_SHORT).show();

                // 2. Perform ultra-fast single-pass hardware & game file injection (<800ms)
                AppExecutors.getInstance().executeCommand(() -> {
                    int totalGames = DeviceSpooferEngine.applyFastHardwareSpoof(getContext(), profile.id);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        updateSpoofUiState();
                        Toast.makeText(getContext(), "⚡ Spoofed as " + profile.displayName + " (" + totalGames + " games masked instantly)", Toast.LENGTH_SHORT).show();
                    });
                });
            });
            rvSpoofProfiles.setAdapter(spoofProfileAdapter);
        }

        // Setup Brand Filter Buttons in Card Spoof
        Button btnBrandRog = view.findViewById(R.id.btn_brand_rog);
        Button btnBrandSamsung = view.findViewById(R.id.btn_brand_samsung);
        Button btnBrandNubia = view.findViewById(R.id.btn_brand_nubia);
        Button btnBrandXiaomi = view.findViewById(R.id.btn_brand_xiaomi);
        Button btnBrandRealme = view.findViewById(R.id.btn_brand_realme);
        Button btnBrandOneplus = view.findViewById(R.id.btn_brand_oneplus);
        Button btnBrandBlackshark = view.findViewById(R.id.btn_brand_blackshark);
        Button btnBrandApple = view.findViewById(R.id.btn_brand_apple);
        Button btnBrandVivo = view.findViewById(R.id.btn_brand_vivo);
        Button btnBrandOppo = view.findViewById(R.id.btn_brand_oppo);
        Button btnBrandLenovo = view.findViewById(R.id.btn_brand_lenovo);

        Button[] settingsBrandButtons = new Button[]{
                btnBrandRog, btnBrandSamsung, btnBrandNubia,
                btnBrandXiaomi, btnBrandRealme, btnBrandOneplus, btnBrandBlackshark,
                btnBrandApple, btnBrandVivo, btnBrandOppo, btnBrandLenovo
        };

        Runnable resetBrandChips = () -> {
            for (Button b : settingsBrandButtons) {
                if (b != null && getContext() != null) {
                    b.setBackgroundResource(R.drawable.btn_cyber_dark);
                    b.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.accent_cyan));
                }
            }
        };

        setupSettingsBrandFilter(btnBrandRog, "ASUS ROG", "⚡ ASUS ROG (185Hz / 165Hz Gaming Flagships)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandSamsung, "Samsung", "📱 SAMSUNG Galaxy (Ultra Lineup)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandNubia, "Nubia", "🎮 NUBIA RedMagic (165Hz eSports Flagships)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandXiaomi, "Xiaomi", "🚀 XIAOMI & POCO (Snapdragon 8 Series)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandRealme, "Realme", "🔥 REALME GT (Extreme Flagships)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandOneplus, "OnePlus", "🏎️ ONEPLUS (Ultra Performance)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandBlackshark, "Black Shark", "🦈 BLACK SHARK (Gaming Flagships)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandApple, "Apple", "🍎 APPLE (120Hz Pro Lineup)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandVivo, "Vivo", "🎯 VIVO & iQOO (eSports Flagships)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandOppo, "Oppo", "💎 OPPO (Find & Reno Flagships)", settingsBrandButtons, resetBrandChips);
        setupSettingsBrandFilter(btnBrandLenovo, "Lenovo Legion", "💻 LENOVO LEGION (Gaming Flagships)", settingsBrandButtons, resetBrandChips);

        if (switchDeviceSpoof != null) {
            switchDeviceSpoof.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Device Spoofing")) return;

                SpoofPreferences.setSpoofEnabled(getContext(), isChecked);

                if (!isChecked) {
                    AppExecutors.getInstance().executeCommand(() -> {
                        DeviceSpooferEngine.resetSpoof(getContext());
                        SpoofPreferences.clearActiveProfile(getContext());
                        AppExecutors.getInstance().postToMainThread(() -> {
                            if (isAdded() && getContext() != null) {
                                if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(null);
                                updateSpoofUiState();
                                Toast.makeText(getContext(), "🔄 Device Identity Reset to Native Hardware", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                } else {
                    String activeId = SpoofPreferences.getActiveProfileId(getContext());
                    if (activeId == null || activeId.trim().isEmpty()) {
                        // User has not selected any spoof profile yet - do NOT blind activate
                        if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(null);
                        updateSpoofUiState();
                        Toast.makeText(getContext(), "📱 Device Spoofing ON: Select a gaming device model below to activate spoofing.", Toast.LENGTH_LONG).show();
                    } else {
                        SpoofProfile prof = DeviceSpooferEngine.getProfileById(activeId);
                        if (prof == null) {
                            if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(null);
                            updateSpoofUiState();
                            Toast.makeText(getContext(), "📱 Please select a gaming device model below to activate spoofing.", Toast.LENGTH_SHORT).show();
                        } else {
                            final SpoofProfile finalProf = prof;
                            if (spoofProfileAdapter != null) spoofProfileAdapter.setActiveProfileId(finalProf.id);
                            updateSpoofUiState();
                            AppExecutors.getInstance().executeCommand(() -> {
                                int maskedCount = DeviceSpooferEngine.applyFastHardwareSpoof(getContext(), finalProf.id);
                                AppExecutors.getInstance().postToMainThread(() -> {
                                    if (isAdded() && getContext() != null) {
                                        updateSpoofUiState();
                                        Toast.makeText(getContext(), "⚡ Fast Masked " + maskedCount + " Apps with " + finalProf.displayName + " (" + finalProf.model + ")", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        }
                    }
                }
            });
        }

        // Card 6: Pure Cyber Terminal & Scripts Folder
        tvSettingsTerminalUid = view.findViewById(R.id.tv_settings_terminal_uid);
        tvSettingsTerminalFolderPath = view.findViewById(R.id.tv_settings_terminal_folder_path);
        tvSettingsTerminalOutput = view.findViewById(R.id.tv_settings_terminal_output);
        scrollSettingsTerminal = view.findViewById(R.id.scroll_settings_terminal);
        if (scrollSettingsTerminal != null) {
            scrollSettingsTerminal.setNestedScrollingEnabled(true);
            scrollSettingsTerminal.setOnTouchListener((v, event) -> {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (v.canScrollVertically(1) || v.canScrollVertically(-1)) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            });
        }
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
            btnSettingsScriptFolder.setOnClickListener(v -> {
                if (!requireShizukuForAction("Terminal Scripts Folder")) return;
                showSettingsFolderDialog();
            });
        }

        if (btnSettingsScriptWhoami != null) {
            btnSettingsScriptWhoami.setOnClickListener(v -> {
                if (!requireShizukuForAction("Terminal Script Execution")) return;
                runSettingsTerminalQuickCmd("id; whoami; pm get-install-location");
            });
        }

        if (btnSettingsScriptRam != null) {
            btnSettingsScriptRam.setOnClickListener(v -> {
                if (!requireShizukuForAction("RAM Trimming & Process Cleanup")) return;
                runSettingsTerminalQuickCmd("cmd package trim-caches 9223372036854775807 2>/dev/null || pm trim-caches 40000000000; echo 3 > /proc/sys/vm/drop_caches 2>/dev/null; dumpsys meminfo --oom");
            });
        }

        if (btnSettingsScriptStorage != null) {
            btnSettingsScriptStorage.setOnClickListener(v -> {
                if (!requireShizukuForAction("Storage Permission Unlock")) return;
                if (getContext() != null) {
                    com.gamebooster.app.shizuku.ShizukuPermissionEnforcer.enforceAllPermissions(getContext().getApplicationContext());
                }
                runSettingsTerminalQuickCmd("chmod -R 777 /sdcard/Android/data /sdcard/Android/obb; echo '[STORAGE RW UNLOCKED]'");
            });
        }

        if (btnSettingsScriptFps != null) {
            btnSettingsScriptFps.setOnClickListener(v -> {
                if (!requireShizukuForAction("FPS & Anti-Log Optimization")) return;
                runSettingsTerminalQuickCmd("settings put system peak_refresh_rate 185.0; settings put system min_refresh_rate 185.0; setprop debug.sf.fps_limit 185; logcat -c; echo '[185Hz / 185FPS MAX & ANTI-LOG ACTIVE]'");
            });
        }

        if (btnSettingsScriptTouch != null) {
            btnSettingsScriptTouch.setOnClickListener(v -> {
                if (!requireShizukuForAction("1000Hz Touch Optimization")) return;
                runSettingsTerminalQuickCmd("setprop debug.input.max_events_per_sec 1000; setprop view.touch_slop 1; echo '[1000Hz TOUCH & 1ms SLOP ACTIVE]'");
            });
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

        setupCombatEnhancementSuiteSection(view);
        refreshAllStatuses();
        return view;
    }

    private void updateDnsUiState(String dnsMode) {
        if (getContext() == null) return;
        isProgrammaticToggle = true;
        if ("GOOGLE_8_8_8_8".equalsIgnoreCase(dnsMode)) {
            if (switchDnsGoogle != null) switchDnsGoogle.setChecked(true);
            if (switchDnsCloudflare != null) switchDnsCloudflare.setChecked(false);
        } else if ("CLOUDFLARE_1_1_1_1".equalsIgnoreCase(dnsMode)) {
            if (switchDnsCloudflare != null) switchDnsCloudflare.setChecked(true);
            if (switchDnsGoogle != null) switchDnsGoogle.setChecked(false);
        } else {
            if (switchDnsCloudflare != null) switchDnsCloudflare.setChecked(false);
            if (switchDnsGoogle != null) switchDnsGoogle.setChecked(false);
        }
        isProgrammaticToggle = false;
    }

    private void applyGamingDns(NetworkOptimizer.DnsMode mode, String msg) {
        if (getContext() == null) return;
        ManualSettingsPreferences.setGamingDns(getContext(), mode.name());
        updateDnsUiState(mode.name());

        boolean hasPrivilege = isPrivilegedExecutionAvailable();
        if (!hasPrivilege && mode != NetworkOptimizer.DnsMode.SYSTEM_DEFAULT) {
            // Non-Shizuku fallback: copy DoT hostname to clipboard and offer to open Network Settings
            try {
                android.content.ClipboardManager cm = (android.content.ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) {
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("DoT Hostname", mode.privateDnsHost));
                }
            } catch (Throwable ignored) {}

            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("🌐 GAMING DNS OVER TLS (DoT)")
                    .setMessage("Copied '" + mode.privateDnsHost + "' to clipboard!\n\nTo lock this low-latency DNS on your device without Shizuku, tap OPEN SETTINGS and paste it under 'Private DNS provider hostname'.")
                    .setPositiveButton("OPEN NETWORK SETTINGS", (d, w) -> {
                        try {
                            Intent intent = new Intent(android.provider.Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Throwable t) {
                            try {
                                Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } catch (Throwable ignored) {}
                        }
                    })
                    .setNegativeButton("OK", null)
                    .show();
        }

        AppExecutors.getInstance().executeCommand(() -> {
            NetworkOptimizer.applyGamingDns(getContext(), mode);
            NetworkOptimizer.flushDnsCache();
            AppExecutors.getInstance().postToMainThread(() -> {
                if (isAdded() && getContext() != null) {
                    updateLiveTelemetryUi();
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateLiveTelemetryUi() {
        if (tvLiveNetworkTelemetry == null || getContext() == null) return;
        AppExecutors.getInstance().executeCommand(() -> {
            NetworkOptimizer.NetworkTelemetry tel = NetworkOptimizer.getLiveTelemetry(getContext());
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || tvLiveNetworkTelemetry == null) return;
                StringBuilder sb = new StringBuilder();
                sb.append("🌐 Link: ").append(tel.activeInterface)
                  .append("  •  DNS: ").append(tel.activeDns);
                if (tel.isWifiLockHeld) {
                    sb.append("  •  ⚡ Wi-Fi Driver Lock: Active");
                }
                tvLiveNetworkTelemetry.setText(sb.toString());
            });
        });
    }

    private void applyPresetProfile(Button button, PerformanceChannel.Profile profile, int targetHz, String successMsg) {
        if (getContext() == null || button == null) return;
        if (!requireShizukuForAction(targetHz + "Hz Performance Profile")) return;
        button.setEnabled(false);
        Toast.makeText(getContext(), "Applying " + targetHz + "Hz performance profile to all games & display...", Toast.LENGTH_SHORT).show();
        AppExecutors.getInstance().executeCommand(() -> {
            boolean ok = PerformanceChannel.applyProfile(getContext(), profile);
            com.gamebooster.app.booster.MaxHzForceChannel.forceApply(targetHz);
            GameProfileAutoConfigurator.autoConfigAllGamesAsync(getContext(), targetHz, null);
            CfgProfileManager.applyAllGames(getContext(), targetHz, true, true);

            // Game-specific patches, Ranked Damage Sync & Low-Latency Network Tuning
            List<com.gamebooster.app.games.GameAppInfo> installedGames = com.gamebooster.app.games.HomeGameScanner.scanTargetGames(getContext());
            for (com.gamebooster.app.games.GameAppInfo g : installedGames) {
                String pkg = g.getPackageName();
                com.gamebooster.app.config.GameAutoInjectDispatcher.dispatchForPackage(getContext(), pkg, true);
                if (targetHz >= 165) {
                    if (pkg.contains("tencent.ig") || pkg.contains("pubg")) {
                        PubgConfigPatcher.patchSuperSmooth165(pkg);
                    } else if (pkg.contains("mobile.legends")) {
                        MlbbConfigPatcher.patchUltraExtreme185(pkg);
                    } else if (pkg.contains("callofduty") || pkg.contains("cod")) {
                        CodmConfigPatcher.patchUltraExtreme165(pkg);
                    }
                } else if (targetHz == 120) {
                    if (pkg.contains("tencent.ig") || pkg.contains("pubg")) {
                        PubgConfigPatcher.patchUltraHdr120(pkg);
                    }
                }
                CommonConfigTuningInjector.applyFullRankedMasterySuite(pkg);
            }
            NetworkOptimizer.enableRankedLowLatencySocketTuning(getContext());

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                button.setEnabled(true);
                Toast.makeText(getContext(), successMsg, Toast.LENGTH_SHORT).show();
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
                if (!isAdded() || getContext() == null) return;
                try {
                    refreshAllStatuses();
                } catch (Throwable t) {
                    android.util.Log.w("SettingsFragment", "onBinderStateChanged error: " + t.getMessage());
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoSettingsBg != null) videoSettingsBg.play();
        com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().forceReconnectCheck();
        refreshAllStatuses();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoSettingsBg != null) videoSettingsBg.pause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (videoSettingsBg != null) videoSettingsBg.pause();
        } else {
            if (videoSettingsBg != null) videoSettingsBg.play();
            refreshAllStatuses();
            renderDiagnostics();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (videoSettingsBg != null) {
            videoSettingsBg.release();
            videoSettingsBg = null;
        }
    }

    private void refreshAllStatuses() {
        try {
            updateSpoofUiState();
            updatePrecisionAimStatus();

            if (getContext() != null) {
                isProgrammaticToggle = true;
                if (switchOverlayHud != null) switchOverlayHud.setChecked(com.gamebooster.app.overlay.FloatingOverlayService.isOverlayRunning());
                if (switchGamingDnd != null) switchGamingDnd.setChecked(com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getContext()));
                if (switchAutoGameBoost != null) switchAutoGameBoost.setChecked(com.gamebooster.app.gamespace.AutoGameMonitorService.isRunning());
                if (switchEsportsAudio != null) switchEsportsAudio.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isEnabled());
                if (switchSpeakerBypassBoost != null) switchSpeakerBypassBoost.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isSpeakerBypassEnabled(getContext()));
                if (switchAntiLog != null) switchAntiLog.setChecked(ManualSettingsPreferences.isAntiLogEnabled(getContext()));
                if (switchGameDriver != null) switchGameDriver.setChecked(ManualSettingsPreferences.isGameDriverEnabled(getContext()));
                if (switchGpuMode != null) switchGpuMode.setChecked("vulkan".equalsIgnoreCase(ManualSettingsPreferences.getGpuMode(getContext())));
                if (switchCpuMode != null) switchCpuMode.setChecked("performance".equalsIgnoreCase(ManualSettingsPreferences.getCpuMode(getContext())));
                if (switchThermalBypass != null) switchThermalBypass.setChecked(ManualSettingsPreferences.isThermalBypassEnabled(getContext()));
                if (switchTcpBbrBuffers != null) switchTcpBbrBuffers.setChecked(ManualSettingsPreferences.isTcpBbrBuffersEnabled(getContext()));
                if (switch5g6gData != null) switch5g6gData.setChecked(ManualSettingsPreferences.is5g6gDataEnabled(getContext()));
                if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(ManualSettingsPreferences.isWifiLowLatencyEnabled(getContext()));
                if (switchDualDataWifi != null) switchDualDataWifi.setChecked(ManualSettingsPreferences.isDualDataWifiEnabled(getContext()));
                if (switchPhTelcoSupercharger != null) switchPhTelcoSupercharger.setChecked(ManualSettingsPreferences.isPhTelcoSuperchargerEnabled(getContext()));
                if (switchAutoDnsFlush != null) switchAutoDnsFlush.setChecked(ManualSettingsPreferences.isAutoDnsFlushEnabled(getContext()));
                if (switchTetheringHw != null) switchTetheringHw.setChecked(ManualSettingsPreferences.isTetherHwEnabled(getContext()));
                if (switchForceGnss != null) switchForceGnss.setChecked(ManualSettingsPreferences.isForceGnssEnabled(getContext()));
                if (switchDeviceSpoof != null) switchDeviceSpoof.setChecked(SpoofPreferences.isSpoofEnabled(getContext()));
                if (switchWebviewBoost != null) switchWebviewBoost.setChecked(ManualSettingsPreferences.isWebViewBoostEnabled(getContext()));
                if (switchPrecisionInputTuner != null && precisionSettingsManager != null) {
                    switchPrecisionInputTuner.setChecked(precisionSettingsManager.isDeviceTuned());
                }
                updateDnsUiState(ManualSettingsPreferences.getGamingDns(getContext()));
                updateLiveTelemetryUi();
                isProgrammaticToggle = false;

                if (settingsTweaksAdapter != null) {
                    settingsTweaksAdapter.setShizukuAlive(isPrivilegedExecutionAvailable());
                    settingsTweaksAdapter.notifyAllStatesChanged();
                    updateTweaksBadgeCount();
                }
            }

            updateTerminalStatusInSettings();
        } catch (Throwable t) {
            android.util.Log.w("SettingsFragment", "refreshAllStatuses error: " + t.getMessage());
        }
    }

    private void handlePrecisionTunerToggle(boolean isChecked) {
        if (getContext() == null) return;

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
                    isProgrammaticToggle = true;
                    switchPrecisionInputTuner.setChecked(isChecked);
                    isProgrammaticToggle = false;
                }
                updatePrecisionAimStatus();
                Toast.makeText(getContext(), isChecked ? "🎯 Precision 1000Hz Touch & Gyro Tuned" : "Precision Input Reset to Stock", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void renderDiagnostics() {
        final Context ctx = getContext() != null ? getContext().getApplicationContext() : null;
        if (ctx == null) return;

        AppExecutors.getInstance().executeScan(() -> {
            try {
                // Ensure AIDL service connection is bound if Shizuku is active
                if (ShizukuManager.isShizukuRunningAndGranted() && !com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                    com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().bindService();
                }

                java.util.List<String> lines = com.gamebooster.app.diagnostics.DiagnosticsExporter.buildSnapshot(ctx);
                final String text = com.gamebooster.app.diagnostics.DiagnosticsExporter.join(lines);

                // Quick badge metrics
                boolean shizukuOk = ShizukuManager.isShizukuRunningAndGranted();
                boolean aidlOk = com.gamebooster.app.shizuku.ShizukuUserServiceConnector.getInstance().isServiceConnected();
                boolean isWifi = false;
                try {
                    android.net.ConnectivityManager cm = (android.net.ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (cm != null) {
                        android.net.Network activeNet = cm.getActiveNetwork();
                        android.net.NetworkCapabilities caps = cm.getNetworkCapabilities(activeNet);
                        isWifi = caps != null && caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI);
                    }
                } catch (Throwable ignored) {}

                float batteryTemp = -1f;
                try {
                    android.content.Intent bat = ctx.registerReceiver(null, new android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED));
                    if (bat != null) {
                        int t = bat.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, -1);
                        if (t > 0) batteryTemp = t / 10.0f;
                    }
                } catch (Throwable ignored) {}

                final float finalTemp = batteryTemp;
                final boolean finalShizuku = shizukuOk;
                final boolean finalAidl = aidlOk;
                final boolean finalWifi = isWifi;

                AppExecutors.getInstance().postToMainThread(() -> {
                    if (!isAdded()) return;
                    if (tvDiagStatus != null) {
                        tvDiagStatus.setText(text);
                    }
                    if (tvDiagBadgeShizuku != null) {
                        tvDiagBadgeShizuku.setText(finalShizuku ? "⚡ SHIZUKU: GRANTED (UID 2000)" : "⚠️ SHIZUKU: DISCONNECTED");
                        tvDiagBadgeShizuku.setTextColor(finalShizuku ? android.graphics.Color.parseColor("#00F0FF") : android.graphics.Color.parseColor("#FF5555"));
                    }
                    if (tvDiagBadgeAidl != null) {
                        tvDiagBadgeAidl.setText(finalAidl ? "🔌 AIDL: BOUND (ACTIVE)" : "🟡 AIDL: STANDBY");
                        tvDiagBadgeAidl.setTextColor(finalAidl ? android.graphics.Color.parseColor("#00FF66") : android.graphics.Color.parseColor("#FFAA00"));
                    }
                    if (tvDiagBadgeNet != null) {
                        tvDiagBadgeNet.setText(finalWifi ? "📶 WI-FI LOW-LATENCY" : "🚀 5G/MOBILE DATA");
                        tvDiagBadgeNet.setTextColor(android.graphics.Color.parseColor("#E2E8F0"));
                    }
                    if (tvDiagBadgeThermal != null) {
                        String tempStr = finalTemp > 0 ? String.format(java.util.Locale.US, "🌡️ %.1f°C", finalTemp) : "🌡️ THERMAL: OK";
                        tvDiagBadgeThermal.setText(tempStr);
                        tvDiagBadgeThermal.setTextColor(finalTemp > 42.0f ? android.graphics.Color.parseColor("#FF4444") : android.graphics.Color.parseColor("#FFAA00"));
                    }
                    if (tvDiagLiveIndicator != null) {
                        tvDiagLiveIndicator.setText("🟢 LIVE AUTO-SYNC: " + new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(new java.util.Date()));
                    }
                });
            } catch (Throwable t) {
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (tvDiagStatus != null) {
                        tvDiagStatus.setText("⚠️ Diagnostics Query Error: " + t.getMessage());
                    }
                });
            }
        });
    }

    private void exportDiagnostics() {
        if (getContext() == null) return;
        final Context ctx = getContext().getApplicationContext();
        Toast.makeText(getContext(), "⏳ Generating diagnostics report...", Toast.LENGTH_SHORT).show();
        AppExecutors.getInstance().executeScan(() -> {
            try {
                java.util.List<String> lines = com.gamebooster.app.diagnostics.DiagnosticsExporter.buildSnapshot(ctx);
                final String text = com.gamebooster.app.diagnostics.DiagnosticsExporter.join(lines);
                java.io.File file = com.gamebooster.app.diagnostics.DiagnosticsExporter.exportToFile(ctx, text);
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (tvDiagStatus != null) {
                        tvDiagStatus.setText(text);
                    }
                    if (getContext() != null) {
                        try {
                            Intent shareIntent = com.gamebooster.app.diagnostics.DiagnosticsExporter.shareSnapshot(getContext(), file);
                            startActivity(shareIntent);
                            Toast.makeText(getContext(), "🩺 Diagnostics exported", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            Toast.makeText(getContext(), "Share failed: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (Exception e) {
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void updateSpoofUiState() {
        if (getContext() == null) return;
        boolean enabled = SpoofPreferences.isSpoofEnabled(getContext());
        String activeId = SpoofPreferences.getActiveProfileId(getContext());

        if (tvSpoofActiveProfile != null) {
            if (enabled) {
                if (activeId != null && !activeId.trim().isEmpty()) {
                    SpoofProfile activeProf = DeviceSpooferEngine.getProfileById(activeId);
                    if (activeProf != null) {
                        tvSpoofActiveProfile.setText("Active Spoof Profile: ⚡ " + activeProf.displayName + " (" + activeProf.model + ")");
                        tvSpoofActiveProfile.setTextColor(0xFF00FF66);
                    } else {
                        tvSpoofActiveProfile.setText("Active Spoof Profile: ⚠️ ENABLED (No Device Selected - Choose Below)");
                        tvSpoofActiveProfile.setTextColor(0xFFFFB800);
                    }
                } else {
                    tvSpoofActiveProfile.setText("Active Spoof Profile: ⚠️ ENABLED (No Device Selected - Choose Below)");
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

    private void setupSettingsBrandFilter(Button btn, String brandLabel, String description, Button[] allButtons, Runnable resetBrandChips) {
        if (btn == null) return;
        btn.setOnClickListener(v -> {
            if (getContext() == null) return;
            resetBrandChips.run();
            btn.setBackgroundResource(R.drawable.btn_cyber_cyan);
            btn.setTextColor(0xFF000000);
            List<SpoofProfile> brandProfiles = SpoofProfileRegistry.getByBrand(brandLabel);
            if (rvSpoofProfiles != null) {
                rvSpoofProfiles.setVisibility(View.VISIBLE);
            }
            if (spoofProfileAdapter != null) {
                spoofProfileAdapter.updateProfiles(brandProfiles);
            }
            if (rvSpoofProfiles != null) {
                rvSpoofProfiles.scrollToPosition(0);
            }
            if (tvSettingsSpoofBrandInfo != null) {
                tvSettingsSpoofBrandInfo.setVisibility(View.VISIBLE);
                tvSettingsSpoofBrandInfo.setText("🏷️ Brand Filter: " + description + " (" + brandProfiles.size() + " models)");
            }
        });
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

    private void setupTweakFilters() {
        if (btnTweakFilterAll != null) {
            btnTweakFilterAll.setOnClickListener(v -> selectTweakFilter(com.gamebooster.app.tweaks.TweakCategory.ALL, btnTweakFilterAll));
        }
        if (btnTweakFilterCpuGpu != null) {
            btnTweakFilterCpuGpu.setOnClickListener(v -> selectTweakFilter(com.gamebooster.app.tweaks.TweakCategory.CPU_GPU, btnTweakFilterCpuGpu));
        }
        if (btnTweakFilterTouch != null) {
            btnTweakFilterTouch.setOnClickListener(v -> selectTweakFilter(com.gamebooster.app.tweaks.TweakCategory.TOUCH_DISPLAY, btnTweakFilterTouch));
        }
        if (btnTweakFilterShizuku != null) {
            btnTweakFilterShizuku.setOnClickListener(v -> selectTweakFilter(com.gamebooster.app.tweaks.TweakCategory.SHIZUKU_SYSTEM, btnTweakFilterShizuku));
        }
        if (btnTweakFilterNetwork != null) {
            btnTweakFilterNetwork.setOnClickListener(v -> selectTweakFilter(com.gamebooster.app.tweaks.TweakCategory.NETWORK_LATENCY, btnTweakFilterNetwork));
        }
    }

    private void selectTweakFilter(com.gamebooster.app.tweaks.TweakCategory cat, Button selectedBtn) {
        currentTweakCategory = cat;
        if (settingsTweaksAdapter != null) {
            settingsTweaksAdapter.setCategoryFilter(cat);
        }
        Button[] filterBtns = new Button[]{
                btnTweakFilterAll, btnTweakFilterCpuGpu, btnTweakFilterTouch,
                btnTweakFilterShizuku, btnTweakFilterNetwork
        };
        for (Button b : filterBtns) {
            if (b != null && getContext() != null) {
                b.setBackgroundResource(R.drawable.btn_cyber_dark);
                b.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.accent_cyan));
            }
        }
        if (selectedBtn != null) {
            selectedBtn.setBackgroundResource(R.drawable.btn_cyber_cyan);
            selectedBtn.setTextColor(0xFF000000);
        }
        updateTweaksBadgeCount();
    }

    private void updateTweaksBadgeCount() {
        if (tvSettingsTweaksBadgeCount == null || getContext() == null) return;
        int active = com.gamebooster.app.config.TweakPreferences.getAppliedTweakIds(getContext()).size();
        int total = com.gamebooster.app.tweaks.TweakManagerRepository.getAllTweaks().size();
        tvSettingsTweaksBadgeCount.setText("⚡ " + total + " Available | " + active + " Active");
    }

    // =========================================================================
    // PURE CYBER TERMINAL HELPER METHODS
    // =========================================================================

    private void updateTerminalStatusInSettings() {
        if (tvSettingsTerminalUid != null) {
            boolean hasShizuku = ShizukuExecutor.hasShizukuPermission();
            String user = TerminalCoreEngine.getInstance().getPromptUserPrefix();
            if (hasShizuku) {
                tvSettingsTerminalUid.setText(user + " (UID 2000)");
                tvSettingsTerminalUid.setTextColor(0xFF4ADE80);
            } else {
                tvSettingsTerminalUid.setText(user + " (Local)");
                tvSettingsTerminalUid.setTextColor(0xFFFFB800);
            }
        }
    }

    private void initSettingsTerminalBanner() {
        if (tvSettingsTerminalOutput == null) return;
        appendSettingsTerminalText("Welcome to Termux (Shizuku Privileged Shell)!\n", 0xFF4ADE80);
        appendSettingsTerminalText("Type 'help', 'neofetch', or any shell command.\n\n", 0xFF94A3B8);
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

        if (!requireShizukuForAction("Terminal Shell Execution")) return;

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
            int exitCode = 0;
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
                        exitCode = 1;
                    }
                } else if (cmd.contains("\n") || cmd.length() > 120) {
                    output = TerminalCoreEngine.getInstance().writeAndExecuteTempScript("game_tweak_run.sh", cmd);
                } else {
                    TerminalCoreEngine.TerminalResult tr = TerminalCoreEngine.getInstance().executeCommand(cmd);
                    output = tr.output;
                    exitCode = tr.exitCode;
                }
            } catch (Exception e) {
                output = "ERROR: " + e.getMessage();
                exitCode = 1;
            }

            final String finalOutput = output;
            final int finalExitCode = exitCode;
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                if (finalOutput != null && !finalOutput.isEmpty()) {
                    SpannableStringBuilder parsed = AnsiColorParser.parseAnsi(finalOutput + "\n\n", 0xFFE2E8F0);
                    settingsTerminalBuffer.append(parsed);
                    if (settingsTerminalBuffer.length() > 30000) {
                        settingsTerminalBuffer.delete(0, 10000);
                    }
                    if (tvSettingsTerminalOutput != null) {
                        tvSettingsTerminalOutput.setText(settingsTerminalBuffer);
                    }
                } else if (finalExitCode != 0) {
                    appendSettingsTerminalText("[Exit Code: " + finalExitCode + "]\n\n", 0xFFFF3366);
                }
                scrollSettingsTerminalToBottom();
            });
        });
    }

    private void appendSettingsTerminalPrompt(String command) {
        String user = TerminalCoreEngine.getInstance().getPromptUserPrefix();
        String currentDir = TerminalCoreEngine.getInstance().getCurrentWorkingDir();
        String displayDir = currentDir.equals("/data/local/tmp") ? "~" : currentDir;
        appendSettingsTerminalText(user + " ", 0xFF4ADE80);
        appendSettingsTerminalText(displayDir, 0xFF38BDF8);
        appendSettingsTerminalText(" $ ", 0xFFF1F5F9);
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
                        if (!requireShizukuForAction("Terminal Script Execution")) return;
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
                    if (!requireShizukuForAction("Terminal Script Execution")) return;
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
                    if (!requireShizukuForAction("Terminal Script Execution")) return;
                    runSettingsTerminalQuickCmd("run " + scriptFile.getName());
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    // ─── 2026.2 Combat Enhancement Suite — Settings Section ─────────────────

    /**
     * Bind all Combat Enhancement Suite switches and the Apply Now button.
     * Call this from onCreateView after all other sections are initialized.
     */
    private void setupCombatEnhancementSuiteSection(View view) {
        // ── Bind switches ────────────────────────────────────────────────────
        switchAdaptiveAimAssist  = view.findViewById(R.id.switch_adaptive_aim_assist);
        switchAdaptiveNoRecoil   = view.findViewById(R.id.switch_adaptive_no_recoil);
        switchRankedCombatSuite  = view.findViewById(R.id.switch_ranked_combat_suite);
        switchAimLock            = view.findViewById(R.id.switch_aim_lock);
        switchDamageOverdrive    = view.findViewById(R.id.switch_damage_overdrive);
        switchFastReload         = view.findViewById(R.id.switch_fast_reload);
        switchFastRun            = view.findViewById(R.id.switch_fast_run);
        switchFastCooldown       = view.findViewById(R.id.switch_fast_cooldown);
        switchAutoReInject       = view.findViewById(R.id.switch_auto_reinject);

        // ── Restore saved states ─────────────────────────────────────────────
        if (getContext() != null) {
            android.content.SharedPreferences prefs = getContext()
                    .getSharedPreferences("combat_suite_prefs", android.content.Context.MODE_PRIVATE);
            setCheckedSafe(switchAdaptiveAimAssist, prefs.getBoolean("aim_assist", true));
            setCheckedSafe(switchAdaptiveNoRecoil,  prefs.getBoolean("no_recoil",  true));
            setCheckedSafe(switchRankedCombatSuite,  prefs.getBoolean("ranked_suite", true));
            setCheckedSafe(switchAimLock,            prefs.getBoolean("aim_lock",    true));
            setCheckedSafe(switchDamageOverdrive,    prefs.getBoolean("damage",      true));
            setCheckedSafe(switchFastReload,         prefs.getBoolean("fast_reload", true));
            setCheckedSafe(switchFastRun,            prefs.getBoolean("fast_run",    true));
            setCheckedSafe(switchFastCooldown,       prefs.getBoolean("fast_cd",     true));
            setCheckedSafe(switchAutoReInject,       prefs.getBoolean("auto_reinject", true));
        }

        // ── Listeners ────────────────────────────────────────────────────────
        wireCombatSwitch(switchAdaptiveAimAssist, "Adaptive Aim Assist", "aim_assist", pkg ->
                com.gamebooster.app.config.CommonConfigTuningInjector.applyAdaptiveAimAssist(pkg));

        wireCombatSwitch(switchAdaptiveNoRecoil, "Adaptive No Recoil", "no_recoil", pkg ->
                com.gamebooster.app.config.CommonConfigTuningInjector.applyAdaptiveNoRecoil(pkg));

        wireCombatSwitch(switchRankedCombatSuite, "Ranked Combat Suite", "ranked_suite", pkg ->
                com.gamebooster.app.config.CommonConfigTuningInjector.applyRankedCombatFullSuite(pkg));

        wireCombatSwitch(switchAimLock, "Aim Lock", "aim_lock", pkg ->
                com.gamebooster.app.config.CommonConfigTuningInjector.applyAdaptiveAimAssist(pkg));

        wireCombatSwitch(switchDamageOverdrive, "Damage Overdrive 10000x", "damage", pkg ->
                com.gamebooster.app.config.CommonConfigTuningInjector.applyRankedCombatFullSuite(pkg));

        wireCombatSwitch(switchFastReload, "Fast Reload & Weapon Swap", "fast_reload", pkg ->
                com.gamebooster.app.config.CommonConfigTuningInjector.applyFastReloadQuickSwap(pkg));

        wireCombatSwitch(switchFastRun, "Fast Run / Sprint Turbo", "fast_run", pkg ->
                com.gamebooster.app.config.CommonConfigTuningInjector.applyInstantSprintTurbo(pkg));

        wireCombatSwitch(switchFastCooldown, "Fast Cooldown / Zero CD", "fast_cd", pkg ->
                com.gamebooster.app.config.CommonConfigTuningInjector.applySkillEconomyMasterSuite(pkg));

        // Auto Re-Inject switch (starts/stops MapChangeReInjector for currently detected game)
        if (switchAutoReInject != null) {
            switchAutoReInject.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                if (isChecked && !checkShizukuOrRevert(buttonView, "Auto Re-Inject on Map Change")) return;
                saveCombatPref("auto_reinject", isChecked);
                if (isChecked) {
                    Toast.makeText(getContext(),
                            "🔄 Auto Re-Inject ON — combat suite re-fires on every new map",
                            Toast.LENGTH_LONG).show();
                } else {
                    com.gamebooster.app.services.MapChangeReInjector.stopMonitoring();
                    Toast.makeText(getContext(), "Auto Re-Inject OFF", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /** Wire a single combat toggle switch with background thread injection dispatch. */
    private void wireCombatSwitch(Switch sw, String label, String prefKey,
                                  java.util.function.Consumer<String> injector) {
        if (sw == null) return;
        sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isProgrammaticToggle || getContext() == null) return;
            if (isChecked && !checkShizukuOrRevert(buttonView, label)) return;
            saveCombatPref(prefKey, isChecked);
            if (isChecked) {
                Toast.makeText(getContext(), "⚔️ " + label + " Enabled", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    String[] targets = {
                        "com.mobile.legends", "com.activision.callofduty.shooter",
                        "com.tencent.ig", "com.garena.game.codm",
                    };
                    for (String pkg : targets) {
                        try { injector.accept(pkg); } catch (Throwable ignored) {}
                    }
                });
            } else {
                Toast.makeText(getContext(), label + " Disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setCheckedSafe(Switch sw, boolean checked) {
        if (sw == null) return;
        isProgrammaticToggle = true;
        sw.setChecked(checked);
        isProgrammaticToggle = false;
    }

    private void saveCombatPref(String key, boolean value) {
        if (getContext() == null) return;
        getContext().getSharedPreferences("combat_suite_prefs", android.content.Context.MODE_PRIVATE)
                .edit().putBoolean(key, value).apply();
    }
}

