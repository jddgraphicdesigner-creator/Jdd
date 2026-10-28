package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.model.PlayItem
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bg_player_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HISTORY = "playback_history"
        private const val KEY_FAVORITES = "playback_favorites"
        private const val KEY_LAST_PLAYED_ID = "last_played_id"
        private const val KEY_AUTO_PIP = "auto_pip_enabled"
        private const val MAX_HISTORY_ITEMS = 30
    }

    fun saveHistory(items: List<PlayItem>) {
        val array = JSONArray()
        items.take(MAX_HISTORY_ITEMS).forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("channelName", item.channelName)
                put("thumbnailUrl", item.thumbnailUrl)
                put("originalUrl", item.originalUrl)
                put("duration", item.duration)
                put("addedAt", item.addedAt)
                put("category", item.category)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, array.toString()).apply()
    }

    fun loadHistory(): List<PlayItem> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<PlayItem>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PlayItem(
                        id = obj.getString("id"),
                        title = obj.optString("title", "YouTube Video"),
                        channelName = obj.optString("channelName", ""),
                        thumbnailUrl = obj.optString(
                            "thumbnailUrl",
                            "https://img.youtube.com/vi/${obj.getString("id")}/hqdefault.jpg"
                        ),
                        originalUrl = obj.optString("originalUrl", "https://youtube.com/watch?v=${obj.getString("id")}"),
                        duration = obj.optString("duration", ""),
                        addedAt = obj.optLong("addedAt", System.currentTimeMillis()),
                        category = obj.optString("category", "History")
                    )
                )
            }
        } catch (_: Exception) {
            // fallback
        }
        return list
    }

    fun saveFavorites(items: List<PlayItem>) {
        val array = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("channelName", item.channelName)
                put("thumbnailUrl", item.thumbnailUrl)
                put("originalUrl", item.originalUrl)
                put("duration", item.duration)
                put("addedAt", item.addedAt)
                put("category", item.category)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_FAVORITES, array.toString()).apply()
    }

    fun loadFavorites(): List<PlayItem> {
        val raw = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        val list = mutableListOf<PlayItem>()
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PlayItem(
                        id = obj.getString("id"),
                        title = obj.optString("title", "YouTube Video"),
                        channelName = obj.optString("channelName", ""),
                        thumbnailUrl = obj.optString(
                            "thumbnailUrl",
                            "https://img.youtube.com/vi/${obj.getString("id")}/hqdefault.jpg"
                        ),
                        originalUrl = obj.optString("originalUrl", "https://youtube.com/watch?v=${obj.getString("id")}"),
                        duration = obj.optString("duration", ""),
                        addedAt = obj.optLong("addedAt", System.currentTimeMillis()),
                        isFavorite = true,
                        category = obj.optString("category", "Favorite")
                    )
                )
            }
        } catch (_: Exception) {
            // fallback
        }
        return list
    }

    fun setLastPlayedId(id: String) {
        prefs.edit().putString(KEY_LAST_PLAYED_ID, id).apply()
    }

    fun getLastPlayedId(): String? = prefs.getString(KEY_LAST_PLAYED_ID, null)

    fun isAutoPipEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_PIP, true)

    fun setAutoPipEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PIP, enabled).apply()
    }
}
