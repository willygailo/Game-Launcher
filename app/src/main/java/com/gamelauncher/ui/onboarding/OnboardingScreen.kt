package com.gamelauncher.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gamelauncher.ui.theme.*

private val pages = listOf(
    OnboardingPage(
        title = "Welcome to Game Launcher",
        description = "Boost your gaming performance with real-time CPU/GPU optimization, FPS monitoring, and smart performance tweaks.",
        icon = "🎮"
    ),
    OnboardingPage(
        title = "Auto Game Detection",
        description = "The app automatically detects when you launch a game and applies the best performance settings instantly.",
        icon = "🤖"
    ),
    OnboardingPage(
        title = "Permissions Needed",
        description = "Grant overlay permission for the FPS counter, usage access for game detection, and system settings for brightness/animation control.",
        icon = "🔐"
    ),
    OnboardingPage(
        title = "Ready to Boost?",
        description = "You're all set! Tap Get Started and let the app guide you through the permissions one by one.",
        icon = "⚡"
    )
)

data class OnboardingPage(val title: String, val description: String, val icon: String)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = pages[currentPage].icon,
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = pages[currentPage].title,
            style = MaterialTheme.typography.headlineMedium,
            color = PrimaryNeon,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = pages[currentPage].description,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 24.dp else 8.dp, 8.dp)
                        .background(
                            if (index == currentPage) PrimaryNeon else TextSecondary.copy(alpha = 0.4f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (currentPage < pages.lastIndex) currentPage++
                else onComplete()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryNeon,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (currentPage < pages.lastIndex) "Next" else "Get Started",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (currentPage < pages.lastIndex) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onComplete) {
                Text("Skip", color = TextSecondary)
            }
        }
    }
}
