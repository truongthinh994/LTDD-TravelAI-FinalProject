package com.travelai.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import com.travelai.data.db.LandmarkScanDao
import com.travelai.data.db.entities.LandmarkScanEntity
import com.travelai.data.model.LandmarkInfo
import com.travelai.data.model.LandmarkScan
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LandmarkScanRepository @Inject constructor(
    private val dao: LandmarkScanDao,
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    private val imagesDir: File
        get() = File(context.filesDir, IMAGES_DIR_NAME).apply { mkdirs() }

    fun observeHistory(): Flow<List<LandmarkScan>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun save(bitmap: Bitmap, info: LandmarkInfo): Long = withContext(Dispatchers.IO) {
        val createdAt = System.currentTimeMillis()
        val file = File(imagesDir, "$createdAt.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
        dao.insert(
            LandmarkScanEntity(
                name = info.name,
                location = info.location,
                description = info.description,
                history = info.history,
                tipsJson = gson.toJson(info.tips),
                confidence = info.confidence,
                imagePath = file.absolutePath,
                createdAt = createdAt
            )
        )
    }

    suspend fun getById(id: Long): LandmarkScan? = withContext(Dispatchers.IO) {
        dao.getById(id)?.toDomain()
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.getById(id)?.let { entity ->
            runCatching { File(entity.imagePath).delete() }
        }
        dao.deleteById(id)
    }

    private fun LandmarkScanEntity.toDomain(): LandmarkScan {
        val tips = runCatching {
            gson.fromJson(tipsJson, Array<String>::class.java).toList()
        }.getOrDefault(emptyList())
        return LandmarkScan(
            id = id,
            info = LandmarkInfo(
                isLandmark = true,
                name = name,
                location = location,
                description = description,
                history = history,
                tips = tips,
                confidence = confidence
            ),
            imagePath = imagePath,
            createdAt = createdAt
        )
    }

    companion object {
        private const val IMAGES_DIR_NAME = "landmarks"
        private const val JPEG_QUALITY = 85
    }
}
