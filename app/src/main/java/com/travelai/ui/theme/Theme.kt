package com.travelai.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ══════════════════════════════════════════════════════════════════════════
// TravelAI Theme — Light is the default.
// Light scheme is wired to the new pastel tokens in Color.kt.
// Dark scheme is kept as a fallback only; it now reuses the same tokens
// so the app stays visually coherent if a user ever toggles dark mode.
// ══════════════════════════════════════════════════════════════════════════

val TravelAILightColorScheme = lightColorScheme(
    primary              = BrandPurple,
    onPrimary            = OnBrand,
    primaryContainer     = BrandPurpleSoft,
    onPrimaryContainer   = BrandPurpleStrong,

    secondary            = BrandBlue,
    onSecondary          = OnBrand,
    secondaryContainer   = BrandBlueSoft,
    onSecondaryContainer = Color(0xFF003A66),

    tertiary             = BrandTeal,
    onTertiary           = OnBrand,
    tertiaryContainer    = SuccessGreenSoft,
    onTertiaryContainer  = Color(0xFF003D33),

    background           = SurfaceBgTop,
    onBackground         = InkPrimary,

    surface              = SurfaceCard,
    onSurface            = InkPrimary,
    surfaceVariant       = SurfaceContainer,
    onSurfaceVariant     = InkSecondary,
    surfaceTint          = BrandPurple,

    error                = DangerRed,
    onError              = OnBrand,
    errorContainer       = DangerRedSoft,
    onErrorContainer     = Color(0xFF5C0010),

    outline              = BorderSubtle,
    outlineVariant       = BorderSoft,

    inverseSurface       = InkPrimary,
    inverseOnSurface     = SurfaceCard,
    inversePrimary       = BrandPurpleSoft
)

val TravelAIDarkColorScheme = darkColorScheme(
    primary              = BrandPurple,
    onPrimary            = OnBrand,
    primaryContainer     = BrandPurpleStrong,
    onPrimaryContainer   = BrandPurpleSoft,

    secondary            = BrandBlue,
    onSecondary          = OnBrand,
    secondaryContainer   = Color(0xFF0F3A66),
    onSecondaryContainer = BrandBlueSoft,

    tertiary             = BrandTeal,
    onTertiary           = OnBrand,
    tertiaryContainer    = Color(0xFF0F3A33),
    onTertiaryContainer  = SuccessGreenSoft,

    background           = DarkBgTop,
    onBackground         = DarkInkPrimary,

    surface              = DarkSurfaceCard,
    onSurface            = DarkInkPrimary,
    surfaceVariant       = DarkSurfaceRaised,
    onSurfaceVariant     = DarkInkSecondary,
    surfaceTint          = BrandPurple,

    error                = DangerRed,
    onError              = OnBrand,
    errorContainer       = Color(0xFF5C0010),
    onErrorContainer     = DangerRedSoft,

    outline              = DarkBorderSubtle,
    outlineVariant       = Color(0xFF1F2645),

    inverseSurface       = SurfaceCard,
    inverseOnSurface     = InkPrimary,
    inversePrimary       = BrandPurpleSoft
)

@Composable
fun TravelAITheme(
    darkTheme: Boolean = false,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TravelAIDarkColorScheme else TravelAILightColorScheme
    val typography = TravelAITypography.scaledBy(fontScale)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = TravelAIShapes,
        content = content
    )
}
