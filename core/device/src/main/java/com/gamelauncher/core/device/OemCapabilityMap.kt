package com.gamelauncher.core.device

import javax.inject.Inject
import javax.inject.Singleton

/**
 * OemCapabilityMap — Feature flags mapping OEM capabilities based on detected brand.
 */
@Singleton
class OemCapabilityMap @Inject constructor(
    private val detector: DeviceProfileDetector
) {
    suspend fun currentBrand(): OemBrand = detector.detectOemBrand()

    /**
     * Indicates whether the device supports Transsion (Infinix/Tecno) proprietary performance flags.
     */
    suspend fun supportsTranssionGameMode(): Boolean {
        val brand = currentBrand()
        return brand == OemBrand.INFINIX_HIOS || brand == OemBrand.TECNO_XOS
    }

    /**
     * Indicates whether thermal throttling override is supported via system settings or ADB.
     */
    suspend fun supportsThermalThrottlingOverride(): Boolean {
        val brand = currentBrand()
        return brand == OemBrand.INFINIX_HIOS || brand == OemBrand.TECNO_XOS || brand == OemBrand.GENERIC_AOSP
    }

    /**
     * Indicates whether 120Hz/144Hz high refresh rate forcing is supported.
     * NOTE: Currently synchronous because it returns static support capability. If dynamic per-device
     * display mode hardware probing (e.g., via dumpsys display) is added in future steps, this method
     * MUST be refactored into a suspend function for non-blocking I/O consistency.
     */
    fun supportsPeakRefreshRateOverride(): Boolean {
        return true
    }
}
