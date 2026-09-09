package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.HologramGold
import com.example.ui.theme.NeonCrimson
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    isActive: Boolean = true,
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    isLocked: Boolean = false,
    audioLevel: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactor")

    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking || isListening) 4000 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRotation"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking || isListening) 6000 else 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRotation"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 600 else if (isListening) 400 else 2200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primaryColor = when {
        isLocked -> NeonCrimson
        isListening -> HologramGold
        else -> CyberCyan
    }

    val secondaryColor = when {
        isLocked -> Color(0xFFFF88A5)
        isListening -> Color(0xFFFFE082)
        else -> Color(0xFF8CE8FF)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val radius = (size.toPx() / 2f) * 0.88f * (if (isSpeaking) pulseScale else 1f)

            // 1. Outer Background Radial Energy Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = if (isSpeaking || isListening) 0.35f else 0.15f),
                        primaryColor.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.3f
                ),
                radius = radius * 1.3f,
                center = center
            )

            // 2. Outermost Thin HUD Radar Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 3. Segmented Rotating Outer Arc Tracks
            rotate(outerRotation, pivot = center) {
                val segments = 8
                val sweep = 360f / segments
                for (i in 0 until segments) {
                    val startAngle = i * sweep + 4f
                    drawArc(
                        color = if (i % 2 == 0) primaryColor else HologramGold.copy(alpha = 0.8f),
                        startAngle = startAngle,
                        sweepAngle = sweep - 12f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 4. Middle Arc Reactor Coils (Counter-Rotating)
            val midRadius = radius * 0.72f
            rotate(innerRotation, pivot = center) {
                val coils = 10
                val angleStep = (2 * PI / coils).toFloat()
                for (i in 0 until coils) {
                    val angle = i * angleStep
                    val spokeStartX = center.x + cos(angle) * (midRadius * 0.75f)
                    val spokeStartY = center.y + sin(angle) * (midRadius * 0.75f)
                    val spokeEndX = center.x + cos(angle) * midRadius
                    val spokeEndY = center.y + sin(angle) * midRadius

                    drawLine(
                        color = secondaryColor.copy(alpha = 0.85f),
                        start = Offset(spokeStartX, spokeStartY),
                        end = Offset(spokeEndX, spokeEndY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                drawCircle(
                    color = primaryColor.copy(alpha = 0.7f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 5. Inner Power Core & Triangular Focus Nodes
            val innerRadius = radius * 0.42f
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = innerRadius,
                center = center
            )
            drawCircle(
                color = primaryColor,
                radius = innerRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Dynamic Audio Wave / Core Triangle
            val triPath = Path().apply {
                val dynamicOffset = if (isSpeaking) (audioLevel * 12f) else 0f
                val r = innerRadius * 0.65f + dynamicOffset
                moveTo(center.x, center.y - r)
                lineTo(center.x + r * 0.866f, center.y + r * 0.5f)
                lineTo(center.x - r * 0.866f, center.y + r * 0.5f)
                close()
            }

            drawPath(
                path = triPath,
                color = Color.White.copy(alpha = if (isSpeaking) 0.95f else 0.75f)
            )

            // Center Point Light
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = center
            )
        }
    }
}
