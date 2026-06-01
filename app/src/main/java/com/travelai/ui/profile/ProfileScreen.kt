package com.travelai.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.BuildConfig
import com.travelai.data.prefs.AvatarStyle
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.components.BentoGrid
import com.travelai.ui.components.BentoSpan
import com.travelai.ui.components.cascadeReveal
import com.travelai.ui.planner.components.PlannerExploreChip
import com.travelai.ui.theme.BrandBlue
import com.travelai.ui.theme.BrandOrange
import com.travelai.ui.theme.BrandPink
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleStrong
import com.travelai.ui.theme.BrandTeal
import com.travelai.ui.theme.MintFresh
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.SunshineAmber
import com.travelai.ui.theme.TravelAITheme

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        uiState = uiState,
        onBack = onBack,
        onOpenSettings = onOpenSettings,
        onStartEditingProfile = viewModel::startEditingProfile,
        onDismissProfileEditor = viewModel::dismissProfileEditor,
        onEditDraftChange = viewModel::onEditDraftChange,
        onEditAvatarStyleChange = viewModel::onEditAvatarStyleChange,
        onSaveProfile = viewModel::saveProfile
    )
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartEditingProfile: () -> Unit,
    onDismissProfileEditor: () -> Unit,
    onEditDraftChange: (String) -> Unit,
    onEditAvatarStyleChange: (AvatarStyle) -> Unit,
    onSaveProfile: () -> Unit
) {
    var showAbout by remember { mutableStateOf(false) }

    AppBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    title = "Hồ sơ",
                    subtitle = "Thông tin cá nhân và thống kê",
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
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                ProfileAvatarCard(
                    displayName = uiState.displayName.ifBlank { "Du khách TravelAI" },
                    avatarStyle = uiState.avatarStyle,
                    onEdit = onStartEditingProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .cascadeReveal(0)
                )

                BentoGrid(
                    modifier = Modifier.cascadeReveal(1),
                    gap = 12.dp
                ) {
                    tile(BentoSpan.HalfWidth, 110.dp) {
                        ProfileStatTile(
                            icon = Icons.Filled.Map,
                            label = "Chuyến đi",
                            value = uiState.tripCount.toString(),
                            accent = BrandPurple,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    tile(BentoSpan.HalfWidth, 110.dp) {
                        ProfileStatTile(
                            icon = Icons.Filled.PhotoCamera,
                            label = "Camera AI",
                            value = uiState.scanCount.toString(),
                            accent = BrandPink,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cascadeReveal(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlannerExploreChip(
                        icon = Icons.Filled.Settings,
                        text = "Cài đặt",
                        accent = BrandPurple,
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxWidth()
                    )
                    PlannerExploreChip(
                        icon = Icons.Filled.Info,
                        text = "Về ứng dụng",
                        accent = BrandPink,
                        onClick = { showAbout = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (uiState.isEditingProfile) {
        EditProfileDialog(
            draft = uiState.editDraft,
            selectedAvatarStyle = uiState.editAvatarStyle,
            onDraftChange = onEditDraftChange,
            onAvatarStyleChange = onEditAvatarStyleChange,
            onDismiss = onDismissProfileEditor,
            onConfirm = onSaveProfile
        )
    }

    if (showAbout) {
        AboutDialog(onDismiss = { showAbout = false })
    }
}

@Composable
private fun ProfileAvatarCard(
    displayName: String,
    avatarStyle: AvatarStyle,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier.height(200.dp),
        cornerRadius = 24,
        contentPadding = PaddingValues(18.dp),
        variant = AppCardVariant.Solid,
        shadowLevel = ShadowLevel.L3
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(avatarStyle.brush())
                    .border(2.dp, Color.White.copy(alpha = 0.58f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = OnBrand,
                    modifier = Modifier.size(66.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = displayName,
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Sửa hồ sơ",
                        tint = colors.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatTile(
    icon: ImageVector,
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    AppCard(
        modifier = modifier,
        cornerRadius = 18,
        contentPadding = PaddingValues(12.dp),
        variant = AppCardVariant.Solid,
        shadowLevel = ShadowLevel.L1
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = label,
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = value,
                    color = colors.onSurface,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    draft: String,
    selectedAvatarStyle: AvatarStyle,
    onDraftChange: (String) -> Unit,
    onAvatarStyleChange: (AvatarStyle) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurfaceVariant,
        title = {
            Text("Hồ sơ của bạn", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Ví dụ: Nam") },
                    label = { Text("Tên hiển thị") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.onSurface,
                        unfocusedTextColor = colors.onSurface,
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        focusedLabelColor = colors.primary,
                        cursorColor = colors.primary,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    )
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Avatar",
                        color = colors.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    AvatarStyleRows(
                        selected = selectedAvatarStyle,
                        onSelected = onAvatarStyleChange
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Lưu", color = colors.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = colors.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun AvatarStyleRows(
    selected: AvatarStyle,
    onSelected: (AvatarStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AvatarStyleOptions.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { style ->
                    AvatarStyleChip(
                        style = style,
                        selected = selected == style,
                        onClick = { onSelected(style) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarStyleChip(
    style: AvatarStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(if (selected) colors.primaryContainer else colors.surfaceVariant.copy(alpha = 0.64f))
            .border(1.dp, if (selected) colors.primary.copy(alpha = 0.38f) else colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(style.brush())
                .border(1.dp, Color.White.copy(alpha = 0.46f), CircleShape)
        )
        Text(
            text = style.label,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurfaceVariant,
        title = {
            Text("Về TravelAI", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Phiên bản ${BuildConfig.VERSION_NAME}")
                Text("TravelAI - đồ án LTDD")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = colors.primary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

private val AvatarStyleOptions = listOf(
    AvatarStyle.Aurora,
    AvatarStyle.Ocean,
    AvatarStyle.Sunset,
    AvatarStyle.Mint
)

private val AvatarStyle.label: String
    get() = when (this) {
        AvatarStyle.Aurora -> "Aurora"
        AvatarStyle.Ocean -> "Ocean"
        AvatarStyle.Sunset -> "Sunset"
        AvatarStyle.Mint -> "Mint"
    }

private fun AvatarStyle.brush(): Brush = when (this) {
    AvatarStyle.Aurora -> Brush.linearGradient(listOf(BrandPurpleStrong, BrandPink, BrandBlue))
    AvatarStyle.Ocean -> Brush.linearGradient(listOf(BrandBlue, BrandTeal))
    AvatarStyle.Sunset -> Brush.linearGradient(listOf(SunshineAmber, BrandOrange, BrandPink))
    AvatarStyle.Mint -> Brush.linearGradient(listOf(MintFresh, BrandTeal, BrandBlue))
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProfileContentPreview() {
    TravelAITheme(darkTheme = false) {
        ProfileContent(
            uiState = ProfileUiState(
                displayName = "Nam",
                avatarStyle = AvatarStyle.Aurora,
                tripCount = 8,
                scanCount = 3
            ),
            onBack = {},
            onOpenSettings = {},
            onStartEditingProfile = {},
            onDismissProfileEditor = {},
            onEditDraftChange = {},
            onEditAvatarStyleChange = {},
            onSaveProfile = {}
        )
    }
}
