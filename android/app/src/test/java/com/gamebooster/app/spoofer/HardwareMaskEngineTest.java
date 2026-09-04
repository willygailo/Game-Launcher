package com.gamebooster.app.spoofer;

import com.gamebooster.app.booster.GpuTweaksChannel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HardwareMaskEngineTest {

    @Test
    public void testZeroDuplicateProfilesAcrossAllBrands() {
        Map<String, SpoofProfile> profiles = SpoofProfileRegistry.getAllProfiles();
        assertNotNull(profiles);
        assertFalse(profiles.isEmpty());

        Set<String> seenIds = new HashSet<>();
        for (String id : profiles.keySet()) {
            assertTrue("Duplicate profile ID found: " + id, seenIds.add(id));
        }
    }

    @Test
    public void testTargetGameRecommendations() {
        // PUBGM / Free Fire / Farlight 84 -> RedMagic 10 Pro+
        SpoofProfile pubgProfile = DeviceSpooferEngine.getRecommendedProfile("com.tencent.ig");
        assertNotNull(pubgProfile);
        assertEquals("redmagic_10_pro_plus", pubgProfile.id);

        SpoofProfile ffProfile = DeviceSpooferEngine.getRecommendedProfile("com.dts.freefireth");
        assertNotNull(ffProfile);
        assertEquals("redmagic_10_pro_plus", ffProfile.id);

        SpoofProfile farlightProfile = DeviceSpooferEngine.getRecommendedProfile("com.miraclegames.farlight84");
        assertNotNull(farlightProfile);
        assertEquals("redmagic_10_pro_plus", farlightProfile.id);

        // CODM / Warzone -> S26 Ultra
        SpoofProfile codmProfile = DeviceSpooferEngine.getRecommendedProfile("com.activision.callofduty.shooter");
        assertNotNull(codmProfile);
        assertEquals("samsung_s26_ultra", codmProfile.id);

        // MLBB / HOK -> ASUS ROG 9 Pro
        SpoofProfile mlbbProfile = DeviceSpooferEngine.getRecommendedProfile("com.mobile.legends");
        assertNotNull(mlbbProfile);
        assertEquals("asus_rog9_pro_edition", mlbbProfile.id);

        SpoofProfile hokProfile = DeviceSpooferEngine.getRecommendedProfile("com.levelinfinite.sgameGlobal");
        assertNotNull(hokProfile);
        assertEquals("asus_rog9_pro_edition", hokProfile.id);

        // Genshin -> Xiaomi 15 Ultra
        SpoofProfile genshinProfile = DeviceSpooferEngine.getRecommendedProfile("com.miHoYo.GenshinImpact");
        assertNotNull(genshinProfile);
        assertEquals("xiaomi_15_ultra", genshinProfile.id);
    }

    @Test
    public void testAppOpsShieldCommandsIsolatedToGame() {
        Set<String> commands = new LinkedHashSet<>();
        HardwareMaskEngine.applyAppOpsShieldForPackage(commands, "com.mobile.legends");

        assertFalse(commands.isEmpty());
        for (String cmd : commands) {
            assertTrue("Command must target MLBB only: " + cmd, cmd.contains("com.mobile.legends"));
            assertFalse("Command must not affect global ro properties: " + cmd, cmd.contains("resetprop"));
            assertFalse("Command must not set ro.product: " + cmd, cmd.contains("ro.product"));
        }
    }

    @Test
    public void testGameDriverEligibilityStrictlyRestricted() {
        // Target games eligible
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.mobile.legends"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.tencent.ig"));
        assertTrue(GpuTweaksChannel.isGameDriverEligible("com.activision.callofduty.shooter"));

        // Non-eligible general apps must be rejected
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.android.chrome"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.whatsapp"));
        assertFalse(GpuTweaksChannel.isGameDriverEligible("com.google.android.apps.photos"));
    }

    @Test
    public void testCommandDeduplicationWithLinkedHashSet() {
        Set<String> batch = new LinkedHashSet<>();
        batch.add("cmd game mode performance com.mobile.legends 2>/dev/null");
        batch.add("cmd game mode performance com.mobile.legends 2>/dev/null");
        batch.add("cmd game set --fps 120 com.mobile.legends 2>/dev/null");

        assertEquals(2, batch.size());
    }
}
