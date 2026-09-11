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

    @Test
    public void testUe4ProfileKeysValidEnumAndCvars() {
        SpoofProfile profile = DeviceSpooferEngine.getRecommendedProfile("com.tencent.ig");
        assertNotNull(profile);

        String[] keys120 = profile.generateUe4DeviceProfileKeys(120);
        boolean hasValidFrameRateLevel = false;
        boolean hasCVar02 = false;
        boolean hasLevel10 = false;

        for (String k : keys120) {
            if (k.equals("FrameRateLevel=7")) hasValidFrameRateLevel = true;
            if (k.equals("+CVars=0,2,120")) hasCVar02 = true;
            if (k.contains("FrameRateLevel=10") || k.contains("PUBGDeviceFPS=10")) hasLevel10 = true;
        }

        assertTrue("120 FPS must use valid UE4 Level 7", hasValidFrameRateLevel);
        assertTrue("Must include +CVars=0,2,120 for UE4 frame cap unlock", hasCVar02);
        assertFalse("Must never output invalid Level 10 which breaks UE4 enum bounds", hasLevel10);
    }

    @Test
    public void testInjectHardwareMaskProfileJson() throws java.io.IOException {
        java.io.File tempJson = java.io.File.createTempFile("HardwareProfile", ".json");
        tempJson.deleteOnExit();

        boolean ok = com.gamebooster.app.config.NativeConfigInjector.injectHardwareMaskProfile(
                tempJson.getAbsolutePath(),
                "Adreno (TM) 750",
                "Snapdragon 8 Gen 3",
                16384,
                120
        );
        assertTrue("JSON hardware mask injection should succeed", ok);

        String content = new String(java.nio.file.Files.readAllBytes(tempJson.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue("Must contain JSON property GPURenderer", content.contains("\"GPURenderer\": \"Adreno (TM) 750\""));
        assertTrue("Must contain JSON property MaxFrameRate", content.contains("\"MaxFrameRate\": 120"));
        assertFalse("Must not contain INI section in JSON file", content.contains("[HardwareProfile]"));
    }

    @Test
    public void testInjectHardwareMaskProfileXmlForMlbb() throws java.io.IOException {
        java.io.File tempXml = java.io.File.createTempFile("mlbb_playerprefs", ".xml");
        tempXml.deleteOnExit();

        boolean ok = com.gamebooster.app.config.NativeConfigInjector.injectHardwareMaskProfile(
                tempXml.getAbsolutePath(),
                "Adreno (TM) 750",
                "ASUS_AI2401",
                16384,
                120
        );
        assertTrue("XML playerprefs injection should succeed", ok);

        String content = new String(java.nio.file.Files.readAllBytes(tempXml.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue("Must contain HighFpsMode=120", content.contains("name=\"HighFpsMode\" value=\"120\""));
        assertTrue("Must contain HighFpsModeSee=4", content.contains("name=\"HighFpsModeSee\" value=\"4\""));
        assertTrue("Must contain HighFPS=3", content.contains("name=\"HighFPS\" value=\"3\""));
        assertTrue("Must contain spoofed deviceModel", content.contains("name=\"SystemInfo_deviceModel\">ASUS_AI2401</string>"));
    }

    @Test
    public void testUe4ProfileKeys185FpsUncapped() {
        SpoofProfile profile = DeviceSpooferEngine.getRecommendedProfile("com.tencent.ig");
        assertNotNull(profile);

        String[] keys185 = profile.generateUe4DeviceProfileKeys(185);
        boolean hasLevel10 = false;
        boolean hasCVar02_185 = false;
        boolean hasUnlock185 = false;

        for (String k : keys185) {
            if (k.equals("FrameRateLevel=10")) hasLevel10 = true;
            if (k.equals("+CVars=0,2,185")) hasCVar02_185 = true;
            if (k.equals("Unlock185Hz=1") || k.equals("Unlock185FPS=1")) hasUnlock185 = true;
        }

        assertTrue("185 FPS must map to FrameRateLevel=10", hasLevel10);
        assertTrue("185 FPS must include +CVars=0,2,185", hasCVar02_185);
        assertTrue("185 FPS must include Unlock185Hz=1 or Unlock185FPS=1", hasUnlock185);
    }

    @Test
    public void testInjectHardwareMaskProfile185FpsJsonAndXml() throws java.io.IOException {
        java.io.File tempJson = java.io.File.createTempFile("HardwareProfile185", ".json");
        tempJson.deleteOnExit();

        boolean okJson = com.gamebooster.app.config.NativeConfigInjector.injectHardwareMaskProfile(
                tempJson.getAbsolutePath(),
                "Adreno (TM) 830",
                "SM8750-AB",
                16384,
                185
        );
        assertTrue("JSON hardware mask injection for 185 FPS should succeed", okJson);

        String contentJson = new String(java.nio.file.Files.readAllBytes(tempJson.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue("Must contain MaxFrameRate: 185", contentJson.contains("\"MaxFrameRate\": 185"));
        assertTrue("Must contain Unlock185Hz: true", contentJson.contains("\"Unlock185Hz\": true"));

        java.io.File tempXml = java.io.File.createTempFile("mlbb_playerprefs_185", ".xml");
        tempXml.deleteOnExit();

        boolean okXml = com.gamebooster.app.config.NativeConfigInjector.injectHardwareMaskProfile(
                tempXml.getAbsolutePath(),
                "Adreno (TM) 830",
                "ASUS_AI2501",
                16384,
                185
        );
        assertTrue("XML playerprefs injection for 185 FPS should succeed", okXml);

        String contentXml = new String(java.nio.file.Files.readAllBytes(tempXml.toPath()), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue("Must contain HighFpsMode=185", contentXml.contains("name=\"HighFpsMode\" value=\"185\""));
        assertTrue("Must contain HighFpsModeSee=5", contentXml.contains("name=\"HighFpsModeSee\" value=\"5\""));
        assertTrue("Must contain HighFPS=4", contentXml.contains("name=\"HighFPS\" value=\"4\""));
        assertTrue("Must contain TargetFrameRate=185", contentXml.contains("name=\"TargetFrameRate\" value=\"185\""));
    }
}
