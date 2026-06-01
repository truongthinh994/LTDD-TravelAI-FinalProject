package com.travelai.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Five-level shadow ladder for the bento + glassmorphism design language.
 *
 * Each level pairs an ambient color (used for the soft outer halo) with a
 * spot color (used for the directional drop). Levels L3-L4 inject brand
 * color into the shadow so feature/CTA elements feel like they belong to
 * the same gradient family.
 *
 * Usage:
 * ```
 * Modifier.bentoShadow(ShadowLevel.L3, BentoShapes.hero)
 * ```
 */
enum class ShadowLevel(
    val elevation: Dp,
    val ambient: Color,
    val spot: Color
) {
    L1(elevation = 2.dp,  ambient = ShadowL1Ambient, spot = ShadowL1Spot),
    L2(elevation = 6.dp,  ambient = ShadowL2Ambient, spot = ShadowL2Spot),
    L3(elevation = 12.dp, ambient = ShadowL3Ambient, spot = ShadowL3Spot),
    L4(elevation = 14.dp, ambient = ShadowL4Ambient, spot = ShadowL4Spot),
    L5(elevation = 20.dp, ambient = ShadowL5Ambient, spot = ShadowL5Spot)
}

fun Modifier.bentoShadow(
    level: ShadowLevel,
    shape: Shape
): Modifier = this.shadow(
    elevation = level.elevation,
    shape = shape,
    ambientColor = level.ambient,
    spotColor = level.spot
)
