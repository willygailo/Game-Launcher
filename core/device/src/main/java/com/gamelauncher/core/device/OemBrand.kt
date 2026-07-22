// core/device/src/main/java/com/gamelauncher/core/device/OemBrand.kt
package com.gamelauncher.core.device

/**
 * OemBrand — Enum categorizing detected OEM device manufacturer / ROM skin.
 */
enum class OemBrand(val displayName: String) {
    PIXEL("Google Pixel"),
    XIAOMI("Xiaomi (MIUI/HyperOS)"),
    ONEPLUS("OnePlus (OxygenOS)"),
    OPPO_REALME("Oppo/Realme (ColorOS)"),
    SAMSUNG("Samsung (One UI)"),
    TRANSSION("Infinix/Tecno (HiOS/XOS)"),
    GENERIC("Generic Android (AOSP)")
}
