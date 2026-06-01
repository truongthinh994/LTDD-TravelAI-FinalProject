package com.travelai.ui.map

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.travelai.data.model.TripMapPlace
import com.travelai.data.model.TripMapPlaceStatus
import com.travelai.data.model.TripPlanPeriodType
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppBottomBar
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.components.BottomNavDestination
import com.travelai.ui.components.CategoryChip
import com.travelai.ui.components.EmptyStateCard
import com.travelai.ui.components.TopBarIconButton
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.BorderSubtle
import com.travelai.ui.theme.BrandBlue
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleSoft
import com.travelai.ui.theme.BrandTeal
import com.travelai.ui.theme.DangerRed
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.SurfaceCard
import com.travelai.ui.theme.TravelAITheme

@Composable
fun TripMapScreen(
    onBack: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenChat: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: TripMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AppBackground {
        TripMapScreenContent(
            uiState = uiState,
            onBack = onBack,
            onOpenPlanner = onOpenPlanner,
            onOpenChat = onOpenChat,
            onOpenHistory = onOpenHistory,
            onOpenProfile = onOpenProfile,
            onSelectDay = viewModel::selectDay
        )
    }
}

@Composable
private fun TripMapScreenContent(
    uiState: TripMapUiState,
    onBack: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenChat: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    onSelectDay: (Int?) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            AppTopBar(
                title = uiState.title.ifBlank { "Bản đồ chuyến đi" },
                subtitle = uiState.destination.ifBlank { "Các địa điểm từ lịch trình AI" },
                onBack = onBack,
                actions = {
                    uiState.sessionId?.let { sessionId ->
                        TopBarIconButton(
                            icon = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Mở trò chuyện",
                            onClick = { onOpenChat(sessionId) },
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
                LoadingMapState()
            }

            uiState.errorMessage != null -> CenterContent(modifier = Modifier.padding(innerPadding)) {
                MessageState(
                    title = "Không thể mở bản đồ",
                    message = uiState.errorMessage,
                    isError = true
                )
            }

            uiState.emptyMessage != null -> CenterContent(modifier = Modifier.padding(innerPadding)) {
                MessageState(
                    title = "Chưa có bản đồ",
                    message = uiState.emptyMessage
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.mapsApiKeyMissing) {
                    item(key = "api-key-warning") {
                        SetupWarningCard()
                    }
                }

                item(key = "map") {
                    if (uiState.mapsApiKeyMissing) {
                        MapPlaceholderCard(
                            title = "Chưa cấu hình Google Maps",
                            message = "Thêm MAPS_API_KEY vào local.properties để hiển thị bản đồ trong app."
                        )
                    } else if (uiState.visibleResolvedPlaces.isEmpty()) {
                        MapPlaceholderCard(
                            title = "Đang chuẩn bị tọa độ",
                            message = "TravelAI vẫn hiển thị danh sách địa điểm và có thể mở Google Maps ngoài app."
                        )
                    } else {
                        TripGoogleMap(
                            places = uiState.visibleResolvedPlaces,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item(key = "filters") {
                    DayFilterRow(
                        dayNumbers = uiState.dayNumbers,
                        selectedDayNumber = uiState.selectedDayNumber,
                        onSelectDay = onSelectDay
                    )
                }

                if (uiState.isResolving || uiState.geocodeMessage != null) {
                    item(key = "geocode-state") {
                        GeocodeStateCard(
                            isResolving = uiState.isResolving,
                            message = uiState.geocodeMessage
                        )
                    }
                }

                items(
                    items = uiState.visiblePlaces,
                    key = { place -> place.id }
                ) { place ->
                    MapPlaceRow(
                        place = place,
                        onSearch = { openGoogleMapsSearch(context, place) },
                        onDirections = { openGoogleMapsDirections(context, place) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TripGoogleMap(
    places: List<TripMapPlace>,
    modifier: Modifier = Modifier
) {
    val markerPlaces = remember(places) {
        places.mapNotNull { place ->
            val latitude = place.latitude
            val longitude = place.longitude
            if (latitude != null && longitude != null) {
                place to LatLng(latitude, longitude)
            } else {
                null
            }
        }
    }
    val initialPosition = markerPlaces.firstOrNull()?.second ?: DEFAULT_CAMERA
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 12f)
    }
    val cameraKey = markerPlaces.joinToString("|") { "${it.second.latitude},${it.second.longitude}" }

    LaunchedEffect(cameraKey) {
        markerPlaces.firstOrNull()?.second?.let { firstPosition ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(firstPosition, 12.5f),
                durationMs = 450
            )
        }
    }

    AppCard(
        modifier = modifier.height(320.dp),
        cornerRadius = 22,
        variant = AppCardVariant.Glass,
        shadowLevel = ShadowLevel.L3,
        contentPadding = PaddingValues(0.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                compassEnabled = true,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false
            )
        ) {
            if (markerPlaces.size > 1) {
                Polyline(
                    points = markerPlaces.map { it.second },
                    color = BrandPurple,
                    width = 6f
                )
            }
            markerPlaces.forEach { (place, position) ->
                Marker(
                    state = MarkerState(position = position),
                    title = place.name,
                    snippet = "Ngày ${place.dayNumber} - ${place.period.label}"
                )
            }
        }
    }
}

@Composable
private fun DayFilterRow(
    dayNumbers: List<Int>,
    selectedDayNumber: Int?,
    onSelectDay: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryChip(
            text = "Tất cả",
            selected = selectedDayNumber == null,
            onClick = { onSelectDay(null) }
        )
        dayNumbers.forEach { dayNumber ->
            CategoryChip(
                text = "Ngày $dayNumber",
                selected = selectedDayNumber == dayNumber,
                onClick = { onSelectDay(dayNumber) }
            )
        }
    }
}

@Composable
private fun MapPlaceRow(
    place: TripMapPlace,
    onSearch: () -> Unit,
    onDirections: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 18,
        variant = AppCardVariant.Solid,
        shadowLevel = ShadowLevel.L1,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandPurpleSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = BrandPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = place.name,
                    color = InkPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ngày ${place.dayNumber} - ${place.period.label} • ${place.status.label()}",
                    color = place.statusColor(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Tìm trên Google Maps",
                    tint = BrandBlue
                )
            }
            IconButton(onClick = onDirections) {
                Icon(
                    imageVector = Icons.Filled.Directions,
                    contentDescription = "Chỉ đường",
                    tint = BrandTeal
                )
            }
        }
    }
}

@Composable
private fun SetupWarningCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DangerRed.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = DangerRed
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Thiếu MAPS_API_KEY trong local.properties. App không crash; danh sách địa điểm và nút mở Google Maps ngoài app vẫn dùng được.",
                color = InkPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun MapPlaceholderCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(22.dp)),
        color = SurfaceCard
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(46.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                color = InkPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                color = InkSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GeocodeStateCard(
    isResolving: Boolean,
    message: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = BrandPurpleSoft,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isResolving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = BrandPurple
                )
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = message ?: "Đang định vị địa điểm từ lịch trình...",
                color = InkPrimary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LoadingMapState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(color = BrandPurple, strokeWidth = 3.dp)
        Text(
            text = "Đang tải bản đồ chuyến đi...",
            color = InkSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = if (isError) DangerRed else InkPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
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

private fun TripMapPlaceStatus.label(): String = when (this) {
    TripMapPlaceStatus.PENDING -> "Đang định vị"
    TripMapPlaceStatus.RESOLVED -> "Đã định vị"
    TripMapPlaceStatus.FAILED -> "Mở bằng tìm kiếm"
}

@Composable
private fun TripMapPlace.statusColor(): Color = when (status) {
    TripMapPlaceStatus.PENDING -> BrandBlue
    TripMapPlaceStatus.RESOLVED -> BrandTeal
    TripMapPlaceStatus.FAILED -> InkSecondary
}

private fun openGoogleMapsSearch(context: Context, place: TripMapPlace) {
    val uri = "geo:0,0?q=${Uri.encode(place.query)}".toUri()
    openMapIntent(context, uri, preferGoogleMapsApp = true)
}

private fun openGoogleMapsDirections(context: Context, place: TripMapPlace) {
    val latitude = place.latitude
    val longitude = place.longitude
    val uri = if (latitude != null && longitude != null) {
        "google.navigation:q=$latitude,$longitude".toUri()
    } else {
        "geo:0,0?q=${Uri.encode(place.query)}".toUri()
    }
    openMapIntent(context, uri, preferGoogleMapsApp = true)
}

private fun openMapIntent(
    context: Context,
    uri: Uri,
    preferGoogleMapsApp: Boolean
) {
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        if (preferGoogleMapsApp) {
            setPackage("com.google.android.apps.maps")
        }
    }
    val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(fallbackIntent)
    }
}

private val DEFAULT_CAMERA = LatLng(16.047079, 108.20623)

@Preview(showBackground = true)
@Composable
private fun TripMapScreenMissingKeyPreview() {
    TravelAITheme {
        TripMapScreenContent(
            uiState = TripMapUiState(
                sessionId = 1L,
                title = "3 ngày Đà Nẵng",
                destination = "Đà Nẵng",
                mapsApiKeyMissing = true,
                places = listOf(
                    TripMapPlace(
                        id = 1L,
                        sessionId = 1L,
                        dayNumber = 1,
                        period = TripPlanPeriodType.MORNING,
                        name = "Bãi biển Mỹ Khê",
                        query = "Bãi biển Mỹ Khê, Đà Nẵng",
                        latitude = null,
                        longitude = null,
                        status = TripMapPlaceStatus.PENDING,
                        createdAt = 0L,
                        updatedAt = 0L
                    )
                )
            ),
            onBack = {},
            onOpenPlanner = {},
            onOpenChat = {},
            onOpenHistory = {},
            onOpenProfile = {},
            onSelectDay = {}
        )
    }
}
