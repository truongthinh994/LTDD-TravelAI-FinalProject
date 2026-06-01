package com.travelai.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelai.data.model.TripProfile
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleTint
import com.travelai.ui.theme.InkPrimary

/**
 * A single quick-start option shown as a chip on the empty chat screen.
 *
 * Clicking the chip creates a brand-new chat session pre-populated with
 * [profile] so the AI gets full trip context in its system prompt — same code
 * path the Trip Planner form uses, so the response renders as a structured
 * itinerary instead of free-form markdown.
 */
data class SamplePromptOption(
    val label: String,
    val profile: TripProfile
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SamplePromptChips(
    onChipClick: (SamplePromptOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Gợi ý nhanh",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = InkPrimary,
            modifier = Modifier.padding(start = 4.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SAMPLE_PROMPTS.forEach { option ->
                AssistChip(
                    onClick = { onChipClick(option) },
                    label = { Text(option.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = BrandPurple
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = BrandPurpleTint,
                        labelColor = InkPrimary,
                        leadingIconContentColor = BrandPurple
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = BrandPurple.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}

private val SAMPLE_PROMPTS: List<SamplePromptOption> = listOf(
    SamplePromptOption(
        label = "3 ngày Đà Nẵng 5 triệu",
        profile = TripProfile(
            destination = "Đà Nẵng",
            days = 3,
            budget = "5 triệu",
            people = 2,
            travelStyle = "biển, ẩm thực",
            transport = "xe máy",
            note = ""
        )
    ),
    SamplePromptOption(
        label = "Cuối tuần Hà Nội tháng 6",
        profile = TripProfile(
            destination = "Hà Nội",
            days = 2,
            budget = "3 triệu",
            people = 2,
            travelStyle = "văn hóa, ẩm thực đường phố",
            transport = "xe máy",
            note = "đi cuối tuần tháng 6, có thể nắng nóng"
        )
    ),
    SamplePromptOption(
        label = "Phú Quốc 2N1Đ couple",
        profile = TripProfile(
            destination = "Phú Quốc",
            days = 2,
            budget = "6 triệu",
            people = 2,
            travelStyle = "biển, nghỉ dưỡng, lãng mạn",
            transport = "thuê xe máy tại đảo",
            note = "couple, ưu tiên resort và biển đẹp"
        )
    ),
    SamplePromptOption(
        label = "Sapa mùa đông 4 ngày",
        profile = TripProfile(
            destination = "Sapa",
            days = 4,
            budget = "5 triệu",
            people = 2,
            travelStyle = "núi, trekking, ngắm tuyết",
            transport = "xe khách + xe máy",
            note = "đi mùa đông, mang đồ ấm"
        )
    ),
    SamplePromptOption(
        label = "Đà Lạt với người già",
        profile = TripProfile(
            destination = "Đà Lạt",
            days = 3,
            budget = "4 triệu",
            people = 4,
            travelStyle = "nhẹ nhàng, ngắm cảnh, café",
            transport = "ô tô",
            note = "đi cùng người già, tránh đi bộ nhiều, lịch trình thư giãn"
        )
    )
)
