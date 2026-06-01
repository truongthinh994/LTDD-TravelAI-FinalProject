package com.travelai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleSoft
import com.travelai.ui.theme.DarkBgTop
import com.travelai.ui.theme.GlassHighlightDark
import com.travelai.ui.theme.GlassHighlight
import com.travelai.ui.theme.GlassSurfaceDarkL1
import com.travelai.ui.theme.GlassSurfaceL1
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.TravelAIGradients
import com.travelai.ui.theme.bentoShadow

/**
 * Bottom-nav destinations shared by all main screens.
 */
sealed class BottomNavDestination(
    val label: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector
) {
    data object Home : BottomNavDestination(
        label = "Home",
        outlinedIcon = Icons.Outlined.Home,
        filledIcon = Icons.Outlined.Home
    )
    data object Chat : BottomNavDestination(
        label = "Chat",
        outlinedIcon = Icons.Outlined.Chat,
        filledIcon = Icons.Outlined.Chat
    )
    data object Itinerary : BottomNavDestination(
        label = "Itinerary",
        outlinedIcon = Icons.Outlined.Explore,
        filledIcon = Icons.Filled.Map
    )
    data object Saved : BottomNavDestination(
        label = "Saved",
        outlinedIcon = Icons.Outlined.BookmarkBorder,
        filledIcon = Icons.Filled.Bookmark
    )
    data object Profile : BottomNavDestination(
        label = "Profile",
        outlinedIcon = Icons.Outlined.PersonOutline,
        filledIcon = Icons.Filled.Person
    )
}

/**
 * Floating glass dock. The center Itinerary action is lifted as the primary
 * route because it is the user's main travel-planning artifact.
 */
@Composable
fun AppBottomBar(
    selected: BottomNavDestination,
    onSelected: (BottomNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    destinations: List<BottomNavDestination> = DefaultBottomDestinations
) {
    val colors = MaterialTheme.colorScheme
    val darkTheme = colors.background == DarkBgTop
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        val dockShape = RoundedCornerShape(30.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .bentoShadow(ShadowLevel.L5, dockShape)
                .clip(dockShape)
                .background(if (darkTheme) GlassSurfaceDarkL1.copy(alpha = 0.86f) else GlassSurfaceL1)
                .border(1.dp, if (darkTheme) GlassHighlightDark else GlassHighlight, dockShape)
                .padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEachIndexed { index, destination ->
                BottomNavItem(
                    destination = destination,
                    isSelected = destination == selected,
                    isCenter = index == destinations.lastIndex / 2,
                    onClick = { onSelected(destination) },
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    destination: BottomNavDestination,
    isSelected: Boolean,
    isCenter: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val selectedContainer = if (colors.background == DarkBgTop) {
        BrandPurple.copy(alpha = 0.22f)
    } else {
        BrandPurpleSoft
    }
    val pillWidth by animateDpAsState(
        targetValue = if (isSelected) 128.dp else 44.dp,
        animationSpec = tween(220),
        label = "pillWidth"
    )
    val container by animateColorAsState(
        targetValue = if (isSelected && !isCenter) selectedContainer else Color.Transparent,
        animationSpec = tween(220),
        label = "pillBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isCenter) OnBrand else if (isSelected) colors.primary else colors.onSurfaceVariant,
        animationSpec = tween(220),
        label = "pillFg"
    )
    val interactionSource = remember { MutableInteractionSource() }
    val shape = CircleShape

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(44.dp)
                .width(pillWidth)
                .then(if (isCenter) Modifier.offset(y = (-6).dp) else Modifier)
                .then(if (isCenter) Modifier.bentoShadow(ShadowLevel.L4, shape) else Modifier)
                .clip(shape)
                .then(
                    if (isCenter) Modifier.background(TravelAIGradients.auroraDiagonal)
                    else Modifier.background(container)
                )
                .then(
                    if (isCenter) Modifier.border(1.dp, Color.White.copy(alpha = 0.38f), shape)
                    else Modifier
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isSelected) Arrangement.Start else Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) destination.filledIcon else destination.outlinedIcon,
                contentDescription = destination.label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
            if (isSelected) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = destination.label,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

private val DefaultBottomDestinations: List<BottomNavDestination> = listOf(
    BottomNavDestination.Home,
    BottomNavDestination.Chat,
    BottomNavDestination.Itinerary,
    BottomNavDestination.Saved,
    BottomNavDestination.Profile
)
