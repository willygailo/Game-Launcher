// core/device/src/main/java/com/gamelauncher/core/device/OemBrand.kt
package com.gamelauncher.core.device

/**
 * OemBrand — Enum categorizing detected OEM device manufacturer / ROM skin.
 */
enum class OemBrand(val displayName: String, val osName: String) {
    INFINIX("Infinix", "XOS"),
    TECNO("Tecno", "HiOS"),
    SAMSUNG("Samsung", "One UI"),
    XIAOMI("Xiaomi", "HyperOS / MIUI"),
    HUAWEI("Huawei", "EMUI / HarmonyOS"),
    PIXEL("Google", "Pixel UI"),
    ONEPLUS("OnePlus", "OxygenOS"),
    OPPO_REALME("Oppo/Realme", "ColorOS"),
    TRANSSION("Transsion", "XOS / HiOS"),
    GENERIC("Generic Android", "AOSP")
}

