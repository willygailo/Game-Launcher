// core/device/src/main/java/com/gamelauncher/core/device/DeviceProfileDetector.kt
package com.gamelauncher.core.device

import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DeviceProfileDetector — Inspects device hardware properties to detect OEM ROM variant
 * and expose matching OEM tweak keys.
 */
@Singleton
class DeviceProfileDetector @Inject constructor() {

    fun detectOemBrand(): OemBrand = resolveBrandInternal()

    /**
     * Synchronously resolves OemBrand from Build properties and returns matching OemTweakKeys.
     */
    fun getTweakKeys(): OemTweakKeys {
        val oemBrand = resolveBrandInternal()
        return when (oemBrand) {
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

    private fun resolveBrandInternal(): OemBrand {
        val manufacturer = Build.MANUFACTURER?.uppercase() ?: ""
        val brand = Build.BRAND?.uppercase() ?: ""

        return when {
            manufacturer.contains("GOOGLE") || brand.contains("GOOGLE") -> OemBrand.PIXEL
            manufacturer.contains("XIAOMI") || brand.contains("XIAOMI") || brand.contains("POCO") || brand.contains("REDMI") -> OemBrand.XIAOMI
            manufacturer.contains("ONEPLUS") || brand.contains("ONEPLUS") -> OemBrand.ONEPLUS
            manufacturer.contains("OPPO") || brand.contains("OPPO") || manufacturer.contains("REALME") || brand.contains("REALME") -> OemBrand.OPPO_REALME
            manufacturer.contains("SAMSUNG") || brand.contains("SAMSUNG") -> OemBrand.SAMSUNG
            manufacturer.contains("INFINIX") || brand.contains("INFINIX") || manufacturer.contains("TECNO") || brand.contains("TECNO") || manufacturer.contains("ITEL") || brand.contains("ITEL") -> OemBrand.TRANSSION
            else -> OemBrand.GENERIC
        }
    }
}
