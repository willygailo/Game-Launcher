package com.gamelauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gamelauncher.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val autoBoost by viewModel.globalAutoBoost.collectAsState()
    val overlayEnabled by viewModel.isOverlayEnabled.collectAsState()
    val detectorEnabled by viewModel.isGameDetectorEnabled.collectAsState()
    val hasUsageAccess by viewModel.hasUsageAccessPermission.collectAsState()
    val hasOverlayPerm by viewModel.hasOverlayPermission.collectAsState()
    val hasWriteSettings by viewModel.hasWriteSettingsPermission.collectAsState()
    val hasNotificationPerm by viewModel.hasNotificationPermission.collectAsState()
    val hasBatteryExempt by viewModel.hasBatteryExemption.collectAsState()
    val hasWriteSecure by viewModel.hasWriteSecureSettings.collectAsState()
    val hasPhoneState by viewModel.hasPhoneStatePermission.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Secure toggles
    val secureAnimScale by viewModel.secureAnimScale.collectAsState()
    val secureGameDriver by viewModel.secureGameDriver.collectAsState()
    val secureSyncOff by viewModel.secureSyncOff.collectAsState()
    val secureMobileData by viewModel.secureMobileData.collectAsState()
    val secureBatterySaver by viewModel.secureBatterySaver.collectAsState()
    val secureLocationOff by viewModel.secureLocationOff.collectAsState()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Compatibility Mode: Android 7+ | Works on Snapdragon, MediaTek, Exynos, Kirin, Tensor, and Unisoc",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        // ── Standard Permissions ──────────────────────────────
        PermissionCard(
            title = "System Settings Access",
            subtitle = "Needed for brightness and animation controls",
            buttonText = if (hasWriteSettings) "Granted ✓" else "Grant Write Settings",
            granted = hasWriteSettings,
            onClick = viewModel::requestWriteSettingsPermission
        )

        PermissionCard(
            title = "Overlay Permission",
            subtitle = "Required for floating FPS counter",
            buttonText = if (hasOverlayPerm) "Granted ✓" else "Grant Overlay",
            granted = hasOverlayPerm,
            onClick = viewModel::requestOverlayPermission
        )

        PermissionCard(
            title = "Usage Access",
            subtitle = "Required for auto game detection while gaming",
            buttonText = if (hasUsageAccess) "Granted ✓" else "Grant Usage Access",
            granted = hasUsageAccess,
            onClick = viewModel::requestUsageAccessPermission
        )

        PermissionCard(
            title = "Battery Optimization",
            subtitle = "Keeps booster services alive in long sessions",
            buttonText = if (hasBatteryExempt) "Granted ✓" else "Ignore Battery Optimization",
            granted = hasBatteryExempt,
            onClick = viewModel::requestBatteryOptimizationExemption
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val notifLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* result handled by state polling */ }

            PermissionCard(
                title = "Notifications",
                subtitle = "Required for booster status notifications (Android 13+)",
                buttonText = if (hasNotificationPerm) "Granted ✓" else "Grant Notifications",
                granted = hasNotificationPerm,
                onClick = {
                    if (!hasNotificationPerm) {
                        notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
        }

        val phoneLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { /* result handled by state polling */ }

        PermissionCard(
            title = "Phone State (5G Detection)",
            subtitle = "Detects 5G network for optimal boost settings",
            buttonText = if (hasPhoneState) "Granted ✓" else "Grant Phone State",
            granted = hasPhoneState,
            onClick = {
                if (!hasPhoneState) {
                    phoneLauncher.launch(android.Manifest.permission.READ_PHONE_STATE)
                }
            }
        )

        // ── ADB Advanced Unlock Guide ──────────────────────────
        val adbCmd = "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasWriteSecure) SuccessGreen.copy(alpha = 0.08f)
                                 else PrimaryNeon.copy(alpha = 0.06f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (hasWriteSecure) SuccessGreen.copy(alpha = 0.5f) else PrimaryNeon.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (hasWriteSecure) "⚡ Advanced Boost: ACTIVE" else "🔓 Unlock Advanced Boost",
                        color = if (hasWriteSecure) SuccessGreen else PrimaryNeon,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (hasWriteSecure) {
                        Box(
                            modifier = Modifier
                                .background(SuccessGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) { Text("UNLOCKED", color = SuccessGreen, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black) }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (!hasWriteSecure) {
                    Text(
                        "Run this one-time ADB command from a PC to unlock deep system tweaks: animation scale, game driver, sync freeze, network priority, battery optimizer, and touch latency.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Copyable command box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                adbCmd,
                                color = PrimaryNeon,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(adbCmd))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon, contentColor = BackgroundDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📋  Copy ADB Command", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        "Deep system optimizations are active. Use the toggles below to control each boost independently.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // ── Secure Settings Toggles (shown when unlocked) ──────
        if (hasWriteSecure) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "⚡ Advanced System Tweaks",
                        color = PrimaryNeon,
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    SecureToggleRow("Kill Animations (Ultra Smooth)", "Sets scale to 0 — eliminates UI lag", secureAnimScale) { viewModel.setSecureAnimScale(it) }
                    HorizontalDivider(color = SurfaceVariantDark, modifier = Modifier.padding(vertical = 4.dp))
                    SecureToggleRow("Game GPU Driver", "Forces system to use optimized game GPU driver", secureGameDriver) { viewModel.setSecureGameDriver(it) }
                    HorizontalDivider(color = SurfaceVariantDark, modifier = Modifier.padding(vertical = 4.dp))
                    SecureToggleRow("Freeze Background Sync", "Stops background data sync while gaming", secureSyncOff) { viewModel.setSecureSyncOff(it) }
                    HorizontalDivider(color = SurfaceVariantDark, modifier = Modifier.padding(vertical = 4.dp))
                    SecureToggleRow("Mobile Data Priority", "Keeps mobile data active at max priority", secureMobileData) { viewModel.setSecureMobileData(it) }
                    HorizontalDivider(color = SurfaceVariantDark, modifier = Modifier.padding(vertical = 4.dp))
                    SecureToggleRow("Battery Saver Override", "Prevents battery saver from throttling performance", secureBatterySaver) { viewModel.setSecureBatterySaver(it) }
                    HorizontalDivider(color = SurfaceVariantDark, modifier = Modifier.padding(vertical = 4.dp))
                    SecureToggleRow("Disable Location Scan", "Stops WiFi/BT scanning to reduce CPU interrupts", secureLocationOff) { viewModel.setSecureLocationOff(it) }
                }
            }
        }

        // ── General Feature Toggles ───────────────────────────
        SettingCard(
            title = "Global Auto Boost",
            subtitle = "Boost performance globally for all games",
            checked = autoBoost,
            onCheckedChange = { viewModel.setGlobalAutoBoost(it) }
        )
        
        SettingCard(
            title = "Floating FPS Counter",
            subtitle = "Display a floating FPS overlay on screen",
            checked = overlayEnabled,
            onCheckedChange = { viewModel.setOverlayEnabled(it) }
        )

        SettingCard(
            title = "Auto Game Detector",
            subtitle = "Automatically apply boost when a game is detected",
            checked = detectorEnabled,
            onCheckedChange = { viewModel.setGameDetectorEnabled(it) }
        )

        val isDarkTheme by viewModel.isDarkTheme.collectAsState()
        SettingCard(
            title = "Dark Theme",
            subtitle = "Toggle between dark and light theme",
            checked = isDarkTheme,
            onCheckedChange = { viewModel.setDarkTheme(it) }
        )

        viewModel.profileMessage.collectAsState().value?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearProfileMessage()
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(msg, color = SuccessGreen, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { viewModel.exportProfiles() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryNeon),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Export Profiles") }

            val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
            ) { uri -> uri?.let { viewModel.importProfiles(it) } }

            OutlinedButton(
                onClick = { filePickerLauncher.launch("application/json") },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryNeon),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Import Profiles") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.stopAllBoosts() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Stop All Active Boosts", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SecureToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryNeon,
                checkedTrackColor = PrimaryNeon.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
fun PermissionCard(
    title: String,
    subtitle: String,
    buttonText: String,
    granted: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (granted) SuccessGreen.copy(alpha = 0.1f) else SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (granted) SuccessGreen else SecondaryNeon,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryNeon, checkedTrackColor = PrimaryNeon.copy(alpha=0.5f))
            )
        }
    }
}
