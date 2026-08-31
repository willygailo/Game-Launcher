#ifndef GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H
#define GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H

#include <jni.h>

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

#ifdef __cplusplus
}
#endif

#endif // GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H
