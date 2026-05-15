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
import com.gamelauncher.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class OverlayService : Service() {

    @Inject lateinit var fpsManager: FPSManager
    @Inject lateinit var performanceManager: PerformanceManager

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

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
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
            startForeground(2, notification)
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
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
