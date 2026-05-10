package com.gamelauncher.core

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootShellManager: RootShellManager,
    private val socManager: SocManager
) {
    private val activityManager = context.getSystemService(ActivityManager::class.java)
    private val powerManager = context.getSystemService(PowerManager::class.java)
    private var batteryLevel: Int = 100
    private var batteryTemp: Float = 25f
    private var batteryStatus: String = "Unknown"
    private var batteryVoltage: Int = 4200
    private var batteryHealth: String = "Unknown"
    private var batteryTech: String = "Unknown"

    init {
        registerBatteryReceiver()
    }

    private fun registerBatteryReceiver() {
        try {
            val batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                        batteryLevel = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / 
                            intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1))
                        batteryTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250) / 10f
                        batteryVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4200)
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        batteryStatus = when (status) {
                            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                            BatteryManager.BATTERY_STATUS_FULL -> "Full"
                            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                            else -> "Unknown"
                        }
                        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                        batteryHealth = when (health) {
                            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                            else -> "Unknown"
                        }
                        batteryTech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
                    }
                }
            }
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {}
    }

    private var lastCpuTime: Long = 0
    private var lastCpuIdle: Long = 0

    fun getCpuUsagePercent(): Float {
        return try {
            val reader = java.io.RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()

            val toks = load.split(" +".toRegex()).dropLastWhile { it.isEmpty() }
            val idle = toks[4].toLong()
            val cpu = toks[1].toLong() + toks[2].toLong() + toks[3].toLong() + toks[5].toLong() +
                      toks[6].toLong() + toks[7].toLong()

            if (lastCpuTime == 0L) {
                lastCpuTime = cpu
                lastCpuIdle = idle
                return 0f
            }

            val diffCpu = cpu - lastCpuTime
            val diffIdle = idle - lastCpuIdle
            lastCpuTime = cpu
            lastCpuIdle = idle

            if (diffCpu + diffIdle > 0) {
                ((diffCpu.toFloat() / (diffCpu + diffIdle)) * 100f).coerceIn(0f, 100f)
            } else 0f
        } catch (e: Exception) {
            try {
                val total = Runtime.getRuntime().totalMemory()
                val free = Runtime.getRuntime().freeMemory()
                ((total - free).toFloat() / total.toFloat() * 100f).coerceIn(0f, 100f)
            } catch (e2: Exception) { 0f }
        }
    }

    fun getCpuFreqMhz(): Long {
        return try {
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (freqFile.exists()) {
                freqFile.readText().trim().toLongOrNull()?.div(1000) ?: return 0L
            }
            0L
        } catch (e: Exception) { 0L }
    }

    fun getCpuMaxFreqMhz(): Long {
        return try {
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            if (freqFile.exists()) {
                freqFile.readText().trim().toLongOrNull()?.div(1000) ?: 3000L
            }
            3000L
        } catch (e: Exception) { 3000L }
    }

    fun getCpuGovernor(): String {
        return try {
            val govFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
            if (govFile.exists()) govFile.readText().trim().ifEmpty { "sched" } else "sched"
        } catch (e: Exception) { "sched" }
    }

    fun getCoreCount(): Int = Runtime.getRuntime().availableProcessors()

    fun getRamInfo(): Triple<Long, Long, Long> {
        return try {
            val info = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(info)
            val totalMb = info.totalMem / 1_048_576L
            val freeMb = info.availMem / 1_048_576L
            val usedMb = totalMb - freeMb
            Triple(totalMb, usedMb, freeMb)
        } catch (e: Exception) {
            val total = Runtime.getRuntime().totalMemory() / 1_048_576L
            val free = Runtime.getRuntime().freeMemory() / 1_048_576L
            Triple(total, total - free, free)
        }
    }

    fun getBatteryLevelInt(): Int = batteryLevel

    fun getBatteryTemperatureFloat(): Float = batteryTemp

    fun getBatteryStatusString(): String = batteryStatus

    fun getBatteryVoltage(): Int = batteryVoltage

    fun getBatteryHealth(): String = batteryHealth

    fun getBatteryTech(): String = batteryTech

    fun getSocInfo(): SocInfo = socManager.getSocInfo()

    fun getDeviceRating(): Int = socManager.getDeviceRating()

    fun getThermalStatus(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                powerManager?.currentThermalStatus ?: 0
            } else 0
        } catch (e: Exception) { 0 }
    }

    // Legacy methods for backward compatibility
    fun getBatteryLevel(): Int = getBatteryLevelInt()
    fun getBatteryTemperature(): Float = getBatteryTemperatureFloat()
    fun getBatteryStatus(): String = getBatteryStatusString()
    fun getCpuFreqKhz(): Long = getCpuFreqMhz() * 1000L
    fun getCpuMaxFreqKhz(): Long = getCpuMaxFreqMhz() * 1000L
    fun getRamUsedMb(): Long = getRamInfo().second
    fun getRamFreeMb(): Long = getRamInfo().third
    fun getRamTotalMb(): Long = getRamInfo().first

    suspend fun killBackgroundApps(): Long = withContext(Dispatchers.IO) {
        val (_, usedBefore, _) = getRamInfo()
        
        if (rootShellManager.isRootAvailable()) {
            rootShellManager.executeCommand("am kill-all")
            rootShellManager.executeCommand("sync")
            rootShellManager.executeCommand("echo 3 > /proc/sys/vm/drop_caches")
            try { Thread.sleep(300) } catch (e: Exception) {}
        } else {
            System.gc()
            Runtime.getRuntime().gc()
            try { Thread.sleep(200) } catch (e: Exception) {}
        }
        
        val (_, usedAfter, _) = getRamInfo()
        (usedBefore - usedAfter).coerceAtLeast(0L)
    }

    fun optimizeMemory(): Long {
        val (_, usedBefore, _) = getRamInfo()
        System.gc()
        Runtime.getRuntime().gc()
        try { Thread.sleep(200) } catch (e: Exception) {}
        val (_, usedAfter, _) = getRamInfo()
        return (usedBefore - usedAfter).coerceAtLeast(0L)
    }
}
