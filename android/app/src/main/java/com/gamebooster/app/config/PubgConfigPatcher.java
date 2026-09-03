package com.gamebooster.app.config;

import android.util.Log;
import com.gamebooster.app.engine.CommandExecutor;
import com.gamebooster.app.shizuku.ShizukuExecutor;
import com.gamebooster.app.shizuku.ShizukuFileManager;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * PubgConfigPatcher manages internal config files for PUBG Mobile, BGMI, and regional variants.
 *
 * Two patching modes:
 *  - patch()            → standard patch: in-memory key/CVar upserting
 *  - patchCompetitive() → competitive force-write: overwrites all paths atomically via ConfigFileHelper
 */
public class PubgConfigPatcher {


    // ── 2026 Lock Methods ─────────────────────────────────────────────────────

    /**
     * Damage Lock Max — 2026 Edition.
     * Locks DPS at maximum via config-file injection into all resolved game paths.
     * Ban-safe: config-file writes only.
     */
    public static void applyDamageLockMax(String packageName) {
        CommonConfigTuningInjector.applyDamageLockMax(packageName);
    }

    /**
     * Aim Assist Lock Max — 2026 Edition.
     * Locks aim tracking at maximum magnetism + zero deadzone via config injection.
     * Ban-safe: config-file writes only.
     */
    public static void applyAimAssistLockMax(String packageName) {
        CommonConfigTuningInjector.applyAimAssistLockMax(packageName);
    }

    /**
     * Vulkan Pipeline Prime — 2026 Edition.
     * Pre-warms Vulkan pipeline cache + async shader compile. Eliminates mid-match stutter.
     * Ban-safe: config-file writes only.
     */
    public static void applyVulkanPipelinePrime(String packageName) {
        CommonConfigTuningInjector.applyVulkanPipelinePrime(packageName);
    }

    /**
     * Anti-Telemetry Safe — 2026 Edition.
     * Disables game-internal analytics/crash reporters only. Never touches anti-cheat.
     * Ban-safe: config-file writes only.
     */
    public static void applyAntiTelemetrySafe(String packageName) {
        CommonConfigTuningInjector.applyAntiTelemetrySafe(packageName);
    }

    /**
     * Network Lag Compensation — 2026 Edition.
     * Client-side lag comp + 64-tick + jitter buffer keys. UE4/5 games only (silently ignored on Unity).
     * Ban-safe: config-file writes only.
     */
    public static void applyNetworkLagCompensation(String packageName) {
        CommonConfigTuningInjector.applyNetworkLagCompensation(packageName);
    }


    public static void applyNoScopeAimbot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectNoScopeAimbot(path);
        }
    }

    public static void applyAllScopeAimbot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectAllScopeAimbot(path);
        }
    }

    public static void applyLongRangeScopeHeadshot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectLongRangeScopeHeadshot(path);
        }
    }

    public static void applyMidRangeAutoHeadshot(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMidRangeAutoHeadshot(path);
        }
    }

    public static void applyPubgmFastAttackSpeed(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmFastAttackSpeed(path);
        }
    }

    public static void applyFastLootAndWeaponSwap(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectFastLootAndWeaponSwap(path);
        }
    }

    public static void applyInstantSprintTurbo(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectInstantSprintTurbo(path);
        }
    }

    public static void applyMultiRangeHeadshotCalibration(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectMultiRangeHeadshotCalibration(path);
        }
    }

    public static void applyUniversalZeroDelaySkillTapAllHero(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectUniversalZeroDelaySkillTapAllHero(path);
        }
    }

    public static void applyPubgmAllWeaponMaxDamage2026(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmAllWeaponMaxDamage2026(path);
        }
    }

    public static void applyPubgmUltraAimbot2026(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmUltraAimbot2026(path);
        }
    }

    public static void applyDamage10000AttackSpeedMax(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmDamage10000AttackSpeedMax(path);
        }
        Log.i(TAG, "PUBGM Damage10000AttackSpeedMax applied for " + packageName);
    }

    public static void applyPubgmBallisticsVelocityPenetration(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmBallisticsVelocityPenetration(path);
        }
    }

    public static void applyFastLoadAsyncStreaming(String packageName) {
        if (packageName == null) return;
        for (String path : getConfigPaths(packageName)) {
            NativeConfigInjector.injectPubgmFastLoadAsyncStreaming(path);
        }
    }

    private static final String TAG = "PubgConfigPatcher";

    // ─── Standard Patch ───────────────────────────────────────────────────────

    public static boolean patch(String packageName, int targetFps) {
        if (packageName == null) return false;
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        List<String> paths = getConfigPaths(packageName);
        int patched = 0;
        for (String path : paths) {
            if (applyPatch(path, forcedFps)) patched++;
        }
        patchActiveSavBinary(packageName, forcedFps);
        Log.i(TAG, "PUBGM patch: " + patched + " files for " + packageName + " @ " + forcedFps + "fps");
        return patched > 0;
    }

    // ─── UltraExtreme 144fps SuperSmooth Patch ───────────────────────────────

    public static boolean patchUltraExtreme144(String packageName) {
        return patchForTier(packageName, FpsUnlockTier.FPS_144);
    }

    // ─── UltraExtreme 165fps SuperSmooth Patch ───────────────────────────────

    public static boolean patchUltraExtreme165(String packageName) {
        return patchForTier(packageName, FpsUnlockTier.FPS_165);
    }

    // ─── UltraExtreme 185fps SuperSmooth Patch ───────────────────────────────

    public static boolean patchUltraExtreme185(String packageName) {
        return patchForTier(packageName, FpsUnlockTier.FPS_185);
    }

    // ─── Competitive Force-Write ─────────────────────────────────────────────

    public static boolean patchCompetitive(String packageName, int targetFps) {
        final int forcedFps = FpsUnlockTier.resolveTargetFps(targetFps);
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(forcedFps);
        boolean ok = patchForTier(packageName, tier);
        deployPakPatch(packageName);
        return ok;
    }

    private static boolean patchForTier(String packageName, FpsUnlockTier tier) {
        if (packageName == null || tier == null) return false;
        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (applyPubgFilePatch(path, tier)) {
                written++;
            }
        }
        patchActiveSavBinary(packageName, tier.fps);
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "PUBGM " + tier.label + " SuperSmooth & UltraExtreme patch: " + written + " paths for " + packageName);
        return written > 0;
    }

    /**
     * Optional deployment of a custom game_patch_*.pak file to PUBGM Saved/Paks directory.
     * Note: PAK patches are completely optional; 100% of FPS, Graphics, and Performance unlocks
     * operate natively through UserCustom.ini and Active.sav binary byte patching.
     */
    public static boolean deployPakPatch(String pkg) {
        if (pkg == null || pkg.trim().isEmpty()) return false;

        String[] candidateDirs = {
            "/storage/emulated/0/Download",
            "/sdcard/Download",
            "/storage/emulated/0/Documents/Game-Launcher",
            "/sdcard/Documents/Game-Launcher",
            "/storage/emulated/0/GameLauncher",
            "/sdcard/GameLauncher",
            "/data/local/tmp"
        };

        String foundSource = null;
        String pakFileName = null;

        for (String dir : candidateDirs) {
            if (!ShizukuFileManager.fileExists(dir)) continue;
            // Quick check for any game_patch*.pak
            String checkCmd = "ls -1 \"" + dir + "\"/game_patch*.pak 2>/dev/null | head -n 1";
            String res = ShizukuExecutor.hasShizukuPermission()
                    ? ShizukuExecutor.executeShizukuCommand(checkCmd)
                    : CommandExecutor.executeSystemCommand(checkCmd);
            if (res != null && !res.trim().isEmpty() && !res.startsWith("ERROR:") && !res.contains("No such")) {
                foundSource = res.trim();
                int slash = foundSource.lastIndexOf('/');
                pakFileName = (slash >= 0) ? foundSource.substring(slash + 1) : foundSource;
                break;
            }
        }

        if (foundSource == null || pakFileName == null || pakFileName.isEmpty()) {
            Log.d(TAG, "No optional PAK file found for " + pkg + "; relying purely on native INI & Active.sav binary patch.");
            return false;
        }

        List<String> basePaths = GameConfigPathResolver.generateBasePaths(pkg);
        boolean deployed = false;

        for (String base : basePaths) {
            String dir = base + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Paks";
            ShizukuFileManager.makeDirectory(dir);
            String dest = dir + "/" + pakFileName;
            String copyCmd = "cp -f \"" + foundSource + "\" \"" + dest + "\" && chmod 666 \"" + dest + "\"";
            if (ShizukuExecutor.hasShizukuPermission()) {
                ShizukuExecutor.executeShizukuCommand(copyCmd);
            } else {
                CommandExecutor.executeSystemCommand(copyCmd);
            }
            if (ShizukuFileManager.fileExists(dest)) {
                deployed = true;
                Log.i(TAG, "Successfully deployed optional " + pakFileName + " to " + dest);
            }
        }
        return deployed;
    }



    // ─── Delegated Common Tuning Injectors ───────────────────────────────────

    public static void applySuperFastTouch(String packageName) {
        CommonConfigTuningInjector.applySuperFastTouch(packageName);
    }

    public static void applyAimAssistConfig(String packageName) {
        CommonConfigTuningInjector.applyAimAssistConfig(packageName);
    }

    public static void applyRecoilControlConfig(String packageName) {
        CommonConfigTuningInjector.applyRecoilControlConfig(packageName);
    }

    public static void applyDamageScriptConfig(String packageName) {
        CommonConfigTuningInjector.applyDamageScriptConfig(packageName);
    }

    public static void applyFastCooldownConfig(String packageName) {
        CommonConfigTuningInjector.applyFastCooldownConfig(packageName);
    }

    public static void applyShield1500Config(String packageName) {
        CommonConfigTuningInjector.applyShield1500Config(packageName);
    }

    public static void applyDroneViewUltraConfig(String packageName) {
        CommonConfigTuningInjector.applyDroneViewUltraConfig(packageName);
    }

    public static void applyDroneViewConfig(String packageName) {
        CommonConfigTuningInjector.applyDroneViewConfig(packageName);
    }

    public static void applyArmorDefConfig(String packageName) {
        CommonConfigTuningInjector.applyArmorDefConfig(packageName);
    }

    public static void applySpeedBoostConfig(String packageName) {
        CommonConfigTuningInjector.applySpeedBoostConfig(packageName);
    }

    public static void applyTrackingBulletConfig(String packageName) {
        CommonConfigTuningInjector.applyTrackingBulletConfig(packageName);
    }

    public static void applyAimHeadLockConfig(String packageName) {
        CommonConfigTuningInjector.applyAimHeadLockConfig(packageName);
    }

    public static void applyUltraDamageOverdriveConfig(String packageName) {
        CommonConfigTuningInjector.applyUltraDamageOverdriveConfig(packageName);
    }

    public static void applyHeroAimLockConfig(String packageName) {
        CommonConfigTuningInjector.applyHeroAimLockConfig(packageName);
    }

    public static void applyAntiLog(String packageName) {
        CommonConfigTuningInjector.applyAntiLog(packageName);
    }

    /**
     * PUBGM — Magic bullet aimbot + zero recoil + no spread.
     * Iterates all PUBGM config paths and calls NativeConfigInjector.injectMagicBulletAimbot
     * (UE4 CVar format on UserCustom.ini, plain-key on others).
     */
    public static void applyMagicBulletAimbot(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectMagicBulletAimbot(path);
        }
    }

// ─── Internal ─────────────────────────────────────────────────────────────

    private static List<String> getConfigPaths(String pkg) {
        return GameConfigPathResolver.getPathsForGame(pkg);
    }

    /**
     * Format-aware per-file patcher for PUBGM:
     * - EnjoyCJZC.ini / GameUserSettings.ini: patches [/Script/ShadowTrackerExtra.UserSetting]
     * - UserCustom.ini / DeviceProfile.ini: patches [UserCustom DeviceProfile] with CVars & INI keys
     * - PlayerPrefs XML: patches XML nodes
     */
    public static boolean applyPubgFilePatch(String path, FpsUnlockTier tier) {
        return applyPubgFilePatch(path, tier, true);
    }

    /**
     * Format-aware per-file patcher for PUBGM supporting both SuperSmooth (1) and HDR (4) modes:
     * - EnjoyCJZC.ini / GameUserSettings.ini: patches [/Script/ShadowTrackerExtra.UserSetting]
     * - UserCustom.ini: patches [UserCustom DeviceProfile] with CVars & INI keys
     * - EnjoyCJZC.ini: patches [EnjoyCJZC DeviceProfile]
     * - DeviceProfile.ini: patches [DeviceProfile]
     * - PlayerPrefs XML: patches XML nodes
     */
    public static boolean applyPubgFilePatch(String path, FpsUnlockTier tier, boolean enableHdr) {
        if (path == null || path.trim().isEmpty() || tier == null) return false;

        final int qualityLevel = enableHdr ? 4 : 1; // 4 = HDR, 1 = Smooth
        if (NativeConfigInjector.injectPubgm165FpsGraphics(path, tier.fps, qualityLevel)) {
            return true;
        }

        // In PUBGM UE4 engine: Level 7 is 120 FPS (Ultra Extreme).
        // Any level > 7 fails the engine enum boundary check and causes PUBGM to fallback to 90 FPS (Level 6).
        final int effectiveLevel = (tier.fps >= 120) ? 7 : tier.level;

        if (path.endsWith("EnjoyCJZC.ini") || path.endsWith("EnjoyCJ.ini")
                || path.endsWith("BGMIEnjoyCJZC.ini") || path.endsWith("KREnjoyCJZC.ini")
                || path.endsWith("VNGEnjoyCJZC.ini") || path.endsWith("GameUserSettings.ini")) {
            String[] userSettingKeys = {
                "FrameRateLevel=" + effectiveLevel,
                "BattleFPS=" + effectiveLevel,
                "LobbyFPS=" + effectiveLevel,
                "FPS=" + tier.fps,
                "MaxFPS=" + tier.fps,
                "TargetFPS=" + tier.fps,
                "FrameRateLimit=" + tier.fps,
                "MobileFPSLimit=" + tier.fps,
                "GraphicQuality=" + qualityLevel,
                "ArtQuality=" + qualityLevel,
                "ShadowQuality=" + (enableHdr ? 3 : 1),
                "MobileHDRMode=" + (enableHdr ? 1 : 0),
                "HighFPSMode=3",
                "bUseHDRMode=" + (enableHdr ? "True" : "False"),
                "bUseUltraExtreme=True",
                "bFramePacingEnabled=True",
                "ResolutionScale=100",
                "ResolutionQuality=100",
                "UnlockFPS=1",
                "Unlock120Hz=1",
                "Unlock144Hz=1",
                "Unlock165Hz=1",
                "Unlock185Hz=1",
                "Unlock240Hz=1"
            };
            return ConfigFileHelper.patchKeys(path, userSettingKeys, "[/Script/ShadowTrackerExtra.UserSetting]");
        }

        if (path.endsWith(".playerprefs.xml") || path.endsWith("_preferences.xml")) {
            String[] xmlKeys = {
                "FPS=" + tier.fps,
                "FrameRateLevel=" + effectiveLevel,
                "GraphicQuality=" + qualityLevel,
                "MobileHDRMode=" + (enableHdr ? 1 : 0),
                "HighFPSMode=3"
            };
            return ConfigFileHelper.patchKeys(path, xmlKeys, "<map>");
        }

        // Default: UE4 UserCustom / DeviceProfile INI
        String sectionHeader = "[UserCustom DeviceProfile]";
        if (path.endsWith("DeviceProfile.ini")) {
            sectionHeader = "[DeviceProfile]";
        } else if (path.endsWith("EnjoyCJZC.ini") || path.endsWith("EnjoyCJ.ini")) {
            sectionHeader = "[EnjoyCJZC DeviceProfile]";
        }

        String[] cVarsAndIniKeys = {
            // ── Frame Rate & Device Profile CVars ──
            "+CVars=r.PUBGDeviceFPS=" + effectiveLevel,
            "+CVars=r.PUBGDeviceFPSPolicy=1",
            "+CVars=r.DefaultDeviceFPS=" + effectiveLevel,
            "+CVars=r.UserFPSSetting=" + effectiveLevel,
            "+CVars=r.PUBGTargetFPS=" + tier.fps,
            "+CVars=r.PUBGMaxFPS=" + tier.fps,
            "+CVars=r.PUBGFrameRateLimit=" + tier.fps,
            "+CVars=r.FrameRateLimit=" + tier.fps,
            "+CVars=r.MobileFPSLimit=" + tier.fps,
            "+CVars=r.Vsync=0",
            "+CVars=r.Unlock120Hz=1",
            "+CVars=r.Unlock144Hz=1",
            "+CVars=r.Unlock165Hz=1",
            "+CVars=r.Unlock185Hz=1",
            "+CVars=r.Unlock240Hz=1",
            "+CVars=r.TouchBoostHz=" + tier.fps,
            "+CVars=r.MobileTouchBoostRate=" + tier.fps,
            "+CVars=r.FramePacing=1",
            // ── HDR & High-Fidelity Rendering CVars ──
            "+CVars=r.MobileHDR=" + (enableHdr ? 1 : 0),
            "+CVars=r.PUBGHDRMode=" + (enableHdr ? 1 : 0),
            "+CVars=r.PUBGQualityLevel=" + qualityLevel,
            "+CVars=r.PUBGSDKQualityLevel=" + qualityLevel,
            "+CVars=r.UserQualitySetting=" + qualityLevel,
            "+CVars=r.ShadowQuality=" + (enableHdr ? 3 : 1),
            "+CVars=r.PostProcessAAQuality=3",
            "+CVars=r.Tonemapper.Quality=" + (enableHdr ? 4 : 3),
            "+CVars=r.MobileContentScaleFactor=1.0",
            "+CVars=r.MaxAnisotropy=16",
            "+CVars=r.TemporalAA.Upscale=1",
            "+CVars=r.AllowOcclusionQueries=1",
            "+CVars=r.Vulkan.Enable=1",
            "+CVars=r.PUBGTPPViewRange=100.00",
            "+CVars=r.PUBGFPPViewRange=150.00",
            "+CVars=r.SuppressLogs=1",
            "+CVars=r.DisableDebugLog=1",
            "+CVars=r.AsyncCompute=1",
            "+CVars=r.VRS.Enable=1",
            "+CVars=r.EnableAsyncPipelineCompilation=1",
            // ── INI Graphics & FPS Keys ──
            "FPS=" + tier.fps,
            "MaxFPS=" + tier.fps,
            "TargetFPS=" + tier.fps,
            "FrameRateLimit=" + tier.fps,
            "MobileFPSLimit=" + tier.fps,
            "FrameRateLevel=" + effectiveLevel,
            "GraphicQuality=" + qualityLevel,
            "ArtQuality=" + qualityLevel,
            "UnlockFPS=1",
            "Unlock120FPS=1",
            "Unlock144FPS=1",
            "Unlock165FPS=1",
            "Unlock185FPS=1",
            "Ultra144FPS=1",
            "Ultra165FPS=1",
            "Ultra185FPS=1",
            "HighFPSMode=3",
            "SuperHighFPS=1",
            "Unlock90Hz=1",
            "Unlock120Hz=1",
            "Unlock144Hz=1",
            "Unlock165Hz=1",
            "Unlock185Hz=1",
            "Unlock240Hz=1",
            "UltraExtreme=1",
            "bUseUltraExtreme=True",
            "ResolutionQuality=100",
            "ResolutionScale=100",
            "ScreenScale=100",
            "HDRMode=" + (enableHdr ? 1 : 0),
            "AntiAliasingQuality=2",
            "bUseAntiAliasing=True",
            "VulkanPipelineCache=1",
            "AsyncCompute=1",
            "VRS=1",
            "bReduceLoadedMips=False",
            "bFramePacingEnabled=True",
            "Vsync=0",
            "TPPFieldOfView=100",
            "FPPFieldOfView=150",
            "bDisableAnalytics=True",
            "bDisableBugReporting=True",
            "TouchBoostHz=" + tier.fps,
            "TouchPollingRate=1000"
        };
        return ConfigFileHelper.patchKeys(path, cVarsAndIniKeys, sectionHeader);
    }

    public static void patchActiveSavBinary(String pkg, int targetFps) {
        patchActiveSavBinary(pkg, targetFps, true);
    }

    /**
     * Patches Active.sav binary savegame file directly using byte manipulation.
     * Enforces FPSLevel, BattleFPS, and LobbyFPS to Level 7 (120 FPS / Ultra Extreme),
     * and sets BattleRenderQuality / LobbyRenderQuality to Level 4 (HDR) or Level 1 (Smooth).
     */
    public static void patchActiveSavBinary(String pkg, int targetFps, boolean enableHdr) {
        if (pkg == null) return;
        final int rawLevel = FpsUnlockTier.fromFps(targetFps).level;
        // In PUBGM UE4 engine: Level 7 is 120 FPS. Any level > 7 fails enum validation and clamps to 90 FPS (Level 6).
        final int effectiveFpsLevel = (targetFps >= 120) ? 7 : rawLevel;
        final int effectiveQuality = enableHdr ? 4 : 1; // 4 = HDR, 1 = Smooth

        String[] savPaths = {
            "/storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/ActiveShadow.sav",
            "/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/sdcard/Android/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/ActiveShadow.sav",
            "/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/data/data/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/ActiveShadow.sav",
            "/data/user/0/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav",
            "/data/user/0/" + pkg + "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/ActiveShadow.sav",
            // New State save paths
            "/storage/emulated/0/Android/data/" + pkg + "/files/UE4Game/PUBGNewState/PUBGNewState/Saved/SaveGames/Active.sav",
            "/sdcard/Android/data/" + pkg + "/files/UE4Game/PUBGNewState/PUBGNewState/Saved/SaveGames/Active.sav"
        };

        for (String sav : savPaths) {
            try {
                byte[] data = null;
                if (ShizukuFileManager.fileExists(sav)) {
                    data = ShizukuFileManager.readFileBytes(sav);
                } else {
                    java.io.File f = new java.io.File(sav);
                    if (f.exists() && f.canRead()) {
                        data = java.nio.file.Files.readAllBytes(f.toPath());
                    }
                }

                if (data != null && data.length > 0) {
                    boolean modified = false;
                    // FPS unlocks
                    modified |= patchGvasIntProperty(data, "FPSLevel", effectiveFpsLevel);
                    modified |= patchGvasIntProperty(data, "BattleFPS", effectiveFpsLevel);
                    modified |= patchGvasIntProperty(data, "LobbyFPS", effectiveFpsLevel);
                    modified |= patchGvasIntProperty(data, "MainCityFPS", effectiveFpsLevel);
                    modified |= patchGvasIntProperty(data, "HighFPSMode", 3);

                    // Graphics unlocks (HDR vs Smooth)
                    modified |= patchGvasIntProperty(data, "BattleRenderQuality", effectiveQuality);
                    modified |= patchGvasIntProperty(data, "LobbyRenderQuality", effectiveQuality);
                    modified |= patchGvasIntProperty(data, "MainCityRenderQuality", effectiveQuality);
                    modified |= patchGvasIntProperty(data, "GraphicQuality", effectiveQuality);
                    modified |= patchGvasIntProperty(data, "ArtQuality", effectiveQuality);
                    modified |= patchGvasIntProperty(data, "MobileHDRMode", enableHdr ? 1 : 0);
                    modified |= patchGvasIntProperty(data, "ShadowQuality", enableHdr ? 3 : 1);
                    modified |= patchGvasIntProperty(data, "ShadowSwitch", enableHdr ? 1 : 0);

                    if (modified) {
                        if (ShizukuFileManager.fileExists(sav)) {
                            ShizukuFileManager.uploadBytes(sav, data, "666");
                        } else {
                            java.io.File f = new java.io.File(sav);
                            java.nio.file.Files.write(f.toPath(), data);
                        }
                    }
                }
            } catch (Throwable t) {
                Log.w(TAG, "patchActiveSavBinary error for " + sav + ": " + t.getMessage());
            }
        }
        Log.i(TAG, "PUBGM Active.sav binary enforced level " + effectiveFpsLevel + " (" + targetFps + " FPS, HDR=" + enableHdr + ") for " + pkg);
    }

    /**
     * Precisely modifies a 4-byte little-endian IntProperty in a GVAS binary savegame.
     * Matches the property name, verifies the IntProperty tag, and overwrites the exact 4-byte payload.
     */
    public static boolean patchGvasIntProperty(byte[] data, String fieldName, int value) {
        if (data == null || fieldName == null || fieldName.isEmpty()) return false;
        byte[] fieldBytes = fieldName.getBytes(StandardCharsets.UTF_8);
        byte[] propTypeBytes = "IntProperty\0".getBytes(StandardCharsets.UTF_8);

        int idx = 0;
        boolean found = false;
        while ((idx = indexOfBytesFrom(data, fieldBytes, idx)) != -1) {
            int propTypeIdx = indexOfBytesFrom(data, propTypeBytes, idx + fieldBytes.length);
            if (propTypeIdx != -1 && propTypeIdx - idx <= 48) {
                int valOffset = propTypeIdx + propTypeBytes.length + 9;
                if (valOffset + 3 < data.length) {
                    data[valOffset] = (byte) (value & 0xFF);
                    data[valOffset + 1] = (byte) ((value >> 8) & 0xFF);
                    data[valOffset + 2] = (byte) ((value >> 16) & 0xFF);
                    data[valOffset + 3] = (byte) ((value >> 24) & 0xFF);
                    found = true;
                }
            }
            idx += fieldBytes.length;
        }
        return found;
    }

    /**
     * Dedicated unlocker for 165 FPS and HDR Graphics for PUBG Mobile.
     */
    public static boolean apply165FpsHdrUnlock(String packageName) {
        if (packageName == null) return false;
        final FpsUnlockTier tier = FpsUnlockTier.FPS_165;
        patchActiveSavBinary(packageName, 165, true);
        List<String> paths = getConfigPaths(packageName);
        int written = 0;
        for (String path : paths) {
            if (applyPubgFilePatch(path, tier, true)) {
                written++;
            }
        }
        AntiLogPatcher.applyAntiLog(packageName);
        Log.i(TAG, "PUBGM 165 FPS & HDR Graphics unlocked across " + written + " paths for " + packageName);
        return written > 0;
    }

    private static int indexOfBytesFrom(byte[] source, byte[] target, int fromIndex) {
        if (source == null || target == null || source.length < target.length || fromIndex < 0) return -1;
        for (int i = fromIndex; i <= source.length - target.length; i++) {
            boolean match = true;
            for (int j = 0; j < target.length; j++) {
                if (source[i + j] != target[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private static boolean applyPatch(String path, int targetFps) {
        final FpsUnlockTier tier = FpsUnlockTier.fromFps(targetFps);
        return applyPubgFilePatch(path, tier, true);
    }

    // ─── 2026 Skill Economy Overdrive ─────────────────────────────────────────

    /**
     * PUBGM/BGMI — Fast Stamina, Adrenaline Energy, HP Regen, Zero Parachute/Vehicle Cooldown.
     */
    public static void applyFastStaminaEnergyBoost(String packageName) {
        List<String> paths = getConfigPaths(packageName);
        for (String path : paths) {
            NativeConfigInjector.injectFastCooldown(path);
            NativeConfigInjector.injectFastFullEnergy(path);
            NativeConfigInjector.injectFastHpRegen(path);
            NativeConfigInjector.injectFastStaminaFuryRegen(path);
            NativeConfigInjector.injectZeroSkillCost(path);
            NativeConfigInjector.injectMaxUltCharge(path);
        }
    }

    /** Convenience alias. */
    public static void applySkillEconomy(String packageName) {
        applyFastStaminaEnergyBoost(packageName);
    }
}
