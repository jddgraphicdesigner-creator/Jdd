package com.example.util

import java.util.regex.Pattern

object YouTubeUrlParser {

    private val videoIdPattern = Pattern.compile(
        """(?:https?://)?(?:www\.|m\.|music\.)?(?:youtube\.com/(?:watch\?.*?v=|embed/|v/|shorts/|live/)|youtu\.be/)([a-zA-Z0-9_-]{11})""",
        Pattern.CASE_INSENSITIVE
    )

    private val rawIdPattern = Pattern.compile("""^[a-zA-Z0-9_-]{11}$""")

    /**
     * Extracts YouTube video ID from a raw URL, shared text, or raw video ID.
     */
    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        // Check if raw ID was provided
        if (rawIdPattern.matcher(trimmed).matches()) {
            return trimmed
        }

        // Match against regex
        val matcher = videoIdPattern.matcher(trimmed)
        if (matcher.find()) {
            return matcher.group(1)
        }

        // Fallback search for any 11-char pattern after v= or youtu.be/
        val fallbackMatch = Regex("""(?:v=|youtu\.be/|shorts/)([a-zA-Z0-9_-]{11})""").find(trimmed)
        return fallbackMatch?.groupValues?.get(1)
    }

    /**
     * Checks if input contains a valid YouTube link or ID
     */
    fun isValidYouTubeInput(input: String): Boolean {
        return extractVideoId(input) != null
    }

    /**
     * Extracts start time in seconds if available in the url (e.g., ?t=120 or &t=2m15s or ?start=90)
     */
    fun extractStartTimeSeconds(input: String): Int {
        try {
            val tMatch = Regex("""[?&](?:t|start)=([0-9a-zA-Z]+)""").find(input)
            val tParam = tMatch?.groupValues?.get(1) ?: return 0

            val digitsOnly = Regex("""^(\d+)s?$""").find(tParam)
            if (digitsOnly != null) {
                return digitsOnly.groupValues[1].toInt()
            }

            var total = 0
            val h = Regex("""(\d+)h""").find(tParam)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val m = Regex("""(\d+)m""").find(tParam)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            val s = Regex("""(\d+)s""").find(tParam)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            total += h * 3600 + m * 60 + s
            return total
        } catch (_: Exception) {
            return 0
        }
    }
}
