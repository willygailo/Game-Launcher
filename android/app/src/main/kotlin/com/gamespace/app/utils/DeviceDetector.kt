package com.gamespace.app.utils

import android.os.Build
import java.io.RandomAccessFile

object DeviceDetector {

    fun getDeviceInfoMap(): Map<String, Any> {
        val hardware = Build.HARDWARE ?: "Unknown"
        val board = Build.BOARD ?: "Unknown"
        val manufacturer = Build.MANUFACTURER ?: "Unknown"
        val model = Build.MODEL ?: "Unknown"
        val chipset = detectChipset(hardware, board)
        val cores = Runtime.getRuntime().availableProcessors()
        val totalRamMb = getTotalMemoryMb()

        return mapOf(
            "manufacturer" to manufacturer,
            "model" to model,
            "hardware" to hardware,
            "board" to board,
            "chipset" to chipset,
            "cpuCores" to cores,
            "totalRamMb" to totalRamMb,
            "androidVersion" to Build.VERSION.RELEASE,
            "sdkInt" to Build.VERSION.SDK_INT
        )
    }

    private fun detectChipset(hardware: String, board: String): String {
        val hwLower = hardware.lowercase()
        val boardLower = board.lowercase()

        return when {
            hwLower.contains("mt") || boardLower.contains("mt") -> "MediaTek (Helio / Dimensity)"
            hwLower.contains("qcom") || hwLower.contains("snapdragon") || boardLower.contains("msm") || boardLower.contains("sdm") -> "Qualcomm Snapdragon"
            hwLower.contains("ums") || hwLower.contains("sp98") || hwLower.contains("unisoc") || boardLower.contains("sc98") -> "Unisoc (Tiger Series)"
            hwLower.contains("exynos") || boardLower.contains("universal") || boardLower.contains("s5e") -> "Samsung Exynos"
            hwLower.contains("gs101") || hwLower.contains("gs201") || hwLower.contains("zuma") || boardLower.contains("tensor") -> "Google Tensor"
            hwLower.contains("kirin") || boardLower.contains("hi36") || boardLower.contains("hi62") -> "HiSilicon Kirin"
            else -> "$hardware ($board)"
        }
    }

    private fun getTotalMemoryMb(): Long {
        return try {
            val reader = RandomAccessFile("/proc/meminfo", "r")
            val load = reader.readLine()
            reader.close()
            val tokens = load.split("\\s+".toRegex())
            tokens[1].toLong() / 1024
        } catch (e: Exception) {
            0L
        }
    }
}
