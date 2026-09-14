package com.gamebooster.app.engine.lua

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * GameOptimizationProfile — Parsed configuration profile for a target game.
 */
data class GameOptimizationProfile(
    val gameId: String,
    val packageName: String,
    val targetFps: Int = 120,
    val graphicsTier: String = "ULTRA",
    val forceVulkan: Boolean = true,
    val touchBoostHz: Int = 1000,
    val cpuGovernor: String = "performance",
    val rawProperties: Map<String, String> = emptyMap()
)

/**
 * LuaConfigEngine — Lightweight local script profile interpreter for game optimization.
 *
 * Requirements addressed:
 *  - 100% Offline: Reads Lua profile scripts from local APK assets (assets/lua/).
 *  - Configurable behavior without requiring internet servers or dynamic code downloads.
 *  - Extracts parameters for PUBG, CODM, and MLBB.
 */
object LuaConfigEngine {

    private const val TAG = "LuaConfigEngine"

    /**
     * Loads and evaluates a local Lua config script from assets/lua/<scriptName>.
     * Uses safe offline key-value parsing to build a GameOptimizationProfile.
     */
    @JvmStatic
    fun loadGameProfile(context: Context, scriptFileName: String): GameOptimizationProfile? {
        val props = mutableMapOf<String, String>()
        try {
            val assetPath = if (scriptFileName.startsWith("lua/")) scriptFileName else "lua/$scriptFileName"
            context.assets.open(assetPath).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("--") && trimmed.contains("=")) {
                            val parts = trimmed.split("=", limit = 2)
                            if (parts.size == 2) {
                                val key = parts[0].trim().removePrefix("profile.").removePrefix("local ")
                                val value = parts[1].trim().trim('"', '\'', ';')
                                props[key] = value
                            }
                        }
                        line = reader.readLine()
                    }
                }
            }

            val gameId = props["game_id"] ?: scriptFileName.removeSuffix(".lua")
            val pkg = props["package_name"] ?: ""
            val fps = props["target_fps"]?.toIntOrNull() ?: 120
            val gfx = props["graphics_tier"] ?: "ULTRA"
            val vulkan = props["force_vulkan"]?.equals("true", ignoreCase = true) ?: true
            val touch = props["touch_boost_hz"]?.toIntOrNull() ?: 1000
            val governor = props["cpu_governor"] ?: "performance"

            Log.i(TAG, "Loaded local Lua profile: game=$gameId, fps=$fps, gfx=$gfx, pkg=$pkg")
            return GameOptimizationProfile(
                gameId = gameId,
                packageName = pkg,
                targetFps = fps,
                graphicsTier = gfx,
                forceVulkan = vulkan,
                touchBoostHz = touch,
                cpuGovernor = governor,
                rawProperties = props
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load Lua profile '$scriptFileName': ${e.message}")
            return null
        }
    }
}
