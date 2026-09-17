#ifndef GAMEBOOSTER_NATIVE_LUA_ENGINE_H
#define GAMEBOOSTER_NATIVE_LUA_ENGINE_H

#include <jni.h>
#include <string>
#include <unordered_map>
#include <vector>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Native C++ Lua Script Engine for Game Booster
 *
 * Fast offline parser and evaluator for Game Booster Lua profiles in assets/lua.
 * Extracts key-value pairs from Lua table definitions (e.g., profile.key = value)
 * and executes atomic config injection directly to game configuration files.
 */

/*
 * Class:     com_gamebooster_app_engine_lua_LuaConfigEngine
 * Method:    nativeParseLuaProfile
 * Signature: (Ljava/lang/String;)Ljava/util/Map;
 */
JNIEXPORT jobject JNICALL Java_com_gamebooster_app_engine_lua_LuaConfigEngine_nativeParseLuaProfile
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_gamebooster_app_engine_lua_LuaConfigEngine
 * Method:    nativeInjectLuaDirect
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_gamebooster_app_engine_lua_LuaConfigEngine_nativeInjectLuaDirect
  (JNIEnv *, jclass, jstring, jstring);

#ifdef __cplusplus
}
#endif

namespace gamebooster {
namespace lua {

struct LuaProfile {
    std::string game_id;
    std::string package_name;
    int target_fps = 120;
    std::string graphics_tier = "ULTRA";
    bool force_vulkan = true;
    int touch_boost_hz = 1000;
    std::string cpu_governor = "performance";
    std::unordered_map<std::string, std::string> properties;
};

/**
 * Parses raw Lua script text content into a structured LuaProfile.
 */
LuaProfile parseLuaScript(const std::string& scriptContent);

/**
 * Injects parsed Lua properties directly into target game config path.
 */
bool injectLuaProfileToPath(const std::string& targetPath, const LuaProfile& profile);

} // namespace lua
} // namespace gamebooster

#endif // GAMEBOOSTER_NATIVE_LUA_ENGINE_H
