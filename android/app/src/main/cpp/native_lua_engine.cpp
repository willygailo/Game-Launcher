#include "native_lua_engine.h"
#include "config_common.h"

#include <android/log.h>
#include <sstream>
#include <algorithm>

namespace gamebooster {
namespace lua {

static inline std::string trim(const std::string& str) {
    size_t first = str.find_first_not_of(" \t\r\n");
    if (first == std::string::npos) return "";
    size_t last = str.find_last_not_of(" \t\r\n");
    return str.substr(first, (last - first + 1));
}

static inline std::string stripQuotesAndSemicolons(const std::string& str) {
    std::string s = trim(str);
    if (s.empty()) return s;
    if (s.back() == ';') {
        s.pop_back();
        s = trim(s);
    }
    if (s.size() >= 2 && ((s.front() == '"' && s.back() == '"') || (s.front() == '\'' && s.back() == '\''))) {
        s = s.substr(1, s.size() - 2);
    }
    return s;
}

LuaProfile parseLuaScript(const std::string& scriptContent) {
    LuaProfile profile;
    std::istringstream stream(scriptContent);
    std::string line;

    while (std::getline(stream, line)) {
        std::string trimmed = trim(line);
        if (trimmed.empty() || trimmed.rfind("--", 0) == 0) {
            continue; // Skip comments and empty lines
        }

        // Check for inline comment '--'
        size_t commentPos = trimmed.find("--");
        if (commentPos != std::string::npos) {
            trimmed = trim(trimmed.substr(0, commentPos));
        }

        size_t eqPos = trimmed.find('=');
        if (eqPos != std::string::npos) {
            std::string key = trim(trimmed.substr(0, eqPos));
            std::string val = stripQuotesAndSemicolons(trimmed.substr(eqPos + 1));

            // Strip prefixes: "profile.", "local "
            if (key.rfind("profile.", 0) == 0) {
                key = key.substr(8);
            } else if (key.rfind("local ", 0) == 0) {
                key = key.substr(6);
            }
            key = trim(key);

            // Fix L1: Skip table-init lines (e.g. "profile = {}" or "profile = { }").
            // After prefix stripping, if the key is empty or the value is a Lua table
            // constructor (starts with '{'), discard — writing "{...}" into a game INI/XML
            // would corrupt the config file.
            if (key.empty() || (!val.empty() && val.front() == '{')) {
                continue;
            }

            if (!key.empty()) {
                profile.properties[key] = val;

                if (key == "game_id") {
                    profile.game_id = val;
                } else if (key == "package_name") {
                    profile.package_name = val;
                } else if (key == "target_fps") {
                    profile.target_fps = (int) std::strtol(val.c_str(), nullptr, 10);
                } else if (key == "graphics_tier") {
                    profile.graphics_tier = val;
                } else if (key == "force_vulkan") {
                    profile.force_vulkan = (val == "true" || val == "1");
                } else if (key == "touch_boost_hz") {
                    profile.touch_boost_hz = (int) std::strtol(val.c_str(), nullptr, 10);
                } else if (key == "cpu_governor") {
                    profile.cpu_governor = val;
                }
            }
        }
    }

    LOGI("Parsed Lua profile [%s] for pkg [%s]: %zu properties extracted (FPS: %d, GFX: %s)",
         profile.game_id.c_str(), profile.package_name.c_str(), profile.properties.size(),
         profile.target_fps, profile.graphics_tier.c_str());

    return profile;
}

static inline std::string toPascalCase(const std::string& snake) {
    std::string result;
    bool capitalize = true;
    for (char c : snake) {
        if (c == '_') {
            capitalize = true;
        } else if (capitalize) {
            result += (char)::toupper(c);
            capitalize = false;
        } else {
            result += c;
        }
    }
    return result;
}

bool injectLuaProfileToPath(const std::string& targetPath, const LuaProfile& profile) {
    if (targetPath.empty() || profile.properties.empty()) return false;

    std::vector<std::pair<std::string, std::string>> keys;
    keys.reserve(profile.properties.size() * 2);

    bool isMlbb = (targetPath.find("mobile.legends") != std::string::npos
                   || profile.game_id == "mlbb"
                   || profile.package_name == "com.mobile.legends");

    bool isPubgm = (targetPath.find("tencent.ig") != std::string::npos
                    || targetPath.find("pubg") != std::string::npos
                    || profile.game_id == "pubgm"
                    || profile.package_name == "com.tencent.ig");

    bool isCodm = (targetPath.find("callofduty") != std::string::npos
                   || targetPath.find("codm") != std::string::npos
                   || profile.game_id == "codm"
                   || profile.package_name == "com.activision.callofduty.shooter");

    for (const auto& kv : profile.properties) {
        keys.emplace_back(kv.first, kv.second);

        // For MLBB and Unity XML PlayerPrefs: auto-emit PascalCase equivalents
        if (isMlbb && kv.first.find('_') != std::string::npos) {
            std::string pascal = toPascalCase(kv.first);
            if (!pascal.empty()) {
                keys.emplace_back(pascal, kv.second);
            }
        }

        // For PUBGM UE4 CVars: auto-emit +CVars=r.* format if key starts with r_ or r.
        if (isPubgm) {
            if (kv.first.rfind("r_", 0) == 0) {
                std::string cvarName = "r." + toPascalCase(kv.first.substr(2));
                keys.emplace_back(cvarName, kv.second);
                keys.emplace_back("+CVars=" + cvarName, kv.second);
            } else if (kv.first.rfind("r.", 0) == 0) {
                keys.emplace_back("+CVars=" + kv.first, kv.second);
            }
        }
    }

    // Special MLBB Ultra Drone View & Combat mappings
    if (isMlbb) {
        auto itDrone = profile.properties.find("ultra_drone_view");
        if (itDrone == profile.properties.end()) itDrone = profile.properties.find("drone_view");
        if (itDrone != profile.properties.end() && (itDrone->second == "true" || itDrone->second == "1")) {
            keys.emplace_back("CameraHeight", "4");
            keys.emplace_back("FOVBoost", "1.75");
            keys.emplace_back("DroneView", "1");
            keys.emplace_back("PanoramicFOV", "1.75");
            keys.emplace_back("DroneFOV", "180");
            keys.emplace_back("MaxFOV", "180");
            keys.emplace_back("FieldOfView", "180");
            keys.emplace_back("CameraDistance", "180");
            keys.emplace_back("WideCameraAngle", "1");
            keys.emplace_back("MapScale", "1.35");
            keys.emplace_back("MapVisibilityRange", "2.0");
        }
        auto itDmg = profile.properties.find("damage_multiplier");
        if (itDmg != profile.properties.end()) {
            keys.emplace_back("DamageLockMax", itDmg->second);
            keys.emplace_back("DamageBoost", itDmg->second);
            keys.emplace_back("TrueDamageBoost", itDmg->second);
        }
        auto itAtk = profile.properties.find("attack_speed_boost");
        if (itAtk != profile.properties.end()) {
            keys.emplace_back("AttackSpeedBoost", itAtk->second);
            keys.emplace_back("AttackSpeedMax", "1");
            keys.emplace_back("BasicAttackRate", "10");
        }
    }

    // Special PUBGM Ultra Drone View (iPad FOV) & Combat mappings
    if (isPubgm) {
        auto itDrone = profile.properties.find("ultra_drone_view");
        if (itDrone == profile.properties.end()) itDrone = profile.properties.find("drone_view");
        if (itDrone == profile.properties.end()) itDrone = profile.properties.find("r_ipad_view");
        if (itDrone != profile.properties.end() && (itDrone->second == "true" || itDrone->second == "1")) {
            keys.emplace_back("r.PUBGCameraFOV", "130");
            keys.emplace_back("r.PUBGCameraDistance", "220");
            keys.emplace_back("r.PUBGIpadView", "1");
            keys.emplace_back("r.IpadView", "1");
            keys.emplace_back("r.ThirdPersonFOV", "130");
            keys.emplace_back("r.ThirdPersonCameraDistance", "220");
            keys.emplace_back("r.WideView", "1");
            keys.emplace_back("r.AspectRatioAxisConstraint", "AspectRatio_MaintainYFOV");
            keys.emplace_back("DroneView", "1");
            keys.emplace_back("DroneFOV", "130");
            keys.emplace_back("IpadView", "1");
        }
        auto itDmg = profile.properties.find("damage_multiplier");
        if (itDmg != profile.properties.end()) {
            keys.emplace_back("r.PUBGDamageLockMax", itDmg->second);
            keys.emplace_back("r.PUBGDamageBoost", itDmg->second);
            keys.emplace_back("DamageLockMax", itDmg->second);
        }
    }

    // Special CODM Ultra Drone View & Combat mappings
    if (isCodm) {
        auto itDrone = profile.properties.find("ultra_drone_view");
        if (itDrone == profile.properties.end()) itDrone = profile.properties.find("drone_view");
        if (itDrone != profile.properties.end() && (itDrone->second == "true" || itDrone->second == "1")) {
            keys.emplace_back("CameraFOV", "120");
            keys.emplace_back("ThirdPersonFOV", "120");
            keys.emplace_back("FirstPersonFOV", "120");
            keys.emplace_back("FPP_FOV", "120");
            keys.emplace_back("TPP_FOV", "120");
            keys.emplace_back("DroneView", "1");
            keys.emplace_back("DroneFOV", "120");
            keys.emplace_back("CameraDistance", "220");
            keys.emplace_back("CameraHeight", "4");
            keys.emplace_back("Camera_Elevation", "4.0");
            keys.emplace_back("FOV_Scale_Float", "1.5");
            keys.emplace_back("iPadView", "1");
        }
        auto itDmg = profile.properties.find("damage_floor_max");
        if (itDmg == profile.properties.end()) itDmg = profile.properties.find("damage_multiplier");
        if (itDmg != profile.properties.end()) {
            keys.emplace_back("DamageLockMax", itDmg->second);
            keys.emplace_back("DamageFloorMax", itDmg->second);
            keys.emplace_back("HeadshotMultiplier", "999");
        }
    }

    // Use apply_keys_to_file from config_common.
    bool ok = apply_keys_to_file(targetPath, targetPath.c_str(), keys, "NativeLuaProfile");
    LOGI("Native Lua injection to [%s] -> %s (keys: %zu)",
         targetPath.c_str(), ok ? "SUCCESS" : "FAILED", keys.size());
    return ok;
}

} // namespace lua
} // namespace gamebooster

extern "C" {

JNIEXPORT jobject JNICALL Java_com_gamebooster_app_engine_lua_LuaConfigEngine_nativeParseLuaProfile
  (JNIEnv *env, jclass, jstring scriptContent) {
    if (!scriptContent) return nullptr;

    const char *scriptCStr = env->GetStringUTFChars(scriptContent, nullptr);
    std::string script(scriptCStr);
    env->ReleaseStringUTFChars(scriptContent, scriptCStr);

    gamebooster::lua::LuaProfile profile = gamebooster::lua::parseLuaScript(script);

    // Create java.util.HashMap
    jclass mapClass = env->FindClass("java/util/HashMap");
    if (!mapClass) return nullptr;
    jmethodID mapInit = env->GetMethodID(mapClass, "<init>", "()V");
    jmethodID mapPut = env->GetMethodID(mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

    jobject hashMap = env->NewObject(mapClass, mapInit);

    for (const auto& kv : profile.properties) {
        jstring kStr = env->NewStringUTF(kv.first.c_str());
        jstring vStr = env->NewStringUTF(kv.second.c_str());
        env->CallObjectMethod(hashMap, mapPut, kStr, vStr);
        env->DeleteLocalRef(kStr);
        env->DeleteLocalRef(vStr);
    }

    return hashMap;
}

JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_engine_lua_LuaConfigEngine_nativeInjectLuaDirect
  (JNIEnv *env, jclass, jstring scriptContent, jstring targetFilePath) {
    if (!scriptContent || !targetFilePath) return JNI_FALSE;

    const char *scriptCStr = env->GetStringUTFChars(scriptContent, nullptr);
    const char *pathCStr = env->GetStringUTFChars(targetFilePath, nullptr);

    std::string script(scriptCStr);
    std::string targetPath(pathCStr);

    env->ReleaseStringUTFChars(scriptContent, scriptCStr);
    env->ReleaseStringUTFChars(targetFilePath, pathCStr);

    gamebooster::lua::LuaProfile profile = gamebooster::lua::parseLuaScript(script);
    bool result = gamebooster::lua::injectLuaProfileToPath(targetPath, profile);

    return result ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
