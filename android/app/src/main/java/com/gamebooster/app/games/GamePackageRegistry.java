package com.gamebooster.app.games;

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
        // Mobile Legends / MLBB
        KNOWN_GAMES.put("com.mobile.legends", new GameInfoSpec("Mobile Legends: Bang Bang", "MOBA", 120));
        KNOWN_GAMES.put("com.mobile.legends.vng", new GameInfoSpec("Mobile Legends VNG", "MOBA", 120));

        // PUBG Mobile & Regional Variants
        KNOWN_GAMES.put("com.tencent.ig", new GameInfoSpec("PUBG Mobile Global", "Battle Royale", 120));
        KNOWN_GAMES.put("com.pubg.imobile", new GameInfoSpec("Battlegrounds Mobile India (BGMI)", "Battle Royale", 120));
        KNOWN_GAMES.put("com.pubg.krmobile", new GameInfoSpec("PUBG Mobile KR", "Battle Royale", 120));
        KNOWN_GAMES.put("com.vng.pubgmobile", new GameInfoSpec("PUBG Mobile VNG", "Battle Royale", 120));
        KNOWN_GAMES.put("com.tencent.iglite", new GameInfoSpec("PUBG Mobile Lite", "Battle Royale", 90));

        // Call of Duty Mobile (CODM)
        KNOWN_GAMES.put("com.activision.callofduty.shooter", new GameInfoSpec("Call of Duty: Mobile", "FPS", 120));
        KNOWN_GAMES.put("com.garena.game.codm", new GameInfoSpec("COD Mobile Garena", "FPS", 120));

        // Free Fire / Free Fire MAX
        KNOWN_GAMES.put("com.dts.freefireth", new GameInfoSpec("Garena Free Fire", "Battle Royale", 90));
        KNOWN_GAMES.put("com.dts.freefiremax", new GameInfoSpec("Free Fire MAX", "Battle Royale", 120));

        // Honor of Kings (HOK) / Arena of Valor
        KNOWN_GAMES.put("com.levelinfinite.sgameGlobal", new GameInfoSpec("Honor of Kings", "MOBA", 120));
        KNOWN_GAMES.put("com.tencent.tmgp.sgame", new GameInfoSpec("Honor of Kings CN", "MOBA", 120));

        // Genshin Impact & HoYoverse
        KNOWN_GAMES.put("com.miHoYo.GenshinImpact", new GameInfoSpec("Genshin Impact", "Action RPG", 120));
        KNOWN_GAMES.put("com.cognosphere.GenshinImpact", new GameInfoSpec("Genshin Impact Global", "Action RPG", 120));
        KNOWN_GAMES.put("com.HoYoverse.hkrpgoversea", new GameInfoSpec("Honkai: Star Rail", "Turn-Based RPG", 120));

        // League of Legends: Wild Rift
        KNOWN_GAMES.put("com.riotgames.league.wildrift", new GameInfoSpec("Wild Rift", "MOBA", 120));

        // Roblox & Standoff 2
        KNOWN_GAMES.put("com.roblox.client", new GameInfoSpec("Roblox", "Sandbox", 144));
        KNOWN_GAMES.put("com.axlebolt.standoff2", new GameInfoSpec("Standoff 2", "FPS", 144));
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
