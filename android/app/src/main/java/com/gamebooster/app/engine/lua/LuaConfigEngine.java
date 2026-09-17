package com.gamebooster.app.engine.lua;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * LuaConfigEngine — High-performance Native C++ and Lua script interpreter bridge.
 *
 * 100% Offline: Reads Lua profile scripts from local APK assets (assets/lua/).
 * Delegates directly to Native C++ (libgamebooster_native.so) for native parsing
 * and atomic config injection.
 */
public final class LuaConfigEngine {

    private static final String TAG = "LuaConfigEngine";

    static {
        try {
            System.loadLibrary("gamebooster_native");
        } catch (Throwable t) {
            Log.w(TAG, "Native library load deferred: " + t.getMessage());
        }
    }

    private LuaConfigEngine() {}

    /**
     * Native C++ parser entry point: parses raw Lua script string into a Java Map.
     */
    public static native Map<String, String> nativeParseLuaProfile(String scriptContent);

    /**
     * Native C++ direct injector: parses Lua and immediately writes to target file via POSIX C++.
     */
    public static native boolean nativeInjectLuaDirect(String scriptContent, String targetFilePath);

    /**
     * Loads and evaluates a local Lua config script from assets/lua/<scriptName>.
     * Uses high-speed Native C++ parser backed by fallback Java tokenizer.
     */
    public static GameOptimizationProfile loadGameProfile(Context context, String scriptFileName) {
        if (context == null || scriptFileName == null) return null;

        Map<String, String> props = new HashMap<>();
        try {
            String assetPath = scriptFileName.startsWith("lua/") ? scriptFileName : "lua/" + scriptFileName;
            StringBuilder sb = new StringBuilder();
            try (InputStream is = context.getAssets().open(assetPath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }

            String scriptContent = sb.toString();

            // First attempt: Native C++ fast-path parser
            try {
                Map<String, String> nativeMap = nativeParseLuaProfile(scriptContent);
                if (nativeMap != null && !nativeMap.isEmpty()) {
                    props.putAll(nativeMap);
                }
            } catch (Throwable t) {
                Log.d(TAG, "Native Lua parse fallback: " + t.getMessage());
            }

            // Fallback tokenizer if native library wasn't loaded
            if (props.isEmpty()) {
                String[] lines = scriptContent.split("\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--") && trimmed.contains("=")) {
                        int eqIdx = trimmed.indexOf('=');
                        if (eqIdx > 0) {
                            String key = trimmed.substring(0, eqIdx).trim();
                            if (key.startsWith("profile.")) key = key.substring(8).trim();
                            if (key.startsWith("local ")) key = key.substring(6).trim();

                            String val = trimmed.substring(eqIdx + 1).trim();
                            if (val.endsWith(";")) val = val.substring(0, val.length() - 1).trim();
                            if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                                val = val.substring(1, val.length() - 1);
                            }
                            props.put(key, val);
                        }
                    }
                }
            }

            String defaultGameId = scriptFileName.endsWith(".lua")
                    ? scriptFileName.substring(0, scriptFileName.length() - 4)
                    : scriptFileName;

            String gameId = props.containsKey("game_id") ? props.get("game_id") : defaultGameId;
            String pkg = props.containsKey("package_name") ? props.get("package_name") : "";
            int fps = parseInt(props.get("target_fps"), 120);
            String gfx = props.containsKey("graphics_tier") ? props.get("graphics_tier") : "ULTRA";
            boolean vulkan = parseBool(props.get("force_vulkan"), true);
            int touch = parseInt(props.get("touch_boost_hz"), 1000);
            String governor = props.containsKey("cpu_governor") ? props.get("cpu_governor") : "performance";

            Log.i(TAG, "Loaded local Lua profile: game=" + gameId + ", fps=" + fps + ", gfx=" + gfx + ", pkg=" + pkg);
            return new GameOptimizationProfile(gameId, pkg, fps, gfx, vulkan, touch, governor, props);

        } catch (Exception e) {
            Log.w(TAG, "Failed to load Lua profile '" + scriptFileName + "': " + e.getMessage());
            return null;
        }
    }

    private static int parseInt(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception ignored) {
            return defaultVal;
        }
    }

    private static boolean parseBool(String val, boolean defaultVal) {
        if (val == null) return defaultVal;
        return "true".equalsIgnoreCase(val.trim()) || "1".equals(val.trim());
    }
}
