package com.travelai.ui.theme

import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════════════════════════════════════════
// TravelAI — Light/Pastel design tokens
// Single source of truth. All screens read from MaterialTheme + these names.
// ══════════════════════════════════════════════════════════════════════════

// ── Brand palette ─────────────────────────────────────────────────────────
val BrandPurple        = Color(0xFF9B4DFF)
val BrandPurpleStrong  = Color(0xFF7B2FFF)
val BrandPurpleSoft    = Color(0xFFE9D7FF)
val BrandPurpleTint    = Color(0xFFF3EBFF)

val BrandBlue          = Color(0xFF28A8FF)
val BrandBlueSoft      = Color(0xFFC7E6FF)
val BrandCyan          = Color(0xFF27C8E8)
val BrandTeal          = Color(0xFF19BFA6)
val BrandOrange        = Color(0xFFFF8A2C)
val BrandPink          = Color(0xFFE160FF)

// ── Backgrounds (light) ───────────────────────────────────────────────────
val SurfaceBgTop       = Color(0xFFFDFBFF) // upper page gradient stop
val SurfaceBgMid       = Color(0xFFFFFFFF)
val SurfaceBgBottom    = Color(0xFFF2FAFF) // lower page gradient stop
val SurfaceCard        = Color(0xFFFFFFFF)
val SurfaceContainer   = Color(0xFFF4F2FB) // subtle elevated container
val SurfaceMuted       = Color(0xFFEEF2FB)

// ── Borders (light) ───────────────────────────────────────────────────────
val BorderSubtle       = Color(0xFFE3EAF8) // default outline
val BorderSoft         = Color(0xFFDCE7FF) // brand-tinted outline
val BorderStrong       = Color(0xFFB6C5E0)

// ── Text on light ─────────────────────────────────────────────────────────
val InkPrimary         = Color(0xFF07132F) // titles / strong text
val InkSecondary       = Color(0xFF53647F) // body / muted text
val InkTertiary        = Color(0xFF8895AE)
val InkDisabled        = Color(0xFFB0B9CC)
val OnBrand            = Color(0xFFFFFFFF)

// ── Functional ────────────────────────────────────────────────────────────
val SuccessGreen       = Color(0xFF18BFA6)
val SuccessGreenSoft   = Color(0xFFD3F6EE)
val WarningAmber       = Color(0xFFFFB347)
val WarningAmberSoft   = Color(0xFFFFE8C7)
val DangerRed          = Color(0xFFE5475C)
val DangerRedSoft      = Color(0xFFFFDDE3)

// ══════════════════════════════════════════════════════════════════════════
// ── Wave 2: Bento + soft glassmorphism upgrade tokens ────────────────────
// Glass tier, warm accents, shadow ladder, dark refine.
// ══════════════════════════════════════════════════════════════════════════

// ── Glass surfaces (light) ────────────────────────────────────────────────
// Used as overlay on top of a backdrop blurred via RenderEffect (API 31+).
// Fallback on API <31: apply directly as solid alpha tint.
val GlassSurfaceL1     = Color(0xCCFFFFFF) // alpha 0.80 — modals/sheets
val GlassSurfaceL2     = Color(0xB3FFFFFF) // alpha 0.70 — main cards
val GlassSurfaceL3     = Color(0x80FFFFFF) // alpha 0.50 — overlay pills on imagery
val GlassHighlight     = Color(0x66FFFFFF) // soft inner highlight (refraction hint)
val GlassTintPurple    = Color(0x269B4DFF) // 15% purple wash for glass over images
val GlassTintBlue      = Color(0x2628A8FF) // 15% blue wash

// ── Glass surfaces (dark) ─────────────────────────────────────────────────
val GlassSurfaceDarkL1 = Color(0xCC1A1F38)
val GlassSurfaceDarkL2 = Color(0xB31A1F38)
val GlassSurfaceDarkL3 = Color(0x801A1F38)
val GlassHighlightDark = Color(0x33FFFFFF) // top-edge highlight in dark glass

// ── Warm accents (break tím monotony) ────────────────────────────────────
val SunshineAmber      = Color(0xFFFFB547) // primary warm accent
val SunshineAmberSoft  = Color(0xFFFFE4B8) // soft container
val SunshineAmberDeep  = Color(0xFFE08A1F) // text-on-amber-soft
val SunriseCoral       = Color(0xFFFF7B6B) // contrast accent (empty state, callout)
val SunriseCoralSoft   = Color(0xFFFFD8D2)
val MintFresh          = Color(0xFF5DD4B5) // success-secondary, saved-state pill

// ── Shadow ladder (5 cấp) ────────────────────────────────────────────────
// Replaces single BrandPurple.copy(0.08). Use Modifier.bentoShadow(level).
val ShadowL1Ambient    = Color(0x140A0E2A) //  8% — chip, input micro-lift
val ShadowL1Spot       = Color(0x1F0A0E2A) // 12%
val ShadowL2Ambient    = Color(0x1A0A0E2A) // 10% — standard card
val ShadowL2Spot       = Color(0x2B0A0E2A) // 17%
val ShadowL3Ambient    = Color(0x269B4DFF) // 15% purple — bento feature/hero
val ShadowL3Spot       = Color(0x3D9B4DFF) // 24% purple
val ShadowL4Ambient    = Color(0x3DE160FF) // 24% pink — primary CTA
val ShadowL4Spot       = Color(0x52E160FF) // 32% pink
val ShadowL5Ambient    = Color(0x520A0E2A) // 32% — floating dock
val ShadowL5Spot       = Color(0x660A0E2A) // 40%

// ── Dark mode refine ─────────────────────────────────────────────────────
val DarkBgTop          = Color(0xFF0B1024) // deep indigo (NOT pure black)
val DarkBgMid          = Color(0xFF14182E)
val DarkBgBottom       = Color(0xFF0F1430)
val DarkSurfaceCard    = Color(0xFF1A1F38)
val DarkSurfaceRaised  = Color(0xFF222848) // bento-feature in dark
val DarkInkPrimary     = Color(0xFFF1F3FB)
val DarkInkSecondary   = Color(0xFFAAB3CC)
val DarkInkTertiary    = Color(0xFF7782A0)
val DarkBorderSubtle   = Color(0xFF2A3258)

// ══════════════════════════════════════════════════════════════════════════
// Legacy aliases — kept so existing screens keep compiling.
// New code should reference the names above directly.
// ══════════════════════════════════════════════════════════════════════════

// Brand (was dark cyberpunk → now mapped to light pastel)
val ElectricPurple        = BrandPurple
val ElectricPurpleLight   = Color(0xFFB97BFF)
val ElectricPurpleVivid   = BrandPurpleStrong
val PurpleGlow            = BrandPurple.copy(alpha = 0.25f)
val PurpleGradientStart   = BrandPurple
val PurpleGradientEnd     = BrandPurple.copy(alpha = 0f)

val NeonGreen             = SuccessGreen
val NeonGreenDark         = Color(0xFF12A38C)
val NeonGreenGlow         = SuccessGreen.copy(alpha = 0.25f)
val NeonGreenGradientStart= SuccessGreen
val NeonGreenGradientEnd  = NeonGreenDark
val NeonGlow              = SuccessGreen
val OnNeonGreen           = OnBrand

val CoralAccent           = Color(0xFFFF6B8A)
val AmberAccent           = WarningAmber
val CyanAccent            = BrandCyan

// Backgrounds — old dark names now bound to light surfaces
val DeepNavy              = SurfaceBgTop
val MediumNavy            = SurfaceBgMid
val SurfaceDark           = SurfaceCard
val SurfaceContainerDark  = SurfaceContainer
val LightBg               = SurfaceBgTop
val LightSurface          = SurfaceCard
val LightSurfaceContainer = SurfaceContainer

// Text — old dark names now bound to ink scale
val TextPrimary           = InkPrimary
val TextSecondary         = InkSecondary
val TextTertiary          = InkTertiary
val TextOnLight           = InkPrimary
val TextSecondaryOnLight  = InkSecondary

// Glass surfaces — old translucent dark names → light card surfaces
val GlassSurface          = SurfaceCard
val GlassSurfaceLighter   = SurfaceMuted
val GlassBorder           = BorderSubtle
val GlassSurfaceLight     = BrandPurpleTint
val GlassBorderLight      = BorderSoft

// Functional aliases
val ErrorRed              = DangerRed
val ErrorRedContainer     = DangerRedSoft

// Misc legacy
val OceanBlue             = BrandBlue
val OceanBlueDark         = BrandBlueSoft
val SeafoamGreen          = SuccessGreen
val SeafoamGreenDark      = NeonGreenDark
val SunsetAmber           = WarningAmber
val SunsetAmberDark       = BrandOrange
val WarmWhite             = SurfaceBgTop
val CoolDark              = SurfaceBgTop
val HeroCardLight         = SurfaceCard
val HeroCardDark          = SurfaceCard
val ActionCardLight       = SurfaceCard
val ActionCardDark        = SurfaceCard
val OnWarmWhite           = InkPrimary
val OnCoolDark            = InkPrimary
val CardSurface           = SurfaceCard
val CardBorder            = BorderSubtle
