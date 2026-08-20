#include "native_config_injector.h"
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <cstring>
#include <string>
#include <vector>
#include <sstream>
#include <iostream>
#include <android/log.h>

#define LOG_TAG "NativeConfigInjectorNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static bool write_file_posix(const std::string& path, const std::string& content) {
    int fd = open(path.c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0666);
    if (fd < 0) {
        return false;
    }
    ssize_t written = write(fd, content.data(), content.size());
    fchmod(fd, 0666);
    fsync(fd);
    close(fd);
    return written == static_cast<ssize_t>(content.size());
}

static std::string read_file_posix(const std::string& path) {
    int fd = open(path.c_str(), O_RDONLY);
    if (fd < 0) {
        return "";
    }
    struct stat st;
    if (fstat(fd, &st) < 0 || st.st_size == 0) {
        close(fd);
        return "";
    }
    std::string content(st.st_size, '\0');
    ssize_t bytes_read = read(fd, &content[0], st.st_size);
    close(fd);
    if (bytes_read <= 0) return "";
    content.resize(bytes_read);
    return content;
}

static bool patch_key_value(std::string& content, const std::string& key, const std::string& value) {
    std::string pattern = key + "=";
    size_t pos = content.find(pattern);
    if (pos != std::string::npos) {
        size_t end_pos = content.find('\n', pos);
        if (end_pos == std::string::npos) end_pos = content.length();
        content.replace(pos, end_pos - pos, key + "=" + value);
        return true;
    } else {
        if (!content.empty() && content.back() != '\n') {
            content += "\n";
        }
        content += key + "=" + value + "\n";
        return true;
    }
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectConfig
  (JNIEnv *env, jclass, jstring jPath, jstring jContent) {
    if (!jPath || !jContent) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *content = env->GetStringUTFChars(jContent, nullptr);
    if (!path || !content) return JNI_FALSE;

    bool success = write_file_posix(std::string(path), std::string(content));

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jContent, content);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchKey
  (JNIEnv *env, jclass, jstring jPath, jstring jKey, jstring jValue) {
    if (!jPath || !jKey || !jValue) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *key = env->GetStringUTFChars(jKey, nullptr);
    const char *value = env->GetStringUTFChars(jValue, nullptr);

    std::string content = read_file_posix(path);
    patch_key_value(content, key, value);
    bool success = write_file_posix(path, content);

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jKey, key);
    env->ReleaseStringUTFChars(jValue, value);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeBatchPatchKeys
  (JNIEnv *env, jclass, jstring jPath, jobjectArray jKeys, jobjectArray jValues) {
    if (!jPath || !jKeys || !jValues) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    jsize lenKeys = env->GetArrayLength(jKeys);
    jsize lenVals = env->GetArrayLength(jValues);
    if (lenKeys != lenVals) {
        env->ReleaseStringUTFChars(jPath, path);
        return JNI_FALSE;
    }

    std::string content = read_file_posix(path);
    for (jsize i = 0; i < lenKeys; i++) {
        auto jKeyStr = (jstring)env->GetObjectArrayElement(jKeys, i);
        auto jValStr = (jstring)env->GetObjectArrayElement(jValues, i);
        if (jKeyStr && jValStr) {
            const char *k = env->GetStringUTFChars(jKeyStr, nullptr);
            const char *v = env->GetStringUTFChars(jValStr, nullptr);
            patch_key_value(content, k, v);
            env->ReleaseStringUTFChars(jKeyStr, k);
            env->ReleaseStringUTFChars(jValStr, v);
        }
        if (jKeyStr) env->DeleteLocalRef(jKeyStr);
        if (jValStr) env->DeleteLocalRef(jValStr);
    }

    bool success = write_file_posix(path, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageBoost
  (JNIEnv *env, jclass, jstring jPath, jfloat multiplier, jfloat headshotMultiplier, jint critRate) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    std::string content = read_file_posix(path);
    std::ostringstream ssMult, ssHead, ssCrit;
    ssMult << multiplier;
    ssHead << headshotMultiplier;
    ssCrit << critRate;

    patch_key_value(content, "DamageMultiplier", ssMult.str());
    patch_key_value(content, "PhysicalDamageBoost", ssMult.str());
    patch_key_value(content, "MagicDamageBoost", ssMult.str());
    patch_key_value(content, "TrueDamageBoost", ssMult.str());
    patch_key_value(content, "BulletDamageBoost", ssMult.str());
    patch_key_value(content, "HeadshotDamageMultiplier", ssHead.str());
    patch_key_value(content, "CriticalHitRate", ssCrit.str());
    patch_key_value(content, "CriticalDamageMultiplier", ssHead.str());
    patch_key_value(content, "PenetrationBoost", "99");
    patch_key_value(content, "ArmorPenetration", "99");
    patch_key_value(content, "HighDamageRateMode", "1");

    bool success = write_file_posix(path, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroRecoil
  (JNIEnv *env, jclass, jstring jPath, jfloat recoilScale, jint stability) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    std::string content = read_file_posix(path);
    std::ostringstream ssRecoil, ssStab;
    ssRecoil << recoilScale;
    ssStab << stability;

    patch_key_value(content, "RecoilControl", "1");
    patch_key_value(content, "ZeroRecoil", "1");
    patch_key_value(content, "NoRecoil", "1");
    patch_key_value(content, "RecoilScale", ssRecoil.str());
    patch_key_value(content, "VerticalRecoil", ssRecoil.str());
    patch_key_value(content, "HorizontalRecoil", ssRecoil.str());
    patch_key_value(content, "RecoilReduction", "1.50");
    patch_key_value(content, "WeaponStability", ssStab.str());
    patch_key_value(content, "ScreenShake", "0");
    patch_key_value(content, "GunKick", "0");
    patch_key_value(content, "BulletSpread", "0.00");
    patch_key_value(content, "CrosshairSpread", "0.00");
    patch_key_value(content, "ScopeStability", "1.50");
    patch_key_value(content, "FirstBulletAccuracy", "1");

    bool success = write_file_posix(path, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist
  (JNIEnv *env, jclass, jstring jPath, jint strength, jint precision) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    std::string content = read_file_posix(path);
    std::ostringstream ssStr, ssPrec;
    ssStr << strength;
    ssPrec << precision;

    patch_key_value(content, "AimAssist", "1");
    patch_key_value(content, "AimAssistStrength", ssStr.str());
    patch_key_value(content, "AimAssistLevel", "5");
    patch_key_value(content, "AimPrecision", ssPrec.str());
    patch_key_value(content, "AutoAim", "1");
    patch_key_value(content, "AimTracking", "1");
    patch_key_value(content, "TargetLock", "1");
    patch_key_value(content, "SmartTargetingMode", "1");
    patch_key_value(content, "HeroPriorityLock", "1");
    patch_key_value(content, "LowestHPTargetLock", "1");
    patch_key_value(content, "AimAssistRadius", "200");
    patch_key_value(content, "CrosshairMagnetism", "1.50");
    patch_key_value(content, "GyroSampleRate", "1000");
    patch_key_value(content, "GyroZeroDelay", "1");
    patch_key_value(content, "GyroSensitivityRatio", "2.5");
    patch_key_value(content, "GyroStabilization", "1");

    bool success = write_file_posix(path, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *env, jclass, jstring jPath, jfloat defBoost, jfloat dmgReduction) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    std::string content = read_file_posix(path);
    std::ostringstream ssDef, ssRed;
    ssDef << defBoost;
    ssRed << dmgReduction;

    patch_key_value(content, "PhysicalDefenseBoost", ssDef.str());
    patch_key_value(content, "MagicDefenseBoost", ssDef.str());
    patch_key_value(content, "DamageReductionRatio", ssRed.str());
    patch_key_value(content, "ShieldMultiplier", "2.00");
    patch_key_value(content, "MaxHPMultiplier", "1.50");
    patch_key_value(content, "DamageAbsorbRatio", "1.50");
    patch_key_value(content, "ArmorBoost", "150");
    patch_key_value(content, "VestDurability", "2.00");
    patch_key_value(content, "HelmetDamageReduction", "0.60");
    patch_key_value(content, "TenacityRatio", "0.50");
    patch_key_value(content, "ResilienceLevel", "3");

    bool success = write_file_posix(path, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeFastMemorySync
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    int fd = open(path, O_RDWR);
    if (fd < 0) {
        env->ReleaseStringUTFChars(jPath, path);
        return JNI_FALSE;
    }
    struct stat st;
    if (fstat(fd, &st) == 0 && st.st_size > 0) {
        void *addr = mmap(nullptr, st.st_size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
        if (addr != MAP_FAILED) {
            msync(addr, st.st_size, MS_SYNC);
            munmap(addr, st.st_size);
        }
    }
    fsync(fd);
    close(fd);
    env->ReleaseStringUTFChars(jPath, path);
    return JNI_TRUE;
}
