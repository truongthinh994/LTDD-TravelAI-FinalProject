package com.travelai.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.BuildConfig
import com.travelai.data.prefs.ThemeMode
import com.travelai.data.prefs.UserPreferences
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.components.SectionHeader
import com.travelai.ui.components.cascadeReveal
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.TravelAITheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()

    SettingsContent(
        prefs = prefs,
        onBack = onBack,
        onThemeModeChange = viewModel::setThemeMode,
        onFontScaleChange = viewModel::setFontScale
    )
}

@Composable
private fun SettingsContent(
    prefs: UserPreferences,
    onBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onFontScaleChange: (Float) -> Unit
) {
    AppBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    title = "Cài đặt",
                    subtitle = "Giao diện và thông tin ứng dụng",
                    onBack = onBack
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionHeader(
                    title = "Giao diện",
                    subtitle = "Chọn chế độ màu của ứng dụng",
                    modifier = Modifier.cascadeReveal(0)
                )
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cascadeReveal(1),
                    cornerRadius = 22,
                    contentPadding = PaddingValues(14.dp),
                    variant = AppCardVariant.Solid,
                    shadowLevel = ShadowLevel.L1
                ) {
                    ThemeModeSelector(
                        selected = prefs.themeMode,
                        onSelected = onThemeModeChange
                    )
                }

                SectionHeader(
                    title = "Hiển thị",
                    subtitle = "Tinh chỉnh cỡ chữ",
                    modifier = Modifier.cascadeReveal(2)
                )
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cascadeReveal(3),
                    cornerRadius = 22,
                    contentPadding = PaddingValues(16.dp),
                    variant = AppCardVariant.Solid,
                    shadowLevel = ShadowLevel.L1
                ) {
                    FontScaleSection(
                        fontScale = prefs.fontScale,
                        onFontScaleChange = onFontScaleChange
                    )
                }

                SectionHeader(
                    title = "Về ứng dụng",
                    modifier = Modifier.cascadeReveal(4)
                )
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cascadeReveal(5),
                    cornerRadius = 22,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    variant = AppCardVariant.Solid,
                    shadowLevel = ShadowLevel.L1
                ) {
                    Column {
                        SettingsInfoRow(label = "Phiên bản", value = BuildConfig.VERSION_NAME)
                        SettingsInfoRow(label = "Bản quyền", value = "Sử dụng cho mục đích học tập")
                        SettingsInfoRow(label = "Liên hệ", value = "travelai@example.com")
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(colors.surfaceVariant.copy(alpha = 0.72f))
            .border(1.dp, colors.outline, CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ThemeSegment(
            mode = ThemeMode.SystemDefault,
            label = "Hệ thống",
            icon = Icons.Filled.PhonelinkSetup,
            selected = selected == ThemeMode.SystemDefault,
            onClick = onSelected,
            modifier = Modifier.weight(1f)
        )
        ThemeSegment(
            mode = ThemeMode.Light,
            label = "Sáng",
            icon = Icons.Filled.LightMode,
            selected = selected == ThemeMode.Light,
            onClick = onSelected,
            modifier = Modifier.weight(1f)
        )
        ThemeSegment(
            mode = ThemeMode.Dark,
            label = "Tối",
            icon = Icons.Filled.DarkMode,
            selected = selected == ThemeMode.Dark,
            onClick = onSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeSegment(
    mode: ThemeMode,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = tween(180),
        label = "themeSegmentContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        animationSpec = tween(180),
        label = "themeSegmentContent"
    )

    Row(
        modifier = modifier
            .height(42.dp)
            .clip(CircleShape)
            .background(container)
            .clickable { onClick(mode) }
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun FontScaleSection(
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Cỡ chữ",
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(fontScale * 100).toInt()}%",
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Slider(
            value = fontScale,
            onValueChange = onFontScaleChange,
            valueRange = 0.85f..1.30f,
            steps = 8
        )
        Text(
            text = "Cỡ chữ được lưu trên máy này và áp dụng ngay cho toàn bộ ứng dụng.",
            color = colors.onSurfaceVariant.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SettingsInfoRow(
    label: String,
    value: String
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsContentPreview() {
    TravelAITheme(darkTheme = false) {
        SettingsContent(
            prefs = UserPreferences(themeMode = ThemeMode.SystemDefault),
            onBack = {},
            onThemeModeChange = {},
            onFontScaleChange = {}
        )
    }
}
