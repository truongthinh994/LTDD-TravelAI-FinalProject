package com.travelai.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.travelai.data.api.ApiClient
import com.travelai.data.api.DeepSeekApi
import com.travelai.data.api.DeepSeekChatRequest
import com.travelai.data.api.DeepSeekMessage
import com.travelai.data.api.DeepSeekStreamChunk
import com.travelai.data.db.ChatDao
import com.travelai.data.db.entities.BudgetItemEntity
import com.travelai.data.db.entities.ChecklistItemEntity
import com.travelai.data.db.entities.ChatMessageEntity
import com.travelai.data.db.entities.ChatSessionEntity
import com.travelai.data.db.entities.TripMapPlaceEntity
import com.travelai.data.db.entities.TripPlanSnapshotEntity
import com.travelai.data.db.entities.TripProfileEntity
import com.travelai.data.model.BudgetCategory
import com.travelai.data.model.BudgetItem
import com.travelai.data.model.ChecklistItem
import com.travelai.data.model.WeatherDay
import com.travelai.data.model.TripExport
import com.travelai.data.model.TripMapData
import com.travelai.data.model.TripMapPlace
import com.travelai.data.model.TripMapPlaceCandidate
import com.travelai.data.model.TripMapPlaceStatus
import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.TripPlanPeriodType
import com.travelai.data.model.TripPlanSnapshot
import com.travelai.data.model.TripProfile
import com.travelai.data.model.buildSmartChecklistPrompt
import com.travelai.data.model.parseSmartChecklistResponse
import com.travelai.data.model.toShareText
import com.travelai.data.model.toSessionTitle
import com.travelai.data.parser.ItineraryParser
import com.travelai.data.parser.MapPlaceExtractor
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

class ChatRepository @Inject constructor(
    private val deepSeekApi: DeepSeekApi,
    private val chatDao: ChatDao,
    @Named("DeepSeekApiKey") private val apiKey: String,
    @Named("DeepSeekOkHttpClient") private val streamingClient: OkHttpClient
) {
    private val gson = Gson()
    private val jsonMediaType = "application/json".toMediaType()

    suspend fun loadLatestSession(): StoredChatSession? {
        val session = chatDao.getLatestSession() ?: return null
        return loadSessionFromEntity(session)
    }

    suspend fun loadSession(sessionId: Long): StoredChatSession? {
        val session = chatDao.getSession(sessionId) ?: return null
        return loadSessionFromEntity(session)
    }

    suspend fun getSessions(): List<StoredChatSessionSummary> =
        chatDao.getSessions().map { session ->
            StoredChatSessionSummary(
                id = session.id,
                title = session.title,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt,
                isPinned = session.isPinned
            )
        }

    private suspend fun loadSessionFromEntity(session: ChatSessionEntity): StoredChatSession {
        val messages = chatDao.getMessagesForSession(session.id)
        val tripProfile = chatDao.getTripProfile(session.id)
        val tripPlanSnapshot = chatDao.getTripPlanSnapshot(session.id)
        val budgetItems = chatDao.getBudgetItems(session.id)
        val checklistItems = chatDao.getChecklistItems(session.id)

        return StoredChatSession(
            id = session.id,
            title = session.title,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            isPinned = session.isPinned,
            tripProfile = tripProfile?.toTripProfile(),
            tripPlanSnapshot = tripPlanSnapshot?.toTripPlanSnapshot(gson),
            budgetItems = budgetItems.map { it.toBudgetItem() },
            checklistItems = checklistItems.map { it.toChecklistItem() },
            messages = messages.map {
                StoredChatMessage(
                    role = it.role,
                    content = it.content,
                    createdAt = it.createdAt
                )
            }
        )
    }

    suspend fun createTripSession(profile: TripProfile): Long {
        val now = System.currentTimeMillis()
        return chatDao.insertSessionAndProfile(
            session = ChatSessionEntity(
                title = profile.toSessionTitle(),
                createdAt = now,
                updatedAt = now
            ),
            profile = profile.toEntity(sessionId = 0L, createdAt = now)
        )
    }

    suspend fun createSession(firstMessage: String): Long {
        val now = System.currentTimeMillis()
        return chatDao.insertSession(
            ChatSessionEntity(
                title = createSessionTitle(firstMessage),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun saveMessage(sessionId: Long, role: String, content: String) {
        val now = System.currentTimeMillis()
        chatDao.insertMessageAndTouchSession(
            message = ChatMessageEntity(
                sessionId = sessionId,
                role = role,
                content = content,
                createdAt = now
            ),
            updatedAt = now
        )
    }

    suspend fun getBudgetItems(sessionId: Long): List<BudgetItem> =
        chatDao.getBudgetItems(sessionId).map { it.toBudgetItem() }

    suspend fun getChecklistItems(sessionId: Long): List<ChecklistItem> =
        chatDao.getChecklistItems(sessionId).map { it.toChecklistItem() }

    suspend fun loadLatestTripMapData(): TripMapData? {
        val session = chatDao.getLatestSession() ?: return null
        return loadTripMapData(session.id)
    }

    suspend fun loadTripMapData(sessionId: Long): TripMapData? {
        val session = loadSession(sessionId) ?: return null
        val fallbackText = session.tripPlanSnapshot?.rawResponse
            ?.takeIf { it.isNotBlank() }
            ?: session.messages
                .lastOrNull { it.role == ROLE_ASSISTANT }
                ?.content
                .orEmpty()
        val days = session.tripPlanSnapshot
            ?.days
            .orEmpty()
            .ifEmpty { ItineraryParser.parseDays(fallbackText) }
        val destination = session.tripProfile?.destination.orEmpty()
        val candidates = MapPlaceExtractor.extract(
            days = days,
            destination = destination
        )
        val places = syncTripMapPlaces(
            sessionId = session.id,
            candidates = candidates
        )

        return TripMapData(
            sessionId = session.id,
            title = session.title,
            destination = destination,
            places = places
        )
    }

    suspend fun getTripMapPlaces(sessionId: Long): List<TripMapPlace> =
        chatDao.getTripMapPlaces(sessionId).map { it.toTripMapPlace() }

    suspend fun updateTripMapPlaceGeocode(
        sessionId: Long,
        placeId: Long,
        latitude: Double?,
        longitude: Double?,
        status: TripMapPlaceStatus
    ) {
        chatDao.updateTripMapPlaceGeocode(
            sessionId = sessionId,
            placeId = placeId,
            latitude = latitude,
            longitude = longitude,
            status = status.name,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun addBudgetItem(
        sessionId: Long,
        category: BudgetCategory,
        title: String,
        amountVnd: Long,
        note: String
    ): BudgetItem {
        val now = System.currentTimeMillis()
        val itemId = chatDao.insertBudgetItemAndTouchSession(
            item = BudgetItemEntity(
                sessionId = sessionId,
                category = category.name,
                title = title.trim(),
                amountVnd = amountVnd,
                note = note.trim(),
                createdAt = now,
                updatedAt = now
            ),
            updatedAt = now
        )
        return BudgetItem(
            id = itemId,
            sessionId = sessionId,
            category = category,
            title = title.trim(),
            amountVnd = amountVnd,
            note = note.trim(),
            createdAt = now,
            updatedAt = now
        )
    }

    suspend fun updateBudgetItem(
        sessionId: Long,
        itemId: Long,
        category: BudgetCategory,
        title: String,
        amountVnd: Long,
        note: String
    ) {
        val now = System.currentTimeMillis()
        chatDao.updateBudgetItemAndTouchSession(
            sessionId = sessionId,
            itemId = itemId,
            category = category.name,
            title = title.trim(),
            amountVnd = amountVnd,
            note = note.trim(),
            updatedAt = now
        )
    }

    suspend fun deleteBudgetItem(sessionId: Long, itemId: Long) {
        chatDao.deleteBudgetItemAndTouchSession(
            sessionId = sessionId,
            itemId = itemId,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun addChecklistItem(
        sessionId: Long,
        title: String
    ): ChecklistItem {
        val now = System.currentTimeMillis()
        val cleanTitle = title.trim()
        val itemId = chatDao.insertChecklistItemAndTouchSession(
            item = ChecklistItemEntity(
                sessionId = sessionId,
                title = cleanTitle,
                isChecked = false,
                createdAt = now,
                updatedAt = now
            ),
            updatedAt = now
        )
        return ChecklistItem(
            id = itemId,
            sessionId = sessionId,
            title = cleanTitle,
            isChecked = false,
            createdAt = now,
            updatedAt = now
        )
    }

    suspend fun updateChecklistItemChecked(
        sessionId: Long,
        itemId: Long,
        isChecked: Boolean
    ) {
        val now = System.currentTimeMillis()
        chatDao.updateChecklistItemCheckedAndTouchSession(
            sessionId = sessionId,
            itemId = itemId,
            isChecked = isChecked,
            updatedAt = now
        )
    }

    suspend fun deleteChecklistItem(sessionId: Long, itemId: Long) {
        chatDao.deleteChecklistItemAndTouchSession(
            sessionId = sessionId,
            itemId = itemId,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun renameSession(sessionId: Long, title: String) {
        val cleanTitle = title.trim()
        require(cleanTitle.isNotBlank()) { "Tên chuyến đi không được để trống." }
        chatDao.renameSession(
            sessionId = sessionId,
            title = cleanTitle.take(SESSION_TITLE_MAX_LENGTH).trimEnd(),
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updateSessionPinned(sessionId: Long, isPinned: Boolean) {
        chatDao.updateSessionPinned(sessionId = sessionId, isPinned = isPinned)
    }

    suspend fun deleteSession(sessionId: Long) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun createTripExportText(sessionId: Long): String? =
        loadSession(sessionId)?.toTripExport()?.toShareText()

    /**
     * Materialise the session into a [TripExport] usable by both the text and
     * PDF share paths. Returns null when the session is missing.
     */
    suspend fun loadTripExport(sessionId: Long): TripExport? =
        loadSession(sessionId)?.toTripExport()

    suspend fun saveTripPlanSnapshot(
        sessionId: Long,
        rawResponse: String,
        keepRawWhenUnparsed: Boolean
    ): TripPlanSnapshot? {
        if (rawResponse.isBlank()) return null

        val parsedDays = ItineraryParser.parseDays(rawResponse)
        if (parsedDays.isEmpty() && !keepRawWhenUnparsed) return null

        val now = System.currentTimeMillis()
        val existingSnapshot = chatDao.getTripPlanSnapshot(sessionId)
        val snapshot = TripPlanSnapshot(
            sessionId = sessionId,
            rawResponse = rawResponse,
            days = parsedDays,
            createdAt = existingSnapshot?.createdAt ?: now,
            updatedAt = now
        )
        chatDao.upsertTripPlanSnapshot(snapshot.toEntity(gson))
        return snapshot
    }

    suspend fun updateTripPlanSnapshot(
        sessionId: Long,
        days: List<TripPlanDay>
    ): TripPlanSnapshot? {
        if (days.isEmpty()) return null
        val now = System.currentTimeMillis()
        val existingSnapshot = chatDao.getTripPlanSnapshot(sessionId)
        val snapshot = TripPlanSnapshot(
            sessionId = sessionId,
            rawResponse = days.toItineraryRawText(),
            days = days,
            createdAt = existingSnapshot?.createdAt ?: now,
            updatedAt = now
        )
        chatDao.upsertTripPlanSnapshot(snapshot.toEntity(gson))
        return snapshot
    }

    suspend fun generateSmartChecklist(
        profile: TripProfile?,
        days: List<TripPlanDay>,
        weatherDays: List<WeatherDay>
    ): List<String> {
        val raw = sendMessage(
            messages = listOf(
                DeepSeekMessage(
                    role = ROLE_SYSTEM,
                    content = "Bạn là trợ lý chuẩn bị hành lý du lịch cho người Việt. Trả lời ngắn, thực tế, dễ tick checklist."
                ),
                DeepSeekMessage(
                    role = ROLE_USER,
                    content = buildSmartChecklistPrompt(
                        profile = profile,
                        days = days,
                        weatherDays = weatherDays
                    )
                )
            )
        )
        return parseSmartChecklistResponse(raw)
    }

    suspend fun regenerateTripPlanDay(
        profile: TripProfile?,
        currentDays: List<TripPlanDay>,
        dayNumber: Int
    ): TripPlanDay? {
        val currentDay = currentDays.firstOrNull { it.dayNumber == dayNumber } ?: return null
        val prompt = buildString {
            appendLine("Hãy tạo lại riêng Ngày $dayNumber cho lịch trình du lịch.")
            appendLine("Giữ đúng format: Ngày $dayNumber, rồi 3 mục Sáng / Chiều / Tối.")
            appendLine("Không tạo lại các ngày khác. Không dùng markdown heading hoặc emoji ở tiêu đề.")
            appendLine()
            if (profile != null) {
                appendLine("Thông tin chuyến đi:")
                appendLine("- Điểm đến: ${profile.destination}")
                appendLine("- Số ngày: ${profile.days}")
                appendLine("- Số người: ${profile.people}")
                appendLine("- Ngân sách: ${profile.budget}")
                appendLine("- Phong cách: ${profile.travelStyle}")
                appendLine("- Phương tiện: ${profile.transport}")
                appendLine("- Ghi chú: ${profile.note}")
            }
            appendLine()
            appendLine("Ngày hiện tại cần cải thiện:")
            append(currentDay.toItineraryRawText())
        }

        val raw = sendMessage(
            messages = listOf(
                DeepSeekMessage(
                    role = ROLE_SYSTEM,
                    content = "Bạn là trợ lý lập lịch trình du lịch. Luôn trả lời bằng tiếng Việt, đúng format Ngày/Sáng/Chiều/Tối."
                ),
                DeepSeekMessage(role = ROLE_USER, content = prompt)
            )
        )
        return ItineraryParser.parseDays(raw)
            .firstOrNull()
            ?.copy(dayNumber = dayNumber)
    }

    suspend fun sendMessage(messages: List<DeepSeekMessage>): String {
        if (messages.none { it.role == ROLE_USER && it.content.isNotBlank() }) {
            throw IllegalArgumentException("Tin nhắn không được để trống.")
        }
        if (apiKey.isBlank()) {
            throw IllegalStateException("Thiếu DEEPSEEK_API_KEY trong local.properties.")
        }

        val response = deepSeekApi.sendMessage(
            DeepSeekChatRequest(
                messages = messages
            )
        )

        return response.choices
            .firstOrNull()
            ?.message
            ?.content
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("DeepSeek không trả về nội dung.")
    }

    /**
     * Stream a chat completion via Server-Sent Events.
     *
     * Emits each incremental content fragment ("delta") from the model in
     * order. The flow completes when the API sends `data: [DONE]`. If the
     * stream fails mid-way (HTTP error, network drop, malformed payload at
     * the SSE level), the flow throws.
     *
     * Callers are expected to accumulate the emitted fragments themselves —
     * the repository deliberately does not buffer the full response so the
     * UI can render incrementally.
     */
    fun streamMessage(messages: List<DeepSeekMessage>): Flow<String> = callbackFlow {
        if (messages.none { it.role == ROLE_USER && it.content.isNotBlank() }) {
            close(IllegalArgumentException("Tin nhắn không được để trống."))
            return@callbackFlow
        }
        if (apiKey.isBlank()) {
            close(IllegalStateException("Thiếu DEEPSEEK_API_KEY trong local.properties."))
            return@callbackFlow
        }

        val requestJson = gson.toJson(
            DeepSeekChatRequest(
                messages = messages,
                stream = true
            )
        )
        val request = Request.Builder()
            .url(ApiClient.BASE_URL + "chat/completions")
            .post(requestJson.toRequestBody(jsonMediaType))
            .addHeader("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data == STREAM_DONE_SENTINEL) {
                    close()
                    return
                }
                val chunk = runCatching {
                    gson.fromJson(data, DeepSeekStreamChunk::class.java)
                }.getOrNull() ?: return
                val content = chunk.choices.firstOrNull()?.delta?.content
                if (!content.isNullOrEmpty()) {
                    trySend(content)
                }
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                val cause = t ?: IllegalStateException(
                    "DeepSeek stream lỗi HTTP ${response?.code ?: "?"}"
                )
                close(cause)
            }
        }

        val eventSource = EventSources.createFactory(streamingClient)
            .newEventSource(request, listener)

        awaitClose { eventSource.cancel() }
    }

    private fun createSessionTitle(firstMessage: String): String {
        val title = firstMessage.trim().lineSequence().firstOrNull().orEmpty()
        if (title.isBlank()) return "New trip"
        return if (title.length <= SESSION_TITLE_MAX_LENGTH) {
            title
        } else {
            title.take(SESSION_TITLE_MAX_LENGTH).trimEnd()
        }
    }

    private companion object {
        const val ROLE_SYSTEM = "system"
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"
        const val SESSION_TITLE_MAX_LENGTH = 60
        const val STREAM_DONE_SENTINEL = "[DONE]"
    }

    private suspend fun syncTripMapPlaces(
        sessionId: Long,
        candidates: List<TripMapPlaceCandidate>
    ): List<TripMapPlace> {
        val existingPlaces = chatDao.getTripMapPlaces(sessionId)
        val existingByKey = existingPlaces.associateBy { it.mapCacheKey() }
        val now = System.currentTimeMillis()
        val mergedPlaces = candidates.map { candidate ->
            val existing = existingByKey[candidate.mapCacheKey()]
            TripMapPlaceEntity(
                id = existing?.id ?: 0L,
                sessionId = sessionId,
                dayNumber = candidate.dayNumber,
                period = candidate.period.name,
                name = candidate.name,
                query = candidate.query,
                latitude = existing?.latitude,
                longitude = existing?.longitude,
                status = existing?.status ?: TripMapPlaceStatus.PENDING.name,
                createdAt = existing?.createdAt ?: now,
                updatedAt = existing?.updatedAt ?: now
            )
        }

        chatDao.replaceTripMapPlacesForSession(
            sessionId = sessionId,
            places = mergedPlaces
        )

        return chatDao.getTripMapPlaces(sessionId).map { it.toTripMapPlace() }
    }
}

private fun TripProfileEntity.toTripProfile(): TripProfile = TripProfile(
    destination = destination,
    days = days,
    budget = budget,
    people = people,
    travelStyle = travelStyle,
    transport = transport,
    note = note
)

private fun TripProfile.toEntity(
    sessionId: Long,
    createdAt: Long
): TripProfileEntity = TripProfileEntity(
    sessionId = sessionId,
    destination = destination.trim(),
    days = days,
    budget = budget.trim(),
    people = people,
    travelStyle = travelStyle.trim(),
    transport = transport.trim(),
    note = note.trim(),
    createdAt = createdAt
)

data class StoredChatSession(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val tripProfile: TripProfile?,
    val tripPlanSnapshot: TripPlanSnapshot?,
    val budgetItems: List<BudgetItem>,
    val checklistItems: List<ChecklistItem>,
    val messages: List<StoredChatMessage>
)

data class StoredChatSessionSummary(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean
)

data class StoredChatMessage(
    val role: String,
    val content: String,
    val createdAt: Long
)

private fun TripPlanSnapshotEntity.toTripPlanSnapshot(gson: Gson): TripPlanSnapshot =
    TripPlanSnapshot(
        sessionId = sessionId,
        rawResponse = rawResponse,
        days = parsedDays(gson),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

private fun TripPlanSnapshotEntity.parsedDays(gson: Gson): List<TripPlanDay> {
    val json = parsedJson?.takeIf { it.isNotBlank() } ?: return emptyList()
    return runCatching {
        gson.fromJson(json, TripPlanSnapshotPayload::class.java)?.days.orEmpty()
    }.getOrDefault(emptyList())
}

private fun TripPlanSnapshot.toEntity(gson: Gson): TripPlanSnapshotEntity =
    TripPlanSnapshotEntity(
        sessionId = sessionId,
        rawResponse = rawResponse,
        parsedJson = days.takeIf { it.isNotEmpty() }?.let { parsedDays ->
            gson.toJson(TripPlanSnapshotPayload(days = parsedDays))
        },
        createdAt = createdAt,
        updatedAt = updatedAt
    )

private data class TripPlanSnapshotPayload(
    val days: List<TripPlanDay>
)

private fun List<TripPlanDay>.toItineraryRawText(): String =
    joinToString(separator = "\n\n") { it.toItineraryRawText() }

private fun TripPlanDay.toItineraryRawText(): String = buildString {
    appendLine(title.ifBlank { "Ngày $dayNumber" })
    periods.forEach { period ->
        appendLine("${period.period.label}:")
        appendLine(period.content)
        appendLine()
    }
}.trim()

private fun BudgetItemEntity.toBudgetItem(): BudgetItem = BudgetItem(
    id = id,
    sessionId = sessionId,
    category = category.toBudgetCategory(),
    title = title,
    amountVnd = amountVnd,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun String.toBudgetCategory(): BudgetCategory =
    runCatching { BudgetCategory.valueOf(this) }
        .getOrDefault(BudgetCategory.INCIDENTAL)

private fun ChecklistItemEntity.toChecklistItem(): ChecklistItem = ChecklistItem(
    id = id,
    sessionId = sessionId,
    title = title,
    isChecked = isChecked,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun TripMapPlaceEntity.toTripMapPlace(): TripMapPlace = TripMapPlace(
    id = id,
    sessionId = sessionId,
    dayNumber = dayNumber,
    period = period.toTripPlanPeriodType(),
    name = name,
    query = query,
    latitude = latitude,
    longitude = longitude,
    status = status.toTripMapPlaceStatus(),
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun TripMapPlaceEntity.mapCacheKey(): String =
    "$dayNumber|$period|${name.normalizedMapName()}"

private fun TripMapPlaceCandidate.mapCacheKey(): String =
    "$dayNumber|${period.name}|${name.normalizedMapName()}"

private fun String.normalizedMapName(): String =
    lowercase()
        .replace(Regex("\\s+"), " ")
        .trim()

private fun String.toTripMapPlaceStatus(): TripMapPlaceStatus =
    runCatching { TripMapPlaceStatus.valueOf(this) }
        .getOrDefault(TripMapPlaceStatus.PENDING)

private fun String.toTripPlanPeriodType(): TripPlanPeriodType =
    runCatching { TripPlanPeriodType.valueOf(this) }
        .getOrDefault(TripPlanPeriodType.MORNING)

private fun StoredChatSession.toTripExport(): TripExport =
    TripExport(
        title = title,
        tripProfile = tripProfile,
        tripPlanSnapshot = tripPlanSnapshot,
        budgetItems = budgetItems,
        checklistItems = checklistItems,
        fallbackAssistantText = messages.asReversed()
            .firstOrNull { it.role == "assistant" }
            ?.content
    )
