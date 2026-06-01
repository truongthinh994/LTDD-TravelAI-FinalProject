package com.travelai.ui.chat

import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.travelai.ui.chat.components.ChatBubble
import com.travelai.ui.chat.components.MessageInput
import com.travelai.ui.chat.components.SamplePromptChips
import com.travelai.ui.chat.components.SamplePromptOption
import com.travelai.ui.components.AppBackground
import com.travelai.ui.components.AppBottomBar
import com.travelai.ui.components.AppTopBar
import com.travelai.ui.components.BottomNavDestination
import com.travelai.ui.components.EmptyStateCard
import com.travelai.ui.components.TopBarIconButton
import com.travelai.ui.share.shareTripText
import com.travelai.ui.theme.BorderSubtle
import com.travelai.ui.theme.BrandBlue
import com.travelai.ui.theme.BrandPink
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.BrandPurpleSoft
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.SurfaceCard
import com.travelai.ui.theme.TravelAITheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onOpenPlanner: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenItinerary: (Long) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // One-shot: auto-start trip flow signals navigation to Itinerary
    // once the first AI response is saved.
    LaunchedEffect(uiState.navigateToItinerarySessionId) {
        uiState.navigateToItinerarySessionId?.let { sessionId ->
            onOpenItinerary(sessionId)
            viewModel.consumeNavigateToItinerary()
        }
    }

    ChatScreenContent(
        uiState = uiState,
        onInputChange = viewModel::onInputChange,
        onSend = viewModel::sendMessage,
        onRetry = viewModel::retryLastMessage,
        onShare = viewModel::shareCurrentSession,
        onShareConsumed = viewModel::consumeShareText,
        onSampleChipClick = viewModel::startTripFromSample,
        onStartNewChat = viewModel::startNewChat,
        onOpenPlanner = onOpenPlanner,
        onOpenHistory = onOpenHistory,
        onOpenItinerary = onOpenItinerary,
        onOpenProfile = onOpenProfile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    uiState: ChatUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    onShareConsumed: () -> Unit,
    onSampleChipClick: (SamplePromptOption) -> Unit,
    onStartNewChat: () -> Unit,
    onOpenPlanner: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenItinerary: (Long) -> Unit,
    onOpenProfile: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(uiState.shareText) {
        uiState.shareText?.let { exportText ->
            shareTripText(context, exportText)
            onShareConsumed()
        }
    }

    AppBackground {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            containerColor = Color.Transparent,
            topBar = {
                AppTopBar(
                    title = "TravelAI",
                    subtitle = "Trợ lý du lịch của bạn",
                    actions = {
                        TopBarIconButton(
                            icon = Icons.Filled.Edit,
                            contentDescription = "Cuộc trò chuyện mới",
                            onClick = onStartNewChat
                        )
                        TopBarIconButton(
                            icon = Icons.Filled.History,
                            contentDescription = "Lịch sử trò chuyện",
                            onClick = onOpenHistory
                        )
                        TopBarIconButton(
                            icon = Icons.Filled.Share,
                            contentDescription = "Chia sẻ",
                            onClick = onShare
                        )
                        TopBarIconButton(
                            icon = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Xem lịch trình",
                            onClick = { uiState.sessionId?.let(onOpenItinerary) },
                            tinted = true
                        )
                    }
                )
            },
            bottomBar = {
                Column {
                    MessageInput(
                        value = uiState.inputText,
                        onValueChange = onInputChange,
                        onSend = onSend,
                        isLoading = uiState.isLoading
                    )
                    AppBottomBar(
                        selected = BottomNavDestination.Chat,
                        onSelected = { destination ->
                            when (destination) {
                                BottomNavDestination.Home -> onOpenPlanner()
                                // Tapping Chat while already on Chat starts a
                                // fresh new conversation. Previous sessions
                                // remain reachable via History.
                                BottomNavDestination.Chat -> onStartNewChat()
                                BottomNavDestination.Itinerary ->
                                    uiState.sessionId?.let(onOpenItinerary) ?: onOpenPlanner()
                                BottomNavDestination.Saved -> onOpenHistory()
                                BottomNavDestination.Profile -> onOpenProfile()
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            ChatMessages(
                uiState = uiState,
                onRetry = onRetry,
                onCopyAssistantMessage = { content ->
                    clipboardManager.setText(AnnotatedString(content))
                    Toast.makeText(context, "Đã copy lịch trình", Toast.LENGTH_SHORT).show()
                },
                onSampleChipClick = onSampleChipClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@Composable
private fun ChatMessages(
    uiState: ChatUiState,
    onRetry: () -> Unit,
    onCopyAssistantMessage: (String) -> Unit,
    onSampleChipClick: (SamplePromptOption) -> Unit,
    modifier: Modifier = Modifier
) {
    if (
        uiState.messages.isEmpty() &&
        !uiState.isLoading &&
        uiState.errorMessage == null &&
        uiState.offlineBannerMessage == null
    ) {
        EmptyChatPlaceholder(
            onSampleChipClick = onSampleChipClick,
            modifier = modifier
        )
        return
    }

    val listState = rememberLazyListState()
    val itemCount = uiState.messages.size +
        (if (uiState.offlineBannerMessage != null) 1 else 0) +
        (if (uiState.isLoading) 1 else 0) +
        (if (uiState.errorMessage != null) 1 else 0)

    LaunchedEffect(itemCount) {
        if (itemCount > 0) {
            listState.animateScrollToItem(itemCount - 1)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        uiState.offlineBannerMessage?.let { offlineMessage ->
            item(key = "offline-banner") {
                OfflineBanner(message = offlineMessage)
            }
        }

        itemsIndexed(
            items = uiState.messages,
            key = { index, _ -> "message-$index" }
        ) { index, message ->
            ChatBubble(
                role = message.role,
                content = message.content,
                index = index,
                onLongPress = if (message.role == ChatRole.ASSISTANT) {
                    { onCopyAssistantMessage(message.content) }
                } else {
                    null
                }
            )
        }

        if (uiState.isLoading) {
            item(key = "loading") {
                AssistantLoadingIndicator()
            }
        }

        uiState.errorMessage?.let { errorMessage ->
            item(key = "error") {
                ErrorMessageCard(
                    message = errorMessage,
                    canRetry = uiState.canRetry,
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun OfflineBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorMessageCard(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
            if (canRetry) {
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Thử lại")
                }
            }
        }
    }
}

@Composable
private fun EmptyChatPlaceholder(
    onSampleChipClick: (SamplePromptOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmptyStateCard(
            title = "Bắt đầu cuộc trò chuyện",
            body = "Hỏi tôi về điểm đến, ngân sách, lịch trình hoặc một trải nghiệm bạn muốn thử trong chuyến đi."
        )
        Spacer(Modifier.size(8.dp))
        SamplePromptChips(
            onChipClick = onSampleChipClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AssistantLoadingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(BrandPurple, BrandBlue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = OnBrand,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(8.dp))

        Surface(
            color = SurfaceCard,
            shape = MaterialTheme.shapes.medium,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "dot$index")
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotAlpha$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(BrandPurple.copy(alpha = dotAlpha))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    TravelAITheme {
        ChatScreenContent(
            uiState = ChatUiState(
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.USER,
                        content = "Gợi ý 3 ngày Đà Nẵng"
                    ),
                    ChatMessage(
                        role = ChatRole.ASSISTANT,
                        content = "Bạn có thể dành ngày đầu tiên để khám phá bán đảo Sơn Trà."
                    )
                )
            ),
            onInputChange = {},
            onSend = {},
            onRetry = {},
            onShare = {},
            onShareConsumed = {},
            onSampleChipClick = {},
            onStartNewChat = {},
            onOpenPlanner = {},
            onOpenHistory = {},
            onOpenItinerary = {},
            onOpenProfile = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyChatScreenPreview() {
    TravelAITheme {
        ChatScreenContent(
            uiState = ChatUiState(),
            onInputChange = {},
            onSend = {},
            onRetry = {},
            onShare = {},
            onShareConsumed = {},
            onSampleChipClick = {},
            onStartNewChat = {},
            onOpenPlanner = {},
            onOpenHistory = {},
            onOpenItinerary = {},
            onOpenProfile = {}
        )
    }
}
