package com.gamebooster.app.games;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.gamebooster.app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ultra-fast dedicated scanner focused STRICTLY on MLBB, PUBG Mobile, and CODM (all regional variants).
 * Scans in under 15ms by querying target packages directly rather than scanning full system app lists.
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

    // 1. Mobile Legends: Bang Bang (ALL Regional Packages)
    private static final TargetGameSpec MLBB_SPEC = new TargetGameSpec(
            new String[]{
                    "com.mobile.legends",
                    "com.mobile.legends.vng",
                    "com.mobile.legends.kr",
                    "com.mobile.legends.jp"
            },
            "Mobile Legends: Bang Bang",
            "MOBA",
            R.drawable.home_game_card_bg_ml,
            Color.parseColor("#4A90E2")
    );

    // 2. PUBG Mobile (ALL Regional Packages)
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
            "PUBG Mobile",
            "BATTLE ROYALE",
            R.drawable.home_game_card_bg_pubg,
            Color.parseColor("#FF8800")
    );

    // 3. Call of Duty: Mobile (ALL Regional Packages)
    private static final TargetGameSpec CODM_SPEC = new TargetGameSpec(
            new String[]{
                    "com.activision.callofduty.shooter",
                    "com.garena.game.codm",
                    "com.tencent.tmgp.kr.codm",
                    "com.tencent.tmgp.cod"
            },
            "Call of Duty: Mobile",
            "FPS",
            R.drawable.home_game_card_bg_codm,
            Color.parseColor("#FF0055")
    );

    // 4. Free Fire & Free Fire MAX
    private static final TargetGameSpec FREEFIRE_SPEC = new TargetGameSpec(
            new String[]{
                    "com.dts.freefireth",
                    "com.dts.freefiremax"
            },
            "Free Fire MAX",
            "BATTLE ROYALE",
            R.drawable.home_game_card_bg_pubg,
            Color.parseColor("#FF6600")
    );

    // 5. Genshin Impact & HoYoverse Titles
    private static final TargetGameSpec GENSHIN_SPEC = new TargetGameSpec(
            new String[]{
                    "com.miHoYo.GenshinImpact",
                    "com.cognosphere.GenshinImpact",
                    "com.HoYoverse.hkrpgoversea",
                    "com.HoYoverse.nap",
                    "com.miHoYo.bh3oversea"
            },
            "Genshin Impact",
            "ACTION RPG",
            R.drawable.home_game_card_bg_ml,
            Color.parseColor("#9933FF")
    );

    // 6. Honor of Kings / Arena of Valor
    private static final TargetGameSpec HOK_SPEC = new TargetGameSpec(
            new String[]{
                    "com.levelinfinite.sgameGlobal",
                    "com.levelinfinite.sgameGlobal.gpkg",
                    "com.tencent.tmgp.sgame",
                    "com.garena.game.kgtw",
                    "com.garena.game.kgvn",
                    "com.garena.game.kgid",
                    "com.riotgames.league.wildrift"
            },
            "Honor of Kings",
            "MOBA",
            R.drawable.home_game_card_bg_ml,
            Color.parseColor("#00E5FF")
    );

    // 7. Roblox
    private static final TargetGameSpec ROBLOX_SPEC = new TargetGameSpec(
            new String[]{
                    "com.roblox.client"
            },
            "Roblox",
            "SANDBOX",
            R.drawable.home_game_card_bg_codm,
            Color.parseColor("#00FF66")
    );

    // 8. Valorant Mobile (CN Project C / Global)
    private static final TargetGameSpec VALORANT_SPEC = new TargetGameSpec(
            new String[]{
                    "com.tencent.tmgp.projectc",
                    "com.riotgames.valorantmobile",
                    "com.tencent.tmgp.valorant",
                    "com.riotgames.valorant"
            },
            "Valorant Mobile",
            "TACTICAL FPS",
            R.drawable.home_game_card_bg_codm,
            Color.parseColor("#FF4655")
    );

    // 9. Farlight 84
    private static final TargetGameSpec FARLIGHT_SPEC = new TargetGameSpec(
            new String[]{
                    "com.miracle.farlight84",
                    "com.miraclegames.farlight84",
                    "com.farlightgames.farlight84.gp",
                    "com.farlightgames.farlight84.global"
            },
            "Farlight 84",
            "HERO SHOOTER",
            R.drawable.home_game_card_bg_pubg,
            Color.parseColor("#FFCC00")
    );

    private static final TargetGameSpec[] ALL_TARGET_SPECS = new TargetGameSpec[]{
            MLBB_SPEC, PUBG_SPEC, CODM_SPEC, FREEFIRE_SPEC, GENSHIN_SPEC, HOK_SPEC, ROBLOX_SPEC, VALORANT_SPEC, FARLIGHT_SPEC
    };

    /**
     * Scans and returns ONLY installed instances of MLBB, PUBG Mobile, or CODM.
     */
    public static List<GameAppInfo> scanTargetGames(Context context) {
        List<GameAppInfo> detectedGames = new ArrayList<>();
        if (context == null) return detectedGames;

        PackageManager pm = context.getPackageManager();
        Set<String> addedPackages = new HashSet<>();

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
                        // Fallback if appInfo fails but launchIntent exists
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

        return detectedGames;
    }
}
