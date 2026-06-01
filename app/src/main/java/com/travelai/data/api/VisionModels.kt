package com.travelai.data.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// Request -------------------------------------------------------------------

data class VisionChatRequest(
    val model: String,
    val messages: List<VisionMessage>,
    @SerializedName("max_tokens")
    val maxTokens: Int,
    val temperature: Double = 0.2,
    @SerializedName("response_format")
    val responseFormat: VisionResponseFormat? = null
)

data class VisionResponseFormat(
    val type: String = "json_object"
)

data class VisionMessage(
    val role: String,
    // For system messages content is a plain string; for user messages it is a list of parts.
    // We keep it as Any so Gson serializes whichever shape is supplied.
    val content: Any
)

sealed class VisionContentPart {
    data class Text(val text: String) : VisionContentPart()
    data class Image(val dataUrl: String) : VisionContentPart()
}

// Response ------------------------------------------------------------------

data class VisionChatResponse(
    val choices: List<VisionChoice> = emptyList()
)

data class VisionChoice(
    val message: VisionResponseMessage
)

data class VisionResponseMessage(
    val role: String = "assistant",
    val content: String = ""
)

// Gson adapter so VisionContentPart serializes to the OpenAI-compatible shape:
//   { "type": "text", "text": "..." }
//   { "type": "image_url", "image_url": { "url": "data:image/jpeg;base64,..." } }
class VisionContentPartAdapter :
    JsonSerializer<VisionContentPart>,
    JsonDeserializer<VisionContentPart> {

    override fun serialize(
        src: VisionContentPart,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val obj = JsonObject()
        when (src) {
            is VisionContentPart.Text -> {
                obj.add("type", JsonPrimitive("text"))
                obj.add("text", JsonPrimitive(src.text))
            }
            is VisionContentPart.Image -> {
                obj.add("type", JsonPrimitive("image_url"))
                val inner = JsonObject().apply {
                    add("url", JsonPrimitive(src.dataUrl))
                }
                obj.add("image_url", inner)
            }
        }
        return obj
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): VisionContentPart {
        val obj = json.asJsonObject
        return when (obj.get("type")?.asString) {
            "image_url" -> VisionContentPart.Image(
                dataUrl = obj.getAsJsonObject("image_url")?.get("url")?.asString.orEmpty()
            )
            else -> VisionContentPart.Text(text = obj.get("text")?.asString.orEmpty())
        }
    }
}
