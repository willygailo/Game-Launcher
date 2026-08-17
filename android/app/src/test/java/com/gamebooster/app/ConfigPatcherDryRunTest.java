package com.gamebooster.app;

import org.junit.Test;
import static org.junit.Assert.*;

import com.gamebooster.app.config.CompetitiveCfgProfile;
import com.gamebooster.app.spoofer.SpoofProfile;
import com.gamebooster.app.spoofer.SpoofProfileRegistry;
import com.gamebooster.app.spoofer.brands.AsusRogProfiles;
import com.gamebooster.app.spoofer.brands.SamsungProfiles;
import com.gamebooster.app.spoofer.brands.XiaomiProfiles;
import com.gamebooster.app.spoofer.brands.VivoProfiles;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ConfigPatcherDryRunTest {

    @Test
    public void testCompetitiveCfgProfileDefaults() {
        CompetitiveCfgProfile profile = CompetitiveCfgProfile.defaultCompetitive(CompetitiveCfgProfile.GAME_PUBGM);
        assertNotNull("Profile should not be null", profile);
        assertEquals("Target FPS should default to 185", 185, profile.getTargetFps());
        assertTrue("Super fast touch should be enabled", profile.isSuperFastTouchEnabled());
        assertTrue("Force write system Hz should be enabled", profile.isForceWriteSystemHz());
        assertTrue("Aim assist should be enabled", profile.isAimAssistEnabled());
        assertTrue("Recoil control should be enabled", profile.isRecoilControlEnabled());
    }

    @Test
    public void testAllSpoofProfilesRegistered() {
        Map<String, SpoofProfile> all = SpoofProfileRegistry.getAllProfiles();
        assertNotNull("Spoof profiles map should not be null", all);
        assertTrue("Should have multiple flagship profiles", all.size() >= 10);

        SpoofProfile rog9 = SpoofProfileRegistry.getById("asus_rog9_pro");
        assertNotNull("ASUS ROG 9 Pro profile should exist", rog9);
        assertEquals("Brand should be asus", "asus", rog9.brand);
        assertTrue("Display name should contain 185Hz", rog9.displayName.contains("185Hz"));
        assertTrue("RAM total should be 24GB", rog9.ramTotalMb >= 24000);

        String ue4Profile = rog9.generateUe4DeviceProfile(185);
        assertNotNull("UE4 device profile should generate", ue4Profile);
        assertTrue("UE4 profile should contain 185 FPS", ue4Profile.contains("185"));
        assertTrue("UE4 profile should contain DeviceProfile", ue4Profile.contains("DeviceProfile"));

        String jsonProfile = rog9.generateJsonHardwareProfile(185);
        assertNotNull("JSON hardware profile should generate", jsonProfile);
        assertTrue("JSON should contain model", jsonProfile.contains(rog9.model));
    }

    @Test
    public void testSamsungFlagshipProfile() {
        List<SpoofProfile> samsungList = SamsungProfiles.getProfiles();
        assertNotNull(samsungList);
        assertFalse(samsungList.isEmpty());
        SpoofProfile s26 = samsungList.get(0);
        assertTrue(s26.displayName.contains("S26 Ultra") || s26.displayName.contains("S25 Ultra"));
        assertEquals("Qualcomm", s26.glVendor);
    }

    @Test
    public void testBinaryByteScanningSimulation() {
        // Simulate an Active.sav binary payload containing GVAS properties
        String dummySavContent = "GVAS\0\0\0\1\0\0\0FPSLevel\0\0\0\0\0\0\0\0\0\5BattleFPS\0\0\0\0\0\0\0\0\0\5LobbyFPS\0\0\0\0\0\0\0\0\0\5";
        byte[] data = dummySavContent.getBytes(StandardCharsets.US_ASCII);

        String[] keys = {"FPSLevel", "BattleFPS", "LobbyFPS"};
        int patchedCount = 0;
        int targetLevel = 10; // 185 FPS

        for (String key : keys) {
            byte[] keyBytes = key.getBytes(StandardCharsets.US_ASCII);
            int idx = -1;
            // Scan bytes
            for (int i = 0; i <= data.length - keyBytes.length; i++) {
                boolean match = true;
                for (int j = 0; j < keyBytes.length; j++) {
                    if (data[i + j] != keyBytes[j]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    idx = i;
                    break;
                }
            }

            assertTrue("Key " + key + " should be found in binary stream", idx != -1);
            int valOffset = idx + keyBytes.length;
            for (int o = valOffset; o < Math.min(valOffset + 32, data.length); o++) {
                if (data[o] >= 1 && data[o] <= 10) {
                    data[o] = (byte) targetLevel;
                    patchedCount++;
                    break;
                }
            }
        }

        assertEquals("All 3 FPS keys should be patched to level 10", 3, patchedCount);
    }
}
