package com.travelai.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppBottomBar
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.components.BottomNavDestination
import com.travelai.ui.components.EmptyStateCard
import com.travelai.ui.components.SkeletonLoader
import com.travelai.ui.share.shareTripText
import com.travelai.ui.theme.BorderSubtle
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleSoft
import com.travelai.ui.theme.GlassHighlight
import com.travelai.ui.theme.GlassSurfaceL2
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.SurfaceCard
import com.travelai.ui.theme.TravelAIGradients
import com.travelai.ui.theme.TravelAITheme
import com.travelai.ui.theme.bentoShadow

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onOpenItinerary: (Long) -> Unit,
    onOpenMap: (Long) -> Unit,
    onOpenPlanner: () -> Unit = onBack,
    onOpenProfile: () -> Unit = onBack,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.shareText) {
        uiState.shareText?.let { exportText ->
            shareTripText(context, exportText)
            viewModel.consumeShareText()
        }
    }

    AppBackground {
        HistoryScreenContent(
            uiState = uiState,
            onBack = onBack,
            onSessionClick = onSessionClick,
            onOpenItinerary = onOpenItinerary,
            onOpenMap = onOpenMap,
            onOpenPlanner = onOpenPlanner,
            onOpenProfile = onOpenProfile,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onStartRename = viewModel::startRenameSession,
            onRenameTitleChange = viewModel::onRenameTitleChange,
            onConfirmRename = viewModel::confirmRenameSession,
            onDismissRename = viewModel::dismissRenameSession,
            onRequestDelete = viewModel::requestDeleteSession,
            onConfirmDelete = viewModel::confirmDeleteSession,
            onDismissDelete = viewModel::dismissDeleteSession,
            onTogglePinned = viewModel::togglePinned,
            onShareSession = viewModel::shareSession
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreenContent(
    uiState: HistoryUiState,
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onOpenItinerary: (Long) -> Unit,
    onOpenMap: (Long) -> Unit,
    onOpenPlanner: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit,
    onStartRename: (HistorySession) -> Unit,
    onRenameTitleChange: (String) -> Unit,
    onConfirmRename: () -> Unit,
    onDismissRename: () -> Unit,
    onRequestDelete: (HistorySession) -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onTogglePinned: (HistorySession) -> Unit,
    onShareSession: (HistorySession) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = "Thư viện chuyến đi",
                subtitle = "Lưu trữ lịch trình của bạn",
                onBack = onBack
            )
        },
        bottomBar = {
            AppBottomBar(
                selected = BottomNavDestination.Saved,
                onSelected = { dest ->
                    when (dest) {
                        BottomNavDestination.Home -> onOpenPlanner()
                        BottomNavDestination.Chat -> onOpenPlanner()
                        BottomNavDestination.Itinerary -> onOpenPlanner()
                        BottomNavDestination.Saved -> Unit
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
                .padding(horizontal = 16.dp)
        ) {
            // Search
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Tìm chuyến đi",
                        color = InkSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = BrandPurple
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = BorderSubtle,
                    cursorColor = BrandPurple,
                    focusedTextColor = InkPrimary,
                    unfocusedTextColor = InkPrimary
                )
            )

            Spacer(Modifier.height(12.dp))

            when {
                uiState.isLoading -> LoadingState(modifier = Modifier.weight(1f))
                uiState.errorMessage != null -> ErrorState(
                    message = uiState.errorMessage,
                    modifier = Modifier.weight(1f)
                )
                uiState.sessions.isEmpty() && uiState.hasSearchQuery -> NoSearchResultsState(
                    query = uiState.searchQuery,
                    modifier = Modifier.weight(1f)
                )
                uiState.sessions.isEmpty() -> EmptyHistoryState(modifier = Modifier.weight(1f))
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.sessions,
                        key = { session -> session.id }
                    ) { session ->
                        HistorySessionRow(
                            session = session,
                            isSaving = uiState.isSavingSession,
                            isSharing = uiState.sharingSessionId == session.id,
                            onClick = { onSessionClick(session.id) },
                            onOpenItinerary = { onOpenItinerary(session.id) },
                            onOpenMap = { onOpenMap(session.id) },
                            onStartRename = { onStartRename(session) },
                            onRequestDelete = { onRequestDelete(session) },
                            onTogglePinned = { onTogglePinned(session) },
                            onShareSession = { onShareSession(session) }
                        )
                    }
                }
            }
        }
    }

    uiState.renamingSession?.let { session ->
        RenameSessionDialog(
            session = session,
            title = uiState.renameTitle,
            errorMessage = uiState.renameErrorMessage,
            isSaving = uiState.isSavingSession,
            onTitleChange = onRenameTitleChange,
            onConfirm = onConfirmRename,
            onDismiss = onDismissRename
        )
    }

    uiState.deletingSession?.let { session ->
        DeleteSessionDialog(
            session = session,
            isSaving = uiState.isSavingSession,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDelete
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SurfaceCard
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SkeletonLoader(width = 180.dp, height = 22.dp)
                    Spacer(Modifier.height(12.dp))
                    SkeletonLoader(height = 14.dp)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        SkeletonLoader(width = 60.dp, height = 32.dp, shape = RoundedCornerShape(16.dp))
                        Spacer(Modifier.width(8.dp))
                        SkeletonLoader(width = 80.dp, height = 32.dp, shape = RoundedCornerShape(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        EmptyStateCard(
            title = "Chưa có chuyến đi nào",
            body = "Tạo chuyến mới từ Planner để lưu lịch trình, ngân sách và checklist vào thư viện."
        )
    }
}

@Composable
private fun NoSearchResultsState(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Không tìm thấy chuyến đi",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Không có title nào khớp với \"$query\".",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HistorySessionRow(
    session: HistorySession,
    isSaving: Boolean,
    isSharing: Boolean,
    onClick: () -> Unit,
    onOpenItinerary: () -> Unit,
    onOpenMap: () -> Unit,
    onStartRename: () -> Unit,
    onRequestDelete: () -> Unit,
    onTogglePinned: () -> Unit,
    onShareSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    val container = if (session.isPinned) GlassSurfaceL2 else SurfaceCard
    val borderColor = if (session.isPinned) GlassHighlight else BorderSubtle
    val shadow = if (session.isPinned) ShadowLevel.L3 else ShadowLevel.L2

    Box(
        modifier = modifier
            .fillMaxWidth()
            .bentoShadow(shadow, shape)
            .clip(shape)
            .background(container)
            .border(1.dp, borderColor, shape)
    ) {
        if (session.isPinned) {
            // Aurora left-edge stripe — accents pinned rows.
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(vertical = 14.dp)
                    .width(4.dp)
                    .fillMaxHeight(fraction = 0.78f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TravelAIGradients.auroraDiagonal)
            )
        }
        Card(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (session.isPinned) 6.dp else 0.dp),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(BrandPurpleSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = BrandPurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = session.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (session.isPinned) {
                        Icon(
                            imageVector = Icons.Filled.PushPin,
                            contentDescription = "Đã ghim",
                            tint = BrandPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = session.createdAtText,
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onClick,
                        shape = RoundedCornerShape(100),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(100))
                            .background(TravelAIGradients.auroraDiagonal)
                    ) {
                        Text(
                            text = "Chat",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = OnBrand,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        onClick = onOpenItinerary,
                        shape = RoundedCornerShape(100),
                        color = BrandPurpleSoft
                    ) {
                        Text(
                            text = "Lịch trình",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = BrandPurple,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    SessionOverflowMenu(
                        isPinned = session.isPinned,
                        isSaving = isSaving,
                        isSharing = isSharing,
                        onOpenMap = onOpenMap,
                        onShareSession = onShareSession,
                        onTogglePinned = onTogglePinned,
                        onStartRename = onStartRename,
                        onRequestDelete = onRequestDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionOverflowMenu(
    isPinned: Boolean,
    isSaving: Boolean,
    isSharing: Boolean,
    onOpenMap: () -> Unit,
    onShareSession: () -> Unit,
    onTogglePinned: () -> Unit,
    onStartRename: () -> Unit,
    onRequestDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Thao tác khác")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Map, contentDescription = null)
                },
                text = { Text("Bản đồ") },
                onClick = {
                    expanded = false
                    onOpenMap()
                }
            )
            DropdownMenuItem(
                text = { Text(if (isSharing) "Đang xuất..." else "Chia sẻ") },
                enabled = !isSharing,
                onClick = {
                    expanded = false
                    onShareSession()
                }
            )
            DropdownMenuItem(
                text = { Text(if (isPinned) "Bỏ ghim" else "Ghim") },
                enabled = !isSaving,
                onClick = {
                    expanded = false
                    onTogglePinned()
                }
            )
            DropdownMenuItem(
                text = { Text("Đổi tên") },
                enabled = !isSaving,
                onClick = {
                    expanded = false
                    onStartRename()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Xóa",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                enabled = !isSaving,
                onClick = {
                    expanded = false
                    onRequestDelete()
                }
            )
        }
    }
}

@Composable
private fun RenameSessionDialog(
    session: HistorySession,
    title: String,
    errorMessage: String?,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi tên chuyến đi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = session.title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    label = { Text("Tên mới") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = BorderSubtle,
                        cursorColor = BrandPurple
                    )
                )
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
            TextButton(
                onClick = onConfirm,
                enabled = !isSaving
            ) {
                Text("Lưu")
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
private fun DeleteSessionDialog(
    session: HistorySession,
    isSaving: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa chuyến đi?") },
        text = {
            Text("Xóa \"${session.title}\" khỏi máy này. Tin nhắn, lịch trình, ngân sách và checklist sẽ bị xóa theo.")
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isSaving
            ) {
                Text(
                    text = "Xóa",
                    color = MaterialTheme.colorScheme.error
                )
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

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    TravelAITheme {
        HistoryScreenContent(
            uiState = HistoryUiState(
                sessions = listOf(
                    HistorySession(
                        id = 1L,
                        title = "3 ngày Đà Nẵng",
                        createdAtText = "04/05/2026 14:30",
                        isPinned = true
                    ),
                    HistorySession(
                        id = 2L,
                        title = "2 ngày Hội An",
                        createdAtText = "05/05/2026 09:15",
                        isPinned = false
                    )
                )
            ),
            onBack = {},
            onSessionClick = {},
            onOpenItinerary = {},
            onOpenMap = {},
            onSearchQueryChange = {},
            onStartRename = {},
            onRenameTitleChange = {},
            onConfirmRename = {},
            onDismissRename = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onDismissDelete = {},
            onTogglePinned = {},
            onShareSession = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyHistoryScreenPreview() {
    TravelAITheme {
        HistoryScreenContent(
            uiState = HistoryUiState(),
            onBack = {},
            onSessionClick = {},
            onOpenItinerary = {},
            onOpenMap = {},
            onSearchQueryChange = {},
            onStartRename = {},
            onRenameTitleChange = {},
            onConfirmRename = {},
            onDismissRename = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onDismissDelete = {},
            onTogglePinned = {},
            onShareSession = {}
        )
    }
}
