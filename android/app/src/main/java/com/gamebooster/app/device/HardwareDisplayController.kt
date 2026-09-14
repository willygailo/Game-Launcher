package com.gamebooster.app.device

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
import android.view.Surface
import com.gamebooster.app.shizuku.ShizukuExecutor

/**
 * HardwareDisplayController — Handles display modes enumeration and forces hardware refresh rates (60Hz -> 165Hz+).
 *
 * Implements:
 *  - DisplayManager.getSupportedModes() enumeration
 *  - Activity preferredDisplayModeId injection
 *  - Surface.setFrameRate() and Surface.setFrameRateCategory()
 *  - Shizuku privileged settings overrides: peak_refresh_rate, min_refresh_rate, user_refresh_rate
 */
object HardwareDisplayController {

    private const val TAG = "HardwareDisplayCtrl"

    /**
     * Finds the maximum physical refresh rate supported by the default display hardware.
     */
    @JvmStatic
    fun getMaxHardwareRefreshRate(context: Context?): Float {
        if (context == null) return 60f
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return 60f
        val display = dm.getDisplay(Display.DEFAULT_DISPLAY) ?: return 60f
        val modes = display.supportedModes ?: return 60f

        var maxRate = 60f
        for (mode in modes) {
            if (mode.refreshRate > maxRate) {
                maxRate = mode.refreshRate
            }
        }
        return maxRate
    }

    /**
     * Returns the Display.Mode with the highest refresh rate.
     */
    @JvmStatic
    fun getMaxHardwareMode(context: Context?): Display.Mode? {
        if (context == null) return null
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return null
        val display = dm.getDisplay(Display.DEFAULT_DISPLAY) ?: return null
        val modes = display.supportedModes ?: return null

        var maxMode: Display.Mode? = null
        for (mode in modes) {
            if (maxMode == null || mode.refreshRate > maxMode.refreshRate) {
                maxMode = mode
            }
        }
        return maxMode
    }

    /**
     * Injects the preferred display mode into the window attributes to enforce max Hz on this Activity.
     */
    @JvmStatic
    fun applyMaxRefreshRateToWindow(activity: Activity) {
        try {
            val maxMode = getMaxHardwareMode(activity) ?: return
            val params = activity.window.attributes
            params.preferredDisplayModeId = maxMode.modeId
            activity.window.attributes = params
            Log.i(TAG, "Window preferred display mode set to modeId=${maxMode.modeId} (${maxMode.refreshRate}Hz)")
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to apply preferred display mode to window: ${t.message}")
        }
    }

    /**
     * Applies frame rate hint directly to a Surface instance (Android 13+).
     */
    @JvmStatic
    fun applySurfaceFrameRate(surface: Surface, targetFps: Float) {
        if (Build.VERSION.SDK_INT >= 33) {
            try {
                surface.setFrameRate(
                    targetFps,
                    Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE,
                    Surface.CHANGE_FRAME_RATE_ALWAYS
                )
                Log.d(TAG, "Surface frame rate hint applied: ${targetFps}fps")
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to set Surface frame rate: ${t.message}")
            }
        }

        if (Build.VERSION.SDK_INT >= 34) {
            try {
                // API 34 FRAME_RATE_CATEGORY_HIGH = 3
                val method = surface.javaClass.getMethod("setFrameRateCategory", Int::class.javaPrimitiveType)
                method.invoke(surface, 3)
                Log.d(TAG, "Surface FRAME_RATE_CATEGORY_HIGH applied via reflection")
            } catch (t: Throwable) {
                Log.d(TAG, "setFrameRateCategory not available on this platform")
            }
        }
    }

    /**
     * Force-locks the system display refresh rate to the hardware maximum using Shizuku privileged shell.
     * Prevents OEM adaptive refresh stepping down to 60Hz during gaming.
     */
    @JvmStatic
    fun forceUnlockSystemMaxRefreshRate(context: Context): Boolean {
        val maxHz = Math.round(getMaxHardwareRefreshRate(context))
        Log.i(TAG, "Hardware ceiling detected: ${maxHz}Hz. Pushing system overrides via Shizuku...")

        val commands = listOf(
            "settings put system peak_refresh_rate $maxHz",
            "settings put system min_refresh_rate $maxHz",
            "settings put system user_refresh_rate $maxHz",
            "settings put global low_power 0",
            "settings put global low_power_sticky 0",
            "settings put global adaptive_battery_management_enabled 0"
        )

        var allSuccess = true
        for (cmd in commands) {
            val res = ShizukuExecutor.executeShizukuCommand(cmd)
            if (res == null || res.startsWith("ERROR")) {
                allSuccess = false
                Log.w(TAG, "Command warning: $cmd ($res)")
            }
        }
        return allSuccess
    }
}
