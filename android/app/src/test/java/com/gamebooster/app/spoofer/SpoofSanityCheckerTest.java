package com.gamebooster.app.spoofer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.gamebooster.app.device.DeviceDetector;
import com.gamebooster.app.spoofer.SpoofSanityChecker.GpuFamily;
import com.gamebooster.app.spoofer.SpoofSanityChecker.SanityResult;

public class SpoofSanityCheckerTest {

    private static SpoofProfile profile(String id, String socModel, String glRenderer) {
        return new SpoofProfile(id, id, id, "Test Model", "TestBrand", "TestMfg",
                "test_hw", "test_product", "test_product", "qcom", "taro",
                socModel, "taro", "taro",
                "test/fingerprint", "TEST123", glRenderer);
    }

    // ── GPU family inference ────────────────────────────────────────────────

    @Test
    public void inferGpu_adrenoFromRendererAndVendor() {
        assertEquals(GpuFamily.ADRENO, SpoofSanityChecker.inferGpuFamily("Qualcomm", "Adreno (TM) 840"));
        assertEquals(GpuFamily.ADRENO, SpoofSanityChecker.inferGpuFamily("qualcomm", null));
    }

    @Test
    public void inferGpu_maliFromRendererAndArmVendor() {
        assertEquals(GpuFamily.MALI, SpoofSanityChecker.inferGpuFamily("ARM", "Mali-G720 Immortalis MC16"));
        assertEquals(GpuFamily.MALI, SpoofSanityChecker.inferGpuFamily("ARM", null));
        assertEquals(GpuFamily.MALI, SpoofSanityChecker.inferGpuFamily(null, "Immortalis-G925"));
    }

    @Test
    public void inferGpu_appleFromVendor() {
        assertEquals(GpuFamily.APPLE, SpoofSanityChecker.inferGpuFamily("Apple Inc.", "Apple A18 Pro GPU"));
        assertEquals(GpuFamily.APPLE, SpoofSanityChecker.inferGpuFamily("Apple", null));
    }

    @Test
    public void inferGpu_unknownWhenNothingMatches() {
        assertEquals(GpuFamily.UNKNOWN, SpoofSanityChecker.inferGpuFamily(null, null));
        assertEquals(GpuFamily.UNKNOWN, SpoofSanityChecker.inferGpuFamily("MysteryCo", "MysteryRenderer"));
    }

    @Test
    public void inferDeviceGpu_byChipset() {
        assertEquals(GpuFamily.ADRENO, SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.QUALCOMM));
        assertEquals(GpuFamily.MALI, SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.MEDIATEK));
        assertEquals(GpuFamily.MALI, SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.EXYNOS));
        assertEquals(GpuFamily.MALI, SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.KIRIN));
        assertEquals(GpuFamily.MALI, SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.TENSOR));
        assertEquals(GpuFamily.MALI, SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.UNISOC));
        assertEquals(GpuFamily.UNKNOWN, SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.GENERIC));
        assertEquals(GpuFamily.UNKNOWN, SpoofSanityChecker.inferDeviceGpuFamily(null));
    }

    // ── Pre-apply blocking ──────────────────────────────────────────────────

    @Test
    public void check_nullProfileBlocked() {
        SanityResult r = SpoofSanityChecker.check(DeviceDetector.ChipsetVendor.MEDIATEK, null);
        assertFalse(r.allowed);
        assertTrue(r.reason.contains("No spoof profile"));
    }

    @Test
    public void check_appleProfileOnMaliDeviceBlocked() {
        // Exactly the ban vector from the plan: A18 Pro GPU spoofed onto a Mali (MediaTek) device
        SpoofProfile apple = profile("apple_a18", "Apple A18 Pro", "Apple A18 Pro GPU");
        SanityResult r = SpoofSanityChecker.check(DeviceDetector.ChipsetVendor.MEDIATEK, apple);
        assertFalse(r.allowed);
        assertTrue(r.reason.contains("GPU feature set mismatch"));
        assertTrue(r.reason.contains("known ban vector"));
        assertTrue(r.reason.contains("MALI"));
        assertTrue(r.reason.contains("APPLE"));
    }

    @Test
    public void check_adrenoProfileOnMaliDeviceBlocked() {
        SpoofProfile snapdragon = profile("samsung_s26", "Snapdragon 8 Elite", "Adreno (TM) 840");
        SanityResult r = SpoofSanityChecker.check(DeviceDetector.ChipsetVendor.EXYNOS, snapdragon);
        assertFalse(r.allowed);
        assertTrue(r.reason.contains("GPU feature set mismatch"));
    }

    @Test
    public void check_matchingMaliProfileAllowed() {
        SpoofProfile dimensity = profile("mediaTek_pro", "Dimensity 9400", "Mali-G925 Immortalis");
        SanityResult r = SpoofSanityChecker.check(DeviceDetector.ChipsetVendor.MEDIATEK, dimensity);
        assertTrue(r.allowed);
        assertTrue(r.reason.contains("compatible"));
    }

    @Test
    public void check_matchingAdrenoProfileAllowed() {
        SpoofProfile snapdragon = profile("rog9", "Snapdragon 8 Elite", "Adreno (TM) 840");
        SanityResult r = SpoofSanityChecker.check(DeviceDetector.ChipsetVendor.QUALCOMM, snapdragon);
        assertTrue(r.allowed);
    }

    @Test
    public void check_socMismatchBlocksEvenWhenGpuMatches() {
        // Adreno GPU on both sides, but device is Snapdragon and profile claims Dimensity
        SpoofProfile dimensity = profile("mediattek_x", "Dimensity 9400", "Adreno (TM) 840");
        SanityResult r = SpoofSanityChecker.check(DeviceDetector.ChipsetVendor.QUALCOMM, dimensity);
        assertFalse(r.allowed);
        assertTrue(r.reason.contains("SoC feature set mismatch"));
    }

    @Test
    public void check_undetectableDeviceAllowedWithWarning() {
        SpoofProfile apple = profile("apple_a18", "Apple A18 Pro", "Apple GPU");
        SanityResult r = SpoofSanityChecker.check(DeviceDetector.ChipsetVendor.GENERIC, apple);
        assertTrue(r.allowed);
        assertTrue(r.reason.contains("not verifiable"));
    }

    @Test
    public void check_nullChipsetAllowedWhenProfileUnknown() {
        SpoofProfile mystery = profile("mystery", "Mystery SoC", "Mystery Renderer");
        SanityResult r = SpoofSanityChecker.check(null, mystery);
        assertTrue(r.allowed);
        assertNotNull(r.reason);
    }
}