@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.travelai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.travelai.R

// ══════════════════════════════════════════════════════════════════════════
// TravelAI — Typography
//
// Wave 2 — custom font pairing:
//   • Be Vietnam Pro (body/UI) — full Vietnamese diacritic coverage,
//     modern geometric sans, 4 static weights bundled.
//   • Plus Jakarta Sans (display/hero) — distinctive hairlines for
//     headline copy. Shipped as variable font; weight axis driven via
//     FontVariation.Settings.
//
// Text color is driven by MaterialTheme.colorScheme (onSurface /
// onSurfaceVariant) so the same scale works in light + dark.
// ══════════════════════════════════════════════════════════════════════════

/** Body / UI font — used for everything below headlineLarge. */
val BodyFontFamily = FontFamily(
    Font(R.font.be_vietnam_pro_regular,  FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium,   FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_bold,     FontWeight.Bold)
)

/** Display / hero font — variable Plus Jakarta Sans driven by weight axis. */
val DisplayFontFamily = FontFamily(
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        resId = R.font.plus_jakarta_sans_variable,
        weight = FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(800))
    )
)

// Backward-compat alias — kept so legacy references still resolve.
val TravelAIFontFamily: FontFamily = BodyFontFamily

val TravelAITypography = Typography(
    // ── Display: hero copy uses Plus Jakarta Sans ──────────────────────────
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 64.sp,
        lineHeight = 72.sp,
        letterSpacing = (-2.0).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-2.0).sp
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.8).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.4).sp
    ),

    // ── Headline / title / body / label: Be Vietnam Pro ────────────────────
    headlineMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

const val MIN_APP_FONT_SCALE = 0.85f
const val MAX_APP_FONT_SCALE = 1.30f

fun Typography.scaledBy(fontScale: Float): Typography {
    val scale = fontScale.coerceIn(MIN_APP_FONT_SCALE, MAX_APP_FONT_SCALE)
    if (scale == 1.0f) return this

    return copy(
        displayLarge = displayLarge.scaledBy(scale),
        displayMedium = displayMedium.scaledBy(scale),
        displaySmall = displaySmall.scaledBy(scale),
        headlineLarge = headlineLarge.scaledBy(scale),
        headlineMedium = headlineMedium.scaledBy(scale),
        headlineSmall = headlineSmall.scaledBy(scale),
        titleLarge = titleLarge.scaledBy(scale),
        titleMedium = titleMedium.scaledBy(scale),
        titleSmall = titleSmall.scaledBy(scale),
        bodyLarge = bodyLarge.scaledBy(scale),
        bodyMedium = bodyMedium.scaledBy(scale),
        bodySmall = bodySmall.scaledBy(scale),
        labelLarge = labelLarge.scaledBy(scale),
        labelMedium = labelMedium.scaledBy(scale),
        labelSmall = labelSmall.scaledBy(scale)
    )
}

private fun TextStyle.scaledBy(scale: Float): TextStyle = copy(
    fontSize = fontSize * scale,
    lineHeight = lineHeight * scale
)
