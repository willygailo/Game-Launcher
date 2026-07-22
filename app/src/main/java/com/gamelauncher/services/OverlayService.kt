// app/src/main/java/com/gamelauncher/services/OverlayService.kt
package com.gamelauncher.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.gamelauncher.core.FPSManager
import com.gamelauncher.core.GameLauncherApp
import com.gamelauncher.core.NetworkManager
import com.gamelauncher.core.PerformanceManager
import com.gamelauncher.ui.MainActivity
import com.gamelauncher.ui.components.ArcGauge
import com.gamelauncher.ui.theme.*
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
class OverlayService : Service(), SavedStateRegistryOwner, ViewModelStoreOwner {

    @Inject lateinit var fpsManager: FPSManager
    @Inject lateinit var performanceManager: PerformanceManager
    @Inject lateinit var networkManager: NetworkManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var overlayParams: WindowManager.LayoutParams? = null

    private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private var fpsVal by mutableStateOf(0f)
    private var hzVal by mutableStateOf(60)
    private var ramVal by mutableStateOf("0/0 MB")
    private var pingVal by mutableStateOf("--")
    private var cpuTempVal by mutableStateOf("--")
    private var batTempVal by mutableStateOf("--")

    private val batteryReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val temp = intent.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0)
                batTempVal = "${temp / 10f}°C"
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

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
            .setContentTitle("In-Game Performance Side Panel Active")
            .setContentText("Slide-in overlay active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(2, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                @Suppress("DEPRECATION")
                startForeground(2, notification)
            }
        } catch (_: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }

        showSidePanelOverlay()
        return START_NOT_STICKY
    }

    private fun showSidePanelOverlay() {
        if (composeView != null) return

        val params = WindowManager.LayoutParams(
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
        ).apply {
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
            x = 0
            y = 0
            screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        overlayParams = params

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)

            setContent {
                GameLauncherTheme {
                    SideDockOverlayContent(
                        fps = fpsVal,
                        hz = hzVal,
                        ram = ramVal,
                        ping = pingVal,
                        cpuTemp = cpuTempVal,
                        batTemp = batTempVal
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, overlayParams)

            val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val actualOrientation = resources.configuration.orientation
                    if (actualOrientation != Configuration.ORIENTATION_LANDSCAPE) {
                        Log.w("OverlayService", "Device not in landscape after overlay request (actual: $actualOrientation)")
                    }
                    composeView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                }
            }
            composeView?.viewTreeObserver?.addOnGlobalLayoutListener(listener)

            fpsManager.startTracking()
            startTelemetryLoop()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun startTelemetryLoop() {
        // FPS collector stays on Main since it receives updates from UI Choreographer
        serviceScope.launch {
            fpsManager.fps.collectLatest { currentFps ->
                fpsVal = currentFps
                hzVal = performanceManager.getCurrentRefreshRate().roundToInt()
            }
        }

        // Blocking ping, RAM memory info, and thermal sysfs file reading dispatched to Dispatchers.IO
        serviceScope.launch(Dispatchers.IO) {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            while (isActive) {
                activityManager.getMemoryInfo(memoryInfo)
                val availMb = memoryInfo.availMem / 1048576L
                val totalMb = memoryInfo.totalMem / 1048576L
                val usedMb = totalMb - availMb
                ramVal = "${usedMb}M / ${totalMb}M"

                var netPing = "--"
                try {
                    val proc = Runtime.getRuntime().exec(arrayOf("ping", "-c", "1", "-W", "1", "8.8.8.8"))
                    if (proc.waitFor(1000L, java.util.concurrent.TimeUnit.MILLISECONDS) && proc.exitValue() == 0) {
                        val out = proc.inputStream.bufferedReader().readText()
                        val m = "time=([0-9.]+) ms".toRegex().find(out)
                        if (m != null) netPing = "${m.groupValues[1]}ms"
                    }
                } catch (_: Exception) {}

                if (netPing == "--") {
                    netPing = "Q:${networkManager.networkSnapshot.value.qualityScore}"
                }
                pingVal = netPing

                try {
                    val cpuFile = java.io.File("/sys/class/thermal/thermal_zone0/temp")
                    if (cpuFile.exists()) {
                        val t = cpuFile.readText().trim().toFloat() / 1000f
                        cpuTempVal = "${String.format("%.1f", t)}°C"
                    }
                } catch (_: Exception) {}

                kotlinx.coroutines.delay(1500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
        fpsManager.stopTracking()
        serviceScope.cancel()

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()

        composeView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
            composeView = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "STOP_OVERLAY"
    }
}

@Composable
fun SideDockOverlayContent(
    fps: Float,
    hz: Int,
    ram: String,
    ping: String,
    cpuTemp: String,
    batTemp: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Edge Dock Handle Strip (Compact vertically stacked icons)
        Column(
            modifier = Modifier
                .width(48.dp)
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .background(DockBgDark.copy(alpha = 0.92f))
                .border(1.dp, PrimaryNeon.copy(alpha = 0.5f), RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = { isExpanded = !isExpanded; selectedTab = 0 }) {
                Icon(Icons.Default.Speed, contentDescription = "Performance", tint = if (isExpanded && selectedTab == 0) PrimaryNeon else TextSecondary)
            }
            IconButton(onClick = { isExpanded = !isExpanded; selectedTab = 1 }) {
                Icon(Icons.Default.NetworkCheck, contentDescription = "Network", tint = if (isExpanded && selectedTab == 1) SecondaryNeon else TextSecondary)
            }
            IconButton(onClick = { isExpanded = !isExpanded; selectedTab = 2 }) {
                Icon(Icons.Default.Tv, contentDescription = "Monitor", tint = if (isExpanded && selectedTab == 2) TertiaryAccent else TextSecondary)
            }
            IconButton(onClick = { isExpanded = !isExpanded; selectedTab = 3 }) {
                Icon(Icons.Default.Build, contentDescription = "Tweaks", tint = if (isExpanded && selectedTab == 3) SuccessGreen else TextSecondary)
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { isExpanded = !isExpanded }) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Expand Panel",
                    tint = PrimaryNeon
                )
            }
        }

        // Expanded Side Drawer Panel
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it })
        ) {
            Surface(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight(0.85f)
                    .padding(start = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DockCardBorder, RoundedCornerShape(16.dp)),
                color = DockSurfaceDark.copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "GAME DASHBOARD DOCK",
                        color = PrimaryNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    when (selectedTab) {
                        0 -> {
                            Text("PERFORMANCE TELEMETRY", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                ArcGauge(progress = (fps / 120f).coerceIn(0f, 1f), color = PrimaryNeon, label = "FPS", valueText = "${fps.roundToInt()}")
                                ArcGauge(progress = (hz / 144f).coerceIn(0f, 1f), color = SecondaryNeon, label = "DISPLAY", valueText = "${hz}Hz")
                            }
                        }
                        1 -> {
                            Text("NETWORK STATUS", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("PING LATENCY: $ping", color = PrimaryNeon, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        2 -> {
                            Text("SYSTEM MONITOR", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("RAM USAGE: $ram", color = TextSecondary, fontSize = 12.sp)
                            Text("CPU TEMP: $cpuTemp", color = TertiaryAccent, fontSize = 12.sp)
                            Text("BATTERY TEMP: $batTemp", color = SuccessGreen, fontSize = 12.sp)
                        }
                        else -> {
                            Text("QUICK SYSTEM TWEAKS", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("✓ Shizuku Service Bound", color = SuccessGreen, fontSize = 12.sp)
                            Text("✓ Thermal Override Active", color = PrimaryNeon, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
