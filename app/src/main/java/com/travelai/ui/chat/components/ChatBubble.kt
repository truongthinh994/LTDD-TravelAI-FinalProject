package com.travelai.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.travelai.ui.chat.ChatRole
import com.travelai.ui.components.AppCard
import com.travelai.ui.components.AppCardVariant
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.TravelAIGradients
import com.travelai.ui.theme.TravelAITheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    role: ChatRole,
    content: String,
    index: Int = 0,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null
) {
    val isUser = role == ChatRole.USER
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220, delayMillis = index.coerceAtMost(8) * 40)) +
            scaleIn(initialScale = 0.96f, animationSpec = tween(220, delayMillis = index.coerceAtMost(8) * 40)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(TravelAIGradients.auroraDiagonal),
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
            }

            Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
                val bubbleShape = if (isUser) {
                    RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
                } else {
                    RoundedCornerShape(20.dp, 20.dp, 20.dp, 6.dp)
                }
                val pressModifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = onLongPress
                )

                if (isUser) {
                    Text(
                        text = content,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(bubbleShape)
                            .background(TravelAIGradients.auroraDiagonal)
                            .then(pressModifier)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = OnBrand,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    AppCard(
                        cornerRadius = 20,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        variant = AppCardVariant.Glass,
                        shadowLevel = ShadowLevel.L1,
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(bubbleShape)
                            .then(pressModifier)
                    ) {
                        Text(
                            text = content,
                            color = InkPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Text(
                    text = "vừa xong",
                    style = MaterialTheme.typography.labelSmall,
                    color = InkSecondary,
                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                )
            }

            if (isUser) {
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatBubblePreview() {
    TravelAITheme {
        ChatBubble(
            role = ChatRole.USER,
            content = "Gợi ý 3 ngày Đà Nẵng"
        )
    }
}
