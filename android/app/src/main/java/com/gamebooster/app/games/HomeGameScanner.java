package com.gamebooster.app.games;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import com.gamebooster.app.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Universal Game Scanner for Game Launcher Pro V2.0.
 *
 * Scans all known eSports and high-FPS games (MLBB, PUBGM, CODM, HOK, Genshin, Roblox,
 * Free Fire, Wild Rift, Standoff 2, Blood Strike, Farlight 84, Supercell titles, etc.)
 * and falls back to dynamic ApplicationInfo.CATEGORY_GAME system scanning.
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

    // 1. Mobile Legends: Bang Bang
    private static final TargetGameSpec MLBB_SPEC = new TargetGameSpec(
            new String[]{
                    "com.mobile.legends", "com.mobile.legends.vng", "com.mobile.legends.kr",
                    "com.mobile.legends.jp", "com.mobilelegends.mi", "com.mobilelegends.na"
            },
            "Mobile Legends: Bang Bang", "MOBA", R.drawable.home_game_card_bg_ml, Color.parseColor("#4A90E2")
    );

    // 2. PUBG Mobile & BGMI
    private static final TargetGameSpec PUBG_SPEC = new TargetGameSpec(
            new String[]{
                    "com.tencent.ig", "com.pubg.imobile", "com.pubg.krmobile",
                    "com.vng.pubgmobile", "com.tencent.iglite", "com.pubg.newstate", "com.tencent.tmgp.pubgm"
            },
            "PUBG Mobile", "BATTLE ROYALE", R.drawable.home_game_card_bg_pubg, Color.parseColor("#FF8800")
    );

    // 3. Call of Duty: Mobile
    private static final TargetGameSpec CODM_SPEC = new TargetGameSpec(
            new String[]{
                    "com.activision.callofduty.shooter", "com.garena.game.codm",
                    "com.tencent.tmgp.kr.codm", "com.tencent.tmgp.cod", "com.vng.codmvn"
            },
            "Call of Duty: Mobile", "FPS", R.drawable.home_game_card_bg_codm, Color.parseColor("#FF0055")
    );

    // 4. Honor of Kings (HOK) / Arena of Valor
    private static final TargetGameSpec HOK_SPEC = new TargetGameSpec(
            new String[]{
                    "com.levelinfinite.sgameGlobal", "com.tencent.tmgp.sgame",
                    "com.garena.game.kgtw", "com.garena.game.kgvn", "com.garena.game.kgid"
            },
            "Honor of Kings", "MOBA", R.drawable.home_game_card_bg_ml, Color.parseColor("#FFD700")
    );

    // 5. Genshin Impact & Star Rail
    private static final TargetGameSpec GENSHIN_SPEC = new TargetGameSpec(
            new String[]{
                    "com.miHoYo.GenshinImpact", "com.cognosphere.GenshinImpact",
                    "com.HoYoverse.hkrpgoversea", "com.miHoYo.hkrpg", "com.HoYoverse.nap"
            },
            "Genshin Impact", "ACTION RPG", R.drawable.home_game_card_bg_ml, Color.parseColor("#9B51E0")
    );

    // 6. Roblox
    private static final TargetGameSpec ROBLOX_SPEC = new TargetGameSpec(
            new String[]{"com.roblox.client"},
            "Roblox", "SANDBOX", R.drawable.home_game_card_bg_ml, Color.parseColor("#00E5FF")
    );

    // 7. Free Fire & Free Fire MAX
    private static final TargetGameSpec FREEFIRE_SPEC = new TargetGameSpec(
            new String[]{"com.dts.freefireth", "com.dts.freefiremax"},
            "Free Fire MAX", "BATTLE ROYALE", R.drawable.home_game_card_bg_pubg, Color.parseColor("#FF6D00")
    );

    // 8. Wild Rift
    private static final TargetGameSpec WILDRIFT_SPEC = new TargetGameSpec(
            new String[]{"com.riotgames.league.wildrift", "com.riotgames.league.wildrifttw", "com.riotgames.league.wildriftvn"},
            "Wild Rift", "MOBA", R.drawable.home_game_card_bg_ml, Color.parseColor("#29B6F6")
    );

    private static final TargetGameSpec[] TARGET_SPECS = new TargetGameSpec[]{
            MLBB_SPEC, PUBG_SPEC, CODM_SPEC, HOK_SPEC, GENSHIN_SPEC, ROBLOX_SPEC, FREEFIRE_SPEC, WILDRIFT_SPEC
    };

    /**
     * Scans and returns all installed target eSports games, known registry games, and CATEGORY_GAME apps.
     */
    public static List<GameAppInfo> scanTargetGames(Context context) {
        List<GameAppInfo> detectedGames = new ArrayList<>();
        if (context == null) return detectedGames;

        PackageManager pm = context.getPackageManager();
        Set<String> addedPackages = new HashSet<>();

        // Phase 1: High-priority curated target specs (MLBB, PUBGM, CODM, HOK, Genshin, Roblox, Free Fire, Wild Rift)
        for (TargetGameSpec spec : TARGET_SPECS) {
            for (String pkg : spec.packageNames) {
                if (addedPackages.contains(pkg)) continue;

                Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    try {
                        ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                        String label = pm.getApplicationLabel(appInfo).toString();
                        Drawable icon = pm.getApplicationIcon(appInfo);

                        detectedGames.add(new GameAppInfo(
                                label, pkg, icon, launchIntent,
                                spec.gameType, spec.cardBgRes, spec.badgeColor
                        ));
                        addedPackages.add(pkg);
                    } catch (Throwable ignored) {
                        Drawable defaultIcon = context.getApplicationInfo().loadIcon(pm);
                        detectedGames.add(new GameAppInfo(
                                spec.defaultTitle, pkg, defaultIcon, launchIntent,
                                spec.gameType, spec.cardBgRes, spec.badgeColor
                        ));
                        addedPackages.add(pkg);
                    }
                }
            }
        }

        // Phase 2: Registry Known Games scan
        Map<String, GamePackageRegistry.GameInfoSpec> knownMap = GamePackageRegistry.getAllKnownGames();
        for (Map.Entry<String, GamePackageRegistry.GameInfoSpec> entry : knownMap.entrySet()) {
            String pkg = entry.getKey();
            if (addedPackages.contains(pkg)) continue;

            Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
            if (launchIntent != null) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                    String label = pm.getApplicationLabel(appInfo).toString();
                    Drawable icon = pm.getApplicationIcon(appInfo);

                    detectedGames.add(new GameAppInfo(
                            label, pkg, icon, launchIntent,
                            entry.getValue().category, R.drawable.home_game_card_bg_ml, Color.parseColor("#00E5FF")
                    ));
                    addedPackages.add(pkg);
                } catch (Throwable ignored) {}
            }
        }

        // Phase 3: Dynamic System CATEGORY_GAME Fallback Scanner
        try {
            List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo app : installedApps) {
                if (app == null || addedPackages.contains(app.packageName)) continue;

                boolean isGame = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (app.category == ApplicationInfo.CATEGORY_GAME) isGame = true;
                }
                if ((app.flags & ApplicationInfo.FLAG_IS_GAME) != 0) isGame = true;

                if (isGame) {
                    Intent launchIntent = pm.getLaunchIntentForPackage(app.packageName);
                    if (launchIntent != null) {
                        String label = pm.getApplicationLabel(app).toString();
                        Drawable icon = pm.getApplicationIcon(app);

                        detectedGames.add(new GameAppInfo(
                                label, app.packageName, icon, launchIntent,
                                "GAME", R.drawable.home_game_card_bg_ml, Color.parseColor("#00E5FF")
                        ));
                        addedPackages.add(app.packageName);
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Dynamic CATEGORY_GAME fallback scan exception: " + t.getMessage());
        }

        Log.i(TAG, "Universal Game Scanner completed: " + detectedGames.size() + " games detected.");
        return detectedGames;
    }
}
