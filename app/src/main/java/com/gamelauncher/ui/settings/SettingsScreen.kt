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
    val context = androidx.compose.ui.platform.LocalContext.current

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

        PermissionCard(
            title = "System Settings Access",
            subtitle = "Needed for brightness and animation controls",
            buttonText = if (hasWriteSettings) "Granted" else "Grant Write Settings",
            granted = hasWriteSettings,
            onClick = viewModel::requestWriteSettingsPermission
        )

        PermissionCard(
            title = "Overlay Permission",
            subtitle = "Required for floating FPS counter",
            buttonText = if (hasOverlayPerm) "Granted" else "Grant Overlay",
            granted = hasOverlayPerm,
            onClick = viewModel::requestOverlayPermission
        )

        PermissionCard(
            title = "Usage Access",
            subtitle = "Required for auto game detection while gaming",
            buttonText = if (hasUsageAccess) "Granted" else "Grant Usage Access",
            granted = hasUsageAccess,
            onClick = viewModel::requestUsageAccessPermission
        )

        PermissionCard(
            title = "Battery Optimization",
            subtitle = "Keeps booster services alive in long sessions",
            buttonText = if (hasBatteryExempt) "Granted" else "Ignore Battery Optimization",
            granted = hasBatteryExempt,
            onClick = viewModel::requestBatteryOptimizationExemption
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            PermissionCard(
                title = "Notifications",
                subtitle = "Required for booster status notifications (Android 13+)",
                buttonText = if (hasNotificationPerm) "Granted" else "Grant Notifications",
                granted = hasNotificationPerm,
                onClick = {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            )
        }

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
