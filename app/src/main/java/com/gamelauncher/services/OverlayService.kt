package com.gamelauncher.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.gamelauncher.core.FPSManager
import com.gamelauncher.core.GameLauncherApp
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class OverlayService : Service() {

    @Inject lateinit var fpsManager: FPSManager
    @Inject lateinit var performanceManager: PerformanceManager
    @Inject lateinit var networkManager: NetworkManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var initialX = 100
    private var initialY = 100
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var fpsText: TextView? = null
    private var hzText: TextView? = null
    private var ramText: TextView? = null
    private var pingText: TextView? = null
    private var cpuTempText: TextView? = null
    private var batTempText: TextView? = null

    private var currentBatteryTemp: Float = 0f

    private val batteryReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val temp = intent.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0)
                currentBatteryTemp = temp / 10f
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        registerReceiver(batteryReceiver, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val hasOverlayPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)
        if (!hasOverlayPermission) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, GameLauncherApp.CHANNEL_OVERLAY)
            .setContentTitle("FPS Overlay Active")
            .setContentText("Displaying frame rate")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(2, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                @Suppress("DEPRECATION")
                startForeground(2, notification)
            }
        } catch (e: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }

        showOverlay()

        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return

        overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )

        overlayParams!!.gravity = Gravity.TOP or Gravity.START
        overlayParams!!.x = initialX
        overlayParams!!.y = initialY

        overlayView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#80000000"))
            setPadding(12, 8, 12, 8)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            fpsText = TextView(context).apply {
                text = "FPS: --"
                setTextColor(Color.GREEN)
                textSize = 16f
                setPadding(4, 2, 4, 2)
            }.also { addView(it) }

            hzText = TextView(context).apply {
                text = "Hz: --"
                setTextColor(Color.CYAN)
                textSize = 11f
                setPadding(4, 0, 4, 2)
            }.also { addView(it) }

            ramText = TextView(context).apply {
                text = "RAM: --"
                setTextColor(Color.parseColor("#FFA500"))
                textSize = 10f
                setPadding(4, 0, 4, 2)
            }.also { addView(it) }

            pingText = TextView(context).apply {
                text = "Net: --"
                setTextColor(Color.parseColor("#00BFFF")) // Deep sky blue
                textSize = 10f
                setPadding(4, 0, 4, 2)
            }.also { addView(it) }

            cpuTempText = TextView(context).apply {
                text = "CPU: --°C"
                setTextColor(Color.parseColor("#FF4500")) // OrangeRed
                textSize = 10f
                setPadding(4, 0, 4, 2)
            }.also { addView(it) }

            batTempText = TextView(context).apply {
                text = "BAT: --°C"
                setTextColor(Color.parseColor("#32CD32")) // LimeGreen
                textSize = 10f
                setPadding(4, 0, 4, 2)
            }.also { addView(it) }

            setOnTouchListener { _, event ->
                val params = overlayParams ?: return@setOnTouchListener false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = (initialX + (event.rawX - initialTouchX)).toInt()
                        params.y = (initialY + (event.rawY - initialTouchY)).toInt()
                        windowManager.updateViewLayout(this, params)
                        true
                    }
                    else -> false
                }
            }
        }

        try {
            windowManager.addView(overlayView, overlayParams)
            fpsManager.startTracking()

            serviceScope.launch {
                fpsManager.fps.collectLatest { currentFps ->
                    val currentHz = performanceManager.getCurrentRefreshRate().roundToInt()
                    fpsText?.text = "FPS: ${currentFps.roundToInt()}"
                    hzText?.text = "${currentHz}Hz"
                    
                    val color = when {
                        currentFps >= 55 -> Color.GREEN
                        currentFps >= 30 -> Color.YELLOW
                        else -> Color.RED
                    }
                    fpsText?.setTextColor(color)
                }
            }

            serviceScope.launch {
                val activityManager = getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                val memoryInfo = android.app.ActivityManager.MemoryInfo()
                while (isActive) {
                    activityManager.getMemoryInfo(memoryInfo)
                    val availableMegs = memoryInfo.availMem / 1048576L
                    val totalMegs = memoryInfo.totalMem / 1048576L
                    val usedMegs = totalMegs - availableMegs
                    ramText?.text = "RAM: ${usedMegs}MB / ${totalMegs}MB"

                    // Try to get actual ping via shell, fallback to quality score
                    var pingResult = "--"
                    try {
                        val process = Runtime.getRuntime().exec("ping -c 1 -W 1 8.8.8.8")
                        process.waitFor()
                        if (process.exitValue() == 0) {
                            val output = process.inputStream.bufferedReader().readText()
                            val match = "time=([0-9.]+) ms".toRegex().find(output)
                            if (match != null) {
                                pingResult = "${match.groupValues[1]}ms"
                            }
                        }
                    } catch (e: Exception) {}
                    
                    if (pingResult == "--") {
                        val netSnap = networkManager.networkSnapshot.value
                        val quality = netSnap.qualityScore
                        pingResult = "Q:$quality"
                    }
                    pingText?.text = "Net: $pingResult"
                    
                    // CPU Temp
                    var cpuTemp = 0f
                    try {
                        // Commonly zone 0 is CPU, but this varies heavily.
                        val cpuFile = java.io.File("/sys/class/thermal/thermal_zone0/temp")
                        if (cpuFile.exists()) {
                            val tempStr = cpuFile.readText().trim()
                            cpuTemp = tempStr.toFloat() / 1000f
                        }
                    } catch (e: Exception) {}
                    
                    if (cpuTemp > 0) {
                        cpuTempText?.text = "CPU: ${String.format("%.1f", cpuTemp)}°C"
                    } else {
                        cpuTempText?.text = "CPU: --°C"
                    }

                    // Battery Temp
                    if (currentBatteryTemp > 0) {
                        batTempText?.text = "BAT: ${currentBatteryTemp}°C"
                    } else {
                        batTempText?.text = "BAT: --°C"
                    }

                    kotlinx.coroutines.delay(2000)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {}
        fpsManager.stopTracking()
        serviceScope.cancel()
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "STOP_OVERLAY"
    }
}
