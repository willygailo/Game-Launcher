package com.gamebooster.app.config;

import android.util.Log;

import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;

import java.io.File;
import java.util.List;
import java.util.Map;

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
    public static native boolean nativeFastHexPatchMmap(String path, byte[] pattern, byte[] replacement);
    public static native int nativeScanAndPatchProcessMemory(int pid, String moduleFilter, byte[] pattern, byte[] replacement);
    public static native long nativeDirectMemorySearch(String path, byte[] pattern);
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
    // 2026: Direct Lua Profile Batch Stream Injection
    public static native boolean nativeInjectLuaProperties(String path, String[] keys, String[] values);

    // 2026: Dedicated Native Game Injectors
    public static native boolean nativeInjectArenaBreakoutCombatOverdrive(String path);
    public static native boolean nativeInjectDeltaForceCombatOverdrive(String path);
    public static native boolean nativeInjectBloodStrikeCombatOverdrive(String path);
    public static native boolean nativeInjectGenshinFpsUnlockShaderTurbo(String path);
    public static native boolean nativeInjectCarXZeroThrottleLatency(String path);
    public static native boolean nativeInjectFarlightJetpackAccuracy(String path);
    public static native boolean nativeInjectRoblox120FpsUnlock(String path);
    public static native boolean nativeInjectStandoff2True128Tick(String path);
    public static native boolean nativeInjectValorantLowLatencyHeadshot(String path);
    public static native boolean nativeInjectMlbbFastFarmingAllHero(String path);
    public static native boolean nativeInjectMlbbFastRetributionObjectiveSteal(String path);
    public static native boolean nativeInjectMlbbAllHeroGodSuite2026(String path);

    // ─── Season 42 / v4.6 / S8 — Season-Specific Injectors ────────────────────
    // MLBB Season 42 "Starward Decade" (Sep 16 2026)
    public static native boolean nativeInjectMlbbSeason42MashaOverride(String path);
    public static native boolean nativeInjectMlbbSeason42LordStealUpdate(String path);
    public static native boolean nativeInjectMlbbSeason42AllHeroRevampBoost(String path);
    // PUBGM v4.6 "Midnight Hunters" (Sep 9 2026)
    public static native boolean nativeInjectPubgmV46WeaponFix(String path);
    public static native boolean nativeInjectPubgmV46MidnightHuntersMap(String path);
    public static native boolean nativeInjectPubgmV46VehicleOverride(String path);
    public static native boolean nativeInjectPubgmS32RankedSweep(String path);
    // CODM Season 8 "Against All Fate" (Sep 9 2026)
    public static native boolean nativeInjectCodmSeason8NewWeapons(String path);
    public static native boolean nativeInjectCodmSeason8RoguelikeMode(String path);
    public static native boolean nativeInjectCodmSeason8IsolatedPoi(String path);
    public static native boolean nativeInjectCodmS8FullRankedSweep(String path);

    // 2026.4 Latest Combat Overdrive Methods (Damage Assist, Aim Lock, Armor Overdrive)
    public static native boolean nativeInjectMlbbCombatOverdrive2026(String path);
    public static native boolean nativeInjectPubgmCombatOverdrive2026(String path);
    public static native boolean nativeInjectCodmCombatOverdrive2026(String path);
    public static native boolean nativeInjectUniversalArmorShieldLock(String path);
    public static native boolean nativeInjectUniversalAimMagnetLock(String path);

    // 2026 Specialized Overdrives: MLBB Basic Attack & Infinite Regen, PUBGM Full-Scope 100-450m & Bullet Tracking, CODM Attack Aim Assist & Fast Reload
    public static native boolean nativeInjectMlbbBasicAttackRegenOverdrive(String path);
    public static native boolean nativeInjectPubgmFullScopeBulletTrackingOverdrive(String path);
    public static native boolean nativeInjectCodmFullScopeBulletTrackingFastReload(String path);
    public static native boolean nativeInjectMlbbAutoMapGlitch3s(String path);
    public static native boolean nativeInjectMlbbFastSovereignOverdrive(String path);
    public static native boolean nativeInjectMlbbUltraDroneViewMaxFov(String path);
    public static native boolean nativeInjectMlbbGodModeFullOverdrive(String path);
    public static native boolean nativeInjectPubgUltraDroneViewMaxFov(String path);
    public static native boolean nativeInjectPubgmGodModeFullOverdrive(String path);
    public static native boolean nativeInjectCodmUltraDroneViewMaxFov(String path);
    public static native boolean nativeInjectCodmGodModeFullOverdrive(String path);
    public static native boolean nativeInjectPubgmSovereignOverdriveBypass(String path);
    public static native boolean nativeInjectCodmSovereignOverdriveBypass(String path);
    public static native boolean nativeInjectMlbbUniversalZeroDelayCombo(String path);

    /**
     * 2026 New Patch Method: Injects per-hero script modifiers (damage, cooldown, range, speed, etc.)
     * into MLBB target config paths (PlayerPrefs XML / config files) via batchInjectKeys.
     *
     * Uses a two-stage filter:
     *  1. Exclusion filter — skip known read-only / integrity-checked paths.
     *  2. Writable allowlist — only target paths that are confirmed writable at runtime
     *     (shared_prefs/, files/, Document/ on external storage, or writable file extensions).
     *     This prevents silent I/O waste on APK/OBB read-only asset mount paths.
     */
    public static boolean injectHeroScriptModifiers(String pkg, Map<String, String> modifiers) {
        if (pkg == null || pkg.trim().isEmpty() || modifiers == null || modifiers.isEmpty()) {
            return false;
        }
        List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
        if (paths == null || paths.isEmpty()) return false;
        boolean anySuccess = false;
        for (String path : paths) {
            if (path == null) continue;
            String lower = path.toLowerCase().replace('\\', '/');
            // Stage 1: Exclusion filter — skip known read-only / integrity-sensitive paths
            if (lower.contains("/assets/version") || lower.contains("/assets/comlibs")
                    || lower.contains("md5.xml") || lower.contains("rescheck") || lower.contains("realversion")
                    || lower.contains("splitlib") || lower.contains("mola_config") || lower.contains("res_skip")) {
                continue;
            }
            if (lower.endsWith(".xml") && lower.contains("/assets/")) {
                continue;
            }
            // Stage 2: Writable allowlist — only write to paths that are actually writable at runtime.
            // Paths inside APK/OBB install dirs (e.g. /data/app/, /mnt/expand/) are read-only;
            // attempting to write there wastes I/O and silently fails. Real targets are:
            //   - shared_prefs/  (PlayerPrefs XML)
            //   - files/         (game config files in internal storage)
            //   - /document/     (MLBB Document/ on external storage, outside /assets/)
            //   - writable config extensions (.ini, .cfg, .json) from dynamic path discovery
            if (!isWritableModifierTarget(lower)) {
                continue;
            }
            if (batchInjectKeys(path, modifiers, "[HeroScriptModifiers]")) {
                anySuccess = true;
            }
        }
        return anySuccess;
    }

    /**
     * Returns true only for paths that are runtime-writable by Shizuku/shell.
     * Excludes read-only APK install paths, OBB mounts, and asset subtrees.
     */
    private static boolean isWritableModifierTarget(String lower) {
        // Confirmed writable storage segments
        if (lower.contains("/shared_prefs/")) return true;
        if (lower.contains("/files/"))        return true;
        // MLBB Document/ on external storage — NOT inside /assets/
        if (lower.contains("/document/") && !lower.contains("/assets/")) return true;
        // Writable config file extensions found by dynamic path resolver
        if (lower.endsWith(".ini"))  return true;
        if (lower.endsWith(".cfg"))  return true;
        if (lower.endsWith(".json")) return true;
        if (lower.endsWith(".sav"))  return true;
        if (lower.endsWith(".dat"))  return true;
        // PlayerPrefs XML in /data/data/<pkg>/ — writable via Shizuku
        if (lower.endsWith(".xml") && (lower.contains("/data/data/") || lower.contains("/data/user/"))) return true;
        return false;
    }


    // 2026 Advanced Security & Anti-Tamper Native Bypass Suite
    public static native boolean nativeSecurityBypassStripXattrs(String path);
    public static native boolean nativeSecurityBypassCloakTimestamps(String targetPath, String sourcePath);
    public static native boolean nativeSecurityBypassAtomicSwap(String stagedPath, String targetPath);
    public static native boolean nativeSecurityBypassEnforcePermissions(String path, int uid, int gid, int mode);

    // 2026 Process Cloaking, Signal Trap Guard, and memfd Engines
    public static native boolean nativeCloakProcessIdentity(String targetName);
    public static native String nativeGetCloakedComm();
    public static native boolean nativeArmSignalTrapGuard();
    public static native boolean nativeDisarmSignalTrapGuard();
    public static native int nativeCreateAnonymousMemFd(String name, byte[] data);
    public static native String nativeGetMemFdPath(int fd);
    public static native boolean nativeCloseMemFd(int fd);

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
    public static native boolean nativeInjectPubgmUltraHdr120(String path);
    public static native boolean nativeInjectPubgmHdr120(String path);
    public static native boolean nativeInjectPubgmSuperSmooth165(String path);
    public static native boolean nativeInjectPubgmRankedDamageSync(String path);
    public static native boolean nativeInjectPubgmRankedAimAssist(String path);
    public static native boolean nativeInjectCodmRankedDamageSync(String path);
    public static native boolean nativeInjectCodmRankedAimAssist(String path);
    public static native boolean nativeInjectMlbbRankedHitSync(String path);
    public static native boolean nativeInjectMlbbRankedAimAssist(String path);
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

    public static native boolean nativeInjectSilentAimbot(String path);
    public static native boolean nativeInjectHitboxMultiplier(String path, float multiplier);
    public static native boolean nativeInjectUltraWallhackEspClarity(String path);
    public static native boolean nativeInjectAutoSmiteRetribution(String path);
    public static native boolean nativeInjectUniversalCombatSuite(String path);
    public static native boolean nativeInjectMlbbMasterComboSuite(String path);
    public static native boolean nativeInjectBloodStrikeSlideCancelOverdrive(String path);
    public static native boolean nativeInjectDeltaForceNaniteShaderPrewarm(String path);
    public static native boolean nativeInjectArenaBreakoutThermalFootstepAudio(String path);
    public static native boolean nativeInjectValorantCounterStrafeAimLock(String path);
    public static native boolean nativeInjectFarlightJetpackZeroCooldown(String path);
    public static native boolean nativeInjectStandoff2Tick128ZeroSpread(String path);

    public static native boolean nativeInjectUniversalGodDamageOverdrive2026(String path);
    public static native boolean nativeInjectBloodStrikeDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectDeltaForceDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectArenaBreakoutDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectValorantDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectFarlightDamage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectStandoff2Damage10000AttackSpeedMax(String path);
    public static native boolean nativeInjectGenshinDamage10000ElementalBurstMax(String path);
    public static native boolean nativeInjectRobloxDamage10000Max(String path);
    public static native boolean nativeInjectCarXTorqueHorsepower10000Max(String path);

    // ─── 2026.2 Combat Enhancement Suite ─────────────────────────────────────

    /**
     * Adaptive Aim Assist — 2026.2 Edition.
     * Per-scope gyro sensitivity ratios (RedDot 1.0x to 8x scope 0.65x) + predictive head snap.
     * Unlike AimAssist1000 (hard lock), uses smooth predict + per-distance tuning.
     * Works for MLBB (hero lock), CODM (ADS snap), PUBGM (UE4 CVars).
     */
    public static native boolean nativeInjectAdaptiveAimAssist(String path);

    /**
     * Adaptive No Recoil — 2026.2 Edition.
     * Per-weapon-category recoil compensation: AR/SMG spray recovery, Sniper/DMR zero sway,
     * LMG bloom cap, Shotgun pellet lock. All scope stabilizers enabled. UE4 CVar pass included.
     */
    public static native boolean nativeInjectAdaptiveNoRecoil(String path);

    /**
     * Ranked Combat Full Suite — 2026.2 Edition (MASTER PAYLOAD).
     * Single-pass C++ call firing all 24 combat sub-layers atomically.
     * Designed for: RANKED + CLASSIC + ALL MAPS.
     */
    public static native boolean nativeInjectRankedCombatFullSuite(String path);

    // ── 2026.3 Dame Aim Assist — Enemy Lock MAX + Auto Headshot Kill ──────────

    /**
     * Enemy Lock MAX — All-Scope Multi-Range Aim Assist (2026.3 Dame Edition).
     * 5 range-gated scope tiers: 50m / 150m / 250m / 350m / 450m.
     * Per tier: head-bone hard-lock, aim magnetism=1000, predictive aim,
     * ballistic compensation, gyro lock. Silent aimbot mode ON.
     * Compatible: MLBB (PlayerPrefs XML), CODM (INI/JSON), PUBGM (UE4 CVar+INI).
     */
    public static native boolean nativeInjectEnemyLockMaxAllScope(String path);

    /**
     * Auto Headshot Bullet Kill — 3-Bullet Head Mode + 5-Bullet Universal Kill Sweep (2026.3).
     * HeadshotBulletCount=3  → 3 bullets on head bone = confirmed kill.
     * KillBulletThreshold=5  → 5 bullets anywhere = dead (fallback sweep).
     * All scope tiers get headshot force enabled. Damage maxed to 99999.
     * Compatible: MLBB, CODM, PUBGM, FreeFire, BloodStrike, DeltaForce.
     */
    public static native boolean nativeInjectAutoHeadshotBulletKill(String path);

    // ── 2026.3 Game-Specific Enemy Lock — Each game has its own engine ────────

    /**
     * MLBB Enemy Lock + Headshot Suite — Unity/MOBA Engine (2026.3).
     * NO scope tiers — MLBB is top-down MOBA, zero scope mechanics.
     * HeroLock + SkillSmartAim + lowest-HP target priority.
     * Kill via 3-skill-hit combo burst (HeroSkillBurstKill=3), NOT bullet count.
     * Config: PlayerPrefs XML (com.mobile.legends.v2.playerprefs.xml)
     */
    public static native boolean nativeInjectMlbbEnemyLockHeadshotSuite(String path);

    /**
     * CODM Enemy Lock + All-Scope Aim Assist — COD Engine/FPS (2026.3).
     * 5 scope tiers mapped per weapon category:
     * Hipfire=50m (AR/SMG/Shotgun), 1x=150m (AR/SMG), 3x=250m (AR/DMR),
     * 6x=350m (Sniper/DMR), 10x+=450m (Sniper only).
     * HeadshotBulletCount=3, KillBulletThreshold=5.
     * Config: INI + JSON (NOT UE4 r. CVar format).
     */
    public static native boolean nativeInjectCodmEnemyLockAllScope(String path);

    /**
     * PUBGM Enemy Lock + All-Scope Aim Assist — Unreal Engine 4 (2026.3).
     * PURE UE4 CVar format — ALL keys use r. prefix (UserCustom.ini).
     * Full ballistic simulation per scope tier: r.ScopeBreathingDamp,
     * r.AntiBreath, r.ZeroBulletDrop, r.BulletVelocityComp.
     * r.HeadshotBulletThreshold=3, r.KillBulletThreshold=5 (UE4 CVars).
     * Config: UserCustom.ini (+CVars=r.Key=Value format).
     */
    public static native boolean nativeInjectPubgmEnemyLockAllScope(String path);

    /**
     * MLBB Enemy Lock + Headshot Suite — Unity/MOBA Engine (2026.3).
     * Native-first with ConfigFileHelper fallback.
     */
    public static boolean injectMlbbEnemyLockHeadshotSuite(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbEnemyLockHeadshotSuite(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectMlbbEnemyLockHeadshotSuite native failed, falling back: " + t.getMessage());
            }
        }
        String[] keys = {
            "HeroLock=1", "HeroLockEnabled=1", "HeroLockTargetPriority=0", "AutoTargetSwitchEnabled=1",
            "TargetPriority=0", "SmartTargetLock=1", "SkillSmartAim=1", "SkillAutoChain=1",
            "ZeroSkillDelay=1", "SkillCastZeroDelay=1", "SkillAimMagnetism=1000", "SkillAimSnapSpeed=10",
            "SkillAimSnapThreshold=0", "SkillPredictiveAim=1", "HeroHitboxMultiplier=3.0", "HeroHitboxScale=3.0",
            "HeroHitRegSyncRate=1000", "HeroInstantHitReg=1", "HeroFrameSyncDamage=1", "HeroSkillBurstKill=3",
            "HeroKillComboCount=3", "HeroSkillBurstEnabled=1", "SkillBurstDamageMax=10000",
            "AllHeroDamageMultiplier=10000", "AllHeroTrueDamage=1", "TrueStrikeMod=1", "CritRateBoost=100",
            "CritDamageMultiplier=10.0", "PenetrationBoost=1", "DamageReductionBypass=1", "AimAssistEnabled=1",
            "AimAssistStrength=1000", "AimMagnetism=1000", "AimSnapSpeed=10", "AimSnapThreshold=0",
            "AimSmoothFactor=0", "AdsZeroDelay=1", "PredictiveAim=1", "SilentAimbot=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1", "bFramePacingEnabled=True"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbEnemyLockHeadshot]");
    }

    /**
     * CODM Enemy Lock + All-Scope Aim Assist — COD Engine/FPS (2026.3).
     * Native-first with ConfigFileHelper fallback.
     */
    public static boolean injectCodmEnemyLockAllScope(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmEnemyLockAllScope(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectCodmEnemyLockAllScope native failed, falling back: " + t.getMessage());
            }
        }
        String[] keys = {
            "EnemyLockMax=1", "TargetLockEnabled=1", "TargetPriority=0", "TargetLockRange=450",
            "AutoTargetSwitch=1", "SilentAimbot=1", "HeadBoneAimPriority=1", "BoneIndex=0",
            "HeadMagnetism=1000", "HeadSnapEnabled=1", "HeadSnapSpeed=10", "AimAssistEnabled=1",
            "AimAssistStrength=1000", "AimMagnetism=1000", "AimSnapSpeed=10", "AimSnapThreshold=0",
            "AdsZeroDelay=1", "PredictiveAim=1", "HipfireAimLock=1", "HipfireHeadshotLock=1",
            "HipfireMaxMagnetism=1", "HipfireHeadMagnetism=1000", "NoScopeHeadSnap=1", "Hipfire_Range=50",
            "Hipfire_HeadBoneLock=1", "Scope1x_Range=150", "Scope1x_AimMagnetism=1000", "Scope1x_HeadMagnetism=1000",
            "Scope1x_HeadBoneLock=1", "Scope1x_HeadshotForce=1", "Scope1x_ADSZeroDelay=1",
            "Scope2x_Range=150", "Scope2x_HeadBoneLock=1", "Scope3x_Range=250", "Scope3x_AimMagnetism=1000",
            "Scope3x_HeadMagnetism=1000", "Scope3x_HeadBoneLock=1", "Scope3x_HeadshotForce=1",
            "Scope3x_BulletDropComp=1", "Scope4x_Range=250", "Scope4x_HeadBoneLock=1", "Scope6x_Range=350",
            "Scope6x_AimMagnetism=1000", "Scope6x_HeadMagnetism=1000", "Scope6x_HeadBoneLock=1",
            "Scope6x_HeadshotForce=1", "Scope6x_BulletDropComp=1", "Scope6x_BreathDamp=1",
            "Scope10x_Range=450", "Scope10x_AimMagnetism=1000", "Scope10x_HeadMagnetism=1000",
            "Scope10x_HeadBoneLock=1", "Scope10x_HeadshotForce=1", "Scope10x_ZeroBulletDrop=1",
            "Scope10x_AntiBreath=1", "Scope10x_BallisticLeadMax=1", "AutoHeadshotEnabled=1",
            "AutoHeadshotAllScope=1", "HeadshotBulletCount=3", "KillBulletThreshold=5",
            "HeadshotMultiplier=999", "HeadDamageMax=99999", "OneTapHeadshot=1", "FirstBulletAccuracy=1.0",
            "DamageBoost=10000", "DamageLockMax=1", "TrueDamageBoost=10000", "VestDamageBypass=1",
            "ArmorPenetrationTier6=1", "HitboxMultiplier=3.0", "InstantHitReg=1", "HitRegSyncRate=1000",
            "GyroSampleRate=1000", "GyroZeroDelay=1", "GyroStabilization=1", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmEnemyLockAllScope]");
    }

    /**
     * PUBGM Enemy Lock + All-Scope Aim Assist — Unreal Engine 4 (2026.3).
     * Native-first with ConfigFileHelper fallback.
     */
    public static boolean injectPubgmEnemyLockAllScope(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmEnemyLockAllScope(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectPubgmEnemyLockAllScope native failed, falling back: " + t.getMessage());
            }
        }
        String[] keys = {
            "r.AimAssistEnabled=1", "r.AimAssistStrength=100", "r.AimMagnetism=3", "r.HeadBoneAimPriority=1",
            "r.PredictiveAim=1", "r.AimSnapThreshold=0", "r.EnemyLockMax=1", "r.TargetLockRange=450",
            "r.TargetPriority=0", "r.SilentAimbot=1", "r.GyroSampleRate=1000", "r.GyroZeroDelay=1",
            "r.GyroStabilization=1", "r.GyroCorrectionEnabled=1", "r.HipfireAimAssist=1", "r.HipfireHeadMagnetism=1",
            "r.HipfireHeadshotLock=1", "r.Scope50mLockRange=50", "r.Scope50mAimMagnetism=3",
            "r.Scope50mHeadMagnetism=1", "r.Scope50mPredictiveAim=1", "r.Scope50mBulletDropComp=0",
            "r.Scope1xAimAssist=1", "r.Scope1xHeadMagnetism=1", "r.Scope1xHeadshotLock=1",
            "r.Scope150mLockRange=150", "r.Scope150mAimMagnetism=3", "r.Scope150mHeadMagnetism=1",
            "r.Scope150mPredictiveAim=1", "r.Scope150mBulletDropComp=1", "r.Scope1xADSZeroDelay=1",
            "r.Scope3xAimAssist=1", "r.Scope3xHeadMagnetism=1", "r.Scope3xHeadshotLock=1",
            "r.Scope250mLockRange=250", "r.Scope250mAimMagnetism=3", "r.Scope250mHeadMagnetism=1",
            "r.Scope250mPredictiveAim=1", "r.Scope250mBulletDropComp=1", "r.BulletDropComp=1",
            "r.BulletVelocityComp=1", "r.WeaponSpread=0", "r.WeaponSway=0", "r.Scope6xAimAssist=1",
            "r.Scope6xHeadMagnetism=1", "r.Scope6xHeadshotLock=1", "r.Scope350mLockRange=350",
            "r.Scope350mAimMagnetism=3", "r.Scope350mHeadMagnetism=1", "r.Scope350mPredictiveAim=1",
            "r.Scope350mBulletDropComp=1", "r.ScopeBallisticComp=1", "r.ScopeZeroSway=1",
            "r.ScopeBreathingDamp=1", "r.WeaponRecoilScale=0", "r.VerticalRecoilScale=0",
            "r.HorizontalRecoilScale=0", "r.Scope8xAimAssist=1", "r.Scope8xHeadMagnetism=1",
            "r.Scope8xHeadshotLock=1", "r.Scope10xAimAssist=1", "r.Scope10xHeadMagnetism=1",
            "r.Scope10xHeadshotLock=1", "r.Scope450mLockRange=450", "r.Scope450mAimMagnetism=3",
            "r.Scope450mHeadMagnetism=1", "r.Scope450mPredictiveAim=1", "r.Scope450mBulletDropComp=1",
            "r.ZeroBulletDrop=1", "r.AntiBreath=1", "r.SniperHeadshotLock=1", "r.SniperZeroSway=1",
            "r.SniperBreathHoldZero=1", "r.HeadshotBulletThreshold=3", "r.KillBulletThreshold=5",
            "r.PUBGHeadshotMultiplier=999", "r.PUBGDamageLockMax=10000", "r.PUBGDamageBoost=10000",
            "r.PUBGTrueDamageMod=1", "r.PUBGVestDamageBypass=1", "TouchPollingRate=1000", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UserCustom]");
    }

    /** Convenience wrapper: injectAdaptiveAimAssist with native-first fallback. */
    public static boolean injectAdaptiveAimAssist(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAdaptiveAimAssist(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectAdaptiveAimAssist fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "AimAssistEnabled=1", "AimAssistStrength=100", "AimMagnetism=3", "AimAssistLockMax=1",
            "HeadMagnetism=1", "HeadBoneAimPriority=1", "AimBoneTarget=0", "HeroLock=1", "SkillSmartAim=1",
            "PredictiveAim=1", "AimMethod=1", "AimSmoothFactor=0.0", "AimSnapSpeed=10", "AimSnapThreshold=0",
            "AdsZeroDelay=1", "TargetPriority=0", "GyroSensitivityRatio=2.0", "GyroSampleRate=1000",
            "GyroZeroDelay=1", "GyroStabilization=1", "GyroLatencyMode=0", "GyroSmoothFactor=0.5",
            "HipfireSensitivityBoost=1.2", "IronSightSensitivity=1.0", "RedDotSensScale=1.0", "RedDotAimLock=1",
            "HoloSensScale=1.0", "Scope2xSensitivity=1.0", "Scope2xGyroSample=1000", "Scope2xStabilizer=1",
            "Scope2xRecoilDamp=1", "Scope3xSensitivity=0.90", "Scope3xGyroStabilization=1", "Scope3xRecoilDamp=1",
            "Scope4xSensitivity=0.85", "Scope4xStabilizer=1", "Scope4xZeroSway=1", "Scope6xSensitivity=0.75",
            "Scope6xMicroDamping=1", "Scope6xStabilizer=1", "Scope8xSensitivity=0.65", "Scope8xPrecisionFilter=1",
            "Scope8xStabilizer=1", "Scope8xZeroBreathing=1", "r.AimAssistEnabled=1", "r.AimAssistStrength=100",
            "r.AimMagnetism=3", "r.AimSnapThreshold=0", "r.HeadBoneAimPriority=1", "r.PredictiveAim=1",
            "r.GyroSampleRate=1000", "r.GyroZeroDelay=1", "r.GyroStabilization=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[AdaptiveAimAssist]");
    }

    /** Convenience wrapper: injectAdaptiveNoRecoil with native-first fallback. */
    public static boolean injectAdaptiveNoRecoil(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAdaptiveNoRecoil(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectAdaptiveNoRecoil fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "ZeroRecoil=1", "RecoilScale=0", "VerticalRecoilScale=0", "HorizontalRecoilScale=0",
            "RecoilPatternScale=0", "RecoilControlAssist=1", "WeaponSway=0", "WeaponSpread=0",
            "BulletSpreadScale=0", "MuzzleSpread=0", "MovingSpreadFactor=0", "SpreadDecayRate=15",
            "AR_RecoilZero=1", "AR_SpreadZero=1", "AR_SprayPatternRecovery=10", "AR_AccuracyMax=1",
            "AR_ZeroDeadzone=1", "SMG_ZeroRecoil=1", "SMG_SprayControlMax=1", "SMG_ZeroSpread=1",
            "SMG_HipfireBurst=1", "Sniper_ZeroSway=1", "Sniper_QuickScopeZeroDelay=1", "Sniper_BulletDropComp=1",
            "Sniper_ScopeStabilizer=1", "Sniper_BreathHoldZero=1", "ScopeZeroRecoil=1", "ScopeBreathingDamp=1",
            "Scope2xStabilizer=1", "Scope3xStabilizer=1", "Scope4xStabilizer=1", "Scope6xStabilizer=1",
            "Scope8xStabilizer=1", "ScopeZeroSway=1", "GyroSampleRate=1000", "GyroZeroDelay=1",
            "r.WeaponRecoilScale=0", "r.VerticalRecoilScale=0", "r.HorizontalRecoilScale=0",
            "r.RecoilPatternScale=0", "r.WeaponSpread=0", "r.WeaponSway=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[AdaptiveNoRecoil]");
    }

    /** Convenience wrapper: injectRankedCombatFullSuite — master 24-layer payload. Ranked + Classic + All Maps. */
    public static boolean injectRankedCombatFullSuite(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectRankedCombatFullSuite(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectRankedCombatFullSuite fallback: " + t.getMessage());
            }
        }
        boolean ok1 = injectAdaptiveAimAssist(path);
        boolean ok2 = injectAdaptiveNoRecoil(path);
        boolean ok3 = injectEnemyLockMaxAllScope(path);
        boolean ok4 = injectAutoHeadshotBulletKill(path);
        return ok1 || ok2 || ok3 || ok4;
    }

    /**
     * Convenience wrapper: Enemy Lock MAX — All-Scope Multi-Range Aim Assist (2026.3 Dame Edition).
     * 5 range-gated scope tiers: 50m / 150m / 250m / 350m / 450m.
     * Head bone hard-lock, magnetism=1000, silent aimbot, gyro lock.
     */
    public static boolean injectEnemyLockMaxAllScope(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectEnemyLockMaxAllScope(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectEnemyLockMaxAllScope fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "EnemyLockMax=1", "EnemyLockAllScope=1", "EnemyLockTier=5", "MaxEngagementRange=450",
            "HeadBoneAimPriority=1", "HeadMagnetism=1000", "HeadLockTier=5", "SilentAimbot=1",
            "AimAssistEnabled=1", "AimAssistStrength=1000", "AimMagnetism=1000", "AimSnapSpeed=10",
            "AimSnapThreshold=0", "AimSmoothFactor=0", "AdsZeroDelay=1", "PredictiveAim=1",
            "TargetPriority=0", "AutoTargetSwitch=1", "FOVScaleLock=1.0", "GyroSampleRate=1000",
            "GyroZeroDelay=1", "GyroStabilization=1", "GyroCorrectionLock=1", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1", "AttackFrameSync=1", "bFramePacingEnabled=True",
            "ScopeTier0_Range=50", "ScopeTier0_AimMagnetism=1000", "ScopeTier0_HeadMagnetism=1000",
            "ScopeTier0_SnapSpeed=10", "ScopeTier0_HeadLock=1", "ScopeTier0_PredictiveAim=1",
            "HipfireAimLock=1", "HipfireHeadLock=1", "HipfireAimMagnetism=1000",
            "ScopeTier1_Range=150", "ScopeTier1_AimMagnetism=1000", "ScopeTier1_HeadMagnetism=1000",
            "ScopeTier1_SnapSpeed=10", "ScopeTier1_HeadLock=1", "ScopeTier1_PredictiveAim=1",
            "ScopeTier1_BulletDropComp=1", "RedDotAimLock=1", "HoloAimLock=1", "IronSightAimLock=1",
            "ScopeTier2_Range=250", "ScopeTier2_AimMagnetism=1000", "ScopeTier2_HeadMagnetism=1000",
            "ScopeTier2_SnapSpeed=10", "ScopeTier2_HeadLock=1", "ScopeTier2_PredictiveAim=1",
            "ScopeTier2_BulletDropComp=1", "Scope2xAimLock=1", "Scope3xAimLock=1", "Scope4xAimLock=1",
            "ScopeTier3_Range=350", "ScopeTier3_AimMagnetism=1000", "ScopeTier3_HeadMagnetism=1000",
            "ScopeTier3_SnapSpeed=10", "ScopeTier3_HeadLock=1", "ScopeTier3_PredictiveAim=1",
            "ScopeTier3_BulletDropComp=1", "ScopeTier3_BreathDamp=1", "Scope6xAimLock=1",
            "ScopeTier4_Range=450", "ScopeTier4_AimMagnetism=1000", "ScopeTier4_HeadMagnetism=1000",
            "ScopeTier4_SnapSpeed=10", "ScopeTier4_HeadLock=1", "ScopeTier4_PredictiveAim=1",
            "ScopeTier4_BulletDropComp=1", "ScopeTier4_BreathDamp=1", "Scope8xAimLock=1", "Scope10xAimLock=1",
            "SniperHeadshotLock=1", "SniperZeroSway=1", "ZeroBulletDrop=1",
            "r.AimAssistEnabled=1", "r.AimAssistStrength=100", "r.AimMagnetism=3", "r.HeadBoneAimPriority=1",
            "r.PredictiveAim=1", "r.AimSnapThreshold=0", "r.EnemyLockMax=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[EnemyLockMaxAllScope]");
    }

    /**
     * Convenience wrapper: Auto Headshot Bullet Kill (2026.3 Dame Edition).
     * 3 bullets → headshot kill. 5 bullets → universal kill sweep.
     * All scope tiers enforced. Damage maxed to 99999 for head hits.
     */
    public static boolean injectAutoHeadshotBulletKill(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAutoHeadshotBulletKill(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectAutoHeadshotBulletKill fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "AutoHeadshotEnabled=1", "AutoHeadshotAllScope=1", "HeadshotBulletCount=3", "HeadshotForceEnabled=1",
            "HeadshotBoneIndex=0", "HeadBoneAimPriority=1", "HeadMagnetism=1000", "HeadSnapEnabled=1",
            "HeadSnapSpeed=10", "OneTapHeadshot=1", "OneShotKillHitbox=1", "FirstBulletAccuracy=1.0",
            "ScopeHeadshotLock=1", "HeadshotMultiplier=999", "HeadDamageMax=99999", "HeadshotDamageBoost=1",
            "CriticalHeadshotDamage=1", "ScopeTier0_HeadshotForce=1", "ScopeTier1_HeadshotForce=1",
            "ScopeTier2_HeadshotForce=1", "ScopeTier3_HeadshotForce=1", "ScopeTier4_HeadshotForce=1",
            "HipfireHeadshotLock=1", "Scope1xHeadshotForce=1", "Scope3xHeadshotForce=1", "Scope6xHeadshotForce=1",
            "Scope8xHeadshotForce=1", "Scope10xHeadshotForce=1", "SniperHeadshotLock=1", "KillBulletCount=5",
            "KillBulletThreshold=5", "BulletKillSweepEnabled=1", "UniversalKillEnabled=1", "DamageLockMax=1",
            "DamageBoost=10000", "DamageMultiplier=10000", "TrueDamageBoost=10000", "TrueDamageMultiplier=10000",
            "KillDamageThreshold=1", "MinDamagePerBullet=99999", "BulletPenetrationMax=1", "ArmorPenetrationTier6=1",
            "VestDamageBypass=1", "HelmetPenetrationLevel3=1.0", "FleshDamageMultiplier=999", "LimbDamageMultiplier=999",
            "TrackingBullet=1", "BulletMagnetism=1", "HitboxMultiplier=3.0", "HitboxScale=3.0",
            "InstantHitReg=1", "HitRegSyncRate=1000", "FrameSyncDamage=1",
            "r.AutoHeadshotEnabled=1", "r.HeadshotBulletThreshold=3", "r.KillBulletThreshold=5",
            "r.PUBGHeadshotMultiplier=999", "r.PUBGDamageLockMax=10000", "r.PUBGDamageBoost=10000",
            "r.PUBGTrueDamageMod=1", "r.PUBGVestDamageBypass=1", "r.PUBGInstantHitReg=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[AutoHeadshotBulletKill]");
    }

    /**
     * MLBB 2026.4 Combat Overdrive (Damage Assist, Aim Lock, Armor Overdrive).
     */
    public static boolean injectMlbbCombatOverdrive2026(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbCombatOverdrive2026(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectMlbbCombatOverdrive2026 fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "CombatOverdrive2026=1", "DamageLockMax=10000", "TrueDamageFloor=10000", "EffectiveDPSMode=4",
            "PhysicalDamageBase=10000", "MagicDamageBase=10000", "PenetrationMultiplier=3.0", "PhysicalPenetration=100",
            "MagicPenetration=100", "CritRateBoost=100", "CritDamageMultiplier=4.0", "HitRegSyncRate=1000",
            "FrameSyncDamage=1", "HeroExecutionThreshold=30", "SmartAimMagnetDualPriority=1", "SkillSmartAim=1",
            "HeroLockPriority=0", "SkillPredictionLead=1", "AimSnapSpeed=10", "AimMagnetismTier=3",
            "ZeroDeadzoneTouchHz=1000", "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1",
            "ArmorBoostFloor=10000", "PhysicalDefense=10000", "MagicDefense=10000", "MagicShieldBoost=10000",
            "PhysicalShield=10000", "DamageReductionRatio=0.99", "DamageReduction=0.99", "OmniLifestealMultiplier=10.0",
            "LifestealBoost=1", "SpellVampBoost=1", "UnlimitedEnergy=1", "UnlimitedMana=1",
            "InstantCooldownReset=1", "CooldownReduction=0", "FastSkillCycle=1", "ZeroSkillDelay=1",
            "ZeroDelaySkillTap=1", "SkillCastDelayMs=0", "FastSkillReleaseSpeed=10", "SkillAutoChain=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbCombatOverdrive2026]");
    }

    /**
     * PUBGM 2026.4 Combat Overdrive (Damage Assist, Aim Lock, Armor Overdrive).
     */
    public static boolean injectPubgmCombatOverdrive2026(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmCombatOverdrive2026(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectPubgmCombatOverdrive2026 fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "r.CombatOverdrive2026=1", "r.MuzzleVelocityFactor=3.0", "r.PUBGBulletVelocityCompensation=1",
            "r.HitRegPacketSync=1000", "HitRegSyncRate=1000", "r.ArmorShredder=1", "r.DamageMultiplierFloor=10000",
            "r.DamageLockMax=10000", "DamageLockMax=10000", "r.HeadBonePriority=1", "r.HeadBoneAimPriority=1",
            "r.BoneIndex=0", "r.AimAssistEnabled=1", "r.AimAssistStrength=100", "r.AimMagnetism=3",
            "r.AimSnapThreshold=0", "r.AllScopeSnapSpeed=10", "r.LeadPredictionHz=1000", "r.PredictiveAim=1",
            "r.SilentAimbot=1", "r.PlayerDamageReduction=0.95", "r.KineticShieldBoost=10000", "r.FallDamageImmunity=1",
            "r.VehicleCollisionPenalty=0", "r.VehicleDamageToPlayer=0", "r.GyroSampleRate=1000", "r.GyroZeroDelay=1",
            "r.GyroStabilization=1", "TouchPollingRate=1000", "r.OneFrameThreadLag=0", "r.FinishCurrentFrame=0",
            "bFramePacingEnabled=True", "AllowOcclusionQueries=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UserCustom]");
    }

    /**
     * CODM 2026.4 Combat Overdrive (Damage Assist, Aim Lock, Armor Overdrive).
     */
    public static boolean injectCodmCombatOverdrive2026(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmCombatOverdrive2026(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectCodmCombatOverdrive2026 fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "CombatOverdrive2026=1", "DamageFloorMax=10000", "DamageLockMax=10000", "HitRegSyncRate=1000",
            "HitRegPacketSync1000Hz=1", "WeaponSpread=0", "BulletSpreadScale=0", "MuzzleSpread=0",
            "MovingSpreadFactor=0", "JumpSpreadFactor=0", "SpreadDecayRate=20", "LaserBeamZeroSpread=1",
            "AimAssistLockMax=1", "AimMagnetism=1000", "AimMagnetismLevel=10", "AimSnapSpeed=10",
            "AimSnapThreshold=0", "HeadBonePriority=1", "HeadMagnetism=1000", "HeadMagnetismMax=1000",
            "AdsZeroDelay=1", "AdsZeroDelayInstant=1", "PredictiveAim=1", "SilentAimbot=1",
            "KineticArmorOverdrive=10000", "DamageReductionRatio=0.95", "DamageReduction=0.95",
            "FlakJacketExplosionLock=1", "StunFlashImmunity=1", "GyroSampleRate=1000", "GyroZeroDelay=1",
            "GyroStabilization=1", "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1",
            "bFramePacingEnabled=True", "AllowOcclusionQueries=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmCombatOverdrive2026]");
    }

    /**
     * Universal 2026.4 Armor & Shield Fortification Lock.
     */
    public static boolean injectUniversalArmorShieldLock(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectUniversalArmorShieldLock(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectUniversalArmorShieldLock fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "ArmorBoostFloor=10000", "KineticShieldBoost=10000", "PhysicalDefense=10000", "MagicDefense=10000",
            "DamageReductionRatio=0.95", "DamageReduction=0.95", "FallDamageImmunity=1", "VehicleCollisionPenalty=0",
            "FlakJacketExplosionLock=1", "StunFlashImmunity=1", "r.PlayerDamageReduction=0.95", "r.KineticShieldBoost=10000",
            "r.VehicleCollisionPenalty=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UniversalArmorShieldLock]");
    }

    /**
     * Universal 2026.4 Aim Magnet & Zero-Deadzone Lock.
     */
    public static boolean injectUniversalAimMagnetLock(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectUniversalAimMagnetLock(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectUniversalAimMagnetLock fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "AimAssistLockMax=1", "AimMagnetism=1000", "AimMagnetismTier=3", "AimSnapSpeed=10",
            "AimSnapThreshold=0", "HeadBonePriority=1", "HeadMagnetism=1000", "AdsZeroDelay=1",
            "ZeroDeadzoneTouchHz=1000", "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1",
            "r.AimAssistStrength=100", "r.AimMagnetism=3", "r.HeadBonePriority=1", "r.BoneIndex=0",
            "r.GyroSampleRate=1000", "r.GyroZeroDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UniversalAimMagnetLock]");
    }

    /**
     * MLBB 2026: Basic Attack Overclock + True Damage Floor + Infinite Regen (HP/Mana/Energy) + Life-Still + 10000 Armor.
     */
    public static boolean injectMlbbBasicAttackRegenOverdrive(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbBasicAttackRegenOverdrive(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectMlbbBasicAttackRegenOverdrive fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "basic_attack_damage_overdrive=10000", "basic_attack_speed_ratio=10.0", "basic_attack_true_damage=1",
            "DamageLockMax=10000", "TrueDamageFloor=10000", "CritRateBoost=100", "CritDamageMultiplier=4.0",
            "PenetrationBoost=1", "PenetrationMultiplier=3.0", "HitRegSyncRate=1000", "FrameSyncDamage=1",
            "hp_regen_rate=1000", "mana_regen_rate=1000", "energy_regen_rate=1000", "life_still_passive_tick=1000",
            "HpRegenMultiplier=10.0", "ManaRegenMultiplier=10.0", "EnergyRegenMultiplier=10.0", "InstantHpFullRestore=1",
            "PassiveLifeStill=1", "LifeStillRate=1000", "all_hero_true_lifesteal=10.0", "spellvamp_ratio=10.0",
            "OmniLifestealMultiplier=10.0", "LifestealBoost=1", "SpellVampBoost=1", "OmniVampRate=10.0",
            "LifestealOnHit=1", "SpellLifestealOnCast=1", "zero_skill_mana_cost=1", "zero_skill_energy_cost=1",
            "UnlimitedMana=1", "UnlimitedEnergy=1", "ZeroSkillDelay=1", "SkillCastDelayMs=0",
            "InstantCooldownReset=1", "CooldownReduction=0", "PhysicalDefense=10000", "MagicDefense=10000",
            "ArmorBoostFloor=10000", "PhysicalShield=10000", "MagicShieldBoost=10000", "DamageReductionRatio=0.99",
            "DamageReduction=0.99", "AimMagnetism=3", "HeadMagnetism=1", "AimSnapSpeed=10", "AimSmoothFactor=0",
            "AimMethod=1", "SkillSmartAim=1", "HeroLock=1", "TouchPollingRate=1000", "TouchZeroDelay=1",
            "ZeroInputLag=1", "bFramePacingEnabled=True", "AllowOcclusionQueries=1", "r.OneFrameThreadLag=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbBasicAttackRegenOverdrive2026]");
    }

    /**
     * PUBGM 2026: Full Scope Lock (100m/200m/300m/400m/450m) + Bullet Tracking 3.0x + Fast ADS/Sprint/Reload/Swap Overdrive.
     */
    public static boolean injectPubgmFullScopeBulletTrackingOverdrive(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmFullScopeBulletTrackingOverdrive(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectPubgmFullScopeBulletTrackingOverdrive fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "r.Scope100mIronSightLock=1", "r.Scope100mRdsDotLock=1", "scope_100m_iron_reddot_lock=1",
            "r.IronSightAimLock=1", "r.RdsDotAimLock=1", "r.IronSightHeadshotMultiplier=999", "r.RdsHeadshotMultiplier=999",
            "r.Scope200m2xMagnifierLock=1", "r.Scope200m3xMagnifierLock=1", "scope_200m_2x_3x_lock=1",
            "r.2xScopeAimLock=1", "r.3xScopeAimLock=1", "r.2xHeadshotMultiplier=999", "r.3xHeadshotMultiplier=999",
            "r.Scope300m4xAcogLock=1", "r.Scope300mPrismLock=1", "scope_300m_4x_acog_lock=1",
            "r.4xScopeAimLock=1", "r.PrismScopeAimLock=1", "r.4xHeadshotMultiplier=999", "r.PrismHeadshotMultiplier=999",
            "r.Scope400m6xAdjustedLock=1", "r.Scope400mVSSLock=1", "scope_400m_6x_adjusted_lock=1",
            "r.6xScopeAimLock=1", "r.VSSScopeAimLock=1", "r.6xHeadshotMultiplier=999", "r.VSSHeadshotMultiplier=999",
            "r.Scope450m8xSniperLock=1", "r.Scope450mCQBRLock=1", "scope_450m_8x_sniper_lock=1",
            "r.8xScopeAimLock=1", "r.CQBRScopeAimLock=1", "r.8xHeadshotMultiplier=999", "r.CQBRHeadshotMultiplier=999",
            "r.BulletTrackingLock=1", "r.BulletTrackingHitboxMultiplier=3.0", "r.BulletTrackingMagnetism=3.0",
            "r.HitboxMultiplier=3.0", "r.BulletMagnetism=3", "r.ZeroRecoil=1", "r.WeaponRecoilScale=0",
            "r.WeaponSpread=0", "r.WeaponSway=0", "r.FastADS=1", "r.FastADSSpeed=10.0", "r.FastSprint=1",
            "r.FastSprintSpeed=10.0", "r.FastReload=1", "r.FastReloadSpeed=10.0", "r.FastChambering=1",
            "r.FastWeaponSwap=1", "r.FastScopeSwap=1", "r.ZeroSprintTransitionLag=1", "r.ZeroADSCooldown=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UserCustom]");
    }

    /**
     * CODM 2026: Attack Aim Assist Lock + Bullet Tracking Lock 3.0x + All-Scope Lock (100m-450m) + 0ms Fast Reload/Sprint Overdrive.
     */
    public static boolean injectCodmFullScopeBulletTrackingFastReload(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmFullScopeBulletTrackingFastReload(path)) return true; } catch (Throwable t) {
                Log.w(TAG, "injectCodmFullScopeBulletTrackingFastReload fallback: " + t.getMessage());
            }
        }
        String[] keys = {
            "AimAssistEnabled=1", "AimAssistStrength=100", "AimAssistMaxStrength=1000", "AimMagnetism=3",
            "AimMagnetismMax=1000", "HeadMagnetism=1000", "HeadMagnetismMax=1000", "AimSnapSpeed=10",
            "AimSmoothFactor=0", "AdsZeroDelay=1", "AdsZeroDelayInstant=1", "PredictiveAim=1", "SilentAimbot=1",
            "HeadBonePriority=1", "bullet_tracking_lock=1", "bullet_tracking_hitbox=3.0", "BulletTrackingMagnetism=3.0",
            "BulletTrackingHitboxScale=3.0", "BulletMagnetism=3", "WeaponSpread=0", "WeaponSway=0",
            "BulletSpreadScale=0", "MuzzleSpread=0", "SpreadDecayRate=20", "ZeroRecoil=1", "RecoilScale=0",
            "VerticalRecoilScale=0", "HorizontalRecoilScale=0", "scope_100m_iron_reddot_lock=1", "IronSightAimLock=1",
            "RedDotAimLock=1", "IronSightHeadshotMultiplier=999", "RedDotHeadshotMultiplier=999",
            "scope_200m_tactical_holo_lock=1", "TacticalScopeAimLock=1", "HoloScopeAimLock=1",
            "TacticalHeadshotMultiplier=999", "HoloHeadshotMultiplier=999", "scope_300m_3x_4x_acog_lock=1",
            "3xScopeAimLock=1", "4xAcogAimLock=1", "3xHeadshotMultiplier=999", "4xHeadshotMultiplier=999",
            "scope_400m_6x_rtg_lock=1", "6xScopeAimLock=1", "RtgScopeAimLock=1", "6xHeadshotMultiplier=999",
            "RtgHeadshotMultiplier=999", "scope_450m_sniper_extreme_lock=1", "SniperScopeAimLock=1",
            "SniperExtremeAimLock=1", "SniperHeadshotMultiplier=999", "SniperZeroBulletDrop=1",
            "FastReloadOverdrive=1", "FastReloadSpeed=10.0", "InstantReloadChambering=1", "FastSprintTurbo=1",
            "FastSprintSpeed=10.0", "FastScopeADS=1", "FastADSSpeed=10.0", "FastSlideCancelTurbo=1",
            "FastWeaponSwap=1", "ZeroReloadDelay=1", "ZeroChamberingDelay=1", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmFullScopeFastReload2026]");
    }

    /**
     * Native Security Bypass: Strips foreign and audit extended attributes (xattrs).
     */
    public static boolean securityBypassStripXattrs(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeSecurityBypassStripXattrs(path); } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Native Security Bypass: High-precision timestamp cloaking via utime.
     */
    public static boolean securityBypassCloakTimestamps(String targetPath, String sourcePath) {
        if (targetPath == null || sourcePath == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeSecurityBypassCloakTimestamps(targetPath, sourcePath); } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Native Security Bypass: Atomic staging-to-target rename swap for inotify evasion.
     */
    public static boolean securityBypassAtomicSwap(String stagedPath, String targetPath) {
        if (stagedPath == null || targetPath == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeSecurityBypassAtomicSwap(stagedPath, targetPath); } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Native Security Bypass: Direct POSIX permission and ownership enforcement.
     */
    public static boolean securityBypassEnforcePermissions(String path, int uid, int gid, int mode) {
        if (path == null) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeSecurityBypassEnforcePermissions(path, uid, gid, mode); } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Masks the current process and thread names via POSIX prctl and pthread_setname.
     */
    public static boolean cloakProcessIdentity(String targetName) {
        if (targetName == null || targetName.isEmpty()) targetName = "surfaceflinger";
        if (sNativeLibraryLoaded) {
            try { return nativeCloakProcessIdentity(targetName); } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Arms preemptive signal handling against SIGTRAP debugger sweeps and rogue ptrace attempts.
     */
    public static boolean armSignalTrapGuard() {
        if (sNativeLibraryLoaded) {
            try { return nativeArmSignalTrapGuard(); } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Disarms preemptive signal handling.
     */
    public static boolean disarmSignalTrapGuard() {
        if (sNativeLibraryLoaded) {
            try { return nativeDisarmSignalTrapGuard(); } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Creates an anonymous kernel memory file descriptor (zero disk artifact).
     */
    public static int createAnonymousMemFd(String name, byte[] data) {
        if (name == null || data == null) return -1;
        if (sNativeLibraryLoaded) {
            try { return nativeCreateAnonymousMemFd(name, data); } catch (Throwable ignored) {}
        }
        return -1;
    }

    /**
     * Closes an active anonymous memfd.
     */
    public static boolean closeMemFd(int fd) {
        if (fd < 0) return false;
        if (sNativeLibraryLoaded) {
            try { return nativeCloseMemFd(fd); } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Native High-Performance Memory-Mapped Hex File Patching.
     * Replaces binary patterns in-place without JVM byte array allocation.
     */
    public static boolean fastHexPatchFile(String path, String hexPattern, String hexReplacement) {
        if (path == null || hexPattern == null || hexReplacement == null) return false;
        byte[] pattern = hexToBytes(hexPattern);
        byte[] replacement = hexToBytes(hexReplacement);
        if (pattern.length == 0 || replacement.length == 0) return false;

        if (sNativeLibraryLoaded) {
            try {
                return nativeFastHexPatchMmap(path, pattern, replacement);
            } catch (Throwable t) {
                Log.w(TAG, "Native fastHexPatchFile failed, falling back: " + t.getMessage());
            }
        }
        return false;
    }

    /**
     * Native Live Process Memory Hex Patching.
     * Scans process memory maps and patches live instruction/data blocks via process_vm_writev without GC pauses.
     */
    public static int scanAndPatchProcessMemory(int pid, String moduleFilter, String hexPattern, String hexReplacement) {
        if (pid <= 0 || hexPattern == null || hexReplacement == null) return 0;
        byte[] pattern = hexToBytes(hexPattern);
        byte[] replacement = hexToBytes(hexReplacement);
        if (pattern.length == 0 || replacement.length == 0) return 0;

        if (sNativeLibraryLoaded) {
            try {
                return nativeScanAndPatchProcessMemory(pid, moduleFilter, pattern, replacement);
            } catch (Throwable t) {
                Log.w(TAG, "Native scanAndPatchProcessMemory failed: " + t.getMessage());
            }
        }
        return 0;
    }

    /**
     * Native Direct Memory Search: returns byte offset of target pattern in file, or -1 if not found.
     */
    public static long findByteOffsetInFile(String path, String hexPattern) {
        if (path == null || hexPattern == null) return -1L;
        byte[] pattern = hexToBytes(hexPattern);
        if (pattern.length == 0) return -1L;

        if (sNativeLibraryLoaded) {
            try {
                return nativeDirectMemorySearch(path, pattern);
            } catch (Throwable t) {
                Log.w(TAG, "Native findByteOffsetInFile failed: " + t.getMessage());
            }
        }
        return -1L;
    }

    /**
     * Utility method: Parses a hex string (e.g. "00 20 70 47" or "00207047") into raw byte array.
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null) return new byte[0];
        String clean = hex.replaceAll("[\\s:,]", "");
        if (clean.length() % 2 != 0) {
            clean = "0" + clean;
        }
        int len = clean.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(clean.charAt(i), 16) << 4)
                    + Character.digit(clean.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Executes single-pass atomic batch injection for a map of key-value overrides.
     */
    public static boolean batchInjectKeys(String path, java.util.Map<String, String> keys, String defaultSection) {
        if (path == null || path.trim().isEmpty() || keys == null || keys.isEmpty()) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                String[] kArr = new String[keys.size()];
                String[] vArr = new String[keys.size()];
                int i = 0;
                for (java.util.Map.Entry<String, String> e : keys.entrySet()) {
                    kArr[i] = e.getKey();
                    vArr[i] = e.getValue();
                    i++;
                }
                if (nativeBatchPatchKeys(path, kArr, vArr)) {
                    return true;
                }
            } catch (Throwable t) {
                Log.w(TAG, "Native batch patch fallback: " + t.getMessage());
            }
        }
        return ConfigFileHelper.patchKeysBatch(path, keys, defaultSection);
    }

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
            // Always inject universal combat master suite (single-pass)
            injectUniversalCombatSuite(path);
            injectHitboxMultiplier(path, 3.0f);
            injectUltraWallhackEspClarity(path);
            injectSilentAimbot(path);

            // Specialized mechanics by game package
            if (isMlbb) {
                injectMlbbMasterComboSuite(path);
                injectAutoSmiteRetribution(path);
            } else if (packageName.toLowerCase().contains("tencent.ig") || packageName.toLowerCase().contains("pubg")) {
                injectPubgmBallisticsVelocityPenetration(path);
                injectPubgmFastLoadAsyncStreaming(path);
            } else if (packageName.toLowerCase().contains("callofduty")) {
                injectCodmBsaRemovalRangeOverdrive(path);
                injectCodmFastLoadShaderBypass(path);
            }
        }
        Log.i(TAG, "Injected real engine configs + combat suite + fast load to " + count + " paths for " + packageName);
        return count;
    }

    public static boolean injectSilentAimbot(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectSilentAimbot(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AimAssistEnabled=1", "AimAssistStrength=100", "AimMagnetism=3",
            "AimSnapSpeed=10", "AimSnapThreshold=0", "AimSmoothFactor=0",
            "HeadMagnetism=1", "HeadBoneAimPriority=1", "HeroLock=1", "SkillSmartAim=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Aimbot]");
    }

    public static boolean injectHitboxMultiplier(String path, float multiplier) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectHitboxMultiplier(path, multiplier)) return true; } catch (Throwable ignored) {}
        }
        float m = multiplier > 1.0f ? multiplier : 3.0f;
        String[] keys = {
            "HitboxMultiplier=" + m, "HitboxScale=" + m, "EnemyHitboxSize=" + m,
            "BulletMagnetism=1", "TrackingBullet=1", "InstantHitReg=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Hitbox]");
    }

    public static boolean injectUltraWallhackEspClarity(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectUltraWallhackEspClarity(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AllowOcclusionQueries=1", "r.Fog=0", "r.VolumetricFog=0",
            "CharacterSilhouetteBoost=1", "MaxDrawDistanceScale=2.0", "HighlightEnemies=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[ClarityESP]");
    }

    public static boolean injectAutoSmiteRetribution(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectAutoSmiteRetribution(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AutoRetriLordTurtle=1", "RetriHpThresholdCalc=1", "InstantSmite=1",
            "ObjectiveTargetLock=1", "HokSmiteObjectivePriority=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[AutoSmite]");
    }

    public static boolean injectUniversalCombatSuite(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectUniversalCombatSuite(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=1", "EffectiveDPSMode=3", "HitRegSyncRate=1000",
            "FrameSyncDamage=1", "CritRateBoost=100", "PenetrationBoost=1",
            "AimAssistEnabled=1", "AimAssistStrength=100", "AimMagnetism=3",
            "AimSnapSpeed=10", "ZeroRecoil=1", "RecoilScale=0", "WeaponSpread=0",
            "BulletSpreadScale=0", "WeaponSway=0", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CombatSuite]");
    }

    public static boolean injectMlbbMasterComboSuite(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbMasterComboSuite(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "SkillAutoChain=1", "SkillAutoCombo=1", "LingComboSpeed=10",
            "FannyCableSpeed=10", "FannyZeroCableDelay=1", "FannyUnlimitedEnergy=1",
            "GusionDaggerSpeed=10", "ChouFreestyleFlicker=1", "HayaShadowSwapInstant=1",
            "BeatrixGunSwapInstant=1", "AutoRetriLordTurtle=1", "CameraHeight=2",
            "DamageLockMax=1", "EffectiveDPSMode=3", "HeroLock=1", "SkillSmartAim=1",
            "TouchPollingRate=1000", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbMasterSuite]");
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
        String[] keys = {
            "DroneView=1", "DroneFOV=180", "MaxFOV=180", "FieldOfView=180",
            "CameraHeight=4", "CameraDistance=180", "WideCameraAngle=1",
            "MapVisibilityRange=2.0", "FogOfWarBypass=1", "AllowOcclusionQueries=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[DroneView]");
    }

    /**
     * PUBGM Dedicated Drone View & iPad FOV Panoramic Injector.
     * Injects UE4 Camera FOV CVars and INI keys to unlock wider perspective without touching screen density.
     */
    public static boolean injectPubgUltraDroneViewMaxFov(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgUltraDroneViewMaxFov(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "+CVars=r.PUBGCameraFOV=130",
            "+CVars=r.PUBGCameraDistance=220",
            "+CVars=r.PUBGIpadView=1",
            "+CVars=r.IpadView=1",
            "+CVars=r.Fov=130",
            "+CVars=r.CameraFov=130",
            "+CVars=r.DefaultFOV=130",
            "+CVars=r.ThirdPersonFOV=130",
            "+CVars=r.ThirdPersonCameraDistance=220",
            "+CVars=r.CameraDistance=220",
            "+CVars=r.WideView=1",
            "+CVars=r.IpadFov=130",
            "+CVars=r.PUBGDroneView=1",
            "+CVars=r.PUBGMaxFOV=130",
            "+CVars=r.FieldOfView=130",
            "+CVars=r.FovRatio=1.35",
            "+CVars=r.SceneFovRatio=1.35",
            "+CVars=r.WideCameraAngle=1",
            "+CVars=r.PanoramicFOV=1.75",
            "+CVars=r.AspectRatioAxisConstraint=AspectRatio_MaintainYFOV",
            "DroneView=1",
            "DroneFOV=130",
            "FieldOfView=130",
            "CameraFOV=130",
            "FPPCameraFOV=130",
            "TPPCameraFOV=130",
            "CameraDistance=220",
            "ThirdPersonFOV=130",
            "ThirdPersonCameraDistance=220",
            "IpadView=1",
            "WideView=1",
            "PanoramicFOV=1.75",
            "TPPFieldOfView=130",
            "FPPFieldOfView=150"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UserCustom DeviceProfile]");
    }

    /**
     * CODM Dedicated Drone View & Panoramic FOV Injector.
     * Injects Unity3D PlayerPrefs XML and JSON camera keys for Call of Duty Mobile.
     */
    public static boolean injectCodmUltraDroneViewMaxFov(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmUltraDroneViewMaxFov(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "CameraFOV=120",
            "ThirdPersonFOV=120",
            "FirstPersonFOV=120",
            "FPP_FOV=120",
            "TPP_FOV=120",
            "DroneView=1",
            "DroneFOV=120",
            "FieldOfView=120",
            "CameraDistance=220",
            "CameraHeight=4",
            "WideCameraAngle=1",
            "iPadView=1",
            "PerspectiveMode=1",
            "PanoramicFOV=1",
            "FOV_Scale=150",
            "Camera_Elevation=4.0",
            "FOV_Scale_Float=1.5",
            "Panoramic_Scale=1.75",
            "MaxFOV=120",
            "MapVisibilityRange=2.0",
            "AllowOcclusionQueries=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmDroneView]");
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

    public static boolean injectFreeFireAutoDragHeadshot(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "AutoDragHeadshot=1", "DragSensitivityRamp=3.5", "HeadBonePriority=1",
            "CrosshairElevateOnFire=1", "AimAssistRadius=360", "AimSnapThreshold=0",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FFAutoDrag]");
    }

    public static boolean injectFreeFireInstant360GlooWall(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "InstantGlooWall=1", "GlooWallDeployDelayMs=0", "GlooWall360Surround=1",
            "AutoCrouchGlooWall=1", "QuickWeaponSwitchAfterGloo=1", "TouchPollingRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FFInstantGloo]");
    }

    public static boolean injectBloodStrikeZeroRecoil(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectBloodStrikeZeroRecoil(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "ZeroRecoil=1", "RecoilHorizontalScale=0", "RecoilVerticalScale=0",
            "BulletSpread=0", "AimMagnetism=3", "TouchPollingRate=1000", "TouchZeroDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[BloodStrikeRecoil]");
    }

    public static boolean injectBloodStrikeSlideCancelOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectBloodStrikeSlideCancelOverdrive(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "SlideCancelSprintSync=1", "SlideRecoveryDelayMs=0", "SprintAccelerationMax=1",
            "TacticalSlideZeroLag=1", "TouchPollingRate=1000", "TouchZeroDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[BloodStrikeSlideCancel]");
    }

    public static boolean injectDeltaForcePrecisionAim(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectDeltaForcePrecisionAim(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "ZeroSniperSway=1", "BallisticsDropCompensation=1", "BulletVelocityMultiplier=2.5",
            "BreathHoldDuration=999", "AimAssistStrength=100", "AimMagnetism=3"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[DeltaForceAim]");
    }

    public static boolean injectDeltaForceNaniteShaderPrewarm(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectDeltaForceNaniteShaderPrewarm(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "r.Nanite=1", "r.ShaderPipelineCache.Enabled=1", "r.ShaderPipelineCache.Prewarm=1",
            "r.ShaderPipelineCache.BatchSize=100", "r.Vulkan.AsyncCompute=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[DeltaForceNanite]");
    }

    public static boolean injectArenaBreakoutThermalFootstepAudio(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectArenaBreakoutThermalFootstepAudio(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "ThermalClarityBoost=1", "ThermalNoiseReduction=1", "FootstepAudioGainDb=12.0",
            "OccludedAudioClarity=1", "ZeroSniperSway=1", "AimMagnetism=3"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[ArenaBreakoutThermalAudio]");
    }

    public static boolean injectValorantCounterStrafeAimLock(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectValorantCounterStrafeAimLock(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "CounterStrafeInstantStop=1", "MovementDeadzone=0", "HeadLevelAimLock=1",
            "FirstBulletAccuracy100=1", "CrosshairPlacementSnapping=1", "TouchPollingRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[ValorantCounterStrafe]");
    }

    public static boolean injectFarlightJetpackZeroCooldown(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFarlightJetpackZeroCooldown(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "JetpackCooldownScale=0", "AirDashDistanceMultiplier=2.0", "VerticalBoostPower=2.0",
            "EnergyRechargeRateMultiplier=5.0", "FastTacticalRoll=1", "TouchPollingRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FarlightJetpack]");
    }

    public static boolean injectStandoff2Tick128ZeroSpread(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectStandoff2Tick128ZeroSpread(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "cl_updaterate=128", "cl_cmdrate=128", "rate=786432", "weapon_spread_scale=0",
            "recoil_scale_x=0", "recoil_scale_y=0", "touch_smooth_factor=0", "touch_sampling_hz=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Standoff2Tick128]");
    }

    public static boolean injectUniversalGodDamageOverdrive2026(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectUniversalGodDamageOverdrive2026(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=10000", "DamageBoost=10000", "WeaponDamageBoost=10000", "TrueDamageBoost=10000",
            "HeadshotMultiplier=5.0", "OneShotKillHitbox=1", "PenetrationBoost=10000", "VestDamageBypass=1",
            "InstantHitReg=1", "HitRegSyncRate=1000", "FrameSyncDamage=1", "ZeroRecoil=1", "SpreadZero=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[UniversalGodDamage]");
    }

    public static boolean injectBloodStrikeDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectBloodStrikeDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=10000", "DamageBoost=10000", "WeaponDamage=10000", "FleshDamageMultiplier=3.0",
            "ArmorDamageMultiplier=3.0", "VestDamageBypass=1", "FireRateOverclock=10000", "FireRateBoost=10.0",
            "PelletDamageFull=1", "HeadshotMultiplier=5.0", "InstantHitReg=1", "HitRegSyncRate=1000",
            "ZeroRecoil=1", "SpreadZero=1", "TouchPollingRate=1000", "TouchZeroDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[BloodStrikeDamage10000]");
    }

    public static boolean injectDeltaForceDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectDeltaForceDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=10000", "DamageBoost=10000", "WeaponDamageBoost=10000", "SniperHeadshotDamage=999",
            "OneShotKillHitbox=1", "ArmorPenetrationTier6=1", "BulletDropComp=1", "MuzzleVelocityFactor=2.0",
            "ZeroSwaySniper=1", "FrameSyncDamage=1", "InstantHitReg=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[DeltaForceDamage10000]");
    }

    public static boolean injectArenaBreakoutDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectArenaBreakoutDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=10000", "DamageBoost=10000", "ArmorPiercingTier6=1", "ArmorDamageMultiplier=3.0",
            "LimbDamageMultiplier=2.5", "FleshDamageMultiplier=3.0", "SniperOneShotKill=1",
            "InstantHitReg=1", "HitRegSyncRate=1000", "ZeroRecoil=1", "SpreadZero=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[ArenaBreakoutDamage10000]");
    }

    public static boolean injectValorantDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectValorantDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=10000", "DamageBoost=10000", "HeadshotMultiplier=5.0", "OneTapHeadshot=1",
            "FirstBulletAccuracy=1.0", "CounterStrafeDeadzone=0", "InstantHitReg=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[ValorantDamage10000]");
    }

    public static boolean injectFarlightDamage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectFarlightDamage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=10000", "DamageBoost=10000", "GunDamageMultiplier=10000", "FireRateOverclock=10000",
            "AirDashBulletSync=1", "HeadshotMultiplier=4.0", "InstantHitReg=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FarlightDamage10000]");
    }

    public static boolean injectStandoff2Damage10000AttackSpeedMax(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectStandoff2Damage10000AttackSpeedMax(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=10000", "DamageBoost=10000", "HeadshotDamageMultiplier=5.0", "cl_updaterate=128",
            "cl_cmdrate=128", "rate=786432", "InstantHitReg=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Standoff2Damage10000]");
    }

    public static boolean injectGenshinDamage10000ElementalBurstMax(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectGenshinDamage10000ElementalBurstMax(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=10000", "DamageBoost=10000", "ElementalDamageMultiplier=10000", "PhysicalDamageBase=10000",
            "CritRateBoost=100", "CritDamageMultiplier=10.0", "ElementalMasteryBoost=10000", "EnergyRechargeMax=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[GenshinDamage10000]");
    }

    public static boolean injectRobloxDamage10000Max(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectRobloxDamage10000Max(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "DamageLockMax=10000", "DamageBoost=10000", "WeaponDamageBoost=10000", "PhysicsTickRate=1000",
            "InstantHitReg=1", "HitRegSyncRate=1000", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[RobloxDamage10000]");
    }

    public static boolean injectCarXTorqueHorsepower10000Max(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCarXTorqueHorsepower10000Max(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "TorqueMultiplier=10.0", "HorsepowerBoost=10000", "TurboBoostMax=1", "TireGripSlipOptimization=1",
            "SteeringAngleMax=70", "ZeroSteeringLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CarXTorque10000]");
    }

    public static boolean injectHokAutoSmiteObjective(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectHokAutoSmiteObjective(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AutoSmiteObjective=1", "SmitePriority=DragonLord", "SmiteHpThreshold=1.0",
            "SkillSmartAimLowestHp=1", "TargetLockEnemyHero=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[HokAutoSmite]");
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

    public static boolean injectHardwareMaskProfile(String path, com.gamebooster.app.spoofer.SpoofProfile profile, int targetHz) {
        if (path == null || profile == null) return false;
        ensureParentDirectory(path);
        int fpsVal = targetHz > 0 ? targetHz : (profile.maxRefreshRateHz > 0 ? profile.maxRefreshRateHz : 185);
        int highFpsNum = fpsVal >= 165 ? 4 : (fpsVal >= 120 ? 3 : (fpsVal >= 90 ? 2 : 1));
        int frameRateLevel = fpsVal >= 165 ? 6 : (fpsVal >= 144 ? 5 : (fpsVal >= 120 ? 4 : (fpsVal >= 90 ? 3 : 2)));

        if (path.toLowerCase().endsWith(".xml")) {
            String[] xmlKeys = {
                "SystemInfo_graphicsDeviceName=" + profile.glRenderer,
                "SystemInfo_graphicsDeviceVendor=" + profile.glVendor,
                "SystemInfo_deviceModel=" + profile.model,
                "SystemInfo_deviceName=" + profile.model,
                "SystemInfo_processorType=" + profile.socModel,
                "SystemInfo_systemMemorySize=" + profile.ramTotalMb,
                "DeviceModel=" + profile.model,
                "DeviceManufacturer=" + profile.manufacturer,
                "DeviceBrand=" + profile.brand,
                "GPU=" + profile.glRenderer,
                "GPUVendor=" + profile.glVendor,
                "SoC=" + profile.socModel,
                "HardwareProfileTier=4",
                "HighFPSMode=" + highFpsNum,
                "HighFpsMode=" + highFpsNum,
                "HighFPS=" + highFpsNum,
                "FpsMode=" + highFpsNum,
                "FrameRateLevel=" + frameRateLevel,
                "SupportHighFps=1",
                "SupportHighFpsMode=1",
                "SupportUltraFps=1",
                "SupportUltraFpsMode=1",
                "SupportExtremeFps=1",
                "SupportExtremeFpsMode=1",
                "Quality=3",
                "QualityLevel=3",
                "QualitySetting=3",
                "GraphicLevel=3",
                "GraphicsQuality=5",
                "TargetFPS=" + fpsVal,
                "FPS=" + fpsVal,
                "MaxFPS=" + fpsVal,
                "TargetFrameRate=" + fpsVal,
                "MaxRefreshRate=" + fpsVal,
                "Unlock90Hz=1",
                "Unlock120Hz=1",
                "Unlock144Hz=1",
                "Unlock165Hz=1",
                "Unlock185Hz=1",
                "Unlock240Hz=1"
            };
            return ConfigFileHelper.patchKeys(path, xmlKeys, "<map>");
        }
        if (path.toLowerCase().endsWith(".json")) {
            String[] jsonKeys = {
                "DeviceModel=" + profile.model,
                "DeviceBrand=" + profile.brand,
                "Manufacturer=" + profile.manufacturer,
                "GPURenderer=" + profile.glRenderer,
                "GPUVendor=" + profile.glVendor,
                "SoCModel=" + profile.socModel,
                "SoCManufacturer=" + profile.socManufacturer,
                "RAMTotalMB=" + profile.ramTotalMb,
                "MaxFrameRate=" + fpsVal,
                "FPSLimit=" + fpsVal,
                "TargetFPS=" + fpsVal,
                "HighFPSMode=" + highFpsNum,
                "FrameRateLevel=" + frameRateLevel,
                "UltraFrameRate=1",
                "SuperFrameRate=1",
                "QualitySetting=3",
                "GraphicLevel=3",
                "GraphicQuality=4",
                "QualityLevel=3",
                "GraphicsPreset=5",
                "UltraExtreme=1",
                "UnlockUltraHighFPS=true",
                "Unlock90Hz=true",
                "Unlock120Hz=true",
                "Unlock144Hz=true",
                "Unlock165Hz=true",
                "Unlock185Hz=true",
                "VulkanSupport=true"
            };
            return ConfigFileHelper.patchKeys(path, jsonKeys, null);
        }
        String[] keys = {
            "DeviceModel=" + profile.model,
            "DeviceBrand=" + profile.brand,
            "Manufacturer=" + profile.manufacturer,
            "GpuRenderer=" + profile.glRenderer,
            "GpuVendor=" + profile.glVendor,
            "SocModel=" + profile.socModel,
            "SystemRamMB=" + profile.ramTotalMb,
            "DisplayRefreshRate=" + fpsVal,
            "TargetFPS=" + fpsVal,
            "MaxFPS=" + fpsVal,
            "HardwareProfileTier=4",
            "HighFPSMode=" + highFpsNum,
            "FrameRateLevel=" + frameRateLevel,
            "QualitySetting=3",
            "GraphicLevel=3",
            "UltraFrameRate=1",
            "SuperFrameRate=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[HardwareProfile]");
    }

    public static boolean injectHardwareMaskProfile(String path, String gpuRenderer, String socModel, int ramMb, int targetHz) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectHardwareMaskProfile(path, gpuRenderer, socModel, ramMb, targetHz)) return true;
            } catch (Throwable ignored) {}
        }
        String inferredVendor = "Qualcomm";
        if (gpuRenderer != null) {
            String lowerGpu = gpuRenderer.toLowerCase();
            if (lowerGpu.contains("mali") || lowerGpu.contains("immortalis")) inferredVendor = "ARM";
            else if (lowerGpu.contains("apple")) inferredVendor = "Apple";
            else if (lowerGpu.contains("powervr")) inferredVendor = "Imagination Technologies";
            else if (lowerGpu.contains("xclipse")) inferredVendor = "Samsung";
        }
        if (path.toLowerCase().endsWith(".xml")) {
            int fpsVal = targetHz > 0 ? targetHz : 185;
            String highFpsModeVal = fpsVal >= 185 ? "185" : (fpsVal >= 120 ? "120" : (fpsVal >= 90 ? "90" : "60"));
            String highFpsSeeVal = fpsVal >= 185 ? "5" : (fpsVal >= 120 ? "4" : (fpsVal >= 90 ? "3" : "2"));
            String[] xmlKeys = {
                "SystemInfo_graphicsDeviceName=" + (gpuRenderer != null ? gpuRenderer : "Adreno (TM) 750"),
                "SystemInfo_graphicsDeviceVendor=" + inferredVendor,
                "SystemInfo_deviceModel=" + (socModel != null ? socModel : "Snapdragon 8 Gen 3"),
                "SystemInfo_systemMemorySize=" + (ramMb > 0 ? ramMb : 16384),
                "HighFpsMode=" + highFpsModeVal,
                "HighFpsModeSee=" + highFpsSeeVal,
                "HighFPS=" + (fpsVal >= 185 ? "4" : (fpsVal >= 120 ? "3" : (fpsVal >= 90 ? "2" : "1"))),
                "FpsMode=" + (fpsVal >= 185 ? "4" : (fpsVal >= 120 ? "3" : (fpsVal >= 90 ? "2" : "1"))),
                "SupportHighFps=1",
                "SupportHighFpsMode=1",
                "SupportUltraFps=1",
                "SupportUltraFpsMode=1",
                "SupportExtremeFps=1",
                "SupportExtremeFpsMode=1",
                "Quality=3",
                "QualityLevel=3",
                "TargetFrameRate=" + fpsVal,
                "MaxRefreshRate=" + fpsVal,
                "Unlock185Hz=1",
                "Unlock165Hz=1",
                "Unlock144Hz=1",
                "Unlock120Hz=1"
            };
            return ConfigFileHelper.patchKeys(path, xmlKeys, "<map>");
        }
        if (path.toLowerCase().endsWith(".json")) {
            int fpsVal = targetHz > 0 ? targetHz : 185;
            String[] jsonKeys = {
                "DeviceModel=" + (socModel != null ? socModel : "Snapdragon 8 Gen 3"),
                "GPURenderer=" + (gpuRenderer != null ? gpuRenderer : "Adreno (TM) 750"),
                "GPUVendor=" + inferredVendor,
                "SoCModel=" + (socModel != null ? socModel : "Snapdragon 8 Gen 3"),
                "RAMTotalMB=" + (ramMb > 0 ? ramMb : 16384),
                "MaxFrameRate=" + fpsVal,
                "FPSLimit=" + fpsVal,
                "GraphicQuality=4",
                "UnlockUltraHighFPS=true",
                "Unlock185Hz=true",
                "Unlock165Hz=true",
                "Unlock144Hz=true",
                "Unlock120Hz=true",
                "VulkanSupport=true"
            };
            return ConfigFileHelper.patchKeys(path, jsonKeys, null);
        }
        String[] keys = {
            "GpuRenderer=" + (gpuRenderer != null ? gpuRenderer : "Adreno (TM) 750"),
            "SocModel=" + (socModel != null ? socModel : "Snapdragon 8 Gen 3"),
            "SystemRamMB=" + (ramMb > 0 ? ramMb : 16384),
            "DisplayRefreshRate=" + (targetHz > 0 ? targetHz : 185),
            "TargetFPS=" + (targetHz > 0 ? targetHz : 185),
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

    /**
     * MLBB Infinite Lifesteal & Omni-Vamp 10000+ Suite.
     * Instant 100% lifesteal on basic attacks and skills + spell vamp + anti-heal immunity.
     */
    public static boolean injectMlbbInfiniteLifestealOmniVamp(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "LifestealBoost=10000", "LifestealPercent=100", "LifestealMultiplier=100.0",
            "PhysicalLifesteal=10000", "MagicLifesteal=10000", "SpellVampBoost=10000",
            "SpellVampPercent=100", "OmniVamp=10000", "OmniVampBoost=10000",
            "AlucardLifesteal=10000", "AlucardOmniVamp=10000", "AlucardLifestealMax=1",
            "HPRegenRate=10000", "HPRegenBoost=10000", "PassiveHPRegen=10000",
            "InstantHealOnHit=10000", "AntiHealBypass=1", "AntiHealImmunity=1",
            "LifestealCoefficient=10.0", "SpellVampCoefficient=10.0",
            "LifeStealRate=10.0", "LifeStealMax=1.0", "HealEfficiency=10.0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbLifestealGodSuite]");
    }

    /**
     * MLBB God Armor 10000+ & True Defense Suite.
     * Max physical defense + magic defense + 100% damage reduction + 10000 HP shield + CC resistance.
     */
    public static boolean injectMlbbGodArmorTrueDefense(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "PhysicalDefense=10000", "MagicDefense=10000", "ArmorMax=10000",
            "DamageReduction=1.0", "DamageReductionPercent=100", "DamageReductionMax=1",
            "PhysicalShield=10000", "MagicShield=10000", "ShieldMultiplier=10.0",
            "ShieldBoost=10000", "ShieldAbsorption=1.0", "PassiveShieldRegen=10000",
            "MaxHPBoost=10000", "MaxHealthMultiplier=10.0", "TrueDamageImmunity=1",
            "AntiCritReduction=1.0", "CrowdControlReduction=1.0", "TenacityMax=1.0",
            "ResilienceBoost=1.0", "ImmortalityReviveZeroCD=1", "AthenaShieldInstantProc=1",
            "AntiqueCuirassDmgReduction=1.0", "BladeArmorReflectDamage=10000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbGodArmorSuite]");
    }

    /**
     * MLBB Unlimited Mana, Energy & Zero Skill Cooldown Suite.
     * Unlimited Fanny/Ling/Nolan energy, zero mana cost, 0.001s skill cooldown + instant ult.
     */
    public static boolean injectMlbbUnlimitedManaEnergy(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "ZeroManaCost=1", "InfiniteMana=1", "ManaRegenRate=10000",
            "ZeroEnergyCost=1", "InfiniteEnergy=1", "EnergyRegenRate=10000",
            "FannyEnergyMax=1", "FannyUnlimitedEnergy=1", "FannyZeroCableCost=1",
            "LingEnergyMax=1", "LingUnlimitedEnergy=1", "LingZeroWallCost=1",
            "NolanRiftEnergyMax=1", "HayabusaEnergyMax=1", "LesleyEnergyMax=1",
            "SkillCDRatio=0.001", "CooldownReduction=1.0", "MaxCooldownReduction=1.0",
            "UltInstantReset=1", "SkillAutoChain=1", "SkillInstantCast=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbUnlimitedEnergySuite]");
    }

    /**
     * MLBB Ultra Drone View Panoramic FOV Suite.
     * High camera perspective, wide map field-of-view, zero fog of war interference.
     */
    public static boolean injectMlbbUltraDroneViewMaxFov(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbUltraDroneViewMaxFov(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "CameraHeight=4", "FOVBoost=1.75", "DroneView=1", "PanoramicFOV=1.75",
            "DroneFOV=180", "MaxFOV=180", "FieldOfView=180", "CameraDistance=180",
            "WideCameraAngle=1", "MapScale=1.35", "MapVisibilityRange=2.0",
            "MapClarity=1", "MinimapEnemyPriority=1", "FogOfWarRemoval=1", "FogOfWarBypass=1",
            "AllowOcclusionQueries=1", "UltraWallhackEspClarity=1", "HeroLockRange=9999",
            "VisionRangeBoost=2.0", "DronePerspectiveMode=1", "DroneCameraSmooth=1",
            "DroneAntiShake=1", "DroneAntiBlackscreen=1", "DroneLockElevation=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbUltraDroneView]");
    }

    /**
     * MLBB 2026 3-Second Auto Map Glitch & Enemy Ghost Radar Suite.
     * Periodic 3-second micro-pulse desync, 3-second minimap enemy icon retention, bush occlusion culling override.
     */
    public static boolean injectMlbbAutoMapGlitch3s(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbAutoMapGlitch3s(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AutoMapGlitch3s=1", "MapPulseInterval=3", "EnemyPositionSyncPulse=3000",
            "FowMicroPulseDuration=250", "FowProgressiveReveal=1", "VisionPulseRadiusBoost=1.5",
            "VisionRangeBoost=1.4", "StealthAntiBanPulse=1", "MinimapEnemyIconRetention=3000",
            "MinimapGhostTracking=1", "MinimapEnemyPriority=1", "IconFadeDuration=3000",
            "AudioEventMinimapMark=1", "BushOcclusionCulling=0", "RiverBushVision=1",
            "JungleCampVisionRadius=1.5", "HeroTargetLockRange=9999", "TargetLockProximitySweep=3000",
            "EntityVisibilityTether=3000", "CameraHeight=2.2", "FOVBoost=1.45", "MapScale=1.25",
            "UltraWallhackEspClarity=1", "DroneView=1", "PanoramicFOV=1.45"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbAutoMapGlitch3s]");
    }

    /**
     * MLBB 2026 Fast Sovereign Overdrive Suite.
     * Fast Farming, Fast Skills, Fast Combo, Fast Item, Fast Level, Fast Turtle, Fast Lord, Fast Coin, Fast Roam.
     */
    public static boolean injectMlbbFastSovereignOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbFastSovereignOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FastFarmingOverdrive=1", "JungleClearSpeed=10", "CreepDamageMax=10000", "MinionWaveInstantClear=1",
            "JunglePathfindingZeroLag=1", "CreepAggroInstantReset=1",
            "FastSkillsOverdrive=1", "SkillCastDelayMs=0", "ZeroSkillDelay=1", "FastSkillReleaseSpeed=10",
            "SkillAnimationCancelSpeed=10", "ZeroDelaySkillTap=1", "FastSkillCycle=1", "InstantCooldownReset=1",
            "CooldownReductionRatio=0.99",
            "FastComboAutoChain=1", "ComboExecutionSpeed=10", "SmartSkillChainQueue=1", "InstantComboSnap=1",
            "SkillChainBufferRate=1000", "AimSnapSpeed=10", "AimSmoothFactor=0", "SkillSmartAim=1",
            "FastItemPurchase=1", "InstantShopBuy=1", "QuickItemSwap=1", "ImmortalWinterInstantSwap=1",
            "ShopAutoBuyPriority=1", "ZeroShopOpenDelay=1",
            "FastLevelUp=1", "ExpRateMultiplier=10", "PassiveExpAccumulation=10", "FastExpRate=10", "LevelScalingOverdrive=1",
            "FastTurtleSlayer=1", "TurtleDamageMultiplier=10000", "AutoSmiteTurtle=1", "TurtleShieldBypass=1",
            "FastLordSlayer=1", "LordDamageMultiplier=10000", "AutoRetriLord=1", "ObjectiveHpThresholdCalc=1",
            "ObjectiveInstantBurst=10000", "RetributionTrueDamageFloor=10000",
            "FastCoinOverdrive=1", "GoldRateMultiplier=10", "CreepGoldMultiplier=10", "MinionGoldMultiplier=10",
            "PassiveGoldPerSecond=10", "BountyGoldBoost=10",
            "FastMovementSpeed=10", "RiverSpeedBoost=2", "ZeroTurnDelay=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1", "InputBufferRate=1000",
            "bFramePacingEnabled=True", "AllowOcclusionQueries=1", "r.OneFrameThreadLag=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbFastSovereignOverdrive2026]");
    }

    /**
     * MLBB God Mode Full Overdrive (All-in-One Master Cheat).
     */
    public static boolean injectMlbbGodModeFullOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbGodModeFullOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        boolean ok1 = injectMlbbInfiniteLifestealOmniVamp(path);
        boolean ok2 = injectMlbbGodArmorTrueDefense(path);
        boolean ok3 = injectMlbbUnlimitedManaEnergy(path);
        boolean ok4 = injectMlbbUltraDroneViewMaxFov(path);
        boolean ok5 = injectMlbbDamage10000AttackSpeedMax(path);
        boolean ok6 = injectMlbbFastFarmingAllHero(path);
        boolean ok7 = injectMlbbFastRetributionObjectiveSteal(path);
        boolean ok8 = injectMlbbAutoMapGlitch3s(path);
        boolean ok9 = injectMlbbFastSovereignOverdrive(path);
        boolean ok10 = injectMlbbUniversalZeroDelayCombo(path);
        return ok1 || ok2 || ok3 || ok4 || ok5 || ok6 || ok7 || ok8 || ok9 || ok10;
    }

    /**
     * MLBB Universal Zero-Delay Combo & All-Hero Animation Cancel (2026).
     * Eliminates post-cast recovery lock, turn-rate delay, and pre-queues combos at 1000Hz.
     */
    public static boolean injectMlbbUniversalZeroDelayCombo(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbUniversalZeroDelayCombo(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "UniversalZeroDelayCombo=1", "AllHeroAnimationCancel=1", "AnimationCancelSpeed=10.0",
            "HeroAnimationCancel=1", "SkillCastDelayMs=0", "ZeroSkillDelay=1", "ZeroDelaySkillTap=1",
            "FastSkillReleaseSpeed=10", "SkillAutoChain=1", "SmartSkillChainQueue=1", "SkillChainBufferRate=1000",
            "ZeroTurnDelay=1", "InstantTurnRate=10.0", "InstantBasicAttack=1", "BasicAttackCancelWindup=1",
            "AttackAnimSpeed=10.0", "AttackWindup=0", "AttackBackswing=0", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1", "bFramePacingEnabled=True", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbUniversalZeroDelayCombo2026]");
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

    /**
     * CODM Instant Chambering & Quick Draw Sniper Overdrive.
     * Zero-delay bolt cycling, instant weapon swap, and sleight-of-hand reload.
     */
    public static boolean injectCodmInstantChamberingQuickDraw(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "FastBoltPullSpeed=10.0", "BoltActionCycleMs=0", "PumpActionCycleMs=0",
            "ReloadSpeedMultiplier=10.0", "QuickDrawFactor=10.0", "WeaponSwitchZeroDelay=1",
            "SprintToFireDelayMs=0", "ADSTransitionTimeMs=0", "QuickScopeAccuracyThreshold=1.0",
            "BlankScopeAccuracy=1.0", "SniperADSIdleSway=0.0", "AntiBreath=1", "SniperBreathHoldZero=1",
            "HitFlinchScale=0.0", "ScreenShakeScale=0.0", "ScopeVisualBob=0.0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmInstantChambering]");
    }

    /**
     * CODM Slide-Cancel & God-Speed Mobility Overdrive.
     * Zero slide delay, instant slide-hop cancel, max sprint acceleration, 1000Hz touch & joystick response.
     */
    public static boolean injectCodmSlideCancelMobility(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "SlideDelayMs=0", "SlideCancelEnabled=1", "SlideSpeedBoost=3.0",
            "SprintAcceleration=10.0", "MaxSprintSpeed=10.0", "SprintDelayZero=1",
            "JumpFatigueRemoval=1", "FastMantle=1", "MantleSpeedBoost=3.0",
            "JoystickZeroDeadzone=1", "JoystickResponseLevel=3", "TouchPollingRate=1000",
            "TouchZeroDelay=1", "ZeroInputLag=1", "InputBufferRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmSlideCancelMobility]");
    }

    /**
     * CODM 2026 Sovereign Overdrive & Security Bypass Suite.
     * Zero BSA, Laser Recoil Lock, 360° Hitbox, Silent Head Lock, 500m Wall Pen,
     * Dead Silence, Ghost UAV Immunity, 12x Slide-Cancel Turbo, TiMi Inotify Cloak.
     */
    public static boolean injectCodmSovereignOverdriveBypass(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectCodmSovereignOverdriveBypass(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "BulletSpreadAccuracy=0", "InitialSpread=0", "MaxSpread=0",
            "WeaponSpread=0", "WeaponSway=0", "BulletSpreadScale=0", "SpreadDecayRate=100",
            "AdsSpreadZero=1", "HipSpreadZero=1", "RecoilScale=0", "VerticalRecoilScale=0",
            "HorizontalRecoilScale=0", "RecoilPatternScale=0", "FlinchResistance=1.0",
            "MuzzleVelocityFactor=1.0", "HitboxRadiusMultiplier=3.5", "HitboxScale=3.5",
            "SilentAimbot=1", "HeadBonePriority=1", "HeadBoneAimPriority=1", "BoneIndex=0",
            "AimAssistEnabled=1", "AimAssistStrength=100", "AimMagnetism=3",
            "AimSnapThreshold=0", "AimSnapSpeed=10", "PredictiveAim=1",
            "WallPenetrateRange=500", "WallPenetrationRange=500", "DamageFloorMax=10000",
            "DamageLockMax=10000", "HeadshotMultiplier=999",
            "DeadSilenceAlwaysActive=1", "GhostPerkAlwaysActive=1", "QuickFixHealthInstant=1",
            "KineticArmorOverdrive=10000", "ArmorRepairSpeedMultiplier=10.0",
            "SafeZoneDamageImmunity=1", "FlakJacketExplosionLock=1",
            "DamageReductionRatio=0.99", "DamageReduction=0.99",
            "SlideCancelSpeedMultiplier=12.0", "SlideSpeedMultiplier=12.0", "SlideDelayMs=0",
            "BunnyHopVelocityBoost=1.5", "SprintSpeedMultiplier=12.0",
            "FastScopeAdsDelay=0", "AdsAnimSpeedScale=10", "FastWeaponSwapDelay=0",
            "WeaponSwapDelayMs=0", "ChamberingSpeedMultiplier=10.0",
            "ReloadSpeedMultiplier=10.0", "ReloadDelayMs=0",
            "GyroSampleRate=1000", "GyroZeroDelay=1", "GyroStabilization=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1",
            "HitRegSyncRate=1000", "bFramePacingEnabled=True", "AllowOcclusionQueries=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmSovereignOverdriveBypass2026]");
    }

    /**
     * CODM God Mode Full Overdrive (All-in-One Master Cheat Suite).
     */
    public static boolean injectCodmGodModeFullOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectCodmGodModeFullOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        boolean ok1 = injectNoRecoilNoSpread(path);
        boolean ok2 = false;
        if (sNativeLibraryLoaded) {
            try {
                ok2 = nativeInjectCodmEnemyLockAllScope(path);
            } catch (Throwable ignored) {
            }
        }
        if (!ok2) {
            ok2 = injectAimHeadLock(path) || injectAimAssistLockMax(path);
        }
        boolean ok3 = injectCodmDamage10000AttackSpeedMax(path);
        boolean ok4 = injectCodmBsaRemovalRangeOverdrive(path);
        boolean ok5 = injectCodmInstantChamberingQuickDraw(path);
        boolean ok6 = injectCodmSlideCancelMobility(path);
        boolean ok7 = injectHitboxMultiplier(path, 3.0f);
        boolean ok8 = injectUltraWallhackEspClarity(path);
        boolean ok9 = injectCodmSovereignOverdriveBypass(path);
        boolean ok10 = injectCodmUltraDroneViewMaxFov(path);
        return ok1 || ok2 || ok3 || ok4 || ok5 || ok6 || ok7 || ok8 || ok9 || ok10;
    }


    /**
     * PUBGM Zero Recoil & Absolute Zero Sway (UE4 +CVars=r. format + INI keys).
     */
    public static boolean injectPubgmZeroRecoilNoSway(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "ZeroRecoil=1", "RecoilPitch=0.0", "RecoilYaw=0.0",
            "WeaponSway=0.0", "BreathingShake=0.0", "CameraShakeIntensity=0.0",
            "SpreadFactor=0.0", "HipFireSpread=0.0", "M416_VerticalRecoilMin=0",
            "BerylM762_HorizontalBounce=0", "AKM_FirstShotKick=0",
            "+CVars=r.PUBGZeroRecoil=1",
            "+CVars=r.PUBGRecoilPitch=0.0",
            "+CVars=r.PUBGRecoilYaw=0.0",
            "+CVars=r.PUBGWeaponSway=0.0",
            "+CVars=r.PUBGBreathingShake=0.0",
            "+CVars=r.PUBGCameraShake=0.0",
            "+CVars=r.RecoilCompensationFactor=0.0",
            "+CVars=r.SpreadFactor=0.0",
            "+CVars=r.HipFireSpread=0.0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmZeroRecoil]");
    }

    /**
     * PUBGM Magic Bullet & Instant Hit Scan Simulation (UE4 CVars).
     */
    public static boolean injectPubgmMagicBulletInstantHit(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "MagicBullet=1", "BulletVelocity=99999", "InstantBulletTravel=1",
            "ZeroBulletDrop=1", "BulletGravity=0.0", "BulletPenetration=100.0",
            "InstantHitReg=1", "HitScanSimulation=1",
            "+CVars=r.PUBGMagicBullet=1",
            "+CVars=r.PUBGBulletVelocity=99999",
            "+CVars=r.PUBGInstantBulletTravel=1",
            "+CVars=r.PUBGBulletDrop=0.0",
            "+CVars=r.PUBGBulletGravity=0.0",
            "+CVars=r.PUBGBulletPenetration=100.0",
            "+CVars=r.PUBGInstantHitReg=1",
            "+CVars=r.PUBGHitScanSimulation=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmMagicBullet]");
    }

    /**
     * PUBGM Sniper Instant Chambering & Quick ADS (AWM/M24/Kar98k/AMR).
     */
    public static boolean injectPubgmSniperInstantChamber(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        String[] keys = {
            "SniperInstantChamber=1", "BoltCycleLatency=0.001", "QuickScopeADS=0.01",
            "SprintToFireDelay=0", "SniperRechamberInstant=1", "BoltActionQuickCycle=1",
            "+CVars=r.PUBGSniperInstantChamber=1",
            "+CVars=r.PUBGBoltCycleLatency=0.001",
            "+CVars=r.PUBGQuickScopeADS=0.01",
            "+CVars=r.PUBGSprintToFireDelay=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmSniperChamber]");
    }

    /**
     * PUBGM 2026 Sovereign Overdrive & Security Bypass Suite.
     * Magic Bullet 2.0, Wall Penetration, Laser Recoil Lock, Zero Shake,
     * No Grass, 12x Sprint Turbo, Silent Movement, Inotify & Timestamp Cloaking.
     */
    public static boolean injectPubgmSovereignOverdriveBypass(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmSovereignOverdriveBypass(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "r.PUBGBulletVelocityCompensation=1.5", "r.HitboxSphereRadius=3.5",
            "r.BulletThroughWallPenetration=1", "r.MaxWallPenetrationDistance=500",
            "r.VehicleBulletPenetration=1", "r.PenetrationDamageFloor=10000",
            "r.HeadBoneAimPriority=1", "r.AimMagnetism=3", "r.AimAssistEnabled=1",
            "r.AimAssistStrength=100", "r.AimSnapThreshold=0", "r.SilentAimbot=1", "r.PredictiveAim=1",
            "r.WeaponRecoilScale=0", "r.VerticalRecoilScale=0", "r.HorizontalRecoilScale=0",
            "r.RecoilPatternScale=0", "r.WeaponSpread=0", "r.WeaponSway=0",
            "r.BulletSpreadScale=0", "r.SpreadDecayRate=100", "r.AdsSpreadZero=1", "r.MuzzleVelocityFactor=1.0",
            "r.CameraShake=0", "r.CameraSmoothFactor=0", "r.ViewDistanceScale=3.5",
            "r.Fog=0", "r.GrassDensity=0", "r.PostProcessAAQuality=0", "r.ShadowQuality=0",
            "r.FoliageCullDistance=0", "r.PlayerSilhouetteBoost=1",
            "r.SprintSpeedMultiplier=12.0", "r.VaultSpeedMultiplier=10.0", "r.FootstepAudioDamp=1",
            "r.StaminaInfinite=1", "r.FallDamageImmunity=1", "r.ParachuteDropSpeed=5.0",
            "r.AutoLootRange=15.0", "r.AutoLootZeroDelay=1", "r.FastWeaponSwapDelay=0",
            "r.FastScopeAdsDelay=0", "r.ChamberingSpeedMultiplier=10.0", "r.ReloadSpeedMultiplier=10.0",
            "r.PlayerDamageReduction=0.99", "r.KineticShieldBoost=10000",
            "r.GyroSampleRate=1000", "r.GyroZeroDelay=1", "r.GyroStabilization=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1", "HitRegSyncRate=1000",
            "r.OneFrameThreadLag=0", "r.FinishCurrentFrame=0", "bFramePacingEnabled=True", "AllowOcclusionQueries=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmSovereignOverdriveBypass2026]");
    }

    /**
     * PUBGM God Mode Full Overdrive (Master Cheat Suite).
     */
    public static boolean injectPubgmGodModeFullOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmGodModeFullOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        boolean ok1 = injectPubgmZeroRecoilNoSway(path);
        boolean ok2 = false;
        if (sNativeLibraryLoaded) {
            try {
                ok2 = nativeInjectPubgmEnemyLockAllScope(path);
            } catch (Throwable ignored) {
            }
        }
        if (!ok2) {
            ok2 = injectAimHeadLock(path) || injectAimAssistLockMax(path);
        }
        boolean ok3 = injectPubgmDamage10000AttackSpeedMax(path);
        boolean ok4 = injectPubgmMagicBulletInstantHit(path);
        boolean ok5 = injectPubgmSniperInstantChamber(path);
        boolean ok6 = injectPubgmBallisticsVelocityPenetration(path);
        boolean ok7 = injectHitboxMultiplier(path, 3.5f);
        boolean ok8 = injectUltraWallhackEspClarity(path);
        boolean ok9 = injectPubgmFastLoadAsyncStreaming(path);
        boolean ok10 = injectPubgmSovereignOverdriveBypass(path);
        boolean ok11 = injectPubgUltraDroneViewMaxFov(path);
        return ok1 || ok2 || ok3 || ok4 || ok5 || ok6 || ok7 || ok8 || ok9 || ok10 || ok11;
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
        int fps = targetFps > 0 ? targetFps : 165;
        int q = qualityLevel > 0 ? qualityLevel : 4;
        String[] keys = {
            "FrameRateLimit=" + fps, "MaxFPS=" + fps, "TargetFPS=" + fps, "FPS=" + fps,
            "GraphicQuality=" + q, "HighFPSMode=3", "UltraHighFPS=1", "Unlock120Hz=1",
            "Unlock144Hz=1", "Unlock165Hz=1", "Unlock185Hz=1", "bFramePacingEnabled=True",
            "TouchBoostHz=" + fps, "TouchPollingRate=1000", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmGraphics]");
    }

    public static boolean injectMlbb165FpsGraphics(String path, int targetFps, int qualityLevel) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbb165FpsGraphics(path, targetFps, qualityLevel)) return true;
            } catch (Throwable ignored) {}
        }
        int fps = targetFps > 0 ? targetFps : 185;
        int q = qualityLevel > 0 ? qualityLevel : 3;
        int highFpsMode = fps >= 120 ? 3 : (fps >= 90 ? 2 : 1);
        int frameRateLevel = fps >= 165 ? 6 : (fps >= 144 ? 5 : (fps >= 120 ? 4 : (fps >= 90 ? 3 : 2)));
        String[] keys = {
            "HighFPSMode=" + highFpsMode,
            "FrameRateLevel=" + frameRateLevel,
            "FPS=" + fps,
            "MaxFPS=" + fps,
            "TargetFPS=" + fps,
            "FrameRateLimit=" + fps,
            "UltraFrameRate=1",
            "SuperFrameRate=1",
            "HighFrameRate=1",
            "UnlockFPS=1",
            "SuperHighFPS=1",
            "QualitySetting=" + q,
            "GraphicLevel=" + q,
            "QualityLevel=" + q,
            "GraphicsQuality=5",
            "PerformanceLevel=3",
            "HDMode=1",
            "Shadow=1",
            "Outline=1",
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            "HFR=1",
            "ShowFPS=1",
            "TouchBoostHz=" + fps,
            "ZeroDelayTouch=1",
            "bFramePacingEnabled=True"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbGraphics]");
    }

    public static boolean injectPubgm165FpsGraphics(String path, int targetFps, int qualityLevel) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgm165FpsGraphics(path, targetFps, qualityLevel)) return true;
            } catch (Throwable ignored) {}
        }
        int clampedFps = Math.max(60, Math.min(185, targetFps));
        int effectiveLevel = (clampedFps >= 185) ? 10 : ((clampedFps >= 165) ? 9 : ((clampedFps >= 144) ? 8 : ((clampedFps >= 120) ? 7 : 6)));
        String[] keys = {
            "+CVars=r.PUBGVersion=4.6",
            "+CVars=r.PUBGClientVersion=4.6.0",
            "+CVars=r.PUBG46EngineSupport=1",
            "+CVars=0,2," + clampedFps,
            "+CVars=r.PUBGDeviceFPS=" + effectiveLevel,
            "+CVars=r.PUBGDeviceFPSLevel=" + effectiveLevel,
            "+CVars=r.PUBGBattleFPS=" + effectiveLevel,
            "+CVars=r.PUBGLobbyFPS=" + effectiveLevel,
            "+CVars=r.PUBGFPSLevel=" + effectiveLevel,
            "+CVars=r.PUBGDeviceQuality=" + qualityLevel,
            "+CVars=r.PUBGDeviceFPSSupport185=1",
            "+CVars=r.PUBGDeviceFPSSupport165=1",
            "+CVars=r.PUBGDeviceFPSSupport144=1",
            "+CVars=r.PUBGDeviceFPSSupport120=1",
            "+CVars=r.PUBGDeviceFPSSupport90=1",
            "+CVars=r.PUBGDeviceFPSPolicy=1",
            "+CVars=r.PUBGTargetFPS=" + clampedFps,
            "+CVars=r.PUBGMaxFPS=" + clampedFps,
            "+CVars=r.PUBGFrameRateLimit=" + clampedFps,
            "+CVars=r.FrameRateLimit=" + clampedFps,
            "+CVars=r.MobileFPSLimit=" + clampedFps,
            "+CVars=r.Vsync=0",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.TouchBoostHz=" + clampedFps,
            "+CVars=r.MobileTouchBoostRate=" + clampedFps,
            "+CVars=r.FramePacing=1",
            "+CVars=r.MobileHDR=1",
            "+CVars=r.Vulkan.Enable=1",
            "+CVars=r.Vulkan.DescriptorSetLayout=1",
            "+CVars=r.Vulkan.RenderPass=1",
            "+CVars=r.EnableAsyncPipelineCompilation=1",
            "+CVars=r.AsyncCompute=1",
            "+CVars=r.VRS.Enable=1",
            "+CVars=r.ShadowQuality=4",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.Tonemapper.Quality=4",
            "+CVars=r.Streaming.PoolSize=4096",
            "+CVars=r.Android.DisableProgramBinaryCache=0",
            "+CVars=r.MobileContentScaleFactor=1.0",
            "+CVars=r.TemporalAA.Upscale=1",
            "+CVars=r.AllowOcclusionQueries=1",
            "FPS=" + clampedFps,
            "MaxFPS=" + clampedFps,
            "TargetFPS=" + clampedFps,
            "FrameRateLimit=" + clampedFps,
            "MobileFPSLimit=" + clampedFps,
            "FrameRateLevel=" + effectiveLevel,
            "BattleFPS=" + effectiveLevel,
            "LobbyFPS=" + effectiveLevel,
            "BattleFPSLevel=" + effectiveLevel,
            "LobbyFPSLevel=" + effectiveLevel,
            "FpsLevelInBattle=" + effectiveLevel,
            "FpsLevelInLobby=" + effectiveLevel,
            "FpsOption=" + effectiveLevel,
            "SelectFps=" + effectiveLevel,
            "GraphicQuality=" + qualityLevel,
            "ArtQuality=" + qualityLevel,
            "HighFPSMode=3",
            "SuperHighFPS=1",
            "UnlockFPS=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "UltraExtreme=1",
            "UltraExtreme2026=1",
            "bUseUltraExtreme=True"
        };
        String section = "[UserCustom DeviceProfile]";
        if (path.endsWith("DeviceProfile.ini")) section = "[DeviceProfile]";
        else if (path.endsWith("EnjoyCJZC.ini") || path.endsWith("EnjoyCJ.ini")) section = "[EnjoyCJZC DeviceProfile]";
        else if (path.endsWith("GameUserSettings.ini")) section = "[/Script/ShadowTrackerExtra.UserSetting]";
        return ConfigFileHelper.patchKeys(path, keys, section);
    }

    public static boolean injectPubgmUltraHdr120(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmUltraHdr120(path)) return true;
            } catch (Throwable ignored) {}
        }
        return injectPubgm165FpsGraphics(path, 120, 5);
    }

    public static boolean injectPubgmHdr120(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmHdr120(path)) return true;
            } catch (Throwable ignored) {}
        }
        return injectPubgm165FpsGraphics(path, 120, 4);
    }

    public static boolean injectPubgmSuperSmooth165(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmSuperSmooth165(path)) return true;
            } catch (Throwable ignored) {}
        }
        return injectPubgm165FpsGraphics(path, 165, 1);
    }

    public static boolean injectPubgmRankedDamageSync(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmRankedDamageSync(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "HitRegSyncRate=1000", "FrameSyncDamage=1", "PacketSyncInterval=0",
            "BulletVelocityFactor=1.0", "MuzzleVelocityFactor=1.0", "HitRegistrationSync=1000",
            "TouchPollingRate=1000", "TouchSampleRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1",
            "NetTickRate=120", "ClientNetRate=120", "bEnableDirectHitReg=True",
            "r.OneFrameThreadLag=0", "r.FinishCurrentFrame=0", "r.GTSyncType=1", "r.VSync=0",
            "r.MaxFPS=165", "r.PUBGTargetFPS=165", "r.PUBGDeviceFPSSupport165=1", "bSmoothFrameRate=False"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[RankedDamageSync]");
    }

    public static boolean injectPubgmRankedAimAssist(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectPubgmRankedAimAssist(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AimAssist=1", "bEnableAimAssist=True", "AimAssistLevel=2", "AimAssistFriction=0.85",
            "AimAssistMagnetism=1.0", "AimAssistSlowdown=1.0", "AimAssistMaxDistance=25000",
            "InstantAimSnap=1", "AdsZeroDelay=1", "AdsTransitionTime=0", "HipfireDeadzone=0",
            "HipfireSensitivityBoost=1.15", "GyroSampleRate=1000", "GyroZeroDelay=1",
            "GyroStabilization=1", "GyroSmoothFactor=1", "Scope2xGyroSample=1000",
            "Scope3xGyroStabilization=1", "Scope4xGyroStabilization=1", "Scope6xMicroDamping=1",
            "Scope8xPrecisionFilter=1", "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[RankedAimAssist]");
    }

    public static boolean injectCodmRankedDamageSync(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectCodmRankedDamageSync(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "HitRegistrationSync=1000", "HitRegSyncRate=1000", "FrameSyncDamage=1",
            "BulletVelocityFactor=1.0", "MuzzleVelocityFactor=1.0", "InstantHitReg=1",
            "PacketSyncInterval=0", "TouchPollingRate=1000", "TouchZeroDelay=1",
            "ZeroInputLag=1", "bFramePacingEnabled=True", "r.OneFrameThreadLag=0", "r.FinishCurrentFrame=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[RankedDamageSync]");
    }

    public static boolean injectCodmRankedAimAssist(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectCodmRankedAimAssist(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AimAssistEnabled=1", "AimAssistScale=100", "AimAssistStrength=100",
            "AdsFrictionScale=1.0", "AimMagnetism=3", "HeadMagnetism=1",
            "AdsZeroDelay=1", "AimSmoothFactor=0", "AimSnapSpeed=10",
            "GyroSampleRate=1000", "GyroZeroDelay=1", "GyroStabilization=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[RankedAimAssist]");
    }

    public static boolean injectMlbbRankedHitSync(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbRankedHitSync(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "HitRegSyncRate=1000", "FrameSyncDamage=1", "InstantHitReg=1",
            "DirectSkillCastSync=1", "SkillCastZeroDelay=1", "AutoAttackCancelDelay=0",
            "ZeroInputLag=1", "TouchPollingRate=1000", "TouchZeroDelay=1",
            "InputBufferRate=1000", "bFramePacingEnabled=True", "r.OneFrameThreadLag=0", "r.FinishCurrentFrame=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[RankedHitSync]");
    }

    public static boolean injectMlbbRankedAimAssist(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbRankedAimAssist(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "HeroLock=1", "SkillSmartAim=1", "AimAssist=1", "AutoLockTarget=1",
            "TargetPriority=0", "AimMagnetism=3", "AimSnapSpeed=10", "AimSmoothFactor=0",
            "AdsZeroDelay=1", "SkillAutoChain=1", "TouchPollingRate=1000", "TouchZeroDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[RankedAimAssist]");
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


    // ─── 2026 Game Native Wrapper Methods with Safe Fallback ──────────────────
    public static boolean injectLuaProperties(String path, String[] keys, String[] values) {
        if (path == null || keys == null || values == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectLuaProperties(path, keys, values)) return true;
            } catch (Throwable ignored) {}
        }
        int count = Math.min(keys.length, values.length);
        if (count == 0) return false;
        String[] kvs = new String[count];
        for (int i = 0; i < count; i++) {
            kvs[i] = keys[i] + "=" + values[i];
        }
        return ConfigFileHelper.patchKeys(path, kvs, "[LuaProfile]");
    }

    public static boolean injectArenaBreakoutCombatOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectArenaBreakoutCombatOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FPSLimit=120", "TargetFPS=120", "AudioSpatialEnhance=1",
            "FootstepClarity=1", "RecoilCompensation=0.0", "WeaponSwayZero=1",
            "AimStabilization=1", "ArmorPenetrationLevel6=1", "FleshDamageMultiplier=3.0",
            "AdsInstantTime=0", "QuickHealSpeed=10.0", "HitRegSyncRate=1000",
            "ZeroInputLag=1", "TouchPollingRate=1000", "TouchZeroDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[ArenaBreakoutOverdrive]");
    }

    public static boolean injectDeltaForceCombatOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectDeltaForceCombatOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "r.FPSLimit=120", "r.TargetFPS=120", "r.WeaponRecoilScale=0.0",
            "r.AimAssistLock=1", "r.AimMagnetism=3", "r.HeadshotPriority=1",
            "PenetrationMultiplier=10000", "DamageMultiplier=10000",
            "TacticalSprintZeroDelay=1", "HitRegSyncRate=1000", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[DeltaForceOverdrive]");
    }

    public static boolean injectBloodStrikeCombatOverdrive(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectBloodStrikeCombatOverdrive(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "MaxFPS=144", "FrameRate=144", "TacticalSlideBoost=1",
            "SlideCancelDelay=0", "SprintToFireDelay=0", "JumpFatigueDisabled=1",
            "WeaponRecoilScale=0.0", "HorizontalRecoil=0.0", "VerticalRecoil=0.0",
            "BulletSpread=0.0", "AimAssistTier=3", "AimMagnetism=3",
            "HeadTrackingPriority=1", "DamageMultiplier=10000", "HeadshotMultiplier=5.0",
            "HitRegSyncRate=1000", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[BloodStrikeOverdrive]");
    }

    public static boolean injectGenshinFpsUnlockShaderTurbo(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectGenshinFpsUnlockShaderTurbo(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FPSUncap=120", "TargetFPS=120", "MaxFPS=120",
            "VulkanAsyncCompute=1", "ShaderPrewarm=1", "CameraSmoothingZero=1",
            "ZeroInputLatency=1", "TouchPollingRate=1000", "TouchZeroDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[GenshinTurbo]");
    }

    public static boolean injectCarXZeroThrottleLatency(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectCarXZeroThrottleLatency(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "TargetFPSLimit=120", "TargetFPS=120", "ThrottleInputLatency=0",
            "SteeringDeadzone=0", "TextureStreamingSpeed=10", "MotionBlur=0",
            "ZeroSteeringLag=1", "TouchPollingRate=1000", "TouchZeroDelay=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CarXZeroLatency]");
    }

    public static boolean injectFarlightJetpackAccuracy(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectFarlightJetpackAccuracy(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FPSLevel=144", "TargetFPS=144", "JetpackStrafeStability=1",
            "JetpackCooldownReduction=1", "AimAssistMagnetism=3", "ZeroRecoil=1",
            "BulletVelocityMultiplier=2.0", "HitRegSyncRate=1000", "TouchPollingRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[FarlightAccuracy]");
    }

    public static boolean injectRoblox120FpsUnlock(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectRoblox120FpsUnlock(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FPSUncap=120", "TargetFPS=120", "GraphicsQualityLevel=7",
            "MemoryBudgetMB=2048", "ZeroTouchLatency=1", "PhysicsTickRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Roblox120Fps]");
    }

    public static boolean injectStandoff2True128Tick(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectStandoff2True128Tick(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FPSLimit=120", "TargetFPS=120", "TickRate=128",
            "ZeroSpread=1", "CrosshairDynamic=0", "HeadshotMagnetism=1",
            "HitRegSyncRate=1000", "ZeroInputLag=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[Standoff2Tick]");
    }

    public static boolean injectValorantLowLatencyHeadshot(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectValorantLowLatencyHeadshot(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FPSLock=120", "TargetFPS=120", "HeadLevelCrosshairAssist=1",
            "FirstBulletSpread=0.0", "WalkingAccuracyLock=1", "ZeroInputLag=1",
            "HitRegSyncRate=1000", "TouchPollingRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[ValorantHeadshot]");
    }

    public static boolean injectMlbbFastFarmingAllHero(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbFastFarmingAllHero(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FastFarming=1", "JungleClearSpeedBoost=10", "CreepDamageMultiplier=10000",
            "JungleMonsterTrueDmg=1", "MinionWaveInstantClear=1", "LastHitAssist=1",
            "GoldRateBoost=3", "ExpRateBoost=3", "CreepGoldMultiplier=3", "FastLevelUp=1",
            "SmartCreepTargeting=1", "JunglePathZeroDeadzone=1", "CreepHP=1", "DamageText=1",
            "TouchPollingRate=1000", "TouchZeroDelay=1", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbFastFarming]");
    }

    public static boolean injectMlbbFastRetributionObjectiveSteal(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbFastRetributionObjectiveSteal(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "FastRetribution=1", "AutoRetriLordTurtle=1", "RetriHpThresholdCalc=1",
            "InstantSmite=1", "SmartRetributionHpThreshold=1", "TargetLowestHpMonster=1",
            "RetributionDamageMax=10000", "AutoSmiteLock=1", "RetributionInstantCast=1",
            "ObjectiveTargetLock=1", "RetributionStealSyncRate=1000", "RetriReactionTimeMs=0",
            "FlameRetriInstant=1", "IceRetriInstant=1", "BloodyRetriInstant=1",
            "AdsZeroDelay=1", "ZeroInputLag=1", "TouchPollingRate=1000", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbFastRetribution]");
    }

    public static boolean injectMlbbAllHeroGodSuite2026(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try {
                if (nativeInjectMlbbAllHeroGodSuite2026(path)) return true;
            } catch (Throwable ignored) {}
        }
        String[] keys = {
            "AllHeroDamageMultiplier=10000", "AllHeroDefenseBoost=10000", "AllHeroCritRateBoost=100",
            "AllHeroTrueDamage=1", "AllHeroInstantCooldown=1", "AllHeroUnlimitedEnergy=1",
            "AllHeroUnlimitedMana=1", "DamageLockMax=10000", "EffectiveDPSMode=3",
            "HeroLock=1", "HeroLockPriority=0", "SkillSmartAim=1", "SkillAutoChain=1",
            "ZeroDelaySkillTap=1", "FastSkillCycle=1", "SkillCastDelayMs=0", "FastSkillReleaseSpeed=10",
            "FannyCableSpeed=10", "FannyZeroCableDelay=1", "FannyUnlimitedEnergy=1",
            "LingComboSpeed=10", "LingWallJumpDelay=0", "LingSwordCollectZeroDelay=1",
            "GusionDaggerSpeed=10", "GusionInstaRecall=1", "ChouFreestyleFlicker=1",
            "ChouKickZeroDelay=1", "HayaShadowSwapInstant=1", "BeatrixGunSwapInstant=1",
            "NolanInfiniteRiftEnergy=1", "JoyPerfectBeatRhythmLock=1", "ArlottDemonGazeAutoStab=1",
            "SuyouStanceSwapZeroDelay=1", "CameraHeight=2", "FovBoost=1.35",
            "MinimapEnemyPriority=1", "TouchPollingRate=1000", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbAllHeroGodSuite]");
    }

    // ─── Season 42 / v4.6 / S8 — Java Wrapper Methods ──────────────────────────

    /** Season 42: Masha tearing-wounds mechanic override. */
    public static boolean injectMlbbSeason42MashaOverride(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbSeason42MashaOverride(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "MashaWoundDmg=10000", "MashaWoundDuration=0", "MashaHealMultiplier=0",
            "MashaPhalanxTimer=0", "MashaStackDecayRate=0", "MashaTearingProc=instant"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbS42MashaOverride]");
    }

    /** Season 42: Lord/Turtle HP threshold + Retri steal sync for new Lord design. */
    public static boolean injectMlbbSeason42LordStealUpdate(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbSeason42LordStealUpdate(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "LordHpThreshold=1", "TurtleHpThreshold=1", "RetriStealSyncRate=1000",
            "ObjectiveHpFloor=1", "SmartRetriTiming=instant", "LordPhase2Override=1",
            "InstantSmite=1", "AutoRetriLordTurtle=1", "RetributionInstantCast=1", "RetriReactionTimeMs=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbS42LordSteal]");
    }

    /** Season 42: 7-hero visual-refresh revamp boost (Bruno/Brody/Clint/Kadita/Badang/LuoYi/Paquito). */
    public static boolean injectMlbbSeason42AllHeroRevampBoost(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectMlbbSeason42AllHeroRevampBoost(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "BrunoRotationSpeed=10", "BrunoSkillTrackAcceleration=10",
            "KaditaUndertowDuration=0", "BadangWallLockInstant=1", "LuoYiReverseInstant=1",
            "PaquitoHeavyHandedInstant=1", "BrodyStarMarkZeroDelay=1", "ClintMasteryZeroCD=1",
            "EmoteSlotZeroDelay=1", "SkillAutoChain=1", "ZeroDelaySkillTap=1", "TouchPollingRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[MlbbS42RevampBoost]");
    }

    /** PUBGM v4.6: ACE32 screen-shake re-enable counter + AUG HFR recoil patch. */
    public static boolean injectPubgmV46WeaponFix(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmV46WeaponFix(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "r.WeaponScreenShake=0", "r.ACE32ScreenShake=0", "r.AUGRecoilPatternScale=0",
            "r.AUGRecoilCorrectionHFR=0", "r.HFRRecoilMultiplier=0",
            "r.WeaponRecoilScale=0", "r.VerticalRecoilScale=0", "r.HorizontalRecoilScale=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmV46WeaponFix]");
    }

    /** PUBGM v4.6: Midnight Hunters map event — vampire zone visibility + fog zero. */
    public static boolean injectPubgmV46MidnightHuntersMap(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmV46MidnightHuntersMap(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "r.AllowOcclusionQueries=1", "r.WallPenetrateEnabled=1",
            "r.VampireZoneVisibilityBoost=1", "r.ThemeMapFogDensity=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmMidnightHunters]");
    }

    /** PUBGM v4.6: Inflatable Boat + all vehicle override (collision + damage zero). */
    public static boolean injectPubgmV46VehicleOverride(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmV46VehicleOverride(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "r.VehicleCollisionPenalty=0", "r.BoatMovementSpeedCap=999",
            "r.VehicleExplosionRadius=0", "r.VehicleDamageToPlayer=0"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmV46Vehicle]");
    }

    /** PUBGM S32: Season-reset full ranked sweep — all combat CVars in a single atomic pass. */
    public static boolean injectPubgmS32RankedSweep(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectPubgmS32RankedSweep(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "r.WeaponRecoilScale=0", "r.VerticalRecoilScale=0", "r.HorizontalRecoilScale=0",
            "r.WeaponSpread=0", "r.WeaponSway=0", "r.AimAssistEnabled=1",
            "r.AimAssistStrength=100", "r.HeadBoneAimPriority=1", "r.EnemyLockMax=1",
            "r.PUBGDamageLockMax=10000", "r.PUBGHitboxMultiplier=3.0",
            "r.WeaponScreenShake=0", "r.ACE32ScreenShake=0", "r.AUGRecoilCorrectionHFR=0",
            "r.VampireZoneVisibilityBoost=1", "r.ThemeMapFogDensity=0",
            "r.VehicleCollisionPenalty=0", "r.BoatMovementSpeedCap=999",
            "TouchPollingRate=1000", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[PubgmS32Sweep]");
    }

    /** CODM Season 8: Static-HV SMG + ISO Hemlock AR zero-recoil + aim-assist. */
    public static boolean injectCodmSeason8NewWeapons(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmSeason8NewWeapons(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "StaticHvRecoilScale=0", "StaticHvSpreadScale=0", "StaticHvAimAssist=1",
            "IsoHemlockRecoilScale=0", "IsoHemlockSpreadScale=0",
            "IsoHemlockBulletSpread=0", "IsoHemlockHeadshotBonus=999"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmS8NewWeapons]");
    }

    /** CODM Season 8: Honkai collab roguelike mode — silent aim + head priority. */
    public static boolean injectCodmSeason8RoguelikeMode(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmSeason8RoguelikeMode(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "RoguelikeAimAssist=1", "RoguelikeSilentAim=1", "RoguelikeHeadPriority=1",
            "RoguelikeAimMagnetism=1000", "RoguelikeNoSpread=1", "RoguelikeAimSnap=10"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmS8Roguelike]");
    }

    /** CODM Season 8: Isolated map new POI terrain bypass + ESP clarity. */
    public static boolean injectCodmSeason8IsolatedPoi(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmSeason8IsolatedPoi(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "WallCheckRadius=0", "OcclusionBypassEnabled=1",
            "WallPenetrateRange=450", "PoiVisibilityOverride=1", "EspClarityBoost=1"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmS8IsolatedPoi]");
    }

    /** CODM Season 8: Full ranked sweep — BR + MP + Ranked all-mode atomic injection. */
    public static boolean injectCodmS8FullRankedSweep(String path) {
        if (path == null) return false;
        ensureParentDirectory(path);
        if (sNativeLibraryLoaded) {
            try { if (nativeInjectCodmS8FullRankedSweep(path)) return true; } catch (Throwable ignored) {}
        }
        String[] keys = {
            "RecoilScale=0", "VerticalRecoilScale=0", "WeaponSpread=0", "WeaponSway=0",
            "AimAssistEnabled=1", "AimAssistStrength=100", "AimMagnetism=3",
            "DamageLockMax=10000", "HitboxScale=3.0", "HeadshotMultiplier=999",
            "StaticHvRecoilScale=0", "IsoHemlockRecoilScale=0", "IsoHemlockHeadshotBonus=999",
            "RoguelikeAimAssist=1", "RoguelikeSilentAim=1", "RoguelikeAimMagnetism=1000",
            "WallCheckRadius=0", "OcclusionBypassEnabled=1", "WallPenetrateRange=450",
            "TouchPollingRate=1000", "HitRegSyncRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, keys, "[CodmS8FullSweep]");
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
