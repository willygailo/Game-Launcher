package com.gamelauncher.core.device

/**
 * OemBrand — Enum categorizing detected OEM device manufacturer / ROM skin.
 */
enum class OemBrand(val displayName: String) {
    INFINIX_HIOS("Infinix (HiOS)"),
    TECNO_XOS("Tecno (XOS)"),
    SAMSUNG_ONEUI("Samsung (One UI)"),
    XIAOMI_HYPEROS("Xiaomi (MIUI/HyperOS)"),
    GENERIC_AOSP("Generic Android (AOSP)")
}
