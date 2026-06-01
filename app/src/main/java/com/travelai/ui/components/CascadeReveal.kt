package com.travelai.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * Fade + slide-up entry animation with per-item stagger.
 *
 * Apply to direct children of a screen-level Column so each block reveals in
 * sequence after composition. The first item appears immediately and each
 * subsequent item is offset by [delayPerItemMs] milliseconds.
 *
 * @param index zero-based position of this item in the stagger sequence
 * @param delayPerItemMs delay between items, default 50 ms
 * @param translationY initial Y offset in pixels, default 24 px (~ 8 dp at xxhdpi)
 * @param durationMs reveal animation duration, default 320 ms
 */
@Composable
fun Modifier.cascadeReveal(
    index: Int,
    delayPerItemMs: Long = 50L,
    translationY: Float = 24f,
    durationMs: Int = 320
): Modifier {
    // Per-instance trigger flipped to true after the stagger delay.
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index.coerceAtLeast(0) * delayPerItemMs)
        revealed = true
    }

    val progress by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = durationMs, easing = LinearOutSlowInEasing),
        label = "cascadeReveal-$index"
    )

    return this.graphicsLayer {
        alpha = progress
        this.translationY = (1f - progress) * translationY
    }
}
