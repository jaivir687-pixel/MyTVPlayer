package com.example.mytvplayer.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable // 👈 Compose UI को 60fps स्मूथ रखने के लिए
@Serializable
data class Movie(
    val id: String,
    val title: String,
    val description: String = "",
    val videoUrl: String = "", // Movies ke liye direct link, Series ke liye khali
    val thumbnailUrl: String,
    val category: String = "All Movies",
    val isSeries: Boolean = false,
    val episodes: List<Episode> = emptyList()
)

@Immutable // 👈 Episodes लिस्ट के लिए
@Serializable
data class Episode(
    val id: String,
    val title: String,
    val videoUrl: String
)