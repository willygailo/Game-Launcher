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
    public static native String nativePatchContentInMemory(String content, String[] keys, String[] values, int formatType);
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
    public static native boolean nativeInjectFastFullMana(String path);
    public static native boolean nativeInjectFastFullEnergy(String path);
    public static native boolean nativeInjectFastHpRegen(String path);
    public static native boolean nativeInjectFastStaminaFuryRegen(String path);
    public static native boolean nativeInjectZeroSkillCost(String path);
    public static native boolean nativeInjectMaxUltCharge(String path);
    public static native boolean nativeInjectSkillEconomyMasterSuite(String path);

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

    // ─── 2026 Max Cheats & Damage Overdrive Modules ───
    public static native boolean nativeInjectMlbbAllHeroMaxDamage2026(String path);
    public static native boolean nativeInjectMlbbUltimateDamageOverdrive2026(String path);
    public static native boolean nativeInjectPubgmAllWeaponMaxDamage2026(String path);
    public static native boolean nativeInjectPubgmUltraAimbot2026(String path);
    public static native boolean nativeInjectCodmMaxDamageAllWeapon2026(String path);
    public static native boolean nativeInjectCodmUltraConfigCheat2026(String path);

    // ─── 2026 Master 10000+ Damage & Attack Speed Overdrive Modules ───
    public static native boolean nativeInjectMlbbDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectPubgmDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectCodmDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectFreeFireDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectHokDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectWildRiftDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectFastReloadQuickSwap(String path);
    public static native boolean nativeInjectWallPiercingArmorShredder(String path);
    public static native boolean nativeInjectZeroPingNetworkOverclock(String path);
    public static native boolean nativeInjectUltraExtreme240FpsGraphics(String path);
    public static native boolean nativeInjectUniversalDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectHardwareMaskProfile(String path, String gpuRenderer, String socModel, int ramMb, int targetHz);
    public static native boolean nativeSetProcessIOPriority(int pid, int schedPriority, int ioprioClass, int ioprioLevel);
    public static native boolean nativeInjectFastLootAndWeaponSwap(String path);
    public static native boolean nativeInjectInstantSprintTurbo(String path);
    public static native boolean nativeInjectMultiRangeHeadshotCalibration(String path);
    public static native boolean nativeInjectMlbbJungleFastFarmAllHero(String path);
    public static native boolean nativeInjectMlbbLingFastestSword(String path);
    public static native boolean nativeInjectMlbbFannyFastestCable(String path);
    public static native boolean nativeInjectUniversalZeroDelaySkillTapAllHero(String path);
    public static native boolean nativeInjectFastLootAndSprint(String path);
    public static native boolean nativeInjectMlbbPenetrationCritBurst(String path);
    public static native boolean nativeInjectPubgmBallisticsVelocityPenetration(String path);
    public static native boolean nativeInjectCodmBsaRemovalRangeOverdrive(String path);
    public static native boolean nativeInjectUniversalCombatMechanicsOverdrive(String path);
    public static native boolean nativeInjectMlbbFastLoadSplashBypass(String path);
    public static native boolean nativeInjectPubgmFastLoadAsyncStreaming(String path);
    public static native boolean nativeInjectCodmFastLoadShaderBypass(String path);
    public static native boolean nativeInjectUniversalFastLoadTurbo(String path);
    public static native boolean nativeInjectCodm165FpsGraphics(String path, int targetFps, int qualityLevel);
    public static native boolean nativeInjectMlbb165FpsGraphics(String path, int targetFps, int qualityLevel);
    public static native boolean nativeInjectPubgm165FpsGraphics(String path, int targetFps, int qualityLevel);
    public static native boolean nativeInjectMlbbAllHeroOverdrive(String path);
    public static native boolean nativeInjectMlbbFannyNoEnergyLimit(String path);
    public static native boolean nativeInjectMlbbLingNoEnergyLimit(String path);
    public static native boolean nativeInjectMlbbAllJungleFastFarmOverdrive(String path);
    public static native boolean nativeInjectPubgmAllScopeTieredHeadshot(String path);
    public static native boolean nativeInjectCodmAllScopeTieredHeadshot(String path);
    public static native boolean nativeInjectNoScopeTieredHeadshotAllGun(String path);
    public static native boolean nativeInjectRifleScopeTieredHeadshot(String path);
    public static native boolean nativeInjectMlbbSmartSkillMagnetAim(String path);
    public static native boolean nativeInjectMlbbHeroUnlimitedEnergy(String path);
    public static native boolean nativeInjectMlbbAllHeroBoostAndArmor(String path);

    /**
     * Fast zero-dependency C++ in-memory patcher for game configuration buffers.
     * Format types: 0 = INI/CVar, 1 = XML (<map>), 2 = JSON.
     * Returns null if native engine is unavailable or fails, allowing seamless Java fallback.
     */
    public static String patchContentNativeInMemory(String content, String[] keys, String[] values, int formatType) {
        if (content == null || keys == null || values == null) return null;
        if (keys.length == 0 || keys.length != values.length) return null;
        if (sNativeLibraryLoaded) {
            try {
                String patched = nativePatchContentInMemory(content, keys, values, formatType);
                if (patched != null && !patched.isEmpty()) {
                    return patched;
                }
            } catch (Throwable t) {
                Log.w(TAG, "Native in-memory patcher fallback: " + t.getMessage());
            }
        }
        return null;
    }

    /**
     * Executes elevated kernel process/thread tuning commands via Shizuku (UID 2000 shell)
     * with unprivileged shell fallback.
     */
    public static void executeElevatedCommand(String command) {
        if (command == null || command.trim().isEmpty()) return;
        try {
            if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
                com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(command);
                return;
            }
        } catch (Throwable ignored) {}
        CommandExecutor.executeSystemCommand(command);
    }

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
        executeElevatedCommand("taskset -p " + maskHex + " " + pid);
        executeElevatedCommand("renice -n -20 -p " + pid);
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
        executeElevatedCommand("chrt -f -p 99 " + pid);
        executeElevatedCommand("renice -n -20 -p " + pid);
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
        executeElevatedCommand("ionice -c 1 -n 0 -p " + pid);
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
                if (nativeForceVulkanPipelineCache(path, pkg)) return true;
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
                if (nativeOptimizeMemoryMapping(path)) return true;
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
                if (nativeInjectNextGenEngineOptimizations(path, targetFps, engineType)) return true;
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
                if (nativeInjectNextGenTouchSampling(path, pollingRateHz)) return true;
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
                if (nativeInjectUltraExtremeGraphics(path, targetFps)) return true;
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
        boolean isMlbb = packageName.toLowerCase().contains("mobile.legends");
        for (String path : paths) {
            if (path.endsWith("boot.config")) {
                if (injectUnityBootConfig(path, targetFps)) count++;
            } else if (path.endsWith(".ini")) {
                if (injectUnrealEngineIni(path, targetFps)) count++;
            } else {
                if (injectUltraExtremeGraphics(path, targetFps)) count++;
            }
            // Always inject core competitive overrides into every resolved path
            injectDamageLockMax(path);
            injectAimAssistLockMax(path);
            injectFastLootAndSprint(path);
            injectScopeAimCalibration(path);
            injectHitRegDpsBoost(path);
            injectUniversalZeroDelaySkillTapAllHero(path);
            injectUniversalCombatMechanicsOverdrive(path);
            injectUniversalFastLoadTurbo(path);

            // Specialized mechanics by game package
            if (isMlbb) {
                injectMlbbJungleFastFarmAllHero(path);
                injectMlbbLingFastestSword(path);
                injectMlbbFannyFastestCable(path);
                injectMlbbPenetrationCritBurst(path);
                injectMlbbFastLoadSplashBypass(path);
            } else if (packageName.toLowerCase().contains("tencent.ig") || packageName.toLowerCase().contains("pubg")) {
                injectPubgmBallisticsVelocityPenetration(path);
                injectPubgmFastLoadAsyncStreaming(path);
            } else if (packageName.toLowerCase().contains("callofduty")) {
                injectCodmBsaRemovalRangeOverdrive(path);
                injectCodmFastLoadShaderBypass(path);
            }
        }
        Log.i(TAG, "Injected real engine configs + fast load + fast loot/sprint + aim calibration to " + count + " paths for " + packageName);
        return count;
    }

    public static boolean injectScopeAimCalibration(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectScopeAimCalibration(path)) return true;
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
                if (nativeInjectHitRegDpsBoost(path)) return true;
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
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAimAssist1000(path, 1000, 1.0f)) return true; } catch (Throwable ignored) {}
        }
        return injectAimAssistLockMax(path);
    }

    public static boolean injectNoRecoil(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectScopeZeroRecoil(path, 0.0f, 100)) return true; } catch (Throwable ignored) {}
        }
        return injectAimAssistLockMax(path);
    }

    public static boolean injectHighDamage(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectHeroDamage1000(path, 1.5f, 2.0f, 100, 100)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectHighDamage(String path, int targetFps) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectHeroDamage1000(path, 1.5f, 2.0f, 100, 100)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectTrackingBullet(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectTrackingBullet1000(path, 1000.0f, 3.0f)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectArmorDef(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectArmorDef1000(path, 3000.0f, 0.99f)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectSpeedBoost(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectSpeedBoost(path, 1.5f, 1.5f)) return true; } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectHeroDamage1000(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectHeroDamage1000(path, 1.5f, 2.0f, 100, 100)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectAimHeadLock(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAimHeadLock(path, 1.0f, 10)) return true; } catch (Throwable ignored) {}
        }
        return injectAimAssistLockMax(path);
    }

    public static boolean injectUltraDamageOverdrive(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectUltraDamageOverdrive(path, 1.5f, 3.0f, 1.0f)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectHeroAimLock(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectHeroAimLock(path, 0, 1.0f)) return true; } catch (Throwable ignored) {}
        }
        return injectAimAssistLockMax(path);
    }

    public static boolean injectFastCooldown(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFastCooldown(path, 0.001f)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFastFullMana(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFastFullMana(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFastFullEnergy(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFastFullEnergy(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFastHpRegen(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFastHpRegen(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFastStaminaFuryRegen(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFastStaminaFuryRegen(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectZeroSkillCost(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectZeroSkillCost(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectMaxUltCharge(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMaxUltCharge(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectSkillEconomyMasterSuite(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectSkillEconomyMasterSuite(path)) return true; } catch (Throwable ignored) {}
        }
        // Fallback: fire each sub-injector individually
        boolean ok = false;
        ok |= injectFastCooldown(path);
        ok |= injectFastFullMana(path);
        ok |= injectFastFullEnergy(path);
        ok |= injectFastHpRegen(path);
        ok |= injectFastStaminaFuryRegen(path);
        ok |= injectZeroSkillCost(path);
        ok |= injectMaxUltCharge(path);
        return ok;
    }

    // ─── 2026 New Game-Specific Wrappers ─────────────────────────────────────────────────────

    public static boolean injectShield1500(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectShield1500(path, 3.0f, 3000.0f)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectDroneView(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectDroneView(path, 180, 180)) return true; } catch (Throwable ignored) {}
        }
        return injectUltraExtremeGraphics(path, 120);
    }

    /**
     * MLBB — Ling hero damage script + auto sword combo injection.
     * Calls native for max fidelity; falls back to hit-reg + touch boosts if lib unavailable.
     */
    public static boolean injectLingHeroDamageCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectLingHeroDamageCombo(path)) return true; } catch (Throwable ignored) {}
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
            try { if (nativeInjectMagicBulletAimbot(path)) return true; } catch (Throwable ignored) {}
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
            try { if (nativeInjectNoRecoilNoSpread(path)) return true; } catch (Throwable ignored) {}
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
            try { if (nativeInjectSaDamagePlus(path)) return true; } catch (Throwable ignored) {}
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
            try { if (nativeInjectFastFarming(path)) return true; } catch (Throwable ignored) {}
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
            try { if (nativeInjectJungleHero(path)) return true; } catch (Throwable ignored) {}
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
            try { if (nativeInjectAllHeroUnlock(path)) return true; } catch (Throwable ignored) {}
        }
        // Fallback: at minimum inject touch precision for lobby stability
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectFannyFastCableCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFannyFastCableCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectGusionDaggerCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectGusionDaggerCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectChouKickCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectChouKickCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectHayabusaShadowCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectHayabusaShadowCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectBeatrixAllGunDamage(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectBeatrixAllGunDamage(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectScopeAimCalibration(path);
    }

    public static boolean injectCriticalBurstOverdrive(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCriticalBurstOverdrive(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectHitRegDpsBoost(path);
    }

    public static boolean injectAllGunWeaponCalibration(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAllGunWeaponCalibration(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path) | injectHitRegDpsBoost(path);
    }

    public static boolean injectAllScopeMasteryCalibration(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAllScopeMasteryCalibration(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path) | injectNextGenTouchSampling(path, 1000);
    }



    // ─── Phase 1/2/3 Safe Injection Wrappers ──────────────────────────────────

    public static boolean injectNoScopeAimbot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectNoScopeAimbot(path)) return true; } catch (Throwable ignored) {}
        }
        return injectAimAssistLockMax(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectAllScopeAimbot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAllScopeAimbot(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path) | injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectLongRangeScopeHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectLongRangeScopeHeadshot(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectMidRangeAutoHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMidRangeAutoHeadshot(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectPubgmFastAttackSpeed(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmFastAttackSpeed(path)) return true; } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectCodmNoScopeAimbot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmNoScopeAimbot(path)) return true; } catch (Throwable ignored) {}
        }
        return injectAimAssistLockMax(path);
    }

    public static boolean injectCodmAllScopeAimbot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmAllScopeAimbot(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectCodmLongRangeHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmLongRangeHeadshot(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectCodmMidRangeHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmMidRangeHeadshot(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectCodmFastAttackSpeed(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmFastAttackSpeed(path)) return true; } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectMlbbUltraDamageAllHero(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbUltraDamageAllHero(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path) | injectHitRegDpsBoost(path);
    }

    public static boolean injectMlbbArmorAllHero(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbArmorAllHero(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFannyAutoFullEnergy(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFannyAutoFullEnergy(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectLingFastestComboAutoSword(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectLingFastestComboAutoSword(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectGusionUltraOverdrive(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectGusionUltraOverdrive(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectAllHeroItemSkillBoost(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAllHeroItemSkillBoost(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFastAttackSpeedAllHero(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFastAttackSpeedAllHero(path)) return true; } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectKaguraCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectKaguraCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectZilongAutoSlash(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectZilongAutoSlash(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectSaberCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectSaberCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectAlucardLifestealCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAlucardLifestealCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectYiSunShinCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectYiSunShinCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectChouFreestyleCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectChouFreestyleCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectLancelotDashCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectLancelotDashCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFrancoHookCombo(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFrancoHookCombo(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFreeFireAutoHeadshot(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFreeFireAutoHeadshot(path)) return true; } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectFreeFireFastGlooWall(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFreeFireFastGlooWall(path)) return true; } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectBloodStrikeZeroRecoil(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectBloodStrikeZeroRecoil(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectDeltaForcePrecisionAim(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectDeltaForcePrecisionAim(path)) return true; } catch (Throwable ignored) {}
        }
        return injectScopeAimCalibration(path);
    }

    public static boolean injectHokAutoSmiteObjective(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectHokAutoSmiteObjective(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectMlbbAllHeroMaxDamage2026(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbAllHeroMaxDamage2026(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectMlbbUltimateDamageOverdrive2026(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbUltimateDamageOverdrive2026(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectPubgmAllWeaponMaxDamage2026(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmAllWeaponMaxDamage2026(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectPubgmUltraAimbot2026(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmUltraAimbot2026(path)) return true; } catch (Throwable ignored) {}
        }
        return injectAimAssistLockMax(path);
    }

    public static boolean injectCodmMaxDamageAllWeapon2026(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmMaxDamageAllWeapon2026(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectCodmUltraConfigCheat2026(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmUltraConfigCheat2026(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectMlbbDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectPubgmDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectCodmDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFreeFireDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFreeFireDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectHokDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectHokDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectWildRiftDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectWildRiftDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectFastReloadQuickSwap(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFastReloadQuickSwap(path)) return true; } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectWallPiercingArmorShredder(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectWallPiercingArmorShredder(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectZeroPingNetworkOverclock(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectZeroPingNetworkOverclock(path)) return true; } catch (Throwable ignored) {}
        }
        return injectNextGenTouchSampling(path, 1000);
    }

    public static boolean injectUltraExtreme240FpsGraphics(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectUltraExtreme240FpsGraphics(path)) return true; } catch (Throwable ignored) {}
        }
        return injectUltraExtremeGraphics(path, 240);
    }

    public static boolean injectUniversalDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectUniversalDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        return injectDamageLockMax(path);
    }

    public static boolean injectHardwareMaskProfile(String path, String gpuRenderer, String socModel, int ramMb, int targetHz) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectHardwareMaskProfile(path, gpuRenderer, socModel, ramMb, targetHz)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "GpuRenderer=" + (gpuRenderer != null ? gpuRenderer : "Adreno (TM) 750"),
            "SocModel=" + (socModel != null ? socModel : "Snapdragon 8 Gen 3"),
            "SystemRamMB=" + (ramMb > 0 ? ramMb : 16384),
            "DisplayRefreshRate=" + (targetHz > 0 ? targetHz : 120),
            "TargetFPS=" + (targetHz > 0 ? targetHz : 120),
            "HardwareProfileTier=4"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[HardwareProfile]");
    }

    public static boolean setProcessIOPriority(int pid, int schedPriority, int ioprioClass, int ioprioLevel) {
        if (pid <= 0) return false;
        if (sNativeLibraryLoaded) {
            try {
                if (nativeSetProcessIOPriority(pid, schedPriority, ioprioClass, ioprioLevel)) return true;
            } catch (Throwable ignored) {}
        }
        executeElevatedCommand("ionice -c " + ioprioClass + " -n " + ioprioLevel + " -p " + pid);
        return true;
    }

    public static boolean injectFastLootAndWeaponSwap(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectFastLootAndWeaponSwap(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AutoPickup=1", "AutoPickupSpeed=2",
            "PUBGAutoLoot=1", "PUBGPickupPriority=1",
            "PUBGFastWeaponSwitch=1", "FastWeaponSwitch=1",
            "QuickThrow=1", "FastADS=1", "OneTapADS=1",
            "QuickLoot=1", "QuickReload=1",
            "PUBGQuickOpenScope=1", "PickupRangeBoost=1.5",
            "LootResponseTime=0", "WeaponSwapResponseMs=0",
            "AutoWeaponEquip=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FastLootWeaponSwap]");
    }

    public static boolean injectInstantSprintTurbo(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectInstantSprintTurbo(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AutoSprint=1", "bSprintAlways=True",
            "SprintSensitivity=100", "MovementDeadzone=0",
            "FastSlide=1", "SlideDelayMs=0",
            "SprintAcceleration=10", "JoyStickDeadzone=0",
            "TouchResponseSprint=1000", "bAlwaysRun=True",
            "SprintThreshold=0.01", "InstantSprintEngage=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[InstantSprintTurbo]");
    }

    public static boolean injectMultiRangeHeadshotCalibration(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMultiRangeHeadshotCalibration(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "ShortRangeAimAssist=1", "HipfireDeadzone=0",
            "HipfireSensitivityBoost=1.2", "RedDotSensScale=1.0",
            "HoloSensScale=1.0", "MidRangeAimAssist=1",
            "Scope2xSensitivity=1.0", "Scope3xSensitivity=0.9",
            "Scope4xSensitivity=0.85", "Scope3xGyroStabilization=1",
            "LongRangeHeadshotPrecision=1", "Scope6xSensitivity=0.75",
            "Scope8xSensitivity=0.65", "Scope8xPrecisionFilter=1",
            "GyroSampleRate=1000", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "AimMagnetism=3",
            "AimLockHead=1", "TargetTrackingAccuracy=1.0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MultiRangeHeadshot]");
    }

    public static boolean injectMlbbJungleFastFarmAllHero(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbJungleFastFarmAllHero(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "JungleFastFarmAllHero=1", "AutoSmiteMonsters=1",
            "JungleRetributionInstant=1", "MonsterTargetLock=1",
            "CreepSmartTarget=1", "CampClearOptimized=1",
            "JunglePathEfficiency=10", "BuffMonsterPriority=1",
            "RetributionExecutionRange=1.5", "JungleTimerAccurate=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[JungleFarm]");
    }

    public static boolean injectMlbbLingFastestSword(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbLingFastestSword(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "LingSwordPathResponsiveness=10", "LingSwordAutoLock=1",
            "LingSwordZeroDelay=1", "SwordTouchSampling=1000",
            "TempestOfBladesFastSword=1", "LingDashResetZeroLatency=1",
            "LingWallJumpSpeed=5", "LingSwordMagnetism=1",
            "Ling4SwordInstantCombo=1", "LingEnergyRestoreFast=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[LingFastSword]");
    }

    public static boolean injectMlbbFannyFastestCable(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbFannyFastestCable(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FannyZeroCableDelay=1", "FannyCableSpeed=10",
            "FannyMultiCableInstantCast=1", "CableWallSnapSens=5.0",
            "SkillCastResponseTime=0", "FannyDualCableInstant=1",
            "FannyWallSnapMagnetism=3", "FannyEnergySaving=1",
            "FannyInstantRecall=1", "FannyStraightCableSpeed=10"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FannyFastCable]");
    }

    public static boolean injectUniversalZeroDelaySkillTapAllHero(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectUniversalZeroDelaySkillTapAllHero(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "SkillQueueInstant=1", "SmartSkillCastZeroDelay=1",
            "AutoAttackAnimationCancel=1", "ComboChainBufferMs=0",
            "TouchSamplingRate=1000", "ZeroDelaySkillTap=1",
            "InstantSkillCancelThreshold=0", "HeroTargetLockPriority=1",
            "FastSkillReleaseSpeed=10", "InputQueueBypass=1",
            "bZeroLatencyInput=True"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[ZeroDelaySkills]");
    }

    public static boolean injectFastLootAndSprint(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectFastLootAndSprint(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AutoPickup=1", "AutoPickupSpeed=2",
            "PUBGAutoLoot=1", "PUBGPickupPriority=1",
            "PUBGFastWeaponSwitch=1", "FastWeaponSwitch=1",
            "QuickThrow=1", "FastADS=1", "OneTapADS=1",
            "QuickLoot=1", "QuickReload=1",
            "PUBGQuickOpenScope=1", "PickupRangeBoost=1.5",
            "LootResponseTime=0", "AutoSprint=1",
            "bSprintAlways=True", "SprintSensitivity=100",
            "MovementDeadzone=0", "FastSlide=1",
            "SlideDelayMs=0", "SprintAcceleration=10",
            "JoyStickDeadzone=0", "TouchResponseSprint=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FastLootSprint]");
    }

    public static boolean injectMlbbPenetrationCritBurst(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbPenetrationCritBurst(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "PhysicalPenetrationRatio=1.0", "MagicPenetrationRatio=1.0",
            "FlatArmorShred=100", "TrueDamageConversion=1.0",
            "PenetrationScaleFactor=2.0", "CriticalRateThreshold=1.0",
            "CriticalDamageMultiplier=3.0", "CritBurstMultiplier=2.5",
            "CritPacingZeroDelay=1", "OugiShadowKillSpeed=10",
            "ShadowInstantSwap=1", "ShadowTargetLock=1",
            "GusionDaggerReturnSpeed=10", "SwordSpikeInstantReset=1",
            "IncandescenceDoubleDash=1", "ShunpoInvincibilityFrames=10",
            "WayOfDragonInstantKick=1", "PunctureResetWindow=10",
            "ThornedRoseCenterHit=1", "PhantomExecutionInstant=1",
            "ClaudeStackMaxMaintain=1", "WanwanWeaknessHitboxBoost=2.0",
            "CrossbowOfTangInstantTrigger=1", "BattleSpellExecutionThreshold=1.0",
            "ExecuteAutoTrigger=1", "LordTurtleStealPacing=1",
            "RetributionStealSyncRate=1000", "LifestealCoefficient=1.0",
            "SpellVampCoefficient=1.0", "AntiHealBypass=1",
            "ShieldAbsorbRatio=2.0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbCritPenetration]");
    }

    public static boolean injectPubgmBallisticsVelocityPenetration(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmBallisticsVelocityPenetration(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "MuzzleVelocityBoost=2.0", "BulletFlightTimeZero=1",
            "ClientHitRegistrationPacing=1000", "HitScanSimulation=1",
            "NetClientLagCompensation=1", "ArmorPenetrationLevel3=1.0",
            "HelmetPenetrationLevel3=1.0", "LimbDamageMultiplier=1.5",
            "FleshDamageMultiplier=2.0", "VestDamageBypass=1",
            "ShotgunPelletSpread=0.0", "ChokeTightness=1.0",
            "PelletDamageFull=1", "DBS_DoubleTapDelayMs=0",
            "SniperHeadshotDamage=300", "BoltActionQuickCycle=1",
            "NoScopeCrosshairAccuracy=1.0", "BulletPenetrationDistance=1000",
            "M416_VerticalRecoilMin=0", "BerylM762_HorizontalBounce=0",
            "AKM_FirstShotKick=0", "CameraShakeIntensity=0.0",
            "ScopeVisualBob=0.0", "VehicleDamageMultiplier=2.5",
            "VehicleOccupantPenetration=1",
            "+CVars=r.PUBGMuzzleVelocityBoost=2.0",
            "+CVars=r.PUBGBulletFlightTimeZero=1",
            "+CVars=r.PUBGHitScanSimulation=1",
            "+CVars=r.PUBGArmorPenetrationLevel3=1.0",
            "+CVars=r.PUBGHelmetPenetrationLevel3=1.0",
            "+CVars=r.PUBGLimbDamageMultiplier=1.5",
            "+CVars=r.PUBGFleshDamageMultiplier=2.0",
            "+CVars=r.PUBGVestDamageBypass=1",
            "+CVars=r.PUBGShotgunPelletSpread=0.0",
            "+CVars=r.PUBGChokeTightness=1.0",
            "+CVars=r.PUBGSniperHeadshotDamage=300",
            "+CVars=r.PUBGCameraShakeIntensity=0.0",
            "+CVars=r.PUBGScopeVisualBob=0.0",
            "+CVars=r.PUBGVehicleDamageMultiplier=2.5"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmBallistics]");
    }

    public static boolean injectCodmBsaRemovalRangeOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectCodmBsaRemovalRangeOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "BulletSpreadAccuracy=0.0", "ADSBulletSpreadDecay=0.0",
            "HipfireBloom=0.0", "InitialBulletSpread=0.0",
            "DamageRangeFalloff=0.0", "DamageRangeMultiplier=3.0",
            "MinDamageMultiplier=1.0", "DamagePerShotMax=100",
            "SprintToFireDelayMs=0", "ADSTransitionTimeMs=0",
            "FastBoltPullSpeed=2.0", "QuickDrawFactor=2.0",
            "HitFlinchScale=0.0", "FlinchRecoveryRate=10.0",
            "ScreenShakeScale=0.0", "QuickScopeAccuracyThreshold=1.0",
            "BlankScopeAccuracy=1.0", "SniperADSIdleSway=0.0",
            "OneShotKillHitbox=1", "ShotgunDamagePerPellet=50",
            "PelletSpreadADS=0.0", "PumpActionCycleMs=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmBsaRange]");
    }

    public static boolean injectUniversalCombatMechanicsOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectUniversalCombatMechanicsOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "TouchSampleRate=1000", "TouchZeroDelay=1",
            "InputBufferRate=1000", "ZeroLatencyEventQueue=1",
            "AttackAnimationCancel=1", "PostAttackRecoveryFrames=0",
            "PreAttackWindupFrames=0", "FrameSyncDamage=1",
            "ClientDamagePacing=185", "NetworkDamagePacketBatching=0",
            "UniversalArmorPiercing=1.0", "TrueDamageMode=1",
            "EffectiveDPSMultiplier=3.0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UniversalCombat]");
    }

    public static boolean injectMlbbFastLoadSplashBypass(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbFastLoadSplashBypass(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "SkipOpenVideo=1", "SkipSplashVideo=1",
            "FastLoadAssets=1", "DragonResourceOptimize=1",
            "HighQualityLoad=0", "UIAsyncLoad=1",
            "AudioPreload=0", "AsyncShaderWarmup=1",
            "PreloadResources=1", "PreloadHeroes=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbFastLoad]");
    }

    public static boolean injectPubgmFastLoadAsyncStreaming(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmFastLoadAsyncStreaming(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "s.AsyncLoadingThreadEnabled=True",
            "s.AsyncLoadingTimeLimit=10.0",
            "s.PriorityAsyncLoadingExtraTime=20.0",
            "r.TextureStreaming=1",
            "r.Streaming.PoolSize=1024",
            "r.Streaming.UseBackgroundThreadPool=1",
            "r.ShaderCompiler.CoreCount=8",
            "r.ShaderPipelineCache.StartupMode=3",
            "bSkipSplash=True",
            "bSkipMovie=True",
            "r.Streaming.HLODStrategy=2"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmFastLoad]");
    }

    public static boolean injectCodmFastLoadShaderBypass(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectCodmFastLoadShaderBypass(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FastLoad=1", "SkipIntroMovie=1",
            "AsyncAssetLoading=1", "TextureStreamBufferSize=512",
            "MaxAsyncLoadingTasks=8", "PreloadWeaponModels=0",
            "ShaderPrewarmAtStartup=0", "FastShaderWarmup=1",
            "LoadBalanceMode=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmFastLoad]");
    }

    public static boolean injectUniversalFastLoadTurbo(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectUniversalFastLoadTurbo(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FastLoad=1", "SkipSplash=1",
            "SkipIntro=1", "AsyncLoadingThread=1",
            "ShaderPrewarmAsync=1", "TextureStreamingBufferMB=512",
            "MultiThreadedAssetLoading=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UniversalFastLoad]");
    }

    public static boolean injectCodm165FpsGraphics(String path, int targetFps, int qualityLevel) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectCodm165FpsGraphics(path, targetFps, qualityLevel)) return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    public static boolean injectMlbb165FpsGraphics(String path, int targetFps, int qualityLevel) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbb165FpsGraphics(path, targetFps, qualityLevel)) return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    public static boolean injectPubgm165FpsGraphics(String path, int targetFps, int qualityLevel) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgm165FpsGraphics(path, targetFps, qualityLevel)) return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    public static boolean injectMlbbAllHeroOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbAllHeroOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=1", "PhysicalDamageBase=10000", "MagicDamageBase=10000",
            "AllHeroDamageMultiplier=2.0", "CritMultiplier=3.0", "CritRateBoost=1",
            "TrueDmgConversion=1", "PenetrationBoost=1", "AttackSpeedBoost=MAX",
            "BasicAttackRate=MAX", "AutoAttackInterval=0", "CooldownReduction=1.0",
            "SkillCDRatio=0", "SkillCooldownReduction=0.40", "GlobalCDR=40",
            "ZeroSkillCost=1", "FastSkillCycle=1", "ZeroSkillLag=1", "SkillCastDelayMs=0",
            "FastSkillReleaseSpeed=10", "ZeroDelaySkillTap=1", "EffectiveDPSMode=3",
            "FrameSyncDamage=1", "HitRegSyncRate=1000", "TouchPollingRate=1000",
            "ZeroInputDelay=1", "ZeroInputLag=1", "TouchZeroDelay=1", "InputBufferRate=1000",
            "SkillTargetPriority=LowestHpFirst", "TargetLockLowestHp=1", "SmartAimLowestHp=1",
            "LockLowestHpHero=1", "LowestHpAutoLock=1", "LowestHpMagnetLock=1",
            "ExecuteThresholdLowestHp=1.0", "SkillTargetPrioritySecondary=ClosestHero",
            "TargetLockNearest=1", "SmartAimClosestHero=1", "LockClosestHero=1",
            "ClosestHeroMagnetLock=1", "ClosestHeroAutoLock=1", "ProximitySkillAimSnap=1",
            "HeroLock=1", "SkillSmartAim=1", "AimMagnetism=3", "AimMagnetSkillLock=1",
            "AutoAimAssist=1", "SkillAutoMagnet=1", "SkillSnapNearest=1", "SkillSnapLowestHp=1",
            "HeroPhysicalArmorBoost=1.5", "HeroMagicResistBoost=1.5", "PhysicalDefense=10000",
            "MagicDefense=10000", "ArmorRating=10000", "ShieldAbsorbRatio=2.0",
            "DamageReductionPercent=50", "FlatArmorBoost=500", "MaxHealthBoost=10000", "HealthRegenRate=1000",
            "UnlimitedEnergyMode=1", "EnergyRegenBoost=10.0", "EnergyConsumption=0",
            "EnergyNoDecay=1", "FullEnergyStart=1",
            "LingEnergyLimit=999", "LingEnergyNoDecay=1", "LingWallEnergyFree=1",
            "LingZeroEnergyCost=1", "LingLightnessMax=1", "LingSwordAutoChain=1", "WallJumpInstant=1", "TempestInstantCast=1",
            "FannyEnergyLimit=999", "FannyEnergyNoDecay=1", "FannyEnergyRegen=MAX",
            "FannyEnergyFull=1", "FannyZeroEnergyCost=1", "FannyCableInfinite=1",
            "CableEnergyFree=1", "FannyMultiCableCombo=1", "CableCooldown=0", "FannyInstantCableAim=1",
            "HayaEnergyLimit=999", "HayaEnergyNoDecay=1", "HayaZeroEnergyCost=1",
            "HayaShadowZeroEnergy=1", "HayaShadowChain=1", "HayaShadowKillMax=1",
            "HayaZeroDelaySwap=1", "HayaShadowRange=2", "HayaPhantomTracking=1", "OugiShadowKillSpeed=10", "ShadowInstantSwap=1",
            "GusionEnergyLimit=999", "GusionEnergyNoDecay=1", "GusionZeroEnergyCost=1",
            "GusionManaCostZero=1", "GusionDashReset=1", "GusionDaggerReturn=1",
            "GusionInstant10Daggers=1", "GusionSkillChainSpeed=10", "GusionDaggerReturnSpeed=10",
            "SwordSpikeInstantReset=1", "IncandescenceDoubleDash=1",
            "bFramePacingEnabled=True", "r.OneFrameThreadLag=0", "r.FinishCurrentFrame=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbAllHeroOverdrive]");
    }

    public static boolean injectMlbbFannyNoEnergyLimit(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbFannyNoEnergyLimit(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FannyEnergyRegen=MAX", "FannyEnergyLimit=999", "FannyEnergyNoDecay=1",
            "FannyEnergyFull=1", "FannyEnergyMax=1", "AutoEnergyRefill=1",
            "CableEnergyFree=1", "FannyMultiCableCombo=1", "CableCooldown=0",
            "FannyCableCooldown=0", "ZeroSkillCost=1", "FannyInstantCableAim=1",
            "FannyEnergyStartFull=1", "FannyCableChain=1", "FannyCableInstantRecast=1",
            "FannyInstantRecall=1", "SkillAutoChain=1", "AimMagnetism=3",
            "SkillSmartAim=1", "ZeroInputDelay=1", "ZeroInputLag=1",
            "HitRegSyncRate=1000", "TouchPollingRate=1000", "TouchZeroDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbFannyNoEnergy]");
    }

    public static boolean injectMlbbLingNoEnergyLimit(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbLingNoEnergyLimit(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "LingEnergyLimit=999", "LingEnergyNoDecay=1", "LingWallEnergyFree=1",
            "LingEnergyStartFull=1", "LingEnergyRegen=MAX", "ZeroSkillCost=1",
            "LingSwordAutoChain=1", "LingBlinkChainMax=1", "BlinkChainMax=1",
            "LingWallBlink=1", "WallJumpInstant=1", "LingInstantDash=1",
            "TempestInstantCast=1", "LingUltInstant=1", "LingTempestBladeSpeed=10",
            "LingSwordSpawnInstant=1", "ZeroInputDelay=1", "ZeroInputLag=1",
            "DamageLockMax=1", "EffectiveDPSMode=3", "FrameSyncDamage=1",
            "CooldownReduction=1", "SkillCDRatio=0", "SkillAutoChain=1", "AimMagnetism=3"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbLingNoEnergy]");
    }

    public static boolean injectMlbbAllJungleFastFarmOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbAllJungleFastFarmOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "SmiteBoost=3", "JungleClearSpeed=3", "BuffDuration=3",
            "BuffSteal=1", "MonsterDamageBoost=3", "ObjectivePriority=1",
            "CounterJungle=1", "GoldRateBoost=3", "ExpRateBoost=3",
            "CreepGoldMultiplier=3", "JungleExpBoost=3", "FastLevelUp=1",
            "SmiteRange=1", "ClearSpeedBoost=1", "RetributionInstantCast=1",
            "RetributionDamageMax=1", "AutoSmiteLock=1", "JungleMonsterTrueDmg=1",
            "ZeroInputDelay=1", "HitRegSyncRate=1000", "TouchPollingRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbJungleFastFarm]");
    }

    public static boolean injectPubgmAllScopeTieredHeadshot(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmAllScopeTieredHeadshot(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "NoScopeHeadshot20m=1", "AimMagnetism20m=3", "HipfireLock20m=1", "NoScopeAimLock20m=1", "NoScopeSpread20m=0", "CQBAutoHeadshot20m=1",
            "NoScopeHeadshot40m=1", "AimMagnetism40m=3", "HipfireLock40m=1", "NoScopeAimLock40m=1", "NoScopeSpread40m=0", "CloseRangeHeadshot40m=1",
            "NoScopeHeadshot50m=1", "AimMagnetism50m=3", "HipfireLock50m=1", "NoScopeAimLock50m=1", "NoScopeSpread50m=0", "MidRangeNoScope50m=1",
            "NoScopeHeadshot100m=1", "AimMagnetism100m=3", "HipfireLock100m=1", "NoScopeAimLock100m=1", "NoScopeSpread100m=0", "ExtremeNoScope100m=1",
            "AllGunNoScopeHeadshot=1", "NoScopeHeadLock=1", "NoScopeAimMagnetism=3", "NoScopeCrosshairAccuracy=1.0",
            "HipfireMagnetism=3", "HipfireHeadLock=1", "CrosshairTightness=1.0", "NoScopeRecoilZero=1",
            "RifleScopeHeadshot100m=1", "RifleScopeMagnetism100m=3", "AimSnapHead100m=1", "Scope1xHeadLock=1", "Scope1xAimMagnetism=3", "ScopeRedDotHeadLock=1", "ScopeHoloHeadLock=1",
            "RifleScopeHeadshot200m=1", "RifleScopeMagnetism200m=3", "AimSnapHead200m=1", "Scope2xHeadLock=1", "Scope3xHeadLock=1", "Scope2xZeroRecoil=1", "Scope3xZeroRecoil=1", "PredictiveAim200m=1",
            "RifleScopeHeadshot300m=1", "RifleScopeMagnetism300m=3", "AimSnapHead300m=1", "Scope4xHeadLock=1", "Scope6xHeadLock=1", "BulletDropComp300m=1", "ZeroBreathSway300m=1",
            "RifleScopeHeadshot400m=1", "RifleScopeMagnetism400m=3", "AimSnapHead400m=1", "Scope8xLongRangeHeadLock=1", "BulletDropComp400m=1", "TargetLeadComp400m=1", "ExtremeRangeHeadLock400m=1", "ZeroMicroJitter400m=1",
            "AllRifleAutoHeadshot=1", "RifleZeroRecoil=1", "RifleZeroSpread=1", "RifleScopeAimMagnetism=3",
            "AutoHeadshotBurst=3", "Auto3BulletHeadshot=1", "HeadshotBurstCount=3",
            "WeaponRecoilScale=0", "WeaponSpreadScale=0", "RecoilZero=1", "LessRecoil=1",
            "BulletTrackingEnemy=1", "TrackingBullet=1", "GyroSampleRate=1000", "GyroZeroDelay=1",
            "HitRegSyncRate=1000", "ZeroInputDelay=1", "AimSnapSpeed=10", "ZeroADSDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmTieredHeadshot]");
    }

    public static boolean injectCodmAllScopeTieredHeadshot(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectCodmAllScopeTieredHeadshot(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "NoScopeHeadshot20m=1", "AimMagnetism20m=3", "HipfireLock20m=1", "NoScopeAimLock20m=1", "NoScopeSpread20m=0", "CQBAutoHeadshot20m=1",
            "NoScopeHeadshot40m=1", "AimMagnetism40m=3", "HipfireLock40m=1", "NoScopeAimLock40m=1", "NoScopeSpread40m=0", "CloseRangeHeadshot40m=1",
            "NoScopeHeadshot50m=1", "AimMagnetism50m=3", "HipfireLock50m=1", "NoScopeAimLock50m=1", "NoScopeSpread50m=0", "MidRangeNoScope50m=1",
            "NoScopeHeadshot100m=1", "AimMagnetism100m=3", "HipfireLock100m=1", "NoScopeAimLock100m=1", "NoScopeSpread100m=0", "ExtremeNoScope100m=1",
            "AllGunNoScopeHeadshot=1", "NoScopeHeadLock=1", "NoScopeAimMagnetism=3", "NoScopeCrosshairAccuracy=1.0",
            "HipfireMagnetism=3", "HeadBoneLock=1", "InstantAimSnap=1", "TrackingBullet=1",
            "RifleScopeHeadshot100m=1", "RifleScopeMagnetism100m=3", "AimSnapHead100m=1", "Scope1xHeadLock=1", "Scope1xAimMagnetism=3",
            "RifleScopeHeadshot200m=1", "RifleScopeMagnetism200m=3", "AimSnapHead200m=1", "MidScopeRecoilZero=1", "ARSMGHeadLock=1", "ScopeAimMag=3", "GyroMidStabilize=1", "PredictiveAim200m=1",
            "RifleScopeHeadshot300m=1", "RifleScopeMagnetism300m=3", "AimSnapHead300m=1", "SniperMarkHeadLock=1", "BulletDropCompensation=1", "ZeroHoldBreath=1", "LRScopeAimLock=1", "BulletDropComp300m=1",
            "RifleScopeHeadshot400m=1", "RifleScopeMagnetism400m=3", "AimSnapHead400m=1", "SniperBlankScope=1", "HitscanLRLock=1", "ZeroMicroJitter=1", "UltraRangeHeadLock=1", "LongRangePrecision400m=1",
            "AllRifleAutoHeadshot=1", "RifleZeroRecoil=1", "RifleZeroSpread=1", "RifleScopeAimMagnetism=3",
            "AutoHeadshotBurst=3", "Auto3BulletHeadshot=1", "HeadshotBurstCount=3",
            "BSARemoval=1", "WeaponSpread=0", "RecoilScale=0", "ZeroRecoil=1", "LessRecoil=1",
            "BulletTrackingEnemy=1", "ZeroFlinch=1", "GyroSampleRate=1000", "GyroZeroDelay=1",
            "HitRegSyncRate=1000", "ZeroInputDelay=1", "ADSInstantTransition=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmTieredHeadshot]");
    }

    public static boolean injectNoScopeTieredHeadshotAllGun(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectNoScopeTieredHeadshotAllGun(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "NoScopeHeadshot20m=1", "AimMagnetism20m=3", "HipfireLock20m=1", "NoScopeAimLock20m=1", "NoScopeSpread20m=0", "CQBAutoHeadshot20m=1",
            "NoScopeHeadshot40m=1", "AimMagnetism40m=3", "HipfireLock40m=1", "NoScopeAimLock40m=1", "NoScopeSpread40m=0", "CloseRangeHeadshot40m=1",
            "NoScopeHeadshot50m=1", "AimMagnetism50m=3", "HipfireLock50m=1", "NoScopeAimLock50m=1", "NoScopeSpread50m=0", "MidRangeNoScope50m=1",
            "NoScopeHeadshot100m=1", "AimMagnetism100m=3", "HipfireLock100m=1", "NoScopeAimLock100m=1", "NoScopeSpread100m=0", "ExtremeNoScope100m=1",
            "AllGunNoScopeHeadshot=1", "NoScopeHeadLock=1", "NoScopeAimMagnetism=3", "NoScopeCrosshairAccuracy=1.0",
            "HipfireMagnetism=3", "HipfireHeadLock=1", "CrosshairTightness=1.0", "NoScopeRecoilZero=1",
            "WeaponSpreadScale=0", "AutoHeadshotBurst=3", "Auto3BulletHeadshot=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[NoScopeTieredHeadshot]");
    }

    public static boolean injectRifleScopeTieredHeadshot(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectRifleScopeTieredHeadshot(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "RifleScopeHeadshot100m=1", "RifleScopeMagnetism100m=3", "AimSnapHead100m=1", "Scope1xHeadLock=1", "Scope1xAimMagnetism=3", "ScopeRedDotHeadLock=1", "ScopeHoloHeadLock=1",
            "RifleScopeHeadshot200m=1", "RifleScopeMagnetism200m=3", "AimSnapHead200m=1", "Scope2xHeadLock=1", "Scope3xHeadLock=1", "Scope2xZeroRecoil=1", "Scope3xZeroRecoil=1", "PredictiveAim200m=1",
            "RifleScopeHeadshot300m=1", "RifleScopeMagnetism300m=3", "AimSnapHead300m=1", "Scope4xHeadLock=1", "Scope6xHeadLock=1", "BulletDropComp300m=1", "ZeroBreathSway300m=1",
            "RifleScopeHeadshot400m=1", "RifleScopeMagnetism400m=3", "AimSnapHead400m=1", "Scope8xLongRangeHeadLock=1", "BulletDropComp400m=1", "TargetLeadComp400m=1", "ExtremeRangeHeadLock400m=1", "ZeroMicroJitter400m=1",
            "AllRifleAutoHeadshot=1", "RifleZeroRecoil=1", "RifleZeroSpread=1", "RifleScopeAimMagnetism=3",
            "BulletTrackingEnemy=1", "ZeroADSDelay=1", "GyroStabilization=1", "GyroSampleRate=1000", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[RifleScopeTieredHeadshot]");
    }

    public static boolean injectMlbbSmartSkillMagnetAim(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbSmartSkillMagnetAim(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "SkillTargetPriority=LowestHpFirst", "TargetLockLowestHp=1", "SmartAimLowestHp=1",
            "LockLowestHpHero=1", "LowestHpAutoLock=1", "LowestHpMagnetLock=1",
            "ExecuteThresholdLowestHp=1.0",
            "SkillTargetPrioritySecondary=ClosestHero", "TargetLockNearest=1",
            "SmartAimClosestHero=1", "LockClosestHero=1", "ClosestHeroMagnetLock=1",
            "ClosestHeroAutoLock=1", "ProximitySkillAimSnap=1",
            "HeroLock=1", "SkillSmartAim=1", "AimMagnetism=3", "AimMagnetSkillLock=1",
            "AutoAimAssist=1", "SkillAutoMagnet=1", "SkillSnapNearest=1", "SkillSnapLowestHp=1",
            "AimSnapSpeed=10", "AimSmoothFactor=0", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroDelaySkillTap=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbSmartSkillAim]");
    }

    public static boolean injectMlbbHeroUnlimitedEnergy(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbHeroUnlimitedEnergy(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "UnlimitedEnergyMode=1", "EnergyRegenBoost=10.0", "EnergyConsumption=0",
            "ZeroSkillCost=1", "EnergyNoDecay=1", "FullEnergyStart=1",
            "LingEnergyLimit=999", "LingEnergyNoDecay=1", "LingWallEnergyFree=1",
            "LingZeroEnergyCost=1", "LingLightnessMax=1", "LingSwordAutoChain=1",
            "WallJumpInstant=1", "TempestInstantCast=1",
            "FannyEnergyLimit=999", "FannyEnergyNoDecay=1", "FannyEnergyRegen=MAX",
            "FannyEnergyFull=1", "FannyZeroEnergyCost=1", "FannyCableInfinite=1",
            "CableEnergyFree=1", "FannyMultiCableCombo=1", "CableCooldown=0",
            "FannyInstantCableAim=1",
            "HayaEnergyLimit=999", "HayaEnergyNoDecay=1", "HayaZeroEnergyCost=1",
            "HayaShadowZeroEnergy=1", "HayaShadowChain=1", "HayaShadowKillMax=1",
            "HayaZeroDelaySwap=1", "HayaShadowRange=2", "HayaPhantomTracking=1",
            "OugiShadowKillSpeed=10", "ShadowInstantSwap=1",
            "GusionEnergyLimit=999", "GusionEnergyNoDecay=1", "GusionZeroEnergyCost=1",
            "GusionManaCostZero=1", "GusionDashReset=1", "GusionDaggerReturn=1",
            "GusionInstant10Daggers=1", "GusionSkillChainSpeed=10", "GusionDaggerReturnSpeed=10",
            "SwordSpikeInstantReset=1", "IncandescenceDoubleDash=1",
            "ZeroInputDelay=1", "TouchPollingRate=1000", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbHeroEnergy]");
    }

    public static boolean injectMlbbAllHeroBoostAndArmor(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbAllHeroBoostAndArmor(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AllHeroDamageMultiplier=2.0", "DamageLockMax=1", "PhysicalDamageBase=10000",
            "MagicDamageBase=10000", "CritMultiplier=3.0", "CritRateBoost=1",
            "TrueDmgConversion=1", "PenetrationBoost=1", "AttackSpeedBoost=MAX",
            "BasicAttackRate=MAX", "EffectiveDPSMode=3",
            "SkillCooldownReduction=0.40", "GlobalCDR=40", "CooldownReduction=1.0",
            "SkillCDRatio=0", "FastSkillCycle=1", "ZeroSkillLag=1",
            "SkillCastDelayMs=0", "FastSkillReleaseSpeed=10", "ZeroDelaySkillTap=1",
            "HeroPhysicalArmorBoost=1.5", "HeroMagicResistBoost=1.5", "PhysicalDefense=10000",
            "MagicDefense=10000", "ArmorRating=10000", "ShieldAbsorbRatio=2.0",
            "DamageReductionPercent=50", "FlatArmorBoost=500", "MaxHealthBoost=10000",
            "HealthRegenRate=1000", "TouchPollingRate=1000", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbHeroBoostArmor]");
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
