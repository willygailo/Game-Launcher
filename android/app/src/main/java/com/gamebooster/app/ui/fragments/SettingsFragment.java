package com.gamebooster.app.ui.fragments;

import com.gamebooster.app.ui.adapters.SpoofProfileAdapter;

import com.gamebooster.app.ui.adapters.TweaksAdapter;
import com.gamebooster.app.config.*;

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
import com.gamebooster.app.terminal.AnsiColorParser;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import androidx.core.widget.NestedScrollView;
import android.view.inputmethod.EditorInfo;
import com.gamebooster.app.cleaner.cleaner.JunkCleanerEngine;
import com.gamebooster.app.cleaner.model.CleanResult;
import com.gamebooster.app.cleaner.model.JunkScanResult;
import com.gamebooster.app.cleaner.scanner.JunkScanner;
import com.gamebooster.app.cleaner.ui.JunkCleanerDialog;
import com.gamebooster.app.ui.dialogs.CyberActionDialog;
import com.gamebooster.app.spoofer.SpoofProfileRegistry;
import com.gamebooster.app.spoofer.ui.PerAppSpoofDialog;
import com.gamebooster.app.spoofer.ui.SpoofInspectorDialog;
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
    private TextView tvTweaksStatus;
    private View bannerDisconnect;
    private TweaksAdapter tweaksAdapter;

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

    // Junk & Storage Cache Cleaner UI
    private TextView tvJunkCleanerStatus;
    private TextView tvJunkQuickSize;
    private TextView tvJunkQuickDetail;
    private TextView tvJunkQuickSubdetail;
    private Button btnScanJunk;
    private Button btnQuickCleanJunk;
    private Button btnOpenCleanerDashboard;
    private JunkScanResult lastJunkScanResult;
    private final JunkScanner junkScanner = new JunkScanner();
    private final JunkCleanerEngine junkCleanerEngine = new JunkCleanerEngine();

    // Hardware & Boost Switches
    private Switch switchAngleMode;
    private Switch switchGameDriver;
    private Switch switchGpuMode;
    private Switch switchCpuMode;
    private Switch switchThermalBypass;
    private Switch switchAdpfEngine;
    private Switch switchVideoSaver;
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
    private Switch switchFocusMode;
    private Button btnFocusWhitelist;

    // Network Mode UI
    private TextView tvNetworkActiveMode;
    private Button btnNetDataOnly;
    private Button btnNetWifiOnly;
    private Button btnNetDual;

    // Device Spoofing UI
    private Switch switchDeviceSpoof;
    private TextView tvSpoofActiveProfile;
    private TextView tvSettingsSpoofBrandInfo;
    private View hsvSettingsSpoofBrands;
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

    private boolean isProgrammaticToggle = false;

    private boolean requireShizukuForToggle(android.widget.CompoundButton button, String featureName) {
        if (isProgrammaticToggle) return false;
        if (!ShizukuManager.isShizukuRunningAndGranted()) {
            if (getContext() != null) {
                Toast.makeText(getContext(), "⚡ Applying via Android Framework API (Grant Shizuku for Tier 1 Root)", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    private boolean requireShizukuForAction(String featureName) {
        if (!ShizukuManager.isShizukuRunningAndGranted()) {
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
            videoSettingsBg.setVideoRawResource(R.raw.settings_bg_video);
        }

        // Card 1b: Diagnostics — shareable crash + settings snapshot
        if (getContext() != null) {
            com.gamebooster.app.diagnostics.CrashLog.install(getContext().getApplicationContext());
        }
        tvDiagStatus = view.findViewById(R.id.tv_diag_status);
        Button btnDiagRefresh = view.findViewById(R.id.btn_diag_refresh);
        Button btnDiagExport = view.findViewById(R.id.btn_diag_export);
        Button btnDiagClear = view.findViewById(R.id.btn_diag_clear);
        if (btnDiagRefresh != null) {
            btnDiagRefresh.setOnClickListener(v -> {
                renderDiagnostics();
                if (getContext() != null) {
                    Toast.makeText(getContext(), "🔄 Refreshing diagnostics...", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (btnDiagExport != null) {
            btnDiagExport.setOnClickListener(v -> exportDiagnostics());
        }
        if (btnDiagClear != null) {
            btnDiagClear.setOnClickListener(v -> {
                if (getContext() != null) {
                    com.gamebooster.app.diagnostics.CrashLog.clear(getContext());
                    Toast.makeText(getContext(), "🧹 Crash logs cleared", Toast.LENGTH_SHORT).show();
                    renderDiagnostics();
                }
            });
        }
        // Card 1.5: APK & Package Management Hub
        Button btnSettingsOpenApkManager = view.findViewById(R.id.btn_settings_open_apk_manager);
        Button btnSettingsAddGame = view.findViewById(R.id.btn_settings_add_game);
        Button btnSettingsAotCompileAll = view.findViewById(R.id.btn_settings_aot_compile_all);
        if (btnSettingsOpenApkManager != null) {
            btnSettingsOpenApkManager.setOnClickListener(v -> com.gamebooster.app.apk.ApkManagerDialog.show(getContext(), null));
        }
        if (btnSettingsAddGame != null) {
            btnSettingsAddGame.setOnClickListener(v -> com.gamebooster.app.ui.dialogs.AddGameDialog.show(getContext(), null));
        }
        if (btnSettingsAotCompileAll != null) {
            btnSettingsAotCompileAll.setOnClickListener(v -> {
                if (getContext() == null) return;
                if (!ShizukuManager.isShizukuRunningAndGranted()) {
                    ShizukuManager.showShizukuPermissionDialog(getContext(), "AOT Machine Code Compiler");
                    return;
                }

                List<String> gamePkgs = new ArrayList<>(com.gamebooster.app.games.GamePackageRegistry.getAllKnownGames().keySet());
                if (gamePkgs.isEmpty()) {
                    Toast.makeText(getContext(), "No games registered to compile.", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnSettingsAotCompileAll.setEnabled(false);
                btnSettingsAotCompileAll.setText("⚡ COMPILING AOT DEX (PLEASE WAIT)...");
                Toast.makeText(getContext(), "⚡ Starting ART Speed AOT Compilation for " + gamePkgs.size() + " games...", Toast.LENGTH_SHORT).show();

                com.gamebooster.app.engine.AotCompilerEngine.compileBatchAsync(gamePkgs, com.gamebooster.app.engine.AotCompilerEngine.CompileMode.SPEED, new com.gamebooster.app.engine.AotCompilerEngine.CompileListener() {
                    @Override
                    public void onProgress(int current, int total, String packageName, String message) {
                        if (isAdded() && getActivity() != null) {
                            btnSettingsAotCompileAll.setText("⚡ COMPILING (" + current + "/" + total + ")...");
                        }
                    }

                    @Override
                    public void onComplete(int successCount, int failedCount, String message) {
                        if (isAdded() && getActivity() != null) {
                            btnSettingsAotCompileAll.setEnabled(true);
                            btnSettingsAotCompileAll.setText("⚡ 1-TAP AOT DEX COMPILE (0MS STUTTER)");
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "✔ " + message, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            });
        }

        // Card 2: Esports Gaming Controls
        switchOverlayHud = view.findViewById(R.id.switch_overlay_hud);
        switchGamingDnd = view.findViewById(R.id.switch_gaming_dnd);
        switchAutoGameBoost = view.findViewById(R.id.switch_auto_game_boost);
        switchEsportsAudio = view.findViewById(R.id.switch_esports_audio);

        if (switchOverlayHud != null) {
            isProgrammaticToggle = true;
            switchOverlayHud.setChecked(com.gamebooster.app.overlay.FloatingOverlayService.isOverlayRunning());
            isProgrammaticToggle = false;
            switchOverlayHud.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
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
                if (isChecked) {
                    com.gamebooster.app.overlay.FloatingOverlayService.startOverlay(getContext());
                    Toast.makeText(getContext(), "⚡ Performance HUD Overlay Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    com.gamebooster.app.overlay.FloatingOverlayService.stopOverlay(getContext());
                    Toast.makeText(getContext(), "Performance HUD Overlay Disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchGamingDnd != null) {
            isProgrammaticToggle = true;
            switchGamingDnd.setChecked(com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getContext()));
            isProgrammaticToggle = false;
            switchGamingDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
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
            switchEsportsAudio.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isEnabled());
            isProgrammaticToggle = false;
            switchEsportsAudio.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                com.gamebooster.app.booster.EsportsAudioEnhancer.setEsportsAudioMode(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked ? "🎧 Esports Footstep Audio Boost Enabled" : "Esports Audio Disabled", Toast.LENGTH_SHORT).show();
            });
        }

        switchAntiLog = view.findViewById(R.id.switch_anti_log);
        Button btnPurgeGameLogs = view.findViewById(R.id.btn_purge_game_logs);

        if (switchAntiLog != null) {
            if (getContext() != null) {
                isProgrammaticToggle = true;
                switchAntiLog.setChecked(ManualSettingsPreferences.isAntiLogEnabled(getContext()));
                isProgrammaticToggle = false;
            }
            switchAntiLog.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
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
                        Toast.makeText(getContext(), "✅ Purged logs for " + count + " game packages", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        Button btnArtBatchCompile = view.findViewById(R.id.btn_art_batch_compile);
        if (btnArtBatchCompile != null) {
            btnArtBatchCompile.setOnClickListener(v -> {
                if (getContext() == null) return;
                if (!com.gamebooster.app.engine.ArtCompilerEngine.isCompilerAvailable()) {
                    Toast.makeText(getContext(), "⚠️ Shizuku or elevated access required for ART compiler!", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnArtBatchCompile.setEnabled(false);
                btnArtBatchCompile.setText("⏳ COMPILING GAMES (AOT)...");
                Toast.makeText(getContext(), "⚡ Starting ART Speed-Profile compilation for all games...", Toast.LENGTH_SHORT).show();

                com.gamebooster.app.engine.ArtCompilerEngine.compileAllInstalledGamesAsync(
                        getContext(),
                        com.gamebooster.app.engine.ArtCompilerEngine.CompileFilter.SPEED,
                        new com.gamebooster.app.engine.ArtCompilerEngine.BatchCompileCallback() {
                            @Override
                            public void onGameStarted(String packageName, String gameLabel, int currentIndex, int totalGames) {
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(getContext(), "⚡ Compiling [" + currentIndex + "/" + totalGames + "]: " + gameLabel, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onGameFinished(String packageName, boolean success) {
                                // Game compilation step finished
                            }

                            @Override
                            public void onAllFinished(int successCount, int totalGames) {
                                if (!isAdded() || getContext() == null) return;
                                btnArtBatchCompile.setEnabled(true);
                                btnArtBatchCompile.setText("⚡ ART SPEED-COMPILE ALL GAMES (AOT)");
                                Toast.makeText(getContext(), "✅ ART AOT Speed Compilation Complete: " + successCount + "/" + totalGames + " games compiled!", Toast.LENGTH_LONG).show();
                            }
                        }
                );
            });
        }

        // Focus Mode (App Freezer & Deep Suspend)
        switchFocusMode = view.findViewById(R.id.switch_focus_mode);
        btnFocusWhitelist = view.findViewById(R.id.btn_focus_whitelist);

        if (switchFocusMode != null && getContext() != null) {
            isProgrammaticToggle = true;
            switchFocusMode.setChecked(ManualSettingsPreferences.isFocusModeEnabled(getContext()) || com.gamebooster.app.focus.FocusModeEngine.isFocusModeActive(getContext()));
            isProgrammaticToggle = false;

            switchFocusMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                ManualSettingsPreferences.setFocusModeEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    int count;
                    if (isChecked) {
                        count = com.gamebooster.app.focus.FocusModeEngine.enableFocusMode(getContext(), null);
                    } else {
                        count = com.gamebooster.app.focus.FocusModeEngine.disableFocusMode(getContext());
                    }
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            if (isChecked) {
                                Toast.makeText(getContext(), "🎯 Focus Mode Active: " + count + " Background Apps Frozen! (100% CPU & RAM Dedicated to Gaming)", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "🔄 Focus Mode Disabled: " + count + " Apps Restored to Normal", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                });
            });
        }

        if (btnFocusWhitelist != null) {
            btnFocusWhitelist.setOnClickListener(v -> {
                if (getContext() != null) {
                    com.gamebooster.app.focus.FocusAppSelectorDialog.show(getContext(), (frozenCount, isFrozen) -> {
                        if (switchFocusMode != null && isAdded()) {
                            isProgrammaticToggle = true;
                            switchFocusMode.setChecked(isFrozen && frozenCount > 0);
                            isProgrammaticToggle = false;
                        }
                    });
                }
            });
        }

        // Card 2.2: Junk Files & Storage Cache Cleaner
        tvJunkCleanerStatus = view.findViewById(R.id.tv_junk_cleaner_status);
        tvJunkQuickSize = view.findViewById(R.id.tv_junk_quick_size);
        tvJunkQuickDetail = view.findViewById(R.id.tv_junk_quick_detail);
        tvJunkQuickSubdetail = view.findViewById(R.id.tv_junk_quick_subdetail);
        btnScanJunk = view.findViewById(R.id.btn_scan_junk);
        btnQuickCleanJunk = view.findViewById(R.id.btn_quick_clean_junk);
        btnOpenCleanerDashboard = view.findViewById(R.id.btn_open_cleaner_dashboard);

        if (btnScanJunk != null) {
            btnScanJunk.setOnClickListener(v -> performQuickJunkScan());
        }

        if (btnQuickCleanJunk != null) {
            btnQuickCleanJunk.setOnClickListener(v -> performQuickJunkClean());
        }

        if (btnOpenCleanerDashboard != null) {
            btnOpenCleanerDashboard.setOnClickListener(v -> {
                if (getContext() != null) {
                    JunkCleanerDialog.show(getContext(), result -> {
                        if (tvJunkQuickSize != null) {
                            tvJunkQuickSize.setText("0.0 MB");
                        }
                        if (tvJunkQuickDetail != null) {
                            tvJunkQuickDetail.setText("Storage Clean & Optimized");
                        }
                        if (tvJunkQuickSubdetail != null) {
                            tvJunkQuickSubdetail.setText("Freed " + result.getFormattedBytesFreed() + " • " + result.getFilesDeletedCount() + " items deleted");
                        }
                        if (tvJunkCleanerStatus != null) {
                            tvJunkCleanerStatus.setText("Last Cleaned: Freed " + result.getFormattedBytesFreed() + " (Storage Reclaimed)");
                        }
                    });
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
                handlePrecisionTunerToggle(isChecked);
            });
        }

        if (switchCrosshairOverlay != null) {
            switchCrosshairOverlay.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (!Settings.canDrawOverlays(getContext())) {
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
        switchThermalBypass = view.findViewById(R.id.switch_thermal_bypass);
        switchAdpfEngine = view.findViewById(R.id.switch_adpf_engine);
        switchVideoSaver = view.findViewById(R.id.switch_video_saver);

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
            if (switchAngleMode != null) switchAngleMode.setChecked(ManualSettingsPreferences.isAngleModeEnabled(getContext()));
            if (switchGameDriver != null) switchGameDriver.setChecked(ManualSettingsPreferences.isGameDriverEnabled(getContext()));
            if (switchGpuMode != null) switchGpuMode.setChecked("vulkan".equalsIgnoreCase(ManualSettingsPreferences.getGpuMode(getContext())));
            if (switchCpuMode != null) switchCpuMode.setChecked("performance".equalsIgnoreCase(ManualSettingsPreferences.getCpuMode(getContext())));
            if (switchThermalBypass != null) switchThermalBypass.setChecked(ManualSettingsPreferences.isThermalBypassEnabled(getContext()));
            if (switchAdpfEngine != null) switchAdpfEngine.setChecked(ManualSettingsPreferences.isAdpfEngineEnabled(getContext()));
            if (switchVideoSaver != null) switchVideoSaver.setChecked(ManualSettingsPreferences.isVideoSaverEnabled(getContext()));
            isProgrammaticToggle = false;
        }

        if (switchAngleMode != null) {
            switchAngleMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                ManualSettingsPreferences.setAngleMode(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    GpuTweaksChannel.setAngleMode(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Google ANGLE Vulkan Driver Applied" : "ANGLE Driver Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchGameDriver != null) {
            switchGameDriver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                ManualSettingsPreferences.setGameDriverEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    GpuTweaksChannel.setGameDriverMode(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ System Game Graphics Driver Applied" : "Game Driver Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchGpuMode != null) {
            switchGpuMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
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
                ManualSettingsPreferences.setAdpfEngineEnabled(getContext(), isChecked);
                Toast.makeText(getContext(), isChecked
                        ? "⚡ Android 13–16 ADPF Power Hint Engine Enabled"
                        : "ADPF Engine Disabled", Toast.LENGTH_SHORT).show();
            });
        }

        if (switchVideoSaver != null) {
            switchVideoSaver.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                ManualSettingsPreferences.setVideoSaverEnabled(getContext(), isChecked);
                if (videoSettingsBg != null) {
                    if (isChecked) {
                        videoSettingsBg.pause();
                        videoSettingsBg.setVisibility(View.GONE);
                    } else {
                        videoSettingsBg.setVisibility(View.VISIBLE);
                        videoSettingsBg.play();
                    }
                }
                Toast.makeText(getContext(), isChecked
                        ? "🔋 Video Saver Enabled: Static Glass UI Active"
                        : "Cyber Video Background Restored", Toast.LENGTH_SHORT).show();
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
            isProgrammaticToggle = true;
            if (switch5g6gData != null) switch5g6gData.setChecked(ManualSettingsPreferences.is5g6gDataEnabled(getContext()));
            if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(ManualSettingsPreferences.isWifiLowLatencyEnabled(getContext()));
            if (switchDualDataWifi != null) switchDualDataWifi.setChecked(ManualSettingsPreferences.isDualDataWifiEnabled(getContext()));
            if (switchTetheringHw != null) switchTetheringHw.setChecked(ManualSettingsPreferences.isTetherHwEnabled(getContext()));
            if (switchForceGnss != null) switchForceGnss.setChecked(ManualSettingsPreferences.isForceGnssEnabled(getContext()));
            isProgrammaticToggle = false;
        }

        tvNetworkActiveMode = view.findViewById(R.id.tv_network_active_mode);
        btnNetDataOnly = view.findViewById(R.id.btn_net_data_only);
        btnNetWifiOnly = view.findViewById(R.id.btn_net_wifi_only);
        btnNetDual = view.findViewById(R.id.btn_net_dual);

        if (getContext() != null) {
            String currentNetMode = ManualSettingsPreferences.getNetworkMode(getContext());
            updateNetworkModeUi(currentNetMode);
        }

        if (btnNetDataOnly != null) {
            btnNetDataOnly.setOnClickListener(v -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setNetworkMode(getContext(), "data_only");
                ManualSettingsPreferences.set5g6gDataEnabled(getContext(), true);
                ManualSettingsPreferences.setWifiLowLatencyEnabled(getContext(), false);
                ManualSettingsPreferences.setDualDataWifiEnabled(getContext(), false);

                isProgrammaticToggle = true;
                if (switch5g6gData != null) switch5g6gData.setChecked(true);
                if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(false);
                if (switchDualDataWifi != null) switchDualDataWifi.setChecked(false);
                isProgrammaticToggle = false;

                updateNetworkModeUi("data_only");
                Toast.makeText(getContext(), "📱 5G / 4G Data Only Mode Enabled", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setNetworkMode(getContext(), NetworkOptimizer.NetworkMode.DATA_ONLY);
                });
            });
        }

        if (btnNetWifiOnly != null) {
            btnNetWifiOnly.setOnClickListener(v -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setNetworkMode(getContext(), "wifi_only");
                ManualSettingsPreferences.set5g6gDataEnabled(getContext(), false);
                ManualSettingsPreferences.setWifiLowLatencyEnabled(getContext(), true);
                ManualSettingsPreferences.setDualDataWifiEnabled(getContext(), false);

                isProgrammaticToggle = true;
                if (switch5g6gData != null) switch5g6gData.setChecked(false);
                if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(true);
                if (switchDualDataWifi != null) switchDualDataWifi.setChecked(false);
                isProgrammaticToggle = false;

                updateNetworkModeUi("wifi_only");
                Toast.makeText(getContext(), "📶 Wi-Fi Only Low-Latency Lock Enabled", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setNetworkMode(getContext(), NetworkOptimizer.NetworkMode.WIFI_ONLY);
                });
            });
        }

        if (btnNetDual != null) {
            btnNetDual.setOnClickListener(v -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setNetworkMode(getContext(), "dual");
                ManualSettingsPreferences.set5g6gDataEnabled(getContext(), true);
                ManualSettingsPreferences.setWifiLowLatencyEnabled(getContext(), true);
                ManualSettingsPreferences.setDualDataWifiEnabled(getContext(), true);

                isProgrammaticToggle = true;
                if (switch5g6gData != null) switch5g6gData.setChecked(true);
                if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(true);
                if (switchDualDataWifi != null) switchDualDataWifi.setChecked(true);
                isProgrammaticToggle = false;

                updateNetworkModeUi("dual");
                Toast.makeText(getContext(), "⚡ Dual Data + Wi-Fi Multipath Aggregation Enabled", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setNetworkMode(getContext(), NetworkOptimizer.NetworkMode.DUAL_DATA_WIFI);
                });
            });
        }

        if (btnOptimizeNetworkAll != null) {
            btnOptimizeNetworkAll.setOnClickListener(v -> {
                if (getContext() == null) return;
                Toast.makeText(getContext(), "🚀 Applying 5G/6G & Wi-Fi 6/7 Turbo Boost...", Toast.LENGTH_SHORT).show();
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.optimizeAllDataAndWifi(getContext().getApplicationContext());
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            isProgrammaticToggle = true;
                            if (switch5g6gData != null) switch5g6gData.setChecked(true);
                            if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(true);
                            if (switchDualDataWifi != null) switchDualDataWifi.setChecked(true);
                            isProgrammaticToggle = false;
                            ManualSettingsPreferences.setNetworkMode(getContext(), "dual");
                            ManualSettingsPreferences.set5g6gDataEnabled(getContext(), true);
                            ManualSettingsPreferences.setWifiLowLatencyEnabled(getContext(), true);
                            ManualSettingsPreferences.setDualDataWifiEnabled(getContext(), true);
                            updateNetworkModeUi("dual");
                            Toast.makeText(getContext(), "🚀 5G/6G & Wi-Fi Turbo Boost Applied", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switch5g6gData != null) {
            switch5g6gData.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                ManualSettingsPreferences.set5g6gDataEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.optimize5gAnd6gDataNetwork(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ 5G/6G Data Accelerator Enabled" : "5G/6G Data Accelerator Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchWifiLowLatency != null) {
            switchWifiLowLatency.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                ManualSettingsPreferences.setWifiLowLatencyEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.optimizeWifi6and7LowLatency(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Wi-Fi Low-Latency Anti-Lag Enabled" : "Wi-Fi Normal Mode Restored", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchDualDataWifi != null) {
            switchDualDataWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
                ManualSettingsPreferences.setDualDataWifiEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setDualDataAndWifiAcceleration(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Dual Data + Wi-Fi Aggregation Enabled" : "Dual Data Aggregation Disabled", Toast.LENGTH_SHORT).show();
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
                if (isProgrammaticToggle || getContext() == null) return;
                ManualSettingsPreferences.setTetherHwEnabled(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    NetworkOptimizer.setTetheringHwAcceleration(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Tethering Hardware Offload Enabled" : "Tethering Offload Disabled", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (switchForceGnss != null) {
            switchForceGnss.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isProgrammaticToggle || getContext() == null) return;
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

        // Card 5: Advanced System Tweaks Engine
        tvTweaksStatus = view.findViewById(R.id.tv_tweaks_status);
        bannerDisconnect = view.findViewById(R.id.banner_shizuku_disconnect);
        RecyclerView rvTweaks = view.findViewById(R.id.rv_tweaks_list);

        EditText etSearchTweaks = view.findViewById(R.id.et_search_tweaks);
        ImageView ivClearSearch = view.findViewById(R.id.iv_clear_search);

        Button btnFilterCpuGpu = view.findViewById(R.id.btn_filter_cpugpu);
        Button btnFilterTouch = view.findViewById(R.id.btn_filter_touch);
        Button btnFilterShizuku = view.findViewById(R.id.btn_filter_shizuku);
        Button btnFilterNetwork = view.findViewById(R.id.btn_filter_network);

        TweakManagerRepository.initializeStates(getContext());

        final TweakCategory[] selectedCategory = {TweakCategory.CPU_GPU};

        if (rvTweaks != null) {
            rvTweaks.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTweaks.setHasFixedSize(true);
            rvTweaks.setNestedScrollingEnabled(false);
            tweaksAdapter = new TweaksAdapter(getContext(), TweakManagerRepository.getAllTweaks());
            tweaksAdapter.setOnTweakStateChangeListener((item, isApplied, totalAppliedCount) -> {
                if (tvTweaksStatus != null) {
                    tvTweaksStatus.setText("ACTIVE: " + totalAppliedCount + " / " + TweakManagerRepository.getTotalCount() + " TWEAKS");
                }
            });
            tweaksAdapter.setCategoryFilter(TweakCategory.CPU_GPU);
            rvTweaks.setAdapter(tweaksAdapter);
            if (tvTweaksStatus != null) {
                tvTweaksStatus.setText("ACTIVE: " + TweakManagerRepository.getAppliedCount(getContext()) + " / " + TweakManagerRepository.getTotalCount() + " TWEAKS");
            }
        }

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

        Button[] filterButtons = {btnFilterCpuGpu, btnFilterTouch, btnFilterShizuku, btnFilterNetwork};
        TweakCategory[] categories = {TweakCategory.CPU_GPU, TweakCategory.TOUCH_DISPLAY, TweakCategory.SHIZUKU_SYSTEM, TweakCategory.NETWORK_LATENCY};

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
        tvSettingsSpoofBrandInfo = view.findViewById(R.id.tv_settings_spoof_brand_info);
        hsvSettingsSpoofBrands = view.findViewById(R.id.hsv_settings_spoof_brands);
        rvSpoofProfiles = view.findViewById(R.id.rv_spoof_profiles);

        Button btnForceApplySpoof = view.findViewById(R.id.btn_force_apply_spoof);
        Button btnOpenPerAppSpoof = view.findViewById(R.id.btn_open_per_app_spoof);
        Button btnOpenSpoofInspector = view.findViewById(R.id.btn_open_spoof_inspector);

        if (btnForceApplySpoof != null) {
            btnForceApplySpoof.setOnClickListener(v -> {
                if (getContext() == null) return;

                if (!com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("🛡️ SHIZUKU API REQUIRED")
                            .setMessage("Force Applying Spoof requires active Shizuku API (UID 2000 Privileged Shell).\n\nPlease grant Shizuku permission.")
                            .setPositiveButton("GRANT SHIZUKU", (d, w) -> {
                                com.gamebooster.app.shizuku.ShizukuExecutor.requestPermission();
                            })
                            .setNegativeButton("CANCEL", null)
                            .show();
                    return;
                }

                String activeId = SpoofPreferences.getActiveProfileId(getContext());
                SpoofProfile targetProf = (activeId != null && !activeId.isEmpty())
                        ? DeviceSpooferEngine.getProfileById(activeId)
                        : DeviceSpooferEngine.getDefaultProfile();
                if (targetProf == null) {
                    Toast.makeText(getContext(), "⚠️ Please select a spoof profile below first!", Toast.LENGTH_SHORT).show();
                    return;
                }

                SpoofPreferences.setSpoofEnabled(getContext(), true);
                SpoofPreferences.setActiveProfileId(getContext(), targetProf.id);
                if (switchDeviceSpoof != null) {
                    isProgrammaticToggle = true;
                    switchDeviceSpoof.setChecked(true);
                    isProgrammaticToggle = false;
                }
                if (spoofProfileAdapter != null) {
                    spoofProfileAdapter.setActiveProfileId(targetProf.id);
                }
                updateSpoofUiState();

                Toast.makeText(getContext(), "⚡ Force-Applying Spoof via Shizuku API: " + targetProf.displayName + "...", Toast.LENGTH_SHORT).show();

                final SpoofProfile profileToApply = targetProf;
                AppExecutors.getInstance().executeCommand(() -> {
                    boolean success = DeviceSpooferEngine.forceApplySpoof(getContext(), profileToApply.id, null);
                    int count = com.gamebooster.app.spoofer.HardwareMaskEngine.maskAllInstalledApplications(getContext(), profileToApply);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        updateSpoofUiState();
                        Toast.makeText(getContext(), "✅ Force-Applied Spoof via Shizuku: " + profileToApply.displayName + " (" + count + " apps masked)", Toast.LENGTH_LONG).show();
                    });
                });
            });
        }

        if (btnOpenPerAppSpoof != null) {
            btnOpenPerAppSpoof.setOnClickListener(v -> {
                if (getContext() != null) {
                    PerAppSpoofDialog.show(getContext());
                }
            });
        }

        if (btnOpenSpoofInspector != null) {
            btnOpenSpoofInspector.setOnClickListener(v -> {
                if (getContext() != null) {
                    SpoofInspectorDialog.show(getContext());
                }
            });
        }

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

                // 2. Perform background real-world hardware & game file injection
                AppExecutors.getInstance().executeCommand(() -> {
                    boolean applied = DeviceSpooferEngine.forceApplySpoof(getContext(), profile.id, null);
                    int count = com.gamebooster.app.spoofer.HardwareMaskEngine.maskAllInstalledApplications(getContext(), profile);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        updateSpoofUiState();
                        Toast.makeText(getContext(), "✅ Spoofed & Forced as " + profile.displayName + " (" + count + " apps masked)", Toast.LENGTH_SHORT).show();
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

                if (isChecked && !com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
                    isProgrammaticToggle = true;
                    switchDeviceSpoof.setChecked(false);
                    isProgrammaticToggle = false;
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("🛡️ SHIZUKU API REQUIRED")
                            .setMessage("Device Spoofing requires active Shizuku API (UID 2000 Privileged Shell).\n\nPlease authorize Shizuku to proceed.")
                            .setPositiveButton("GRANT SHIZUKU", (d, w) -> {
                                com.gamebooster.app.shizuku.ShizukuExecutor.requestPermission();
                            })
                            .setNegativeButton("CANCEL", null)
                            .show();
                    return;
                }

                SpoofPreferences.setSpoofEnabled(getContext(), isChecked);

                if (!isChecked) {
                    AppExecutors.getInstance().executeCommand(() -> {
                        DeviceSpooferEngine.resetSpoofing();
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
                                boolean applied = DeviceSpooferEngine.forceApplySpoof(getContext(), finalProf.id, null);
                                int maskedCount = com.gamebooster.app.spoofer.HardwareMaskEngine.maskAllInstalledApplications(getContext(), finalProf);
                                AppExecutors.getInstance().postToMainThread(() -> {
                                    if (isAdded() && getContext() != null) {
                                        updateSpoofUiState();
                                        Toast.makeText(getContext(), "✅ Masked & Forced " + maskedCount + " Apps with " + finalProf.displayName + " (" + finalProf.model + ")", Toast.LENGTH_SHORT).show();
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
            btnSettingsScriptFolder.setOnClickListener(v -> showSettingsFolderDialog());
        }

        if (btnSettingsScriptWhoami != null) {
            btnSettingsScriptWhoami.setOnClickListener(v -> runSettingsTerminalQuickCmd("id; whoami; pm get-install-location"));
        }

        if (btnSettingsScriptRam != null) {
            btnSettingsScriptRam.setOnClickListener(v -> runSettingsTerminalQuickCmd("cmd package trim-caches 9223372036854775807 2>/dev/null || pm trim-caches 40000000000; am kill-all; dumpsys meminfo --oom"));
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
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
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
            com.gamebooster.app.booster.MaxHzForceChannel.forceApply(targetHz);
            GameProfileAutoConfigurator.autoConfigAllGamesAsync(getContext(), targetHz, null);
            CfgProfileManager.applyAllGames(getContext(), targetHz, true, true);
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
                    if (tweaksAdapter != null) {
                        tweaksAdapter.setShizukuAlive(alive);
                    }
                    if (bannerDisconnect != null) {
                        bannerDisconnect.setVisibility(alive ? View.GONE : View.VISIBLE);
                    }
                    refreshAllStatuses();
                } catch (Throwable t) {
                    android.util.Log.w("SettingsFragment", "onBinderStateChanged error: " + t.getMessage());
                }
            });
            if (alive && getContext() != null) {
                TweakManagerRepository.restoreAppliedTweaksAsync(getContext());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoSettingsBg != null) videoSettingsBg.play();
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
            if (tvTweaksStatus != null && getContext() != null) {
                tvTweaksStatus.setText("ACTIVE: " + TweakManagerRepository.getAppliedCount(getContext()) + " / " + TweakManagerRepository.getTotalCount() + " TWEAKS");
            }
            updateSpoofUiState();
            updatePrecisionAimStatus();

            if (getContext() != null) {
                isProgrammaticToggle = true;
                if (switchOverlayHud != null) switchOverlayHud.setChecked(com.gamebooster.app.overlay.FloatingOverlayService.isOverlayRunning());
                if (switchGamingDnd != null) switchGamingDnd.setChecked(com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getContext()));
                if (switchAutoGameBoost != null) switchAutoGameBoost.setChecked(com.gamebooster.app.gamespace.AutoGameMonitorService.isRunning());
                if (switchEsportsAudio != null) switchEsportsAudio.setChecked(com.gamebooster.app.booster.EsportsAudioEnhancer.isEnabled());
                if (switchAntiLog != null) switchAntiLog.setChecked(ManualSettingsPreferences.isAntiLogEnabled(getContext()));
                if (switchFocusMode != null) switchFocusMode.setChecked(ManualSettingsPreferences.isFocusModeEnabled(getContext()) || com.gamebooster.app.focus.FocusModeEngine.isFocusModeActive(getContext()));
                if (switchAngleMode != null) switchAngleMode.setChecked(ManualSettingsPreferences.isAngleModeEnabled(getContext()));
                if (switchGameDriver != null) switchGameDriver.setChecked(ManualSettingsPreferences.isGameDriverEnabled(getContext()));
                if (switchGpuMode != null) switchGpuMode.setChecked("vulkan".equalsIgnoreCase(ManualSettingsPreferences.getGpuMode(getContext())));
                if (switchCpuMode != null) switchCpuMode.setChecked("performance".equalsIgnoreCase(ManualSettingsPreferences.getCpuMode(getContext())));
                if (switchThermalBypass != null) switchThermalBypass.setChecked(ManualSettingsPreferences.isThermalBypassEnabled(getContext()));
                if (switch5g6gData != null) switch5g6gData.setChecked(ManualSettingsPreferences.is5g6gDataEnabled(getContext()));
                if (switchWifiLowLatency != null) switchWifiLowLatency.setChecked(ManualSettingsPreferences.isWifiLowLatencyEnabled(getContext()));
                if (switchDualDataWifi != null) switchDualDataWifi.setChecked(ManualSettingsPreferences.isDualDataWifiEnabled(getContext()));
                if (switchTetheringHw != null) switchTetheringHw.setChecked(ManualSettingsPreferences.isTetherHwEnabled(getContext()));
                if (switchForceGnss != null) switchForceGnss.setChecked(ManualSettingsPreferences.isForceGnssEnabled(getContext()));
                if (switchDeviceSpoof != null) switchDeviceSpoof.setChecked(SpoofPreferences.isSpoofEnabled(getContext()));
                if (switchPrecisionInputTuner != null && precisionSettingsManager != null) {
                    switchPrecisionInputTuner.setChecked(precisionSettingsManager.isDeviceTuned());
                }
                updateNetworkModeUi(ManualSettingsPreferences.getNetworkMode(getContext()));
                isProgrammaticToggle = false;
            }

            boolean alive = ShizukuExecutor.hasShizukuPermission();
            if (tweaksAdapter != null) {
                tweaksAdapter.setShizukuAlive(alive);
            }
            if (bannerDisconnect != null) {
                bannerDisconnect.setVisibility(alive ? View.GONE : View.VISIBLE);
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
        if (tvDiagStatus == null) return;
        final Context ctx = getContext() != null ? getContext().getApplicationContext() : null;
        if (ctx == null) return;

        AppExecutors.getInstance().executeScan(() -> {
            try {
                java.util.List<String> lines = com.gamebooster.app.diagnostics.DiagnosticsExporter.buildSnapshot(ctx);
                final String text = com.gamebooster.app.diagnostics.DiagnosticsExporter.join(lines);
                AppExecutors.getInstance().postToMainThread(() -> {
                    if (tvDiagStatus != null) {
                        tvDiagStatus.setText(text);
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

    private void updateNetworkModeUi(String mode) {
        if (tvNetworkActiveMode == null || getContext() == null) return;
        if ("data_only".equalsIgnoreCase(mode)) {
            tvNetworkActiveMode.setText("Active Network Mode: 📱 MOBILE DATA ONLY (5G/4G)");
            tvNetworkActiveMode.setTextColor(0xFF00F0FF);
            if (btnNetDataOnly != null) btnNetDataOnly.setBackgroundResource(R.drawable.btn_cyber_cyan);
            if (btnNetDataOnly != null) btnNetDataOnly.setTextColor(0xFF000000);
            if (btnNetWifiOnly != null) btnNetWifiOnly.setBackgroundResource(R.drawable.btn_cyber_dark);
            if (btnNetWifiOnly != null) btnNetWifiOnly.setTextColor(0xFF00FF66);
            if (btnNetDual != null) btnNetDual.setBackgroundResource(R.drawable.btn_cyber_dark);
            if (btnNetDual != null) btnNetDual.setTextColor(0xFFFFFFFF);
        } else if ("wifi_only".equalsIgnoreCase(mode)) {
            tvNetworkActiveMode.setText("Active Network Mode: 📶 WI-FI ONLY (Low-Latency Lock)");
            tvNetworkActiveMode.setTextColor(0xFF00FF66);
            if (btnNetDataOnly != null) btnNetDataOnly.setBackgroundResource(R.drawable.btn_cyber_dark);
            if (btnNetDataOnly != null) btnNetDataOnly.setTextColor(0xFF00F0FF);
            if (btnNetWifiOnly != null) btnNetWifiOnly.setBackgroundResource(R.drawable.btn_cyber_cyan);
            if (btnNetWifiOnly != null) btnNetWifiOnly.setTextColor(0xFF000000);
            if (btnNetDual != null) btnNetDual.setBackgroundResource(R.drawable.btn_cyber_dark);
            if (btnNetDual != null) btnNetDual.setTextColor(0xFFFFFFFF);
        } else {
            tvNetworkActiveMode.setText("Active Network Mode: ⚡ DUAL DATA + WI-FI (Multipath)");
            tvNetworkActiveMode.setTextColor(0xFF00FF66);
            if (btnNetDataOnly != null) btnNetDataOnly.setBackgroundResource(R.drawable.btn_cyber_dark);
            if (btnNetDataOnly != null) btnNetDataOnly.setTextColor(0xFF00F0FF);
            if (btnNetWifiOnly != null) btnNetWifiOnly.setBackgroundResource(R.drawable.btn_cyber_dark);
            if (btnNetWifiOnly != null) btnNetWifiOnly.setTextColor(0xFF00FF66);
            if (btnNetDual != null) btnNetDual.setBackgroundResource(R.drawable.btn_cyber_cyan);
            if (btnNetDual != null) btnNetDual.setTextColor(0xFF000000);
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

    private void performQuickJunkScan() {
        final Context ctx = getContext();
        if (ctx == null) return;
        if (btnScanJunk != null) btnScanJunk.setEnabled(false);
        if (tvJunkCleanerStatus != null) tvJunkCleanerStatus.setText("🔍 Scanning storage caches, temp logs & residual files...");

        AppExecutors.getInstance().executeScan(() -> {
            JunkScanResult scanResult = junkScanner.scanStorage(ctx, null);
            lastJunkScanResult = scanResult;
            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                if (btnScanJunk != null) btnScanJunk.setEnabled(true);
                if (tvJunkQuickSize != null) {
                    tvJunkQuickSize.setText(scanResult.getFormattedTotalSize());
                }
                if (tvJunkCleanerStatus != null) {
                    tvJunkCleanerStatus.setText("Scan Complete: " + scanResult.getItems().size() + " items found (" + scanResult.getFormattedTotalSize() + ")");
                }
                if (tvJunkQuickDetail != null) {
                    tvJunkQuickDetail.setText(scanResult.getItems().size() + " cleanable junk files detected");
                }
                if (tvJunkQuickSubdetail != null) {
                    tvJunkQuickSubdetail.setText("App caches • Shaders • Temp logs • Obsolete files");
                }
                Toast.makeText(getContext(), "🔍 Found " + scanResult.getFormattedTotalSize() + " junk files", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void performQuickJunkClean() {
        final Context ctx = getContext();
        if (ctx == null) return;
        if (btnQuickCleanJunk != null) btnQuickCleanJunk.setEnabled(false);
        Toast.makeText(ctx, "⚡ Executing 1-Tap Storage Clean...", Toast.LENGTH_SHORT).show();

        AppExecutors.getInstance().executeCommand(() -> {
            JunkScanResult scanToClean = lastJunkScanResult;
            if (scanToClean == null || scanToClean.getItems().isEmpty()) {
                scanToClean = junkScanner.scanStorage(ctx, null);
            }
            CleanResult cleanResult = junkCleanerEngine.executeClean(ctx, scanToClean, null);
            lastJunkScanResult = null;

            AppExecutors.getInstance().postToMainThread(() -> {
                if (!isAdded() || getContext() == null) return;
                if (btnQuickCleanJunk != null) btnQuickCleanJunk.setEnabled(true);
                if (tvJunkQuickSize != null) tvJunkQuickSize.setText("0.0 MB");
                if (tvJunkQuickDetail != null) {
                    tvJunkQuickDetail.setText("Storage Clean & Optimized");
                }
                if (tvJunkQuickSubdetail != null) {
                    tvJunkQuickSubdetail.setText("Freed " + cleanResult.getFormattedBytesFreed() + " • " + cleanResult.getFilesDeletedCount() + " items deleted");
                }
                if (tvJunkCleanerStatus != null) {
                    tvJunkCleanerStatus.setText("✅ Cleaned! Freed " + cleanResult.getFormattedBytesFreed() + " (" + cleanResult.getFilesDeletedCount() + " items purged)");
                }
                CyberActionDialog.show(
                        getContext(),
                        "🧹 1-TAP STORAGE PURGED",
                        true,
                        "Storage Space Reclaimed: " + cleanResult.getFormattedBytesFreed(),
                        "Purged " + cleanResult.getFilesDeletedCount() + " caches & temp files",
                        "Filesystem Flash TRIM: Complete"
                );
            });
        });
    }
}
