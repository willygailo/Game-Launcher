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

static bool make_parent_dirs(const std::string& path) {
    size_t pos = path.rfind('/');
    if (pos == std::string::npos || pos == 0) return true;
    std::string dir = path.substr(0, pos);
    std::string current;
    if (dir[0] == '/') current = "/";
    std::stringstream ss(dir);
    std::string segment;
    while (std::getline(ss, segment, '/')) {
        if (segment.empty()) continue;
        current += segment + "/";
        mkdir(current.c_str(), 0777);
        chmod(current.c_str(), 0777);
    }
    return true;
}

static bool write_file_posix(const std::string& path, const std::string& content) {
    make_parent_dirs(path);
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
        {"CriticalDamage", "1000"},
        {"CriticalDamageRate", ssCrit.str()},
        {"CriticalDamageMultiplier", ssHead.str()},
        {"PenetrationBoost", "1000"},
        {"ArmorPenetration", "1000"},
        {"PhysicalPenetrationBoost", "1000"},
        {"MagicPenetrationBoost", "1000"},
        {"MagicResistPenetration", "1000"},
        {"HighDamageRateMode", "1"},
        {"AttackSpeedMultiplier", "10.00"},
        {"AttackSpeedBoost", "10.00"},
        {"ReloadSpeedMultiplier", "10.00"},
        {"FireRateMultiplier", "10.00"},
        {"MovementSpeedMultiplier", "10.00"},
        {"SprintSpeedMultiplier", "10.00"},
        {"SprintSensitivity", "500"},
        {"AgilityMultiplier", "10.00"},
        {"SkillDamageMultiplier", ssMult.str()},
        {"DamageAssetOverride", "1"},
        {"AutoDamageExecutionMode", "1"},
        {"AutoSmiteExecution", "1"},
        {"RetributionDamageThreshold", "99999"},
        {"SmiteTrueDamage", "99999"},
        {"ExecuteThreshold", "99999"},
        {"TurretDamageReduction", "0.01"},
        {"MinionDamageBoost", "100.00"},
        {"MonsterDamageBoost", ssMult.str()},
        {"HitboxExpansion", "10.00"},
        {"BulletVelocityMultiplier", "50.00"},
        {"BulletVelocityScale", "50.00"},
        {"BodyDamageMultiplier", "10.00"},
        {"LimbDamageMultiplier", "10.00"},
        {"ExplosiveDamageMultiplier", "10.00"},
        {"AimAssist", "1"},
        {"AimAssistStrength", "1000"},
        {"AimAssistLevel", "10"},
        {"AimAssistRadius", "1000"},
        {"AimPrecision", "10"},
        {"AutoAim", "1"},
        {"AimTracking", "1"},
        {"TargetLock", "1"},
        {"TargetLockSensitivity", "1000"},
        {"SmartTargetingMode", "1"},
        {"HeroPriorityLock", "1"},
        {"LowestHPTargetLock", "1"},
        {"CrosshairMagnetism", "100.00"},
        {"AimSnapStrength", "100.00"},
        {"AimMagnetism", "100.00"},
        {"ScopeAimAssist", "1"},
        {"RedDotAimAssist", "1"},
        {"AllHeroDamageMultiplier", "10.00"},
        {"TankDamageMultiplier", "10.00"},
        {"FighterDamageMultiplier", "10.00"},
        {"AssassinDamageMultiplier", "10.00"},
        {"MageDamageMultiplier", "10.00"},
        {"MarksmanDamageMultiplier", "10.00"},
        {"SupportDamageMultiplier", "10.00"}
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

        // Unreal Engine 4/5 CVars — 1000% Ultra Overdrive Damage
        patch_cvar(content, "r.DamageMultiplier", ssMult.str());
        patch_cvar(content, "r.BulletDamageScale", ssMult.str());
        patch_cvar(content, "r.HeadshotMultiplier", ssHead.str());
        patch_cvar(content, "r.WeaponDamageScale", ssMult.str());
        patch_cvar(content, "r.CriticalHitRate", "1.00");
        patch_cvar(content, "r.HitboxExpansion", "10.00");
        patch_cvar(content, "r.BulletVelocityScale", "50.00");
        patch_cvar(content, "r.PenetrationPower", "50.00");
        patch_cvar(content, "r.BodyDamageMultiplier", "10.00");
        patch_cvar(content, "r.LimbDamageMultiplier", "10.00");
        patch_cvar(content, "r.ExplosiveDamageMultiplier", "10.00");
        patch_cvar(content, "r.MovementSpeedMultiplier", "10.00");
        patch_cvar(content, "r.SprintSpeedMultiplier", "10.00");
        patch_cvar(content, "r.AttackSpeedMultiplier", "10.00");
        patch_cvar(content, "r.AimAssist", "1");
        patch_cvar(content, "r.AimAssist.Strength", "10.00");
        patch_cvar(content, "r.AimAssistRadius", "800");
        patch_cvar(content, "r.CrosshairMagnetism", "10.00");
        patch_cvar(content, "r.TargetLockSensitivity", "500");
        patch_cvar(content, "r.GyroSampleRate", "1000");
        patch_cvar(content, "r.GyroSensitivityRatio", "10.00");
        patch_cvar(content, "r.GyroZeroDelay", "1");
        patch_cvar(content, "r.GyroStabilization", "1");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectHeroDamage1000
  (JNIEnv *env, jclass, jstring jPath, jfloat damageMultiplier, jfloat headshotMultiplier, jint critRate, jint penetration) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssMult, ssHead, ssCrit, ssPen;
    ssMult << damageMultiplier;
    ssHead << headshotMultiplier;
    ssCrit << critRate;
    ssPen << penetration;

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"DamageMultiplier", ssMult.str()},
        {"PhysicalDamageBoost", ssMult.str()},
        {"MagicDamageBoost", ssMult.str()},
        {"TrueDamageBoost", ssMult.str()},
        {"BulletDamageBoost", ssMult.str()},
        {"DamageBoostRatio", ssMult.str()},
        {"SkillDamageMultiplier", ssMult.str()},
        {"HeroDamageMultiplier", ssMult.str()},
        {"AllHeroDamageMultiplier", "10.00"},
        {"TankDamageMultiplier", "10.00"},
        {"FighterDamageMultiplier", "10.00"},
        {"AssassinDamageMultiplier", "10.00"},
        {"MageDamageMultiplier", "10.00"},
        {"MarksmanDamageMultiplier", "10.00"},
        {"SupportDamageMultiplier", "10.00"},
        {"HeadshotMultiplier", ssHead.str()},
        {"HeadshotDamageMultiplier", ssHead.str()},
        {"CriticalHitRate", ssCrit.str()},
        {"CriticalDamage", "1000"},
        {"CriticalDamageRate", ssCrit.str()},
        {"CriticalDamageMultiplier", "10.00"},
        {"PenetrationBoost", ssPen.str()},
        {"ArmorPenetration", ssPen.str()},
        {"PhysicalPenetrationBoost", ssPen.str()},
        {"MagicPenetrationBoost", ssPen.str()},
        {"MagicResistPenetration", ssPen.str()},
        {"HighDamageRateMode", "1"},
        {"DamageAssetOverride", "1"},
        {"AutoDamageExecutionMode", "1"},
        {"AutoSmiteExecution", "1"},
        {"RetributionDamageThreshold", "99999"},
        {"SmiteTrueDamage", "99999"},
        {"ExecuteThreshold", "99999"},
        {"AttackSpeedMultiplier", "10.00"},
        {"AttackSpeedBoost", "10.00"},
        {"MovementSpeedMultiplier", "10.00"},
        {"SprintSpeedMultiplier", "10.00"},
        {"CooldownReductionBoost", "0.80"},
        {"SkillCoolDownReduceMode", "1"},
        {"HitboxExpansion", "10.00"},
        {"MinionDamageBoost", "100.00"},
        {"MonsterDamageBoost", ssMult.str()},
        {"TurretDamageReduction", "0.01"}
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
        if (content.find("[HeroDamage1000]") == std::string::npos) {
            content += "\n[HeroDamage1000]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }
        patch_cvar(content, "r.DamageMultiplier", ssMult.str());
        patch_cvar(content, "r.HeroDamageMultiplier", "10.00");
        patch_cvar(content, "r.PhysicalDamageScale", ssMult.str());
        patch_cvar(content, "r.MagicDamageScale", ssMult.str());
        patch_cvar(content, "r.TrueDamageScale", ssMult.str());
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
        {"RecoilReduction", "1.00"},
        {"WeaponStability", ssStab.str()},
        {"ScreenShake", "0"},
        {"CameraShake", "0"},
        {"NoCameraShake", "1"},
        {"GunKick", "0"},
        {"GunKickReduction", "1.00"},
        {"WeaponKickReduction", "1.00"},
        {"AllGunsRecoilReduction", "1.00"},
        {"ScopeShakeReduction", "1.00"},
        {"ScopeRecoilMultiplier", "0.00"},
        {"ScopeStability", "5.00"},
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
        {"ZeroInputDelay", "1"},
        {"IronSightRecoil", "0.00"},
        {"RedDotRecoil", "0.00"},
        {"HoloRecoil", "0.00"},
        {"Scope2xRecoil", "0.00"},
        {"Scope3xRecoil", "0.00"},
        {"Scope4xRecoil", "0.00"},
        {"Scope6xRecoil", "0.00"},
        {"Scope8xRecoil", "0.00"},
        {"CantedSightRecoil", "0.00"},
        {"ThermalScopeRecoil", "0.00"},
        {"SniperScopeRecoil", "0.00"},
        {"ARRecoilReduction", "1.00"},
        {"DMRRecoilReduction", "1.00"},
        {"SniperRecoilReduction", "1.00"},
        {"SMGRecoilReduction", "1.00"},
        {"LMGRecoilReduction", "1.00"},
        {"ShotgunRecoilReduction", "1.00"},
        {"PistolRecoilReduction", "1.00"}
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

        // Unreal Engine 4/5 CVars — Full Zero Recoil
        patch_cvar(content, "r.WeaponRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.VerticalRecoilMultiplier", ssRecoil.str());
        patch_cvar(content, "r.HorizontalRecoilMultiplier", ssRecoil.str());
        patch_cvar(content, "r.GunKickReduction", "1");
        patch_cvar(content, "r.CameraShake", "0");
        patch_cvar(content, "r.ScreenShake", "0");
        patch_cvar(content, "r.WeaponSway", "0");
        patch_cvar(content, "r.BulletSpread", "0.00");
        patch_cvar(content, "r.CrosshairSpread", "0.00");
        patch_cvar(content, "r.ScopeStability", "5.00");
        patch_cvar(content, "r.FirstBulletAccuracy", "1");
        patch_cvar(content, "r.AimPunchReduction", "1");
        patch_cvar(content, "r.FlinchReduction", "1");
        patch_cvar(content, "r.WeaponKick", "0.00");
        patch_cvar(content, "r.ViewKick", "0.00");
        patch_cvar(content, "r.RedDotRecoilScale", "0.00");
        patch_cvar(content, "r.HoloRecoilScale", "0.00");
        patch_cvar(content, "r.Scope2xRecoilScale", "0.00");
        patch_cvar(content, "r.Scope3xRecoilScale", "0.00");
        patch_cvar(content, "r.Scope4xRecoilScale", "0.00");
        patch_cvar(content, "r.Scope6xRecoilScale", "0.00");
        patch_cvar(content, "r.Scope8xRecoilScale", "0.00");
        patch_cvar(content, "r.CantedSightRecoilScale", "0.00");
        patch_cvar(content, "r.IronSightRecoilScale", "0.00");
        patch_cvar(content, "r.ARRecoilScale", "0.00");
        patch_cvar(content, "r.DMRRecoilScale", "0.00");
        patch_cvar(content, "r.SniperRecoilScale", "0.00");
        patch_cvar(content, "r.SMGRecoilScale", "0.00");
        patch_cvar(content, "r.LMGRecoilScale", "0.00");
        patch_cvar(content, "r.ShotgunRecoilScale", "0.00");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectScopeZeroRecoil
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
        {"ScopeRecoilZero", "1"},
        {"AllScopesZeroRecoil", "1"},
        {"IronSightRecoil", ssRecoil.str()},
        {"RedDotRecoil", ssRecoil.str()},
        {"HoloRecoil", ssRecoil.str()},
        {"Scope2xRecoil", ssRecoil.str()},
        {"Scope3xRecoil", ssRecoil.str()},
        {"Scope4xRecoil", ssRecoil.str()},
        {"Scope6xRecoil", ssRecoil.str()},
        {"Scope8xRecoil", ssRecoil.str()},
        {"CantedSightRecoil", ssRecoil.str()},
        {"ThermalScopeRecoil", ssRecoil.str()},
        {"SniperScopeRecoil", ssRecoil.str()},
        {"ScopeShakeReduction", "1.00"},
        {"ScopeStability", "5.00"},
        {"ScopeRecoilMultiplier", ssRecoil.str()},
        {"AllGunsRecoilReduction", "1.00"},
        {"ARRecoilScale", ssRecoil.str()},
        {"DMRRecoilScale", ssRecoil.str()},
        {"SniperRecoilScale", ssRecoil.str()},
        {"SMGRecoilScale", ssRecoil.str()},
        {"LMGRecoilScale", ssRecoil.str()},
        {"ShotgunRecoilScale", ssRecoil.str()},
        {"PistolRecoilScale", ssRecoil.str()},
        {"WeaponStability", ssStab.str()}
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
        if (content.find("[ScopeRecoilControl]") == std::string::npos) {
            content += "\n[ScopeRecoilControl]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }
        patch_cvar(content, "r.RedDotRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.HoloRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.Scope2xRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.Scope3xRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.Scope4xRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.Scope6xRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.Scope8xRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.CantedSightRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.IronSightRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.ARRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.DMRRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.SniperRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.SMGRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.LMGRecoilScale", ssRecoil.str());
        patch_cvar(content, "r.ShotgunRecoilScale", ssRecoil.str());
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
    ssStr << (strength > 0 ? strength : 1000);
    ssPrec << (precision > 0 ? precision : 10);

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"AimAssist", "1"},
        {"AimAssistStrength", ssStr.str()},
        {"AimAssistLevel", "10"},
        {"AimPrecision", ssPrec.str()},
        {"AutoAim", "1"},
        {"AimTracking", "1"},
        {"TargetLock", "1"},
        {"TargetLockSensitivity", "1000"},
        {"SmartTargetingMode", "1"},
        {"HeroPriorityLock", "1"},
        {"LowestHPTargetLock", "1"},
        {"AimAssistRadius", "1000"},
        {"CrosshairMagnetism", "100.00"},
        {"ScopeAimAssist", "1"},
        {"RedDotAimAssist", "1"},
        {"SniperAimAssist", "1"},
        {"AimSnapStrength", "100.00"},
        {"AimMagnetism", "100.00"},
        {"AimLead", "1"},
        {"AimLeadStrength", "100.00"},
        {"GyroSampleRate", "1000"},
        {"GyroZeroDelay", "1"},
        {"GyroSensitivityRatio", "10.00"},
        {"GyroStabilization", "1"},
        {"GyroSmoothFactor", "1"},
        {"GyroLatencyMode", "0"},
        {"GyroAimAssist", "1"}
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

        // Unreal Engine 4/5 CVars — 1000% Aim Assist
        patch_cvar(content, "r.AimAssist", "1");
        patch_cvar(content, "r.AimAssist.Strength", "100.00");
        patch_cvar(content, "r.AimAssist.Magnetism", "100.00");
        patch_cvar(content, "r.AimAssist.SnapSpeed", "100.00");
        patch_cvar(content, "r.AimAssistRadius", "1000");
        patch_cvar(content, "r.CrosshairMagnetism", "100.00");
        patch_cvar(content, "r.TargetLockSensitivity", "1000");
        patch_cvar(content, "r.AimSnapStrength", "100.00");
        patch_cvar(content, "r.AimLead", "1");
        patch_cvar(content, "r.AimLeadStrength", "100.00");
        patch_cvar(content, "r.GyroSampleRate", "1000");
        patch_cvar(content, "r.GyroZeroDelay", "1");
        patch_cvar(content, "r.GyroSensitivityRatio", "10.00");
        patch_cvar(content, "r.GyroStabilization", "1");
        patch_cvar(content, "r.GyroAimAssist", "1");
        patch_cvar(content, "r.SniperAimAssist", "1");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist1000
  (JNIEnv *env, jclass clazz, jstring jPath, jint strength, jfloat precision) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectAimAssist(env, clazz, jPath, strength, (jint)precision);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet
  (JNIEnv *env, jclass, jstring jPath, jfloat trackingStrength, jfloat hitboxMultiplier) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssTrack, ssHitbox;
    ssTrack << (trackingStrength > 0.0f ? trackingStrength : 100.00f);
    ssHitbox << (hitboxMultiplier > 0.0f ? hitboxMultiplier : 50.00f);

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
        {"BulletCurveFactor", "50.00"},
        {"BulletVelocityMultiplier", "100.00"},
        {"BulletSpread", "0.00"},
        {"CrosshairMagnetism", "100.00"},
        {"FirstBulletAccuracy", "1"},
        {"ProjectileHoming", "1"},
        {"HomingStrength", "100.00"}
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

        // Unreal Engine 4/5 CVars — 1000% Tracking Bullet
        patch_cvar(content, "r.BulletTracking", "1");
        patch_cvar(content, "r.MagicBullet", "1");
        patch_cvar(content, "r.HitboxExpansion", ssHitbox.str());
        patch_cvar(content, "r.BulletMagnetism", ssTrack.str());
        patch_cvar(content, "r.BulletVelocityScale", "100.00");
        patch_cvar(content, "r.BulletCurveFactor", "50.00");
        patch_cvar(content, "r.ProjectileHoming", "1");
        patch_cvar(content, "r.HomingStrength", "100.00");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet1000
  (JNIEnv *env, jclass clazz, jstring jPath, jfloat trackingStrength, jfloat hitboxMultiplier) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectTrackingBullet(env, clazz, jPath, trackingStrength, hitboxMultiplier);
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef
  (JNIEnv *env, jclass, jstring jPath, jfloat defBoost, jfloat dmgReduction) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssDef, ssRed;
    ssDef << (defBoost > 0.0f ? defBoost : 100.00f);
    ssRed << (dmgReduction > 0.0f ? dmgReduction : 0.999f);

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"PhysicalDefenseBoost", ssDef.str()},
        {"MagicDefenseBoost", ssDef.str()},
        {"PhysicalDefenseMultiplier", ssDef.str()},
        {"MagicDefenseMultiplier", ssDef.str()},
        {"DamageReductionRatio", ssRed.str()},
        {"DamageReduction", ssRed.str()},
        {"IncomingDamageReduction", ssRed.str()},
        {"ShieldMultiplier", "100.00"},
        {"ShieldCapacity", "100.00"},
        {"ShieldStrength", "100.00"},
        {"MaxHPMultiplier", "50.00"},
        {"HPBoostRatio", "50.00"},
        {"DamageAbsorbRatio", "50.00"},
        {"ArmorBoost", "10000"},
        {"MagicResistBoost", "10000"},
        {"VestDurability", "100.00"},
        {"VestDurabilityBoost", "100.00"},
        {"HelmetDamageReduction", "0.999"},
        {"TenacityRatio", "0.999"},
        {"ResilienceLevel", "10"},
        {"ArmorLevel", "10"},
        {"DamageResistance", ssRed.str()},
        {"ShieldEfficiency", "100.00"},
        {"ShieldPointsMultiplier", "100.00"},
        {"ArmorPlateEfficiency", "100.00"},
        {"KineticArmorBoost", "100.00"},
        {"FlakJacketRatio", "0.999"},
        {"HealthRegenDelay", "0.00"},
        {"HealthRegenBoost", "100.00"},
        {"HealthRegenRate", "100.00"},
        {"FallDamageReduction", "1.00"},
        {"ExplosionResistance", "0.999"},
        {"HeadshotDamageReduction", "0.999"},
        {"HighDamageMitigationRatio", "10.00"},
        {"HeavyHitAbsorption", "10.00"},
        {"BurstDamageReduction", "10.00"}
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

        // Unreal Engine 4/5 CVars — 1000% Defense Tier
        patch_cvar(content, "r.ArmorDamageReduction", ssRed.str());
        patch_cvar(content, "r.VestDurabilityBoost", "100.00");
        patch_cvar(content, "r.HelmetDamageReduction", "0.999");
        patch_cvar(content, "r.IncomingDamageScale", "0.001");
        patch_cvar(content, "r.ShieldEfficiency", "100.00");
        patch_cvar(content, "r.DamageResistance", ssRed.str());
        patch_cvar(content, "r.TenacityRatio", "0.999");
        patch_cvar(content, "r.HealthRegenBoost", "100.00");
        patch_cvar(content, "r.FallDamageReduction", "1.00");
        patch_cvar(content, "r.ExplosionResistance", "0.999");
        patch_cvar(content, "r.HeadshotDamageReduction", "0.999");
        patch_cvar(content, "r.HeavyDamageDampener", "10.00");
        patch_cvar(content, "r.BurstDamageReduction", "10.00");
        patch_cvar(content, "r.HighDamageMitigationRatio", "10.00");
        patch_cvar(content, "r.MaxHPMultiplier", "50.00");
        patch_cvar(content, "r.ShieldMultiplier", "100.00");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef1000
  (JNIEnv *env, jclass clazz, jstring jPath, jfloat defBoost, jfloat dmgReduction) {
    return Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectArmorDef(env, clazz, jPath, defBoost, dmgReduction);
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
        {"SprintSensitivity", "400"},
        {"AgilityMultiplier", ssSpeed.str()},
        {"AttackSpeedMultiplier", "13.50"},
        {"AttackSpeedBoost", "13.50"},
        {"ReloadSpeedMultiplier", "13.50"},
        {"FireRateMultiplier", "10.00"},
        {"BulletVelocityMultiplier", "25.00"},
        {"BulletVelocityScale", "25.00"},
        {"ThrottleResponse", "4.50"},
        {"AccelerationMultiplier", "4.50"},
        {"TopSpeedBoost", "3.50"},
        {"TouchPollingRate", "1000"},
        {"TouchZeroDelay", "1"},
        {"ZeroInputLag", "1"},
        {"HighSpeedMovement", "1"},
        {"SwimSpeedMultiplier", "13.50"},
        {"ClimbSpeedMultiplier", "13.50"},
        {"VehicleSpeedMultiplier", "4.50"}
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

        // Unreal Engine 4/5 CVars — 450% Ultra Speed
        patch_cvar(content, "r.MovementSpeedMultiplier", ssSpeed.str());
        patch_cvar(content, "r.SprintSpeedMultiplier", ssSprint.str());
        patch_cvar(content, "r.AttackSpeedMultiplier", "13.50");
        patch_cvar(content, "r.ReloadSpeedMultiplier", "13.50");
        patch_cvar(content, "r.FireRateMultiplier", "10.00");
        patch_cvar(content, "r.BulletVelocityScale", "25.00");
        patch_cvar(content, "r.ZeroInputLag", "1");
        patch_cvar(content, "r.AccelerationMultiplier", "4.50");
        patch_cvar(content, "r.VehicleSpeedMultiplier", "4.50");
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
        patch_key_value(content, "DamageMultiplier", "100.00");
        patch_key_value(content, "PhysicalDamageBoost", "100.00");
        patch_key_value(content, "MagicDamageBoost", "100.00");
        patch_key_value(content, "TrueDamageBoost", "100.00");
        patch_key_value(content, "BulletDamageBoost", "100.00");
        patch_key_value(content, "HeadshotDamageMultiplier", "100.00");
        patch_key_value(content, "CriticalHitRate", "100");
        patch_key_value(content, "CriticalDamage", "1000");
        patch_key_value(content, "CriticalDamageRate", "100");
        patch_key_value(content, "CriticalDamageMultiplier", "10.00");
        patch_key_value(content, "PenetrationBoost", "1000");
        patch_key_value(content, "ArmorPenetration", "1000");
        patch_key_value(content, "PhysicalPenetrationBoost", "1000");
        patch_key_value(content, "MagicPenetrationBoost", "1000");
        patch_key_value(content, "MagicResistPenetration", "1000");
        patch_key_value(content, "SkillDamageMultiplier", "100.00");
        patch_key_value(content, "HeroDamageMultiplier", "10.00");
        patch_key_value(content, "AllHeroDamageMultiplier", "10.00");
        patch_key_value(content, "TankDamageMultiplier", "10.00");
        patch_key_value(content, "FighterDamageMultiplier", "10.00");
        patch_key_value(content, "AssassinDamageMultiplier", "10.00");
        patch_key_value(content, "MageDamageMultiplier", "10.00");
        patch_key_value(content, "MarksmanDamageMultiplier", "10.00");
        patch_key_value(content, "SupportDamageMultiplier", "10.00");
        patch_key_value(content, "HitboxExpansion", "10.00");
        patch_key_value(content, "BulletVelocityMultiplier", "50.00");
        patch_key_value(content, "SmiteTrueDamage", "99999");
        patch_key_value(content, "RetributionDamageThreshold", "99999");
        patch_key_value(content, "ExecuteThreshold", "99999");
        patch_key_value(content, "AutoDamageExecutionMode", "1");
        patch_key_value(content, "AutoSmiteExecution", "1");
        patch_key_value(content, "AimAssist", "1");
        patch_key_value(content, "AimAssistStrength", "1000");
        patch_key_value(content, "AimAssistLevel", "10");
        patch_key_value(content, "AimPrecision", "10");
        patch_key_value(content, "TargetLockSensitivity", "1000");
        patch_key_value(content, "AimAssistRadius", "1000");
        patch_key_value(content, "CrosshairMagnetism", "100.00");
        patch_key_value(content, "AimSnapStrength", "100.00");
        patch_key_value(content, "AimMagnetism", "100.00");
        patch_cvar(content, "r.DamageMultiplier", "100.00");
        patch_cvar(content, "r.BulletDamageScale", "100.00");
        patch_cvar(content, "r.HeadshotMultiplier", "100.00");
        patch_cvar(content, "r.WeaponDamageScale", "100.00");
        patch_cvar(content, "r.PhysicalDamageScale", "100.00");
        patch_cvar(content, "r.MagicDamageScale", "100.00");
        patch_cvar(content, "r.TrueDamageScale", "100.00");
        patch_cvar(content, "r.PenetrationPower", "50.00");
        patch_cvar(content, "r.HitboxExpansion", "10.00");
        patch_cvar(content, "r.BulletVelocityScale", "50.00");
        patch_cvar(content, "r.AimAssist", "1");
        patch_cvar(content, "r.AimAssist.Strength", "100.00");
        patch_cvar(content, "r.AimAssistRadius", "1000");
        patch_cvar(content, "r.CrosshairMagnetism", "100.00");
        patch_cvar(content, "r.TargetLockSensitivity", "1000");
    }

    if (noRecoil) {
        patch_key_value(content, "RecoilControl", "1");
        patch_key_value(content, "ZeroRecoil", "1");
        patch_key_value(content, "NoRecoil", "1");
        patch_key_value(content, "RecoilScale", "0.00");
        patch_key_value(content, "VerticalRecoil", "0.00");
        patch_key_value(content, "HorizontalRecoil", "0.00");
        patch_key_value(content, "VerticalRecoilScale", "0.00");
        patch_key_value(content, "HorizontalRecoilScale", "0.00");
        patch_key_value(content, "RecoilReduction", "1.00");
        patch_key_value(content, "WeaponStability", "500");
        patch_key_value(content, "ScreenShake", "0");
        patch_key_value(content, "CameraShake", "0");
        patch_key_value(content, "NoCameraShake", "1");
        patch_key_value(content, "GunKick", "0");
        patch_key_value(content, "GunKickReduction", "1.00");
        patch_key_value(content, "WeaponKickReduction", "1.00");
        patch_key_value(content, "AllGunsRecoilReduction", "1.00");
        patch_key_value(content, "ScopeShakeReduction", "1.00");
        patch_key_value(content, "ScopeRecoilMultiplier", "0.00");
        patch_key_value(content, "ScopeStability", "5.00");
        patch_key_value(content, "BulletSpread", "0.00");
        patch_key_value(content, "CrosshairSpread", "0.00");
        patch_key_value(content, "SpreadScale", "0.00");
        patch_key_value(content, "BulletSpreadReduction", "1");
        patch_key_value(content, "FirstBulletAccuracy", "1");
        patch_key_value(content, "WeaponSway", "0");
        patch_key_value(content, "IronSightRecoil", "0.00");
        patch_key_value(content, "RedDotRecoil", "0.00");
        patch_key_value(content, "HoloRecoil", "0.00");
        patch_key_value(content, "Scope2xRecoil", "0.00");
        patch_key_value(content, "Scope3xRecoil", "0.00");
        patch_key_value(content, "Scope4xRecoil", "0.00");
        patch_key_value(content, "Scope6xRecoil", "0.00");
        patch_key_value(content, "Scope8xRecoil", "0.00");
        patch_key_value(content, "CantedSightRecoil", "0.00");
        patch_key_value(content, "ThermalScopeRecoil", "0.00");
        patch_key_value(content, "SniperScopeRecoil", "0.00");
        patch_key_value(content, "ARRecoilReduction", "1.00");
        patch_key_value(content, "DMRRecoilReduction", "1.00");
        patch_key_value(content, "SniperRecoilReduction", "1.00");
        patch_key_value(content, "SMGRecoilReduction", "1.00");
        patch_key_value(content, "LMGRecoilReduction", "1.00");
        patch_key_value(content, "ShotgunRecoilReduction", "1.00");
        patch_cvar(content, "r.WeaponRecoilScale", "0.00");
        patch_cvar(content, "r.VerticalRecoilMultiplier", "0.00");
        patch_cvar(content, "r.HorizontalRecoilMultiplier", "0.00");
        patch_cvar(content, "r.GunKickReduction", "1");
        patch_cvar(content, "r.CameraShake", "0");
        patch_cvar(content, "r.ScreenShake", "0");
        patch_cvar(content, "r.WeaponSway", "0");
        patch_cvar(content, "r.BulletSpread", "0.00");
        patch_cvar(content, "r.CrosshairSpread", "0.00");
        patch_cvar(content, "r.ScopeStability", "5.00");
        patch_cvar(content, "r.RedDotRecoilScale", "0.00");
        patch_cvar(content, "r.HoloRecoilScale", "0.00");
        patch_cvar(content, "r.Scope2xRecoilScale", "0.00");
        patch_cvar(content, "r.Scope3xRecoilScale", "0.00");
        patch_cvar(content, "r.Scope4xRecoilScale", "0.00");
        patch_cvar(content, "r.Scope6xRecoilScale", "0.00");
        patch_cvar(content, "r.Scope8xRecoilScale", "0.00");
        patch_cvar(content, "r.CantedSightRecoilScale", "0.00");
        patch_cvar(content, "r.IronSightRecoilScale", "0.00");
        patch_cvar(content, "r.ARRecoilScale", "0.00");
        patch_cvar(content, "r.DMRRecoilScale", "0.00");
        patch_cvar(content, "r.SniperRecoilScale", "0.00");
        patch_cvar(content, "r.SMGRecoilScale", "0.00");
        patch_cvar(content, "r.LMGRecoilScale", "0.00");
        patch_cvar(content, "r.ShotgunRecoilScale", "0.00");
    }

    if (trackingBullet) {
        patch_key_value(content, "TrackingBullet", "1");
        patch_key_value(content, "BulletTracking", "1");
        patch_key_value(content, "AutoTrackingBullet", "1");
        patch_key_value(content, "MagicBullet", "1");
        patch_key_value(content, "BulletMagnetism", "100.00");
        patch_key_value(content, "HitboxExpansion", "50.00");
        patch_key_value(content, "TargetLockTracking", "1");
        patch_key_value(content, "BulletCurveFactor", "50.00");
        patch_key_value(content, "BulletVelocityMultiplier", "100.00");
        patch_key_value(content, "BulletSpread", "0.00");
        patch_key_value(content, "CrosshairMagnetism", "100.00");
        patch_key_value(content, "ProjectileHoming", "1");
        patch_key_value(content, "HomingStrength", "100.00");
        patch_cvar(content, "r.BulletTracking", "1");
        patch_cvar(content, "r.MagicBullet", "1");
        patch_cvar(content, "r.HitboxExpansion", "50.00");
        patch_cvar(content, "r.BulletMagnetism", "100.00");
        patch_cvar(content, "r.BulletVelocityScale", "100.00");
        patch_cvar(content, "r.BulletCurveFactor", "50.00");
        patch_cvar(content, "r.ProjectileHoming", "1");
        patch_cvar(content, "r.HomingStrength", "100.00");
    }

    if (aimAssist) {
        patch_key_value(content, "AimAssist", "1");
        patch_key_value(content, "AimAssistStrength", "1000");
        patch_key_value(content, "AimAssistLevel", "10");
        patch_key_value(content, "AimPrecision", "10");
        patch_key_value(content, "AutoAim", "1");
        patch_key_value(content, "AimTracking", "1");
        patch_key_value(content, "TargetLock", "1");
        patch_key_value(content, "TargetLockSensitivity", "1000");
        patch_key_value(content, "SmartTargetingMode", "1");
        patch_key_value(content, "HeroPriorityLock", "1");
        patch_key_value(content, "LowestHPTargetLock", "1");
        patch_key_value(content, "AimAssistRadius", "1000");
        patch_key_value(content, "CrosshairMagnetism", "100.00");
        patch_key_value(content, "AimSnapStrength", "100.00");
        patch_key_value(content, "AimMagnetism", "100.00");
        patch_key_value(content, "AimLead", "1");
        patch_key_value(content, "AimLeadStrength", "100.00");
        patch_key_value(content, "GyroSampleRate", "1000");
        patch_key_value(content, "GyroZeroDelay", "1");
        patch_key_value(content, "GyroSensitivityRatio", "10.00");
        patch_key_value(content, "GyroStabilization", "1");
        patch_cvar(content, "r.AimAssist", "1");
        patch_cvar(content, "r.AimAssist.Strength", "100.00");
        patch_cvar(content, "r.AimAssist.Magnetism", "100.00");
        patch_cvar(content, "r.AimAssist.SnapSpeed", "100.00");
        patch_cvar(content, "r.AimAssistRadius", "1000");
        patch_cvar(content, "r.CrosshairMagnetism", "100.00");
        patch_cvar(content, "r.TargetLockSensitivity", "1000");
        patch_cvar(content, "r.AimSnapStrength", "100.00");
        patch_cvar(content, "r.AimLeadStrength", "100.00");
        patch_cvar(content, "r.GyroSampleRate", "1000");
        patch_cvar(content, "r.GyroZeroDelay", "1");
        patch_cvar(content, "r.GyroSensitivityRatio", "10.00");
        patch_cvar(content, "r.GyroAimAssist", "1");
    }

    // Always inject 1000% defense config
    patch_key_value(content, "PhysicalDefenseBoost", "100.00");
    patch_key_value(content, "MagicDefenseBoost", "100.00");
    patch_key_value(content, "PhysicalDefenseMultiplier", "100.00");
    patch_key_value(content, "MagicDefenseMultiplier", "100.00");
    patch_key_value(content, "DamageReductionRatio", "0.999");
    patch_key_value(content, "DamageReduction", "0.999");
    patch_key_value(content, "IncomingDamageReduction", "0.999");
    patch_key_value(content, "ShieldMultiplier", "100.00");
    patch_key_value(content, "ShieldCapacity", "100.00");
    patch_key_value(content, "ShieldStrength", "100.00");
    patch_key_value(content, "MaxHPMultiplier", "50.00");
    patch_key_value(content, "HPBoostRatio", "50.00");
    patch_key_value(content, "DamageAbsorbRatio", "50.00");
    patch_key_value(content, "HealthRegenBoost", "100.00");
    patch_key_value(content, "HealthRegenRate", "100.00");
    patch_key_value(content, "ArmorBoost", "10000");
    patch_key_value(content, "MagicResistBoost", "10000");
    patch_key_value(content, "VestDurability", "100.00");
    patch_key_value(content, "VestDurabilityBoost", "100.00");
    patch_key_value(content, "HelmetDamageReduction", "0.999");
    patch_key_value(content, "ExplosionResistance", "0.999");
    patch_key_value(content, "FallDamageReduction", "1.00");
    patch_key_value(content, "HighDamageMitigationRatio", "10.00");
    patch_key_value(content, "HeavyHitAbsorption", "10.00");
    patch_key_value(content, "BurstDamageReduction", "10.00");
    patch_key_value(content, "HeadshotDamageReduction", "0.999");
    patch_cvar(content, "r.ArmorDamageReduction", "0.999");
    patch_cvar(content, "r.VestDurabilityBoost", "100.00");
    patch_cvar(content, "r.HelmetDamageReduction", "0.999");
    patch_cvar(content, "r.IncomingDamageScale", "0.001");
    patch_cvar(content, "r.ShieldMultiplier", "100.00");
    patch_cvar(content, "r.ShieldEfficiency", "100.00");
    patch_cvar(content, "r.MaxHPMultiplier", "50.00");
    patch_cvar(content, "r.HealthRegenBoost", "100.00");
    patch_cvar(content, "r.HeavyDamageDampener", "10.00");
    patch_cvar(content, "r.BurstDamageReduction", "10.00");
    patch_cvar(content, "r.HighDamageMitigationRatio", "10.00");
    patch_cvar(content, "r.ExplosionResistance", "0.999");
    patch_cvar(content, "r.HeadshotDamageReduction", "0.999");

    bool success = write_file_posix(path, content);
    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jGameKey, gameKey);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectFastCooldown
  (JNIEnv *env, jclass, jstring jPath, jfloat cdrRatio) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssCdr;
    ssCdr << (cdrRatio > 0.0f ? cdrRatio : 0.99f);

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"SkillCoolDownReduceMode", "1"},
        {"CooldownReductionBoost", ssCdr.str()},
        {"CooldownReduction", ssCdr.str()},
        {"SkillCooldownMultiplier", "0.01"},
        {"UltimateCooldownReduction", ssCdr.str()},
        {"PassiveCooldownReduction", ssCdr.str()},
        {"SpellCooldownReduction", ssCdr.str()},
        {"SkillAnimationCancelZeroDelay", "1"},
        {"SkillResponseZeroDelay", "1"},
        {"SkillCastZeroDelay", "1"},
        {"InstantSkillRelease", "1"},
        {"NoCastDelay", "1"},
        {"AttackSpeedMultiplier", "25.00"},
        {"AttackSpeedBoost", "25.00"},
        {"AttackDelayReduction", "1"},
        {"EnergyRegenRate", "100.00"},
        {"ManaRegenRate", "100.00"},
        {"UnlimitedEnergy", "1"},
        {"UnlimitedMana", "1"},
        {"NoManaCost", "1"},
        {"NoEnergyCost", "1"}
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
        if (content.find("[FastCooldown]") == std::string::npos) {
            content += "\n[FastCooldown]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }
        patch_cvar(content, "r.CooldownReduction", ssCdr.str());
        patch_cvar(content, "r.SkillResponseZeroDelay", "1");
        patch_cvar(content, "r.InstantCast", "1");
        patch_cvar(content, "r.AttackSpeedMultiplier", "25.00");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectShield1500
  (JNIEnv *env, jclass, jstring jPath, jfloat shieldMultiplier, jfloat defBoost) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssShield, ssDef;
    ssShield << (shieldMultiplier > 0.0f ? shieldMultiplier : 1500.00f);
    ssDef << (defBoost > 0.0f ? defBoost : 1000.00f);

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"ShieldMultiplier", ssShield.str()},
        {"ShieldCapacity", ssShield.str()},
        {"ShieldStrength", ssShield.str()},
        {"ShieldEfficiency", ssShield.str()},
        {"ShieldPointsMultiplier", ssShield.str()},
        {"PhysicalDefenseBoost", ssDef.str()},
        {"MagicDefenseBoost", ssDef.str()},
        {"PhysicalDefenseMultiplier", ssDef.str()},
        {"MagicDefenseMultiplier", ssDef.str()},
        {"DamageReductionRatio", "0.9999"},
        {"DamageReduction", "0.9999"},
        {"IncomingDamageReduction", "0.9999"},
        {"DamageResistance", "0.9999"},
        {"ArmorBoost", "50000"},
        {"MagicResistBoost", "50000"},
        {"MaxHPMultiplier", "100.00"},
        {"HPBoostRatio", "100.00"},
        {"DamageAbsorbRatio", "100.00"},
        {"VestDurability", "1000.00"},
        {"VestDurabilityBoost", "1000.00"},
        {"HelmetDamageReduction", "0.9999"},
        {"TenacityRatio", "0.9999"},
        {"ResilienceLevel", "10"},
        {"ArmorLevel", "10"},
        {"HealthRegenDelay", "0.00"},
        {"HealthRegenBoost", "1000.00"},
        {"HealthRegenRate", "1000.00"},
        {"FallDamageReduction", "1.00"},
        {"ExplosionResistance", "0.9999"},
        {"HeadshotDamageReduction", "0.9999"},
        {"HighDamageMitigationRatio", "100.00"},
        {"HeavyHitAbsorption", "100.00"},
        {"BurstDamageReduction", "100.00"}
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
        if (content.find("[DefenseShield1500]") == std::string::npos) {
            content += "\n[DefenseShield1500]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }
        patch_cvar(content, "r.ArmorDamageReduction", "0.9999");
        patch_cvar(content, "r.ShieldMultiplier", ssShield.str());
        patch_cvar(content, "r.ShieldEfficiency", ssShield.str());
        patch_cvar(content, "r.MaxHPMultiplier", "100.00");
        patch_cvar(content, "r.HealthRegenBoost", "1000.00");
        patch_cvar(content, "r.HeavyDamageDampener", "100.00");
        patch_cvar(content, "r.BurstDamageReduction", "100.00");
        patch_cvar(content, "r.HighDamageMitigationRatio", "100.00");
    }

    bool success = write_file_posix(pathStr, content);
    env->ReleaseStringUTFChars(jPath, path);
    return success ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeInjectDroneView
  (JNIEnv *env, jclass, jstring jPath, jint fov, jint height) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string pathStr(path);
    std::string content = read_file_posix(pathStr);

    std::ostringstream ssFov, ssHeight;
    ssFov << (fov > 0 ? fov : 180);
    ssHeight << (height > 0 ? height : 4);

    bool isXml = (pathStr.rfind(".xml") != std::string::npos || content.find("<map>") != std::string::npos);
    bool isJson = (pathStr.rfind(".json") != std::string::npos || (!content.empty() && content.front() == '{'));

    std::vector<std::pair<std::string, std::string>> keys = {
        {"DroneView", "1"},
        {"DroneViewHeight", ssHeight.str()},
        {"CameraHeight", ssHeight.str()},
        {"CameraDistance", ssFov.str()},
        {"CameraFOV", ssFov.str()},
        {"FieldOfView", ssFov.str()},
        {"WideScreenMode", "1"},
        {"UltraWideCamera", "1"},
        {"MapOverviewScale", "2.0"}
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
        if (content.find("[DroneViewUltra]") == std::string::npos) {
            content += "\n[DroneViewUltra]\n";
        }
        for (const auto& kv : keys) {
            patch_key_value(content, kv.first, kv.second);
        }
        patch_cvar(content, "r.CameraFOV", ssFov.str());
        patch_cvar(content, "r.DroneViewHeight", ssHeight.str());
        patch_cvar(content, "r.FieldOfView", ssFov.str());
    }

    bool success = write_file_posix(pathStr, content);
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

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativePreserveFileTimestamps
  (JNIEnv *env, jclass, jstring jPath, jlong atimeSec, jlong mtimeSec) {
    if (!jPath) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);

    struct timespec times[2];
    times[0].tv_sec = static_cast<time_t>(atimeSec);
    times[0].tv_nsec = 0;
    times[1].tv_sec = static_cast<time_t>(mtimeSec);
    times[1].tv_nsec = 0;

    int res = utimensat(AT_FDCWD, path, times, 0);
    env->ReleaseStringUTFChars(jPath, path);
    return (res == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeStealthWrite
  (JNIEnv *env, jclass, jstring jPath, jstring jContent) {
    if (!jPath || !jContent) return JNI_FALSE;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    const char *content = env->GetStringUTFChars(jContent, nullptr);
    jsize len = env->GetStringUTFLength(jContent);

    struct stat st;
    bool hadStat = (stat(path, &st) == 0);

    make_parent_dirs(path);
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, hadStat ? st.st_mode : 0660);
    if (fd < 0) {
        env->ReleaseStringUTFChars(jPath, path);
        env->ReleaseStringUTFChars(jContent, content);
        return JNI_FALSE;
    }

    ssize_t written = write(fd, content, len);
    fchmod(fd, hadStat ? st.st_mode : 0660);
    fsync(fd);
    close(fd);

    if (hadStat) {
        struct timespec times[2];
        times[0].tv_sec = st.st_atime;
        times[0].tv_nsec = 0;
        times[1].tv_sec = st.st_mtime;
        times[1].tv_nsec = 0;
        utimensat(AT_FDCWD, path, times, 0);
    }

    env->ReleaseStringUTFChars(jPath, path);
    env->ReleaseStringUTFChars(jContent, content);
    return (written == len) ? JNI_TRUE : JNI_FALSE;
}

static uint32_t calculate_crc32(const unsigned char *data, size_t length) {
    uint32_t crc = 0xFFFFFFFF;
    for (size_t i = 0; i < length; ++i) {
        crc ^= data[i];
        for (int j = 0; j < 8; ++j) {
            crc = (crc >> 1) ^ (0xEDB88320 & -(crc & 1));
        }
    }
    return ~crc;
}

JNIEXPORT jlong JNICALL Java_com_gamebooster_app_config_NativeConfigInjector_nativeCalculateConfigCrc32
  (JNIEnv *env, jclass, jstring jPath) {
    if (!jPath) return 0;
    const char *path = env->GetStringUTFChars(jPath, nullptr);
    std::string content = read_file_posix(path);
    env->ReleaseStringUTFChars(jPath, path);
    if (content.empty()) return 0;
    return static_cast<jlong>(calculate_crc32(reinterpret_cast<const unsigned char*>(content.data()), content.size()));
}
