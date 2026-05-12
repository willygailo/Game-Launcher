package com.gamelauncher.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.gamelauncher.data.model.GameModel
import com.gamelauncher.data.repository.GamesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gamesRepository: GamesRepository
) {
    suspend fun exportProfiles(): Uri = withContext(Dispatchers.IO) {
        val games = gamesRepository.allGames
        val profiles = JSONArray()
        // Collect first batch
        val gameList = mutableListOf<GameModel>()

        val json = JSONObject().apply {
            put("version", 1)
            put("app", "GameLauncher")
            put("exportedAt", System.currentTimeMillis())
            put("profiles", profiles)
        }

        val file = File(context.cacheDir, "game_profiles.json")
        file.writeText(json.toString(2))
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    suspend fun importProfiles(uri: Uri): Int = withContext(Dispatchers.IO) {
        val reader = BufferedReader(InputStreamReader(context.contentResolver.openInputStream(uri)))
        val text = reader.readText()
        reader.close()
        val json = JSONObject(text)
        val profiles = json.getJSONArray("profiles")
        var count = 0
        for (i in 0 until profiles.length()) {
            val profile = profiles.getJSONObject(i)
            val packageName = profile.getString("packageName")
            gamesRepository.getGame(packageName)?.let { existing ->
                gamesRepository.updateGame(existing.copy(
                    highPerformanceMode = profile.optBoolean("highPerformanceMode", existing.highPerformanceMode),
                    targetFps = profile.optInt("targetFps", existing.targetFps),
                    wifiLockEnabled = profile.optBoolean("wifiLockEnabled", existing.wifiLockEnabled),
                    graphicsMode = profile.optString("graphicsMode", existing.graphicsMode),
                    cpuBoost = profile.optBoolean("cpuBoost", existing.cpuBoost),
                    killBackgroundOnLaunch = profile.optBoolean("killBackgroundOnLaunch", existing.killBackgroundOnLaunch)
                ))
                count++
            }
        }
        count
    }
}
