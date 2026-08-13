package com.gamebooster.app.feature.games;

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

    private static void addGame(String pkg, String title, String category, int maxFps) {
        KNOWN_GAMES.put(pkg.toLowerCase(), new GameInfoSpec(title, category, maxFps));
    }

    static {
        // Mobile Legends: Bang Bang (MLBB) & Regional Variants
        addGame("com.mobile.legends", "Mobile Legends: Bang Bang", "MOBA", 165);
        addGame("com.mobile.legends.vng", "Mobile Legends VNG", "MOBA", 165);
        addGame("com.mobile.legends.kr", "Mobile Legends KR", "MOBA", 165);
        addGame("com.mobile.legends.jp", "Mobile Legends JP", "MOBA", 165);
        addGame("com.mobilelegends.hw", "Mobile Legends Huawei", "MOBA", 165);
        addGame("com.mobile.legends.moonton", "Mobile Legends Moonton", "MOBA", 165);

        // Call of Duty: Mobile (CODM) & Regional Variants
        addGame("com.activision.callofduty.shooter", "Call of Duty: Mobile (Global)", "FPS", 165);
        addGame("com.garena.game.codm", "Call of Duty: Mobile (Garena)", "FPS", 165);
        addGame("com.tencent.tmgp.kr.codm", "Call of Duty: Mobile (KR)", "FPS", 165);
        addGame("com.tencent.tmgp.cod", "Call of Duty: Mobile (CN)", "FPS", 165);

        // PUBG Mobile & Regional Variants
        addGame("com.tencent.ig", "PUBG Mobile (Global)", "Battle Royale", 165);
        addGame("com.pubg.imobile", "Battlegrounds Mobile India (BGMI)", "Battle Royale", 165);
        addGame("com.pubg.krmobile", "PUBG Mobile (KR/JP)", "Battle Royale", 165);
        addGame("com.vng.pubgmobile", "PUBG Mobile (VNG)", "Battle Royale", 165);
        addGame("com.tencent.iglite", "PUBG Mobile Lite", "Battle Royale", 165);
        addGame("com.pubg.newstate", "PUBG: NEW STATE", "Battle Royale", 165);
        addGame("com.tencent.tmgp.pubgm", "PUBG Mobile (CN Peacekeeper Elite)", "Battle Royale", 165);

        // Free Fire & Free Fire MAX
        addGame("com.dts.freefireth", "Garena Free Fire", "Battle Royale", 90);
        addGame("com.dts.freefiremax", "Free Fire MAX", "Battle Royale", 120);

        // League of Legends: Wild Rift & Regional Variants
        addGame("com.riotgames.league.wildrift", "League of Legends: Wild Rift", "MOBA", 120);
        addGame("com.riotgames.league.wildrifttw", "Wild Rift (TW)", "MOBA", 120);
        addGame("com.riotgames.league.wildriftvn", "Wild Rift (VNG)", "MOBA", 120);

        // Honor of Kings (HOK) / Arena of Valor (AoV)
        addGame("com.levelinfinite.sgameGlobal", "Honor of Kings (Global)", "MOBA", 120);
        addGame("com.levelinfinite.sgameGlobal.gpkg", "Honor of Kings (Global GP)", "MOBA", 120);
        addGame("com.tencent.tmgp.sgame", "Honor of Kings (CN)", "MOBA", 120);
        addGame("com.garena.game.kgtw", "Arena of Valor (TW)", "MOBA", 120);
        addGame("com.garena.game.kgvn", "Arena of Valor (VNG)", "MOBA", 120);
        addGame("com.garena.game.kgid", "Arena of Valor (ID)", "MOBA", 120);

        // Genshin Impact & HoYoverse Titles
        addGame("com.miHoYo.GenshinImpact", "Genshin Impact (CN)", "Action RPG", 120);
        addGame("com.cognosphere.GenshinImpact", "Genshin Impact (Global)", "Action RPG", 120);
        addGame("com.HoYoverse.hkrpgoversea", "Honkai: Star Rail", "RPG", 120);
        addGame("com.HoYoverse.nap", "Zenless Zone Zero", "Action RPG", 120);
        addGame("com.kurogame.wutheringwaves.global", "Wuthering Waves", "Action RPG", 120);
        addGame("com.miHoYo.bh3oversea", "Honkai Impact 3rd", "Action RPG", 120);

        // Roblox & Tactical FPS / Competitors
        addGame("com.tencent.dfm", "Delta Force: Hawk Ops", "FPS", 165);
        addGame("com.activision.callofduty.warzone", "Call of Duty: Warzone Mobile", "Battle Royale", 165);
        addGame("com.riotgames.valorant", "Valorant Mobile", "Tactical FPS", 165);
        addGame("com.roblox.client", "Roblox", "Sandbox", 165);
        addGame("com.axlebolt.standoff2", "Standoff 2", "FPS", 165);
        addGame("com.ofg.bloodstrike", "Blood Strike", "FPS", 144);
        addGame("com.netease.bloodstrike", "Blood Strike (NetEase)", "FPS", 144);
        addGame("com.miracle.farlight84", "Farlight 84", "Battle Royale", 144);

        // Sports & Racing
        addGame("jp.konami.pesam", "eFootball 2024 / PES", "Sports", 120);
        addGame("com.ea.gp.fifamobile", "EA SPORTS FC Mobile", "Sports", 120);
        addGame("com.garena.game.fdtw", "Speed Drifters (Garena)", "Racing", 120);
        addGame("com.gameloft.anmp.android.glofta9hm", "Asphalt 9: Legends", "Racing", 120);
        addGame("com.h20.carxstreet", "CarX Street", "Racing", 120);

        // Supercell Hits
        addGame("com.supercell.brawlstars", "Brawl Stars", "Action", 144);
        addGame("com.supercell.clashroyale", "Clash Royale", "Strategy", 120);
        addGame("com.supercell.clashofclans", "Clash of Clans", "Strategy", 120);
        addGame("com.supercell.squad", "Squad Busters", "Action", 120);
    }

    public static boolean isKnownGame(String packageName) {
        return packageName != null && KNOWN_GAMES.containsKey(packageName.toLowerCase());
    }

    public static boolean isKnownGame(android.content.Context context, String packageName) {
        if (packageName == null) return false;
        if (KNOWN_GAMES.containsKey(packageName.toLowerCase())) return true;
        return CustomGameManager.isCustomGame(context, packageName);
    }

    public static GameInfoSpec getSpec(String packageName) {
        if (packageName == null) return null;
        return KNOWN_GAMES.get(packageName.toLowerCase());
    }

    public static Map<String, GameInfoSpec> getAllKnownGames() {
        return KNOWN_GAMES;
    }
}
