package com.travelai.ui.landmark

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.components.AppPrimaryButton
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.landmark.components.LandmarkResultCard
import com.travelai.ui.theme.BorderSubtle
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleTint
import com.travelai.ui.theme.DangerRed
import com.travelai.ui.theme.DangerRedSoft
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.ShadowLevel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LandmarkHistoryDetailScreen(
    onBack: () -> Unit,
    onAskInChat: (String) -> Unit,
    viewModel: LandmarkHistoryDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(
                title = state.scan?.info?.name?.ifBlank { "Chi tiết" } ?: "Chi tiết",
                subtitle = state.scan?.createdAt?.let(::formatDetailTimestamp),
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    state.isLoading -> LoadingCard()

                    state.errorMessage != null -> ErrorCard(message = state.errorMessage!!)

                    state.scan != null -> {
                        ImagePreviewCard(
                            bitmapValue = state.bitmap,
                            label = state.scan?.info?.name.orEmpty()
                        )
                        LandmarkResultCard(result = state.scan!!.info)
                        AppPrimaryButton(
                            text = "Hỏi thêm trong Chat",
                            leadingIcon = Icons.AutoMirrored.Filled.Chat,
                            onClick = { onAskInChat(viewModel.buildChatPrompt()) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ImagePreviewCard(
    bitmapValue: android.graphics.Bitmap?,
    label: String
) {
    AppCard(variant = AppCardVariant.Glass, shadowLevel = ShadowLevel.L3) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Ảnh đã quét",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = InkPrimary
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandPurpleTint)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmapValue != null) {
                    Image(
                        bitmap = bitmapValue.asImageBitmap(),
                        contentDescription = label.ifBlank { "Ảnh đã quét" },
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    AppCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = BrandPurple, strokeWidth = 3.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Đang tải...",
                    color = InkSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    AppCard(container = DangerRedSoft, border = DangerRed.copy(alpha = 0.4f)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Lỗi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = DangerRed
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = InkPrimary
            )
        }
    }
}

private val DETAIL_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())

private fun formatDetailTimestamp(timestamp: Long): String =
    Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DETAIL_DATE_FORMATTER)
