package com.travelai.ui.components

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Soft radial accent used inside bento cards.
 *
 * It is intentionally cheap: a single radial gradient drawn behind the card
 * content instead of a blurred layer. This keeps the effect API-26 friendly.
 */
fun Modifier.decorativeBlob(
    color: Color,
    alignment: Alignment = Alignment.TopEnd,
    sizeDp: Dp = 88.dp
): Modifier = drawBehind {
    val radius = sizeDp.toPx() / 2f
    val center = when (alignment) {
        Alignment.TopStart -> Offset(0f, 0f)
        Alignment.TopCenter -> Offset(size.width / 2f, 0f)
        Alignment.TopEnd -> Offset(size.width, 0f)
        Alignment.CenterStart -> Offset(0f, size.height / 2f)
        Alignment.Center -> Offset(size.width / 2f, size.height / 2f)
        Alignment.CenterEnd -> Offset(size.width, size.height / 2f)
        Alignment.BottomStart -> Offset(0f, size.height)
        Alignment.BottomCenter -> Offset(size.width / 2f, size.height)
        Alignment.BottomEnd -> Offset(size.width, size.height)
        else -> Offset(size.width, 0f)
    }
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
