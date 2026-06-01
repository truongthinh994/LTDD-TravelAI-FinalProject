package com.travelai.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface DeepSeekApi {
    @POST("chat/completions")
    suspend fun sendMessage(
        @Body request: DeepSeekChatRequest
    ): DeepSeekChatResponse
}
