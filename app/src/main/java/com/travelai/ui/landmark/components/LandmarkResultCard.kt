package com.travelai.ui.landmark.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelai.data.model.LandmarkInfo
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.components.decorativeBlob
import com.travelai.ui.theme.BrandBlue
import com.travelai.ui.theme.BrandPink
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.ShadowLevel

/**
 * Reusable card hiển thị thông tin landmark đã nhận diện.
 * Dùng cho cả scanner và màn hình chi tiết lịch sử.
 */
@Composable
fun LandmarkResultCard(
    result: LandmarkInfo,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.decorativeBlob(BrandPink),
        variant = AppCardVariant.Glass,
        shadowLevel = ShadowLevel.L2
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkPrimary
                )
                if (result.location.isNotBlank()) {
                    Text(
                        text = result.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary
                    )
                }
                ConfidenceChip(confidence = result.confidence)
            }

            if (result.description.isNotBlank()) {
                LabeledBlock(label = "Mô tả", body = result.description)
            }
            if (result.history.isNotBlank()) {
                LabeledBlock(label = "Lịch sử", body = result.history)
            }
            if (result.tips.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Mẹo du lịch",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple
                    )
                    result.tips.forEach { tip ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "•",
                                color = BrandPink,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = InkPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledBlock(label: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = BrandPurple
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = InkPrimary
        )
    }
}

@Composable
private fun ConfidenceChip(confidence: Float) {
    val pct = (confidence.coerceIn(0f, 1f) * 100).toInt()
    val color = when {
        pct >= 80 -> BrandPurple
        pct >= 50 -> BrandBlue
        else -> InkSecondary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Độ tin cậy: $pct%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
