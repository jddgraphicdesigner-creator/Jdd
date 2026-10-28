package com.example.model

data class PlayItem(
    val id: String,
    val title: String,
    val channelName: String = "",
    val thumbnailUrl: String = "https://img.youtube.com/vi/$id/hqdefault.jpg",
    val originalUrl: String = "https://www.youtube.com/watch?v=$id",
    val duration: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val category: String = "General"
)
