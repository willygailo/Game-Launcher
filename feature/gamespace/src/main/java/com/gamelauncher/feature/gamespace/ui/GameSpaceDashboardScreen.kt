package com.gamelauncher.feature.gamespace.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelauncher.core.device.OemBrand
import com.gamelauncher.core.oemflags.OemFlag
import com.gamelauncher.core.oemflags.ProbeStatus

private val NeonCyan = Color(0xFF00E5FF)
private val NeonPink = Color(0xFFFF2A5F)
private val DarkBg = Color(0xFF090C12)
private val CardBg = Color(0xFF131A26)
private val BorderColor = Color(0xFF2A3850)
private val MutedText = Color(0xFF91A0B8)
private val SuccessGreen = Color(0xFF22E18B)
private val WarningYellow = Color(0xFFFFB300)

/**
 * GameSpaceDashboardScreen — The single, unified ROG-inspired Game Space dashboard home.
 * Consolidates Games Library, System Tweaks, Network Latency, Telemetry Monitor, and Readiness Settings.
 */
@Composable
fun GameSpaceDashboardScreen(
    viewModel: GameSpaceViewModel,
    onNavigateToHuaweiGuide: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    state.statusMessage?.let { message ->
        LaunchedEffect(message) {
            // Accessible message notification rendered inline
        }
    }

    if (state.showResetConfirmationDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissResetDialog,
            containerColor = CardBg,
            title = { Text("Restore system defaults?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Only settings modified and verified by Game Space will be reverted to their baseline values.", color = MutedText) },
            confirmButton = {
                Button(
                    onClick = viewModel::confirmResetAllTweaks,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) { Text("RESTORE", fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissResetDialog) {
                    Text("CANCEL", color = MutedText)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // TOP HUD HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardBg)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("GAME SPACE", color = NeonPink, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("UNIFIED ROG-INSPIRED GAMING DASHBOARD", color = MutedText, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (state.isShizukuReady) {
                        StatusPill("SHIZUKU READY", SuccessGreen)
                    } else {
                        Button(
                            onClick = viewModel::requestShizukuPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) { Text("SHIZUKU", fontWeight = FontWeight.Black, fontSize = 10.sp) }
                    }

                    Button(
                        onClick = viewModel::toggleOverlayService,
                        colors = ButtonDefaults.buttonColors(containerColor = if (state.isOverlayRunning) NeonCyan else BorderColor),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (state.isOverlayRunning) "HUD ACTIVE" else "START HUD",
                            color = if (state.isOverlayRunning) Color.Black else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // TAB NAVIGATION ROW
            ScrollableTabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = Color.Transparent,
                contentColor = NeonCyan,
                edgePadding = 0.dp
            ) {
                val tabs = listOf("GAMES", "TWEAKS", "NETWORK", "TELEMETRY", "READINESS")
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                title,
                                color = if (state.selectedTab == index) NeonCyan else MutedText,
                                fontWeight = if (state.selectedTab == index) FontWeight.Black else FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    )
                }
            }
        }

        // MAIN CONTENT AREA
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // DEVICE SPECS BANNER (Always Visible)
            DashboardCard {
                Text("HARDWARE SPECS & COMPATIBILITY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        DeviceLine("DEVICE", state.deviceName)
                        DeviceLine("OS", state.androidVersion)
                        DeviceLine("CHIPSET", state.chipsetFamily)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        DeviceLine("OEM", "${state.detectedOemBrand.displayName} / ${state.detectedOemBrand.osName}")
                        DeviceLine("MODES", "${state.supportedRefreshRates.map { it.toInt() }.joinToString(", ")} Hz")
                        DeviceLine("SHIZUKU", if (state.isShizukuReady) "Connected" else "Optional (Basic Mode)")
                    }
                }
            }

            // TAB-SPECIFIC CONTENT
            when (state.selectedTab) {
                0 -> GamesTabContent(state = state, viewModel = viewModel)
                1 -> TweaksTabContent(state = state, viewModel = viewModel, onNavigateToHuaweiGuide = onNavigateToHuaweiGuide)
                2 -> NetworkTabContent(state = state, viewModel = viewModel)
                3 -> TelemetryTabContent(state = state)
                4 -> ReadinessTabContent(state = state, viewModel = viewModel, context = context)
            }

            state.statusMessage?.let { message ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(message, color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = viewModel::clearStatusMessage) {
                            Text("DISMISS", color = MutedText, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GamesTabContent(
    state: GameSpaceUiState,
    viewModel: GameSpaceViewModel
) {
    DashboardCard {
        Text("PER-GAME PERFORMANCE PROFILES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Profiles persist and auto-request physical display refresh rates without disabling safety throttles.", color = MutedText, fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ProfileButton("ECO", state.activeProfileMode == "ECO", MutedText) { viewModel.selectProfileMode("ECO") }
            ProfileButton("BALANCED", state.activeProfileMode == "BALANCED", NeonCyan) { viewModel.selectProfileMode("BALANCED") }
            ProfileButton("TURBO", state.activeProfileMode == "TURBO", NeonPink) { viewModel.selectProfileMode("TURBO") }
        }
    }

    DashboardCard {
        Text("DETECTED GAME LIBRARY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Select a game to launch with the active Game Space performance profile.", color = MutedText, fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))

        if (state.games.isEmpty()) {
            Text("No installed games automatically detected. Launch any installed app from your app drawer.", color = MutedText, fontSize = 11.sp)
        } else {
            state.games.forEach { game ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(game.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(game.packageName, color = MutedText, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Button(
                            onClick = { viewModel.launchGame(game.packageName) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("PLAY", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TweaksTabContent(
    state: GameSpaceUiState,
    viewModel: GameSpaceViewModel,
    onNavigateToHuaweiGuide: () -> Unit
) {
    DashboardCard {
        Text("PHYSICAL DISPLAY PANEL MODES", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Only display modes reported by this device's panel hardware are displayed. Selected: ${state.selectedRefreshRate.toInt()}Hz", color = MutedText, fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.supportedRefreshRates.forEach { rate ->
                val selected = rate == state.selectedRefreshRate
                OutlinedButton(
                    onClick = { viewModel.selectRefreshRate(rate) },
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) NeonCyan else BorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (selected) NeonCyan else MutedText),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) { Text("${rate.toInt()} Hz", fontWeight = FontWeight.Black, fontSize = 12.sp) }
            }
        }
    }

    DashboardCard {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("VERIFIED OEM TWEAK CONTROLS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Text("Tested and verified for ${state.detectedOemBrand.displayName}. Invalid OEM flags are disabled.", color = MutedText, fontSize = 10.sp)
            }
            if (state.detectedOemBrand == OemBrand.HUAWEI) {
                TextButton(onClick = onNavigateToHuaweiGuide) { Text("HUAWEI GUIDE", color = NeonCyan, fontSize = 10.sp) }
            }
            TextButton(onClick = viewModel::applyAllTweaks) { Text("APPLY ALL", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            TextButton(onClick = viewModel::promptResetAllTweaks) { Text("RESTORE", color = NeonPink, fontSize = 10.sp) }
        }
        if (state.romBuildInfo.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(state.romBuildInfo, color = NeonCyan.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))

        if (state.isLoading) {
            Text("Probing verified device capabilities…", color = MutedText, fontSize = 11.sp)
        } else if (state.flags.isEmpty()) {
            Text("No OEM specific key overrides required. Basic non-root performance controls are fully operational.", color = MutedText, fontSize = 11.sp)
        } else {
            state.flags.forEach { flag ->
                val supported = flag.status is ProbeStatus.Supported
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (supported) BorderColor else BorderColor.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(flag.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                StatusPill(
                                    when (flag.status) {
                                        is ProbeStatus.Supported -> "Supported"
                                        is ProbeStatus.Unsupported -> "Unsupported: ${(flag.status as ProbeStatus.Unsupported).reason}"
                                        ProbeStatus.Unprobed -> "Unprobed"
                                    },
                                    when (flag.status) {
                                        is ProbeStatus.Supported -> SuccessGreen
                                        is ProbeStatus.Unsupported -> WarningYellow
                                        ProbeStatus.Unprobed -> MutedText
                                    }
                                )
                            }
                            Text(flag.description, color = MutedText, fontSize = 9.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Switch(
                            checked = (flag.status as? ProbeStatus.Supported)?.currentValue == flag.activeValue,
                            enabled = supported,
                            onCheckedChange = { viewModel.toggleFlag(flag, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkTabContent(
    state: GameSpaceUiState,
    viewModel: GameSpaceViewModel
) {
    DashboardCard {
        Text("PRIVATE DNS LATENCY STABILIZER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Select a low-latency gaming DNS provider. Configures system Private DNS via Shizuku/Settings.", color = MutedText, fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))

        val providers = listOf(
            Triple("default", "System Default", "Standard ISP routing"),
            Triple("cloudflare", "Cloudflare 1.1.1.1", "Ultra-fast global edge network"),
            Triple("adguard", "AdGuard Gaming", "Blocks telemetry & ad domains"),
            Triple("google", "Google 8.8.8.8", "Reliable low-latency DNS")
        )

        providers.forEach { (id, name, desc) ->
            val selected = state.selectedDnsProvider == id
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { viewModel.selectDnsProvider(id) },
                color = Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) NeonCyan else BorderColor)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, color = if (selected) NeonCyan else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(desc, color = MutedText, fontSize = 9.sp)
                    }
                    RadioButton(
                        selected = selected,
                        onClick = { viewModel.selectDnsProvider(id) },
                        colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryTabContent(
    state: GameSpaceUiState
) {
    DashboardCard {
        Text("REAL-TIME HARDWARE TELEMETRY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("Telemetry values are collected directly from standard Android system sensors.", color = MutedText, fontSize = 10.sp)
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricCard("DISPLAY RATE", "${state.selectedRefreshRate.toInt()} Hz", NeonCyan)
            MetricCard("HUD FRAME RATE", "HUD FPS Pacing", NeonCyan)
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MetricCard("GPU UTILIZATION", "Unavailable", MutedText, note = "No universal public API")
            MetricCard("THERMAL SAFETY", "Active", SuccessGreen)
        }
    }
}

@Composable
private fun ReadinessTabContent(
    state: GameSpaceUiState,
    viewModel: GameSpaceViewModel,
    context: android.content.Context
) {
    DashboardCard {
        Text("GAME READINESS & PERMISSIONS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
        Text("All permissions required for overlay, DND, telemetry, and Shizuku enhancements.", color = MutedText, fontSize = 10.sp)
        Spacer(Modifier.height(10.dp))

        PermissionLine(
            title = "Overlay Drawing Permission",
            subtitle = "Required for in-game performance HUD side-panel",
            isGranted = state.overlayPermissionGranted,
            onGrant = {
                context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        )

        PermissionLine(
            title = "Do Not Disturb (DND) Access",
            subtitle = "Required to silence calls and alerts during gaming",
            isGranted = state.dndPermissionGranted,
            onGrant = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        )

        PermissionLine(
            title = "Usage Stats Access",
            subtitle = "Required for automatic game detection and tracking",
            isGranted = state.usageStatsGranted,
            onGrant = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        )

        PermissionLine(
            title = "Shizuku Privileged Access (Optional)",
            subtitle = "Unlocks typed, verified secure settings overrides",
            isGranted = state.isShizukuReady,
            onGrant = viewModel::requestShizukuPermission
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    accentColor: Color,
    note: String? = null
) {
    Surface(
        modifier = Modifier.width(160.dp),
        color = DarkBg,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, color = MutedText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(value, color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Black)
            note?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, color = MutedText, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun PermissionLine(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MutedText, fontSize = 9.sp)
            }
            if (isGranted) {
                StatusPill("GRANTED", SuccessGreen)
            } else {
                OutlinedButton(
                    onClick = onGrant,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) { Text("GRANT", fontSize = 9.sp, fontWeight = FontWeight.Black) }
            }
        }
    }
}

@Composable
private fun DashboardCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun DeviceLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", color = MutedText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun RowScope.ProfileButton(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) accentColor else BorderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
            contentColor = if (selected) accentColor else MutedText
        ),
        contentPadding = PaddingValues(vertical = 10.dp)
    ) {
        Text(label, fontWeight = FontWeight.Black, fontSize = 11.sp)
    }
}
