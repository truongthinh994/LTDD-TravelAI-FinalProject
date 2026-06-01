package com.travelai.ui.landmark

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.data.model.LandmarkScan
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.EmptyStateCard
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.theme.BorderSubtle
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleTint
import com.travelai.ui.theme.DangerRed
import com.travelai.ui.theme.DangerRedSoft
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LandmarkHistoryScreen(
    onBack: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    viewModel: LandmarkHistoryViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    var pendingDeleteId by remember { mutableStateOf<Long?>(null) }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                title = "Lịch sử Camera AI",
                subtitle = "Các địa điểm bạn đã nhận diện",
                onBack = onBack
            )

            if (items.isEmpty()) {
                EmptyHistory()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = items, key = { it.id }) { scan ->
                        SwipeableScanRow(
                            scan = scan,
                            onClick = { onOpenDetail(scan.id) },
                            onSwipeDelete = { pendingDeleteId = scan.id }
                        )
                    }
                }
            }
        }
    }

    pendingDeleteId?.let { id ->
        ConfirmDeleteDialog(
            onConfirm = {
                viewModel.delete(id)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null }
        )
    }
}

@Composable
private fun EmptyHistory() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        EmptyStateCard(
            title = "Chưa có ảnh nào được nhận diện",
            body = "Hãy quay lại Camera AI và quét một địa điểm để bắt đầu xây dựng thư viện của bạn."
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableScanRow(
    scan: LandmarkScan,
    onClick: () -> Unit,
    onSwipeDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeDelete()
                // Return false so the row snaps back; confirm dialog handles the real delete.
                false
            } else {
                false
            }
        }
    )

    // Reset state if user opened dialog then cancelled — keeps row from showing red.
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DangerRedSoft)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = DangerRed
                    )
                    Text(
                        text = "Xoá",
                        color = DangerRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        content = {
            ScanRowCard(scan = scan, onClick = onClick)
        }
    )
}

@Composable
private fun ScanRowCard(
    scan: LandmarkScan,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ThumbnailBox(imagePath = scan.imagePath)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.info.name.ifBlank { "Chưa rõ tên" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary,
                    maxLines = 1
                )
                if (scan.info.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = InkSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = scan.info.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = formatScanTimestamp(scan.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary
                )
            }
        }
    }
}

@Composable
private fun ThumbnailBox(imagePath: String) {
    val bitmap by produceState<Bitmap?>(initialValue = null, key1 = imagePath) {
        value = withContext(Dispatchers.IO) {
            runCatching { BitmapFactory.decodeFile(imagePath) }.getOrNull()
        }
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BrandPurpleTint)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = "Ảnh đã quét",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                tint = BrandPurple
            )
        }
    }
}

@Composable
private fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Xoá khỏi lịch sử?", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = "Ảnh và thông tin nhận diện sẽ bị xoá khỏi thiết bị. Hành động này không thể hoàn tác.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Xoá", color = DangerRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Huỷ")
            }
        }
    )
}

private val SCAN_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())

private fun formatScanTimestamp(timestamp: Long): String =
    Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(SCAN_DATE_FORMATTER)
