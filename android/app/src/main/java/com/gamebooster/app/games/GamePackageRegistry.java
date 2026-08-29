package com.gamebooster.app.games;
import com.gamebooster.app.config.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GamePackageRegistry {

    public static class GameInfoSpec {
        public final String title;
        public final String category;
        public final int maxSupportedFps;

        public GameInfoSpec(String title, String category, int maxSupportedFps) {
            this.title = title;
            this.category = category;
            this.maxSupportedFps = maxSupportedFps;
        }
    }

    private static final Map<String, GameInfoSpec> KNOWN_GAMES = new HashMap<>();

    static {
        // Mobile Legends: Bang Bang (MLBB) & Regional Variants
        KNOWN_GAMES.put("com.mobile.legends", new GameInfoSpec("Mobile Legends: Bang Bang", "MOBA", 185));
        KNOWN_GAMES.put("com.mobile.legends.vng", new GameInfoSpec("Mobile Legends VNG", "MOBA", 185));
        KNOWN_GAMES.put("com.mobile.legends.kr", new GameInfoSpec("Mobile Legends KR", "MOBA", 185));
        KNOWN_GAMES.put("com.mobile.legends.jp", new GameInfoSpec("Mobile Legends JP", "MOBA", 185));
        KNOWN_GAMES.put("com.mobilelegends.hw", new GameInfoSpec("Mobile Legends Huawei", "MOBA", 185));
        KNOWN_GAMES.put("com.mobile.legends.moonton", new GameInfoSpec("Mobile Legends Moonton", "MOBA", 185));

        // Call of Duty: Mobile (CODM) & Regional Variants & Warzone Mobile
        KNOWN_GAMES.put("com.activision.callofduty.shooter", new GameInfoSpec("Call of Duty: Mobile (Global)", "FPS", 185));
        KNOWN_GAMES.put("com.garena.game.codm", new GameInfoSpec("Call of Duty: Mobile (Garena)", "FPS", 185));
        KNOWN_GAMES.put("com.tencent.tmgp.kr.codm", new GameInfoSpec("Call of Duty: Mobile (KR)", "FPS", 185));
        KNOWN_GAMES.put("com.tencent.tmgp.cod", new GameInfoSpec("Call of Duty: Mobile (CN)", "FPS", 185));
        KNOWN_GAMES.put("com.activision.callofduty.warzone", new GameInfoSpec("Call of Duty: Warzone Mobile", "Battle Royale", 185));

        // PUBG Mobile & Regional Variants
        KNOWN_GAMES.put("com.tencent.ig", new GameInfoSpec("PUBG Mobile (Global)", "Battle Royale", 185));
        KNOWN_GAMES.put("com.pubg.imobile", new GameInfoSpec("Battlegrounds Mobile India (BGMI)", "Battle Royale", 185));
        KNOWN_GAMES.put("com.pubg.krmobile", new GameInfoSpec("PUBG Mobile (KR/JP)", "Battle Royale", 185));
        KNOWN_GAMES.put("com.vng.pubgmobile", new GameInfoSpec("PUBG Mobile (VNG)", "Battle Royale", 185));
        KNOWN_GAMES.put("com.tencent.iglite", new GameInfoSpec("PUBG Mobile Lite", "Battle Royale", 185));
        KNOWN_GAMES.put("com.pubg.newstate", new GameInfoSpec("PUBG: NEW STATE", "Battle Royale", 185));
        KNOWN_GAMES.put("com.tencent.tmgp.pubgm", new GameInfoSpec("PUBG Mobile (CN Peacekeeper Elite)", "Battle Royale", 185));

        // Free Fire & Free Fire MAX
        KNOWN_GAMES.put("com.dts.freefireth", new GameInfoSpec("Garena Free Fire", "Battle Royale", 185));
        KNOWN_GAMES.put("com.dts.freefiremax", new GameInfoSpec("Free Fire MAX", "Battle Royale", 185));

        // League of Legends: Wild Rift & Regional Variants
        KNOWN_GAMES.put("com.riotgames.league.wildrift", new GameInfoSpec("League of Legends: Wild Rift", "MOBA", 185));
        KNOWN_GAMES.put("com.riotgames.league.wildrifttw", new GameInfoSpec("Wild Rift (TW)", "MOBA", 185));
        KNOWN_GAMES.put("com.riotgames.league.wildriftvn", new GameInfoSpec("Wild Rift (VNG)", "MOBA", 185));

        // Honor of Kings (HOK) / Arena of Valor (AoV)
        KNOWN_GAMES.put("com.levelinfinite.sgameGlobal", new GameInfoSpec("Honor of Kings (Global)", "MOBA", 185));
        KNOWN_GAMES.put("com.levelinfinite.sgameGlobal.gpkg", new GameInfoSpec("Honor of Kings (Global GP)", "MOBA", 185));
        KNOWN_GAMES.put("com.tencent.tmgp.sgame", new GameInfoSpec("Honor of Kings (CN)", "MOBA", 185));
        KNOWN_GAMES.put("com.garena.game.kgtw", new GameInfoSpec("Arena of Valor (TW)", "MOBA", 185));
        KNOWN_GAMES.put("com.garena.game.kgvn", new GameInfoSpec("Arena of Valor (VNG)", "MOBA", 185));
        KNOWN_GAMES.put("com.garena.game.kgid", new GameInfoSpec("Arena of Valor (ID)", "MOBA", 185));

        // Genshin Impact & HoYoverse & Kuro Titles
        KNOWN_GAMES.put("com.miHoYo.GenshinImpact", new GameInfoSpec("Genshin Impact (CN)", "Action RPG", 185));
        KNOWN_GAMES.put("com.cognosphere.GenshinImpact", new GameInfoSpec("Genshin Impact (Global)", "Action RPG", 185));
        KNOWN_GAMES.put("com.HoYoverse.hkrpgoversea", new GameInfoSpec("Honkai: Star Rail", "RPG", 185));
        KNOWN_GAMES.put("com.HoYoverse.nap", new GameInfoSpec("Zenless Zone Zero", "Action RPG", 185));
        KNOWN_GAMES.put("com.miHoYo.bh3oversea", new GameInfoSpec("Honkai Impact 3rd", "Action RPG", 185));
        KNOWN_GAMES.put("com.kurogame.wutheringwaves.global", new GameInfoSpec("Wuthering Waves", "Action RPG", 185));

        // Roblox & FPS Competitors
        KNOWN_GAMES.put("com.roblox.client", new GameInfoSpec("Roblox", "Sandbox", 185));
        KNOWN_GAMES.put("com.axlebolt.standoff2", new GameInfoSpec("Standoff 2", "FPS", 185));
        KNOWN_GAMES.put("com.netease.bloodstrike", new GameInfoSpec("Blood Strike", "FPS", 185));
        KNOWN_GAMES.put("com.netease.newspike", new GameInfoSpec("Blood Strike (NewSpike)", "FPS", 185));
        KNOWN_GAMES.put("com.proximabeta.mf.uamo", new GameInfoSpec("Arena Breakout", "Tactical FPS", 185));
        KNOWN_GAMES.put("com.levelinfinite.deltaforce", new GameInfoSpec("Delta Force", "Tactical FPS", 185));

        // Valorant Mobile (CN Server Project C & Global)
        KNOWN_GAMES.put("com.tencent.tmgp.projectc", new GameInfoSpec("Valorant Mobile (CN Project C)", "Tactical FPS", 185));
        KNOWN_GAMES.put("com.riotgames.valorantmobile", new GameInfoSpec("Valorant Mobile (Global)", "Tactical FPS", 185));
        KNOWN_GAMES.put("com.tencent.tmgp.valorant", new GameInfoSpec("Valorant Mobile (Tencent)", "Tactical FPS", 185));
        KNOWN_GAMES.put("com.riotgames.valorant", new GameInfoSpec("Valorant Mobile (Riot)", "Tactical FPS", 185));

        // Farlight 84 & Variants
        KNOWN_GAMES.put("com.miracle.farlight84", new GameInfoSpec("Farlight 84", "Hero Shooter", 185));
        KNOWN_GAMES.put("com.miraclegames.farlight84", new GameInfoSpec("Farlight 84 (Miracle)", "Hero Shooter", 185));
        KNOWN_GAMES.put("com.farlightgames.farlight84.gp", new GameInfoSpec("Farlight 84 (GP)", "Hero Shooter", 185));
        KNOWN_GAMES.put("com.farlightgames.farlight84.global", new GameInfoSpec("Farlight 84 (Global)", "Hero Shooter", 185));

        // Sports & Racing
        KNOWN_GAMES.put("jp.konami.pesam", new GameInfoSpec("eFootball 2024 / PES", "Sports", 185));
        KNOWN_GAMES.put("com.ea.gp.fifamobile", new GameInfoSpec("EA SPORTS FC Mobile", "Sports", 185));
        KNOWN_GAMES.put("com.garena.game.fdtw", new GameInfoSpec("Speed Drifters (Garena)", "Racing", 185));
        KNOWN_GAMES.put("com.gameloft.anmp.android.glofta9hm", new GameInfoSpec("Asphalt 9: Legends", "Racing", 185));
        KNOWN_GAMES.put("com.h20.carxstreet", new GameInfoSpec("CarX Street", "Racing", 185));
        KNOWN_GAMES.put("com.ea.games.r3_row", new GameInfoSpec("Real Racing 3", "Racing", 185));

        // Supercell Hits
        KNOWN_GAMES.put("com.supercell.brawlstars", new GameInfoSpec("Brawl Stars", "Action", 185));
        KNOWN_GAMES.put("com.supercell.clashroyale", new GameInfoSpec("Clash Royale", "Strategy", 185));
        KNOWN_GAMES.put("com.supercell.clashofclans", new GameInfoSpec("Clash of Clans", "Strategy", 185));
        KNOWN_GAMES.put("com.supercell.squad", new GameInfoSpec("Squad Busters", "Action", 185));
        KNOWN_GAMES.put("com.supercell.hayday", new GameInfoSpec("Hay Day", "Simulation", 120));

        // Popular Global Hits (Android 13-16)
        KNOWN_GAMES.put("com.mojang.minecraftpe", new GameInfoSpec("Minecraft", "Sandbox", 185));
        KNOWN_GAMES.put("com.kiloo.subwaysurf", new GameInfoSpec("Subway Surfers", "Arcade", 185));
        KNOWN_GAMES.put("com.kitkagames.fallbuddies", new GameInfoSpec("Stumble Guys", "Party Royale", 185));
        KNOWN_GAMES.put("com.miniclip.eightballpool", new GameInfoSpec("8 Ball Pool", "Sports", 185));
        KNOWN_GAMES.put("com.netmarble.sololv", new GameInfoSpec("Solo Leveling: Arise", "Action RPG", 185));
        KNOWN_GAMES.put("com.gameloft.android.ANMP.GloftA9HM", new GameInfoSpec("Asphalt Legends Unite", "Racing", 185));
        KNOWN_GAMES.put("com.fingersoft.hillclimb", new GameInfoSpec("Hill Climb Racing", "Racing", 120));
        KNOWN_GAMES.put("com.fingersoft.hcr2", new GameInfoSpec("Hill Climb Racing 2", "Racing", 185));
        KNOWN_GAMES.put("com.innersloth.spacemafia", new GameInfoSpec("Among Us", "Party", 120));
        KNOWN_GAMES.put("com.plarium.raidlegends", new GameInfoSpec("RAID: Shadow Legends", "RPG", 185));
        KNOWN_GAMES.put("com.habby.archero", new GameInfoSpec("Archero", "Action", 185));
        KNOWN_GAMES.put("com.habby.punball", new GameInfoSpec("PunBall", "Action", 185));
        KNOWN_GAMES.put("com.habby.kinja", new GameInfoSpec("Kinja Run", "Action", 185));
        KNOWN_GAMES.put("com.nekki.shadowfight3", new GameInfoSpec("Shadow Fight 3", "Action", 185));
        KNOWN_GAMES.put("com.nekki.shadowfight4", new GameInfoSpec("Shadow Fight 4: Arena", "Action", 185));
        KNOWN_GAMES.put("com.firsttouchgames.dls7", new GameInfoSpec("Dream League Soccer 2024", "Sports", 185));
        KNOWN_GAMES.put("com.chucklefish.stardewvalley", new GameInfoSpec("Stardew Valley", "RPG", 120));
        KNOWN_GAMES.put("com.rockstargames.gtasa", new GameInfoSpec("GTA: San Andreas", "Action", 120));
        KNOWN_GAMES.put("com.rockstargames.gtavc", new GameInfoSpec("GTA: Vice City", "Action", 120));
        KNOWN_GAMES.put("com.rockstargames.gta3", new GameInfoSpec("GTA III", "Action", 120));
        KNOWN_GAMES.put("com.sega.sonicdash", new GameInfoSpec("Sonic Dash", "Runner", 185));
    }

    public static boolean isSupportedGame(String packageName) {
        return isKnownGame(packageName);
    }

    public static boolean isKnownGame(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) return false;
        String pkg = packageName.toLowerCase(java.util.Locale.ROOT).trim();
        if (KNOWN_GAMES.containsKey(pkg)) return true;

        // Heuristic fallback for regional or modded package variants
        return pkg.contains("tencent.ig")
                || pkg.contains("pubg")
                || pkg.contains("activision.callofduty")
                || pkg.contains("codm")
                || pkg.contains("warzone")
                || pkg.contains("bloodstrike")
                || pkg.contains("newspike")
                || pkg.contains("standoff2")
                || pkg.contains("carxstreet")
                || pkg.contains("uamo")
                || pkg.contains("deltaforce")
                || pkg.contains("supercell")
                || pkg.contains("brawlstars")
                || pkg.contains("clashofclans")
                || pkg.contains("clashroyale")
                || pkg.contains("freefire")
                || pkg.contains("mobile.legends")
                || pkg.contains("mobilelegends")
                || pkg.contains("genshin")
                || pkg.contains("hkrpg")
                || pkg.contains("honkai")
                || pkg.contains("cognosphere")
                || pkg.contains("mihoyo")
                || pkg.contains("hoyoverse")
                || pkg.contains("wutheringwaves")
                || pkg.contains("sgame")
                || pkg.contains("levelinfinite")
                || pkg.contains("arenaofvalor")
                || pkg.contains("roblox")
                || pkg.contains("wildrift")
                || pkg.contains("projectc")
                || pkg.contains("valorant")
                || pkg.contains("farlight")
                || pkg.contains("solarland")
                || pkg.contains("minecraft")
                || pkg.contains("subwaysurf")
                || pkg.contains("fallbuddies")
                || pkg.contains("sololv")
                || pkg.contains("fifamobile")
                || pkg.contains("gameloft")
                || pkg.contains("konami")
                || pkg.contains(".game.")
                || pkg.contains(".games.")
                || pkg.endsWith(".game")
                || pkg.endsWith(".games");
    }

    public static GameInfoSpec getSpec(String packageName) {
        if (packageName == null) return null;
        String pkg = packageName.toLowerCase().trim();
        GameInfoSpec spec = KNOWN_GAMES.get(pkg);
        if (spec != null) return spec;
        if (isKnownGame(pkg)) {
            return new GameInfoSpec("High-Performance Game (" + pkg + ")", "Gaming", 185);
        }
        return null;
    }

    public static Map<String, GameInfoSpec> getAllKnownGames() {
        return Collections.unmodifiableMap(KNOWN_GAMES);
    }
}
