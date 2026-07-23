package com.gamelauncher.feature.gamespace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NeonCyan = Color(0xFF00F0FF)
private val NeonPink = Color(0xFFFF0055)
private val DarkBg = Color(0xFF0B0E14)
private val CardBg = Color(0xFF141923)
private val BorderColor = Color(0xFF263044)
private val MutedText = Color(0xFF8A99AD)
private val SuccessGreen = Color(0xFF00FF88)

@Composable
fun HuaweiPairingGuideScreen(
    onBack: () -> Unit = {},
    onLaunchShizuku: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "HUAWEI SETUP GUIDE",
                    color = NeonPink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    "EMUI / HarmonyOS Wireless Debugging -> Shizuku Pairing",
                    color = MutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = BorderColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("BACK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                StepCard(
                    stepNumber = "01",
                    title = "Enable Developer Options on EMUI / HarmonyOS",
                    description = "Open Settings -> About Phone -> Tap 'Build Number' 7 times until the popup 'You are now a developer' appears."
                )
            }
            item {
                StepCard(
                    stepNumber = "02",
                    title = "Access System & Updates Settings",
                    description = "Go back to Settings -> System & Updates -> Developer Options."
                )
            }
            item {
                StepCard(
                    stepNumber = "03",
                    title = "Enable Wireless Debugging",
                    description = "Scroll down to Networking section -> Toggle 'Wireless Debugging' ON -> Tap 'Allow' on the network prompt."
                )
            }
            item {
                StepCard(
                    stepNumber = "04",
                    title = "Pair Code with Shizuku",
                    description = "Tap 'Pair device with pairing code'. Pull down the Shizuku pairing notification, enter the 6-digit code, and tap 'Start Shizuku'."
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLaunchShizuku,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("OPEN SHIZUKU FOR PAIRING", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StepCard(
    stepNumber: String,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, BorderColor, RoundedCornerShape(14.dp)),
        color = CardBg
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = NeonPink.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink)
            ) {
                Text(
                    stepNumber,
                    color = NeonPink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, color = MutedText, fontSize = 11.sp)
            }
        }
    }
}
