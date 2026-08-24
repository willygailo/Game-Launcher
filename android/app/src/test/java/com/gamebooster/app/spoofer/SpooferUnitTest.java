package com.gamebooster.app.spoofer;

import com.gamebooster.app.device.DeviceDetector;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SpooferUnitTest {

    @Test
    public void testProfileRegistryLoading() {
        Map<String, SpoofProfile> allProfiles = DeviceSpooferEngine.getAllProfiles();
        assertNotNull(allProfiles);
        assertFalse(allProfiles.isEmpty());

        SpoofProfile rog9 = DeviceSpooferEngine.getProfileById("asus_rog9_pro_edition");
        assertNotNull(rog9);
        assertEquals("asus", rog9.brand);
        assertEquals("SM8750-AB", rog9.socModel);
        assertEquals("Snapdragon 8 Elite", rog9.chipname);
        assertEquals("Adreno (TM) 840", rog9.glRenderer);
        assertEquals(185, rog9.maxRefreshRateHz);

        SpoofProfile s26 = DeviceSpooferEngine.getProfileById("samsung_s26_ultra");
        assertNotNull(s26);
        assertEquals("samsung", s26.brand);
    }

    @Test
    public void testProfileSystemPropertiesGeneration() {
        SpoofProfile profile = DeviceSpooferEngine.getProfileById("asus_rog9_pro_edition");
        assertNotNull(profile);

        Map<String, String> props = profile.generateSystemProperties();
        assertNotNull(props);
        assertTrue(props.containsKey("ro.product.model"));
        assertTrue(props.containsKey("ro.product.brand"));
        assertTrue(props.containsKey("ro.soc.model"));
        assertEquals("asus", props.get("ro.product.brand"));
        assertEquals("SM8750-AB", props.get("ro.soc.model"));
    }

    @Test
    public void testProfileGameConfigsGeneration() {
        SpoofProfile profile = DeviceSpooferEngine.getProfileById("asus_rog9_pro_edition");
        assertNotNull(profile);

        String ue4Config = profile.generateUe4DeviceProfile(185);
        assertNotNull(ue4Config);
        assertTrue(ue4Config.contains("[Android DeviceProfile]") || ue4Config.contains("+CVars="));
        assertTrue(ue4Config.contains("185"));

        String jsonConfig = profile.generateJsonHardwareProfile(185);
        assertNotNull(jsonConfig);
        assertTrue(jsonConfig.contains("Adreno 830") || jsonConfig.contains("ASUS"));
    }

    @Test
    public void testSmartRecommendations() {
        SpoofProfile pubgProfile = DeviceSpooferEngine.getRecommendedProfile("com.tencent.ig");
        assertNotNull(pubgProfile);

        SpoofProfile codmProfile = DeviceSpooferEngine.getRecommendedProfile("com.activision.callofduty.shooter");
        assertNotNull(codmProfile);

        SpoofProfile genshinProfile = DeviceSpooferEngine.getRecommendedProfile("com.miHoYo.GenshinImpact");
        assertNotNull(genshinProfile);
    }

    @Test
    public void testSanityChecker() {
        SpoofProfile qualcommProfile = DeviceSpooferEngine.getProfileById("asus_rog9_pro_edition");
        assertNotNull(qualcommProfile);

        // Matching GPU family (Adreno on Qualcomm)
        SpoofSanityChecker.SanityResult resultOk = SpoofSanityChecker.check(DeviceDetector.ChipsetVendor.QUALCOMM, qualcommProfile);
        assertTrue(resultOk.allowed);

        // Mismatch GPU family on high risk game
        SpoofSanityChecker.SanityResult resultMismatch = SpoofSanityChecker.checkForGame(
                DeviceDetector.ChipsetVendor.MEDIATEK,
                qualcommProfile,
                GameSpoofSafetyRegistry.RiskTier.HIGH_RISK
        );
        assertFalse(resultMismatch.allowed);
    }
}
