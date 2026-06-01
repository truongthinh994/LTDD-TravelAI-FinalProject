package com.travelai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.DarkBgTop
import com.travelai.ui.theme.GlassHighlightDark
import com.travelai.ui.theme.GlassHighlight
import com.travelai.ui.theme.GlassSurfaceDarkL3
import com.travelai.ui.theme.GlassSurfaceL3

/**
 * Compact glass-tinted pill — used as a badge / label / status chip overlaid
 * on top of imagery or aurora gradients.
 *
 * - Background: [GlassSurfaceL3] (50% white) for the frosted feel.
 * - Border: [GlassHighlight] for the soft inner highlight refraction hint.
 * - Tint: drives both the leading icon and the text color.
 */
@Composable
fun GlassPill(
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    tint: Color = BrandPurple
) {
    val darkTheme = MaterialTheme.colorScheme.background == DarkBgTop
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (darkTheme) GlassSurfaceDarkL3 else GlassSurfaceL3)
            .border(1.dp, if (darkTheme) GlassHighlightDark else GlassHighlight, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
