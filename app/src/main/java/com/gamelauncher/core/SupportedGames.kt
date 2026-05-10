package com.gamelauncher.core

object SupportedGames {
    
    data class GameInfo(
        val packageName: String,
        val name: String,
        val region: String,
        val maxFps: Int,
        val supportsHighRefresh: Boolean = true,
        val requiresGamingNetwork: Boolean = true
    )

    val games = listOf(
        // PUBG Mobile (Global & Regional)
        GameInfo("com.tencent.ig", "PUBG Mobile Global", "Global", 165, true, true),
        GameInfo("com.pubg.krmobile", "PUBG Mobile Korea", "Korea", 165, true, true),
        GameInfo("com.pubg.imobile", "PUBG Mobile India", "India", 165, true, true),
        GameInfo("com.pubg.mobile", "PUBG Mobile", "Global", 165, true, true),
        GameInfo("com.tencent.ig.haptic", "PUBG Mobile (Beta)", "Beta", 165, true, true),
        
        // PUBG NEW STATE
        GameInfo("com.krafton.gamepubg", "PUBG NEW STATE", "Global", 165, true, true),
        
        // Call of Duty Mobile (Global & Regional)
        GameInfo("com.activision.callofduty.shooter", "Call of Duty Mobile", "Global", 165, true, true),
        GameInfo("com.activision.callofduty.warzone", "Call of Duty Warzone Mobile", "Global", 165, true, true),
        GameInfo("com.garena.game.codm", "Call of Duty Mobile (Garena)", "SEA", 165, true, true),
        
        // Free Fire (Global & Regional)
        GameInfo("com.garena.game.freefire", "Free Fire", "Global", 165, true, true),
        GameInfo("com.garena.game.freefiremobile", "Free Fire Max", "Global", 165, true, true),
        GameInfo("com.garena.game.freefire.th", "Free Fire Thailand", "Thailand", 165, true, true),
        GameInfo("com.garena.game.freefire.vn", "Free Fire Vietnam", "Vietnam", 165, true, true),
        GameInfo("com.garena.game.freefire.id", "Free Fire Indonesia", "Indonesia", 165, true, true),
        GameInfo("com.garena.game.freefire.in", "Free Fire India", "India", 165, true, true),
        GameInfo("com.garena.game.freefire.ph", "Free Fire Philippines", "Philippines", 165, true, true),
        GameInfo("com.garena.game.freefire.my", "Free Fire Malaysia", "Malaysia", 165, true, true),
        GameInfo("com.garena.game.freefire.sg", "Free Fire Singapore", "Singapore", 165, true, true),
        
        // Mobile Legends: Bang Bang (Global & Regional)
        GameInfo("com.mobile.legends", "Mobile Legends: Bang Bang", "Global", 165, true, true),
        GameInfo("com.mobile.legends.mobilelegends", "Mobile Legends", "Global", 165, true, true),
        GameInfo("com.mobile.legends.miHoYo", "Mobile Legends (MiHoYo)", "Alternative", 165, true, true),
        
        // Marvel Future Fight
        GameInfo("com.proximabeta.mf", "Marvel Future Fight", "Global", 165, true, true),
        
        // Genshin Impact
        GameInfo("com.miHoYo.GenshinImpact", "Genshin Impact", "Global", 165, true, true),
        GameInfo("com.miHoYo.ys", "Genshin Impact (China)", "China", 165, true, true),
        
        // Honkai: Star Rail
        GameInfo("com.miHoYo.hsr", "Honkai: Star Rail", "Global", 165, true, true),
        
        // League of Legends: Wild Rift
        GameInfo("com.riotgames.leagueofwildrift", "League of Legends: Wild Rift", "Global", 165, true, true),
        
        // Valorant Mobile
        GameInfo("com.riotgames.valorant", "Valorant Mobile", "Global", 165, true, true),
        
        // Apex Legends Mobile
        GameInfo("com.ea.gp.apexlegendsmobilefps", "Apex Legends Mobile", "Global", 165, true, true),
        
        // Fortnite
        GameInfo("com.epicgames.fortnite", "Fortnite", "Global", 165, true, true),
        GameInfo("com.epicgames.fortnitemobile", "Fortnite Mobile", "Global", 165, true, true),
        
        // Minecraft
        GameInfo("com.mojang.minecraftpe", "Minecraft PE", "Global", 165, true, false),
        GameInfo("com.mojang.beegame", "Minecraft", "Global", 165, true, false),
        
        // Clash of Clans
        GameInfo("com.supercell.clashofclans", "Clash of Clans", "Global", 165, true, true),
        
        // Clash Royale
        GameInfo("com.supercell.clashroyale", "Clash Royale", "Global", 165, true, true),
        
        // Brawl Stars
        GameInfo("com.supercell.brawlstars", "Brawl Stars", "Global", 165, true, true),
        
        // FIFA Mobile
        GameInfo("com.ea.gp.fifamobile", "FIFA Mobile", "Global", 165, true, true),
        
        // Asphalt 9
        GameInfo("com.gameloft.asphalt9", "Asphalt 9: Legends", "Global", 165, true, true),
        
        // NBA 2K
        GameInfo("com.t2ksports.nba2kmobile", "NBA 2K Mobile", "Global", 165, true, true),
        
        // Dead by Daylight Mobile
        GameInfo("com.bhvr.dbdmobile", "Dead by Daylight Mobile", "Global", 165, true, true),
        
        // Rainbow Six Mobile
        GameInfo("com.ubisoft.rainbowsixmobile", "Rainbow Six Mobile", "Global", 165, true, true),
        
        // League of Legends: Pokémon UNITE
        GameInfo("com.pokemon.unite", "Pokémon UNITE", "Global", 165, true, true),
        
        // Diablo Immortal
        GameInfo("com.blizzard.diabloimmortal", "Diablo Immortal", "Global", 165, true, true),
        
        // Tower of Fantasy
        GameInfo("com.hotta.dreamroom", "Tower of Fantasy", "Global", 165, true, true),
        
        // Sdut Royale
        GameInfo("com.netease.sdut", "Shroud of the Avatar", "Global", 165, true, true),
        
        // Lost Light
        GameInfo("com.wondergame.saf", "Lost Light", "Global", 165, true, true),
        
        // Warface Next
        GameInfo("com.mail.ru.warface_next", "Warface Next", "Global", 165, true, true),
        
        // Modern Combat 5
        GameInfo("com.gameloft.moderncombat5", "Modern Combat 5", "Global", 165, true, true),
        
        // Into The Death
        GameInfo("com.pixelracers.death", "Into The Death", "Global", 165, true, true),
        
        // Bullet League
        GameInfo("com.droidhen.bullet", "Bullet League", "Global", 165, true, true),
        
        // Dragon Storm
        GameInfo("com.galarobotics.dragonstorm", "Dragon Storm", "Global", 165, true, true),
        
        // Shadowgun: Legends
        GameInfo("com.madfingergames.shadowgun", "Shadowgun: Legends", "Global", 165, true, true),
        
        // Space Marshals
        GameInfo("com.pixelart.space_marshals", "Space Marshals", "Global", 165, true, true),
        
        // Cover Fire
        GameInfo("com.generagames.coverfire", "Cover Fire", "Global", 165, true, true),
        
        // N.O.V.A. Legacy
        GameInfo("com.gameloft.nova", "N.O.V.A. Legacy", "Global", 165, true, true),
        
        // World War Heroes
        GameInfo("com.fps.war.military.shooting.game3d", "World War Heroes", "Global", 165, true, true),
        
        // War Robots
        GameInfo("com.pixonic.wwr", "War Robots", "Global", 165, true, true),
        
        // Shadowgun
        GameInfo("com.madfingergames.hero", "Shadowgun", "Global", 165, true, true),
        
        // Dungeon Hunter 5
        GameInfo("com.gameloft.dh5", "Dungeon Hunter 5", "Global", 165, true, true),
        
        // Asphalt 8
        GameInfo("com.gameloft.asphalt8", "Asphalt 8", "Global", 165, true, true),
        
        // Real Racing 3
        GameInfo("com.ea.games.realracing3", "Real Racing 3", "Global", 165, true, true)
    )

    fun findGame(packageName: String): GameInfo? {
        return games.find { it.packageName == packageName }
    }

    fun isSupportedGame(packageName: String): Boolean {
        return games.any { it.packageName == packageName }
    }

    fun getRecommendedFps(packageName: String): Int {
        return findGame(packageName)?.maxFps ?: 165
    }

    fun getAllPackageNames(): Set<String> {
        return games.map { it.packageName }.toSet()
    }
}