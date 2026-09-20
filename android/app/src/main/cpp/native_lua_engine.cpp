#include "native_lua_engine.h"
#include "config_common.h"
#include "native_config_injector.h"

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

bool injectLuaProfileToPath(const std::string& targetPath, const LuaProfile& profile) {
    if (targetPath.empty() || profile.properties.empty()) return false;

    std::vector<std::pair<std::string, std::string>> keys;
    keys.reserve(profile.properties.size());

    for (const auto& kv : profile.properties) {
        keys.emplace_back(kv.first, kv.second);
    }

    // Use apply_keys_to_file from config_common
    bool ok = apply_keys_to_file(targetPath, targetPath.c_str(), keys, "NativeLuaProfile");
    LOGI("Native Lua injection to [%s] -> %s (keys: %zu)", targetPath.c_str(), ok ? "SUCCESS" : "FAILED", keys.size());
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
