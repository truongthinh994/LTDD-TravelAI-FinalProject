package com.travelai.data.api

import com.google.gson.annotations.SerializedName
import com.travelai.util.Constants

data class DeepSeekChatRequest(
    val messages: List<DeepSeekMessage>,
    val model: String = Constants.DEEPSEEK_MODEL,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = Constants.DEEPSEEK_MAX_TOKENS,
    val stream: Boolean = false
)

data class DeepSeekMessage(
    val role: String,
    val content: String
)

data class DeepSeekChatResponse(
    val choices: List<DeepSeekChoice> = emptyList()
)

data class DeepSeekChoice(
    val message: DeepSeekMessage
)

// ── Streaming SSE chunk shape (OpenAI-compatible) ─────────────────────────
// DeepSeek emits one `data: {json}` event per chunk and a final `data: [DONE]`
// to signal completion. Each chunk's `delta.content` carries the incremental
// text piece to append to the assistant message.

data class DeepSeekStreamChunk(
    val choices: List<DeepSeekStreamChoice> = emptyList()
)

data class DeepSeekStreamChoice(
    val delta: DeepSeekDelta? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

data class DeepSeekDelta(
    val role: String? = null,
    val content: String? = null
)
