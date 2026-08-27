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
 * Session pipeline (called after Phase 1 & 2 in GameManagerLauncher):
 *  1. Capture baseline session state + target FPS
 *  2. Grant full combo storage access (MediaStore + data dir + external)
 *  3. Apply Android 13-16 Game Mode API flags
 *  4. Hardware Device Masking (GPU / RAM / Model spoof via HardwareMaskEngine)
 *  5. Force high display refresh rate (MaxHzForceChannel + HzFpsChannel + AIDL)
 *  6. Elevate CPU/GPU governors (AIDL setCpuGpuPerformanceGovernors, thermal boost)
 *  7. Acquire sustained performance lock + low-latency WiFi lock + ADPF session
 *  8. Native C++ config injection — Unreal Engine .ini + Unity boot.config + Vulkan cache
 *  9. Auto-patch game config files (GameConfigPatcher + GameProfileAutoConfigurator)
 * 10. DND gaming mode + Network turbo optimization
 * 11. Async post-launch CPU core pinning (Big/Prime cores) + SCHED_FIFO realtime scheduling
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

        // ── 3. Apply modern Android 13-16 performance API flags ──────────────
        GameModeApiSupport.applyModernAndroidPerformanceFlags(pkg, targetFps);

        // ── 4. Enforce Hardware Device Masking for target game ───────────────
        HardwareMaskEngine.maskPackage(appContext, pkg);

        // ── 5. Force High Hardware Display Refresh Rate ──────────────────────
        try {
            MaxHzForceChannel.forceApply(targetFps);
            HzFpsChannel.forceSetRefreshRate(appContext, targetFps);
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                ShizukuUserServiceConnector.getInstance().forceDisplayRefreshRate(targetFps);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Refresh rate lock warning: " + t.getMessage());
        }

        // ── 6. Elevate CPU/GPU Performance Governors via AIDL/Shizuku ────────
        try {
            if (ShizukuUserServiceConnector.getInstance().isServiceConnected()) {
                ShizukuUserServiceConnector.getInstance().setCpuGpuPerformanceGovernors();
                ShizukuUserServiceConnector.getInstance().applyThermalAndKernelBoost();
            }
            PerformanceChannel.applyProfile(appContext, PerformanceChannel.Profile.EXTREME_PERFORMANCE);
        } catch (Throwable t) {
            Log.w(TAG, "Performance channel warning: " + t.getMessage());
        }

        // ── 7. Acquire Sustained Performance Lock & Low-Latency WiFi Lock ────
        try {
            NativeFrameworkBridge.acquireSustainedPerformanceLock(appContext);
            NativeFrameworkBridge.acquireLowLatencyWifiLock(appContext);
            NativeFrameworkBridge.startAdpfSession(appContext, targetFps);
        } catch (Throwable t) {
            Log.w(TAG, "Native framework lock warning: " + t.getMessage());
        }

        // ── 8. Native C++ Engine Config Injection ────────────────────────────
        // Detect engine type and inject UE .ini / Unity boot.config / HoYo via POSIX C++ layer.
        // This ensures game engines re-read optimal settings on cold start.
        try {
            final int detectedEngine = detectEngineType(pkg);
            List<String> cfgPaths = GameConfigPathResolver.getPathsForGame(pkg);
            if (cfgPaths != null && !cfgPaths.isEmpty()) {
                for (String path : cfgPaths) {
                    if (path.endsWith(".ini") && detectedEngine == ENGINE_UNREAL) {
                        NativeConfigInjector.injectUnrealEngineIni(path, targetFps);
                    } else if (path.contains("boot.config") && detectedEngine == ENGINE_UNITY) {
                        NativeConfigInjector.injectUnityBootConfig(path, targetFps);
                    } else if ((path.endsWith(".ini") || path.endsWith(".json"))
                            && (detectedEngine == ENGINE_HOYO || detectedEngine == ENGINE_CUSTOM)) {
                        NativeConfigInjector.injectNextGenEngine(path, targetFps, detectedEngine);
                    }
                }
            }
            // Always try Vulkan pipeline cache warming for faster first-frame load
            String vulkanCacheDir = "/data/data/" + pkg + "/cache/vulkan_shader_cache";
            NativeConfigInjector.forceVulkanPipelineCache(vulkanCacheDir, pkg);
        } catch (Throwable t) {
            Log.w(TAG, "Native C++ injection warning: " + t.getMessage());
        }

        // ── 9. Auto-patch game config files if configured ────────────────────
        try {
            GameConfigPatcher.applyGameFpsPatch(appContext, pkg, targetFps);
            GameProfileAutoConfigurator.autoConfigGamePackage(appContext, pkg, targetFps);
        } catch (Throwable t) {
            Log.w(TAG, "Config patch warning: " + t.getMessage());
        }

        // ── 10. DND & Network Turbo ──────────────────────────────────────────
        try {
            NetworkOptimizer.optimizeAllDataAndWifi(appContext);
            GameSpaceDndManager.setGamingDndMode(appContext, true);
        } catch (Throwable t) {
            Log.w(TAG, "DND/Network warning: " + t.getMessage());
        }

        // ── 11. Async Post-Launch CPU Core Pinning, I/O Priority & Realtime Scheduling ──
        // Wait 1.5s for the game process to fully start, then:
        //   a) Pin main PID to Big/Prime cores (mask 0xf0) + SCHED_FIFO priority 80
        //   b) Apply real-time I/O class (ionice class 1, level 0) to the game PID
        //   c) Scan /proc/<pid>/task/ for known render threads and pin them individually
        final int finalFps = targetFps;
        com.gamebooster.app.core.AppExecutors.getInstance().executeCommand(() -> {
            try {
                Thread.sleep(1500);
                String pidOut = ShizukuExecutor.executeShizukuCommand("pidof " + pkg);
                if (pidOut != null && !pidOut.trim().isEmpty() && !pidOut.startsWith("ERROR")) {
                    String[] pids = pidOut.trim().split("\\s+");
                    for (String pStr : pids) {
                        try {
                            int pid = Integer.parseInt(pStr.trim());
                            if (pid <= 0) continue;

                            // Dynamic CPU core topology detection
                            int totalCores = Runtime.getRuntime().availableProcessors();
                            if (totalCores <= 0) totalCores = 8;
                            int cpuMask = (totalCores >= 8) ? 0xf0 : ((totalCores >= 6) ? 0x38 : ((1 << totalCores) - 1));
                            String maskHex = Integer.toHexString(cpuMask);

                            // a) Native sched_setaffinity — pin to Big/Prime CPU cluster safely
                            NativeConfigInjector.setProcessCpuAffinity(pid, cpuMask);

                            // b) Safe Linux CFS High-Priority renice (-20) & taskset (avoids dangerous RT watchdog SIGKILL)
                            ShizukuExecutor.executeShizukuCommands(
                                "taskset -p " + maskHex + " " + pid + " 2>/dev/null",
                                "renice -n -20 -p " + pid + " 2>/dev/null",
                                "ionice -c 2 -n 0 -p " + pid + " 2>/dev/null"
                            );

                            // c) Per-thread render thread affinity + high priority
                            // Scan /proc/<pid>/task/ for known game render thread names
                            // and elevate each one for maximum frame consistency without watchdog starvation
                            try {
                                String taskListOut = ShizukuExecutor.executeShizukuCommand(
                                    "ls /proc/" + pid + "/task/ 2>/dev/null"
                                );
                                if (taskListOut != null && !taskListOut.startsWith("ERROR")) {
                                    for (String tidStr : taskListOut.trim().split("\\s+")) {
                                        try {
                                            int tid = Integer.parseInt(tidStr.trim());
                                            if (tid <= 0) continue;
                                            String comm = ShizukuExecutor.executeShizukuCommand(
                                                "cat /proc/" + pid + "/task/" + tid + "/comm 2>/dev/null"
                                            );
                                            if (comm == null) continue;
                                            comm = comm.trim().toLowerCase();
                                            // Target: Unity main, OpenGL/Vulkan render threads, game threads
                                            boolean isRenderThread =
                                                comm.contains("unitymain") ||
                                                comm.contains("renderthread") ||
                                                comm.contains("glthread") ||
                                                comm.contains("vulkanqueuethr") ||
                                                comm.contains("ue4") ||
                                                comm.contains("renderdoc") ||
                                                comm.startsWith("render") ||
                                                comm.contains("gamethrea");
                                            if (isRenderThread) {
                                                ShizukuExecutor.executeShizukuCommands(
                                                    "taskset -p " + maskHex + " " + tid + " 2>/dev/null",
                                                    "renice -n -20 -p " + tid + " 2>/dev/null",
                                                    "ionice -c 2 -n 0 -p " + tid + " 2>/dev/null"
                                                );
                                                Log.i(TAG, "✅ Optimized render thread [" + comm + "] TID:" + tid);
                                            }
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }
                            } catch (Throwable rt) {
                                Log.d(TAG, "Render thread scan skipped: " + rt.getMessage());
                            }

                            Log.i(TAG, "✅ Safe high-priority scheduling & core pinning (" + maskHex + ") for PID: " + pid);
                        } catch (NumberFormatException ignored) {}
                    }
                } else {
                    Log.d(TAG, "PID not found for " + pkg + " (game may still be loading)");
                }

                // Focus Mode (Deep App Freezer) for the active game session
                String gameKey = com.gamebooster.app.config.CfgProfileManager.resolveGameKey(pkg);
                com.gamebooster.app.config.CompetitiveCfgProfile profile = com.gamebooster.app.config.CfgProfileManager.loadProfile(appContext, gameKey);
                boolean shouldFreeze = (profile != null && profile.isFocusFreezeEnabled()) || com.gamebooster.app.config.ManualSettingsPreferences.isFocusModeEnabled(appContext);
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
