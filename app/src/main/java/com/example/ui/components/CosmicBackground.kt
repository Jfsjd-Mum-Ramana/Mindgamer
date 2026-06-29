package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.ui.sound.SoundSynth

data class Star(
    val initialX: Float,
    val initialY: Float,
    val speedX: Float,
    val speedY: Float,
    val radius: Float,
    val color: Color
)

data class TapRipple(
    val x: Float,
    val y: Float,
    val startTime: Long,
    val durationMs: Long = 800
)

@Composable
fun CosmicBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_bg")
    
    // Slow drift transition (0.0f to 1.0f)
    val driftState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "drift"
    )

    // Glowing pulsation (0.4f to 1.0f)
    val pulseState by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Generate random star nodes
    val stars = remember {
        val list = mutableListOf<Star>()
        val colors = listOf(
            Color(0xFFD0BCFF), // Purple
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFF9E00), // Amber
            Color(0xFFEFB8C8)  // Pink
        )
        for (i in 0..25) {
            list.add(
                Star(
                    initialX = (0..100).random().toFloat() / 100f,
                    initialY = (0..100).random().toFloat() / 100f,
                    speedX = ((5..15).random() * if ((0..1).random() == 0) 1 else -1).toFloat() / 1000f,
                    speedY = ((5..15).random() * if ((0..1).random() == 0) 1 else -1).toFloat() / 1000f,
                    radius = (4..12).random().toFloat(),
                    color = colors.random()
                )
            )
        }
        list
    }

    // Interactive ripple effects
    val ripples = remember { mutableStateListOf<TapRipple>() }
    val systemTime = System.currentTimeMillis()

    // Periodically prune stale ripples
    LaunchedEffect(key1 = systemTime) {
        while (true) {
            val now = System.currentTimeMillis()
            ripples.removeAll { now - it.startTime > it.durationMs }
            kotlinx.coroutines.delay(100)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    SoundSynth.playHover()
                    ripples.add(
                        TapRipple(
                            x = offset.x,
                            y = offset.y,
                            startTime = System.currentTimeMillis()
                        )
                    )
                }
            }
    ) {
        val width = size.width
        val height = size.height

        // Draw deep cosmic space background gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F0B1E), // Ultra deep space purple
                    Color(0xFF07050E)  // Black void
                )
            )
        )

        // Draw connections (synaptic lines) first so they sit in background
        for (i in 0 until stars.size) {
            val s1 = stars[i]
            val s1X = ((s1.initialX + s1.speedX * driftState * 10f) % 1.0f) * width
            val s1Y = ((s1.initialY + s1.speedY * driftState * 10f) % 1.0f) * height

            // Connect close stars
            for (j in i + 1 until stars.size) {
                val s2 = stars[j]
                val s2X = ((s2.initialX + s2.speedX * driftState * 10f) % 1.0f) * width
                val s2Y = ((s2.initialY + s2.speedY * driftState * 10f) % 1.0f) * height

                val dx = s1X - s2X
                val dy = s1Y - s2Y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)

                // If within 300dp, draw a thin link line
                if (dist < 280f) {
                    val alpha = (1.0f - (dist / 280f)) * 0.12f * pulseState
                    drawLine(
                        color = Color(0xFFD0BCFF).copy(alpha = alpha),
                        start = Offset(s1X, s1Y),
                        end = Offset(s2X, s2Y),
                        strokeWidth = 1.5f
                    )
                }
            }
        }

        // Draw floating star nodes
        for (star in stars) {
            // Apply drift position wrapping
            var sX = ((star.initialX + star.speedX * driftState * 10f) % 1.0f) * width
            var sY = ((star.initialY + star.speedY * driftState * 10f) % 1.0f) * height
            if (sX < 0) sX += width
            if (sY < 0) sY += height

            // Pulsating glow ring
            drawCircle(
                color = star.color.copy(alpha = 0.15f * pulseState),
                radius = star.radius * 2.8f,
                center = Offset(sX, sY)
            )

            // Inner solid core
            drawCircle(
                color = star.color.copy(alpha = 0.7f),
                radius = star.radius * 0.9f,
                center = Offset(sX, sY)
            )
        }

        // Draw active psychic ripples
        val now = System.currentTimeMillis()
        for (ripple in ripples) {
            val elapsed = now - ripple.startTime
            if (elapsed in 0..ripple.durationMs) {
                val fraction = elapsed.toFloat() / ripple.durationMs.toFloat()
                val radius = fraction * 220f
                val alpha = (1.0f - fraction) * 0.45f
                
                // Expanding neon glow ring
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = alpha),
                    radius = radius,
                    center = Offset(ripple.x, ripple.y),
                    style = Stroke(width = 4f - (fraction * 2.5f))
                )

                // Secondary inner purple ring
                drawCircle(
                    color = Color(0xFFD0BCFF).copy(alpha = alpha * 0.6f),
                    radius = radius * 0.6f,
                    center = Offset(ripple.x, ripple.y),
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}
