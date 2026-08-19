package com.gamebooster.app.spoofer;

import com.gamebooster.app.device.DeviceDetector;
import org.junit.Test;
import static org.junit.Assert.*;

public class SpoofSanityCheckerTest {

    @Test
    public void testInferGpuFamilyFromRenderer() {
        assertEquals(SpoofSanityChecker.GpuFamily.ADRENO,
                SpoofSanityChecker.inferGpuFamily("Qualcomm", "Adreno (TM) 750"));
        assertEquals(SpoofSanityChecker.GpuFamily.MALI,
                SpoofSanityChecker.inferGpuFamily("ARM", "Mali-G720 Immortalis MC12"));
        assertEquals(SpoofSanityChecker.GpuFamily.APPLE,
                SpoofSanityChecker.inferGpuFamily("Apple Inc.", "Apple A18 Pro GPU"));
        assertEquals(SpoofSanityChecker.GpuFamily.POWERVR,
                SpoofSanityChecker.inferGpuFamily("Imagination Technologies", "PowerVR GM9446"));
    }

    @Test
    public void testInferDeviceGpuFamilyFromChipset() {
        assertEquals(SpoofSanityChecker.GpuFamily.ADRENO,
                SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.QUALCOMM));
        assertEquals(SpoofSanityChecker.GpuFamily.MALI,
                SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.MEDIATEK));
        assertEquals(SpoofSanityChecker.GpuFamily.MALI,
                SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.TENSOR));
        assertEquals(SpoofSanityChecker.GpuFamily.MALI,
                SpoofSanityChecker.inferDeviceGpuFamily(DeviceDetector.ChipsetVendor.EXYNOS));
    }
}
