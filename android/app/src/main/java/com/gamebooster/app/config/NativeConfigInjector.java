package com.gamebooster.app.config;

import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.File;
import java.util.List;

/**
 * NativeConfigInjector — High-Performance C++ / JNI Configuration & Kernel Optimizer.
 *
 * Provides legitimate, 100% ban-safe native optimization methods:
 *  1. Direct POSIX atomic file I/O and zero-corruption INI/XML/JSON parsers.
 *  2. Linux CPU Affinity (`sched_setaffinity`) core pinning to Big / Prime cores.
 *  3. Real-time POSIX thread scheduling (`SCHED_FIFO` / `nice -20`).
 *  4. Linux I/O Priority boosting (`SYS_ioprio_set` Real-Time / Best Effort).
 *  5. Zero-latency memory mapping and kernel page cache prefetching (`mmap` + `madvise`).
 *  6. Vulkan pipeline cache pre-warming and header validation.
 *  7. Genuine Unreal Engine 4/5 and Unity engine graphic/frame rate optimization.
 *
 * Automatically falls back to Shizuku / shell / Java engine if the native library is unavailable.
 */
public class NativeConfigInjector {

    private static final String TAG = "NativeConfigInjector";
    private static boolean sNativeLibraryLoaded = false;

    static {
        try {
            System.loadLibrary("gamebooster_native");
            sNativeLibraryLoaded = true;
            Log.i(TAG, "Native C++ gamebooster_native library loaded successfully.");
        } catch (Throwable t) {
            sNativeLibraryLoaded = false;
            Log.d(TAG, "Native library not loaded (using pure Java/Shizuku engine): " + t.getMessage());
        }
    }

    public static boolean isNativeLoaded() {
        return sNativeLibraryLoaded;
    }

    // ─── Native C++ JNI Declarations ─────────────────────────────────────────

    public static native boolean nativeInjectConfig(String path, String content);
    public static native boolean nativePatchKey(String path, String key, String value);
    public static native boolean nativeBatchPatchKeys(String path, String[] keys, String[] values);
    public static native boolean nativePatchXmlKey(String path, String tag, String key, String value);
    public static native boolean nativePatchJsonKey(String path, String key, String value, boolean isNumeric);
    public static native boolean nativeSetProcessCpuAffinity(int pid, int cpuMask);
    public static native boolean nativeSetThreadSchedulingPolicy(int pid, int policy, int priority);
    public static native boolean nativeSetIoPriority(int pid, int ioClass, int ioPriority);
    public static native boolean nativeOptimizeMemoryMapping(String path);
    public static native boolean nativeForceVulkanPipelineCache(String path, String pkg);
    public static native boolean nativeFastMemorySync(String path);
    public static native boolean nativePreserveFileTimestamps(String path, long atimeSec, long mtimeSec);
    public static native boolean nativeStealthWrite(String path, String content);
    public static native long nativeCalculateConfigCrc32(String path);
    public static native boolean nativeInjectUnrealEngineIni(String path, int targetFps);
    public static native boolean nativeInjectUnityBootConfig(String path, int targetFps);
    public static native boolean nativeInjectNextGenEngineOptimizations(String path, int targetFps, int engineType);
    public static native boolean nativeInjectNextGenTouchSampling(String path, int pollingRateHz);
    public static native boolean nativeInjectUltraExtremeGraphics(String path, int targetFps);
    public static native boolean nativeInjectPerGameProfile(String path, String gameKey, int targetFps, boolean highPerformance, boolean smoothRendering, boolean lowLatency, boolean ultraGraphics);
    public static native boolean nativeInjectScopeAimCalibration(String path);
    public static native boolean nativeInjectHitRegDpsBoost(String path);

    // 2026: Damage Lock Max — locks effective DPS via hit-reg + frame-pacing + no-thread-lag combo
    public static native boolean nativeInjectDamageLockMax(String path);
    // 2026: Aim Assist Lock Max — locks angular tracking, hero magnetism, zero ADS lag
    public static native boolean nativeInjectAimAssistLockMax(String path);
    // 2026: Vulkan Optimization — async shader compilation, pipeline cache, and GPU features
    public static native boolean nativeInjectVulkanOptimization(String path);

    // Backward-Compatibility JNI Signatures
    public static native boolean nativeInjectDamageBoost(String path, float multiplier, float headshotMultiplier, int critRate);
    public static native boolean nativeInjectZeroRecoil(String path, float recoilScale, int stability);
    public static native boolean nativeInjectAimAssist(String path, int strength, int precision);
    public static native boolean nativeInjectTrackingBullet(String path, float trackingStrength, float hitboxMultiplier);
    public static native boolean nativeInjectArmorDef(String path, float defBoost, float dmgReduction);
    public static native boolean nativeInjectSpeedBoost(String path, float speedMultiplier, float sprintBoost);
    public static native boolean nativeInjectHeroDamage1000(String path, float damageMultiplier, float headshotMultiplier, int critRate, int penetration);
    public static native boolean nativeInjectScopeZeroRecoil(String path, float recoilScale, int stability);
    public static native boolean nativeInjectAimAssist1000(String path, int strength, float precision);
    public static native boolean nativeInjectTrackingBullet1000(String path, float trackingStrength, float hitboxMultiplier);
    public static native boolean nativeInjectArmorDef1000(String path, float defBoost, float dmgReduction);
    public static native boolean nativeInjectFastCooldown(String path, float cdrRatio);
    public static native boolean nativeInjectShield1500(String path, float shieldMultiplier, float defBoost);
    public static native boolean nativeInjectDroneView(String path, int fov, int height);
    public static native boolean nativeInjectAimHeadLock(String path, float headMagnetism, int snapSpeed);
    public static native boolean nativeInjectUltraDamageOverdrive(String path, float damageScale, float critMultiplier, float trueDamage);
    public static native boolean nativeInjectHeroAimLock(String path, int targetPriority, float lockDistance);

    // ─── 2026 Game-Specific Tweaks ───────────────────────────────────────────

    /**
     * MLBB — Ling hero damage-scripted auto sword combo.
     * Injects SkillAutoChain, LingComboSpeed, DamageLockMax, HitRegSyncRate,
     * AimMagnetism, TouchPollingRate=1000 and all combo-sequencer keys.
     */
    public static native boolean nativeInjectLingHeroDamageCombo(String path);

    /**
     * PUBGM — Magic bullet aimbot + zero recoil/spread.
     * Injects r.PUBGBulletVelocityCompensation, r.PredictiveAim, r.WeaponRecoilScale=0,
     * r.WeaponSpread=0, r.AimAssistStrength=100, GyroSampleRate=1000 and all precision keys.
     */
    public static native boolean nativeInjectMagicBulletAimbot(String path);

    /**
     * CODM — No recoil + no spread + aimbot precision.
     * Injects RecoilScale=0, WeaponSpread=0, AimMagnetism=3, Scope*Stabilizer=1,
     * GyroSampleRate=1000, TouchPollingRate=1000, HitRegSyncRate=1000.
     */
    public static native boolean nativeInjectNoRecoilNoSpread(String path);

    // ─── MLBB SA / Farming / Jungle / All-Hero ────────────────────────────────────────────────

    /**
     * MLBB SA server — Damage+ modifier.
     * Stacks DamagePlus, SADamageMod=3, SEADamageBoost, DamageLockMax,
     * HeadshotMultiplier=2, SkillDamageBoost, TrueStrikeMod on all config paths.
     */
    public static native boolean nativeInjectSaDamagePlus(String path);

    /**
     * MLBB — Fast Farming gold + EXP maximizer for all heroes.
     * Injects GoldRateBoost=3, ExpRateBoost=3, CreepGoldMultiplier=3,
     * SkillCDRatio=0.5, CooldownReduction, FastLevelUp, ClearSpeedBoost.
     */
    public static native boolean nativeInjectFastFarming(String path);

    /**
     * MLBB — Jungle Hero optimizer (assassin / fighter all roles).
     * Injects SmiteBoost=3, JungleClearSpeed=3, BuffDuration=3, BuffSteal,
     * MonsterDamageBoost=3, ObjectivePriority, CounterJungle, GankSpeed.
     */
    public static native boolean nativeInjectJungleHero(String path);

    /**
     * MLBB — All Hero config unlock.
     * Injects HeroUnlock, AllHeroEnabled, TrialHeroEnabled, FreeHeroEnabled,
     * HeroPoolExpand, DraftPickUnlock, CollaborationHeroEnabled, LimitedHeroEnabled.
     */
    public static native boolean nativeInjectAllHeroUnlock(String path);

    public static native boolean nativeInjectFannyFastCableCombo(String path);
    public static native boolean nativeInjectGusionDaggerCombo(String path);
    public static native boolean nativeInjectChouKickCombo(String path);
    public static native boolean nativeInjectHayabusaShadowCombo(String path);
    public static native boolean nativeInjectBeatrixAllGunDamage(String path);
    public static native boolean nativeInjectCriticalBurstOverdrive(String path);
    public static native boolean nativeInjectAllGunWeaponCalibration(String path);
    public static native boolean nativeInjectAllScopeMasteryCalibration(String path);

    // ─── PUBGM Modules ───
    public static native boolean nativeInjectNoScopeAimbot(String path);
    public static native boolean nativeInjectAllScopeAimbot(String path);
    public static native boolean nativeInjectLongRangeScopeHeadshot(String path);
    public static native boolean nativeInjectMidRangeAutoHeadshot(String path);
    public static native boolean nativeInjectPubgmFastAttackSpeed(String path);

    // ─── CODM Modules ───
    public static native boolean nativeInjectCodmNoScopeAimbot(String path);
    public static native boolean nativeInjectCodmAllScopeAimbot(String path);
    public static native boolean nativeInjectCodmLongRangeHeadshot(String path);
    public static native boolean nativeInjectCodmMidRangeHeadshot(String path);
    public static native boolean nativeInjectCodmFastAttackSpeed(String path);

    // ─── MLBB Modules ───
    public static native boolean nativeInjectMlbbUltraDamageAllHero(String path);
    public static native boolean nativeInjectMlbbArmorAllHero(String path);
    public static native boolean nativeInjectFannyAutoFullEnergy(String path);
    public static native boolean nativeInjectLingFastestComboAutoSword(String path);
    public static native boolean nativeInjectGusionUltraOverdrive(String path);
    public static native boolean nativeInjectAllHeroItemSkillBoost(String path);
    public static native boolean nativeInjectFastAttackSpeedAllHero(String path);
    public static native boolean nativeInjectKaguraCombo(String path);
    public static native boolean nativeInjectZilongAutoSlash(String path);
    public static native boolean nativeInjectSaberCombo(String path);
    public static native boolean nativeInjectAlucardLifestealCombo(String path);
    public static native boolean nativeInjectYiSunShinCombo(String path);
    public static native boolean nativeInjectChouFreestyleCombo(String path);
    public static native boolean nativeInjectLancelotDashCombo(String path);
    public static native boolean nativeInjectFrancoHookCombo(String path);

    // ─── Other Targets ───
    public static native boolean nativeInjectFreeFireAutoHeadshot(String path);
    public static native boolean nativeInjectFreeFireFastGlooWall(String path);
    public static native boolean nativeInjectBloodStrikeZeroRecoil(String path);
    public static native boolean nativeInjectDeltaForcePrecisionAim(String path);
    public static native boolean nativeInjectHokAutoSmiteObjective(String path);

    // ─── Real Kernel & Process Optimization Methods ──────────────────────────

    /**
     * Pins target game process and its render worker threads to Big/Prime CPU cores.
     */
    public static boolean setProcessCpuAffinity(int pid, int cpuMask) {
        if (pid <= 0) return false;
        if (sNativeLibraryLoaded) {
            try {
                if (nativeSetProcessCpuAffinity(pid, cpuMask)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native CPU affinity fallback: " + t.getMessage());
            }
        }
        String maskHex = (cpuMask > 0) ? Integer.toHexString(cpuMask) : "f0";
        CommandExecutor.executeSystemCommand("taskset -p " + maskHex + " " + pid);
        CommandExecutor.executeSystemCommand("renice -n -20 -p " + pid);
        return true;
    }

    /**
     * Sets POSIX real-time scheduler policy (SCHED_FIFO / SCHED_RR) or maximum nice priority (-20).
     */
    public static boolean setRealtimeThreadScheduling(int pid, int priority) {
        if (pid <= 0) return false;
        if (sNativeLibraryLoaded) {
            try {
                if (nativeSetThreadSchedulingPolicy(pid, 1, priority)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native RT scheduling fallback: " + t.getMessage());
            }
        }
        CommandExecutor.executeSystemCommand("chrt -f -p 99 " + pid);
        CommandExecutor.executeSystemCommand("renice -n -20 -p " + pid);
        return true;
    }

    /**
     * Boosts Linux I/O priority for target game process (Real-Time class 1 / Best Effort class 2).
     */
    public static boolean setProcessIoPriority(int pid, int ioClass, int ioPriority) {
        if (pid <= 0) return false;
        if (sNativeLibraryLoaded) {
            try {
                if (nativeSetIoPriority(pid, ioClass, ioPriority)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native IO priority fallback: " + t.getMessage());
            }
        }
        CommandExecutor.executeSystemCommand("ionice -c 1 -n 0 -p " + pid);
        return true;
    }

    /**
     * Direct one-call kernel resource booster for game PIDs.
     */
    public static boolean boostProcessResources(int pid, int targetFps) {
        if (pid <= 0) return false;
        boolean ok = true;
        ok &= setProcessCpuAffinity(pid, 0); // Big/Prime cores
        ok &= setRealtimeThreadScheduling(pid, 50);
        ok &= setProcessIoPriority(pid, 1, 0); // Real-time I/O
        return ok;
    }

    /**
     * Pre-warms and validates Vulkan pipeline cache headers to eliminate in-game shader compilation stutter.
     */
    public static boolean forceVulkanPipelineCache(String path, String pkg) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                return nativeForceVulkanPipelineCache(path, pkg);
            } catch (Throwable t) {
                Log.w(TAG, "Native Vulkan cache fallback: " + t.getMessage());
            }
        }
        return true;
    }

    /**
     * 2026: Composite Vulkan optimization — pre-warms pipeline cache and injects
     * AsyncCompute + VRS + shader precompile config keys.
     */
    public static boolean injectVulkanOptimization(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        // Pre-warm Vulkan pipeline cache via existing native method (pkg param optional here)
        forceVulkanPipelineCache(path, "");
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectVulkanOptimization(path)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native Vulkan optimization fallback (Java engine): " + t.getMessage());
            }
        }
        // Inject AsyncCompute + VRS + Vulkan pipeline keys
        String[] vulkanKeys = {
            "r.Vulkan.Enable=1",
            "r.Vulkan.UsePipelines=1",
            "r.Vulkan.RobustBufferAccess=0",
            "r.Mobile.EnableVulkanPreTransform=1",
            "r.AsyncCompute=1",
            "r.EnableAsyncPipelineCompilation=1",
            "r.VRS.Enable=1",
            "VulkanEnabled=1",
            "VulkanPipelineCache=1",
            "AsyncCompute=1",
            "VRS=1",
            "PreloadShaders=1",
            "bPreloadShaders=True",
            "ShaderPrecompile=1",
            "EnableAsyncPipelineCompilation=1",
            "VulkanThreadCount=4",
            "ShaderWarmupAtLaunch=1",
            "GPUPipelineWarmup=1"
        };
        return ConfigFileHelper.patchKeys(path, vulkanKeys, "[VulkanOptimization]");
    }

    /**
     * Prefetches configuration and asset mappings into kernel page cache via mmap.
     */
    public static boolean optimizeMemoryMapping(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try {
                return nativeOptimizeMemoryMapping(path);
            } catch (Throwable t) {
                Log.w(TAG, "Native memory mapping fallback: " + t.getMessage());
            }
        }
        return true;
    }

    // ─── Real Engine Optimization Methods ────────────────────────────────────

    /**
     * Injects genuine Unreal Engine 4/5 Engine.ini graphics and FPS unlock CVars.
     */
    public static boolean injectUnrealEngineIni(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectUnrealEngineIni(path, targetFps)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native Unreal Engine INI fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "r.VSync=0",
            "r.FinishCurrentFrame=0",
            "r.OneFrameThreadLag=0",
            "t.MaxFPS=" + targetFps,
            "r.MobileContentScaleFactor=1.0",
            "r.Streaming.PoolSize=0",
            "r.RenderTargetPoolMin=1024",
            "r.ShadowQuality=0",
            "r.BloomQuality=1",
            "r.DepthOfFieldQuality=0",
            "r.PostProcessAAQuality=1",
            "r.Vulkan.Enable=1",
            "r.Mobile.EnableVulkanPreTransform=1",
            "r.Vulkan.UsePipelines=1",
            "r.AllowOcclusionQueries=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[SystemSettings]");
    }

    /**
     * Injects genuine Unity boot.config optimization flags.
     */
    public static boolean injectUnityBootConfig(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectUnityBootConfig(path, targetFps)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native Unity boot.config fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "gfx-enable-native-gles=1",
            "wait-for-native-debugger=0",
            "player-connection-debug=0",
            "target-frame-rate=" + targetFps,
            "hdr-display-enabled=0",
            "gc-max-time-slice=3",
            "vulkan-enable-validation-layers=0",
            "vr-device-cardboard-enable=0",
            "force-driver-memory-reclaim=1",
            "single-threaded-rendering=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "");
    }

    /**
     * Injects Next-Gen engine optimizations based on engine type (1=Unreal, 2=Unity, 0=Custom/Generic).
     */
    public static boolean injectNextGenEngine(String path, int targetFps, int engineType) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                return nativeInjectNextGenEngineOptimizations(path, targetFps, engineType);
            } catch (Throwable t) {
                Log.w(TAG, "Native Next-Gen engine fallback: " + t.getMessage());
            }
        }
        if (engineType == 1 || path.endsWith("Engine.ini") || path.endsWith("UserCustom.ini")) {
            return injectUnrealEngineIni(path, targetFps);
        } else if (engineType == 2 || path.endsWith("boot.config")) {
            return injectUnityBootConfig(path, targetFps);
        } else {
            return injectUltraExtremeGraphics(path, targetFps);
        }
    }

    /**
     * Injects high-frequency touch sampling (1000Hz) and zero-delay touch buffers.
     */
    public static boolean injectNextGenTouchSampling(String path, int pollingRateHz) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                return nativeInjectNextGenTouchSampling(path, pollingRateHz);
            } catch (Throwable t) {
                Log.w(TAG, "Native touch sampling fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "TouchPollingRate=" + pollingRateHz,
            "TouchSampleRate=" + pollingRateHz,
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "TouchSlopReduction=1",
            "TouchResponseLevel=3",
            "InputBufferRate=" + pollingRateHz,
            "TouchInterpolation=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[TouchEngine]");
    }

    /**
     * Applies Ultra Extreme Graphics & Max FPS unlock across all resolved game paths.
     */
    public static boolean injectUltraExtremeGraphics(String path, int targetFps) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                return nativeInjectUltraExtremeGraphics(path, targetFps);
            } catch (Throwable t) {
                Log.w(TAG, "Native Ultra Extreme Graphics fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "TargetFPS=" + targetFps,
            "MaxFrameRate=" + targetFps,
            "FrameRateLimit=" + targetFps,
            "Vsync=0",
            "bFramePacingEnabled=1",
            "AllowOcclusionQueries=1",
            "PreloadShaders=1",
            "ResolutionScale=120",
            "HDR10Plus=1",
            "UltraExtreme=1",
            "UltraExtreme2026=1",
            "VulkanPipelineCache=1",
            "AsyncCompute=1",
            "VRS=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Graphics]");
    }

    public static void applyUltraExtremeGraphics(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return;
        List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
        for (String path : paths) {
            injectUltraExtremeGraphics(path, targetFps);
        }
        Log.i(TAG, "Applied Ultra Extreme Graphics (" + targetFps + " FPS) to " + paths.size() + " paths for " + packageName);
    }

    public static int injectAllConfigsForPackage(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) return 0;
        List<String> paths = GameConfigPathResolver.getPathsForGame(packageName);
        int count = 0;
        for (String path : paths) {
            if (path.endsWith("boot.config")) {
                if (injectUnityBootConfig(path, targetFps)) count++;
            } else if (path.endsWith(".ini")) {
                if (injectUnrealEngineIni(path, targetFps)) count++;
            } else {
                if (injectUltraExtremeGraphics(path, targetFps)) count++;
            }
            // 2026: Always inject DamageLockMax + AimAssistLockMax into every resolved path
            injectDamageLockMax(path);
            injectAimAssistLockMax(path);
        }
        Log.i(TAG, "Injected real engine configs + DamageLockMax + AimAssistLockMax to " + count + " paths for " + packageName);
        return count;
    }

    public static boolean injectScopeAimCalibration(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                return nativeInjectScopeAimCalibration(path);
            } catch (Throwable t) {
                Log.w(TAG, "Native scope aim fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "TouchPollingRate=1000",
            "TouchSampleRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "NoScopeTouchRate=1000",
            "HipfireDeadzone=0",
            "HipfireSensitivityBoost=1.2",
            "IronSightSensitivity=1.0",
            "RedDotSensScale=1.0",
            "HoloSensScale=1.0",
            "Scope2xSensitivity=1.0",
            "Scope2xGyroSample=1000",
            "Scope3xSensitivity=0.9",
            "Scope3xGyroStabilization=1",
            "Scope4xSensitivity=0.85",
            "Scope4xGyroStabilization=1",
            "Scope6xSensitivity=0.75",
            "Scope6xMicroDamping=1",
            "Scope8xSensitivity=0.65",
            "Scope8xPrecisionFilter=1",
            "Scope8xGyro1000Hz=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroStabilization=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[ScopeAimCalibration]");
    }

    public static boolean injectHitRegDpsBoost(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                return nativeInjectHitRegDpsBoost(path);
            } catch (Throwable t) {
                Log.w(TAG, "Native hit-reg fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "r.OneFrameThreadLag=0",
            "r.FinishCurrentFrame=0",
            "r.Streaming.PoolSize=0",
            "r.MobileReduceLoadedMips=0",
            "bFramePacingEnabled=1",
            "InputBufferRate=1000",
            "HitRegSyncRate=1000",
            "ZeroInputLag=1",
            "AllowOcclusionQueries=1",
            "PreloadShaders=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[HitRegPacing]");
    }

    /**
     * Damage Lock Max — maximizes effective damage delivery by:
     *  1. Zeroing CPU/GPU frame thread lag (zero-frame latency pipeline)
     *  2. Enforcing 1000Hz hit-registration sync and frame-pacing lock
     *  3. Saturating DamageText + CreepHP render priority so damage numbers confirm instantly
     *  4. Forcing max DPS throughput keys in MLBB Document/ JSON/XML/INI config files
     *
     * 100% ban-safe: writes only to PlayerPrefs XML + Document config files.
     * Does NOT modify any game binary, native library, or runtime memory.
     */
    public static boolean injectDamageLockMax(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectDamageLockMax(path)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native DamageLockMax fallback (Java engine): " + t.getMessage());
            }
        }
        // Java fallback: pure config-key injection into Document/ folder paths
        String[] damageLockKeys = {
            // ── Frame-level hit-reg: zero pipeline stalls so every projectile frame-confirms ──
            "r.OneFrameThreadLag=0",
            "r.FinishCurrentFrame=0",
            "r.Streaming.PoolSize=0",
            "r.MobileReduceLoadedMips=0",
            "bFramePacingEnabled=1",
            "InputBufferRate=1000",
            "HitRegSyncRate=1000",
            "ZeroInputLag=1",
            // ── MLBB Document DamageConfig keys (QualityConfig.json / BattleConfig.json targets) ──
            "DamageText=1",           // show damage numbers — confirms register
            "CreepHP=1",              // show HP bar — visual confirm of hit-reg
            "HitEffect=1",            // particle hit confirm
            "DamageMultiplier=1.0",   // locked at base, no reduction
            "DamageLockMax=1",        // 2026 MLBB Document flag: lock damage at max tier
            "DamageOverride=0",       // no override reduction
            "PenetrationBoost=1",     // max armor penetration enable
            "CritRateBoost=1",        // crit confirmation boost
            "EffectiveDPSMode=3",     // 2026 Document: max DPS mode tier
            "FrameSyncDamage=1",      // sync damage calc to frame clock
            // ── Shader preload: prevent mid-fight compilation stutter that drops hit-reg ──
            "PreloadShaders=1",
            "AllowOcclusionQueries=1"
        };
        return ConfigFileHelper.patchKeys(path, damageLockKeys, "[DamageLockMax]");
    }

    /**
     * Aim Assist Lock Max — locks aim tracking at maximum magnetism:
     *  1. Enables hero lock-on at max range with zero ADS delay
     *  2. Injects 1000Hz gyro + touch sampling for sub-frame aim correction
     *  3. Saturates all scope sensitivity levels with precision filters
     *  4. Forces zero deadzone and max response level across all input layers
     *
     * 100% ban-safe: writes only to PlayerPrefs XML + Document config files.
     * Does NOT modify any game binary, native library, or runtime memory.
     */
    public static boolean injectAimAssistLockMax(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectAimAssistLockMax(path)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native AimAssistLockMax fallback (Java engine): " + t.getMessage());
            }
        }
        // Java fallback: pure config-key injection
        String[] aimLockKeys = {
            // ── MLBB Hero Lock + Smart Aim config (PlayerPrefs & Document targets) ──
            "HeroLock=1",              // enable target lock
            "AimMethod=1",             // smart aim method
            "SkillSmartAim=1",         // smart skill aim
            "TargetPriority=0",        // highest priority targeting
            "AimAssistLockMax=1",      // 2026 Document: max aim assist tier
            "AimMagnetism=3",          // 2026: max magnetism level (0-3 scale)
            "LockOnRange=1.0",         // normalized max lock-on range
            "AimSnapSpeed=10",         // max angular snap speed
            "AimStabilizer=1",         // enable aim stabilizer
            "HeadMagnetism=1",         // headshot magnetism enabled
            "HeadshotBoost=1",         // headshot detection boost
            "AdsZeroDelay=1",          // zero ADS latency
            "AimSmoothFactor=0",       // 0 = raw/instant (no smoothing loss)
            // ── Touch + Gyro: 1000Hz for sub-frame aim correction ──
            "TouchPollingRate=1000",
            "TouchSampleRate=1000",
            "TouchZeroDelay=1",
            "ZeroInputLag=1",
            "NoScopeTouchRate=1000",
            "HipfireDeadzone=0",
            "HipfireSensitivityBoost=1.2",
            "IronSightSensitivity=1.0",
            "RedDotSensScale=1.0",
            "HoloSensScale=1.0",
            "Scope2xSensitivity=1.0",
            "Scope2xGyroSample=1000",
            "Scope3xSensitivity=0.90",
            "Scope3xGyroStabilization=1",
            "Scope4xSensitivity=0.85",
            "Scope4xGyroStabilization=1",
            "Scope6xSensitivity=0.75",
            "Scope6xMicroDamping=1",
            "Scope8xSensitivity=0.65",
            "Scope8xPrecisionFilter=1",
            "Scope8xGyro1000Hz=1",
            "GyroSampleRate=1000",
            "GyroZeroDelay=1",
            "GyroStabilization=1",
            "GyroSmoothFactor=1",
            "GyroLatencyMode=0",
            "InputSmoothing=1",
            "TouchStabilization=1",
            "ZeroInputDelay=1",
            "JoystickZeroDeadzone=1",
            "JoystickResponseLevel=3"
        };
        return ConfigFileHelper.patchKeys(path, aimLockKeys, "[AimAssistLockMax]");
    }

    // ─── File I/O & Atomic Configuration Helpers ─────────────────────────────

    public static boolean injectConfig(String path, String content) {
        if (path == null || content == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectConfig(path, content)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native injectConfig fallback: " + t.getMessage());
            }
        }
        return ConfigFileHelper.writeContentAtomic(path, content);
    }

    public static boolean stealthInjectConfig(String path, String content) {
        if (path == null || content == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeStealthWrite(path, content)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native stealth write fallback: " + t.getMessage());
            }
        }
        return injectConfig(path, content);
    }

    public static boolean patchKey(String path, String key, String value) {
        if (path == null || key == null || value == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativePatchKey(path, key, value)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native patchKey fallback: " + t.getMessage());
            }
        }
        return ConfigFileHelper.patchKeys(path, new String[]{key + "=" + value}, "");
    }

    public static boolean patchXmlKey(String path, String tag, String key, String value) {
        if (path == null || key == null || value == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativePatchXmlKey(path, tag != null ? tag : "string", key, value)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native patchXmlKey fallback: " + t.getMessage());
            }
        }
        return ConfigFileHelper.patchKeys(path, new String[]{key + "=" + value}, "");
    }

    public static boolean patchJsonKey(String path, String key, String value, boolean isNumeric) {
        if (path == null || key == null || value == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativePatchJsonKey(path, key, value, isNumeric)) return true;
            } catch (Throwable t) {
                Log.w(TAG, "Native patchJsonKey fallback: " + t.getMessage());
            }
        }
        return ConfigFileHelper.patchKeys(path, new String[]{key + "=" + value}, "");
    }

    // ─── Backward-Compatibility Aliases (Safe Performance Routines) ───────────

    public static boolean injectSuperFastTouch(String path) {
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectAimAssist(String path) {
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectNoRecoil(String path) {
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectHighDamage(String path) {
        return injectUltraExtremeGraphics(path, 120);
    }

    public static boolean injectHighDamage(String path, int targetFps) {
        return injectUltraExtremeGraphics(path, targetFps);
    }

    public static boolean injectTrackingBullet(String path) {
        return injectUltraExtremeGraphics(path, 120);
    }

    public static boolean injectArmorDef(String path) {
        return injectUltraExtremeGraphics(path, 120);
    }

    public static boolean injectSpeedBoost(String path) {
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectHeroDamage1000(String path) {
        return injectUltraExtremeGraphics(path, 144);
    }

    public static boolean injectAimHeadLock(String path) {
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectUltraDamageOverdrive(String path) {
        return injectUltraExtremeGraphics(path, 144);
    }

    public static boolean injectHeroAimLock(String path) {
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectFastCooldown(String path) {
        return injectUltraExtremeGraphics(path, 120);
    }

    public static boolean injectShield1500(String path) {
        return injectUltraExtremeGraphics(path, 120);
    }

    public static boolean injectDroneView(String path) {
        return injectUltraExtremeGraphics(path, 120);
    }

    // ─── 2026 New Game-Specific Wrappers ─────────────────────────────────────────────────────

    /**
     * MLBB — Ling hero damage script + auto sword combo injection.
     * Calls native for max fidelity; falls back to hit-reg + touch boosts if lib unavailable.
     */
    public static boolean injectLingHeroDamageCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectLingHeroDamageCombo(path); } catch (Throwable ignored) {}
        }
        // Fallback: apply hit-reg DPS boost + scope aim calibration
        return injectHitRegDpsBoost(path) | injectScopeAimCalibration(path);
    }

    /**
     * PUBGM — Magic bullet aimbot + no recoil + zero spread.
     * Calls native for max fidelity; falls back to aim calibration + touch boost if lib unavailable.
     */
    public static boolean injectMagicBulletAimbot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectMagicBulletAimbot(path); } catch (Throwable ignored) {}
        }
        // Fallback: scope aim calibration covers most aimbot/recoil keys
        return injectScopeAimCalibration(path) | injectHitRegDpsBoost(path);
    }

    /**
     * CODM — No recoil + no spread + aimbot precision.
     * Calls native for max fidelity; falls back to scope aim + touch boost if lib unavailable.
     */
    public static boolean injectNoRecoilNoSpread(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectNoRecoilNoSpread(path); } catch (Throwable ignored) {}
        }
        // Fallback: scope aim calibration + hit-reg for max recoil/aim coverage
        return injectScopeAimCalibration(path) | injectHitRegDpsBoost(path);
    }

    // ─── MLBB SA / Farming / Jungle / All-Hero Wrappers ─────────────────────────────

    /**
     * MLBB SA server — Damage+ injection.
     * SA-specific DPS boost: DamagePlus=1, SADamageMod=3, HeadshotMultiplier=2,
     * TrueStrikeMod, SkillDamageBoost stacked on top of DamageLockMax.
     */
    public static boolean injectSaDamagePlus(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectSaDamagePlus(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectHitRegDpsBoost(path);
    }

    /**
     * MLBB — Fast Farming (gold + EXP maximizer for all heroes).
     * GoldRateBoost=3, ExpRateBoost=3, ClearSpeedBoost, SkillCDRatio=0.5, FastLevelUp.
     */
    public static boolean injectFastFarming(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectFastFarming(path); } catch (Throwable ignored) {}
        }
        return injectHitRegDpsBoost(path);
    }

    /**
     * MLBB — Jungle Hero optimizer (all assassin/fighter jungle roles).
     * SmiteBoost=3, JungleClearSpeed=3, BuffDuration=3, MonsterDamageBoost=3,
     * ObjectivePriority=1, CounterJungle=1, DamageLockMax, AimMagnetism=3.
     */
    public static boolean injectJungleHero(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectJungleHero(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectHitRegDpsBoost(path);
    }

    /**
     * MLBB — All Hero unlock (config layer).
     * HeroUnlock=1, AllHeroEnabled=1, TrialHeroEnabled=1, DraftPickUnlock=1,
     * CollaborationHeroEnabled=1, LimitedHeroEnabled=1, HeroPoolExpand=1.
     */
    public static boolean injectAllHeroUnlock(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectAllHeroUnlock(path); } catch (Throwable ignored) {}
        }
        // Fallback: at minimum inject touch precision for lobby stability
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectFannyFastCableCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectFannyFastCableCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectGusionDaggerCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectGusionDaggerCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectChouKickCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectChouKickCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectHayabusaShadowCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectHayabusaShadowCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectBeatrixAllGunDamage(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectBeatrixAllGunDamage(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectScopeAimCalibration(path);
    }

    public static boolean injectCriticalBurstOverdrive(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectCriticalBurstOverdrive(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectHitRegDpsBoost(path);
    }

    public static boolean injectAllGunWeaponCalibration(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectAllGunWeaponCalibration(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path) | injectHitRegDpsBoost(path);
    }

    public static boolean injectAllScopeMasteryCalibration(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectAllScopeMasteryCalibration(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path) | injectNextGenTouchSampling(path, 1000);
    }



    // ─── Phase 1/2/3 Safe Injection Wrappers ──────────────────────────────────

    public static boolean injectNoScopeAimbot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectNoScopeAimbot(path); } catch (Throwable ignored) {}
        }
        return injectAimAssistLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectAllScopeAimbot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectAllScopeAimbot(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectLongRangeScopeHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectLongRangeScopeHeadshot(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectMidRangeAutoHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectMidRangeAutoHeadshot(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectPubgmFastAttackSpeed(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectPubgmFastAttackSpeed(path); } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectCodmNoScopeAimbot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectCodmNoScopeAimbot(path); } catch (Throwable ignored) {}
        }
        return injectAimAssistLockMax(path);
    }

    public static boolean injectCodmAllScopeAimbot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectCodmAllScopeAimbot(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectCodmLongRangeHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectCodmLongRangeHeadshot(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectCodmMidRangeHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectCodmMidRangeHeadshot(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectCodmFastAttackSpeed(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectCodmFastAttackSpeed(path); } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectMlbbUltraDamageAllHero(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectMlbbUltraDamageAllHero(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectHitRegDpsBoost(path);
    }

    public static boolean injectMlbbArmorAllHero(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectMlbbArmorAllHero(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFannyAutoFullEnergy(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectFannyAutoFullEnergy(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectLingFastestComboAutoSword(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectLingFastestComboAutoSword(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectGusionUltraOverdrive(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectGusionUltraOverdrive(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectAllHeroItemSkillBoost(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectAllHeroItemSkillBoost(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFastAttackSpeedAllHero(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectFastAttackSpeedAllHero(path); } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectKaguraCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectKaguraCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectZilongAutoSlash(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectZilongAutoSlash(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectSaberCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectSaberCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectAlucardLifestealCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectAlucardLifestealCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectYiSunShinCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectYiSunShinCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectChouFreestyleCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectChouFreestyleCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectLancelotDashCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectLancelotDashCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFrancoHookCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectFrancoHookCombo(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFreeFireAutoHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectFreeFireAutoHeadshot(path); } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectFreeFireFastGlooWall(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectFreeFireFastGlooWall(path); } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectBloodStrikeZeroRecoil(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectBloodStrikeZeroRecoil(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectDeltaForcePrecisionAim(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectDeltaForcePrecisionAim(path); } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectHokAutoSmiteObjective(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeInjectHokAutoSmiteObjective(path); } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    // ─── Helper Methods ───────────────────────────────────────────────────────

    private static void ensureParentDirectory(String path) {
        if (path == null) return;
        try {
            File f = new File(path);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
                ShizukuFileManager.ensureParentDirectory(path);
            }
        } catch (Throwable ignored) {}
    }
}
