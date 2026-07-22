package com.gamelauncher.core.device

import com.gamelauncher.core.shizuku.IShellExecutor
import com.gamelauncher.core.shizuku.ShellResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceProfileDetectorTest {

    @Test
    fun testOemBrand_EnumValuesFormat() {
        assertEquals("Infinix (HiOS)", OemBrand.INFINIX_HIOS.displayName)
        assertEquals("Tecno (XOS)", OemBrand.TECNO_XOS.displayName)
        assertEquals("Samsung (One UI)", OemBrand.SAMSUNG_ONEUI.displayName)
        assertEquals("Xiaomi (MIUI/HyperOS)", OemBrand.XIAOMI_HYPEROS.displayName)
        assertEquals("Generic Android (AOSP)", OemBrand.GENERIC_AOSP.displayName)
    }

    @Test
    fun testOemCapabilityMap_TranssionDetection() = runBlocking {
        // Use interface stub for IShellExecutor
        val stubExecutor = object : IShellExecutor {
            override suspend fun executeCommand(command: String, timeoutMs: Long): ShellResult =
                ShellResult(0, "", "")

            override suspend fun executeArgs(vararg args: String, timeoutMs: Long): ShellResult =
                ShellResult(0, "", "")
        }

        val detector = DeviceProfileDetector(stubExecutor)
        val capabilityMap = OemCapabilityMap(detector)

        // Default local JVM test runner will detect Generic AOSP
        assertTrue(capabilityMap.supportsPeakRefreshRateOverride())
    }
}
