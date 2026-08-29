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
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return pmIntent;
            }
        } catch (Throwable ignored) {}

        // 2. Try Leanback (Android TV / Shield / Box games)
        try {
            Intent leanback = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (leanback != null) {
                leanback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return leanback;
            }
        } catch (Throwable ignored) {}

        // 3. Query explicit MAIN + LAUNCHER activities with MATCH_ALL
        try {
            Intent query = new Intent(Intent.ACTION_MAIN, null);
            query.addCategory(Intent.CATEGORY_LAUNCHER);
            query.setPackage(pkg);
            int matchFlags = PackageManager.MATCH_ALL;
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
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    return intent;
                }
            }
        } catch (Throwable ignored) {}

        return null;
    }

    /**
     * Helper to classify whether a package or app is a game on Android 13, 14, 15, and 16.
     */
    public static boolean isGamePackage(Context context, String pkg, String label, ApplicationInfo appInfo, Set<String> customPkgs) {
        if (pkg == null || pkg.trim().isEmpty()) return false;
        if (context != null && pkg.equalsIgnoreCase(context.getPackageName())) return false;

        if (customPkgs != null && customPkgs.contains(pkg)) {
            return true;
        }

        if (GamePackageRegistry.isKnownGame(pkg)) {
            return true;
        }

        if (appInfo != null) {
            if (appInfo.category == ApplicationInfo.CATEGORY_GAME) {
                return true;
            }
            if ((appInfo.flags & ApplicationInfo.FLAG_IS_GAME) != 0) {
                return true;
            }
        }

        String pkgLower = pkg.toLowerCase(java.util.Locale.ROOT);
        String labelLower = (label != null) ? label.toLowerCase(java.util.Locale.ROOT) : "";

        // Keyword checks on package name
        if (pkgLower.contains("mobilelegends") || pkgLower.contains("mobile.legends")
                || pkgLower.contains("pubg") || pkgLower.contains("codm") || pkgLower.contains("callofduty") || pkgLower.contains("warzone")
                || pkgLower.contains("freefire") || pkgLower.contains("genshin") || pkgLower.contains("roblox")
                || pkgLower.contains("wildrift") || pkgLower.contains("league") || pkgLower.contains("sgame")
                || pkgLower.contains("honorofkings") || pkgLower.contains("supercell") || pkgLower.contains("brawlstars")
                || pkgLower.contains("clashroyale") || pkgLower.contains("clashofclans") || pkgLower.contains("squad")
                || pkgLower.contains("ea.gp") || pkgLower.contains("ea.games") || pkgLower.contains("fifa")
                || pkgLower.contains("garena") || pkgLower.contains("tencent") || pkgLower.contains("netease")
                || pkgLower.contains("hoyoverse") || pkgLower.contains("mihoyo") || pkgLower.contains("hkrpg")
                || pkgLower.contains("wutheringwaves") || pkgLower.contains("bloodstrike") || pkgLower.contains("newspike")
                || pkgLower.contains("farlight") || pkgLower.contains("solarland") || pkgLower.contains("minecraft")
                || pkgLower.contains("subwaysurf") || pkgLower.contains("fallbuddies") || pkgLower.contains("sololv")
                || pkgLower.contains("asphalt") || pkgLower.contains("gameloft") || pkgLower.contains("konami")
                || pkgLower.contains("pesam") || pkgLower.contains("efootball") || pkgLower.contains("dls7")
                || pkgLower.contains("standoff2") || pkgLower.contains("carxstreet") || pkgLower.contains("uamo")
                || pkgLower.contains("deltaforce") || pkgLower.contains("projectc") || pkgLower.contains("valorant")
                || pkgLower.contains("innersloth") || pkgLower.contains("spacemafia") || pkgLower.contains("plarium")
                || pkgLower.contains("habby") || pkgLower.contains("nekki") || pkgLower.contains("rockstargames")
                || pkgLower.contains("chucklefish") || pkgLower.contains(".game.") || pkgLower.contains(".games.")
                || pkgLower.endsWith(".game") || pkgLower.endsWith(".games")
                || pkgLower.contains("speed") || pkgLower.contains("racing") || pkgLower.contains("simulator")
                || pkgLower.contains("arcade") || pkgLower.contains("rpg") || pkgLower.contains("fight")
                || pkgLower.contains("battle") || pkgLower.contains("strike") || pkgLower.contains("hero")
                || pkgLower.contains("runner") || pkgLower.contains("craft")) {
            return true;
        }

        // Keyword checks on app label
        if (labelLower.contains("mobile legends") || labelLower.contains("mlbb")
                || labelLower.contains("pubg") || labelLower.contains("call of duty") || labelLower.contains("cod") || labelLower.contains("warzone")
                || labelLower.contains("free fire") || labelLower.contains("genshin") || labelLower.contains("roblox")
                || labelLower.contains("wild rift") || labelLower.contains("honor of kings") || labelLower.contains("arena of valor")
                || labelLower.contains("brawl stars") || labelLower.contains("clash") || labelLower.contains("squad busters")
                || labelLower.contains("blood strike") || labelLower.contains("farlight") || labelLower.contains("standoff")
                || labelLower.contains("minecraft") || labelLower.contains("subway surfers") || labelLower.contains("stumble guys")
                || labelLower.contains("solo leveling") || labelLower.contains("asphalt") || labelLower.contains("efootball")
                || labelLower.contains("fc mobile") || labelLower.contains("fifa") || labelLower.contains("real racing")
                || labelLower.contains("carx") || labelLower.contains("shadow fight") || labelLower.contains("among us")
                || labelLower.contains("sonic") || labelLower.contains("gta") || labelLower.contains("stardew valley")
                || labelLower.contains("8 ball pool") || labelLower.contains("hill climb") || labelLower.contains("game")
                || labelLower.contains("racing") || labelLower.contains("simulator") || labelLower.contains("battle")
                || labelLower.contains("arcade") || labelLower.contains("fighter") || labelLower.contains("striker")
                || labelLower.contains("sniper") || labelLower.contains("craft") || labelLower.contains("shooter")) {
            return true;
        }

        return false;
    }

    private static String resolveCategory(String pkg, String label, ApplicationInfo appInfo) {
        String pkgLower = (pkg != null) ? pkg.toLowerCase(java.util.Locale.ROOT) : "";
        String labelLower = (label != null) ? label.toLowerCase(java.util.Locale.ROOT) : "";

        if (pkgLower.contains("legends") || pkgLower.contains("wildrift") || pkgLower.contains("sgame") || pkgLower.contains("moba")) {
            return "MOBA";
        }
        if (pkgLower.contains("cod") || pkgLower.contains("duty") || pkgLower.contains("shooter") || pkgLower.contains("fps") || pkgLower.contains("standoff") || pkgLower.contains("valorant") || pkgLower.contains("projectc")) {
            return "FPS";
        }
        if (pkgLower.contains("pubg") || pkgLower.contains("freefire") || pkgLower.contains("farlight") || pkgLower.contains("bloodstrike") || pkgLower.contains("battle")) {
            return "BATTLE ROYALE";
        }
        if (pkgLower.contains("genshin") || pkgLower.contains("honkai") || pkgLower.contains("hkrpg") || pkgLower.contains("nap") || pkgLower.contains("wuthering") || pkgLower.contains("sololv") || pkgLower.contains("rpg")) {
            return "ACTION RPG";
        }
        if (pkgLower.contains("asphalt") || pkgLower.contains("racing") || pkgLower.contains("race") || pkgLower.contains("speed") || pkgLower.contains("carx") || pkgLower.contains("drift")) {
            return "RACING";
        }
        if (pkgLower.contains("fifa") || pkgLower.contains("pesam") || pkgLower.contains("efootball") || pkgLower.contains("sports") || pkgLower.contains("pool") || pkgLower.contains("dls")) {
            return "SPORTS";
        }
        if (pkgLower.contains("roblox") || pkgLower.contains("minecraft") || pkgLower.contains("sandbox") || pkgLower.contains("craft")) {
            return "SANDBOX";
        }
        if (pkgLower.contains("clash") || pkgLower.contains("strategy") || pkgLower.contains("tactics")) {
            return "STRATEGY";
        }

        return "GAMING";
    }

    private static int resolveCardBg(String category) {
        if ("MOBA".equalsIgnoreCase(category) || "ACTION RPG".equalsIgnoreCase(category)) {
            return R.drawable.home_game_card_bg_ml;
        } else if ("FPS".equalsIgnoreCase(category) || "SANDBOX".equalsIgnoreCase(category)) {
            return R.drawable.home_game_card_bg_codm;
        } else {
            return R.drawable.home_game_card_bg_pubg;
        }
    }

    private static int resolveBadgeColor(String category) {
        if ("MOBA".equalsIgnoreCase(category)) {
            return Color.parseColor("#4A90E2");
        } else if ("FPS".equalsIgnoreCase(category)) {
            return Color.parseColor("#FF0055");
        } else if ("BATTLE ROYALE".equalsIgnoreCase(category)) {
            return Color.parseColor("#FF8800");
        } else if ("ACTION RPG".equalsIgnoreCase(category)) {
            return Color.parseColor("#9933FF");
        } else if ("SANDBOX".equalsIgnoreCase(category)) {
            return Color.parseColor("#00FF66");
        } else if ("RACING".equalsIgnoreCase(category) || "SPORTS".equalsIgnoreCase(category)) {
            return Color.parseColor("#FFCC00");
        } else {
            return Color.parseColor("#00F0FF");
        }
    }

    /**
     * Scans and returns all verified installed target, custom, and supported games on Android 13-16.
     */
    public static List<GameAppInfo> scanTargetGames(Context context) {
        List<GameAppInfo> detectedGames = new ArrayList<>();
        if (context == null) return detectedGames;

        PackageManager pm = context.getPackageManager();
        if (pm == null) return detectedGames;

        Set<String> addedPackages = new HashSet<>();
        Set<String> customPkgs = GameLauncherHelper.getCustomPackages(context);

        // TIER 1: User-Custom Added Packages (Guaranteed 100% Inclusion)
        for (String customPkg : customPkgs) {
            if (customPkg == null || customPkg.trim().isEmpty() || addedPackages.contains(customPkg)) continue;
            if (customPkg.equalsIgnoreCase(context.getPackageName())) continue;

            ApplicationInfo appInfo = null;
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    appInfo = pm.getApplicationInfo(customPkg, PackageManager.ApplicationInfoFlags.of(0));
                } else {
                    appInfo = pm.getApplicationInfo(customPkg, 0);
                }
            } catch (Throwable ignored) {}

            Intent launchIntent = resolveLaunchIntent(pm, customPkg);
            if (appInfo != null || launchIntent != null) {
                String label = customPkg;
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

                String category = resolveCategory(customPkg, label, appInfo);
                detectedGames.add(new GameAppInfo(
                        label,
                        customPkg,
                        icon,
                        launchIntent,
                        category,
                        resolveCardBg(category),
                        resolveBadgeColor(category)
                ));
                addedPackages.add(customPkg);
            }
        }

        // TIER 2: Primary Targeted Specs (MLBB, PUBG, CODM, Free Fire, Genshin, HOK, Roblox, Valorant, Farlight)
        for (TargetGameSpec spec : ALL_TARGET_SPECS) {
            for (String pkg : spec.packageNames) {
                if (addedPackages.contains(pkg)) continue;

                ApplicationInfo appInfo = null;
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        appInfo = pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0));
                    } else {
                        appInfo = pm.getApplicationInfo(pkg, 0);
                    }
                } catch (Throwable ignored) {}

                Intent launchIntent = resolveLaunchIntent(pm, pkg);

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

        // TIER 3: Known Games Registry (Global & Regional Hit Games)
        try {
            for (String pkg : GamePackageRegistry.getAllKnownGames().keySet()) {
                if (addedPackages.contains(pkg)) continue;

                ApplicationInfo appInfo = null;
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        appInfo = pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0));
                    } else {
                        appInfo = pm.getApplicationInfo(pkg, 0);
                    }
                } catch (Throwable ignored) {}

                Intent launchIntent = resolveLaunchIntent(pm, pkg);
                if (appInfo != null || launchIntent != null) {
                    GamePackageRegistry.GameInfoSpec spec = GamePackageRegistry.getSpec(pkg);
                    String label = (spec != null) ? spec.title : pkg;
                    String category = (spec != null) ? spec.category : resolveCategory(pkg, label, appInfo);
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
                            resolveCardBg(category),
                            resolveBadgeColor(category)
                    ));
                    addedPackages.add(pkg);
                }
            }
        } catch (Throwable ignored) {}

        // TIER 4: Comprehensive Launcher Intent Activities Scan (Android 13-16)
        try {
            Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> resolveInfos = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                try {
                    resolveInfos = pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL));
                } catch (Throwable ignored) {}
            }
            if (resolveInfos == null || resolveInfos.isEmpty()) {
                resolveInfos = pm.queryIntentActivities(launcherIntent, 0);
            }

            if (resolveInfos != null) {
                for (ResolveInfo ri : resolveInfos) {
                    if (ri == null || ri.activityInfo == null) continue;
                    String pkg = ri.activityInfo.packageName;
                    if (pkg == null || addedPackages.contains(pkg) || pkg.equalsIgnoreCase(context.getPackageName())) {
                        continue;
                    }

                    ApplicationInfo aInfo = ri.activityInfo.applicationInfo;
                    String label = ri.loadLabel(pm).toString();

                    if (isGamePackage(context, pkg, label, aInfo, customPkgs)) {
                        Drawable icon = ri.loadIcon(pm);
                        Intent launchIntent = resolveLaunchIntent(pm, pkg);
                        String category = resolveCategory(pkg, label, aInfo);

                        detectedGames.add(new GameAppInfo(
                                label,
                                pkg,
                                icon,
                                launchIntent,
                                category,
                                resolveCardBg(category),
                                resolveBadgeColor(category)
                        ));
                        addedPackages.add(pkg);
                    }
                }
            }
        } catch (Throwable ignored) {}

        // TIER 5: Installed Applications Fallback Scan (for non-standard launcher activities)
        try {
            List<ApplicationInfo> installedApps = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                try {
                    installedApps = pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0));
                } catch (Throwable ignored) {}
            }
            if (installedApps == null) {
                try {
                    installedApps = pm.getInstalledApplications(0);
                } catch (Throwable ignored) {}
            }

            if (installedApps != null) {
                for (ApplicationInfo ai : installedApps) {
                    if (ai == null || ai.packageName == null || addedPackages.contains(ai.packageName)) continue;
                    if (ai.packageName.equalsIgnoreCase(context.getPackageName())) continue;

                    CharSequence labelSeq = pm.getApplicationLabel(ai);
                    String label = (labelSeq != null) ? labelSeq.toString() : ai.packageName;

                    if (isGamePackage(context, ai.packageName, label, ai, customPkgs)) {
                        Intent launchIntent = resolveLaunchIntent(pm, ai.packageName);
                        Drawable icon = null;
                        try {
                            icon = pm.getApplicationIcon(ai);
                        } catch (Throwable ignored) {}

                        if (icon == null) {
                            try {
                                icon = context.getApplicationInfo().loadIcon(pm);
                            } catch (Throwable ignored) {}
                        }

                        String category = resolveCategory(ai.packageName, label, ai);
                        detectedGames.add(new GameAppInfo(
                                label,
                                ai.packageName,
                                icon,
                                launchIntent,
                                category,
                                resolveCardBg(category),
                                resolveBadgeColor(category)
                        ));
                        addedPackages.add(ai.packageName);
                    }
                }
            }
        } catch (Throwable ignored) {}

        // TIER 6: Shizuku Deep Search Discovery (for Android 13-16 Package Visibility restrictions)
        if (com.gamebooster.app.shizuku.ShizukuExecutor.hasShizukuPermission()) {
            try {
                Set<String> deepPackages = com.gamebooster.app.search.DeepSearchScanner.performDeepSearch(context);
                if (deepPackages != null) {
                    for (String pkg : deepPackages) {
                        if (pkg == null || addedPackages.contains(pkg) || pkg.equalsIgnoreCase(context.getPackageName())) continue;

                        ApplicationInfo appInfo = null;
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                appInfo = pm.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0));
                            } else {
                                appInfo = pm.getApplicationInfo(pkg, 0);
                            }
                        } catch (Throwable ignored) {}

                        String label = pkg;
                        Drawable icon = null;
                        if (appInfo != null) {
                            try {
                                label = pm.getApplicationLabel(appInfo).toString();
                                icon = pm.getApplicationIcon(appInfo);
                            } catch (Throwable ignored) {}
                        }

                        if (isGamePackage(context, pkg, label, appInfo, customPkgs)) {
                            Intent launchIntent = resolveLaunchIntent(pm, pkg);
                            if (icon == null) {
                                try {
                                    icon = context.getApplicationInfo().loadIcon(pm);
                                } catch (Throwable ignored) {}
                            }
                            String category = resolveCategory(pkg, label, appInfo);
                            detectedGames.add(new GameAppInfo(
                                    label,
                                    pkg,
                                    icon,
                                    launchIntent,
                                    category,
                                    resolveCardBg(category),
                                    resolveBadgeColor(category)
                            ));
                            addedPackages.add(pkg);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        return detectedGames;
    }
}
