package com.gamebooster.app.spoofer;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpoofSanityCheckerTest {

    @Test
    public void testInferGpuFamily() {
        assertEquals(SpoofSanityChecker.GpuFamily.ADRENO, 
                SpoofSanityChecker.inferGpuFamily("Qualcomm", "Adreno (TM) 830"));
        assertEquals(SpoofSanityChecker.GpuFamily.MALI, 
                SpoofSanityChecker.inferGpuFamily("ARM", "Mali-G720"));
        assertEquals(SpoofSanityChecker.GpuFamily.MALI, 
                SpoofSanityChecker.inferGpuFamily("ARM", "Immortalis-G925"));
        assertEquals(SpoofSanityChecker.GpuFamily.APPLE, 
                SpoofSanityChecker.inferGpuFamily("Apple Inc.", "Apple A18 Pro GPU"));
        assertEquals(SpoofSanityChecker.GpuFamily.POWERVR, 
                SpoofSanityChecker.inferGpuFamily("Imagination Technologies", "PowerVR Rogue"));
        assertEquals(SpoofSanityChecker.GpuFamily.NVIDIA, 
                SpoofSanityChecker.inferGpuFamily("NVIDIA Corporation", "NVIDIA Tegra"));
    }

    @Test
    public void testSanityCheck_SameGpuFamily_Allowed() {
        SpoofProfile rog = SpoofProfileRegistry.getById("asus_rog9_pro");
        assertNotNull(rog);

        SpoofSanityChecker.SanityResult result = SpoofSanityChecker.check(
                com.gamebooster.app.device.DeviceDetector.ChipsetVendor.QUALCOMM, 
                rog
        );
        assertTrue(result.allowed);
    }
}
