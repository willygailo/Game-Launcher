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
    PERFORMANCE("Performance", 165, "Max FPS, lower graphics quality"),
    BALANCED("Balanced", 90, "Balance of FPS and graphics"),
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
        Triple("com.moonton.mobilelegends", "Mobile Legends: Bang Bang", "MOBA"),
        Triple("com.moonton.mobilelegends.indonesia", "Mobile Legends IN", "MOBA"),
        Triple("com.vng.mlbbvn", "Mobile Legends VN", "MOBA"),
        Triple("com.activision.callofduty.shooter", "Call of Duty: Mobile", "FPS"),
        Triple("com.tencent.ig", "PUBG Mobile", "Battle Royale"),
        Triple("com.pubg.imobile", "PUBG Mobile India", "Battle Royale"),
        Triple("com.pubg.krmobile", "PUBG Mobile Korea", "Battle Royale"),
        Triple("com.vng.pubgmobile", "PUBG Mobile VN", "Battle Royale"),
        Triple("com.tencent.tmgp.pubgmhd", "PUBG Mobile HD", "Battle Royale"),
        Triple("com.dts.freefireth", "Free Fire", "Battle Royale"),
        Triple("com.dts.freefiremax", "Free Fire MAX", "Battle Royale"),
        Triple("com.garena.game.freefire", "Free Fire (Old)", "Battle Royale"),
        Triple("com.garena.game.freefirees", "Free Fire MAX ES", "Battle Royale"),
        Triple("com.garena.game.freefirebd", "Free Fire BD", "Battle Royale"),
        Triple("com.HoYoverse.genshinimpact", "Genshin Impact", "RPG"),
        Triple("com.miHoYo.GenshinImpact", "Genshin Impact (Old)", "RPG"),
        Triple("com.miHoYo.HonkaiStarRail", "Honkai: Star Rail", "RPG"),
        Triple("com.HoYoverse.hkrpgoversea", "Honkai: Star Rail Global", "RPG"),
        Triple("com.nexon.bluearchiveglobal", "Blue Archive", "RPG"),
        Triple("com.YoStarEN.Arknights", "Arknights", "Strategy"),
        Triple("com.riotgames.league.wildrift", "League of Legends: Wild Rift", "MOBA"),
        Triple("com.riotgames.legendsofruneterra", "Legends of Runeterra", "Card"),
        Triple("com.riotgames.teamfighttactics", "Teamfight Tactics", "Strategy"),
        Triple("com.supercell.clashofclans", "Clash of Clans", "Strategy"),
        Triple("com.supercell.clashroyale", "Clash Royale", "Strategy"),
        Triple("com.supercell.brawlstars", "Brawl Stars", "Action"),
        Triple("com.supercell.hayday", "Hay Day", "Casual"),
        Triple("com.tencent.tmgp.sgame", "Honor of Kings", "MOBA"),
        Triple("com.levelinfinite.sgameGlobal", "Honor of Kings Global", "MOBA"),
        Triple("com.netease.lztgglobal", "NARAKA: BLADEPOINT", "Battle Royale"),
        Triple("com.ea.game.pvzfree_row", "Plants vs Zombies", "Strategy"),
        Triple("com.ea.games.r3_row", "Real Racing 3", "Racing"),
        Triple("com.gameloft.android.ANMP.GloftA9HM", "Asphalt 9: Legends", "Racing"),
        Triple("com.ea.gp.apexlegendsmobilefps", "Apex Legends Mobile", "FPS"),
        Triple("com.kiloo.subwaysurf", "Subway Surfers", "Casual"),
        Triple("com.square_enix.android_googleplay.ffbeww", "Final Fantasy Brave Exvius", "RPG"),
        Triple("jp.konami.pesam", "eFootball (PES)", "Sports"),
        Triple("com.ea.gp.fifamobile", "EA FC Mobile", "Sports"),
        Triple("com.netease.mrzhna", "Life After", "Survival"),
        Triple("com.proximabeta.nikke", "GODDESS OF VICTORY: NIKKE", "RPG"),
        Triple("com.nexon.maplem", "MapleStory M", "RPG"),
        Triple("com.bandainamcoent.dblegends_ww", "Dragon Ball Legends", "Fighting"),
        Triple("com.nianticlabs.pokemongo", "Pokemon GO", "AR"),
        Triple("com.roblox.client", "Roblox", "Platform"),
        Triple("com.king.candycrushsaga", "Candy Crush Saga", "Casual"),
        Triple("com.playrix.gardenscapes", "Gardenscapes", "Casual"),
        Triple("com.playdigious.deadcells.mobile", "Dead Cells", "Action"),
        Triple("com.innersloth.spacemafia", "Among Us", "Casual"),
        Triple("com.mojang.minecraftpe", "Minecraft", "Sandbox"),
        Triple("com.epicgames.fortnite", "Fortnite", "Battle Royale"),
        Triple("com.netease.onmyoji", "Onmyoji", "RPG"),
        Triple("com.axlebolt.standoff2", "Standoff 2", "FPS"),
        Triple("com.dena.a12026418", "Pokemon Masters EX", "RPG"),
        Triple("com.lilithgames.hgame.gp", "AFK Arena", "RPG"),
        Triple("com.lilithgames.roc.gp", "Rise of Kingdoms", "Strategy"),
        Triple("com.scopely.monopolygo", "MONOPOLY GO!", "Board"),
        Triple("com.ea.gp.nbamobile", "NBA LIVE Mobile", "Sports"),
        Triple("com.garena.game.codm", "Call of Duty Mobile Garena", "FPS"),
        Triple("com.tencent.lolm", "League of Legends: Wild Rift (CN)", "MOBA"),
        Triple("com.YoStarJP.BlueArchive", "Blue Archive JP", "RPG"),
        Triple("com.nexon.kart", "KartRider Rush+", "Racing"),
        Triple("com.naturalmotion.customstreetracer2", "CSR Racing 2", "Racing"),
    )
}