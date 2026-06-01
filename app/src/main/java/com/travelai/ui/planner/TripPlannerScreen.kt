package com.travelai.ui.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.data.prefs.ThemeMode
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppBottomBar
import com.travelai.ui.components.BentoGrid
import com.travelai.ui.components.BentoSpan
import com.travelai.ui.components.BottomNavDestination
import com.travelai.ui.components.SectionHeader
import com.travelai.ui.components.cascadeReveal
import com.travelai.ui.planner.components.PlannerExploreChip
import com.travelai.ui.planner.components.PlannerHeader
import com.travelai.ui.planner.components.PlannerHeroCard
import com.travelai.ui.planner.components.PlannerInfoCard
import com.travelai.ui.planner.components.PlannerQuickCard
import com.travelai.ui.planner.components.QuickCardSize
import com.travelai.ui.planner.components.ThemeToggleChip
import com.travelai.ui.theme.BrandBlue
import com.travelai.ui.theme.BrandOrange
import com.travelai.ui.theme.BrandPink
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandTeal
import com.travelai.ui.theme.DangerRed
import com.travelai.ui.theme.TravelAITheme

@Composable
fun TripPlannerScreen(
    onOpenChat: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenLandmarkScanner: () -> Unit,
    onOpenWeather: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenItinerary: (Long) -> Unit,
    onCreateItinerary: (Long) -> Unit,
    viewModel: TripPlannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val currentDarkMode = when (themeMode) {
        ThemeMode.SystemDefault -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    LaunchedEffect(uiState.createdSessionId) {
        val sessionId = uiState.createdSessionId ?: return@LaunchedEffect
        onCreateItinerary(sessionId)
        viewModel.consumeCreatedSessionId()
    }

    TripPlannerDashboard(
        uiState = uiState,
        onOpenChat = onOpenChat,
        onOpenHistory = onOpenHistory,
        onOpenMap = onOpenMap,
        onOpenLandmarkScanner = onOpenLandmarkScanner,
        onOpenWeather = onOpenWeather,
        onOpenSettings = onOpenSettings,
        onOpenProfile = onOpenProfile,
        onOpenItinerary = onOpenItinerary,
        isDarkMode = currentDarkMode,
        onToggleDarkMode = { viewModel.setDarkModeEnabled(!currentDarkMode) },
        onCreateTrip = viewModel::createTrip,
        onDestinationChange = viewModel::onDestinationChange,
        onDaysChange = viewModel::onDaysChange,
        onBudgetChange = viewModel::onBudgetChange,
        onPeopleChange = viewModel::onPeopleChange,
        onTravelStyleChange = viewModel::onTravelStyleChange,
        onTransportChange = viewModel::onTransportChange,
        onNoteChange = viewModel::onNoteChange
    )
}

@Composable
private fun TripPlannerDashboard(
    uiState: TripPlannerUiState,
    onOpenChat: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenLandmarkScanner: () -> Unit,
    onOpenWeather: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenItinerary: (Long) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onCreateTrip: () -> Unit,
    onDestinationChange: (String) -> Unit,
    onDaysChange: (String) -> Unit,
    onBudgetChange: (String) -> Unit,
    onPeopleChange: (String) -> Unit,
    onTravelStyleChange: (String) -> Unit,
    onTransportChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    var editingField by remember { mutableStateOf<PlannerEditField?>(null) }

    AppBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                AppBottomBar(
                    selected = BottomNavDestination.Home,
                    onSelected = { destination ->
                        when (destination) {
                            BottomNavDestination.Home -> Unit
                            BottomNavDestination.Chat -> onOpenChat()
                            BottomNavDestination.Itinerary ->
                                uiState.latestSessionId?.let(onOpenItinerary) ?: onOpenHistory()
                            BottomNavDestination.Saved -> onOpenHistory()
                            BottomNavDestination.Profile -> onOpenProfile()
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .statusBarsPadding()
                    .padding(top = 22.dp, bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                PlannerHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .cascadeReveal(0),
                    onProfileClick = onOpenProfile
                )

                PlannerHeroCard(
                    isLoading = uiState.isCreating,
                    onCreateTrip = onCreateTrip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .cascadeReveal(1)
                )

                SectionHeader(
                    title = "Truy cập nhanh",
                    subtitle = "Mở nhanh các tác vụ thường dùng",
                    modifier = Modifier.cascadeReveal(2)
                )

                QuickAccessBento(
                    onOpenChat = onOpenChat,
                    onOpenHistory = onOpenHistory,
                    onOpenMap = onOpenMap,
                    onOpenLandmarkScanner = onOpenLandmarkScanner,
                    onCreateTrip = onCreateTrip,
                    modifier = Modifier.cascadeReveal(3)
                )

                SectionHeader(
                    title = "Thông tin chuyến đi",
                    subtitle = "Tap để chỉnh sửa nhanh",
                    modifier = Modifier.cascadeReveal(4)
                )

                TripInfoBento(
                    uiState = uiState,
                    onEdit = { editingField = it },
                    modifier = Modifier.cascadeReveal(5)
                )

                uiState.errorMessage?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        modifier = Modifier.fillMaxWidth(),
                        color = DangerRed,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                SectionHeader(
                    title = "Khám phá thêm",
                    subtitle = "Các phím tắt cho hành trình của bạn"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PlannerExploreChip(
                        icon = Icons.Filled.WbCloudy,
                        text = "Thời tiết",
                        accent = BrandBlue,
                        onClick = { onOpenWeather(uiState.destination) },
                        modifier = Modifier.weight(1f)
                    )
                    PlannerExploreChip(
                        icon = Icons.Filled.Map,
                        text = "Bản đồ",
                        accent = BrandTeal,
                        onClick = onOpenMap,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeToggleChip(
                        isDarkMode = isDarkMode,
                        accent = BrandPurple,
                        onClick = onToggleDarkMode,
                        modifier = Modifier.weight(1f)
                    )
                    PlannerExploreChip(
                        icon = Icons.Filled.Settings,
                        text = "Cài đặt",
                        accent = BrandOrange,
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(4.dp))
            }
        }
    }

    editingField?.let { field ->
        PlannerEditDialog(
            field = field,
            value = field.valueFrom(uiState),
            onDismiss = { editingField = null },
            onConfirm = { value ->
                when (field) {
                    PlannerEditField.Destination -> onDestinationChange(value)
                    PlannerEditField.Days -> onDaysChange(value)
                    PlannerEditField.Budget -> onBudgetChange(value)
                    PlannerEditField.People -> onPeopleChange(value)
                    PlannerEditField.TravelStyle -> onTravelStyleChange(value)
                    PlannerEditField.Transport -> onTransportChange(value)
                    PlannerEditField.Note -> onNoteChange(value)
                }
                editingField = null
            }
        )
    }
}

@Composable
private fun QuickAccessBento(
    onOpenChat: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenLandmarkScanner: () -> Unit,
    onCreateTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    BentoGrid(modifier = modifier, gap = 10.dp) {
        tile(BentoSpan.FullWidth, 128.dp) {
            PlannerQuickCard(
                icon = Icons.Filled.PhotoCamera,
                title = "Camera AI",
                subtitle = "Nhận diện địa điểm qua ảnh",
                accent = BrandPink,
                onClick = onOpenLandmarkScanner,
                modifier = Modifier.fillMaxSize(),
                size = QuickCardSize.Wide,
                decorativeBlob = BrandPink
            )
        }
        tile(BentoSpan.HalfWidth, 146.dp) {
            PlannerQuickCard(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "Chat AI",
                subtitle = "Hỏi đáp, gợi ý",
                accent = BrandPurple,
                onClick = onOpenChat,
                modifier = Modifier.fillMaxSize()
            )
        }
        tile(BentoSpan.HalfWidth, 146.dp) {
            PlannerQuickCard(
                icon = Icons.Filled.History,
                title = "Lịch sử",
                subtitle = "Trip Library",
                accent = BrandBlue,
                onClick = onOpenHistory,
                modifier = Modifier.fillMaxSize()
            )
        }
        tile(BentoSpan.HalfWidth, 146.dp) {
            PlannerQuickCard(
                icon = Icons.Filled.Route,
                title = "Lịch trình",
                subtitle = "Tạo bằng AI",
                accent = BrandTeal,
                onClick = onCreateTrip,
                modifier = Modifier.fillMaxSize()
            )
        }
        tile(BentoSpan.HalfWidth, 146.dp) {
            PlannerQuickCard(
                icon = Icons.Filled.Map,
                title = "Bản đồ",
                subtitle = "Điểm đến",
                accent = BrandOrange,
                onClick = onOpenMap,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun TripInfoBento(
    uiState: TripPlannerUiState,
    onEdit: (PlannerEditField) -> Unit,
    modifier: Modifier = Modifier
) {
    BentoGrid(modifier = modifier, gap = 12.dp) {
        tile(BentoSpan.HalfWidth, 90.dp) {
            PlannerInfoCard(
                icon = Icons.Filled.LocationOn,
                label = "Điểm đến",
                value = uiState.destination.ifBlank { "Chọn nơi đến" },
                accent = BrandPurple,
                onClick = { onEdit(PlannerEditField.Destination) },
                modifier = Modifier.fillMaxSize()
            )
        }
        tile(BentoSpan.HalfWidth, 90.dp) {
            PlannerInfoCard(
                icon = Icons.Filled.CalendarMonth,
                label = "Số ngày",
                value = "${uiState.days.ifBlank { "0" }} ngày",
                accent = BrandBlue,
                onClick = { onEdit(PlannerEditField.Days) },
                modifier = Modifier.fillMaxSize()
            )
        }
        tile(BentoSpan.HalfWidth, 90.dp) {
            PlannerInfoCard(
                icon = Icons.Filled.Savings,
                label = "Ngân sách",
                value = uiState.budget.ifBlank { "Chưa nhập" },
                accent = BrandTeal,
                onClick = { onEdit(PlannerEditField.Budget) },
                modifier = Modifier.fillMaxSize()
            )
        }
        tile(BentoSpan.HalfWidth, 90.dp) {
            PlannerInfoCard(
                icon = Icons.Filled.Star,
                label = "Phong cách",
                value = uiState.travelStyle.ifBlank { "Tự túc" },
                accent = BrandOrange,
                onClick = { onEdit(PlannerEditField.TravelStyle) },
                modifier = Modifier.fillMaxSize()
            )
        }
        tile(BentoSpan.HalfWidth, 90.dp) {
            PlannerInfoCard(
                icon = Icons.Filled.Person,
                label = "Số người",
                value = "${uiState.people.ifBlank { "0" }} người",
                accent = BrandPurple,
                onClick = { onEdit(PlannerEditField.People) },
                modifier = Modifier.fillMaxSize()
            )
        }
        tile(BentoSpan.HalfWidth, 90.dp) {
            PlannerInfoCard(
                icon = Icons.Filled.DirectionsCar,
                label = "Di chuyển",
                value = uiState.transport.ifBlank { "Linh hoạt" },
                accent = BrandBlue,
                onClick = { onEdit(PlannerEditField.Transport) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PlannerEditDialog(
    field: PlannerEditField,
    value: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var draft by remember(field) { mutableStateOf(value) }
    val colors = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        titleContentColor = colors.onSurface,
        textContentColor = colors.onSurfaceVariant,
        title = {
            Text(
                text = field.title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(field.placeholder) },
                minLines = if (field == PlannerEditField.Note) 3 else 1,
                maxLines = if (field == PlannerEditField.Note) 5 else 1,
                keyboardOptions = KeyboardOptions(keyboardType = field.keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.onSurface,
                    unfocusedTextColor = colors.onSurface,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.outline,
                    cursorColor = colors.primary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(draft) }) {
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

private enum class PlannerEditField(
    val title: String,
    val placeholder: String,
    val keyboardType: KeyboardType = KeyboardType.Text
) {
    Destination("Điểm đến", "Ví dụ: Đà Nẵng"),
    Days("Số ngày", "Ví dụ: 3", KeyboardType.Number),
    Budget("Ngân sách", "Ví dụ: 5 triệu / người"),
    People("Số người", "Ví dụ: 2", KeyboardType.Number),
    TravelStyle("Phong cách", "Ví dụ: Tự túc, cân bằng"),
    Transport("Phương tiện", "Ví dụ: Taxi, xe máy, đi bộ"),
    Note("Ghi chú thêm", "Ví dụ: có trẻ nhỏ, không ăn cay")
}

private fun PlannerEditField.valueFrom(state: TripPlannerUiState): String = when (this) {
    PlannerEditField.Destination -> state.destination
    PlannerEditField.Days -> state.days
    PlannerEditField.Budget -> state.budget
    PlannerEditField.People -> state.people
    PlannerEditField.TravelStyle -> state.travelStyle
    PlannerEditField.Transport -> state.transport
    PlannerEditField.Note -> state.note
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TripPlannerDashboardPreview() {
    TravelAITheme(darkTheme = false) {
        TripPlannerDashboard(
            uiState = TripPlannerUiState(
                destination = "Đà Nẵng",
                days = "2",
                budget = "5 triệu / người",
                people = "2",
                travelStyle = "Tự túc, cân bằng",
                transport = "Taxi, xe máy",
                note = ""
            ),
            onOpenChat = {},
            onOpenHistory = {},
            onOpenMap = {},
            onOpenLandmarkScanner = {},
            onOpenWeather = {},
            onOpenSettings = {},
            onOpenProfile = {},
            onOpenItinerary = {},
            isDarkMode = false,
            onToggleDarkMode = {},
            onCreateTrip = {},
            onDestinationChange = {},
            onDaysChange = {},
            onBudgetChange = {},
            onPeopleChange = {},
            onTravelStyleChange = {},
            onTransportChange = {},
            onNoteChange = {}
        )
    }
}
