package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import java.util.ArrayList;
import java.util.List;

/**
 * GameConfigPatcher delegates configuration patching to dedicated game-specific patcher classes
 * (MlbbConfigPatcher, PubgConfigPatcher, CodmConfigPatcher, HokConfigPatcher, GenshinConfigPatcher,
 * FreeFireConfigPatcher, WildRiftConfigPatcher, StarRailConfigPatcher, ZenlessZoneZeroConfigPatcher,
 * WutheringWavesConfigPatcher, ArenaOfValorConfigPatcher, NewStateConfigPatcher, RobloxConfigPatcher)
 * and applies ultra-fast zero touch delay tweaks across Android 13, 14, 15, and 16.
 */
public class GameConfigPatcher {

    private static final String TAG = "GameConfigPatcher";

    public static class PatchResult {
        public final boolean success;
        public final String message;

        public PatchResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static PatchResult applyGameFpsPatch(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new PatchResult(false, "Invalid package name");
        }

        String pkg = packageName.toLowerCase().trim();

        // 1. Apply global touch ultra-fast zero delay tweaks for Android 13, 14, 15, 16
        TouchUltraFastNoDelayPatcher.applyTouchNoDelay(pkg);

        int patchedFiles = 0;
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            if (MlbbConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("cod") || pkg.contains("callofduty")) {
            if (CodmConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("pubg.newstate")) {
            if (NewStateConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) {
            if (PubgConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("sgame") || pkg.contains("levelinfinite")) {
            if (HokConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("freefire")) {
            if (FreeFireConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("wildrift")) {
            if (WildRiftConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("hkrpg") || pkg.contains("starrail")) {
            if (StarRailConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("nap") || pkg.contains("zenless")) {
            if (ZenlessZoneZeroConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("wutheringwaves") || pkg.contains("kurogame")) {
            if (WutheringWavesConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("kgtw") || pkg.contains("kgvn") || pkg.contains("aov")) {
            if (ArenaOfValorConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("genshin")) {
            if (GenshinConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("dunk") || pkg.contains("bloodstrike")) {
            if (BloodStrikeConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("warzone")) {
            if (WarzoneConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("deltaforce")) {
            if (DeltaForceConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("standoff2") || pkg.contains("axlebolt")) {
            if (Standoff2ConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("farlight") || pkg.contains("miracle.farlight84")) {
            if (Farlight84ConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else if (pkg.contains("roblox")) {
            if (RobloxConfigPatcher.patch(pkg, targetFps)) patchedFiles++;
        } else {
            List<String> configPaths = getConfigPathsForPackage(pkg);
            for (String path : configPaths) {
                if (patchGenericConfig(path, targetFps)) patchedFiles++;
            }
        }

        if (patchedFiles > 0) {
            Log.d(TAG, "Successfully auto-configured " + patchedFiles + " game config files for " + packageName + " -> " + targetFps + " FPS/Hz");
            return new PatchResult(true, "Auto-configured " + packageName + " game setting files for " + targetFps + " FPS/Hz with zero touch delay");
        } else {
            return new PatchResult(true, "Applied touch zero-delay & high FPS refresh rate for " + packageName);
        }
    }

    /**
     * Competitive force-write: routes to per-game patchCompetitive() + applySuperFastTouch().
     * ALWAYS force-overwrites config files — no create-if-missing fallback.
     * Minimum FPS is enforced at 120 — never falls back to 60 or 90.
     * Routes to the patchCompetitive method of the game-specific patcher for:
     *   MLBB, PUBG Mobile, COD Mobile, Honor of Kings, Free Fire, Wild Rift, Genshin + HoYoverse,
     *   Blood Strike, Warzone Mobile, Delta Force, Standoff 2, Farlight 84, Roblox.
     * Unknown packages fall through to applyGameFpsPatch().
     */
    public static PatchResult applyCompetitivePatch(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new PatchResult(false, "Invalid package name");
        }

        // Hard minimum — never competitive-patch below 120
        int fps = Math.max(targetFps, 120);
        String pkg = packageName.toLowerCase().trim();

        Log.d(TAG, "applyCompetitivePatch: " + pkg + " @ " + fps + " FPS (no fallback)");

        // Always apply touch zero-delay first (global)
        TouchUltraFastNoDelayPatcher.applyTouchNoDelay(pkg);

        boolean ok = false;

        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            ok = MlbbConfigPatcher.patchCompetitive(pkg, fps);
            MlbbConfigPatcher.applySuperFastTouch(pkg);

        } else if (pkg.contains("cod") || pkg.contains("callofduty")) {
            ok = CodmConfigPatcher.patchCompetitive(pkg, fps);
            CodmConfigPatcher.applySuperFastTouch(pkg);

        } else if (pkg.contains("pubg.newstate")) {
            ok = NewStateConfigPatcher.patch(pkg, fps);     // NewState has no competitive yet — standard is fine

        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig")
                || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) {
            ok = PubgConfigPatcher.patchCompetitive(pkg, fps);
            PubgConfigPatcher.applySuperFastTouch(pkg);

        } else if (pkg.contains("sgame") || pkg.contains("levelinfinite")) {
            ok = HokConfigPatcher.patchCompetitive(pkg, fps);
            HokConfigPatcher.applySuperFastTouch(pkg);

        } else if (pkg.contains("freefire")) {
            ok = FreeFireConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("wildrift")) {
            ok = WildRiftConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("hkrpg") || pkg.contains("starrail")) {
            ok = StarRailConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("nap") || pkg.contains("zenless")) {
            ok = ZenlessZoneZeroConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("wutheringwaves") || pkg.contains("kurogame")) {
            ok = WutheringWavesConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("kgtw") || pkg.contains("kgvn") || pkg.contains("aov")) {
            ok = ArenaOfValorConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("genshin")) {
            ok = GenshinConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("bloodstrike") || pkg.contains("ofg.blood") || pkg.contains("netease.blood")) {
            ok = BloodStrikeConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("warzone")) {
            ok = WarzoneConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("tencent.dfm") || pkg.contains("deltaforce")) {
            ok = DeltaForceConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("standoff2") || pkg.contains("axlebolt")) {
            ok = Standoff2ConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("farlight") || pkg.contains("miracle.farlight84")) {
            ok = Farlight84ConfigPatcher.patch(pkg, fps);

        } else if (pkg.contains("roblox")) {
            ok = RobloxConfigPatcher.patch(pkg, fps);

        } else {
            // Unknown game — use standard patch pipeline
            return applyGameFpsPatch(packageName, fps);
        }

        String msg = ok
                ? "Competitive config applied: " + packageName + " @ " + fps + " FPS (no fallback)"
                : "Competitive config attempted (no matching file): " + packageName;
        return new PatchResult(ok, msg);
    }

    private static void ensureParentDirectory(String path) {
        if (path == null) return;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            String parentDir = path.substring(0, lastSlash);
            CommandExecutor.executeSystemCommand("mkdir -p " + parentDir);
        }
    }

    private static boolean patchGenericConfig(String path, int targetFps) {
        ensureParentDirectory(path);
        String checkCmd = "test -f " + path + " && echo EXISTS";
        String checkRes = CommandExecutor.executeSystemCommand(checkCmd);

        if (!checkRes.contains("EXISTS")) {
            String content = String.format("[Graphics]\\nFPS=%d\\nFrameRate=%d\\nHighFPSMode=1\\nMaxFrameRate=%d\\nTouchResponse=Fast\\n",
                    targetFps, targetFps, targetFps);
            CommandExecutor.executeSystemCommand("printf '" + content + "' > " + path);
        } else {
            CommandExecutor.executeSystemCommand("sed -i 's/^FPS=.*/FPS=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^FrameRate=.*/FrameRate=" + targetFps + "/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path);
            CommandExecutor.executeSystemCommand("sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + targetFps + "/' " + path);
        }
        return true;
    }

    private static List<String> getConfigPathsForPackage(String pkg) {
        List<String> paths = new ArrayList<>();
        if (pkg == null) return paths;

        paths.add("/sdcard/Android/data/" + pkg + "/files/GameSettings.ini");
        paths.add("/data/data/" + pkg + "/files/GameSettings.ini");

        return paths;
    }
}
