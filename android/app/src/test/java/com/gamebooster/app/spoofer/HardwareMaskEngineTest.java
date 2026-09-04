package com.gamebooster.app.spoofer;

import com.gamebooster.app.booster.GpuTweaksChannel;
import org.junit.Test;

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

        // Verify zero duplicate display names or models within each brand
        for (String brand : SpoofProfileRegistry.getBrandNames()) {
            List<SpoofProfile> brandProfiles = SpoofProfileRegistry.getByBrand(brand);
            assertNotNull(brandProfiles);
            assertFalse(brandProfiles.isEmpty());

            Set<String> displayNames = new HashSet<>();
            Set<String> models = new HashSet<>();
            for (SpoofProfile p : brandProfiles) {
                assertTrue("Duplicate display name in brand " + brand + ": " + p.displayName,
                        displayNames.add(p.displayName));
                assertTrue("Duplicate model code in brand " + brand + ": " + p.model,
                        models.add(p.model));
            }
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

        // CODM / Warzone -> S25 Ultra
        SpoofProfile codmProfile = DeviceSpooferEngine.getRecommendedProfile("com.activision.callofduty.shooter");
        assertNotNull(codmProfile);
        assertEquals("samsung_s25_ultra", codmProfile.id);

        // MLBB / HOK -> ASUS ROG 9 Pro
        SpoofProfile mlbbProfile = DeviceSpooferEngine.getRecommendedProfile("com.mobile.legends");
        assertNotNull(mlbbProfile);
        assertEquals("asus_rog9_pro", mlbbProfile.id);

        SpoofProfile hokProfile = DeviceSpooferEngine.getRecommendedProfile("com.levelinfinite.sgameGlobal");
        assertNotNull(hokProfile);
        assertEquals("asus_rog9_pro", hokProfile.id);

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
            assertFalse("MLBB must be exempted from READ_PHONE_STATE to avoid Moonton security crashes: " + cmd, cmd.contains("READ_PHONE_STATE"));
            assertFalse("MLBB must be exempted from READ_DEVICE_IDENTIFIERS: " + cmd, cmd.contains("READ_DEVICE_IDENTIFIERS"));
        }

        // Non-MLBB games (e.g. PUBG) should include telephony privacy shield
        Set<String> pubgCommands = new LinkedHashSet<>();
        HardwareMaskEngine.applyAppOpsShieldForPackage(pubgCommands, "com.tencent.ig");
        boolean hasPhoneState = false;
        for (String cmd : pubgCommands) {
            if (cmd.contains("READ_PHONE_STATE")) hasPhoneState = true;
        }
        assertTrue("Non-MLBB games must include READ_PHONE_STATE shield", hasPhoneState);
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
