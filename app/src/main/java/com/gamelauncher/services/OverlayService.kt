package com.gamelauncher.services

import android.annotation.SuppressLint
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
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        if (intent?.action == ACTION_STOP || intent?.action == ACTION_STOP_OVERLAY) {
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
            .setContentText("Draggable floating overlay active")
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

    @SuppressLint("ClickableViewAccessibility")
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
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 150
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
                        batTemp = batTempVal,
                        onCleanRam = { performanceManager.clearMemory() },
                        onCloseOverlay = { stopSelf() }
                    )
                }
            }
        }

        // Add touch dragging listener so the floating overlay can be moved anywhere on screen!
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        composeView?.setOnTouchListener { _, event ->
            val p = overlayParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = p.x
                    initialY = p.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).roundToInt()
                    val deltaY = (event.rawY - initialTouchY).roundToInt()
                    if (kotlin.math.abs(deltaX) > 4 || kotlin.math.abs(deltaY) > 4) {
                        p.x = initialX + deltaX
                        p.y = initialY + deltaY
                        try {
                            windowManager.updateViewLayout(composeView, p)
                        } catch (_: Exception) {}
                    }
                    false
                }
                else -> false
            }
        }

        try {
            windowManager.addView(composeView, overlayParams)

            fpsManager.startTracking()
            startTelemetryLoop()
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun startTelemetryLoop() {
        serviceScope.launch {
            fpsManager.fps.collectLatest { currentFps ->
                fpsVal = currentFps
                hzVal = performanceManager.getCurrentRefreshRate().roundToInt()
            }
        }

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
        const val ACTION_STOP_OVERLAY = "com.gamelauncher.ACTION_STOP_OVERLAY"
    }
}

@Composable
fun SideDockOverlayContent(
    fps: Float,
    hz: Int,
    ram: String,
    ping: String,
    cpuTemp: String,
    batTemp: String,
    onCleanRam: () -> Unit = {},
    onSetMode: (String) -> Unit = {},
    onCloseOverlay: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var crosshairEnabled by remember { mutableStateOf(false) }
    var ramFreedText by remember { mutableStateOf<String?>(null) }
    var activeMode by remember { mutableStateOf("TURBO") }

    Box(modifier = Modifier.wrapContentSize()) {
        if (!isExpanded) {
            // COMPACT DRAGGABLE FLOATING BUBBLE (Default collapsed mode - small & non-blocking!)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { isExpanded = true },
                color = DockBgDark.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryNeon)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Drag overlay",
                        tint = PrimaryNeon,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "${fps.roundToInt()} FPS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "| ${hz}Hz",
                        color = PrimaryNeon,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // EXPANDED HUD SIDE DRAWER
            Row(
                modifier = Modifier.wrapContentSize(),
                verticalAlignment = Alignment.Top
            ) {
                // Collapsed edge action strip
                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                        .background(DockBgDark.copy(alpha = 0.95f))
                        .border(1.5.dp, PrimaryNeon, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(onClick = { selectedTab = 0 }) {
                        Icon(Icons.Default.Speed, contentDescription = "Performance", tint = if (selectedTab == 0) PrimaryNeon else TextSecondary)
                    }
                    IconButton(onClick = { selectedTab = 1 }) {
                        Icon(Icons.Default.NetworkCheck, contentDescription = "Network", tint = if (selectedTab == 1) SecondaryNeon else TextSecondary)
                    }
                    IconButton(onClick = { selectedTab = 2 }) {
                        Icon(Icons.Default.Tv, contentDescription = "Monitor", tint = if (selectedTab == 2) TertiaryAccent else TextSecondary)
                    }
                    IconButton(onClick = { selectedTab = 3 }) {
                        Icon(Icons.Default.Build, contentDescription = "Tweaks", tint = if (selectedTab == 3) SuccessGreen else TextSecondary)
                    }
                }

                // Expanded content container
                Surface(
                    modifier = Modifier
                        .width(280.dp)
                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                        .border(1.5.dp, DockCardBorder, RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)),
                    color = DockSurfaceDark.copy(alpha = 0.96f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // HEADER WITH HIDE AND CLOSE BUTTONS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "GAME SPACE HUD",
                                color = PrimaryNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = { isExpanded = false },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("HIDE", color = PrimaryNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = onCloseOverlay, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Close HUD", tint = ErrorRed)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Boost Button
                        Button(
                            onClick = {
                                onCleanRam()
                                ramFreedText = "PURGED RAM"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(34.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("⚡ 1-TAP RAM BOOST", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }

                        ramFreedText?.let { freedMsg ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(freedMsg, color = SuccessGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        when (selectedTab) {
                            0 -> {
                                Text("PERFORMANCE PRESETS", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("ECO", "BALANCED", "TURBO").forEach { mode ->
                                        val isSel = activeMode == mode
                                        val mColor = when(mode) {
                                            "TURBO" -> PrimaryNeon
                                            "BALANCED" -> SecondaryNeon
                                            else -> SuccessGreen
                                        }
                                        Button(
                                            onClick = { activeMode = mode; onSetMode(mode) },
                                            modifier = Modifier.weight(1f).height(30.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSel) mColor.copy(alpha = 0.25f) else Color.Transparent
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) mColor else TextSecondary.copy(alpha = 0.3f)),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(mode, color = if (isSel) mColor else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    ArcGauge(progress = (fps / 120f).coerceIn(0f, 1f), color = PrimaryNeon, label = "HUD FPS", valueText = "${fps.roundToInt()}")
                                    ArcGauge(progress = (hz / 144f).coerceIn(0f, 1f), color = SecondaryNeon, label = "DISPLAY", valueText = "${hz}Hz")
                                }
                            }
                            1 -> {
                                Text("NETWORK LATENCY MONITOR", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("PING: $ping", color = PrimaryNeon, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Sampled live while in-game.", color = TextSecondary, fontSize = 10.sp)
                            }
                            2 -> {
                                Text("SYSTEM TELEMETRY", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("RAM: $ram", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("CPU TEMP: $cpuTemp", color = TertiaryAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("BATTERY TEMP: $batTemp", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            else -> {
                                Text("TACTICAL CONTROLS", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Crosshair Overlay", color = TextSecondary, fontSize = 11.sp)
                                    Switch(
                                        checked = crosshairEnabled,
                                        onCheckedChange = { crosshairEnabled = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryNeon, checkedTrackColor = PrimaryNeon.copy(alpha = 0.4f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Crosshair Overlay (if enabled)
        if (crosshairEnabled) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
                    .background(PrimaryNeon.copy(alpha = 0.8f), androidx.compose.foundation.shape.CircleShape)
                    .border(1.dp, Color.White, androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}
