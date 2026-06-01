package com.travelai.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.travelai.ui.theme.BrandPurple
import com.travelai.ui.theme.GlassHighlight
import com.travelai.ui.theme.GlassSurfaceL1
import com.travelai.ui.theme.InkPrimary
import com.travelai.ui.theme.InkSecondary
import com.travelai.ui.theme.OnBrand
import com.travelai.ui.theme.ShadowLevel
import com.travelai.ui.theme.SurfaceCard
import com.travelai.ui.theme.TravelAIGradients
import com.travelai.ui.theme.TravelAITheme
import com.travelai.ui.theme.bentoShadow

@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val canSend = value.isNotBlank() && !isLoading
    val dockShape = RoundedCornerShape(30.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .bentoShadow(ShadowLevel.L2, dockShape)
            .clip(dockShape)
            .border(1.dp, GlassHighlight, dockShape),
        color = GlassSurfaceL1,
        tonalElevation = 0.dp,
        shape = dockShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nhập tin nhắn...", color = InkSecondary) },
                minLines = 1,
                maxLines = 5,
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = InkPrimary,
                    unfocusedTextColor = InkPrimary,
                    focusedContainerColor = SurfaceCard.copy(alpha = 0.74f),
                    unfocusedContainerColor = SurfaceCard.copy(alpha = 0.58f),
                    disabledContainerColor = SurfaceCard.copy(alpha = 0.44f),
                    focusedBorderColor = BrandPurple.copy(alpha = 0.48f),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = BrandPurple
                )
            )

            IconButton(
                onClick = {
                    if (canSend) {
                        onSend()
                    }
                },
                enabled = canSend,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .then(
                        if (canSend) Modifier.background(TravelAIGradients.auroraDiagonal)
                        else Modifier.background(SurfaceCard)
                    ),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = OnBrand,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = InkSecondary.copy(alpha = 0.55f)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Gửi",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageInputPreview() {
    TravelAITheme {
        MessageInput(
            value = "Gợi ý 3 ngày Đà Nẵng",
            onValueChange = {},
            onSend = {},
            isLoading = false
        )
    }
}
