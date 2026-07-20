package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun OrbComponent(
    state: JarvisState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_transition")

    // Idle breathing scale
    val idleScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_scale"
    )

    // Thinking rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_rotation"
    )

    // Speaking pulse
    val speakingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_scale"
    )

    // Listening rings alpha
    val listeningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening_alpha"
    )
    val listeningRadius by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "listening_radius"
    )

    val currentScale by remember {
        derivedStateOf {
            when (state) {
                JarvisState.IDLE -> idleScale
                JarvisState.SPEAKING -> speakingScale
                else -> 1.0f
            }
        }
    }

    val glowColor = when (state) {
        JarvisState.ERROR -> Color(0xFFFF5C5C)
        JarvisState.SUCCESS -> Color(0xFF4CD97B)
        JarvisState.LISTENING -> Color(0xFF00E5FF)
        else -> Color.Transparent
    }

    val glassSurface = Color.White.copy(alpha = 0.10f)
    val glassBorder = Color.White.copy(alpha = 0.20f)
    val accentPurple = Color(0xFF7C4DFF)
    val accentCyan = Color(0xFF00E5FF)

    Box(
        modifier = modifier
            .size(160.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = currentScale
                    scaleY = currentScale
                    if (state == JarvisState.THINKING) {
                        rotationZ = rotation
                    }
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.width / 3f

            // Glow for states
            if (glowColor != Color.Transparent) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.5f), Color.Transparent),
                        center = center,
                        radius = baseRadius * 2
                    ),
                    radius = baseRadius * 2,
                    center = center
                )
            }

            // Listening Expanding Rings
            if (state == JarvisState.LISTENING) {
                drawCircle(
                    color = accentCyan.copy(alpha = listeningAlpha),
                    radius = baseRadius * listeningRadius,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )
            }

            // Thinking gradient sweep
            if (state == JarvisState.THINKING) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(accentPurple, accentCyan, accentPurple),
                        center = center
                    ),
                    radius = baseRadius * 1.2f,
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )
            }

            // Core Glass Orb
            drawCircle(
                color = glassSurface,
                radius = baseRadius,
                center = center
            )
            drawCircle(
                color = glassBorder,
                radius = baseRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
