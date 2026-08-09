package com.gamebooster.app.games;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.gamebooster.app.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ultra-fast dedicated scanner for Home screen — detects MLBB, PUBGM, CODM, Honor of Kings,
 * Roblox, Free Fire, Wild Rift, Genshin Impact, and all user custom added games.
 */
public class HomeGameScanner {

    public static class TargetGameSpec {
        public final String[] packageNames;
        public final String defaultTitle;
        public final String gameType;
        public final int cardBgRes;
        public final int badgeColor;

        public TargetGameSpec(String[] packageNames, String defaultTitle, String gameType, int cardBgRes, int badgeColor) {
            this.packageNames = packageNames;
            this.defaultTitle = defaultTitle;
            this.gameType = gameType;
            this.cardBgRes = cardBgRes;
            this.badgeColor = badgeColor;
        }
    }

    // 1. Mobile Legends: Bang Bang
    private static final TargetGameSpec MLBB_SPEC = new TargetGameSpec(
            new String[]{
                    "com.mobile.legends",
                    "com.mobile.legends.vng",
                    "com.mobile.legends.kr",
                    "com.mobile.legends.jp"
            },
            "🗡️ Mobile Legends: Bang Bang",
            "MOBA",
            R.drawable.home_game_card_bg_ml,
            Color.parseColor("#4A90E2")
    );

    // 2. PUBG Mobile & Regional
    private static final TargetGameSpec PUBG_SPEC = new TargetGameSpec(
            new String[]{
                    "com.tencent.ig",
                    "com.pubg.imobile",
                    "com.pubg.krmobile",
                    "com.vng.pubgmobile",
                    "com.tencent.iglite",
                    "com.pubg.newstate",
                    "com.tencent.tmgp.pubgm"
            },
            "🪖 PUBG Mobile",
            "BATTLE ROYALE",
            R.drawable.home_game_card_bg_pubg,
            Color.parseColor("#FF8800")
    );

    // 3. Call of Duty: Mobile
    private static final TargetGameSpec CODM_SPEC = new TargetGameSpec(
            new String[]{
                    "com.activision.callofduty.shooter",
                    "com.garena.game.codm",
                    "com.tencent.tmgp.kr.codm",
                    "com.tencent.tmgp.cod"
            },
            "🎯 Call of Duty: Mobile",
            "FPS",
            R.drawable.home_game_card_bg_codm,
            Color.parseColor("#FF0055")
    );

    // 4. Honor of Kings (HoK) & Arena of Valor (AoV)
    private static final TargetGameSpec HOK_SPEC = new TargetGameSpec(
            new String[]{
                    "com.levelinfinite.hok.global",
                    "com.levelinfinite.sgameGlobal",
                    "com.levelinfinite.sgameGlobal.gpkg",
                    "com.tencent.tmgp.sgame",
                    "com.garena.game.kgtw",
                    "com.garena.game.kgvn",
                    "com.garena.game.kgid"
            },
            "👑 Honor of Kings",
            "MOBA",
            R.drawable.home_game_card_bg_ml,
            Color.parseColor("#FFB800")
    );

    // 5. Roblox
    private static final TargetGameSpec ROBLOX_SPEC = new TargetGameSpec(
            new String[]{
                    "com.roblox.client"
            },
            "🕹️ Roblox",
            "SANDBOX",
            R.drawable.home_game_card_bg_codm,
            Color.parseColor("#00F0FF")
    );

    // 6. Free Fire & Free Fire MAX
    private static final TargetGameSpec FREEFIRE_SPEC = new TargetGameSpec(
            new String[]{
                    "com.dts.freefireth",
                    "com.dts.freefiremax"
            },
            "🔥 Free Fire MAX",
            "BATTLE ROYALE",
            R.drawable.home_game_card_bg_pubg,
            Color.parseColor("#FF7000")
    );

    // 7. Wild Rift
    private static final TargetGameSpec WILDRIFT_SPEC = new TargetGameSpec(
            new String[]{
                    "com.riotgames.league.wildrift",
                    "com.riotgames.league.wildrifttw",
                    "com.riotgames.league.wildriftvn"
            },
            "⚔️ League of Legends: Wild Rift",
            "MOBA",
            R.drawable.home_game_card_bg_ml,
            Color.parseColor("#9D4EDD")
    );

    // 8. Genshin Impact
    private static final TargetGameSpec GENSHIN_SPEC = new TargetGameSpec(
            new String[]{
                    "com.cognosphere.GenshinImpact",
                    "com.miHoYo.GenshinImpact"
            },
            "✨ Genshin Impact",
            "ACTION RPG",
            R.drawable.home_game_card_bg_ml,
            Color.parseColor("#00FF66")
    );

    private static final TargetGameSpec[] ALL_TARGET_SPECS = new TargetGameSpec[]{
            MLBB_SPEC, PUBG_SPEC, CODM_SPEC, HOK_SPEC, ROBLOX_SPEC, FREEFIRE_SPEC, WILDRIFT_SPEC, GENSHIN_SPEC
    };

    /**
     * Scans and returns all target online games and user custom added games.
     */
    public static List<GameAppInfo> scanTargetGames(Context context) {
        List<GameAppInfo> detectedGames = new ArrayList<>();
        if (context == null) return detectedGames;

        PackageManager pm = context.getPackageManager();
        Set<String> addedPackages = new HashSet<>();

        // 1. Scan pre-configured target game specs
        for (TargetGameSpec spec : ALL_TARGET_SPECS) {
            for (String pkg : spec.packageNames) {
                if (addedPackages.contains(pkg)) continue;

                Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                        String label = pm.getApplicationLabel(appInfo).toString();
                        Drawable icon = pm.getApplicationIcon(appInfo);

                        detectedGames.add(new GameAppInfo(
                                label,
                                pkg,
                                icon,
                                launchIntent,
                                spec.gameType,
                                spec.cardBgRes,
                                spec.badgeColor
                        ));
                        addedPackages.add(pkg);
                    } catch (Throwable ignored) {
                        Drawable defaultIcon = context.getApplicationInfo().loadIcon(pm);
                        detectedGames.add(new GameAppInfo(
                                spec.defaultTitle,
                                pkg,
                                defaultIcon,
                                launchIntent,
                                spec.gameType,
                                spec.cardBgRes,
                                spec.badgeColor
                        ));
                        addedPackages.add(pkg);
                    }
                }
            }
        }

        // 2. Include all manually added user custom game packages
        Set<String> customPkgs = GameLauncherHelper.getCustomPackages(context);
        for (String customPkg : customPkgs) {
            if (!addedPackages.contains(customPkg)) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(customPkg, 0);
                    String label = pm.getApplicationLabel(appInfo).toString();
                    Drawable icon = pm.getApplicationIcon(appInfo);
                    Intent launchIntent = pm.getLaunchIntentForPackage(customPkg);
                    detectedGames.add(new GameAppInfo(
                            label,
                            customPkg,
                            icon,
                            launchIntent,
                            "CUSTOM GAME",
                            R.drawable.home_game_card_bg_ml,
                            Color.parseColor("#00F0FF")
                    ));
                    addedPackages.add(customPkg);
                } catch (Throwable ignored) {}
            }
        }

        return detectedGames;
    }
}
