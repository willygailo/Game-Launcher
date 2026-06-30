package com.gamelauncher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gamelauncher.ui.theme.BackgroundDark
import com.gamelauncher.ui.theme.PrimaryNeon
import com.gamelauncher.ui.theme.SecondaryNeon
import com.gamelauncher.ui.theme.SurfaceDark
import com.gamelauncher.ui.theme.SurfaceVariantDark
import com.gamelauncher.ui.theme.TextPrimary
import com.gamelauncher.ui.theme.TextSecondary

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun LoadingState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = PrimaryNeon, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(PrimaryNeon.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .border(1.dp, PrimaryNeon.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onAction,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryNeon),
                    border = BorderStroke(1.dp, PrimaryNeon.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = BackgroundDark,
            disabledContainerColor = SurfaceVariantDark,
            disabledContentColor = TextSecondary
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (enabled) {
                        Brush.horizontalGradient(listOf(PrimaryNeon, SecondaryNeon.copy(alpha = 0.85f)))
                    } else {
                        Brush.horizontalGradient(listOf(SurfaceVariantDark, SurfaceVariantDark))
                    },
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    borderColor: Color = SurfaceVariantDark,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.45f))
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
