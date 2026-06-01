package com.travelai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.GlassHighlight
import com.travelai.ui.theme.GlassSurfaceL3
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.TravelAIGradients

/**
 * Pill-shaped chip dùng cho category/filter rows.
 *
 * - Khi [selected] = true: nền aurora gradient + chữ [OnBrand].
 * - Khi [selected] = false: glass surface mỏng với viền highlight.
 */
@Composable
fun CategoryChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    selectedColor: Color = BrandPurple
) {
    val shape = RoundedCornerShape(100)
    val textColor = if (selected) OnBrand else selectedColor.copy(alpha = 0.88f)
    val borderColor = if (selected) Color.White.copy(alpha = 0.38f) else GlassHighlight

    Surface(
        onClick = onClick ?: {},
        modifier = if (selected) {
            modifier.clip(shape).background(TravelAIGradients.auroraDiagonal)
        } else {
            modifier.clip(shape).background(GlassSurfaceL3)
        },
        shape = shape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
            color = textColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
