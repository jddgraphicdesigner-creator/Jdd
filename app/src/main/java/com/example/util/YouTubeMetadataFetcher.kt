package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class YouTubeOEmbedMeta(
    val title: String,
    val authorName: String,
    val thumbnailUrl: String
)

object YouTubeMetadataFetcher {

    /**
     * Fetches real video title and channel author from YouTube's public oEmbed endpoint.
     */
    suspend fun fetchMetadata(videoId: String): YouTubeOEmbedMeta? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
            val url = URL(oembedUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val title = json.optString("title", "")
                val author = json.optString("author_name", "")
                val thumb = json.optString("thumbnail_url", "https://img.youtube.com/vi/$videoId/hqdefault.jpg")
                if (title.isNotEmpty()) {
                    return@withContext YouTubeOEmbedMeta(
                        title = title,
                        authorName = author,
                        thumbnailUrl = thumb
                    )
                }
            }
        } catch (_: Exception) {
            // Network unavailable or restricted; caller uses fallback metadata
        } finally {
            connection?.disconnect()
        }
        null
    }

    /**
     * Validates a YouTube URL and opens it externally via Android Intent.ACTION_VIEW.
     */
    fun openYouTubeExternal(context: Context, rawLink: String): Boolean {
        val trimmed = rawLink.trim()
        val normalizedLink = if (YouTubeUrlParser.extractVideoId(trimmed) != null && !trimmed.startsWith("http")) {
            val id = YouTubeUrlParser.extractVideoId(trimmed)
            if (trimmed.contains("youtube.com") || trimmed.contains("youtu.be")) {
                "https://$trimmed"
            } else {
                "https://www.youtube.com/watch?v=$id"
            }
        } else {
            trimmed
        }

        val uri = try {
            Uri.parse(normalizedLink)
        } catch (_: Exception) {
            null
        }

        val host = uri?.host?.lowercase()
        val validHost = host == "youtube.com" ||
            host?.endsWith(".youtube.com") == true ||
            host == "youtu.be" ||
            host?.endsWith(".youtu.be") == true

        if (uri == null ||
            uri.scheme !in listOf("https", "http") ||
            !validHost
        ) {
            Toast.makeText(
                context,
                "Please enter a valid YouTube link",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No app available to open this link",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }
}
