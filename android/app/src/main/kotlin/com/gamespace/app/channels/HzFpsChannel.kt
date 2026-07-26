package com.gamespace.app.channels

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import com.gamespace.app.utils.ShellExecutor
import com.gamespace.app.utils.ShizukuExecutor
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HzFpsChannel(private val context: Context) : MethodChannel.MethodCallHandler {
    companion object {
        const val CHANNEL = "com.gamespace.app/hz_fps"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getDisplayModes" -> {
                val displayInfo = getDisplayRefreshRateInfo()
                result.success(displayInfo)
            }
            "setTargetRefreshRate" -> {
                val hz = call.argument<Double>("hz") ?: 120.0
                val mode = call.argument<String>("mode") ?: "auto" // root, shizuku, or auto
                scope.launch {
                    val success = applyRefreshRateTweak(hz, mode)
                    result.success(success)
                }
            }
            "setGameModeFps" -> {
                val packageName = call.argument<String>("packageName") ?: ""
                val targetFps = call.argument<Int>("fps") ?: 120
                val mode = call.argument<String>("mode") ?: "auto"
                scope.launch {
                    val success = applyGameModeFps(packageName, targetFps, mode)
                    result.success(success)
                }
            }
            "setThermalOverride" -> {
                val mode = call.argument<String>("mode") ?: "auto"
                scope.launch {
                    val success = applyThermalOverride(mode)
                    result.success(success)
                }
            }
            else -> result.notImplemented()
        }
    }

    private fun getDisplayRefreshRateInfo(): Map<String, Any> {
        val rates = mutableSetOf<Double>()
        var currentHz = 60.0

        try {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)

            if (display != null) {
                currentHz = display.refreshRate.toDouble()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    display.supportedModes.forEach { m ->
                        rates.add(m.refreshRate.toDouble())
                    }
                } else {
                    rates.add(currentHz)
                }
            }
        } catch (e: Exception) {
            rates.addAll(listOf(60.0, 90.0, 120.0, 144.0))
        }

        if (rates.isEmpty()) {
            rates.addAll(listOf(60.0, 90.0, 120.0))
        }

        return mapOf(
            "currentHz" to currentHz,
            "supportedRates" to rates.sorted()
        )
    }

    private fun applyRefreshRateTweak(hz: Double, mode: String): Boolean {
        val hzStr = String.format("%.1f", hz)
        val hzInt = hz.toInt()

        val commands = listOf(
            "settings put system peak_refresh_rate $hzStr",
            "settings put system min_refresh_rate $hzStr",
            "settings put global user_refresh_rate $hzInt",
            "settings put system user_refresh_rate $hzInt",
            "setprop persist.sys.sf.fps $hzInt"
        )

        return executeCommandBatch(commands, mode)
    }

    private fun applyGameModeFps(packageName: String, targetFps: Int, mode: String): Boolean {
        if (packageName.isEmpty()) return false

        val commands = mutableListOf(
            "cmd game mode performance $packageName",
            "cmd game set --fps $targetFps $packageName",
            "cmd game set --mode 2 --downscale 1.0 $packageName"
        )

        return executeCommandBatch(commands, mode)
    }

    private fun applyThermalOverride(mode: String): Boolean {
        val commands = listOf(
            "cmd thermal override-status 0",
            "dumpsys thermal reset",
            "settings put global low_power 0",
            "setprop sys.thermal.mode 0"
        )

        return executeCommandBatch(commands, mode)
    }

    private fun executeCommandBatch(commands: List<String>, mode: String): Boolean {
        var successCount = 0

        val useRoot = when (mode) {
            "root" -> true
            "shizuku" -> false
            else -> ShellExecutor.isRootAvailable()
        }

        for (cmd in commands) {
            val success = if (useRoot) {
                val res = ShellExecutor.executeRootCommand(cmd)
                res.success
            } else if (ShizukuExecutor.isPermissionGranted()) {
                val res = ShizukuExecutor.executeShizukuCommand(cmd)
                res.success
            } else {
                false
            }

            if (success) successCount++
        }

        return successCount > 0
    }
}
