package com.gamespace.app.channels

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GameLibraryChannel(private val context: Context) : MethodChannel.MethodCallHandler {
    companion object {
        const val CHANNEL = "com.gamespace.app/game_library"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getInstalledGames" -> {
                scope.launch {
                    val games = getInstalledGamesList()
                    result.success(games)
                }
            }
            else -> result.notImplemented()
        }
    }

    private fun getInstalledGamesList(): List<Map<String, Any>> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val gamesList = mutableListOf<Map<String, Any>>()

        val knownGamePrefixes = listOf(
            "com.pubg", "com.tencent", "com.dts.freefire", "com.miHoYo",
            "com.epicgames", "com.mojang", "com.supercell", "com.riotgames",
            "com.activision", "com.ea.gp", "com.gameloft", "com.roblox"
        )

        for (app in packages) {
            var isGame = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (app.category == ApplicationInfo.CATEGORY_GAME) {
                    isGame = true
                }
            }
            if ((app.flags and ApplicationInfo.FLAG_IS_GAME) != 0) {
                isGame = true
            }
            if (!isGame) {
                val pkgName = app.packageName.lowercase()
                if (knownGamePrefixes.any { pkgName.contains(it) }) {
                    isGame = true
                }
            }

            if (isGame) {
                val appName = pm.getApplicationLabel(app).toString()
                gamesList.add(
                    mapOf(
                        "packageName" to app.packageName,
                        "appName" to appName,
                        "isGame" to true
                    )
                )
            }
        }
        return gamesList
    }
}
