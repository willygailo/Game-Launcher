package com.gamelauncher.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.gamelauncher.core.FPSManager
import com.gamelauncher.core.GameLauncherApp
import com.gamelauncher.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class OverlayService : Service() {

    @Inject lateinit var fpsManager: FPSManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
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

        startForeground(2, notification)

        showOverlay()

        return START_NOT_STICKY
    }

    private fun showOverlay() {
        if (overlayView != null) return

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 100
        layoutParams.y = 100

        overlayView = TextView(this).apply {
            text = "FPS: --"
            setTextColor(Color.GREEN)
            setBackgroundColor(Color.parseColor("#80000000"))
            setPadding(16, 8, 16, 8)
            textSize = 14f
        }

        try {
            windowManager.addView(overlayView, layoutParams)
            fpsManager.startTracking()

            serviceScope.launch {
                fpsManager.fps.collect { currentFps ->
                    (overlayView as? TextView)?.text = "FPS: ${currentFps.roundToInt()}"
                    
                    val color = when {
                        currentFps >= 55 -> Color.GREEN
                        currentFps >= 30 -> Color.YELLOW
                        else -> Color.RED
                    }
                    (overlayView as? TextView)?.setTextColor(color)
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
