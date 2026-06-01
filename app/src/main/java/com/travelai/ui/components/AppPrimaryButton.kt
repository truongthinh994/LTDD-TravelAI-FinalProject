package com.travelai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.TravelAIGradients
import com.travelai.ui.theme.bentoShadow

/**
 * Primary brand button — pill shape, aurora diagonal gradient (purple→pink→blue),
 * inner top highlight (simulated glass refraction), L4 pink-tinted shadow,
 * haptic feedback on click, and optional [isLoading] state showing a spinner.
 */
@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
) {
    val shape = RoundedCornerShape(28.dp)
    val interactive = enabled && !isLoading
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .then(if (interactive) Modifier.bentoShadow(ShadowLevel.L4, shape) else Modifier)
            .clip(shape)
            .background(TravelAIGradients.auroraDiagonal)
            .clickable(enabled = interactive) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        // Inner top highlight — simulates glass refraction on the gradient surface.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.White.copy(alpha = 0.22f),
                            0.35f to Color.White.copy(alpha = 0.04f),
                            1.0f to Color.Transparent
                        )
                    )
                )
        )

        if (isLoading) {
            CircularProgressIndicator(
                color = OnBrand,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = OnBrand,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    color = OnBrand,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
