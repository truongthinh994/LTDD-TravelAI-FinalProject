package com.travelai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.travelai.ui.theme.DarkBgTop
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.GlassHighlightDark
import com.travelai.ui.theme.GlassHighlight
import com.travelai.ui.theme.GlassSurfaceDarkL1
import com.travelai.ui.theme.GlassSurfaceL1
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.bentoShadow

/**
 * Top-bar surface mode.
 *
 * [Transparent] is for full-bleed screens such as maps, [Glass] is the default
 * frosted app chrome, and [Solid] is kept for dense screens/dialog-like flows.
 */
enum class TopBarVariant { Solid, Glass, Transparent }

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    variant: TopBarVariant = TopBarVariant.Glass,
    onScrollOffset: Float = 1f,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val clampedOffset = onScrollOffset.coerceIn(0f, 1f)
    val colors = MaterialTheme.colorScheme
    val darkTheme = colors.background == DarkBgTop
    val surface = when (variant) {
        TopBarVariant.Solid -> colors.surface.copy(alpha = 0.96f)
        TopBarVariant.Glass -> if (darkTheme) {
            GlassSurfaceDarkL1.copy(alpha = 0.70f + 0.18f * clampedOffset)
        } else {
            GlassSurfaceL1.copy(alpha = 0.58f + 0.22f * clampedOffset)
        }
        TopBarVariant.Transparent -> Color.Transparent
    }
    val border = when (variant) {
        TopBarVariant.Transparent -> Color.Transparent
        TopBarVariant.Glass -> if (darkTheme) GlassHighlightDark else GlassHighlight
        TopBarVariant.Solid -> colors.outline
    }
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .then(
                if (variant == TopBarVariant.Transparent) Modifier
                else Modifier.bentoShadow(ShadowLevel.L1, shape)
            )
            .clip(shape)
            .background(surface)
            .border(1.dp, border, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            TopBarIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = if (onBack != null) 12.dp else 0.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = actions
        )
    }
}

@Composable
fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tinted: Boolean = false,
    size: Dp = 40.dp,
    badge: Int? = null
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(if (tinted) colors.primaryContainer else colors.surface.copy(alpha = 0.78f))
                .border(1.dp, if (tinted) colors.primary.copy(alpha = 0.18f) else colors.outline, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (tinted) colors.onPrimaryContainer else colors.onSurface,
                modifier = Modifier.size(size * 0.5f)
            )
        }
        badge?.takeIf { it > 0 }?.let { count ->
            BadgeBubble(count = count)
        }
    }
}

@Composable
private fun BoxScope.BadgeBubble(count: Int) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .size(18.dp)
            .clip(CircleShape)
            .background(BrandPurple)
            .border(1.dp, colors.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = count.coerceAtMost(99).toString(),
            color = OnBrand,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 8.dp)
        )
    }
}
