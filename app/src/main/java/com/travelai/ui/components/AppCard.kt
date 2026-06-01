package com.travelai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.travelai.ui.theme.BorderSubtle
import com.travelai.ui.theme.BrandPurpleTint
import com.travelai.ui.theme.DarkBgTop
import com.travelai.ui.theme.GlassHighlightDark
import com.travelai.ui.theme.GlassHighlight
import com.travelai.ui.theme.GlassSurfaceDarkL2
import com.travelai.ui.theme.GlassSurfaceL2
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.SurfaceCard
import com.travelai.ui.theme.bentoShadow

/**
 * Variant of the shared elevated card.
 *
 * - [Solid]: classic opaque card surface (default — backwards compat).
 * - [Glass]: semi-transparent frosted look. Lets the mesh background show
 *   through with a soft inner top-edge highlight to suggest refraction.
 *   This is the *simulated glass* path — a true backdrop blur via
 *   `RenderEffect.createBlurEffect` requires snapshotting the parent, which
 *   we deferred until wave 3 due to perf cost on lower-end devices.
 * - [Tinted]: muted purple-tint container for grouped subordinate content.
 */
enum class AppCardVariant { Solid, Glass, Tinted }

/**
 * Shared elevated card.
 *
 * The legacy signature (no `variant`, no `shadowLevel`) is preserved so every
 * existing call site keeps compiling. New code should pass a variant +
 * shadow level rather than poking [container] / [border] directly.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 20,
    elevation: Int = 6,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    container: Color = SurfaceCard,
    border: Color = BorderSubtle,
    variant: AppCardVariant = AppCardVariant.Solid,
    shadowLevel: ShadowLevel? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    val colors = MaterialTheme.colorScheme
    val darkTheme = colors.background == DarkBgTop
    val solidContainer = if (container == SurfaceCard) colors.surface.copy(alpha = 0.96f) else container
    val solidBorder = if (border == BorderSubtle) colors.outline else border

    val resolvedContainer = when (variant) {
        AppCardVariant.Solid -> solidContainer
        AppCardVariant.Glass -> if (darkTheme) GlassSurfaceDarkL2 else GlassSurfaceL2
        AppCardVariant.Tinted -> if (darkTheme) colors.surfaceVariant.copy(alpha = 0.78f) else BrandPurpleTint
    }
    val resolvedBorder = when (variant) {
        AppCardVariant.Solid -> solidBorder
        AppCardVariant.Glass -> if (darkTheme) GlassHighlightDark else GlassHighlight
        AppCardVariant.Tinted -> solidBorder
    }
    // Pick a default shadow level per variant when caller hasn't set one.
    val resolvedShadow = shadowLevel ?: when (variant) {
        AppCardVariant.Solid -> ShadowLevel.L2
        AppCardVariant.Glass -> ShadowLevel.L2
        AppCardVariant.Tinted -> ShadowLevel.L1
    }

    Box(
        modifier = modifier
            .bentoShadow(resolvedShadow, shape)
            .clip(shape)
            .background(resolvedContainer)
            .border(1.dp, resolvedBorder, shape)
            .padding(contentPadding)
    ) {
        content()
    }
}
