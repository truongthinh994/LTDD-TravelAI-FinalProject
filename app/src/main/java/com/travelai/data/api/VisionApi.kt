package com.travelai.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface VisionApi {
    @POST("chat/completions")
    suspend fun recognize(
        @Body request: VisionChatRequest
    ): VisionChatResponse
}
