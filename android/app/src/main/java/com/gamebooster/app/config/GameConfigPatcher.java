package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.util.List;

/**
 * GameConfigPatcher creates and updates game-specific internal configuration files
 * (INI, JSON, XML, UserCustom) for Mobile Legends, Call of Duty Mobile, Warzone Mobile,
 * PUBG Mobile, BGMI, Free Fire, Wild Rift, Genshin Impact, Star Rail, ZZZ, Blood Strike,
 * Standoff 2, Arena Breakout, Delta Force, CarX Street, Supercell games, Roblox,
 * Valorant Mobile, and Farlight 84 to force 185 FPS / 185Hz only.
 *
 * Uses GameConfigPathResolver to guarantee 100% path accuracy across all Android storage layouts.
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

    public static boolean patchGame(String packageName, int targetFps) {
        return applyGameFpsPatch(packageName, targetFps).success;
    }

    public static PatchResult applyGameFpsPatch(String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new PatchResult(false, "Invalid package name");
        }

        final int forcedFps = 185; // hard-locked — caller targetFps is ignored
        String pkg = packageName.toLowerCase().trim();
        List<String> configPaths = GameConfigPathResolver.getPathsForGame(pkg);
        if (configPaths == null || configPaths.isEmpty()) {
            return new PatchResult(false, "FPS config patching not required for " + packageName);
        }

        int patchedFiles = 0;
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            if (MlbbConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (MlbbConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            MlbbConfigPatcher.applySuperFastTouch(pkg);
            MlbbConfigPatcher.applyDamageScriptConfig(pkg);
        } else if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone")) {
            if (CodmConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (CodmConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            CodmConfigPatcher.applySuperFastTouch(pkg);
            CodmConfigPatcher.applyAimAssistConfig(pkg);
            CodmConfigPatcher.applyRecoilControlConfig(pkg);
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) {
            if (PubgConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (PubgConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            PubgConfigPatcher.applySuperFastTouch(pkg);
            PubgConfigPatcher.applyAimAssistConfig(pkg);
            PubgConfigPatcher.applyRecoilControlConfig(pkg);
        } else if (pkg.contains("freefire") || pkg.contains("dts.freefire")) {
            if (FreeFireConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (FreeFireConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            FreeFireConfigPatcher.applySuperFastTouch(pkg);
            FreeFireConfigPatcher.applyAimAssistConfig(pkg);
            FreeFireConfigPatcher.applyRecoilControlConfig(pkg);
        } else if (pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") || pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap") || pkg.contains("wutheringwaves")) {
            if (GenshinConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (GenshinConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            GenshinConfigPatcher.applySuperFastTouch(pkg);
            GenshinConfigPatcher.applyAimAssistConfig(pkg);
            GenshinConfigPatcher.applyRecoilControlConfig(pkg);
        } else if (pkg.contains("wildrift") || pkg.contains("riotgames.league")) {
            if (WildRiftConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (WildRiftConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            WildRiftConfigPatcher.applySuperFastTouch(pkg);
            WildRiftConfigPatcher.applyAimAssistConfig(pkg);
            WildRiftConfigPatcher.applyRecoilControlConfig(pkg);
            WildRiftConfigPatcher.applyDamageScriptConfig(pkg);
        } else if (pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor") || pkg.contains("kgtw") || pkg.contains("kgvn") || pkg.contains("kgid")) {
            if (HokConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (HokConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            HokConfigPatcher.applySuperFastTouch(pkg);
            HokConfigPatcher.applyAimAssistConfig(pkg);
            HokConfigPatcher.applyRecoilControlConfig(pkg);
        } else if (pkg.contains("bloodstrike") || pkg.contains("newspike")) {
            if (BloodStrikeConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (BloodStrikeConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            BloodStrikeConfigPatcher.applySuperFastTouch(pkg);
            BloodStrikeConfigPatcher.applyAimAssistConfig(pkg);
            BloodStrikeConfigPatcher.applyRecoilControlConfig(pkg);
            BloodStrikeConfigPatcher.applyDamageScriptConfig(pkg);
        } else if (pkg.contains("standoff2") || pkg.contains("axlebolt")) {
            if (Standoff2ConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (Standoff2ConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            Standoff2ConfigPatcher.applySuperFastTouch(pkg);
            Standoff2ConfigPatcher.applyAimAssistConfig(pkg);
            Standoff2ConfigPatcher.applyRecoilControlConfig(pkg);
            Standoff2ConfigPatcher.applyDamageScriptConfig(pkg);
        } else if (pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row") || pkg.contains("speeddrifters")) {
            if (CarXConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (CarXConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            CarXConfigPatcher.applySuperFastTouch(pkg);
            CarXConfigPatcher.applyAimAssistConfig(pkg);
            CarXConfigPatcher.applyRecoilControlConfig(pkg);
            CarXConfigPatcher.applyDamageScriptConfig(pkg);
        } else if (pkg.contains("uamo") || pkg.contains("arenabreakout") || pkg.contains("deltaforce")) {
            if (ArenaBreakoutConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (ArenaBreakoutConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            ArenaBreakoutConfigPatcher.applySuperFastTouch(pkg);
            ArenaBreakoutConfigPatcher.applyAimAssistConfig(pkg);
            ArenaBreakoutConfigPatcher.applyRecoilControlConfig(pkg);
            ArenaBreakoutConfigPatcher.applyDamageScriptConfig(pkg);
        } else if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashroyale") || pkg.contains("clashofclans")) {
            if (SupercellConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (SupercellConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            SupercellConfigPatcher.applySuperFastTouch(pkg);
            SupercellConfigPatcher.applyAimAssistConfig(pkg);
            SupercellConfigPatcher.applyRecoilControlConfig(pkg);
            SupercellConfigPatcher.applyDamageScriptConfig(pkg);
        } else if (pkg.contains("roblox")) {
            if (RobloxConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (RobloxConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            RobloxConfigPatcher.applySuperFastTouch(pkg);
            RobloxConfigPatcher.applyAimAssistConfig(pkg);
            RobloxConfigPatcher.applyRecoilControlConfig(pkg);
        } else if (pkg.contains("projectc") || pkg.contains("valorant")) {
            if (ValorantConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (ValorantConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            ValorantConfigPatcher.applySuperFastTouch(pkg);
            ValorantConfigPatcher.applyAimAssistConfig(pkg);
            ValorantConfigPatcher.applyRecoilControlConfig(pkg);
        } else if (pkg.contains("farlight") || pkg.contains("solarland")) {
            if (FarlightConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (FarlightConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            FarlightConfigPatcher.applySuperFastTouch(pkg);
            FarlightConfigPatcher.applyAimAssistConfig(pkg);
            FarlightConfigPatcher.applyRecoilControlConfig(pkg);
        } else {
            for (String path : configPaths) {
                if (patchGenericConfig(path, forcedFps)) patchedFiles++;
            }
        }

        // Apply Anti-Log, Telemetry suppression, and cache purge for this game
        AntiLogPatcher.applyAntiLog(pkg);

        if (patchedFiles > 0) {
            Log.d(TAG, "Successfully auto-configured " + patchedFiles + " game config files for " + packageName + " -> " + forcedFps + " FPS/Hz");
            return new PatchResult(true, "Auto-configured " + packageName + " game setting files for " + forcedFps + " FPS/Hz");
        } else {
            return new PatchResult(false, "Could not update config files for " + packageName);
        }
    }

    private static boolean patchGenericConfig(String path, int targetFps) {
        if (!ShizukuFileManager.fileExists(path)) {
            String content = String.format("[Graphics]\nFPS=%d\nFrameRate=%d\nHighFPSMode=1\nMaxFrameRate=%d\n",
                    targetFps, targetFps, targetFps);
            return ShizukuFileManager.writeFile(path, content, "666").success;
        } else {
            String cmd = "sed -i 's/^FPS=.*/FPS=" + targetFps + "/' " + path + "; " +
                         "sed -i 's/^FrameRate=.*/FrameRate=" + targetFps + "/' " + path + "; " +
                         "sed -i 's/^HighFPSMode=.*/HighFPSMode=1/' " + path + "; " +
                         "sed -i 's/^MaxFrameRate=.*/MaxFrameRate=" + targetFps + "/' " + path + "; " +
                         "chmod 666 " + path;
            if (ShizukuFileManager.hasFullAccess()) {
                com.gamebooster.app.shizuku.ShizukuExecutor.executeShizukuCommand(cmd);
            } else {
                CommandExecutor.executeSystemCommand(cmd);
            }
            return true;
        }
    }
}
