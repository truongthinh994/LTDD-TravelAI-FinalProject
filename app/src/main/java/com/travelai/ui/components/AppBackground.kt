package com.travelai.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.travelai.ui.theme.DarkBgBottom
import com.travelai.ui.theme.DarkBgMid
import com.travelai.ui.theme.DarkBgTop
import com.travelai.ui.theme.SurfaceBgBottom
import com.travelai.ui.theme.SurfaceBgMid
import com.travelai.ui.theme.SurfaceBgTop
import com.travelai.ui.theme.travelAiMeshBlobs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared background for all main screens.
 *
 * Wave 2 — mesh gradient: a soft vertical base gradient + 4 slowly orbiting
 * radial-gradient blobs drawn via a single [Canvas] pass. Blob rotation runs
 * on an `infiniteRepeatable` over 18 seconds — cheap GPU cost (single state
 * read, no recomposition of children).
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val darkTheme = MaterialTheme.colorScheme.background == DarkBgTop
    val blobs = travelAiMeshBlobs(darkTheme = darkTheme)

    val infiniteTransition = rememberInfiniteTransition(label = "mesh-orbit")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mesh-orbit-phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (darkTheme) {
                    Brush.verticalGradient(
                        0.0f to DarkBgTop,
                        0.52f to DarkBgMid,
                        1.0f to DarkBgBottom
                    )
                } else {
                    Brush.verticalGradient(
                        0.0f to SurfaceBgTop,
                        0.45f to SurfaceBgMid,
                        1.0f to SurfaceBgBottom
                    )
                }
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val orbitRadius = minOf(w, h) * 0.08f
            blobs.forEachIndexed { index, (color, anchor) ->
                // Slightly different phase per blob → asynchronous orbit.
                val localPhase = phase + index * (Math.PI / 3).toFloat()
                val cx = anchor.x * w + cos(localPhase) * orbitRadius
                val cy = anchor.y * h + sin(localPhase) * orbitRadius
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color, Color.Transparent),
                        center = Offset(cx, cy),
                        radius = minOf(w, h) * 0.65f
                    ),
                    radius = minOf(w, h) * 0.65f,
                    center = Offset(cx, cy)
                )
            }
        }
        content()
    }
}
