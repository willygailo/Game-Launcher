package com.gamelauncher.core

import android.app.GameManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Full Android 13+ (API 33) GameManager API integration.
 *
 * The existing code only uses GameManager.setGameState(). This class adds:
 *  - getGameMode(): read current mode (STANDARD / PERFORMANCE / BATTERY)
 *  - setGameMode(PERFORMANCE): request performance mode for a package
 *  - getGameModeInfo(): check OEM allows the override (Android 14+ / API 34)
 *
 * OEM support matrix (as of 2025):
 *  - Samsung One UI 6+: ✅ full support
 *  - Xiaomi HyperOS:    ✅ PERFORMANCE mode honored
 *  - OPPO/Realme:       ✅ ColorOS 14+
 *  - OnePlus:           ✅ OxygenOS 14+
 *  - Google Pixel:      ✅ Android 13+
 */
@Singleton
class AndroidGameModeApiManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val GAME_MODE_STANDARD    = 1
        const val GAME_MODE_PERFORMANCE = 2
        const val GAME_MODE_BATTERY     = 3
    }

    data class GameModeResult(
        val packageName: String,
        val currentMode: Int,
        val performanceModeSupported: Boolean,
        val batteryModeSupported: Boolean,
        val set: Boolean = false
    )

    /**
     * Query + set performance mode for a package.
     * On Android 14+, checks OEM support before attempting to set.
     * Safe to call on Android 13 (API 33) — falls back gracefully on older APIs.
     *
     * @return null if GameManager is unavailable (Android < 13)
     */
    suspend fun enablePerformanceMode(packageName: String): GameModeResult? =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@withContext null

            val gm = try {
                context.getSystemService(GameManager::class.java) ?: return@withContext null
            } catch (_: Exception) { return@withContext null }

            // Current mode
            val currentMode = try {
                val method = gm.javaClass.getMethod("getGameMode", String::class.java)
                method.invoke(gm, packageName) as? Int ?: GAME_MODE_STANDARD
            } catch (_: Exception) { GAME_MODE_STANDARD }

            // Android 14+: check OEM capability before setting
            var performanceSupported = true
            var batterySupported = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                try {
                    val method = gm.javaClass.getMethod("getGameModeInfo", String::class.java)
                    val info = method.invoke(gm, packageName)
                    if (info != null) {
                        val availableModesMethod = info.javaClass.getMethod("getAvailableGameModes")
                        val modes = availableModesMethod.invoke(info) as? IntArray
                        if (modes != null) {
                            performanceSupported = modes.contains(GAME_MODE_PERFORMANCE)
                            batterySupported = modes.contains(GAME_MODE_BATTERY)
                        }
                    }
                } catch (_: Exception) {}
            }

            // Attempt to set PERFORMANCE mode if supported
            var setSuccess = false
            if (performanceSupported && currentMode != GAME_MODE_PERFORMANCE) {
                try {
                    val method = gm.javaClass.getMethod("setGameMode", String::class.java, Int::class.java)
                    method.invoke(gm, packageName, GAME_MODE_PERFORMANCE)
                    setSuccess = true
                } catch (_: Exception) {}
            }

            GameModeResult(
                packageName = packageName,
                currentMode = currentMode,
                performanceModeSupported = performanceSupported,
                batteryModeSupported = batterySupported,
                set = setSuccess
            )
        }

    /**
     * Restore standard game mode when the session ends.
     */
    suspend fun restoreStandardMode(packageName: String): Boolean =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@withContext false
            try {
                val gm = context.getSystemService(GameManager::class.java) ?: return@withContext false
                val method = gm.javaClass.getMethod("setGameMode", String::class.java, Int::class.java)
                method.invoke(gm, packageName, GAME_MODE_STANDARD)
                true
            } catch (_: Exception) { false }
        }

    /**
     * Get raw game mode for diagnostic display.
     */
    fun getGameModeLabel(mode: Int): String = when (mode) {
        GAME_MODE_STANDARD    -> "Standard"
        GAME_MODE_PERFORMANCE -> "Performance"
        GAME_MODE_BATTERY     -> "Battery Saver"
        else                  -> "Unknown ($mode)"
    }
}
