package com.gamespace.app.utils

import android.os.Build
import java.io.BufferedReader
import java.io.FileReader
import java.io.RandomAccessFile

object DeviceDetector {

    fun getDeviceInfoMap(): Map<String, Any> {
        val hardware = Build.HARDWARE ?: "Unknown"
        val board = Build.BOARD ?: "Unknown"
        val manufacturer = Build.MANUFACTURER ?: "Unknown"
        val model = Build.MODEL ?: "Unknown"
        val cpuInfoHardware = parseCpuInfoHardware()
        val chipset = detectChipset(hardware, board, cpuInfoHardware)
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

    private fun parseCpuInfoHardware(): String {
        return try {
            val reader = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            var cpuHardware = ""
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("Hardware") || line!!.startsWith("Processor")) {
                    val parts = line!!.split(":")
                    if (parts.size > 1) {
                        cpuHardware = parts[1].trim()
                        break
                    }
                }
            }
            reader.close()
            cpuHardware
        } catch (e: Exception) {
            ""
        }
    }

    private fun detectChipset(hardware: String, board: String, cpuInfoHardware: String): String {
        val combined = "$hardware $board $cpuInfoHardware".lowercase()

        return when {
            combined.contains("mt") || combined.contains("mediatek") || combined.contains("helio") || combined.contains("dimensity") -> "MediaTek (Helio / Dimensity)"
            combined.contains("qcom") || combined.contains("snapdragon") || combined.contains("msm") || combined.contains("sdm") || combined.contains("sm") -> "Qualcomm Snapdragon"
            combined.contains("ums") || combined.contains("sp98") || combined.contains("unisoc") || combined.contains("sc98") || combined.contains("spreadtrum") -> "Unisoc (Tiger Series)"
            combined.contains("exynos") || combined.contains("universal") || combined.contains("s5e") -> "Samsung Exynos"
            combined.contains("gs101") || combined.contains("gs201") || combined.contains("zuma") || combined.contains("tensor") -> "Google Tensor"
            combined.contains("kirin") || combined.contains("hi36") || combined.contains("hi62") -> "HiSilicon Kirin"
            else -> if (cpuInfoHardware.isNotEmpty()) cpuInfoHardware else "$hardware ($board)"
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
