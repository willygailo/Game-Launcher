package com.gamelauncher.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        // Permission Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("System Permissions", color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("For max brightness & full control, grant system settings permission:", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.requestWriteSettingsPermission() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryNeon, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Grant System Settings", fontWeight = FontWeight.Bold)
                }
            }
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

        Spacer(modifier = Modifier.weight(1f))

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