package com.gamelauncher.ml

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.gamelauncher.services.GameDetectorService
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interpreter: Interpreter? = null
    private val modelName = "game_classifier.tflite"

    init {
        try {
            val assetFileDescriptor = context.assets.openFd(modelName)
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            interpreter = Interpreter(modelBuffer)
        } catch (e: Exception) {
            // Model not found, use fallback heuristic
        }
    }

    fun isGame(packageName: String, packageManager: PackageManager): Boolean {
        // 1. Check hardcoded popular games
        if (isInPopularGamesList(packageName)) return true

        // 2. Check app category
        if (isGameCategory(packageName, packageManager)) return true

        // 3. Keyword matching
        if (hasGameKeywords(packageName)) return true

        // 4. ML-based classification if model exists
        interpreter?.let {
            return classifyWithML(packageName)
        }

        return false
    }

    private fun isInPopularGamesList(packageName: String): Boolean {
        val popularGames = setOf(
            "com.moonton.mobilelegends",
            "com.moonton.mobilelegends.indonesia",
            "com.activision.callofduty.shooter",
            "com.tencent.ig",
            "com.pubg.imobile",
            "com.pubg.krmobile",
            "com.vng.pubgmobile",
            "com.tencent.tmgp.pubgmhd",
            "com.dts.freefireth",
            "com.dts.freefiremax",
            "com.garena.game.freefire",
            "com.HoYoverse.genshinimpact",
            "com.miHoYo.GenshinImpact",
            "com.miHoYo.HonkaiStarRail",
            "com.riotgames.league.wildrift",
            "com.riotgames.legendsofruneterra",
            "com.riotgames.teamfighttactics",
            "com.supercell.clashofclans",
            "com.supercell.clashroyale",
            "com.supercell.brawlstars",
            "com.epicgames.fortnite",
            "com.tencent.tmgp.sgame",
            "com.levelinfinite.sgameGlobal",
            "com.kiloo.subwaysurf",
            "com.mojang.minecraftpe",
            "com.roblox.client",
            "com.ea.gp.fifamobile",
            "jp.konami.pesam",
            "com.gameloft.android.ANMP.GloftA9HM",
            "com.ea.games.r3_row",
            "com.axlebolt.standoff2",
            "com.proximabeta.nikke",
            "com.nexon.maplem",
            "com.nexon.bluearchiveglobal",
            "com.YoStarEN.Arknights",
            "com.innersloth.spacemafia",
            "com.king.candycrushsaga",
            "com.playrix.gardenscapes",
            "com.scopely.monopolygo",
            "com.lilithgames.hgame.gp",
            "com.lilithgames.roc.gp",
            "com.bandainamcoent.dblegends_ww",
            "com.nianticlabs.pokemongo",
            "com.square_enix.android_googleplay.ffbeww",
            "com.netease.mrzhna",
            "com.netease.lztgglobal",
            "com.netease.onmyoji",
            "com.garena.game.codm",
            "com.ea.gp.apexlegendsmobilefps",
            "com.ea.gp.nbamobile",
            "com.naturalmotion.customstreetracer2",
            "com.nexon.kart",
            "com.YoStarJP.BlueArchive",
            "com.tencent.lolm",
            "com.dena.a12026418",
            "com.playdigious.deadcells.mobile",
            "com.garena.game.freefirees",
            "com.garena.game.freefirebd",
            "com.HoYoverse.hkrpgoversea",
            "com.levelinfinite.sgame"
        )
        return packageName.lowercase() in popularGames
    }

    private fun isGameCategory(packageName: String, pm: PackageManager): Boolean {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                @Suppress("DEPRECATION")
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }
        } catch (e: Exception) { false }
    }

    private fun hasGameKeywords(packageName: String): Boolean {
        val normalized = packageName.lowercase()
        val gameKeywords = listOf(
            "game", "gaming", "legend", "battle", "racing", "pubg", "mobilelegends",
            "freefire", "genshin", "wildrift", "clash", "callofduty", "codm",
            "warzone", "fortnite", "gameplay",
            "supercell", "gameloft", "ea.gp", "netease", "garena", "tencent",
            "lilithgame", "nexon", "hoYoverse", "miHoYo", "riotgame",
            "epicgame", "roblox", "mojang", "minecraft", "king.", "candy",
            "playrix", "scopely", "innersloth", "axlebolt", "proximabeta",
            "dena", "konami", "bandainamco", "square_enix", "niantic",
            ".game.", "games.", "_game", "fps", "moba", "rpg", "arena",
            "shoot", "strike", "hero", "war", "fight", "racer", "driver",
            "puzzle", "quest", "dungeon", "survival", "royale", "offline"
        )
        return gameKeywords.any { normalized.contains(it) }
    }

    private fun classifyWithML(packageName: String): Boolean {
        return try {
            val input = prepareInput(packageName)
            val output = Array(1) { FloatArray(2) } // [not_game, is_game]
            interpreter?.run(input, output)
            output[0][1] > 0.5f // Probability of being a game > 50%
        } catch (e: Exception) { false }
    }

    private fun prepareInput(packageName: String): ByteBuffer {
        // Simple feature extraction: package name length, character type counts
        val buffer = ByteBuffer.allocateDirect(4 * 3).order(ByteOrder.nativeOrder())
        buffer.putFloat(packageName.length.toFloat())
        buffer.putFloat(packageName.count { it.isDigit() }.toFloat())
        buffer.putFloat(packageName.count { it == '.' }.toFloat())
        return buffer
    }

    fun close() {
        interpreter?.close()
    }
}
