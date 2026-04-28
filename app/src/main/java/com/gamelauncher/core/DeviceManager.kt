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
    private val rootShellManager: RootShellManager
) {
    private val activityManager = context.getSystemService(ActivityManager::class.java)
    private val powerManager = context.getSystemService(PowerManager::class.java)
    private var batteryLevel: Int = 100
    private var batteryTemp: Float = 25f
    private var batteryStatus: String = "Unknown"

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
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        batteryStatus = when (status) {
                            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                            BatteryManager.BATTERY_STATUS_FULL -> "Full"
                            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                            else -> "Unknown"
                        }
                    }
                }
            }
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {}
    }

    fun getCpuUsagePercent(): Float {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val cpuUsage = activityManager.processesInErrorState
            val load = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val total = Runtime.getRuntime().totalMemory()
            val usagePercent = (load.toFloat() / total.toFloat() * 100f)
            return usagePercent.coerceIn(0f, 100f)
        } catch (e: Exception) { 0f }
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