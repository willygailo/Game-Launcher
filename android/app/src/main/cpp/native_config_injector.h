#ifndef GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H
#define GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif

// =============================================================================
// ─── Core System, Engine & Kernel Layer ───
// =============================================================================

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeSetProcessCpuAffinity
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetProcessCpuAffinity
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeSetThreadSchedulingPolicy
 * Signature: (III)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetThreadSchedulingPolicy
  (JNIEnv *, jclass, jint, jint, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeSetIoPriority
 * Signature: (III)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetIoPriority
  (JNIEnv *, jclass, jint, jint, jint);

/*
 * nativeSetProcessIOPriority
 * Direct Linux SYS_ioprio_set and setpriority syscall accelerator
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetProcessIOPriority
  (JNIEnv *, jclass, jint, jint, jint, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeOptimizeMemoryMapping
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeOptimizeMemoryMapping
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeFastMemorySync
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeFastMemorySync
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeForceVulkanPipelineCache
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeForceVulkanPipelineCache
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectVulkanOptimization
 * Signature: (Ljava/lang/String;)Z
 *
 * 2026: Optimizes Vulkan pipeline cache, async shader compilation,
 * and graphics settings. Ban-safe: config-file writes only.
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectVulkanOptimization
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativePreserveFileTimestamps
 * Signature: (Ljava/lang/String;JJ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePreserveFileTimestamps
  (JNIEnv *, jclass, jstring, jlong, jlong);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeStealthWrite
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeStealthWrite
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeCalculateConfigCrc32
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeCalculateConfigCrc32
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectConfig
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectConfig
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativePatchKey
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchKey
  (JNIEnv *, jclass, jstring, jstring, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeBatchPatchKeys
 * Signature: (Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeBatchPatchKeys
  (JNIEnv *, jclass, jstring, jobjectArray, jobjectArray);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativePatchContentInMemory
 * Signature: (Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchContentInMemory
  (JNIEnv *, jclass, jstring, jobjectArray, jobjectArray, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativePatchXmlKey
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchXmlKey
  (JNIEnv *, jclass, jstring, jstring, jstring, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativePatchJsonKey
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchJsonKey
  (JNIEnv *, jclass, jstring, jstring, jstring, jboolean);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectUnrealEngineIni
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUnrealEngineIni
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectUnityBootConfig
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUnityBootConfig
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectNextGenEngineOptimizations
 * Signature: (Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenEngineOptimizations
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectNextGenTouchSampling
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectUltraExtremeGraphics
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectPerGameProfile
 * Signature: (Ljava/lang/String;Ljava/lang/String;IZZZZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPerGameProfile
  (JNIEnv *, jclass, jstring, jstring, jint, jboolean, jboolean, jboolean, jboolean);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectScopeAimCalibration
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectScopeAimCalibration
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectHitRegDpsBoost
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHitRegDpsBoost
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectDamageLockMax
 * Signature: (Ljava/lang/String;)Z
 *
 * 2026: Locks effective DPS at maximum by zeroing frame-thread lag,
 * enforcing 1000Hz hit-reg sync, and injecting Document/BattleConfig
 * DPS-floor keys. Ban-safe: config-file writes only, no binary patching.
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageLockMax
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectAimAssistLockMax
 * Signature: (Ljava/lang/String;)Z
 *
 * 2026: Locks aim-assist tracking at maximum magnetism tier by injecting
 * hero-lock, zero-deadzone, 1000Hz gyro/touch, and all-scope precision keys.
 * Ban-safe: config-file writes only, no binary patching.
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssistLockMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectHardwareMaskProfile
 * Fast-path native generator for DeviceProfile.ini (UE4/5) and HardwareProfile.json (Unity)
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHardwareMaskProfile
  (JNIEnv *, jclass, jstring, jstring, jstring, jint, jint);

// =============================================================================
// ─── Mobile Legends: Bang Bang (MLBB) Dedicated Injectors ───
// =============================================================================

/*
 * nativeInjectLingHeroDamageCombo
 * MLBB — Ling hero: damage-scripted auto sword combo.
 * Injects hit-reg + skill timing + damage output maximizers
 * specifically tuned for Ling's Luminous Slash / Tempest of Blades skill chain.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLingHeroDamageCombo
  (JNIEnv *, jclass, jstring);

/*
 * MLBB Hero-Specific Combo & Damage Scripts
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFannyFastCableCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectGusionDaggerCombo
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGusionDaggerCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectChouKickCombo
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectChouKickCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectHayabusaShadowCombo
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHayabusaShadowCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectBeatrixAllGunDamage
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBeatrixAllGunDamage
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectKaguraCombo
 * MLBB — Kagura instant umbrella rapid combo:
 * KaguraUmbrellaThrow=1, KaguraZeroReturnDelay=1, KaguraSkillChain=1,
 * KaguraYinYangMax=1, DamageLockMax=1, HitRegSyncRate=1000.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectKaguraCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectZilongAutoSlash
 * MLBB — Zilong fastest auto slash + spear flip:
 * ZilongAutoSlash=1, ZilongSpearFlipInstant=1, ZilongDragonFlurry=1,
 * AttackSpeedBoost=MAX, BasicAttackRate=MAX, DamageLockMax=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZilongAutoSlash
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectSaberCombo
 * MLBB — Saber triple strike instant combo:
 * SaberTripleStrikeInstant=1, SaberChaseZeroDelay=1, SaberUltLock=1,
 * DamageLockMax=1, CritRateBoost=1, PenetrationBoost=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSaberCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectAlucardLifestealCombo
 * MLBB — Alucard lifesteal burst + full sustain:
 * AlucardLifesteal=MAX, AlucardOmniVamp=1, AlucardResetChain=1,
 * AlucardPhantomStepInstant=1, DamageLockMax=1, LifestealBoost=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAlucardLifestealCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectYiSunShinCombo
 * MLBB — Yi Sun-shin weapon switch rapid combo + global ult lock:
 * YiSunShinAutoSwitch=1, YiSunShinMeleeCrit=1, YiSunShinShipBuffInstant=1,
 * AttackSpeedBoost=MAX, PhysicalDamageBase=2500, DamageLockMax=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectYiSunShinCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectChouFreestyleCombo
 * MLBB — Chou freestyle combo + Shunpo immune:
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectChouFreestyleCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectLancelotDashCombo
 * MLBB — Lancelot infinite triangular dash reset:
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLancelotDashCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFrancoHookCombo
 * MLBB — Franco 100% magnet hook + instant suppress:
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFrancoHookCombo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFannyAutoFullEnergy
 * MLBB — Fanny auto full energy + free cable:
 * FannyEnergyRegen=MAX, CableEnergyFree=1, AutoEnergyRefill=1,
 * EnergyRegenRate=10, CableCooldown=0, FannyMultiCableCombo=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFannyAutoFullEnergy
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectLingFastestComboAutoSword
 * MLBB — Ling fastest combo + auto sword chain:
 * LingSwordAutoChain=1, BlinkChainMax=1, WallJumpInstant=1,
 * TempestInstantCast=1, ZeroInputDelay=1, DamageLockMax=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLingFastestComboAutoSword
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectGusionUltraOverdrive
 * MLBB — Gusion ultra combo + 2500 damage + 3000 armor:
 * All Gusion dagger keys + GusionArmorMax=3000, GusionUltraBurst=1,
 * GusionInstantRecall=1, UltraDamageOverdrive=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGusionUltraOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbLingFastestSword
 * MLBB — Ling Tempest of Blades 4-sword rapid retrieval pathing & instant dash reset
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbLingFastestSword
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbFannyFastestCable
 * MLBB — Fanny instant dual/multi-cable trajectory prediction, wall-snap responsiveness, and zero input lag
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFannyFastestCable
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbHeroUnlimitedEnergy
 * MLBB — Ling, Fanny, Hayabusa, Gusion 4-hero unlimited energy, zero skill cost & infinite mobility
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbHeroUnlimitedEnergy
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbAllHeroBoostAndArmor
 * MLBB — All-hero damage boost (2.0x, true dmg, 3.0x crit), faster cooldown reduction (40% CDR) & armor boost (1.5x, 10000 def)
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllHeroBoostAndArmor
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbUltraDamageAllHero
 * MLBB — 2500+ damage all hero:
 * PhysicalDamageBase=2500, MagicDamageBase=2500, TrueDmgMultiplier,
 * CritMultiplier=3, PenetrationBoost, FrameSyncDamage, HitReg=1000.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbUltraDamageAllHero
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbArmorAllHero
 * MLBB — 3000+ armor all hero:
 * PhysicalDefense=3000, MagicDefense=3000, DamageReduction=0.99,
 * ShieldBoost, PhysicalShield=5000, MagicShield=5000.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbArmorAllHero
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectAllHeroItemSkillBoost
 * MLBB — All hero item + skill max boost:
 * ItemStatBoost=MAX, SkillDamageBoost=MAX, CooldownReduction=0,
 * LifestealBoost=1, SpellVampBoost=1, MovementSpeedBoost=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllHeroItemSkillBoost
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFastAttackSpeedAllHero
 * MLBB — Attack speed MAX all hero:
 * AttackSpeedBoost=MAX, BasicAttackRate=MAX, AutoAttackInterval=0,
 * AttackAnimSpeed=10, HitRegSyncRate=1000, AttackSpeedCap=10.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastAttackSpeedAllHero
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbAllHeroOverdrive
 * MLBB — All-hero overdrive: 10000 base damage, 3.0 crit, zero skill cost, zero CDR, hero lock, 1000Hz touch & hit-reg
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllHeroOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbFannyNoEnergyLimit
 * MLBB — Fanny no-energy-limit: energy limit 999, no energy decay, free cables, zero cooldown, instant cable aim
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFannyNoEnergyLimit
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbLingNoEnergyLimit
 * MLBB — Ling no-energy-limit: energy limit 999, no decay, free wall energy, instant swords, instant tempest cast
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbLingNoEnergyLimit
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFastFarming
 * MLBB — Fast Farming (gold + exp maximizer for all heroes):
 * Injects GoldRateBoost, ExpRateBoost, CreepGoldMultiplier, JungleExpMultiplier,
 * MinionGoldMultiplier, FastLevelUp, GoldFarmRate, ClearSpeedBoost, CooldownReduction,
 * SkillAutoChain, HitRegSyncRate=1000 and TouchPollingRate=1000.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFarming
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectJungleHero
 * MLBB — Jungle Hero optimizer:
 * Injects SmiteBoost, JungleClearSpeed, BuffDuration, ObjectivePriority,
 * MonsterDamageBoost, JungleExpBoost, SmiteRange, JunglePath, BuffSteal=1,
 * and ClearSpeedBoost — tuned for all assassin/fighter jungle roles.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectJungleHero
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectAllHeroUnlock
 * MLBB — All Hero config unlock:
 * Injects HeroUnlock=1, SkinUnlock=1, AllHeroEnabled=1, TrialHeroEnabled=1,
 * FreeHeroEnabled=1, HeroPoolExpand=1, HeroSelectUnlock=1 across all config paths.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllHeroUnlock
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectSaDamagePlus
 * MLBB SA server — Damage+ modifier:
 * Boosts effective DPS via DamageLockMax, EffectiveDPSMode=3, CritRateBoost,
 * PenetrationBoost, FrameSyncDamage, HitRegSyncRate=1000, AimMagnetism=3,
 * SkillSmartAim, HeroLock, TouchPollingRate=1000, zero input lag.
 * Works across SA server PlayerPrefs XML / boot.config / INI formats.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSaDamagePlus
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbJungleFastFarmAllHero
 * MLBB — Jungle monster target priority, Retribution instant cast timing, and creep HP thresholding
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbJungleFastFarmAllHero
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbAllJungleFastFarmOverdrive
 * MLBB — All jungle fast farm overdrive: 3x smite & clear speed, 3x buff duration & gold/exp rates, instant retribution
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllJungleFastFarmOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbSmartSkillMagnetAim
 * MLBB — Dual-priority smart skill aim: lowest HP enemy hero (maliit na buhay) & closest enemy hero (malapit na hero)
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbSmartSkillMagnetAim
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbPenetrationCritBurst
 * MLBB — 100% Malefic Roar/Divine Glaive penetration, 3.0x crit multiplier, hero execution mechanics, instant battle spell
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbPenetrationCritBurst
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbDamage10000AttackSpeedMax
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbFastLoadSplashBypass
 * MLBB — Fast-path asset unbundling, skip intro/splash videos, low-poly initial boot, UI async load, deferred audio load
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFastLoadSplashBypass
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbb165FpsGraphics
 * MLBB — 165 FPS frame rate & Ultra HDR graphics native injection:
 * Patches PlayerPrefs XML (<map>): HFR=1, HighFPSMode=3, FrameRateLevel=6,
 * FPS=165, MaxFPS=165, MaxFrameRate=165, TargetFPS=165, FrameRateLimit=165,
 * QualityLevel=3, GraphicsQuality=3, HDMode=1, Shadow=1, Outline=1,
 * Unlock165Hz=1, Unlock165FPS=1, TouchBoostHz=165, TouchPollingRate=1000.
 * Signature: (Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbb165FpsGraphics
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * nativeInjectMlbbAllHeroMaxDamage2026
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllHeroMaxDamage2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbUltimateDamageOverdrive2026
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbUltimateDamageOverdrive2026
  (JNIEnv *, jclass, jstring);

// =============================================================================
// ─── PUBG Mobile (PUBGM) Dedicated Injectors ───
// =============================================================================

/*
 * nativeInjectMagicBulletAimbot
 * PUBGM — Magic bullet + zero-spread aimbot.
 * Injects predictive aim, gyro 1000Hz, no-spread, zero-sway, and
 * bullet-velocity compensation CVars for all weapon classes.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMagicBulletAimbot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectNoScopeAimbot
 * PUBGM — No-scope / hipfire aimbot:
 * Zero spread, zero sway, head-bone magnetism, instant aim snap,
 * 1000Hz gyro + touch, no bullet-spread RNG, hipfire aim-lock on head.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNoScopeAimbot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectAllScopeAimbot
 * PUBGM — All-scope aimbot (2x→8x):
 * Per-scope gyro 1000Hz, head-lock, zero sway, ADS 0-delay,
 * predictive tracking, zero breathing sway, snap speed max.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllScopeAimbot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectLongRangeScopeHeadshot
 * PUBGM — 6x/8x long-range auto headshot:
 * Bullet drop compensation, hold-breath cancel, head-bone priority aim,
 * zero micro-jitter, thermal tracking, 1000Hz gyro stabilization.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLongRangeScopeHeadshot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMidRangeAutoHeadshot
 * PUBGM — 2x/3x/4x mid-range auto headshot:
 * Target prediction, frame-sync headshot lock, zero vertical kick,
 * aim magnet to head hitbox, touch 1000Hz.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMidRangeAutoHeadshot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmFastAttackSpeed
 * PUBGM — Fast attack / fire rate boost:
 * Full-auto frame-sync, melee punch speed max, ADS fire interval min,
 * weapon fire rate override, 1000Hz hit-reg sync.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFastAttackSpeed
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmBallisticsVelocityPenetration
 * PUBGM — 2.0x Muzzle velocity, instant hit-reg/desync fix, Lv3 vest/helmet shredder, shotgun tight slug, sniper 300 damage lock
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmBallisticsVelocityPenetration
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgm165FpsGraphics
 * PUBGM — 165 FPS frame rate & HDR graphics native injection:
 * Formats: UserCustom.ini, EnjoyCJZC.ini, GameUserSettings.ini, Active.sav, XML prefs.
 * Signature: (Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgm165FpsGraphics
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * PUBGM 4.6.0 Dedicated Graphics Presets
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmUltraHdr120
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmHdr120
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmSuperSmooth165
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmDamage10000AttackSpeedMax
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmAllScopeTieredHeadshot
 * PUBGM — Tiered all-scope auto 3-bullet headshot (100m, 200m, 300m, 400m), zero recoil, bullet tracking & gyro stabilization
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmAllScopeTieredHeadshot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmAllWeaponMaxDamage2026
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmAllWeaponMaxDamage2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmUltraAimbot2026
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmUltraAimbot2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmFastLoadAsyncStreaming
 * PUBGM — UE4 dedicated async loader thread, 8-core concurrent shader compilation, 1024MB texture stream pool, skip movies/splash
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFastLoadAsyncStreaming
  (JNIEnv *, jclass, jstring);

// =============================================================================
// ─── Call of Duty Mobile (CODM) Dedicated Injectors ───
// =============================================================================

/*
 * nativeInjectNoRecoilNoSpread
 * CODM — No recoil + no spread + aimbot precision.
 * Injects zero-recoil, zero-spread, aim magnetism, scope stabilization,
 * and 1000Hz gyro tracking across all CODM config formats (INI/JSON/XML).
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNoRecoilNoSpread
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmNoScopeAimbot
 * CODM — No-scope / hipfire aimbot:
 * Zero spread all weapon classes, head magnetism, instant aim snap,
 * 1000Hz gyro + touch, no bullet-spread RNG, hipfire aim-lock on head.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmNoScopeAimbot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmAllScopeAimbot
 * CODM — All-scope aimbot (all optic classes):
 * ADS 0-delay, headshot priority per optic, gyro 1000Hz,
 * zero sway, predictive tracking, aim snap speed max.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAllScopeAimbot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmLongRangeHeadshot
 * CODM — Sniper/marksman long-range headshot lock:
 * Bullet velocity compensation, hold-breath zero delay, head-bone lock,
 * zero micro-jitter, 1000Hz gyro stabilization.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmLongRangeHeadshot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmMidRangeHeadshot
 * CODM — AR/SMG mid-range auto headshot:
 * Target prediction + frame-sync headshot, zero vertical kick,
 * head hitbox aim magnet, 1000Hz touch+gyro.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmMidRangeHeadshot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmFastAttackSpeed
 * CODM — Fast fire rate + operator attack speed:
 * Melee speed max, operator skill attack rate max, fire interval min,
 * weapon fire rate override, 1000Hz hit-reg sync.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFastAttackSpeed
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmBsaRemovalRangeOverdrive
 * CODM — BSA elimination, infinite damage range falloff bypass, instant ADS, zero hit-flinch, sniper blank-scope hitscan
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmBsaRemovalRangeOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmAllScopeTieredHeadshot
 * CODM — Tiered all-scope auto 3-bullet headshot (100m, 200m, 300m, 400m), BSA removal, zero flinch, bullet tracking & gyro stabilization
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAllScopeTieredHeadshot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmDamage10000AttackSpeedMax
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodm165FpsGraphics
 * CODM — 165 FPS frame rate & Ultra HDR graphics native injection:
 * Patches MaxFrameRate, TargetFPS, FrameRateLimit, MobileFPSLimit, FrameRateLevel=9,
 * GraphicQuality=4, TextureQuality=4, HDRMode, Unlock165Hz, Unlock165FPS, Ultra165FPS.
 * Signature: (Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodm165FpsGraphics
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * nativeInjectCodmMaxDamageAllWeapon2026
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmMaxDamageAllWeapon2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmUltraConfigCheat2026
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmUltraConfigCheat2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmFastLoadShaderBypass
 * CODM — Fast load flag, skip Activision/TiMi splash, async asset loading, deferred weapon mesh prewarm, fast shader cache bypass
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFastLoadShaderBypass
  (JNIEnv *, jclass, jstring);

// =============================================================================
// ─── Other Supported Titles (Free Fire, Blood Strike, Delta Force, HOK, Wild Rift) ───
// =============================================================================

/*
 * nativeInjectFreeFireAutoHeadshot
 * Free Fire — Drag headshot magnetism + zero bloom:
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireAutoHeadshot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFreeFireFastGlooWall
 * Free Fire — 360 Instant gloo wall + fast reload:
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireFastGlooWall
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFreeFireDamage10000AttackSpeedMax
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectBloodStrikeZeroRecoil
 * Blood Strike — Zero recoil + fast slide cancel:
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBloodStrikeZeroRecoil
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectDeltaForcePrecisionAim
 * Delta Force — Sniper bullet drop + thermal precision:
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDeltaForcePrecisionAim
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectHokAutoSmiteObjective
 * HOK — 100% smite steal priority + predictive aim:
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHokAutoSmiteObjective
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectHokDamage10000AttackSpeedMax
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHokDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectWildRiftDamage10000AttackSpeedMax
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectWildRiftDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

// =============================================================================
// ─── Universal Combat & Cross-Game Overdrive ───
// =============================================================================

/*
 * nativeInjectDamageBoost
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jfloat, jint
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageBoost
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jint);

/*
 * nativeInjectZeroRecoil
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jint
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroRecoil
  (JNIEnv *, jclass, jstring, jfloat, jint);

/*
 * nativeInjectAimAssist
 * Method signature: JNIEnv *, jclass, jstring, jint, jint
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * nativeInjectTrackingBullet
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * nativeInjectArmorDef
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * nativeInjectSpeedBoost
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSpeedBoost
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * nativeInjectHeroDamage1000
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jfloat, jint, jint
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroDamage1000
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jint, jint);

/*
 * nativeInjectScopeZeroRecoil
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jint
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectScopeZeroRecoil
  (JNIEnv *, jclass, jstring, jfloat, jint);

/*
 * nativeInjectAimAssist1000
 * Method signature: JNIEnv *, jclass, jstring, jint, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist1000
  (JNIEnv *, jclass, jstring, jint, jfloat);

/*
 * nativeInjectTrackingBullet1000
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet1000
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * nativeInjectArmorDef1000
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef1000
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * nativeInjectShield1500
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectShield1500
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * nativeInjectDroneView
 * Method signature: JNIEnv *, jclass, jstring, jint, jint
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDroneView
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * nativeInjectAimHeadLock
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jint
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimHeadLock
  (JNIEnv *, jclass, jstring, jfloat, jint);

/*
 * nativeInjectUltraDamageOverdrive
 * Method signature: JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraDamageOverdrive
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat);

/*
 * nativeInjectHeroAimLock
 * Method signature: JNIEnv *, jclass, jstring, jint, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroAimLock
  (JNIEnv *, jclass, jstring, jint, jfloat);

/*
 * nativeInjectCriticalBurstOverdrive
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCriticalBurstOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * All-Gun & All-Scope Universal Calibrations
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllGunWeaponCalibration
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectAllScopeMasteryCalibration
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllScopeMasteryCalibration
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFastReloadQuickSwap
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastReloadQuickSwap
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectWallPiercingArmorShredder
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectWallPiercingArmorShredder
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectZeroPingNetworkOverclock
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroPingNetworkOverclock
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUltraExtreme240FpsGraphics
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtreme240FpsGraphics
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalDamage10000AttackSpeedMax
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFastCooldown
 * Method signature: JNIEnv *, jclass, jstring, jfloat
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastCooldown
  (JNIEnv *, jclass, jstring, jfloat);

/*
 * nativeInjectFastFullMana
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullMana
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFastFullEnergy
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastFullEnergy
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFastHpRegen
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastHpRegen
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFastStaminaFuryRegen
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastStaminaFuryRegen
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectZeroSkillCost
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroSkillCost
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMaxUltCharge
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMaxUltCharge
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectSkillEconomyMasterSuite
 * Method signature: JNIEnv *, jclass, jstring
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSkillEconomyMasterSuite
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFastLootAndWeaponSwap
 * Zero-delay weapon pickup, auto-loot prioritization, and instant weapon draw
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastLootAndWeaponSwap
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectInstantSprintTurbo
 * 0ms analog stick deadzone and instant forward sprint transition
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectInstantSprintTurbo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMultiRangeHeadshotCalibration
 * Multi-range micro-aim precision (short CQB, mid 2x-4x recoil stabilization, long sniper micro-aim)
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMultiRangeHeadshotCalibration
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalZeroDelaySkillTapAllHero
 * Universal touch-to-skill zero-latency queue bypass, 1000Hz sampling, and instant combo chaining
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalZeroDelaySkillTapAllHero
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFastLootAndSprint
 * Universal — Auto-loot, fast gun pickup, pickup priority, auto-sprint, instant slide
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastLootAndSprint
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalCombatMechanicsOverdrive
 * Universal — 1000Hz touch-to-damage, attack animation canceling, frame-synced damage, true damage conversion
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalCombatMechanicsOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalFastLoadTurbo
 * Universal — Multi-threaded asset streaming and high-speed texture streaming cache
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalFastLoadTurbo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectNoScopeTieredHeadshotAllGun
 * All Guns — Tiered No-Scope auto headshot (20m, 40m, 50m, 100m) hipfire lock & zero spread
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNoScopeTieredHeadshotAllGun
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectRifleScopeTieredHeadshot
 * All Rifle Guns — Tiered Scope-On auto headshot (100m, 200m, 300m, 400m), lead comp & bullet drop
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectRifleScopeTieredHeadshot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectSilentAimbot
 * Zero-smoothing instant snap aimbot with head magnetism
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSilentAimbot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectHitboxMultiplier
 * Multiplies target collision hitbox for predictive bullet hit detection
 * Signature: (Ljava/lang/String;F)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHitboxMultiplier
  (JNIEnv *, jclass, jstring, jfloat);

/*
 * nativeInjectUltraWallhackEspClarity
 * Eliminates volumetric fog, boosts silhouette contrast, and forces maximum draw distance
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraWallhackEspClarity
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectAutoSmiteRetribution
 * Predictive objective HP threshold calculator for zero-latency steal
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAutoSmiteRetribution
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalCombatSuite
 * Single-pass injection combining damage lock, zero recoil, silent aim, hitbox, and 1000Hz hit-reg
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalCombatSuite
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbMasterComboSuite
 * Single-pass master hero combo suite for Mobile Legends
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbMasterComboSuite
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectBloodStrikeSlideCancelOverdrive
 * Blood Strike fast tactical sprint and instant slide cancel
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBloodStrikeSlideCancelOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectDeltaForceNaniteShaderPrewarm
 * Delta Force UE5 Nanite shader pre-caching and zero sway sniper
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDeltaForceNaniteShaderPrewarm
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectArenaBreakoutThermalFootstepAudio
 * Arena Breakout thermal clarity enhancement and audio visualizer footprint lock
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArenaBreakoutThermalFootstepAudio
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectValorantCounterStrafeAimLock
 * Valorant Mobile zero-deadzone counter strafe and crosshair head-level lock
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectValorantCounterStrafeAimLock
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFarlightJetpackZeroCooldown
 * Farlight 84 air-dash distance max and jetpack zero delay
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFarlightJetpackZeroCooldown
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectStandoff2Tick128ZeroSpread
 * Standoff 2 128-tick network simulation and zero bullet bloom
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectStandoff2Tick128ZeroSpread
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalGodDamageOverdrive2026
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalGodDamageOverdrive2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectBloodStrikeDamage10000AttackSpeedMax
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBloodStrikeDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectDeltaForceDamage10000AttackSpeedMax
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDeltaForceDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectArenaBreakoutDamage10000AttackSpeedMax
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArenaBreakoutDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectValorantDamage10000AttackSpeedMax
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectValorantDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFarlightDamage10000AttackSpeedMax
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFarlightDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectStandoff2Damage10000AttackSpeedMax
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectStandoff2Damage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectGenshinDamage10000ElementalBurstMax
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGenshinDamage10000ElementalBurstMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectRobloxDamage10000Max
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectRobloxDamage10000Max
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCarXTorqueHorsepower10000Max
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCarXTorqueHorsepower10000Max
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmRankedDamageSync
 * PUBGM — Ranked & Classic 1000Hz Hit Registration & Desync Fix
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmRankedDamageSync
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmRankedAimAssist
 * PUBGM — Ranked & Classic Sticky Crosshair Friction & Magnetism
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmRankedAimAssist
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmRankedDamageSync
 * CODM — Ranked 1000Hz Hit Registration & Bullet Velocity Sync
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmRankedDamageSync
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmRankedAimAssist
 * CODM — Ranked Sticky Aim Assist & Ads Friction
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmRankedAimAssist
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbRankedHitSync
 * MLBB — Ranked & Classic Direct Skill Hit Registration & Frame Sync
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbRankedHitSync
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbRankedAimAssist
 * MLBB — Ranked Hero Lock & Smart Aim Magnetism
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbRankedAimAssist
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbFastFarmingAllHero
 * MLBB — Fast Farming & Jungle Creep Clear Overdrive
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFastFarmingAllHero
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbFastRetributionObjectiveSteal
 * MLBB — Fast Retribution Instant Smite & Objective Steal (Lord/Turtle)
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFastRetributionObjectiveSteal
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbAllHeroGodSuite2026
 * MLBB — Universal All Hero Overdrive, Unlimited Energy/Mana & Skill Chain
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllHeroGodSuite2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectLuaProperties
 * Ingests arbitrary key-value sets from parsed Lua profiles directly into target files at native speed.
 * Signature: (Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectLuaProperties
  (JNIEnv *, jclass, jstring, jobjectArray, jobjectArray);

/*
 * nativeInjectArenaBreakoutCombatOverdrive
 * Arena Breakout — 120 FPS, Audio Spatialization, Armor Piercing & Zero Sway
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArenaBreakoutCombatOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectDeltaForceCombatOverdrive
 * Delta Force Mobile — 120 FPS, Tactical Sprint & Muzzle Stabilization
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDeltaForceCombatOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectBloodStrikeCombatOverdrive
 * Blood Strike — 144 FPS, Tactical Slide Acceleration & Zero Recoil
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBloodStrikeCombatOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectGenshinFpsUnlockShaderTurbo
 * Genshin Impact — 120 FPS, Vulkan Async Compute & Pipeline Pre-warming
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGenshinFpsUnlockShaderTurbo
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCarXZeroThrottleLatency
 * CarX Street — Zero Throttle Latency, 120 FPS & Texture Streaming
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCarXZeroThrottleLatency
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectFarlightJetpackAccuracy
 * Farlight 84 — Jetpack Strafe Stability, 144 FPS & Tick-rate Sync
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFarlightJetpackAccuracy
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectRoblox120FpsUnlock
 * Roblox — 120 FPS Uncap, Physics Rate Sync & Memory Budget Boost
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectRoblox120FpsUnlock
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectStandoff2True128Tick
 * Standoff 2 — 128-tick rate network packet alignment & Zero First Bullet Spread
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectStandoff2True128Tick
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectValorantLowLatencyHeadshot
 * Valorant Mobile — Crosshair Head-height Lock & Zero Input Delay
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectValorantLowLatencyHeadshot
  (JNIEnv *, jclass, jstring);

// =============================================================================
// ─── MLBB Season 42 "Starward Decade" — Sep 16 2026 ─────────────────────────
// =============================================================================

/*
 * nativeInjectMlbbSeason42MashaOverride
 * Season 42 Masha rework: tearing-wounds mechanic neutralized.
 * Keys: MashaWoundDmg=10000, MashaWoundDuration=0, MashaHealMultiplier=0,
 *       MashaPhalanxTimer=0, MashaStackDecayRate=0, MashaTearingProc=instant
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbSeason42MashaOverride
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbSeason42LordStealUpdate
 * Season 42 Lord/Turtle HP threshold + Retribution steal sync for new Lord design.
 * Keys: LordHpThreshold=1, TurtleHpThreshold=1, RetriStealSyncRate=1000,
 *       ObjectiveHpFloor=1, SmartRetriTiming=instant, LordPhase2Override=1
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbSeason42LordStealUpdate
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbSeason42AllHeroRevampBoost
 * Season 42 visual-refresh hero suite (Bruno/Brody/Clint/Kadita/Badang/LuoYi/Paquito).
 * Keys: BrunoRotationSpeed=10, BrunoSkillTrackAcceleration=10,
 *       KaditaUndertowDuration=0, BadangWallLockInstant=1, LuoYiReverseInstant=1,
 *       EmoteSlotZeroDelay=1
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbSeason42AllHeroRevampBoost
  (JNIEnv *, jclass, jstring);

// =============================================================================
// ─── PUBGM v4.6 "Midnight Hunters" — Sep 9 2026 ─────────────────────────────
// =============================================================================

/*
 * nativeInjectPubgmV46WeaponFix
 * v4.6 patch counter: universal screen-shake zero + AUG HFR recoil override.
 * Keys: r.WeaponScreenShake=0, r.ACE32ScreenShake=0, r.AUGRecoilPatternScale=0,
 *       r.AUGRecoilCorrectionHFR=0, r.HFRRecoilMultiplier=0
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmV46WeaponFix
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmV46MidnightHuntersMap
 * Midnight Hunters event: vampire-zone wall occlusion + terrain fog override.
 * Keys: r.AllowOcclusionQueries=1, r.WallPenetrateEnabled=1,
 *       r.VampireZoneVisibilityBoost=1, r.ThemeMapFogDensity=0
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmV46MidnightHuntersMap
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmV46VehicleOverride
 * Inflatable Boat + all vehicle collision/damage override for v4.6.
 * Keys: r.VehicleCollisionPenalty=0, r.BoatMovementSpeedCap=999,
 *       r.VehicleExplosionRadius=0, r.VehicleDamageToPlayer=0
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmV46VehicleOverride
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmS32RankedSweep
 * S32 season-start full sweep: re-injects ALL combat CVars atomically.
 * Combines MagicBullet + EnemyLockAllScope + ZeroRecoil + Hitbox3x + DamageLockMax.
 * Designed for season-reset timing when anti-cheat baseline is freshly initialized.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmS32RankedSweep
  (JNIEnv *, jclass, jstring);

// =============================================================================
// ─── CODM Season 8 "Against All Fate" — Sep 9 2026 ──────────────────────────
// =============================================================================

/*
 * nativeInjectCodmSeason8NewWeapons
 * Season 8 new weapons: Static-HV SMG + ISO Hemlock AR zero-recoil + aim-assist injection.
 * Keys: StaticHvRecoilScale=0, StaticHvSpreadScale=0, StaticHvAimAssist=1,
 *       IsoHemlockRecoilScale=0, IsoHemlockSpreadScale=0,
 *       IsoHemlockBulletSpread=0, IsoHemlockHeadshotBonus=999
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmSeason8NewWeapons
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmSeason8RoguelikeMode
 * Honkai Impact 3rd collab roguelike mode: silent aim + headshot priority injection.
 * Keys: RoguelikeAimAssist=1, RoguelikeSilentAim=1, RoguelikeHeadPriority=1,
 *       RoguelikeAimMagnetism=1000, RoguelikeNoSpread=1, RoguelikeAimSnap=10
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmSeason8RoguelikeMode
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmSeason8IsolatedPoi
 * New Isolated map POI terrain bypass + ESP clarity boost for S8 geometry.
 * Keys: WallCheckRadius=0, OcclusionBypassEnabled=1, WallPenetrateRange=450,
 *       PoiVisibilityOverride=1, EspClarityBoost=1
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmSeason8IsolatedPoi
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmS8FullRankedSweep
 * Season 8 full ranked sweep: all-mode (BR + MP + Ranked) atomic injection.
 * Combines NoRecoil + NoSpread + AimbotPrecision + NewWeapons + Hitbox3x
 *         + EnemyLock + HeadshotKill + RoguelikeAssist in single atomic write pass.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmS8FullRankedSweep
  (JNIEnv *, jclass, jstring);

/*
 * ── 2026.4 Latest Combat Overdrive Suite (Damage Assist, Aim Lock, Armor Overdrive) ──
 */

/*
 * nativeInjectMlbbCombatOverdrive2026
 * Injects 10,000 True Damage, Dual-Priority Smart Aim, 10,000 Armor/Magic Shield,
 * 100% Penetration, 4.0x Crit, Omni-Lifesteal, and Zero-Delay Cooldown.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbCombatOverdrive2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmCombatOverdrive2026
 * Injects 3.0x Muzzle Velocity, Head Bone Lock, 10,000 Damage Lock, 95% Damage Reduction,
 * Lv3 Armor Shredder, and 1000Hz Hit-Reg Packet Sync.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmCombatOverdrive2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmCombatOverdrive2026
 * Injects Laser Beam Zero Spread, Kinetic Armor 10000, 1000Hz Gyro Stabilization,
 * ADS 0-Delay Instant Zoom, and Max Damage Floor.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmCombatOverdrive2026
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalArmorShieldLock
 * Universal Armor Fortification: 10000 Armor/Shield, 95% damage absorption, zero collision penalty.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalArmorShieldLock
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalAimMagnetLock
 * Universal Aim Assist: Max magnetism, zero deadzone, 1000Hz touch & gyro.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalAimMagnetLock
  (JNIEnv *, jclass, jstring);

// =============================================================================
// ─── 2026 NEW: Specialized Per-Game Overdrive Functions ──────────────────────
// =============================================================================

/*
 * nativeInjectMlbbBasicAttackRegenOverdrive
 * MLBB 2026 Specialist:
 *   - Basic Attack Damage overclock: 10000 base + 10x speed ratio
 *   - HP / Mana / Energy regen rate: 1000 per tick
 *   - All-hero true lifesteal 10x + spellvamp
 *   - Zero skill mana/energy cost
 *   - Armor / Physical-Defense / Magic-Defense: 10000+
 *   - Life-still (passive HP recovery) constant tick
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbBasicAttackRegenOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmFullScopeBulletTrackingOverdrive
 * PUBGM 2026 Specialist:
 *   - Scope tier locks: 100m iron/RDS, 200m 2x/3x, 300m 4x/ACOG,
 *                       400m 6x adjusted, 450m 8x sniper
 *   - All-gun all-scope working across every weapon class
 *   - Bullet tracking magnetism 3.0x + 3x hitbox expansion
 *   - Fast ADS/scope zero delay
 *   - Fast sprint turbo (infinite stamina)
 *   - Fast weapon swap + fast reload/chambering
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFullScopeBulletTrackingOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmFullScopeBulletTrackingFastReload
 * CODM 2026 Specialist:
 *   - Attack aim assist lock: max strength + zero deadzone
 *   - Bullet tracking lock: 3.0x magnetism, zero spread laser beam
 *   - All scope lock: 100m, 200m, 300m, 400m, 450m — all guns
 *   - Fast reload + fast chambering: 0ms delay
 *   - Fast sprint turbo
 *   - Fast scope / fast ADS
 *   - Slide cancel fast
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFullScopeBulletTrackingFastReload
  (JNIEnv *, jclass, jstring);

// =============================================================================
// ─── 2026 Advanced Security, Anti-Tamper & Anti-Ban Bypass Suite ─────────────
// =============================================================================

/*
 * nativeSecurityBypassStripXattrs
 * Removes foreign/audit extended attributes (xattrs) from target configuration files.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSecurityBypassStripXattrs
  (JNIEnv *, jclass, jstring);

/*
 * nativeSecurityBypassCloakTimestamps
 * Synchronizes atime and mtime of target file with source reference file (e.g. base.apk).
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSecurityBypassCloakTimestamps
  (JNIEnv *, jclass, jstring, jstring);

/*
 * nativeSecurityBypassAtomicSwap
 * Performs atomic in-place file replacement via renameat/rename to evade inotify file watchers.
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSecurityBypassAtomicSwap
  (JNIEnv *, jclass, jstring, jstring);

/*
 * nativeSecurityBypassEnforcePermissions
 * POSIX native chmod and chown enforcement.
 * Signature: (Ljava/lang/String;III)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSecurityBypassEnforcePermissions
  (JNIEnv *, jclass, jstring, jint, jint, jint);

/*
 * nativeInjectMlbbAutoMapGlitch3s
 * Injects 2026 3-second auto map glitch, periodic FOW desync pulses, and minimap ghost icon retention.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAutoMapGlitch3s
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbFastSovereignOverdrive
 * Injects 2026 MLBB Fast Sovereign Overdrive Suite:
 * Fast Farming, Fast Skills, Fast Combo, Fast Item, Fast Level, Fast Turtle, Fast Lord, Fast Coin, Fast Roam.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFastSovereignOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmSovereignOverdriveBypass
 * Injects 2026 PUBGM Sovereign Overdrive & Security Bypass Suite:
 * Magic Bullet 2.0, Wall Penetration, Zero Recoil Laser Lock, Zero Shake,
 * No Grass, 12x Sprint Turbo, Silent Movement, and Anti-Cheat Inotify/Timestamp Cloaking.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmSovereignOverdriveBypass
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmSovereignOverdriveBypass
 * Injects 2026 CODM Sovereign Overdrive & Security Bypass Suite:
 * Zero BSA, Laser Beam Spread Lock, 360° Hitbox, Silent Head Lock, 500m Wall Penetration,
 * Dead Silence, Ghost UAV Immunity, 12x Slide-Cancel Turbo, and TiMi/ACE Inotify Cloaking.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmSovereignOverdriveBypass
  (JNIEnv *, jclass, jstring);

/*
  * nativeFastHexPatchMmap
  * Memory-mapped zero-allocation binary/hex file patcher.
  * Signature: (Ljava/lang/String;[B[B)Z
  */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeFastHexPatchMmap
  (JNIEnv *, jclass, jstring, jbyteArray, jbyteArray);

/*
  * nativeScanAndPatchProcessMemory
  * Direct process memory scanner and patcher via process_vm_readv/writev and /proc/<pid>/mem.
  * Signature: (ILjava/lang/String;[B[B)I
  */
JNIEXPORT jint JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeScanAndPatchProcessMemory
  (JNIEnv *, jclass, jint, jstring, jbyteArray, jbyteArray);

/*
  * nativeDirectMemorySearch
  * Memory-mapped fast byte-offset search.
  * Signature: (Ljava/lang/String;[B)J
  */
JNIEXPORT jlong JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeDirectMemorySearch
  (JNIEnv *, jclass, jstring, jbyteArray);

/*
 * nativeInjectMlbbUltraDroneViewMaxFov
 * Injects 2026 MLBB Ultra Drone View Panoramic FOV Suite:
 * CameraHeight=4, PanoramicFOV=1.75, DroneFOV=180, MapScale=1.35, WideCameraAngle=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbUltraDroneViewMaxFov
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbGodModeFullOverdrive
 * Master atomic injector for MLBB: Damage 10000+, Max Attack Speed, God Armor,
 * Infinite Lifesteal, Unlimited Energy, 3s Map Glitch Radar & Ultra Drone View.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbGodModeFullOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgUltraDroneViewMaxFov
 * Injects 2026 PUBGM Ultra Drone View / iPad FOV Suite:
 * +CVars=r.PUBGCameraFOV=130, +CVars=r.PUBGCameraDistance=220, +CVars=r.PUBGIpadView=1.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgUltraDroneViewMaxFov
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmGodModeFullOverdrive
 * Master atomic injector for PUBGM: Magic Bullet 2.0, Zero Recoil, All-Scope Headshot,
 * Damage 10000+, 1000Hz Gyro Sync, Kinetic Shield 10000+, 12x Sprint Turbo & iPad FOV 130.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmGodModeFullOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmUltraDroneViewMaxFov
 * Injects 2026 CODM Ultra Drone View Panoramic FOV Suite:
 * CameraFOV=120, ThirdPersonFOV=120, FPP_FOV=120, TPP_FOV=120, Camera_Elevation=4.0.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmUltraDroneViewMaxFov
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmGodModeFullOverdrive
 * Master atomic injector for CODM: No Recoil / No Spread, Enemy Lock All-Scope (50-450m),
 * 3-Bullet Head / 5-Bullet Body Kill, Damage 10000+, Kinetic Armor 10000+, 12x Slide Turbo & FOV 120.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmGodModeFullOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbUniversalZeroDelayCombo
 * Injects 2026 MLBB Universal Zero-Delay Combo & All-Hero Animation Cancel Suite:
 * Instant skill chaining, 0ms backswing recovery, turn-rate override, 1000Hz touch queue,
 * and hero-specific fast combo chains (Ling, Fanny, Gusion, Chou, Lancelot, Hayabusa, Nolan, Suyou, etc.)
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbUniversalZeroDelayCombo
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif

#endif // GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H



