package com.gamelauncher.core

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TouchLatencyOptimizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val rootShellManager: RootShellManager
) {
    private var touchBoostEnabled = false

    fun isTouchLatencyReductionSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    suspend fun enableTouchOptimizations(): Boolean = withContext(Dispatchers.IO) {
        touchBoostEnabled = true
        
        if (rootShellManager.isRootAvailable()) {
            applyRootTouchOptimizations()
        } else {
            applyNonRootTouchOptimizations()
        }
        true
    }

    suspend fun disableTouchOptimizations(): Boolean = withContext(Dispatchers.IO) {
        touchBoostEnabled = false
        
        if (rootShellManager.isRootAvailable()) {
            restoreRootTouchOptimizations()
        }
        true
    }

    private fun applyRootTouchOptimizations() {
        val commands = listOf(
            // Hardware digitizer sysfs overrides
            "echo 0 > /sys/class/input/input*/poll_delay",
            "echo 0 > /sys/touchpanel/tp_debug_mask",
            "echo 1 > /sys/class/input/input*/gesture",
            "echo 1 > /sys/devices/virtual/input/fingerprint/fpc1020/report_touch",
            "echo Y > /sys/module/synaptics_dsx/parameters/pinctrl_state",
            "echo 0 > /sys/module/tpd_setting/parameters/tpd_em_debounce_time",
            "echo 0 > /sys/module/tpd_setting/parameters/tpd_em_debounce_time0",
            "echo 0 > /sys/module/tpd_setting/parameters/tpd_em_debounce_time1",
            "echo 1 > /proc/touchpanel/game_switch_enable",
            "echo 1 > /sys/class/touch/switch/set_touchscreen",
            "echo 0 > /sys/class/touchscreen/primary/edge_filter",
            "echo 1 > /sys/touchpanel/touch_game_mode",
            
            // Touch performance system props
            "setprop view.touch_slop 2", // Lowest threshold for instant response
            "setprop view.scroll_friction 10",
            "setprop windowsmgr.max_events_per_sec 2000",
            "setprop ro.max.fling_velocity 20000",
            "setprop ro.min.fling_velocity 8000",
            "setprop persist.vendor.qti.input.touch_boost 1",
            "setprop ro.vendor.touch.sens.supp true",
            "setprop sys.use_fifo_ui 1",
            "setprop debug.hwui.render_thread true",
            "setprop debug.sf.latch_unsignaled 1"
        )

        for (cmd in commands) {
            runCatching { 
                Runtime.getRuntime().exec(arrayOf("su", "-c", cmd)).waitFor() 
            }
        }

        setPointerSpeed(11)
        setTouchSensitivity(2.0f) // Max out at 2.0x sensitivity
    }

    private fun restoreRootTouchOptimizations() {
        val commands = listOf(
            "echo 5 > /sys/class/input/input*/poll_delay",
            "echo 0 > /proc/touchpanel/game_switch_enable",
            "echo 0 > /sys/touchpanel/touch_game_mode",
            
            "setprop view.touch_slop 8",
            "setprop windowsmgr.max_events_per_sec 275",
            "setprop persist.vendor.qti.input.touch_boost 0"
        )

        for (cmd in commands) {
            runCatching { 
                Runtime.getRuntime().exec(arrayOf("su", "-c", cmd)).waitFor() 
            }
        }

        setPointerSpeed(5)
        setTouchSensitivity(1.0f)
    }

    private fun applyNonRootTouchOptimizations() {
        setPointerSpeed(11)
    }

    fun setPointerSpeed(speed: Int) {
        try {
            val clampedSpeed = speed.coerceIn(0, 11)
            Settings.System.putInt(context.contentResolver, "pointer_speed", clampedSpeed)
        } catch (_: Exception) {}
    }

    fun setTouchSensitivity(sensitivity: Float) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val clampedSensitivity = sensitivity.coerceIn(0.5f, 2.0f)
                val intValue = (clampedSensitivity * 100).toInt()
                Settings.System.putInt(context.contentResolver, "touch_sensitivity", intValue)
            }
        } catch (_: Exception) {}
    }

    fun getCurrentPointerSpeed(): Int {
        return try {
            Settings.System.getInt(context.contentResolver, "pointer_speed", 5)
        } catch (_: Exception) { 5 }
    }

    fun enableHighFrequencyTouch(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.System.putInt(context.contentResolver, "touch_report_rate", 1000)
                true
            } else false
        } catch (_: Exception) { false }
    }

    fun disableHighFrequencyTouch(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.System.putInt(context.contentResolver, "touch_report_rate", 0)
                true
            } else false
        } catch (_: Exception) { false }
    }

    fun enableGameModeTouch(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.Global.putInt(context.contentResolver, "game_mode", 1)
                Settings.Global.putInt(context.contentResolver, "game_driver", 1)
                true
            } else false
        } catch (_: Exception) { false }
    }

    fun disableGameModeTouch(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.Global.putInt(context.contentResolver, "game_mode", 0)
                Settings.Global.putInt(context.contentResolver, "game_driver", 0)
                true
            } else false
        } catch (_: Exception) { false }
    }

    suspend fun enableGameMode(): Boolean = withContext(Dispatchers.IO) {
        enableTouchOptimizations()
        enableHighFrequencyTouch()
        enableGameModeTouch()
        true
    }

    suspend fun disableGameMode(): Boolean = withContext(Dispatchers.IO) {
        disableTouchOptimizations()
        disableHighFrequencyTouch()
        disableGameModeTouch()
        true
    }

    fun getTouchSamplingRate(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.System.getInt(context.contentResolver, "touch_report_rate", 0)
            } else 0
        } catch (_: Exception) { 0 }
    }
}