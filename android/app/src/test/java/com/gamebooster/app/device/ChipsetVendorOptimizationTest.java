package com.gamebooster.app.device;

import com.gamebooster.app.booster.GpuTweaksChannel;
import com.gamebooster.app.booster.ThermalChannel;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChipsetVendorOptimizationTest {

    @Test
    public void testAllSixChipsetVendorsExist() {
        assertNotNull(DeviceDetector.ChipsetVendor.QUALCOMM);
        assertNotNull(DeviceDetector.ChipsetVendor.MEDIATEK);
        assertNotNull(DeviceDetector.ChipsetVendor.EXYNOS);
        assertNotNull(DeviceDetector.ChipsetVendor.TENSOR);
        assertNotNull(DeviceDetector.ChipsetVendor.KIRIN);
        assertNotNull(DeviceDetector.ChipsetVendor.UNISOC);
    }

    @Test
    public void testChipsetVendorDetectionNotNull() {
        DeviceDetector.ChipsetVendor vendor = DeviceDetector.detectChipsetVendor();
        assertNotNull(vendor);
    }

    @Test
    public void testGpuTweaksChannelCoversAllSixChipsets() {
        // Exynos
        assertTrue(GpuTweaksChannel.enableExynosXclipseBoost());
        assertTrue(GpuTweaksChannel.applyExtendedExynosFlags());

        // Kirin
        assertTrue(GpuTweaksChannel.enableKirinBoost());
        assertTrue(GpuTweaksChannel.applyExtendedKirinFlags());

        // UNISOC
        assertTrue(GpuTweaksChannel.enableUnisocBoost());
        assertTrue(GpuTweaksChannel.applyExtendedUnisocFlags());

        // Adreno / MediaTek / Tensor
        assertTrue(GpuTweaksChannel.enableAdrenoTurbo());
        assertTrue(GpuTweaksChannel.applyExtendedAdrenoFlags());
        assertTrue(GpuTweaksChannel.enableMediaTekGedBoost());
        assertTrue(GpuTweaksChannel.applyExtendedMediaTekFlags());
        assertTrue(GpuTweaksChannel.enableTensorBoost());
        assertTrue(GpuTweaksChannel.applyExtendedTensorFlags());

        // Universal Max Performance Runner
        assertTrue(GpuTweaksChannel.setGpuMaxPerformance());
    }

    @Test
    public void testThermalBypassRunsCleanly() {
        // Both bypass true and false should execute without throwing exceptions
        ThermalChannel.setThermalOverride(true);
        ThermalChannel.setThermalOverride(false);
    }
}
