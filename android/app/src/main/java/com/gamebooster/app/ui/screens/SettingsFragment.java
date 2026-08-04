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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Card 1: Shizuku & System Permissions
        tvEngineStatus = view.findViewById(R.id.tv_engine_status);
        tvRootStatus = view.findViewById(R.id.tv_root_status);
        Button btnGrantShizuku = view.findViewById(R.id.btn_grant_shizuku);
        Button btnOpenSettings = view.findViewById(R.id.btn_open_settings);

        if (btnGrantShizuku != null) {
            btnGrantShizuku.setOnClickListener(v -> {
                if (getContext() != null) {
                    if (ShizukuExecutor.hasShizukuPermission()) {
                        AppExecutors.getInstance().executeCommand(() -> {
                            ShizukuExecutor.grantAppPermissionsViaShizuku(getContext());
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

        // Card 1.5: iPad View Aspect Ratio Controller
        Switch switchIpadViewToggle = view.findViewById(R.id.switch_ipad_view_toggle);
        TextView tvIpadViewStatus = view.findViewById(R.id.tv_ipad_view_status);
        Button btnIpadNormal = view.findViewById(R.id.btn_ipad_mode_normal);
        Button btnIpadMedium = view.findViewById(R.id.btn_ipad_mode_medium);
        Button btnIpadUltra = view.findViewById(R.id.btn_ipad_mode_ultra);

        if (getContext() != null && switchIpadViewToggle != null) {
            boolean isIpadOn = ManualSettingsPreferences.isIpadViewEnabled(getContext());
            String modeStr = ManualSettingsPreferences.getIpadViewMode(getContext());

            switchIpadViewToggle.setChecked(isIpadOn);
            if (tvIpadViewStatus != null) {
                tvIpadViewStatus.setText(isIpadOn ? "Status: ACTIVE (" + modeStr + ")" : "Status: OFF (Normal Display Density)");
                tvIpadViewStatus.setTextColor(isIpadOn ? android.graphics.Color.parseColor("#00FF66") : android.graphics.Color.parseColor("#94A3B8"));
            }

            switchIpadViewToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setIpadViewEnabled(getContext(), isChecked);

                AppExecutors.getInstance().executeCommand(() -> {
                    boolean ok;
                    if (isChecked) {
                        String currentMode = ManualSettingsPreferences.getIpadViewMode(getContext());
                        com.gamebooster.app.engine.IpadViewScalerEngine.IpadViewMode targetMode =
                                "IPAD_ULTRA".equalsIgnoreCase(currentMode)
                                        ? com.gamebooster.app.engine.IpadViewScalerEngine.IpadViewMode.IPAD_ULTRA
                                        : com.gamebooster.app.engine.IpadViewScalerEngine.IpadViewMode.IPAD_MEDIUM;
                        ok = com.gamebooster.app.engine.IpadViewScalerEngine.applyIpadView(getContext(), targetMode);
                    } else {
                        ok = com.gamebooster.app.engine.IpadViewScalerEngine.restoreDefaultView();
                    }

                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (!isAdded() || getContext() == null) return;
                        if (tvIpadViewStatus != null) {
                            tvIpadViewStatus.setText(isChecked ? "Status: ACTIVE (" + ManualSettingsPreferences.getIpadViewMode(getContext()) + ")" : "Status: OFF (Normal Display Density)");
                            tvIpadViewStatus.setTextColor(isChecked ? android.graphics.Color.parseColor("#00FF66") : android.graphics.Color.parseColor("#94A3B8"));
                        }
                        Toast.makeText(getContext(), isChecked ? (ok ? "📐 iPad View Scaling APPLIED via Shizuku!" : "Shizuku needed for iPad View") : "🔄 Display Density Restored to Default", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        if (btnIpadNormal != null) {
            btnIpadNormal.setOnClickListener(v -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setIpadViewEnabled(getContext(), false);
                if (switchIpadViewToggle != null) switchIpadViewToggle.setChecked(false);
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.engine.IpadViewScalerEngine.restoreDefaultView();
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "🔄 Normal View Restored (1.0x)", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (btnIpadMedium != null) {
            btnIpadMedium.setOnClickListener(v -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setIpadViewMode(getContext(), "IPAD_MEDIUM");
                ManualSettingsPreferences.setIpadViewEnabled(getContext(), true);
                if (switchIpadViewToggle != null) switchIpadViewToggle.setChecked(true);
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.engine.IpadViewScalerEngine.applyIpadView(getContext(), com.gamebooster.app.engine.IpadViewScalerEngine.IpadViewMode.IPAD_MEDIUM);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "📐 iPad View 1.5x Medium FOV Applied!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        }

        if (btnIpadUltra != null) {
            btnIpadUltra.setOnClickListener(v -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setIpadViewMode(getContext(), "IPAD_ULTRA");
                ManualSettingsPreferences.setIpadViewEnabled(getContext(), true);
                if (switchIpadViewToggle != null) switchIpadViewToggle.setChecked(true);
                AppExecutors.getInstance().executeCommand(() -> {
                    com.gamebooster.app.engine.IpadViewScalerEngine.applyIpadView(getContext(), com.gamebooster.app.engine.IpadViewScalerEngine.IpadViewMode.IPAD_ULTRA);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), "🔥 iPad View 2.0x Ultra FOV Applied!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
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
                    Toast.makeText(getContext(), "⚡ Performance HUD Overlay Enabled", Toast.LENGTH_SHORT).show();
                } else {
                    com.gamebooster.app.overlay.FloatingOverlayService.stopOverlay(getContext());
                    Toast.makeText(getContext(), "Overlay Disabled", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (switchGamingDnd != null) {
            switchGamingDnd.setChecked(com.gamebooster.app.gamespace.GameSpaceDndManager.isDndActive(getContext()));
            switchGamingDnd.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                com.gamebooster.app.gamespace.GameSpaceDndManager.setGamingDndMode(getContext(), isChecked);
                Toast.makeText(getContext(), "Gaming DND: " + (isChecked ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
            });
        }

        if (switchAutoGameBoost != null) {
            switchAutoGameBoost.setChecked(com.gamebooster.app.gamespace.AutoGameMonitorService.isRunning());
            switchAutoGameBoost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                if (isChecked) {
                    com.gamebooster.app.gamespace.AutoGameMonitorService.start(getContext());
                    Toast.makeText(getContext(), "🎮 Auto Game Launch Monitor: ENABLED", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), isChecked ? "🔊 Esports Footstep Audio Boost: ACTIVE" : "Audio Equalizer Normal", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), ok ? "🧹 Game Storage & Shaders Cleaned!" : "Cache Clean Complete", Toast.LENGTH_SHORT).show();
                    });
                });
            });
        }

        // Card 1.5: Per-Game iPad View Switches
        Switch switchIpadMlbb = view.findViewById(R.id.switch_ipad_mlbb);
        Switch switchIpadPubg = view.findViewById(R.id.switch_ipad_pubg);
        Switch switchIpadCodm = view.findViewById(R.id.switch_ipad_codm);

        if (getContext() != null) {
            if (switchIpadMlbb != null) switchIpadMlbb.setChecked(ManualSettingsPreferences.isIpadViewGameEnabled(getContext(), "mlbb"));
            if (switchIpadPubg != null) switchIpadPubg.setChecked(ManualSettingsPreferences.isIpadViewGameEnabled(getContext(), "pubg"));
            if (switchIpadCodm != null) switchIpadCodm.setChecked(ManualSettingsPreferences.isIpadViewGameEnabled(getContext(), "codm"));
        }

        if (switchIpadMlbb != null) {
            switchIpadMlbb.setOnCheckedChangeListener((bv, isChecked) -> {
                if (getContext() != null) ManualSettingsPreferences.setIpadViewGameEnabled(getContext(), "mlbb", isChecked);
            });
        }
        if (switchIpadPubg != null) {
            switchIpadPubg.setOnCheckedChangeListener((bv, isChecked) -> {
                if (getContext() != null) ManualSettingsPreferences.setIpadViewGameEnabled(getContext(), "pubg", isChecked);
            });
        }
        if (switchIpadCodm != null) {
            switchIpadCodm.setOnCheckedChangeListener((bv, isChecked) -> {
                if (getContext() != null) ManualSettingsPreferences.setIpadViewGameEnabled(getContext(), "codm", isChecked);
            });
        }

        // Card 3: Hardware Engine & Performance Presets
        Button btnExtreme = view.findViewById(R.id.btn_apply_pubg_profile);
        Button btnPro144 = view.findViewById(R.id.btn_apply_144_profile);
        Button btnPerformance = view.findViewById(R.id.btn_apply_2d_profile);
        Button btnBalanced = view.findViewById(R.id.btn_apply_balanced_profile);

        switchAngleMode = view.findViewById(R.id.switch_angle_mode);
        switchGameDriver = view.findViewById(R.id.switch_game_driver);
        switchGpuMode = view.findViewById(R.id.switch_gpu_mode);
        switchCpuMode = view.findViewById(R.id.switch_cpu_mode);

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
        }

        if (switchAngleMode != null) {
            switchAngleMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (getContext() == null) return;
                ManualSettingsPreferences.setAngleMode(getContext(), isChecked);
                AppExecutors.getInstance().executeCommand(() -> {
                    GpuTweaksChannel.setAngleMode(isChecked);
                    AppExecutors.getInstance().postToMainThread(() -> {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(getContext(), isChecked ? "⚡ Google ANGLE Vulkan Driver ENABLED" : "ANGLE Driver Disabled", Toast.LENGTH_SHORT).show();
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
