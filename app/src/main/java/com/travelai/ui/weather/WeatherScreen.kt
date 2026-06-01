package com.travelai.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.data.model.WeatherDay
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.components.AppPrimaryButton
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.itinerary.components.WeatherCard
import com.travelai.ui.theme.BrandBlue
import com.travelai.ui.theme.BrandBlueSoft
import com.travelai.ui.theme.DangerRed
import com.travelai.ui.theme.DangerRedSoft
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.SuccessGreen
import com.travelai.ui.theme.SuccessGreenSoft
import com.travelai.ui.theme.TravelAITheme

@Composable
fun WeatherScreen(
    onBack: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeatherContent(
        uiState = uiState,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onLoadForecast = viewModel::loadForecast
    )
}

@Composable
private fun WeatherContent(
    uiState: WeatherUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onLoadForecast: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    AppBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    title = "Thời tiết",
                    subtitle = "Dự báo 7 ngày cho điểm đến",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22,
                    contentPadding = PaddingValues(16.dp),
                    variant = AppCardVariant.Solid,
                    shadowLevel = ShadowLevel.L1
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconBadge()
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Xem nhanh trước khi lên lịch",
                                    color = colors.onSurface,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Nhập tên thành phố hoặc điểm đến, app sẽ tự tìm tọa độ bằng Open-Meteo.",
                                    color = colors.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        OutlinedTextField(
                            value = uiState.query,
                            onValueChange = onQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ví dụ: Đà Nẵng") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { onLoadForecast() }),
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

                        AppPrimaryButton(
                            text = "Tải dự báo",
                            leadingIcon = Icons.Filled.Search,
                            onClick = onLoadForecast,
                            enabled = uiState.query.isNotBlank(),
                            isLoading = uiState.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                uiState.errorMessage?.let { message ->
                    MessageCard(
                        message = message,
                        isError = uiState.days.isEmpty()
                    )
                }

                when {
                    uiState.isLoading && uiState.days.isEmpty() -> LoadingCard()
                    uiState.days.isNotEmpty() -> {
                        WeatherCard(
                            days = uiState.days,
                            destination = uiState.loadedDestination.ifBlank { uiState.query },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (uiState.packingAdvice.isNotEmpty()) {
                            PackingAdviceCard(advice = uiState.packingAdvice)
                        }
                    }
                    uiState.errorMessage == null -> MessageCard(
                        message = "Nhập điểm đến để xem nhiệt độ, trạng thái trời, khả năng mưa và gợi ý đồ cần mang.",
                        isError = false
                    )
                }

                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun IconBadge() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BrandBlueSoft)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.WbCloudy,
            contentDescription = null,
            tint = BrandBlue
        )
    }
}

@Composable
private fun PackingAdviceCard(advice: List<String>) {
    AppCard(
        container = SuccessGreenSoft,
        border = SuccessGreen.copy(alpha = 0.28f),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Gợi ý chuẩn bị",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            advice.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen
                    )
                    Text(
                        text = item,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
            Text(
                text = "Đang tải dự báo thời tiết...",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean
) {
    val colors = MaterialTheme.colorScheme
    val container = if (isError) DangerRedSoft else colors.surface
    val border = if (isError) DangerRed.copy(alpha = 0.35f) else colors.outline

    AppCard(
        container = container,
        border = border,
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = message,
            color = if (isError) DangerRed else colors.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isError) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WeatherContentPreview() {
    TravelAITheme(darkTheme = false) {
        WeatherContent(
            uiState = WeatherUiState(
                query = "Đà Nẵng",
                loadedDestination = "Đà Nẵng",
                packingAdvice = listOf("Chuẩn bị kem chống nắng, mũ và nước uống."),
                days = listOf(
                    WeatherDay(
                        date = "2026-05-21",
                        tempMinC = 25,
                        tempMaxC = 32,
                        conditionEmoji = "☀️",
                        conditionLabel = "Nắng",
                        rainChancePct = 10
                    )
                )
            ),
            onBack = {},
            onQueryChange = {},
            onLoadForecast = {}
        )
    }
}
