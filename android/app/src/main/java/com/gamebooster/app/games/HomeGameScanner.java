package com.gamebooster.app.games;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.gamebooster.app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ultra-fast dedicated scanner focused on MLBB, PUBG Mobile, CODM, Free Fire, Genshin, HOK, Roblox,
 * Valorant, Farlight, and all installed device games.
 * Resolves verified, explicit launch intents with FLAG_INCLUDE_STOPPED_PACKAGES for guaranteed opening.
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
            Color.parseColor("#FF0055")
    );

    // 9. Farlight 84
    private static final TargetGameSpec FARLIGHT_SPEC = new TargetGameSpec(
            new String[]{
                    "com.miraclegames.farlight84",
                    "com.lilithgames.farlight84"
            },
            "Farlight 84",
            "HERO SHOOTER",
            R.drawable.home_game_card_bg_pubg,
            Color.parseColor("#FFAA00")
    );

    private static final List<TargetGameSpec> ALL_TARGET_SPECS = Arrays.asList(
            MLBB_SPEC,
            PUBG_SPEC,
            CODM_SPEC,
            FREEFIRE_SPEC,
            GENSHIN_SPEC,
            HOK_SPEC,
            ROBLOX_SPEC,
            VALORANT_SPEC,
            FARLIGHT_SPEC
    );

    /**
     * Resolves a guaranteed working, explicit launch Intent with appropriate flags for the target package.
     */
    public static Intent resolveLaunchIntent(PackageManager pm, String pkg) {
        if (pm == null || pkg == null || pkg.trim().isEmpty()) return null;

        // 1. Try standard PackageManager launch intent
        try {
            Intent pmIntent = pm.getLaunchIntentForPackage(pkg);
            if (pmIntent != null) {
                pmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                return pmIntent;
            }
        } catch (Throwable ignored) {}

        // 2. Try Leanback (Android TV / Shield / Box games)
        try {
            Intent leanback = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (leanback != null) {
                leanback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                return leanback;
            }
        } catch (Throwable ignored) {}

        // 3. Query explicit MAIN + LAUNCHER activities with MATCH_ALL
        try {
            Intent query = new Intent(Intent.ACTION_MAIN, null);
            query.addCategory(Intent.CATEGORY_LAUNCHER);
            query.setPackage(pkg);
            int matchFlags = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                matchFlags = PackageManager.MATCH_ALL;
            }
            List<ResolveInfo> list = pm.queryIntentActivities(query, matchFlags);
            if ((list == null || list.isEmpty()) && matchFlags != 0) {
                list = pm.queryIntentActivities(query, 0);
            }
            if (list != null && !list.isEmpty() && list.get(0).activityInfo != null) {
                ActivityInfo aInfo = list.get(0).activityInfo;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new ComponentName(aInfo.packageName, aInfo.name));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                return intent;
            }
        } catch (Throwable ignored) {}

        // 4. Query CATEGORY_INFO activities
        try {
            Intent queryInfo = new Intent(Intent.ACTION_MAIN, null);
            queryInfo.addCategory(Intent.CATEGORY_INFO);
            queryInfo.setPackage(pkg);
            List<ResolveInfo> listInfo = pm.queryIntentActivities(queryInfo, 0);
            if (listInfo != null && !listInfo.isEmpty() && listInfo.get(0).activityInfo != null) {
                ActivityInfo aInfo = listInfo.get(0).activityInfo;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_INFO);
                intent.setComponent(new ComponentName(aInfo.packageName, aInfo.name));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                return intent;
            }
        } catch (Throwable ignored) {}

        // 5. Query any exported Activity declared in PackageInfo
        try {
            android.content.pm.PackageInfo pi = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES);
            if (pi != null && pi.activities != null && pi.activities.length > 0) {
                for (ActivityInfo ai : pi.activities) {
                    if (ai != null && ai.exported && ai.name != null) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setComponent(new ComponentName(pkg, ai.name));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        return intent;
                    }
                }
                // If none explicitly exported, use the first activity
                ActivityInfo firstAi = pi.activities[0];
                if (firstAi != null && firstAi.name != null) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName(pkg, firstAi.name));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    return intent;
                }
            }
        } catch (Throwable ignored) {}

        return null;
    }

    /**
     * Scans and returns all verified installed target and supported games on the device.
     */
    public static List<GameAppInfo> scanTargetGames(Context context) {
        List<GameAppInfo> detectedGames = new ArrayList<>();
        if (context == null) return detectedGames;

        PackageManager pm = context.getPackageManager();
        Set<String> addedPackages = new HashSet<>();

        // 1. Primary Targeted Specs Scan (MLBB, PUBG, CODM, Free Fire, Genshin, HOK, Roblox, Valorant, Farlight)
        for (TargetGameSpec spec : ALL_TARGET_SPECS) {
            for (String pkg : spec.packageNames) {
                if (addedPackages.contains(pkg)) continue;

                ApplicationInfo appInfo = null;
                try {
                    appInfo = pm.getApplicationInfo(pkg, 0);
                } catch (Throwable ignored) {}

                Intent launchIntent = resolveLaunchIntent(pm, pkg);

                // Add ONLY if the app is actually installed on the physical device
                if (appInfo != null || launchIntent != null) {
                    String label = spec.defaultTitle;
                    Drawable icon = null;
                    try {
                        if (appInfo != null) {
                            label = pm.getApplicationLabel(appInfo).toString();
                            icon = pm.getApplicationIcon(appInfo);
                        } else if (launchIntent != null) {
                            icon = pm.getActivityIcon(launchIntent);
                        }
                    } catch (Throwable ignored) {}

                    if (icon == null) {
                        try {
                            icon = context.getApplicationInfo().loadIcon(pm);
                        } catch (Throwable ignored) {}
                    }

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
                }
            }
        }

        // 2. Secondary Scan: Known Games Registry
        try {
            for (String pkg : GamePackageRegistry.getAllKnownGames().keySet()) {
                if (addedPackages.contains(pkg)) continue;

                ApplicationInfo appInfo = null;
                try {
                    appInfo = pm.getApplicationInfo(pkg, 0);
                } catch (Throwable ignored) {}

                Intent launchIntent = resolveLaunchIntent(pm, pkg);
                if (appInfo != null || launchIntent != null) {
                    GamePackageRegistry.GameInfoSpec info = GamePackageRegistry.getSpec(pkg);
                    String label = info != null ? info.title : pkg;
                    String category = info != null ? info.category : "GAMING";
                    Drawable icon = null;
                    try {
                        if (appInfo != null) {
                            label = pm.getApplicationLabel(appInfo).toString();
                            icon = pm.getApplicationIcon(appInfo);
                        }
                    } catch (Throwable ignored) {}

                    if (icon == null) {
                        try {
                            icon = context.getApplicationInfo().loadIcon(pm);
                        } catch (Throwable ignored) {}
                    }

                    detectedGames.add(new GameAppInfo(
                            label,
                            pkg,
                            icon,
                            launchIntent,
                            category,
                            R.drawable.home_game_card_bg_ml,
                            Color.parseColor("#00F0FF")
                    ));
                    addedPackages.add(pkg);
                }
            }
        } catch (Throwable ignored) {}

        // 3. Tertiary Scan: Query Launcher Intent Activities with CATEGORY_GAME
        try {
            Intent gameIntent = new Intent(Intent.ACTION_MAIN, null);
            gameIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(gameIntent, 0);
            if (resolveInfos != null) {
                for (ResolveInfo ri : resolveInfos) {
                    if (ri == null || ri.activityInfo == null) continue;
                    String pkg = ri.activityInfo.packageName;
                    if (pkg == null || addedPackages.contains(pkg) || pkg.equalsIgnoreCase(context.getPackageName())) {
                        continue;
                    }

                    ApplicationInfo aInfo = ri.activityInfo.applicationInfo;
                    boolean isGame = false;
                    if (aInfo != null) {
                        if ((aInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0) isGame = true;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            if (aInfo.category == ApplicationInfo.CATEGORY_GAME) isGame = true;
                        }
                    }

                    if (isGame || GameLauncherHelper.getCustomPackages(context).contains(pkg)) {
                        String label = ri.loadLabel(pm).toString();
                        Drawable icon = ri.loadIcon(pm);
                        Intent launchIntent = resolveLaunchIntent(pm, pkg);

                        detectedGames.add(new GameAppInfo(
                                label,
                                pkg,
                                icon,
                                launchIntent,
                                "GAME",
                                R.drawable.home_game_card_bg_pubg,
                                Color.parseColor("#FFCC00")
                        ));
                        addedPackages.add(pkg);
                    }
                }
            }
        } catch (Throwable ignored) {}

        return detectedGames;
    }
}
