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
}
