package com.gamelauncher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameModel(
    @PrimaryKey val packageName: String,
    val name: String,
    val isKnownGame: Boolean = false,
    val customCategory: String = "Other",
    val isBookmarked: Boolean = false,
    val lastLaunched: Long = 0L,
    val totalBoostSessions: Int = 0,
    val highPerformanceMode: Boolean = false,
    val targetFps: Int = 60,
    val targetHz: Float = 60f,
    val killBackgroundOnLaunch: Boolean = true,
    val wifiLockEnabled: Boolean = true,
    val graphicsMode: String = "BALANCED",
    val cpuBoost: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gaming_sessions")
data class GamingSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val gameName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = 0L,
    val durationMs: Long = 0L,
    val avgFps: Int = 0,
    val maxFps: Int = 0,
    val minFps: Int = 0,
    val avgCpuUsage: Float = 0f,
    val avgRamUsage: Long = 0L,
    val batteryDrain: Int = 0,
    val wasBoosted: Boolean = false,
    val graphicsMode: String = "BALANCED"
)

enum class GraphicsMode(val displayName: String, val fpsLimit: Int, val description: String) {
    PERFORMANCE("Performance", 120, "Max FPS, lower graphics quality"),
    BALANCED("Balanced", 60, "Balance of FPS and graphics"),
    BATTERY_SAVER("Battery Saver", 30, "Lower FPS for longer battery life"),
    CUSTOM("Custom", 60, "Custom FPS settings")
}

enum class BoostProfile(val displayName: String, val description: String) {
    MAX_PERFORMANCE("Max Performance", "Highest CPU/GPU, max brightness"),
    GAMING("Gaming", "Optimized for gaming"),
    BALANCED("Balanced", "Normal performance"),
    BATTERY("Battery Saver", "Save battery life")
}

object KnownGames {
    val LIST: List<Triple<String, String, String>> = listOf(
        Triple("com.mobile.legends", "Mobile Legends: Bang Bang", "MOBA"),
        Triple("com.activision.callofduty.shooter", "Call of Duty: Mobile", "FPS"),
        Triple("com.tencent.ig", "PUBG Mobile", "Battle Royale"),
        Triple("com.garena.game.freefire", "Free Fire", "Battle Royale"),
        Triple("com.garena.game.freefirees", "Free Fire MAX", "Battle Royale"),
        Triple("com.miHoYo.GenshinImpact", "Genshin Impact", "RPG"),
        Triple("com.nexon.bluearchiveglobal", "Blue Archive", "RPG"),
        Triple("com.riotgames.league.wildrift", "League of Legends: Wild Rift", "MOBA"),
        Triple("com.supercell.clashofclans", "Clash of Clans", "Strategy"),
        Triple("com.supercell.clashroyale", "Clash Royale", "Strategy"),
        Triple("com.vng.mlbbvn", "Mobile Legends VN", "MOBA"),
        Triple("com.tencent.tmgp.sgame", "Honor of Kings", "MOBA"),
        Triple("com.tencent.tmgp.pubgmhd", "PUBG Mobile HD", "Battle Royale"),
        Triple("com.netease.lztgglobal", "Rules of Survival", "Battle Royale"),
        Triple("com.ea.game.pvzfree_row", "Plants vs Zombies", "Strategy"),
        Triple("com.ea.games.r3_row", "Real Racing 3", "Racing"),
        Triple("com.gameloft.android.ANMP.GloftA9HM", "Asphalt 9: Legends", "Racing"),
        Triple("com.kiloo.subwaysurf", "Subway Surfers", "Casual"),
        Triple("com.square_enix.android_googleplay.ffbeww", "Final Fantasy", "RPG"),
        Triple("jp.konami.pesam", "eFootball", "Sports"),
        Triple("com.ea.gp.fifamobile", "EA FC Mobile", "Sports"),
        Triple("com.dts.freefireth", "Free Fire TH", "Battle Royale"),
        Triple("com.vng.pubgmobile", "PUBG Mobile VN", "Battle Royale"),
        Triple("com.netease.mrzhna", "Life After", "Survival"),
        Triple("com.levelinfinite.sgame", "Honor of Kings Global", "MOBA"),
        Triple("com.proximabeta.mfk", "Standoff 2", "FPS"),
    )
}