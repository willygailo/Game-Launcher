package com.gamelauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gamelauncher.ui.theme.SurfaceDark

@Composable
fun ArcGauge(
    modifier: Modifier = Modifier,
    progress: Float,
    color: Color,
    label: String,
    valueText: String,
    strokeWidth: Dp = 8.dp,
    size: Dp = 100.dp
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
            val arcSize = this.size.width - stroke
            val offset = stroke / 2f
            
            // Background Arc
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(offset, offset),
                size = Size(arcSize, arcSize),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Foreground Arc
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
        
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valueText,
                color = color,
                fontSize = (size.value * 0.22f).sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = label,
                color = com.gamelauncher.ui.theme.TextSecondary,
                fontSize = (size.value * 0.12f).sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

val ChamferedShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val c = w * 0.1f // 10% chamfer
    
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
fun HexagonButton(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    val hexShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.25f, 0f)
        lineTo(w * 0.75f, 0f)
        lineTo(w, h * 0.5f)
        lineTo(w * 0.75f, h)
        lineTo(w * 0.25f, h)
        lineTo(0f, h * 0.5f)
        close()
    }

    Box(
        modifier = modifier
            .clip(hexShape)
            .background(color.copy(alpha = 0.15f))
            .border(2.dp, color, hexShape)
            .clickable { onClick() }
            .padding(vertical = 24.dp, horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp
        )
    }
}
