package com.travelai.ui.share

import android.content.Context
import android.content.Intent
import android.net.Uri

fun shareTripText(
    context: Context,
    exportText: String
) {
    if (exportText.isBlank()) return

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, exportText)
    }
    context.startActivity(
        Intent.createChooser(sendIntent, "Chia sẻ lịch trình")
    )
}

/**
 * Open the system share sheet with the given PDF [uri] (must be a content://
 * URI from FileProvider so external apps can read it).
 */
fun shareTripPdf(
    context: Context,
    uri: Uri
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(sendIntent, "Chia sẻ lịch trình PDF")
    )
}
