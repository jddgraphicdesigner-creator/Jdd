package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CuratedPresets
import com.example.model.PlayItem
import com.example.player.PlayerCallback
import com.example.player.YouTubePlayerController
import com.example.util.PreferencesManager
import com.example.util.YouTubeUrlParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NavigationTab {
    NOW_PLAYING,
    DISCOVER,
    LIBRARY
}

class PlayerViewModel(application: Application) : AndroidViewModel(application), PlayerCallback {

    private val prefs = PreferencesManager(application.applicationContext)

    // Current playing video
    private val _currentVideo = MutableStateFlow<PlayItem>(
        prefs.getLastPlayedId()?.let { lastId ->
            CuratedPresets.items.find { it.id == lastId } ?: PlayItem(
                id = lastId,
                title = "Previously Played",
                originalUrl = "https://www.youtube.com/watch?v=$lastId"
            )
        } ?: CuratedPresets.items.first()
    )
    val currentVideo: StateFlow<PlayItem> = _currentVideo.asStateFlow()

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentTimeSeconds = MutableStateFlow(0f)
    val currentTimeSeconds: StateFlow<Float> = _currentTimeSeconds.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(0f)
    val totalDurationSeconds: StateFlow<Float> = _totalDurationSeconds.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode: StateFlow<Boolean> = _isInPipMode.asStateFlow()

    private val _isDimmerActive = MutableStateFlow(false)
    val isDimmerActive: StateFlow<Boolean> = _isDimmerActive.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

    private val _sleepTimerRemainingSeconds = MutableStateFlow<Int?>(null)
    val sleepTimerRemainingSeconds: StateFlow<Int?> = _sleepTimerRemainingSeconds.asStateFlow()

    private var sleepTimerJob: Job? = null

    private val _activeTab = MutableStateFlow(NavigationTab.NOW_PLAYING)
    val activeTab: StateFlow<NavigationTab> = _activeTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _urlInputText = MutableStateFlow("")
    val urlInputText: StateFlow<String> = _urlInputText.asStateFlow()

    private val _history = MutableStateFlow<List<PlayItem>>(prefs.loadHistory())
    val history: StateFlow<List<PlayItem>> = _history.asStateFlow()

    private val _favorites = MutableStateFlow<List<PlayItem>>(prefs.loadFavorites())
    val favorites: StateFlow<List<PlayItem>> = _favorites.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var playerController: YouTubePlayerController? = null

    init {
        // Add current initial item to history if history is empty
        if (_history.value.isEmpty()) {
            addToHistory(_currentVideo.value)
        }
    }

    fun setPlayerController(controller: YouTubePlayerController) {
        this.playerController = controller
    }

    fun setUrlInput(input: String) {
        _urlInputText.value = input
    }

    fun setActiveTab(tab: NavigationTab) {
        _activeTab.value = tab
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setInPipMode(inPip: Boolean) {
        _isInPipMode.value = inPip
    }

    fun toggleDimmer(active: Boolean) {
        _isDimmerActive.value = active
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun playFromInput(): Boolean {
        val input = _urlInputText.value.trim()
        if (input.isEmpty()) return false

        val videoId = YouTubeUrlParser.extractVideoId(input)
        if (videoId != null) {
            val startTime = YouTubeUrlParser.extractStartTimeSeconds(input)
            val isFav = _favorites.value.any { it.id == videoId }
            val item = PlayItem(
                id = videoId,
                title = "Playing from link...",
                originalUrl = if (input.startsWith("http")) input else "https://www.youtube.com/watch?v=$videoId",
                isFavorite = isFav
            )
            playItem(item, startTime)
            _urlInputText.value = ""
            _activeTab.value = NavigationTab.NOW_PLAYING
            _toastMessage.value = "Playing video..."
            return true
        } else {
            _toastMessage.value = "Please enter a valid YouTube link or ID"
            return false
        }
    }

    fun playItem(item: PlayItem, startSeconds: Int = 0) {
        val isFav = _favorites.value.any { it.id == item.id }
        val updated = item.copy(isFavorite = isFav)
        _currentVideo.value = updated
        prefs.setLastPlayedId(item.id)
        addToHistory(updated)
        playerController?.loadVideo(item.id, startSeconds)
        _isPlaying.value = true
        _activeTab.value = NavigationTab.NOW_PLAYING
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            playerController?.pause()
            _isPlaying.value = false
        } else {
            playerController?.play()
            _isPlaying.value = true
        }
    }

    fun seekTo(seconds: Float) {
        _currentTimeSeconds.value = seconds
        playerController?.seekTo(seconds)
    }

    fun seekRelative(deltaSeconds: Float) {
        playerController?.seekRelative(deltaSeconds)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        playerController?.setPlaybackRate(speed)
    }

    fun toggleFavoriteCurrent() {
        val current = _currentVideo.value
        toggleFavorite(current)
    }

    fun toggleFavorite(item: PlayItem) {
        val existingIndex = _favorites.value.indexOfFirst { it.id == item.id }
        val updatedList = _favorites.value.toMutableList()
        val isNowFav: Boolean
        if (existingIndex >= 0) {
            updatedList.removeAt(existingIndex)
            isNowFav = false
            _toastMessage.value = "Removed from Favorites"
        } else {
            updatedList.add(0, item.copy(isFavorite = true))
            isNowFav = true
            _toastMessage.value = "Saved to Favorites"
        }
        _favorites.value = updatedList
        prefs.saveFavorites(updatedList)

        if (_currentVideo.value.id == item.id) {
            _currentVideo.update { it.copy(isFavorite = isNowFav) }
        }
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutes.value = minutes
        val totalSeconds = minutes * 60
        _sleepTimerRemainingSeconds.value = totalSeconds
        _toastMessage.value = "Sleep timer set for $minutes minutes"

        sleepTimerJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _sleepTimerRemainingSeconds.value = remaining
            }
            // Timer expired: stop playback
            playerController?.pause()
            _isPlaying.value = false
            _sleepTimerMinutes.value = null
            _sleepTimerRemainingSeconds.value = null
            _toastMessage.value = "Sleep timer expired. Playback paused."
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerMinutes.value = null
        _sleepTimerRemainingSeconds.value = null
        _toastMessage.value = "Sleep timer cancelled"
    }

    private fun addToHistory(item: PlayItem) {
        val currentList = _history.value.toMutableList()
        currentList.removeAll { it.id == item.id }
        currentList.add(0, item)
        val trimmed = currentList.take(30)
        _history.value = trimmed
        prefs.saveHistory(trimmed)
    }

    fun removeFromHistory(item: PlayItem) {
        val currentList = _history.value.toMutableList()
        currentList.removeAll { it.id == item.id }
        _history.value = currentList
        prefs.saveHistory(currentList)
    }

    fun clearHistory() {
        _history.value = emptyList()
        prefs.saveHistory(emptyList())
        _toastMessage.value = "History cleared"
    }

    // Player Callback Implementation
    override fun onPlayerReady() {
        _isBuffering.value = false
    }

    override fun onPlaybackStateChange(isPlaying: Boolean, isBuffering: Boolean) {
        _isPlaying.value = isPlaying
        _isBuffering.value = isBuffering
    }

    override fun onVideoMetaReceived(title: String, author: String, durationSeconds: Float) {
        if (title.isNotEmpty() || author.isNotEmpty()) {
            _currentVideo.update { curr ->
                val newTitle = if (title.isNotEmpty() && (curr.title.startsWith("Playing") || curr.title.isEmpty() || curr.title == "Previously Played")) title else curr.title
                val newAuthor = if (author.isNotEmpty()) author else curr.channelName
                val updated = curr.copy(title = newTitle, channelName = newAuthor)
                // Also update in history
                val hList = _history.value.map { if (it.id == updated.id) updated else it }
                _history.value = hList
                prefs.saveHistory(hList)
                updated
            }
        }
        if (durationSeconds > 0) {
            _totalDurationSeconds.value = durationSeconds
        }
    }

    override fun onProgressUpdate(currentSeconds: Float, totalSeconds: Float) {
        _currentTimeSeconds.value = currentSeconds
        if (totalSeconds > 0) {
            _totalDurationSeconds.value = totalSeconds
        }
    }

    override fun onError(errorCode: Int) {
        _isBuffering.value = false
        _toastMessage.value = "Playback error ($errorCode)"
    }
}
