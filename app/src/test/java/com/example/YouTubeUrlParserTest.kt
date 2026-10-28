package com.example

import com.example.util.YouTubeUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class YouTubeUrlParserTest {

    @Test
    fun extractVideoId_standardWatchUrl() {
        val url = "https://www.youtube.com/watch?v=jfKfPfyJRdk"
        val id = YouTubeUrlParser.extractVideoId(url)
        assertEquals("jfKfPfyJRdk", id)
    }

    @Test
    fun extractVideoId_shortenedYouTuBe() {
        val url = "https://youtu.be/4xDzrJKXOOY"
        val id = YouTubeUrlParser.extractVideoId(url)
        assertEquals("4xDzrJKXOOY", id)
    }

    @Test
    fun extractVideoId_shortsUrl() {
        val url = "https://youtube.com/shorts/WPni755-Krg?feature=share"
        val id = YouTubeUrlParser.extractVideoId(url)
        assertEquals("WPni755-Krg", id)
    }

    @Test
    fun extractVideoId_rawId() {
        val raw = "jfKfPfyJRdk"
        val id = YouTubeUrlParser.extractVideoId(raw)
        assertEquals("jfKfPfyJRdk", id)
    }

    @Test
    fun extractVideoId_withTimestamp() {
        val url = "https://youtu.be/jfKfPfyJRdk?t=120"
        val id = YouTubeUrlParser.extractVideoId(url)
        assertEquals("jfKfPfyJRdk", id)
    }

    @Test
    fun extractVideoId_invalidUrl() {
        val url = "https://example.com/not-a-youtube-link"
        val id = YouTubeUrlParser.extractVideoId(url)
        assertNull(id)
        assertFalse(YouTubeUrlParser.isValidYouTubeInput(url))
    }

    @Test
    fun extractStartTime_seconds() {
        val url = "https://www.youtube.com/watch?v=abc12345678&t=90"
        val seconds = YouTubeUrlParser.extractStartTimeSeconds(url)
        assertEquals(90, seconds)
    }
}
