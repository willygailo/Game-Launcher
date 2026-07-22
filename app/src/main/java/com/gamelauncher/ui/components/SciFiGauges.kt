package com.gamelauncher.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelauncher.ui.theme.RogCardBorder
import com.gamelauncher.ui.theme.RogSurfaceDark
import com.gamelauncher.ui.theme.SurfaceDark
import com.gamelauncher.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcGauge(
    modifier: Modifier = Modifier,
    progress: Float,
    color: Color,
    label: String,
    valueText: String,
    strokeWidth: Dp = 8.dp,
    size: Dp = 105.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "gaugeProgress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val paddingPx = 8.dp.toPx()
            val arcSize = this.size.width - stroke - paddingPx
            val offset = (stroke + paddingPx) / 2f

            // Outer Tick Marks / Cyber Ring
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.width / 2f - 2.dp.toPx()
            val startAngle = 135f
            val sweepAngle = 270f
            val totalTicks = 18

            for (i in 0..totalTicks) {
                val angleDeg = startAngle + (sweepAngle / totalTicks) * i
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val innerR = radius - 4.dp.toPx()
                val outerR = radius
                val p1 = Offset(
                    (center.x + innerR * cos(angleRad)).toFloat(),
                    (center.y + innerR * sin(angleRad)).toFloat()
                )
                val p2 = Offset(
                    (center.x + outerR * cos(angleRad)).toFloat(),
                    (center.y + outerR * sin(angleRad)).toFloat()
                )
                val tickColor = if (i.toFloat() / totalTicks <= animatedProgress) color else color.copy(alpha = 0.2f)
                drawLine(
                    color = tickColor,
                    start = p1,
                    end = p2,
                    strokeWidth = 3f
                )
            }

            // Background Arc
            drawArc(
                color = color.copy(alpha = 0.15f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Foreground Glowing Arc
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valueText,
                color = color,
                fontSize = (size.value * 0.21f).sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                color = TextSecondary,
                fontSize = (size.value * 0.11f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

val ChamferedShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val c = (w * 0.08f).coerceAtMost(36f)

    moveTo(c, 0f)
    lineTo(w - c, 0f)
    lineTo(w, c)
    lineTo(w, h - c)
    lineTo(w - c, h)
    lineTo(c, h)
    lineTo(0f, h - c)
    lineTo(0f, c)
    close()
}

@Composable
fun AngledCard(
    modifier: Modifier = Modifier,
    color: Color = SurfaceDark,
    borderColor: Color = Color.Transparent,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(ChamferedShape)
            .background(color)
            .border(1.dp, borderColor, ChamferedShape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun RogArmorCard(
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF00E5FF),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(ChamferedShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RogSurfaceDark,
                        Color(0xFF0E1118)
                    )
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.35f), ChamferedShape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun HexagonButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val hexShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.22f, 0f)
        lineTo(w * 0.78f, 0f)
        lineTo(w, h * 0.5f)
        lineTo(w * 0.78f, h)
        lineTo(w * 0.22f, h)
        lineTo(0f, h * 0.5f)
        close()
    }

    Box(
        modifier = modifier
            .clip(hexShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = pulseAlpha * 0.4f),
                        color.copy(alpha = 0.1f),
                        Color.Transparent
                    )
                )
            )
            .border(2.dp, color, hexShape)
            .clickable { onClick() }
            .padding(vertical = 20.dp, horizontal = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⚡ $text",
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Text(
                text = "SYSTEM REACTOR",
                color = color.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}
