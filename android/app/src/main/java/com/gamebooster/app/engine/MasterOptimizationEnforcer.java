package com.gamebooster.app.engine;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.core.AppExecutors;
import com.gamebooster.app.booster.CpuGovernorChannel;
import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.config.FpsUnlockTier;
import com.gamebooster.app.config.GameConfigPatcher;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.tweaks.TweakManagerRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * MasterOptimizationEnforcer — Unified 3-Tier Master Enforcement Pipeline.
 *
 * Combines:
 * 1. Shizuku / Rish Elevated Temporary Root API (AIDL, permissions, AppOps, CFS scheduler, SurfaceFlinger)
 * 2. Native Android Framework APIs (GameManager, PowerManager sustained performance, WifiManager low-latency)
 * 3. Internal APK Optimization Engines (CPU/GPU channels, 60 system tweaks, game config patchers, network turbo)
 */
public class MasterOptimizationEnforcer {

    private static final String TAG = "MasterEnforcer";

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private MasterOptimizationEnforcer() {
        // Utility class; do not instantiate
    }

    public interface OnEnforceProgressListener {
        void onProgress(String currentStep, int progressPct);
        void onComplete(boolean success, int totalAppliedCount, String summaryMessage);
    }

    /**
     * Executes APK-wide master optimization enforcement asynchronously.
     */
    public static void enforceAllOptimizationsAsync(Context context, OnEnforceProgressListener listener) {
        if (context == null) {
            if (listener != null) listener.onComplete(false, 0, "Null context provided.");
            return;
        }

        final Context appContext = context.getApplicationContext();

        AppExecutors.getInstance().executeCommand(() -> {
            int totalApplied = 0;
            final boolean[] shizukuTierRan = {false};

            try {
                // ─────────────────────────────────────────────────────────────
                // TIER 1: SHIZUKU PRIVILEGED TEMPORARY ROOT EXECUTION
                // ─────────────────────────────────────────────────────────────
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> listener.onProgress("⚡ Elevating Shizuku Permissions & AppOps...", 15));
                }

                if (ShizukuExecutor.hasShizukuPermission()) {
                    shizukuTierRan[0] = true;
                    ShizukuPermissionEnforcer.enforceAllPermissions(appContext);
                    ShizukuUserServiceConnector.getInstance().bindService();
                    ShizukuUserServiceConnector.getInstance().setCpuGpuPerformanceGovernors();
                    ShizukuUserServiceConnector.getInstance().applyThermalAndKernelBoost();
                    totalApplied += 15;
                }

                // ─────────────────────────────────────────────────────────────
                // TIER 2: NATIVE ANDROID OS FRAMEWORK APIS
                // ─────────────────────────────────────────────────────────────
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> listener.onProgress("📱 Engaging Native Android OS Framework APIs...", 40));
                }

                NativeFrameworkBridge.acquireLowLatencyWifiLock(appContext);
                NativeFrameworkBridge.acquireSustainedPerformanceLock(appContext);
                NativeFrameworkBridge.requestHighPriorityNetwork(appContext);
                totalApplied += 5;

                // ─────────────────────────────────────────────────────────────
                // TIER 3: APK CORE OPTIMIZATION ENGINES
                // ─────────────────────────────────────────────────────────────
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> listener.onProgress("🚀 Applying 60 Curated Hardware & Kernel Tweaks...", 65));
                }

                // 3A. Apply curated tweaks repository
                int tweaksApplied = TweakManagerRepository.applyAllSupportedTweaks(appContext);
                totalApplied += tweaksApplied;

                // 3B. CPU & GPU hardware channels
                PerformanceChannel.applyProfile(appContext, PerformanceChannel.Profile.EXTREME_PERFORMANCE);
                PerformanceChannel.setGpuRenderMode(true); // Vulkan 3D
                CpuGovernorChannel.setGovernor("extreme");
                GpuTweaksChannel.setAngleMode(true);
                GpuTweaksChannel.setGameDriverMode(true);

                // 3C. Network & DNS optimization
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> listener.onProgress("📶 Activating TCP BBR & Network Turbo Boost...", 85));
                }
                NetworkOptimizer.optimizeAllDataAndWifi(appContext);
                NetworkOptimizer.flushDnsCache();

                // 3D. Execute master root performance script
                int targetHz = GameProfileAutoConfigurator.getTargetFpsHz(appContext);
                if (targetHz <= 0) targetHz = FpsUnlockTier.resolveTargetFps(targetHz);
                PerformanceChannel.writeAndExecuteRootTweaksScript(targetHz);
                MaxHzForceChannel.forceApply(targetHz);

                final int finalCount = totalApplied;
                final boolean tier1Ran = shizukuTierRan[0];
                if (listener != null) {
                    AppExecutors.getInstance().postToMainThread(() -> {
                        listener.onProgress("✅ Master Optimization 100% Enforced!", 100);
                        String summary = tier1Ran
                                ? "All 3 Tiers (Shizuku Root + Android OS API + APK Engines) successfully ENFORCED!"
                                : "Optimizations applied WITHOUT Shizuku — system-level tiers skipped (grant Shizuku permission for full effect).";
                        listener.onComplete(true, finalCount, summary);
                    });
                }
                Log.i(TAG, "Master optimization enforcement completed. Total tweaks/layers applied: " + totalApplied);

            } catch (Throwable t) {
                Log.e(TAG, "Error enforcing master optimization: " + t.getMessage(), t);
                if (listener != null) {
                    final int count = totalApplied;
                    AppExecutors.getInstance().postToMainThread(() -> 
                        listener.onComplete(false, count, "Partial enforcement completed with warning: " + t.getMessage()));
                }
            }
        });
    }

    /**
     * Enforces game-launch optimizations across all 3 tiers with zero delay.
     * Fire-and-forget variant — no report listener (see 4-arg overload).
     */
    public static void enforceGameLaunchOptimizations(Context context, String packageName, int targetFps) {
        enforceGameLaunchOptimizations(context, packageName, targetFps, null);
    }

    /**
     * Enforces game-launch optimizations across all 3 tiers with zero delay
     * and per-step reporting (Phase 1.2).
     *
     * Each ~18 step is recorded into an {@link EnforcementReport}; a failed
     * Tier 1 (Shizuku) step marks root-dependent Tier 3 steps as SKIPPED
     * instead of blindly continuing (e.g. the old behavior of running the
     * `sed -i` config patch even when Shizuku is dead).
     */
    public static void enforceGameLaunchOptimizations(Context context, String packageName, int targetFps,
                                                      OnEnforcementReportListener listener) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) {
            if (listener != null) {
                listener.onEnforcementReport(new EnforcementReport(null, "Invalid context/package"));
            }
            return;
        }

        final Context appContext = context.getApplicationContext();
        final String pkg = packageName.trim();
        final int forcedFps = com.gamebooster.app.config.GameProfileAutoConfigurator
                .clampTargetFpsToDisplay(appContext,
                        com.gamebooster.app.config.FpsUnlockTier.resolveTargetFps(targetFps));

        AppExecutors.getInstance().executeCommand(() -> {
            EnforcementReport report = new EnforcementReport(pkg, null);
            boolean tier1Ok = false;
            boolean shizukuAvailable = false;

            try {
                Log.i(TAG, "Enforcing game launch pipeline for: " + pkg + " @ " + forcedFps + " FPS/Hz");

                // ─────────────────────────────────────────────────────────────
                // TIER 1: SHIZUKU PRIVILEGED SHELL & AIDL
                // ─────────────────────────────────────────────────────────────
                shizukuAvailable = ShizukuExecutor.hasShizukuPermission();
                if (shizukuAvailable) {
                    // Phase 1.1 gate: wait briefly for the AIDL user service
                    if (!com.gamebooster.app.shizuku.ShizukuConnectionManager.getInstance().ensureReady(300)) {
                        report.addStep("Tier 1", "Shizuku AIDL UserService readiness", false,
                                "binder dead / service unavailable");
                        shizukuAvailable = false;
                    }
                } else {
                    report.addStep("Tier 1", "Shizuku privileged shell (6 cmd/settings + Hz + governors)", false,
                            "Shizuku permission is not granted");
                }

                if (shizukuAvailable) {
                    tier1Ok = true;
                    List<String> tier1Commands = new ArrayList<>();
                    // Settings-global driver opt-ins work on every Android version.
                    tier1Commands.add("settings put global game_driver_opt_in_apps " + pkg);
                    tier1Commands.add("settings put global updatable_driver_production_opt_in_apps " + pkg);
                    // Phase 2.1: the GameMode shell set is Android 14+ (API 34) only;
                    // below that, skip and rely on the SurfaceFlinger refresh override
                    // (forceDisplayRefreshRate) + Tier-3 config patchers instead.
                    if (GameModeApiSupport.isAvailable()) {
                        tier1Commands.add(0, "cmd game mode performance " + pkg);
                        tier1Commands.add(1, "cmd window set-app-refresh-rate " + pkg + " " + forcedFps);
                        tier1Commands.add(2, "cmd game set --fps " + forcedFps + " " + pkg);
                        tier1Commands.add(3, "device_config put game_overlay " + pkg + " mode=2,fps=" + forcedFps + ":mode=3,fps=" + forcedFps);
                    } else {
                        report.addStep("Tier 1", "GameMode shell set (cmd game / set-app-refresh-rate / game_overlay)", false,
                                "SKIPPED — requires Android 14+ (API 34); falling back to SurfaceFlinger override + config patchers");
                    }
                    for (String cmd : tier1Commands) {
                        report.attemptStep("Tier 1", cmd, () -> {
                            String res = ShizukuUserServiceConnector.getInstance().executeCommand(cmd);
                            if (res != null && res.startsWith("ERROR:")) {
                                throw new IllegalStateException(res);
                            }
                        });
                    }

                    report.attemptStep("Tier 1", "forceDisplayRefreshRate(" + forcedFps + ")", () ->
                            ShizukuUserServiceConnector.getInstance().forceDisplayRefreshRate(forcedFps));

                    report.attemptStep("Tier 1", "setCpuGpuPerformanceGovernors", () ->
                            ShizukuUserServiceConnector.getInstance().setCpuGpuPerformanceGovernors());

                    tier1Ok = report.tierSucceededWithoutFailures("Tier 1");
                }

                // ─────────────────────────────────────────────────────────────
                // TIER 2: NATIVE ANDROID OS FRAMEWORK APIS (no root needed)
                // ─────────────────────────────────────────────────────────────
                report.attemptStep("Tier 2", "setGameModePerformance", () ->
                        NativeFrameworkBridge.setGameModePerformance(appContext, pkg));
                report.attemptStep("Tier 2", "acquireLowLatencyWifiLock", () ->
                        NativeFrameworkBridge.acquireLowLatencyWifiLock(appContext));
                report.attemptStep("Tier 2", "acquireSustainedPerformanceLock", () ->
                        NativeFrameworkBridge.acquireSustainedPerformanceLock(appContext));
                report.attemptStep("Tier 2", "requestHighPriorityNetwork", () ->
                        NativeFrameworkBridge.requestHighPriorityNetwork(appContext));

                // ─────────────────────────────────────────────────────────────
                // TIER 3: APK CORE GAME CONFIG PATCHING & HARDWARE SPOOFER
                // ─────────────────────────────────────────────────────────────

                // Root-dependent steps — skipped when Tier 1 failed (Phase 1.2)
                if (tier1Ok) {
                    report.attemptStep("Tier 3", "GameConfigPatcher.applyGameFpsPatch(" + pkg + ", " + forcedFps + ")", () ->
                            GameConfigPatcher.applyGameFpsPatch(pkg, forcedFps));
                    report.attemptStep("Tier 3", "MaxHzForceChannel.forceApply(" + forcedFps + ")", () ->
                            MaxHzForceChannel.forceApply(forcedFps));
                    report.attemptStep("Tier 3", "PerformanceChannel.applyProfile(EXTREME_PERFORMANCE)", () ->
                            PerformanceChannel.applyProfile(appContext, PerformanceChannel.Profile.EXTREME_PERFORMANCE));
                    report.attemptStep("Tier 3", "writeAndExecuteRootTweaksScript(" + forcedFps + ")", () ->
                            PerformanceChannel.writeAndExecuteRootTweaksScript(forcedFps));
                    report.attemptStep("Tier 3", "DeviceSpooferEngine.applySpoofing", () ->
                            DeviceSpooferEngine.applySpoofing(appContext, pkg));
                } else {
                    report.addStep("Tier 3", "GameConfigPatcher.applyGameFpsPatch(" + pkg + ", " + forcedFps + ")", false,
                            "SKIPPED — depends on Tier 1 (Shizuku)");
                    report.addStep("Tier 3", "MaxHzForceChannel.forceApply(" + forcedFps + ")", false,
                            "SKIPPED — depends on Tier 1 (Shizuku)");
                    report.addStep("Tier 3", "PerformanceChannel.applyProfile(EXTREME_PERFORMANCE)", false,
                            "SKIPPED — depends on Tier 1 (Shizuku)");
                    report.addStep("Tier 3", "writeAndExecuteRootTweaksScript(" + forcedFps + ")", false,
                            "SKIPPED — depends on Tier 1 (Shizuku)");
                    report.addStep("Tier 3", "DeviceSpooferEngine.applySpoofing", false,
                            "SKIPPED — depends on Tier 1 (Shizuku)");
                }

                // Root-independent native step — always runs
                report.attemptStep("Tier 3", "NetworkOptimizer.flushDnsCache", () ->
                        NetworkOptimizer.flushDnsCache());

                Log.i(TAG, "Game launch optimization report for " + pkg + ": "
                        + report.succeeded + " ok, " + report.failed + " failed, " + report.skipped + " skipped");
            } catch (Throwable t) {
                Log.w(TAG, "Failed to apply full game launch optimization for " + pkg + ": " + t.getMessage());
                report.addStep("All", "Pipeline", false, t.getMessage());
            }

            if (listener != null) {
                listener.onEnforcementReport(report);
            }
        });
    }

    /** Per-step outcome for the Phase 1.2 enforcement report. */
    public static class StepResult {
        public final String tier;
        public final String step;
        public final boolean ok;
        public final String detail;

        public StepResult(String tier, String step, boolean ok, String detail) {
            this.tier = tier;
            this.step = step;
            this.ok = ok;
            this.detail = detail;
        }
    }

    /** Aggregated per-step report of an enforcement run. */
    public static class EnforcementReport {
        public final String packageName;
        public final List<StepResult> steps = new ArrayList<>();
        public int succeeded = 0;
        public int failed = 0;
        public int skipped = 0;

        public EnforcementReport(String packageName, String fatalSummary) {
            this.packageName = packageName;
            if (fatalSummary != null) {
                addStep("All", "Pipeline", false, fatalSummary);
            }
        }

        public void addStep(String tier, String step, boolean ok, String detail) {
            steps.add(new StepResult(tier, step, ok, detail));
            if (ok) {
                succeeded++;
            } else if (detail != null && detail.startsWith("SKIPPED")) {
                skipped++;
            } else {
                failed++;
            }
        }

        public void attemptStep(String tier, String step, Runnable action) {
            try {
                action.run();
                addStep(tier, step, true, "OK");
            } catch (Throwable t) {
                String msg = t.getMessage();
                if (msg == null || msg.trim().isEmpty()) msg = t.getClass().getSimpleName();
                addStep(tier, step, false, msg.trim());
            }
        }

        /** True when every step succeeded and nothing was skipped. */
        public boolean fullyApplied() {
            return failed == 0 && skipped == 0;
        }

        /** Aggregate of one tier, for skip-decision logic. */
        public boolean tierSucceededWithoutFailures(String tier) {
            for (StepResult r : steps) {
                if (tier.equals(r.tier) && !r.ok && !(r.detail != null && r.detail.startsWith("SKIPPED"))) {
                    return false;
                }
            }
            return true;
        }

        /** Renders the report as dialog lines: ✓ ok, ✗ failed, ⤳ skipped. */
        public List<String> toDialogLines() {
            List<String> lines = new ArrayList<>();
            for (StepResult r : steps) {
                String icon = r.ok ? "✓" :
                        (r.detail != null && r.detail.startsWith("SKIPPED")) ? "⤳" : "✗";
                String line = icon + " [" + r.tier + "] " + r.step;
                if (!r.ok && r.detail != null && !r.detail.isEmpty()) {
                    line += " — " + r.detail.replace("ERROR: ", "");
                }
                lines.add(line);
            }
            lines.add("Result: " + succeeded + " ok / " + failed + " failed / " + skipped + " skipped");
            return lines;
        }
    }

    public interface OnEnforcementReportListener {
        void onEnforcementReport(EnforcementReport report);
    }

    public static class EnforcementStatus {
        public final boolean shizukuRootGranted;
        public final boolean aidlConnected;
        public final int tweaksAppliedCount;
        public final int totalSupportedTweaks;
        public final String androidVersion;
        public final int sdkInt;

        public EnforcementStatus(boolean shizukuRootGranted, boolean aidlConnected,
                                 int tweaksAppliedCount, int totalSupportedTweaks,
                                 String androidVersion, int sdkInt) {
            this.shizukuRootGranted = shizukuRootGranted;
            this.aidlConnected = aidlConnected;
            this.tweaksAppliedCount = tweaksAppliedCount;
            this.totalSupportedTweaks = totalSupportedTweaks;
            this.androidVersion = androidVersion;
            this.sdkInt = sdkInt;
        }
    }

    public static EnforcementStatus verifyEnforcementStatus(Context context) {
        boolean shizuku = ShizukuExecutor.hasShizukuPermission();
        boolean aidl = ShizukuUserServiceConnector.getInstance().isServiceConnected();
        int appliedCount = 0;
        int total = TweakManagerRepository.getAllTweaks().size();
        for (com.gamebooster.app.tweaks.TweakItem item : TweakManagerRepository.getAllTweaks()) {
            if (item.isApplied()) appliedCount++;
        }
        return new EnforcementStatus(shizuku, aidl, appliedCount, total, android.os.Build.VERSION.RELEASE, android.os.Build.VERSION.SDK_INT);
    }
}
