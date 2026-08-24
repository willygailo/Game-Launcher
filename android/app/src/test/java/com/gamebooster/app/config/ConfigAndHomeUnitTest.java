package com.gamebooster.app.config;

import com.gamebooster.app.gamemanager.GameManagerStatus;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * ConfigAndHomeUnitTest — Tests for GameConfigPathResolver, FpsUnlockTier,
 * CompetitiveCfgProfile, GameManagerStatus, and ShellSafety.
 */
public class ConfigAndHomeUnitTest {

    @Test
    public void testGameConfigPathResolver_generateBasePaths() {
        String pkg = "com.mobile.legends";
        List<String> basePaths = GameConfigPathResolver.generateBasePaths(pkg);
        assertNotNull(basePaths);
        assertFalse(basePaths.isEmpty());
        assertTrue(basePaths.size() >= 8);

        // Verify standard Android storage roots exist
        assertTrue(basePaths.contains("/sdcard/Android/data/com.mobile.legends"));
        assertTrue(basePaths.contains("/storage/emulated/0/Android/data/com.mobile.legends"));
        assertTrue(basePaths.contains("/data/data/com.mobile.legends"));
        assertTrue(basePaths.contains("/data/user/0/com.mobile.legends"));
        assertTrue(basePaths.contains("/sdcard/Android/media/com.mobile.legends"));
    }

    @Test
    public void testGameConfigPathResolver_getKnownRelativePaths() {
        // Test MLBB known relative paths
        List<String> mlbbPaths = GameConfigPathResolver.getKnownRelativePathsForPackage("com.mobile.legends");
        assertNotNull(mlbbPaths);
        assertFalse(mlbbPaths.isEmpty());
        assertTrue(mlbbPaths.stream().anyMatch(p -> p.contains("UserSystem.ini")));

        // Test PUBG known relative paths
        List<String> pubgPaths = GameConfigPathResolver.getKnownRelativePathsForPackage("com.tencent.ig");
        assertNotNull(pubgPaths);
        assertFalse(pubgPaths.isEmpty());
        assertTrue(pubgPaths.stream().anyMatch(p -> p.contains("UserCustom.ini")));

        // Test CODM known relative paths
        List<String> codmPaths = GameConfigPathResolver.getKnownRelativePathsForPackage("com.activision.callofduty.shooter");
        assertNotNull(codmPaths);
        assertFalse(codmPaths.isEmpty());
        assertTrue(codmPaths.stream().anyMatch(p -> p.contains("PlayerPrefs.xml") || p.contains("Config")));
    }

    @Test
    public void testFpsUnlockTier_resolutions() {
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(90, FpsUnlockTier.resolveTargetFps(90));
        assertEquals(185, FpsUnlockTier.resolveTargetFps(0)); // 0 falls back to top tier 185

        int[] allValues = FpsUnlockTier.getAllFpsValues();
        assertNotNull(allValues);
        assertEquals(5, allValues.length);
        assertEquals(185, allValues[4]);

        String[] allLabels = FpsUnlockTier.getAllLabels();
        assertNotNull(allLabels);
        assertEquals(allValues.length, allLabels.length);
    }

    @Test
    public void testCompetitiveCfgProfile_toggles() {
        CompetitiveCfgProfile profile = new CompetitiveCfgProfile(
                CompetitiveCfgProfile.GAME_PUBGM, 185, true, true,
                true, true, true, true, true, true, true, true, true
        );

        assertEquals(CompetitiveCfgProfile.GAME_PUBGM, profile.getGameKey());
        assertEquals(185, profile.getTargetFps());
        assertTrue(profile.isSuperFastTouchEnabled());
        assertTrue(profile.isForceWriteSystemHz());
        assertTrue(profile.isAimAssistEnabled());
        assertTrue(profile.isMlbbDamageScriptEnabled());
        assertTrue(profile.isRecoilControlEnabled());
        assertTrue(profile.isTrackingBulletEnabled());
        assertTrue(profile.isArmorDefEnabled());
        assertTrue(profile.isHardwareMaskEnabled());
        assertTrue(profile.isAntiLogEnabled());
        assertTrue(profile.isFastCooldownEnabled());
        assertTrue(profile.isShield1500Enabled());
    }

    @Test
    public void testGameManagerStatus_sessionLifecycle() {
        GameManagerStatus status = GameManagerStatus.getInstance();
        assertNotNull(status);

        // Record an apply
        status.recordApply(18, "Session Started for com.mobile.legends @ 185 FPS");
        assertTrue(status.getLastApplySummary().contains("185 FPS"));
        assertTrue(status.getLastApplyTimestamp() > 0);

        // Set active session
        status.setActiveSession("com.mobile.legends");
        assertTrue(status.hasActiveSession());
        assertEquals("com.mobile.legends", status.getActiveGamePackage());

        // End session
        status.setActiveSession(null);
        assertFalse(status.hasActiveSession());
        assertNull(status.getActiveGamePackage());
    }

    @Test
    public void testShellSafety_validation() {
        assertTrue(ShellSafety.isSafePackageName("com.mobile.legends"));
        assertTrue(ShellSafety.isSafePackageName("com.tencent.ig"));
        assertTrue(ShellSafety.isSafePackageName("com.activision.callofduty.shooter"));

        // Reject malicious injections
        assertFalse(ShellSafety.isSafePackageName("com.game; rm -rf /"));
        assertFalse(ShellSafety.isSafePackageName("com.game && echo 1"));
        assertFalse(ShellSafety.isSafePackageName("com.game | sh"));
        assertFalse(ShellSafety.isSafePackageName(""));
        assertFalse(ShellSafety.isSafePackageName(null));
    }
}
