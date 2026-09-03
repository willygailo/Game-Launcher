package com.gamebooster.app.gamemanager;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.booster.HzFpsChannel;
import com.gamebooster.app.booster.MaxHzForceChannel;
import com.gamebooster.app.booster.NetworkOptimizer;
import com.gamebooster.app.booster.PerformanceChannel;
import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.config.GameConfigPatcher;
import com.gamebooster.app.config.GameConfigPathResolver;
import com.gamebooster.app.config.GameConfigStorageAccessEngine;
import com.gamebooster.app.config.GameProfileAutoConfigurator;
import com.gamebooster.app.config.GameProfilePreferences;
import com.gamebooster.app.config.GameSessionSettings;
import com.gamebooster.app.config.NativeConfigInjector;
import com.gamebooster.app.engine.GameModeApiSupport;
import com.gamebooster.app.engine.NativeFrameworkBridge;
import com.gamebooster.app.gamespace.GameSpaceDndManager;
import com.gamebooster.app.gamespace.GameStateReverter;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuPermissionEnforcer;
import com.gamebooster.app.shizuku.ShizukuUserServiceConnector;
import com.gamebooster.app.spoofer.HardwareMaskEngine;

import java.util.List;

/**
 * GameManagerSessionEngine — Manages the lifecycle of an active game session.
 *
 * Ensures all optimizations (Shizuku, Android API, hardware masking, storage unlock,
 * high refresh rate, and CPU/GPU performance governors) are enforced when a game starts,
 * and cleanly reverted to stock baseline when the game session ends.
 *
 * Session pipeline:
 *  1. Capture baseline session state + target FPS
 *  2. Grant full combo storage access (MediaStore + data dir + external)
 *  3. Acquire legal SDK locks (Sustained Performance, Low-Latency Wi-Fi, ADPF)
 *  4. Enforce legal Android GameManager API + Android 13-16 Game Mode performance flags
 *  5. Hardware Device Masking (GPU / RAM / Model spoof via HardwareMaskEngine)
 *  6. Format-Aware Per-Game Configuration & Physics Injection (Unreal/Unity/HoYo/Custom)
 *  7. Force high display refresh rate (MaxHzForceChannel + HzFpsChannel + DisplayManager)
 *  8. Elevate CPU/GPU governors (AIDL setCpuGpuPerformanceGovernors, thermal boost)
 *  9. DND gaming mode + Network turbo optimization
 * 10. Async post-launch CPU core pinning (Big/Prime cores) + SCHED_FIFO realtime scheduling
 */
public final class GameManagerSessionEngine {

    private static final String TAG = "GameManagerSession";

    /** Engine-type constants matching native_config_injector.cpp ENGINE_* defines */
    private static final int ENGINE_UNREAL = 1;
    private static final int ENGINE_UNITY  = 2;
    private static final int ENGINE_HOYO   = 3;
    private static final int ENGINE_CUSTOM = 4;

    private GameManagerSessionEngine() {
    }

    /**
     * Begins an optimized game session for the target package.
     *
     * Must be called on a background thread (AppExecutors.executeCommand).
     */
    public static void beginSession(Context context, String packageName) {
        if (context == null || packageName == null || packageName.trim().isEmpty()) {
            return;
        }
        final String pkg = packageName.trim();
        final Context appContext = context.getApplicationContext();

        Log.i(TAG, "⚡ Starting GameManager Session for: " + pkg);

        // ── 1. Capture baseline session state ────────────────────────────────
        int targetFps = GameProfilePreferences.getTargetHz(appContext, pkg);
        if (targetFps <= 0) targetFps = 185;
        GameSessionSettings.begin(appContext, pkg);
        GameManagerStatus.getInstance().setActiveSession(pkg);

        // ── 2. Grant full combo storage access ───────────────────────────────
        GameConfigStorageAccessEngine.grantAllPathsAccess(appContext, pkg);

        // ── 3. Acquire Sustained Performance Lock & Low-Latency WiFi Lock ────
        try {
            NativeFrameworkBridge.acquireSustainedPerformanceLock(appContext);
            NativeFrameworkBridge.acquireLowLatencyWifiLock(appContext);
            NativeFrameworkBridge.startAdpfSession(appContext, targetFps);
        } catch (Throwable t) {
            Log.w(TAG, "Native framework lock warning: " + t.getMessage());
        }

        // ── 4. Apply modern Android 13-16 performance API flags ──────────────
        try {
            NativeFrameworkBridge.setGameModePerformance(appContext, pkg);
            GameModeApiSupport.applyModernAndroidPerformanceFlags(pkg, targetFps);
        } catch (Throwable t) {
            Log.w(TAG, "Game mode flags warning: " + t.getMessage());
        }

        // ── 5. Enforce Hardware Device Masking for target game ───────────────
        HardwareMaskEngine.maskPackage(appContext, pkg);

        // ── 5b. Stealth In-Lobby Configuration & Overdrive Injection ──────────
        try {
            com.gamebooster.app.config.LobbyInjectionEngine.scheduleLobbyInjection(appContext, pkg, targetFps);
            Log.i(TAG, "🛡️ Scheduled in-lobby safe injection for " + pkg + " @ " + targetFps + " FPS (Anti-Detection Protected)");
        } catch (Throwable t) {
            Log.w(TAG, "Lobby injection scheduling warning: " + t.getMessage());
        }

        // ── 6. Force High Hardware Display Refresh Rate ──────────────────────
        try {
            MaxHzForceChannel.forceApply(targetFps);
            HzFpsChannel.forceSetRefreshRate(appContext, targetFps);
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                ShizukuUserServiceConnector.getInstance().forceDisplayRefreshRate(targetFps);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Refresh rate lock warning: " + t.getMessage());
        }

        // ── 7. Elevate CPU/GPU Performance Governors via AIDL/Shizuku ────────
        try {
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                ShizukuUserServiceConnector.getInstance().setCpuGpuPerformanceGovernors();
                ShizukuUserServiceConnector.getInstance().applyThermalAndKernelBoost();
            }
            PerformanceChannel.applyProfile(appContext, PerformanceChannel.Profile.EXTREME_PERFORMANCE);
        } catch (Throwable t) {
            Log.w(TAG, "Performance channel warning: " + t.getMessage());
        }

        // ── 8. Runtime Session Framework, Touch & Network Boost ──────────────
        try {
            com.gamebooster.app.booster.TouchLatencyChannel.enableUltraTouchResponse();
            NetworkOptimizer.optimizeAllDataAndWifi(appContext);
            GameSpaceDndManager.setGamingDndMode(appContext, true);
        } catch (Throwable t) {
            Log.w(TAG, "DND/Network/Touch warning: " + t.getMessage());
        }

        // ── 9. Safe Process Priority (CFS renice on main PID & Android 13-16 OS hooks) ──
        final int finalFps = targetFps;
        com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
            try {
                // Android 13, 14, 15, 16 Native Game Mode & Per-App Window Refresh Rate
                ShizukuExecutor.executeShizukuCommands(
                        "cmd game set --mode 2 " + pkg + " 2>/dev/null || true",
                        "cmd game set --fps " + finalFps + " " + pkg + " 2>/dev/null || true",
                        "cmd window set-app-refresh-rate " + pkg + " " + finalFps + " 2>/dev/null || true",
                        // Android 14-16 Linux UClamp & CFS Top-App Scheduling Boost
                        "echo 1024 > /dev/cpuset/top-app/uclamp.min 2>/dev/null || true",
                        "echo 1024 > /sys/fs/cgroup/top-app/uclamp.min 2>/dev/null || true",
                        "echo 100 > /dev/stune/top-app/schedtune.boost 2>/dev/null || true"
                );

                Thread.sleep(1500);
                String pidOut = ShizukuExecutor.executeShizukuCommand("pidof " + pkg);
                if (pidOut != null && !pidOut.trim().isEmpty() && !pidOut.startsWith("ERROR")) {
                    String[] pids = pidOut.trim().split("\\s+");
                    for (String pStr : pids) {
                        try {
                            int pid = Integer.parseInt(pStr.trim());
                            if (pid <= 0) continue;

                            // Apply fast native process priority first, fallback to Shizuku shell
                            if (!NativeConfigInjector.setProcessIOPriority(pid, -10, 1, 0)) {
                                ShizukuExecutor.executeShizukuCommands(
                                    "renice -n -10 -p " + pid + " 2>/dev/null",
                                    "ionice -c 2 -n 0 -p " + pid + " 2>/dev/null"
                                );
                            }
                            Log.i(TAG, "✅ Safe high-priority scheduling for PID: " + pid);
                        } catch (NumberFormatException ignored) {}
                    }
                }

                // Focus Mode (Deep App Freezer) for the active game session (respects manual toggle only)
                boolean shouldFreeze = com.gamebooster.app.config.ManualSettingsPreferences.isFocusModeEnabled(appContext);
                if (shouldFreeze) {
                    int frozen = com.gamebooster.app.focus.FocusModeEngine.enableFocusMode(appContext, pkg);
                    Log.i(TAG, "🎯 Focus Mode Engine: " + frozen + " background apps frozen for " + pkg);
                }
            } catch (Throwable t) {
                Log.w(TAG, "Async CPU/IO affinity & focus mode warning: " + t.getMessage());
            }
        });

        GameManagerStatus.getInstance().recordApply(18, "Game Session Activated for " + pkg + " @ " + finalFps + " FPS");
        Log.i(TAG, "✅ Game session fully initialized for: " + pkg + " @ " + finalFps + " FPS");
    }

    /**
     * Ends the active game session and restores baseline state.
     */
    public static void endSession(Context context, String packageName) {
        if (context == null) return;
        final Context appContext = context.getApplicationContext();

        Log.i(TAG, "Restoring baseline — ending GameManager Session for: " + packageName);
        GameStateReverter.revertToBaseline(appContext);
        GameManagerStatus.getInstance().setActiveSession(null);
    }

    /**
     * Checks if a session is currently active.
     */
    public static boolean isSessionActive(Context context) {
        if (context == null) return false;
        return GameSessionSettings.hasActiveSession(context) || GameManagerStatus.getInstance().hasActiveSession();
    }

    /**
     * Detects the game engine type from the package name to route native C++ injection correctly.
     *
     * @return ENGINE_UNREAL, ENGINE_UNITY, ENGINE_HOYO, or ENGINE_CUSTOM
     */
    private static int detectEngineType(String pkg) {
        if (pkg == null) return ENGINE_CUSTOM;
        String p = pkg.toLowerCase();

        // Unreal Engine games (PUBG Mobile, CoD Mobile/Warzone, Valorant Mobile,
        // Arena Breakout, Delta Force, Blood Strike, Farlight 84, Wild Rift, HoK)
        if (p.contains("pubg") || p.contains("tencent.ig") || p.contains("imobile")
                || p.contains("vng.pubgmobile") || p.contains("cod") || p.contains("callofduty")
                || p.contains("warzone") || p.contains("valorant") || p.contains("projectc")
                || p.contains("arenabreakout") || p.contains("uamo") || p.contains("deltaforce")
                || p.contains("bloodstrike") || p.contains("newspike")
                || p.contains("farlight") || p.contains("solarland")
                || p.contains("wildrift") || p.contains("riotgames.league")
                || p.contains("sgame") || p.contains("levelinfinite")
                || p.contains("arenaofvalor") || p.contains("kgtw")) {
            return ENGINE_UNREAL;
        }

        // HoYoverse / Kuro Games custom engines
        if (p.contains("genshin") || p.contains("mihoyo") || p.contains("cognosphere")
                || p.contains("hoyoverse") || p.contains("hkrpg") || p.contains("nap")
                || p.contains("wutheringwaves") || p.contains("kurogame")) {
            return ENGINE_HOYO;
        }

        // Unity games (Mobile Legends, Free Fire, CarX, Roblox, Standoff 2, Supercell)
        if (p.contains("mobile.legends") || p.contains("mobilelegends")
                || p.contains("freefire") || p.contains("dts.freefire")
                || p.contains("carx") || p.contains("roblox")
                || p.contains("standoff2") || p.contains("axlebolt")
                || p.contains("supercell") || p.contains("brawlstars")
                || p.contains("clashroyale") || p.contains("clashofclans")) {
            return ENGINE_UNITY;
        }

        return ENGINE_CUSTOM;
    }
}
