package com.travelai.data.model

data class LandmarkScan(
    val id: Long,
    val info: LandmarkInfo,
    val imagePath: String,
    val createdAt: Long
)
