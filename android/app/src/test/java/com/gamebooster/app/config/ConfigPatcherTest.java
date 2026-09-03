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
}
