package com.gamelauncher.core

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.PointerIcon
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
    private var originalPointerSpeed: Int = 5

    fun isTouchLatencyReductionSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    suspend fun enableTouchOptimizations(): Boolean = withContext(Dispatchers.IO) {
        touchBoostEnabled = true
        originalPointerSpeed = getCurrentPointerSpeed()
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
        } else {
            setPointerSpeed(originalPointerSpeed)
            restoreNonRootTouchOptimizations()
        }
        true
    }

    private fun applyRootTouchOptimizations() {
        val commands = listOf(
            "echo 0 > /sys/class/input/input*/poll_delay",
            "echo 0 > /sys/touchpanel/tp_debug_mask",
            "echo 1 > /sys/class/input/input*/gesture",
            "echo Y > /sys/module/synaptics_dsx/parameters/pinctrl_state",
            "echo 0 > /sys/module/tpd_setting/parameters/tpd_em_debounce_time",
            "echo 0 > /sys/module/tpd_setting/parameters/tpd_em_debounce_time0",
            "echo 0 > /sys/module/tpd_setting/parameters/tpd_em_debounce_time1",
            "echo 1 > /proc/touchpanel/game_switch_enable",
            "echo 1 > /sys/class/touch/switch/set_touchscreen",
            "echo 0 > /sys/class/touchscreen/primary/edge_filter",
            "echo 1 > /sys/touchpanel/touch_game_mode",
            "echo 1 > /proc/touchpanel/oplus_tp_limit_enable",
            "echo 0 > /proc/touchpanel/oppo_tp_limit_enable",
            "echo 100 > /sys/class/touchscreen/primary/report_rate",
            "echo 4 > /sys/class/touchscreen/primary/anti_shake_level",
            "echo 1 > /sys/module/msm_drm/parameters/psr_enable",
            // High performance touch GPU feedback
            "echo 1 > /sys/class/touchscreen/primary/touch_boost",
            "echo 1 > /sys/class/touchscreen/primary/quick_input",
            "echo 1 > /proc/touchpanel/game_switch_enable",
            "echo 0 > /proc/touchpanel/oppo_tp_direction",
            "echo 1 > /proc/touchpanel/oplus_tp_gesture",
            "echo 1 > /sys/touchpanel/double_tap",
            "echo 0 > /sys/touchpanel/edge_touch",
            "echo 0 > /sys/touchpanel/oppo_tp_limit_enable",
            // GPU touch boost (Adreno specific)
            "echo 1 > /sys/class/kgsl/kgsl-3d0/touch_boost",
            "echo 100 > /sys/class/kgsl/kgsl-3d0/max_pwrlevel",
            // Stylus and high polling rate
            "echo 1 > /sys/module/usbtouchscreen/parameters/touch_boost",
            "echo 1 > /sys/module/sy700/parameters/touch_boost",
            "setprop view.touch_slop 1",
            "setprop view.scroll_friction 10",
            "setprop windowsmgr.max_events_per_sec 2000",
            "setprop ro.max.fling_velocity 20000",
            "setprop ro.min.fling_velocity 8000",
            "setprop persist.vendor.qti.input.touch_boost 1",
            "setprop ro.vendor.touch.sens.supp true",
            "setprop sys.use_fifo_ui 1",
            "setprop debug.hwui.render_thread true",
            "setprop debug.sf.latch_unsignaled 1",
            "setprop vendor.display.input_boost 1",
            "setprop persist.sys.touch.xp 1",
            // Extra touch input boost
            "setprop vendor.perf.input_boost.enable 1",
            "setprop vendor.perf.input_boost.duration 1500",
            "setprop vendor.perf.input_boost.freq 2000000",
            "setprop persist.sys.touch.optimization 1",
            "setprop sys.touch.boost 1",
            "setprop persist.sys.input.latency 0",
            "setprop touch.device.type touchscreen",
            "setprop touch.pressure.scale 0.001",
            "setprop ro.input.noautomap 1",
            "setprop view.minimum_fling_velocity 5"
        )
        for (cmd in commands) {
            runCatching { Runtime.getRuntime().exec(arrayOf("su", "-c", cmd)).waitFor() }
        }
        setPointerSpeed(11)
        setTouchSensitivity(2.0f)
    }

    private fun restoreRootTouchOptimizations() {
        val commands = listOf(
            "echo 5 > /sys/class/input/input*/poll_delay",
            "echo 0 > /proc/touchpanel/game_switch_enable",
            "echo 0 > /sys/touchpanel/touch_game_mode",
            "setprop view.touch_slop 8",
            "setprop windowsmgr.max_events_per_sec 275",
            "setprop persist.vendor.qti.input.touch_boost 0",
            "setprop vendor.display.input_boost 0",
            "setprop persist.sys.touch.xp 0"
        )
        for (cmd in commands) {
            runCatching { Runtime.getRuntime().exec(arrayOf("su", "-c", cmd)).waitFor() }
        }
        setPointerSpeed(originalPointerSpeed)
        setTouchSensitivity(1.0f)
    }

    // Saved non-root state for restoration
    private var savedTouchSensitivity: Int = -1
    private var savedTouchReportRate: Int = -1
    private var savedGameMode: Int = -1
    private var savedScrollFriction: Float = -1f

    private fun applyNonRootTouchOptimizations() {
        val cr = context.contentResolver
        // Save originals
        savedTouchSensitivity = runCatching {
            Settings.System.getInt(cr, "touch_sensitivity", 100)
        }.getOrDefault(-1)
        savedTouchReportRate = runCatching {
            Settings.System.getInt(cr, "touch_report_rate", 0)
        }.getOrDefault(-1)
        savedGameMode = runCatching {
            Settings.Global.getInt(cr, "game_mode", 0)
        }.getOrDefault(-1)
        savedScrollFriction = runCatching {
            Settings.System.getFloat(cr, "scroll_friction", 0.015f)
        }.getOrDefault(-1f)

        // 1. Max pointer speed
        setPointerSpeed(11)

        // 2. Max touch sensitivity (200 = 2x)
        runCatching { Settings.System.putInt(cr, "touch_sensitivity", 200) }

        // 3. High-frequency touch report rate (1000Hz if supported)
        runCatching { Settings.System.putInt(cr, "touch_report_rate", 1000) }

        // 4. Minimize scroll friction for snappier scrolling
        runCatching { Settings.System.putFloat(cr, "scroll_friction", 0.005f) }

        // 5. Enable system game mode flag
        runCatching { Settings.Global.putInt(cr, "game_mode", 1) }

        // 6. Enable GPU game driver hint
        runCatching { Settings.Global.putInt(cr, "game_driver_all_apps", 1) }

        // 7. Input boost duration (milliseconds)
        runCatching { Settings.Global.putInt(cr, "input_boost_duration_ms", 1500) }

        // 8. Disable battery saver auto-restrictions on input
        runCatching { Settings.Global.putInt(cr, "low_power_standby_enabled", 0) }

        // 9. Maximize fling velocity for responsive lists
        runCatching { Settings.System.putInt(cr, "max_fling_velocity", 24000) }
        runCatching { Settings.System.putInt(cr, "min_fling_velocity", 80) }

        // 10. Set touch exploration (haptic feedback sharpness)
        runCatching { Settings.System.putInt(cr, "haptic_feedback_enabled", 1) }

        // 11. Minimize long-press timeout for snappier response
        runCatching { Settings.Secure.putInt(cr, "long_press_timeout", 300) }

        // 12. Touch zoom control - faster double-tap
        runCatching { Settings.Secure.putInt(cr, "multi_press_timeout", 150) }
    }

    private fun restoreNonRootTouchOptimizations() {
        val cr = context.contentResolver
        if (savedTouchSensitivity >= 0)
            runCatching { Settings.System.putInt(cr, "touch_sensitivity", savedTouchSensitivity) }
        if (savedTouchReportRate >= 0)
            runCatching { Settings.System.putInt(cr, "touch_report_rate", savedTouchReportRate) }
        if (savedGameMode >= 0)
            runCatching { Settings.Global.putInt(cr, "game_mode", savedGameMode) }
        if (savedScrollFriction >= 0f)
            runCatching { Settings.System.putFloat(cr, "scroll_friction", savedScrollFriction) }

        // Restore remaining defaults
        runCatching { Settings.Global.putInt(cr, "game_driver_all_apps", 0) }
        runCatching { Settings.Global.putInt(cr, "input_boost_duration_ms", 0) }
        runCatching { Settings.Global.putInt(cr, "low_power_standby_enabled", 1) }
        runCatching { Settings.System.putInt(cr, "max_fling_velocity", 8000) }
        runCatching { Settings.System.putInt(cr, "min_fling_velocity", 50) }
        runCatching { Settings.Secure.putInt(cr, "long_press_timeout", 400) }
        runCatching { Settings.Secure.putInt(cr, "multi_press_timeout", 300) }
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
                Settings.System.putInt(context.contentResolver, "touch_sensitivity", (clampedSensitivity * 100).toInt())
            }
        } catch (_: Exception) {}
    }

    fun enableGameModeTouch(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.Global.putInt(context.contentResolver, "game_mode", 1)
                Settings.Global.putInt(context.contentResolver, "game_driver", 1)
                Settings.Global.putInt(context.contentResolver, "game_driver_fps", 1)
                true
            } else false
        } catch (_: Exception) { false }
    }

    fun disableGameModeTouch(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.Global.putInt(context.contentResolver, "game_mode", 0)
                Settings.Global.putInt(context.contentResolver, "game_driver", 0)
                Settings.Global.putInt(context.contentResolver, "game_driver_fps", 0)
                true
            } else false
        } catch (_: Exception) { false }
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


}
