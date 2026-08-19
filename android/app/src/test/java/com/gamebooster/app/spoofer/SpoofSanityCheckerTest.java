package com.gamebooster.app.spoofer;

import org.junit.Test;
import static org.junit.Assert.*;

public class SpoofSanityCheckerTest {

    @Test
    public void testInferGpuFamily() {
        assertEquals(SpoofSanityChecker.GpuFamily.ADRENO, SpoofSanityChecker.inferGpuFamily("Qualcomm", "Adreno (TM) 750"));
        assertEquals(SpoofSanityChecker.GpuFamily.MALI, SpoofSanityChecker.inferGpuFamily("ARM", "Mali-G720 Immortalis MC12"));
        assertEquals(SpoofSanityChecker.GpuFamily.APPLE, SpoofSanityChecker.inferGpuFamily("Apple", "Apple GPU"));
        assertEquals(SpoofSanityChecker.GpuFamily.POWERVR, SpoofSanityChecker.inferGpuFamily("Imagination", "PowerVR Rogue GE8320"));
        assertEquals(SpoofSanityChecker.GpuFamily.UNKNOWN, SpoofSanityChecker.inferGpuFamily("Custom", "CustomRendererX"));
    }

    @Test
    public void testDeviceGpuInference() {
        assertEquals(SpoofSanityChecker.GpuFamily.ADRENO, SpoofSanityChecker.inferDeviceGpuFamily(com.gamebooster.app.device.DeviceDetector.ChipsetVendor.QUALCOMM));
        assertEquals(SpoofSanityChecker.GpuFamily.MALI, SpoofSanityChecker.inferDeviceGpuFamily(com.gamebooster.app.device.DeviceDetector.ChipsetVendor.MEDIATEK));
        assertEquals(SpoofSanityChecker.GpuFamily.MALI, SpoofSanityChecker.inferDeviceGpuFamily(com.gamebooster.app.device.DeviceDetector.ChipsetVendor.EXYNOS));
    }
}
