package com.travelai.ui.itinerary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.travelai.data.model.WeatherDay
import com.travelai.ui.components.AppCard
import com.travelai.ui.theme.BorderSubtle
import com.travelai.ui.theme.BrandBlue
import com.travelai.ui.theme.BrandBlueSoft
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.SurfaceMuted
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Horizontal 7-day forecast strip rendered above the itinerary days.
 *
 * The card silently hides when [days] is empty so the screen still works on
 * trips where geocoding failed or the API request errored.
 */
@Composable
fun WeatherCard(
    days: List<WeatherDay>,
    destination: String,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return

    AppCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandBlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbCloudy,
                        contentDescription = null,
                        tint = BrandBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column {
                    Text(
                        text = "Thời tiết 7 ngày tới",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                    if (destination.isNotBlank()) {
                        Text(
                            text = destination,
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(days, key = { it.date }) { day ->
                    WeatherDayPill(day = day)
                }
            }
        }
    }
}

@Composable
private fun WeatherDayPill(day: WeatherDay) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceMuted)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = day.date.toDayOfWeekVi(),
            style = MaterialTheme.typography.labelMedium,
            color = InkSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = day.conditionEmoji,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "${day.tempMaxC}°/${day.tempMinC}°",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )
        if (day.rainChancePct >= RAIN_DISPLAY_THRESHOLD) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = "💧 ${day.rainChancePct}%",
                style = MaterialTheme.typography.labelSmall,
                color = BrandBlue
            )
        }
    }
}

private const val RAIN_DISPLAY_THRESHOLD = 20

/**
 * Format an ISO date ("2025-05-22") into a Vietnamese-friendly short label:
 * - "Hôm nay" for today
 * - "Mai" for tomorrow
 * - "T3 22/5" otherwise
 */
private fun String.toDayOfWeekVi(): String {
    val date = runCatching { LocalDate.parse(this) }.getOrNull() ?: return this
    val today = LocalDate.now()
    return when {
        date == today -> "Hôm nay"
        date == today.plusDays(1) -> "Mai"
        else -> {
            val dow = date.dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("vi"))
                .replaceFirstChar { it.uppercase() }
            "$dow ${date.dayOfMonth}/${date.monthValue}"
        }
    }
}
