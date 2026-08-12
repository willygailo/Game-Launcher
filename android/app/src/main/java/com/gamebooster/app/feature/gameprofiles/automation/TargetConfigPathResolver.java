package com.gamebooster.app.feature.gameprofiles.automation;

import java.util.ArrayList;
import java.util.List;

public class TargetConfigPathResolver {

    public static class TargetPathInfo {
        public final String packageName;
        public final String primaryPath;
        public final List<String> alternativePaths = new ArrayList<>();
        public final String primaryConfigFile;

        public TargetPathInfo(String packageName, String primaryPath, String primaryConfigFile) {
            this.packageName = packageName;
            this.primaryPath = primaryPath;
            this.primaryConfigFile = primaryConfigFile;
        }
    }

    public static TargetPathInfo resolveTargetPath(String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) {
            return new TargetPathInfo("com.unknown", "/sdcard/Android/data/com.unknown/files/Config/", "game_performance.cfg");
        }

        String lower = pkg.toLowerCase();

        // 1. Mobile Legends: Bang Bang (MLBB)
        if (lower.contains("mobile.legends") || lower.contains("mobilelegends")) {
            TargetPathInfo info = new TargetPathInfo(
                    pkg,
                    "/sdcard/Android/data/" + pkg + "/files/dragon2017/assets/UI/android/",
                    "HighFrameRate.cfg"
            );
            info.alternativePaths.add("/sdcard/Android/data/" + pkg + "/files/Config/");
            info.alternativePaths.add("/data/data/" + pkg + "/files/");
            return info;
        }

        // 2. PUBG Mobile (PUBGM - Global, KR, VN, IN)
        if (lower.contains("tencent.ig") || lower.contains("pubg") || lower.contains("vng.pubgmobile") || lower.contains("imobile")) {
            TargetPathInfo info = new TargetPathInfo(
                    pkg,
                    "/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/",
                    "UserCustom.ini"
            );
            info.alternativePaths.add("/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/");
            info.alternativePaths.add("/data/data/" + pkg + "/files/");
            return info;
        }

        // 3. Call of Duty Mobile (CODM)
        if (lower.contains("codm") || lower.contains("callofduty")) {
            TargetPathInfo info = new TargetPathInfo(
                    pkg,
                    "/sdcard/Android/data/" + pkg + "/files/Config/",
                    "GraphicsSettings.json"
            );
            info.alternativePaths.add("/sdcard/Android/data/" + pkg + "/files/");
            info.alternativePaths.add("/data/data/" + pkg + "/files/");
            return info;
        }

        // 4. Honor of Kings (HOK) / Arena of Valor
        if (lower.contains("sgame") || lower.contains("honorofkings") || lower.contains("arenaofvalor")) {
            TargetPathInfo info = new TargetPathInfo(
                    pkg,
                    "/sdcard/Android/data/" + pkg + "/files/",
                    "HighFpsConfig.cfg"
            );
            info.alternativePaths.add("/sdcard/Android/data/" + pkg + "/files/Config/");
            return info;
        }

        // 5. Genshin Impact & Honkai Star Rail (miHoYo / Cognosphere)
        if (lower.contains("genshin") || lower.contains("mihoyo") || lower.contains("cognosphere") || lower.contains("hkrpg")) {
            TargetPathInfo info = new TargetPathInfo(
                    pkg,
                    "/sdcard/Android/data/" + pkg + "/files/",
                    "device_profile.xml"
            );
            info.alternativePaths.add("/data/data/" + pkg + "/shared_prefs/");
            return info;
        }

        // 6. Roblox
        if (lower.contains("roblox")) {
            TargetPathInfo info = new TargetPathInfo(
                    pkg,
                    "/sdcard/Android/data/" + pkg + "/files/",
                    "GlobalBasicSettings_13.xml"
            );
            info.alternativePaths.add("/sdcard/Android/data/" + pkg + "/files/Config/");
            return info;
        }

        // 7. Generic Fallback Auto-Detection for any com.* package
        TargetPathInfo fallback = new TargetPathInfo(
                pkg,
                "/sdcard/Android/data/" + pkg + "/files/Config/",
                "game_performance.cfg"
        );
        fallback.alternativePaths.add("/sdcard/Android/data/" + pkg + "/files/");
        fallback.alternativePaths.add("/data/data/" + pkg + "/files/");
        return fallback;
    }
}
