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
 * Method:    nativeInjectArmorDef
 * Signature: (Ljava/lang/String;FF)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * Class:     com_gamebooster_app_config_NativeConfigInjector
 * Method:    nativeFastMemorySync
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeFastMemorySync
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif

#endif // GAMEBOOSTER_NATIVE_CONFIG_INJECTOR_H
