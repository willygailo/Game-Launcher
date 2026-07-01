package com.gamelauncher.core

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.concurrent.Volatile
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
    @Volatile private var batteryLevel: Int = 100
    @Volatile private var batteryTemp: Float = 25f
    @Volatile private var batteryStatus: String = "Unknown"
    @Volatile private var batteryVoltage: Int = 4200
    @Volatile private var batteryHealth: String = "Unknown"
    @Volatile private var batteryTech: String = "Unknown"

    private var lastCpuTime: Long = 0
    private var lastCpuIdle: Long = 0
    private var thermalHistory = mutableListOf<Int>()

    init {
        initInitialBatteryState()
        registerBatteryReceiver()
    }

    private fun initInitialBatteryState() {
        try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            if (level in 1..100) {
                batteryLevel = level
            }
        } catch (_: Exception) {}
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
                lastCpuTime = cpu; lastCpuIdle = idle; return 0f
            }
            val diffCpu = cpu - lastCpuTime
            val diffIdle = idle - lastCpuIdle
            lastCpuTime = cpu; lastCpuIdle = idle
            if (diffCpu + diffIdle > 0) ((diffCpu.toFloat() / (diffCpu + diffIdle)) * 100f).coerceIn(0f, 100f) else 0f
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
            if (freqFile.exists()) freqFile.readText().trim().toLongOrNull()?.div(1000) ?: 0L else 0L
        } catch (e: Exception) { 0L }
    }

    fun getCpuGovernor(): String {
        return try {
            val govFile = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")
            if (govFile.exists()) govFile.readText().trim().ifEmpty { "sched" } else "sched"
        } catch (e: Exception) { "sched" }
    }

    fun getCoreCount(): Int = Runtime.getRuntime().availableProcessors()

    fun getPerCoreOnlineStatus(): List<Boolean> {
        val count = getCoreCount()
        return (0 until count).map { i ->
            try {
                File("/sys/devices/system/cpu/cpu$i/online").readText().trim() == "1"
            } catch (_: Exception) { true }
        }
    }

    suspend fun setCoreOnline(coreIndex: Int, online: Boolean): Boolean = withContext(Dispatchers.IO) {
        if (!rootShellManager.isRootAvailable()) return@withContext false
        val value = if (online) "1" else "0"
        val (success, _) = rootShellManager.executeCommand("echo $value > /sys/devices/system/cpu/cpu$coreIndex/online")
        success
    }

    fun getRamInfo(): Triple<Long, Long, Long> {
        return try {
            val info = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(info)
            val totalMb = info.totalMem / 1_048_576L
            val freeMb = info.availMem / 1_048_576L
            Triple(totalMb, totalMb - freeMb, freeMb)
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
    fun getSocInfo(): SocInfo = socManager.getSocInfo()
    fun getDeviceRating(): Int = socManager.getDeviceRating()

    fun getThermalStatus(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE
            } else PowerManager.THERMAL_STATUS_NONE
        } catch (e: Exception) { PowerManager.THERMAL_STATUS_NONE }
    }

    fun getCpuTemperature(): Float {
        val paths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/class/thermal/thermal_zone2/temp",
            "/sys/class/thermal/thermal_zone3/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp"
        )
        return try {
            paths.firstNotNullOfOrNull { path ->
                runCatching { File(path).readText().trim().toFloatOrNull() }.getOrNull()
            }?.let { it / 1000f } ?: 0f
        } catch (_: Exception) { 0f }
    }

    fun isThermalThrottlingActive(): Boolean {
        val status = getThermalStatus()
        val temp = getCpuTemperature()
        return status >= PowerManager.THERMAL_STATUS_MODERATE || temp > 65f
    }

    fun getBatteryLevel(): Int = getBatteryLevelInt()
    fun getBatteryTech(): String = batteryTech
    fun getCpuFreqKhz(): Long = getCpuFreqMhz() * 1000L
    fun getRamUsedMb(): Long = getRamInfo().second
    fun getRamFreeMb(): Long = getRamInfo().third
    fun getRamTotalMb(): Long = getRamInfo().first

    fun optimizeMemory(): Long {
        return killBackgroundAppsSync()
    }

    private fun killBackgroundAppsSync(): Long {
        val (_, usedBefore, _) = getRamInfo()
        try {
            val am = context.getSystemService(ActivityManager::class.java)
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            }
            for (app in apps) {
                val pkg = app.activityInfo.packageName
                if (pkg != context.packageName) {
                    try { am?.killBackgroundProcesses(pkg) } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
        System.gc()
        try { Thread.sleep(200) } catch (e: Exception) {}
        val (_, usedAfter, _) = getRamInfo()
        return (usedBefore - usedAfter).coerceAtLeast(0L)
    }

    suspend fun killBackgroundApps(): Long = withContext(Dispatchers.IO) {
        val (_, usedBefore, _) = getRamInfo()
        if (rootShellManager.isRootAvailable()) {
            rootShellManager.executeCommand("am kill-all")
            rootShellManager.executeCommand("sync")
            rootShellManager.executeCommand("echo 3 > /proc/sys/vm/drop_caches")
            rootShellManager.executeCommand("echo 1 > /proc/sys/vm/compact_memory")
            try { Thread.sleep(300) } catch (e: Exception) {}
        } else {
            try {
                val am = context.getSystemService(ActivityManager::class.java)
                val pm = context.packageManager
                val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
                val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                }
                for (app in apps) {
                    val pkg = app.activityInfo.packageName
                    if (pkg != context.packageName) {
                        try { am?.killBackgroundProcesses(pkg) } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
            System.gc()
            try { Thread.sleep(200) } catch (e: Exception) {}
        }
        val (_, usedAfter, _) = getRamInfo()
        (usedBefore - usedAfter).coerceAtLeast(0L)
    }
}
