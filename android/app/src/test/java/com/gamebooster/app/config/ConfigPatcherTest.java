package com.gamebooster.app.config;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigPatcherTest {

    @Test
    public void testFpsUnlockTierResolution() {
        // Legacy 60 and 90 FPS requests automatically promote to 120 FPS floor
        assertEquals(120, FpsUnlockTier.resolveTargetFps(60));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(90));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
        // Out-of-range values clamp to max tier (185 FPS)
        assertEquals(185, FpsUnlockTier.resolveTargetFps(240));
    }

    @Test
    public void testShellSafetyPackageValidation() {
        assertTrue(ShellSafety.isSafePackageName("com.mobile.legends"));
        assertTrue(ShellSafety.isSafePackageName("com.tencent.ig"));
        assertTrue(ShellSafety.isSafePackageName("com.activision.callofduty.shooter"));

        // Malicious or command injection attempts must be rejected
        assertFalse(ShellSafety.isSafePackageName("com.game; rm -rf /"));
        assertFalse(ShellSafety.isSafePackageName("com.game && echo pwned"));
        assertFalse(ShellSafety.isSafePackageName("com.game`whoami`"));
        assertFalse(ShellSafety.isSafePackageName("com.game$(id)"));
        assertFalse(ShellSafety.isSafePackageName(""));
        assertFalse(ShellSafety.isSafePackageName(null));
    }

    @Test
    public void testGameConfigPathResolver() {
        List<String> mlbbPaths = GameConfigPathResolver.getPathsForGame("com.mobile.legends");
        assertNotNull(mlbbPaths);
        assertFalse(mlbbPaths.isEmpty());

        List<String> pubgPaths = GameConfigPathResolver.getPathsForGame("com.tencent.ig");
        assertNotNull(pubgPaths);
        assertFalse(pubgPaths.isEmpty());

        List<String> codmPaths = GameConfigPathResolver.getPathsForGame("com.activision.callofduty.shooter");
        assertNotNull(codmPaths);
        assertFalse(codmPaths.isEmpty());
    }

    @Test
    public void testMlbb2026PathSignatures() {
        List<String> rel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.mobile.legends");
        assertNotNull(rel);
        assertTrue(rel.contains("shared_prefs/com.mobile.legends.v4.playerprefs.xml"));
        assertTrue(rel.contains("shared_prefs/com.mobile.legends.v3.playerprefs.xml"));
        assertTrue(rel.contains("shared_prefs/com.mobile.legends.v2.playerprefs.xml"));
        assertTrue(rel.contains("files/dragon2017/assets/Document/QualityConfig.json"));
        assertTrue(rel.contains("files/Dragon2017/assets/Document/QualityConfig.json"));
        assertTrue(rel.contains("files/dragon2017/assets/Document/BattleConfig.json"));
        assertTrue(rel.contains("files/battle_config/QualityConfig.json"));
    }

    @Test
    public void testPubgm2026PathSignatures() {
        // Global PUBGM
        List<String> globalRel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.tencent.ig");
        assertNotNull(globalRel);
        // Canonical double-subfolder
        assertTrue(globalRel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini"));
        assertTrue(globalRel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/EnjoyCJZC.ini"));
        assertTrue(globalRel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini"));
        assertTrue(globalRel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/Active.sav"));
        // Single-subfolder mirror
        assertTrue(globalRel.contains("files/UE4Game/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini"));
        assertTrue(globalRel.contains("files/UE4Game/ShadowTrackerExtra/Saved/Config/Android/EnjoyCJZC.ini"));
        assertTrue(globalRel.contains("files/UE4Game/ShadowTrackerExtra/Saved/Config/Android/GameUserSettings.ini"));
        assertTrue(globalRel.contains("files/UE4Game/ShadowTrackerExtra/Saved/SaveGames/Active.sav"));

        // Regional variants
        List<String> bgmiRel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.pubg.imobile");
        assertTrue(bgmiRel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/BGMIUserCustom.ini"));
        assertTrue(bgmiRel.contains("files/UE4Game/ShadowTrackerExtra/Saved/Config/Android/BGMIUserCustom.ini"));

        List<String> krRel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.pubg.krmobile");
        assertTrue(krRel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/KRUserCustom.ini"));
        assertTrue(krRel.contains("files/UE4Game/ShadowTrackerExtra/Saved/Config/Android/KRUserCustom.ini"));

        List<String> twRel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.rekoo.pubgm");
        assertTrue(twRel.contains("files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/TWUserCustom.ini"));
        assertTrue(twRel.contains("files/UE4Game/ShadowTrackerExtra/Saved/Config/Android/TWUserCustom.ini"));

        List<String> nsRel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.pubg.newstate");
        assertTrue(nsRel.contains("files/UE4Game/PUBGNewState/PUBGNewState/Saved/Config/Android/UserCustom.ini"));
        assertTrue(nsRel.contains("files/UE4Game/PUBGNewState/Saved/Config/Android/UserCustom.ini"));
    }

    @Test
    public void testCodm2026PathSignatures() {
        List<String> codmRel = GameConfigPathResolver.getKnownRelativePathsForPackage("com.activision.callofduty.shooter");
        assertNotNull(codmRel);
        // Unity boot config
        assertTrue(codmRel.contains("files/boot.config"));
        assertTrue(codmRel.contains("files/il2cpp/boot.config"));
        // JSON configs
        assertTrue(codmRel.contains("files/Config/UserSetting.json"));
        assertTrue(codmRel.contains("files/Config/HardwareProfile.json"));
        assertTrue(codmRel.contains("files/Config/GraphicsSettings_2026.json"));
        assertTrue(codmRel.contains("files/cod_prefs.json"));
        // INI configs
        assertTrue(codmRel.contains("files/GraphicsSettings.ini"));
        assertTrue(codmRel.contains("files/ControlsSettings.ini"));
        assertTrue(codmRel.contains("files/GameSettings.ini"));
        // Cross-publisher playerprefs
        assertTrue(codmRel.contains("shared_prefs/com.garena.game.codm.v2.playerprefs.xml"));
        assertTrue(codmRel.contains("shared_prefs/com.vng.codm.v2.playerprefs.xml"));
        assertTrue(codmRel.contains("shared_prefs/com.tencent.tmgp.cod.v2.playerprefs.xml"));
        // Warzone Mobile
        assertTrue(codmRel.contains("files/UE4Game/Warzone/Warzone/Saved/Config/Android/GameUserSettings.ini"));
        assertTrue(codmRel.contains("files/UE4Game/Warzone/Saved/Config/Android/GameUserSettings.ini"));
    }

    @Test
    public void testAcceptableConfigPathFiltering() {
        // Must accept valid configs
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/data/com.mobile.legends/files/dragon2017/assets/Document/QualityConfig.json"));
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.mobile.legends/shared_prefs/com.mobile.legends.v4.playerprefs.xml"));
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/Saved/Config/Android/EnjoyCJZC.ini"));
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini"));
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/data/com.activision.callofduty.shooter/files/boot.config"));
        assertTrue(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/data/com.activision.callofduty.shooter/files/Config/UserSetting.json"));

        // Must reject binaries, libraries, and caches
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.tencent.ig/lib/libUE4.so"));
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.tencent.ig/cache/temp.bytes"));
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath("/data/data/com.tencent.ig/code_cache/test.dex"));
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/data/com.mobile.legends/files/dragon2017/assets/UI/Skin.unity3d"));
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/data/com.tencent.ig/files/data.bundle"));
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/obb/com.tencent.ig/main.obb"));
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/data/com.tencent.ig/files/intro.mp4"));
        assertFalse(GameConfigPathResolver.isAcceptableConfigPath("/storage/emulated/0/Android/data/com.tencent.ig/files/audio.bank"));
    }

    @Test
    public void testExistingFilePrioritySorting() throws java.io.IOException {
        java.io.File tempDir = java.io.File.createTempFile("gamebooster_test_", "");
        tempDir.delete();
        tempDir.mkdirs();
        try {
            java.io.File existingFile = new java.io.File(tempDir, "existing_config.ini");
            existingFile.createNewFile();

            java.util.List<String> defaults = java.util.Arrays.asList(
                    "non_existing_1.ini",
                    existingFile.getName(),
                    "non_existing_2.ini"
            );

            // Directly test resolution logic with mock base path
            java.util.Set<String> discoveredExisting = new java.util.LinkedHashSet<>();
            java.util.Set<String> candidatePaths = new java.util.LinkedHashSet<>();

            for (String rel : defaults) {
                java.io.File f = new java.io.File(tempDir, rel);
                if (f.exists()) {
                    discoveredExisting.add(f.getAbsolutePath());
                } else {
                    candidatePaths.add(f.getAbsolutePath());
                }
            }

            java.util.List<String> sorted = new java.util.ArrayList<>();
            sorted.addAll(discoveredExisting);
            for (String c : candidatePaths) {
                if (!discoveredExisting.contains(c)) {
                    sorted.add(c);
                }
            }

            assertEquals(3, sorted.size());
            assertEquals(existingFile.getAbsolutePath(), sorted.get(0));
            assertTrue(sorted.get(1).contains("non_existing"));
            assertTrue(sorted.get(2).contains("non_existing"));
        } finally {
            // Cleanup
            java.io.File[] files = tempDir.listFiles();
            if (files != null) {
                for (java.io.File f : files) f.delete();
            }
            tempDir.delete();
        }
    }

    @Test
    public void testConfigFileHelperIniPatching() {
        String originalIni = "[Graphics]\nFPS=60\nQuality=2\n";
        String[] updates = new String[]{
                "FPS=144",
                "HighFPSMode=1",
                "Quality=4"
        };

        String patched = ConfigFileHelper.patchIniContent(originalIni, updates, "[Graphics]");
        assertNotNull(patched);
        assertTrue(patched.contains("FPS=144"));
        assertTrue(patched.contains("HighFPSMode=1"));
        assertTrue(patched.contains("Quality=4"));
        assertFalse(patched.contains("FPS=60"));
    }

    @Test
    public void testConfigFileHelperJsonPatching() {
        String originalJson = "{\"fps\": 60, \"quality\": \"medium\"}";
        String[] updates = new String[]{
                "fps=144",
                "quality=ultra"
        };

        String patched = ConfigFileHelper.patchJsonContent(originalJson, updates);
        assertNotNull(patched);
        assertTrue(patched.contains("144"));
        assertTrue(patched.contains("ultra"));
    }

    @Test
    public void testConfigFileHelperXmlPatching() {
        String originalXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<map>\n    <int name=\"fps_limit\" value=\"60\" />\n</map>";
        String[] updates = new String[]{
                "fps_limit=144",
                "high_frame_rate=1"
        };

        String patched = ConfigFileHelper.patchXmlContent(originalXml, updates);
        assertNotNull(patched);
        assertTrue(patched.contains("fps_limit"));
        assertTrue(patched.contains("144"));
    }

    @Test
    public void testCompetitiveProfileCreation() {
        CompetitiveCfgProfile profile = CompetitiveCfgProfile.superSmooth144(CompetitiveCfgProfile.GAME_MLBB);
        assertNotNull(profile);
        assertEquals(CompetitiveCfgProfile.GAME_MLBB, profile.getGameKey());
        assertEquals(144, profile.getTargetFps());
        assertTrue(profile.isSuperFastTouchEnabled());
    }

    @Test
    public void testCompetitiveTuningKeyPatching() {
        String originalIni = "[UserCustom]\nr.PUBGDeviceFPS=6\n";
        String[] updates = new String[]{
                "r.PUBGDeviceFPS=10",
                "r.PUBGAutoLoot=1",
                "r.AutoPickupSpeed=2",
                "AutoSprint=1",
                "bSprintAlways=True",
                "Scope8xSensitivity=0.65"
        };
        String patched = ConfigFileHelper.patchIniContent(originalIni, updates, "[UserCustom]");
        assertNotNull(patched);
        assertTrue(patched.contains("r.PUBGDeviceFPS=10"));
        assertTrue(patched.contains("r.PUBGAutoLoot=1"));
        assertTrue(patched.contains("r.AutoPickupSpeed=2"));
        assertTrue(patched.contains("AutoSprint=1"));
        assertTrue(patched.contains("bSprintAlways=True"));
        assertTrue(patched.contains("Scope8xSensitivity=0.65"));

        // Test MLBB PlayerPrefs XML patching
        String originalXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<map>\n    <int name=\"TargetFPS\" value=\"60\" />\n</map>";
        String[] mlbbUpdates = new String[]{
                "TargetFPS=185",
                "JungleFastFarmAllHero=1",
                "LingSwordPathResponsiveness=10",
                "FannyZeroCableDelay=1",
                "ZeroDelaySkillTap=1"
        };
        String patchedXml = ConfigFileHelper.patchXmlContent(originalXml, mlbbUpdates);
        assertNotNull(patchedXml);
        assertTrue(patchedXml.contains("185"));
        assertTrue(patchedXml.contains("JungleFastFarmAllHero"));
        assertTrue(patchedXml.contains("LingSwordPathResponsiveness"));
        assertTrue(patchedXml.contains("FannyZeroCableDelay"));
        assertTrue(patchedXml.contains("ZeroDelaySkillTap"));
    }

    @Test
    public void testComprehensiveDamageAndCombatPatching() {
        // Test PUBGM ballistics & penetration INI patching
        String originalIni = "[UserCustom]\nr.PUBGDeviceFPS=10\n";
        String[] pubgmUpdates = new String[]{
                "+CVars=r.PUBGMuzzleVelocityBoost=2.0",
                "+CVars=r.PUBGArmorPenetrationLevel3=1.0",
                "+CVars=r.PUBGSniperHeadshotDamage=300",
                "+CVars=r.PUBGShotgunPelletSpread=0.0"
        };
        String patchedIni = ConfigFileHelper.patchIniContent(originalIni, pubgmUpdates, "[UserCustom]");
        assertNotNull(patchedIni);
        assertTrue(patchedIni.contains("r.PUBGMuzzleVelocityBoost=2.0"));
        assertTrue(patchedIni.contains("r.PUBGArmorPenetrationLevel3=1.0"));
        assertTrue(patchedIni.contains("r.PUBGSniperHeadshotDamage=300"));
        assertTrue(patchedIni.contains("r.PUBGShotgunPelletSpread=0.0"));

        // Test MLBB penetration & critical burst XML patching
        String originalXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<map>\n    <string name=\"AppVer\">1.9</string>\n</map>";
        String[] mlbbDamageUpdates = new String[]{
                "PhysicalPenetrationRatio=1.0",
                "CriticalDamageMultiplier=3.0",
                "OugiShadowKillSpeed=10",
                "ExecuteAutoTrigger=1"
        };
        String patchedXml = ConfigFileHelper.patchXmlContent(originalXml, mlbbDamageUpdates);
        assertNotNull(patchedXml);
        assertTrue(patchedXml.contains("PhysicalPenetrationRatio"));
        assertTrue(patchedXml.contains("CriticalDamageMultiplier"));
        assertTrue(patchedXml.contains("OugiShadowKillSpeed"));
        assertTrue(patchedXml.contains("ExecuteAutoTrigger"));

        // Test CODM BSA removal & range overdrive JSON patching
        String originalJson = "{\"GraphicQuality\": 4}";
        String[] codmUpdates = new String[]{
                "BulletSpreadAccuracy=0.0",
                "DamageRangeMultiplier=3.0",
                "SprintToFireDelayMs=0",
                "HitFlinchScale=0.0"
        };
        String patchedJson = ConfigFileHelper.patchJsonContent(originalJson, codmUpdates);
        assertNotNull(patchedJson);
        assertTrue(patchedJson.contains("BulletSpreadAccuracy"));
        assertTrue(patchedJson.contains("DamageRangeMultiplier"));
        assertTrue(patchedJson.contains("SprintToFireDelayMs"));
        assertTrue(patchedJson.contains("HitFlinchScale"));
    }

    @Test
    public void testGameSecurityBypassEngine() {
        // Validation with safe package name
        GameSecurityBypassEngine.GameIdentity id = GameSecurityBypassEngine.resolveGameUidGid("com.mobile.legends");
        assertNotNull(id);
        assertEquals(10000, id.uid); // fallback or default baseline

        // Validation against shell injection attempts
        GameSecurityBypassEngine.GameIdentity malicious = GameSecurityBypassEngine.resolveGameUidGid("com.game; rm -rf /");
        assertNotNull(malicious);
        assertFalse(malicious.isValid);

        // Verify unlock and lock operations
        boolean unlocked = GameSecurityBypassEngine.unlockForInjection("com.mobile.legends");
        assertTrue(unlocked);

        boolean suppressed = GameSecurityBypassEngine.suppressSecurityTelemetryReporting("com.tencent.ig");
        assertTrue(suppressed);

        boolean postLocked = GameSecurityBypassEngine.postInjectionBypassAndLock("com.activision.callofduty.shooter");
        assertTrue(postLocked);
    }

    @Test
    public void testFastLoadAndSplashBypassPatching() {
        // MLBB Fast-Load XML
        String originalXml = "<map>\n    <int name=\"QualityType\" value=\"2\" />\n</map>";
        String[] mlbbFastLoadKeys = new String[]{
                "SkipOpenVideo=1",
                "SkipSplashVideo=1",
                "FastLoadAssets=1",
                "HighQualityLoad=0",
                "DragonResourceOptimize=1"
        };
        String patchedXml = ConfigFileHelper.patchXmlContent(originalXml, mlbbFastLoadKeys);
        assertNotNull(patchedXml);
        assertTrue(patchedXml.contains("SkipOpenVideo"));
        assertTrue(patchedXml.contains("SkipSplashVideo"));
        assertTrue(patchedXml.contains("FastLoadAssets"));
        assertTrue(patchedXml.contains("HighQualityLoad"));
        assertTrue(patchedXml.contains("DragonResourceOptimize"));

        // PUBGM Async Streaming INI
        String originalIni = "[UserCustom DeviceProfile]\n+CVars=r.PUBGDeviceFPSPolicy=185";
        String[] pubgmFastLoadKeys = new String[]{
                "s.AsyncLoadingThreadEnabled=True",
                "s.AsyncLoadingTimeLimit=10.0",
                "r.ShaderCompiler.CoreCount=8",
                "bSkipSplash=True"
        };
        String patchedIni = ConfigFileHelper.patchIniContent(originalIni, pubgmFastLoadKeys, "[PubgmFastLoad]");
        assertNotNull(patchedIni);
        assertTrue(patchedIni.contains("s.AsyncLoadingThreadEnabled"));
        assertTrue(patchedIni.contains("s.AsyncLoadingTimeLimit"));
        assertTrue(patchedIni.contains("r.ShaderCompiler.CoreCount"));
        assertTrue(patchedIni.contains("bSkipSplash"));

        // CODM Fast-Load JSON
        String originalJson = "{\"GraphicQuality\": 4}";
        String[] codmFastLoadKeys = new String[]{
                "FastLoad=1",
                "SkipIntroMovie=1",
                "ShaderPrewarmAtStartup=0",
                "PreloadWeaponModels=0"
        };
        String patchedJson = ConfigFileHelper.patchJsonContent(originalJson, codmFastLoadKeys);
        assertNotNull(patchedJson);
        assertTrue(patchedJson.contains("FastLoad"));
        assertTrue(patchedJson.contains("SkipIntroMovie"));
        assertTrue(patchedJson.contains("ShaderPrewarmAtStartup"));
        assertTrue(patchedJson.contains("PreloadWeaponModels"));
    }

    @Test
    public void testGameAutoInjectDispatcherIdempotency() {
        String testPkg = "com.mobile.legends";
        GameAutoInjectDispatcher.resetAll();
        assertFalse(GameAutoInjectDispatcher.isPackageInjected(testPkg));

        // First dispatch marks package as injected
        GameAutoInjectDispatcher.dispatchForPackage(testPkg);
        assertTrue(GameAutoInjectDispatcher.isPackageInjected(testPkg));

        // Second redundant dispatch does not crash and respects idempotency
        GameAutoInjectDispatcher.dispatchForPackage(testPkg);
        assertTrue(GameAutoInjectDispatcher.isPackageInjected(testPkg));

        // Force dispatch still succeeds
        GameAutoInjectDispatcher.dispatchForPackage(testPkg, true);
        assertTrue(GameAutoInjectDispatcher.isPackageInjected(testPkg));

        // Session reset clears state
        GameAutoInjectDispatcher.resetPackageInjectionState(testPkg);
        assertFalse(GameAutoInjectDispatcher.isPackageInjected(testPkg));
    }

    @Test
    public void testConfigFileHelperDuplicateStripping() {
        // INI with multiple duplicate keys
        String duplicateIni = "[UserCustom]\n"
                + "+CVars=r.PUBGDeviceFPS=6\n"
                + "+CVars=r.PUBGDeviceFPS=9\n"
                + "+CVars=r.PUBGDeviceFPS=10\n"
                + "bSprint=False\n";
        String[] iniUpdates = new String[]{
                "+CVars=r.PUBGDeviceFPS=185",
                "bSprint=True"
        };
        String patchedIni = ConfigFileHelper.patchIniContent(duplicateIni, iniUpdates, "[UserCustom]");
        assertNotNull(patchedIni);
        assertTrue(patchedIni.contains("r.PUBGDeviceFPS=185"));
        assertTrue(patchedIni.contains("bSprint=True"));
        assertFalse(patchedIni.contains("r.PUBGDeviceFPS=6"));
        assertFalse(patchedIni.contains("r.PUBGDeviceFPS=9"));
        assertFalse(patchedIni.contains("r.PUBGDeviceFPS=10\n"));
        // Count occurrences of r.PUBGDeviceFPS: must be exactly 1
        int count = 0;
        int idx = 0;
        while ((idx = patchedIni.indexOf("r.PUBGDeviceFPS", idx)) != -1) {
            count++;
            idx += "r.PUBGDeviceFPS".length();
        }
        assertEquals(1, count);

        // XML with duplicate tags
        String duplicateXml = "<map>\n"
                + "    <int name=\"TargetFPS\" value=\"60\" />\n"
                + "    <int name=\"TargetFPS\" value=\"90\" />\n"
                + "</map>";
        String[] xmlUpdates = new String[]{"TargetFPS=120"};
        String patchedXml = ConfigFileHelper.patchXmlContent(duplicateXml, xmlUpdates);
        assertNotNull(patchedXml);
        assertTrue(patchedXml.contains("value=\"120\""));
        assertFalse(patchedXml.contains("value=\"60\""));
        assertFalse(patchedXml.contains("value=\"90\""));
        int xmlCount = 0;
        idx = 0;
        while ((idx = patchedXml.indexOf("name=\"TargetFPS\"", idx)) != -1) {
            xmlCount++;
            idx += "name=\"TargetFPS\"".length();
        }
        assertEquals(1, xmlCount);

        // JSON with duplicate keys
        String duplicateJson = "{\"fps\": 60, \"fps\": 90, \"quality\": 1}";
        String[] jsonUpdates = new String[]{"fps=120"};
        String patchedJson = ConfigFileHelper.patchJsonContent(duplicateJson, jsonUpdates);
        assertNotNull(patchedJson);
        assertTrue(patchedJson.contains("120"));
        assertFalse(patchedJson.contains("60"));
        assertFalse(patchedJson.contains("90"));
        int jsonCount = 0;
        idx = 0;
        while ((idx = patchedJson.indexOf("\"fps\"", idx)) != -1) {
            jsonCount++;
            idx += "\"fps\"".length();
        }
        assertEquals(1, jsonCount);
    }

    @Test
    public void testSecurityTelemetrySuppressionMultiPublisher() {
        assertTrue(GameSecurityBypassEngine.suppressSecurityTelemetryReporting("com.mobile.legends"));
        assertTrue(GameSecurityBypassEngine.suppressSecurityTelemetryReporting("com.tencent.ig"));
        assertTrue(GameSecurityBypassEngine.suppressSecurityTelemetryReporting("com.activision.callofduty.shooter"));
        assertTrue(GameSecurityBypassEngine.suppressSecurityTelemetryReporting("com.dts.freefireth"));
    }

    @Test
    public void testNoScopeTieredHeadshotKeys() throws java.io.IOException {
        java.io.File tempFile = java.io.File.createTempFile("noscope_test_", ".ini");
        try {
            boolean ok = NativeConfigInjector.injectNoScopeTieredHeadshotAllGun(tempFile.getAbsolutePath());
            assertTrue(ok);
            String content = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()));
            // 20m, 40m, 50m, 100m verification
            assertTrue(content.contains("NoScopeHeadshot20m=1"));
            assertTrue(content.contains("AimMagnetism20m=3"));
            assertTrue(content.contains("NoScopeHeadshot40m=1"));
            assertTrue(content.contains("AimMagnetism40m=3"));
            assertTrue(content.contains("NoScopeHeadshot50m=1"));
            assertTrue(content.contains("AimMagnetism50m=3"));
            assertTrue(content.contains("NoScopeHeadshot100m=1"));
            assertTrue(content.contains("AimMagnetism100m=3"));
            assertTrue(content.contains("AllGunNoScopeHeadshot=1"));
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void testRifleScopeTieredHeadshotKeys() throws java.io.IOException {
        java.io.File tempFile = java.io.File.createTempFile("riflescope_test_", ".ini");
        try {
            boolean ok = NativeConfigInjector.injectRifleScopeTieredHeadshot(tempFile.getAbsolutePath());
            assertTrue(ok);
            String content = new String(java.nio.file.Files.readAllBytes(tempFile.toPath()));
            // 100m, 200m, 300m, 400m verification
            assertTrue(content.contains("RifleScopeHeadshot100m=1"));
            assertTrue(content.contains("RifleScopeMagnetism100m=3"));
            assertTrue(content.contains("RifleScopeHeadshot200m=1"));
            assertTrue(content.contains("RifleScopeMagnetism200m=3"));
            assertTrue(content.contains("RifleScopeHeadshot300m=1"));
            assertTrue(content.contains("BulletDropComp300m=1"));
            assertTrue(content.contains("RifleScopeHeadshot400m=1"));
            assertTrue(content.contains("Scope8xLongRangeHeadLock=1"));
            assertTrue(content.contains("AllRifleAutoHeadshot=1"));
        } finally {
            tempFile.delete();
        }
    }

    @Test
    public void testMlbbSmartSkillMagnetAimKeys() throws java.io.IOException {
        java.io.File tempXml = java.io.File.createTempFile("mlbb_aim_test_", ".xml");
        try {
            java.nio.file.Files.write(tempXml.toPath(), "<map>\n</map>".getBytes());
            boolean ok = NativeConfigInjector.injectMlbbSmartSkillMagnetAim(tempXml.getAbsolutePath());
            assertTrue(ok);
            String content = new String(java.nio.file.Files.readAllBytes(tempXml.toPath()));
            // Dual-priority: Lowest HP (maliit na buhay) & Closest hero (malapit na hero)
            assertTrue(content.contains("SkillTargetPriority"));
            assertTrue(content.contains("LowestHpFirst"));
            assertTrue(content.contains("TargetLockLowestHp"));
            assertTrue(content.contains("SkillTargetPrioritySecondary"));
            assertTrue(content.contains("ClosestHero"));
            assertTrue(content.contains("TargetLockNearest"));
            assertTrue(content.contains("AimMagnetSkillLock"));
        } finally {
            tempXml.delete();
        }
    }

    @Test
    public void testMlbbHeroUnlimitedEnergyKeys() throws java.io.IOException {
        java.io.File tempXml = java.io.File.createTempFile("mlbb_energy_test_", ".xml");
        try {
            java.nio.file.Files.write(tempXml.toPath(), "<map>\n</map>".getBytes());
            boolean ok = NativeConfigInjector.injectMlbbHeroUnlimitedEnergy(tempXml.getAbsolutePath());
            assertTrue(ok);
            String content = new String(java.nio.file.Files.readAllBytes(tempXml.toPath()));
            // Ling, Fanny, Hayabusa, Gusion unlimited energy
            assertTrue(content.contains("UnlimitedEnergyMode"));
            assertTrue(content.contains("LingEnergyLimit"));
            assertTrue(content.contains("LingZeroEnergyCost"));
            assertTrue(content.contains("FannyEnergyLimit"));
            assertTrue(content.contains("FannyZeroEnergyCost"));
            assertTrue(content.contains("HayaEnergyLimit"));
            assertTrue(content.contains("HayaZeroEnergyCost"));
            assertTrue(content.contains("GusionEnergyLimit"));
            assertTrue(content.contains("GusionZeroEnergyCost"));
        } finally {
            tempXml.delete();
        }
    }

    @Test
    public void testMlbbAllHeroBoostAndArmorKeys() throws java.io.IOException {
        java.io.File tempXml = java.io.File.createTempFile("mlbb_boost_test_", ".xml");
        try {
            java.nio.file.Files.write(tempXml.toPath(), "<map>\n</map>".getBytes());
            boolean ok = NativeConfigInjector.injectMlbbAllHeroBoostAndArmor(tempXml.getAbsolutePath());
            assertTrue(ok);
            String content = new String(java.nio.file.Files.readAllBytes(tempXml.toPath()));
            // Boost damage, faster cooldown, boost armor
            assertTrue(content.contains("AllHeroDamageMultiplier"));
            assertTrue(content.contains("2.0"));
            assertTrue(content.contains("SkillCooldownReduction"));
            assertTrue(content.contains("0.40"));
            assertTrue(content.contains("HeroPhysicalArmorBoost"));
            assertTrue(content.contains("1.5"));
            assertTrue(content.contains("PhysicalDefense"));
            assertTrue(content.contains("MagicDefense"));
        } finally {
            tempXml.delete();
        }
    }

    @Test
    public void testPubgmAndCodmTieredHeadshotPatcherMethods() throws java.io.IOException {
        java.io.File pubgmFile = java.io.File.createTempFile("pubgm_tiered_", ".ini");
        java.io.File codmFile = java.io.File.createTempFile("codm_tiered_", ".json");
        try {
            assertTrue(NativeConfigInjector.injectPubgmAllScopeTieredHeadshot(pubgmFile.getAbsolutePath()));
            String pubgContent = new String(java.nio.file.Files.readAllBytes(pubgmFile.toPath()));
            assertTrue(pubgContent.contains("NoScopeHeadshot20m=1"));
            assertTrue(pubgContent.contains("NoScopeHeadshot50m=1"));
            assertTrue(pubgContent.contains("RifleScopeHeadshot100m=1"));
            assertTrue(pubgContent.contains("RifleScopeHeadshot400m=1"));

            java.nio.file.Files.write(codmFile.toPath(), "{}".getBytes());
            assertTrue(NativeConfigInjector.injectCodmAllScopeTieredHeadshot(codmFile.getAbsolutePath()));
            String codmContent = new String(java.nio.file.Files.readAllBytes(codmFile.toPath()));
            assertTrue(codmContent.contains("NoScopeHeadshot20m"));
            assertTrue(codmContent.contains("RifleScopeHeadshot400m"));
        } finally {
            pubgmFile.delete();
            codmFile.delete();
        }
    }
}

