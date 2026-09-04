#ifndef GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H
#define GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H

#include <jni.h>

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

#ifdef __cplusplus
extern "C" {
#endif

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
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeOptimizeMemoryMapping
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeOptimizeMemoryMapping
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
 * Method:    nativeFastMemorySync
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeFastMemorySync
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
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectVulkanOptimization
 * Signature: (Ljava/lang/String;)Z
 *
 * 2026: Optimizes Vulkan pipeline cache, async shader compilation,
 * and graphics settings. Ban-safe: config-file writes only.
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectVulkanOptimization
  (JNIEnv *, jclass, jstring);

// ─── Backward-Compatibility / Safe Performance Aliases ─────────────────────────────────────

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageBoost
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jint);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroRecoil
  (JNIEnv *, jclass, jstring, jfloat, jint);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist
  (JNIEnv *, jclass, jstring, jint, jint);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSpeedBoost
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroDamage1000
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jint, jint);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectScopeZeroRecoil
  (JNIEnv *, jclass, jstring, jfloat, jint);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist1000
  (JNIEnv *, jclass, jstring, jint, jfloat);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet1000
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef1000
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastCooldown
  (JNIEnv *, jclass, jstring, jfloat);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectShield1500
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDroneView
  (JNIEnv *, jclass, jstring, jint, jint);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimHeadLock
  (JNIEnv *, jclass, jstring, jfloat, jint);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraDamageOverdrive
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroAimLock
  (JNIEnv *, jclass, jstring, jint, jfloat);

// ─── New 2026 Game-Specific Tweaks ──────────────────────────────────────────

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
 * nativeInjectMagicBulletAimbot
 * PUBGM — Magic bullet + zero-spread aimbot.
 * Injects predictive aim, gyro 1000Hz, no-spread, zero-sway, and
 * bullet-velocity compensation CVars for all weapon classes.
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMagicBulletAimbot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectNoRecoilNoSpread
 * CODM — No recoil + no spread + aimbot precision.
 * Injects zero-recoil, zero-spread, aim magnetism, scope stabilization,
 * and 1000Hz gyro tracking across all CODM config formats (INI/JSON/XML).
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNoRecoilNoSpread
  (JNIEnv *, jclass, jstring);

// ─── MLBB SA / Farming / Jungle / All-Hero Features ──────────────────────────

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
 * MLBB Hero-Specific Combo & Damage Scripts
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFannyFastCableCombo
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectGusionDaggerCombo
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectChouKickCombo
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHayabusaShadowCombo
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectBeatrixAllGunDamage
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCriticalBurstOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * All-Gun & All-Scope Universal Calibrations
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllGunWeaponCalibration
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAllScopeMasteryCalibration
  (JNIEnv *, jclass, jstring);

// ─── PUBGM: Aimbot Expansion ─────────────────────────────────────────────────

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

// ─── CODM: Aimbot Expansion ──────────────────────────────────────────────────

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

// ─── MLBB: Ultra Damage / Armor / Hero Expansions ────────────────────────────

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

// ─── 2026 Master 10000+ Damage & Attack Speed Overdrive Suite ───────────────

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFreeFireDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHokDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectWildRiftDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastReloadQuickSwap
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectWallPiercingArmorShredder
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroPingNetworkOverclock
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtreme240FpsGraphics
  (JNIEnv *, jclass, jstring);

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalDamage10000AttackSpeedMax
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectHardwareMaskProfile
 * Fast-path native generator for DeviceProfile.ini (UE4/5) and HardwareProfile.json (Unity)
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHardwareMaskProfile
  (JNIEnv *, jclass, jstring, jstring, jstring, jint, jint);

/*
 * nativeSetProcessIOPriority
 * Direct Linux SYS_ioprio_set and setpriority syscall accelerator
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetProcessIOPriority
  (JNIEnv *, jclass, jint, jint, jint);

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
 * nativeInjectMlbbJungleFastFarmAllHero
 * MLBB — Jungle monster target priority, Retribution instant cast timing, and creep HP thresholding
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbJungleFastFarmAllHero
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
 * nativeInjectMlbbPenetrationCritBurst
 * MLBB — 100% Malefic Roar/Divine Glaive penetration, 3.0x crit multiplier, hero execution mechanics, instant battle spell
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbPenetrationCritBurst
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmBallisticsVelocityPenetration
 * PUBGM — 2.0x Muzzle velocity, instant hit-reg/desync fix, Lv3 vest/helmet shredder, shotgun tight slug, sniper 300 damage lock
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmBallisticsVelocityPenetration
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmBsaRemovalRangeOverdrive
 * CODM — BSA elimination, infinite damage range falloff bypass, instant ADS, zero hit-flinch, sniper blank-scope hitscan
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmBsaRemovalRangeOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalCombatMechanicsOverdrive
 * Universal — 1000Hz touch-to-damage, attack animation canceling, frame-synced damage, true damage conversion
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalCombatMechanicsOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectMlbbFastLoadSplashBypass
 * MLBB — Fast-path asset unbundling, skip intro/splash videos, low-poly initial boot, UI async load, deferred audio load
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbFastLoadSplashBypass
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmFastLoadAsyncStreaming
 * PUBGM — UE4 dedicated async loader thread, 8-core concurrent shader compilation, 1024MB texture stream pool, skip movies/splash
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmFastLoadAsyncStreaming
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmFastLoadShaderBypass
 * CODM — Fast load flag, skip Activision/TiMi splash, async asset loading, deferred weapon mesh prewarm, fast shader cache bypass
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmFastLoadShaderBypass
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectUniversalFastLoadTurbo
 * Universal — Multi-threaded asset streaming and high-speed texture streaming cache
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUniversalFastLoadTurbo
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
 * nativeInjectPubgm165FpsGraphics
 * PUBGM — 165 FPS frame rate & HDR graphics native injection:
 * Formats: UserCustom.ini, EnjoyCJZC.ini, GameUserSettings.ini, Active.sav, XML prefs.
 * Signature: (Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgm165FpsGraphics
  (JNIEnv *, jclass, jstring, jint, jint);

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
 * nativeInjectMlbbAllJungleFastFarmOverdrive
 * MLBB — All jungle fast farm overdrive: 3x smite & clear speed, 3x buff duration & gold/exp rates, instant retribution
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbAllJungleFastFarmOverdrive
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectPubgmAllScopeTieredHeadshot
 * PUBGM — Tiered all-scope auto 3-bullet headshot (100m, 200m, 300m, 400m), zero recoil, bullet tracking & gyro stabilization
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPubgmAllScopeTieredHeadshot
  (JNIEnv *, jclass, jstring);

/*
 * nativeInjectCodmAllScopeTieredHeadshot
 * CODM — Tiered all-scope auto 3-bullet headshot (100m, 200m, 300m, 400m), BSA removal, zero flinch, bullet tracking & gyro stabilization
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectCodmAllScopeTieredHeadshot
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
 * nativeInjectMlbbSmartSkillMagnetAim
 * MLBB — Dual-priority smart skill aim: lowest HP enemy hero (maliit na buhay) & closest enemy hero (malapit na hero)
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectMlbbSmartSkillMagnetAim
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

#ifdef __cplusplus
}
#endif

#endif // GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H



