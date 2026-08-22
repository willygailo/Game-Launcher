package com.gamebooster.app.config;

import android.content.Context;
import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import com.gamebooster.app.spoofer.DeviceSpooferEngine;
import com.gamebooster.app.spoofer.HardwareMaskEngine;
import com.gamebooster.app.spoofer.SpoofPreferences;
import com.gamebooster.app.spoofer.SpoofProfile;
import java.util.List;

/**
 * GameConfigPatcher creates and updates game-specific internal configuration files
 * (INI, JSON, XML, UserCustom) for Mobile Legends, Call of Duty Mobile, Warzone Mobile,
 * PUBG Mobile, BGMI, Free Fire, Wild Rift, Genshin Impact, Star Rail, ZZZ, Blood Strike,
 * Standoff 2, Arena Breakout, Delta Force, CarX Street, Supercell games, Roblox,
 * Valorant Mobile, and Farlight 84 to force 185 FPS / 185Hz only.
 *
 * Uses GameConfigPathResolver to guarantee 100% path accuracy across all Android storage layouts.
 * Seamlessly integrates with DeviceSpooferEngine to inject spoofed hardware identity (Model,
 * CPU, GPU, RAM) when explicitly enabled and selected by the user.
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
        return applyGameFpsPatch(null, packageName, targetFps).success;
    }

    public static boolean patchGame(Context context, String packageName, int targetFps) {
        return applyGameFpsPatch(context, packageName, targetFps).success;
    }

    public static PatchResult applyGameFpsPatch(String packageName, int targetFps) {
        return applyGameFpsPatch(null, packageName, targetFps);
    }

    public static PatchResult applyGameFpsPatch(Context context, String packageName, int targetFps) {
        if (packageName == null || packageName.trim().isEmpty()) {
            return new PatchResult(false, "Invalid package name");
        }
        // Injection guard (Phase 1.3): reject `;` `'` etc. before anything shell-touching runs
        if (!ShellSafety.isSafePackageName(packageName.trim())) {
            return new PatchResult(false, "Unsafe package name rejected");
        }

        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        String pkg = packageName.toLowerCase().trim();
        List<String> configPaths = GameConfigPathResolver.getPathsForGame(pkg);
        if (configPaths == null || configPaths.isEmpty()) {
            return new PatchResult(false, "FPS config patching not required for " + packageName);
        }

        // Safety net: capture true originals of every candidate config path before any write
        ConfigBackupManager.backupPackage(pkg, configPaths);

        int patchedFiles = 0;
        if (pkg.contains("mobile.legends") || pkg.contains("mobilelegends")) {
            if (MlbbConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (MlbbConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            MlbbConfigPatcher.applySuperFastTouch(pkg);
            MlbbConfigPatcher.applyAimAssistConfig(pkg);
            MlbbConfigPatcher.applyRecoilControlConfig(pkg);
            MlbbConfigPatcher.applyDamageScriptConfig(pkg);
            MlbbConfigPatcher.applyTrackingBulletConfig(pkg);
            MlbbConfigPatcher.applyArmorDefConfig(pkg);
            MlbbConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("cod") || pkg.contains("callofduty") || pkg.contains("warzone")) {
            if (CodmConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (CodmConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            CodmConfigPatcher.applySuperFastTouch(pkg);
            CodmConfigPatcher.applyAimAssistConfig(pkg);
            CodmConfigPatcher.applyRecoilControlConfig(pkg);
            CodmConfigPatcher.applyDamageScriptConfig(pkg);
            CodmConfigPatcher.applyTrackingBulletConfig(pkg);
            CodmConfigPatcher.applyArmorDefConfig(pkg);
            CodmConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("pubg") || pkg.contains("tencent.ig") || pkg.contains("imobile") || pkg.contains("vng.pubgmobile")) {
            if (PubgConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (PubgConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            PubgConfigPatcher.applySuperFastTouch(pkg);
            PubgConfigPatcher.applyAimAssistConfig(pkg);
            PubgConfigPatcher.applyRecoilControlConfig(pkg);
            PubgConfigPatcher.applyDamageScriptConfig(pkg);
            PubgConfigPatcher.applyTrackingBulletConfig(pkg);
            PubgConfigPatcher.applyArmorDefConfig(pkg);
            PubgConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("freefire") || pkg.contains("dts.freefire")) {
            if (FreeFireConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (FreeFireConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            FreeFireConfigPatcher.applySuperFastTouch(pkg);
            FreeFireConfigPatcher.applyAimAssistConfig(pkg);
            FreeFireConfigPatcher.applyRecoilControlConfig(pkg);
            FreeFireConfigPatcher.applyDamageScriptConfig(pkg);
            FreeFireConfigPatcher.applyTrackingBulletConfig(pkg);
            FreeFireConfigPatcher.applyArmorDefConfig(pkg);
            FreeFireConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("genshin") || pkg.contains("mihoyo") || pkg.contains("cognosphere") || pkg.contains("hoyoverse") || pkg.contains("hkrpg") || pkg.contains("nap") || pkg.contains("wutheringwaves")) {
            if (GenshinConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (GenshinConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            GenshinConfigPatcher.applySuperFastTouch(pkg);
            GenshinConfigPatcher.applyAimAssistConfig(pkg);
            GenshinConfigPatcher.applyRecoilControlConfig(pkg);
            GenshinConfigPatcher.applyDamageScriptConfig(pkg);
            GenshinConfigPatcher.applyTrackingBulletConfig(pkg);
            GenshinConfigPatcher.applyArmorDefConfig(pkg);
            GenshinConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("wildrift") || pkg.contains("riotgames.league")) {
            if (WildRiftConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (WildRiftConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            WildRiftConfigPatcher.applySuperFastTouch(pkg);
            WildRiftConfigPatcher.applyAimAssistConfig(pkg);
            WildRiftConfigPatcher.applyRecoilControlConfig(pkg);
            WildRiftConfigPatcher.applyDamageScriptConfig(pkg);
            WildRiftConfigPatcher.applyTrackingBulletConfig(pkg);
            WildRiftConfigPatcher.applyArmorDefConfig(pkg);
            WildRiftConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("sgame") || pkg.contains("levelinfinite") || pkg.contains("arenaofvalor") || pkg.contains("kgtw") || pkg.contains("kgvn") || pkg.contains("kgid")) {
            if (HokConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (HokConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            HokConfigPatcher.applySuperFastTouch(pkg);
            HokConfigPatcher.applyAimAssistConfig(pkg);
            HokConfigPatcher.applyRecoilControlConfig(pkg);
            HokConfigPatcher.applyDamageScriptConfig(pkg);
            HokConfigPatcher.applyTrackingBulletConfig(pkg);
            HokConfigPatcher.applyArmorDefConfig(pkg);
            HokConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("bloodstrike") || pkg.contains("newspike")) {
            if (BloodStrikeConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (BloodStrikeConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            BloodStrikeConfigPatcher.applySuperFastTouch(pkg);
            BloodStrikeConfigPatcher.applyAimAssistConfig(pkg);
            BloodStrikeConfigPatcher.applyRecoilControlConfig(pkg);
            BloodStrikeConfigPatcher.applyDamageScriptConfig(pkg);
            BloodStrikeConfigPatcher.applyTrackingBulletConfig(pkg);
            BloodStrikeConfigPatcher.applyArmorDefConfig(pkg);
            BloodStrikeConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("standoff2") || pkg.contains("axlebolt")) {
            if (Standoff2ConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (Standoff2ConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            Standoff2ConfigPatcher.applySuperFastTouch(pkg);
            Standoff2ConfigPatcher.applyAimAssistConfig(pkg);
            Standoff2ConfigPatcher.applyRecoilControlConfig(pkg);
            Standoff2ConfigPatcher.applyDamageScriptConfig(pkg);
            Standoff2ConfigPatcher.applyTrackingBulletConfig(pkg);
            Standoff2ConfigPatcher.applyArmorDefConfig(pkg);
            Standoff2ConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("carx") || pkg.contains("glofta9hm") || pkg.contains("asphalt") || pkg.contains("r3_row") || pkg.contains("speeddrifters")) {
            if (CarXConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (CarXConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            CarXConfigPatcher.applySuperFastTouch(pkg);
            CarXConfigPatcher.applyAimAssistConfig(pkg);
            CarXConfigPatcher.applyRecoilControlConfig(pkg);
            CarXConfigPatcher.applyDamageScriptConfig(pkg);
            CarXConfigPatcher.applyTrackingBulletConfig(pkg);
            CarXConfigPatcher.applyArmorDefConfig(pkg);
            CarXConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("uamo") || pkg.contains("arenabreakout") || pkg.contains("deltaforce")) {
            if (ArenaBreakoutConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (ArenaBreakoutConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            ArenaBreakoutConfigPatcher.applySuperFastTouch(pkg);
            ArenaBreakoutConfigPatcher.applyAimAssistConfig(pkg);
            ArenaBreakoutConfigPatcher.applyRecoilControlConfig(pkg);
            ArenaBreakoutConfigPatcher.applyDamageScriptConfig(pkg);
            ArenaBreakoutConfigPatcher.applyTrackingBulletConfig(pkg);
            ArenaBreakoutConfigPatcher.applyArmorDefConfig(pkg);
            ArenaBreakoutConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("supercell") || pkg.contains("brawlstars") || pkg.contains("clashroyale") || pkg.contains("clashofclans")) {
            if (SupercellConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (SupercellConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            SupercellConfigPatcher.applySuperFastTouch(pkg);
            SupercellConfigPatcher.applyAimAssistConfig(pkg);
            SupercellConfigPatcher.applyRecoilControlConfig(pkg);
            SupercellConfigPatcher.applyDamageScriptConfig(pkg);
            SupercellConfigPatcher.applyTrackingBulletConfig(pkg);
            SupercellConfigPatcher.applyArmorDefConfig(pkg);
            SupercellConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("roblox")) {
            if (RobloxConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (RobloxConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            RobloxConfigPatcher.applySuperFastTouch(pkg);
            RobloxConfigPatcher.applyAimAssistConfig(pkg);
            RobloxConfigPatcher.applyRecoilControlConfig(pkg);
            RobloxConfigPatcher.applyDamageScriptConfig(pkg);
            RobloxConfigPatcher.applyTrackingBulletConfig(pkg);
            RobloxConfigPatcher.applyArmorDefConfig(pkg);
            RobloxConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("projectc") || pkg.contains("valorant")) {
            if (ValorantConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (ValorantConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            ValorantConfigPatcher.applySuperFastTouch(pkg);
            ValorantConfigPatcher.applyAimAssistConfig(pkg);
            ValorantConfigPatcher.applyRecoilControlConfig(pkg);
            ValorantConfigPatcher.applyDamageScriptConfig(pkg);
            ValorantConfigPatcher.applyTrackingBulletConfig(pkg);
            ValorantConfigPatcher.applyArmorDefConfig(pkg);
            ValorantConfigPatcher.applySpeedBoostConfig(pkg);
        } else if (pkg.contains("farlight") || pkg.contains("solarland")) {
            if (FarlightConfigPatcher.patch(pkg, forcedFps)) patchedFiles++;
            if (FarlightConfigPatcher.patchCompetitive(pkg, forcedFps)) patchedFiles++;
            FarlightConfigPatcher.applySuperFastTouch(pkg);
            FarlightConfigPatcher.applyAimAssistConfig(pkg);
            FarlightConfigPatcher.applyRecoilControlConfig(pkg);
            FarlightConfigPatcher.applyDamageScriptConfig(pkg);
            FarlightConfigPatcher.applyTrackingBulletConfig(pkg);
            FarlightConfigPatcher.applyArmorDefConfig(pkg);
            FarlightConfigPatcher.applySpeedBoostConfig(pkg);
        } else {
            for (String path : configPaths) {
                if (patchGenericConfig(path, forcedFps)) patchedFiles++;
            }
            NativeConfigInjector.injectAllConfigsForPackage(pkg, forcedFps);
        }

        // Apply Ultra Extreme Graphics & Max FPS unlock across all resolved game paths
        NativeConfigInjector.applyUltraExtremeGraphics(pkg, forcedFps);
        int nativeInjected = NativeConfigInjector.injectAllConfigsForPackage(pkg, forcedFps);
        if (nativeInjected > 0) patchedFiles += nativeInjected;

        // Apply Anti-Log, Telemetry suppression, and cache purge for this game
        AntiLogPatcher.applyAntiLog(pkg);

        // Seamlessly inject spoofed hardware identity IF enabled & selected by user
        if (context != null && SpoofPreferences.isSpoofEnabled(context)) {
            String profileId = SpoofPreferences.resolveProfileId(context, pkg);
            if (profileId != null && !profileId.trim().isEmpty()) {
                SpoofProfile spoofProf = DeviceSpooferEngine.getProfileById(profileId);
                if (spoofProf != null) {
                    DeviceSpooferEngine.applySpoofing(context, pkg);
                    HardwareMaskEngine.injectTailoredGameHardwareConfigs(pkg, spoofProf);
                    Log.i(TAG, "Injected hardware spoof identity (" + spoofProf.displayName + ") into " + pkg);
                }
            }
        }

        if (patchedFiles > 0) {
            // Phase 2.3: read the written config files back and assert the forced FPS
            // value actually landed — report "patch confirmed" vs "written but unverified"
            PatchVerifyOutcome verify = verifyWrittenPatches(configPaths, forcedFps);
            Log.d(TAG, "Successfully auto-configured " + patchedFiles + " game config files for "
                    + packageName + " -> " + forcedFps + " FPS/Hz — " + verify.summary);
            return new PatchResult(true, "Auto-configured " + packageName + " game setting files for "
                    + forcedFps + " FPS/Hz — " + verify.summary);
        } else {
            return new PatchResult(false, "Could not update config files for " + packageName);
        }
    }

    private static final class PatchVerifyOutcome {
        final int verifiedFiles;
        final String summary;

        PatchVerifyOutcome(int verifiedFiles, String summary) {
            this.verifiedFiles = verifiedFiles;
            this.summary = summary;
        }
    }

    private static PatchVerifyOutcome verifyWrittenPatches(List<String> configPaths, int targetFps) {
        int verified = 0;
        int checked = 0;
        if (configPaths != null) {
            int cap = Math.min(configPaths.size(), 12);
            for (int i = 0; i < cap; i++) {
                String path = configPaths.get(i);
                if (path == null || path.trim().isEmpty()) continue;
                try {
                    if (!ShizukuFileManager.fileExists(path)) continue;
                    checked++;
                    String content = ShizukuFileManager.readFile(path);
                    if (GameConfigPatchVerifier.verifyFpsInContent(content, targetFps)) {
                        verified++;
                    } else {
                        Log.d(TAG, "Read-back: no FPS value in " + path);
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Read-back failed for " + path, t);
                }
            }
        }
        String summary = GameConfigPatchVerifier.buildVerificationSummary(verified, checked);
        if (checked > 0) {
            Log.d(TAG, "Read-back verification: " + verified + "/" + checked + " files — " + summary);
        }
        return new PatchVerifyOutcome(verified, summary);
    }

    private static boolean patchGenericConfig(String path, int targetFps) {
        String[] genericKeys = {
            "FPS=" + targetFps,
            "FrameRate=" + targetFps,
            "HighFPSMode=1",
            "MaxFrameRate=" + targetFps,
            "TargetFPS=" + targetFps,
            "UnlockFPS=1",
            "AimAssist=1",
            "AimAssistStrength=1000",
            "AimAssistLevel=10",
            "AimPrecision=10",
            "TargetLockSensitivity=1000",
            "CrosshairMagnetism=100.00",
            "AimSnapStrength=100.00",
            "AimMagnetism=100.00",
            "TrackingBullet=1",
            "BulletTracking=1",
            "AutoTrackingBullet=1",
            "MagicBullet=1",
            "HitboxExpansion=50.00",
            "BulletMagnetism=100.00",
            "ProjectileHoming=1",
            "HomingStrength=100.00",
            "PhysicalDefenseBoost=100.00",
            "MagicDefenseBoost=100.00",
            "PhysicalArmor=100.00",
            "MagicResistance=100.00",
            "DamageReductionRatio=0.999",
            "DamageReduction=0.999",
            "IncomingDamageReduction=0.999",
            "ShieldMultiplier=100.00",
            "ShieldCapacity=100.00",
            "ArmorBoost=10000",
            "TenacityRatio=0.999",
            "DamageMultiplier=100.00",
            "BulletDamageBoost=100.00",
            "CriticalHitRate=100",
            "CriticalDamage=1000"
        };
        NativeConfigInjector.injectAimAssist(path);
        NativeConfigInjector.injectTrackingBullet(path);
        NativeConfigInjector.injectArmorDef(path);
        NativeConfigInjector.injectHighDamage(path);
        NativeConfigInjector.injectNoRecoil(path);
        NativeConfigInjector.injectSpeedBoost(path);
        boolean ok = ConfigFileHelper.patchKeys(path, genericKeys, "[Graphics]");
        if (!ok) {
            ConfigBackupManager.restorePath(null, path);
        }
        return ok;
    }
}
