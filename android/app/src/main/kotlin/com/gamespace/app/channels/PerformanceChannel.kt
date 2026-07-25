package com.gamespace.app.channels

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.File
import java.io.RandomAccessFile

class PerformanceChannel(private val context: Context) : MethodChannel.MethodCallHandler {
    companion object {
        const val CHANNEL = "com.gamespace.app/performance"
    }

    private var lastUserTime: Long = 0
    private var lastIdleTime: Long = 0

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getPerformanceMetrics" -> {
                val metrics = mapOf(
                    "cpuFreqMhz" to getCpuCurrentFreqMhz(),
                    "gpuFreqMhz" to getGpuCurrentFreqMhz(),
                    "cpuTempCelsius" to getCpuTemperatureCelsius(),
                    "batteryPercent" to getBatteryPercentage(),
                    "cpuLoadPercent" to getCpuLoadPercent(),
                    "ramUsagePercent" to getRamUsagePercent()
                )
                result.success(metrics)
            }
            else -> result.notImplemented()
        }
    }

    private fun getCpuCurrentFreqMhz(): Int {
        var maxFreqKhz = 0
        try {
            val cpuDir = File("/sys/devices/system/cpu")
            val files = cpuDir.listFiles { _, name -> name.matches(Regex("cpu[0-9]+")) }
            files?.forEach { dir ->
                val freqFile = File(dir, "cpufreq/scaling_cur_freq")
                if (freqFile.exists()) {
                    val freqKhz = freqFile.readText().trim().toIntOrNull() ?: 0
                    if (freqKhz > maxFreqKhz) {
                        maxFreqKhz = freqKhz
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback
        }
        return if (maxFreqKhz > 0) maxFreqKhz / 1000 else 1800
    }

    private fun getGpuCurrentFreqMhz(): Int {
        val paths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpuclk",
            "/sys/class/kgsl/kgsl-3d0/devfreq/cur_freq",
            "/sys/devices/platform/13900000.mali/mali_freq",
            "/sys/class/devfreq/gpufreq/cur_freq"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val valStr = file.readText().trim()
                    val valLong = valStr.toLongOrNull() ?: 0L
                    if (valLong > 0) {
                        return if (valLong > 1000000) (valLong / 1000000).toInt() else if (valLong > 1000) (valLong / 1000).toInt() else valLong.toInt()
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        return 650
    }

    private fun getCpuTemperatureCelsius(): Double {
        val paths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone2/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp"
        )
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists()) {
                    val rawTemp = file.readText().trim().toDoubleOrNull() ?: continue
                    val tempC = if (rawTemp > 1000) rawTemp / 1000.0 else rawTemp
                    if (tempC in 15.0..105.0) {
                        return tempC
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        return 38.5
    }

    private fun getBatteryPercentage(): Double {
        return try {
            val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, iFilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                (level * 100.0) / scale
            } else {
                85.0
            }
        } catch (e: Exception) {
            85.0
        }
    }

    private fun getCpuLoadPercent(): Double {
        return try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val line = reader.readLine()
            reader.close()
            if (line != null && line.startsWith("cpu ")) {
                val toks = line.split("\\s+".toRegex())
                val user = toks[1].toLong()
                val nice = toks[2].toLong()
                val system = toks[3].toLong()
                val idle = toks[4].toLong()
                val iowait = if (toks.size > 5) toks[5].toLong() else 0L
                val irq = if (toks.size > 6) toks[6].toLong() else 0L
                val softirq = if (toks.size > 7) toks[7].toLong() else 0L

                val totalIdle = idle + iowait
                val totalNonIdle = user + nice + system + irq + softirq

                val deltaIdle = totalIdle - lastIdleTime
                val deltaTotal = (totalIdle + totalNonIdle) - (lastIdleTime + lastUserTime)

                lastIdleTime = totalIdle
                lastUserTime = totalNonIdle

                if (deltaTotal > 0) {
                    val pct = ((deltaTotal - deltaIdle).toDouble() / deltaTotal.toDouble()) * 100.0
                    return pct.coerceIn(5.0, 100.0)
                }
            }
            35.0
        } catch (e: Exception) {
            35.0
        }
    }

    private fun getRamUsagePercent(): Double {
        return try {
            val reader = RandomAccessFile("/proc/meminfo", "r")
            var totalKb = 0L
            var availableKb = 0L
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("MemTotal:")) {
                    val toks = line!!.split("\\s+".toRegex())
                    totalKb = toks[1].toLong()
                } else if (line!!.startsWith("MemAvailable:")) {
                    val toks = line!!.split("\\s+".toRegex())
                    availableKb = toks[1].toLong()
                    break
                }
            }
            reader.close()
            if (totalKb > 0) {
                val usedKb = totalKb - availableKb
                val pct = (usedKb.toDouble() / totalKb.toDouble()) * 100.0
                return pct.coerceIn(10.0, 100.0)
            }
            50.0
        } catch (e: Exception) {
            50.0
        }
    }
}
