package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;

import com.gamebooster.app.engine.lua.GameOptimizationProfile;
import com.gamebooster.app.engine.lua.LuaConfigEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LuaProfileBridge — Unifies offline Lua profiles in assets/lua with NativeConfigInjector
 * and GameAutoInjectDispatcher.
 *
 * 100% offline, zero-latency bridge between Lua scripts and native atomic POSIX file injectors.
 */
public class LuaProfileBridge {

    private static final String TAG = "LuaProfileBridge";

    /**
     * Maps package name or game identifier to corresponding offline Lua profile asset.
     */
    public static String resolveLuaScriptName(String pkg) {
        if (pkg == null) return null;
        String lower = pkg.toLowerCase();
        if (lower.contains("mobile.legends")) return "mlbb_boost.lua";
        if (lower.contains("callofduty") || lower.contains("activision")) return "codm_boost.lua";
        if (lower.contains("tencent.ig") || lower.contains("pubg") || lower.contains("vng") || lower.contains("bgmi")) return "pubgm_boost.lua";
        if (lower.contains("freefire") || lower.contains("dts")) return "freefire_boost.lua";
        if (lower.contains("bloodstrike") || lower.contains("netease")) return "bloodstrike_boost.lua";
        if (lower.contains("deltaforce") || lower.contains("dfm")) return "deltaforce_boost.lua";
        if (lower.contains("arenabreakout") || lower.contains("uamo")) return "arenabreakout_boost.lua";
        if (lower.contains("sgame") || lower.contains("hok")) return "hok_boost.lua";
        if (lower.contains("wildrift") || lower.contains("league")) return "wildrift_boost.lua";
        if (lower.contains("genshin") || lower.contains("mihoyo")) return "genshin_boost.lua";
        if (lower.contains("carx") || lower.contains("sr")) return "carx_boost.lua";
        if (lower.contains("farlight")) return "farlight_boost.lua";
        if (lower.contains("roblox")) return "roblox_boost.lua";
        if (lower.contains("standoff") || lower.contains("axlebolt")) return "standoff2_boost.lua";
        if (lower.contains("valorant")) return "valorant_boost.lua";
        return null;
    }

    /**
     * Loads the Lua profile for the package, converts keys/values, and applies them
     * to the resolved configuration file path using NativeConfigInjector.
     */
    public static boolean applyProfileForPackage(Context context, String pkg) {
        if (pkg == null) return false;
        if (context == null) {
            context = ConfigBackupManager.getAppContext();
            if (context == null) {
                try {
                    Class<?> atClass = Class.forName("android.app.ActivityThread");
                    java.lang.reflect.Method m = atClass.getMethod("currentApplication");
                    context = (Context) m.invoke(null);
                } catch (Throwable ignored) {}
            }
        }
        if (context == null) {
            Log.w(TAG, "Cannot apply Lua profile: context is null and could not be resolved for " + pkg);
            return false;
        }

        String scriptName = resolveLuaScriptName(pkg);
        if (scriptName == null) {
            Log.d(TAG, "No dedicated Lua profile mapped for " + pkg);
            return false;
        }

        try {
            GameOptimizationProfile profile = LuaConfigEngine.loadGameProfile(context, scriptName);
            if (profile == null) {
                Log.w(TAG, "Failed to load Lua profile: " + scriptName);
                return false;
            }

            Map<String, String> rawProps = profile.getRawProperties();
            if (rawProps == null || rawProps.isEmpty()) {
                Log.d(TAG, "Lua profile had no raw properties: " + scriptName);
                return false;
            }

            // Resolve target config path for the package
            List<String> paths = GameConfigPathResolver.getPathsForGame(pkg);
            if (paths == null || paths.isEmpty()) {
                Log.d(TAG, "No config paths found for " + pkg);
                return false;
            }

            List<String> keyList = new ArrayList<>();
            List<String> valList = new ArrayList<>();
            List<String> kvList = new ArrayList<>();
            for (Map.Entry<String, String> entry : rawProps.entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if (k != null && !k.isEmpty() && v != null) {
                    keyList.add(k);
                    valList.add(v);
                    kvList.add(k + "=" + v);
                }
            }

            String[] keys = keyList.toArray(new String[0]);
            String[] values = valList.toArray(new String[0]);
            String[] kvs = kvList.toArray(new String[0]);

            boolean injectedAny = false;
            for (String targetPath : paths) {
                if (targetPath != null && !targetPath.isEmpty()) {
                    boolean ok = NativeConfigInjector.injectLuaProperties(targetPath, keys, values);
                    if (!ok) {
                        ok = ConfigFileHelper.patchKeys(targetPath, kvs, "[LuaProfile]");
                    }
                    if (ok) injectedAny = true;
                }
            }

            // If Lua script specifies Drone View / iPad FOV, link and trigger hardware viewport virtualization
            if ("true".equalsIgnoreCase(rawProps.get("ultra_drone_view"))
                    || "true".equalsIgnoreCase(rawProps.get("ipad_view_fov"))
                    || "1".equals(rawProps.get("drone_view"))
                    || "1".equals(rawProps.get("r_ipad_view_fov"))
                    || "1".equals(rawProps.get("r_drone_view"))) {
                try {
                    com.gamebooster.app.engine.ResolutionScalerEngine.applyDroneViewForGame(context, pkg);
                } catch (Throwable ignored) {}
            }

            Log.i(TAG, "⚡ Applied Lua profile [" + scriptName + "] for " + pkg + " across " + paths.size() + " paths (ok=" + injectedAny + ")");
            return injectedAny;
        } catch (Throwable t) {
            Log.w(TAG, "Error applying Lua profile for " + pkg + ": " + t.getMessage());
            return false;
        }
    }
}
