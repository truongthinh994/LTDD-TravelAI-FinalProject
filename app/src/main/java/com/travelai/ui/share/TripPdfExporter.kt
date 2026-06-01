package com.travelai.ui.share

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.travelai.data.model.BudgetItem
import com.travelai.data.model.ChecklistItem
import com.travelai.data.model.TripExport
import com.travelai.data.model.TripPlanDay
import com.travelai.data.model.TripProfile
import com.travelai.data.model.WeatherDay
import com.travelai.data.model.formatBudgetAmount
import com.travelai.data.model.totalAmountVnd
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Render a [TripExport] to a sharable PDF in app cache and return a URI safe
 * to pass through `Intent.EXTRA_STREAM`.
 *
 * Layout: portrait A4 (595×842 pt), single column, with automatic pagination
 * when the running cursor crosses [PAGE_BOTTOM_MARGIN]. Vietnamese diacritics
 * render via the bundled Roboto font (covers Latin Extended-A/B).
 */
object TripPdfExporter {

    @Throws(IOException::class)
    fun export(
        context: Context,
        tripExport: TripExport,
        weatherDays: List<WeatherDay> = emptyList(),
        weatherAdvice: List<String> = emptyList()
    ): android.net.Uri {
        val document = PdfDocument()
        val drawer = PageDrawer(document)
        drawer.startPage()

        drawer.drawHeader(tripExport.title.ifBlank { DEFAULT_TITLE })
        drawer.drawProfile(tripExport.tripProfile)
        drawer.drawItinerary(tripExport)
        drawer.drawWeather(weatherDays, weatherAdvice)
        drawer.drawBudget(tripExport.budgetItems)
        drawer.drawChecklist(tripExport.checklistItems)
        drawer.drawFooter()
        drawer.finishPage()

        val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportsDir, "travelai_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private const val DEFAULT_TITLE = "Lịch trình TravelAI"

    // ── Page geometry (points; A4 portrait) ──────────────────────────────
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val PAGE_MARGIN = 40f
    private const val PAGE_BOTTOM_MARGIN = (PAGE_HEIGHT - 50).toFloat()
    private const val LINE_GAP_SMALL = 4f
    private const val LINE_GAP_MEDIUM = 8f
    private const val LINE_GAP_SECTION = 16f

    private class PageDrawer(private val document: PdfDocument) {
        private var page: PdfDocument.Page? = null
        private var cursorY = 0f
        private var pageNumber = 0

        private val titlePaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF07132F.toInt()
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val sectionPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF7B2FFF.toInt()
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val labelPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF53647F.toInt()
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        private val bodyPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF07132F.toInt()
            textSize = 11f
            typeface = Typeface.DEFAULT
        }
        private val mutedPaint = Paint().apply {
            isAntiAlias = true
            color = 0xFF8895AE.toInt()
            textSize = 9f
            typeface = Typeface.DEFAULT
        }

        fun startPage() {
            pageNumber++
            val info = PdfDocument.PageInfo
                .Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber)
                .create()
            page = document.startPage(info)
            cursorY = PAGE_MARGIN
        }

        fun finishPage() {
            page?.let { document.finishPage(it) }
            page = null
        }

        private fun ensureSpace(neededHeight: Float) {
            if (cursorY + neededHeight > PAGE_BOTTOM_MARGIN) {
                finishPage()
                startPage()
            }
        }

        private fun newline(gap: Float = LINE_GAP_SMALL) {
            cursorY += gap
        }

        private fun drawText(
            text: String,
            paint: Paint,
            x: Float = PAGE_MARGIN
        ) {
            val maxWidth = PAGE_WIDTH - PAGE_MARGIN * 2
            val lines = wrapText(text, paint, maxWidth)
            val lineHeight = paint.textSize + 4f
            lines.forEach { line ->
                ensureSpace(lineHeight)
                page?.canvas?.drawText(line, x, cursorY + paint.textSize, paint)
                cursorY += lineHeight
            }
        }

        fun drawHeader(title: String) {
            drawText(title, titlePaint)
            newline(LINE_GAP_SMALL)
            drawText("Trợ lý du lịch TravelAI", mutedPaint)
            newline(LINE_GAP_MEDIUM)
        }

        fun drawProfile(profile: TripProfile?) {
            if (profile == null) return
            newline(LINE_GAP_SECTION)
            drawText("Thông tin chuyến đi", sectionPaint)
            newline(LINE_GAP_SMALL)

            fun line(label: String, value: String) {
                if (value.isNotBlank()) {
                    drawText("$label: $value", bodyPaint)
                }
            }
            line("Điểm đến", profile.destination)
            line("Số ngày", profile.days.toString())
            line("Số người", profile.people.toString())
            line("Ngân sách", profile.budget)
            line("Phong cách", profile.travelStyle)
            line("Phương tiện", profile.transport)
            line("Ghi chú", profile.note)
        }

        fun drawItinerary(tripExport: TripExport) {
            val days = tripExport.tripPlanSnapshot?.days.orEmpty().sortedBy { it.dayNumber }
            val raw = tripExport.tripPlanSnapshot?.rawResponse?.trim().orEmpty()
            val fallback = tripExport.fallbackAssistantText?.trim().orEmpty()

            newline(LINE_GAP_SECTION)
            drawText("Lịch trình chi tiết", sectionPaint)
            newline(LINE_GAP_SMALL)

            when {
                days.isNotEmpty() -> days.forEach { drawDay(it) }
                raw.isNotBlank() -> drawText(raw, bodyPaint)
                fallback.isNotBlank() -> drawText(fallback, bodyPaint)
                else -> drawText("(Chưa có lịch trình)", mutedPaint)
            }
        }

        private fun drawDay(day: TripPlanDay) {
            newline(LINE_GAP_MEDIUM)
            val title = day.title.trim().takeIf { it.isNotBlank() }
            val header = if (title != null) "Ngày ${day.dayNumber}: $title" else "Ngày ${day.dayNumber}"
            drawText(header, labelPaint)
            day.periods.forEach { period ->
                val content = period.content.trim()
                if (content.isNotBlank()) {
                    drawText("• ${period.period.label}: $content", bodyPaint, x = PAGE_MARGIN + 8f)
                }
            }
        }

        fun drawWeather(
            days: List<WeatherDay>,
            advice: List<String>
        ) {
            if (days.isEmpty() && advice.isEmpty()) return
            newline(LINE_GAP_SECTION)
            drawText("Thời tiết & chuẩn bị", sectionPaint)
            newline(LINE_GAP_SMALL)

            days.take(7).forEach { day ->
                drawText(
                    "• ${day.date}: ${day.conditionLabel}, ${day.tempMinC}-${day.tempMaxC}°C, mưa ${day.rainChancePct}%",
                    bodyPaint
                )
            }
            advice.take(6).forEach { item ->
                drawText("• $item", bodyPaint)
            }
        }

        fun drawBudget(items: List<BudgetItem>) {
            if (items.isEmpty()) return
            newline(LINE_GAP_SECTION)
            drawText("Ngân sách dự kiến", sectionPaint)
            newline(LINE_GAP_SMALL)

            items.forEach { item ->
                val note = item.note.trim().takeIf { it.isNotBlank() }?.let { " — $it" }.orEmpty()
                drawText(
                    "• ${item.category.label}: ${item.title} — ${formatBudgetAmount(item.amountVnd)}$note",
                    bodyPaint
                )
            }
            drawText(
                "Tổng cộng: ${formatBudgetAmount(items.totalAmountVnd())}",
                labelPaint
            )
        }

        fun drawChecklist(items: List<ChecklistItem>) {
            if (items.isEmpty()) return
            newline(LINE_GAP_SECTION)
            drawText("Checklist chuẩn bị", sectionPaint)
            newline(LINE_GAP_SMALL)

            items.forEach { item ->
                val mark = if (item.isChecked) "☑" else "☐"
                drawText("$mark ${item.title}", bodyPaint)
            }
        }

        fun drawFooter() {
            newline(LINE_GAP_SECTION)
            drawText("Generated by TravelAI", mutedPaint)
        }

        /** Greedy word-wrap; falls back to char-wrap for long unbreakable words. */
        private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
            if (text.isBlank()) return listOf("")
            val out = mutableListOf<String>()
            text.split("\n").forEach { paragraph ->
                if (paint.measureText(paragraph) <= maxWidth) {
                    out += paragraph
                    return@forEach
                }
                val words = paragraph.split(" ")
                var current = StringBuilder()
                words.forEach { word ->
                    val candidate = if (current.isEmpty()) word else "$current $word"
                    if (paint.measureText(candidate) <= maxWidth) {
                        current = StringBuilder(candidate)
                    } else {
                        if (current.isNotEmpty()) {
                            out += current.toString()
                            current = StringBuilder()
                        }
                        // Word itself too long — char-break
                        if (paint.measureText(word) > maxWidth) {
                            var chunk = StringBuilder()
                            word.forEach { c ->
                                val nextChunk = chunk.toString() + c
                                if (paint.measureText(nextChunk) <= maxWidth) {
                                    chunk.append(c)
                                } else {
                                    out += chunk.toString()
                                    chunk = StringBuilder().append(c)
                                }
                            }
                            current = chunk
                        } else {
                            current = StringBuilder(word)
                        }
                    }
                }
                if (current.isNotEmpty()) out += current.toString()
            }
            return out
        }
    }
}
