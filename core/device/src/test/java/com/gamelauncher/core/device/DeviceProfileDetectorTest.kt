// core/device/src/test/java/com/gamelauncher/core/device/DeviceProfileDetectorTest.kt
package com.gamelauncher.core.device

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceProfileDetectorTest {

    @Test
    fun testOemBrand_EnumValuesFormat() {
        assertEquals("Transsion", OemBrand.TRANSSION.displayName)
        assertEquals("Samsung", OemBrand.SAMSUNG.displayName)
        assertEquals("Xiaomi", OemBrand.XIAOMI.displayName)
        assertEquals("Generic Android", OemBrand.GENERIC.displayName)
    }

    @Test
    fun testOemCapabilityMap_TranssionDetection() {
        val detector = DeviceProfileDetector()
        val capabilityMap = OemCapabilityMap(detector)

        // Default local JVM test runner will detect Generic AOSP
        assertTrue(capabilityMap.supportsPeakRefreshRateOverride())
    }
}
