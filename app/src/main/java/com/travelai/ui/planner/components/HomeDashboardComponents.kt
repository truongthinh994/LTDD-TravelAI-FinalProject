package com.travelai.ui.planner.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.travelai.R
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.components.AppPrimaryButton
import com.travelai.ui.components.GlassPill
import com.travelai.ui.components.decorativeBlob
import com.travelai.ui.theme.BentoShapes
import com.travelai.ui.theme.BrandBlue
import com.travelai.ui.theme.BrandPink
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleSoft
import com.travelai.ui.theme.DarkBgTop
import com.travelai.ui.theme.GlassHighlightDark
import com.travelai.ui.theme.GlassSurfaceDarkL3
import com.travelai.ui.theme.GlassSurfaceL3
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.TravelAIGradients
import com.travelai.ui.theme.bentoShadow

enum class QuickCardSize(val heightDp: Int) {
    Small(146),
    Medium(180),
    Wide(128)
}

@Composable
fun PlannerHeader(
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val darkTheme = colors.background == DarkBgTop
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = buildAnnotatedString {
                    append("Travel")
                    withStyle(SpanStyle(color = BrandPurple)) {
                        append("AI")
                    }
                },
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 42.sp,
                    lineHeight = 46.sp,
                    letterSpacing = 0.sp
                ),
                color = colors.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Lên kế hoạch chuyến đi thông minh",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
            GlassPill(
                text = "Đà Nẵng • 28°C",
                leadingIcon = Icons.Filled.AutoAwesome,
                tint = BrandBlue
            )
        }
        Box(
            modifier = Modifier
                .size(58.dp)
                .bentoShadow(ShadowLevel.L3, CircleShape)
                .clip(CircleShape)
                .background(if (darkTheme) GlassSurfaceDarkL3 else GlassSurfaceL3)
                .border(
                    1.dp,
                    if (darkTheme) GlassHighlightDark else Color.White.copy(alpha = 0.72f),
                    CircleShape
                )
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TravelAIGradients.auroraDiagonal),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Mở hồ sơ",
                    tint = OnBrand,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun PlannerHeroCard(
    isLoading: Boolean,
    onCreateTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = BentoShapes.hero
    Box(
        modifier = modifier
            .height(286.dp)
            .bentoShadow(ShadowLevel.L3, shape)
            .clip(shape)
            .background(TravelAIGradients.auroraDiagonal)
            .border(1.dp, Color.White.copy(alpha = 0.42f), shape)
    ) {
        Image(
            painter = painterResource(R.drawable.hero_planner_main),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .background(TravelAIGradients.glassTintWash)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = 0.34f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            GlassPill(
                text = "AI gợi ý",
                leadingIcon = Icons.Filled.AutoAwesome,
                tint = BrandPurple
            )

            Column {
                Text(
                    text = "Tạo chuyến đi\nhoàn hảo",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 38.sp,
                        lineHeight = 44.sp,
                        letterSpacing = 0.sp
                    ),
                    color = Color(0xFF07132F),
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Nhập vài thông tin, AI sẽ lên lịch trình chi tiết dành riêng cho bạn.",
                    color = Color(0xFF53647F),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(18.dp))
                // Subtle breathing pulse on the primary CTA draws the eye
                // without being noisy. Disabled while loading (no double-pulse
                // with the spinner) and on a 3 s slow tween so it feels organic.
                val pulse = rememberInfiniteTransition(label = "heroCtaPulse")
                val pulseScale by pulse.animateFloat(
                    initialValue = 1.0f,
                    targetValue = if (isLoading) 1.0f else 1.02f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "heroCtaPulseScale"
                )
                AppPrimaryButton(
                    text = if (isLoading) "Đang tạo..." else "Tạo lịch trình",
                    onClick = onCreateTrip,
                    leadingIcon = Icons.Filled.AutoAwesome,
                    enabled = !isLoading,
                    isLoading = isLoading,
                    modifier = Modifier
                        .width(212.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                )
            }
        }
    }
}

@Composable
fun PlannerQuickCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: QuickCardSize = QuickCardSize.Small,
    decorativeBlob: Color? = accent
) {
    val colors = MaterialTheme.colorScheme
    val darkTheme = colors.background == DarkBgTop
    AppCard(
        modifier = modifier
            .height(size.heightDp.dp)
            .clickable(onClick = onClick)
            .then(if (decorativeBlob != null) Modifier.decorativeBlob(decorativeBlob) else Modifier),
        cornerRadius = if (size == QuickCardSize.Wide) 22 else 20,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        variant = AppCardVariant.Tinted,
        shadowLevel = if (size == QuickCardSize.Wide) ShadowLevel.L3 else ShadowLevel.L2
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(if (size == QuickCardSize.Wide) 52.dp else 48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(accent.copy(alpha = if (darkTheme) 0.22f else 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PlannerInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    AppCard(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        cornerRadius = 18,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
        variant = AppCardVariant.Solid,
        shadowLevel = ShadowLevel.L1
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(accent.copy(alpha = 0.92f), accent.copy(alpha = 0.62f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OnBrand,
                    modifier = Modifier.size(27.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = colors.onSurface.copy(alpha = 0.78f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PlannerExploreChip(
    icon: ImageVector,
    text: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(62.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface.copy(alpha = 0.86f))
            .border(1.dp, colors.outline, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            color = colors.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ThemeToggleChip(
    isDarkMode: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val darkTheme = colors.background == DarkBgTop
    val shape = RoundedCornerShape(18.dp)
    val selectedContainer = if (darkTheme) BrandPurple.copy(alpha = 0.24f) else BrandPurpleSoft
    Row(
        modifier = modifier
            .height(62.dp)
            .clip(shape)
            .background(if (isDarkMode) selectedContainer else colors.surface.copy(alpha = 0.86f))
            .border(
                width = 1.dp,
                color = if (isDarkMode) BrandPurple.copy(alpha = 0.38f) else colors.outline,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isDarkMode) Icons.Filled.DarkMode else Icons.Filled.LightMode,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(30.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Chế độ tối",
                color = colors.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isDarkMode) "Đang bật" else "Đang tắt",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
