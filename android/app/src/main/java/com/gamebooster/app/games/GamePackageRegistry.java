package com.gamebooster.app.games;
import com.gamebooster.app.config.*;

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

        // Call of Duty: Mobile (CODM) & Regional Variants
        KNOWN_GAMES.put("com.activision.callofduty.shooter", new GameInfoSpec("Call of Duty: Mobile (Global)", "FPS", 185));
        KNOWN_GAMES.put("com.garena.game.codm", new GameInfoSpec("Call of Duty: Mobile (Garena)", "FPS", 185));
        KNOWN_GAMES.put("com.tencent.tmgp.kr.codm", new GameInfoSpec("Call of Duty: Mobile (KR)", "FPS", 185));
        KNOWN_GAMES.put("com.tencent.tmgp.cod", new GameInfoSpec("Call of Duty: Mobile (CN)", "FPS", 185));

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

        // Genshin Impact & HoYoverse Titles
        KNOWN_GAMES.put("com.miHoYo.GenshinImpact", new GameInfoSpec("Genshin Impact (CN)", "Action RPG", 185));
        KNOWN_GAMES.put("com.cognosphere.GenshinImpact", new GameInfoSpec("Genshin Impact (Global)", "Action RPG", 185));
        KNOWN_GAMES.put("com.HoYoverse.hkrpgoversea", new GameInfoSpec("Honkai: Star Rail", "RPG", 185));
        KNOWN_GAMES.put("com.HoYoverse.nap", new GameInfoSpec("Zenless Zone Zero", "Action RPG", 185));
        KNOWN_GAMES.put("com.miHoYo.bh3oversea", new GameInfoSpec("Honkai Impact 3rd", "Action RPG", 185));

        // Roblox & FPS Competitors
        KNOWN_GAMES.put("com.roblox.client", new GameInfoSpec("Roblox", "Sandbox", 185));
        KNOWN_GAMES.put("com.axlebolt.standoff2", new GameInfoSpec("Standoff 2", "FPS", 185));
        KNOWN_GAMES.put("com.netease.bloodstrike", new GameInfoSpec("Blood Strike", "FPS", 185));

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

        // Supercell Hits
        KNOWN_GAMES.put("com.supercell.brawlstars", new GameInfoSpec("Brawl Stars", "Action", 185));
        KNOWN_GAMES.put("com.supercell.clashroyale", new GameInfoSpec("Clash Royale", "Strategy", 185));
        KNOWN_GAMES.put("com.supercell.clashofclans", new GameInfoSpec("Clash of Clans", "Strategy", 185));
        KNOWN_GAMES.put("com.supercell.squad", new GameInfoSpec("Squad Busters", "Action", 185));
    }

    public static boolean isKnownGame(String packageName) {
        return packageName != null && KNOWN_GAMES.containsKey(packageName.toLowerCase());
    }

    public static GameInfoSpec getSpec(String packageName) {
        if (packageName == null) return null;
        return KNOWN_GAMES.get(packageName.toLowerCase());
    }

    public static Map<String, GameInfoSpec> getAllKnownGames() {
        return KNOWN_GAMES;
    }
}
