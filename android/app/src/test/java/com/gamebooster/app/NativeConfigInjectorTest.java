package com.gamebooster.app;

import com.gamebooster.app.config.DeltaForceConfigPatcher;
import com.gamebooster.app.config.FpsUnlockTier;
import com.gamebooster.app.config.NativeConfigInjector;
import com.gamebooster.app.config.WutheringWavesConfigPatcher;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class NativeConfigInjectorTest {

    @Test
    public void testDeltaForcePatcher() {
        List<String> paths = DeltaForceConfigPatcher.getConfigPaths("com.levelinfinite.deltaforce");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        assertTrue(paths.get(0).contains("UE4Game/DeltaForce"));
    }

    @Test
    public void testWutheringWavesPatcher() {
        List<String> paths = WutheringWavesConfigPatcher.getConfigPaths("com.kurogame.wutheringwaves.global");
        assertNotNull(paths);
        assertFalse(paths.isEmpty());
        assertTrue(paths.get(0).contains("Saved/Config/Android"));
    }

    @Test
    public void testFpsUnlockTiers() {
        assertEquals(185, FpsUnlockTier.resolveTargetFps(185));
        assertEquals(165, FpsUnlockTier.resolveTargetFps(165));
        assertEquals(144, FpsUnlockTier.resolveTargetFps(144));
        assertEquals(120, FpsUnlockTier.resolveTargetFps(120));
        assertEquals(90, FpsUnlockTier.resolveTargetFps(90));
        assertEquals(90, FpsUnlockTier.resolveTargetFps(60));
    }

    @Test
    public void testExtractKey() {
        assertEquals("r.MaxFPS", NativeConfigInjector.extractKey("r.MaxFPS=185"));
        assertEquals("+CVars=r.PUBGMaxFPS", NativeConfigInjector.extractKey("+CVars=r.PUBGMaxFPS=185"));
    }

    @Test
    public void testNonDestructiveInPlaceMerging() {
        String existingUserConfig = "[UserCustom DeviceProfile]\n" +
                "CustomHudButtonLayout=SavedLayout_3Finger\n" +
                "PlayerSensitivityLook=145\n" +
                "GyroscopeOverallSensitivity=280\n" +
                "+CVars=r.PUBGDeviceFPS=5\n" +
                "FPS=60\n";

        String[] newKeys = new String[]{
                "+CVars=r.PUBGDeviceFPS=10",
                "+CVars=r.PUBGMaxFPS=185",
                "FPS=185",
                "Unlock185FPS=1"
        };

        String merged = com.gamebooster.app.config.ConfigFileHelper.patchIniContent(
                existingUserConfig,
                newKeys,
                "[UserCustom DeviceProfile]"
        );

        // Verify that original player HUD and sensitivity settings are 100% preserved
        assertTrue(merged.contains("CustomHudButtonLayout=SavedLayout_3Finger"));
        assertTrue(merged.contains("PlayerSensitivityLook=145"));
        assertTrue(merged.contains("GyroscopeOverallSensitivity=280"));

        // Verify that target FPS keys are successfully updated/added
        assertTrue(merged.contains("+CVars=r.PUBGDeviceFPS=10"));
        assertTrue(merged.contains("+CVars=r.PUBGMaxFPS=185"));
        assertTrue(merged.contains("FPS=185"));
        assertTrue(merged.contains("Unlock185FPS=1"));
    }
}
