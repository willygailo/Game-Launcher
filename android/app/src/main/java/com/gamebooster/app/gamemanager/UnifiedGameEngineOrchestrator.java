package com.gamebooster.app.gamemanager;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.booster.CpuGovernorChannel;
import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.booster.RamZramChannel;
import com.gamebooster.app.booster.ThermalChannel;
import com.gamebooster.app.booster.TouchLatencyChannel;
import com.gamebooster.app.config.ConfigBackupManager;
import com.gamebooster.app.config.GameConfigStorageAccessEngine;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.GameSessionSettings;
import com.gamebooster.app.config.NativeConfigInjector;
import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.engine.GameModeApiSupport;
import com.gamebooster.app.engine.NativeFrameworkBridge;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.gamespace.GameStateReverter;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.spoofer.DeviceIdentityGenerator;
import com.gamebooster.app.spoofer.GameSpoofSafetyRegistry;
import com.gamebooster.app.spoofer.HardwareMaskEngine;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofProfileRegistry;
import com.gamebooster.app.spoofer.SpoofSanityChecker;
import com.gamebooster.app.tweaks.TweakCategory;
import com.gamebooster.app.tweaks.TweakItem;
import com.gamebooster.app.tweaks.TweakManagerRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * UnifiedGameEngineOrchestrator — Master Transactional Hardware & Engine Coordinator.
 *
 * Harmonizes and coordinates all four core subsystems:
 *  1. tweaks  → 90+ SurfaceFlinger, kernel, and I/O system properties.
 *  2. booster → 14 hardware performance channels (CPU, GPU, 185Hz display, touch, thermal).
 *  3. cpp     → Sub-millisecond atomic memory-mapped file writes & direct Linux scheduling syscalls.
 *  4. spoofer → 6-layer device identity & game engine hardware profiling.
 */
public final class UnifiedGameEngineOrchestrator {

    private static final String TAG = "UnifiedOrchestrator";

    public enum SessionPreset {
        ESPORTS_TOURNAMENT("Esports Tournament Overdrive", 185, true, true),
        ULTRA_FRAMERATE("Ultra Framerate Smoothness", 165, true, false),
        BALANCED_PERFORMANCE("Balanced High Performance", 120, false, false);

        public final String title;
        public final int defaultHz;
        public final boolean extremeGovernor;
        public final boolean zeroLatencyVsync;

        SessionPreset(String title, int defaultHz, boolean extremeGovernor, boolean zeroLatencyVsync) {
            this.title = title;
            this.defaultHz = defaultHz;
            this.extremeGovernor = extremeGovernor;
            this.zeroLatencyVsync = zeroLatencyVsync;
        }
    }

    public interface SessionCallback {
        void onSessionReady(String packageName, int appliedHz, String message);
        void onError(String packageName, Throwable error);
    }

    private static final AtomicBoolean sSessionActive = new AtomicBoolean(false);
    private static volatile String sCurrentPackage = null;
    private static volatile SessionPreset sCurrentPreset = null;

    private UnifiedGameEngineOrchestrator() {}

    /**
     * Returns whether an optimized session is currently active.
     */
    public static boolean isSessionActive() {
        return sSessionActive.get();
    }

    /**
     * Returns current active package name or null.
     */
    public static String getCurrentPackage() {
        return sCurrentPackage;
    }

    /**
     * Launches an atomic, synchronized optimization session for the target game.
     */
    public static void beginSession(Context context, String packageName, SessionPreset preset, SessionCallback callback) {
        if (packageName == null || packageName.trim().isEmpty()) {
            if (callback != null) callback.onError(packageName, new IllegalArgumentException("Package name is empty"));
            return;
        }

        final Context appContext = context != null ? context.getApplicationContext() : null;
        final String pkg = packageName.trim();
        final SessionPreset targetPreset = preset != null ? preset : SessionPreset.ESPORTS_TOURNAMENT;

        AppExecutors.getInstance().executeCommand(() -> {
            try {
                Log.i(TAG, "═══════════════════════════════════════════════════════════════════");
                Log.i(TAG, "⚡ [ORCHESTRATOR] Starting Session for: " + pkg + " [" + targetPreset.title + "]");
                Log.i(TAG, "═══════════════════════════════════════════════════════════════════");

                sSessionActive.set(true);
                sCurrentPackage = pkg;
                sCurrentPreset = targetPreset;

                // ── Step 1: Pre-Flight Safety & Storage Scaffolding ──────────
                GameSpoofSafetyRegistry.RiskTier riskTier = GameSpoofSafetyRegistry.riskTierFor(pkg);
                Log.i(TAG, "▶ Step 1: Safety Audit Tier: " + riskTier);

                if (appContext != null) {
                    GameSessionSettings.begin(appContext, pkg);
                    GameConfigStorageAccessEngine.grantAllPathsAccess(appContext, pkg);
                    ConfigBackupManager.backupAllPaths(pkg);
                }

                int targetHz = (appContext != null) ? GameProfilePreferences.getTargetHz(appContext, pkg) : targetPreset.defaultHz;
                if (targetHz <= 0) targetHz = targetPreset.defaultHz;

                // ── Step 2: Spoofer Hardware Masking & Tailored Engine Injection ──
                SpoofProfile activeProfile = null;
                if (appContext != null) {
                    String activeId = com.gamebooster.app.spoofer.SpoofPreferences.getActiveProfileId(appContext);
                    if (activeId != null && !activeId.isEmpty()) {
                        activeProfile = SpoofProfileRegistry.getById(activeId);
                    }
                }
                if (activeProfile == null) {
                    activeProfile = SpoofProfileRegistry.getById("asus_rog8_pro");
                }
                if (activeProfile != null) {
                    Log.i(TAG, "▶ Step 2: Applying Hardware Masking via Spoofer: " + activeProfile.displayName);
                    HardwareMaskEngine.applyFullHardwareMask(appContext, activeProfile, pkg, riskTier);
                }

                // ── Step 3: Booster Hardware Channels & Display Refresh Rate ──
                Log.i(TAG, "▶ Step 3: Elevating Booster Hardware Channels @ " + targetHz + "Hz");
                MaxHzForceChannel.forceApply(targetHz);
                if (appContext != null) {
                    HzFpsChannel.forceSetRefreshRate(appContext, targetHz);
                }

                // CPU & GPU Performance Governors
                if (targetPreset.extremeGovernor) {
                    CpuGovernorChannel.setGovernor("performance");
                    GpuTweaksChannel.setGpuMaxPerformance();
                } else {
                    CpuGovernorChannel.setGovernor("schedutil");
                }
                GpuTweaksChannel.setGameDriverMode(true);
                TouchLatencyChannel.enableUltraTouchResponse();
                ThermalChannel.setThermalOverride(true);
                if (appContext != null) {
                    NetworkOptimizer.optimizeAllDataAndWifi(appContext);
                    RamZramChannel.trimMemoryAndCleanCache(appContext);
                    RamZramChannel.applyIOAndMemoryFlags();
                }

                // ── Step 4: Transactional OS Tweaks (SurfaceFlinger & Vulkan) ─
                Log.i(TAG, "▶ Step 4: Applying Transactional Low-Overhead Tweaks");
                List<String> tweakBatch = new ArrayList<>();
                if (targetPreset.zeroLatencyVsync) {
                    tweakBatch.add("setprop debug.sf.early_phase_offset_ns 0");
                    tweakBatch.add("setprop debug.sf.early_app_phase_offset_ns 0");
                    tweakBatch.add("setprop debug.sf.early_gl_phase_offset_ns 0");
                    tweakBatch.add("setprop debug.sf.disable_hwc_vds 1");
                }
                tweakBatch.add("setprop debug.renderengine.backend vulkan");
                tweakBatch.add("setprop debug.hwui.render_thread_priority -20");
                tweakBatch.add("setprop debug.hwui.use_gpu_pixel_buffers true");
                tweakBatch.add("setprop debug.sf.hw 1");
                ShizukuExecutor.executeShizukuCommands(tweakBatch.toArray(new String[0]));

                // ── Step 5: Native C++ Fast-Path Process Acceleration ─────────
                Log.i(TAG, "▶ Step 5: Fast-Path Native C++ Process Acceleration");
                final int finalHz = targetHz;
                AppExecutors.getInstance().executeCommand(() -> {
                    try {
                        Thread.sleep(1200);
                        String pidOut = ShizukuExecutor.executeShizukuCommand("pidof " + pkg);
                        if (pidOut != null && !pidOut.trim().isEmpty() && !pidOut.startsWith("ERROR")) {
                            for (String pStr : pidOut.trim().split("\\s+")) {
                                try {
                                    int pid = Integer.parseInt(pStr.trim());
                                    if (pid <= 0) continue;
                                    // Direct Linux Syscall: nice -10, Real-Time I/O priority
                                    if (!NativeConfigInjector.setProcessIOPriority(pid, -10, 1, 0)) {
                                        ShizukuExecutor.executeShizukuCommands(
                                            "renice -n -10 -p " + pid + " 2>/dev/null",
                                            "ionice -c 1 -n 0 -p " + pid + " 2>/dev/null"
                                        );
                                    }
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    } catch (Throwable ignored) {}
                });

                // Android Framework Gaming Locks
                if (appContext != null) {
                    try {
                        NativeFrameworkBridge.acquireSustainedPerformanceLock(appContext);
                        NativeFrameworkBridge.acquireLowLatencyWifiLock(appContext);
                        NativeFrameworkBridge.startAdpfSession(appContext, finalHz);
                        NativeFrameworkBridge.setGameModePerformance(appContext, pkg);
                        GameModeApiSupport.applyModernAndroidPerformanceFlags(pkg, finalHz);
                        GameSpaceDndManager.setGamingDndMode(appContext, true);
                    } catch (Throwable ignored) {}
                }

                GameManagerStatus.getInstance().setActiveSession(pkg);
                GameManagerStatus.getInstance().recordApply(24, "Unified Session: " + pkg + " @ " + finalHz + "Hz");
                Log.i(TAG, "✅ [ORCHESTRATOR] Session successfully initialized for " + pkg);

                if (callback != null) {
                    AppExecutors.getInstance().postToMainThread(() ->
                        callback.onSessionReady(pkg, finalHz, "⚡ Active: " + targetPreset.title + " @ " + finalHz + "Hz")
                    );
                }

            } catch (Throwable t) {
                Log.e(TAG, "❌ [ORCHESTRATOR] Session failure: " + t.getMessage(), t);
                sSessionActive.set(false);
                if (callback != null) {
                    AppExecutors.getInstance().postToMainThread(() -> callback.onError(pkg, t));
                }
            }
        });
    }

    /**
     * Gracefully ends the current game session and reverts all hardware and tweaks.
     */
    public static void endSession(Context context) {
        AppExecutors.getInstance().executeCommand(() -> {
            String pkg = sCurrentPackage;
            Log.i(TAG, "Restoring baseline — terminating session for: " + pkg);
            sSessionActive.set(false);
            sCurrentPackage = null;
            sCurrentPreset = null;

            if (context != null) {
                GameStateReverter.revertToBaseline(context.getApplicationContext());
            }
            GameManagerStatus.getInstance().setActiveSession(null);
            Log.i(TAG, "✅ Baseline fully restored.");
        });
    }
}
