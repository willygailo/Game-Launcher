package com.gamebooster.app.shizuku;

import android.content.Context;
import com.gamebooster.app.engine.lua.GameOptimizationProfile;
import com.gamebooster.app.engine.lua.LuaConfigEngine;
import com.gamebooster.app.device.HardwareDisplayController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ShizukuStatusArchitectureTest {

    @Test
    public void testShizukuStatusModelDefaults() {
        ShizukuStatus status = new ShizukuStatus(
                false,
                false,
                false,
                ShizukuStatus.ConnectionState.DISCONNECTED,
                false,
                ShizukuStatus.AppStatus.RUNNING
        );

        assertFalse(status.isInstalled());
        assertFalse(status.isBinderAlive());
        assertFalse(status.isPermissionGranted());
        assertFalse(status.isServiceRunning());
        assertEquals(ShizukuStatus.ConnectionState.DISCONNECTED, status.getConnectionState());
        assertEquals("Not Installed", status.getStatusSummary());
    }

    @Test
    public void testShizukuStatusModelActiveState() {
        ShizukuStatus status = new ShizukuStatus(
                true,
                true,
                true,
                ShizukuStatus.ConnectionState.ACTIVE,
                true,
                ShizukuStatus.AppStatus.RUNNING
        );

        assertTrue(status.isInstalled());
        assertTrue(status.isBinderAlive());
        assertTrue(status.isPermissionGranted());
        assertTrue(status.isServiceRunning());
        assertTrue(status.isOnline());
        assertEquals(ShizukuStatus.ConnectionState.ACTIVE, status.getConnectionState());
        assertEquals("Active", status.getStatusSummary());
    }

    @Test
    public void testHardwareDisplayRateFallback() {
        // Mocking or invoking with null should return clean 60f default without crashing
        float rate = HardwareDisplayController.getMaxHardwareRefreshRate(
                com.gamebooster.app.GameBoosterApp.getInstance() != null
                        ? com.gamebooster.app.GameBoosterApp.getInstance()
                        : null
        );
        assertTrue("Refresh rate should be at least 60Hz", rate >= 60f);
    }

    @Test
    public void testGameOptimizationProfileData() {
        GameOptimizationProfile profile = new GameOptimizationProfile(
                "pubgm",
                "com.tencent.ig",
                165,
                "ULTRA_HDR",
                true,
                1000,
                "performance",
                java.util.Collections.emptyMap()
        );

        assertEquals("pubgm", profile.getGameId());
        assertEquals("com.tencent.ig", profile.getPackageName());
        assertEquals(165, profile.getTargetFps());
        assertEquals("ULTRA_HDR", profile.getGraphicsTier());
        assertTrue(profile.getForceVulkan());
        assertEquals(1000, profile.getTouchBoostHz());
        assertEquals("performance", profile.getCpuGovernor());
    }

    @Test
    public void testPubgmPresetMethods() {
        // Assert that null package returns false gracefully
        assertFalse(com.gamebooster.app.config.PubgConfigPatcher.patchUltraHdr120(null));
        assertFalse(com.gamebooster.app.config.PubgConfigPatcher.patchHdr120(null));
        assertFalse(com.gamebooster.app.config.PubgConfigPatcher.patchSuperSmooth165(null));

        // In unit test environment, fallback config patcher executes successfully for dummy paths
        boolean resUltra = com.gamebooster.app.config.PubgConfigPatcher.patchUltraHdr120("com.dummy.nonexistent");
        assertTrue(resUltra);

        boolean resHdr = com.gamebooster.app.config.PubgConfigPatcher.patchHdr120("com.dummy.nonexistent");
        assertTrue(resHdr);

        boolean resSmooth = com.gamebooster.app.config.PubgConfigPatcher.patchSuperSmooth165("com.dummy.nonexistent");
        assertTrue(resSmooth);
    }

    @Test
    public void testPakAssetFileExists() {
        java.io.File pak = new java.io.File("src/main/assets/paks/game_patch_4.6.0.21556.pak");
        assertTrue("game_patch_4.6.0.21556.pak must exist in assets/paks/", pak.exists() && pak.length() > 0);
    }
}
