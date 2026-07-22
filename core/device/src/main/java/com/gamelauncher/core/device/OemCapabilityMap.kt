// core/device/src/main/java/com/gamelauncher/core/device/OemCapabilityMap.kt
package com.gamelauncher.core.device

import javax.inject.Inject
import javax.inject.Singleton

/**
 * OemTweakKeys — Data class holding OEM-specific system setting keys and capabilities.
 */
data class OemTweakKeys(
    val refreshRateKey: String,
    val thermalOverrideSupported: Boolean,
    val gameDriverSupported: Boolean
)

/**
 * OemCapabilityMap — Feature flags mapping OEM capabilities based on detected brand.
 */
@Singleton
class OemCapabilityMap @Inject constructor(
    private val detector: DeviceProfileDetector
) {
    fun currentBrand(): OemBrand = detector.detectOemBrand()

    fun getTweakKeysForBrand(brand: OemBrand): OemTweakKeys {
        return when (brand) {
            OemBrand.PIXEL -> OemTweakKeys(
                refreshRateKey = "peak_refresh_rate",
                thermalOverrideSupported = true,
                gameDriverSupported = true
            )
            OemBrand.XIAOMI -> OemTweakKeys(
                refreshRateKey = "user_refresh_rate",
                thermalOverrideSupported = true,
                gameDriverSupported = true
            )
            OemBrand.ONEPLUS -> OemTweakKeys(
                refreshRateKey = "oneplus_screen_refresh_rate",
                thermalOverrideSupported = false,
                gameDriverSupported = true
            )
            OemBrand.OPPO_REALME -> OemTweakKeys(
                refreshRateKey = "oppo_display_refresh_rate",
                thermalOverrideSupported = false,
                gameDriverSupported = true
            )
            OemBrand.SAMSUNG -> OemTweakKeys(
                refreshRateKey = "refresh_rate_mode",
                thermalOverrideSupported = false,
                gameDriverSupported = false
            )
            OemBrand.TRANSSION -> OemTweakKeys(
                refreshRateKey = "peak_refresh_rate",
                thermalOverrideSupported = true,
                gameDriverSupported = true
            )
            OemBrand.GENERIC -> OemTweakKeys(
                refreshRateKey = "peak_refresh_rate",
                thermalOverrideSupported = true,
                gameDriverSupported = true
            )
        }
    }

    fun supportsTranssionGameMode(): Boolean {
        return currentBrand() == OemBrand.TRANSSION
    }

    fun supportsThermalThrottlingOverride(): Boolean {
        return detector.getTweakKeys().thermalOverrideSupported
    }

    fun supportsPeakRefreshRateOverride(): Boolean {
        return true
    }
}
