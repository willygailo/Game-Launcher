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
#include <algorithm>
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

static bool patch_cvar(std::string& content, const std::string& cvar, const std::string& value) {
    std::string key = "+CVars=" + cvar + "=";
    size_t pos = content.find(key);
    if (pos != std::string::npos) {
        size_t end_pos = content.find('\n', pos);
        if (end_pos == std::string::npos) end_pos = content.length();
        content.replace(pos, end_pos - pos, key + value);
        return true;
    } else {
        if (!content.empty() && content.back() != '\n') {
            content += "\n";
        }
        content += key + value + "\n";
        return true;
    }
}

static bool patch_xml_node(std::string& content, const std::string& tag, const std::string& key, const std::string& value) {
    std::string namePattern = "name=\"" + key + "\"";
    size_t pos = content.find(namePattern);
    if (pos != std::string::npos) {
        size_t lineStart = content.rfind('<', pos);
        size_t lineEnd = content.find('>', pos);
        if (lineStart != std::string::npos && lineEnd != std::string::npos) {
            std::string replacement;
            if (tag == "string") {
                size_t closeTag = content.find("</string>", pos);
                if (closeTag != std::string::npos && closeTag < lineEnd + 200) {
                    lineEnd = closeTag + 8;
                }
                replacement = "<string name=\"" + key + "\">" + value + "</string>";
            } else {
                replacement = "<" + tag + " name=\"" + key + "\" value=\"" + value + "\" />";
            }
            content.replace(lineStart, lineEnd - lineStart + 1, replacement);
            return true;
        }
    }

    size_t mapEnd = content.find("</map>");
    if (mapEnd != std::string::npos) {
        std::string insertion;
        if (tag == "string") {
            insertion = "  <string name=\"" + key + "\">" + value + "</string>\n";
        } else {
            insertion = "  <" + tag + " name=\"" + key + "\" value=\"" + value + "\" />\n";
        }
        content.insert(mapEnd, insertion);
        return true;
    } else {
        if (content.empty()) {
            content = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n<map>\n";
            if (tag == "string") {
                content += "  <string name=\"" + key + "\">" + value + "</string>\n";
            } else {
                content += "  <" + tag + " name=\"" + key + "\" value=\"" + value + "\" />\n";
            }
            content += "</map>\n";
            return true;
        }
    }
    return false;
}

static bool patch_json_prop(std::string& content, const std::string& key, const std::string& value, bool isNumeric) {
    std::string keyPattern = "\"" + key + "\":";
    size_t pos = content.find(keyPattern);
    if (pos != std::string::npos) {
        size_t valStart = pos + keyPattern.length();
        while (valStart < content.length() && (content[valStart] == ' ' || content[valStart] == '\t')) valStart++;
        size_t valEnd = content.find_first_of(",}\n", valStart);
        if (valEnd == std::string::npos) valEnd = content.length();
        std::string formattedVal = isNumeric ? value : ("\"" + value + "\"");
        content.replace(valStart, valEnd - valStart, formattedVal);
        return true;
    }

    size_t lastBrace = content.rfind('}');
    if (lastBrace != std::string::npos) {
        size_t prevNonWs = content.find_last_not_of(" \t\n\r", lastBrace - 1);
        std::string insertion;
        if (prevNonWs != std::string::npos && content[prevNonWs] != '{' && content[prevNonWs] != ',') {
            insertion += ",\n";
        } else {
            insertion += "\n";
        }
        insertion += "  \"" + key + "\": " + (isNumeric ? value : ("\"" + value + "\"")) + "\n";
        content.insert(lastBrace, insertion);
        return true;
    } else {
        content = "{\n  \"" + key + "\": " + (isNumeric ? value : ("\"" + value + "\"")) + "\n}\n";
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

    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);
    bool success = false;

    if (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos) {
        success = patch_xml_node(content, "string", key, value);
    } else if (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{')) {
        success = patch_json_prop(content, key, value, false);
    } else {
        success = patch_key_value(content, key, value);
    }

    if (success) {
        success = write_file_posix(pathStr, content);
    }

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jKey, key);
    env->ReleaseStringUTFChars(jValue, value);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchXmlKey
  (JNIEnv *env, jclass, jstring jPath, jstring jTag, jstring jKey, jstring jValue) {
    if (!jPath || !jTag || !jKey || !jValue) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *tag = env->GetStringUTFChars(jTag, nullptr);
    const char *key = env->GetStringUTFChars(jKey, nullptr);
    const char *value = env->GetStringUTFChars(jValue, nullptr);

    std::string content = read_file_posix(path);
    bool patched = patch_xml_node(content, tag, key, value);
    bool success = patched && write_file_posix(path, content);

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jTag, tag);
    env->ReleaseStringUTFChars(jKey, key);
    env->ReleaseStringUTFChars(jValue, value);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePatchJsonKey
  (JNIEnv *env, jclass, jstring jPath, jstring jKey, jstring jValue, jboolean isNumeric) {
    if (!jPath || !jKey || !jValue) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *key = env->GetStringUTFChars(jKey, nullptr);
    const char *value = env->GetStringUTFChars(jValue, nullptr);

    std::string content = read_file_posix(path);
    bool patched = patch_json_prop(content, key, value, isNumeric == JNI_TRUE);
    bool success = patched && write_file_posix(path, content);

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

    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);
    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    for (jsize i = 0; i < lenKeys; i++) {
        auto jKeyStr = (jstring)env->GetObjectArrayElement(jKeys, i);
        auto jValStr = (jstring)env->GetObjectArrayElement(jValues, i);
        if (jKeyStr && jValStr) {
            const char *k = env->GetStringUTFChars(jKeyStr, nullptr);
            const char *v = env->GetStringUTFChars(jValStr, nullptr);
            if (isXml) {
                patch_xml_node(content, "string", k, v);
            } else if (isJson) {
                patch_json_prop(content, k, v, false);
            } else {
                patch_key_value(content, k, v);
            }
            env->ReleaseStringUTFChars(jKeyStr, k);
            env->ReleaseStringUTFChars(jValStr, v);
        }
        if (jKeyStr) env->DeleteLocalRef(jKeyStr);
        if (jValStr) env->DeleteLocalRef(jValStr);
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDamageBoost
  (JNIEnv *env, jclass, jstring jPath, jfloat multiplier, jfloat headshotMultiplier, jint critRate) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssMult, ssHead, ssCrit;
    ssMult << multiplier;
    ssHead << headshotMultiplier;
    ssCrit << critRate;

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageMultiplier", ssMult.str()},
        {"PhysicalDamageBoost", ssMult.str()},
        {"MagicDamageBoost", ssMult.str()},
        {"TrueDamageBoost", ssMult.str()},
        {"BulletDamageBoost", ssMult.str()},
        {"DamageBoostRatio", ssMult.str()},
        {"HeadshotMultiplier", ssHead.str()},
        {"HeadshotDamageMultiplier", ssHead.str()},
        {"CriticalHitRate", ssCrit.str()},
        {"CriticalDamage", ssCrit.str()},
        {"CriticalDamageRate", ssCrit.str()},
        {"CriticalDamageMultiplier", ssHead.str()},
        {"PenetrationBoost", "100"},
        {"ArmorPenetration", "100"},
        {"PhysicalPenetrationBoost", "100"},
        {"MagicPenetrationBoost", "100"},
        {"MagicResistPenetration", "100"},
        {"HighDamageRateMode", "1"},
        {"AttackSpeedMultiplier", "3.00"},
        {"AttackSpeedBoost", "3.00"},
        {"ReloadSpeedMultiplier", "3.00"},
        {"FireRateMultiplier", "2.50"},
        {"MovementSpeedMultiplier", "3.00"},
        {"SprintSpeedMultiplier", "3.00"},
        {"SprintSensitivity", "200"},
        {"AgilityMultiplier", "3.00"},
        {"SkillDamageMultiplier", ssMult.str()},
        {"DamageAssetOverride", "1"},
        {"AutoDamageExecutionMode", "1"},
        {"AutoSmiteExecution", "1"},
        {"RetributionDamageThreshold", "5000"},
        {"TurretDamageReduction", "0.85"},
        {"MinionDamageBoost", "3.00"},
        {"MonsterDamageBoost", ssMult.str()},
        {"HitboxExpansion", "2.50"},
        {"BulletVelocityMultiplier", "5.00"},
        {"BulletVelocityScale", "5.00"},
        {"BodyDamageMultiplier", "3.50"},
        {"LimbDamageMultiplier", "3.00"},
        {"ExplosiveDamageMultiplier", "3.50"}
    };

    if (isXml) {
        for (const auto& kv : keys) {
            patch_xml_node(content, "string", kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : keys) {
            patch_json_prop(content, kv.first, kv.second, false);
        }
    } else {
        if (content.find("[DamageScript]") == std::string::npos) {
            content += "\n[DamageScript]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }

        // Unreal Engine 4/5 CVars
        patch_cvar(content, "r.DamageMultiplier", ssMult.str());
        patch_cvar(content, "r.BulletDamageScale", ssMult.str());
        patch_cvar(content, "r.HeadshotMultiplier", ssHead.str());
        patch_cvar(content, "r.WeaponDamageScale", ssMult.str());
        patch_cvar(content, "r.CriticalHitRate", "1.00");
        patch_cvar(content, "r.HitboxExpansion", "2.50");
        patch_cvar(content, "r.BulletVelocityScale", "5.00");
        patch_cvar(content, "r.PenetrationPower", "5.00");
        patch_cvar(content, "r.BodyDamageMultiplier", "3.50");
        patch_cvar(content, "r.LimbDamageMultiplier", "3.00");
        patch_cvar(content, "r.ExplosiveDamageMultiplier", "3.50");
        patch_cvar(content, "r.MovementSpeedMultiplier", "3.00");
        patch_cvar(content, "r.SprintSpeedMultiplier", "3.00");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectZeroRecoil
  (JNIEnv *env, jclass, jstring jPath, jfloat recoilScale, jint stability) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssRecoil, ssStab;
    ssRecoil << recoilScale;
    ssStab << stability;

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"RecoilControl", "1"},
        {"ZeroRecoil", "1"},
        {"NoRecoil", "1"},
        {"RecoilScale", ssRecoil.str()},
        {"VerticalRecoil", ssRecoil.str()},
        {"HorizontalRecoil", ssRecoil.str()},
        {"VerticalRecoilScale", ssRecoil.str()},
        {"HorizontalRecoilScale", ssRecoil.str()},
        {"VerticalRecoilMultiplier", ssRecoil.str()},
        {"HorizontalRecoilMultiplier", ssRecoil.str()},
        {"RecoilReduction", "2.00"},
        {"WeaponStability", ssStab.str()},
        {"ScreenShake", "0"},
        {"CameraShake", "0"},
        {"NoCameraShake", "1"},
        {"GunKick", "0"},
        {"GunKickReduction", "2.00"},
        {"WeaponKickReduction", "2.00"},
        {"AllGunsRecoilReduction", "2.00"},
        {"ScopeShakeReduction", "2.00"},
        {"ScopeRecoilMultiplier", "0.00"},
        {"ScopeStability", "2.50"},
        {"BulletSpread", "0.00"},
        {"CrosshairSpread", "0.00"},
        {"SpreadScale", "0.00"},
        {"BulletSpreadReduction", "1"},
        {"FirstBulletAccuracy", "1"},
        {"WeaponSway", "0"},
        {"AimPunchReduction", "1"},
        {"FlinchReduction", "1"},
        {"MovementStabilization", "1"},
        {"JoystickZeroDeadzone", "1"},
        {"TouchJitterFilter", "1"},
        {"ZeroInputDelay", "1"}
    };

    if (isXml) {
        for (const auto& kv : keys) {
            patch_xml_node(content, "string", kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : keys) {
            patch_json_prop(content, kv.first, kv.second, false);
        }
    } else {
        if (content.find("[RecoilControl]") == std::string::npos) {
            content += "\n[RecoilControl]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }

        // Unreal Engine 4/5 CVars
        patch_cvar(content, "r.WeaponRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.VerticalRecoilMultiplier", ssRecoil.str());
        patch_cvar(content, "r.HorizontalRecoilMultiplier", ssRecoil.str());
        patch_cvar(content, "r.GunKickReduction", "1");
        patch_cvar(content, "r.CameraShake", "0");
        patch_cvar(content, "r.ScreenShake", "0");
        patch_cvar(content, "r.WeaponSway", "0");
        patch_cvar(content, "r.BulletSpread", "0.00");
        patch_cvar(content, "r.CrosshairSpread", "0.00");
        patch_cvar(content, "r.ScopeStability", "2.50");
        patch_cvar(content, "r.FirstBulletAccuracy", "1");
        patch_cvar(content, "r.AimPunchReduction", "1");
        patch_cvar(content, "r.FlinchReduction", "1");
        patch_cvar(content, "r.WeaponKick", "0.00");
        patch_cvar(content, "r.ViewKick", "0.00");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist
  (JNIEnv *env, jclass, jstring jPath, jint strength, jint precision) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssStr, ssPrec;
    ssStr << strength;
    ssPrec << precision;

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"AimAssist", "1"},
        {"AimAssistStrength", ssStr.str()},
        {"AimAssistLevel", "5"},
        {"AimPrecision", ssPrec.str()},
        {"AutoAim", "1"},
        {"AimTracking", "1"},
        {"TargetLock", "1"},
        {"TargetLockSensitivity", "200"},
        {"SmartTargetingMode", "1"},
        {"HeroPriorityLock", "1"},
        {"LowestHPTargetLock", "1"},
        {"AimAssistRadius", "250"},
        {"CrosshairMagnetism", "2.00"},
        {"ScopeAimAssist", "1"},
        {"RedDotAimAssist", "1"},
        {"GyroSampleRate", "1000"},
        {"GyroZeroDelay", "1"},
        {"GyroSensitivityRatio", "3.0"},
        {"GyroStabilization", "1"},
        {"GyroSmoothFactor", "1"},
        {"GyroLatencyMode", "0"}
    };

    if (isXml) {
        for (const auto& kv : keys) {
            patch_xml_node(content, "string", kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : keys) {
            patch_json_prop(content, kv.first, kv.second, false);
        }
    } else {
        if (content.find("[AimAssist]") == std::string::npos) {
            content += "\n[AimAssist]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }

        // Unreal Engine 4/5 CVars
        patch_cvar(content, "r.AimAssist", "1");
        patch_cvar(content, "r.AimAssist.Strength", "3.0");
        patch_cvar(content, "r.AimAssistRadius", "250");
        patch_cvar(content, "r.GyroSampleRate", "1000");
        patch_cvar(content, "r.GyroZeroDelay", "1");
        patch_cvar(content, "r.GyroSensitivityRatio", "3.0");
        patch_cvar(content, "r.GyroStabilization", "1");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet
  (JNIEnv *env, jclass, jstring jPath, jfloat trackingStrength, jfloat hitboxMultiplier) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssTrack, ssHitbox;
    ssTrack << trackingStrength;
    ssHitbox << hitboxMultiplier;

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"TrackingBullet", "1"},
        {"BulletTracking", "1"},
        {"AutoTrackingBullet", "1"},
        {"MagicBullet", "1"},
        {"BulletMagnetism", ssTrack.str()},
        {"HitboxExpansion", ssHitbox.str()},
        {"TargetLockTracking", "1"},
        {"BulletCurveFactor", "2.00"},
        {"BulletVelocityMultiplier", "5.00"},
        {"BulletSpread", "0.00"},
        {"CrosshairMagnetism", "2.00"},
        {"FirstBulletAccuracy", "1"},
        {"ProjectileHoming", "1"}
    };

    if (isXml) {
        for (const auto& kv : keys) {
            patch_xml_node(content, "string", kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : keys) {
            patch_json_prop(content, kv.first, kv.second, false);
        }
    } else {
        if (content.find("[TrackingBullet]") == std::string::npos) {
            content += "\n[TrackingBullet]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }

        // Unreal Engine 4/5 CVars
        patch_cvar(content, "r.BulletTracking", "1");
        patch_cvar(content, "r.MagicBullet", "1");
        patch_cvar(content, "r.HitboxExpansion", ssHitbox.str());
        patch_cvar(content, "r.BulletMagnetism", ssTrack.str());
        patch_cvar(content, "r.BulletVelocityScale", "5.0");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *env, jclass, jstring jPath, jfloat defBoost, jfloat dmgReduction) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssDef, ssRed;
    ssDef << defBoost;
    ssRed << dmgReduction;

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"PhysicalDefenseBoost", ssDef.str()},
        {"MagicDefenseBoost", ssDef.str()},
        {"DamageReductionRatio", ssRed.str()},
        {"DamageReduction", ssRed.str()},
        {"IncomingDamageReduction", ssRed.str()},
        {"ShieldMultiplier", "5.00"},
        {"ShieldCapacity", "5.00"},
        {"ShieldStrength", "5.00"},
        {"MaxHPMultiplier", "3.00"},
        {"HPBoostRatio", "3.00"},
        {"DamageAbsorbRatio", "3.00"},
        {"ArmorBoost", "500"},
        {"MagicResistBoost", "500"},
        {"VestDurability", "5.00"},
        {"VestDurabilityBoost", "5.00"},
        {"HelmetDamageReduction", "0.90"},
        {"TenacityRatio", "0.80"},
        {"ResilienceLevel", "5"},
        {"ArmorLevel", "6"},
        {"DamageResistance", ssRed.str()},
        {"ShieldEfficiency", "5.00"},
        {"ShieldPointsMultiplier", "5.00"},
        {"ArmorPlateEfficiency", "5.00"},
        {"KineticArmorBoost", "5.00"},
        {"FlakJacketRatio", "0.90"},
        {"HealthRegenDelay", "0.00"},
        {"HealthRegenBoost", "5.00"},
        {"FallDamageReduction", "1.00"},
        {"ExplosionResistance", "0.90"},
        {"HeadshotDamageReduction", "0.90"}
    };

    if (isXml) {
        for (const auto& kv : keys) {
            patch_xml_node(content, "string", kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : keys) {
            patch_json_prop(content, kv.first, kv.second, false);
        }
    } else {
        if (content.find("[DefenseConfig]") == std::string::npos) {
            content += "\n[DefenseConfig]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }

        // Unreal Engine 4/5 CVars
        patch_cvar(content, "r.ArmorDamageReduction", ssRed.str());
        patch_cvar(content, "r.VestDurabilityBoost", "5.00");
        patch_cvar(content, "r.HelmetDamageReduction", "0.90");
        patch_cvar(content, "r.IncomingDamageScale", ssRed.str());
        patch_cvar(content, "r.ShieldEfficiency", "5.00");
        patch_cvar(content, "r.DamageResistance", ssRed.str());
        patch_cvar(content, "r.TenacityRatio", "0.80");
        patch_cvar(content, "r.HealthRegenBoost", "5.00");
        patch_cvar(content, "r.FallDamageReduction", "1.00");
        patch_cvar(content, "r.ExplosionResistance", "0.90");
        patch_cvar(content, "r.HeadshotDamageReduction", "0.90");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectSpeedBoost
  (JNIEnv *env, jclass, jstring jPath, jfloat speedMultiplier, jfloat sprintBoost) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssSpeed, ssSprint;
    ssSpeed << speedMultiplier;
    ssSprint << sprintBoost;

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"MovementSpeedMultiplier", ssSpeed.str()},
        {"MovementSpeedBoost", ssSpeed.str()},
        {"SprintSpeedMultiplier", ssSprint.str()},
        {"SprintSpeedBoost", ssSprint.str()},
        {"SprintSensitivity", "200"},
        {"AgilityMultiplier", ssSpeed.str()},
        {"AttackSpeedMultiplier", "3.00"},
        {"AttackSpeedBoost", "3.00"},
        {"ReloadSpeedMultiplier", "3.00"},
        {"FireRateMultiplier", "2.50"},
        {"BulletVelocityMultiplier", "5.00"},
        {"BulletVelocityScale", "5.00"},
        {"ThrottleResponse", "2.50"},
        {"AccelerationMultiplier", "3.00"},
        {"TopSpeedBoost", "2.50"},
        {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"},
        {"HighSpeedMovement", "1"}
    };

    if (isXml) {
        for (const auto& kv : keys) {
            patch_xml_node(content, "string", kv.first, kv.second);
        }
    } else if (isJson) {
        for (const auto& kv : keys) {
            patch_json_prop(content, kv.first, kv.second, false);
        }
    } else {
        if (content.find("[SpeedEngine]") == std::string::npos) {
            content += "\n[SpeedEngine]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }

        // Unreal Engine 4/5 CVars
        patch_cvar(content, "r.MovementSpeedMultiplier", ssSpeed.str());
        patch_cvar(content, "r.SprintSpeedMultiplier", ssSprint.str());
        patch_cvar(content, "r.AttackSpeedMultiplier", "3.00");
        patch_cvar(content, "r.BulletVelocityScale", "5.00");
        patch_cvar(content, "r.ZeroInputLag", "1");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectUltraExtremeGraphics
  (JNIEnv *env, jclass, jstring jPath, jint targetFps) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    std::string content = read_file_posix(path);
    std::ostringstream ssFps;
    ssFps << targetFps;

    // Real Ultra Extreme FPS & Refresh Rate Unlocks
    patch_key_value(content, "FPS", ssFps.str());
    patch_key_value(content, "TargetFPS", ssFps.str());
    patch_key_value(content, "MaxFPS", ssFps.str());
    patch_key_value(content, "MaxFrameRate", ssFps.str());
    patch_key_value(content, "FrameRateLimit", ssFps.str());
    patch_key_value(content, "MobileFPSLimit", ssFps.str());
    patch_key_value(content, "HighFPSMode", "1");
    patch_key_value(content, "HighFrameRate", "1");
    patch_key_value(content, "SuperHighFPS", "1");
    patch_key_value(content, "UnlockFPS", "1");
    patch_key_value(content, "UnlockHighFPS", "1");
    patch_key_value(content, "Unlock120Hz", "1");
    patch_key_value(content, "Unlock144Hz", "1");
    patch_key_value(content, "Unlock165Hz", "1");
    patch_key_value(content, "Unlock185Hz", "1");
    patch_key_value(content, "Unlock120FPS", "1");
    patch_key_value(content, "Unlock144FPS", "1");
    patch_key_value(content, "Unlock165FPS", "1");
    patch_key_value(content, "Unlock185FPS", "1");
    patch_key_value(content, "Ultra144FPS", "1");
    patch_key_value(content, "Ultra165FPS", "1");
    patch_key_value(content, "Ultra185FPS", "1");

    // Real Ultra Extreme Graphics Unlocks
    patch_key_value(content, "UltraExtreme", "1");
    patch_key_value(content, "bUseUltraExtreme", "True");
    patch_key_value(content, "GraphicsQuality", "5");
    patch_key_value(content, "GraphicQuality", "4");
    patch_key_value(content, "GraphicLevel", "4");
    patch_key_value(content, "HDRMode", "1");
    patch_key_value(content, "HDRColorMode", "2");
    patch_key_value(content, "UltraHDMode", "1");
    patch_key_value(content, "HDMode", "1");
    patch_key_value(content, "SuperResolution", "1");
    patch_key_value(content, "ResolutionScale", "1.20");
    patch_key_value(content, "ScreenScale", "120");
    patch_key_value(content, "Shadow", "1");
    patch_key_value(content, "ShadowQuality", "2");
    patch_key_value(content, "AntiAliasing", "1");
    patch_key_value(content, "AntiAliasingQuality", "4");
    patch_key_value(content, "PostProcessQuality", "3");
    patch_key_value(content, "TextureQuality", "3");
    patch_key_value(content, "EffectsQuality", "3");
    patch_key_value(content, "FoliageQuality", "2");
    patch_key_value(content, "ShadingQuality", "2");
    patch_key_value(content, "VulkanEnabled", "1");
    patch_key_value(content, "bUseHDRMode", "True");
    patch_key_value(content, "bUseHighQualityBloom", "True");
    patch_key_value(content, "bUseAntiAliasing", "True");
    patch_key_value(content, "UnlockMaxGraphics", "1");
    patch_key_value(content, "MaxGraphic", "1");
    patch_key_value(content, "UltraQuality", "1");

    // UE4 CVars
    patch_cvar(content, "r.PUBGDeviceFPS", "10");
    patch_cvar(content, "r.PUBGMaxFPS", ssFps.str());
    patch_cvar(content, "r.PUBGFrameRateLimit", ssFps.str());
    patch_cvar(content, "r.FrameRateLimit", ssFps.str());
    patch_cvar(content, "r.MobileFPSLimit", ssFps.str());
    patch_cvar(content, "r.PUBGQualityLevel", "4");
    patch_cvar(content, "r.PUBGSDKQualityLevel", "4");
    patch_cvar(content, "r.Tonemapper.Quality", "4");
    patch_cvar(content, "r.PUBGHDRMode", "1");
    patch_cvar(content, "r.MobileHDR", "1");
    patch_cvar(content, "r.HDR.Display.OutputDevice", "1");
    patch_cvar(content, "r.Unlock120Hz", "1");
    patch_cvar(content, "r.Unlock144Hz", "1");
    patch_cvar(content, "r.Unlock165Hz", "1");
    patch_cvar(content, "r.Unlock185Hz", "1");
    patch_cvar(content, "r.MobileContentScaleFactor", "1.0");

    bool success = write_file_posix(path, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectPerGameProfile
  (JNIEnv *env, jclass, jstring jPath, jstring jGameKey, jint targetFps, jboolean highDamage, jboolean noRecoil, jboolean trackingBullet, jboolean aimAssist) {
    if (!jPath || !jGameKey) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *gameKey = env->GetStringUTFChars(jGameKey, nullptr);

    std::string content = read_file_posix(path);
    std::ostringstream ssFps;
    ssFps << targetFps;

    // Refresh rate and ultra extreme graphics
    patch_key_value(content, "MaxFPS", ssFps.str());
    patch_key_value(content, "TargetFPS", ssFps.str());
    patch_key_value(content, "FPS", ssFps.str());
    patch_key_value(content, "HighFPSMode", "1");
    patch_key_value(content, "Unlock185Hz", "1");
    patch_key_value(content, "Unlock165Hz", "1");
    patch_key_value(content, "Unlock144Hz", "1");
    patch_key_value(content, "Unlock120Hz", "1");
    patch_key_value(content, "Unlock185FPS", "1");
    patch_key_value(content, "Unlock165FPS", "1");
    patch_key_value(content, "Unlock144FPS", "1");
    patch_key_value(content, "Unlock120FPS", "1");
    patch_key_value(content, "Ultra185FPS", "1");
    patch_key_value(content, "Ultra165FPS", "1");
    patch_key_value(content, "Ultra144FPS", "1");
    patch_key_value(content, "HighFreqTouchHz", ssFps.str());
    patch_key_value(content, "TouchPollingRate", "1000");
    patch_key_value(content, "TouchZeroDelay", "1");

    // Ultra Extreme Graphics
    patch_key_value(content, "UltraExtreme", "1");
    patch_key_value(content, "bUseUltraExtreme", "True");
    patch_key_value(content, "GraphicsQuality", "5");
    patch_key_value(content, "GraphicQuality", "4");
    patch_key_value(content, "HDRMode", "1");
    patch_key_value(content, "HDRColorMode", "2");
    patch_key_value(content, "UltraHDMode", "1");
    patch_key_value(content, "SuperResolution", "1");
    patch_key_value(content, "ResolutionScale", "1.20");
    patch_key_value(content, "ScreenScale", "120");
    patch_key_value(content, "ShadowQuality", "2");
    patch_key_value(content, "AntiAliasingQuality", "4");
    patch_key_value(content, "PostProcessQuality", "3");
    patch_key_value(content, "TextureQuality", "3");
    patch_key_value(content, "EffectsQuality", "3");
    patch_key_value(content, "FoliageQuality", "2");
    patch_key_value(content, "ShadingQuality", "2");
    patch_key_value(content, "VulkanEnabled", "1");
    patch_key_value(content, "UnlockMaxGraphics", "1");
    patch_key_value(content, "MaxGraphic", "1");

    // Unreal Engine CVars
    patch_cvar(content, "r.PUBGDeviceFPS", "10");
    patch_cvar(content, "r.PUBGMaxFPS", ssFps.str());
    patch_cvar(content, "r.PUBGFrameRateLimit", ssFps.str());
    patch_cvar(content, "r.FrameRateLimit", ssFps.str());
    patch_cvar(content, "r.MobileFPSLimit", ssFps.str());
    patch_cvar(content, "r.Unlock120Hz", "1");
    patch_cvar(content, "r.Unlock144Hz", "1");
    patch_cvar(content, "r.Unlock165Hz", "1");
    patch_cvar(content, "r.Unlock185Hz", "1");
    patch_cvar(content, "r.PUBGQualityLevel", "4");
    patch_cvar(content, "r.PUBGSDKQualityLevel", "4");
    patch_cvar(content, "r.MobileHDR", "1");

    if (highDamage) {
        patch_key_value(content, "DamageMultiplier", "2.50");
        patch_key_value(content, "PhysicalDamageBoost", "2.50");
        patch_key_value(content, "MagicDamageBoost", "2.50");
        patch_key_value(content, "TrueDamageBoost", "2.50");
        patch_key_value(content, "BulletDamageBoost", "2.50");
        patch_key_value(content, "HeadshotDamageMultiplier", "3.50");
        patch_key_value(content, "CriticalHitRate", "99");
        patch_key_value(content, "PenetrationBoost", "99");
        patch_key_value(content, "ArmorPenetration", "99");
        patch_cvar(content, "r.DamageMultiplier", "2.50");
        patch_cvar(content, "r.BulletDamageScale", "2.50");
        patch_cvar(content, "r.HeadshotMultiplier", "3.50");
        patch_cvar(content, "r.WeaponDamageScale", "2.50");
    }

    if (noRecoil) {
        patch_key_value(content, "RecoilControl", "1");
        patch_key_value(content, "ZeroRecoil", "1");
        patch_key_value(content, "NoRecoil", "1");
        patch_key_value(content, "RecoilScale", "0.00");
        patch_key_value(content, "VerticalRecoil", "0.00");
        patch_key_value(content, "HorizontalRecoil", "0.00");
        patch_key_value(content, "RecoilReduction", "1.50");
        patch_key_value(content, "WeaponStability", "150");
        patch_key_value(content, "ScreenShake", "0");
        patch_key_value(content, "GunKick", "0");
        patch_key_value(content, "BulletSpread", "0.00");
        patch_key_value(content, "CrosshairSpread", "0.00");
        patch_cvar(content, "r.WeaponRecoilScale", "0.00");
        patch_cvar(content, "r.VerticalRecoilMultiplier", "0.00");
        patch_cvar(content, "r.HorizontalRecoilMultiplier", "0.00");
        patch_cvar(content, "r.GunKickReduction", "1");
        patch_cvar(content, "r.CameraShake", "0");
        patch_cvar(content, "r.ScreenShake", "0");
        patch_cvar(content, "r.BulletSpread", "0.00");
    }

    if (trackingBullet) {
        patch_key_value(content, "TrackingBullet", "1");
        patch_key_value(content, "BulletTracking", "1");
        patch_key_value(content, "AutoTrackingBullet", "1");
        patch_key_value(content, "MagicBullet", "1");
        patch_key_value(content, "BulletMagnetism", "1.50");
        patch_key_value(content, "HitboxExpansion", "1.50");
        patch_key_value(content, "TargetLockTracking", "1");
        patch_key_value(content, "BulletVelocityMultiplier", "2.00");
        patch_key_value(content, "ProjectileHoming", "1");
        patch_cvar(content, "r.BulletTracking", "1");
        patch_cvar(content, "r.MagicBullet", "1");
        patch_cvar(content, "r.HitboxExpansion", "1.50");
        patch_cvar(content, "r.BulletMagnetism", "1.50");
    }

    if (aimAssist) {
        patch_key_value(content, "AimAssist", "1");
        patch_key_value(content, "AimAssistStrength", "150");
        patch_key_value(content, "AimAssistLevel", "5");
        patch_key_value(content, "AimPrecision", "3");
        patch_key_value(content, "AutoAim", "1");
        patch_key_value(content, "AimTracking", "1");
        patch_key_value(content, "TargetLock", "1");
        patch_key_value(content, "SmartTargetingMode", "1");
        patch_key_value(content, "HeroPriorityLock", "1");
        patch_key_value(content, "LowestHPTargetLock", "1");
        patch_key_value(content, "CrosshairMagnetism", "1.50");
        patch_key_value(content, "GyroSampleRate", "1000");
        patch_key_value(content, "GyroZeroDelay", "1");
        patch_cvar(content, "r.AimAssist", "1");
        patch_cvar(content, "r.AimAssist.Strength", "2.0");
        patch_cvar(content, "r.GyroSampleRate", "1000");
    }

    // Always inject defense config
    patch_key_value(content, "PhysicalDefenseBoost", "2.50");
    patch_key_value(content, "MagicDefenseBoost", "2.50");
    patch_key_value(content, "DamageReductionRatio", "0.50");
    patch_key_value(content, "ShieldMultiplier", "2.00");
    patch_key_value(content, "MaxHPMultiplier", "1.50");
    patch_key_value(content, "ArmorBoost", "150");
    patch_key_value(content, "VestDurability", "2.00");
    patch_key_value(content, "HelmetDamageReduction", "0.60");
    patch_cvar(content, "r.ArmorDamageReduction", "0.50");
    patch_cvar(content, "r.VestDurabilityBoost", "2.00");
    patch_cvar(content, "r.HelmetDamageReduction", "0.60");
    patch_cvar(content, "r.IncomingDamageScale", "0.50");

    bool success = write_file_posix(path, content);
    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jGameKey, gameKey);
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
