package com.travelai.util

object Constants {
    const val SYSTEM_PROMPT: String =
        "Bạn là TravelAI, trợ lý du lịch AI cho du khách Việt tự túc. " +
            "Luôn trả lời bằng tiếng Việt, thực tế, dễ làm theo, ưu tiên lịch trình theo ngày và buổi. " +
            "Khi người dùng hỏi về chuyến đi, hãy gợi ý địa điểm, thời lượng, thứ tự tham quan, ăn uống và lưu ý chi phí nếu phù hợp. " +
            "Nếu thiếu thông tin quan trọng như số ngày, điểm đến, ngân sách hoặc nhóm đi, hãy hỏi lại ngắn gọn trước khi lập lịch trình chi tiết."

    const val DEEPSEEK_MODEL: String = "deepseek-chat"
    const val DEEPSEEK_MAX_TOKENS: Int = 2048

    // Approximate input budget because the app does not ship a DeepSeek tokenizer.
    const val MAX_CONTEXT_CHARS: Int = 8000

    const val LANDMARK_SYSTEM_PROMPT: String =
        "Bạn là chuyên gia nhận diện địa điểm du lịch. Hãy phân tích ảnh và trả về JSON đúng schema sau " +
            "(CHỈ JSON, KHÔNG markdown, KHÔNG text thêm bên ngoài JSON):\n" +
            "{\"isLandmark\": true/false, \"name\": \"tên địa điểm\", \"location\": \"thành phố, quốc gia\", " +
            "\"description\": \"1-2 câu mô tả\", \"history\": \"lịch sử ngắn 2-3 câu\", " +
            "\"tips\": [\"mẹo 1\", \"mẹo 2\", \"mẹo 3\"], \"confidence\": 0.0-1.0}\n" +
            "Nếu ảnh KHÔNG phải địa danh nổi tiếng hoặc không nhận diện được: trả về \"isLandmark\": false " +
            "với các field text empty và confidence = 0.0.\n" +
            "Tất cả nội dung text phải bằng tiếng Việt."

    const val LANDMARK_MAX_TOKENS: Int = 1024
}

enum class VisionProvider(
    val baseUrl: String,
    val model: String,
    val supportsJsonResponseFormat: Boolean
) {
    GEMINI(
        baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai/",
        model = "gemini-2.5-flash",
        supportsJsonResponseFormat = true
    ),
    OPENCODE_ZEN(
        // OpenAI-compatible endpoint per OpenCode Zen docs (https://opencode.ai/docs/zen/).
        // gemini-3.5-flash chosen because Gemini family natively supports the
        // OpenAI image_url multimodal format (verified earlier). Swap to
        // claude-sonnet-4-6, gpt-5.5, etc. by editing this one line.
        baseUrl = "https://opencode.ai/zen/v1/",
        model = "qwen3.6-plus",
        supportsJsonResponseFormat = false
    )
}

// ⭐ Switch vision provider tại đây — chỉ 1 dòng:
val ACTIVE_VISION_PROVIDER: VisionProvider = VisionProvider.GEMINI
