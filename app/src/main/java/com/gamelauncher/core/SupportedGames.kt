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
        GameInfo("com.rekoo.pubgm", "PUBG Mobile (Rekoo)", "Global", 165, true, true),
        GameInfo("com.tencent.tmgp.pubgmhd", "PUBG Mobile (CN)", "China", 165, true, true),
        GameInfo("com.pubg.newstate", "PUBG New State", "Global", 165, true, true),
        GameInfo("com.krafton.gamepubg", "PUBG NEW STATE", "Global", 165, true, true),

        // Call of Duty Mobile
        GameInfo("com.activision.callofduty.shooter", "Call of Duty Mobile", "Global", 165, true, true),
        GameInfo("com.activision.callofduty.warzone", "Call of Duty Warzone Mobile", "Global", 165, true, true),
        GameInfo("com.garena.game.codm", "Call of Duty Mobile (Garena)", "SEA", 165, true, true),
        GameInfo("com.tencent.tmgp.codm", "Call of Duty Mobile (CN)", "China", 165, true, true),
        GameInfo("com.vng.codmvn", "Call of Duty Mobile (VN)", "Vietnam", 165, true, true),

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
        GameInfo("com.dts.freefireth", "Free Fire (TH)", "Thailand", 165, true, true),

        // Mobile Legends Bang Bang
        GameInfo("com.mobile.legends", "Mobile Legends: Bang Bang", "Global", 165, true, true),
        GameInfo("com.mobile.legends.mobilelegends", "Mobile Legends", "Global", 165, true, true),
        GameInfo("com.mobile.legends.miHoYo", "Mobile Legends (MiHoYo)", "Alternative", 165, true, true),

        // Genshin Impact
        GameInfo("com.miHoYo.GenshinImpact", "Genshin Impact", "Global", 165, true, true),
        GameInfo("com.miHoYo.ys", "Genshin Impact (China)", "China", 165, true, true),

        // Honkai
        GameInfo("com.miHoYo.hsr", "Honkai: Star Rail", "Global", 165, true, true),
        GameInfo("com.miHoYo.bh3", "Honkai Impact 3rd", "Global", 165, true, true),
        GameInfo("com.HoYoverse.hkrpg", "Honkai: Star Rail (Global)", "Global", 165, true, true),

        // League of Legends
        GameInfo("com.riotgames.leagueofwildrift", "League of Legends: Wild Rift", "Global", 165, true, true),
        GameInfo("com.riotgames.leagueoflegends", "League of Legends Mobile", "Global", 165, true, true),

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

        // Supercell
        GameInfo("com.supercell.clashofclans", "Clash of Clans", "Global", 165, true, true),
        GameInfo("com.supercell.clashroyale", "Clash Royale", "Global", 165, true, true),
        GameInfo("com.supercell.brawlstars", "Brawl Stars", "Global", 165, true, true),
        GameInfo("com.supercell.boombeach", "Boom Beach", "Global", 165, true, false),
        GameInfo("com.supercell.hayday", "Hay Day", "Global", 165, true, false),
        GameInfo("com.supercell.squad", "Squad Busters", "Global", 165, true, true),
        GameInfo("com.supercell.moa", "Mo.co", "Global", 165, true, true),

        // EA Sports
        GameInfo("com.ea.gp.fifamobile", "FIFA Mobile", "Global", 165, true, true),
        GameInfo("com.ea.gp.easportsfc", "EA Sports FC Mobile", "Global", 165, true, true),
        GameInfo("com.ea.game.nfs14_row", "Need for Speed No Limits", "Global", 165, true, true),
        GameInfo("com.ea.game.realracing3", "Real Racing 3", "Global", 165, true, true),

        // Gameloft
        GameInfo("com.gameloft.asphalt9", "Asphalt 9: Legends", "Global", 165, true, true),
        GameInfo("com.gameloft.asphalt8", "Asphalt 8", "Global", 165, true, true),
        GameInfo("com.gameloft.moderncombat5", "Modern Combat 5", "Global", 165, true, true),
        GameInfo("com.gameloft.nova", "N.O.V.A. Legacy", "Global", 165, true, true),
        GameInfo("com.gameloft.android.ANMP.GloftA9HM", "Asphalt 9", "Global", 165, true, true),

        // NBA
        GameInfo("com.t2ksports.nba2kmobile", "NBA 2K Mobile", "Global", 165, true, true),
        GameInfo("com.t2ksports.nba2k23android", "NBA 2K23", "Global", 165, true, true),
        GameInfo("com.t2ksports.nba2k24android", "NBA 2K24", "Global", 165, true, true),
        GameInfo("com.netease.nba", "NBA Mobile", "Global", 165, true, true),

        // Other Major Titles
        GameInfo("com.bhvr.dbdmobile", "Dead by Daylight Mobile", "Global", 165, true, true),
        GameInfo("com.ubisoft.rainbowsixmobile", "Rainbow Six Mobile", "Global", 165, true, true),
        GameInfo("com.pokemon.unite", "Pokemon UNITE", "Global", 165, true, true),
        GameInfo("com.blizzard.diabloimmortal", "Diablo Immortal", "Global", 165, true, true),
        GameInfo("com.hotta.dreamroom", "Tower of Fantasy", "Global", 165, true, true),
        GameInfo("com.wondergame.saf", "Lost Light", "Global", 165, true, true),
        GameInfo("com.mail.ru.warface_next", "Warface Next", "Global", 165, true, true),
        GameInfo("com.madfingergames.shadowgun", "Shadowgun: Legends", "Global", 165, true, true),
        GameInfo("com.madfingergames.hero", "Shadowgun", "Global", 165, true, true),
        GameInfo("com.pixonic.wwr", "War Robots", "Global", 165, true, true),
        GameInfo("com.generagames.coverfire", "Cover Fire", "Global", 165, true, true),
        GameInfo("com.fps.war.military.shooting.game3d", "World War Heroes", "Global", 165, true, true),
        GameInfo("com.galarobotics.dragonstorm", "Dragon Storm", "Global", 165, true, true),
        GameInfo("com.pixelart.space_marshals", "Space Marshals", "Global", 165, true, true),
        GameInfo("com.droidhen.bullet", "Bullet League", "Global", 165, true, true),
        GameInfo("com.pixelracers.death", "Into The Death", "Global", 165, true, true),
        GameInfo("com.netease.sdut", "Shroud of the Avatar", "Global", 165, true, true),
        GameInfo("com.proximabeta.mf", "Marvel Future Fight", "Global", 165, true, true),
        GameInfo("com.gameloft.dh5", "Dungeon Hunter 5", "Global", 165, true, true),
        GameInfo("com.ea.games.realracing3", "Real Racing 3", "Global", 165, true, true),

        // New Popular Games 2025/2026
        GameInfo("com.activision.callofduty.warzone", "Call of Duty Warzone", "Global", 165, true, true),
        GameInfo("com.netease.mcbeta", "Marvel Contest of Champions", "Global", 165, true, true),
        GameInfo("com.kabam.marvelbattle", "Marvel Contest of Champions", "Global", 165, true, true),
        GameInfo("com.netmarble.mheros", "Marvel Future Revolution", "Global", 165, true, true),
        GameInfo("com.nexon.bluearchive", "Blue Archive", "Global", 165, true, false),
        GameInfo("com.nexon.counterside", "Counter:Side", "Global", 165, true, true),
        GameInfo("com.nexon.v4", "V4", "Global", 165, true, true),
        GameInfo("com.nexon.mabinogi", "Mabinogi Mobile", "Global", 165, true, false),
        GameInfo("com.levelinfinite.marvelsnap", "Marvel Snap", "Global", 165, true, false),
        GameInfo("com.netease.identityv", "Identity V", "Global", 165, true, true),
        GameInfo("com.netease.dd2", "Dead by Daylight Mobile", "Global", 165, true, true),
        GameInfo("com.netease.la2", "LifeAfter", "Global", 165, true, false),
        GameInfo("com.netease.g56", "Knives Out", "Global", 165, true, true),
        GameInfo("com.pubg.newstate", "PUBG: New State", "Global", 165, true, true),
        GameInfo("com.tencent.kart", "QQ Speed (Garena Speed Drifters)", "Global", 165, true, true),
        GameInfo("com.tencent.gp", "Arena of Valor", "Global", 165, true, true),
        GameInfo("com.ngame.allstar", "Onmyoji Arena", "Global", 165, true, true),
        GameInfo("com.netease.onmyoji", "Onmyoji", "Global", 165, true, false),
        GameInfo("com.tencent.ig.ce", "PUBG Mobile (Beta)", "Beta", 165, true, true),

        // Dragon Raja / Perfect World
        GameInfo("com.tencent.dragonraja", "Dragon Raja", "Global", 165, true, true),
        GameInfo("com.pwrd.hundred", "Perfect World Mobile", "Global", 165, true, true),
        GameInfo("com.pwrd.lhj", "Legend of the Phoenix", "Global", 165, true, false),

        // ZZZ / Wuthering Waves
        GameInfo("com.HoYoverse.zzz", "Zenless Zone Zero", "Global", 165, true, true),
        GameInfo("com.kuro.wutheringwaves", "Wuthering Waves", "Global", 165, true, true),
        GameInfo("com.kuro.wutheringwaves.cn", "Wuthering Waves (CN)", "China", 165, true, true),

        // Roblox
        GameInfo("com.roblox.client", "Roblox", "Global", 165, true, true),

        // Garena
        GameInfo("com.garena.game.kg", "Garena Speed Drifters", "Global", 165, true, true),
        GameInfo("com.garena.game.codm", "Call of Duty Mobile (Garena)", "SEA", 165, true, true),

        // miHoYo / HoYoverse
        GameInfo("com.miHoYo.GenshinImpact", "Genshin Impact", "Global", 165, true, true),
        GameInfo("com.HoYoverse.hkrpg", "Honkai: Star Rail", "Global", 165, true, true),
        GameInfo("com.miHoYo.bh3", "Honkai Impact 3rd", "Global", 165, true, true),
        GameInfo("com.HoYoverse.zzz", "Zenless Zone Zero", "Global", 165, true, true),
        GameInfo("com.miHoYo.ys", "Genshin Impact (CN)", "China", 165, true, true),

        // Battle Royale
        GameInfo("com.tencent.tmgp.sgame", "Honor of Kings", "Global", 165, true, true),
        GameInfo("com.tencent.lolm", "League of Legends: Wild Rift (CN)", "China", 165, true, true),
        GameInfo("com.ngame.allstar", "Arena of Valor", "SEA", 165, true, true),
        GameInfo("com.proximabeta.nikke", "NIKKE", "Global", 165, true, false),
        GameInfo("com.igg.castleclash", "Castle Clash", "Global", 165, true, false),
        GameInfo("com.igg.lordsmobile", "Lords Mobile", "Global", 165, true, false),
        GameInfo("com.igg.clashoflords", "Clash of Lords 2", "Global", 165, true, false),

        // Simulation / Open World
        GameInfo("com.gameloft.android.ANMP.GloftA8HM", "Asphalt 8", "Global", 165, true, true),
        GameInfo("com.dts.freefireth", "Free Fire (TH)", "Thailand", 165, true, true),
        GameInfo("com.rekoo.pubgm", "PUBG Mobile (Rekoo)", "Global", 165, true, true),
        GameInfo("com.tencent.ig.ce", "PUBG Mobile (SEA)", "SEA", 165, true, true),
        GameInfo("com.tencent.ig.vn", "PUBG Mobile (VN)", "Vietnam", 165, true, true),
        GameInfo("com.pubg.newstate", "PUBG New State", "Global", 165, true, true),

        // Wargaming
        GameInfo("com.wargaming.wot.blitz", "World of Tanks Blitz", "Global", 165, true, true),
        GameInfo("com.wargaming.wows.blitz", "World of Warships Blitz", "Global", 165, true, true),
        GameInfo("com.wargaming.woa", "World of Warplanes", "Global", 165, true, true),

        // CD Projekt
        GameInfo("com.cdprojektred.gwent", "GWENT", "Global", 165, true, false),
        GameInfo("com.cdprojektred.thewitcher", "The Witcher Mobile", "Global", 165, true, false),

        // Sega
        GameInfo("com.sega.sonic", "Sonic Dash", "Global", 165, true, false),
        GameInfo("com.sega.sonicforces", "Sonic Forces", "Global", 165, true, false),
        GameInfo("com.sega.puyopuyo", "Puyo Puyo Puzzle Pop", "Global", 165, true, false),

        // Capcom
        GameInfo("com.capcom.streetfighterduel", "Street Fighter: Duel", "Global", 165, true, true),
        GameInfo("com.capcom.monsterhunter", "Monster Hunter Now", "Global", 165, true, true),

        // Square Enix
        GameInfo("com.squareenix.finalfantasy", "Final Fantasy I-VI", "Global", 165, true, false),
        GameInfo("com.squareenix.dragonquest", "Dragon Quest Series", "Global", 165, true, false),
        GameInfo("com.squareenix.kingdomhearts", "Kingdom Hearts", "Global", 165, true, false),

        // Bandai Namco
        GameInfo("com.bandainamco.dragonballlegends", "Dragon Ball Legends", "Global", 165, true, true),
        GameInfo("com.bandainamco.onepiece", "One Piece Bounty Rush", "Global", 165, true, true),
        GameInfo("com.bandainamco.naruto", "Naruto x Boruto Ninja Voltage", "Global", 165, true, true),
        GameInfo("com.bandainamco.gundam", "Gundam Battle", "Global", 165, true, true),
        GameInfo("com.bandainamco.taiko", "Taiko no Tatsujin", "Global", 165, true, false),

        // Konami
        GameInfo("com.konami.pes", "eFootball PES", "Global", 165, true, true),
        GameInfo("com.konami.yugioh", "Yu-Gi-Oh! Duel Links", "Global", 165, true, false),
        GameInfo("com.konami.metalgear", "Metal Gear Solid", "Global", 165, true, true),

        // Koei Tecmo
        GameInfo("com.koei.wildrift", "Dynasty Warriors Mobile", "Global", 165, true, true),
        GameInfo("com.koei.nobunaga", "Nobunaga's Ambition", "Global", 165, true, false),

        // PlayStation
        GameInfo("com.sony.playstation", "PlayStation App", "Global", 165, true, false),
        GameInfo("com.sony.remoteplay", "Remote Play", "Global", 165, true, false),

        // New Games 2025/2026
        GameInfo("com.rockstargames.gta", "GTA: San Andreas", "Global", 165, true, false),
        GameInfo("com.rockstargames.gtavc", "GTA: Vice City", "Global", 165, true, false),
        GameInfo("com.rockstargames.gtaiii", "GTA III", "Global", 165, true, false),
        GameInfo("com.rockstargames.gtade", "GTA: Definitive Edition", "Global", 165, true, true),
        GameInfo("com.rockstargames.bully", "Bully", "Global", 165, true, false),
        GameInfo("com.rockstargames.maxpayne", "Max Payne", "Global", 165, true, false),
        GameInfo("com.activision.crashbandicoot", "Crash Bandicoot", "Global", 165, true, false),
        GameInfo("com.activision.spyro", "Spyro Reignited", "Global", 165, true, false),
        GameInfo("com.activision.tonyhawk", "Tony Hawk's Pro Skater", "Global", 165, true, false)
    )

    fun findGame(packageName: String): GameInfo? {
        return games.find { it.packageName == packageName }
    }

    fun isSupportedGame(packageName: String): Boolean {
        return games.any { it.packageName == packageName }
    }


}
