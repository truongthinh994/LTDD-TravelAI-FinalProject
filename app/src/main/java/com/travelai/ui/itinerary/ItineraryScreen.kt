package com.travelai.ui.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.data.model.BudgetCategory
import com.travelai.data.model.BudgetItem
import com.travelai.data.model.ChecklistItem
import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.TripPlanPeriod
import com.travelai.data.model.TripPlanPeriodType
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppBottomBar
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.components.BottomNavDestination
import com.travelai.ui.components.EmptyStateCard
import com.travelai.ui.components.TopBarIconButton
import com.travelai.ui.itinerary.components.WeatherCard
import com.travelai.ui.share.shareTripPdf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.PictureAsPdf
import android.widget.Toast
import com.travelai.ui.theme.BorderSubtle
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleSoft
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.SurfaceCard
import com.travelai.ui.theme.TravelAIGradients
import com.travelai.ui.theme.TravelAITheme
import com.travelai.ui.theme.bentoShadow

@Composable
fun ItineraryScreen(
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit,
    onOpenMap: (Long) -> Unit,
    onOpenPlanner: () -> Unit = onBack,
    onOpenHistory: () -> Unit = onBack,
    onOpenProfile: () -> Unit = onBack,
    viewModel: ItineraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    LaunchedEffect(uiState.pdfFileUri) {
        uiState.pdfFileUri?.let { uri ->
            shareTripPdf(context, uri)
            viewModel.consumePdfFileUri()
        }
    }
    LaunchedEffect(uiState.pdfErrorMessage) {
        uiState.pdfErrorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumePdfError()
        }
    }

    AppBackground {
        ItineraryScreenContent(
            uiState = uiState,
            onBack = onBack,
            onOpenChat = onOpenChat,
            onOpenMap = onOpenMap,
            onOpenPlanner = onOpenPlanner,
            onOpenHistory = onOpenHistory,
            onOpenProfile = onOpenProfile,
            onExportPdf = viewModel::onExportPdf,
            onBudgetCategoryChange = viewModel::onBudgetCategoryChange,
            onBudgetTitleChange = viewModel::onBudgetTitleChange,
            onBudgetAmountChange = viewModel::onBudgetAmountChange,
            onBudgetNoteChange = viewModel::onBudgetNoteChange,
            onSaveBudgetItem = viewModel::saveBudgetItem,
            onEditBudgetItem = viewModel::editBudgetItem,
            onDeleteBudgetItem = viewModel::deleteBudgetItem,
            onCancelBudgetEdit = viewModel::cancelBudgetEdit,
            onChecklistTitleChange = viewModel::onChecklistTitleChange,
            onAddChecklistItem = viewModel::addChecklistItem,
            onGenerateSmartChecklist = viewModel::generateSmartChecklist,
            onToggleChecklistItem = viewModel::toggleChecklistItem,
            onDeleteChecklistItem = viewModel::deleteChecklistItem,
            onOpenDayEditor = viewModel::openDayEditor,
            onDismissDayEditor = viewModel::dismissDayEditor,
            onDayEditorTitleChange = viewModel::onDayEditorTitleChange,
            onDayEditorPeriodChange = viewModel::onDayEditorPeriodChange,
            onSaveDayEditor = viewModel::saveDayEditor,
            onRegenerateDay = viewModel::regenerateDay
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryScreenContent(
    uiState: ItineraryUiState,
    onBack: () -> Unit,
    onOpenChat: (Long) -> Unit,
    onOpenMap: (Long) -> Unit,
    onOpenPlanner: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onExportPdf: () -> Unit = {},
    onBudgetCategoryChange: (BudgetCategory) -> Unit = {},
    onBudgetTitleChange: (String) -> Unit = {},
    onBudgetAmountChange: (String) -> Unit = {},
    onBudgetNoteChange: (String) -> Unit = {},
    onSaveBudgetItem: () -> Unit = {},
    onEditBudgetItem: (BudgetItem) -> Unit = {},
    onDeleteBudgetItem: (BudgetItem) -> Unit = {},
    onCancelBudgetEdit: () -> Unit = {},
    onChecklistTitleChange: (String) -> Unit = {},
    onAddChecklistItem: () -> Unit = {},
    onGenerateSmartChecklist: () -> Unit = {},
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit = { _, _ -> },
    onDeleteChecklistItem: (ChecklistItem) -> Unit = {},
    onOpenDayEditor: (TripPlanDay) -> Unit = {},
    onDismissDayEditor: () -> Unit = {},
    onDayEditorTitleChange: (String) -> Unit = {},
    onDayEditorPeriodChange: (TripPlanPeriodType, String) -> Unit = { _, _ -> },
    onSaveDayEditor: () -> Unit = {},
    onRegenerateDay: (Int) -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = uiState.title.ifBlank { "Lịch trình" },
                subtitle = "Lịch trình chi tiết",
                onBack = onBack,
                actions = {
                    if (uiState.sessionId != null) {
                        TopBarIconButton(
                            icon = Icons.Filled.PictureAsPdf,
                            contentDescription = if (uiState.isExportingPdf) {
                                "Đang xuất PDF"
                            } else {
                                "Xuất PDF"
                            },
                            onClick = onExportPdf,
                            tinted = true
                        )
                        TopBarIconButton(
                            icon = Icons.Filled.Map,
                            contentDescription = "Mở bản đồ",
                            onClick = { uiState.sessionId.let(onOpenMap) },
                            tinted = true
                        )
                        TopBarIconButton(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Mở trò chuyện",
                            onClick = { uiState.sessionId.let(onOpenChat) },
                            tinted = true
                        )
                    }
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                selected = BottomNavDestination.Itinerary,
                onSelected = { dest ->
                    when (dest) {
                        BottomNavDestination.Home -> onOpenPlanner()
                        BottomNavDestination.Chat ->
                            uiState.sessionId?.let(onOpenChat) ?: onOpenPlanner()
                        BottomNavDestination.Itinerary -> Unit
                        BottomNavDestination.Saved -> onOpenHistory()
                        BottomNavDestination.Profile -> onOpenProfile()
                    }
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> CenterContent(modifier = Modifier.padding(innerPadding)) {
                LoadingItineraryState()
            }

            uiState.errorMessage != null -> CenterContent(modifier = Modifier.padding(innerPadding)) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }

            uiState.days.isNotEmpty() -> ParsedItineraryContent(
                days = uiState.days,
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onGenerateSmartChecklist = onGenerateSmartChecklist,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem,
                onOpenDayEditor = onOpenDayEditor,
                onRegenerateDay = onRegenerateDay,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            uiState.rawText.isNotBlank() -> RawItineraryContent(
                rawText = uiState.rawText,
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onGenerateSmartChecklist = onGenerateSmartChecklist,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )

            else -> EmptyItineraryContent(
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onGenerateSmartChecklist = onGenerateSmartChecklist,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }

    uiState.dayEditor?.let { editor ->
        DayEditorDialog(
            editor = editor,
            isSaving = uiState.isSavingItineraryEdit,
            errorMessage = uiState.itineraryEditErrorMessage,
            onTitleChange = onDayEditorTitleChange,
            onPeriodChange = onDayEditorPeriodChange,
            onDismiss = onDismissDayEditor,
            onSave = onSaveDayEditor
        )
    }
}

@Composable
private fun ParsedItineraryContent(
    days: List<TripPlanDay>,
    uiState: ItineraryUiState,
    onBudgetCategoryChange: (BudgetCategory) -> Unit,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetNoteChange: (String) -> Unit,
    onSaveBudgetItem: () -> Unit,
    onEditBudgetItem: (BudgetItem) -> Unit,
    onDeleteBudgetItem: (BudgetItem) -> Unit,
    onCancelBudgetEdit: () -> Unit,
    onChecklistTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onGenerateSmartChecklist: () -> Unit,
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit,
    onDeleteChecklistItem: (ChecklistItem) -> Unit,
    onOpenDayEditor: (TripPlanDay) -> Unit,
    onRegenerateDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(days.size) {
        if (selectedDayIndex > days.lastIndex) {
            selectedDayIndex = 0
        }
    }

    val selectedIndex = selectedDayIndex.coerceIn(0, days.lastIndex)
    val selectedDay = days[selectedIndex]

    Column(modifier = modifier) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.Transparent,
            edgePadding = 16.dp,
            divider = {}
        ) {
            days.forEachIndexed { index, day ->
                val isSelected = selectedIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedDayIndex = index },
                    text = {
                        val tabShape = RoundedCornerShape(100)
                        Box(
                            modifier = Modifier
                                .clip(tabShape)
                                .then(
                                    if (isSelected) Modifier.background(TravelAIGradients.auroraDiagonal)
                                    else Modifier.background(SurfaceCard).border(1.dp, BorderSubtle, tabShape)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Ngày ${day.dayNumber}",
                                color = if (isSelected) OnBrand else InkSecondary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.weatherDays.isNotEmpty()) {
                item(key = "weather-forecast") {
                    WeatherCard(
                        days = uiState.weatherDays,
                        destination = uiState.weatherDestination
                    )
                }
            }
            item(key = "day-header-${selectedDay.dayNumber}") {
                DayHeader(
                    day = selectedDay,
                    isRegenerating = uiState.isRegeneratingDayNumber == selectedDay.dayNumber,
                    onEdit = { onOpenDayEditor(selectedDay) },
                    onRegenerate = { onRegenerateDay(selectedDay.dayNumber) }
                )
            }
            uiState.itineraryEditErrorMessage?.let { message ->
                item(key = "itinerary-edit-error") {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            items(
                items = selectedDay.periods,
                key = { period -> period.period.name }
            ) { period ->
                PeriodCard(period = period)
            }
            item(key = "trip-planning-sections") {
                TripPlanningSections(
                    uiState = uiState,
                    onBudgetCategoryChange = onBudgetCategoryChange,
                    onBudgetTitleChange = onBudgetTitleChange,
                    onBudgetAmountChange = onBudgetAmountChange,
                    onBudgetNoteChange = onBudgetNoteChange,
                    onSaveBudgetItem = onSaveBudgetItem,
                    onEditBudgetItem = onEditBudgetItem,
                    onDeleteBudgetItem = onDeleteBudgetItem,
                    onCancelBudgetEdit = onCancelBudgetEdit,
                    onChecklistTitleChange = onChecklistTitleChange,
                    onAddChecklistItem = onAddChecklistItem,
                    onGenerateSmartChecklist = onGenerateSmartChecklist,
                    onToggleChecklistItem = onToggleChecklistItem,
                    onDeleteChecklistItem = onDeleteChecklistItem
                )
            }
        }
    }
}

@Composable
private fun DayHeader(
    day: TripPlanDay,
    isRegenerating: Boolean,
    onEdit: () -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .bentoShadow(ShadowLevel.L3, shape)
            .clip(shape)
            .background(TravelAIGradients.auroraDiagonal)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Ngày ${day.dayNumber}",
                    color = OnBrand,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                if (day.title.isNotBlank()) {
                    Text(
                        text = day.title,
                        color = OnBrand.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TopBarIconButton(
                    icon = Icons.Filled.Edit,
                    contentDescription = "Sửa ngày",
                    onClick = onEdit,
                    tinted = false
                )
                TopBarIconButton(
                    icon = Icons.Filled.Refresh,
                    contentDescription = if (isRegenerating) "Đang tạo lại" else "Tạo lại ngày này",
                    onClick = onRegenerate,
                    tinted = false
                )
            }
        }
    }
}

@Composable
private fun DayHeader(
    day: TripPlanDay,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .bentoShadow(ShadowLevel.L3, shape)
            .clip(shape)
            .background(TravelAIGradients.auroraDiagonal)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Ngày ${day.dayNumber}",
                color = OnBrand,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            if (day.title.isNotBlank()) {
                Text(
                    text = day.title,
                    color = OnBrand.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PeriodCard(
    period: TripPlanPeriod,
    modifier: Modifier = Modifier
) {
    val (icon, label) = when (period.period) {
        TripPlanPeriodType.MORNING -> Icons.Filled.WbSunny to "Sáng"
        TripPlanPeriodType.AFTERNOON -> Icons.Filled.LightMode to "Chiều"
        TripPlanPeriodType.EVENING -> Icons.Filled.NightsStay to "Tối"
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20,
        variant = AppCardVariant.Tinted,
        shadowLevel = ShadowLevel.L1,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandPurpleSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BrandPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = label,
                    color = BrandPurple,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = period.content,
                    color = InkPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun RawItineraryContent(
    rawText: String,
    uiState: ItineraryUiState,
    onBudgetCategoryChange: (BudgetCategory) -> Unit,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetNoteChange: (String) -> Unit,
    onSaveBudgetItem: () -> Unit,
    onEditBudgetItem: (BudgetItem) -> Unit,
    onDeleteBudgetItem: (BudgetItem) -> Unit,
    onCancelBudgetEdit: () -> Unit,
    onChecklistTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onGenerateSmartChecklist: () -> Unit,
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit,
    onDeleteChecklistItem: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20,
                variant = AppCardVariant.Glass,
                shadowLevel = ShadowLevel.L2,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Bản lịch trình gốc",
                        color = BrandPurple,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rawText,
                        color = InkPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        item(key = "trip-planning-sections") {
            TripPlanningSections(
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onGenerateSmartChecklist = onGenerateSmartChecklist,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem
            )
        }
    }
}

@Composable
private fun EmptyItineraryContent(
    uiState: ItineraryUiState,
    onBudgetCategoryChange: (BudgetCategory) -> Unit,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetNoteChange: (String) -> Unit,
    onSaveBudgetItem: () -> Unit,
    onEditBudgetItem: (BudgetItem) -> Unit,
    onDeleteBudgetItem: (BudgetItem) -> Unit,
    onCancelBudgetEdit: () -> Unit,
    onChecklistTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onGenerateSmartChecklist: () -> Unit,
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit,
    onDeleteChecklistItem: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "empty-itinerary") {
            EmptyStateCard(
                title = "Chưa có lịch trình",
                body = "Lịch trình AI sẽ xuất hiện ở đây sau khi bạn tạo chuyến đi từ Planner."
            )
        }
        item(key = "trip-planning-sections") {
            TripPlanningSections(
                uiState = uiState,
                onBudgetCategoryChange = onBudgetCategoryChange,
                onBudgetTitleChange = onBudgetTitleChange,
                onBudgetAmountChange = onBudgetAmountChange,
                onBudgetNoteChange = onBudgetNoteChange,
                onSaveBudgetItem = onSaveBudgetItem,
                onEditBudgetItem = onEditBudgetItem,
                onDeleteBudgetItem = onDeleteBudgetItem,
                onCancelBudgetEdit = onCancelBudgetEdit,
                onChecklistTitleChange = onChecklistTitleChange,
                onAddChecklistItem = onAddChecklistItem,
                onGenerateSmartChecklist = onGenerateSmartChecklist,
                onToggleChecklistItem = onToggleChecklistItem,
                onDeleteChecklistItem = onDeleteChecklistItem
            )
        }
    }
}

@Composable
private fun TripPlanningSections(
    uiState: ItineraryUiState,
    onBudgetCategoryChange: (BudgetCategory) -> Unit,
    onBudgetTitleChange: (String) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    onBudgetNoteChange: (String) -> Unit,
    onSaveBudgetItem: () -> Unit,
    onEditBudgetItem: (BudgetItem) -> Unit,
    onDeleteBudgetItem: (BudgetItem) -> Unit,
    onCancelBudgetEdit: () -> Unit,
    onChecklistTitleChange: (String) -> Unit,
    onAddChecklistItem: () -> Unit,
    onGenerateSmartChecklist: () -> Unit,
    onToggleChecklistItem: (ChecklistItem, Boolean) -> Unit,
    onDeleteChecklistItem: (ChecklistItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BudgetSection(
            budgetItems = uiState.budgetItems,
            formState = uiState.budgetForm,
            errorMessage = uiState.budgetErrorMessage,
            isSaving = uiState.isBudgetSaving,
            onCategoryChange = onBudgetCategoryChange,
            onTitleChange = onBudgetTitleChange,
            onAmountChange = onBudgetAmountChange,
            onNoteChange = onBudgetNoteChange,
            onSave = onSaveBudgetItem,
            onEdit = onEditBudgetItem,
            onDelete = onDeleteBudgetItem,
            onCancelEdit = onCancelBudgetEdit,
            budgetLimitVnd = uiState.budgetLimitVnd
        )
        ChecklistSection(
            checklistItems = uiState.checklistItems,
            draftTitle = uiState.checklistDraftTitle,
            errorMessage = uiState.checklistErrorMessage,
            isSaving = uiState.isChecklistSaving,
            isGeneratingSmartChecklist = uiState.isGeneratingChecklist,
            onDraftTitleChange = onChecklistTitleChange,
            onAdd = onAddChecklistItem,
            onGenerateSmartChecklist = onGenerateSmartChecklist,
            onToggle = onToggleChecklistItem,
            onDelete = onDeleteChecklistItem
        )
    }
}

@Composable
private fun DayEditorDialog(
    editor: DayEditorState,
    isSaving: Boolean,
    errorMessage: String?,
    onTitleChange: (String) -> Unit,
    onPeriodChange: (TripPlanPeriodType, String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isSaving) onDismiss()
        },
        title = { Text("Sửa Ngày ${editor.dayNumber}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = editor.title,
                    onValueChange = onTitleChange,
                    label = { Text("Tiêu đề ngày") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TripPlanPeriodType.entries.forEach { periodType ->
                    OutlinedTextField(
                        value = editor.periodContents[periodType].orEmpty(),
                        onValueChange = { onPeriodChange(periodType, it) },
                        label = { Text(periodType.label) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Đang lưu" else "Lưu")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
private fun LoadingItineraryState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            color = BrandPurple,
            strokeWidth = 3.dp
        )
        Text(
            text = "Đang tải lịch trình...",
            color = InkSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CenterContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
private fun ParsedItineraryScreenPreview() {
    TravelAITheme {
        ItineraryScreenContent(
            uiState = ItineraryUiState(
                sessionId = 1L,
                title = "3 ngày Đà Nẵng",
                days = rememberSampleDays()
            ),
            onBack = {},
            onOpenChat = {},
            onOpenMap = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RawItineraryScreenPreview() {
    TravelAITheme {
        ItineraryScreenContent(
            uiState = ItineraryUiState(
                sessionId = 1L,
                title = "Chuyến đi mới",
                rawText = "TravelAI chưa nhận diện được cấu trúc ngày/buổi, nhưng vẫn giữ bản trả lời gốc để bạn đọc lại."
            ),
            onBack = {},
            onOpenChat = {},
            onOpenMap = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingItineraryScreenPreview() {
    TravelAITheme {
        ItineraryScreenContent(
            uiState = ItineraryUiState(isLoading = true),
            onBack = {},
            onOpenChat = {},
            onOpenMap = {}
        )
    }
}

private fun rememberSampleDays(): List<TripPlanDay> = listOf(
    TripPlanDay(
        dayNumber = 1,
        title = "Bãi biển Mỹ Khê và Sơn Trà",
        periods = listOf(
            TripPlanPeriod(
                period = TripPlanPeriodType.MORNING,
                content = "Tắm biển Mỹ Khê, ăn sáng mì Quảng."
            ),
            TripPlanPeriod(
                period = TripPlanPeriodType.AFTERNOON,
                content = "Tham quan chùa Linh Ứng và bán đảo Sơn Trà."
            ),
            TripPlanPeriod(
                period = TripPlanPeriodType.EVENING,
                content = "Dạo cầu Rồng và ăn hải sản ven biển."
            )
        )
    ),
    TripPlanDay(
        dayNumber = 2,
        title = "Hội An",
        periods = listOf(
            TripPlanPeriod(
                period = TripPlanPeriodType.MORNING,
                content = "Di chuyển đến phố cổ Hội An."
            )
        )
    )
)
