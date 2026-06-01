package com.travelai.ui.landmark

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.data.model.LandmarkInfo
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.components.AppPrimaryButton
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.components.TopBarIconButton
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.landmark.components.LandmarkResultCard
import com.travelai.ui.theme.BorderSubtle
import com.travelai.ui.theme.BrandBlue
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleTint
import com.travelai.ui.theme.DangerRed
import com.travelai.ui.theme.DangerRedSoft
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.SurfaceCard

@Composable
fun LandmarkScannerScreen(
    onBack: () -> Unit,
    onAskInChat: (String) -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: LandmarkScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let(viewModel::onImageSelected)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = decodeUriToBitmap(context, it)
            if (bitmap != null) {
                viewModel.onImageSelected(bitmap)
            }
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(
                title = "Camera AI",
                subtitle = "Nhận diện địa điểm qua ảnh",
                onBack = onBack,
                actions = {
                    TopBarIconButton(
                        icon = Icons.Filled.History,
                        contentDescription = "Lịch sử quét",
                        onClick = onOpenHistory
                    )
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    uiState.isRecognizing -> RecognizingCard()

                    uiState.result != null -> ResultSection(
                        result = uiState.result!!,
                        onAskInChat = { onAskInChat(viewModel.buildChatPrompt()) },
                        onScanAnother = viewModel::onClear
                    )

                    uiState.selectedBitmap != null -> PreviewSection(
                        bitmap = uiState.selectedBitmap!!,
                        onRecognize = viewModel::onRecognize,
                        onChange = viewModel::onClear
                    )

                    else -> EmptyState(
                        onTakePhoto = { cameraLauncher.launch(null) },
                        onPickFromGallery = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                }

                uiState.errorMessage?.let { msg ->
                    ErrorCard(
                        message = msg,
                        onDismiss = viewModel::onDismissError,
                        onRetry = if (uiState.selectedBitmap != null) {
                            { viewModel.onRecognize() }
                        } else null
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Sections ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    AppCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Chụp hoặc chọn ảnh để bắt đầu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = InkPrimary
            )
            Text(
                text = "AI sẽ tự động nhận diện các địa điểm du lịch nổi tiếng trong ảnh và cho bạn biết tên, lịch sử, mẹo tham quan.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary
            )
            Spacer(Modifier.height(4.dp))
            ActionTile(
                icon = Icons.Filled.PhotoCamera,
                title = "Chụp ảnh mới",
                subtitle = "Mở camera để chụp ngay",
                accent = BrandPurple,
                onClick = onTakePhoto
            )
            ActionTile(
                icon = Icons.Filled.PhotoLibrary,
                title = "Chọn từ thư viện",
                subtitle = "Lấy ảnh có sẵn trong máy",
                accent = BrandBlue,
                onClick = onPickFromGallery
            )
        }
    }
}

@Composable
private fun PreviewSection(
    bitmap: Bitmap,
    onRecognize: () -> Unit,
    onChange: () -> Unit
) {
    AppCard(variant = AppCardVariant.Glass, shadowLevel = ShadowLevel.L2) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Ảnh đã chọn",
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
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Ảnh đã chọn để nhận diện",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AppPrimaryButton(
                text = "Nhận diện ngay",
                leadingIcon = Icons.Filled.Search,
                onClick = onRecognize,
                modifier = Modifier.fillMaxWidth()
            )
            SecondaryActionRow(
                icon = Icons.Filled.Refresh,
                label = "Đổi ảnh khác",
                onClick = onChange
            )
        }
    }
}

@Composable
private fun RecognizingCard() {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = BrandPurple,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "Đang nhận diện…",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Text(
                    text = "AI đang phân tích ảnh, vui lòng đợi vài giây.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
            }
        }
    }
}

@Composable
private fun ResultSection(
    result: LandmarkInfo,
    onAskInChat: () -> Unit,
    onScanAnother: () -> Unit
) {
    if (!result.isLandmark || result.name.isBlank()) {
        AppCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Chưa nhận diện được",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkPrimary
                )
                Text(
                    text = "AI chưa thể xác định được địa điểm cụ thể trong ảnh này. Hãy thử ảnh khác rõ nét hơn, hoặc hỏi gợi ý trong Chat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary
                )
                AppPrimaryButton(
                    text = "Hỏi gợi ý trong Chat",
                    leadingIcon = Icons.AutoMirrored.Filled.Chat,
                    onClick = onAskInChat,
                    modifier = Modifier.fillMaxWidth()
                )
                SecondaryActionRow(
                    icon = Icons.Filled.Refresh,
                    label = "Quét ảnh khác",
                    onClick = onScanAnother
                )
            }
        }
        return
    }

    LandmarkResultCard(result = result)

    AppPrimaryButton(
        text = "Hỏi thêm trong Chat",
        leadingIcon = Icons.AutoMirrored.Filled.Chat,
        onClick = onAskInChat,
        modifier = Modifier.fillMaxWidth()
    )
    SecondaryActionRow(
        icon = Icons.Filled.Refresh,
        label = "Quét ảnh khác",
        onClick = onScanAnother
    )
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)?
) {
    AppCard(container = DangerRedSoft, border = DangerRed.copy(alpha = 0.4f)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onRetry != null) {
                    SecondaryActionRow(
                        icon = Icons.Filled.Refresh,
                        label = "Thử lại",
                        onClick = onRetry,
                        modifier = Modifier.weight(1f)
                    )
                }
                SecondaryActionRow(
                    icon = Icons.Filled.Refresh,
                    label = "Đóng",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── Small reusable bits ────────────────────────────────────────────────────

@Composable
private fun ActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )
        }
        androidx.compose.material3.TextButton(onClick = onClick) {
            Text(text = "Chọn", color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SecondaryActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandPurple,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary,
            modifier = Modifier
                .weight(1f)
        )
        androidx.compose.material3.TextButton(onClick = onClick) {
            Text(text = "OK", color = BrandPurple, fontWeight = FontWeight.Bold)
        }
    }
}

// ── Helper: decode gallery URI → Bitmap (supports API 26+) ─────────────────
// Downsample to MAX_IMAGE_DIMENSION on the longest side so Canvas can draw it
// (Canvas hard-fails on bitmaps > ~100MB) and the upload payload stays small.

private const val MAX_IMAGE_DIMENSION = 1600

private fun decodeUriToBitmap(
    context: android.content.Context,
    uri: Uri
): Bitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.isMutableRequired = false
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val w = info.size.width
            val h = info.size.height
            val longest = maxOf(w, h)
            if (longest > MAX_IMAGE_DIMENSION) {
                val scale = MAX_IMAGE_DIMENSION.toFloat() / longest
                decoder.setTargetSize((w * scale).toInt(), (h * scale).toInt())
            }
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        var sampleSize = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > MAX_IMAGE_DIMENSION) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }
}.getOrNull()
