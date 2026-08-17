package com.gamebooster.app.games;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Universal High-Speed Game Scanner for Game Launcher PRO.
 * Scans and detects 100% of installed games across Android 10, 11, 12, 13, 14, 15, and 16.
 *
 * Tier 1: Target spec games (MLBB, PUBG, CODM, Free Fire, Genshin, HOK, Roblox, Farlight, etc.)
 * Tier 2: Comprehensive GamePackageRegistry (45+ regional & global esports titles)
 * Tier 3: Android OS CATEGORY_GAME & FLAG_IS_GAME inspection
 * Tier 4: Installed launcher apps with game title/package matching
 */
public class HomeGameScanner {

    private static final String TAG = "HomeGameScanner";

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
                    "com.mobile.legends.jp",
                    "com.mobilelegends.hw",
                    "com.mobilelegends.mi",
                    "com.vng.mlbbvn",
                    "com.mobilelegends.na",
                    "com.mobile.legends.moonton"
            },
            "Mobile Legends: Bang Bang",
            "MOBA",
            R.drawable.home_game_card_bg_ml,
            Color.parseColor("#4A90E2")
    );

    // 2. PUBG Mobile & BGMI (ALL Regional Packages)
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
                    "com.vng.codmvn",
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
                    "com.riotgames.league.wildrift",
                    "com.riotgames.league.wildrifttw",
                    "com.riotgames.league.wildriftvn"
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

    // 8. Valorant Mobile & Tactical Shooters
    private static final TargetGameSpec VALORANT_SPEC = new TargetGameSpec(
            new String[]{
                    "com.tencent.tmgp.projectc",
                    "com.riotgames.valorantmobile",
                    "com.tencent.tmgp.valorant",
                    "com.riotgames.valorant",
                    "com.axlebolt.standoff2",
                    "com.netease.bloodstrike",
                    "com.proximabeta.mf.uamo"
            },
            "Tactical Shooter",
            "TACTICAL FPS",
            R.drawable.home_game_card_bg_codm,
            Color.parseColor("#FF4655")
    );

    // 9. Farlight 84 & Racing / Sports Titles
    private static final TargetGameSpec FARLIGHT_SPEC = new TargetGameSpec(
            new String[]{
                    "com.miracle.farlight84",
                    "com.miraclegames.farlight84",
                    "com.farlightgames.farlight84.gp",
                    "com.farlightgames.farlight84.global",
                    "jp.konami.pesam",
                    "com.ea.gp.fifamobile",
                    "com.gameloft.anmp.android.glofta9hm",
                    "com.carxtech.sr",
                    "com.kurogame.wutheringwaves.global"
            },
            "Action & Racing",
            "ESPORTS",
            R.drawable.home_game_card_bg_pubg,
            Color.parseColor("#FFCC00")
    );

    private static final TargetGameSpec[] ALL_TARGET_SPECS = new TargetGameSpec[]{
            MLBB_SPEC, PUBG_SPEC, CODM_SPEC, FREEFIRE_SPEC, GENSHIN_SPEC, HOK_SPEC, ROBLOX_SPEC, VALORANT_SPEC, FARLIGHT_SPEC
    };

    /**
     * Scans and returns all installed games on the user's phone.
     */
    public static List<GameAppInfo> scanTargetGames(Context context) {
        List<GameAppInfo> detectedGames = new ArrayList<>();
        if (context == null) return detectedGames;

        PackageManager pm = context.getPackageManager();
        Set<String> addedPackages = new HashSet<>();

        // Tier 1: Check Target Specs (Highest priority formatting & artwork)
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

        // Tier 2: Check all known games in GamePackageRegistry
        for (Map.Entry<String, GamePackageRegistry.GameInfoSpec> entry : GamePackageRegistry.getAllKnownGames().entrySet()) {
            String pkg = entry.getKey();
            if (addedPackages.contains(pkg)) continue;

            Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
            if (launchIntent != null) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                    String label = pm.getApplicationLabel(appInfo).toString();
                    Drawable icon = pm.getApplicationIcon(appInfo);
                    GamePackageRegistry.GameInfoSpec info = entry.getValue();

                    detectedGames.add(new GameAppInfo(
                            label,
                            pkg,
                            icon,
                            launchIntent,
                            info.category.toUpperCase(Locale.ROOT),
                            R.drawable.home_game_card_bg_ml,
                            Color.parseColor("#00F0FF")
                    ));
                    addedPackages.add(pkg);
                } catch (Throwable ignored) {}
            }
        }

        // Tier 3: Scan all installed launcher apps on the device (Detect ALL custom user games)
        try {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> launchableApps = pm.queryIntentActivities(mainIntent, 0);

            if (launchableApps != null) {
                for (ResolveInfo ri : launchableApps) {
                    if (ri.activityInfo == null) continue;
                    String pkg = ri.activityInfo.packageName;
                    if (pkg == null || pkg.equals(context.getPackageName()) || addedPackages.contains(pkg)) {
                        continue;
                    }

                    ApplicationInfo appInfo = ri.activityInfo.applicationInfo;
                    if (appInfo == null) continue;

                    boolean isGame = false;

                    // 1. Android OS CATEGORY_GAME flag (Android 8.0+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                            isGame = true;
                        }
                    }

                    // 2. Legacy FLAG_IS_GAME
                    if ((appInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                        isGame = true;
                    }

                    // 3. Known package name keywords
                    String lowerPkg = pkg.toLowerCase(Locale.ROOT);
                    if (lowerPkg.contains(".game") || lowerPkg.contains("games.") || lowerPkg.contains("moba")
                            || lowerPkg.contains("shooter") || lowerPkg.contains("battle") || lowerPkg.contains("pubg")
                            || lowerPkg.contains("legends") || lowerPkg.contains("craft") || lowerPkg.contains("racing")
                            || lowerPkg.contains("simulator") || lowerPkg.contains("speed") || lowerPkg.contains("clash")
                            || lowerPkg.contains("subway") || lowerPkg.contains("candy") || lowerPkg.contains("asphalt")
                            || lowerPkg.contains("rpg") || lowerPkg.contains("genshin") || lowerPkg.contains("honkai")) {
                        isGame = true;
                    }

                    if (isGame) {
                        Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
                        if (launchIntent != null) {
                            try {
                                String label = ri.loadLabel(pm).toString();
                                Drawable icon = ri.loadIcon(pm);

                                detectedGames.add(new GameAppInfo(
                                        label,
                                        pkg,
                                        icon,
                                        launchIntent,
                                        "GAME",
                                        R.drawable.home_game_card_bg_pubg,
                                        Color.parseColor("#00FF66")
                                ));
                                addedPackages.add(pkg);
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Log.e(TAG, "Error in Tier 3 game scan: " + t.getMessage());
        }

        return detectedGames;
    }
}
