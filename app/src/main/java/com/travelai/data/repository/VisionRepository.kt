package com.travelai.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.travelai.data.api.VisionApi
import com.travelai.data.api.VisionChatRequest
import com.travelai.data.api.VisionContentPart
import com.travelai.data.api.VisionMessage
import com.travelai.data.api.VisionResponseFormat
import com.travelai.data.model.LandmarkInfo
import com.travelai.util.ACTIVE_VISION_PROVIDER
import com.travelai.util.Constants
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class VisionRepository @Inject constructor(
    private val visionApi: VisionApi,
    @Named("VisionApiKey") private val apiKey: String
) {
    private val gson = Gson()

    suspend fun recognizeLandmark(bitmap: Bitmap): LandmarkInfo {
        check(apiKey.isNotBlank()) {
            "Thiếu API key cho nhà cung cấp vision. Hãy kiểm tra local.properties."
        }

        val resized = resizeIfNeeded(bitmap, maxDim = MAX_DIM_PX)
        val base64Jpeg = encodeToBase64Jpeg(resized, quality = JPEG_QUALITY)
        val dataUrl = "data:image/jpeg;base64,$base64Jpeg"

        val request = VisionChatRequest(
            model = ACTIVE_VISION_PROVIDER.model,
            maxTokens = Constants.LANDMARK_MAX_TOKENS,
            responseFormat = if (ACTIVE_VISION_PROVIDER.supportsJsonResponseFormat) {
                VisionResponseFormat(type = "json_object")
            } else {
                null
            },
            messages = listOf(
                VisionMessage(
                    role = "system",
                    content = Constants.LANDMARK_SYSTEM_PROMPT
                ),
                VisionMessage(
                    role = "user",
                    content = listOf(
                        VisionContentPart.Text(
                            "Hãy nhận diện địa điểm trong ảnh này và trả về JSON đúng schema."
                        ),
                        VisionContentPart.Image(dataUrl = dataUrl)
                    )
                )
            )
        )

        val response = visionApi.recognize(request)
        val raw = response.choices.firstOrNull()?.message?.content.orEmpty()
        check(raw.isNotBlank()) { "API trả về nội dung trống." }

        return parseLandmarkJson(raw)
    }

    private fun parseLandmarkJson(raw: String): LandmarkInfo {
        val cleaned = stripJsonFences(raw).trim()
        return try {
            gson.fromJson(cleaned, LandmarkJsonDto::class.java).toDomain()
        } catch (e: JsonSyntaxException) {
            throw IllegalStateException("Không đọc được phản hồi JSON từ AI: ${e.message}", e)
        }
    }

    private fun stripJsonFences(raw: String): String {
        val fencePattern = Regex("^```(?:json)?\\s*|\\s*```$", RegexOption.IGNORE_CASE)
        return raw.replace(fencePattern, "")
    }

    private fun resizeIfNeeded(source: Bitmap, maxDim: Int): Bitmap {
        val w = source.width
        val h = source.height
        val longest = maxOf(w, h)
        if (longest <= maxDim) return source
        val scale = maxDim.toFloat() / longest
        val newW = (w * scale).toInt().coerceAtLeast(1)
        val newH = (h * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, newW, newH, true)
    }

    private fun encodeToBase64Jpeg(bitmap: Bitmap, quality: Int): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val bytes = stream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    companion object {
        private const val MAX_DIM_PX = 1024
        private const val JPEG_QUALITY = 85
    }
}

// Internal DTO used only for Gson parsing.
private data class LandmarkJsonDto(
    val isLandmark: Boolean? = null,
    val name: String? = null,
    val location: String? = null,
    val description: String? = null,
    val history: String? = null,
    val tips: List<String>? = null,
    val confidence: Float? = null
) {
    fun toDomain(): LandmarkInfo = LandmarkInfo(
        isLandmark = isLandmark ?: false,
        name = name.orEmpty(),
        location = location.orEmpty(),
        description = description.orEmpty(),
        history = history.orEmpty(),
        tips = tips.orEmpty(),
        confidence = confidence ?: 0f
    )
}
