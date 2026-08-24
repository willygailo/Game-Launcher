#ifndef GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H
#define GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H

#include <jni.h>
#include <string>
#include <vector>

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
 * Method:    nativeInjectDamageBoost
 * Signature: (Ljava/lang/String;FFI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageBoost
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectZeroRecoil
 * Signature: (Ljava/lang/String;FI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroRecoil
  (JNIEnv *, jclass, jstring, jfloat, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectAimAssist
 * Signature: (Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectTrackingBullet
 * Signature: (Ljava/lang/String;FF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectArmorDef
 * Signature: (Ljava/lang/String;FF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

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
 * Method:    nativeInjectSpeedBoost
 * Signature: (Ljava/lang/String;FF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSpeedBoost
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectHeroDamage1000
 * Signature: (Ljava/lang/String;FFII)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroDamage1000
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jint, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectScopeZeroRecoil
 * Signature: (Ljava/lang/String;FI)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectScopeZeroRecoil
  (JNIEnv *, jclass, jstring, jfloat, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectAimAssist1000
 * Signature: (Ljava/lang/String;IF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist1000
  (JNIEnv *, jclass, jstring, jint, jfloat);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectTrackingBullet1000
 * Signature: (Ljava/lang/String;FF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet1000
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectArmorDef1000
 * Signature: (Ljava/lang/String;FF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef1000
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectFastCooldown
 * Signature: (Ljava/lang/String;F)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastCooldown
  (JNIEnv *, jclass, jstring, jfloat);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectShield1500
 * Signature: (Ljava/lang/String;FF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectShield1500
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectDroneView
 * Signature: (Ljava/lang/String;II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDroneView
  (JNIEnv *, jclass, jstring, jint, jint);

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
 * Method:    nativeSetProcessCpuAffinity
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetProcessCpuAffinity
  (JNIEnv *, jclass, jint, jint);

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
 * Method:    nativeSetThreadSchedulingPolicy
 * Signature: (III)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeSetThreadSchedulingPolicy
  (JNIEnv *, jclass, jint, jint, jint);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeForceVulkanPipelineCache
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeForceVulkanPipelineCache
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeInjectNextGenTouchSampling
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectNextGenTouchSampling
  (JNIEnv *, jclass, jstring, jint);

#ifdef __cplusplus
}
#endif

#endif // GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H

