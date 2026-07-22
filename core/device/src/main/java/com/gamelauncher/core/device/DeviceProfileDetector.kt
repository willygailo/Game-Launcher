package com.gamelauncher.core.device

import android.os.Build
import com.gamelauncher.core.shizuku.IShellExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DeviceProfileDetector — Inspects device hardware properties and Android system properties
 * to detect OEM ROM variant (Infinix HiOS, Tecno XOS, Samsung One UI, Xiaomi HyperOS, AOSP).
 *
 * Primary Path: Uses IShellExecutor.executeArgs("getprop", key) via Shizuku/ADB context to bypass
 * hidden API greylist restrictions and SELinux sandboxing.
 * Fallback Path: SystemProperties reflection and Build fields for pre-Shizuku cold boot detection.
 */
@Singleton
class DeviceProfileDetector @Inject constructor(
    private val shellExecutor: IShellExecutor
) {
    /**
     * OEM Property Hypotheses (Transsion HiOS / XOS):
     * NOTE: Property keys 'ro.gn.gnromver' and 'ro.build.uiversion' are unverified OEM hypotheses.
     * They must be validated on physical Infinix/Tecno hardware during full device testing.
     */
    companion object {
        const val PROP_HIOS_XOS_ROM_VER = "ro.gn.gnromver"
        const val PROP_UI_VERSION = "ro.build.uiversion"
    }

    suspend fun detectOemBrand(): OemBrand = withContext(Dispatchers.IO) {
        val manufacturer = Build.MANUFACTURER?.uppercase() ?: ""
        val brand = Build.BRAND?.uppercase() ?: ""

        // 1. Direct Build field inspection
        if (manufacturer.contains("INFINIX") || brand.contains("INFINIX")) {
            return@withContext OemBrand.INFINIX_HIOS
        }
        if (manufacturer.contains("TECNO") || brand.contains("TECNO")) {
            return@withContext OemBrand.TECNO_XOS
        }
        if (manufacturer.contains("SAMSUNG") || brand.contains("SAMSUNG")) {
            return@withContext OemBrand.SAMSUNG_ONEUI
        }
        if (manufacturer.contains("XIAOMI") || brand.contains("XIAOMI") || brand.contains("POCO") || brand.contains("REDMI")) {
            return@withContext OemBrand.XIAOMI_HYPEROS
        }

        // 2. Shell getprop property inspection via Shizuku (bypasses SELinux)
        val hiosVer = getSystemPropertyViaShell(PROP_HIOS_XOS_ROM_VER)
        val uiVer = getSystemPropertyViaShell(PROP_UI_VERSION)

        if (hiosVer.isNotEmpty() || uiVer.contains("HIOS", ignoreCase = true)) {
            return@withContext OemBrand.INFINIX_HIOS
        }
        if (uiVer.contains("XOS", ignoreCase = true)) {
            return@withContext OemBrand.TECNO_XOS
        }

        // 3. SystemProperties reflection fallback
        val reflectedHios = getSystemPropertyViaReflection(PROP_HIOS_XOS_ROM_VER)
        if (reflectedHios.isNotEmpty()) {
            return@withContext OemBrand.INFINIX_HIOS
        }

        OemBrand.GENERIC_AOSP
    }

    private suspend fun getSystemPropertyViaShell(key: String): String {
        return try {
            val result = shellExecutor.executeArgs("getprop", key)
            if (result.exitCode == 0) result.stdout.trim() else ""
        } catch (_: Exception) {
            ""
        }
    }

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun getSystemPropertyViaReflection(key: String): String {
        return try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java, String::class.java)
            (getMethod.invoke(null, key, "") as? String) ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}
