package com.travelai.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Centralized gradient brushes for the TravelAI wave-2 visual upgrade.
 *
 * - [auroraDiagonal]: 3-stop purple→pink→blue diagonal used for primary CTAs,
 *   hero accents, and user chat bubbles.
 * - [sunrise]: warm amber→coral linear for "callout" / empty state moments.
 * - [glassTintWash]: very soft top-down white wash applied over imagery to
 *   suggest glass refraction without using a real blur (cheap & API-safe).
 *
 * Mesh background lives in [com.travelai.ui.components.AppBackground] as a
 * `Canvas` painter — gradient lists feeding the 4-blob composition are kept
 * close to the consumer rather than centralized here.
 */
object TravelAIGradients {

    val auroraDiagonal: Brush = Brush.linearGradient(
        colorStops = arrayOf(
            0.0f to BrandPurpleStrong,
            0.55f to BrandPink,
            1.0f to BrandBlue
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    val sunrise: Brush = Brush.linearGradient(
        listOf(SunshineAmber, SunriseCoral)
    )

    val glassTintWash: Brush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0f),
            Color.White.copy(alpha = 0.22f)
        )
    )

    /** Convenience: aurora gradient as a vertical orientation (used for thin stripes). */
    val auroraVertical: Brush = Brush.verticalGradient(
        listOf(BrandPurpleStrong, BrandPink, BrandBlue)
    )
}

/**
 * Four-blob mesh gradient palette consumed by [com.travelai.ui.components.AppBackground].
 * Returned as a list of (color, normalizedCenter) pairs.
 */
@Composable
fun travelAiMeshBlobs(darkTheme: Boolean = false): List<Pair<Color, Offset>> =
    if (darkTheme) {
        listOf(
            BrandPurple.copy(alpha = 0.18f) to Offset(0.15f, 0.15f),
            BrandBlue.copy(alpha = 0.16f) to Offset(0.85f, 0.85f),
            BrandPink.copy(alpha = 0.12f) to Offset(0.10f, 0.55f),
            SunshineAmber.copy(alpha = 0.10f) to Offset(0.90f, 0.20f)
        )
    } else {
        listOf(
            BrandPurpleSoft to Offset(0.15f, 0.15f),
            BrandBlueSoft to Offset(0.85f, 0.85f),
            BrandPurpleTint to Offset(0.10f, 0.55f),
            SunshineAmberSoft.copy(alpha = 0.12f) to Offset(0.90f, 0.20f)
        )
    }
